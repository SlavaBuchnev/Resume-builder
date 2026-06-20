package resume

import cats.effect.{IO, Ref}
import cats.effect.std.Semaphore
import Types.{Token, UserId}

class PersistentRef(sem: Semaphore[IO], ref: Ref[IO, AppState], fileState: FileState) {

  private def update(f: AppState => AppState): IO[Unit] =
    sem.permit.use { _ =>
      ref.update(f).flatMap(_ => ref.get.flatMap(s => fileState.save(s)))
    }

  def get: IO[AppState] = ref.get

  def addUser(user: User): IO[Unit] =
    update(s => s.copy(users = s.users :+ user))

  def findUserByEmailOrLogin(emailOrLogin: String): IO[Option[User]] =
    get.map(_.users.find(u => u.email == emailOrLogin || u.login == emailOrLogin))
}
