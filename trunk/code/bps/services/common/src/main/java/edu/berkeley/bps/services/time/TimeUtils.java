package edu.berkeley.bps.services.time;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeUtils {
	private static GregorianCalendar cal = new GregorianCalendar();
	private static SimpleDateFormat simpleYearFormatter = new SimpleDateFormat("yyyy GG");
	public static final long DAY_IN_MILLIS = 24L*60L*60L*1000L;
	public static final long APPROX_YEAR_IN_MILLIS = (long)(365.25*(DAY_IN_MILLIS));

	public static long getTimeInMillisForYear(int year) {
        return getTimeInMillisForYMD(year, Calendar.JULY, 1);
	}

	public static double getApproxTimeInMillisForYearOffset(double year) {
        return year*APPROX_YEAR_IN_MILLIS;
	}

	public static long getTimeInMillisForYMD(int year, int month, int day) {
        cal.clear();
        cal.set(year, month, day);
        return cal.getTimeInMillis();
	}

	public static String millisToSimpleYearString(long millis) {
		return simpleYearFormatter.format(new Date(millis));
	}

	public static String millisToYearOffsetString(double offset) {
		return String.format("%.3f", offset/APPROX_YEAR_IN_MILLIS);
	}
}
