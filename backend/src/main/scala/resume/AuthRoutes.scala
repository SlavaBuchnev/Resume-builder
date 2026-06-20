package resume

import scala.util.Try

import java.util.UUID

import cats.effect.IO
import cats.implicits._
import org.http4s.{HttpRoutes, Response}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._

class AuthRoutes(
    persistentRef: PersistentRef,
    resumeStore: ResumeStore,
) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case req @ POST -> Root / "register" =>
      req.as[SignupRequest].flatMap { signup =>
        persistentRef.findUserByEmailOrLogin(signup.email).flatMap {
          case Some(_) =>
            Conflict("Email or Login already exists")
          case None =>
            for {
              pwHash <- PasswordUtils.hash(signup.password)
              user = User(UUID.randomUUID(), signup.email, signup.login, pwHash)
              _ <- persistentRef.addUser(user)
              resp <- Ok(AuthResponse(UUID.randomUUID().toString, user.copy(password = "")))
            } yield resp
        }
      }

    case req @ POST -> Root / "login" =>
      req.as[LoginRequest].flatMap { loginReq =>
        persistentRef.findUserByEmailOrLogin(loginReq.emailOrLogin).flatMap {
          case None =>
            Response[IO](status = Unauthorized)
              .withEntity("Invalid credentials")
              .pure[IO]
          case Some(user) =>
            PasswordUtils.check(loginReq.password, user.password).flatMap {
              case false =>
                Response[IO](status = Unauthorized)
                  .withEntity("Invalid credentials")
                  .pure[IO]
              case true =>
                Ok(AuthResponse(UUID.randomUUID().toString, user.copy(password = "")))
            }
        }
      }

    case GET -> Root / "resume" / userIdStr =>
      Try(UUID.fromString(userIdStr)).fold(
        _ => BadRequest("Invalid user ID"),
        userId =>
          resumeStore.get(userId).flatMap {
            case Some(html) => Ok(html)
            case None => NotFound()
          },
      )
  }
}
