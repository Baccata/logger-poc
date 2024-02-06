package logger

import scala.collection.{Map => MapLike}
import scala.collection.mutable.{Map => MMap}
import scala.concurrent.duration.FiniteDuration

/** Low-level interface exposing methods to enrich a log record with relevant
  * information. The methods are designed to capture elements that cannot be
  * easily captured from a monadic context (or by running an effect). Elements
  * such as timestamps should be provided by means of middlewares.
  */
trait Log {

  def timestamp: Option[FiniteDuration]
  def level: LogLevel
  def levelValue: Double
  def message: String
  def throwable: Option[Throwable]
  def context: Map[String, Context]
  def fileName: Option[String]
  def className: Option[String]
  def methodName: Option[String]
  def line: Option[Int]

  def unsafeThrowable: Throwable
  def unsafeContext: MapLike[String, Context]

  def withTimestamp(value: FiniteDuration): Log
  def withLevel(level: LogLevel): Log
  def withLevelValue(levelValue: Double): Log
  def withMessage(message: => String): Log
  def withThrowable(throwable: Throwable): Log
  def withContext(name: String)(f: Context): Log
  def withFileName(name: String): Log
  def withClassName(name: String): Log
  def withLine(line: Int): Log

  final def withContextMap[A: Context.Encoder](mdc: Map[String, A]): Log = {
    var log = this
    mdc.foreach { case (k, v) =>
      log = withContext(k)(v)
    }
    log
  }

}

object Log {

  def mutable(): Log = new MutableBuilder()

  private class MutableBuilder private[Log] () extends Log {

    def timestamp: Option[FiniteDuration] = Option(_timestamp)
    def level: LogLevel = if (_level == null) LogLevel.Debug else _level
    def levelValue: Double =
      if (_levelValue < 0) level.value else _levelValue
    def message: String = if (_message == null) "" else _message
    def throwable: Option[Throwable] = Option(_throwable)
    def context: Map[String, Context] =
      if (_context == null) Map.empty else _context.toMap

    def className: Option[String] = Option(_className)
    def fileName: Option[String] = Option(_fileName)
    def methodName: Option[String] = Option(_methodName)
    def line: Option[Int] = Some(_line).filter(_ > 0)

    def unsafeThrowable: Throwable = _throwable
    def unsafeContext: MapLike[String, Context] = _context

    private var _timestamp: FiniteDuration = null
    private var _level: LogLevel = null
    private var _levelValue: Double = -1
    private var _message: String = null
    private var _throwable: Throwable = null
    private var _context: MMap[String, Context] = null
    private var _fileName: String = null
    private var _className: String = null
    private var _methodName: String = null
    private var _line: Int = -1

    def withTimestamp(value: FiniteDuration): this.type = {
      this._timestamp = value
      this
    }

    def withLevel(level: LogLevel): this.type = {
      this._level = level
      this
    }

    def withLevelValue(levelValue: Double): this.type = {
      this._levelValue = levelValue
      this
    }

    def withMessage(message: => String): this.type = {
      this._message = message
      this
    }

    def withThrowable(throwable: Throwable): this.type = {
      this._throwable = throwable
      this
    }

    def withContext(name: String)(value: Context): this.type = {
      if (this._context == null) {
        this._context = MMap.empty[String, Context]
      }
      this._context += name -> value
      this
    }

    def withFileName(name: String): this.type = {
      this._fileName = name
      this
    }

    def withClassName(name: String): this.type = {
      this._className = name
      this
    }

    def withLine(line: Int): this.type = {
      this._line = line
      this
    }

  }

}
