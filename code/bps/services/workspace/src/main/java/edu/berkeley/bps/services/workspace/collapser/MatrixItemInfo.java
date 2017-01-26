package edu.berkeley.bps.services.workspace.collapser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "matrixItemInfo")
@XmlType(propOrder={"row","col","weight"})
public class MatrixItemInfo {
	
	protected String row;
	protected String col;
	protected double weight;

	public MatrixItemInfo() {
	}
	
	public MatrixItemInfo(String row, String col, double weight) {
		this.row = row;
		this.col = col;
		this.weight = weight;
	}

	/**
	 * @return the weight
	 */
	@XmlElement(name = "weight")
	public double getWeight() {
		return weight;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}

	/**
	 * @return the row
	 */
	@XmlElement(name = "row")
	public String getRow() {
		return row;
	}

	/**
	 * @return the col
	 */
	@XmlElement(name = "col")
	public String getCol() {
		return col;
	}
}
