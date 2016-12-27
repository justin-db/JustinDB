import sbt._

object Version {
  val scala       = "2.12.1"
  val scalaBinary = scala.substring(0,4)

  val akka        = "2.4.14"
  val akkaHttp    = "10.0.0"
  val scalatest   = "3.0.1"
  val scalacheck  = "1.13.4"
  val sigarLoader = "1.6.6"
  val scopt       = "3.5.0"
  val crjdt       = "0.0.6"
  val logback     = "1.1.8"
}

object Library {
  val akkaActor            = "com.typesafe.akka" %% "akka-actor"                        % Version.akka
  val akkaSfl4j            = "com.typesafe.akka" %% "akka-slf4j"                        % Version.akka
  val akkaTestkit          = "com.typesafe.akka" %% "akka-testkit"                      % Version.akka

  // http
  val akkaHttp             = "com.typesafe.akka" %% "akka-http"                         % Version.akkaHttp
  val akkaHttpSprayJson    = "com.typesafe.akka" %% "akka-http-spray-json"              % Version.akkaHttp
  val akkaHttpTestkit      = "com.typesafe.akka" %% "akka-http-testkit"                 % Version.akkaHttp
  val akkaStream           = "com.typesafe.akka" %% "akka-stream"                       % Version.akka

  // cluster
  val akkaRemote           = "com.typesafe.akka" %% "akka-remote"                       % Version.akka
  val akkaCluster          = "com.typesafe.akka" %% "akka-cluster"                      % Version.akka
  val akkaClusterMetrics   = "com.typesafe.akka" %% "akka-cluster-metrics"              % Version.akka
  val akkaClusterTools     = "com.typesafe.akka" %% "akka-cluster-tools"                % Version.akka
  val akkaMultiNodeTestkit = "com.typesafe.akka" %% "akka-multi-node-testkit"           % Version.akka

  // test
  val scalactic            = "org.scalactic"     %% "scalactic"                         % Version.scalatest
  val scalatest            = "org.scalatest"     %% "scalatest"                         % Version.scalatest
  val scalacheck           = "org.scalacheck"    %% "scalacheck"                        % Version.scalacheck

  // other
  val kamonSigar           = "io.kamon"           % "sigar-loader"                      % Version.sigarLoader
  val scopt                = "com.github.scopt"  %% "scopt"                             % Version.scopt
  val logback              = "ch.qos.logback"     % "logback-classic"                   % Version.logback

  // crjdt
  val crjdtCore            = "eu.timepit"        %% "crjdt-core"                        % Version.crjdt
  val crjdtCirce           = "eu.timepit"        %% "crjdt-circe"                       % Version.crjdt
}

object Dependencies {
  import Library._

  private val genericTest = Seq(scalactic, scalatest % "test")

  private val akkaCommon        = Seq(akkaActor, akkaSfl4j, akkaTestkit)
  private val akkaHttpCommon    = Seq(akkaHttp, akkaHttpSprayJson, akkaHttpTestkit, akkaStream)
  private val akkaClusterCommon = Seq(akkaRemote, akkaMultiNodeTestkit, akkaCluster, akkaClusterMetrics, akkaClusterTools, kamonSigar)

  val core = akkaCommon ++ akkaClusterCommon ++ genericTest ++ Seq(scalacheck % "test", logback) ++ Seq(akkaHttpSprayJson)

  val httpClient = akkaCommon ++ akkaHttpCommon ++ genericTest ++ Seq(scopt)

  val dbStorageInMem = genericTest

  val dbStorageFilePerKey = genericTest

  val merkleTrees = genericTest

  val vectorClocks = genericTest

  val crdts = genericTest ++ Seq(crjdtCirce, crjdtCirce)

  val consistenHashing = genericTest
}
