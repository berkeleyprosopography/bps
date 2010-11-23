<?php

/* Include Files *********************/
require_once("../../libs/env.php");
require_once("../admin/authUtils.php");
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
form.form_row  { padding:0px; margin:2px;}
div.form_row  { padding:5px 0px 5px 0px; border-bottom: 1px solid black; }
</style>";

$t->assign("style_block", $style_block);

$themebase = $CFG->wwwroot.'/themes/'.$CFG->theme;

$script_block = '
<script type="text/javascript" src="/scripts/setupXMLHttpObj.js"></script>
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

function updateCorpus(corpusID) {
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
	var url = "/bps.services.webapp/services/corpora/"+corpusID;
	var args = "description="+desc;
	//alert( "Preparing request: PUT: "+url+"?"+args );
	xmlhttp.open("PUT", url, true);
	xmlhttp.setRequestHeader("Content-Type",
														"application/x-www-form-urlencoded" );
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

function checkValues( e, name, desc, limit ) {
	if( name.value.length < 4 ) {
    alert( "Corpus name must be at least 4 characters in length." );
		e.returnValue = false;
		if( e.preventDefault )
			e.preventDefault();
    return false;
  }
	if( !limitChars( desc, limit ) ) {
		e.returnValue = false;
		if( e.preventDefault )
			e.preventDefault();
    return false;
  }
	return true;
}


</script>';

$t->assign("script_block", $script_block);

function getCorpus($id){
	global $db;
	// Get all the corpora, with doc counts, and order by when added
	$sql = 	'	SELECT c.name, c.description FROM corpus c WHERE c.id=?';
	$stmt = $db->prepare($sql, array('integer'), MDB2_PREPARE_RESULT);
	$res =& $stmt->execute($id);
	if (PEAR::isError($res)) {
		// FIXME when debugged, comment this out and just return false
    die( 'Error in sql ['.$sql.']to getCorpora: '.$res->getMessage());
		// return false;
	}
	$corpus = false;
	if ($row = $res->fetchRow()) {
		$corpus = array( 'id' => $id, 'name' => $row['name'], 'description' => $row['description']);
	}
	// Free the result
	$res->free();
	$stmt->free();
	return $corpus;
}

if(!isset($_GET['id'])) {
	$errmsg = "Missing corpus specifier. ";
} else {
	$corpus = getCorpus($_GET['id']);
	if($corpus){
		$t->assign('corpus', $corpus);
	} else {
		$errmsg = "Bad or illegal corpus specifier. ";
	}
}


if($errmsg!="")
	$t->assign('errmsg', $errmsg);

$t->display('corpus.tpl');

?>























