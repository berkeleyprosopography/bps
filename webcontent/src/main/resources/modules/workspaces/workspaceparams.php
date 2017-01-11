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

function setStatusP(str) {
	var el = document.getElementById("statusP");
	if(el != null)
		el.innerHTML = str;
}

</script>
';

$t->assign("script_block", $script_block);

$opmsg = false;

function getWorkspace($CFG,$user_id, $wkspid){
	global $opmsg;

	$rest = new RESTclient();
	$url = $CFG->serverwwwroot.$CFG->svcsbase."/workspaces";
	if(isset($wkspid)) {
		$url = $url."/".$wkspid;
	} else {
		$url = $url."?user=".$user_id;
	}
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

$user_id = getCurrUser();

if(!isset($user_id)) {
	$errmsg = "You must be logged in to access your workspace(s).";
} else {
	$workspace = getWorkspace($CFG,$user_id, isset($_GET['wid'])?$_GET['wid']:null);
	if(!$workspace) {
		if($opmsg){
			$errmsg = "Problem getting Workspace details: ".$opmsg;
		} else {
			$errmsg = "Bad or illegal workspace specifier. ";
		}
	} else {
		// Nothing to do now, as info is in $workspace. Later, will get Collapser and convert
		$t->assign('workspace', $workspace);
		$t->assign('wkspId', $workspace['id']);
	}
}


if(isset($errmsg)) {
	if(!$workspace) {
		$t->assign('heading', 'Cannot show workspace settings');
		$t->assign('message', $errmsg);
		$t->display('error.tpl');
		die();
	} else {
		$t->assign('errmsg', $errmsg);
	}
}

$t->display('workspace_params.tpl');

?>























