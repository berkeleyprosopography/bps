package edu.berkeley.bps.services.workspace.collapser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.berkeley.bps.services.corpus.ActivityRole;
import edu.berkeley.bps.services.corpus.Corpus;
import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class RoleMatrixDiscountRule extends CollapserRuleBaseWithUI 
		implements CollapserRule, CollapserRuleUI, CollapserRulePairMatrixUI{
	static final Logger logger = LoggerFactory.getLogger(RoleMatrixDiscountRule.class);
	
	private final static String myClass = "RoleMatrixDiscountRule";
	private static final String DESCRIPTION = "TO DO";

	protected Corpus corpus = null;

	protected HashMap<String, Double> rolePairWeights = new HashMap<String, Double>();
	
	public RoleMatrixDiscountRule() {
		this(myClass);
	}

	public RoleMatrixDiscountRule(String name) {
		super(CollapserRule.DISCOUNT_RULE, name, DESCRIPTION, 1.0, 
					CollapserRule.WITHIN_DOCUMENTS);
	}

	
	@Override
	public void initialize(Workspace workspace) {
		// Get the roles from the workspace, and build the derived Pairs
		super.initialize(workspace);
		
		corpus = workspace.getCorpus();
		if(corpus==null) {
			logger.error("{}.initialize(): No corpus found for workspace: {}", 
					this.getClass().getName(), workspace.getId());
			return;			// Nothing to do
		}
		List<ActivityRole> corpusRoles = corpus.getActivityRoles();
		if(corpusRoles==null||corpusRoles.isEmpty()) {
			logger.error("{}.initialize(): No roles found in corpus: ", 
					this.getClass().getName(), corpus.getId());
			return;			// Nothing to do
		}
		// We should consider all the roles, ignore the family ones, and
		// try to find witness to exclude. That is a hack...
		ActivityRole witnessAR = null;
		for(ActivityRole ar:corpusRoles) {
			if(ar.isFamilyRole())
				continue;
			if(ar.getName().equalsIgnoreCase("witness")) {
				witnessAR = ar;
				break;
			}
		}
		if(witnessAR!=null) {
			for(ActivityRole ar:corpusRoles) {
				if(!ar.isFamilyRole())
					setPairWeight(witnessAR.getName(), ar.getName(), 0);
			}
		}
	}
	
	@Override
	public double evaluate(Entity fromEntity, Entity toEntity) {
		if(rolePairWeights.size()==0)
			return -1.0;	// No way to match the context;
		// Find the NRADS tied to these Persons, and then get
		// the discount weight for the pair of roles. 
		// Is this going to cause grief with partially collapsed Persons?
		// If have multiple nrads, need to find the most restrictive role-pair.
		// Version 1: just consider the originalNRAD roles
		ActivityRole fromRole = fromEntity.getOriginalNRAD().getRole();
		ActivityRole toRole = toEntity.getOriginalNRAD().getRole();
		double discount = getPairWeight(fromRole, toRole);
		return discount;
	}

	@Override
	public List<UserWeightSetting> getUserSettingsForWeight() {
		// We do not have this kind of UI
		return null;
	}

	@Override
	@XmlElement(name="matrixAxisValues")
	public List<String> getMatrixAxisValues() {
		ArrayList<String> strings = new ArrayList<String>();
		for(ActivityRole role:corpus.getActivityRoles()) {
			strings.add(role.getName());
		}
		return strings;
	}

	@XmlElement(name="matrixValues")
	public List<MatrixItemInfo> getMatrixValues() {
		ArrayList<MatrixItemInfo> values = new ArrayList<MatrixItemInfo>();
		for(String pairKey:rolePairWeights.keySet()) {
			String[] pair = pairKey.split("-");
			MatrixItemInfo item = new MatrixItemInfo(pair[1], pair[2], rolePairWeights.get(pairKey));
			values.add(item);
		}
		return values;
	}

	@XmlElement(name="matrixValues")
	public void setMatrixValues(List<MatrixItemInfo> values) {
		rolePairWeights.clear();
		for(MatrixItemInfo item:values) {
			String key = item.value1+"-"+item.value2;
			rolePairWeights.put(key, item.weight);
		}
	}

	/* gets the discount weight for a pair of Roles. 
	 */
	protected double getPairWeight(ActivityRole role1, ActivityRole role2) {
		//Normalize the pair based upon the IDs (original creation order);
		String key = getKeyForPair(role1, role2);
		Double weight = rolePairWeights.get(key);
		if(weight==null) {		// no value, so no discount
			return 1;
		} else {
			return weight.doubleValue();
		}
	}
	
	private String getKeyForPair(ActivityRole role1, ActivityRole role2) {
		//Normalize the pair based upon the IDs (original creation order);
		return (role1.getId()<role2.getId())?
				(role1.getName().toLowerCase()+"-"+role2.getName().toLowerCase())
				:(role2.getName().toLowerCase()+"-"+role1.getName().toLowerCase());
	}

	/* sets the discount weight for a pair of Roles, by name. 
	 * @param weight is interpreted as the value of a discount, so 0 precludes
	 * 		collapsing two persons within a document with this pair of roles, 
	 * 		and 1 means now discount is applied.
	 * (non-Javadoc)
	 * @see edu.berkeley.bps.services.workspace.collapser.CollapserRulePairMatrixUI#setPairWeight(java.lang.String, java.lang.String, double)
	 */
	@Override
	public void setPairWeight(String value1, String value2, double weight) {
		// Get the two roles, then set the weight in the pairs-Weights map
		ActivityRole role1 = corpus.findActivityRole(value1);
		if(role1==null)
			throw new IllegalArgumentException(myClass+
					".setPairWeight: Unrecognized role: "+value1);
		ActivityRole role2 = corpus.findActivityRole(value2);
		if(role2==null)
			throw new IllegalArgumentException(myClass+
					".setPairWeight: Unrecognized role: "+value2);
		if(weight<0||weight>1)
			throw new IllegalArgumentException(myClass+
					".setPairWeight: Illegal weight: "+weight);
		String key = getKeyForPair(role1, role2);
		rolePairWeights.put(key, weight);
	}

}
