package com.dwolla.sbt.cloudformation

import java.io.File

import com.dwolla.awssdk.cloudformation.CloudFormationClient
import com.dwolla.testutils.WithBehaviorMocking
import com.dwolla.util.Environment
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import sbt.Keys.TaskStreams
import sbt.{AttributeMap, Attributed, Logger, ScalaRun}

import scala.concurrent.Future

class CloudFormationStackPluginSpec extends Specification with Mockito with WithBehaviorMocking {

  class Setup(environment: (String, String)*) extends Scope {
    val testClass = new CloudFormationStackPlugin(FakeEnvironment(Map(environment: _*)))

    val log = mock[Logger]
    val streams = mock[TaskStreams] withBehavior (_.log returns log)
  }

  case class FakeEnvironment(map: Map[String, String]) extends Environment {
    override def get(name: String): Option[String] = map.get(name)
  }

  "defaultTemplateJsonFilename" should {
    "be set correctly" in new Setup {
      testClass.defaultTemplateJsonFilename must_== "cloudformation-template.json"
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
      scalaRun.run("mainClass", classpath, Seq("path"), log) returns None

      val output = testClass.runStackTemplateBuilder(Option("mainClass"), outputFile, scalaRun, attributedClasspath, streams)

      output must_== outputFile
    }

    "throw an exception if the main class wasn't found" in new Setup with RunStackTemplateBuilderMocks {
      testClass.runStackTemplateBuilder(None, outputFile, scalaRun, attributedClasspath, streams) must throwA[NoMainClassDetectedException]
    }

    "throw an exception if the runner returns a non-zero exit code" in new Setup with RunStackTemplateBuilderMocks {
      scalaRun.run("mainClass", classpath, Seq("path"), log) returns Option("my-error")

      testClass.runStackTemplateBuilder(Option("mainClass"), outputFile, scalaRun, attributedClasspath, streams) must throwA[RuntimeException].like {
        case ex ⇒ ex.getMessage must_== "my-error"
      }
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
      val roleArn = Option("role-arn")
      val client = mock[CloudFormationClient] withBehavior (_.createOrUpdateTemplate(stackName, input, params, roleArn) returns Future.successful("stack-id"))

      val output = testClass.deployStack(stackName, input, params, roleArn, client)

      output must_== "stack-id"
    }
  }

}
