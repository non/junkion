package junkion

import org.scalatest.matchers.ShouldMatchers
import org.scalatest._
import prop._

import scala.io.Source
import junkion.implicits._

class ReadingTest extends PropSpec with Matchers with PropertyChecks {

  val path = "src/test/resources/lyrics.txt"
  val file = path.file

  property(s"$path has 94 lines") {
    file.lines.foldLeft(0)((n, _) => n + 1) shouldBe 94
    file.lines.iterator.size shouldBe 94
    file.lines.stream.size shouldBe 94
    file.lines.indexedSeq.size shouldBe 94
  }

  val contents = file.lines.indexedSeq.toVector

  property(".lines.stream") {
    file.lines.stream.toVector shouldBe contents
  }

  property(".lines.indexedSeq") {
    file.lines.indexedSeq.toVector shouldBe contents
  }

  property(".lines.vector") {
    file.lines.vector shouldBe contents
  }

  property(".string tests") {
    val s = file.string
    s shouldBe Source.fromFile(file).mkString
    s.length shouldBe 2305
    s shouldBe file.chars.array.mkString
    s shouldBe file.lines.iterator.map(_ + "\n").mkString
  }
}
