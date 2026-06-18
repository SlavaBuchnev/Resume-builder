package resume

import scala.io.Source
import scala.jdk.CollectionConverters.*
import scala.util.Using

import org.yaml.snakeyaml.Yaml

case class Locale(
    name: String,
    aboutMe: String,
    experience: String,
    skills: String,
    generatedBy: String,
)

object Locale {
  def loadAll(resourcePath: String): List[Locale] =
    Using.resource(Source.fromResource(resourcePath)) { source =>
      val yaml = new Yaml()
      val list = yaml
        .load(source.reader())
        .asInstanceOf[java.util.List[java.util.Map[String, Any]]]
      list.asScala.map { m =>
        Locale(
          name = m.get("name").toString,
          aboutMe = m.get("aboutMe").toString,
          experience = m.get("experience").toString,
          skills = m.get("skills").toString,
          generatedBy = m.get("generatedBy").toString,
        )
      }.toList
    }

  // use on production
  val all: List[Locale] = loadAll("locales.yaml")

  def fromString(s: String): Option[Locale] = all.find(_.name == s)
}
