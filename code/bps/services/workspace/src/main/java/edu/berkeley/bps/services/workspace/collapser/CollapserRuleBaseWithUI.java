package edu.berkeley.bps.services.workspace.collapser;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import edu.berkeley.bps.services.workspace.Entity;
import edu.berkeley.bps.services.workspace.Workspace;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class CollapserRuleBaseWithUI extends CollapserRuleBase
		implements CollapserRule, CollapserRuleUI {
	
	public static final Double WEIGHT_ALWAYS = 1.0;
	public static final Double WEIGHT_AGGRESSIVE = 0.7;
	public static final Double WEIGHT_CONSERVATIVE = 0.3;
	public static final Double WEIGHT_IGNORE = 0.0;

	public static final String LABEL_ALWAYS = "Always";
	public static final String LABEL_AGGRESSIVE = "Aggressive";
	public static final String LABEL_CONSERVATIVE = "Conservative";
	public static final String LABEL_IGNORE = "Never";

	@XmlElementWrapper(name = "userWeights")
	@XmlElement(name = "userWeight")
	protected List<UserWeightSetting> settingsList;
	
	public CollapserRuleBaseWithUI() {
		super();
		settingsList = new ArrayList<UserWeightSetting>();
	}
	
	public CollapserRuleBaseWithUI(int type, String name, String description, double weight, boolean intraDocument) {
		super(type, name, weight, intraDocument);
		this.description = description;
		settingsList = new ArrayList<UserWeightSetting>();
	}

	public void initSettings() {
		if(settingsList.isEmpty()) {
			settingsList.add(new UserWeightSetting(LABEL_ALWAYS, WEIGHT_ALWAYS)); 
			settingsList.add(new UserWeightSetting(LABEL_AGGRESSIVE, WEIGHT_AGGRESSIVE)); 
			settingsList.add(new UserWeightSetting(LABEL_CONSERVATIVE, WEIGHT_CONSERVATIVE)); 
			settingsList.add(new UserWeightSetting(LABEL_IGNORE, WEIGHT_IGNORE)); 
		}
	}
	
	@Override
	public void initialize(Workspace workspace) {
		super.initialize(workspace);
		initSettings();
	}

	public void initialize(Workspace workspace, boolean initUserSettings) {
		super.initialize(workspace);
		if(initUserSettings)
			initSettings();
	}


	@XmlElement
	protected String description;
	
	/**
	 * @return a user-readable description of this rule.
	 */
	@Override
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the user-readable description for this rule
	 * @param description
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Provides a list of names and values that can be used in a SELECT
	 * or equivalent list of options for the weight to use on a CollapserRule
	 * @return a list of named values for the weight of this rule,
	 * 			or null, if this rule UI does not support such a model. 
	 */
	@Override
	public List<UserWeightSetting> getUserSettingsForWeight() {
		return settingsList;
	}

	@Override
	public double evaluate(Entity fromEntity, Entity toEntity) {
		throw new RuntimeException("CollapserRuleBaseWithUI.evaluate should always be overridden");
	}


}
