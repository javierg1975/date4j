package tempo

import java.util.regex.{Matcher, Pattern}

/**
 * Created by javier on 6/2/15.
 */
/**
Convert a date-time from a string into a  {@link DateTime}.
 The primary use case for this class is converting date-times from a database <tt>ResultSet</tt>
 into a {@link DateTime}. It can also convert an ISO time, having a 'T' separating the date
 from the time.
  */
object DateTimeParser {



  /**
  Gross pattern for dates.
   Detailed validation is done by DateTime.
   The Group index VARIES for y-m-d according to which option is selected
   Year: Group 1, 4, 6
   Month: Group 2, 5
   Day: Group 3
    */
  val DATE: Pattern = Pattern.compile("(\\d{1,4})-(\\d\\d)-(\\d\\d)|(\\d{1,4})-(\\d\\d)|(\\d{1,4})")
  /**
  Gross pattern for times.
   Detailed validation is done by DateTime.
   The Group index VARIES for h-m-s-f according to which option is selected
   Hour: Group 1, 5, 8, 10
   Minute: Group 2, 6, 9
   Second: Group 3, 7
   Microsecond:  Group 4
    */
  val CL: String = "\\:"
  val TT: String = "(\\d\\d)"
  val NUM_DIGITS_FOR_FRACTIONAL_SECONDS: String = "9"
  val NUM_DIGITS: Int = Int.valueOf(NUM_DIGITS_FOR_FRACTIONAL_SECONDS)
  val TIME: Pattern = Pattern.compile("" + TT + CL + TT + CL + TT + "\\." + "(\\d{1," + NUM_DIGITS_FOR_FRACTIONAL_SECONDS + "})" + "|" + TT + CL + TT + CL + TT + "|" + TT + CL + TT + "|" + TT)
  val COLON: String = ":"
  val THIRD_POSITION: Int = 2
}

class DateTimeParser {
  def parse(aDateTime: String): DateTime = {
    if (aDateTime == null) {
      throw new NullPointerException("DateTime string is null")
    }
    val dateTime: String = aDateTime.trim
    val parts: DateTimeParser#Parts = splitIntoDateAndTime(dateTime)
    if (parts.hasTwoParts) {
      parseDate(parts.datePart)
      parseTime(parts.timePart)
    }
    else if (parts.hasDateOnly) {
      parseDate(parts.datePart)
    }
    else if (parts.hasTimeOnly) {
      parseTime(parts.timePart)
    }
    val result: DateTime = new DateTime(year, month, day, hour, minute, second, nanosecond)
    result
  }


  class Parts {
    var datePart: String = null
    var timePart: String = null

    def hasTwoParts: Boolean = {
      datePart != null && timePart != null
    }

    def hasDateOnly: Boolean = {
      timePart == null
    }

    def hasTimeOnly: Boolean = {
      datePart == null
    }
  }

  /** Date and time can be separated with a single space, or with a 'T' character (case-sensitive). */
  private def splitIntoDateAndTime(aDateTime: String): DateTimeParser#Parts = {
    val result: DateTimeParser#Parts = new DateTimeParser#Parts
    val dateTimeSeparator: Int = getDateTimeSeparator(aDateTime)
    val hasDateTimeSeparator: Boolean = 0 < dateTimeSeparator && dateTimeSeparator < aDateTime.length
    if (hasDateTimeSeparator) {
      result.datePart = aDateTime.substring(0, dateTimeSeparator)
      result.timePart = aDateTime.substring(dateTimeSeparator + 1)
    }
    else if (hasColonInThirdPlace(aDateTime)) {
      result.timePart = aDateTime
    }
    else {
      result.datePart = aDateTime
    }
    result
  }

  /** Return the index of a space character, or of a 'T' character. If not found, -1. */
  def getDateTimeSeparator(aDateTime: String): Int = {
    val SPACE: String = " "
    val NOT_FOUND: Int = -1
    var result: Int = NOT_FOUND
    result = aDateTime.indexOf(SPACE)
    if (result == NOT_FOUND) {
      result = aDateTime.indexOf("T")
    }
    result
  }

  private def hasColonInThirdPlace(aDateTime: String): Boolean = {
    var result: Boolean = false
    if (aDateTime.length >= DateTimeParser.THIRD_POSITION) {
      result = (DateTimeParser.COLON == aDateTime.substring(DateTimeParser.THIRD_POSITION, DateTimeParser.THIRD_POSITION + 1))
    }
    result
  }

  private def parseDate(aDate: String) {
    val matcher: Matcher = DateTimeParser.DATE.matcher(aDate)
    if (matcher.matches) {
      val year: String = getGroup(matcher, 1, 4, 6)
      if (year != null) {
        year = Int.valueOf(year)
      }
      val month: String = getGroup(matcher, 2, 5)
      if (month != null) {
        month = Int.valueOf(month)
      }
      val day: String = getGroup(matcher, 3)
      if (day != null) {
        day = Int.valueOf(day)
      }
    }
    else {
      throw new DateTimeParser.UnknownDateTimeFormat("Unexpected format for date:" + aDate)
    }
  }

  private def getGroup(aMatcher: Matcher, aGroupIds: Int*): String = {
    var result: String = null
    for (id <- aGroupIds) {
      result = aMatcher.group(id)
      if (result != null) break //todo: break is not supported
    }
    result
  }

  private def parseTime(aTime: String) {
    val matcher: Matcher = DateTimeParser.TIME.matcher(aTime)
    if (matcher.matches) {
      val hour: String = getGroup(matcher, 1, 5, 8, 10)
      if (hour != null) {
        hour = Int.valueOf(hour)
      }
      val minute: String = getGroup(matcher, 2, 6, 9)
      if (minute != null) {
        minute = Int.valueOf(minute)
      }
      val second: String = getGroup(matcher, 3, 7)
      if (second != null) {
        second = Int.valueOf(second)
      }
      val decimalSeconds: String = getGroup(matcher, 4)
      if (decimalSeconds != null) {
        nanosecond = Int.valueOf(convertToNanoseconds(decimalSeconds))
      }
    }
    else {
      throw new DateTimeParser.UnknownDateTimeFormat("Unexpected format for time:" + aTime)
    }
  }

  /**
  Convert any number of decimals (1..9) into the form it would have taken if nanos had been used,
   by adding any 0's to the right side.
    */
  private def convertToNanoseconds(aDecimalSeconds: String): String = {
    val result: StringBuilder = new StringBuilder(aDecimalSeconds)
    while (result.length < DateTimeParser.NUM_DIGITS) {
      result.append("0")
    }
    result.toString
  }
}
