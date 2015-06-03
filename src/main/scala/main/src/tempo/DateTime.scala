package tempo

/**
 * Created by javier on 6/2/15.
 */

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.List
import java.util.Locale
import java.util.TimeZone


/**
Building block class for an immutable date-time, with no time zone.

 <P>
 This class is provided as an alternative to java.util.{@link java.util.Date}.

<P>This class can hold :
<ul>
  <li>a date-and-time : <tt>1958-03-31 18:59:56.123456789</tt>
  <li>a date only : <tt>1958-03-31</tt>
  <li>a time only : <tt>18:59:56.123456789</tt>
</ul>

 <P>
 <a href='#Examples'>Examples</a><br>
 <a href='#JustificationForThisClass'>Justification For This Class</a><br>
 <a href='#DatesAndTimesInGeneral'>Dates and Times In General</a><br>
 <a href='#TheApproachUsedByThisClass'>The Approach Used By This Class</a><br>
 <a href='#TwoSetsOfOperations'>Two Sets Of Operations</a><br>
 <a href='#ParsingDateTimeAcceptedFormats'>Parsing DateTime - Accepted Formats</a><br>
 <a href='#FormattingLanguage'>Mini-Language for Formatting</a><br>
 <a href='#PassingDateTimeToTheDatabase'>Passing DateTime Objects to the Database</a>

 <a name='Examples'></a>
 <h3> Examples</h3>
 Some quick examples of using this class :
 <PRE>
  DateTime dateAndTime = new DateTime("2010-01-19 23:59:59");
  //highest precision is nanosecond, not millisecond:
  DateTime dateAndTime = new DateTime("2010-01-19 23:59:59.123456789");

  DateTime dateOnly = new DateTime("2010-01-19");
  DateTime timeOnly = new DateTime("23:59:59");

  DateTime dateOnly = DateTime.forDateOnly(2010,01,19);
  DateTime timeOnly = DateTime.forTimeOnly(23,59,59,0);

  DateTime dt = new DateTime("2010-01-15 13:59:15");
  boolean leap = dt.isLeapYear(); //false
  dt.getNumDaysInMonth(); //31
  dt.getStartOfMonth(); //2010-01-01, 00:00:00
  dt.getEndOfDay(); //2010-01-15, 23:59:59
  dt.format("YYYY-MM-DD"); //formats as '2010-01-15'
  dt.plusDays(30); //30 days after Jan 15
  dt.numDaysFrom(someDate); //returns an int
  dueDate.lt(someDate); //less-than
  dueDate.lteq(someDate); //less-than-or-equal-to

  DateTime.now(aTimeZone);
  DateTime.today(aTimeZone);
  DateTime fromMilliseconds = DateTime.forInstant(31313121L, aTimeZone);
  birthday.isInFuture(aTimeZone);
 </PRE>

 <a name='JustificationForThisClass'></a>
 <h3> Justification For This Class</h3>
 The fundamental reasons why this class exists are :
 <ul>
 <li>to avoid the embarrassing number of distasteful inadequacies in the JDK's date classes
 <li>to oppose the very "mental model" of the JDK's date-time classes with something significantly simpler
 </ul>

 <a name='MentalModels'></a>
 <P><b>There are 2 distinct mental models for date-times, and they don't play well together</b> :
 <ul>
 <li><b>timeline</b> - an instant on the timeline, as a physicist would picture it, representing the number of
 seconds from some epoch. In this picture, such a date-time can have many, many different
 representations according to calendar and time zone. That is, the date-time, <i> as seen and understood by
 the end user</i>, can change according to "who's looking at it". It's important to understand that a timeline instant,
 before being presented to the user, <i>must always have an associated time zone - even in the case of
 a date only, with no time.</i>
 <li><b>everyday</b> - a date-time in the Gregorian calendar, such as '2009-05-25 18:25:00',
 which never changes according to "who's looking at it". Here, <i>the time zone is always both implicit and immutable</i>.
 </ul>

 <P>The problem is that java.util.{@link java.util.Date} uses <i>only</i> the timeline style, while <i>most</i> users, <i>most</i>
 of the time, think in terms of the <i>other</i> mental model - the 'everday' style.

 In particular, there are a large number of applications which experience
 <a href='http://martinfowler.com/bliki/TimeZoneUncertainty.html'>problems with time zones</a>, because the timeline model
 is used instead of the everday model.
 <i>Such problems are often seen by end users as serious bugs, because telling people the wrong date or time is often a serious issue.</i>
 <b>These problems make you look stupid.</b>

 <a name='JDKDatesMediocre'></a>
 <h4>Date Classes in the JDK are Mediocre</h4>
 The JDK's classes related to dates are widely regarded as frustrating to work with, for various reasons:
 <ul>
 <li>mistakes regarding time zones are very common
 <li>month indexes are 0-based, leading to off-by-one errors
 <li>difficulty of calculating simple time intervals
 <li><tt>java.util.Date</tt> is mutable, but 'building block' classes should be
 immutable
 <li>numerous other minor nuisances
 </ul>

 <a name='JodaTimeDrawbacks'></a>
 <h4>Joda Time Has Drawbacks As Well</h4>
 The <a href='http://joda-time.sourceforge.net/'>Joda Time</a> library is used by some programmers as an alternative
 to the JDK classes. Joda Time has the following drawbacks :
 <ul>
 <li>it limits precision to milliseconds. Database timestamp values almost always have a precision of microseconds
 or even nanoseconds. This is a serious defect: <b>a library should never truncate your data, for any reason.</b>
 <li>it's large, with well over 100 items in its <a href='http://joda-time.sourceforge.net/api-release/index.html'>javadoc</a>
 <li>in order to stay current, it needs to be manually updated occasionally with fresh time zone data
 <li>it has mutable versions of classes
 <li>it always coerces March 31 + 1 Month to April 30 (for example), without giving you any choice in the matter
 <li>some databases allow invalid date values such as '0000-00-00', but Joda Time doesn't seem to be able to handle them
 </ul>


 <a name='DatesAndTimesInGeneral'></a>
 <h3>Dates and Times in General</h3>

 <h4>Civil Timekeeping Is Complex</h4>
 Civil timekeeping is a byzantine hodge-podge of arcane and arbitrary rules. Consider the following :
 <ul>
 <li>months have varying numbers of days
 <li>one month (February) has a length which depends on the year
 <li>not all years have the same number of days
 <li>time zone rules spring forth arbitrarily from the fecund imaginations of legislators
 <li>summer hours mean that an hour is 'lost' in the spring, while another hour must
 repeat itself in the autumn, during the switch back to normal time
 <li>summer hour logic varies widely across various jurisdictions
 <li>the cutover from the Julian calendar to the Gregorian calendar happened at different times in
 different places, which causes a varying number of days to be 'lost' during the cutover
 <li>occasional insertion of leap seconds are used to ensure synchronization with the
 rotating Earth (whose speed of rotation is gradually slowing down, in an irregular way)
 <li>there is no year 0 (1 BC is followed by 1 AD), except in the reckoning used by
 astronomers
 </ul>

 <h4>How Databases Treat Dates</h4>
 <b>Most databases model dates and times using the Gregorian Calendar in an aggressively simplified form</b>,
 in which :
 <ul>
 <li>the Gregorian calendar is extended back in time as if it was in use previous to its
 inception (the 'proleptic' Gregorian calendar)
 <li>the transition between Julian and Gregorian calendars is entirely ignored
 <li>leap seconds are entirely ignored
 <li>summer hours are entirely ignored
 <li>often, even time zones are ignored, in the sense that <i>the underlying database
 column doesn't usually explicitly store any time zone information</i>.
 </ul>

 <P><a name='NoTimeZoneInDb'></a>The final point requires elaboration.
 Some may doubt its veracity, since they have seen date-time information "change time zone" when
 retrieved from a database. But this sort of change is usually applied using logic which is <i>external</i> to the data
 stored in the particular column.

 <P> For example, the following items might be used in the calculation of a time zone difference :
 <ul>
 <li>time zone setting for the client (or JDBC driver)
 <li>time zone setting for the client's connection to the database server
 <li>time zone setting of the database server
 <li>time zone setting of the host where the database server resides
 </ul>

 <P>(Note as well what's <i>missing</i> from the above list: your own application's logic, and the user's time zone preference.)

 <P>When an end user sees such changes to a date-time, all they will say to you is
 <i>"Why did you change it? That's not what I entered"</i> - and this is a completely valid question.
 Why <i>did</i> you change it? Because you're using the timeline model instead of the everyday model.
 Perhaps you're using a inappropriate abstraction for what the user really wants.

<a name='TheApproachUsedByThisClass'></a>
 <h3>The Approach Used By This Class</h3>

 This class takes the following design approach :
 <ul>
 <li>it models time in the "everyday" style, not in the "timeline" style (see <a href='#MentalModels'>above</a>)
 <li>its precision matches the highest precision used by databases (nanosecond)
 <li>it uses only the proleptic Gregorian Calendar, over the years <tt>1..9999</tt>
 <li><i>it ignores all non-linearities</i>: summer-hours, leap seconds, and the cutover
 from Julian to Gregorian calendars
 <li><i>it ignores time zones</i>. Most date-times are stored in columns whose type
 does <i>not</i> include time zone information (see note <a href='#NoTimeZoneInDb'>above</a>).
 <li>it has (very basic) support for wonky dates, such as the magic value <tt>0000-00-00</tt> used by MySQL
 <li>it's immutable
 <li>it lets you choose among 4 policies for 'day overflow' conditions during calculations
 </ul>

 <P>Even though the above list may appear restrictive, it's very likely true that
 <tt>DateTime</tt> can handle the dates and times you're currently storing in your database.

<a name='TwoSetsOfOperations'></a>
 <h3>Two Sets Of Operations</h3>
 This class allows for 2 sets of operations: a few "basic" operations, and many "computational" ones.

 <P><b>Basic operations</b> model the date-time as a simple, dumb String, with absolutely no parsing or substructure.
 This will always allow your application to reflect exactly what is in a <tt>ResultSet</tt>, with
 absolutely no modification for time zone, locale, or for anything else.

 <P>This is meant as a back-up, to ensure that <i>your application will always be able
 to, at the very least, display a date-time exactly as it appears in your
 <tt>ResultSet</tt> from the database</i>. This style is particularly useful for handling invalid
 dates such as <tt>2009-00-00</tt>, which can in fact be stored by some databases (MySQL, for
 example). It can also be used to handle unusual items, such as MySQL's
 <a href='http://dev.mysql.com/doc/refman/5.1/en/time.html'>TIME</a> datatype.

 <P>The basic operations are represented by {@link #DateTime(String)}, {@link #toString()}, and {@link #getRawDateString()}.

 <P><b>Computational operations</b> allow for calculations and formatting.
 If a computational operation is performed by this class (for example, if the caller asks for the month),
 then any underlying date-time String must be parseable by this class into its components - year, month, day, and so on.
 Computational operations require such parsing, while the basic operations do not. Almost all methods in this class
 are categorized as computational operations.

 <a name="ParsingDateTimeAcceptedFormats"></a>
 <h3>Parsing DateTime - Accepted Formats</h3>
  The {@link #DateTime(String)} constructor accepts a <tt>String</tt> representation of a date-time.
 The format of the String can take a number of forms. When retrieving date-times from a database, the
 majority of cases will have little problem in conforming to these formats. If necessary, your SQL statements
 can almost always use database formatting functions to generate a String whose format conforms to one of the
 many formats accepted by the {@link #DateTime(String)} constructor.

   <p>The {@link #isParseable(String)} method lets you explicitly test if a given String is in a form that can be parsed by this class.

 <a name="FormattingLanguage"></a>
 <h3>Mini-Language for Formatting</h3>
 This class defines a simple mini-language for formatting a <tt>DateTime</tt>, used by the various <tt>format</tt> methods.

 <P>The following table defines the symbols used by this mini-language, and the corresponding text they
 would generate given the date:
 <PRE>1958-04-09 Wednesday, 03:05:06.123456789 AM</PRE>
 in an English Locale. (Items related to date are in upper case, and items related to time are in lower case.)

 <P><table border='1' cellpadding='3' cellspacing='0'>
 <tr><th>Format</th><th>Output</th> <th>Description</th><th>Needs Locale?</th></tr>
 <tr><td>YYYY</td> <td>1958</td> <td>Year</td><td>...</td></tr>
 <tr><td>YY</td> <td>58</td> <td>Year without century</td><td>...</td></tr>
 <tr><td>M</td> <td>4</td> <td>Month 1..12</td><td>...</td></tr>
 <tr><td>MM</td> <td>04</td> <td>Month 01..12</td><td>...</td></tr>
 <tr><td>MMM</td> <td>Apr</td> <td>Month Jan..Dec</td><td>Yes</td></tr>
 <tr><td>MMMM</td> <td>April</td> <td>Month January..December</td><td>Yes</td></tr>
 <tr><td>DD</td> <td>09</td> <td>Day 01..31</td><td>...</td></tr>
 <tr><td>D</td> <td>9</td> <td>Day 1..31</td><td>...</td></tr>
 <tr><td>WWWW</td> <td>Wednesday</td> <td>Weekday Sunday..Saturday</td><td>Yes</td></tr>
 <tr><td>WWW</td> <td>Wed</td> <td>Weekday Sun..Sat</td><td>Yes</td></tr>
 <tr><td>hh</td> <td>03</td> <td>Hour 01..23</td><td>...</td></tr>
 <tr><td>h</td> <td>3</td> <td>Hour 1..23</td><td>...</td></tr>
 <tr><td>hh12</td> <td>03</td> <td>Hour 01..12</td><td>...</td></tr>
 <tr><td>h12</td> <td>3</td> <td>Hour 1..12</td><td>...</td></tr>
 <tr><td>a</td> <td>AM</td> <td>AM/PM Indicator</td><td>Yes</td></tr>
 <tr><td>mm</td> <td>05</td> <td>Minutes 01..59</td><td>...</td></tr>
 <tr><td>m</td> <td>5</td> <td>Minutes 1..59</td><td>...</td></tr>
 <tr><td>ss</td> <td>06</td> <td>Seconds 01..59</td><td>...</td></tr>
 <tr><td>s</td> <td>6</td> <td>Seconds 1..59</td><td>...</td></tr>
 <tr><td>f</td> <td>1</td> <td>Fractional Seconds, 1 decimal</td><td>...</td></tr>
 <tr><td>ff</td> <td>12</td> <td>Fractional Seconds, 2 decimals</td><td>...</td></tr>
 <tr><td>fff</td> <td>123</td> <td>Fractional Seconds, 3 decimals</td><td>...</td></tr>
 <tr><td>ffff</td> <td>1234</td> <td>Fractional Seconds, 4 decimals</td><td>...</td></tr>
 <tr><td>fffff</td> <td>12345</td> <td>Fractional Seconds, 5 decimals</td><td>...</td></tr>
 <tr><td>ffffff</td> <td>123456</td> <td>Fractional Seconds, 6 decimals</td><td>...</td></tr>
 <tr><td>fffffff</td> <td>1234567</td> <td>Fractional Seconds, 7 decimals</td><td>...</td></tr>
 <tr><td>ffffffff</td> <td>12345678</td> <td>Fractional Seconds, 8 decimals</td><td>...</td></tr>
 <tr><td>fffffffff</td> <td>123456789</td> <td>Fractional Seconds, 9 decimals</td><td>...</td></tr>
 <tr><td>|</td> <td>(no example)</td> <td>Escape characters</td><td>...</td></tr>
 </table>

 <P>As indicated above, some of these symbols can only be used with an accompanying <tt>Locale</tt>.
 In general, if the output is text, not a number, then a <tt>Locale</tt> will be needed.
 For example, 'September' is localizable text, while '09' is a numeric representation, which doesn't require a <tt>Locale</tt>.
 Thus, the symbol 'MM' can be used without a <tt>Locale</tt>, while 'MMMM' and 'MMM' both require a <tt>Locale</tt>, since they
 generate text, not a number.

 <P>The fractional seconds 'f' doesn't perform any rounding.

 <P>The escape character '|' allows you
 to insert arbitrary text. The escape character always appears in pairs; these pairs define a range of characters
 over which the text will not be interpreted using the special format symbols defined above.

 <P>Here are some practical examples of using the above formatting symbols:
 <table border='1' cellpadding='3' cellspacing='0'>
 <tr><th>Format</th><th>Output</th></tr>
 <tr><td>YYYY-MM-DD hh:mm:ss.fffffffff a</td> <td>1958-04-09 03:05:06.123456789 AM</td></tr>
 <tr><td>YYYY-MM-DD hh:mm:ss.fff a</td> <td>1958-04-09 03:05:06.123 AM</td></tr>
 <tr><td>YYYY-MM-DD</td> <td>1958-04-09</td></tr>
 <tr><td>hh:mm:ss.fffffffff</td> <td>03:05:06.123456789</td></tr>
 <tr><td>hh:mm:ss</td> <td>03:05:06</td></tr>
 <tr><td>YYYY-M-D h:m:s</td> <td>1958-4-9 3:5:6</td></tr>
 <tr><td>WWWW, MMMM D, YYYY</td> <td>Wednesday, April 9, 1958</td></tr>
 <tr><td>WWWW, MMMM D, YYYY |at| D a</td> <td>Wednesday, April 9, 1958 at 3 AM</td></tr>
 </table>

 <P>In the last example, the escape characters are needed only because 'a', the formating symbol for am/pm, appears in the text.

 <a name='PassingDateTimeToTheDatabase'></a>
 <h3>Passing DateTime Objects to the Database</h3>
 When a <tt>DateTime</tt> is passed as a parameter to an SQL statement, the <tt>DateTime</tt> can always
 be formatted into a <tt>String</tt> of a form accepted by the database, using one of the <tt>format</tt> methods.
  */
