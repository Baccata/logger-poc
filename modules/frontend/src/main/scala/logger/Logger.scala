//> using lib "com.lihaoyi::sourcecode:0.3.1"
package logger
package frontend

abstract class Logger[F[_]] extends LoggerKernel[F] {

  final def info(logBit: LogRecord, others: LogRecord*)(implicit
      pkg: sourcecode.Pkg,
      filename: sourcecode.FileName,
      name: sourcecode.Name,
      line: sourcecode.Line
  ): F[Unit] = log_(LogLevel.Info, logBit, others: _*)

  final def warn(logBit: LogRecord, others: LogRecord*)(implicit
      pkg: sourcecode.Pkg,
      filename: sourcecode.FileName,
      name: sourcecode.Name,
      line: sourcecode.Line
  ): F[Unit] = log_(LogLevel.Warn, logBit, others: _*)

  final def error(logBit: LogRecord, others: LogRecord*)(implicit
      pkg: sourcecode.Pkg,
      filename: sourcecode.FileName,
      name: sourcecode.Name,
      line: sourcecode.Line
  ): F[Unit] = log_(LogLevel.Error, logBit, others: _*)

  final def trace(logBit: LogRecord, others: LogRecord*)(implicit
      pkg: sourcecode.Pkg,
      filename: sourcecode.FileName,
      name: sourcecode.Name,
      line: sourcecode.Line
  ): F[Unit] = log_(LogLevel.Trace, logBit, others: _*)

  final def debug(logBit: LogRecord, others: LogRecord*)(implicit
      pkg: sourcecode.Pkg,
      filename: sourcecode.FileName,
      name: sourcecode.Name,
      line: sourcecode.Line
  ): F[Unit] = log_(LogLevel.Debug, logBit, others: _*)

  private final def log_(
      level: LogLevel,
      bit: LogRecord,
      others: LogRecord*
  )(implicit
      pkg: sourcecode.Pkg,
      filename: sourcecode.FileName,
      name: sourcecode.Name,
      line: sourcecode.Line
  ): F[Unit] = {
    log(
      level,
      (record: Log.Builder) =>
        LogRecord.combine(others)(
          bit(
            record
              .withLevel(level)
              .withClassName(pkg.value + "." + name.value)
              .withFileName(filename.value)
              .withLine(line.value)
          )
        )
    )
  }

}

object Logger {

  def wrap[F[_]](kernel: LoggerKernel[F]): Logger[F] = new Logger[F] {
    def log(level: LogLevel, record: Log.Builder => Log.Builder): F[Unit] =
      kernel.log(level, record)
  }

}
