package tempo

/**
 * Created by javier on 6/3/15.
 */

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
sealed trait DayOverflow
case object LastDay extends DayOverflow
case object FirstDay extends DayOverflow
case object Spillover extends DayOverflow
case object Abort extends DayOverflow
