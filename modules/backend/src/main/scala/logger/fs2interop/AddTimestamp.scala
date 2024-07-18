package logger.fs2interop

import logger.LoggerKernel
import cats.effect.Temporal
import cats.syntax.all._
import logger.Log
import logger.LogLevel

object AddTimestamp {

  def apply[F[_]: Temporal](logger: LoggerKernel[F]): LoggerKernel[F] =
    new LoggerKernel[F] {
      def log(level: LogLevel, record: Log.Builder => Log.Builder): F[Unit] =
        Temporal[F].realTime.flatMap { ts =>
          logger.log(level, log => record(log.withTimestamp(ts)))
        }
    }

}
