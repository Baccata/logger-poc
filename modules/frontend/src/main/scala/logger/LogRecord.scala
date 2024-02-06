package logger

/** This allows to capture several elements in vararg-based interface methods,
  * enriching a single log with various pieces of information.
  *
  * This allows for an interesting UX, where the details of the encoding of some
  * data into a log can be separate from the actual log statements.
  */
trait LogRecord extends (Log => Log)

object LogRecord {
  def combine(all: Seq[LogRecord]): LogRecord = Combined(all)

  implicit def toLogRecord[A: Loggable](value: => A): LogRecord =
    Loggable[A].record(value)

  private case class Combined(all: Seq[LogRecord]) extends LogRecord {
    def apply(record: Log): Log = {
      var current = record
      all.foreach { logBit =>
        current = logBit(current)
      }
      current
    }
  }
}
