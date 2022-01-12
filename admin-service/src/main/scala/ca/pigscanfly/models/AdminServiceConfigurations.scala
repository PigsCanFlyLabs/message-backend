package ca.pigscanfly.models

case class AdminConfigurations(app: ApplicationConf,
                               akka: AkkaConfig,
                               dbConfig: DBConfig)

case class ApplicationConf(host: String, port: Int)

case class AkkaConfig(futureAwaitDurationMins: Int, akkaWorkersCount: Int)
