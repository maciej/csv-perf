import sbt.Keys._
import sbt._

object Resolvers {
//  val ivyLocal = Resolver.file("local", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

  val customResolvers = Seq(
    "SoftwareMill Public Releases" at "https://nexus.softwaremill.com/content/repositories/releases/",
    "SoftwareMill Public Snapshots" at "https://nexus.softwaremill.com/content/repositories/snapshots/",
    "spray" at "http://repo.spray.io/"
//    ivyLocal
  )
}

object Dependencies {

  val combinatorParsers = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1"
  val parboiled = "org.parboiled" %% "parboiled" % "2.0.1"

  val scalaTest = "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test"

  val gardenVersion = "0.0.20-SNAPSHOT"
  val theGarden = Seq(
    "com.softwaremill.thegarden" %% "lawn" % gardenVersion,
    "com.softwaremill.thegarden" %% "shrubs" % gardenVersion % "test"
  )

  val testDeps = Seq(scalaTest)

  val scalaMeter = "com.github.axel22" %% "scalameter" % "0.5-M2" % "test"

  val commonDependencies = Seq(combinatorParsers, parboiled, scalaMeter) ++ testDeps ++ theGarden
}

object CSVPerfBuild extends Build {

  import Dependencies._

  val scalaOptionsSeq = Seq("-unchecked", "-deprecation", "-feature")

  lazy val slf4jExclusionHack = Seq(
    ivyXML :=
      <dependencies>
        <exclude org="org.slf4j" artifact="slf4j-log4j12"/>
        <exclude org="log4j" artifact="log4j"/>
      </dependencies>
  )

  override val settings = super.settings ++ Seq(
    name := "csv-perf",
    version := "1.0",
    scalaVersion := "2.11.1",
    scalacOptions in GlobalScope in Compile := scalaOptionsSeq,
    scalacOptions in GlobalScope in Test := scalaOptionsSeq,
    organization := "com.softwaremill.csvperf",
    resolvers ++= Resolvers.customResolvers
  ) ++ slf4jExclusionHack

  lazy val root = Project(
    id = "csv-perf",
    base = file(".")
  ).settings(libraryDependencies ++= commonDependencies)
}
