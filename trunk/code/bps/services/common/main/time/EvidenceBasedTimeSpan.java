package bps.services.common.main.time;

import java.util.ArrayList;

//Rewrite for new interface def. Keep the array of dates, or at least
//keep the sums of weights and weighted times.

/**
 * @author pschmitz
 * Represents a date range that is inferred from data. This version supports
 * a range of the explicit dates seen, the inferred full range for the time span,
 * and a reference to a defined temporal range used to infer the full range.
 * The inferred range will center over the explicit range.
 * Later versions may incorporate a list of all data values seen with weights,
 * to support more elaborate inference.
 */
public class EvidenceBasedTimeSpan extends BaseTimeSpan {
	private ArrayList<Long> times = null;
	private ArrayList<Double> weights = null;
	double totalWeight = 0;
	long centerPoint = Long.MIN_VALUE;

	public EvidenceBasedTimeSpan(long centerPoint, double stdDev) {
		super(stdDev);
		this.centerPoint = centerPoint;
		times = new ArrayList<Long>();
		weights = new ArrayList<Double>();
		times.add(centerPoint);
		weights.add(1.0);
		totalWeight = 1.0;
	}

	/* (non-Javadoc)
	 * @see bps.services.common.main.InferredTimeSpan#isValid()
	 */
	public boolean isValid() {
		return (centerPoint > Long.MIN_VALUE) && (stdDev > 0);
	}

	public long getCenterPoint() {
		return centerPoint;
	}

	public void addDate(long newDate, double weight) {
		times.add(newDate);
		weights.add(weight);
		double temp = centerPoint*totalWeight + newDate*weight;
		totalWeight += weight;
		centerPoint = Math.round(temp/totalWeight);
	}

	private void updateInferredDates() {
	}

}
