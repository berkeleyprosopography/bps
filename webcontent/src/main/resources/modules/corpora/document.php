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

$t->assign('page_title', 'Corpus-Document Details'.$CFG->page_title_default);
/**
	* If we add support to set document dates, control it with a block like this
$canUpdateCorpus = false;
if(currUserHasPerm( 'CorpusUpdate' )) {
	$canUpdateCorpus = true;
	$t->assign('canUpdateCorpus', 1);
}
**/

$style_block = "<style>
td.title { border-bottom: 2px solid black; font-weight:bold; text-align:left; 
		font-style:italic; color:#777777; }
div.nrads_row  { padding:5px 0px 5px 0px; border-bottom: 1px solid black; }
td.document, td.nrad { font-weight:bold; }
p.nav-right { float:right; padding-top:10px;}
span.familyIndent { padding-right: 10px; }
</style>";

$t->assign("style_block", $style_block);

//$themebase = $CFG->wwwroot.'/themes/'.$CFG->theme;

$opmsg = false;
unset($errmsg);

function getDocUrl($CFG,$cid,$did){
	return $CFG->serverwwwroot.$CFG->svcsbase."/corpora/".$cid."/documents/".$did;
}

function getDocNRADsUrl($CFG,$cid,$did){
	return getDocUrl($CFG,$cid,$did)."/nrads";
}

function getCorpusMediaDocDate($CFG,$id){
	global $opmsg;

	$rest = new RESTclient();
	$url = $CFG->serverwwwroot.$CFG->svcsbase."/corpora/".$id;
	$rest->createRequest($url,"GET");
	// Get the results in JSON for easier manipulation
	$rest->setJSONMode();
	if($rest->sendRequest()) {
		$ServCorpOutput = $rest->getResponse();
		$result = json_decode($ServCorpOutput, true);
		$corpObj = &$result['corpus'];
		$corpusMDD = $corpObj['medianDocDate'];
		unset($corpObj);
		return $corpusMDD;
	} else if($rest->getStatus() == 404) {
		$opmsg = "Bad or illegal corpus specifier. ";
	} else {
		$opmsg = $rest->getError();
	}
	return false;
}

function getDocInfo($CFG,$corpid,$docid){
	global $opmsg;

	$rest = new RESTclient();
	$url = getDocUrl($CFG,$corpid,$docid);
	$rest->createRequest($url,"GET");
	// Get the results in JSON for easier manipulation
	$rest->setJSONMode();
	if($rest->sendRequest()) {
		$ServiceOutput = $rest->getResponse();
		$result = json_decode($ServiceOutput, true);
		$docObj = &$result['document'];
		$document = array(
			'id' => $docObj['id'],
			'alt_id' => isset($docObj['alt_id'])?($docObj['alt_id']):null, 
			'primaryPubl' => isset($docObj['primaryPubl'])?($docObj['primaryPubl']):null,
			'notes' => isset($docObj['notes'])?($docObj['notes']):null,
			'sourceURL' => isset($docObj['sourceURL'])?($docObj['sourceURL']):null,
			'xml_id' => isset($docObj['xml_id'])?($docObj['xml_id']):null,
			'date_norm' => isset($docObj['dateValue'])?($docObj['dateValue']):null,
			'date_str' => isset($docObj['dateString'])?($docObj['dateString']):null );
		unset($docObj);
		return $document;
	} else if($rest->getStatus() == 404) {
		$opmsg = "Bad or illegal corpus or document specifier. ";
	} else {
		$opmsg = $rest->getError();
	}
	return false;
}

function getDocNRADs($CFG,$cid,$did){
	global $opmsg;

	$rest = new RESTclient();
	$url = getDocNRADsUrl($CFG,$cid,$did);
	$rest->createRequest($url,"GET");
	// Get the results in JSON for easier manipulation
	$rest->setJSONMode();
	if($rest->sendRequest()) {
		$ServCorpDocsOutput = $rest->getResponse();
		$results = json_decode($ServCorpDocsOutput, true);
		$nrads = array();
		foreach($results as &$result) {
			$nradObj = &$result['nameRoleActivity'];
			$nrad = array(	
				'id' => $nradObj['id'],
				'xmlId' => isset($nradObj['xmlID'])?($nradObj['xmlID']):null,
				'nameId' => isset($nradObj['nameId'])?($nradObj['nameId']):null, 
				'name' => isset($nradObj['name'])?($nradObj['name']):null, 
				'normalNameId' => isset($nradObj['normalNameId'])?($nradObj['normalNameId']):null, 
				'normalName' => isset($nradObj['normalName'])?($nradObj['normalName']):null, 
				'activityRoleId' => isset($nradObj['activityRoleId'])?($nradObj['activityRoleId']):null, 
				'activityRole' => isset($nradObj['activityRole'])?($nradObj['activityRole']):null, 
				'activityRoleIsFamily' => (isset($nradObj['activityRoleIsFamily'])
																	&&($nradObj['activityRoleIsFamily']=='true')), 
				'activityId' => isset($nradObj['activityId'])?($nradObj['activityId']):null, 
				'activity' => isset($nradObj['activity'])?($nradObj['activity']):null 
			);
			array_push($nrads, $nrad);
			// Supposed to help with efficiency (dangling refs?)
			unset($results);
			unset($result);
			unset($nradObj);
			unset($rest);
		}
		return $nrads;
	}
	$opmsg = $rest->getError();
	return false;
}

if(!(isset($_GET['cid'])&&isset($_GET['did']))) {
	$errmsg = "Missing corpus or document specifier(s).";
} else {
	$document = getDocInfo($CFG,$_GET['cid'],$_GET['did']);
	if($document){
		$t->assign('corpusID', $_GET['cid']);
		if($document['date_norm']==0) {
			$document['date_str'] = '<em>('.getCorpusMediaDocDate($CFG,$_GET['cid']).'?)</em>';
		}
		$t->assign('document', $document);
		$nrads = getDocNRADs($CFG,$_GET['cid'],$_GET['did']);
		if($nrads) {
			$t->assign('nrads', $nrads);
		} else if($opmsg){
			$errmsg = "Problem getting Document Name-Role-Activity items: ".$opmsg;
		}
	} else if($opmsg){
		$errmsg = "Problem getting Document Name-Role-Activity items: ".$opmsg;
	} else {
		$errmsg = "Bad or illegal corpus/document specifier. ";
	}
}


if(isset($errmsg))
	$t->assign('errmsg', $errmsg);

$t->display('document.tpl');

?>

