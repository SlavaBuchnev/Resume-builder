package resume

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

case class SignupRequest(email: String, password: String, login: String)
object SignupRequest {
  implicit val decoder: Decoder[SignupRequest] = deriveDecoder
  implicit val encoder: Encoder[SignupRequest] = deriveEncoder
}

case class LoginRequest(emailOrLogin: String, password: String)
object LoginRequest {
  implicit val decoder: Decoder[LoginRequest] = deriveDecoder
  implicit val encoder: Encoder[LoginRequest] = deriveEncoder
}

case class AuthResponse(token: String, user: User)
object AuthResponse {
  implicit val decoder: Decoder[AuthResponse] = deriveDecoder
  implicit val encoder: Encoder[AuthResponse] = deriveEncoder
}

case class User(
    id: Long,
    email: String,
    login: String,
    password: String,
)
object User {
  implicit val decoder: Decoder[User] = deriveDecoder
  implicit val encoder: Encoder[User] = deriveEncoder
}
