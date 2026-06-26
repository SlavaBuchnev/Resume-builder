package resume

import scalatags.Text.all._
import scalatags.Text.tags2.{style => styleTag, title => titleTag}

object ResumeLayouts {

  def generate(
      layout: Layout,
      mainSections: List[SectionData],
      sideSections: Option[List[SectionData]],
      sidePosition: Option[String],
  ): String = {

    val bodyContent = layout match {
      case Classic => classicBody(mainSections, sideSections, sidePosition)
      case _ => classicBody(mainSections, sideSections, sidePosition)
    }

    "<!DOCTYPE html>" + html(
      head(
        meta(charset := "UTF-8"),
        titleTag("Резюме"),
        styleTag("""
          .resume-wrapper {
            display: flex;
            gap: 20px;
          }
          .main-content {
            flex: 1;
          }
          .side-column {
            flex: 0 0 200px;
          }
          .side-left {
            order: -1;
          }
        """),
      ),
      body(bodyContent),
    ).render
  }

  private def renderSideColumn(sections: List[SectionData]): Frag = {
    val nonEmpty = sections.filter(s => s.title.nonEmpty || s.content.nonEmpty)
    if (nonEmpty.isEmpty) div()
    else {
      div(
        `class` := "side-column",
        nonEmpty.map { s =>
          div(
            h4(s.title),
            p(raw(s.content.replace("\n", "<br>"))),
          )
        },
      )
    }
  }

  private def classicBody(main: List[SectionData], side: Option[List[SectionData]], pos: Option[String]): Frag = {
    val mainContent = div(
      `class` := "main-content",
      main.filter(s => s.title.nonEmpty || s.content.nonEmpty).map { s =>
        div(
          h2(s.title),
          p(raw(s.content.replace("\n", "<br>"))),
        )
      },
    )

    side match {
      case Some(s) if s.nonEmpty =>
        val sideHtml = renderSideColumn(s)
        div(
          `class` := "resume-wrapper",
          if (pos.contains("Left")) {
            Seq(div(`class` := "side-left", sideHtml), mainContent)
          } else {
            Seq(mainContent, sideHtml)
          },
        )
      case _ => mainContent
    }
  }
}
