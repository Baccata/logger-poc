//> using lib "com.lihaoyi::sourcecode:0.3.1"
package logger
package frontend

abstract class Logger[F[_]] extends LoggerKernel[F] {

  final def info(logBit: LogRecord, others: LogRecord*)(implicit
      pkg: sourcecode.Pkg,
      filename: sourcecode.FileName,
      name: sourcecode.Name,
      line: sourcecode.Line
  ): F[Unit] = log(LogLevel.Info, logBit, others: _*)

  final def warn(logBit: LogRecord, others: LogRecord*)(implicit
      pkg: sourcecode.Pkg,
      filename: sourcecode.FileName,
      name: sourcecode.Name,
      line: sourcecode.Line
  ): F[Unit] = log(LogLevel.Warn, logBit, others: _*)

  final def error(logBit: LogRecord, others: LogRecord*)(implicit
      pkg: sourcecode.Pkg,
      filename: sourcecode.FileName,
      name: sourcecode.Name,
      line: sourcecode.Line
  ): F[Unit] = log(LogLevel.Error, logBit, others: _*)

  final def trace(logBit: LogRecord, others: LogRecord*)(implicit
      pkg: sourcecode.Pkg,
      filename: sourcecode.FileName,
      name: sourcecode.Name,
      line: sourcecode.Line
  ): F[Unit] = log(LogLevel.Trace, logBit, others: _*)

  final def debug(logBit: LogRecord, others: LogRecord*)(implicit
      pkg: sourcecode.Pkg,
      filename: sourcecode.FileName,
      name: sourcecode.Name,
      line: sourcecode.Line
  ): F[Unit] = log(LogLevel.Debug, logBit, others: _*)

  private final def log(
      level: LogLevel,
      bit: LogRecord,
      others: LogRecord*
  )(implicit
      pkg: sourcecode.Pkg,
      filename: sourcecode.FileName,
      name: sourcecode.Name,
      line: sourcecode.Line
  ): F[Unit] = {
    log((record: Log.Builder) =>
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
    def log(record: Log.Builder => Log.Builder): F[Unit] = kernel.log(record)
  }

}
