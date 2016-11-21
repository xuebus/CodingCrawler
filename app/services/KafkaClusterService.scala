package services

import java.util.Properties
import javax.inject.Inject

import crawlerConfig.CrawlerConfig
import crawlerglobal.Global
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

/**
  * Created by spoofer on 16-11-15.
  */
class KafkaClusterService @Inject() () {
  private var producerOpt: Option[KafkaProducer[String, String]] = None

  initKafkaCluster()

  @inline
  private def getBootstrapServers(default: String = "localhost:9002"): String = {
    CrawlerConfig.getValue("Kafka.BootstrapServers", default)
  }

  @inline
  private def getAcksConfig(default: String = "0"): String = {
    CrawlerConfig.getValue("Kafka.Required.acks", default)
  }

  private def getProperties: Properties = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers())
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[org.apache.kafka.common.serialization.StringSerializer])
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[org.apache.kafka.common.serialization.StringSerializer])
    props.put(ProducerConfig.ACKS_CONFIG, getAcksConfig())
    props
  }

  private def initKafkaCluster() = {
    val producer = new KafkaProducer[String, String](getProperties)
    producerOpt = Some(producer)
    Global.KafkaClusterOpt = Some(this)
  }

  def send(topic: String, data: String) = {
    producerOpt match {
      case Some(producer) =>
        producer.send(new ProducerRecord(topic, data))
        true
      case None =>
        false
    }
  }

  def stop() = {
    producerOpt match {
      case Some(producer) => producer.close()
      case None =>
    }
  }
}
