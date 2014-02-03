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
import sample.cluster.counter.ShardingApp

object ClusterShardingSpec extends MultiNodeConfig {
  val controller = role("controller")
  val first = role("first")
  val second = role("second")
  val third = role("third")

  commonConfig(ConfigFactory.parseString(
    """
      |akka {
      |   actor {
      |     provider = "akka.cluster.ClusterActorRefProvider"
      |   }
      |  cluster {
      |    seed-nodes = [
      |      "akka.tcp://ClusterShardingSpec@localhost:2551",
      |      "akka.tcp://ClusterShardingSpec@localhost:2552"]
      |
      |    auto-down-unreachable-after = 10s
      |  }
      |  persistence {
      |    journal.plugin = "akka.persistence.journal.leveldb-shared"
      |    journal.leveldb-shared.store {
      |      native = off
      |      dir = "target/shared-journal"
      |    }
      |    snapshot-store.local.dir = "target/snapshots"
      |  }
      |}
      |
    """.stripMargin))

  def nodeConf(port: String) = ConfigFactory.parseString( s"akka.remote.netty.tcp.port=$port")

  nodeConfig(first)(nodeConf("2551"))
  nodeConfig(second)(nodeConf("2552"))
  nodeConfig(third)(nodeConf("2553"))
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

  class Stats extends Actor {
    def receive: Actor.Receive = {
      case x => println("received" + x)
    }
  }

  test("startup cluster") {
    runOn(controller){
      enterBarrier("store started")
      enterBarrier("started")
    }
    runOn(first){
      ShardingApp.startup("2551", system)
      enterBarrier("store started")
      enterBarrier("started")
    }
    runOn(second){
      enterBarrier("store started")
      ShardingApp.startup("2552", system)
      enterBarrier("started")
      println("members " + Cluster(system).state.members)
    }
    runOn(third){
      enterBarrier("store started")
      ShardingApp.startup("2553", system)
      enterBarrier("started")
      println("members " + Cluster(system).state.members)
    }
  }

//  test("illustrate how to startup cluster") {
//    runOn(controller){
//      enterBarrier("cluster")
//    }
//    runOn(first, second, third){
//      Cluster(system).subscribe(testActor, classOf[MemberUp])
//      expectMsgClass(classOf[CurrentClusterState])
//      enterBarrier("cluster")
//
//
//      val firstAddress = node(first).address
//      val secondAddress = node(second).address
//      val thirdAddress = node(third).address
//
//      Cluster(system) join firstAddress
//
//      system.actorOf(Props[Counter], "statsWorker")
//      system.actorOf(Props[Counter], "statsService")
//
//      receiveN(3).collect { case MemberUp(m) => m.address }.toSet should be(
//        Set(firstAddress, secondAddress, thirdAddress))
//
//      Cluster(system).unsubscribe(testActor)
//
//    }
//    testConductor.enter("all-up")
//  }
}
