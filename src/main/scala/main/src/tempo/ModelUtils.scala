package tempo

/**
 * Created by javier on 6/2/15.
 */
/**
Collected utilities for overriding {@link Object#toString}, {@link Object#equals},
 and {@link Object#hashCode}, and implementing {@link Comparable}.

 <P>All Model Objects should override the above {@link Object} methods.
 All Model Objects that are being sorted in code should implement {@link Comparable}.

 <P>In general, it is easier to use this class with <em>object</em> fields (<tt>String</tt>, <tt>Date</tt>,
 <tt>BigDecimal</tt>, and so on), instead of <em>primitive</em> fields (<tt>int</tt>, <tt>boolean</tt>, and so on).

 <P>See below for example implementations of :
 <ul>
 <li><a href="#ToString">toString()</a>
 <li><a href="#HashCode">hashCode()</a>
 <li><a href="#Equals">equals()</a>
 <li><a href="#Comparable">compareTo()</a>
 </ul>

 <a name="ToString"><P><b>toString()</b><br>
 This class is intended for the most common case, where <tt>toString</tt> is used in
 an <em>informal</em> manner (usually for logging and stack traces). That is, <span class="highlight">
 the caller should not rely on the <tt>toString()</tt> text returned by this class to define program logic.</span>

 <P>Typical example :
<PRE>
  &#064;Override public String toString() {
    ModelUtil.toStringFor(this);
  }
</PRE>

 <P>There is one <em>occasional</em> variation, used only when two model objects reference each other. To avoid
 a problem with cyclic references and infinite looping, implement as :
 <PRE>
  &#064;Override public String toString() {
    ModelUtil.toStringAvoidCyclicRefs(this, Product.class, "getId");
  }
 </PRE>

 Here, the usual behavior is overridden for any method in 'this' object
 which returns a <tt>Product</tt> : instead of calling <tt>Product.toString()</tt>,
 the value of <tt>Product.getId()</tt> is used instead.

 <a name="HashCode"><P><b>hashCode()</b><br>
 Example of the simplest style :
 <pre>
  &#064;Override public int hashCode() {
    ModelUtil.hashFor(getSignificantFields());
  }
  ...
  private String fName;
  private Boolean fIsActive;
  private Object[] getSignificantFields(){
    //any primitive fields can be placed in a wrapper Object
    new Object[]{fName, fIsActive};
  }
 </pre>

 <P><a name="GetSignificantFields"></a><span class="highlight">Since the {@link Object#equals} and
 {@link Object#hashCode} methods are so closely related, and should always refer to the same fields,
 defining a <tt>private</tt> method to the <tt>Object[]</tt> of significant fields is highly
 recommended.</span> Such a method would be called by <em>both</em> <tt>equals</tt> and <tt>hashCode</tt>.

 <P>If an object is <a href="http://www.javapractices.com/Topic29.cjp">immutable</a>,
 then the result may be calculated once, and then cached, as a small performance
 optimization :
 <pre>
  &#064;Override public int hashCode() {
    if ( fHashCode == 0 ) {
      fHashCode = ModelUtil.hashFor(getSignificantFields());
    }
    fHashCode;
  }
  ...
  private String fName;
  private Boolean fIsActive;
  private int fHashCode;
  private Object[] getSignificantFields(){
    new Object[]{fName, fIsActive};
  }
 </pre>

 The most verbose style does not require wrapping primitives in an <tt>Object</tt>:
 <pre>
  &#064;Override public int hashCode(){
    int result = ModelUtil.HASH_SEED;
    //collect the contributions of various fields
    result = ModelUtil.hash(result, fPrimitive);
    result = ModelUtil.hash(result, fObject);
    result = ModelUtil.hash(result, fArray);
    result;
  }
 </pre>

 <a name="Equals"><P><b>equals()</b><br>
 Simplest example, in a class called <tt>Visit</tt> (this is the recommended style):
 <PRE>
  &#064;Override public boolean equals(Object aThat) {
    Boolean result = ModelUtil.quickEquals(this, aThat);
    if ( result == null ){
      Visit that = (Visit) aThat;
      result = ModelUtil.equalsFor(this.getSignificantFields(), that.getSignificantFields());
    }
    result;
  }
  ...
  private final Code fRestaurantCode;
  private final Date fLunchDate;
  private final String fMessage;
  private Object[] getSignificantFields(){
    new Object[] {fRestaurantCode, fLunchDate, fMessage};
  }
 </PRE>

 Second example, in a class called <tt>Member</tt> :
 <PRE>
  &#064;Override public boolean equals( Object aThat ) {
    if ( this == aThat ) true;
    if ( !(aThat instanceof Member) ) false;
    Member that = (Member)aThat;
    ModelUtil.equalsFor(this.getSignificantFields(), that.getSignificantFields());
  }
  ...
  private final String fName;
  private final Boolean fIsActive;
  private final Code fDisposition;
  private Object[] getSignificantFields(){
    new Object[]{fName, fIsActive, fDisposition};
  }
 </PRE>
 See note above regarding <a href="#GetSignificantFields">getSignificantFields()</a>.

 <P>More verbose example, in a class called <tt>Planet</tt> :
 <PRE>
  &#064;Override public boolean equals(Object aThat){
    if ( this == aThat ) true;
    if ( !(aThat instanceof Planet) ) false;
    Planet that = (Planet)aThat;
    return
      EqualsUtil.areEqual(this.fPossiblyNullObject, that.fPossiblyNullObject) &&
      EqualsUtil.areEqual(this.fCollection, that.fCollection) &&
      EqualsUtil.areEqual(this.fPrimitive, that.fPrimitive) &&
      Arrays.equals(this.fArray, that.fArray); //arrays are different!
  }
 </PRE>

 <a name="Comparable"><P><b>compareTo()</b><br>
 The {@link Comparable} interface is distinct, since it is not an overridable method of the
 {@link Object} class.

 <P>Example use case of using <a href='#comparePossiblyNull(T, T, hirondelle.web4j.model.ModelUtil.NullsGo)'>comparePossiblyNull</a>,
 (where <tt>EQUAL</tt> takes the value <tt>0</tt>) :
 <PRE>
  public int compareTo(Movie aThat) {
    if ( this == aThat ) EQUAL;

    int comparison = ModelUtil.comparePossiblyNull(this.fDateViewed, aThat.fDateViewed, NullsGo.LAST);
    if ( comparison != EQUAL ) comparison;

    //this field is never null
    comparison = this.fTitle.compareTo(aThat.fTitle);
    if ( comparison != EQUAL ) comparison;

    comparison = ModelUtil.comparePossiblyNull(this.fRating, aThat.fRating, NullsGo.LAST);
    if ( comparison != EQUAL ) comparison;

    comparison = ModelUtil.comparePossiblyNull(this.fComment, aThat.fComment, NullsGo.LAST);
    if ( comparison != EQUAL ) comparison;

    EQUAL;
  }
 </PRE>

 @author Hirondelle Systems
@author with a contribution by an anonymous user of javapractices.com
  */
