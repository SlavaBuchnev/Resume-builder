package resume

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

import com.raquo.laminar.api.L._
import io.circe.parser._
import io.circe.syntax._
import org.scalajs.dom

object LoginView {
  def apply(): HtmlElement = {
    val emailVar = Var("")
    val passVar = Var("")
    val errorVar = Var("")

    div(
      h2(FieldLabels.loginTitle),
      input(
        typ("emailOrLogin"),
        placeholder(FieldLabels.emailOrLogin),
        value <-- emailVar,
        onInput.mapToValue --> emailVar,
      ),
      input(
        typ("password"),
        placeholder(FieldLabels.password),
        value <-- passVar,
        onInput.mapToValue --> passVar,
      ),
      div(color.red, child.text <-- errorVar),
      button(
        FieldLabels.loginButton,
        onClick.preventDefault --> { _ =>
          val emailOrLogin = emailVar.now()
          val pass = passVar.now()
          if (emailOrLogin.isEmpty || pass.isEmpty) {
            errorVar.set(FieldLabels.requiredEmailOrLogin)
          } else {
            val requestBody = LoginRequest(emailOrLogin, pass).asJson.noSpaces
            val respFuture = dom.Fetch
              .fetch(
                "/api/login",
                js.Dynamic
                  .literal(
                    method = "POST",
                    headers = js.Dynamic.literal("Content-Type" -> "application/json"),
                    body = requestBody,
                  )
                  .asInstanceOf[dom.RequestInit],
              )
              .toFuture
              .flatMap(_.text().toFuture)

            respFuture
              .map { text =>
                decode[AuthResponse](text) match {
                  case Right(auth) =>
                    UIState.token.set(Some(auth.token))
                    UIState.currentView.set("resume")
                    errorVar.set("")
                  case Left(_) =>
                    errorVar.set(FieldLabels.loginFailed)
                }
              }
              .recover { case _ =>
                errorVar.set(FieldLabels.loginFailed)
              }
          }
        },
      ),
      a(
        FieldLabels.registerLink,
        onClick.preventDefault --> { _ => UIState.currentView.set("register") },
      ),
    )
  }
}
