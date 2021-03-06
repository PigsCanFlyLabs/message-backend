import sbt._

trait Versions {
  val typeConfVersion = "1.4.1"
  val AkkaVersion = "2.6.18"
  val AkkaHttpVersion = "10.2.7"
  val akkaHttpCirceVersion = "1.26.0"
  val ScalaTestVersion = "3.3.0-SNAP3"
  val ScalaMockVersion = "5.1.0"
  val circeVersion = "0.15.0-M1"
  val scalaMatcher = "3.3.0-SNAP3"
  val twilioVersion = "8.23.0"
  val sendGridVersion = "4.8.1"
  val macwireVersion = "2.5.2"
  val macwireAkkaVersion = "2.5.2"
  val jansiVersion = "2.4.0"
  val pureConfigVersion = "0.17.1"
  val slickVersion = "3.3.2"
  val mySqlVersion = "8.0.27"
  val slickPgVersion = "0.18.0"
  val hikaricpVersion = "3.2.3"
  val slickCircePgVersion = "0.19.4"
  val flywayVersion = "3.2.1"
  val jwtCirceVersion = "7.1.2"
  val akkaSmtpVersion = "0.2.1.1"
}

object Dependencies extends Versions {
  val scalaPbCompiler = "com.thesamet.scalapb" %% "compilerplugin" % "0.11.1"
  val scalaPbRuntime = "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
  val typeConf = "com.typesafe" % "config" % typeConfVersion

  val actor = "com.typesafe.akka" %% "akka-actor" % AkkaVersion
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % AkkaVersion
  val akkaSlf4J = "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
  val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion
  val circeCore = "io.circe" %% "circe-core" % circeVersion
  val circeParser = "io.circe" %% "circe-parser" % circeVersion
  val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  val twilio = "com.twilio.sdk" % "twilio" % twilioVersion
  val akkaSmtp = "com.github.ajozwik" %% "akka-smtp" % akkaSmtpVersion
  val sendGrid = "com.sendgrid" % "sendgrid-java" % sendGridVersion
  val macwire = "com.softwaremill.macwire" %% "macros" % macwireVersion % "provided"
  val macwireAkka = "com.softwaremill.macwire" %% "macrosakka" % macwireAkkaVersion % "provided"
  val jansi = "org.fusesource.jansi" % "jansi" % jansiVersion
  val pureConfig = "com.github.pureconfig" %% "pureconfig" % pureConfigVersion

  //Testing
  val scalaMock = "org.scalamock" %% "scalamock" % "4.4.0"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"
  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % AkkaVersion
  val akkaHttpTestKit = "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion
  val akkaStreamTestKit="com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion
  val mockito = "org.mockito" %% "mockito-scala-scalatest" % "1.5.11"
  val mock = "org.mockito" % "mockito-core" % "1.9.5"
  val sprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.7"


  //DB
  val slick = "com.typesafe.slick" %% "slick" % slickVersion
  val mySql = "mysql" % "mysql-connector-java" % mySqlVersion
  val slickPg = "com.github.tminglei" %% "slick-pg" % slickPgVersion
  val hikaricp = "com.typesafe.slick" %% "slick-hikaricp" % hikaricpVersion
  val slickCirce = "com.github.tminglei" %% "slick-pg_circe-json" % slickCircePgVersion
  val flyway = "org.flywaydb" % "flyway-core" % flywayVersion
  val jwtCirce = "com.github.jwt-scala" %% "jwt-circe" % jwtCirceVersion
  val slickMySql = "com.foerster-technologies" %% "slick-mysql" % "1.0.0"
  val h2db = "com.h2database" % "h2" % "1.4.200" % Test
  val commonDependencies = List(
    akkaHttpCirce,
    typeConf,
    circeCore,
    circeParser,
    circeGeneric,
    twilio,
    sendGrid,
    sprayJson,
    scalaPbCompiler,
    scalaPbRuntime,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    jansi,
    pureConfig,
    flyway,
    jwtCirce,
    akkaSmtp,
    "com.sun.mail" % "smtp" % "1.4.7"
  )

  val dbDependencies = Seq(slick, slickPg, mySql, hikaricp, slickCirce, slickMySql)

  val testDependencies = Seq(mockito, mock, akkaHttpTestKit)

}
