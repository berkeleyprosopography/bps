/**
 *
 */
package edu.berkeley.bps.services.time;
import java.util.Date;

/**
 * @author pschmitz
 * Defines a representation of an Era in BPS. These are generally fixed, and so support
 * only getters, not setters.
 */
public interface Era {

	/**
	 * @return the string name for this era
	 */
	public String getName();

	/**
	 * Return the start date for this era
	 * @return Date useful for arithmetic, or for output with GregorianCalendar
	 */
	public Date getStartDate();

	/**
	 * Return the ending date for this era
	 * @return Date useful for arithmetic, or for output with GregorianCalendar
	 */
	public Date getEndDate();

	/**
	 * Produce a BPS Date from the year and day of year, in this era.
	 * @param year cardinal year within this era
	 * @param doy cardinal day offset into the entire year
	 * @return Date useful for arithmetic, or for output with GregorianCalendar
	 * @throws IllegalArgumentException for arguments out of legal range.
	 */
	public Date getDate(int year, int doy)
		throws IllegalArgumentException;

	/**
	 * Produce a BPS Date from the year, cardinal month, and day, in this era.
	 * @param year year within this era
	 * @param month cardinal month
	 * @param day legal day
	 * @return Date useful for arithmetic, or for output with GregorianCalendar
	 * @throws IllegalArgumentException for arguments out of legal range.
	 */
	public Date getDate(int year, int month, int day)
		throws IllegalArgumentException;

	/**
	 * Produce a BPS Date from the year, named month, and day, in this era.
	 * @param year year within this era
	 * @param month named month (case insensitive)
	 * @param day legal day
	 * @return Date useful for arithmetic, or for output with GregorianCalendar
	 * @throws IllegalArgumentException for arguments out of legal range.
	 */
	public Date getDate(int year, String month, int day)
		throws IllegalArgumentException;

}
