package edu.berkeley.bps.services.common.time;

public interface TimeSpan {
	
	public abstract boolean isValid();

	public abstract long getCenterPoint();

	public abstract double getWindow();

	/**
	 * Returns the closest time to the passed time, that is within this
	 * time span's window. If the window is defined to be 0, will return
	 * the center-point time.
	 * @param time
	 * @return
	 */
	public abstract long getClosestWindowPointToTime(long time);

	public abstract double getStdDev();

	public abstract void setStdDev(double value);

	/**
	 * Computes the likelihood that the passed time is within this TimeSpan
	 * Will return 1 if it is within the window, and will return a value
	 * between 0 and 1 otherwise.
	 * @param time
	 * @return likelihood in the range of 0 to 1
	 */
	public abstract double computeProbabilityForTime(long time);

	/**
	 * Computes the likelihood that the passed time span overlaps with this TimeSpan
	 * Will return 1 if the windows overlap, and otherwise will use the closest
	 * window time in the passed span with computeProbabilityForTime()
	 * @param time
	 * @return likelihood in the range of 0 to 1
	 */
	public abstract double computeMutualProbability(TimeSpan span);

}
