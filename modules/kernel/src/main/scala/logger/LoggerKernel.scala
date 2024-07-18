package logger

/** This is the fundamental abstraction : a single-abstract-method interface
  * that has the following properties :
  *
  *   - Doesn't enforce a specific memory layout. More specific interfaces can
  *     back `LogRecord` with mutables/immutables values, and avoid storing
  *     things that are not important.
  *
  *   - LogRecord allows to precisely capture a lot of information. In
  *     particular, it does not enforce a `Map[String, String]` representation
  *     of context values that is not sufficient to leverage all the power from
  *     logging backends query engines, and without pulling a third-party JSON
  *     library.
  *
  *   - the SAM-like nature of the construct makes it inherently middleware
  *     friendly, as a single methods calls needs to be intercepted/ proxied in
  *     order to amend the behaviour of the logger.
  *
  * This also means that different libraries can use wrappers on top of this
  * kernel interface to use whatever UX is preferred without necessarily
  * imposing
  */
trait LoggerKernel[F[_]] {
  def log(level: LogLevel, record: Log.Builder => Log.Builder): F[Unit]
}
