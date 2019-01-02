package colog.slf4j

import cats.effect.{IO, LiftIO}
import colog.{LogRecord, Logger, Severity}
import org.slf4j.{LoggerFactory, Logger => JLogger}

object Slf4jLogger {

  def apply[F[_]](baseLogger: JLogger)(implicit F: LiftIO[F]): Logger[F, LogRecord] = {
    def unsafeLog(record: LogRecord)(f: (String, Throwable) => Unit): F[Unit] =
      F.liftIO(IO(f(record.message, record.error.orNull)))

    Logger { record =>
      val loggingFun: (String, Throwable) => Unit = record.severity match {
        case Severity.Debug => baseLogger.debug
        case Severity.Info => baseLogger.info
        case Severity.Warning => baseLogger.warn
        case Severity.Error => baseLogger.error
      }
      unsafeLog(record)(loggingFun)
    }
  }

  def forName[F[_]: LiftIO](name: String): Logger[F, LogRecord] =
    apply[F](LoggerFactory.getLogger(name))

  def forClass[F[_]: LiftIO](clazz: Class[_]): Logger[F, LogRecord] =
    apply[F](LoggerFactory.getLogger(clazz))

}