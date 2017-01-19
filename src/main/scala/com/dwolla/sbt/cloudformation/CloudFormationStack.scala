package com.dwolla.sbt.cloudformation

import com.dwolla.awssdk.cloudformation.CloudFormationClient
import sbt.IO.{read, utf8}
import sbt.Keys._
import sbt._

import scala.language.{implicitConversions, postfixOps}

object CloudFormationStack extends AutoPlugin {

  object autoImport extends CloudFormationStackKeys

  import autoImport._

  lazy val plugin = new CloudFormationStackPlugin

  lazy val defaultValues = Seq(
    templateJsonFilename := plugin.defaultTemplateJsonFilename,
    templateJson := target.value / templateJsonFilename.value,
    stackParameters := plugin.defaultStackParameters,
    cloudformationClient := CloudFormationClient(),
    stackName := normalizedName.value,
    stackRoleArn := None
  )

  lazy val tasks = Seq(
    generateStack := plugin.runStackTemplateBuilder((mainClass in run in Compile).value, templateJson.value, (runner in run).value, (fullClasspath in Runtime).value, streams.value),
    generatedStackFile := read(generateStack.toTask("").value, utf8),
    deployStack := plugin.deployStack(stackName.value, generatedStackFile.value, stackParameters.value, stackRoleArn.value, cloudformationClient.value)
  )

  private lazy val generatedStackFile = taskKey[String]("generatedStackFile")

  lazy val pluginSettings = defaultValues ++ tasks

  override lazy val projectSettings = pluginSettings
}
