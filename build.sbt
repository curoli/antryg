import sbt.project

lazy val Versions = new {
  val App = "0.1-SNAPSHOT"
  val LogBack = "1.2.3"
  val Scala = "2.12.4"
  val ScalaMajor = "2.12"
  val ScalaTest = "3.0.0"
  val ScalikeJDBC = "3.1.0"
  val MySQLConnector = "8.0.8-dmr"
}

lazy val mainDeps = Seq(
  "org.scala-lang" % "scala-library" % Versions.Scala,
  "org.scala-lang" % "scala-reflect" % Versions.Scala,
  "ch.qos.logback" % "logback-classic" % Versions.LogBack,
  "org.scalikejdbc" %% "scalikejdbc" % Versions.ScalikeJDBC,
  "mysql" % "mysql-connector-java" % Versions.MySQLConnector
)

lazy val testDeps = Seq(
  "org.scalatest" %% "scalatest" % Versions.ScalaTest % "it,test",
  "org.scalikejdbc" %% "scalikejdbc-test" % Versions.ScalikeJDBC % "test"
)

lazy val commonSettings = Seq(
  version := Versions.App,
  scalaVersion := Versions.Scala,
  scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked"),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    "Broad Artifactory Releases" at "https://broadinstitute.jfrog.io/broadinstitute/libs-release/",
    "Broad Artifactory Snapshots" at "https://broadinstitute.jfrog.io/broadinstitute/libs-snapshot/"
  ),
  libraryDependencies ++= (mainDeps ++ testDeps),
  scalastyleFailOnError := true
)

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(commonSettings: _*)
  .settings(Defaults.itSettings : _*)
  .settings(
    name := "antryg",
    packageSummary in Linux := "antryg - a Scala wrapper for SQL and Cassandra with unknown, dynamic or sparse schema",
    packageSummary in Windows := "antryg - a Scala wrapper for SQL and Cassandra with unknown, dynamic or sparse schema",
    packageDescription := "antryg - a Scala wrapper for SQL and Cassandra with unknown, dynamic or sparse schema",
    maintainer in Windows := "Oliver Ruebenacker, Broad Institute, oliverr@broadinstitute.org",
    maintainer in Debian := "Oliver Ruebenacker, Broad Institute, oliverr@broadinstitute.org"
  ).enablePlugins(JavaAppPackaging)

enablePlugins(GitVersioning)

scalastyleConfig in Test := file("scalastyle-config-for-tests.xml")

val buildInfoTask = taskKey[Seq[File]]("buildInfo")

buildInfoTask := {
  val dir = (resourceManaged in Compile).value
  val n = name.value
  val v = version.value
  val branch = git.gitCurrentBranch.value
  val lastCommit = git.gitHeadCommit.value
  val describedVersion = git.gitDescribedVersion.value
  val anyUncommittedChanges = git.gitUncommittedChanges.value

  val buildDate = java.time.Instant.now

  val file = dir / "versionInfo.properties"

  val contents = s"name=$n\nversion=$v\nbranch=$branch\nlastCommit=${lastCommit.getOrElse("")}\nuncommittedChanges=$anyUncommittedChanges\ndescribedVersion=${describedVersion.getOrElse("")}\nbuildDate=$buildDate\n"

  IO.write(file, contents)

  Seq(file)
}

(resourceGenerators in Compile) += buildInfoTask.taskValue


