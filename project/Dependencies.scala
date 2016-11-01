import sbt._

object Version {
  val scala       = "2.11.8"
  val scalaBinary = scala.substring(0,4)

  val akka        = "2.4.11"
  val scalatest   = "3.0.0"
  val sigarLoader = "1.6.6-rev002"
}

object Library {
  val akkaActor            = "com.typesafe.akka" %% "akka-actor"              % Version.akka
  val akkaRemote           = "com.typesafe.akka" %% "akka-remote"             % Version.akka
  val akkaCluster          = "com.typesafe.akka" %% "akka-cluster"            % Version.akka
  val akkaClusterMetrics   = "com.typesafe.akka" %% "akka-cluster-metrics"    % Version.akka
  val akkaClusterTools     = "com.typesafe.akka" %% "akka-cluster-tools"      % Version.akka
  val akkaMultiNodeTestkit = "com.typesafe.akka" %% "akka-multi-node-testkit" % Version.akka

  val kamonSigar           = "io.kamon"           % "sigar-loader"            % Version.sigarLoader

  val scalactic            = "org.scalactic"     %% "scalactic"               % Version.scalatest
  val scalatest            = "org.scalatest"     %% "scalatest"               % Version.scalatest
}

object Dependencies {
  import Library._

  val genericTest = Seq(
    scalactic,
    scalatest % "test"
  )

  val core = Seq(
    akkaActor, akkaRemote, akkaMultiNodeTestkit,
    akkaCluster, akkaClusterMetrics, akkaClusterTools,
    kamonSigar
  ) ++ genericTest

  val dbStorageInMem = genericTest

  val dbStorageFilePerKey = genericTest

  val merkleTrees = genericTest

  val vectorClocks = genericTest

  val consistentHashing = genericTest
}
