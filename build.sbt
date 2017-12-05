import sbt._
import com.typesafe.sbt.packager.docker._

name           := "JustinDB"
maintainer     := "mateusz.maciaszekhpc@gmail.com"

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

fork in run := true

// DOCKER DEFINITION
daemonUser.in(Docker) := "root"
maintainer.in(Docker) := "Mateusz Maciaszek"
dockerRepository      := Some("justindb")
dockerUpdateLatest    := true
dockerBaseImage       := "local/openjdk-jre-8-bash"
dockerCommands        += Cmd("RUN", s"ln -s /opt/docker/cli /bin/cli")
dockerCommands        += Cmd("RUN", "echo 'cat /opt/docker/motd' >> /etc/bash.bashrc")
dockerEntrypoint      ++= Seq(
  """-Djustin.netty-tcp-hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")"""",
  """-Djustin.netty-tcp-port="$AKKA_REMOTING_BIND_PORT"""",
  """-Djustin.system="$AKKA_ACTOR_SYSTEM_NAME"""",
  """-Djustin.kubernetes-hostname="$(eval "echo $JUSTINDB_NODE_ID_NAME")"""",
  """$(IFS=','; I=0; for NODE in $AKKA_SEED_NODES; do echo "-Dakka.cluster.seed-nodes.$I=akka.tcp://$AKKA_ACTOR_SYSTEM_NAME@$NODE"; I=$(expr $I + 1); done)""",
  "-Dakka.io.dns.resolver=async-dns",
  "-Dakka.io.dns.async-dns.resolve-srv=true",
  "-Dakka.io.dns.async-dns.resolv-conf=on"
)
dockerCommands :=
  dockerCommands.value.flatMap {
    case ExecCmd("ENTRYPOINT", args @ _*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
    case v => Seq(v)
  }

// Force building with Java 8
initialize := {
  val required = "1.8"
  val current = sys.props("java.specification.version")
  assert(current == required, s"Unsupported build JDK: java.specification.version $current != $required")
}

// *****************************************************************************
// PROJECTS
// *****************************************************************************
lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin, SbtMultiJvm, JavaServerAppPackaging)
  .configs(MultiJvm)
  .settings(commonSettings: _*)
  .settings(
    mainClass in assembly := Some("justin.db.Main"),
    assemblyJarName in assembly := "justindb.jar",
    test in assembly := {},
    libraryDependencies ++= Dependencies.root,
    scalaVersion := Version.scala,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, git.gitHeadCommit, git.gitCurrentBranch),
    buildInfoOptions += BuildInfoOption.ToJson
  )
  .settings(
    versionWithGit,
    git.useGitDescribe := true
  )
  .settings(configAnnotationSettings)
  .aggregate(core, httpApi, storageInMem, storageRocksDB)
  .dependsOn(core, httpApi, storageInMem, storageRocksDB)

lazy val core = (project in file("justin-core"))
  .disablePlugins(RevolverPlugin)
  .configs(MultiJvm)
  .settings(commonSettings: _*)
  .settings(
    name := "justin-core",
    scalaVersion := Version.scala,
    libraryDependencies ++= Dependencies.core
  )
  .aggregate(storageApi, ring, vectorClocks)
  .dependsOn(storageApi, ring, vectorClocks)

lazy val ring = (project in file("justin-ring"))
  .disablePlugins(RevolverPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "justin-ring",
    scalaVersion := Version.scala,
    libraryDependencies ++= Dependencies.ring
  )

lazy val vectorClocks = (project in file("justin-vector-clocks"))
  .disablePlugins(RevolverPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "justin-vector-clocks",
    scalaVersion := Version.scala,
    libraryDependencies ++= Dependencies.vectorClocks
  )

lazy val httpApi = (project in file("justin-http-api"))
  .disablePlugins(RevolverPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "justin-http-api",
    scalaVersion := Version.scala,
    libraryDependencies ++= Dependencies.httpApi,
    fork in Test := true,
    javaOptions in Test += "-Dconfig.resource=test.conf"
  )
  .dependsOn(core)

lazy val storageApi = (project in file("justin-storage-api"))
  .disablePlugins(RevolverPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "justin-storage-api",
    scalaVersion := Version.scala,
    libraryDependencies ++= Dependencies.storageApi
  )

lazy val storageInMem = (project in file("justin-storage-in-mem"))
  .disablePlugins(RevolverPlugin)
  .settings(configAnnotationSettings)
  .settings(commonSettings: _*)
  .settings(
    name := "justin-storage-in-mem",
    scalaVersion := Version.scala,
    libraryDependencies ++= Dependencies.storageInMem
  )
  .dependsOn(storageApi)

lazy val storageRocksDB = (project in file("justin-storage-rocksdb"))
  .disablePlugins(RevolverPlugin)
  .settings(configAnnotationSettings)
  .settings(commonSettings: _*)
  .settings(
    name := "justin-storage-rocksdb",
    scalaVersion := Version.scala,
    libraryDependencies ++= Dependencies.storageRocksDB
  )
  .dependsOn(storageApi)


// *****************************************************************************
// Settings
// *****************************************************************************
lazy val configAnnotationSettings: Seq[sbt.Setting[_]] = {
  Seq(
    scalacOptions += "-Xmacro-settings:conf.output.dir=" + baseDirectory.value.getAbsolutePath + "/src/main/resources",
    addCompilerPlugin(Library.macroParadise cross CrossVersion.full),
    libraryDependencies += Library.configAnnotation
  )
}

// *****************************************************************************
// Aliases
// *****************************************************************************
addCommandAlias("compileAll", ";compile;test:compile;multi-jvm:compile")
addCommandAlias("testAll", ";test:test;multi-jvm:test")

// SETTINGS
lazy val commonSettings = Def.settings(
  compileSettings
)

lazy val compileSettings = Def.settings(
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked",
    "-encoding",
    "utf8",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:existentials",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-Xfatal-warnings",
    "-Xfuture",
    "-Yno-adapted-args",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Ywarn-dead-code"
  )
)

lazy val configAnnotationSettings: Seq[sbt.Setting[_]] = {
  Seq(
    scalacOptions += "-Xmacro-settings:conf.output.dir=" + baseDirectory.value.getAbsolutePath + "/src/main/resources",
    addCompilerPlugin(Library.macroParadise cross CrossVersion.full),
    libraryDependencies += Library.configAnnotation
  )
}
