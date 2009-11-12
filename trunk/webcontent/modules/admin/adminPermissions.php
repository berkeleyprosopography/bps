<?php 
/* Include Files *********************/
require_once("../../libs/env.php");
require_once("authUtils.php");
/*************************************/
// If the user isn't logged in, send to the login page.
if(($login_state != BPS_LOGGED_IN) && ($login_state != BPS_REG_PENDING)){
	header( 'Location: ' . $CFG->wwwroot .
					'/login?redir=' .$_SERVER['REQUEST_URI'] );
	die();
}

$t->assign('page_title', 'BPS: Edit Permission Definitions');
$opmsg = "";

// This needs to verify perms. 
if( !currUserHasPerm( 'EditPerms' ) ) {
	$opmsg = "You do not have rights to Edit permissions. <br />
		Please contact your BPS administrator for help.";
	$t->assign('perm_error', $opmsg);

	$t->display('adminPermissions.tpl');
	die();
}

$style_block = "<style>
td.title { border-bottom: 2px solid black; font-weight:bold; text-align:left; 
		font-style:italic; color:#777777; }
td.perm_label { font-weight:bold; color:#61615f; }
td.permname { font-weight:bold; }
td.perm { border-bottom: 1px solid black; }
td.permdesc textarea { font-family: Arial, Helvetica, sans-serif; }
form.form_row  { padding:0px; margin:0px;}
</style>";

$t->assign("style_block", $style_block);

$themebase = $CFG->wwwroot.'/themes/'.$CFG->theme;

$script_block = '
<script type="text/javascript" src="/scripts/setupXMLHttpObj.js"></script>
<script>

// The ready state change callback method that waits for a response.
function updatePermRSC() {
  if (xmlhttp.readyState==4) {
		if( xmlhttp.status == 200 ) {
			// Maybe this should change the cursor or something
			window.status = "Permission updated.";
	    //alert( "Response: " + xmlhttp.status + " Body: " + xmlhttp.responseText );
		} else {
			alert( "Error encountered when trying to update permission.\nResponse: "
			 				+ xmlhttp.status + "\nBody: " + xmlhttp.responseText );
		}
	}
}

function updatePerm(permName) {
	// Could change cursor and disable button until get response
	var descTextEl = document.getElementById("D_"+permName);
	var desc = descTextEl.value;
	if( desc.length <= 2 )
		alert( "You must enter a description that is at least 3 characters long" );
	else if( !xmlhttp )
		alert( "Cannot update permission - no http obj!\n Please advise BPS support." );
	else {
		var url = "/api/updatePerm.php";
		var args = "p="+permName+"&d="+desc;
		//alert( "Preparing request: POST: "+url+"?"+args );
		xmlhttp.open("POST", url, true);
		xmlhttp.setRequestHeader("Content-Type",
															"application/x-www-form-urlencoded" );
		xmlhttp.onreadystatechange=updatePermRSC;
		xmlhttp.send(args);
		//window.status = "request sent: POST: "+url+"?"+args;
		var el = document.getElementById("U_"+permName);
		el.disabled = true;
	}
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
    alert( "Permission name must be at least 4 characters in length." );
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

if(isset($_POST['delete'])){
	if(empty($_POST['perm']))
		$opmsg = "Problem deleting perm.";
	else {
		$permname = $_POST['perm'];
		$deleteQ = "DELETE FROM permission WHERE name='".$permname."'";
		$res =& $db->query($deleteQ);
		if (PEAR::isError($res)) {
			$opmsg = "Problem deleting permission \"".$permname."\".<br />".$res->getMessage();
		} else {
			$opmsg = "Permission \"".$permname."\" deleted.";
		}
	}
}
else if(isset($_POST['add'])){
	unset($errmsg);
	if(empty($_POST['perm']))
		$errmsg = "Missing Permission name.";
	else {
		$permname = trim($_POST['perm']);
		if( strlen( $permname ) < 4 )
			$errmsg = "Invalid permission name: [".$permname."]";
		else if( preg_match( "/[^\w\s]/", $permname ))
			$errmsg = "Invalid permission name (invalid chars): [".$permname."]";
		else if(empty($_POST['desc']))
			$errmsg = "Missing permission description.";
		else {
			$permdesc = trim($_POST['desc']);
			if( strlen( $permdesc ) > 255 )
				$errmsg = "Invalid permission description (too long);";
			else if( preg_match( "/[^\w\-\s.:'()]/", $permdesc ))
				$errmsg = "Invalid permission description (invalid chars): [".$permdesc."]";
		}
	}
	if(!empty($errmsg))
		$opmsg = $errmsg;
	else {
		$addQ = "INSERT IGNORE INTO permission(name, description, creation_time)"
			." VALUES ('".mysql_real_escape_string($permname)."', '"
			.mysql_real_escape_string($permdesc)."', now())";
		$res =& $db->query($addQ);
		if (PEAR::isError($res)) {
			$opmsg = "Problem adding permission \"".$permname."\".<br />".$res->getMessage();
		} else {
			$opmsg = "Permission \"".$permname."\" added.";
		}
	}
}

function getFullPerms(){
	global $db;
   /* Get all the users and their assigned perms */
	$q = "select name, description from permission";
	$res =& $db->query($q);
	if (PEAR::isError($res))
		return false;
	$perms = array();
	while ($row = $res->fetchRow()) {
		$perm = array(	'name' => $row['name'], 
						'description' => $row['description']);
		
		array_push($perms, $perm);
	}
	// Free the result
	$res->free();
	return $perms;
}

$perms = getFullPerms();
if($perms){
	$t->assign('perms', $perms);
}

if($opmsg!="")
	$t->assign('opmsg', $opmsg);

$t->display('adminPermissions.tpl');
?>
