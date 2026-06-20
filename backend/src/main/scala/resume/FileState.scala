package resume

import java.nio.file.{Files, Path, StandardCopyOption}

import cats.effect.IO
import io.circe.parser.decode
import io.circe.syntax.EncoderOps

class FileState(path: Path) {
  def load(): IO[AppState] = IO.blocking {
    if (Files.exists(path)) {
      val content = Files.readString(path)
      decode[AppState](content)
        .getOrElse(AppState(Nil, Map.empty))
    } else {
      AppState(Nil, Map.empty)
    }
  }

  def save(state: AppState): IO[Unit] = IO.blocking {
    Files.createDirectories(path.getParent)
    val tmp = path.resolveSibling(path.getFileName.toString + ".tmp")
    Files.writeString(tmp, state.asJson.noSpaces)
    Files.move(tmp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
  }

}
