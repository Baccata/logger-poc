package logger.fs2interop

import logger.LoggerKernel
import cats.effect.Temporal
import cats.syntax.all._
import logger.Log

object AddTimestamp {

  def apply[F[_]: Temporal](logger: LoggerKernel[F]): LoggerKernel[F] =
    new LoggerKernel[F] {
      def log(record: Log => Log): F[Unit] = Temporal[F].realTime.flatMap {
        ts => logger.log(log => record(log.withTimestamp(ts)))
      }
    }

}