object ModelUtil {
  /**
  Implements an override of <tt>Object.toString()</tt> (see class comment).

  <P>Example output format, for an <tt>Rsvp</tt> object with 4 fields :
   <PRE>
  hirondelle.fish.main.rsvp.Rsvp {
  Response: null
  MemberId: 4
  MemberName: Tom Thumb
  VisitId: 13
  }
   </PRE>
   (There is no indentation since it causes problems when there is nesting.)

   <P>The only items which contribute to the result are :
  <ul>
   <li>the full class name
   <li>all no-argument <tt>public</tt> methods which a value
  </ul>

  <P>These items are excluded from the result :
  <ul>
   <li>methods defined in {@link Object}
   <li>factory methods which an object of the native class ("<tt>getInstance()</tt>" methods)
  </ul>

   <P>Reflection is used to access field values. Items are converted to a <tt>String</tt> simply by calling
   their <tt>toString method</tt>, with the following exceptions :
   <ul>
   <li>for arrays, the {@link Util#getArrayAsString(Object)} is used
   <li>for methods whose name contains the text <tt>"password"</tt> (case-insensitive),
   their values hard-coded to '<tt>****</tt>'.
   </ul>

   <P>If the method name follows the pattern '<tt>getXXX</tt>', then the word '<tt>get</tt>'
   is removed from the result.

   <P><span class="highlight">WARNING</span>: If two classes have cyclic references
   (that is, each has a reference to the other), then infinite looping will result
   if <em>both</em> call this method! To avoid this problem, use <tt>toStringFor</tt>
   for one of the classes, and {@link #toStringAvoidCyclicRefs} for the other class.

   @param aObject the object for which a <tt>toString()</tt> result is required.
    */
  def toStringFor(aObject: AnyRef): String = {
    ToStringUtil.getText(aObject)
  }

