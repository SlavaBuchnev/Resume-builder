package resume

import java.util.UUID

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

case class AppState(
    users: List[User],
    tokens: Map[String, UUID],//ToDo: use tokens for after implement exit
)

object AppState {
  implicit val decoder: Decoder[AppState] = deriveDecoder
  implicit val encoder: Encoder[AppState] = deriveEncoder
}
