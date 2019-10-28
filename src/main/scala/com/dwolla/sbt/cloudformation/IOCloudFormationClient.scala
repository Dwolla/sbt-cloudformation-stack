package com.dwolla.sbt.cloudformation

import cats.effect._
import cats.implicits._
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudformation._
import com.dwolla.fs2aws.cloudformation._

class IOCloudFormationClient(client: AmazonCloudFormationAsync) extends CloudFormationClientImpl[IO](client) {
  def this(region: Option[Regions]) =
    this(region.foldl(AmazonCloudFormationAsyncClientBuilder.standard())(_ withRegion _).build())

  def this() = this(None)
}
