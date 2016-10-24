lazy val root = (project in file(".")).settings( 
 name := "justin-vector-clocks", 
 version := "0.0.1", 
 scalaVersion := "2.11.8",
 libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.0",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
 )
) 
