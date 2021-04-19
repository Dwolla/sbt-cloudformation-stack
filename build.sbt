inThisBuild(List(
  organization := "com.dwolla.sbt",
  description := "SBT plugin to deploy projects using CloudFormation",
  sbtPlugin := true,
  startYear := Option(2016),
  homepage := Some(url("https://github.com/Dwolla/sbt-cloudformation-stack")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  developers := List(
    Developer(
      "bpholt",
      "Brian Holt",
      "bholt@dwolla.com",
      url("https://dwolla.com")
    )
  ),
  githubWorkflowJavaVersions := Seq("adopt@1.8", "adopt@1.11"),
  githubWorkflowTargetTags ++= Seq("v*"),
  githubWorkflowPublishTargetBranches :=
    Seq(RefPredicate.StartsWith(Ref.Tag("v"))),
  githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("test", "scripted"), name = Some("Build and test project"))),
  githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release"))),
  githubWorkflowPublish := Seq(
    WorkflowStep.Sbt(
      List("ci-release"),
      env = Map(
        "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
        "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
        "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
        "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
      )
    )
  ),
))

lazy val pipeline = InputKey[Unit]("pipeline", "Runs the full build pipeline: compile, test, integration tests")
pipeline := scripted.dependsOn(Test / test).evaluated

scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value)

// uncomment to see sbt output for each scripted test run
//scriptedBufferLog := false

val `sbt-cloudformation-stack` = (project in file("."))
  .settings(
    sonatypeProfileName := "com.dwolla",
    libraryDependencies ++= {
      val awsSdkVersion = "1.11.996"
      val specs2Version = "4.10.6"

      Seq(
        "com.amazonaws"   %  "aws-java-sdk-cloudformation"  % awsSdkVersion,
        "com.dwolla"      %% "fs2-aws"                      % "2.0.0-M11",
        "ch.qos.logback"  %  "logback-classic"              % "1.2.3",
        "org.specs2"      %% "specs2-core"                  % specs2Version % Test,
        "org.specs2"      %% "specs2-mock"                  % specs2Version % Test
      )
    },
  )
  .enablePlugins(SbtPlugin)
