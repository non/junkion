## Junkion

### Dedication

> Take some wooden nickles / Look for Mr. Goodbar /
> Get your mojo working now / I'll show you how /
> You can dare to be stupid!
>
> -- "Weird Al" Yankovich, "Dare To Be Stupid"

### Overview

Doing I/O in Scala is usually junky -- you often end up writing
Java-esque code using a wide constellation of types from the `java.io`
and `java.nio` packages. It never looks attractive, and it's often hard
to be sure if it's the "right way" or not.

Writing a library that supports all possible types of I/O (from small
text files up to gigabytes of binary data) is hard. Instead Junkion is
designed to be used when you mostly aren't worried about supporting
gigantic files, or a specific use case, but just want an easy-to-use
library that is efficient in "normal" cases.

### Recipes

Junkion is really just a collection of very simple implicit methods
that should make the humdrum business of opening and working with
small [1] files a bit easier.

```scala
import junkion.implicits._

val file = "/some/example/file.txt".file

val s1: String = file.string

val bytes: Array[Byte] = file.bytes.array
val bb: ByteBuffer = file.bytes.byteBuffer
val s2: String = file.bytes.string

val chars: Array[Char] = file.chars.array
val cb: CharBuffer = file.chars.charBuffer
val s3: String = file.chars.string

val it: Iterator[String] = file.lines.iterator
val stream: Stream[String] = file.lines.stream
val seq: IndexedSeq[String] = file.lines.indexedSeq
val array: Array[String] = file.lines.array
val list: List[String] = file.lines.list
val vector: Vector[String] = file.lines.vector

val lineCount: Int = file.lines.foldLeft(0)((n, _) => n + 1)
val wordCount: Int = file.lines.foldLeft(0)((n, s) => n + s.split(" +").length)
```

([1] By "small" I just mean a file that you don't necessarily mind
loading into memory all at once. 50 MB probably qualifies as small,
while 5 GB probably doesn't.)

By default all operations involving `String` and `Char` use the UTF-8
encoding, but alternate character encodings can be provided explicitly:

```scala
val s1: String = file.string("Big5")
val s2: String = file.bytes.string("ISO-8859-1")
val chars: Array[Char] = file.chars("KOI8-R").array
val lines: Iterator[String] = file.lines("UTF-16").iterator
```

### Disclaimers

Any project named after robots living in a junkyard, and inspired by
*Dare To Be Stupid* should not need this section, but:

> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
> IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
> FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
> AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
> LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
> OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
> SOFTWARE. 

### Copyright and License

All code is available to you under the MIT license, available at
http://opensource.org/licenses/mit-license.php and also in the
[COPYING](COPYING) file.

Copyright Erik Osheim, 2014.
