package tempo

import java.util
import java.util.StringTokenizer
import java.util.logging.Logger
import java.util.regex.{Matcher, Pattern}

/**
 * Created by javier on 6/2/15.
 */
/**
Implements the <tt>toString</tt> method for some common cases.

 <P>This class is intended only for cases where <tt>toString</tt> is used in
 an informal manner (usually for logging and stack traces). It is especially
 suited for <tt>public</tt> classes which model domain objects.

 Here is an example of a return value of the {@link #getText} method :
 <PRE>
hirondelle.web4j.model.MyUser {
LoginName: Bob
LoginPassword: ****
EmailAddress: bob@blah.com
StarRating: 1
FavoriteTheory: Quantum Chromodynamics
SendCard: true
Age: 42
DesiredSalary: 42000
BirthDate: Sat Feb 26 13:45:43 EST 2005
}
 </PRE>
 (Previous versions of this classes used indentation within the braces. That has
 been removed, since it displays poorly when nesting occurs.)

 <P>Here are two more examples, using classes taken from the JDK :
 <PRE>
java.util.StringTokenizer {
nextElement: This
hasMoreElements: true
countTokens: 3
nextToken: is
hasMoreTokens: true
}

java.util.ArrayList {
size: 3
toArray: [blah, blah, blah]
isEmpty: false
}
 </PRE>

 There are two use cases for this class. The typical use case is :
 <PRE>
  public String toString() {
    return ToStringUtil.getText(this);
  }
 </PRE>

 <span class="highlight">However, there is a case where this typical style can
 fail catastrophically</span> : when two objects reference each other, and each
 has <tt>toString</tt> implemented as above, then the program will loop
 indefinitely!

 <P>As a remedy for this problem, the following variation is provided :
 <PRE>
  public String toString() {
    ToStringUtil.getTextAvoidCyclicRefs(this, Product.class, "getId");
  }
 </PRE>
 Here, the usual behavior is overridden for any method
 which returns a <tt>Product</tt> : instead of calling <tt>Product.toString</tt>,
 the value of <tt>Product.getId()</tt> is used to textually represent
 the object.
  */
object ToStringUtil {
  /**
  Return an informal textual description of an object.
   <P>It is highly recommened that the caller <em>not</em> rely on details
   of the returned <tt>String</tt>. See class description for examples of return
   values.

   <P><span class="highlight">WARNING</span>: If two classes have cyclic references
   (that is, each has a reference to the other), then infinite looping will result
   if <em>both</em> call this method! To avoid this problem, use <tt>getText</tt>
   for one of the classes, and {@link #getTextAvoidCyclicRefs} for the other class.

   <P>The only items which contribute to the result are the class name, and all
   no-argument <tt>public</tt> methods which a value. As well, methods
   defined by the <tt>Object</tt> class, and factory methods which an
   <tt>Object</tt> of the native class ("<tt>getInstance</tt>" methods) do not contribute.

   <P>Items are converted to a <tt>String</tt> simply by calling their
   <tt>toString method</tt>, with these exceptions :
   <ul>
   <li>{@link Util#getArrayAsString(Object)} is used for arrays
   <li>a method whose name contain the text <tt>"password"</tt> (not case-sensitive) have
   their values hard-coded to <tt>"****"</tt>.
   </ul>

   <P>If the method name follows the pattern <tt>getXXX</tt>, then the word 'get'
   is removed from the presented result.

   @param aObject the object for which a <tt>toString</tt> result is required.
    */
  def getText(aObject: AnyRef): String = {
    getTextAvoidCyclicRefs(aObject, null, null)
  }

  /**
  As in {@link #getText}, but, for values which are instances of
   <tt>aSpecialClass</tt>, then call <tt>aMethodName</tt> instead of <tt>toString</tt>.

   <P> If <tt>aSpecialClass</tt> and <tt>aMethodName</tt> are <tt>null</tt>, then the
   behavior is exactly the same as calling {@link #getText}.
    */
  def getTextAvoidCyclicRefs(aObject: AnyRef, aSpecialClass: Class[_], aMethodName: String): String = {
    val result: StringBuilder = new StringBuilder
    addStartLine(aObject, result)
    val methods: Array[Method] = aObject.getClass.getDeclaredMethods
    for (method <- methods) {
      if (isContributingMethod(method, aObject.getClass)) {
        addLineForGetXXXMethod(aObject, method, result, aSpecialClass, aMethodName)
      }
    }
    addEndLine(result)
    result.toString
  }

  val fGET_CLASS: String = "getClass"
  val fCLONE: String = "clone"
  val fHASH_CODE: String = "hashCode"
  val fTO_STRING: String = "toString"
  val fGET: String = "get"
  val fNO_ARGS: Array[AnyRef] = new Array[AnyRef](0)
  val fNO_PARAMS: Array[Class[_]] = new Array[Class[_]](0)
  val fINDENT: String = ""
  val fAVOID_CIRCULAR_REFERENCES: String = "[circular reference]"
  val fLogger: Logger = Util.getLogger(classOf[ToStringUtil])
  val NEW_LINE: String = System.getProperty("line.separator")
  private var PASSWORD_PATTERN: Pattern = Pattern.compile("password", Pattern.CASE_INSENSITIVE)
  private var HIDDEN_PASSWORD_VALUE: String = "****"

  private def addStartLine(aObject: AnyRef, aResult: StringBuilder) {
    aResult.append(aObject.getClass.getName)
    aResult.append(" {")
    aResult.append(NEW_LINE)
  }

