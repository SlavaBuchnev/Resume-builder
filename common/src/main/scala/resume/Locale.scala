//package resume
//
//import scala.io.Source
//import scala.jdk.CollectionConverters._
//import scala.util.Using
//
//import org.yaml.snakeyaml.Yaml
//
//case class Locale(
//    login: String,
//    aboutMe: String,
//    experience: String,
//    skills: String,
//    generatedBy: String,
//)
//
//object Locale {
//  def loadAll(resourcePath: String): List[Locale] =
//    Using.resource(Source.fromResource(resourcePath)) { source =>
//      val yaml = new Yaml()
//      val list = yaml
//        .load(source.reader())
//        .asInstanceOf[java.util.List[java.util.Map[String, Any]]]
//      list.asScala.map { m =>
//        Locale(
//          login = m.get("login").toString,
//          aboutMe = m.get("aboutMe").toString,
//          experience = m.get("experience").toString,
//          skills = m.get("skills").toString,
//          generatedBy = m.get("generatedBy").toString,
//        )
//      }.toList
//    }
//
//  // use on production
//  val all: List[Locale] = loadAll("locales.yaml")
//
//  def fromString(s: String): Option[Locale] = all.find(_.login == s)
//}
