package logger
package fs2interop

import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.QueueSink

import scala.concurrent.duration._
import logger.Log
import java.nio.charset.StandardCharsets

object QueuedLogger {

  def toStdout[F[_]: Async](
      capacity: Int = 1024,
      drainTimeout: FiniteDuration = 1.second
  ): Resource[F, LoggerKernel[F]] =
    make(
      capacity,
      drainTimeout,
      _.map(SingleLineRenderer.render)
        .through(fs2.text.utf8Encode)
        .through(fs2.io.stdout)
    )

  def make[F[_]: Async](
      capacity: Int,
      drainTimeout: FiniteDuration,
      sink: fs2.Pipe[F, Log, Unit]
  ): Resource[F, LoggerKernel[F]] = {
    StreamFunnel[F, Log](capacity, drainTimeout, sink).map(q => new Impl(q))
  }

  private[fs2interop] final class Impl[F[_]](
      recordsSink: QueueSink[F, Log]
  ) extends LoggerKernel[F] {
    def log(record: Log => Log): F[Unit] = {
      recordsSink.offer(record(Log.mutable()))
    }
  }

}
