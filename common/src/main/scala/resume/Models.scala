package resume

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

case class User(id: Long, email: String, name: String, password: String)

case class SignupRequest(email: String, password: String, name: String)
case class LoginRequest(email: String, password: String)
case class AuthResponse(token: String, user: User)

object SignupRequest {
  implicit val decoder: Decoder[SignupRequest] = deriveDecoder
  implicit val encoder: Encoder[SignupRequest] = deriveEncoder
}
object LoginRequest {
  implicit val decoder: Decoder[LoginRequest] = deriveDecoder
  implicit val encoder: Encoder[LoginRequest] = deriveEncoder
}
object AuthResponse {
  implicit val decoder: Decoder[AuthResponse] = deriveDecoder
  implicit val encoder: Encoder[AuthResponse] = deriveEncoder
}
object User {
  implicit val decoder: Decoder[User] = deriveDecoder
  implicit val encoder: Encoder[User] = deriveEncoder
}
