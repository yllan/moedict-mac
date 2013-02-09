package tw.`3du`

import scala.slick.driver.SQLiteDriver.simple._
import Database.threadLocalSession

object Moedictionary extends App {

  def bopomofoMarkup(bpmf: String) = {
    val isTone = Set('ˊ', 'ˇ', 'ˋ')

    <span class="rt">{
      if (isTone(bpmf.last)) bpmf.dropRight(1) else bpmf
    }{
      if (isTone(bpmf.last)) <span class="tone">{bpmf.last.toString}</span>
    }</span>
  }

  val db = Database.forURL(url = "jdbc:sqlite:dict-revised.unicode.sqlite3", driver = "org.sqlite.JDBC")

  import java.io._
  val out = new PrintWriter("moedict_templates/MoeDictionary.xml", "UTF-8")

  db withSession {
    
    val query = for (
      e <- Entries;
      h <- Heteronyms if h.entryID === e.id;
      d <- Definitions if d.heteronymID === h.id
    ) yield (e, h, d)

    val heteronymsOf = scala.collection.mutable.Map[Entry, Vector[Heteronym]]()
    val definitionsOf = scala.collection.mutable.Map[Heteronym, Vector[Definition]]()

    /* Building the relations in memory */
    query.foreach { case (entry, heteronym, definition) => {
      val heteronyms = heteronymsOf.get(entry).getOrElse(Vector[Heteronym]())
      if (!heteronyms.contains(heteronym)) {
        heteronymsOf.put(entry, heteronyms :+ heteronym)
      }

      val definitions = definitionsOf.get(heteronym).getOrElse(Vector[Definition]())
      definitionsOf.put(heteronym, definitions :+ definition)
    }}

    out.println("""<?xml version="1.0" encoding="UTF-8"?>""")
    out.println("""<d:dictionary xmlns="http://www.w3.org/1999/xhtml" xmlns:d="http://www.apple.com/DTDs/DictionaryService-1.0.rng">""")
    (heteronymsOf.par.filterNot(_._1.title.contains("img")).map { case (entry, heteronyms) => {
      val xml = 

      <d:entry id={entry.id.toString} d:title={entry.title}>
      <d:index d:value={entry.title} />{
        heteronyms.sortWith(_.idx < _.idx).map(h => {
          // val titleWithBopomofo = entry.title zip h.bopomofo.get.split("　").drop(1)

          <h1 class="title">{entry.title}</h1>
          <span d:pr="bpmf" class="bopomofo">{h.bopomofo.getOrElse("").replaceAll("　", " ").trim}</span>
          <span d:pr="bpmf2" class="bopomofo2">{h.bopomofo2.getOrElse("")}</span>
          <span d:pr="pinyin" class="pinyin">{h.pinyin.getOrElse("")}</span>
          <div>
          {
            definitionsOf(h).groupBy(_.partOfSpeech).map({ case (pos, ds)=> 
              <div>{if (pos.isDefined) <span class="part-of-speech">{pos.getOrElse("")}</span>}
              <ol>{
                ds.sortWith(_.idx < _.idx).map(d =>
                  <li>
                    <p class="definition">{d.definition}</p>
                    { if (d.example.isDefined) d.example.get.split(",").map(e => <p class="example">{e}</p>) }
                    { if (d.quote.isDefined) d.quote.get.split(",").map(q => <p class="quote">{q}</p>) }
                    { if (d.synonyms.isDefined) <p class="synonyms">{d.synonyms.get}</p> }
                    { if (d.antonyms.isDefined) <p class="antonyms">{d.antonyms.get}</p> }
                  </li>
                )
              }</ol>
              </div>
            })
          }
          </div>
        })
      }</d:entry>

      xml.toString
    }}).seq.foreach(out.println(_))
  }
  out.println("""</d:dictionary>""")
  out.close
}
