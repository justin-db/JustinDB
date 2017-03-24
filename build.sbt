import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

name           := "JustinDB"
version        := "0.1"
maintainer     := "Mateusz Maciaszek"
packageSummary := "JustinDB Cluster"

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

fork in run := true

scalacOptions := Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-Xlint:_",
  "-Xfatal-warnings",
  "-encoding",
  "utf8",
  "-language:implicitConversions"
)

// Force building with Java 8
initialize := {
  val required = "1.8"
  val current = sys.props("java.specification.version")
  assert(current == required, s"Unsupported build JDK: java.specification.version $current != $required")
}

// PROJECT DEFINITIONS
lazy val core = (project in file("justin-core"))
  .settings(SbtMultiJvm.multiJvmSettings: _*)
  .settings(
    name := "justin-core",
    scalaVersion := Version.scala,
    scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint"),
    javacOptions in Compile ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"),
    libraryDependencies ++= Dependencies.core,
    javaOptions in run ++= Seq("-Xms128m", "-Xmx1024m", "-Djava.library.path=./target/native"),
    Keys.fork in run := true,
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    parallelExecution in Test := false,
    executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
      case (testResults, multiNodeResults)  =>
        val overall = if (testResults.overall.id < multiNodeResults.overall.id) multiNodeResults.overall else testResults.overall
        Tests.Output(overall, testResults.events ++ multiNodeResults.events, testResults.summaries ++ multiNodeResults.summaries)
    }
  )
  .configs (MultiJvm)
  .dependsOn(merkleTrees, vectorClocks, consistentHashing, crdts, storageAPi)

lazy val httpApi = (project in file("justin-http-api"))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "justin-http-api",
    scalaVersion := Version.scala,
    libraryDependencies ++= Dependencies.httpClient,
    fork in Test := true,
    javaOptions in Test += "-Dconfig.resource=test.conf"
  )
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, git.gitHeadCommit, git.gitCurrentBranch),
    buildInfoOptions += BuildInfoOption.ToJson
  )
  .settings(versionWithGit)
  .settings(git.useGitDescribe := true)
  .dependsOn(core)

lazy val storageAPi = (project in file("justin-storage-api")).settings(
  name := "justin-storage-api",
  scalaVersion := Version.scala
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

lazy val merkleTrees = (project in file("justin-merkle-trees")).settings(
  name := "justin-merkle-trees",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.merkleTrees
)

lazy val vectorClocks = (project in file("justin-vector-clocks")).settings(
  name := "justin-vector-clocks",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.vectorClocks
)

lazy val crdts = (project in file("justin-crdts")).settings(
  name := "justin-crdts",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.crdts
)

lazy val consistentHashing = (project in file("justin-consistent-hashing")).settings(
  name := "justin-consistent-hashing",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.consistenHashing
)

// ALIASES
addCommandAlias("compileAll", ";compile;test:compile;multi-jvm:compile")

// RUN
run in Compile <<= (run in Compile in httpApi)
