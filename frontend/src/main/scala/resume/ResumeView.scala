package resume

import com.raquo.laminar.api.L._

object ResumeView {
  def apply(): HtmlElement = div(
    h1(FieldLabels.welcomeTitle),
    p(FieldLabels.welcomeText),
    button(
      FieldLabels.logoutButton,
      onClick --> { _ =>
        UIState.token.set(None)
        UIState.currentView.set("login")
      },
    ),
  )
}
