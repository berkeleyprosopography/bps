<?php

/* Include Files *********************/
require_once("../../libs/env.php");
require_once("../admin/authUtils.php");
require_once "../../libs/RESTClient.php";
/*************************************/

// If the user isn't logged in, send to the login page.
// FIXME - allow not logged in state - will be all no-edit
if(($login_state != BPS_LOGGED_IN) && ($login_state != BPS_REG_PENDING)){
	header( 'Location: ' . $CFG->wwwroot .
					'/login?redir=' .$_SERVER['REQUEST_URI'] );
	die();
}

//$t->debugging = true;

$t->assign('page_title', $CFG->page_title_default.': Workspace Parameter Settings');

$style_block = "<style>
</style>";

$t->assign("style_block", $style_block);

// $themebase = $CFG->wwwroot.'/themes/'.$CFG->theme;
unset($errmsg);

// Need this for the sub-menu to work correctly.
$t->assign("currSubNav", 'params');

if(!isset($_GET['wid'])) {
	$errmsg = "Internal error - No Workspace ID specified. Please report this to your BPS admin.";
} else {
	$wid = $_GET['wid'];
	$widSpec = '&wid='.$wid;
}

$script_block = '
<script type="text/javascript" src="/scripts/setupXMLHttpObj.js"></script>
<script type="text/javascript" src="/scripts/DOMUtils.js"></script>
<script>

// The ready state change callback method for update.
function updateGenParamsRSC() {
  if (xmlhttp.readyState==4) {
		if( xmlhttp.status == 200 ) {
			// Maybe this should change the cursor or something
			setStatusP("Workspace params updated.");
	    //alert( "Response: " + xmlhttp.status + " Body: " + xmlhttp.responseText );
		} else {
			alert( "Error encountered when trying to update workspace params.\nResponse: "
			 				+ xmlhttp.status + "\nBody: " + xmlhttp.responseText );
		}
	}
}


function updateGenParams(workspaceID, ALifeElName, GenSepElName) {
	var ALife = getNumberFromInput( ALifeElName, 10, 100, "an active life duration" );
	var GenSep = getNumberFromInput( GenSepElName, 10, 100, "a generation separation" );
	if( isNaN(ALife) || isNaN(GenSep))
		return;

	if( !xmlhttp ) {
		alert( "Cannot update workspace - no http obj!\n Please advise BPS support." );
		return;
	}
	var url = "'.$CFG->svcsbase.'/workspaces/"+workspaceID+"/params?life="+ALife+"&gen="+GenSep;
	// alert( "Preparing request: PUT: "+url);
	xmlhttp.open("PUT", url, true);
	xmlhttp.onreadystatechange=updateGenParamsRSC;
	xmlhttp.send();
	enableElement( "U_gen", false );
}

// The ready state change callback method for update.
function updateSimpleRuleWeightRSC() {
  if (xmlhttp.readyState==4) {
		if( xmlhttp.status == 200 ) {
			// Maybe this should change the cursor or something
			setStatusP("Workspace Collapser Rule weight updated.");
	    //alert( "Response: " + xmlhttp.status + " Body: " + xmlhttp.responseText );
		} else {
			alert( "Error encountered when trying to update Workspace Collapser Rule weight.\nResponse: "
			 				+ xmlhttp.status + "\nBody: " + xmlhttp.responseText );
		}
	}
}


function updateSimpleRuleWeight(workspaceID, RuleName, Weight) {
	var weightVal = checkNumberRange( Weight, 0, 1.0, "a collapser rule weight" );
	if( isNaN(weightVal))
		return;

	if( !xmlhttp ) {
		alert( "Cannot update Workspace Collapser Rule - no http obj!\n Please advise BPS support." );
		return;
	}
	var url = "'.$CFG->svcsbase.'/workspaces/"+workspaceID+"/collapserrule?name="+RuleName+"&weight="+weightVal;
	// alert( "Preparing request: PUT: "+url);
	xmlhttp.open("PUT", url, true);
	xmlhttp.onreadystatechange=updateSimpleRuleWeightRSC;
	xmlhttp.send();
}

// The ready state change callback method for update.
function updateMatrixRuleWeightRSC() {
  if (xmlhttp.readyState==4) {
		if( xmlhttp.status == 200 ) {
			// Maybe this should change the cursor or something
			setStatusP("Workspace Collapser Rule weight updated.");
	    //alert( "Response: " + xmlhttp.status + " Body: " + xmlhttp.responseText );
		} else {
			alert( "Error encountered when trying to update Workspace Collapser Rule weight.\nResponse: "
			 				+ xmlhttp.status + "\nBody: " + xmlhttp.responseText );
		}
	}
}


function updateMatrixRuleWeight(workspaceID, RuleName, row, col, Weight) {
	var weightVal = checkNumberRange( Weight, 0, 1.0, "a collapser rule weight" );
	if( isNaN(weightVal))
		return;

	if( !xmlhttp ) {
		alert( "Cannot update Workspace Collapser Rule - no http obj!\n Please advise BPS support." );
		return;
	}
	var url = "'.$CFG->svcsbase.'/workspaces/"+workspaceID+"/collapserrule?name="+RuleName+"&weight="+weightVal
				+"&row="+row+"&col="+col;
	// alert( "Preparing request: PUT: "+url);
	xmlhttp.open("PUT", url, true);
	xmlhttp.onreadystatechange=updateMatrixRuleWeightRSC;
	xmlhttp.send();
}

