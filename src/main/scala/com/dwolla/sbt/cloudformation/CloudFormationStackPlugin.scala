package com.dwolla.sbt.cloudformation

import com.dwolla.awssdk.cloudformation.CloudFormationClient
import com.dwolla.util.{Environment, SystemEnvironment}
import sbt.Attributed._
import sbt.Keys._
import sbt.{File, _}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CloudFormationStackPlugin(environment: Environment = SystemEnvironment) {
  val defaultStackParameters = List.empty[(String, String)]
  val defaultTemplateJsonFilename = "cloudformation-template.json"

  def runStackTemplateBuilder(maybeMainClass: Option[String], outputFile: File, scalaRun: ScalaRun, classpath: Seq[Attributed[File]], streams: TaskStreams): File = {
    maybeMainClass.fold(throw new NoMainClassDetectedException) { mainClass ⇒
      toError(scalaRun.run(mainClass, data(classpath), Seq(outputFile.getCanonicalPath), streams.log))

      outputFile
    }
  }

  def deployStack(projectName: String, input: String, params: List[(String, String)], roleArn: Option[String], client: CloudFormationClient): String =
    Await.result(client.createOrUpdateTemplate(projectName, input, params, roleArn), Duration.Inf)
}

class NoMainClassDetectedException extends RuntimeException("No main class detected.")
