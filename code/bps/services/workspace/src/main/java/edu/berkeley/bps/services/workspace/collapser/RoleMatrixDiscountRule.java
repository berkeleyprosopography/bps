package edu.berkeley.bps.services.workspace.collapser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.berkeley.bps.services.corpus.ActivityRole;
import edu.berkeley.bps.services.corpus.Corpus;
import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Workspace;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "Rule")
public class RoleMatrixDiscountRule extends CollapserRuleBaseWithUI 
		implements CollapserRule, CollapserRuleUI, CollapserRulePairMatrixUI{
	static final Logger logger = LoggerFactory.getLogger(RoleMatrixDiscountRule.class);
	
	private final static String myClass = "RoleMatrixDiscountRule";
	private static final String DESCRIPTION = 
			"Can two instances of the same name within a document possibly be the same,"
			+" just given the associated roles for the two names?";
	private static final String UIGROUP_INTRA = "Step1C";
	private static final String UIGROUP_INTER = "Step2C";

	protected Corpus corpus = null;

	protected HashMap<String, Double> rolePairWeights = new HashMap<String, Double>();
	
	public RoleMatrixDiscountRule() {
		this(WEIGHT_ALWAYS, WITHIN_DOCUMENTS);
	}

	public RoleMatrixDiscountRule(double weight, boolean intraDocument) {
		super(CollapserRule.DISCOUNT_RULE, ComputeDefaultName(myClass,intraDocument), DESCRIPTION, (intraDocument?UIGROUP_INTRA:UIGROUP_INTER), 
				1.0, CollapserRule.WITHIN_DOCUMENTS);
	}

	public static final String LABEL_YES = "Yes";
	public static final String LABEL_MAYBE = "Maybe";
	public static final String LABEL_NO = "No";
	
	@Override
	public void initialize(Workspace workspace) {
		// Set our own labels, to preclude superclass defaults
		if(settingsList.isEmpty()) {
			settingsList.add(new UserWeightSetting(LABEL_YES, WEIGHT_ALWAYS)); 
			settingsList.add(new UserWeightSetting(LABEL_MAYBE, WEIGHT_MAYBE)); 
			settingsList.add(new UserWeightSetting(LABEL_NO, WEIGHT_IGNORE)); 
		}
		super.initialize(workspace, true);
		
		corpus = workspace.getCorpus();
		if(corpus==null) {
			logger.error("{}.initialize(): No corpus found for workspace: {}", 
					this.getClass().getName(), workspace.getId());
			return;			// Nothing to do
		}
		// Skip all the family roles, but set up pairs for the others.
		List<ActivityRole> corpusRoles = corpus.getActivityRoles(true);
		if(corpusRoles==null||corpusRoles.isEmpty()) {
			logger.error("{}.initialize(): No roles found in corpus: ", 
					this.getClass().getName(), corpus.getId());
			return;			// Nothing to do
		}
		//TODO - initialize the UI to allow all role interactions until user says otherwise
		// We should consider all the roles, ignore the family ones, and
		// try to find witness to exclude. That is a hack...
		for(int i=0; i<corpusRoles.size(); i++ ) {
			// For each activity role in the list
			ActivityRole ar_i = corpusRoles.get(i);
			// Pair up the first activity role with itself, and all later ones in the list
			for(int j=i; j<corpusRoles.size(); j++ ) {
				ActivityRole ar_j = corpusRoles.get(j);
				setPairWeight(ar_i, ar_j, WEIGHT_ALWAYS);	// Until we know otherwise, assume all good
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

	/*
	@Override
	public List<UserWeightSetting> getUserSettingsForWeight() {
		// Use those from Base - the list applies to each cell in the matrix 
		return null;
	}
	*/

	@Override
	@XmlElementWrapper(name="matrixAxisValues")
	@XmlElement(name = "axisValue")
	public List<String> getMatrixAxisValues() {
		ArrayList<String> strings = new ArrayList<String>();
		for(ActivityRole role:corpus.getActivityRoles(true)) { // Note that we skip the family roles
			strings.add(role.getName());
		}
		return strings;
	}

	// TODO This should be refactored into a base class so other matrix rules can share it
	@XmlElementWrapper(name="matrixItems")
	@XmlElement(name = "matrixItemInfo")
	public List<MatrixItemInfo> getMatrixValues() {
		ArrayList<MatrixItemInfo> values = new ArrayList<MatrixItemInfo>();
		for(String pairKey:rolePairWeights.keySet()) {
			String[] pair = getRowColFromKey(pairKey);
			MatrixItemInfo item = new MatrixItemInfo(pair[0], pair[1], rolePairWeights.get(pairKey));
			values.add(item);
		}
		return values;
	}

	// @XmlElement(name="matrixValues")
	public void setMatrixValues(List<MatrixItemInfo> values) {
		rolePairWeights.clear();
		for(MatrixItemInfo item:values) {
			String key = getKeyForRowCol(item.getRow(), item.getCol());
			// Does not assert the roles, as this comes from persistence
			rolePairWeights.put(key, item.getWeight());
		}
	}
	
	public void setMatrixValue(MatrixItemInfo item) {
		String key = getKeyForRowCol(item.getRow(), item.getCol());
		rolePairWeights.put(key, item.getWeight());
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
	
	// TODO This should be refactored into a base class so other matrix rules can share it
	private static final ActivityRole.RoleRankComparator roleRankCmp = new ActivityRole.RoleRankComparator();

	private static String getKeyForPair(ActivityRole role1, ActivityRole role2) {
		// Normalize the pair based upon the roleRanks
		return (roleRankCmp.compare(role1, role2)<0)?
				getKeyForRowCol(role1.getName().toLowerCase(), role2.getName().toLowerCase())
				:getKeyForRowCol(role2.getName().toLowerCase(), role1.getName().toLowerCase());
	}

	public static String getKeyForRowCol(String rowName, String colName ) {
		//Normalize the pair based upon the IDs (original creation order);
		return rowName+ROW_COL_KEY_SEP+colName;
	}

	public static String[] getRowColFromKey(String pairKey) {
		//Normalize the pair based upon the IDs (original creation order);
		return pairKey.split(ROW_COL_KEY_SEP_REGEX);
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
		setPairWeight(role1, role2, weight);
	}

	public void setPairWeight(ActivityRole role1, ActivityRole role2, double weight) {
		if(weight<0||weight>1)
			throw new IllegalArgumentException(myClass+
					".setPairWeight: Illegal weight: "+weight);
		String key = getKeyForPair(role1, role2);
		rolePairWeights.put(key, weight);
	}

}
