package edu.berkeley.bps.services.workspace.collapser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name = "matrixItemInfo")
public class MatrixItemInfo {
	public String value1;
	public String value2;
	public double weight;

	public MatrixItemInfo() {
	}
	
	public MatrixItemInfo(String value1, String value2, double weight) {
		this.value1 = value1;
		this.value2 = value2;
		this.weight = weight;
	}
}
