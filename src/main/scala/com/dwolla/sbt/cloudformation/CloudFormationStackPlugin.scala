package com.dwolla.sbt.cloudformation

import cats.effect._
import com.dwolla.fs2aws.cloudformation.CloudFormationClient
import com.dwolla.sbt.cloudformation.CloudFormationStack.autoImport._
import com.dwolla.sbt.cloudformation.CloudFormationStack.plugin
import com.dwolla.sbt.cloudformation.CloudFormationStackParsers.buildCloudFormationStackParser
import sbt.Attributed._
import sbt.Keys._
import sbt.{File, _}

import scala.reflect.ClassTag

class CloudFormationStackPlugin {
  val defaultStackParameters = List.empty[(String, String)]
  val defaultTemplateJsonFilename = "cloudformation-template.json"

  def runStackTemplateBuilder(maybeMainClass: Option[String], outputFile: File, scalaRun: ScalaRun, classpath: Seq[Attributed[File]], streams: TaskStreams): File = {
    maybeMainClass.fold(throw new NoMainClassDetectedException) { mainClass ⇒
      scalaRun.run(mainClass, data(classpath), Seq(outputFile.getCanonicalPath), streams.log).failed.foreach(sys error _.getMessage)

      outputFile
    }
  }

  def deployStack(projectName: String,
                  input: String,
                  params: List[(String, String)],
                  maybeRoleArn: Option[String],
                  deployEnvironment: Option[String],
                  deployEnvironmentParameterName: String,
                  changeSetName: Option[String],
                  client: CloudFormationClient[IO]): String = {

    val paramsWithEnvironment = deployEnvironment.fold(params) { environment ⇒
      (deployEnvironmentParameterName → environment) :: params.filterNot {
        case (key, _) ⇒ key == deployEnvironmentParameterName
      }
    }

    client.createOrUpdateTemplate(projectName, input, paramsWithEnvironment, maybeRoleArn, changeSetName)
      .unsafeRunSync()
  }

  def roleArn(maybeAccountId: Option[String], maybeRoleName: Option[String]): Option[String] = for {
    accountId ← maybeAccountId
    roleName ← maybeRoleName
  } yield s"arn:aws:iam::$accountId:role/$roleName"

  def find[T <: CloudFormationOption](seq: Seq[CloudFormationOption])(implicit tag: ClassTag[T]): Option[String] = seq.find {
    case _: T ⇒ true
    case _ ⇒ false
  }.map(_.value)

  def parseSettings: Command = Command("withDeployParameters")(buildCloudFormationStackParser) { (state: State, options: Seq[CloudFormationOption]) ⇒
    val extracted: Extracted = Project extract state
    val maybeAccountId = plugin.find[AwsAccountId](options)
    val maybeRoleName = plugin.find[AwsRoleName](options)
    val maybeRoleArn = plugin.roleArn(maybeAccountId, maybeRoleName)

    extracted.appendWithSession(Seq(
      awsAccountId := maybeAccountId,
      awsRoleName := maybeRoleName,
      deployEnvironment := plugin.find[Environment](options),
      stackRoleArn := maybeRoleArn
    ), state)
  }
}

class NoMainClassDetectedException extends RuntimeException("No main class detected.")