function setStatusP(str) {
	var el = document.getElementById("statusP");
	if(el != null)
		el.innerHTML = str;
}

</script>
';

$t->assign("script_block", $script_block);

$opmsg = false;

function getWorkspace($CFG,$wkspid){
	global $opmsg;

	$rest = new RESTclient();
	$url = $CFG->serverwwwroot.$CFG->svcsbase."/workspaces/".$wkspid;
	$rest->createRequest($url,"GET");
	// Get the results in JSON for easier manipulation
	$rest->setJSONMode();
	if($rest->sendRequest()) {
		$ServWkspOutput = $rest->getResponse();
		unset($result);
		if(isset($wkspid)) {
			$result = json_decode($ServWkspOutput, true);
		} else {
			$results = json_decode($ServWkspOutput, true);
			if(count($results) <= 0) {
				$opmsg = "Current user has no workspace(s).";
			} else {
				$result = current($results);
			}
		}
		if(isset($result)) {
			$wkspObj = &$result['workspace'];
			$workspace = array(
				'id' => $wkspObj['id'],
				'name' => isset($wkspObj['name'])?($wkspObj['name']):null, 
				'description' => isset($wkspObj['description'])?($wkspObj['description']):null,
				'importedCorpusId' => isset($wkspObj['builtFromCorpus'])?($wkspObj['builtFromCorpus']):null,
				'importedCorpusName' => isset($wkspObj['importedCorpusName'])?($wkspObj['importedCorpusName']):null,
				'generationOffset' => isset($wkspObj['generationOffset'])?($wkspObj['generationOffset']):null,
				'activeLife' => isset($wkspObj['activeLife'])?($wkspObj['activeLife']):null,
				'medianDocDate' => isset($wkspObj['medianDocDate'])?($wkspObj['medianDocDate']):null			);
			// Look for trailing "(Clone)" and trim it
			if((strlen($workspace['importedCorpusName']) > 7 ) 
				&& 0==substr_compare($workspace['importedCorpusName'], '(Clone)', -7, 7)) {
					$workspace['importedCorpusName'] = 
						substr($workspace['importedCorpusName'], 0, -7);
			}
			unset($wkspObj);
			return $workspace;
		}
	} else if($rest->getStatus() == 404) {
		$opmsg = "Bad or illegal workspace specifier. ";
	} else {
		$opmsg = $rest->getError();
	}
	return false;
}


function addRuleToGroup( &$groupSet,$rule ) {
	foreach( $groupSet as &$group ) {
		if($rule['uiGroupName'] == $group['name']) {
			$group['rules'][] = $rule;
			return true;
		}
	}
	return false;
}
function processRuleSet(&$ruleSet, &$groupset ) {
	global $opmsg;

	// Handle the vagaries of json parsing for rules
	// If we have an array of Rule objects, then ruleSet is just an Array
	if(isset($ruleSet['Rule']) && !isset($ruleSet['Rule']['name'])) {					// Array of Rules
		foreach($ruleSet['Rule'] as &$ruleObj) {
			$lastRule = print_r($ruleObj, true);
			$newRule = processRule($ruleObj);
			if(!$newRule) {
				$opmsg .= " (array case) \n".$lastRule;
				return false;
			}
			if(!addRuleToGroup( $groupset, $newRule )) {
				$opmsg = "UI Rule: ".$newRule['name'].
									" with unknown uiGroupName: ".$newRule['uiGroupName'].
									" passed back in collapser.\n".$lastRule;
				return false;
			}
		}
	} else if(isset($ruleSet['Rule']['name'])) {
			$lastRule = print_r($ruleSet['Rule'], true);
			$newRule = processRule($ruleSet['Rule']);
			if(!$newRule) {
				$opmsg .= " (object case) \n".$lastRule;
				return false;
			}
			if(!addRuleToGroup( $groupset, $newRule )) {
				$opmsg = "UI Rule: ".$newRule['name'].
									" with unknown uiGroupName: ".$newRule['uiGroupName'].
									" passed back in collapser.\n".$lastRule;
				return false;
			}
	} else {
		// Empty rule set - just do nothing
	} 
	return true;
}

