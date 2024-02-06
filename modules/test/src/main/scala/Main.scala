package test

import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect.IO
import logger._
import logger.fs2interop._
import logger.frontend.Logger
import cats.syntax.all._

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    QueuedLogger
      .toStdout[IO]()
      .map(AddTimestamp(_))
      .map(Logger.wrap(_))
      .use { logger =>
        summon[Context.Encoder[String]]

        logger.warn(
          _.withLevel(LogLevel.Warn)
            .withMessage("hello")
            .withContext("string")("some_string")
            .withContext("baz")(1)
            .withThrowable(new Exception("BOOM"))
        ) *>
          logger.info(
            "hello",
            "foo" -> "string",
            "baz" -> 1,
            new Exception("KABOOM")
          )
      }
      .as(ExitCode.Success)
}
