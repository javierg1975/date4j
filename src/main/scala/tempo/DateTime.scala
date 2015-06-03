package tempo

/**
 * Created by javier on 6/2/15.
 */

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone


/**
Constructor taking each time unit explicitly.

   <P>Although all parameters are optional, many operations on this class require year-month-day to be
   present.

  @param year 1..9999, optional
  @param month 1..12 , optional
  @param day 1..31, cannot exceed the number of days in the given month/year, optional
  @param hour 0..23, optional
  @param minute 0..59, optional
  @param second 0..59, optional
  @param nanosecond 0..999,999,999, optional (allows for databases that store timestamps up to nanosecond precision).
  */

case class Date(year: Int, month: Int, day: Int) extends Ordered[Date]{
  require(1 <= year && year >= 9999)
  require(1 <= month && month >= 12)
  require(1 <= day && day >= 31)

  override def compare(that: Date): Int = ???
}

case class Time(hour: Int, minute: Int, second: Int = 0, nanosecond: Int = 0) extends Ordered[Time]{
  require(0 <= hour && hour >= 23)
  require(0 <= minute && minute >= 59)
  require(0 <= second && second >= 59)
  require(0 <= nanosecond && nanosecond >= 999999999)

  override def compare(that: Time): Int = ???
}

case class DateTime(date: Option[Date] = Option, time: Option[Time] = None) extends Ordered[DateTime] {

  List(date, time)

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
  def getModifiedJulianDayNumber: Int = {
    ensureHasYearMonthDay
    val result: Int = calculateJulianDayNumberAtNoon - 1 - DateTime.EPOCH_MODIFIED_JD
    result
  }

  /**
  Return an index for the weekday for this <tt>DateTime</tt>.
   Returns 1..7 for Sunday..Saturday.
   <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
    */
  def getWeekDay: Int = {
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
  def getDayOfYear: Int = {
    ensureHasYearMonthDay
    val k: Int = if (isLeapYear) 1 else 2
    val result: Int = ((275 * month) / 9) - k * ((month + 9) / 12) + day - 30
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
  def getWeekIndex(aStartingFromDate: DateTime): Int = {
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
  def getWeekIndex: Int = {
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
  def getPrecision: DateTime.DateTimeUnit = {
    ensureParsed
    var result: DateTime.DateTimeUnit = null
    if (isPresent(nanosecond)) {
      result = DateTime.DateTimeUnit.NANOSECONDS
    }
    else if (isPresent(second)) {
      result = DateTime.DateTimeUnit.SECOND
    }
    else if (isPresent(minute)) {
      result = DateTime.DateTimeUnit.MINUTE
    }
    else if (isPresent(hour)) {
      result = DateTime.DateTimeUnit.HOUR
    }
    else if (isPresent(day)) {
      result = DateTime.DateTimeUnit.DAY
    }
    else if (isPresent(month)) {
      result = DateTime.DateTimeUnit.MONTH
    }
    else if (isPresent(year)) {
      result = DateTime.DateTimeUnit.YEAR
    }
    result
  }

  /**
  Truncate this <tt>DateTime</tt> to the given precision.
   <P>The value will have all items lower than the given precision simply set to
   <tt>null</tt>. In addition, the value will not include any date-time String passed to the
   {@link #DateTime(String)} constructor.

   @param aPrecision takes any value <i>except</i> { @link DateTimeUnit#NANOSECONDS} (since it makes no sense to truncate to the highest
   available precision).
    */
  def truncate(aPrecision: DateTime.DateTimeUnit): DateTime = {
    ensureParsed
    var result: DateTime = null
    if (DateTime.DateTimeUnit.NANOSECONDS eq aPrecision) {
      throw new IllegalArgumentException("It makes no sense to truncate to nanosecond precision, since that's the highest precision available.")
    }
    else if (DateTime.DateTimeUnit.SECOND eq aPrecision) {
      result = new DateTime(year, month, day, hour, minute, second, null)
    }
    else if (DateTime.DateTimeUnit.MINUTE eq aPrecision) {
      result = new DateTime(year, month, day, hour, minute, null, null)
    }
    else if (DateTime.DateTimeUnit.HOUR eq aPrecision) {
      result = new DateTime(year, month, day, hour, null, null, null)
    }
    else if (DateTime.DateTimeUnit.DAY eq aPrecision) {
      result = new DateTime(year, month, day, null, null, null, null)
    }
    else if (DateTime.DateTimeUnit.MONTH eq aPrecision) {
      result = new DateTime(year, month, null, null, null, null, null)
    }
    else if (DateTime.DateTimeUnit.YEAR eq aPrecision) {
      result = new DateTime(year, null, null, null, null, null, null)
    }
    result
  }

  /**
  Return <tt>true</tt> only if all of the given units are present in this <tt>DateTime</tt>.
   If a unit is <i>not</i> included in the argument list, then no test is made for its presence or absence
   in this <tt>DateTime</tt> by this method.
    */
  def unitsAllPresent(aUnits: DateTime.DateTimeUnit*): Boolean = {
    var result: Boolean = true
    ensureParsed
    for (unit <- aUnits) {
      if (DateTime.DateTimeUnit.NANOSECONDS eq unit) {
        result = result && nanosecond != null
      }
      else if (DateTime.DateTimeUnit.SECOND eq unit) {
        result = result && second != null
      }
      else if (DateTime.DateTimeUnit.MINUTE eq unit) {
        result = result && minute != null
      }
      else if (DateTime.DateTimeUnit.HOUR eq unit) {
        result = result && hour != null
      }
      else if (DateTime.DateTimeUnit.DAY eq unit) {
        result = result && day != null
      }
      else if (DateTime.DateTimeUnit.MONTH eq unit) {
        result = result && month != null
      }
      else if (DateTime.DateTimeUnit.YEAR eq unit) {
        result = result && year != null
      }
    }
    result
  }

  /**
  Return <tt>true</tt> only if this <tt>DateTime</tt> has a non-null values for year, month, and day.
    */
  def hasYearMonthDay: Boolean = {
    unitsAllPresent(DateTime.DateTimeUnit.YEAR, DateTime.DateTimeUnit.MONTH, DateTime.DateTimeUnit.DAY)
  }

  /**
  Return <tt>true</tt> only if this <tt>DateTime</tt> has a non-null values for hour, minute, and second.
    */
  def hasHourMinuteSecond: Boolean = {
    unitsAllPresent(DateTime.DateTimeUnit.HOUR, DateTime.DateTimeUnit.MINUTE, DateTime.DateTimeUnit.SECOND)
  }

  /**
  Return <tt>true</tt> only if all of the given units are absent from this <tt>DateTime</tt>.
   If a unit is <i>not</i> included in the argument list, then no test is made for its presence or absence
   in this <tt>DateTime</tt> by this method.
    */
  def unitsAllAbsent(aUnits: DateTime.DateTimeUnit*): Boolean = {
    var result: Boolean = true
    ensureParsed
    for (unit <- aUnits) {
      if (DateTime.DateTimeUnit.NANOSECONDS eq unit) {
        result = result && nanosecond == null
      }
      else if (DateTime.DateTimeUnit.SECOND eq unit) {
        result = result && second == null
      }
      else if (DateTime.DateTimeUnit.MINUTE eq unit) {
        result = result && minute == null
      }
      else if (DateTime.DateTimeUnit.HOUR eq unit) {
        result = result && hour == null
      }
      else if (DateTime.DateTimeUnit.DAY eq unit) {
        result = result && day == null
      }
      else if (DateTime.DateTimeUnit.MONTH eq unit) {
        result = result && month == null
      }
      else if (DateTime.DateTimeUnit.YEAR eq unit) {
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

   <P>See {@link #plusDays(Int)} as well.

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
  def plus(aNumYears: Int, aNumMonths: Int, aNumDays: Int, aNumHours: Int, aNumMinutes: Int, aNumSeconds: Int, aNumNanoseconds: Int, aDayOverflow: DateTime.DayOverflow): DateTime = {
    val interval: DateTimeInterval = new DateTimeInterval(this, aDayOverflow)
    interval.plus(aNumYears, aNumMonths, aNumDays, aNumHours, aNumMinutes, aNumSeconds, aNumNanoseconds)
  }

  /**
  Create a new <tt>DateTime</tt> by subtracting an interval to this one.

   <P>See {@link #minusDays(Int)} as well.
   <P>This method has nearly the same behavior as {@link #plus(Int, Int, Int, Int, Int, Int, Int, DayOverflow)},
   except that the value cannot come before <tt>0001-01-01 00:00:00</tt>.
    */
  def minus(aNumYears: Int, aNumMonths: Int, aNumDays: Int, aNumHours: Int, aNumMinutes: Int, aNumSeconds: Int, aNumNanoseconds: Int, aDayOverflow: DateTime.DayOverflow): DateTime = {
    val interval: DateTimeInterval = new DateTimeInterval(this, aDayOverflow)
    interval.minus(aNumYears, aNumMonths, aNumDays, aNumHours, aNumMinutes, aNumSeconds, aNumNanoseconds)
  }

  /**
  Return a new <tt>DateTime</tt> by adding an integral number of days to this one.

   <P>Requires year-month-day to be present; if not, a runtime exception is thrown.
   @param aNumDays can be either sign; if negative, then the days are subtracted.
    */
  def plusDays(aNumDays: Int): DateTime = {
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
  def minusDays(aNumDays: Int): DateTime = {
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
    if (unitsAllAbsent(DateTime.DateTimeUnit.HOUR)) {
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
    val minute: Int = if (getMinute != null) toDate.get(Calendar.MINUTE) else null
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

  private def checkRange(aValue: Int, aMin: Int, aMax: Int, aName: String): DateTimeUnit = {
    if (aValue != null) {
      if (aValue < aMin || aValue > aMax) {
        throw new DateTime.ItemOutOfRange(aName + " is not in the range " + aMin + "main" + aMax + ". Value is:" + aValue)
      }
    }
  }

  private def checkNumDaysInMonth(aYear: Int, aMonth: Int, aDay: Int): DateTimeUnit = {
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

  private def hasYearMonthDay(aYear: Int, aMonth: Int, aDay: Int): Boolean = {
    isPresent(aYear, aMonth, aDay)
  }

  private def getSignificantFields: Array[AnyRef] = {
    Array[AnyRef](year, month, day, hour, minute, second, nanosecond)
  }

  private def addToString(aName: String, aValue: AnyRef, aBuilder: StringBuilder): DateTimeUnit = {
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

  private def getStartEndDateTime(aDay: Int, aHour: Int, aMinute: Int, aSecond: Int, aNanosecond: Int): DateTime = {
    ensureHasYearMonthDay
    new DateTime(year, month, aDay, aHour, aMinute, aSecond, aNanosecond)
  }

  private def calcToStringFormat: String = {
    var result: String = null
    if (unitsAllPresent(DateTime.DateTimeUnit.YEAR) && unitsAllAbsent(DateTime.DateTimeUnit.MONTH, DateTime.DateTimeUnit.DAY, DateTime.DateTimeUnit.HOUR, DateTime.DateTimeUnit.MINUTE, DateTime.DateTimeUnit.SECOND, DateTime.DateTimeUnit.NANOSECONDS)) {
      result = "YYYY"
    }
    else if (unitsAllPresent(DateTime.DateTimeUnit.YEAR, DateTime.DateTimeUnit.MONTH) && unitsAllAbsent(DateTime.DateTimeUnit.DAY, DateTime.DateTimeUnit.HOUR, DateTime.DateTimeUnit.MINUTE, DateTime.DateTimeUnit.SECOND, DateTime.DateTimeUnit.NANOSECONDS)) {
      result = "YYYY-MM"
    }
    else if (unitsAllPresent(DateTime.DateTimeUnit.YEAR, DateTime.DateTimeUnit.MONTH, DateTime.DateTimeUnit.DAY) && unitsAllAbsent(DateTime.DateTimeUnit.HOUR, DateTime.DateTimeUnit.MINUTE, DateTime.DateTimeUnit.SECOND, DateTime.DateTimeUnit.NANOSECONDS)) {
      result = "YYYY-MM-DD"
    }
    else if (unitsAllPresent(DateTime.DateTimeUnit.YEAR, DateTime.DateTimeUnit.MONTH, DateTime.DateTimeUnit.DAY, DateTime.DateTimeUnit.HOUR) && unitsAllAbsent(DateTime.DateTimeUnit.MINUTE, DateTime.DateTimeUnit.SECOND, DateTime.DateTimeUnit.NANOSECONDS)) {
      result = "YYYY-MM-DD hh"
    }
    else if (unitsAllPresent(DateTime.DateTimeUnit.YEAR, DateTime.DateTimeUnit.MONTH, DateTime.DateTimeUnit.DAY, DateTime.DateTimeUnit.HOUR, DateTime.DateTimeUnit.MINUTE) && unitsAllAbsent(DateTime.DateTimeUnit.SECOND, DateTime.DateTimeUnit.NANOSECONDS)) {
      result = "YYYY-MM-DD hh:mm"
    }
    else if (unitsAllPresent(DateTime.DateTimeUnit.YEAR, DateTime.DateTimeUnit.MONTH, DateTime.DateTimeUnit.DAY, DateTime.DateTimeUnit.HOUR, DateTime.DateTimeUnit.MINUTE, DateTime.DateTimeUnit.SECOND) && unitsAllAbsent(DateTime.DateTimeUnit.NANOSECONDS)) {
      result = "YYYY-MM-DD hh:mm:ss"
    }
    else if (unitsAllPresent(DateTime.DateTimeUnit.YEAR, DateTime.DateTimeUnit.MONTH, DateTime.DateTimeUnit.DAY, DateTime.DateTimeUnit.HOUR, DateTime.DateTimeUnit.MINUTE, DateTime.DateTimeUnit.SECOND, DateTime.DateTimeUnit.NANOSECONDS)) {
      result = "YYYY-MM-DD hh:mm:ss.fffffffff"
    }
    else if (unitsAllAbsent(DateTime.DateTimeUnit.YEAR, DateTime.DateTimeUnit.MONTH, DateTime.DateTimeUnit.DAY) && unitsAllPresent(DateTime.DateTimeUnit.HOUR, DateTime.DateTimeUnit.MINUTE, DateTime.DateTimeUnit.SECOND, DateTime.DateTimeUnit.NANOSECONDS)) {
      result = "hh:mm:ss.fffffffff"
    }
    else if (unitsAllAbsent(DateTime.DateTimeUnit.YEAR, DateTime.DateTimeUnit.MONTH, DateTime.DateTimeUnit.DAY, DateTime.DateTimeUnit.NANOSECONDS) && unitsAllPresent(DateTime.DateTimeUnit.HOUR, DateTime.DateTimeUnit.MINUTE, DateTime.DateTimeUnit.SECOND)) {
      result = "hh:mm:ss"
    }
    else if (unitsAllAbsent(DateTime.DateTimeUnit.YEAR, DateTime.DateTimeUnit.MONTH, DateTime.DateTimeUnit.DAY, DateTime.DateTimeUnit.SECOND, DateTime.DateTimeUnit.NANOSECONDS) && unitsAllPresent(DateTime.DateTimeUnit.HOUR, DateTime.DateTimeUnit.MINUTE)) {
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
  private def readObject(aInputStream: ObjectInputStream): DateTimeUnit = {
    aInputStream.defaultReadObject
    validateState
  }

  /**
  This is the default implementation of writeObject.
    Customise if necessary.
    */
  @throws(classOf[IOException])
  private def writeObject(aOutputStream: ObjectOutputStream): DateTimeUnit = {
    aOutputStream.defaultWriteObject
  }
}

object DateTime {

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
   <P>See {@link #DateTime(Int, Int, Int, Int, Int, Int, Int)} for constraints on the parameters.
    */
  def forDateOnly(aYear: Int, aMonth: Int, aDay: Int): DateTime = {
    new DateTime(Some(aYear), Some(aMonth), Some(aDay))
  }

  /**
  Factory method returns a <tt>DateTime</tt> having hour-minute-second-nanosecond only, with no date portion.
   <P>See {@link #DateTime(Int, Int, Int, Int, Int, Int, Int)} for constraints on the parameters.
    */
  def forTimeOnly(aHour: Int, aMinute: Int, aSecond: Int, aNanoseconds: Int): DateTime = {
    new DateTime(aHour, aMinute, aSecond, aNanoseconds)
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
    result.truncate(DateTimeUnit.DAY)
  }



  /**
  Return the number of days in the given month. The returned value depends on the year as
   well, because of leap years. Returns <tt>null</tt> if either year or month are
   absent. WRONG - should be public??
   Package-private, needed for interval calcs.
    */
  def getNumDaysInMonth(aYear: Int, aMonth: Int): Int = {
    var result: Int = null
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

  private def isLeapYear(aYear: Int): Boolean = {
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
