import java.io.PrintWriter
import java.nio.charset.Charset
import java.io.File
import resource._

object Stack extends App {
  for {
    output ← managed(new PrintWriter(new File(args(0)), "utf8"))
  } {
    output.write(stack)
  }

  def stack: String =
    """{
      |  "template": true
      |}""".stripMargin
}
