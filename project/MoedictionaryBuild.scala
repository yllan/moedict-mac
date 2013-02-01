import sbt._
import sbt.Keys._

import scala.sys.process._

object MoedictionaryBuild extends Build {

  lazy val moedictionary = Project(
    id = "moedictionary",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      getDBTask,
      patchDBTask,
      buildDictTask,
      archiveTask,
      uploadTask,

      name := "MoeDictionary",
      organization := "tw.3du",
      version := "1.0",
      scalaVersion := "2.10.0",
      // add other settings here
      libraryDependencies ++= Seq(
        "com.typesafe.slick" %% "slick" % "1.0.0-RC2",
        "org.slf4j" % "slf4j-nop" % "1.6.4",
        "org.xerial" % "sqlite-jdbc" % "3.7.2"
      ),

      scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
    )
  )

  val getDB = TaskKey[Unit]("get-db", "Get sqlite db from kcwu")

  def okay(jobDescription: String, job: scala.sys.process.ProcessBuilder, stream: TaskStreams) = {
    stream.log.info(jobDescription + " ...")
    if (job.! != 0) {
      sys.error(jobDescription + " failed.")
    }
  }

  val getDBTask = getDB <<= streams map { (s: TaskStreams) ⇒
    fetchSourceDatabase(s)
  }

  def fetchSourceDatabase(s: TaskStreams) = {
    okay("Fetching sqlite databse", 
         "curl http://kcwu.csie.org/~kcwu/tmp/moedict/development.sqlite3.bz2" #| "bzcat" #> new File("development.sqlite3.downloading"), 
         s)

    okay("Move downloaded database to correct path", 
         "mv -f development.sqlite3.downloading development.sqlite3", 
         s)
  }

  val patchDB = TaskKey[Unit]("patch-db", "Patch db using db2unicode.pl by au")

  val patchDBTask = patchDB <<= streams map { (s: TaskStreams) ⇒
    if (! new File("development.sqlite3").exists) fetchSourceDatabase(s)

    okay("Fetching the latest sym.txt", 
         "curl -O https://raw.github.com/g0v/moedict-epub/master/sym.txt", 
         s)
    
    okay("Clean old unicode database", 
         "rm -f development.unicode.sqlite3", 
         s)

    okay("Apply the patch", 
         "curl https://raw.github.com/g0v/moedict-epub/master/db2unicode.pl" #| "perl" #| "sqlite3 development.unicode.sqlite3", 
         s)
    
    okay("Clean up",
         "rm -f sym.txt development.sqlite3.dump",
         s)
  }

  val buildDict = TaskKey[Unit]("build-dict", "Build the dictionary")

  val buildDictTask = buildDict <<= streams map { (s: TaskStreams) ⇒
    okay("Build Dictionary",
         "echo cd moedict_templates; make; make install" #| "sh",
         s)
  }

  val archive = TaskKey[Unit]("archive", "Archive the dictionary")

  val archiveTask = archive <<= streams map { (s: TaskStreams) ⇒
    okay("Archive dictionary",
         "echo tar jcf moedict_templates/objects/moe.dictionary.tbz moedict_templates/objects/*.dictionary" #| "sh",
         s)
  }

  val upload = TaskKey[Unit]("upload", "Upload to moe.hypo.cc")

  val uploadTask = upload <<= streams map { (s: TaskStreams) ⇒
    val ts = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date())
    okay("Upload archive to moe.hypo.cc",
         "scp moedict_templates/objects/moe.dictionary.tbz yllan@moe.hypo.cc:moe-www/moe-" + ts + ".dictionary.tbz",
         s)
  }
}
