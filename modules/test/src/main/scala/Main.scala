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
        // This is a direct call to the logger kernel interface.
        // The user is expected to provide a function to add information
        // to some log builder.
        //
        // This is applying inversion of control so that the
        // LoggerKernel implementation can decide on the memory layout
        // of the data.
        val loggerKernelCall = logger
          .log(
            _.withLevel(LogLevel.Warn)
              .withMessage("hello")
              .withContext("string")("some_string")
              .withContext("int")(1)
              .withThrowable(new Exception("BOOM"))
          )

        // This showcases a higher level UX, using a varargs-taking methods
        // combined with the "typeclassed" pattern in order to capture the same
        // information in a more concise way.
        //
        // Additionally, the higher-level interface depends on Haoyi's Sourcecode
        // library, automating the capture of information about where
        // the log statement was issued (classname, file, line).
        val higherLevelLoggerCall = logger.info(
          "hello",
          "string" -> "some_string",
          "int" -> 1,
          new Exception("KABOOM")
        )
        loggerKernelCall *> higherLevelLoggerCall
      }
      .as(ExitCode.Success)
}
