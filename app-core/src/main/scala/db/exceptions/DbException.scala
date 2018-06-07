package db.exceptions

/**
 * Indicates that an error occurred during a database query.
 *
 * @param msg   The error message.
 * @param cause The exception cause.
 */
class DbException(msg: String, cause: Option[Throwable] = None)
  extends RuntimeException(msg, cause.orNull)
