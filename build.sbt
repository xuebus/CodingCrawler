name := """crawler"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.typesafe.akka" %% "akka-actor" % "2.4.12",
  "org.apache.httpcomponents" % "httpclient" % "4.5.2",
  "org.json4s" % "json4s-jackson_2.11" % "3.4.2",
  "org.apache.kafka" % "kafka-clients" % "0.9.0.1",
  "redis.clients" % "jedis" % "2.9.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)