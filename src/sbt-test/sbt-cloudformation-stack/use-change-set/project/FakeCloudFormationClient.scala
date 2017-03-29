import com.dwolla.awssdk.cloudformation.CloudFormationClient
import com.dwolla.awssdk.cloudformation.CloudFormationClient.StackID

import scala.concurrent.Future

object FakeCloudFormationClient extends CloudFormationClient {
  override def createOrUpdateTemplate(stackName: String,
                                      template: String,
                                      params: List[(String, String)],
                                      roleArn: Option[String] = None,
                                      changeSetName: Option[String] = None): Future[StackID] = Future.successful(
    s"""$stackName:
       |
       |$template
       |
       |with parameters:
       |$params
       |
       |with role ARN: $roleArn
       |change set name: $changeSetName
       |""".stripMargin
  )
}