object DateTime {

  /** The seven parts of a <tt>DateTime</tt> object. The <tt>DAY</tt> represents the day of the month (1..31), not the weekday. */
  object Unit extends Enumeration {
    type Unit = Value
    val YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, NANOSECONDS = Value
  }

  /**
  Policy for treating 'day-of-the-month overflow' conditions encountered during some date calculations.

   <P>Months are different from other units of time, since the length of a month is not fixed, but rather varies with
   both month and year. This leads to problems. Take the following simple calculation, for example :

   <PRE>May 31 + 1 month = ?</PRE>

   <P>What's the answer? Since there is no such thing as June 31, the result of this operation is inherently ambiguous.
   This  <tt>DayOverflow</tt> enumeration lists the various policies for treating such situations, as supported by
   <tt>DateTime</tt>.

   <P>This table illustrates how the policies behave :
   <P><table BORDER="1" CELLPADDING="3" CELLSPACING="0">
   <tr>
   <th>Date</th>
   <th>DayOverflow</th>
   <th>Result</th>
   </tr>
   <tr>
   <td>May 31 + 1 Month</td>
   <td>LastDay</td>
   <td>June 30</td>
   </tr>
   <tr>
   <td>May 31 + 1 Month</td>
   <td>FirstDay</td>
   <td>July 1</td>
   </tr>
   <tr>
   <td>December 31, 2001 + 2 Months</td>
   <td>Spillover</td>
   <td>March 3</td>
   </tr>
   <tr>
   <td>May 31 + 1 Month</td>
   <td>Abort</td>
   <td>RuntimeException</td>
   </tr>
   </table>
    */
  object DayOverflow extends Enumeration {
    type DayOverflow = Value
    val LastDay, FirstDay, Spillover, Abort = Value
  }

