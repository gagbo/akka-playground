ThisBuild / scalaVersion := "2.12.7"
ThisBuild / organization := "co.example"

val AkkaVersion = "2.6.14"

lazy val pocAkka = (project in file("."))
  .settings(
    name := "PocAkka",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test,
      "ch.qos.logback"        % "logback-classic"          % "1.2.3",
      "net.logstash.logback"  % "logstash-logback-encoder" % "6.2",
    ),
    mainClass in (Compile, run) := Some("co.example.PocAkka")
    )
