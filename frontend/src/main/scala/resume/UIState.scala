package resume

import java.util.UUID

import com.raquo.laminar.api.L.Var

object UIState {
  val token = Var(Option.empty[String])
  val userId = Var(Option.empty[UUID])
  val currentView = Var("login")
}
