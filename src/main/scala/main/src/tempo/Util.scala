package tempo

import java.util.logging.Logger

/**
 * Created by javier on 6/2/15.
 */
object Util {
  def textHasContent(aText: String): Boolean = {
    (aText != null) && (aText.trim.length > 0)
  }

  def quote(aObject: AnyRef): String = {
    SINGLE_QUOTE + String.valueOf(aObject) + SINGLE_QUOTE
  }

  def getArrayAsString(aArray: AnyRef): String = {
    val fSTART_CHAR: String = "["
    val fEND_CHAR: String = "]"
    val fSEPARATOR: String = ", "
    val fNULL: String = "null"
    if (aArray == null) fNULL
    checkObjectIsArray(aArray)
    val result: StringBuilder = new StringBuilder(fSTART_CHAR)
    val length: Int = Array.getLength(aArray)
    {
      var idx: Int = 0
      while (idx < length) {
        {
          val item: AnyRef = Array.get(aArray, idx)
          if (isNonNullArray(item)) {
            result.append(getArrayAsString(item))
          }
          else {
            result.append(item)
          }
          if (!isLastItem(idx, length)) {
            result.append(fSEPARATOR)
          }
        }
        ({
          idx += 1; idx
        })
      }
    }
    result.append(fEND_CHAR)
    result.toString
  }

  def getLogger(aClass: Class[_]): Logger = {
    Logger.getLogger(aClass.getPackage.getName)
  }

  val SINGLE_QUOTE: String = "'"

  private def isNonNullArray(aItem: AnyRef): Boolean = {
    aItem != null && aItem.getClass.isArray
  }

  private def checkObjectIsArray(aArray: AnyRef) {
    if (!aArray.getClass.isArray) {
      throw new IllegalArgumentException("Object is not an array.")
    }
  }

  private def isLastItem(aIdx: Int, aLength: Int): Boolean = {
    (aIdx == aLength - 1)
  }
}
