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
$maxK = 10000;
$maxfilesize = $maxK * 1024;
$t->assign('maxfilesize', $maxfilesize);

$canUpdateCorpus = false;
if(currUserHasPerm( 'CorpusUpdate' )) {
	$canUpdateCorpus = true;
	$t->assign('canUpdateCorpus', 1);
}

$style_block = "<style>
td.title { border-bottom: 2px solid black; font-weight:bold; text-align:left; 
		font-style:italic; color:#777777; }
td.corpus_label { font-weight:bold; color:#61615f; }
td.corpusname { font-weight:bold; }
td.corpusdesc p { font-weight:bold; }
td.corpusX { border-bottom: 1px solid black; }
td.corpusdesc textarea { font-family: Arial, Helvetica, sans-serif; padding:2px;}
td.corpusndocs { text-align:center; padding-right:10px;}
form.form_row  { padding:0px; margin:2px;}
div.form_row  { padding:5px 0px 5px 0px; border-bottom: 1px solid black; }
div.docs_row  { padding:5px 0px 5px 0px; border-bottom: 1px solid black; }
td.document { font-weight:bold; }
</style>";

$t->assign("style_block", $style_block);

$themebase = $CFG->wwwroot.'/themes/'.$CFG->theme;

$script_block = '
<script type="text/javascript" src="/scripts/setupXMLHttpObj.js"></script>
<script type="text/javascript" src="/scripts/corpus.js"></script>
<script>

// The ready state change callback method that waits for a response.
function updateCorpusRSC() {
  if (xmlhttp.readyState==4) {
		if( xmlhttp.status == 200 ) {
			// Maybe this should change the cursor or something
			setStatusP("Corpus updated.");
	    //alert( "Response: " + xmlhttp.status + " Body: " + xmlhttp.responseText );
		} else {
			alert( "Error encountered when trying to update corpus.\nResponse: "
			 				+ xmlhttp.status + "\nBody: " + xmlhttp.responseText );
		}
	}
}


function updateCorpus(corpusID, name) {
	// Could change cursor and disable button until get response
	var descTextEl = document.getElementById("D_"+corpusID);
	var desc = descTextEl.value;
	if( desc.length <= 2 ) {
		alert( "You must enter a description that is at least 3 characters long" );
		return;
	}
	if( !xmlhttp ) {
		alert( "Cannot update corpus - no http obj!\n Please advise BPS support." );
		return;
	}
	var url = "'.$CFG->svcsbase.'/corpora/"+corpusID;
	var args = prepareCorpusXML(corpusID, name, desc,"");
	//alert( "Preparing request: PUT: "+url+"?"+args );
	xmlhttp.open("PUT", url, true);
	xmlhttp.setRequestHeader("Content-Type",
														"application/xml" );
	xmlhttp.onreadystatechange=updateCorpusRSC;
	xmlhttp.send(args);
	//window.status = "request sent: POST: "+url+"?"+args;
	var el = document.getElementById("U_"+corpusID);
	el.disabled = true;
}

// This should go into a utils.js - how to include?
function enableElement( elID ) {
	//alert( "enableElement" );
	var el = document.getElementById(elID);
	el.disabled = false;
	//window.status = "Element ["+elID+"] enabled.";
}

function setStatusP(str) {
	var el = document.getElementById("statusP");
	el.innerHTML = str;
}

function limitChars( field, maxlimit ) {
  if ( field.value.length > maxlimit )
  {
    field.value = field.value.substring( 0, maxlimit-1 );
    alert( "Description can only be 255 characters in length." );
    return false;
  }
	return true;
}

</script>';

$t->assign("script_block", $script_block);

$opmsg = false;

function getCorpus($CFG,$id){
	global $opmsg;

	$rest = new RESTclient();
	$url = $CFG->wwwroot.$CFG->svcsbase."/corpora/".$id;
	$rest->createRequest($url,"GET");
	// Get the results in JSON for easier manipulation
	$rest->setJSONMode();
	if($rest->sendRequest()) {
		$ServCorpOutput = $rest->getResponse();
		$result = json_decode($ServCorpOutput, true);
		$corpObj = &$result['corpus'];
		$corpus = array(	'id' => $corpObj['id'], 'name' => $corpObj['name'], 
					'nDocs' => $corpObj['ndocs'], 'description' => $corpObj['description']);
		unset($corpObj);
		return $corpus;
	} else if($rest->getStatus() == 404) {
		$opmsg = "Bad or illegal corpus specifier. ";
	} else {
		$opmsg = $rest->getError();
	}
	return false;
}

function getCorpusDocs($CFG,$id){
	global $opmsg;

	$rest = new RESTclient();
	$url = $CFG->wwwroot.$CFG->svcsbase."/corpora/".$id."/documents";
	$rest->createRequest($url,"GET");
	// Get the results in JSON for easier manipulation
	$rest->setJSONMode();
	if($rest->sendRequest()) {
		$ServCorpDocsOutput = $rest->getResponse();
		$results = json_decode($ServCorpDocsOutput, true);
		$documents = array();
		foreach($results as &$result) {
			$docObj = &$result['document'];
			$document = array(	'id' => $docObj['id'], 'alt_id' => $docObj['alt_id'], 
				'notes' => $docObj['notes'], 'sourceURL' => $docObj['sourceURL'],
				'xml_id' => $docObj['xml_id'], 'date_str' => $docObj['date_str']
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

if(!isset($_GET['id'])) {
	$errmsg = "Missing corpus specifier. ";
} else {
	$corpus = getCorpus($CFG,$_GET['id']);
	if($corpus){
		$t->assign('corpus', $corpus);
		$corp_file = $CFG->corpusdir.'/'.$_GET['id'].'/tei/corpus.xml';
		if(file_exists($corp_file)) {
			$t->assign('corpus_file', $corp_file);
			$teisummaryloc = $CFG->wwwroot.$CFG->svcsbase.'/corpora/'.$_GET['id'].'/teisummary';
			$t->assign('teisummaryloc', $teisummaryloc);
		}
		$docs = getCorpusDocs($CFG,$_GET['id']);
		if($docs) {
			$t->assign('documents', $docs);
		} else if($opmsg){
			$errmsg = "Problem getting Corpus documents: ".$opmsg;
		}
	} else if($opmsg){
		$errmsg = "Problem getting Corpus details: ".$opmsg;
	} else {
		$errmsg = "Bad or illegal corpus specifier. ";
	}
}


if($errmsg!="")
	$t->assign('errmsg', $errmsg);

$t->display('corpus.tpl');

?>























