import Dependencies._

import scala.sys.process.Process

import com.typesafe.sbt.packager.docker._

scalaVersion := "2.13.7"

// Docker  -- see https://softwaremill.com/how-to-build-multi-platform-docker-image-with-sbt-and-docker-buildx/
lazy val ensureDockerBuildx = taskKey[Unit]("Ensure that docker buildx configuration exists")
lazy val dockerBuildWithBuildx = taskKey[Unit]("Build docker images using buildx")

lazy val dockerBuildxSettings = Seq(
  ensureDockerBuildx := {
    if (Process("docker buildx inspect multi-arch-builder").! == 1) {
      Process("docker buildx create --use --name multi-arch-builder", baseDirectory.value).!
    }
  },
  dockerExecCommand := Seq("docker", "buildx"),
  dockerExposedPorts := Seq(8080, 8558, 25520),
  Docker / packageName := "dockerised-akka-http-messaging-app",
  dockerUsername := Some("holdenk"),
  dockerCommands ++= Seq(Cmd("USER", "root"), ExecCmd("RUN", "apt-get", "install", "-y", "bash")),
  dockerBuildWithBuildx := {
    streams.value.log("Building and pushing image with Buildx")
    dockerAliases.value.foreach(
      alias => Process("docker buildx build --platform=linux/arm64,linux/amd64 --push -t " +
        alias + " .", baseDirectory.value / "target" / "docker"/ "stage").!
    )
  },
  dockerBaseImage := "adoptopenjdk/openjdk11:jdk11u-debian-nightly",
  publish in Docker := Def.sequential(
    publishLocal in Docker,
    ensureDockerBuildx,
    dockerBuildWithBuildx
  ).value
)

// Docker packaging
enablePlugins(JavaAppPackaging)

// Make version compatible with docker for publishing.
ThisBuild / dynverSeparator := "-"

// Normal build

lazy val common = (project in file("common"))
  .settings(
    libraryDependencies ++= commonDependencies,
    commonSettings
  )

lazy val persistence = (project in file("persistence"))
  .settings(
    libraryDependencies ++= commonDependencies ++ dbDependencies ++ Seq(Dependencies.scalaTest, Dependencies.h2db),
    commonSettings
  ) dependsOn common

lazy val adminService = (project in file("admin-service"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      actor,
      akkaStream,
      akkaHttp,
      scalaMock,
      scalaTest,
      akkaTestKit,
      akkaSlf4J,
      akkaStreamTestKit
    ) ++ testDependencies
  ).dependsOn(common, persistence)

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
    ) ++ testDependencies
  ).dependsOn(common, persistence)

lazy val commonSettings = Seq(
  organization := "ca.pigscanfly",
  publishMavenStyle := true,
  version := "0.0.1d",
  scalaVersion := "2.13.7",
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-Yrangepos"),
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

lazy val container = (project in file("container"))
  .dependsOn(common, client, adminService, persistence)
  .settings(commonSettings, dockerBuildxSettings,
    libraryDependencies ++= commonDependencies)
  .enablePlugins(JavaAppPackaging, DockerPlugin)

lazy val noPublishSettings =
  skip in publish := true
lazy val root = (project in file("."))
  .aggregate(common, client, adminService, persistence, container)

envFileName in ThisBuild := ".env-swarmservice"
