//package multinodetest
//
//import akka.remote.testkit.MultiNodeSpec
//import akka.testkit.ImplicitSender
//import akka.actor.{ActorSelection, Props, Actor}
//import akka.remote.testkit.MultiNodeConfig
//import org.scalatest._
//import akka.remote.testkit.MultiNodeSpecCallbacks
//import com.typesafe.config.ConfigFactory
//
//class MultiNodeSample extends MultiNodeSpec(MultiNodeSampleConfig) with FunSuiteLike with MultiSpec with ImplicitSender {
//
//  import MultiNodeSampleConfig._
//
//  def initialParticipants = roles.size
//
//  test("all nodes wait for one or more barriers"){
//    //without runOn(node) it is on all nodes
//    enterBarrier("startup", "initialization")
//    enterBarrier("finished")
//  }
//
//  test("exchange messages between nodes"){
//    runOn(node1) {
//      system.actorOf(Twice.props, "twicer")
//      enterBarrier("twicer initialized")
//    }
//    runOn(node2, node3) {
//      enterBarrier("twicer initialized")
//      val remoteActor = system.actorSelection(node(node1) / "user" / "twicer")
//      remoteActor ! 5
//      expectMsg(10)
//      system.shutdown()
//    }
//  }
//}
//
//object MultiNodeSampleConfig extends MultiNodeConfig {
//  val node1 = role("node1")
//  val node2 = role("node2")
//  val node3 = role("node3") //if you add here something, don't forget to add the MultiNodeSampleSpecMultiJvmNode3 class
//}
//
//class MultiNodeSampleSpecMultiJvmNode1 extends MultiNodeSample
//class MultiNodeSampleSpecMultiJvmNode2 extends MultiNodeSample
//class MultiNodeSampleSpecMultiJvmNode3 extends MultiNodeSample
//
//class Twice extends Actor {
//  def receive = {
//    case i: Int => sender ! i * 2
//  }
//}
//object Twice {
//  def props = Props(new Twice)
//}