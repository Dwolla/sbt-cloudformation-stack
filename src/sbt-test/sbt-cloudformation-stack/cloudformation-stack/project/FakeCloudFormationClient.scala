import cats.effect._
import com.dwolla.fs2aws.cloudformation._
import com.dwolla.sbt.cloudformation._

object FakeCloudFormationClient extends IOCloudFormationClient(FakeAmazonCloudFormationAsync) {
  override def createOrUpdateTemplate(stackName: String,
                                      template: String,
                                      params: List[(String, String)],
                                      roleArn: Option[String] = None,
                                      changeSetName: Option[String] = None): IO[StackID] = IO.pure(
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
