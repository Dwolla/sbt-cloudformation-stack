lazy val buildSettings = Seq(
  organization := "com.dwolla.sbt",
  name := "sbt-cloudformation-stack",
  homepage := Some(url("https://github.com/Dwolla/sbt-cloudformation-stack")),
  description := "SBT plugin to deploy projects using CloudFormation",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  version := "1.2.0",
  scalaVersion := "2.10.6",
  sbtPlugin := true,
  startYear := Option(2016),
  resolvers += Resolver.bintrayIvyRepo("dwolla", "maven"),
  libraryDependencies ++= {
    val awsSdkVersion = "1.11.104"
    val specs2Version = "3.8.6"
    Seq(
      "com.amazonaws"   %  "aws-java-sdk-cloudformation"  % awsSdkVersion,
      "ch.qos.logback"  %  "logback-classic"              % "1.1.7",
      "com.dwolla"      %% "scala-aws-utils"              % "1.3.0",
      "org.specs2"      %% "specs2-core"                  % specs2Version % Test,
      "org.specs2"      %% "specs2-mock"                  % specs2Version % Test
    )
  }
)

lazy val bintraySettings = Seq(
  bintrayVcsUrl := Some("https://github.com/Dwolla/sbt-cloudformation-stack"),
  publishMavenStyle := false,
  bintrayRepository := "sbt-plugins",
  bintrayOrganization := Option("dwolla"),
  pomIncludeRepository := { _ ⇒ false }
)

lazy val pipeline = InputKey[Unit]("pipeline", "Runs the full build pipeline: compile, test, integration tests")
pipeline := scripted.dependsOn(test in Test).evaluated

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
}

// uncomment to see sbt output for each scripted test run
//scriptedBufferLog := false

val cloudformationStack = (project in file("."))
  .settings(buildSettings ++ bintraySettings: _*)
  .settings(ScriptedPlugin.scriptedSettings: _*)
