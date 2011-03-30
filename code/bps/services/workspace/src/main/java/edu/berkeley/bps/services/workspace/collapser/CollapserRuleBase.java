package edu.berkeley.bps.services.workspace.collapser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class CollapserRuleBase implements CollapserRule {
	public static final int DOCUMENT_CONTEXT = 0x1;
	public static final int CORPUS_CONTEXT = 0x2;
	public static final int BOTH_CONTEXTS = 0x3;
	
	@XmlElement
	protected int type;
	@XmlElement
	protected int context;
	@XmlElement
	protected String name;
	@XmlElement
	protected double weight;
	
	public CollapserRuleBase() {
		this(SHIFT_RULE, "Unknown", 1.0, BOTH_CONTEXTS);
	}
	
	public CollapserRuleBase(int type, String name, double weight, int context) {
		this.type = checkType(type);
		this.context = checkContext(type);
		setName(name);
		setWeight(weight);
	}

	/**
	 * @see edu.berkeley.bps.services.workspace.collapser.CollapserRule#getType()
	 */
	public int getType() {
		return type;
	}
	
	@XmlElement(name="typeString")
	public String getTypeString() {
		return type==SHIFT_RULE?"Shift":(type==DISCOUNT_RULE?"Discount":"Boost");
	}
	
	/**
	 * Not public, since we do not allow setting the type, except at construction
	 */
	protected int checkType(int type) {
		if(type < SHIFT_RULE || type > BOOST_RULE)  
			throw new IllegalArgumentException("Not a valid CollapserRule type");
		return type;
	}
	
	/**
	 * Not public, since we do not allow setting the type, except at construction
	 */
	protected int checkContext(int context) {
		if((context == 0) || ((context & ~BOTH_CONTEXTS)!=0))  
			throw new IllegalArgumentException("Not a valid CollapserRule context");
		return context;
	}
	
	/**
	 * @see edu.berkeley.bps.services.workspace.collapser.CollapserRule#getName()
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @see edu.berkeley.bps.services.workspace.collapser.CollapserRule#setName(String)
	 */
	public void setName(String name) {
		if(name==null || name.isEmpty())
			throw new IllegalArgumentException("CollapserRule must have a non-null name");
		this.name = name;
	}
	
	/**
	 * @see edu.berkeley.bps.services.workspace.collapser.CollapserRule#getWeight()
	 */
	public double getWeight() {
		return weight;
	}
	
	/**
	 * @see edu.berkeley.bps.services.workspace.collapser.CollapserRule#setWeight(double)
	 */
	public void setWeight(double weight) {
		if(weight<0 || weight > 1)
			throw new IllegalArgumentException("CollapserRule weight must be >= 0 and <= 1");
		this.weight = weight;
	}

	/**
	 * @see edu.berkeley.bps.services.workspace.collapser.CollapserRule#appliesWithinDocument()
	 */
	public boolean appliesWithinDocument() {
		return (context & DOCUMENT_CONTEXT) != 0;
	}
	
	/**
	 * @see edu.berkeley.bps.services.workspace.collapser.CollapserRule#appliesAcrossCorpus()
	 */
	public boolean appliesAcrossCorpus() {
		return (context & CORPUS_CONTEXT) != 0;
	}
	
	
}
