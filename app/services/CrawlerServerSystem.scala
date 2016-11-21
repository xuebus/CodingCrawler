package services

import javax.inject.Inject

import Actors.CrawlerSystem
import crawlerConfig.CrawlerConfig
import play.Environment
import play.api.Configuration

class CrawlerServerSystem @Inject() (environment: Environment, config: Configuration) {
  initSystem()

  def initSystem() = {
    CrawlerConfig.configOpt = Some(config)
    CrawlerSystem.initCrawlerSystem match {
      case Some(system) =>
        println("crawler system initted")
        CrawlerSystem.scheduleOnce(system, 3)
      case _ => println("init crawler system error")
    }
  }
}
