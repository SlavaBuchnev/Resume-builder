import com.raquo.laminar.api.L._
import org.scalajs.dom
import resume._

object Main {
  def main(args: Array[String]): Unit = {
    val appContainer = dom.document.getElementById("app")
    appContainer.innerHTML = ""
    val app = div(child <-- UIState.currentView.signal.map {
      case "login" => LoginView()
      case "register" => RegisterView()
      case "resume" => ResumeView()
    })
    render(appContainer, app)
  }
}
