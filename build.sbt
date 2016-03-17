organization := "com.github.jw3"
name := "consulq"
description := "Query services registered in Consul"
version := "0.1"

scalaVersion := "2.11.8"

resolvers += "jw3 at bintray" at "https://dl.bintray.com/jw3/maven"
licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
com.updateimpact.Plugin.apiKey in ThisBuild := sys.env.getOrElse("UPDATEIMPACT_API_KEY", (com.updateimpact.Plugin.apiKey in ThisBuild).value)

libraryDependencies ++= {
  val akkaVersion = "2.4.2"
  val scalaTest = "3.0.0-M15"

  Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,

    "org.scalactic" %% "scalactic" % scalaTest % Test,
    "org.scalatest" %% "scalatest" % scalaTest % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
  )
}


