//from https://raw2.github.com/akka/akka/master/akka-contrib/src/multi-jvm/scala/akka/contrib/pattern/ClusterShardingSpec.scala

/**
 *  Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package sharding

import language.postfixOps
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.persistence.EventsourcedProcessor
import akka.persistence.Persistence
import akka.persistence.journal.leveldb.SharedLeveldbJournal
import akka.persistence.journal.leveldb.SharedLeveldbStore
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeConfig
import akka.remote.testkit.MultiNodeSpec
import akka.testkit._
import akka.testkit.TestEvent.Mute
import java.io.File
import org.scalatest.{Matchers, FunSuiteLike}
import akka.contrib.pattern._
import akka.remote.testconductor.RoleName
import scala.Some
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.ClusterEvent.CurrentClusterState

object ClusterShardingSpec extends MultiNodeConfig {
  val controller = role("controller")
  val first = role("first")
  val second = role("second")
  val third = role("third")

  commonConfig(ConfigFactory.parseString("""
    akka.loglevel = INFO
    akka.actor.provider = "akka.cluster.ClusterActorRefProvider"
    akka.remote.log-remote-lifecycle-events = off
    akka.cluster.auto-down-unreachable-after = 0s
    akka.cluster.min-nr-of-members = 1
    akka.persistence.journal.plugin = "akka.persistence.journal.leveldb-shared"
    akka.persistence.journal.leveldb-shared.store {
      native = off
      dir = "target/shared-journal"
    }
    akka.persistence.snapshot-store.local.dir = "target/snapshots"
    akka.contrib.cluster.sharding {
      retry-interval = 1 s
      handoff-timeout = 10 s
      rebalance-interval = 2 s
      least-shard-allocation-strategy {
        rebalance-threshold = 2
        max-simultaneous-rebalance = 1
      }
    }  
                                         """))

  //#counter-actor
  case object Increment
  case object Decrement
  case class Get(counterId: Long)
  case class EntryEnvelope(id: Long, payload: Any)

  case object Stop
  case class CounterChanged(delta: Int)

  class Counter extends EventsourcedProcessor {
    import ShardRegion.Passivate

    context.setReceiveTimeout(120.seconds)

    var count = 0
    //#counter-actor

    override def postStop(): Unit = {
      super.postStop()
      // Simulate that the passivation takes some time, to verify passivation bufffering
      Thread.sleep(500)
    }
    //#counter-actor

    def updateState(event: CounterChanged): Unit =
      count += event.delta

    override def receiveRecover: Receive = {
      case evt: CounterChanged ⇒ updateState(evt)
    }

    override def receiveCommand: Receive = {
      case Increment      ⇒ persist(CounterChanged(+1))(updateState)
      case Decrement      ⇒ persist(CounterChanged(-1))(updateState)
      case Get(_)         ⇒
        throw new Exception("in GET")
        sender ! count
      case ReceiveTimeout ⇒ context.parent ! Passivate(stopMessage = Stop)
      case Stop           ⇒ context.stop(self)
    }
  }
  //#counter-actor

  //#counter-extractor
  val idExtractor: ShardRegion.IdExtractor = {
    case EntryEnvelope(id, payload) ⇒ (id.toString, payload)
    case msg @ Get(id)              ⇒ (id.toString, msg)
  }

  val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case EntryEnvelope(id, _) ⇒ (id % 10).toString
    case Get(id)              ⇒ (id % 10).toString
  }
  //#counter-extractor

}

class ClusterShardingMultiJvmNode1 extends ClusterShardingSpec
class ClusterShardingMultiJvmNode2 extends ClusterShardingSpec
class ClusterShardingMultiJvmNode3 extends ClusterShardingSpec
class ClusterShardingMultiJvmNode4 extends ClusterShardingSpec

class ClusterShardingSpec extends MultiNodeSpec(ClusterShardingSpec) with FunSuiteLike with ImplicitSender with Matchers {
  import ClusterShardingSpec._


  override def initialParticipants = roles.size

  override protected def atStartup() {
  }

  override protected def afterTermination() {
  }

  def join(from: RoleName, to: RoleName): Unit = {
    runOn(from) {
      Cluster(system) join node(to).address
      createCoordinator()
    }
    enterBarrier(from.name + "-joined")
  }

  def createCoordinator(): Unit = {
    val allocationStrategy = new ShardCoordinator.LeastShardAllocationStrategy(rebalanceThreshold = 2, maxSimultaneousRebalance = 1)
    system.actorOf(ClusterSingletonManager.props(
      singletonProps = ShardCoordinator.props(handOffTimeout = 10.second, rebalanceInterval = 2.seconds,
        snapshotInterval = 3600.seconds, allocationStrategy),
      singletonName = "singleton",
      terminationMessage = PoisonPill,
      role = None),
      name = "counterCoordinator")
  }

  lazy val region = system.actorOf(ShardRegion.props(
    entryProps = Props[Counter],
    role = None,
    coordinatorPath = "/user/counterCoordinator/singleton",
    retryInterval = 1.second,
    bufferSize = 1000,
    idExtractor = idExtractor,
    shardResolver = shardResolver),
    name = "counterRegion")


  def xtest(s: String)(f: => Unit) =()


  class Stats extends Actor {
    def receive: Actor.Receive = {
      case x => println("received" + x)
    }
  }

  test("illustrate how to startup cluster") {
    runOn(controller){
      enterBarrier("cluster")
    }
    runOn(first, second, third){
      Cluster(system).subscribe(testActor, classOf[MemberUp])
      expectMsgClass(classOf[CurrentClusterState])
      enterBarrier("cluster")


      val firstAddress = node(first).address
      val secondAddress = node(second).address
      val thirdAddress = node(third).address

      Cluster(system) join firstAddress

      system.actorOf(Props[Counter], "statsWorker")
      system.actorOf(Props[Counter], "statsService")

      receiveN(3).collect { case MemberUp(m) => m.address }.toSet should be(
        Set(firstAddress, secondAddress, thirdAddress))

      Cluster(system).unsubscribe(testActor)

    }
    testConductor.enter("all-up")
  }



  //  test("Cluster sharding"){
  //    val cluster = Cluster(system)
  //    val x = system.actorOf(Props[Counter], "xc")
  //    enterBarrier("cluster val created")
  //    runOn(first){
  //      val to = second
  //      Cluster(system) join node(to).address
  //      enterBarrier("join")
  //      val service = system.actorSelection(node(to) / "user" / "xc")
  //      service ! Get(100)
  //      expectMsg(0)
  //    }
  //    runOn(controller, second, third, fourth, fifth, sixth){
  //      enterBarrier("join")
  //    }
  //    enterBarrier("fin")
  //  }

  //  xtest("Cluster sharding") {
  //
  //    runOn(third, fourth, fifth, sixth) {
  //      ClusterSharding(system).start(typeName = "Counter", entryProps = Some(Props[Counter]), idExtractor = idExtractor, shardResolver = shardResolver)
  //    }
  //    Seq(fourth/*, fifth, sixth*/).foreach{ node =>
  //      join(third, node)
  //    }
  //    enterBarrier("extension-started")
  //    runOn(third) {
  //      val counterRegion: ActorRef = ClusterSharding(system).shardRegion("Counter")
  //      counterRegion ! Get(100)
  //      //TODO new
  //      region ! Get(100)
  //      expectMsg(0)
  ////
  ////      counterRegion ! EntryEnvelope(100, Increment)
  ////      counterRegion ! Get(100)
  ////      expectMsg(1)
  //    }
  ////
  ////    enterBarrier("extension-used")
  ////
  ////    // sixth is a frontend node, i.e. proxy only
  ////    runOn(sixth) {
  ////      for (n ← 1000 to 1010) {
  ////        ClusterSharding(system).shardRegion("Counter") ! EntryEnvelope(n, Increment)
  ////        ClusterSharding(system).shardRegion("Counter") ! Get(n)
  ////        expectMsg(1)
  //////        lastSender.path.address should not be (Cluster(system).selfAddress)
  ////      }
  ////    }
  //
  ////    enterBarrier("after-9")
  //  }
}
