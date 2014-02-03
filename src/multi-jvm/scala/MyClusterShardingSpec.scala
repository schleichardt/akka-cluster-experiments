package sharding

import akka.remote.testkit.MultiNodeSpec
import multinodetest.MultiSpec
import akka.testkit.ImplicitSender
import akka.actor._
import akka.remote.testkit.MultiNodeConfig
import org.scalatest._
import akka.remote.testkit.MultiNodeSpecCallbacks
import com.typesafe.config.{Config, ConfigFactory}
import akka.contrib.pattern.{ClusterSingletonManager, ShardCoordinator, ShardRegion, ClusterSharding}
import akka.remote.testconductor.RoleName
import akka.cluster.Cluster
import scala.concurrent.duration._
import akka.remote.testconductor.RoleName
import scala.Some
//
///**
// * See http://doc.akka.io/docs/akka/2.3-M2/contrib/cluster-sharding.html
// */
//class ClusterShardingSpec extends MultiNodeSpec(NodeConfig) with FunSuiteLike with MultiSpec with ImplicitSender {
//  def initialParticipants = roles.size
//
//  //from https://github.com/akka/akka/blob/master/akka-contrib/src/multi-jvm/scala/akka/contrib/pattern/ClusterShardingSpec.scala
//  def join(from: RoleName, to: RoleName): Unit = {
//    runOn(from) {
//      Cluster(system) join node(to).address
//      createCoordinator()
//    }
//    enterBarrier(from.name + "-joined")
//  }
//
//  //from https://github.com/akka/akka/blob/master/akka-contrib/src/multi-jvm/scala/akka/contrib/pattern/ClusterShardingSpec.scala
//  def createCoordinator(): Unit = {
//    val allocationStrategy = new ShardCoordinator.LeastShardAllocationStrategy(rebalanceThreshold = 2, maxSimultaneousRebalance = 1)
//    system.actorOf(ClusterSingletonManager.props(
//      singletonProps = ShardCoordinator.props(handOffTimeout = 10.second, rebalanceInterval = 2.seconds,
//        snapshotInterval = 3600.seconds, allocationStrategy),
//      singletonName = "singleton",
//      terminationMessage = PoisonPill,
//      role = None),
//      name = "counterCoordinator")
//  }
//
//  test("actor exists only in one system"){
//    import NodeConfig._
//    val sharding: ClusterSharding = ClusterSharding(system)
//    sharding.start(typeName = "Echo", entryProps = Some(EchoActor.props),
//      idExtractor = OnceScenario.idExtractor, shardResolver = OnceScenario.shardResolver)
//    enterBarrier("startup")
//    Seq(node1, node2, node3).foreach(node => join(node, node1))
//    val echoRegion: ActorRef = sharding.shardRegion("Echo")
//    echoRegion ! EchoActor.Message("bla", "hello")
//    //    enterBarrier("send")
//    expectMsg("fi")
//  }
//
//
//  test("distribute Actors to different cluster nodes and don't have to worry about physical location")(pending)
//  test("change location of an Actor over time")(pending)//stateful actors!
//  test("Proxy Only Mode")(pending)
//  test("Passivation")(pending)
//}
//
//object NodeConfig extends MultiNodeConfig {
//  val node1 = role("node1")
//  val node2 = role("node2")
//  val node3 = role("node3")
//  val node4 = role("node4")
//  //from https://github.com/akka/akka/blob/master/akka-contrib/src/multi-jvm/scala/akka/contrib/pattern/ClusterShardingSpec.scala
//  commonConfig(ConfigFactory.parseString("""
//    akka.actor.provider = "akka.cluster.ClusterActorRefProvider"
//                                         """))
//}
//
//class ClusterShardingSpecMultiJvmNode1 extends ClusterShardingSpec
//class ClusterShardingSpecMultiJvmNode2 extends ClusterShardingSpec
//class ClusterShardingSpecMultiJvmNode3 extends ClusterShardingSpec
//class ClusterShardingSpecMultiJvmNode4 extends ClusterShardingSpec