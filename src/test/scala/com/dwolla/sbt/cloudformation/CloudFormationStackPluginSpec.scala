package com.dwolla.sbt.cloudformation

import java.io.File

import com.dwolla.awssdk.cloudformation.CloudFormationClient
import com.dwolla.testutils.WithBehaviorMocking
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import sbt.Keys.TaskStreams
import sbt.internal.util.ManagedLogger
import sbt.{AttributeMap, Attributed, ScalaRun}

import scala.concurrent.Future
import scala.util.Try

class SbtCloudFormationStackPluginSpec extends Specification with Mockito with WithBehaviorMocking {

  class Setup extends Scope {
    val testClass = new CloudFormationStackPlugin

    val log = mock[ManagedLogger]
    val streams = mock[TaskStreams] withBehavior (_.log returns log)
  }

  "defaultTemplateJsonFilename" should {
    "be set correctly" in new Setup {
      testClass.defaultTemplateJsonFilename must_== "cloudformation-template.json"
    }
  }

  "default stack paramters" should {
    "be an empty list" in new Setup {
      testClass.defaultStackParameters must_== List.empty[(String, String)]
    }
  }

  "deployStack" should {
    "delegate to CloudFormationClient" in new Setup {
      val stackName = "project"
      val input = "template"
      val params = List("param1" → "value1")
      val cloudFormationOptions = Seq(AwsAccountId("account-id"), AwsRoleName("role-name"))
      val roleArn = Option("arn:aws:iam::account-id:role/role-name")
      val deployEnvironment = None
      val parameterName = "Environment"
      val changeSetName = None
      val client = mock[CloudFormationClient] withBehavior (_.createOrUpdateTemplate(stackName, input, params, roleArn, changeSetName) returns Future.successful("stack-id"))

      val output = testClass.deployStack(stackName, input, params, roleArn, deployEnvironment, parameterName, changeSetName, client)

      output must_== "stack-id"
    }

    "append an Environment parameter if set" in new Setup {
      val stackName = "project"
      val input = "template"
      val params = List("param1" → "value1")
      val cloudFormationOptions = Seq(AwsAccountId("account-id"), AwsRoleName("role-name"))
      val roleArn = Option("arn:aws:iam::account-id:role/role-name")
      val deployEnvironment = Option("Admin")
      val parameterName = "Environment"
      val changeSetName = None
      val client = mock[CloudFormationClient]

      val parameterCaptor = capture[List[(String, String)]]

      client.createOrUpdateTemplate(any[String], any[String], parameterCaptor.capture, any[Option[String]], any[Option[String]]) returns Future.successful("stack-id")

      val output = testClass.deployStack(stackName, input, params, roleArn, deployEnvironment, parameterName, changeSetName, client)

      parameterCaptor.value must contain(parameterName → "Admin")
      parameterCaptor.value must contain("param1" → "value1")
    }

    "replace an Environment parameter if set" in new Setup {
      val stackName = "project"
      val input = "template"
      val parameterName = "Environment"
      val params = List("param1" → "value1", parameterName → "NotAdmin")
      val cloudFormationOptions = Seq(AwsAccountId("account-id"), AwsRoleName("role-name"))
      val roleArn = Option("arn:aws:iam::account-id:role/role-name")
      val deployEnvironment = Option("Admin")
      val changeSetName = None
      val client = mock[CloudFormationClient]

      val parameterCaptor = capture[List[(String, String)]]

      client.createOrUpdateTemplate(any[String], any[String], parameterCaptor.capture, any[Option[String]], any[Option[String]]) returns Future.successful("stack-id")

      val output = testClass.deployStack(stackName, input, params, roleArn, deployEnvironment, parameterName, changeSetName, client)

      parameterCaptor.value must contain(parameterName → "Admin")
      parameterCaptor.value must not(contain(parameterName → "NotAdmin"))
      parameterCaptor.value must contain("param1" → "value1")
    }
  }

  "runStackTemplateBuilder" should {
    trait RunStackTemplateBuilderMocks { this: Setup ⇒
      val outputFile = mock[File] withBehavior (_.getCanonicalPath returns "path")
      val scalaRun = mock[ScalaRun]
      val fileOnClasspath = mock[File]
      val attributedClasspath = Seq(Attributed(fileOnClasspath)(AttributeMap()))
      val classpath = Seq(fileOnClasspath)
    }

    "run without issue" in new Setup with RunStackTemplateBuilderMocks {
      scalaRun.run("mainClass", classpath, Seq("path"), log) returns Try {}

      val output = testClass.runStackTemplateBuilder(Option("mainClass"), outputFile, scalaRun, attributedClasspath, streams)

      output must_== outputFile
    }

    "throw an exception if the main class wasn't found" in new Setup with RunStackTemplateBuilderMocks {
      testClass.runStackTemplateBuilder(None, outputFile, scalaRun, attributedClasspath, streams) must throwA[NoMainClassDetectedException]
    }

    "throw an exception if the runner returns a non-zero exit code" in new Setup with RunStackTemplateBuilderMocks {
      scalaRun.run("mainClass", classpath, Seq("path"), log) returns Try {
        throw new RuntimeException("my-error", null, true, false) {}
      }

      testClass.runStackTemplateBuilder(Option("mainClass"), outputFile, scalaRun, attributedClasspath, streams) must throwA[RuntimeException].like {
        case ex ⇒ ex.getMessage must_== "my-error"
      }
    }
  }
}
