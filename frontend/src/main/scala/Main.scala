import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

import com.raquo.laminar.api.L._
import io.circe.parser._
import io.circe.syntax._
import org.scalajs.dom
import org.scalajs.dom.Fetch.fetch
import resume._

object Main {
  def main(args: Array[String]): Unit = {
    val appContainer = dom.document.getElementById("app")
    appContainer.innerHTML = ""
    val app = div(child <-- UIState.currentView.signal.map {
      case "login" => loginView()
      case "register" => registerView()
      case "resume" => resumeView()
    })
    render(appContainer, app)
  }

  private def loginView(): HtmlElement = {
    val emailVar = Var("")
    val passVar = Var("")
    val errorVar = Var("")
    div(
      h2("Login"),
      input(typ("emailOrLogin"), placeholder("Email/Login"), value <-- emailVar, onInput.mapToValue --> emailVar),
      input(typ("password"), placeholder("Password"), value <-- passVar, onInput.mapToValue --> passVar),
      div(color.red, child.text <-- errorVar),
      button(
        "Login",
        onClick.preventDefault --> { _ =>
          val emailOrLogin = emailVar.now()
          val pass = passVar.now()
          if (emailOrLogin.isEmpty || pass.isEmpty) {
            errorVar.set("Email/Login and password required")
          } else {
            val requestBody = LoginRequest(emailOrLogin, pass).asJson.noSpaces
            val respFuture = fetch(
              "/api/login",
              js.Dynamic
                .literal(
                  method = "POST",
                  headers = js.Dynamic.literal("Content-Type" -> "application/json"),
                  body = requestBody,
                )
                .asInstanceOf[org.scalajs.dom.RequestInit],
            ).toFuture.flatMap(_.text().toFuture)

            respFuture
              .map { text =>
                decode[AuthResponse](text) match {
                  case Right(auth) =>
                    UIState.token.set(Some(auth.token))
                    UIState.currentView.set("resume")
                    errorVar.set("")
                  case Left(_) =>
                    errorVar.set("Login failed")
                }
              }
              .recover { case _ =>
                errorVar.set("Login failed")
              }
          }
        },
      ),
      a("Register", onClick.preventDefault --> { _ => UIState.currentView.set("register") }),
    )
  }

  private def registerView(): HtmlElement = {
    val emailVar = Var("")
    val nameVar = Var("")
    val passVar = Var("")
    val errorVar = Var("")
    div(
      h2("Register"),
      input(typ("text"), placeholder("Name"), value <-- nameVar, onInput.mapToValue --> nameVar),
      input(typ("emailOrLogin"), placeholder("Email/Login"), value <-- emailVar, onInput.mapToValue --> emailVar),
      input(typ("password"), placeholder("Password"), value <-- passVar, onInput.mapToValue --> passVar),
      div(color.red, child.text <-- errorVar),
      button(
        "Register",
        onClick.preventDefault --> { _ =>
          val email = emailVar.now()
          val name = nameVar.now()
          val pass = passVar.now()
          if (email.isEmpty || name.isEmpty || pass.isEmpty) {
            errorVar.set("All fields required")
          } else {
            val requestBody = SignupRequest(email, pass, name).asJson.noSpaces
            val respFuture = fetch(
              "/api/register",
              js.Dynamic
                .literal(
                  method = "POST",
                  headers = js.Dynamic.literal("Content-Type" -> "application/json"),
                  body = requestBody,
                )
                .asInstanceOf[org.scalajs.dom.RequestInit],
            ).toFuture.flatMap(_.text().toFuture)

            respFuture
              .map { text =>
                decode[AuthResponse](text) match {
                  case Right(auth) =>
                    UIState.token.set(Some(auth.token))
                    UIState.currentView.set("resume")
                    errorVar.set("")
                  case Left(_) =>
                    errorVar.set("Registration failed")
                }
              }
              .recover { case _ =>
                errorVar.set("Registration failed")
              }
          }
        },
      ),
      a("Back to login", onClick.preventDefault --> { _ => UIState.currentView.set("login") }),
    )
  }

  private def resumeView(): HtmlElement = div(
    h1("Welcome!"),
    p("You are logged in. Resume builder coming soon."),
    button(
      "Logout",
      onClick --> { _ =>
        UIState.token.set(None)
        UIState.currentView.set("login")
      },
    ),
  )
}

object UIState {
  val token = Var(Option.empty[String])
  val currentView = Var("login")
}
