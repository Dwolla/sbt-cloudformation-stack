package com.dwolla.sbt.cloudformation

import com.amazonaws.regions.Regions
import sbt._

trait CloudFormationStackKeys {
  lazy val generateStack = InputKey[File]("generate")
  lazy val deployStack = TaskKey[String]("deploy")
  lazy val stackParameters = TaskKey[List[(String, String)]]("parameters")

  lazy val stackName = SettingKey[String]("stackName", "Name of the stack to deploy")
  lazy val templateJson = SettingKey[File]("templateJson", "File location where the CloudFormation stack template will be output")
  lazy val templateJsonFilename = settingKey[String]("Filename where the CloudFormation stack template will be output")

  lazy val awsAccountId = settingKey[Option[String]]("Optional Account ID used to help populate the Stack Role ARN")
  lazy val awsRoleName = settingKey[Option[String]]("Optional role name used to help populate the Stack Role ARN")
  lazy val awsRegion = settingKey[Option[Regions]]("Region where the stack should be deployed")
  lazy val stackRoleArn = settingKey[Option[String]]("Optional Role ARN used by CloudFormation to execute the changes required by the stack")

  lazy val deployEnvironment = settingKey[Option[String]]("Environment into which the stack is being deployed")
  lazy val deployEnvironmentParameterName = settingKey[String]("CloudFormation parameter name for the deploy environment")
  lazy val deployEnvironmentOptions = settingKey[Seq[String]]("allowed options for the Environment into which the stack can be deployed")

  lazy val changeSetName = settingKey[Option[String]]("if supplied, CloudFormation will create a change set to allow stack creates or updates to be evaluated before being completed")

  /**
    * This is `IOCloudFormationClient` instead of `CloudFormationClient[IO]` because sbt's macros
    * don't work with higher-kinded types. See https://github.com/sbt/sbt/issues/2188
    */
  lazy val cloudformationClient = settingKey[IOCloudFormationClient]("cloudformation client")
}