  private def addEndLine(aResult: StringBuilder) {
    aResult.append("}")
    aResult.append(NEW_LINE)
  }

  /**
  Return <tt>true</tt> only if <tt>aMethod</tt> is public, takes no args,
   returns a value whose class is not the native class, is not a method of
   <tt>Object</tt>.
    */
  private def isContributingMethod(aMethod: Method, aNativeClass: Class[_]): Boolean = {
    val isPublic: Boolean = Modifier.isPublic(aMethod.getModifiers)
    val hasNoArguments: Boolean = aMethod.getParameterTypes.length == 0
    val hasReturnValue: Boolean = aMethod.getReturnType ne Void.TYPE
    val returnsNativeObject: Boolean = aMethod.getReturnType eq aNativeClass
    val isMethodOfObjectClass: Boolean = (aMethod.getName == fCLONE) || (aMethod.getName == fGET_CLASS) || (aMethod.getName == fHASH_CODE) || (aMethod.getName == fTO_STRING)
    isPublic && hasNoArguments && hasReturnValue && !isMethodOfObjectClass && !returnsNativeObject
  }

  private def addLineForGetXXXMethod(aObject: AnyRef, aMethod: Method, aResult: StringBuilder, aCircularRefClass: Class[_], aCircularRefMethodName: String) {
    aResult.append(fINDENT)
    aResult.append(getMethodNameMinusGet(aMethod))
    aResult.append(": ")
    var returnValue: AnyRef = getMethodReturnValue(aObject, aMethod)
    if (returnValue != null && returnValue.getClass.isArray) {
      aResult.append(Util.getArrayAsString(returnValue))
    }
    else {
      if (aCircularRefClass == null) {
        aResult.append(returnValue)
      }
      else {
        if (aCircularRefClass eq returnValue.getClass) {
          val method: Method = getMethodFromName(aCircularRefClass, aCircularRefMethodName)
          if (isContributingMethod(method, aCircularRefClass)) {
            returnValue = getMethodReturnValue(returnValue, method)
            aResult.append(returnValue)
          }
          else {
            aResult.append(fAVOID_CIRCULAR_REFERENCES)
          }
        }
      }
    }
    aResult.append(NEW_LINE)
  }

  private def getMethodNameMinusGet(aMethod: Method): String = {
    var result: String = aMethod.getName
    if (result.startsWith(fGET)) {
      result = result.substring(fGET.length)
    }
    result
  }

  /** Return value is possibly-null.  */
  private def getMethodReturnValue(aObject: AnyRef, aMethod: Method): AnyRef = {
    var result: AnyRef = null
    try {
      result = aMethod.invoke(aObject, fNO_ARGS)
    }
    catch {
      case ex: IllegalAccessException => {
        vomit(aObject, aMethod)
      }
      case ex: InvocationTargetException => {
        vomit(aObject, aMethod)
      }
    }
    result = dontShowPasswords(result, aMethod)
    result
  }

  private def getMethodFromName(aSpecialClass: Class[_], aMethodName: String): Method = {
    var result: Method = null
    try {
      result = aSpecialClass.getMethod(aMethodName, fNO_PARAMS)
    }
    catch {
      case ex: NoSuchMethodException => {
        vomit(aSpecialClass, aMethodName)
      }
    }
    result
  }

  private def vomit(aObject: AnyRef, aMethod: Method) {
    fLogger.severe("Cannot get value using reflection. Class: " + aObject.getClass.getName + " Method: " + aMethod.getName)
  }

  private def vomit(aSpecialClass: Class[_], aMethodName: String) {
    fLogger.severe("Reflection fails to get no-arg method named: " + Util.quote(aMethodName) + " for class: " + aSpecialClass.getName)
  }

  private def dontShowPasswords(aReturnValue: AnyRef, aMethod: Method): AnyRef = {
    var result: AnyRef = aReturnValue
    val matcher: Matcher = PASSWORD_PATTERN.matcher(aMethod.getName)
    if (matcher.find) {
      result = HIDDEN_PASSWORD_VALUE
    }
    result
  }

  class Ping {
    def setPong(aPong: ToStringUtil.Pong) {
      fPong = aPong
    }

    def getPong: ToStringUtil.Pong = {
      fPong
    }

    def getId: Integer = {
      new Integer(123)
    }

    def getUserPassword: String = {
      "blah"
    }

    override def toString: String = {
      getText(this)
    }

    private var fPong: ToStringUtil.Pong = null
  }

  class Pong {
    def setPing(aPing: ToStringUtil.Ping) {
      fPing = aPing
    }

    def getPing: ToStringUtil.Ping = {
      fPing
    }

    override def toString: String = {
      getTextAvoidCyclicRefs(this, classOf[ToStringUtil.Ping], "getId")
    }

    private var fPing: ToStringUtil.Ping = null
  }

  /**
  Informal test harness.
    */
  def main(args: String*) {
    val list: List[String] = new util.ArrayList[String]
    list.add("blah")
    list.add("blah")
    list.add("blah")
    System.out.println(ToStringUtil.getText(list))
    val parser: StringTokenizer = new StringTokenizer("This is the end.")
    System.out.println(ToStringUtil.getText(parser))
    val ping: ToStringUtil.Ping = new ToStringUtil.Ping
    val pong: ToStringUtil.Pong = new ToStringUtil.Pong
    ping.setPong(pong)
    pong.setPing(ping)
    System.out.println(ping)
    System.out.println(pong)
  }
}

class ToStringUtil {
  private def this() {
    this()
  }
}