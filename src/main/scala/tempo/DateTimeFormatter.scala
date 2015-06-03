package tempo

/**
 * Created by javier on 6/2/15.
 */

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Collection
import java.util.GregorianCalendar
import java.util.LinkedHashMap
import java.util.List
import java.util.Locale
import java.util.Map
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
Formats a {@link DateTime}, and implements {@link DateTime#format(String)}.

 <P>This class defines a mini-language for defining how a {@link DateTime} is formatted.
 See {@link DateTime#format(String)} for details regarding the formatting mini-language.

 <P>The DateFormatSymbols class might be used to grab the locale-specific text, but the arrays it
 returns are wonky and weird, so I have avoided it.
  */
object DateTimeFormatter {

  /** A section of fFormat containing a token that must be interpreted. */
  class InterpretedRange {
    var Start: Int = 0
    var End: Int = 0
    var Text: String = null

    override def toString: String = {
      "Start:" + Start + " End:" + End + " '" + Text + "'"
    }
  }

  /** A section of fFormat bounded by a pair of escape characters; such ranges contain uninterpreted text. */
  class EscapedRange {
    var Start: Int = 0
    var End: Int = 0
  }

  /** Special character used to escape the interpretation of parts of fFormat. */
  val ESCAPE_CHAR: String = "|"
  val ESCAPED_RANGE: Pattern = Pattern.compile("\\|[^\\|]*\\|")
  val YYYY: String = "YYYY"
  val YY: String = "YY"
  val M: String = "M"
  val MM: String = "MM"
  val MMM: String = "MMM"
  val MMMM: String = "MMMM"
  val D: String = "D"
  val DD: String = "DD"
  val WWW: String = "WWW"
  val WWWW: String = "WWWW"
  val hh: String = "hh"
  val h: String = "h"
  val m: String = "m"
  val mm: String = "mm"
  val s: String = "s"
  val ss: String = "ss"
  /**
  The 12-hour clock style.

   12:00 am is midnight, 12:30am is 30 minutes past midnight, 12:00 pm is 12 noon.
   This item is almost always used with 'a' to indicate am/pm.
    */
  val h12: String = "h12"
  /** As {@link #h12}, but with leading zero. */
  val hh12: String = "hh12"
  val AM: Int = 0
  val PM: Int = 1
  /**
  A.M./P.M. text is sensitive to Locale, in the same way that names of months and weekdays are
   sensitive to Locale.
    */
  val a: String = "a"
  val FRACTIONALS: Pattern = Pattern.compile("f{1,9}")
  val EMPTY_STRING: String = ""
  /**
  The order of these items is significant, and is critical for how fFormat is interpreted.
   The 'longer' tokens must come first, in any group of related tokens.
    */
  val TOKENS: List[String] = new ArrayList[String]
  try {
    TOKENS.add(YYYY)
    TOKENS.add(YY)
    TOKENS.add(MMMM)
    TOKENS.add(MMM)
    TOKENS.add(MM)
    TOKENS.add(M)
    TOKENS.add(DD)
    TOKENS.add(D)
    TOKENS.add(WWWW)
    TOKENS.add(WWW)
    TOKENS.add(hh12)
    TOKENS.add(h12)
    TOKENS.add(hh)
    TOKENS.add(h)
    TOKENS.add(mm)
    TOKENS.add(m)
    TOKENS.add(ss)
    TOKENS.add(s)
    TOKENS.add(a)
    TOKENS.add("fffffffff")
    TOKENS.add("ffffffff")
    TOKENS.add("fffffff")
    TOKENS.add("ffffff")
    TOKENS.add("fffff")
    TOKENS.add("ffff")
    TOKENS.add("fff")
    TOKENS.add("ff")
    TOKENS.add("f")
  }
}

class DateTimeFormatter {
  /**
  Constructor used for patterns that represent date-time elements using only numbers, and no localizable text.
   @param aFormat uses the syntax described by { @link DateTime#format(String)}.
    */
  def this(aFormat: String): Unit = {
    this()
    fFormat = aFormat
    fLocale = null
    fCustomLocalization = null
    validateState
  }

  /**
  Constructor used for patterns that represent date-time elements using not only numbers, but text as well.
   The text needs to be localizable.
   @param aFormat uses the syntax described by { @link DateTime#format(String)}.
  @param aLocale used to generate text for Month, Weekday, and AM-PM indicator; required only by patterns which localized
   text, instead of numeric forms for date-time elements.
    */
  def this(aFormat: String, aLocale: Locale): Unit = {
    this()
    fFormat = aFormat
    fLocale = aLocale
    fCustomLocalization = null
    validateState
  }

  /**
  Constructor used for patterns that represent using not only numbers, but customized text as well.

   <P>This constructor exists mostly since SimpleDateFormat doesn't support all locales, and it has a
   policy of N letters for text, where N != 3.

   @param aFormat must match the syntax described by { @link DateTime#format(String)}.
  @param aMonths contains text for all 12 months, starting with January; size must be 12.
  @param aWeekdays contains text for all 7 weekdays, starting with Sunday; size must be 7.
  @param aAmPmIndicators contains text for A.M and P.M. indicators (in that order); size must be 2.
    */
  def this(aFormat: String, aMonths: List[String], aWeekdays: List[String], aAmPmIndicators: List[String]): Unit = {
    this()
    fFormat = aFormat
    fLocale = null
    fCustomLocalization = new DateTimeFormatter#CustomLocalization(aMonths, aWeekdays, aAmPmIndicators)
    validateState
  }

  /** Format a {@link DateTime}.  */
  def format(aDateTime: DateTime): String = {
    fEscapedRanges = new ArrayList[DateTimeFormatter.EscapedRange]
    fInterpretedRanges = new ArrayList[DateTimeFormatter.InterpretedRange]
    findEscapedRanges
    interpretInput(aDateTime)
    produceFinalOutput
  }

  private final val fFormat: String = null
  private final val fLocale: Locale = null
  private var fInterpretedRanges: Collection[DateTimeFormatter.InterpretedRange] = null
  private var fEscapedRanges: Collection[DateTimeFormatter.EscapedRange] = null
  /**
  Table mapping a Locale to the names of the months.
    Initially empty, populated only when a specific Locale is needed for presenting such text.
    Used for MMMM and MMM tokens.
    */
  private final val fMonths: Map[Locale, List[String]] = new LinkedHashMap[Locale, List[String]]
  /**
  Table mapping a Locale to the names of the weekdays.
   Initially empty, populated only when a specific Locale is needed for presenting such text.
   Used for WWWW and WWW tokens.
    */
  private final val fWeekdays: Map[Locale, List[String]] = new LinkedHashMap[Locale, List[String]]
  /**
  Table mapping a Locale to the text used to indicate a.m. and p.m.
    Initially empty, populated only when a specific Locale is needed for presenting such text.
    Used for the 'a' token.
    */
  private final val fAmPm: Map[Locale, List[String]] = new LinkedHashMap[Locale, List[String]]
  private final val fCustomLocalization: DateTimeFormatter#CustomLocalization = null

  class CustomLocalization {
    def this(aMonths: List[String], aWeekdays: List[String], aAmPm: List[String]) {
      this()
      if (aMonths.size != 12) {
        throw new IllegalArgumentException("Your List of custom months must have size 12, but its size is " + aMonths.size)
      }
      if (aWeekdays.size != 7) {
        throw new IllegalArgumentException("Your List of custom weekdays must have size 7, but its size is " + aWeekdays.size)
      }
      if (aAmPm.size != 2) {
        throw new IllegalArgumentException("Your List of custom a.m./p.m. indicators must have size 2, but its size is " + aAmPm.size)
      }
      Months = aMonths
      Weekdays = aWeekdays
      AmPmIndicators = aAmPm
    }

    var Months: List[String] = null
    var Weekdays: List[String] = null
    var AmPmIndicators: List[String] = null
  }

  /** Escaped ranges are bounded by a PAIR of {@link #ESCAPE_CHAR} characters. */
  private def findEscapedRanges {
    val matcher: Matcher = DateTimeFormatter.ESCAPED_RANGE.matcher(fFormat)
    while (matcher.find) {
      val escapedRange: DateTimeFormatter.EscapedRange = new DateTimeFormatter.EscapedRange
      escapedRange.Start = matcher.start
      escapedRange.End = matcher.end - 1
      fEscapedRanges.add(escapedRange)
    }
  }

  /** Return true only if the start of the interpreted range is in an escaped range. */
  private def isInEscapedRange(aInterpretedRange: DateTimeFormatter.InterpretedRange): Boolean = {
    var result: Boolean = false
    import scala.collection.JavaConversions._
    for (escapedRange <- fEscapedRanges) {
      if (escapedRange.Start <= aInterpretedRange.Start && aInterpretedRange.Start <= escapedRange.End) {
        result = true
        break //todo: break is not supported
      }
    }
    result
  }

  /**
  Scan fFormat for all tokens, in a specific order, and interpret them with the given DateTime.
   The interpreted tokens are saved for output later.
    */
  private def interpretInput(aDateTime: DateTime) {
    var format: String = fFormat
    import scala.collection.JavaConversions._
    for (token <- DateTimeFormatter.TOKENS) {
      val pattern: Pattern = Pattern.compile(token)
      val matcher: Matcher = pattern.matcher(format)
      while (matcher.find) {
        val interpretedRange: DateTimeFormatter.InterpretedRange = new DateTimeFormatter.InterpretedRange
        interpretedRange.Start = matcher.start
        interpretedRange.End = matcher.end - 1
        if (!isInEscapedRange(interpretedRange)) {
          interpretedRange.Text = interpretThe(matcher.group, aDateTime)
          fInterpretedRanges.add(interpretedRange)
        }
      }
      format = format.replace(token, withCharDenotingAlreadyInterpreted(token))
    }
  }

  /**
  Return a temp placeholder string used to identify sections of fFormat that have already been interpreted.
   The returned string is a list of "@" characters, whose length is the same as aToken.
    */
  private def withCharDenotingAlreadyInterpreted(aToken: String): String = {
    val result: StringBuilder = new StringBuilder
    {
      var idx: Int = 1
      while (idx <= aToken.length) {
        {
          result.append("@")
        }
        ({
          idx += 1; idx
        })
      }
    }
    result.toString
  }

  /** Render the final output returned to the caller. */
  private def produceFinalOutput: String = {
    val result: StringBuilder = new StringBuilder
    var idx: Int = 0
    while (idx < fFormat.length) {
      val letter: String = nextLetter(idx)
      val interpretation: DateTimeFormatter.InterpretedRange = getInterpretation(idx)
      if (interpretation != null) {
        result.append(interpretation.Text)
        idx = interpretation.End
      }
      else {
        if (!(DateTimeFormatter.ESCAPE_CHAR == letter)) {
          result.append(letter)
        }
      }
      idx += 1
    }
    result.toString
  }

  private def getInterpretation(aIdx: Int): DateTimeFormatter.InterpretedRange = {
    var result: DateTimeFormatter.InterpretedRange = null
    import scala.collection.JavaConversions._
    for (interpretedRange <- fInterpretedRanges) {
      if (interpretedRange.Start == aIdx) {
        result = interpretedRange
      }
    }
    result
  }

  private def nextLetter(aIdx: Int): String = {
    fFormat.substring(aIdx, aIdx + 1)
  }

  private def interpretThe(aCurrentToken: String, aDateTime: DateTime): String = {
    var result: String = DateTimeFormatter.EMPTY_STRING
    if (DateTimeFormatter.YYYY == aCurrentToken) {
      result = valueStr(aDateTime.getYear)
    }
    else if (DateTimeFormatter.YY == aCurrentToken) {
      result = noCentury(valueStr(aDateTime.getYear))
    }
    else if (DateTimeFormatter.MMMM == aCurrentToken) {
      val month: Int = aDateTime.getMonth
      result = fullMonth(month)
    }
    else if (DateTimeFormatter.MMM == aCurrentToken) {
      val month: Int = aDateTime.getMonth
      result = firstThreeChars(fullMonth(month))
    }
    else if (DateTimeFormatter.MM == aCurrentToken) {
      result = addLeadingZero(valueStr(aDateTime.getMonth))
    }
    else if (DateTimeFormatter.M == aCurrentToken) {
      result = valueStr(aDateTime.getMonth)
    }
    else if (DateTimeFormatter.DD == aCurrentToken) {
      result = addLeadingZero(valueStr(aDateTime.getDay))
    }
    else if (DateTimeFormatter.D == aCurrentToken) {
      result = valueStr(aDateTime.getDay)
    }
    else if (DateTimeFormatter.WWWW == aCurrentToken) {
      val weekday: Int = aDateTime.getWeekDay
      result = fullWeekday(weekday)
    }
    else if (DateTimeFormatter.WWW == aCurrentToken) {
      val weekday: Int = aDateTime.getWeekDay
      result = firstThreeChars(fullWeekday(weekday))
    }
    else if (DateTimeFormatter.hh == aCurrentToken) {
      result = addLeadingZero(valueStr(aDateTime.getHour))
    }
    else if (DateTimeFormatter.h == aCurrentToken) {
      result = valueStr(aDateTime.getHour)
    }
    else if (DateTimeFormatter.h12 == aCurrentToken) {
      result = valueStr(twelveHourStyle(aDateTime.getHour))
    }
    else if (DateTimeFormatter.hh12 == aCurrentToken) {
      result = addLeadingZero(valueStr(twelveHourStyle(aDateTime.getHour)))
    }
    else if (DateTimeFormatter.a == aCurrentToken) {
      val hour: Int = aDateTime.getHour
      result = amPmIndicator(hour)
    }
    else if (DateTimeFormatter.mm == aCurrentToken) {
      result = addLeadingZero(valueStr(aDateTime.getMinute))
    }
    else if (DateTimeFormatter.m == aCurrentToken) {
      result = valueStr(aDateTime.getMinute)
    }
    else if (DateTimeFormatter.ss == aCurrentToken) {
      result = addLeadingZero(valueStr(aDateTime.getSecond))
    }
    else if (DateTimeFormatter.s == aCurrentToken) {
      result = valueStr(aDateTime.getSecond)
    }
    else if (aCurrentToken.startsWith("f")) {
      val matcher: Matcher = DateTimeFormatter.FRACTIONALS.matcher(aCurrentToken)
      if (matcher.matches) {
        val nanos: String = nanosWithLeadingZeroes(aDateTime.getNanoseconds)
        val numDecimalsToShow: Int = aCurrentToken.length
        result = firstNChars(nanos, numDecimalsToShow)
      }
      else {
        throw new IllegalArgumentException("Unknown token in date formatting pattern: " + aCurrentToken)
      }
    }
    else {
      throw new IllegalArgumentException("Unknown token in date formatting pattern: " + aCurrentToken)
    }
    result
  }

  private def valueStr(aItem: AnyRef): String = {
    var result: String = DateTimeFormatter.EMPTY_STRING
    if (aItem != null) {
      result = String.valueOf(aItem)
    }
    result
  }

  private def noCentury(aItem: String): String = {
    var result: String = DateTimeFormatter.EMPTY_STRING
    if (Util.textHasContent(aItem)) {
      result = aItem.substring(2)
    }
    result
  }

  private def nanosWithLeadingZeroes(aNanos: Int): String = {
    var result: String = valueStr(aNanos)
    while (result.length < 9) {
      result = "0" + result
    }
    result
  }

  /** Pad 0..9 with a leading zero. */
  private def addLeadingZero(aTimePart: String): String = {
    var result: String = aTimePart
    if (Util.textHasContent(aTimePart) && aTimePart.length == 1) {
      result = "0" + result
    }
    result
  }

  private def firstThreeChars(aText: String): String = {
    var result: String = aText
    if (Util.textHasContent(aText) && aText.length >= 3) {
      result = aText.substring(0, 3)
    }
    result
  }

  private def fullMonth(aMonth: Int): String = {
    var result: String = ""
    if (aMonth != null) {
      if (fCustomLocalization != null) {
        result = lookupCustomMonthFor(aMonth)
      }
      else if (fLocale != null) {
        result = lookupMonthFor(aMonth)
      }
      else {
        throw new IllegalArgumentException("Your date pattern requires either a Locale, or your own custom localizations for text:" + Util.quote(fFormat))
      }
    }
    result
  }

  private def lookupCustomMonthFor(aMonth: Int): String = {
    fCustomLocalization.Months.get(aMonth - 1)
  }

  private def lookupMonthFor(aMonth: Int): String = {
    var result: String = DateTimeFormatter.EMPTY_STRING
    if (!fMonths.containsKey(fLocale)) {
      val months: List[String] = new ArrayList[String]
      val format: SimpleDateFormat = new SimpleDateFormat("MMMM", fLocale)
      {
        var idx: Int = Calendar.JANUARY
        while (idx <= Calendar.DECEMBER) {
          {
            val firstDayOfMonth: Calendar = new GregorianCalendar
            firstDayOfMonth.set(Calendar.YEAR, 2000)
            firstDayOfMonth.set(Calendar.MONTH, idx)
            firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 15)
            val monthText: String = format.format(firstDayOfMonth.getTime)
            months.add(monthText)
          }
          ({
            idx += 1; idx
          })
        }
      }
      fMonths.put(fLocale, months)
    }
    result = fMonths.get(fLocale).get(aMonth - 1)
    result
  }

  private def fullWeekday(aWeekday: Int): String = {
    var result: String = ""
    if (aWeekday != null) {
      if (fCustomLocalization != null) {
        result = lookupCustomWeekdayFor(aWeekday)
      }
      else if (fLocale != null) {
        result = lookupWeekdayFor(aWeekday)
      }
      else {
        throw new IllegalArgumentException("Your date pattern requires either a Locale, or your own custom localizations for text:" + Util.quote(fFormat))
      }
    }
    result
  }

  private def lookupCustomWeekdayFor(aWeekday: Int): String = {
    fCustomLocalization.Weekdays.get(aWeekday - 1)
  }

  private def lookupWeekdayFor(aWeekday: Int): String = {
    var result: String = DateTimeFormatter.EMPTY_STRING
    if (!fWeekdays.containsKey(fLocale)) {
      val weekdays: List[String] = new ArrayList[String]
      val format: SimpleDateFormat = new SimpleDateFormat("EEEE", fLocale)
      {
        var idx: Int = 8
        while (idx <= 14) {
          {
            val firstDayOfWeek: Calendar = new GregorianCalendar
            firstDayOfWeek.set(Calendar.YEAR, 2009)
            firstDayOfWeek.set(Calendar.MONTH, 1)
            firstDayOfWeek.set(Calendar.DAY_OF_MONTH, idx)
            val weekdayText: String = format.format(firstDayOfWeek.getTime)
            weekdays.add(weekdayText)
          }
          ({
            idx += 1; idx
          })
        }
      }
      fWeekdays.put(fLocale, weekdays)
    }
    result = fWeekdays.get(fLocale).get(aWeekday - 1)
    result
  }

  private def firstNChars(aText: String, aN: Int): String = {
    var result: String = aText
    if (Util.textHasContent(aText) && aText.length >= aN) {
      result = aText.substring(0, aN)
    }
    result
  }

  /** Coerce the hour to match the number used in the 12-hour style. */
  private def twelveHourStyle(aHour: Int): Int = {
    var result: Int = aHour
    if (aHour != null) {
      if (aHour eq 0) {
        result = 12
      }
      else if (aHour > 12) {
        result = aHour - 12
      }
    }
    result
  }

  private def amPmIndicator(aHour: Int): String = {
    var result: String = ""
    if (aHour != null) {
      if (fCustomLocalization != null) {
        result = lookupCustomAmPmFor(aHour)
      }
      else if (fLocale != null) {
        result = lookupAmPmFor(aHour)
      }
      else {
        throw new IllegalArgumentException("Your date pattern requires either a Locale, or your own custom localizations for text:" + Util.quote(fFormat))
      }
    }
    result
  }

  private def lookupCustomAmPmFor(aHour: Int): String = {
    var result: String = DateTimeFormatter.EMPTY_STRING
    if (aHour < 12) {
      result = fCustomLocalization.AmPmIndicators.get(DateTimeFormatter.AM)
    }
    else {
      result = fCustomLocalization.AmPmIndicators.get(DateTimeFormatter.PM)
    }
    result
  }

  private def lookupAmPmFor(aHour: Int): String = {
    var result: String = DateTimeFormatter.EMPTY_STRING
    if (!fAmPm.containsKey(fLocale)) {
      val indicators: List[String] = new ArrayList[String]
      indicators.add(getAmPmTextFor(6))
      indicators.add(getAmPmTextFor(18))
      fAmPm.put(fLocale, indicators)
    }
    if (aHour < 12) {
      result = fAmPm.get(fLocale).get(DateTimeFormatter.AM)
    }
    else {
      result = fAmPm.get(fLocale).get(DateTimeFormatter.PM)
    }
    result
  }

  private def getAmPmTextFor(aHour: Int): String = {
    val format: SimpleDateFormat = new SimpleDateFormat("a", fLocale)
    val someDay: Calendar = new GregorianCalendar
    someDay.set(Calendar.YEAR, 2000)
    someDay.set(Calendar.MONTH, 6)
    someDay.set(Calendar.DAY_OF_MONTH, 15)
    someDay.set(Calendar.HOUR_OF_DAY, aHour)
    format.format(someDay.getTime)
  }

  private def validateState {
    if (!Util.textHasContent(fFormat)) {
      throw new IllegalArgumentException("DateTime format has no content.")
    }
  }
}