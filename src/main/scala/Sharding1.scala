package sharding

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import akka.contrib.pattern.ShardRegion

case class WhoReceivedWhat(who: ActorRef, what: Any)

class EchoActor extends Actor with ActorLogging {
  println("created EchoActor on " + self.path.address)

  def receive: Actor.Receive = {
    case what =>
      log.error("received " + what)
      sender ! WhoReceivedWhat(self, what)
  }
}

object EchoActor {
  def props = Props[EchoActor]
  case class Message(to: String, payload: Any) extends Recipient
}


trait Recipient {
  def to: String
}

object OnceScenario {

  val idExtractor: ShardRegion.IdExtractor = {


    case x: Recipient =>
      println("idExtractor for " + x)
      (x.to, x)

    case x =>
    println(s"not found for $x idExtractor")
      (x.toString, x)
  }

  val shardResolver: ShardRegion.ShardResolver = msg => msg match {
    case x: Recipient =>
      val shard: String = (x.to.hashCode % 13).toString
      println("shardResolver for " + x + " = " + shard)
      shard
  }
}


object X extends App {
  println("found")
}