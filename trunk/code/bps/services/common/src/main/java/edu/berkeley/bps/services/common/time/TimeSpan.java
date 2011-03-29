package edu.berkeley.bps.services.common.time;

public interface TimeSpan {
	
	public abstract boolean isValid();

	public abstract long getCenterPoint();

	public abstract double getWindow();

	public abstract double getStdDev();

	public abstract void setStdDev(double value);

	public abstract double computeProbabilityForTime(long time);

}
