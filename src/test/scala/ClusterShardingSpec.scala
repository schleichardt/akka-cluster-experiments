import org.scalatest.{Matchers, FunSuite}

/**
 * See http://doc.akka.io/docs/akka/2.3-M2/contrib/cluster-sharding.html
 */
class ClusterShardingSpec extends FunSuite with Matchers {

  test("distribute Actors to different cluster nodes and don't have to worry about physical location")(pending)
  test("change location of an Actor over time")(pending)//stateful actors!
  test("Proxy Only Mode")(pending)
  test("Passivation")(pending)
}