  /**
  Return <tt>true</tt> only if the given String follows one of the formats documented by {@link #DateTime(String)}.
   <P>If the text is not from a trusted source, then the caller may use this method to validate whether the text
   is in a form that's parseable by this class.
    */
  def isParseable(aCandidateDateTime: String): Boolean = {
    var result: Boolean = true
    try {
      val dt: DateTime = new DateTime(aCandidateDateTime)
      dt.ensureParsed
    }
    catch {
      case ex: RuntimeException => {
        result = false
      }
    }
    result
  }

  /**
  Factory method returns a <tt>DateTime</tt> having year-month-day only, with no time portion.
   <P>See {@link #DateTime(Integer, Integer, Integer, Integer, Integer, Integer, Integer)} for constraints on the parameters.
    */
  def forDateOnly(aYear: Integer, aMonth: Integer, aDay: Integer): DateTime = {
    new DateTime(aYear, aMonth, aDay, null, null, null, null)
  }

  /**
  Factory method returns a <tt>DateTime</tt> having hour-minute-second-nanosecond only, with no date portion.
   <P>See {@link #DateTime(Integer, Integer, Integer, Integer, Integer, Integer, Integer)} for constraints on the parameters.
    */
  def forTimeOnly(aHour: Integer, aMinute: Integer, aSecond: Integer, aNanoseconds: Integer): DateTime = {
    new DateTime(null, null, null, aHour, aMinute, aSecond, aNanoseconds)
  }

  /**
  Constructor taking a millisecond value and a {@link TimeZone}.
   This constructor may be use to convert a <tt>java.util.Date</tt> into a <tt>DateTime</tt>.

   <P>To use nanosecond precision, please use {@link #forInstantNanos(long, TimeZone)} instead.

   @param aMilliseconds must be in the range corresponding to the range of dates supported by this class (year 1..9999); corresponds
   to a millisecond instant on the time-line, measured from the epoch used by { @link java.util.Date}.
    */
  def forInstant(aMilliseconds: Long, aTimeZone: TimeZone): DateTime = {
    val calendar: Calendar = new GregorianCalendar(aTimeZone)
    calendar.setTimeInMillis(aMilliseconds)
    val year: Int = calendar.get(Calendar.YEAR)
    val month: Int = calendar.get(Calendar.MONTH) + 1
    val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
    val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
    val minute: Int = calendar.get(Calendar.MINUTE)
    val second: Int = calendar.get(Calendar.SECOND)
    val milliseconds: Int = calendar.get(Calendar.MILLISECOND)
    val nanoseconds: Int = milliseconds * 1000 * 1000
    new DateTime(year, month, day, hour, minute, second, nanoseconds)
  }

  /**
  Constructor taking a nanosecond value and a {@link TimeZone}.

   <P>To use milliseconds instead of nanoseconds, please use {@link #forInstant(long, TimeZone)}.

   @param aNanoseconds must be in the range corresponding to the range of dates supported by this class (year 1..9999); corresponds
   to a nanosecond instant on the time-line, measured from the epoch used by { @link java.util.Date}.
    */
  def forInstantNanos(aNanoseconds: Long, aTimeZone: TimeZone): DateTime = {
    var millis: Long = aNanoseconds / MILLION
    var nanosRemaining: Long = aNanoseconds % MILLION
    if (aNanoseconds < 0) {
      millis = millis - 1
      nanosRemaining = MILLION + nanosRemaining
    }
    val calendar: Calendar = new GregorianCalendar(aTimeZone)
    calendar.setTimeInMillis(millis)
    val year: Int = calendar.get(Calendar.YEAR)
    val month: Int = calendar.get(Calendar.MONTH) + 1
    val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
    val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
    val minute: Int = calendar.get(Calendar.MINUTE)
    val second: Int = calendar.get(Calendar.SECOND)
    val milliseconds: Int = calendar.get(Calendar.MILLISECOND)
    val withoutNanos: DateTime = new DateTime(year, month, day, hour, minute, second, milliseconds * MILLION)
    val withNanos: DateTime = withoutNanos.plus(0, 0, 0, 0, 0, 0, nanosRemaining.toInt, DayOverflow.Spillover)
    withNanos
  }

  /**
  Return the current date-time.
   <P>Combines the value of {@link System#currentTimeMillis()} with the given {@link TimeZone}.

   <P>Only millisecond precision is possible for this method.
    */
  def now(aTimeZone: TimeZone): DateTime = {
    forInstant(System.currentTimeMillis, aTimeZone)
  }

  /**
  Return the current date.
   <P>As in {@link #now(TimeZone)}, but truncates the time portion, leaving only year-month-day.
    */
  def today(aTimeZone: TimeZone): DateTime = {
    val result: DateTime = now(aTimeZone)
    result.truncate(Unit.DAY)
  }



  /**
  Return the number of days in the given month. The returned value depends on the year as
   well, because of leap years. Returns <tt>null</tt> if either year or month are
   absent. WRONG - should be public??
   Package-private, needed for interval calcs.
    */
  def getNumDaysInMonth(aYear: Integer, aMonth: Integer): Integer = {
    var result: Integer = null
    if (aYear != null && aMonth != null) {
      if (aMonth eq 1) {
        result = 31
      }
      else if (aMonth eq 2) {
        result = if (isLeapYear(aYear)) 29 else 28
      }
      else if (aMonth eq 3) {
        result = 31
      }
      else if (aMonth eq 4) {
        result = 30
      }
      else if (aMonth eq 5) {
        result = 31
      }
      else if (aMonth eq 6) {
        result = 30
      }
      else if (aMonth eq 7) {
        result = 31
      }
      else if (aMonth eq 8) {
        result = 31
      }
      else if (aMonth eq 9) {
        result = 30
      }
      else if (aMonth eq 10) {
        result = 31
      }
      else if (aMonth eq 11) {
        result = 30
      }
      else if (aMonth eq 12) {
        result = 31
      }
      else {
        throw new AssertionError("Month is out of range 1..12:" + aMonth)
      }
    }
    result
  }

