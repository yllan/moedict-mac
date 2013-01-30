import sbt._
import sbt.Keys._

object MoedictionaryBuild extends Build {

  lazy val moedictionary = Project(
    id = "moedictionary",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "MoeDictionary",
      organization := "tw.3du",
      version := "1.0",
      scalaVersion := "2.10.0",
      // add other settings here
      libraryDependencies ++= Seq(
        "com.typesafe" %% "slick" % "1.0.0-RC2",
        "org.slf4j" % "slf4j-nop" % "1.6.4",
        "org.xerial" % "sqlite-jdbc" % "3.7.2"
      ),

      scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
    )
  )
}
