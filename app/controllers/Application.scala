package controllers

import info.schleichardt.experiments.akka.cluster.{MemberInformation, MemberInformationQuestion, ClusterInitializationPlugin}
import play.api._
import play.api.mvc._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent._
import ExecutionContext.Implicits.global

object Application extends Controller {
  
  def index = Action {
    implicit val timeout = Timeout(500)
    val stateActor = Play.current.plugin(classOf[ClusterInitializationPlugin]).get.stateActor
    val clusterInformation = (stateActor ask MemberInformationQuestion).mapTo[MemberInformation]
    AsyncResult {
      clusterInformation map { c =>
        Ok(views.html.index(c))
      }
    }
  }

}