package com.dwolla.sbt.cloudformation

import com.dwolla.sbt.cloudformation.CloudFormationStack.autoImport._
import sbt.complete.DefaultParsers._
import sbt.complete.Parser
import sbt.{Project, State}

object CloudFormationStackParsers {
  val awsAccountIdParser: Parser[AwsAccountId] = charClass(_.isDigit, "digit").+.map(_.mkString).filter(_.length == 12, s ⇒ s"`$s` is not a 12-digit AWS Account ID").map(AwsAccountId)
  val awsRoleNameParser: Parser[AwsRoleName] = ("role/" ~> (charClass(_.isLetterOrDigit, "alphanumeric") | chars("+=,.@_-")).+.map(_.mkString)).map(AwsRoleName)

  private def selectSome(items: Seq[(String, Parser[CloudFormationOption])]): Parser[Seq[CloudFormationOption]] = {
    def select1(items: Seq[Parser[CloudFormationOption]]): Parser[CloudFormationOption] = {
      val combined: Parser[CloudFormationOption] = items.reduceLeft(_ | _)
      token(Space ~> combined)
    }

    select1(items.map(_._2)).flatMap { (v: CloudFormationOption) ⇒
      val remaining = items.filter { tuple ⇒
        tuple._1 != v.getClass.getCanonicalName
      }
      if (remaining.isEmpty)
        success(v :: Nil)
      else
        selectSome(remaining).?.map(v +: _.getOrElse(Seq()))
    }
  }

  def buildCloudFormationStackParser(state: State): Parser[Seq[CloudFormationOption]] = {
    val generatedEnvironmentParser = Project.extract(state)
      .get(deployEnvironmentOptions)
      .map(Parser.literal)
      .reduce(_ | _)
      .map(Environment)

    OptSpace ~> selectSome(Seq(
      // these tuples are a terrible hack
      classOf[AwsRoleName].getCanonicalName → awsRoleNameParser,
      classOf[AwsAccountId].getCanonicalName → awsAccountIdParser,
      classOf[Environment].getCanonicalName → generatedEnvironmentParser
    )).?.map {
      case Some(x) ⇒ x
      case None ⇒ Seq.empty[CloudFormationOption]
    }
  }
}
