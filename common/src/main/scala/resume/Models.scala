package resume

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import Types._

case class SignupRequest(email: String, password: Password, login: String)
object SignupRequest {
  implicit val decoder: Decoder[SignupRequest] = deriveDecoder
  implicit val encoder: Encoder[SignupRequest] = deriveEncoder
}

case class LoginRequest(emailOrLogin: String, password: Password)
object LoginRequest {
  implicit val decoder: Decoder[LoginRequest] = deriveDecoder
  implicit val encoder: Encoder[LoginRequest] = deriveEncoder
}

case class AuthResponse(token: Token, user: User)
object AuthResponse {
  implicit val decoder: Decoder[AuthResponse] = deriveDecoder
  implicit val encoder: Encoder[AuthResponse] = deriveEncoder
}

case class User(
    id: UserId,
    email: String,
    login: String,
    password: Password,
)
object User {
  implicit val decoder: Decoder[User] = deriveDecoder
  implicit val encoder: Encoder[User] = deriveEncoder
}
