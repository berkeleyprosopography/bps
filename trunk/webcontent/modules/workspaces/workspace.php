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

$t->assign('page_title', 'Corpus Details'.$CFG->page_title_default);

$style_block = "<style>
td.title { border-bottom: 2px solid black; font-weight:bold; text-align:left; 
		font-style:italic; color:#777777; }
td.corpus_label { font-weight:bold; color:#61615f; }
td.workspacename { font-weight:bold; }
td.workspacedesc p { font-weight:bold; }
td.corpusX { border-bottom: 1px solid black; }
td.workspacedesc textarea { font-family: Arial, Helvetica, sans-serif; padding:2px;}
td.workspacendocs { text-align:center; padding-right:10px;}
form.form_row  { padding:0px; margin:2px;}
div.form_row  { padding:5px 0px 5px 0px; border-bottom: 1px solid black; }
div.docs_row  { padding:5px 0px 5px 0px; border-bottom: 1px solid black; }
td.document { font-weight:bold; }
</style>";

$t->assign("style_block", $style_block);

$themebase = $CFG->wwwroot.'/themes/'.$CFG->theme;

$script_block = '
<script type="text/javascript" src="/scripts/setupXMLHttpObj.js"></script>
<script>

</script>
';

$t->assign("script_block", $script_block);

$opmsg = false;

function getWorkspace($CFG,$user_id){
	global $opmsg;

	$rest = new RESTclient();
	$url = $CFG->wwwroot.$CFG->svcsbase."/workspaces?user=".$user_id;
	$rest->createRequest($url,"GET");
	// Get the results in JSON for easier manipulation
	$rest->setJSONMode();
	if($rest->sendRequest()) {
		$ServWkspOutput = $rest->getResponse();
		$results = json_decode($ServWkspOutput, true);
		if(count($results) <= 0) {
			$opmsg = "Current user has no workspace(s).";
		} else {
			$result = current($results);
			$wkspObj = &$result['workspace'];
			$workspace = array(
				'id' => $wkspObj['id'],
				'name' => $wkspObj['name'], 
				'description' => $wkspObj['description'],
				'importedCorpus' => $wkspObj['importedCorpus'],
				'medianDocDate' => $wkspObj['medianDocDate']
			);
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

function getWorkspaceDocs($CFG,$id,$medianDocDate) {
	global $opmsg;

	$rest = new RESTclient();
	$url = $CFG->wwwroot.$CFG->svcsbase."/workspaces/".$id."/documents";
	$rest->createRequest($url,"GET");
	// Get the results in JSON for easier manipulation
	$rest->setJSONMode();
	if($rest->sendRequest()) {
		$ServWkspDocsOutput = $rest->getResponse();
		$results = json_decode($ServWkspDocsOutput, true);
		$documents = array();
		foreach($results as &$result) {
			$docObj = &$result['document'];
			$docDate = ($docObj['dateString']==0)?$medianDocDate:$docObj['dateString'];
			$document = array(	'id' => $docObj['id'], 'alt_id' => $docObj['alt_id'], 
				'notes' => $docObj['notes'], 'sourceURL' => $docObj['sourceURL'],
				'xml_id' => $docObj['xml_id'], 'date_str' => $docDate
			);
			array_push($documents, $document);
			// Supposed to help with efficiency (dangling refs?)
			unset($result);
			unset($docObj);
		}
		return $documents;
	}
	$opmsg = $rest->getError();
	return false;
}

function getCorpora($CFG) {
	global $opmsg;
	
	$rest = new RESTclient();
	$url = $CFG->wwwroot.$CFG->svcsbase."/corpora/";
	$rest->createRequest($url,"GET");
	// Get the results in JSON for easier manipulation
	$rest->setJSONMode();
	$documents;
	if($rest->sendRequest()) {
		$ServCorpOutput = $rest->getResponse();
		$results = json_decode($ServCorpOutput, true);
		$corpora = array();
		foreach($results as &$result) {
			$corpObj = &$result['corpus'];
			$corpus = array(	'id' => $corpObj['id'], 'name' => $corpObj['name'], 
						'nDocs' => $corpObj['ndocs'], 'description' => $corpObj['description']);
			array_push($corpora, $corpus);
			// Supposed to help with efficiency (dangling refs?)
			unset($result);
			unset($corpObj);
		}
		return $corpora;
	} else {
		$opmsg = $rest->getError();
	}
}

$user_id = getCurrUser();

if(!isset($user_id)) {
	$errmsg = "You must be logged in to access your workspace(s).";
} else {
	$workspace = getWorkspace($CFG,$user_id);
	if($workspace && isset($workspace['importedCorpus'])){
		$workspace['nDocs'] = 0;
		$docs = getWorkspaceDocs($CFG,$workspace['id'], '<em>('.$workspace['medianDocDate'].'?)</em>');
		if($docs) {
			$workspace['nDocs'] = count($docs);
			$t->assign('documents', $docs);
		} else if($opmsg){
			$errmsg = "Problem getting Workspace documents: ".$opmsg;
		}
		$t->assign('workspace', $workspace);
	} else if($workspace){
		$corpora = getCorpora($CFG);
		if($corpora) {
			$t->assign('corpora', $corpora);
		} else if($opmsg){
			$errmsg = "Problem getting Corpora list: ".$opmsg;
		}
		$t->assign('workspace', $workspace);
	} else if($opmsg){
		$errmsg = "Problem getting Workspace details: ".$opmsg;
	} else {
		$errmsg = "Bad or illegal workspace specifier. ";
	}
}


if($errmsg!="")
	$t->assign('errmsg', $errmsg);

$t->display('workspace.tpl');

?>























