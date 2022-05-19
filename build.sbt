name := "JsonSchemaValidator"

version := "0.1"

scalaVersion := "2.13.6"

val catsVersion = "2.7.0"
val akkaVersion = "10.2.9"
val scalaTestVersion = "3.2.11"
val zioVersion = "1.0.13"
val json4sVersion = "4.0.4"
val akkaHttpJson4sVersion = "1.39.2"
val akkaStreamVersion = "2.6.18"
val redisVersion = "3.42"
val jsonSchemaVersion = "0.2.0"
val scalaCheckVersion = "1.15.4"

resolvers += "jitpack".at("https://jitpack.io")

libraryDependencies := Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "dev.zio" %% "zio" % zioVersion,
  "com.typesafe.akka" %% "akka-http" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
  "de.heikoseeberger" %% "akka-http-json4s" % akkaHttpJson4sVersion,
  "org.json4s" %% "json4s-native" % json4sVersion,
  "org.json4s" %% "json4s-jackson" % json4sVersion,
  "org.json4s" %% "json4s-ext" % json4sVersion,
  "net.debasishg" %% "redisclient" % redisVersion,
  "io.circe" %% "circe-json-schema" % jsonSchemaVersion,
  "io.circe" %% "circe-parser" % "0.14.1",
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % Test
)

enablePlugins(JavaAppPackaging)
Docker / dockerExposedPorts := Seq(9000)