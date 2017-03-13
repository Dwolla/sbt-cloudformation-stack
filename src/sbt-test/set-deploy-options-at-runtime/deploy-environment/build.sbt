import scala.language.postfixOps

name := "test-project"
scalaVersion := "2.11.8"

libraryDependencies ++= {
  lazy val specs2Version = "3.7.2"

  Seq(
    "com.jsuereth" %% "scala-arm" % "1.4",      // use scala-arm to prove we can pull in an arbitrary dependency into the stack code
    "org.specs2" %% "specs2-core" % specs2Version % Test
  )
}

cloudformationClient := FakeCloudFormationClient

val app = (project in file("."))
  .enablePlugins(CloudFormationStack)

TaskKey[Unit]("check") := {
  val testResults = (executeTests in Test).value
  val environmentValue = deployEnvironment.value
  val stackId: String = deployStack.value

  val expectedStackId =
    """test-project:
      |
      |{
      |  "template": true
      |}
      |
      |with parameters:
      |List((Environment,Admin))
      |
      |with role ARN: None
      |""".stripMargin

  val tests = Seq(
    (testResults.overall == TestResult.Passed, "Tests must all have passed"),
    (environmentValue == Option("Admin"), s"Deploy Environment must match expected value, which should be one of ${deployEnvironmentOptions.value}. got:\n$environmentValue\nexpected:\nSome(Admin)"),
    (stackId == expectedStackId, s"stack ID must match FakeCloudFormationClient template. got:\n${stackId.replace(" ", ".")}\nexpected:\n${expectedStackId.replace(" ", ".")}")
  )

  val allErrors = tests.filterNot { case (passed, _) ⇒ passed }.map { case (_, failureMessage) ⇒ failureMessage }
  if (allErrors.nonEmpty) sys.error(
    s"""${allErrors.size} failures detected:
       |
       |${allErrors.mkString("\n")}
     """.stripMargin)
}
