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

$t->assign('page_title', 'Workspace-Document Details'.$CFG->page_title_default);
/**
	* If we add support to set document dates, control it with a block like this
$canUpdateWorkspace = false;
if(currUserHasPerm( 'WorkspaceUpdate' )) {
	$canUpdateWorkspace = true;
	$t->assign('canUpdateWorkspace', 1);
}
**/

$style_block = "<style>
td.title { border-bottom: 2px solid black; font-weight:bold; text-align:left; 
		font-style:italic; color:#777777; }
div.nrads_row  { padding:5px 0px 5px 0px; border-bottom: 1px solid black; }
td.document, td.nrad { font-weight:bold; }
p.nav-right { float:right; padding-top:10px;}
span.familyIndent { padding-right: 10px; }
span.nradLink { padding-left: 5px; font-weight:normal; }
span.nradLinkWeight { font-style:italic; }
</style>";

$t->assign("style_block", $style_block);

//$themebase = $CFG->wwwroot.'/themes/'.$CFG->theme;

$opmsg = false;

function getDocUrl($CFG,$wid,$did){
	return $CFG->wwwroot.$CFG->svcsbase."/workspaces/".$wid."/documents/".$did;
}

function getDocNRADsUrl($CFG,$wid,$did){
	return getDocUrl($CFG,$wid,$did)."/nrads";
}

function getWorkspaceMedianDocDate($CFG,$id){
	global $opmsg;

	$rest = new RESTclient();
	$url = $CFG->wwwroot.$CFG->svcsbase."/corpora/".$id;
	$rest->createRequest($url,"GET");
	// Get the results in JSON for easier manipulation
	$rest->setJSONMode();
	if($rest->sendRequest()) {
		$ServWkspOutput = $rest->getResponse();
		$result = json_decode($ServWkspOutput, true);
		$corpObj = &$result['workspace'];
		$workspaceMDD = $corpObj['medianDocDate'];
		unset($corpObj);
		return $workspaceMDD;
	} else if($rest->getStatus() == 404) {
		$opmsg = "Bad or illegal workspace specifier. ";
	} else {
		$opmsg = $rest->getError();
	}
	return false;
}

function getDocInfo($CFG,$wkspid,$docid){
	global $opmsg;

	$rest = new RESTclient();
	$url = getDocUrl($CFG,$wkspid,$docid);
	$rest->createRequest($url,"GET");
	// Get the results in JSON for easier manipulation
	$rest->setJSONMode();
	if($rest->sendRequest()) {
		$ServiceOutput = $rest->getResponse();
		$result = json_decode($ServiceOutput, true);
		$docObj = &$result['document'];
		$document = array(
			'id' => $docObj['id'],
			'alt_id' => $docObj['alt_id'], 
			'notes' => $docObj['notes'],
			'sourceURL' => $docObj['sourceURL'],
			'xml_id' => $docObj['xml_id'],
			'date_norm' => $docObj['dateValue'],
			'date_str' => $docObj['dateString'] );
		unset($docObj);
		return $document;
	} else if($rest->getStatus() == 404) {
		$opmsg = "Bad or illegal workspace or document specifier. ";
	} else {
		$opmsg = $rest->getError();
	}
	return false;
}

function getDocNRADs($CFG,$wid,$did,$linkListMap){
	global $opmsg;

	$rest = new RESTclient();
	$url = getDocNRADsUrl($CFG,$wid,$did);
	$rest->createRequest($url,"GET");
	// Get the results in JSON for easier manipulation
	$rest->setJSONMode();
	if($rest->sendRequest()) {
		$ServNRADsOutput = $rest->getResponse();
		$results = json_decode($ServNRADsOutput, true);
		$nrads = array();
		foreach($results as &$result) {
			$nradObj = &$result['nameRoleActivity'];
			$nradId = $nradObj['id'];
			$nrads[] = array(	
				'id' => $nradObj['id'],
			  'xmlId' => $nradObj['xmlID'],
			 	'nameId' => $nradObj['nameId'], 
			 	'name' => $nradObj['name'], 
			 	'normalNameId' => $nradObj['normalNameId'], 
			 	'normalName' => $nradObj['normalName'], 
			 	'activityRoleId' => $nradObj['activityRoleId'], 
			 	'activityRole' => $nradObj['activityRole'], 
			 	'activityRoleIsFamily' => ($nradObj['activityRoleIsFamily']=='true'), 
			 	'activityId' => $nradObj['activityId'], 
			 	'activity' => $nradObj['activity'],
			 	'links' => $linkListMap[$nradId]
			);
			// Supposed to help with efficiency (dangling refs?)
			unset($result);
			unset($nradObj);
		}
		// Supposed to help with efficiency (dangling refs?)
		unset($results);
		unset($rest);
		return $nrads;
	}
	$opmsg = $rest->getError();
	return false;
}

function getEntitiesForDocNRADs($CFG,$wid,$did){
	global $opmsg;

	$rest = new RESTclient();
	$url = getDocNRADsUrl($CFG,$wid,$did).'/links';
	$rest->createRequest($url,"GET");
	// Get the results in JSON for easier manipulation
	$rest->setJSONMode();
	if($rest->sendRequest()) {
		$ServLinksOutput = $rest->getResponse();
		$results = json_decode($ServLinksOutput, true);
		$linkListMap = array();
		foreach($results as &$result) {
			$entObj = &$result['nradToEntityLink'];
			$nradId = $entObj['nradId'];
			// Each map entry is a list of links for that nrad
			if(!isset($linkListMap[$nradId])) {
				$linkListMap[$nradId] = array();
			}
			// Create link info object and add to list
			$linkListMap[$nradId][] =
								array('linkType' => $entObj['type'],
											'weight' => (round($entObj['weight']*100)),
											'linkTo' => $entObj['linkTo'] ); 		// Entity displayName 
			// Supposed to help with efficiency (dangling refs?)
			unset($result);
			unset($entObj);
		}
		// Supposed to help with efficiency (dangling refs?)
		unset($results);
		unset($rest);
		// TODO create a function to sort on weight descending
		// Run through each list to sort.
		return $linkListMap;
	}
	$opmsg = $rest->getError();
	return false;
}

if(!(isset($_GET['wid'])&&isset($_GET['did']))) {
	$errmsg = "Missing workspace or document specifier(s).";
} else {
	$wid = $_GET['wid'];
	$did = $_GET['did'];
	$document = getDocInfo($CFG,$wid,$did);
	if($document){
		$t->assign('workspaceID', $_GET['wid']);
		if($document['date_norm']==0) {
			$document['date_str'] = '<em>('.getWorkspaceMedianDocDate($CFG,$wid).'?)</em>';
		}
		$t->assign('document', $document);
		$linkListMap = getEntitiesForDocNRADs($CFG,$wid,$did);
		if(!$linkListMap) {
			$errmsg = "Problem getting Document nrad-to-Entity links: ".$opmsg;
		} else {
			$nrads = getDocNRADs($CFG,$wid,$did, $linkListMap);
			if($nrads ) {
				$t->assign('nrads', $nrads);
			} else if($opmsg){
				$errmsg = "Problem getting Document Name-Role-Activity items: ".$opmsg;
			}
		}
	} else if($opmsg){
		$errmsg = "Problem getting Document Name-Role-Activity items: ".$opmsg;
	} else {
		$errmsg = "Bad or illegal workspace/document specifier. ";
	}
}


if($errmsg!="")
	$t->assign('errmsg', $errmsg);

$t->display('document.tpl');

?>

