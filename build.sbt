ThisBuild / resolvers ++= Seq(
  "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal
)

name := "flink-dataset"

version := "0.1-SNAPSHOT"

organization := "expedition"

ThisBuild / scalaVersion := "2.12.10"

val flinkVersion = "1.14.0"

val excludelog4jSlf4jImpl = ExclusionRule(organization = "org.apache.logging.log4j", name = "log4j-slf4j-impl")

val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-clients" % flinkVersion excludeAll excludelog4jSlf4jImpl, //% "provided",
  "org.apache.flink" %% "flink-scala" % flinkVersion excludeAll excludelog4jSlf4jImpl, //% "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion excludeAll excludelog4jSlf4jImpl, //% "provided"
  "org.apache.flink" %% "flink-test-utils" % flinkVersion excludeAll excludelog4jSlf4jImpl //% "provided"

)

lazy val root = (project in file(".")).settings(
  libraryDependencies ++= flinkDependencies,
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.scalatest" %% "scalatest" % "3.0.8" % Test,
    "org.mockito" % "mockito-core" % "2.28.2" % Test
  )
)

// make run command include the provided dependencies
Compile / run := Defaults.runTask(Compile / fullClasspath, Compile / run / mainClass, Compile / run / runner).evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true

// exclude Scala library from assembly
assembly / assemblyOption := (assembly / assemblyOption).value.copy(includeScala = false)

mainClass in(Compile, run) := Some("com.kinaxis.expedition.Main")

addCommandAlias("fmt", ";scalafmtAll; scalafmtSbt")
