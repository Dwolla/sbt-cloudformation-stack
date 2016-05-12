package com.dwolla.sbt.cloudformation

import com.dwolla.awssdk.cloudformation.CloudFormationClient
import sbt._

import scala.language.postfixOps

trait CloudFormationStackKeys {
  lazy val generateStack = InputKey[File]("generate")
  lazy val deployStack = TaskKey[String]("deploy")
  lazy val stackParameters = TaskKey[List[(String, String)]]("parameters")

  lazy val stackName = SettingKey[String]("stackName", "Name of the stack to deploy")
  lazy val templateJson = SettingKey[File]("templateJson", "File location where the CloudFormation stack template will be output")
  lazy val templateJsonFilename = settingKey[String]("Filename where the CloudFormation stack template will be output")

  lazy val cloudformationClient = settingKey[CloudFormationClient]("cloudformation client")
}
