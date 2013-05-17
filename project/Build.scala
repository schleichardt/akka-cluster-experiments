import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "akka-cluster-experiments"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.typesafe.akka" %% "akka-cluster-experimental" % "2.1.4"
    , "junit" % "junit-dep" % "4.11" % "test"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
