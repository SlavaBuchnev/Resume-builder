package resume

import java.nio.file.{Files, Path}

import cats.effect.IO
import Types.UserId

class ResumeStore(baseDir: Path) {
  def save(userId: UserId, html: String): IO[Unit] = IO.blocking {
    Files.createDirectories(baseDir)
    Files.writeString(baseDir.resolve(s"$userId.html"), html)
  }

  def get(userId: UserId): IO[Option[String]] = IO.blocking {
    val file = baseDir.resolve(s"$userId.html")
    if (Files.exists(file)) Some(Files.readString(file)) else None
  }
}
