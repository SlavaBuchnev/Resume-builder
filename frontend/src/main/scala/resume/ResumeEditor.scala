package resume

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

import java.util.UUID

import com.raquo.laminar.api.L._
import io.circe.syntax._
import org.scalajs.dom

object ResumeEditor {
  private case class SectionData(title: String, content: String)
  private case class Section(title: Var[String], content: Var[String])

  def apply(userId: UUID): HtmlElement = {
    val sectionsVar = Var(List(Section(Var(""), Var(""))))
    val errorVar = Var("")

    def generateHtml(sections: List[SectionData]): String = {
      import scalatags.Text.all._
      import scalatags.Text.tags2.{title => titleTag}
      "<!DOCTYPE html>" + html(
        head(
          meta(charset := "UTF-8"),
          titleTag("Резюме"),
        ),
        body(
          for {
            s <- sections
            if s.title.nonEmpty || s.content.nonEmpty
          } yield frag(h2(s.title), p(s.content)),
        ),
      ).render
    }

    val previewHtml = sectionsVar.signal.flatMap { sections =>
      val sectionSignals = sections.map { s =>
        s.title.signal.combineWith(s.content.signal).map { case (title, content) =>
          SectionData(title, content)
        }
      }
      if (sectionSignals.isEmpty) Signal.fromValue(generateHtml(Nil))
      else Signal.combineSeq(sectionSignals).map(data => generateHtml(data.toList))
    }

    def addSection(): Unit =
      sectionsVar.update(_ :+ Section(Var(""), Var("")))

    def saveResume(): Unit = {
      val sections = sectionsVar.now()
      val data = sections.map(s => SectionData(s.title.now(), s.content.now()))
      val html = generateHtml(data)
      val request = ResumeSaveHtmlRequest(userId, html)
      dom.Fetch
        .fetch(
          "/api/resume/html",
          js.Dynamic
            .literal(
              method = "POST",
              headers = js.Dynamic.literal("Content-Type" -> "application/json"),
              body = request.asJson.noSpaces,
            )
            .asInstanceOf[dom.RequestInit],
        )
        .toFuture
        .map { resp =>
          if (resp.status == 200) errorVar.set("Сохранено!")
          else errorVar.set("Ошибка сохранения")
        }
        .recover { case _ => errorVar.set("Ошибка сохранения") }
    }

    def renderSection(s: Section, idx: Int): HtmlElement = div(
      div(
        label("Заголовок: "),
        input(
          typ("text"),
          controlled(
            value <-- s.title.signal,
            onInput.mapToValue --> s.title.writer,
          ),
        ),
      ),
      div(
        label("Содержимое: "),
        textArea(
          controlled(
            value <-- s.content.signal,
            onInput.mapToValue --> s.content.writer,
          ),
        ),
      ),
    )

    div(
      h1("Редактор резюме"),
      div(
        children <-- sectionsVar.signal.map(_.zipWithIndex.map { case (s, idx) =>
          renderSection(s, idx)
        }),
      ),
      button("Добавить секцию", onClick.preventDefault --> (_ => addSection())),
      button("Сохранить", onClick.preventDefault --> (_ => saveResume())),
      div(color.red, child.text <-- errorVar),
      h2("Предпросмотр"),
      iframe(
        src <-- previewHtml.map(html => s"data:text/html;charset=utf-8,${js.URIUtils.encodeURIComponent(html)}"),
        width := "100%",
        height := "400px",
      ),
    )
  }
}
