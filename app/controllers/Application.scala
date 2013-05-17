package controllers

import info.schleichardt.experiments.akka.cluster.{MemberInformation, MemberInformationQuestion, ClusterPlugin}
import play.api._
import play.api.mvc._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.data._
import play.api.data.Forms._

case class SimpleMessage(message: String)

object Application extends Controller {

  val simpleMessageForm = Form(mapping("message" -> nonEmptyText)(SimpleMessage.apply)(SimpleMessage.unapply))

  def index = Action {
    implicit val timeout = Timeout(500)
    val clusterInformation = (stateActor ask MemberInformationQuestion).mapTo[MemberInformation]
    AsyncResult {
      clusterInformation map { c =>
        Ok(views.html.index(c))
      }
    }
  }

  private def stateActor = Play.current.plugin(classOf[ClusterPlugin]).get.stateActor

  def broadcastMessage = Action {
    Ok(views.html.broadcast(simpleMessageForm))
  }

  def sendBroadcastMessage = Action { implicit request =>
    simpleMessageForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.broadcast(formWithErrors)),
      value => {
        stateActor tell (value, stateActor)
        Redirect(routes.Application.broadcastMessage)
      }
    )
  }
}