enablePlugins(JavaAppPackaging)

organization := "com.github.jw3"
name := "example-consul"
version := "0.1"
scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaVersion = "2.4.2"

  Seq(
    "net.ceedubs" %% "ficus" % "1.1.2",

    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % Runtime,

    "org.scalatest" %% "scalatest" % "2.2.5" % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion % Test
  )
}


