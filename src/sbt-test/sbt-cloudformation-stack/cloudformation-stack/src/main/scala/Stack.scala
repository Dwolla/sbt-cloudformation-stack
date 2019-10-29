import java.io._
import cats.effect._

object Stack extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    Resource.fromAutoCloseable(IO(new PrintWriter(new File(args.head), "utf8")))
      .use(output => IO(output.write(stack)))
      .map(_ => ExitCode.Success)

  def stack: String =
    """{
      |  "template": true
      |}""".stripMargin
}
