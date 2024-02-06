package logger

/** Typeclass representing the notion that a value can contribute to a log, by
  * transforming it in some way.
  */
trait Loggable[A] {
  def record(value: => A): LogRecord
}

object Loggable {

  def apply[A](implicit ev: Loggable[A]): ev.type = ev

  implicit val stringLoggable: Loggable[String] = new Loggable[String] {
    def record(value: => String) = _.withMessage(value)
  }

  implicit def tupleLoggable[T: Context.Encoder]: Loggable[(String, Context)] =
    new Loggable[(String, Context)] {

      override def record(value: => (String, Context)): LogRecord = {
        val (k, v) = value
        (_: Log).withContext(k)(v)
      }

    }

  implicit def throwableLoggable[T <: Throwable]: Loggable[T] =
    new Loggable[T] {
      def record(value: => T): LogRecord = _.withThrowable(value)
    }

}
