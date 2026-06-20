package resume

import com.raquo.laminar.api.L.Var

object UIState {
  val token = Var(Option.empty[String])
  val currentView = Var("login")
}
