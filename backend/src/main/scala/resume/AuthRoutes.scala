package resume

import java.util.UUID

import cats.effect.{IO, Ref}
import cats.implicits.catsSyntaxApplicativeId
import org.http4s.{HttpRoutes, Response}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._

class AuthRoutes(
    usersRef: Ref[IO, List[User]],
    tokensRef: Ref[IO, Map[String, Long]],
    nextIdRef: Ref[IO, Long],
) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "register" =>
      req.as[SignupRequest].flatMap { signup =>
        usersRef.get.flatMap { users =>
          if (users.exists(u => (u.login == signup.login) || (u.email == signup.email)))
            Conflict("Email/login already exists")
          else {
            nextIdRef.get.flatMap { id =>
              val user = User(id, signup.email, signup.login, signup.password)
              val token = UUID.randomUUID().toString
              for {
                _ <- usersRef.update(_ :+ user)
                _ <- nextIdRef.update(_ + 1)
                _ <- tokensRef.update(_ + (token -> id))
                resp <- Ok(AuthResponse(token, user.copy(password = "")))
              } yield resp
            }
          }
        }
      }

    case req @ POST -> Root / "login" =>
      req.as[LoginRequest].flatMap { request =>
        usersRef.get
          .map(
            _.find(u =>
              (u.email == request.emailOrLogin || u.login == request.emailOrLogin) &&
                u.password == request.password,
            ),
          )
          .flatMap {
            case None =>
              Response[IO](status = Unauthorized)
                .withEntity("Invalid credentials")
                .pure[IO]
            case Some(user) =>
              val token = UUID.randomUUID().toString
              tokensRef.update(_ + (token -> user.id)).flatMap { _ =>
                Ok(AuthResponse(token, user.copy(password = "")))
              }
          }
      }
  }
}
