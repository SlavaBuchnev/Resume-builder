package resume

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

import java.util.UUID

import com.raquo.laminar.api.L._
import io.circe.syntax._
import org.scalajs.dom
import resume.{ResumeSaveHtmlRequest, SectionData}

object ResumeEditor {
  private case class Section(title: Var[String], content: Var[String], level: Var[Int])

  def apply(userId: UUID): HtmlElement = {
    val sectionsVar = Var(List(Section(Var(""), Var(""), Var(2))))
    val errorVar = Var("")
    val selectedLayout = Var[Layout](Classic)

    // --- Переменные боковой колонки ---
    val sideEnabledVar = Var(false)
    val sidePositionVar = Var[String]("Right")
    val sideSectionsVar = Var(List(Section(Var(""), Var(""), Var(2))))

    // Реактивный сигнал данных основных секций (обновляется при изменении любой секции)
    val mainSectionsDataSignal: Signal[List[SectionData]] =
      sectionsVar.signal.flatMap { sections =>
        val signals =
          sections.map(s => s.title.signal.combineWith(s.content.signal).map { case (t, c) => SectionData(t, c) })
        if (signals.isEmpty) Signal.fromValue(Nil)
        else Signal.combineSeq(signals).map(_.toList)
      }

    // Реактивный сигнал данных боковых секций
    val sideSectionsDataSignal: Signal[List[SectionData]] =
      sideSectionsVar.signal.flatMap { sections =>
        val signals =
          sections.map(s => s.title.signal.combineWith(s.content.signal).map { case (t, c) => SectionData(t, c) })
        if (signals.isEmpty) Signal.fromValue(Nil)
        else Signal.combineSeq(signals).map(_.toList)
      }

    // Итоговая конфигурация боковой колонки (с учётом enabled/position)
    val sideColumnConfigSignal: Signal[Option[(List[SectionData], String)]] =
      sideEnabledVar.signal
        .combineWith(sidePositionVar.signal)
        .combineWith(sideSectionsDataSignal)
        .map { case (enabled, position, sections) =>
          if (!enabled) None
          else Some((sections, position))
        }

    // Предпросмотр (реактивный)
    val previewHtml = mainSectionsDataSignal
      .combineWith(sideColumnConfigSignal)
      .combineWith(selectedLayout.signal)
      .map { case (mainData, sideConfig, layout) =>
        val (sideData, sidePos) = sideConfig match {
          case Some((data, pos)) => (Some(data), Some(pos))
          case None => (None, None)
        }
        ResumeLayouts.generate(layout, mainData, sideData, sidePos)
      }

    previewHtml.foreach(html => dom.console.log(html))(unsafeWindowOwner)

    def addSection(): Unit =
      sectionsVar.update(_ :+ Section(Var(""), Var(""), Var(2)))

    def removeSection(idx: Int): Unit =
      sectionsVar.update(_.zipWithIndex.filterNot(_._2 == idx).map(_._1))

    // --- Боковая колонка ---
    def addSideSection(): Unit =
      sideSectionsVar.update(_ :+ Section(Var(""), Var(""), Var(2)))

    def removeSideSection(idx: Int): Unit =
      sideSectionsVar.update(_.zipWithIndex.filterNot(_._2 == idx).map(_._1))

    def saveResume(): Unit = {
      val mainData = sectionsVar.now().map(s => SectionData(s.title.now(), s.content.now()))
      val layout = selectedLayout.now()
      val enabled = sideEnabledVar.now()
      val sideData: Option[List[SectionData]] =
        if (enabled) Some(sideSectionsVar.now().map(s => SectionData(s.title.now(), s.content.now())))
        else None
      val sidePos = if (enabled) Some(sidePositionVar.now()) else None
      val html = ResumeLayouts.generate(layout, mainData, sideData, sidePos)

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

    def layoutSelector: HtmlElement = div(
      label("Выберите макет: "),
      select(
        onChange.mapToValue --> { value =>
          Layout.fromString(value).foreach(selectedLayout.set)
        },
        option(value := "Classic", "Классический"),
        option(value := "Developer", "Современный (разработчик)"),
      ),
    )

    def sideColumnControl: HtmlElement = div(
      h4("Боковая колонка"),
      div(
        label("Включить:"),
        input(
          typ("checkbox"),
          checked <-- sideEnabledVar.signal,
          onClick.mapToChecked --> sideEnabledVar.writer,
        ),
        label("Сторона:"),
        select(
          value <-- sidePositionVar.signal,
          onChange.mapToValue --> sidePositionVar.writer,
          option(value := "Left", "Слева"),
          option(value := "Right", "Справа"),
        ),
      ),
      children <-- sideEnabledVar.signal.combineWith(sideSectionsVar.signal).map {
        case (true, sections) =>
          sections.zipWithIndex.map { case (s, idx) => renderSideSection(s, idx) }
        case _ => Nil
      },
      button("➕ Добавить секцию в колонку", onClick.preventDefault --> (_ => addSideSection())),
    )

    def renderSideSection(s: Section, idx: Int): HtmlElement = div(
      label("Заголовок:"),
      input(
        typ("text"),
        controlled(
          value <-- s.title.signal,
          onInput.mapToValue --> s.title.writer,
        ),
      ),
      label("Содержимое:"),
      textArea(
        rows := 3,
        controlled(
          value <-- s.content.signal,
          onInput.mapToValue --> s.content.writer,
        ),
      ),
      button("✕", onClick.preventDefault --> (_ => removeSideSection(idx))),
    )

    def renderSection(s: Section, idx: Int): HtmlElement = div(
      label("Заголовок: "),
      input(
        typ("text"),
        placeholder("Например: Опыт работы"),
        controlled(
          value <-- s.title.signal,
          onInput.mapToValue --> s.title.writer,
        ),
      ),
      label("Уровень: "),
      select(
        value <-- s.level.signal.map(_.toString),
        onChange.mapToValue --> { str =>
          str.toIntOption.filter(l => l >= 1 && l <= 3).foreach(s.level.set)
        },
        option(value := "1", "h1"),
        option(value := "2", "h2"),
        option(value := "3", "h3"),
      ),
      button("✕", onClick.preventDefault --> (_ => removeSection(idx))),
      label("Содержимое: "),
      textArea(
        rows := 4,
        placeholder("Введите текст..."),
        controlled(
          value <-- s.content.signal,
          onInput.mapToValue --> s.content.writer,
        ),
      ),
    )

    // ---------- Основная структура редактора ----------
    div(
      display.flex,
      flexDirection.row,
      width := "100%",
      height := "100vh",

      // Левая колонка – редактор
      div(
        width := "45%",
        minWidth := "300px",
        overflowY.auto,
        padding := "20px",
        borderRight := "1px solid #ccc",
        boxSizing := "border-box",
        h1("📄 Редактор резюме"),
        layoutSelector,
        hr(),
        div(
          children <-- sectionsVar.signal.map { sections =>
            sections.zipWithIndex.map { case (s, idx) => renderSection(s, idx) }
          },
        ),
        div(
          button("➕ Добавить секцию", onClick.preventDefault --> (_ => addSection())),
          button(
            "💾 Сохранить",
            onClick.preventDefault --> { _ =>
              saveResume()
              UIState.currentView.set("resume")
            },
          ),
        ),
        hr(),
        sideColumnControl,
        div(child.text <-- errorVar),
      ),

      // Правая колонка – предпросмотр
      div(
        width := "55%",
        padding := "20px",
        display.flex,
        flexDirection.column,
        boxSizing := "border-box",
        h2("🔍 Предпросмотр"),
        iframe(
          src <-- previewHtml.map { html =>
            s"data:text/html;charset=utf-8,${js.URIUtils.encodeURIComponent(html)}#${js.Date.now()}"
          },
          width := "100%",
          flexGrow := 1,
          border := "1px solid #ccc",
        ),
      ),
    )
  }
}