function processRule(&$ruleObj ) {
	global $opmsg;
	if(!isset($ruleObj['name'])) {
		$opmsg = "UI Rule with no name passed back in collapser. \n".print_r($ruleObj,true);
		return false;
	}
	if(!isset($ruleObj['uiGroupName'])) {
		$opmsg = "UI Rule with no uiGroupName passed back in collapser. ";
		return false;
	}
	$newRule = array(
		'name' => $ruleObj['name'], 
		'uiGroupName' => $ruleObj['uiGroupName'], 
		'weight' => isset($ruleObj['weight'])?($ruleObj['weight']):"1",
		'description' => isset($ruleObj['description'])?($ruleObj['description']):"(missing description)" );
	if(isset($ruleObj['userWeights'])) {
		$newRule['userWeights'] = array();
		foreach($ruleObj['userWeights']['userWeight'] as &$uw) {
			if(!isset($uw['label'])) {
				$opmsg = "UI Rule: ".$newRule['name'].
							" has a userWeight with no label (passed back in collapser). ";
				return false;
			}
			$newRule['userWeights'][]= array(
				'label' => $uw['label'], 
				'weight' => isset($uw['weight'])?($uw['weight']):"1",
				);
		}
	}
	if(isset($ruleObj['matrixAxisValues'])) {
		$newRule['matrixAxisValues'] = array();
		$newRule['matrixAxisValuesLower'] = array();
		foreach($ruleObj['matrixAxisValues']['axisValue'] as &$axis) {
			$newRule['matrixAxisValues'][] = $axis;
			$newRule['matrixAxisValuesLower'][] = strtolower($axis);
		}
		if(isset($ruleObj['matrixItems'])) {
			$matrixItemsArray = array();
			foreach($ruleObj['matrixItems']['matrixItemInfo'] as &$axis) {
				if(!isset($axis['row']) || !isset($axis['col']) || !isset($axis['weight'])) {
					$opmsg = "UI Rule: ".$newRule['name'].
								" has a matrix Item that is missing values (passed back in collapser). ";
					return false;
				}
				$matrixItemsArray[$axis['row']][$axis['col']] = $axis['weight'];
			}
			$newRule['matrixItems'] = $matrixItemsArray;
		} else {
			$opmsg = "UI Rule: ".$newRule['name']." has matrixAxisValues with no matrixItems! ";
			return false;
		}
	}
	return $newRule;
}

function getCollapser($CFG,$wkspid){
	global $opmsg;

	$rest = new RESTclient();
	$url = $CFG->serverwwwroot.$CFG->svcsbase."/workspaces/".$wkspid."/collapser";
	$rest->createRequest($url,"GET");
	// Get the results in JSON for easier manipulation
	$rest->setJSONMode();
	if($rest->sendRequest()) {
		$ServClpsrOutput = $rest->getResponse();
		unset($result);
		$result = json_decode($ServClpsrOutput, true);
		if(isset($result)) {
			$clpsrObj = &$result['personCollapser'];
			$collapser = array();
			// First, collect the uiGroups, into arrays by type
			if(isset($clpsrObj['uiGroups'])) {
				$collapser['intra_groups'] = array();
				$collapser['inter_groups'] = array();
				foreach($clpsrObj['uiGroups']['uiGroup'] as &$uigObj) {
					//$uigObj = &$uig['uiGroup'];
					if(!isset($uigObj['name'])) {
						$opmsg = "UI Group with no name passed back in collapser. ";
						return false;
					}
					$newGroup = array(
						'name' => $uigObj['name'], 
						'header' => isset($uigObj['header'])?($uigObj['header']):null,
						'rules' => array() );
					if($uigObj['intraDocument'] == 'true') {
						$collapser['intra_groups'][] = $newGroup;
					} else {
						$collapser['inter_groups'][] = $newGroup;
					}
				}
			}
			// Next, collect the rules, by section, adding them to the appropriate group
			if(isset($clpsrObj['allIntraDocRules'])) {
				$ruleSet = &$clpsrObj['allIntraDocRules'];
				if(!processRuleSet($ruleSet, $collapser['intra_groups'] ))
					return false;
			}
			if(isset($clpsrObj['allCorpusWideRules'])) {
				$ruleSet = &$clpsrObj['allCorpusWideRules'];
				if(!processRuleSet($ruleSet, $collapser['inter_groups'] ))
					return false;
			}

			unset($clpsrObj);
			return $collapser;
		}
	} else if($rest->getStatus() == 404) {
		$opmsg = "Bad or illegal workspace specifier. ";
	} else {
		$opmsg = $rest->getError();
	}
	return false;
}


$user_id = getCurrUser();

if(!isset($user_id)) {
	$errmsg = "You must be logged in to access your workspace(s).";
} else if(!isset($errmsg)) {
	$workspace = getWorkspace($CFG,$wid);
	if(!$workspace) {
		if($opmsg){
			$errmsg = "Problem getting Workspace details: ".$opmsg;
		} else {
			$errmsg = "Bad or illegal Workspace specifier. ";
		}
	} else {
		$t->assign('workspace', $workspace);
		$t->assign('wkspId', $wid);
		$collapser = getCollapser($CFG,$wid);
		if(!$collapser) {
			if($opmsg){
				$errmsg = "Problem getting Collapser details: ".$opmsg;
			} else {
				$errmsg = "Bad or illegal Collapser specifier. ";
			}
		} else {
			$t->assign('collapser', $collapser);
		}
	}
}


if(isset($errmsg)) {
	if(!$workspace || !$collapser) {
		$t->assign('heading', 'Cannot show workspace collapser settings');
		$t->assign('message', $errmsg);
		$t->display('error.tpl');
		die();
	} else {
		$t->assign('errmsg', $errmsg);
	}
}

$t->display('workspace_params.tpl');

?>























