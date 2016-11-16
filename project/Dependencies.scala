import sbt._

object Version {
  val scala       = "2.11.8"
  val scalaBinary = scala.substring(0,4)

  val akka        = "2.4.11"
  val scalatest   = "3.0.1"
  val scalacheck  = "1.13.4"
  val sigarLoader = "1.6.6"
}

object Library {
  val akkaActor            = "com.typesafe.akka" %% "akka-actor"                        % Version.akka
  val akkaSfl4j            = "com.typesafe.akka" %% "akka-slf4j"                        % Version.akka
  val akkaStream           = "com.typesafe.akka" %% "akka-stream"                       % Version.akka

  // http
  val akkaHttpCore         = "com.typesafe.akka" %% "akka-http-core"                    % Version.akka
  val akkaHttp             = "com.typesafe.akka" %% "akka-http-experimental"            % Version.akka
  val akkaSprayJson        = "com.typesafe.akka" %% "akka-http-spray-json-experimental" % Version.akka
  val akkaHttpTestkit      = "com.typesafe.akka" %% "akka-http-testkit"                 % Version.akka

  // cluster
  val akkaRemote           = "com.typesafe.akka" %% "akka-remote"                       % Version.akka
  val akkaCluster          = "com.typesafe.akka" %% "akka-cluster"                      % Version.akka
  val akkaClusterMetrics   = "com.typesafe.akka" %% "akka-cluster-metrics"              % Version.akka
  val akkaClusterTools     = "com.typesafe.akka" %% "akka-cluster-tools"                % Version.akka
  val akkaMultiNodeTestkit = "com.typesafe.akka" %% "akka-multi-node-testkit"           % Version.akka

  val kamonSigar           = "io.kamon"           % "sigar-loader"                      % Version.sigarLoader

  val scalactic            = "org.scalactic"     %% "scalactic"                         % Version.scalatest
  val scalatest            = "org.scalatest"     %% "scalatest"                         % Version.scalatest
  val scalacheck           = "org.scalacheck"    %% "scalacheck"                        % Version.scalacheck
}

object Dependencies {
  import Library._

  private val genericTest = Seq(scalactic, scalatest % "test")

  private val akkaCommon = Seq(akkaActor, akkaSfl4j)
  private val akkaHttpCommon = Seq(akkaHttpCore, akkaHttp, akkaSprayJson, akkaHttpTestkit)
  private val akkaClusterCommon = Seq(akkaRemote, akkaMultiNodeTestkit, akkaCluster, akkaClusterMetrics, akkaClusterTools, kamonSigar)

  val core = akkaCommon ++ akkaClusterCommon ++ genericTest ++ Seq(scalacheck % "test")

  val httpClient = akkaCommon ++ Seq(akkaStream) ++ akkaHttpCommon ++ genericTest

  val dbStorageInMem = genericTest

  val dbStorageFilePerKey = genericTest

  val merkleTrees = genericTest

  val vectorClocks = genericTest
}
