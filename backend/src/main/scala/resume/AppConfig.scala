package resume

import java.nio.file.Path

import com.comcast.ip4s.{Host, Port}
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.error.UserValidationFailed

case class AppConfig(
    statePath: Path,
    resumePath: Path,
    port: Port,
    host: Host,
)
object AppConfig {
  implicit val pathReader: ConfigReader[Path] = ConfigReader[String].map(Path.of(_))
  implicit val portReader: ConfigReader[Port] = ConfigReader[Int]
    .emap(i => Port.fromInt(i).toRight(UserValidationFailed(s"Invalid port: $i")))
  implicit val hostReader: ConfigReader[Host] = ConfigReader[String]
    .emap(s => Host.fromString(s).toRight(UserValidationFailed(s"Invalid host: $s")))

  implicit val appConfigReader: ConfigReader[AppConfig] =
    ConfigReader.forProduct4("statePath", "resumePath", "port", "host")(AppConfig.apply)

  def loadOrThrow(): AppConfig = ConfigSource.default.at("app").loadOrThrow[AppConfig]
}
