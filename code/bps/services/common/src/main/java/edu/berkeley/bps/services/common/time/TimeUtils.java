package edu.berkeley.bps.services.common.time;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeUtils {
	static final Logger logger = LoggerFactory.getLogger(TimeUtils.class);
	private static GregorianCalendar cal = new GregorianCalendar();
	private static SimpleDateFormat simpleYearFormatter = new SimpleDateFormat("y GG");
	private static SimpleDateFormat simpleISODateFormatter = new SimpleDateFormat("yyyy-MM-dd GGG");
	private static StringBuilder out = new StringBuilder(8);

	public static final long DAY_IN_MILLIS = 24L*60L*60L*1000L;
	public static final long APPROX_YEAR_IN_MILLIS = (long)(365.25*(DAY_IN_MILLIS));
	
	public static long convertYearsToMillis(long years) {
		return 	years*APPROX_YEAR_IN_MILLIS;
	}
	
	public static double getDefaultWindowForActiveLife(int activeYears) {
		return 	activeYears*APPROX_YEAR_IN_MILLIS*2/3;
	}
	
	public static double getDefaultStdDevForActiveLife(int activeYears) {
		return 	activeYears*APPROX_YEAR_IN_MILLIS*2;
	}

	/**
	 * @param year (use negative years to indicate years BCE)
	 * @return standard milliseconds from epoch
	 */
	public static long getTimeInMillisForYear(int year) {
        return getTimeInMillisForYMD(year, Calendar.JULY, 1);
	}

	/**
	 * @param dateString a standard ISO yyyy-mm-dd string with an optional era suffix
	 * @return standard milliseconds from epoch
	 * @throws ParseException 
	 */
	public static long parseISO_G_DateToMillis(String dateString) throws ParseException {
		Date parsed = simpleISODateFormatter.parse(dateString);
        cal.clear();
		cal.setTime(parsed);
	    return cal.getTimeInMillis();
	}

	/**
	 * @param year (use negative years to indicate years BCE)
	 * @return computed offset value in milliseconds
	 */
	public static double getApproxTimeInMillisForYearOffset(double year) {
        return year*APPROX_YEAR_IN_MILLIS;
	}

	/**
	 * @param year (use negative years to indicate years BCE)
	 * @param month the value used to set the MONTH calendar field in the calendar. Month value is 0-based. e.g., 0 for January.
	 * @param day the value used to set the DAY_OF_MONTH calendar field in the calendar.
	 * @return standard milliseconds from epoch
	 */
	public static long getTimeInMillisForYMD(int year, int month, int day) {
        cal.clear();
        cal.set(year, month, day);
        return cal.getTimeInMillis();
	}

	/**
	 * @param millis
	 * @return standard formatted string for millisecond date value
	 */
	public static String millisToSimpleYearString(long millis) {
		cal.clear();
		cal.setTimeInMillis(millis);
		int year = cal.get(Calendar.YEAR);
		int era = cal.get(Calendar.ERA);
		out.setLength(0);
		out.append(year);
		out.append((era==GregorianCalendar.BC)?" BCE":" CE");
		return out.toString();
	}

	/**
	 * @param offset
	 * @return standard formatted string for millisecond offset value
	 */
	public static String millisToYearOffsetString(double offset) {
		return String.format("%.3f", offset/APPROX_YEAR_IN_MILLIS);
	}
}
