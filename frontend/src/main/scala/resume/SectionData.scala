package resume

case class SectionData(title: String, content: String)

sealed trait Layout
case object Classic extends Layout

object Layout {
  def fromString(s: String): Option[Layout] = s match {
    case "Classic" => Some(Classic)
    case _ => None
  }

  def displayName(layout: Layout): String = layout match {
    case Classic => "Классический"
  }
}
