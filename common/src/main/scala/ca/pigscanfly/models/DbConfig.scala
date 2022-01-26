package ca.pigscanfly.models

case class DBConfig(profile: String,
                    driver: String,
                    url: String,
                    user: String,
                    password: String,
                    adminSchema: String,
                    threadsPoolCount: Int,
                    queueSize: Int,
                    searchLimit: Int)
