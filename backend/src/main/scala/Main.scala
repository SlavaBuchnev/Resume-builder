import scala.collection.immutable.Map

import java.nio.file.Path

import cats.effect.{IO, IOApp, Ref, Resource}
import cats.effect.std.Semaphore
import com.comcast.ip4s._
import org.http4s.{HttpRoutes, StaticFile}
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.server.Router
import resume.{AppState, AuthRoutes, FileState, PersistentRef, ResumeStore, User}
import resume.Types.{Token, UserId}

object Main extends IOApp.Simple {

  def run: IO[Unit] = {
    val statePath = Path.of("data/state.json")
    val resumePath = Path.of("data/resumes")

    val fileState = new FileState(statePath)
    val resumeStore = new ResumeStore(resumePath)

    val serverResource = for {
      initialState <- Resource.eval(fileState.load())
      ref <- Resource.eval(Ref.of[IO, AppState](initialState))
      sem <- Resource.eval(Semaphore[IO](1))
      persistentRef = new PersistentRef(sem, ref, fileState)
      authRoutes = new AuthRoutes(persistentRef, resumeStore).routes

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