  def fromJulianDayNumberAtNoon(aJDAtNoon: Int): DateTime = {
    var l: Int = aJDAtNoon + 68569
    val n: Int = (4 * l) / 146097
    l = l - (146097 * n + 3) / 4
    val i: Int = (4000 * (l + 1)) / 1461001
    l = l - (1461 * i) / 4 + 31
    val j: Int = (80 * l) / 2447
    val d: Int = l - (2447 * j) / 80
    l = j / 11
    val m: Int = j + 2 - (12 * l)
    val y: Int = 100 * (n - 49) + i + l
    DateTime.forDateOnly(y, m, d)
  }

  val EQUAL: Int = 0
  private var EPOCH_MODIFIED_JD: Int = 2400000
  val MILLION: Int = 1000000

  private def isLeapYear(aYear: Integer): Boolean = {
    var result: Boolean = false
    if (aYear % 100 == 0) {
      if (aYear % 400 == 0) {
        result = true
      }
    }
    else if (aYear % 4 == 0) {
      result = true
    }
    result
  }
}

/**
Constructor taking each time unit explicitly.

   <P>Although all parameters are optional, many operations on this class require year-month-day to be
   present.

  @param aYear 1..9999, optional
@param aMonth 1..12 , optional
@param aDay 1..31, cannot exceed the number of days in the given month/year, optional
@param aHour 0..23, optional
@param aMinute 0..59, optional
@param aSecond 0..59, optional
@param aNanoseconds 0..999,999,999, optional (allows for databases that store timestamps up to
   nanosecond precision).
  */
