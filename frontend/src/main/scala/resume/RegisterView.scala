package resume

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

import com.raquo.laminar.api.L._
import io.circe.parser._
import io.circe.syntax._
import org.scalajs.dom

object RegisterView {
  def apply(): HtmlElement = {
    val emailVar = Var("")
    val nameVar = Var("")
    val passVar = Var("")
    val errorVar = Var("")

    div(
      h2(FieldLabels.registerTitle),
      input(
        typ("text"),
        placeholder(FieldLabels.name),
        value <-- nameVar,
        onInput.mapToValue --> nameVar,
      ),
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
        FieldLabels.registerButton,
        onClick.preventDefault --> { _ =>
          val email = emailVar.now()
          val name = nameVar.now()
          val pass = passVar.now()
          if (email.isEmpty || name.isEmpty || pass.isEmpty) {
            errorVar.set(FieldLabels.requiredAllFields)
          } else {
            val requestBody = SignupRequest(email, pass, name).asJson.noSpaces
            val respFuture = dom.Fetch
              .fetch(
                "/api/register",
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
                    errorVar.set(FieldLabels.registrationFailed)
                }
              }
              .recover { case _ =>
                errorVar.set(FieldLabels.registrationFailed)
              }
          }
        },
      ),
      a(
        FieldLabels.backToLogin,
        onClick.preventDefault --> { _ => UIState.currentView.set("login") },
      ),
    )
  }
}
