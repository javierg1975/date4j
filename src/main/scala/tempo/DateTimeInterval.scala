package tempo

/**
 * Created by javier on 6/3/15.
 */
/**
Helper class for adding intervals of time.
 The mental model of this class is similar to that of a car's odometer.
  */
object DateTimeInterval {
  private val MIN: Int = 0
  private val MAX: Int = 9999
  private val MIN_NANOS: Int = 0
  private val MAX_NANOS: Int = 999999999
  private val PLUS: Boolean = true
  private val MINUS: Boolean = false
}

case class DateTimeInterval(aFrom: DateTime, aMonthOverflow: Nothing) {


  def plus(aYear: Int, aMonth: Int, aDay: Int, aHour: Int, aMinute: Int, aSecond: Int, aNanosecond: Int): DateTime = {
    return plusOrMinus(DateTimeInterval.PLUS, aYear, aMonth, aDay, aHour, aMinute, aSecond, aNanosecond)
  }

  def minus(aYear: Int, aMonth: Int, aDay: Int, aHour: Int, aMinute: Int, aSecond: Int, aNanosecond: Int): DateTime = {
    return plusOrMinus(DateTimeInterval.MINUS, aYear, aMonth, aDay, aHour, aMinute, aSecond, aNanosecond)
  }

  private final val fFrom: DateTime = null
  private var fIsPlus: Boolean = false
  private var fDayOverflow: DateTime.DayOverflow.type = null
  private var fYearIncr: Int = 0
  private var fMonthIncr: Int = 0
  private var fDayIncr: Int = 0
  private var fHourIncr: Int = 0
  private var fMinuteIncr: Int = 0
  private var fSecondIncr: Int = 0
  private var fNanosecondIncr: Int = 0
  private var fYear: Int = null
  private var fMonth: Int = null
  private var fDay: Int = null
  private var fHour: Int = null
  private var fMinute: Int = null
  private var fSecond: Int = null
  private var fNanosecond: Int = null

  private def checkUnits {
    var success: Boolean = false
    if (fFrom.unitsAllPresent(DateTimeUnit.YEAR, DateTimeUnit.MONTH, DateTimeUnit.DAY, DateTimeUnit.HOUR, DateTimeUnit.MINUTE, DateTimeUnit.SECOND)) {
      success = true
    }
    else if (fFrom.unitsAllPresent(DateTimeUnit.YEAR, DateTimeUnit.MONTH, DateTimeUnit.DAY) && fFrom.unitsAllAbsent(DateTimeUnit.HOUR, DateTimeUnit.MINUTE, DateTimeUnit.SECOND)) {
      success = true
    }
    else if (fFrom.unitsAllAbsent(DateTimeUnit.YEAR, DateTimeUnit.MONTH, DateTimeUnit.DAY) && fFrom.unitsAllPresent(DateTimeUnit.HOUR, DateTimeUnit.MINUTE, DateTimeUnit.SECOND)) {
      success = true
    }
    else {
      success = false
    }
    if (!success) {
      throw new IllegalArgumentException("For interval calculations, DateTime must have year-month-day, or hour-minute-second, or both.")
    }
  }

  private def plusOrMinus(aIsPlus: Boolean, aYear: Int, aMonth: Int, aDay: Int, aHour: Int, aMinute: Int, aSecond: Int, aNanosecond: Int): DateTime = {
    fIsPlus = aIsPlus
    fYearIncr = aYear
    fMonthIncr = aMonth
    fDayIncr = aDay
    fHourIncr = aHour
    fMinuteIncr = aMinute
    fSecondIncr = aSecond
    fNanosecondIncr = aNanosecond
    checkRange(fYearIncr, "Year")
    checkRange(fMonthIncr, "Month")
    checkRange(fDayIncr, "Day")
    checkRange(fHourIncr, "Hour")
    checkRange(fMinuteIncr, "Minute")
    checkRange(fSecondIncr, "Second")
    checkRangeNanos(fNanosecondIncr)
    changeYear
    changeMonth
    handleMonthOverflow
    changeDay
    changeHour
    changeMinute
    changeSecond
    changeNanosecond
    return new DateTime(fYear, fMonth, fDay, fHour, fMinute, fSecond, fNanosecond)
  }

  private def checkRange(aValue: Int, aName: String) {
    if (aValue < DateTimeInterval.MIN || aValue > DateTimeInterval.MAX) {
      throw new IllegalArgumentException(aName + " is not in the range " + DateTimeInterval.MIN + "main" + DateTimeInterval.MAX)
    }
  }

  private def checkRangeNanos(aValue: Int) {
    if (aValue < DateTimeInterval.MIN_NANOS || aValue > DateTimeInterval.MAX_NANOS) {
      throw new IllegalArgumentException("Nanosecond interval is not in the range " + DateTimeInterval.MIN_NANOS + "main" + DateTimeInterval.MAX_NANOS)
    }
  }

  private def changeYear {
    if (fIsPlus) {
      fYear = fYear + fYearIncr
    }
    else {
      fYear = fFrom.getYear - fYearIncr
    }
  }

  private def changeMonth {
    var count: Int = 0
    while (count < fMonthIncr) {
      stepMonth
      count += 1
    }
  }

  private def changeDay {
    var count: Int = 0
    while (count < fDayIncr) {
      stepDay
      count += 1
    }
  }

  private def changeHour {
    var count: Int = 0
    while (count < fHourIncr) {
      stepHour
      count += 1
    }
  }

  private def changeMinute {
    var count: Int = 0
    while (count < fMinuteIncr) {
      stepMinute
      count += 1
    }
  }

  private def changeSecond {
    var count: Int = 0
    while (count < fSecondIncr) {
      stepSecond
      count += 1
    }
  }

  /**
  Nanos are different from other items. They don't cycle one step at a time.
   They are just added. If they under/over flow, then extra math is performed.
   They don't over/under by more than 1 second, since the size of the increment is limited.
    */
  private def changeNanosecond {
    if (fIsPlus) {
      fNanosecond = fNanosecond + fNanosecondIncr
    }
    else {
      fNanosecond = fNanosecond - fNanosecondIncr
    }
    if (fNanosecond > DateTimeInterval.MAX_NANOS) {
      stepSecond
      fNanosecond = fNanosecond - DateTimeInterval.MAX_NANOS - 1
    }
    else if (fNanosecond < DateTimeInterval.MIN_NANOS) {
      stepSecond
      fNanosecond = DateTimeInterval.MAX_NANOS + fNanosecond + 1
    }
  }

  private def stepYear {
    if (fIsPlus) {
      fYear = fYear + 1
    }
    else {
      fYear = fYear - 1
    }
  }

  private def stepMonth {
    if (fIsPlus) {
      fMonth = fMonth + 1
    }
    else {
      fMonth = fMonth - 1
    }
    if (fMonth > 12) {
      fMonth = 1
      stepYear
    }
    else if (fMonth < 1) {
      fMonth = 12
      stepYear
    }
  }

  private def stepDay {
    if (fIsPlus) {
      fDay = fDay + 1
    }
    else {
      fDay = fDay - 1
    }
    if (fDay > numDaysInMonth) {
      fDay = 1
      stepMonth
    }
    else if (fDay < 1) {
      fDay = numDaysInPreviousMonth
      stepMonth
    }
  }

  private def numDaysInMonth: Int = {
    return DateTime.getNumDaysInMonth(fYear, fMonth)
  }

  private def numDaysInPreviousMonth: Int = {
    var result: Int = 0
    if (fMonth > 1) {
      result = DateTime.getNumDaysInMonth(fYear, fMonth - 1)
    }
    else {
      result = DateTime.getNumDaysInMonth(fYear - 1, 12)
    }
    return result
  }

  private def stepHour {
    if (fIsPlus) {
      fHour = fHour + 1
    }
    else {
      fHour = fHour - 1
    }
    if (fHour > 23) {
      fHour = 0
      stepDay
    }
    else if (fHour < 0) {
      fHour = 23
      stepDay
    }
  }

  private def stepMinute {
    if (fIsPlus) {
      fMinute = fMinute + 1
    }
    else {
      fMinute = fMinute - 1
    }
    if (fMinute > 59) {
      fMinute = 0
      stepHour
    }
    else if (fMinute < 0) {
      fMinute = 59
      stepHour
    }
  }

  private def stepSecond {
    if (fIsPlus) {
      fSecond = fSecond + 1
    }
    else {
      fSecond = fSecond - 1
    }
    if (fSecond > 59) {
      fSecond = 0
      stepMinute
    }
    else if (fSecond < 0) {
      fSecond = 59
      stepMinute
    }
  }

  private def handleMonthOverflow {
    val daysInMonth: Int = numDaysInMonth
    if (fDay > daysInMonth) {
      if (DayOverflow.Abort eq fDayOverflow) {
        throw new RuntimeException("Day Overflow: Year:" + fYear + " Month:" + fMonth + " has " + daysInMonth + " days, but day has value:" + fDay + " To avoid these exceptions, please specify a different DayOverflow policy.")
      }
      else if (DayOverflow.FirstDay eq fDayOverflow) {
        fDay = 1
        stepMonth
      }
      else if (DayOverflow.LastDay eq fDayOverflow) {
        fDay = daysInMonth
      }
      else if (DayOverflow.Spillover eq fDayOverflow) {
        val overflowAmount: Int = fDay - daysInMonth
        fDay = overflowAmount
        stepMonth
      }
    }
  }
}