case class DateTime(year: Integer, month: Integer, day: Integer, hour: Integer, minute: Integer, second: Integer, nanosecond: Integer) extends Comparable[DateTime] {
  /**
  Constructor taking a date-time as a String. The text is trimmed by this class.

   <P> When this constructor is called, the underlying text can be in an absolutely arbitrary
   form, since it will not, initially, be parsed in any way. This policy of extreme
   leniency allows you to use dates in an arbitrary format, without concern over possible
   transformations of the date (time zone in particular), and without concerns over possibly bizarre content, such
   as '2005-00-00', as seen in some databases, such as MySQL.

   <P><i>However</i>, the moment you attempt to call <a href='#TwoSetsOfOperations'>almost any method</a>
   in this class, an attempt will be made to parse
   the given date-time string into its constituent parts. Then, if the date-time string does not match one of the
   example formats listed below, a <tt>RuntimeException</tt> will be thrown.

   <P>Before calling this constructor, you may wish to call {@link #isParseable(String)} to explicitly test whether a
   given String is parseable by this class.

   <P>The full date format expected by this class is <tt>'YYYY-MM-YY hh:mm:ss.fffffffff'</tt>.
   All fields except for the fraction of a second have a fixed width.
   In addition, various portions of this format are also accepted by this class.

   <P>All of the following dates can be parsed by this class to make a <tt>DateTime</tt> :
   <ul>
   <li><tt>2009-12-31 00:00:00.123456789</tt>
   <li><tt>2009-12-31T00:00:00.123456789</tt>
   <li><tt>2009-12-31 00:00:00.12345678</tt>
   <li><tt>2009-12-31 00:00:00.1234567</tt>
   <li><tt>2009-12-31 00:00:00.123456</tt>
   <li><tt>2009-12-31 23:59:59.12345</tt>
   <li><tt>2009-01-31 16:01:01.1234</tt>
   <li><tt>2009-01-01 16:59:00.123</tt>
   <li><tt>2009-01-01 16:00:01.12</tt>
   <li><tt>2009-02-28 16:25:17.1</tt>
   <li><tt>2009-01-01 00:01:01</tt>
   <li><tt>2009-01-01T00:01:01</tt>
   <li><tt>2009-01-01 16:01</tt>
   <li><tt>2009-01-01 16</tt>
   <li><tt>2009-01-01</tt>
   <li><tt>2009-01</tt>
   <li><tt>2009</tt>
   <li><tt>0009</tt>
   <li><tt>9</tt>
   <li><tt>00:00:00.123456789</tt>
   <li><tt>00:00:00.12345678</tt>
   <li><tt>00:00:00.1234567</tt>
   <li><tt>00:00:00.123456</tt>
   <li><tt>23:59:59.12345</tt>
   <li><tt>01:59:59.1234</tt>
   <li><tt>23:01:59.123</tt>
   <li><tt>00:00:00.12</tt>
   <li><tt>00:59:59.1</tt>
   <li><tt>23:59:00</tt>
   <li><tt>23:00:10</tt>
   <li><tt>00:59</tt>
   </ul>

   <P>The range of each field is :
   <ul>
   <li>year: 1..9999 (leading zeroes optional)
   <li>month: 01..12
   <li>day: 01..31
   <li>hour: 00..23
   <li>minute: 00..59
   <li>second: 00..59
   <li>nanosecond: 0..999999999
   </ul>

   <P>Note that <b>database format functions</b> are an option when dealing with date formats.
   Since your application is always in control of the SQL used to talk to the database, you can, if needed, usually
    use database format functions to alter the format of dates returned in a <tt>ResultSet</tt>.
    */

  require(1 <= year && year >= 9999)
  require(1 <= month && month >= 12)
  require(1 <= day && day >= 31)
  require(0 <= hour && hour >= 23)
  require(0 <= minute && minute >= 59)
  require(0 <= second && second >= 59)
  require(0 <= nanosecond && nanosecond >= 999999999)

  //TODO def this(strDate: String) => replace with companion object factory method




  /**
  For the given time zone,  the corresponding time in milliseconds-since-epoch for this <tt>DateTime</tt>.

    <P>This method is meant to help you convert between a <tt>DateTime</tt> and the
    JDK's date-time classes, which are based on the combination of a time zone and a
    millisecond value from the Java epoch.
    <P>Since <tt>DateTime</tt> can go to nanosecond accuracy, the value can
    lose precision. The nanosecond value is truncated to milliseconds, not rounded.
    To retain nanosecond accuracy, please use {@link #getNanosecondsInstant(TimeZone)} instead.
   <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
    */
  def getMilliseconds(aTimeZone: TimeZone): Long = {

    val calendar: Calendar = new GregorianCalendar(aTimeZone)
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month - 1)
    calendar.set(Calendar.DAY_OF_MONTH, day)
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, second)
    calendar.set(Calendar.MILLISECOND, nanosecond / 1000000)
    calendar.getTimeInMillis
  }

  /**
  For the given time zone,  the corresponding time in nanoseconds-since-epoch for this <tt>DateTime</tt>.

   <P>For conversion between a <tt>DateTime</tt> and the JDK's date-time classes,
   you should likely use {@link #getMilliseconds(TimeZone)} instead.
  <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
    */
  def getNanosecondsInstant(aTimeZone: TimeZone): Long = {
    val year: Integer = getYear
    val month: Integer = getMonth
    val day: Integer = getDay
    val hour: Integer = if (getHour == null) 0 else getHour
    val minute: Integer = if (getMinute == null) 0 else getMinute
    val second: Integer = if (getSecond == null) 0 else getSecond
    val nanos: Integer = if (getNanoseconds == null) 0 else getNanoseconds
    val millis: Int = nanos / DateTime.MILLION
    val nanosRemaining: Int = nanos % DateTime.MILLION
    val calendar: Calendar = new GregorianCalendar(aTimeZone)
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month - 1)
    calendar.set(Calendar.DAY_OF_MONTH, day)
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, second)
    calendar.set(Calendar.MILLISECOND, millis)
    val baseResult: Long = calendar.getTimeInMillis * DateTime.MILLION
    baseResult + nanosRemaining
  }

  /**
  Return the raw date-time String passed to the {@link #DateTime(String)} constructor.
   Returns <tt>null</tt> if that constructor was not called. See {@link #toString()} as well.
    */
  def getRawDateString: String = {
    fDateTime
  }


  /**
  Return the Modified Julian Day Number.
   <P>The Modified Julian Day Number is defined by astronomers for simplifying the calculation of the number of days between 2 dates.
   Returns a monotonically increasing sequence number.
   Day 0 is November 17, 1858 00:00:00 (whose Julian Date was 2400000.5).

   <P>Using the Modified Julian Day Number instead of the Julian Date has 2 advantages:
   <ul>
   <li>it's a smaller number
   <li>it starts at midnight, not noon (Julian Date starts at noon)
   </ul>

   <P>Does not reflect any time portion, if present.

   <P>(In spite of its name, this method, like all other methods in this class, uses the
   proleptic Gregorian calendar - not the Julian calendar.)

   <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
    */
  def getModifiedJulianDayNumber: Integer = {
    ensureHasYearMonthDay
    val result: Int = calculateJulianDayNumberAtNoon - 1 - DateTime.EPOCH_MODIFIED_JD
    result
  }

  /**
  Return an index for the weekday for this <tt>DateTime</tt>.
   Returns 1..7 for Sunday..Saturday.
   <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
    */
  def getWeekDay: Integer = {
    ensureHasYearMonthDay
    val dayNumber: Int = calculateJulianDayNumberAtNoon + 1
    val index: Int = dayNumber % 7
    index + 1
  }

  /**
  Return an integer in the range 1..366, representing a count of the number of days from the start of the year.
   January 1 is counted as day 1.
   <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
    */
  def getDayOfYear: Integer = {
    ensureHasYearMonthDay
    val k: Int = if (isLeapYear) 1 else 2
    val result: Integer = ((275 * month) / 9) - k * ((month + 9) / 12) + day - 30
    result
  }

  /**
  Returns true only if the year is a leap year.
   <P>Requires year to be present; if not, a runtime exception is thrown.
    */
  def isLeapYear: Boolean = {
    ensureParsed
    var result: Boolean = null
    if (isPresent(year)) {
      result = DateTime.isLeapYear(year)
    }
    else {
      throw new DateTime.MissingItem("Year is absent. Cannot determine if leap year.")
    }
    result
  }

  /**
  Return the number of days in the month which holds this <tt>DateTime</tt>.
   <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
    */
  def getNumDaysInMonth: Int = {
    ensureHasYearMonthDay
    DateTime.getNumDaysInMonth(year, month)
  }

  /**
  Return The week index of this <tt>DateTime</tt> with respect to a given starting <tt>DateTime</tt>.
   <P>The single parameter to this method defines first day of week number 1.
   See {@link #getWeekIndex()} as well.
   <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
    */
  def getWeekIndex(aStartingFromDate: DateTime): Integer = {
    ensureHasYearMonthDay
    aStartingFromDate.ensureHasYearMonthDay
    val diff: Int = getModifiedJulianDayNumber - aStartingFromDate.getModifiedJulianDayNumber
    (diff / 7) + 1
  }

  /**
  Return The week index of this <tt>DateTime</tt>, taking day 1 of week 1 as Sunday, January 2, 2000.
   <P>See {@link #getWeekIndex(DateTime)} as well, which takes an arbitrary date to define
   day 1 of week 1.
   <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
    */
  def getWeekIndex: Integer = {
    val start: DateTime = DateTime.forDateOnly(2000, 1, 2)
    getWeekIndex(start)
  }

  /**
  Return <tt>true</tt> only if this <tt>DateTime</tt> has the same year-month-day as the given parameter.
   Time is ignored by this method.
   <P> Requires year-month-day to be present, both for this <tt>DateTime</tt> and for
   <tt>aThat</tt>; if not, a runtime exception is thrown.
    */
  def isSameDayAs(aThat: DateTime): Boolean = {
    var result: Boolean = false
    ensureHasYearMonthDay
    aThat.ensureHasYearMonthDay
    result = ((year == aThat.year) && (month == aThat.month) && (day == aThat.day))
    result
  }

  /**
  'Less than' comparison.
   Return <tt>true</tt> only if this <tt>DateTime</tt> comes before the given parameter, according to {@link #compareTo(DateTime)}.
    */
  def lt(aThat: DateTime): Boolean = {
    compareTo(aThat) < DateTime.EQUAL
  }

  /**
  'Less than or equal to' comparison.
   Return <tt>true</tt> only if this <tt>DateTime</tt> comes before the given parameter, according to {@link #compareTo(DateTime)},
   or this <tt>DateTime</tt> equals the given parameter.
    */
  def lteq(aThat: DateTime): Boolean = {
    compareTo(aThat) < DateTime.EQUAL || (this == aThat)
  }

  /**
  'Greater than' comparison.
   Return <tt>true</tt> only if this <tt>DateTime</tt> comes after the given parameter, according to {@link #compareTo(DateTime)}.
    */
  def gt(aThat: DateTime): Boolean = {
    compareTo(aThat) > DateTime.EQUAL
  }

  /**
  'Greater than or equal to' comparison.
   Return <tt>true</tt> only if this <tt>DateTime</tt> comes after the given parameter, according to {@link #compareTo(DateTime)},
   or this <tt>DateTime</tt> equals the given parameter.
    */
  def gteq(aThat: DateTime): Boolean = {
    compareTo(aThat) > DateTime.EQUAL || (this == aThat)
  }

  /** Return the smallest non-null time unit encapsulated by this <tt>DateTime</tt>. */
  def getPrecision: DateTime.Unit = {
    ensureParsed
    var result: DateTime.Unit = null
    if (isPresent(nanosecond)) {
      result = DateTime.Unit.NANOSECONDS
    }
    else if (isPresent(second)) {
      result = DateTime.Unit.SECOND
    }
    else if (isPresent(minute)) {
      result = DateTime.Unit.MINUTE
    }
    else if (isPresent(hour)) {
      result = DateTime.Unit.HOUR
    }
    else if (isPresent(day)) {
      result = DateTime.Unit.DAY
    }
    else if (isPresent(month)) {
      result = DateTime.Unit.MONTH
    }
    else if (isPresent(year)) {
      result = DateTime.Unit.YEAR
    }
    result
  }

  /**
  Truncate this <tt>DateTime</tt> to the given precision.
   <P>The value will have all items lower than the given precision simply set to
   <tt>null</tt>. In addition, the value will not include any date-time String passed to the
   {@link #DateTime(String)} constructor.

   @param aPrecision takes any value <i>except</i> { @link Unit#NANOSECONDS} (since it makes no sense to truncate to the highest
   available precision).
    */
  def truncate(aPrecision: DateTime.Unit): DateTime = {
    ensureParsed
    var result: DateTime = null
    if (DateTime.Unit.NANOSECONDS eq aPrecision) {
      throw new IllegalArgumentException("It makes no sense to truncate to nanosecond precision, since that's the highest precision available.")
    }
    else if (DateTime.Unit.SECOND eq aPrecision) {
      result = new DateTime(year, month, day, hour, minute, second, null)
    }
    else if (DateTime.Unit.MINUTE eq aPrecision) {
      result = new DateTime(year, month, day, hour, minute, null, null)
    }
    else if (DateTime.Unit.HOUR eq aPrecision) {
      result = new DateTime(year, month, day, hour, null, null, null)
    }
    else if (DateTime.Unit.DAY eq aPrecision) {
      result = new DateTime(year, month, day, null, null, null, null)
    }
    else if (DateTime.Unit.MONTH eq aPrecision) {
      result = new DateTime(year, month, null, null, null, null, null)
    }
    else if (DateTime.Unit.YEAR eq aPrecision) {
      result = new DateTime(year, null, null, null, null, null, null)
    }
    result
  }

  /**
  Return <tt>true</tt> only if all of the given units are present in this <tt>DateTime</tt>.
   If a unit is <i>not</i> included in the argument list, then no test is made for its presence or absence
   in this <tt>DateTime</tt> by this method.
    */
  def unitsAllPresent(aUnits: DateTime.Unit*): Boolean = {
    var result: Boolean = true
    ensureParsed
    for (unit <- aUnits) {
      if (DateTime.Unit.NANOSECONDS eq unit) {
        result = result && nanosecond != null
      }
      else if (DateTime.Unit.SECOND eq unit) {
        result = result && second != null
      }
      else if (DateTime.Unit.MINUTE eq unit) {
        result = result && minute != null
      }
      else if (DateTime.Unit.HOUR eq unit) {
        result = result && hour != null
      }
      else if (DateTime.Unit.DAY eq unit) {
        result = result && day != null
      }
      else if (DateTime.Unit.MONTH eq unit) {
        result = result && month != null
      }
      else if (DateTime.Unit.YEAR eq unit) {
        result = result && year != null
      }
    }
    result
  }

  /**
  Return <tt>true</tt> only if this <tt>DateTime</tt> has a non-null values for year, month, and day.
    */
  def hasYearMonthDay: Boolean = {
    unitsAllPresent(DateTime.Unit.YEAR, DateTime.Unit.MONTH, DateTime.Unit.DAY)
  }

  /**
  Return <tt>true</tt> only if this <tt>DateTime</tt> has a non-null values for hour, minute, and second.
    */
  def hasHourMinuteSecond: Boolean = {
    unitsAllPresent(DateTime.Unit.HOUR, DateTime.Unit.MINUTE, DateTime.Unit.SECOND)
  }

  /**
  Return <tt>true</tt> only if all of the given units are absent from this <tt>DateTime</tt>.
   If a unit is <i>not</i> included in the argument list, then no test is made for its presence or absence
   in this <tt>DateTime</tt> by this method.
    */
  def unitsAllAbsent(aUnits: DateTime.Unit*): Boolean = {
    var result: Boolean = true
    ensureParsed
    for (unit <- aUnits) {
      if (DateTime.Unit.NANOSECONDS eq unit) {
        result = result && nanosecond == null
      }
      else if (DateTime.Unit.SECOND eq unit) {
        result = result && second == null
      }
      else if (DateTime.Unit.MINUTE eq unit) {
        result = result && minute == null
      }
      else if (DateTime.Unit.HOUR eq unit) {
        result = result && hour == null
      }
      else if (DateTime.Unit.DAY eq unit) {
        result = result && day == null
      }
      else if (DateTime.Unit.MONTH eq unit) {
        result = result && month == null
      }
      else if (DateTime.Unit.YEAR eq unit) {
        result = result && year == null
      }
    }
    result
  }

  /**
  Return this <tt>DateTime</tt> with the time portion coerced to '00:00:00.000000000'.
   <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
    */
  def getStartOfDay: DateTime = {
    ensureHasYearMonthDay
    getStartEndDateTime(day, 0, 0, 0, 0)
  }

  /**
  Return this <tt>DateTime</tt> with the time portion coerced to '23:59:59.999999999'.
   <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
    */
  def getEndOfDay: DateTime = {
    ensureHasYearMonthDay
    getStartEndDateTime(day, 23, 59, 59, 999999999)
  }

  /**
  Return this <tt>DateTime</tt> with the time portion coerced to '00:00:00.000000000',
   and the day coerced to 1.
   <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
    */
  def getStartOfMonth: DateTime = {
    ensureHasYearMonthDay
    getStartEndDateTime(1, 0, 0, 0, 0)
  }

  /**
  Return this <tt>DateTime</tt> with the time portion coerced to '23:59:59.999999999',
   and the day coerced to the end of the month.
   <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
    */
  def getEndOfMonth: DateTime = {
    ensureHasYearMonthDay
    getStartEndDateTime(getNumDaysInMonth, 23, 59, 59, 999999999)
  }

  /**
  Create a new <tt>DateTime</tt> by adding an interval to this one.

   <P>See {@link #plusDays(Integer)} as well.

   <P>Changes are always applied by this class <i>in order of decreasing units of time</i>:
   years first, then months, and so on. After changing both the year and month, a check on the month-day combination is made before
   any change is made to the day. If the day exceeds the number of days in the given month/year, then
   (and only then) the given {@link DayOverflow} policy applied, and the day-of-the-month is adusted accordingly.

   <P>Afterwards, the day is then changed in the usual way, followed by the remaining items (hour, minute, second, and nanosecond).

   <P><em>The mental model for this method is very similar to that of a car's odometer.</em> When a limit is reach for one unit of time,
   then a rollover occurs for a neighbouring unit of time.

   <P>The returned value cannot come after <tt>9999-12-13 23:59:59</tt>.

   <P>This class works with <tt>DateTime</tt>'s having the following items present :
   <ul>
   <li>year-month-day and hour-minute-second (and optional nanoseconds)
   <li>year-month-day only. In this case, if a calculation with a time part is performed, that time part
   will be initialized by this class to 00:00:00.0, and the <tt>DateTime</tt> returned by this class will include a time part.
   <li>hour-minute-second (and optional nanoseconds) only. In this case, the calculation is done starting with the
   the arbitrary date <tt>0001-01-01</tt> (in order to remain within a valid state space of <tt>DateTime</tt>).
   </ul>

   @param aNumYears positive, required, in range 0...9999
  @param aNumMonths positive, required, in range 0...9999
  @param aNumDays positive, required, in range 0...9999
  @param aNumHours positive, required, in range 0...9999
  @param aNumMinutes positive, required, in range 0...9999
  @param aNumSeconds positive, required, in range 0...9999
  @param aNumNanoseconds positive, required, in range 0...999999999
    */
  def plus(aNumYears: Integer, aNumMonths: Integer, aNumDays: Integer, aNumHours: Integer, aNumMinutes: Integer, aNumSeconds: Integer, aNumNanoseconds: Integer, aDayOverflow: DateTime.DayOverflow): DateTime = {
    val interval: DateTimeInterval = new DateTimeInterval(this, aDayOverflow)
    interval.plus(aNumYears, aNumMonths, aNumDays, aNumHours, aNumMinutes, aNumSeconds, aNumNanoseconds)
  }

  /**
  Create a new <tt>DateTime</tt> by subtracting an interval to this one.

   <P>See {@link #minusDays(Integer)} as well.
   <P>This method has nearly the same behavior as {@link #plus(Integer, Integer, Integer, Integer, Integer, Integer, Integer, DayOverflow)},
   except that the value cannot come before <tt>0001-01-01 00:00:00</tt>.
    */
  def minus(aNumYears: Integer, aNumMonths: Integer, aNumDays: Integer, aNumHours: Integer, aNumMinutes: Integer, aNumSeconds: Integer, aNumNanoseconds: Integer, aDayOverflow: DateTime.DayOverflow): DateTime = {
    val interval: DateTimeInterval = new DateTimeInterval(this, aDayOverflow)
    interval.minus(aNumYears, aNumMonths, aNumDays, aNumHours, aNumMinutes, aNumSeconds, aNumNanoseconds)
  }

  /**
  Return a new <tt>DateTime</tt> by adding an integral number of days to this one.

   <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
   @param aNumDays can be either sign; if negative, then the days are subtracted.
    */
  def plusDays(aNumDays: Integer): DateTime = {
    ensureHasYearMonthDay
    val thisJDAtNoon: Int = getModifiedJulianDayNumber + 1 + DateTime.EPOCH_MODIFIED_JD
    val resultJD: Int = thisJDAtNoon + aNumDays
    val datePortion: DateTime = DateTime.fromJulianDayNumberAtNoon(resultJD)
    new DateTime(datePortion.getYear, datePortion.getMonth, datePortion.getDay, hour, minute, second, nanosecond)
  }

  /**
  Return a new <tt>DateTime</tt> by subtracting an integral number of days from this one.

   <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
   @param aNumDays can be either sign; if negative, then the days are added.
    */
  def minusDays(aNumDays: Integer): DateTime = {
    plusDays(-1 * aNumDays)
  }

  /**
  The whole number of days between this <tt>DateTime</tt> and the given parameter.
   <P>Requires year-month-day to be present, both for this <tt>DateTime</tt> and for the <tt>aThat</tt>
   parameter; if not, a runtime exception is thrown.
    */
  def numDaysFrom(aThat: DateTime): Int = {
    aThat.getModifiedJulianDayNumber - this.getModifiedJulianDayNumber
  }

  /**
  The number of seconds between this <tt>DateTime</tt> and the given argument.
    <P>If any date information is present, in either this <tt>DateTime</tt> or <tt>aThat</tt>,
    then full year-month-day must be present in <em>both</em>; if not, then the date portion will be ignored, and only the
    time portion will contribute to the calculation.
    */
  def numSecondsFrom(aThat: DateTime): Long = {
    var result: Long = 0
    aThat.ensureParsed
    if (hasYearMonthDay && aThat.hasYearMonthDay) {
      result = numDaysFrom(aThat) * 86400
    }
    result = result - this.numSecondsInTimePortion + aThat.numSecondsInTimePortion
    result
  }

  /**
  Output this <tt>DateTime</tt> as a formatted String using numbers, with no localizable text.

   <P>Example:
   <PRE>dt.format("YYYY-MM-DD hh:mm:ss");</PRE>
   would generate text of the form
   <PRE>2009-09-09 18:23:59</PRE>

   <P>If months, weekdays, or AM/PM indicators are output as localizable text, you must use {@link #format(String, Locale)}.
   @param aFormat uses the <a href="#FormattingLanguage">formatting mini-language</a> defined in the class comment.
    */
  def format(aFormat: String): String = {
    val format: DateTimeFormatter = new DateTimeFormatter(aFormat)
    format.format(this)
  }

  /**
  Output this <tt>DateTime</tt> as a formatted String using numbers and/or localizable text.

   <P>This method is intended for alphanumeric output, such as '<tt>Sunday, November 14, 1858 10:00 AM</tt>'.
   <P>If months and weekdays are output as numbers, you are encouraged to use {@link #format(String)} instead.

   @param aFormat uses the <a href="#FormattingLanguage">formatting mini-language</a> defined in the class comment.
  @param aLocale used to generate text for Month, Weekday and AM/PM indicator; required only by patterns which localized
   text, instead of numeric forms.
    */
  def format(aFormat: String, aLocale: Locale): String = {
    val format: DateTimeFormatter = new DateTimeFormatter(aFormat, aLocale)
    format.format(this)
  }

  /**
  Output this <tt>DateTime</tt> as a formatted String using numbers and explicit text for months, weekdays, and AM/PM indicator.

   <P>Use of this method is likely relatively rare; it should be used only if the output of {@link #format(String, Locale)}  is
   inadequate.

   @param aFormat uses the <a href="#FormattingLanguage">formatting mini-language</a> defined in the class comment.
  @param aMonths contains text for all 12 months, starting with January; size must be 12.
  @param aWeekdays contains text for all 7 weekdays, starting with Sunday; size must be 7.
  @param aAmPmIndicators contains text for A.M and P.M. indicators (in that order); size must be 2.
    */
  def format(aFormat: String, aMonths: List[String], aWeekdays: List[String], aAmPmIndicators: List[String]): String = {
    val format: DateTimeFormatter = new DateTimeFormatter(aFormat, aMonths, aWeekdays, aAmPmIndicators)
    format.format(this)
  }

  /** Return <tt>true</tt> only if this date is in the future, with respect to {@link #now(TimeZone)}. */
  def isInTheFuture(aTimeZone: TimeZone): Boolean = {
    DateTime.now(aTimeZone).lt(this)
  }

  /** Return <tt>true</tt> only if this date is in the past, with respect to {@link #now(TimeZone)}. */
  def isInThePast(aTimeZone: TimeZone): Boolean = {
    DateTime.now(aTimeZone).gt(this)
  }

  /**
  Return a <tt>DateTime</tt> corresponding to a change from one {@link TimeZone} to another.

    <P>A <tt>DateTime</tt> object has an implicit and immutable time zone.
    If you need to change the implicit time zone, you can use this method to do so.

    <P>Example :
    <PRE>
TimeZone fromUK = TimeZone.getTimeZone("Europe/London");
TimeZone toIndonesia = TimeZone.getTimeZone("Asia/Jakarta");
DateTime newDt = oldDt.changeTimeZone(fromUK, toIndonesia);
    </PRE>

   <P>Requires year-month-day-hour to be present; if not, a runtime exception is thrown.
   @param aFromTimeZone the implicit time zone of this object.
  @param aToTimeZone the implicit time zone of the <tt>DateTime</tt> returned by this method.
  @aDateTime corresponding to the change of time zone implied by the 2 parameters.
    */
  def changeTimeZone(aFromTimeZone: TimeZone, aToTimeZone: TimeZone): DateTime = {
    var result: DateTime = null
    ensureHasYearMonthDay
    if (unitsAllAbsent(DateTime.Unit.HOUR)) {
      throw new IllegalArgumentException("DateTime does not include the hour. Cannot change the time zone if no hour is present.")
    }
    val fromDate: Calendar = new GregorianCalendar(aFromTimeZone)
    fromDate.set(Calendar.YEAR, getYear)
    fromDate.set(Calendar.MONTH, getMonth - 1)
    fromDate.set(Calendar.DAY_OF_MONTH, getDay)
    fromDate.set(Calendar.HOUR_OF_DAY, getHour)
    if (getMinute != null) {
      fromDate.set(Calendar.MINUTE, getMinute)
    }
    else {
      fromDate.set(Calendar.MINUTE, 0)
    }
    fromDate.set(Calendar.SECOND, 0)
    fromDate.set(Calendar.MILLISECOND, 0)
    val toDate: Calendar = new GregorianCalendar(aToTimeZone)
    toDate.setTimeInMillis(fromDate.getTimeInMillis)
    val minute: Integer = if (getMinute != null) toDate.get(Calendar.MINUTE) else null
    result = new DateTime(toDate.get(Calendar.YEAR), toDate.get(Calendar.MONTH) + 1, toDate.get(Calendar.DAY_OF_MONTH), toDate.get(Calendar.HOUR_OF_DAY), minute, getSecond, getNanoseconds)
    result
  }

  /**
  Compare this object to another, for ordering purposes.
   <P> Uses the 7 date-time elements (year..nanosecond). The Year is considered the most
   significant item, and the Nanosecond the least significant item. Null items are placed first in this comparison.
    */
  def compareTo(aThat: DateTime): Int = {
    if (this eq aThat) DateTime.EQUAL
    ensureParsed
    aThat.ensureParsed
    val nullsGo: ModelUtil.NullsGo = ModelUtil.NullsGo.FIRST
    var comparison: Int = ModelUtil.comparePossiblyNull(this.year, aThat.year, nullsGo)
    if (comparison != DateTime.EQUAL) comparison
    comparison = ModelUtil.comparePossiblyNull(this.month, aThat.month, nullsGo)
    if (comparison != DateTime.EQUAL) comparison
    comparison = ModelUtil.comparePossiblyNull(this.day, aThat.day, nullsGo)
    if (comparison != DateTime.EQUAL) comparison
    comparison = ModelUtil.comparePossiblyNull(this.hour, aThat.hour, nullsGo)
    if (comparison != DateTime.EQUAL) comparison
    comparison = ModelUtil.comparePossiblyNull(this.minute, aThat.minute, nullsGo)
    if (comparison != DateTime.EQUAL) comparison
    comparison = ModelUtil.comparePossiblyNull(this.second, aThat.second, nullsGo)
    if (comparison != DateTime.EQUAL) comparison
    comparison = ModelUtil.comparePossiblyNull(this.nanosecond, aThat.nanosecond, nullsGo)
    if (comparison != DateTime.EQUAL) comparison
    DateTime.EQUAL
  }

  /**
  Equals method for this object.

   <P>Equality is determined by the 7 date-time elements (year..nanosecond).
    */
  override def equals(aThat: AnyRef): Boolean = {
    ensureParsed
    var result: Boolean = ModelUtil.quickEquals(this, aThat)
    if (result == null) {
      val that: DateTime = aThat.asInstanceOf[DateTime]
      that.ensureParsed
      result = ModelUtil.equalsFor(this.getSignificantFields, that.getSignificantFields)
    }
    result
  }

  /**
  Hash code for this object.

   <P> Uses the same 7 date-time elements (year..nanosecond) as used by
   {@link #equals(Object)}.
    */
  override def hashCode: Int = {
    if (fHashCode == 0) {
      ensureParsed
      fHashCode = ModelUtil.hashCodeFor(getSignificantFields)
    }
    fHashCode
  }

  /**
  Intended for <i>debugging and logging</i> only.

   <P><b>To format this <tt>DateTime</tt> for presentation to the user, see the various <tt>format</tt> methods.</b>

   <P>If the {@link #DateTime(String)} constructor was called, then that String.

   <P>Otherwise, the value is constructed from each date-time element, in a fixed format, depending
   on which time units are present. Example values :
   <ul>
    <li>2011-04-30 13:59:59.123456789
    <li>2011-04-30 13:59:59
    <li>2011-04-30
    <li>2011-04-30 13:59
    <li>13:59:59.123456789
    <li>13:59:59
    <li>and so on...
   </ul>

   <P>In the great majority of cases, this will give reasonable output for debugging and logging statements.

   <P>In cases where a bizarre combinations of time units is present, the value is presented in a verbose form.
   For example, if all time units are present <i>except</i> for minutes, the value has this form:
   <PRE>Y:2001 M:1 D:31 h:13 m:null s:59 f:123456789</PRE>
    */
  override def toString: String = {
    var result: String = ""
    if (Util.textHasContent(fDateTime)) {
      result = fDateTime
    }
    else {
      val format: String = calcToStringFormat
      if (format != null) {
        result = format(calcToStringFormat)
      }
      else {
        val builder: StringBuilder = new StringBuilder
        addToString("Y", year, builder)
        addToString("M", month, builder)
        addToString("D", day, builder)
        addToString("h", hour, builder)
        addToString("m", minute, builder)
        addToString("s", second, builder)
        addToString("f", nanosecond, builder)
        result = builder.toString.trim
      }
    }
    result
  }

  /** Intended as internal tool, for testing only. Note scope is not public! */
  def ensureParsed {
    if (!fIsAlreadyParsed) {
      parseDateTimeText
    }
  }


  /**
  Return a the whole number, with no fraction.
   The JD at noon is 1 more than the JD at midnight.
    */
  private def calculateJulianDayNumberAtNoon: Int = {
    val y: Int = year
    val m: Int = month
    val d: Int = day
    val result: Int = (1461 * (y + 4800 + (m - 14) / 12)) / 4 + (367 * (m - 2 - 12 * ((m - 14) / 12))) / 12 - (3 * ((y + 4900 + (m - 14) / 12) / 100)) / 4 + d - 32075
    result
  }

  private def ensureHasYearMonthDay {
    ensureParsed
    if (!hasYearMonthDay) {
      throw new DateTime.MissingItem("DateTime does not include year/month/day.")
    }
  }

  /** Return the number of seconds in any existing time portion of the date. */
  private def numSecondsInTimePortion: Int = {
    var result: Int = 0
    if (second != null) {
      result = result + second
    }
    if (minute != null) {
      result = result + 60 * minute
    }
    if (hour != null) {
      result = result + 3600 * hour
    }
    result
  }

  private def validateState {

    checkNumDaysInMonth(year, month, day)
  }

  private def checkRange(aValue: Integer, aMin: Int, aMax: Int, aName: String): Unit = {
    if (aValue != null) {
      if (aValue < aMin || aValue > aMax) {
        throw new DateTime.ItemOutOfRange(aName + " is not in the range " + aMin + "main" + aMax + ". Value is:" + aValue)
      }
    }
  }

  private def checkNumDaysInMonth(aYear: Integer, aMonth: Integer, aDay: Integer): Unit = {
    if (hasYearMonthDay(aYear, aMonth, aDay) && aDay > DateTime.getNumDaysInMonth(aYear, aMonth)) {
      throw new DateTime.ItemOutOfRange("The day-of-the-month value '" + aDay + "' exceeds the number of days in the month: " + DateTime.getNumDaysInMonth(aYear, aMonth))
    }
  }

  private def parseDateTimeText {
    val parser: DateTimeParser = new DateTimeParser
    val dateTime: DateTime = parser.parse(fDateTime)
    year = dateTime.year
    month = dateTime.month
    day = dateTime.day
    hour = dateTime.hour
    minute = dateTime.minute
    second = dateTime.second
    nanosecond = dateTime.nanosecond
    validateState
  }

  private def hasYearMonthDay(aYear: Integer, aMonth: Integer, aDay: Integer): Boolean = {
    isPresent(aYear, aMonth, aDay)
  }

  private def getSignificantFields: Array[AnyRef] = {
    Array[AnyRef](year, month, day, hour, minute, second, nanosecond)
  }

  private def addToString(aName: String, aValue: AnyRef, aBuilder: StringBuilder): Unit = {
    aBuilder.append(aName + ":" + String.valueOf(aValue) + " ")
  }

  /** Return true only if all the given arguments are non-null. */
  private def isPresent(aItems: AnyRef*): Boolean = {
    var result: Boolean = true
    for (item <- aItems) {
      if (item == null) {
        result = false
        break //todo: break is not supported
      }
    }
    result
  }

  private def getStartEndDateTime(aDay: Integer, aHour: Integer, aMinute: Integer, aSecond: Integer, aNanosecond: Integer): DateTime = {
    ensureHasYearMonthDay
    new DateTime(year, month, aDay, aHour, aMinute, aSecond, aNanosecond)
  }

  private def calcToStringFormat: String = {
    var result: String = null
    if (unitsAllPresent(DateTime.Unit.YEAR) && unitsAllAbsent(DateTime.Unit.MONTH, DateTime.Unit.DAY, DateTime.Unit.HOUR, DateTime.Unit.MINUTE, DateTime.Unit.SECOND, DateTime.Unit.NANOSECONDS)) {
      result = "YYYY"
    }
    else if (unitsAllPresent(DateTime.Unit.YEAR, DateTime.Unit.MONTH) && unitsAllAbsent(DateTime.Unit.DAY, DateTime.Unit.HOUR, DateTime.Unit.MINUTE, DateTime.Unit.SECOND, DateTime.Unit.NANOSECONDS)) {
      result = "YYYY-MM"
    }
    else if (unitsAllPresent(DateTime.Unit.YEAR, DateTime.Unit.MONTH, DateTime.Unit.DAY) && unitsAllAbsent(DateTime.Unit.HOUR, DateTime.Unit.MINUTE, DateTime.Unit.SECOND, DateTime.Unit.NANOSECONDS)) {
      result = "YYYY-MM-DD"
    }
    else if (unitsAllPresent(DateTime.Unit.YEAR, DateTime.Unit.MONTH, DateTime.Unit.DAY, DateTime.Unit.HOUR) && unitsAllAbsent(DateTime.Unit.MINUTE, DateTime.Unit.SECOND, DateTime.Unit.NANOSECONDS)) {
      result = "YYYY-MM-DD hh"
    }
    else if (unitsAllPresent(DateTime.Unit.YEAR, DateTime.Unit.MONTH, DateTime.Unit.DAY, DateTime.Unit.HOUR, DateTime.Unit.MINUTE) && unitsAllAbsent(DateTime.Unit.SECOND, DateTime.Unit.NANOSECONDS)) {
      result = "YYYY-MM-DD hh:mm"
    }
    else if (unitsAllPresent(DateTime.Unit.YEAR, DateTime.Unit.MONTH, DateTime.Unit.DAY, DateTime.Unit.HOUR, DateTime.Unit.MINUTE, DateTime.Unit.SECOND) && unitsAllAbsent(DateTime.Unit.NANOSECONDS)) {
      result = "YYYY-MM-DD hh:mm:ss"
    }
    else if (unitsAllPresent(DateTime.Unit.YEAR, DateTime.Unit.MONTH, DateTime.Unit.DAY, DateTime.Unit.HOUR, DateTime.Unit.MINUTE, DateTime.Unit.SECOND, DateTime.Unit.NANOSECONDS)) {
      result = "YYYY-MM-DD hh:mm:ss.fffffffff"
    }
    else if (unitsAllAbsent(DateTime.Unit.YEAR, DateTime.Unit.MONTH, DateTime.Unit.DAY) && unitsAllPresent(DateTime.Unit.HOUR, DateTime.Unit.MINUTE, DateTime.Unit.SECOND, DateTime.Unit.NANOSECONDS)) {
      result = "hh:mm:ss.fffffffff"
    }
    else if (unitsAllAbsent(DateTime.Unit.YEAR, DateTime.Unit.MONTH, DateTime.Unit.DAY, DateTime.Unit.NANOSECONDS) && unitsAllPresent(DateTime.Unit.HOUR, DateTime.Unit.MINUTE, DateTime.Unit.SECOND)) {
      result = "hh:mm:ss"
    }
    else if (unitsAllAbsent(DateTime.Unit.YEAR, DateTime.Unit.MONTH, DateTime.Unit.DAY, DateTime.Unit.SECOND, DateTime.Unit.NANOSECONDS) && unitsAllPresent(DateTime.Unit.HOUR, DateTime.Unit.MINUTE)) {
      result = "hh:mm"
    }
    result
  }

  /**
  Always treat de-serialization as a full-blown constructor, by
    validating the final state of the de-serialized object.
    */
  @throws(classOf[ClassNotFoundException])
  @throws(classOf[IOException])
  private def readObject(aInputStream: ObjectInputStream): Unit = {
    aInputStream.defaultReadObject
    validateState
  }

  /**
  This is the default implementation of writeObject.
    Customise if necessary.
    */
  @throws(classOf[IOException])
  private def writeObject(aOutputStream: ObjectOutputStream): Unit = {
    aOutputStream.defaultWriteObject
  }
}