package edu.berkeley.bps.services.workspace.collapser;

import java.util.List;

public interface CollapserRulePairMatrixUI extends CollapserRuleUI {

	/**
	 * @return the list of values that are paired to construct the
	 * matrix. A sparse matrix is built so that each pair is considered. 
	 * The list of List<UserWeightSetting> return by getUserSettings
	 * are used for each cell in the matrix.
	 */
	public List<String> getMatrixAxisValues();

	public void setPairWeight(String value1, String value2, double weight);
}
