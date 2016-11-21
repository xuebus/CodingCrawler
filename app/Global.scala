package crawlerglobal

import play.api.Configuration
import services.KafkaClusterService

object Global {
  var configOpt: Option[Configuration] = None
  var KafkaClusterOpt: Option[KafkaClusterService] = None
}