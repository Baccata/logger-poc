package logger.fs2interop

import cats.effect.kernel.Async
import cats.effect.kernel.Fiber
import cats.effect.kernel.Resource
import cats.effect.std.QueueSink
import cats.effect.syntax.spawn._
import cats.effect.syntax.temporal._
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.concurrent.Channel

import scala.concurrent.duration.FiniteDuration

/** Courtesy of @systemfw
  *
  * Multiple-producers, single-consumer bounded queue that has a graceful
  * closure that waits a certain duration before terminating consumption.
  */
object StreamFunnel {
  def apply[F[_]: Async, T](
      capacity: Int,
      drainTimeout: FiniteDuration,
      process: fs2.Pipe[F, T, Unit]
  ): Resource[F, QueueSink[F, T]] = {
    for {
      channel <- Resource.eval(Channel.bounded[F, T](capacity))
      _ <- consume(channel, drainTimeout)(process)
    } yield new QueueSink[F, T] {

      /** channel.send returns a Either[Channel.Closed, Unit]. When you get a
        * Channel.Closed, it meant that the `send` call was a no-op.
        *
        * channel.send can semantically block, which applies back-pressure
        */
      override def offer(a: T): F[Unit] = channel.send(a).void

      /** channel.trySend returns a Either[Channel.Closed, Unit]. When you get a
        * Channel.Closed, it meant that the `send` call was a no-op. we convert
        * it to `false` to signal the fact that `a` was not sent in the channel
        *
        * this method does not block
        */
      override def tryOffer(a: T): F[Boolean] =
        channel.trySend(a).map(_.getOrElse(true))
    }
  }

  private def consume[F[_]: Async, T](
      channel: Channel[F, T],
      drainTimeout: FiniteDuration
  )(sink: fs2.Pipe[F, T, Unit]): Resource[F, Unit] = {
    def gracefulClosure(f: Fiber[F, Throwable, Unit]) =
      channel.close >> f.join.void.timeoutTo(drainTimeout, f.cancel)
    val consumer = channel.stream
      .through(sink)
      .compile
      .drain
    Resource.make(consumer.start)(gracefulClosure).void
  }
}
