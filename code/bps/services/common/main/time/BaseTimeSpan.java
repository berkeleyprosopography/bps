package bps.services.common.main.time;

public abstract class BaseTimeSpan implements TimeSpan {

	protected double stdDev = 0;
	private double twoVariance = 0;

	public BaseTimeSpan(double stdDev) {
		super();
		setStdDev(stdDev);
	}

	public double getStdDev() {
		return stdDev;
	}

	public void setStdDev(double value) {
		if(stdDev<=0)
			throw new IllegalArgumentException("TimeSpace StdDev must be > 0");
		stdDev = value;
		twoVariance = 2*stdDev*stdDev;
	}

	public double computeProbabilityForTime(long time) {
		long delta = (time-getCenterPoint());
		return Math.pow(Math.E, (-delta*delta/twoVariance));
	}

}