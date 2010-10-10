<?php
//
// This should combine both an admin view and a browse view, depending upon
// the role of the logged in user. Can check for CorporaAdmin role.
// If not an admin, just lists the corpora with name and description.
// Should probably base this upon something like the adminRoles page.
// If an Admin, can make those widgets be editable. Else, just <p> elements.
// They link to the default workspace for each, to see stats, etc.
// Question: should we separate view and admin, so the view feature is
// the common one for all, and then support an admin link that has the alternate
// UI? Could make it the same page, with a different option. I think yes.

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

$t->assign('page_title', 'Corpora Management'.$CFG->page_title_default);

$canAddCorpus = false;
$canUpdateCorpus = false;
$canDeleteCorpus = false;
if(currUserHasPerm( 'CorpusAdd' )) {
	$canAddCorpus = true;
	$t->assign('canAddCorpus', 1);
}
if(currUserHasPerm( 'CorpusUpdate' )) {
	$canUpdateCorpus = true;
	$t->assign('canUpdateCorpus', 1);
}
if(currUserHasPerm( 'CorpusDelete' )) {
	$canDeleteCorpus = true;
	$t->assign('canDeleteCorpus', 1);
}

$style_block = "<style>
td.title { border-bottom: 2px solid black; font-weight:bold; text-align:left; 
		font-style:italic; color:#777777; }
td.corpus_label { font-weight:bold; color:#61615f; }
td.corpusname { font-weight:bold; }
td.corpusdesc p { font-weight:bold; }
td.corpusndocs { text-align:right; padding-right:10px;}
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
			window.status = "Corpus updated.";
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
	var url = "/api/updateCorpus.php";
	var args = "id="+corpusID+"&d="+desc;
	//alert( "Preparing request: POST: "+url+"?"+args );
	xmlhttp.open("POST", url, true);
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

/**********************************
 HANDLE UPDATE AND DELETE
**********************************/
if(isset($_POST['delete'])){
	if(empty($_POST['id'])) {
		$opmsg = "Problem deleting corpus (no ID specified).";
	} else if(!$canDeleteCorpus) {
		$opmsg = "You do not have rights to delete a corpus.";
	} else {
		$corpusID = $_POST['id'];
		// FIXME need to move to prepared statements and params
		$deleteQ = "DELETE FROM corpus WHERE id='".$corpusID."'";
		$res =& $db->query($deleteQ);
		if (PEAR::isError($res)) {
			$opmsg = "Problem deleting corpus.<br />".$res->getMessage();
		} else {
			$opmsg = "Corpus deleted.";
		}
	}
}
else if(isset($_POST['add'])){
	unset($errmsg);
	$corpusname = "";
	$corpusdesc = "";
	if(!$canAddCorpus) {
		$errmsg = "You do not have rights to add a corpus.";
	} else {
		// FIXME need to move to prepared statements and params
		$corpusname = trim($_POST['corpname']);
		if( strlen( $corpusname ) < 4 )
			$errmsg = "Invalid corpus name: [".$corpusname."]";
		else if( preg_match( "/[^\w\s]/", $corpusname ))
			$errmsg = "Invalid corpus name (invalid chars): [".$corpusname."]";
		else if(empty($_POST['desc']))
			$errmsg = "Missing corpus description.";
		else {
			$corpusdesc = trim($_POST['desc']);
			if( strlen( $corpusdesc ) > 255 )
				$errmsg = "Invalid corpus description (too long);";
			else if( preg_match( "/[^\w\-\s.:'()]/", $corpusdesc ))
				$errmsg = "Invalid corpus description (invalid chars): [".$corpusdesc."]";
		}
	}
	if(!empty($errmsg))
		$opmsg = $errmsg;
	else {
		$user_id = $_SESSION['id'];
		$addQ = "INSERT IGNORE INTO corpus(name, description, owner_id, creation_time)"
			." VALUES ('".mysql_real_escape_string($corpusname)."', '"
			.mysql_real_escape_string($corpusdesc)."', ".$user_id.", now())";
		$res =& $db->query($addQ);
		if (PEAR::isError($res)) {
			$opmsg = "Problem adding corpus \"".$corpusname."\".<br />".$res->getMessage();
		} else {
			$opmsg = "Corpus \"".$corpusname."\" added.";
		}
	}
}

/**********************************
GET ALL CORPORA IN SYSTEM
**********************************/

function getCorpora(){
	global $db;
	// Get all the corpora, with doc counts, and order by when added
	$sql = 	'	SELECT c.id, c.name, c.description, count(*) nDocs, d.id docid'
				 .' FROM corpus c LEFT JOIN document d ON d.corpus_id=c.id GROUP BY c.id'
				 .' ORDER BY c.creation_time';
	$res =& $db->query($sql);
	if (PEAR::isError($res)) {
		// FIXME when debugged, comment this out and just return false
    die( 'Error in sql ['.$sql.']to getCorpora: '.$res->getMessage());
		// return false;
	}
	$corpora = array();
	while ($row = $res->fetchRow()) {
		$nDocs = 0 + $row['nDocs'];
		if(( $nDocs == 1 ) && empty($row['docid'])) {
			$nDocs = 0;
		}
		$corpus = array(	'id' => $row['id'], 'name' => $row['name'], 
						'nDocs' => $nDocs, 'description' => $row['description']);
		
		array_push($corpora, $corpus);
	}
	// Free the result
	$res->free();
	return $corpora;
}

$corpora = getCorpora();
if($corpora){
	$t->assign('corpora', $corpora);
}

if($opmsg!="")
	$t->assign('opmsg', $opmsg);

$t->display('corpora.tpl');

?>






















