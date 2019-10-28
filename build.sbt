lazy val buildSettings = Seq(
  organization := "com.dwolla.sbt",
  name := "sbt-cloudformation-stack",
  homepage := Some(url("https://github.com/Dwolla/sbt-cloudformation-stack")),
  description := "SBT plugin to deploy projects using CloudFormation",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  sbtPlugin := true,
  startYear := Option(2016),
  resolvers += Resolver.bintrayRepo("dwolla", "maven"),
  libraryDependencies ++= {
    val awsSdkVersion = "1.11.331"
    val specs2Version = "4.8.0"
    Seq(
      "com.amazonaws"   %  "aws-java-sdk-cloudformation"  % awsSdkVersion,
      "com.dwolla"      %% "fs2-aws"                      % "2.0.0-M5",
      "ch.qos.logback"  %  "logback-classic"              % "1.2.3",
      "org.specs2"      %% "specs2-core"                  % specs2Version % Test,
      "org.specs2"      %% "specs2-mock"                  % specs2Version % Test
    )
  },
  releaseVersionBump := sbtrelease.Version.Bump.Minor,
  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings"),
  scalacOptions in Test --= Seq("-Xfatal-warnings"),
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
  .enablePlugins(ScriptedPlugin)
