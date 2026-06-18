//package resume
//
//import io.circe.{Decoder, Encoder}
//import io.circe.generic.semiauto._
//
//case class User(
//    id: Long,
//    email: String,
//    name: String,
//    passwordHash: String,
//)
//
//object User {
//  implicit val decoder: Decoder[User] = deriveDecoder
//  implicit val encoder: Encoder[User] = deriveEncoder
//}
