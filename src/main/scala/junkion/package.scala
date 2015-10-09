package junkion

import language.implicitConversions

import java.io.File
import java.io.{BufferedReader, FileInputStream, InputStreamReader}
import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}

import java.nio.{ByteBuffer, CharBuffer}
import java.nio.charset.Charset
import java.nio.channels.FileChannel.MapMode.READ_ONLY

import scala.collection.mutable

object implicits {
  implicit def stringOps(s: String): StringOps = new StringOps(s)
  implicit def fileOps(file: File): FileOps = new FileOps(file)
  implicit def writeOps(file: File): WriteOps = new WriteOps(file, "UTF-8")
}

class StringOps(val s: String) extends AnyVal {
  def file(): File = new File(s)
  def tempfile(): File = File.createTempFile(s, ".tmp")
}

class FileOps(val file: File) extends AnyVal {

  def bytes(): BytesOps = new BytesOps(file)

  def chars: CharsOps = chars()
  def chars(cs: String = "UTF-8"): CharsOps = new CharsOps(file, cs)

  def lines: LinesOps = lines()
  def lines(cs: String = "UTF-8"): LinesOps = new LinesOps(file, cs)

  def string: String = new BytesOps(file).string()
  def string(cs: String = "UTF-8"): String = new BytesOps(file).string(cs)

  def write(cs: String = "UTF-8"): WriteOps = new WriteOps(file, cs)
  def writelines(cs: String = "UTF-8"): WriteLinesOps = new WriteLinesOps(file, cs)
}

class BytesOps(val file: File) extends AnyVal {
  def array(): Array[Byte] = {
    val fis = new FileInputStream(file)
    val len = file.length.toInt
    val ch = fis.getChannel()
    val buf = ch.map(READ_ONLY, 0, len)
    fis.close()

    var i = 0
    val bytes = new Array[Byte](len)
    while (buf.hasRemaining) {
      val n = Math.min(buf.remaining, 131072)
      buf.get(bytes, i, n)
      i += n
    }

    bytes
  }

  def byteBuffer(): ByteBuffer =
    ByteBuffer.wrap(array)

  def chunked(bufSize: Int = 131072): Stream[ByteBuffer] = {
    require(bufSize > 0)
    val ch = new FileInputStream(file).getChannel()
    def next(): Stream[ByteBuffer] = {
      val bb = ByteBuffer.allocate(bufSize)
      val n = ch.read(bb)
      if (n > -1) bb #:: next() else {
        ch.close()
        Stream.Empty
      }
    }
    next()
  }

  def string: String =
    string()

  def string(cs: String = "UTF-8"): String =
    new String(array, cs)
}

class CharsOps(file: File, cs: String) {
  def array(): Array[Char] =
    charBuffer.array()

  def charBuffer(): CharBuffer =
    Charset.forName(cs).decode(new BytesOps(file).byteBuffer)

  def string(): String =
    new BytesOps(file).string(cs)
}

class LinesOps(file: File, cs: String) {
  def bufferedReader(): BufferedReader =
    new BufferedReader(new InputStreamReader(new FileInputStream(file), cs))

  def foldLeft[A](init: A)(f: (A, String) => A): A = {
    val reader = bufferedReader()
    var state: A = init
    var line: String = reader.readLine()
    while (line != null) {
      state = f(state, line)
      line = reader.readLine()
    }
    state
  }

  def indexedSeq: IndexedSeq[String] = {
    val reader = bufferedReader()
    val buf = mutable.ArrayBuffer.empty[String]
    var line: String = reader.readLine()
    while (line != null) {
      buf.append(line)
      line = reader.readLine()
    }
    buf
  }

  def array: Array[String] = indexedSeq.toArray

  def list: List[String] = indexedSeq.toList

  def vector: Vector[String] = indexedSeq.toVector

  def iterator: Iterator[String] = {
    val reader = bufferedReader()
    val line0 = reader.readLine()
    new Iterator[String] {
      var line: String = line0
      if (line == null) reader.close()
      def hasNext(): Boolean = line != null
      def next(): String = {
        if (line == null) throw new NoSuchElementException("next on empty iterator")
        val out = line
        line = reader.readLine()
        if (line == null) reader.close()
        out
      }
    }
  }

  def stream: Stream[String] = {
    val reader = bufferedReader()
    def stream(): Stream[String] = {
      val line = reader.readLine()
      if (line != null) line #:: stream() else {
        reader.close()
        Stream.Empty
      }
    }
    stream()
  }
}

trait WriteSupport {
  protected[this] def makeWriter(file: File, cs: String): BufferedWriter = {
    val fos = new FileOutputStream(file)
    val osw = new OutputStreamWriter(fos, cs)
    new BufferedWriter(osw)
  }
}

class WriteOps(file: File, cs: String) extends WriteSupport {

  def string(s: String): Unit = {
    val bw = makeWriter(file, cs)
    bw.write(s)
    bw.close()
  }

  def array(arr: Array[String]): Unit = {
    val bw = makeWriter(file, cs)
    var i = 0
    while (i < arr.length) {
      bw.write(arr(i))
      i += 1
    }
    bw.close()
  }

  def iterator(it: Iterator[String]): Unit = {
    val bw = makeWriter(file, cs)
    while (it.hasNext) bw.write(it.next)
    bw.close()
  }

  def iterable(strs: Iterable[String]): Unit =
    iterator(strs.iterator)

  def using(f: BufferedWriter => Unit): Unit = {
    val bw = makeWriter(file, cs)
    f(bw)
    bw.close()
  }
}

class WriteLinesOps(file: File, cs: String) extends WriteSupport {

  def string(s: String): Unit = {
    val bw = makeWriter(file, cs)
    bw.write(s)
    bw.newLine()
    bw.close()
  }

  def array(arr: Array[String]): Unit = {
    val bw = makeWriter(file, cs)
    var i = 0
    while (i < arr.length) {
      bw.write(arr(i))
      bw.newLine()
      i += 1
    }
    bw.close()
  }

  def iterator(it: Iterator[String]): Unit = {
    val bw = makeWriter(file, cs)
    while (it.hasNext) {
      bw.write(it.next)
      bw.newLine()
    }
    bw.close()
  }

  def iterable(strs: Iterable[String]): Unit =
    iterator(strs.iterator)
}
