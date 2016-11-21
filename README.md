#CodingCrawler

CodingCrawler is a tool for get the user's infos from coding.net.

CodingCrawler use play2 framework to develop.


### install

> cd CodingCrawler

> sbt

> compile

> run -Dhttp.port=12000

now you have to visist localhost:1200 to start the program!


### Dependency

you have to install kafka, and start the kafka topic "CodingCrawler"!

you need install redis, and start it.

all of the kafka and redis config, you can visist in config/application.conf

```
#redis 配置
RedisHost = "localhost"
RedisPort = 6379
UserQueue.Key="userqueuekey^"

#kafka 集群配置
Kafka.BootstrapServers="localhost:9092,localhost:9093,localhost:9094"
Kafka.Required.acks="1"
Kafka.Toptic="CodingCrawler"
```

### user infos

all of the user info will save in kafka~~~~~
