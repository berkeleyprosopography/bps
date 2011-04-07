package edu.berkeley.bps.services.workspace.collapser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.berkeley.bps.services.common.utils.Pair;
import edu.berkeley.bps.services.corpus.ActivityRole;
import edu.berkeley.bps.services.corpus.Corpus;
import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Workspace;

public class RoleMatrixDiscountRule extends CollapserRuleBaseWithUI 
		implements CollapserRule, CollapserRuleUI, CollapserRulePairMatrixUI{
	private final static String myClass = "RoleMatrixDiscountRule";

	protected List<ActivityRole> corpusRoles = null;
	
	protected HashMap<String, ActivityRole> nameToRoleMap = null;
	
	protected HashMap<String, Double> rolePairWeights = null;
	
	public RoleMatrixDiscountRule() {
		this(myClass);
	}

	public RoleMatrixDiscountRule(String name) {
		super(CollapserRule.DISCOUNT_RULE, name, 1.0, 
					CollapserRule.WITHIN_DOCUMENTS);
	}

	
	@Override
	public void initialize(Workspace workspace) {
		// Get the roles from the workspace, and build the derived Pairs
		super.initialize(workspace);
		
		corpusRoles = null;
		Corpus corpus = workspace.getCorpus();
		if(corpus==null)
			return;			// Nothing to do
		corpusRoles = corpus.getActivityRoles();
		if(corpusRoles==null||corpusRoles.isEmpty())
			return;			// Nothing to do
		// Sort the corpusRoles by ID (creation order)
		Collections.sort(corpusRoles, new ActivityRole.IdComparator());
		// We have roles, so now we build role-pairs with the weights
		nameToRoleMap = new HashMap<String, ActivityRole>();
		for(ActivityRole role:corpusRoles) {
			nameToRoleMap.put(role.getName(), role);
		}
		hackInInitialHBTINValues();
	}
	
	// HACK - this should be configured,
	private void hackInInitialHBTINValues() {
		// Principle, Witness, Father, Mother, Grandfather, Ancestor
		// All the family roles can combine with anything, so we will skip them, 
		// and just put in the conflicts between Principle and Witness.
		setPairWeight("Principle", "Witness", 0);
		setPairWeight("Witness", "Witness", 0);
	}

	@Override
	public double evaluate(Entity fromEntity, Entity toEntity) {
		if(corpusRoles==null||corpusRoles.isEmpty())
			return -1.0;	// No way to match the context;
		// Find the NRADS tied to these Persons, and then get
		// the discount weight for the pair of roles. 
		// Is this going to cause grief with partially collapsed Persons?
		// If have multiple nrads, need to find the most restrictive role-pair.
		// Version 1: just consider the originalNRAD roles
		ActivityRole fromRole = fromEntity.getOriginalNRAD().getRole();
		ActivityRole toRole = fromEntity.getOriginalNRAD().getRole();
		double discount = getPairWeight(fromRole, toRole);
		return discount;
	}

	@Override
	public List<UserWeightSetting> getUserSettingsForWeight() {
		// We do not have this kind of UI
		return null;
	}

	@Override
	public List<String> getMatrixAxisValues() {
		// TODO Auto-generated method stub
		ArrayList<String> strings = new ArrayList<String>();
		for(ActivityRole role:corpusRoles) {
			strings.add(role.getName());
		}
		return strings;
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
				(role1.getName()+"-"+role2.getName()):(role2.getName()+"-"+role1.getName());
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
		ActivityRole role1 = nameToRoleMap.get(value1);
		if(role1==null)
			throw new IllegalArgumentException(myClass+
					".setPairWeight: Unrecognized role: "+value1);
		ActivityRole role2 = nameToRoleMap.get(value2);
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
