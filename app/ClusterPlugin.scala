package info.schleichardt.experiments.akka.cluster

import play.api.{Logger, Plugin, Application}
import akka.actor._
import akka.cluster.ClusterEvent._
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.ClusterEvent.UnreachableMember
import akka.cluster.ClusterEvent.MemberJoined
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.{Member, Cluster}
import scala.collection.immutable.SortedSet
import akka.cluster.ClusterEvent.MemberJoined
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberLeft
import akka.cluster.ClusterEvent.MemberRemoved
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.ClusterEvent.MemberExited
import akka.cluster.ClusterEvent.MemberDowned
import akka.cluster.ClusterEvent.UnreachableMember
import controllers.SimpleMessage

class ClusterPlugin(app: Application) extends Plugin {
  lazy val system = ActorSystem("ClusterSystem")

  override def onStart() {
    Logger.info(s"starting ${this.getClass.getName}")
    val clusterListener = system.actorOf(Props[ClusterLoggingActor], "clusterLogger")
    Cluster(system).subscribe(clusterListener, classOf[ClusterDomainEvent])
    val clusterStateActor = system.actorOf(Props[ClusterStateActor], "clusterState")
    Cluster(system).subscribe(clusterStateActor, classOf[ClusterDomainEvent])
    val broadcastActor = system.actorOf(Props[BroadcastActor], "broadcast")
  }


  override def onStop() {
    super.onStop()
    Logger.info(s"stopping ${this.getClass.getName}")
    system.shutdown()
  }

  def stateActor = system.actorFor("user/clusterState")
}

//inspired from http://doc.akka.io/docs/akka/2.1.4/cluster/cluster-usage-scala.html
class ClusterLoggingActor extends Actor {
  def receive = {
    case state: CurrentClusterState ⇒
      Logger.info("Current members: " + state.members)
    case MemberJoined(member) ⇒
      Logger.info("Member joined: " + member)
    case MemberUp(member) ⇒
      Logger.info("Member is Up: " + member)
    case UnreachableMember(member) ⇒
      Logger.info("Member detected as unreachable: " + member)
    case MemberDowned(member) =>
      Logger.info("Member is down: " + member)
    case MemberExited(member) =>
      Logger.info("Member exited: " + member)
    case MemberLeft(member) =>
      Logger.info("Member left: " + member)
    case _: ClusterDomainEvent ⇒ // ignore

  }
}

case object MemberInformationQuestion
case class MemberInformation(members: SortedSet[Member])

class BroadcastActor extends Actor {
  def receive = {
    case SimpleMessage(message) => Logger.info("received broadcast message:" + message)
    case _ => Logger.warn("do not understand")
  }
}

class ClusterStateActor extends Actor {
  var members: SortedSet[Member] = SortedSet()

  def receive = {
    case state: CurrentClusterState => members = state.members
    case MemberUp(member) => members += member
    case MemberRemoved(member) => members -= member
    case MemberDowned(member) => members -= member
    case MemberInformationQuestion => sender tell (MemberInformation(members), self)
    case s: SimpleMessage => {
      members.foreach { member =>
        context.actorFor(RootActorPath(member.address) / "user" / "broadcast") tell s
      }
    }
  }
}