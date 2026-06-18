import scala.collection.immutable.Map

import cats.effect.{IO, IOApp, Ref, Resource}
import com.comcast.ip4s._
import org.http4s.{HttpRoutes, StaticFile}
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.server.Router
import resume.{AuthRoutes, User}

object Main extends IOApp.Simple {
  type Token = String
  type UserId = Long

  def run: IO[Unit] = {
    val serverResource = for {
      usersRef <- Resource.eval(Ref.of[IO, List[User]](Nil))
      tokensRef <- Resource.eval(Ref.of[IO, Map[Token, UserId]](Map.empty))
      nextIdRef <- Resource.eval(Ref.of[IO, Long](1L))
      authRoutes = new AuthRoutes(usersRef, tokensRef, nextIdRef).routes

      staticRoutes = HttpRoutes.of[IO] {
        case request @ GET -> Root =>
          StaticFile.fromResource("/web/index.html", Some(request)).getOrElseF(NotFound())
        case request @ GET -> _ =>
          StaticFile.fromResource("/web" + request.pathInfo.toString, Some(request)).getOrElseF(NotFound())
      }

      httpApp = Router("/api" -> authRoutes, "/" -> staticRoutes).orNotFound
      loggedApp = Logger.httpApp(true, true)(httpApp)

      server <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(loggedApp)
        .build
    } yield server

    serverResource.use(_ => IO.never)
  }
}
