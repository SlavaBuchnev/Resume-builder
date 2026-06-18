package resume

import scala.io.Source
import scala.jdk.CollectionConverters.*
import scala.util.Using

import org.yaml.snakeyaml.Yaml

case class Theme(
    name: String,
    primaryColor: String,
    secondaryColor: String,
    backgroundColor: String,
    fontFamily: String,
    headingColor: String,
    textColor: String,
    borderColor: String,
)

object Theme {
  def loadAll(resourcePath: String): List[Theme] =
    Using.resource(Source.fromResource(resourcePath)) { source =>
      val yaml = new Yaml()
      val list = yaml
        .load(source.reader())
        .asInstanceOf[java.util.List[java.util.Map[String, Any]]]
      list.asScala.map { m =>
        Theme(
          name = m.get("name").toString,
          primaryColor = m.get("primaryColor").toString,
          secondaryColor = m.get("secondaryColor").toString,
          backgroundColor = m.get("backgroundColor").toString,
          fontFamily = m.get("fontFamily").toString,
          headingColor = m.get("headingColor").toString,
          textColor = m.get("textColor").toString,
          borderColor = m.get("borderColor").toString,
        )
      }.toList
    }

  // use on production
  def all: List[Theme] = loadAll("themes.yaml")

  def fromString(s: String): Option[Theme] = all.find(_.name == s)
}
