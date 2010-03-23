package bps.services.common.main.time;

public interface TimeSpan {

	public abstract boolean isValid();

	public abstract long getCenterPoint();

	public abstract double getStdDev();

	public abstract void setStdDev(double value);

	public abstract double computeProbabilityForTime(long time);

}