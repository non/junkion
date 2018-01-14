package junkion

import org.scalatest._
import prop._

import java.io.File
import scala.io.Source
import junkion.implicits._

class WritingTest extends PropSpec with Matchers with PropertyChecks {

  property(".writelines") {
    val file = "src/test/resources/output0.txt".file
    val ns = (0 until 20).map(_.toString).toVector

    def tryit(f: () => Unit): Unit = {
      f()
      file.string shouldBe ns.mkString("", "\n", "\n")
      file.delete()
    }

    tryit(() => file.writelines(ns.iterator))
    tryit(() => file.writelines(ns))
    tryit(() => file.writelines(ns.toArray))
    tryit(() => file.writelines(ns.mkString("\n")))
  }

  property(".write") {
    val file = "src/test/resources/output1.txt".file
    val ns = (0 until 20).map(_.toString).toVector

    def tryit(f: () => Unit): Unit = {
      f()
      file.string shouldBe ns.mkString
      file.delete()
    }

    tryit(() => file.write(ns.iterator))
    tryit(() => file.write(ns))
    tryit(() => file.write(ns.toArray))
    tryit(() => file.write(ns.mkString))
  }

  property(".writing") {
    val file = "src/test/resources/output2.txt".file

    file.writing { w =>
      w.write("this ")
      w.write('i')
      w.write('s')
      w.newLine()
      w.write("very good.\n")
    }

    file.string shouldBe "this is\nvery good.\n"
    file.delete()
  }
}
