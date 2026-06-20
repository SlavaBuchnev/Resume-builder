package resume

import cats.effect.IO
import org.mindrot.jbcrypt.BCrypt

object PasswordUtils {
  def hash(plain: String): IO[String] =
    IO.blocking(BCrypt.hashpw(plain, BCrypt.gensalt()))

  def check(plain: String, hashed: String): IO[Boolean] =
    IO.blocking(BCrypt.checkpw(plain, hashed))
}
