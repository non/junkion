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
}

class StringOps(val s: String) extends AnyVal {
  def file(): File = new File(s)
  def tempfile(): File = File.createTempFile(s, ".tmp")
}

abstract class GenericFileOps(val file: File, val cs: String) extends WriteOps {
  def bytes(): BytesOps = new BytesOps(file, cs)

  def chars: CharsOps = new CharsOps(file, cs)
  def chars(cs: String): CharsOps = new CharsOps(file, cs)

  def lines: LinesOps = new LinesOps(file, cs)
  def lines(cs: String): LinesOps = new LinesOps(file, cs)

  def string: String = new BytesOps(file, cs).string
  def string(cs: String): String = new BytesOps(file, cs).string
}

class FileOps(f: File) extends GenericFileOps(f, "UTF-8") {
  def as(cs: String): AsOps = new AsOps(file, cs)
}

class AsOps(f: File, c: String) extends GenericFileOps(f, c)

class BytesOps(val file: File, cs: String) {
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
    new String(array, cs)

  def string(cs: String): String =
    new String(array, cs)
}

class CharsOps(file: File, cs: String) {
  def array(): Array[Char] =
    charBuffer.array()

  def charBuffer(): CharBuffer =
    Charset.forName(cs).decode(new BytesOps(file, cs).byteBuffer)

  def string(): String =
    new BytesOps(file, cs).string
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

trait WriteOps {

  def file: File
  def cs: String

  protected[this] def makeWriter(): BufferedWriter = {
    val fos = new FileOutputStream(file)
    val osw = new OutputStreamWriter(fos, cs)
    new BufferedWriter(osw)
  }

  def write(s: String): Unit = {
    val bw = makeWriter()
    bw.write(s)
    bw.close()
  }

  def write(arr: Array[String]): Unit = {
    val bw = makeWriter()
    var i = 0
    while (i < arr.length) {
      bw.write(arr(i))
      i += 1
    }
    bw.close()
  }

  def write(it: Iterator[String]): Unit = {
    val bw = makeWriter()
    while (it.hasNext) bw.write(it.next)
    bw.close()
  }

  def write(strs: Iterable[String]): Unit =
    write(strs.iterator)

  def writelines(s: String): Unit = {
    val bw = makeWriter()
    bw.write(s)
    bw.newLine()
    bw.close()
  }

  def writelines(arr: Array[String]): Unit = {
    val bw = makeWriter()
    var i = 0
    while (i < arr.length) {
      bw.write(arr(i))
      bw.newLine()
      i += 1
    }
    bw.close()
  }

  def writelines(it: Iterator[String]): Unit = {
    val bw = makeWriter()
    while (it.hasNext) {
      bw.write(it.next)
      bw.newLine()
    }
    bw.close()
  }

  def writelines(strs: Iterable[String]): Unit =
    writelines(strs.iterator)

  def writing(f: BufferedWriter => Unit): Unit = {
    val bw = makeWriter()
    f(bw)
    bw.close()
  }
}
