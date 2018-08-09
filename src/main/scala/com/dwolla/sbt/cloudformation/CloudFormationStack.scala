package com.dwolla.sbt.cloudformation

import com.dwolla.awssdk.cloudformation.CloudFormationClient
import sbt.IO.{read, utf8}
import sbt.Keys._
import sbt._

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
    changeSetName := None,
    awsAccountId := None,
    awsRoleName := None,
    stackRoleArn := plugin.roleArn(awsAccountId.value, awsRoleName.value),
    deployEnvironment := None,
    deployEnvironmentParameterName := "Environment",
    deployEnvironmentOptions := Seq("Sandbox", "DevInt", "Uat", "Prod", "Admin")
  )

  lazy val tasks = Seq(
    generateStack := plugin.runStackTemplateBuilder((mainClass in run in Compile).value, templateJson.value, (runner in run).value, (fullClasspath in Runtime).value, streams.value),
    generatedStackFile := read(generateStack.toTask("").value, utf8),
    deployStack := plugin.deployStack(
      stackName.value,
      generatedStackFile.value,
      stackParameters.value,
      stackRoleArn.value,
      deployEnvironment.value,
      deployEnvironmentParameterName.value,
      changeSetName.value,
      cloudformationClient.value
    ),
    commands += plugin.parseSettings
  )

  private lazy val generatedStackFile = taskKey[String]("generatedStackFile")

  lazy val pluginSettings = defaultValues ++ tasks

  override lazy val projectSettings = pluginSettings
}

sealed trait CloudFormationOption {
  val value: String
}
case class AwsAccountId(value: String) extends CloudFormationOption {
  override def toString: String = value
}
case class AwsRoleName(value: String) extends CloudFormationOption {
  override def toString: String = s"role/$value"
}
case class Environment(value: String) extends CloudFormationOption {
  override def toString: String = value
}
