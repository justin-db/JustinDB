import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

fork in run := true

// Force building with Java 8
initialize := {
  val required = "1.8"
  val current = sys.props("java.specification.version")
  assert(current == required, s"Unsupported build JDK: java.specification.version $current != $required")
}

lazy val core = (project in file("justin-db"))
  .settings(SbtMultiJvm.multiJvmSettings: _*)
  .settings(
    name := "justin-db",
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
        val overall =
          if (testResults.overall.id < multiNodeResults.overall.id)
            multiNodeResults.overall
          else
            testResults.overall
        Tests.Output(overall,
          testResults.events ++ multiNodeResults.events,
          testResults.summaries ++ multiNodeResults.summaries)
    }
  )
  .configs (MultiJvm)
  .dependsOn(merkleTrees, vectorClocks)

lazy val httpClient = (project in file("justin-db/client/http")).settings(
  name := "justin-db-client-http",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.httpClient,
  fork in Test := true,
  javaOptions in Test += "-Dconfig.resource=test.conf"
).dependsOn(core, dbStorageInMem)

lazy val dbStorageInMem = (project in file("justin-db/storage/in-mem")).settings(
  name := "justin-db-storage-in-mem",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.dbStorageInMem
).dependsOn(core)

lazy val dbStorageFilePerKey = (project in file("justin-db/storage/file-per-key")).settings(
  name := "justin-db-storage-file-per-key",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.dbStorageFilePerKey
).dependsOn(core)

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
