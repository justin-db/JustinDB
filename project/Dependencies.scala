import sbt._

object Version {
  val scala        = "2.12.3"
  val scalaBinary  = scala.substring(0,4)

  val akka               = "2.5.6"
  val akkaHttp           = "10.0.10"
  val akkaClusterManager = "0.5"
  val akkaKryo           = "0.5.2"
  val kryo               = "4.0.0"
  val scalatest          = "3.0.4"
  val scalacheck         = "1.13.5"
  val sigarLoader        = "1.6.6"
  val logback            = "1.2.3"
  val scalaLogging       = "3.7.2"
  val constructR         = "0.17.0"
  val configAnnotation   = "0.3.7"
  val macroParadise      = "2.1.1"
  val rocksDB            = "5.5.1"
  val dockerItScala      = "0.9.6"
}

object Library {
  val akkaActor            = "com.typesafe.akka"     %% "akka-actor"                    % Version.akka
  val akkaTestkit          = "com.typesafe.akka"     %% "akka-testkit"                  % Version.akka
  val akkaRemote           = "com.typesafe.akka"     %% "akka-remote"                   % Version.akka
  val akkaCluster          = "com.typesafe.akka"     %% "akka-cluster"                  % Version.akka
  val akkaClusterMetrics   = "com.typesafe.akka"     %% "akka-cluster-metrics"          % Version.akka
  val akkaClusterTools     = "com.typesafe.akka"     %% "akka-cluster-tools"            % Version.akka
  val akkaStream           = "com.typesafe.akka"     %% "akka-stream"                   % Version.akka
  val akkaMultiNodeTestkit = "com.typesafe.akka"     %% "akka-multi-node-testkit"       % Version.akka
  val akkaHttp             = "com.typesafe.akka"     %% "akka-http"                     % Version.akkaHttp
  val akkaHttpSprayJson    = "com.typesafe.akka"     %% "akka-http-spray-json"          % Version.akkaHttp
  val akkaHttpTestkit      = "com.typesafe.akka"     %% "akka-http-testkit"             % Version.akkaHttp
  val akkaKryo             = "com.github.romix.akka" %% "akka-kryo-serialization"       % Version.akkaKryo
  val akkaClusterManager   = "com.lightbend.akka"    %% "akka-management-cluster-http"  % Version.akkaClusterManager
  val kamonSigar           = "io.kamon"               % "sigar-loader"                  % Version.sigarLoader

  // test libraries
  val scalactic            = "org.scalactic"     %% "scalactic"                         % Version.scalatest
  val scalatest            = "org.scalatest"     %% "scalatest"                         % Version.scalatest
  val scalacheck           = "org.scalacheck"    %% "scalacheck"                        % Version.scalacheck
  val dockerTestKit        = "com.whisk"         %% "docker-testkit-scalatest"          % Version.dockerItScala
  val dockerTestKitImpl    = "com.whisk"         %% "docker-testkit-impl-docker-java"   % Version.dockerItScala

  // logging
  val akkaSfl4j            = "com.typesafe.akka"          %% "akka-slf4j"               % Version.akka
  val logback              = "ch.qos.logback"              % "logback-classic"          % Version.logback
  val scalaLogging         = "com.typesafe.scala-logging" %% "scala-logging"            % Version.scalaLogging

  // config
  val configAnnotation     = "com.wacai"         %% "config-annotation"                 % Version.configAnnotation
  val macroParadise        = "org.scalamacros"    % "paradise"                          % Version.macroParadise

  // service discovery
  val constructR           = "de.heikoseeberger" %% "constructr"                        % Version.constructR
  val constructRetcd       = "de.heikoseeberger" %% "constructr-coordination-etcd"      % Version.constructR

  // storage
  val rocksdb              = "org.rocksdb"           % "rocksdbjni"                     % Version.rocksDB
  val kryo                 = "com.esotericsoftware"  % "kryo"                           % Version.kryo % "provided"
}

object Dependencies {
  import Library._

  private val genericTest = Seq(scalactic, scalatest % "test")

  private val akkaCommon        = Seq(akkaActor, akkaSfl4j, akkaTestkit, akkaKryo, akkaStream)
  private val akkaHttpCommon    = Seq(akkaHttp, akkaHttpSprayJson, akkaHttpTestkit)
  private val akkaClusterCommon = Seq(akkaRemote, akkaMultiNodeTestkit % "multi-jvm", akkaCluster, akkaClusterMetrics, akkaClusterTools, kamonSigar, akkaClusterManager)

  private val constructr = Seq(constructR, constructRetcd)

  val core = akkaCommon ++ akkaClusterCommon ++ genericTest ++ Seq(scalacheck % "test", logback, scalaLogging) ++ Seq(akkaHttpSprayJson)
  val ring = genericTest
  val vectorClocks = genericTest
  val httpApi = akkaCommon ++ akkaHttpCommon ++ genericTest

  val storageApi = genericTest
  val storageInMem = genericTest
  val storageLogDBExperimental = genericTest
  val storageRocksDB = Seq(rocksdb, rocksdb % "test", kryo) ++ genericTest

  val root = core ++ httpApi ++ storageApi ++ constructr ++ Seq(dockerTestKit, dockerTestKitImpl)
}
