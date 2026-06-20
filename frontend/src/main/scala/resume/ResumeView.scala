package resume

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.URIUtils

import com.raquo.laminar.api.L._
import org.scalajs.dom

object ResumeView {
  def apply(): HtmlElement = {
    val resumeHtmlVar = Var(Option.empty[String])
    val isEditing = Var(false)

    val userIdOpt = UIState.userId.now()
    userIdOpt.foreach { userId =>
      dom.Fetch
        .fetch(s"/api/resume/$userId")
        .toFuture
        .flatMap { resp =>
          if (resp.status == 200) resp.text().toFuture.map(Some(_))
          else Future.successful(None)
        }
        .map { htmlOpt =>
          resumeHtmlVar.set(htmlOpt)
          if (htmlOpt.isEmpty) isEditing.set(true)
        }
        .recover { case _ => isEditing.set(true) }
    }

    def logoutHandler(): Unit = {
      UIState.token.set(None)
      UIState.userId.set(None)
      UIState.currentView.set("login")
    }

    div(
      h1("Ваше резюме"),
      child <-- resumeHtmlVar.signal.map {
        case Some(html) =>
          div(
            iframe(
              src := s"data:text/html;charset=utf-8,${URIUtils.encodeURIComponent(html)}",
              width := "100%",
              height := "400px",
            ),
            button("Редактировать", onClick.preventDefault --> (_ => isEditing.set(true))),
          )
        case None => div("Резюме ещё не создано")
      },
      child <-- isEditing.signal.map {
        case true =>
          userIdOpt.map(uid => ResumeEditor(uid)).getOrElse(div("Пользователь не определён"))
        case false => div()
      },
      button("Выйти", onClick.preventDefault --> (_ => logoutHandler())),
    )
  }
}
