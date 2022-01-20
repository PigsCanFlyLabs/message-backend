package ca.pigscanfly.models

case class AdminConfigurations(app: ApplicationConf,
                               akka: AkkaConfig,
                               dbConfig: DBConfig)

case class DBConfig(profile: String,
                    driver: String,
                    url: String,
                    user: String,
                    password: String,
                    adminSchema: String,
                    threadsPoolCount: Int,
                    queueSize: Int,
                    searchLimit: Int)

case class ApplicationConf(host: String, port: Int)

case class AkkaConfig(futureAwaitDurationMins: Int, akkaWorkersCount: Int)
