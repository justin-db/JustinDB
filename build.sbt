//import com.typesafe.sbt.SbtMultiJvm
//import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

addCommandAlias("compileAll", ";compile;test:compile") //;multi-jvm:compile")

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

lazy val core = (project in file("justin-db"))
  .settings(
    name := "justin-db",
    scalaVersion := Version.scala,
    scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint"),
    javacOptions in Compile ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"),
    libraryDependencies ++= Dependencies.core
  ).dependsOn(merkleTrees, vectorClocks, consistentHashing, crdts)

lazy val httpClient = (project in file("justin-db/client/http")).settings(
  name := "justin-db-client-http",
  scalaVersion := Version.scala,
  libraryDependencies ++= Dependencies.httpClient,
  fork in Test := true,
  javaOptions in Test += "-Dconfig.resource=test.conf",
  mainClass in assembly := Some("justin.db.client.Main")
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
