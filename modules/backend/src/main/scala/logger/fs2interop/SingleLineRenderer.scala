package logger.fs2interop

import logger.Log
import scala.collection.mutable
import logger.JsonLike
import scala.concurrent.duration.FiniteDuration
import io.AnsiColor
import logger.LogLevel

object SingleLineRenderer {

  val space = " "

  // This is just an example implementation, it's not cross-platform
  def render(log: Log): String = {
    val context = log.context
    val builder = mutable.StringBuilder.newBuilder
    val colour = log.level match {
      case LogLevel.Info  => AnsiColor.CYAN
      case LogLevel.Warn  => AnsiColor.YELLOW
      case LogLevel.Error => AnsiColor.RED
      case _              => AnsiColor.GREEN
    }
    builder.append(colour)
    log.timestamp.foreach { ts =>
      val instant = java.time.Instant.ofEpochSecond(ts.toSeconds)
      builder
        .append('[')
        .append(instant.toString())
        .append(']')
    }
    builder
      .append('[')
      .append(log.level.name)
      .append(']')
      .append(space)
      .append(AnsiColor.RESET)

    builder.append(log.message).append(space)
    log.throwable.foreach { throwable =>
      builder
        .append(AnsiColor.RED)
        .append("[")
        .append(throwable.getClass().getName())
        .append(':')
        .append(throwable.getMessage())
        .append(']')
        .append(AnsiColor.RESET)
        .append(space)
    }
    log.context.foreachEntry { case (k, v) =>
      v.capture(ContextRenderer)(builder.append(k).append(":"))
        .append(space)
    }
    builder.append(System.lineSeparator())
    builder.result()
  }

  private object ContextRenderer extends JsonLike {
    type J = (StringBuilder => mutable.StringBuilder)

    def timestamp(ts: FiniteDuration): J = { builder =>
      val instant = java.time.Instant.ofEpochSecond(ts.toSeconds)
      builder.append(instant.toString())
    }
    def bool(value: Boolean): J = _.append(value)
    def int(value: Int): J = _.append(value)
    def double(value: Double): J = _.append(value)
    def short(value: Int): J = _.append(value)
    def string(value: String): J = _.append(value)
    def long(value: Int): J = _.append(value)
    def nul: J = _.append("null")
    def obj(bindings: (String, J)*): J = _.append("{...}")
    def arr(elems: J*): J = _.append("[...]")
  }

}
