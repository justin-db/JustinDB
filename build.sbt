import com.typesafe.sbt.packager.docker._

name           := "JustinDB"
version        := "0.1"
maintainer     := "Mateusz Maciaszek"
packageSummary := "JustinDB"

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

fork in run := true

scalacOptions := Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-Xfatal-warnings",
  "-encoding",
  "utf8",
  "-language:implicitConversions"
)

daemonUser.in(Docker) := "root"
maintainer.in(Docker) := "Mateusz Maciaszek"
dockerBaseImage       := "java:8"
dockerExposedPorts    := Vector(2552, 8000)
dockerRepository      := Some("justindb")
dockerCommands        += Cmd("RUN", "echo 'cat /opt/docker/motd' >> /etc/bash.bashrc")

// Force building with Java 8
initialize := {
  val required = "1.8"
  val current = sys.props("java.specification.version")
  assert(current == required, s"Unsupported build JDK: java.specification.version $current != $required")
}

lazy val configAnnotationSettings: Seq[sbt.Setting[_]] = {
  Seq(
    scalacOptions += "-Xmacro-settings:conf.output.dir=" + baseDirectory.value.getAbsolutePath + "/src/main/resources",
    addCompilerPlugin(Library.macroParadise cross CrossVersion.full),
    libraryDependencies += Library.configAnnotation
  )
}

// PROJECT DEFINITIONS
lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin, SbtMultiJvm, JavaAppPackaging, DockerPlugin)
  .configs(MultiJvm)
  .settings(
    mainClass in assembly := Some("justin.Main"),
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
  .aggregate(core, httpApi, storageInMem, storagePersistent)
  .dependsOn(core, httpApi, storageInMem, storagePersistent) // TODO: storageInMem/storagePersistent should be provided

lazy val core = (project in file("justin-core"))
  .enablePlugins(SbtMultiJvm)
  .configs(MultiJvm)
  .settings(
    name := "justin-core",
    scalaVersion := Version.scala,
    libraryDependencies ++= Dependencies.core
  )
  .aggregate(storageAPi)
  .dependsOn(storageAPi)

lazy val httpApi = (project in file("justin-http-api"))
  .settings(
    name := "justin-http-api",
    scalaVersion := Version.scala,
    libraryDependencies ++= Dependencies.httpApi,
    fork in Test := true,
    javaOptions in Test += "-Dconfig.resource=test.conf"
  )
  .dependsOn(core)

lazy val storageAPi = (project in file("justin-storage-api")).settings(
  name := "justin-storage-api",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.storageApi
)

lazy val storageInMem = (project in file("justin-storage-in-mem")).settings(
  name := "justin-storage-in-mem",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.storageInMem
).dependsOn(storageAPi)

lazy val storagePersistent = (project in file("justin-storage-persistent")).settings(
  name := "justin-storage-persistent",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.storagePersistent
).dependsOn(storageAPi)

// ALIASES
addCommandAlias("compileAll", ";compile;test:compile;multi-jvm:compile")
