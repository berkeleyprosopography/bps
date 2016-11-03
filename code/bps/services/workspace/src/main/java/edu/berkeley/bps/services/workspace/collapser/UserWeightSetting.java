package edu.berkeley.bps.services.workspace.collapser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.berkeley.bps.services.common.utils.Pair;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "userWeight")
public class UserWeightSetting extends Pair<String, Double> {
	private static final String DUMMY_LABEL = "temp";
	private static final Double DUMMY_WEIGHT = 0.0;
	
		/**
	 * Internal null Ctor for JAXB
	 */
	public UserWeightSetting() {
		this(DUMMY_LABEL, DUMMY_WEIGHT);
	}

	public UserWeightSetting(String label, Double weight) {
		super(label, weight);
		if(label == null || label.isEmpty())
			throw new IllegalArgumentException("UserWeightSetting must have non-null label");
		if(weight < 0 || weight > 1)
			throw new IllegalArgumentException("UserWeightSetting weight must be >= 0 and <= 1");
	}
	
	@XmlElement(name = "label")
	public String getLabel() {
		return getFirst();
	}

	// @XmlElement(name = "label")
	public void setLabel(String newLabel) {
		setFirst(newLabel);
	}

	@XmlElement(name = "weight")
	public Double getWeight() {
		return getSecond();
	}

	// @XmlElement(name = "weight")
	public void setWeight(Double newWeight) {
		setSecond(newWeight);
	}

}
