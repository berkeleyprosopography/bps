package edu.berkeley.bps.services.workspace.collapser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Workspace;

@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({CollapserRuleBaseWithUI.class})
@XmlRootElement
public class CollapserRuleBase implements CollapserRule {
	
	protected Workspace owner = null;
	
	@XmlElement
	protected int type;
	
	/**
	 * Specifies context within which this rule functions. If true, this
	 * rule applies within documents, otherwise it applies across documents, 
	 * corpus wide. Note that it cannot be changed once the rule is created.
	 */
	@XmlElement
	protected boolean intraDocument;
	@XmlElement
	protected String name;
	@XmlElement
	protected double weight;
	
	public CollapserRuleBase() {
		this(SHIFT_RULE, "Unknown", 1.0, WITHIN_DOCUMENTS);
	}
	
	public CollapserRuleBase(int type, String name, double weight, boolean intraDocument) {
		this.type = checkType(type);
		this.intraDocument = intraDocument;
		setName(name);
		setWeight(weight);
	}

	@Override
	public void initialize(Workspace workspace) {
		// Get the roles from the workspace, and build the derived Pairs
		owner = workspace;
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
		return intraDocument;
	}

	@Override
	public double evaluate(Entity fromEntity, Entity toEntity) {
		throw new RuntimeException("CollapserRuleBase.evaluate should always be overridden");
	}
	
	
}
