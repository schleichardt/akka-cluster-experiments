package info.schleichardt.experiments.akka.cluster

import play.api.{Logger, Plugin, Application}
import akka.actor.{Props, ActorLogging, Actor, ActorSystem}
import akka.cluster.ClusterEvent._
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.ClusterEvent.UnreachableMember
import akka.cluster.ClusterEvent.MemberJoined
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.Cluster

class ClusterInitializationPlugin(app: Application) extends Plugin {
  lazy val system = ActorSystem("ClusterSystem")

  override def onStart() {
    Logger.info(s"starting ${this.getClass.getName}")
    val clusterListener = system.actorOf(Props[ClusterLoggingActor], "clusterLogger")
    Cluster(system).subscribe(clusterListener, classOf[ClusterDomainEvent])
  }
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
    case _: ClusterDomainEvent ⇒ // ignore

  }
}