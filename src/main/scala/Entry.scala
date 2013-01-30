package tw.`3du`

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.lifted._


case class Entry( id: Int, 
                  title: String, 
                  radical: Option[String]
                  // strokeCount: Option[Int], 
                  // nonStrokeCount: Option[Int], 
                  )

object Entries extends Table[Entry]("entries") {
  def id = column[Int]("id", O.PrimaryKey)
  def title = column[String]("title")
  def radical = column[Option[String]]("radical")

  def * = id ~ title ~ radical <> (Entry, Entry.unapply _)
}

case class Heteronym( id: Int, 
                      entryID: Int,
                      bopomofo: Option[String],
                      bopomofo2: Option[String],
                      pinyin: Option[String])

object Heteronyms extends Table[Heteronym]("heteronyms") {
  def id = column[Int]("id", O.PrimaryKey)
  def entryID = column[Int]("entry_id")
  def bopomofo = column[Option[String]]("bopomofo")
  def bopomofo2 = column[Option[String]]("bopomofo2")
  def pinyin = column[Option[String]]("pinyin")

  def * = id ~ entryID ~ bopomofo ~ bopomofo2 ~ pinyin <> (Heteronym, Heteronym.unapply _)
}

case class Definition(id: Int,
                      heteronymID: Int,
                      partOfSpeech: String,
                      definition: String,
                      example: String)

object Definitions extends Table[Definition]("definitions") {
  def id = column[Int]("id", O.PrimaryKey)
  def heteronymID = column[Int]("heteronym_id")
  def partOfSpeech = column[String]("type")
  def definition = column[String]("def")
  def example = column[String]("example")
  
  def * = id ~ heteronymID ~ partOfSpeech ~ definition ~ example <> (Definition, Definition.unapply _)
}