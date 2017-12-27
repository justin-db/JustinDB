import com.typesafe.sbt.packager.docker._

name           := "JustinDB"
version        := "0.1"
maintainer     := "Mateusz Maciaszek"
packageSummary := "JustinDB"

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

fork in run := true

daemonUser.in(Docker) := "root"
maintainer.in(Docker) := "Mateusz Maciaszek"
dockerBaseImage       := "java:8"
dockerExposedPorts    := Vector(2552, 8000)
dockerRepository      := Some("justindb")
dockerCommands        += Cmd("RUN", s"ln -s /opt/docker/cli /bin/cli")
dockerCommands        += Cmd("RUN", "echo 'cat /opt/docker/motd' >> /etc/bash.bashrc")

// Force building with Java 8
initialize := {
  val required = "1.8"
  val current = sys.props("java.specification.version")
  assert(current == required, s"Unsupported build JDK: java.specification.version $current != $required")
}

// PROJECT DEFINITIONS
lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin, SbtMultiJvm, JavaAppPackaging, DockerPlugin)
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
  .settings(versionWithGit)
  .settings(git.useGitDescribe := true)
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
  .aggregate(storageApi, ring)
  .dependsOn(storageApi, ring)

lazy val ring = (project in file("justin-ring"))
  .disablePlugins(RevolverPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "justin-ring",
    scalaVersion := Version.scala,
    libraryDependencies ++= Dependencies.ring
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

// ALIASES
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