  /**
  As in {@link #toStringFor}, but avoid problems with cyclic references.

   <P>Cyclic references occur when one Model Object references another, and both Model Objects have
   their <tt>toString()</tt> methods implemented with this utility class.

   <P>Behaves as in {@link #toStringFor}, with one exception: for methods of <tt>aObject</tt> that
   instances of <tt>aSpecialClass</tt>, then call <tt>aMethodName</tt> on such instances,
   instead of <tt>toString()</tt>.
    */
  def toStringAvoidCyclicRefs(aObject: AnyRef, aSpecialClass: Class[_], aMethodName: String): String = {
    ToStringUtil.getTextAvoidCyclicRefs(aObject, aSpecialClass, aMethodName)
  }

  /**
  Return the hash code in a single step, using all significant fields passed in an {@link Object} sequence parameter.

   <P>(This is the recommended way of implementing <tt>hashCode</tt>.)

   <P>Each element of <tt>aFields</tt> must be an {@link Object}, or an array containing
   possibly-null <tt>Object</tt>s. These items will each contribute to the
   result. (It is not a requirement to use <em>all</em> fields related to an object.)

   <P>If the caller is using a <em>primitive</em> field, then it must be converted to a corresponding
   wrapper object to be included in <tt>aFields</tt>. For example, an <tt>int</tt> field would need
   conversion to an {@link Integer} before being passed to this method.
    */
  def hashCodeFor(aFields: AnyRef*): Int = {
    var result: Int = HASH_SEED
    for (field <- aFields) {
      result = hash(result, field)
    }
    result
  }

  /**
  Initial seed value for a <tt>hashCode</tt>.

   Contributions from individual fields are 'added' to this initial value.
   (Using a non-zero value decreases collisons of <tt>hashCode</tt> values.)
    */
  val HASH_SEED: Int = 23

  /** Hash code for <tt>boolean</tt> primitives. */
  def hash(aSeed: Int, aBoolean: Boolean): Int = {
    firstTerm(aSeed) + (if (aBoolean) 1 else 0)
  }

  /** Hash code for <tt>char</tt> primitives. */
  def hash(aSeed: Int, aChar: Char): Int = {
    firstTerm(aSeed) + aChar
  }

  /**
  Hash code for <tt>int</tt> primitives.
   <P>Note that <tt>byte</tt> and <tt>short</tt> are also handled by this method, through implicit conversion.
    */
  def hash(aSeed: Int, aInt: Int): Int = {
    firstTerm(aSeed) + aInt
  }

  /** Hash code for <tt>long</tt> primitives.  */
  def hash(aSeed: Int, aLong: Long): Int = {
    firstTerm(aSeed) + (aLong ^ (aLong >>> 32)).toInt
  }

  /** Hash code for <tt>float</tt> primitives.  */
  def hash(aSeed: Int, aFloat: Float): Int = {
    hash(aSeed, Float.floatToIntBits(aFloat))
  }

