package com.dwolla.sbt.cloudformation

import java.io.File

import com.dwolla.awssdk.cloudformation.CloudFormationClient
import com.dwolla.testutils.WithBehaviorMocking
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import sbt.Keys.TaskStreams
import sbt.{AttributeMap, Attributed, ScalaRun}
import sbt.internal.util.ManagedLogger

import scala.concurrent.Future
import scala.util.Try

class CloudFormationStackPluginSpec extends Specification with Mockito with WithBehaviorMocking {

  class Setup extends Scope {
    val testClass = new CloudFormationStackPlugin

    val log = mock[ManagedLogger]
    val streams = mock[TaskStreams] withBehavior (_.log returns log)
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
