import Dependencies._

scalaVersion := "2.13.7"

lazy val sparkMiscUtils = (project in file("spark-misc-utils"))
  .settings(
    name := "spark-misc-utils",
    commonSettings,
    publishSettings,
    libraryDependencies ++= Seq(
      scalaPbCompiler,
      scalaPbRuntime
    )
  ).dependsOn(common)

lazy val common = (project in file("common"))
  .settings(
    libraryDependencies ++= commonDependencies,
    commonSettings
  )

lazy val client = (project in file("client"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      actor,
      akkaStream,
      akkaHttp,
      scalaMock,
      scalaTest,
      akkaTestKit
    )
  ).dependsOn(common)

lazy val commonSettings = Seq(
  organization := "ca.pigscanfly.ca.satellite.backend",
  publishMavenStyle := true,
  version := "0.0.1",
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-Yrangepos", "-Ywarn-unused-import"),
  javacOptions ++= {
    Seq("-source", "1.11", "-target", "1.11")
  },
  javaOptions ++= Seq("-Xms6G", "-Xmx6G", "-XX:MaxPermSize=4048M", "-XX:+CMSClassUnloadingEnabled"),

  parallelExecution in Test := false,
  fork := true,

  scalastyleSources in Compile ++= {
    unmanagedSourceDirectories in Compile
  }.value,
  scalastyleSources in Test ++= {
    unmanagedSourceDirectories in Test
  }.value,

  resolvers ++= Seq(
    "JBoss Repository" at "https://repository.jboss.org/nexus/content/repositories/releases/",
    "Cloudera Repository" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
    "Apache HBase" at "https://repository.apache.org/content/repositories/releases",
    "Twitter Maven Repo" at "https://maven.twttr.com/",
    "scala-tools" at "https://oss.sonatype.org/content/groups/scala-tools",
    "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
    "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
    "Second Typesafe repo" at "https://repo.typesafe.com/typesafe/maven-releases/",
    "Mesosphere Public Repository" at "https://downloads.mesosphere.io/maven",
    Resolver.sonatypeRepo("public")
  ),
  Compile / PB.targets := Seq(
    scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
  )
)

// publish settings
lazy val publishSettings = Seq(
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },

  licenses := Seq("Apache License 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),

  homepage := Some(url("https://github.com/PigsCanFlyLabs/message-backend")),

  scmInfo := Some(ScmInfo(
    url("https://github.com/PigsCanFlyLabs/message-backend.git"),
    "scm:git@github.com:PigsCanFlyLabs/message-backend.git"
  )),

  developers := List(
    Developer("holdenk", "Holden Karau", "holden@pigscanfly.ca", url("http://www.holdenkarau.com"))
  ),

  //credentials += Credentials(Path.userHome / ".ivy2" / ".spcredentials")
  credentials ++= Seq(Credentials(Path.userHome / ".ivy2" / ".sbtcredentials"), Credentials(Path.userHome / ".ivy2" / ".sparkcredentials")),
  useGpg := true,
)

lazy val noPublishSettings =
  skip in publish := true
lazy val root = (project in file("."))
  .aggregate(common, client, sparkMiscUtils)