  /** Hash code for <tt>double</tt> primitives.  */
  def hash(aSeed: Int, aDouble: Double): Int = {
    hash(aSeed, Double.doubleToLongBits(aDouble))
  }

  /**
  Hash code for an Object.

   <P><tt>aObject</tt> is a possibly-null object field, and possibly an array.

   If <tt>aObject</tt> is an array, then each element may be a primitive
   or a possibly-null object.
    */
  def hash(aSeed: Int, aObject: AnyRef): Int = {
    var result: Int = aSeed
    if (aObject == null) {
      result = hash(result, 0)
    }
    else if (!isArray(aObject)) {
      result = hash(result, aObject.hashCode)
    }
    else {
      val length: Int = Array.getLength(aObject)
      {
        var idx: Int = 0
        while (idx < length) {
          {
            val item: AnyRef = Array.get(aObject, idx)
            result = hash(result, item)
          }
          ({
            idx += 1; idx
          })
        }
      }
    }
    result
  }

  /**
  Quick checks for <em>possibly</em> determining equality of two objects.

   <P>This method exists to make <tt>equals</tt> implementations read more legibly,
   and to avoid multiple <tt>return</tt> statements.

   <P><em>It cannot be used by itself to fully implement <tt>equals</tt>. </em>
   It uses <tt>==</tt> and <tt>instanceof</tt> to determine if equality can be
   found cheaply, without the need to examine field values in detail. It is
   <em>always</em> paired with some other method
   (usually {@link #equalsFor(Object[], Object[])}), as in the following example :
   <PRE>
   public boolean equals(Object aThat){
     Boolean result = ModelUtil.quickEquals(this, aThat);
     <b>if ( result == null ){</b>
       //quick checks not sufficient to determine equality,
       //so a full field-by-field check is needed :
       This this = (This) aThat; //will not fail
       result = ModelUtil.equalsFor(this.getSignificantFields(), that.getSignificantFields());
     }
     result;
   }
   </PRE>

   <P>This method is unusual since it returns a <tt>Boolean</tt> that takes
   <em>3</em> values : <tt>true</tt>, <tt>false</tt>, and <tt>null</tt>. Here,
   <tt>true</tt> and <tt>false</tt> mean that a simple quick check was able to
   determine equality. <span class='highlight'>The <tt>null</tt> case means that the
   quick checks were not able to determine if the objects are equal or not, and that
   further field-by-field examination is necessary. The caller must always perform a
   check-for-null on the value.</span>
    */
  def quickEquals(aThis: AnyRef, aThat: AnyRef): Boolean = {
    var result: Boolean = null
    if (aThis eq aThat) {
      result = Boolean.TRUE
    }
    else {
      val thisClass: Class[_] = aThis.getClass
      if (!thisClass.isInstance(aThat)) {
        result = Boolean.FALSE
      }
    }
    result
  }

  /**
  Return the result of comparing all significant fields.

   <P>Both <tt>Object[]</tt> parameters are the same size. Each includes all fields that have been
   deemed by the caller to contribute to the <tt>equals</tt> method. <em>None of those fields are
   array fields.</em> The order is the same in both arrays, in the sense that the Nth item
   in each array corresponds to the same underlying field. The caller controls the order in which fields are
   compared simply through the iteration order of these two arguments.

   <P>If a primitive field is significant, then it must be converted to a corresponding
   wrapper <tt>Object</tt> by the caller.
    */
  def equalsFor(aThisSignificantFields: Array[AnyRef], aThatSignificantFields: Array[AnyRef]): Boolean = {
    if (aThisSignificantFields.length != aThatSignificantFields.length) {
      throw new IllegalArgumentException("Array lengths do not match. 'This' length is " + aThisSignificantFields.length + ", while 'That' length is " + aThatSignificantFields.length + "")
    }
    var result: Boolean = true
    {
      var idx: Int = 0
      while (idx < aThisSignificantFields.length) {
        {
          if (!areEqual(aThisSignificantFields(idx), aThatSignificantFields(idx))) {
            result = false
            break //todo: break is not supported
          }
        }
        ({
          idx += 1; idx
        })
      }
    }
    result
  }

