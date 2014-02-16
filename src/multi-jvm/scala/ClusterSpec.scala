import akka.actor.{Actor, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberUp, CurrentClusterState}
import akka.remote.testkit.{MultiNodeSpec, MultiNodeConfig}
import akka.testkit.ImplicitSender
import com.typesafe.config.ConfigFactory
import multinodetest.MultiSpec
import org.scalatest._
import language.postfixOps
import scala.concurrent.duration._

object ClusterSpecConfig extends MultiNodeConfig {
  val seed = role("controller")
  val first = role("first")
  val second = role("second")
  val third = role("third")

  commonConfig(ConfigFactory.parseString(""" akka.actor.provider="akka.cluster.ClusterActorRefProvider" """))
}

class ClusterSpecMultiJvmNode1 extends ClusterSpec
class ClusterSpecMultiJvmNode2 extends ClusterSpec
class ClusterSpecMultiJvmNode3 extends ClusterSpec
class ClusterSpecMultiJvmNode4 extends ClusterSpec

class ClusterSpec extends MultiNodeSpec(ClusterSpecConfig) with WordSpecLike with MustMatchers with MultiSpec with ImplicitSender {

  import ClusterSpecConfig._

  def initialParticipants = roles.size

  muteDeadLetters(classOf[Any])(system)

  "A cluster" must {
    "contain of all nodes" in within(10 seconds) {
      Cluster(system).subscribe(testActor, classOf[MemberUp])
      expectMsgClass(classOf[CurrentClusterState])
      Cluster(system).join(node(seed).address)
      receiveN(4).map {
        case MemberUp(m) => m.address
      }.toSet must be(
        Set(node(seed).address, node(first).address, node(second).address, node(third).address))
      Cluster(system).unsubscribe(testActor)
    }
    enterBarrier("cluster-up")
    "execute a job" in within(10 seconds) {
      runOn(first) {
        val actorRef = system.actorOf(Props[DemoActor1], "demoActor1")
        actorRef ! "hello"
        expectMsg("HELLO")
      }
      enterBarrier("job-done")
    }
  }
}

class DemoActor1 extends Actor {
  override def receive = {
    case x => sender ! x.toString.toUpperCase
  }
}