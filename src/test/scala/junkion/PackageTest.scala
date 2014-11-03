package junkion

import org.scalatest.matchers.ShouldMatchers
import org.scalatest._
import prop._

import org.scalacheck.{Arbitrary, Gen}
import Arbitrary.arbitrary

import java.io.File
import scala.io.Source
import junkion.implicits._

class PackageTest extends PropSpec with Matchers with GeneratorDrivenPropertyChecks {
  val path = "src/test/resources/lyrics.txt"
  val file = new File(path)

  property(s"$path has 94 lines") {
    file.lines.iterator.size shouldBe 94
    file.lines.stream.size shouldBe 94
    file.lines.indexedSeq.size shouldBe 94
  }

  property(".lines.stream = .lines.iterator") {
    file.lines.stream.toList shouldBe file.lines.iterator.toList
  }

  property(".string tests") {
    file.string shouldBe Source.fromFile(file).mkString
    file.string shouldBe file.chars.array.mkString
    file.string shouldBe file.lines.iterator.map(_ + "\n").mkString
  }
}