  /** Equals for <tt>boolean</tt> fields. */
  def areEqual(aThis: Boolean, aThat: Boolean): Boolean = {
    aThis == aThat
  }

  /** Equals for <tt>char</tt> fields. */
  def areEqual(aThis: Char, aThat: Char): Boolean = {
    aThis == aThat
  }

  /**
  Equals for <tt>long</tt> fields.

   <P>Note that <tt>byte</tt>, <tt>short</tt>, and <tt>int</tt> are handled by this method, through
   implicit conversion.
    */
  def areEqual(aThis: Long, aThat: Long): Boolean = {
    aThis == aThat
  }

  /** Equals for <tt>float</tt> fields. */
  def areEqual(aThis: Float, aThat: Float): Boolean = {
    Float.floatToIntBits(aThis) == Float.floatToIntBits(aThat)
  }

  /** Equals for <tt>double</tt> fields. */
  def areEqual(aThis: Double, aThat: Double): Boolean = {
    Double.doubleToLongBits(aThis) == Double.doubleToLongBits(aThat)
  }

  /**
  Equals for possibly-<tt>null</tt> object field.

   <P><em>Does not include arrays</em>. (This restriction will likely be removed in a future version.)
    */
  def areEqual(aThis: AnyRef, aThat: AnyRef): Boolean = {
    if (isArray(aThis) || isArray(aThat)) {
      throw new IllegalArgumentException("This method does not currently support arrays.")
    }
    if (aThis == null) aThat == null else (aThis == aThat)
  }

  /**
  Define hows <tt>null</tt> items are treated in a comparison. Controls if <tt>null</tt>
   items appear first or last.

   <P>See <a href='#comparePossiblyNull(T, T, hirondelle.web4j.model.ModelUtil.NullsGo)'>comparePossiblyNull</a>.
    */
  object NullsGo extends Enumeration {
    type NullsGo = Value
    val FIRST, LAST = Value
  }

  /**
  Utility for implementing {@link Comparable}. See <a href='#Comparable'>class example</a>
   for illustration.

   <P>The {@link Comparable} interface specifies that
   <PRE>
   blah.compareTo(null)
   </PRE> should throw a {@link NullPointerException}. You should follow that
   guideline. Note that this utility method itself
   accepts nulls <em>without</em> throwing a {@link NullPointerException}.
   In this way, this method can handle nullable fields just like any other field.

   <P>There are
   <a href='http://www.javapractices.com/topic/TopicAction.do?Id=207'>special issues</a>
   for sorting {@link String}s regarding case, {@link java.util.Locale},
   and accented characters.

   @param aThis an object that implements { @link Comparable}
  @param aThat an object of the same type as <tt>aThis</tt>
  @param aNullsGo defines if <tt>null</tt> items should be placed first or last
    */
  def comparePossiblyNull[T <: Comparable[T]](aThis: T, aThat: T, aNullsGo: ModelUtil.NullsGo): Int = {
    val EQUAL: Int = 0
    val BEFORE: Int = -1
    val AFTER: Int = 1
    var result: Int = EQUAL
    if (aThis != null && aThat != null) {
      result = aThis.compareTo(aThat)
    }
    else {
      if (aThis == null && aThat == null) {
      }
      else if (aThis == null && aThat != null) {
        result = BEFORE
      }
      else if (aThis != null && aThat == null) {
        result = AFTER
      }
      if (NullsGo.LAST eq aNullsGo) {
        result = (-1) * result
      }
    }
    result
  }

  val fODD_PRIME_NUMBER: Int = 37

  private def firstTerm(aSeed: Int): Int = {
    fODD_PRIME_NUMBER * aSeed
  }

  private def isArray(aObject: AnyRef): Boolean = {
    aObject != null && aObject.getClass.isArray
  }
}

class ModelUtil {
  private def this() {
    this()
  }
}