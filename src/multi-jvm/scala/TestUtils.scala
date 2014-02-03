package multinodetest

import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.{Suite, BeforeAndAfterAll}
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import akka.actor.{ActorSelection, Props, Actor}
import akka.remote.testkit.MultiNodeConfig
import org.scalatest._
import akka.remote.testkit.MultiNodeSpecCallbacks
import com.typesafe.config.ConfigFactory

trait MultiSpec extends MultiNodeSpecCallbacks with BeforeAndAfterAll {
  this: Suite =>

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()
}

trait MyMultiNodeSpec {

}
