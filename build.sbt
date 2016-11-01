import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

fork in run := true

lazy val core = (project in file("justin-db"))
  .settings(SbtMultiJvm.multiJvmSettings: _*)
  .settings(
    name := "justin-db",
    version := "0.0.1",
    scalaVersion := Version.scala,
    scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint"),
    javacOptions in Compile ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"),
    libraryDependencies ++= Dependencies.core,
    javaOptions in run ++= Seq("-Xms128m", "-Xmx1024m", "-Djava.library.path=./target/native"),
    Keys.fork in run := true,
    // make sure that MultiJvm test are compiled by the default test compilation
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    // disable parallel tests
    parallelExecution in Test := false,
    // make sure that MultiJvm tests are executed by the default test target,
    // and combine the results from ordinary test and multi-jvm tests
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
  .dependsOn(justin_merkle_trees, justin_vector_clocks, justin_consistent_hashing)

lazy val justin_db_storage_in_mem = (project in file("justin-db/storage/in-mem")).settings(
  name := "justin-db-storage-in-mem",
  version := "0.0.1",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.dbStorageInMem
).dependsOn(core)

lazy val justin_db_storage_file_per_key = (project in file("justin-db/storage/file-per-key")).settings(
  name := "justin-db-storage-file-per-key",
  version := "0.0.1",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.dbStorageFilePerKey
).dependsOn(core)

lazy val justin_merkle_trees = (project in file("justin-merkle-trees")).settings(
  name := "justin-merkle-trees",
  version := "0.0.1",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.merkleTrees
)

lazy val justin_vector_clocks = (project in file("justin-vector-clocks")).settings(
  name := "justin-vector-clocks",
  version := "0.0.1",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.vectorClocks
)

lazy val justin_consistent_hashing = (project in file("justin-consistent-hashing")).settings(
  name := "justin-consistent-hashing",
  version := "0.0.1",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.consistentHashing
)
