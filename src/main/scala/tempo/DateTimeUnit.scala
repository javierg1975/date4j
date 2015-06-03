package tempo

/**
 * Created by javier on 6/3/15.
 */

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
sealed trait DateTimeUnit
case object Year extends DateTimeUnit
case object Month extends DateTimeUnit
case object Day extends DateTimeUnit
case object Hour extends DateTimeUnit
case object Minute extends DateTimeUnit
case object Second extends DateTimeUnit
case object Nanoseconds extends DateTimeUnit