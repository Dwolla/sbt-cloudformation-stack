package com.dwolla.sbt.cloudformation

import cats.effect._
import com.amazonaws.services.cloudformation._
import com.dwolla.fs2aws.cloudformation._

class IOCloudFormationClient(client: AmazonCloudFormationAsync = AmazonCloudFormationAsyncClientBuilder.defaultClient()) extends CloudFormationClientImpl[IO](client)
