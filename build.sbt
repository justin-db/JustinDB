import scalariform.formatter.preferences._

name          := "JustinDB"
organization  := "com.github.justindb"
version       := "0.0.1"
scalaVersion  := "2.11.6"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val scalazV          = "7.2.0-M1"
  val akkaV            = "2.3.11"
  val scalaTestV       = "3.0.0-M1"
  val scalaMockV       = "3.2.2"
  val scalazScalaTestV = "0.2.3"

  Seq(
    "com.typesafe.akka" %% "akka-actor"                  % akkaV,
    "com.typesafe.akka" %% "akka-cluster"                % akkaV,
    "com.typesafe.akka" %% "akka-slf4j"                  % akkaV,
    "org.scalaz"        %% "scalaz-core"                 % scalazV,
    "org.scalatest"     %% "scalatest"                   % scalaTestV       % "it,test",
    "org.scalamock"     %% "scalamock-scalatest-support" % scalaMockV       % "it,test",
    "org.scalaz"        %% "scalaz-scalacheck-binding"   % scalazV          % "it,test",
    "org.typelevel"     %% "scalaz-scalatest"            % scalazScalaTestV % "it,test",
    "com.typesafe.akka" %% "akka-actor-tests"            % akkaV            % "it,test"
  )
}

lazy val root = project.in(file(".")).configs(IntegrationTest)
Defaults.itSettings
scalariformSettings
Revolver.settings
enablePlugins(JavaAppPackaging)

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)

initialCommands := """|import scalaz._
                      |import Scalaz._
                      |import akka.actor._
                      |import akka.pattern._
                      |import akka.util._
                      |import scala.concurrent._
                      |import scala.concurrent.duration._""".stripMargin
