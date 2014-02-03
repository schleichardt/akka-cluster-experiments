1. akka/akka-contrib/src/multi-jvm/scala/akka/contrib/pattern/ClusterShardingSpec.scala
1. http://doc.akka.io/docs/akka/2.3-M2/contrib/cluster-sharding.html
1. http://doc.akka.io/docs/akka/2.3-M2/dev/multi-jvm-testing.html
1. http://doc.akka.io/docs/akka/2.3-M2/dev/multi-node-testing.html
1. http://doc.akka.io/docs/akka/snapshot/scala/cluster-usage.html#How_to_Test

## Notes
* https://github.com/akka/akka/blob/master/akka-contrib/src/multi-jvm/scala/akka/contrib/pattern/ClusterSingletonManagerSpec.scala
* root/src/multi-jvm/scala instead of root/src/main/multi-jvm
* how to eliminate dead letter warnings by shuting down correctly?
* ```sbt "~multi-jvm:test-only sharding.ClusterShardingSpec"```
* ClusterSharding(system).start(typeName = "xyz" [...]  creates some kind of collection of actors (all shards)
    * it is not possible to send directly a message to an actor of this collection, but the messages
      are passed to the application specific idExtractor that retrieves the ID from the message
    * the ShardResolver uses the message to find the shard
    * ShardResolver => finds the ActorSystem, idExtractor can be used to find the actor inside the ActorSystem?
* an ActorSystem can have multiple ShardRegiens distinguished by typeName
