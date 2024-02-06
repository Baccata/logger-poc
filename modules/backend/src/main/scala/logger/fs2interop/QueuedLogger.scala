package logger
package fs2interop

import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.QueueSink

import scala.concurrent.duration.FiniteDuration
import logger.Log

final class QueuedLogger[F[_]] private[fs2interop] (
    recordsSink: QueueSink[F, Log]
) extends LoggerKernel[F] {
  def log(record: Log => Log): F[Unit] = recordsSink.offer(record(Log.mutableBuilder()))
}

object QueuedLogger {


  def make[F[_]: Async](
      capacity: Int,
      drainTimeout: FiniteDuration,
      sink: fs2.Pipe[F, Log, Unit]
  ): Resource[F, LoggerKernel[F]] = {
    StreamFunnel[F, Log](capacity, drainTimeout, sink).map(q =>
      new QueuedLogger(q)
    )
  }

}
