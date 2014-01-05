package multinodetest

import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.{Suite, BeforeAndAfterAll}

trait MultiSpec extends MultiNodeSpecCallbacks with BeforeAndAfterAll {
  this: Suite =>

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()
}