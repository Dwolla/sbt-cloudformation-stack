import com.dwolla.awssdk.cloudformation.CloudFormationClient
import com.dwolla.awssdk.cloudformation.CloudFormationClient.StackID

import scala.concurrent.Future

object FakeCloudFormationClient extends CloudFormationClient {
  override def createOrUpdateTemplate(stackName: String,
                                      template: String,
                                      params: List[(String, String)]): Future[StackID] = Future.successful(
    s"""$stackName:
       |
       |$template
       |
       |with parameters:
       |$params
       |""".stripMargin
  )
}
