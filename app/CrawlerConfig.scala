package crawlerConfig
import play.api.Configuration

object CrawlerConfig {
  var configOpt: Option[Configuration] = None

  def getValue(key: String, default: String): String = {
    configOpt match {
      case Some(config) =>
        config.getString(key) match {
          case Some(value) => value
          case None => default
        }
      case None => default
    }
  }
}
