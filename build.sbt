import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.{ MultiJvm, jvmOptions }

name := "akka-cluster-experiments"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-feature")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "compile,test"

val akkaVersion = "2.3-M2"

libraryDependencies += "com.typesafe.akka" %% "akka-contrib" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-persistence-experimental" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "compile,test"

libraryDependencies += "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % "compile,test"

//see http://doc.akka.io/docs/akka/2.3-M2/dev/multi-jvm-testing.html#multi-jvm-testing
lazy val multiJvmSettingsForThisProject = SbtMultiJvm.multiJvmSettings ++ Seq(
  // make sure that MultiJvm test are compiled by the default test compilation
  compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
  // disable parallel tests
  parallelExecution in Test := false,
  // make sure that MultiJvm tests are executed by the default test target
  executeTests in Test <<=
    ((executeTests in Test), (executeTests in MultiJvm)) map {
      case ((testResults), (multiJvmResults)) =>
        val overall =
          if (testResults.overall.id < multiJvmResults.overall.id)
            multiJvmResults.overall
          else
            testResults.overall
        Tests.Output(overall,
          testResults.events ++ multiJvmResults.events,
          testResults.summaries ++ multiJvmResults.summaries)
    }
)

multiJvmSettingsForThisProject

jvmOptions in MultiJvm := Seq("-Dmultinode.max-nodes=4")