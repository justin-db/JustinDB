resolvers += Classpaths.typesafeResolver

addSbtPlugin("com.typesafe.sbt"  % "sbt-multi-jvm"         % "0.3.11")
addSbtPlugin("org.scoverage"     % "sbt-scoverage"         % "1.5.0")
addSbtPlugin("io.spray"          % "sbt-revolver"          % "0.8.0")
addSbtPlugin("com.timushev.sbt"  % "sbt-updates"           % "0.2.0")
addSbtPlugin("org.scalastyle"   %% "scalastyle-sbt-plugin" % "0.8.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-native-packager"   % "1.2.2")
addSbtPlugin("com.typesafe.sbt"  % "sbt-git"               % "0.8.5")
addSbtPlugin("com.eed3si9n"      % "sbt-buildinfo"         % "0.6.1")
addSbtPlugin("io.get-coursier"   % "sbt-coursier"          % "1.0.0-RC10")

libraryDependencies += "org.slf4j" % "slf4j-nop"          % "1.7.21"
