package edu.berkeley.bps.services.workspace.collapser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MatrixItemInfo {
	public String value1;
	public String value2;
	public double weight;
	
	public MatrixItemInfo(String value1, String value2, double weight) {
		this.value1 = value1;
		this.value2 = value2;
		this.weight = weight;
	}
}
