package sample.cluster.counter

import scala.concurrent.duration._
import com.typesafe.config.{Config, ConfigFactory}
import akka.actor._
import akka.contrib.pattern.ClusterSharding
import akka.persistence.journal.leveldb.SharedLeveldbStore
import akka.pattern.ask
import akka.util.Timeout
import akka.persistence.journal.leveldb.SharedLeveldbJournal
import akka.actor.ActorIdentity
import scala.Some
import akka.actor.Identify
import sample.cluster.counter.Bot.Tick

object ShardingApp {

  def startup(port: String, system: ActorSystem): Unit = {
    startupSharedJournal(system, startStore = (port == "2551"), path =
      ActorPath.fromString("akka.tcp://ClusterShardingSpec@localhost:2551/user/store"))

    ClusterSharding(system).start(
      typeName = "Counter",
      entryProps = Some(Props[Counter]),
      idExtractor = Counter.idExtractor,
      shardResolver = Counter.shardResolver)

    ClusterSharding(system).start(
      typeName = "AnotherCounter",
      entryProps = Some(Props[Counter]),
      idExtractor = Counter.idExtractor,
      shardResolver = Counter.shardResolver)

    val bot: ActorRef = system.actorOf(Props[Bot], "bot")
  }

  def startupSharedJournal(system: ActorSystem, startStore: Boolean, path: ActorPath): Unit = {
    // Start the shared journal one one node (don't crash this SPOF)
    // This will not be needed with a distributed journal
    if (startStore)
      system.actorOf(Props[SharedLeveldbStore], "store")
    // register the shared journal
    import system.dispatcher
    implicit val timeout = Timeout(1.minute)
    val f = (system.actorSelection(path) ? Identify(None))
    f.onSuccess {
      case ActorIdentity(_, Some(ref)) => SharedLeveldbJournal.setStore(ref, system)
      case _ =>
        system.log.error("Shared journal not started at {}", path)
        system.shutdown()
    }
    f.onFailure {
      case _ =>
        system.log.error("Lookup of shared journal at {} timed out", path)
        system.shutdown()
    }
  }

}


