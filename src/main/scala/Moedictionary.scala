package tw.`3du`

import scala.slick.driver.SQLiteDriver.simple._
import Database.threadLocalSession

object Moedictionary extends App {
  val db = Database.forURL(url = "jdbc:sqlite:development.unicode.sqlite3", driver = "org.sqlite.JDBC")

  import java.io._
  val out = new PrintWriter("moedict_templates/MoeDictionary.xml")

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
          heteronyms.map(h => {
            <span class="title">{entry.title}</span>
            <span d:pr="bopomofo">{h.bopomofo.get.replaceAll("　", " ").trim}</span>
            <div>
              <ol>{
                definitionsOf(h).map(d => 
                  <li>
                    <p class="definition">
                    {(if (d.partOfSpeech != "") s"〖${d.partOfSpeech}〗" else "") + d.definition}
                    </p>
                    { if (d.example != "") <p class="example">{d.example}</p> } 
                  </li>
                )
              }</ol>
            </div>
          })
        }</d:entry>

        xml.toString
    }}).seq.foreach(out.println(_))
  }
  out.println("""</d:dictionary>""")
  out.close
}
