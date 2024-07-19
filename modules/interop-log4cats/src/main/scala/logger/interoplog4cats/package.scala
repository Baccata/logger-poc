package logger

import org.typelevel.log4cats.StructuredLogger
import scala.concurrent.duration.FiniteDuration
import org.typelevel.log4cats.{Logger => CatsLogger}

package object interoplog4cats {

  // scalafmt: {maxColumn = 120}
  def log4catsStructuredFrontend[F[_]](logger: LoggerKernel[F]): StructuredLogger[F] =
    new StructuredLogger[F] {
      override def warn(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
        logger.log(LogLevel.Warn, _.withMessage(msg).withThrowable(t).withContextMap(ctx))
      override def warn(ctx: Map[String, String])(msg: => String): F[Unit] =
        logger.log(LogLevel.Warn, _.withMessage(msg).withContextMap(ctx))
      override def warn(t: Throwable)(message: => String): F[Unit] =
        logger.log(LogLevel.Warn, _.withMessage(message).withThrowable(t))
      override def warn(message: => String): F[Unit] =
        logger.log(LogLevel.Warn, _.withMessage(message))
      override def debug(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
        logger.log(LogLevel.Debug, _.withMessage(msg).withThrowable(t).withContextMap(ctx))
      override def debug(ctx: Map[String, String])(msg: => String): F[Unit] =
        logger.log(LogLevel.Debug, _.withMessage(msg).withContextMap(ctx))
      override def debug(t: Throwable)(message: => String): F[Unit] =
        logger.log(LogLevel.Debug, _.withMessage(message).withThrowable(t))
      override def debug(message: => String): F[Unit] =
        logger.log(LogLevel.Debug, _.withMessage(message))
      override def info(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
        logger.log(LogLevel.Info, _.withMessage(msg).withThrowable(t).withContextMap(ctx))
      override def info(ctx: Map[String, String])(msg: => String): F[Unit] =
        logger.log(LogLevel.Info, _.withMessage(msg).withContextMap(ctx))
      override def info(t: Throwable)(message: => String): F[Unit] =
        logger.log(LogLevel.Info, _.withMessage(message).withThrowable(t))
      override def info(message: => String): F[Unit] =
        logger.log(LogLevel.Info, _.withMessage(message))
      override def trace(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
        logger.log(LogLevel.Trace, _.withMessage(msg).withThrowable(t).withContextMap(ctx))
      override def trace(ctx: Map[String, String])(msg: => String): F[Unit] =
        logger.log(LogLevel.Trace, _.withMessage(msg).withContextMap(ctx))
      override def trace(t: Throwable)(message: => String): F[Unit] =
        logger.log(LogLevel.Trace, _.withMessage(message).withThrowable(t))
      override def trace(message: => String): F[Unit] =
        logger.log(LogLevel.Trace, _.withMessage(message))
      override def error(ctx: Map[String, String], t: Throwable)(msg: => String): F[Unit] =
        logger.log(LogLevel.Error, _.withLevel(LogLevel.Error).withMessage(msg).withThrowable(t).withContextMap(ctx))
      override def error(ctx: Map[String, String])(msg: => String): F[Unit] =
        logger.log(LogLevel.Error, _.withMessage(msg).withContextMap(ctx))
      override def error(t: Throwable)(message: => String): F[Unit] =
        logger.log(LogLevel.Error, _.withMessage(message).withThrowable(t))
      override def error(message: => String): F[Unit] =
        logger.log(LogLevel.Error, _.withMessage(message))
    }

  def log4catsStructuredBackend[F[_]](logger: StructuredLogger[F]): LoggerKernel[F] = new LoggerKernel[F] {
    def log(level: LogLevel, record: Log.Builder => Log.Builder): F[Unit] = {
      val log = record(Log.mutableBuilder().withLevel(level)).build()
      val ctx = log.unsafeContext
      val log4catsContext = locally {
        if (ctx != null) {
          var builder = new scala.collection.mutable.HashMap[String, String]()
          ctx.foreachEntry { (key, v) =>
            val value: String = v.capture(stringlyJsonLike)
            if (value != null) builder.put(key, value)
          }
          builder
        } else null
      }
      (log.level, log.unsafeThrowable, log4catsContext) match {
        // WARN
        case (LogLevel.Warn, null, null)         => logger.warn(log.message)
        case (LogLevel.Warn, throwable, null)    => logger.warn(throwable)(log.message)
        case (LogLevel.Warn, null, context)      => logger.warn(context.toMap)(log.message)
        case (LogLevel.Warn, throwable, context) => logger.warn(context.toMap, throwable)(log.message)
        // DEBUG
        case (LogLevel.Debug, null, null)         => logger.debug(log.message)
        case (LogLevel.Debug, throwable, null)    => logger.debug(throwable)(log.message)
        case (LogLevel.Debug, null, context)      => logger.debug(context.toMap)(log.message)
        case (LogLevel.Debug, throwable, context) => logger.debug(context.toMap, throwable)(log.message)
        // INFO
        case (LogLevel.Info, null, null)         => logger.info(log.message)
        case (LogLevel.Info, throwable, null)    => logger.info(throwable)(log.message)
        case (LogLevel.Info, null, context)      => logger.info(context.toMap)(log.message)
        case (LogLevel.Info, throwable, context) => logger.info(context.toMap, throwable)(log.message)
        // ERROR
        case (LogLevel.Error, null, null)         => logger.error(log.message)
        case (LogLevel.Error, throwable, null)    => logger.error(throwable)(log.message)
        case (LogLevel.Error, null, context)      => logger.error(context.toMap)(log.message)
        case (LogLevel.Error, throwable, context) => logger.error(context.toMap, throwable)(log.message)
        // TRACE
        case (LogLevel.Trace, null, null)         => logger.trace(log.message)
        case (LogLevel.Trace, throwable, null)    => logger.trace(throwable)(log.message)
        case (LogLevel.Trace, null, context)      => logger.trace(context.toMap)(log.message)
        case (LogLevel.Trace, throwable, context) => logger.trace(context.toMap, throwable)(log.message)
      }
    }
  }

  def log4catsBackend[F[_]](logger: CatsLogger[F]): LoggerKernel[F] =
    if (logger.isInstanceOf[StructuredLogger[F]])
      log4catsBackend(logger.asInstanceOf[StructuredLogger[F]])
    else
      new LoggerKernel[F] {
        def log(level: LogLevel, record: Log.Builder => Log.Builder): F[Unit] = {
          val log = record(Log.mutableBuilder().withLevel(level)).build()
          val ctx = log.unsafeContext
          (log.level, log.unsafeThrowable) match {
            // WARN
            case (LogLevel.Warn, null)      => logger.warn(log.message)
            case (LogLevel.Warn, throwable) => logger.warn(throwable)(log.message)
            // DEBUG
            case (LogLevel.Debug, null)      => logger.debug(log.message)
            case (LogLevel.Debug, throwable) => logger.debug(throwable)(log.message)
            // INFO
            case (LogLevel.Info, null)      => logger.info(log.message)
            case (LogLevel.Info, throwable) => logger.info(throwable)(log.message)
            // ERROR
            case (LogLevel.Error, null)      => logger.error(log.message)
            case (LogLevel.Error, throwable) => logger.error(throwable)(log.message)
            // TRACE
            case (LogLevel.Trace, null)      => logger.trace(log.message)
            case (LogLevel.Trace, throwable) => logger.trace(throwable)(log.message)
          }
        }
      }

  private object stringlyJsonLike extends JsonLike {
    type J = String
    def string(value: String): J = value
    def bool(value: Boolean): J = value.toString()
    def int(value: Int): J = value.toString()
    def double(value: Double): J = value.toString()
    def long(value: Int): J = value.toString()
    def short(value: Int): J = value.toString
    def timestamp(ts: FiniteDuration): J = ts.toSeconds.toString()
    def nul: J = null
    def arr(elems: String*): J = null
    def obj(bindings: (String, String)*): J = null
  }

}
