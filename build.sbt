enablePlugins(JavaAppPackaging)

organization := "com.github.jw3"
name := "example-consul"
version := "0.1"
scalaVersion := "2.11.7"

libraryDependencies ++= {
  Seq(
    "org.scalatest" %% "scalatest" % "2.2.5" % Test
  )
}


