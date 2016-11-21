package services

import redis.clients.jedis.Jedis

case class RedisConfig(host: String, port: Int)

class RedisService(config: RedisConfig) {
  private var jedisOpt: Option[Jedis] = None
  initRedisCluster()

  private def initRedisCluster() = {
    val jedis = new Jedis(config.host, config.port)
    jedisOpt = Some(jedis)
  }

  def set(key: String, value: String) = {
    jedisOpt match {
      case Some(jc) =>
        jc.set(key, value)
        true
      case None => false
    }
  }

  def isExist(key: String): Boolean = {
    jedisOpt match {
      case Some(jc) =>
        jc.exists(key)
      case None =>
        false
    }
  }

  def sadd(key: String, value: String) = {
    jedisOpt match {
      case Some(jc) =>
        jc.sadd(key, value)
        true
      case None =>
        false
    }
  }

  def spop(key: String): Option[String] = {
    jedisOpt match {
      case Some(jc) =>
        val value: String = jc.spop(key)
        value match {
          case null => None
          case str => Some(str)
        }
      case None => None
    }
  }

  def stop() = {
    jedisOpt match {
      case Some(jc) =>
        jc.close()
      case None =>
        None
    }
  }
}