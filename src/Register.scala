/**
  * @author Christian Goldapp
  * @version 1.0
  */
class Register(n: String) extends Util {
  val name = n
  var content: Word32 = new Word32(0)

  override def toString = String.format("%s:%s", n, content.toString)

  def set(word: Word32) = content = word

  def get() = content
}
