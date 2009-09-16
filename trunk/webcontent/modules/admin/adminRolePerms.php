<?php 
/* Include Files *********************/
require_once("../../libs/env.php");
require_once("authUtils.php");
/*************************************/
// If the user isn't logged in, send to the login page.
if(($login_state != DELPHI_LOGGED_IN) && ($login_state != DELPHI_REG_PENDING)){
	header( 'Location: ' . $CFG->wwwroot .
					'/modules/auth/login.php?redir=' .$_SERVER['REQUEST_URI'] );
	die();
}

$t->assign('page_title', 'PAHMA/Delphi: Edit Role Permissions');

// This needs to verify perms. 
if( !currUserHasPerm( 'EditRoles' ) ) {
	$opmsg = "You do not have rights to Edit roles. <br />
		Please contact your Delphi administrator for help.";
	$t->assign('perm_error', $opmsg);

	$t->display('adminPermissions.tpl');
	die();
}

$style_block = "<style>
td.title { border-bottom: 2px solid black; font-weight:bold;  
		font-style:italic; color:#777777; }
td.label { font-weight:bold; }
td.role { border-bottom: 1px solid black; }
</style>";


$t->assign("style_block", $style_block);

$themebase = $CFG->wwwroot.'/themes/'.$CFG->theme;

$script_block = '
<script type="text/javascript" src="'.$themebase.'/scripts/setupXMLHttpObj.js"></script>

<script>

// The ready state change callback method that waits for a response.
function setPermForRoleRSC() {
  if (xmlhttp.readyState==4) {
		if( xmlhttp.status == 200 ) {
			// Maybe this should change the cursor or something
			window.status = "Permission for role updated.";
	    //alert( "Response: " + xmlhttp.status + " Body: " + xmlhttp.responseText );
		} else {
			alert( "Error encountered when trying to update role/perms.\nResponse: "
			 				+ xmlhttp.status + "\nBody: " + xmlhttp.responseText );
		}
	}
}

function setPermForRole( perm, role, action ) {
	if( !xmlhttp )
	  alert( "Cannot update role:permission - no http obj!\n Please advise Delphi support." );
	else {
		var url = "../../api/setRolePerm.php";
		var args = "r="+role+"&p="+perm+"&a="+action;
		//alert( "Preparing request: POST: "+url+"?"+args );
		xmlhttp.open("POST", url, true);
		xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded" );
 		xmlhttp.onreadystatechange=setPermForRoleRSC;
		xmlhttp.send(args);
		//window.status = "request sent: POST: "+url+"?"+args;
	}
}

function MarkChanged(evt) {
	var evt = evt || window.event; // event object
	var target = evt.target || window.event.srcElement; // event target
	var targetID = target.getAttribute("id"); // event target id
	var iDot = targetID.indexOf(".");
	if(iDot < 1)
		alert( "Error on page - cannot find role to set for item" );
	else {
		var perm = targetID.substr( 0, iDot );
		var role = targetID.substr( iDot+1 );
		var action = target.checked? "set":"unset";
		//alert( "Calling setPermForRole( "+perm+", "+role+", "+action+")" );
		setPermForRole( perm, role, action );
	}
}

</script>';

$t->assign("script_block", $script_block);

function getRoles(){
	global $db;
  /* Get all the roles */
	$q = "select name from role";
	$res =& $db->query($q);
	if (PEAR::isError($res))
		return false;
	$roles = array();
	while ($row = $res->fetchRow()) {
		$roles[] = $row['name'];
	}
	return $roles;
}

function getPermRoles(){
	global $db;
   /* Get all the permissions and their assigned roles */
	$q = "select p.name perm, r.name role from permission p "
			." left join role_perms rp on ( p.id=rp.perm_id )"
	 		." left join role r on (rp.role_id=r.id)";
	$res =& $db->query($q);
	if (PEAR::isError($res))
		return false;
	$permroles = array();
	while ($row =& $res->fetchRow()) {
		$permroles[$row['perm']][$row['role']] = 1;
	}
	// Free the result
	$res->free();
	return $permroles;
}

$roles = getRoles();
if($roles){
	$t->assign('roles', $roles);
}

$permroles = getPermRoles();
if($permroles){
	$t->assign('permroles', $permroles);
}

//if($opmsg!="")
//	$t->assign('opmsg', $opmsg);

$t->display('adminRolePerms.tpl');

?>
