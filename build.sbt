
name := "scala-macro-experiments"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-feature")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "test"

val akkaVersion = "2.3-M2"

libraryDependencies += "com.typesafe.akka" %% "akka-contrib" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-persistence-experimental" % akkaVersion

