package edu.berkeley.bps.services.common.time;

public abstract class BaseTimeSpan implements TimeSpan {
	private static final String myClass = "BaseTimeSpan";

	protected double window = 0;
	protected double stdDev = 0;
	private double twoVariance = 0;

	public BaseTimeSpan(double stdDev, double window) {
		super();
		setStdDev(stdDev);
		setWindow(window);
	}

	/**
	 * @return the window
	 */
	public double getWindow() {
		return window;
	}

	/**
	 * @param window the window to set
	 */
	public void setWindow(double window) {
		if(window<=0)
			throw new IllegalArgumentException(myClass+" Window must be > 0");
		this.window = window;
	}

	public double getStdDev() {
		return stdDev;
	}

	public void setStdDev(double value) {
		if(value<=0)
			throw new IllegalArgumentException(myClass+" StdDev must be > 0");
		stdDev = value;
		twoVariance = 2*stdDev*stdDev;
	}

	public double computeProbabilityForTime(long time) {
		double delta = Math.max(0, time-getCenterPoint()-getWindow());
		// within window, probability is 1.0
		return (delta==0)?1.0:Math.pow(Math.E, (-delta*delta/twoVariance));
	}

}
