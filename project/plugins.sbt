resolvers += Resolver.sonatypeRepo("releases")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.1")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.21")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.1")

addSbtPlugin("au.com.onegeek" % "sbt-dotenv" % "2.1.204")

addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.1.1")
