name := "espressivo"

version := "0.1"

scalaVersion := "2.10.1"

unmanagedJars in Compile += Attributed.blank(
    file(scala.util.Properties.javaHome) / "lib" / "jfxrt.jar")

fork in run := true

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies ++= Seq( 
    "com.typesafe.akka" %% "akka-actor" % "2.1.4",
    "com.typesafe.akka" %% "akka-remote" % "2.1.4",
    "com.typesafe.akka" %% "akka-testkit" % "2.1.4",
    "io.spray" % "spray-routing" % "1.1-M8",
    "io.spray" % "spray-can" % "1.1-M8",
    "io.spray" %%  "spray-json" % "1.2.5",
    "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
    "org.scala-lang" % "scala-reflect" % "2.10.2",
    "com.novus" %% "salat" % "1.9.3",
    "org.scalafx" %% "scalafx" % "1.0.0-M5"
)