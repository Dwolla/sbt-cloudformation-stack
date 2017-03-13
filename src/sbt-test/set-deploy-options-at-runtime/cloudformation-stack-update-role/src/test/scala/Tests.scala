import org.specs2.mutable.Specification

class Tests extends Specification {
  "Stack" should {
    "resolve, because the cloudformation-stack-test config depends on the cloudformation-stack config" in {
      Stack.stack must_==
        """{
          |  "template": true
          |}""".stripMargin
    }
  }
}
