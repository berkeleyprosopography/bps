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

$t->assign('page_title', 'PAHMA/Delphi: Edit User Roles');

// This needs to verify perms. 
if( !currUserHasPerm( 'AssignRoles' ) ) {
	$opmsg = "You do not have rights to Assign roles to users. <br />
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
var sess_login_id = '.((isset($_SESSION['login_id']))?$_SESSION['login_id']:-1).';

// The ready state change callback method that waits for a response.
function setRoleForUserRSC() {
  if (xmlhttp.readyState==4) {
		if( xmlhttp.status == 200 ) {
			// Maybe this should change the cursor or something
			window.status = "Role for user updated.";
	    //alert( "Response: " + xmlhttp.status + " Body: " + xmlhttp.responseText );
		} else {
			alert( "Error encountered when trying to update user/roles.\nResponse: "
			 				+ xmlhttp.status + "\nBody: " + xmlhttp.responseText );
		}
	}
}

function setRoleForUser( role, user, action ) {
	if( !xmlhttp )
	  alert( "Cannot update role:permission - no http obj!\n Please advise Delphi support." );
	else {
		var url = "../../api/setUserRole.php";
		var args = "r="+role+"&u="+user+"&a="+action;
		if(sess_login_id >= 0)
			args += "&ap="+sess_login_id;
		//alert( "Preparing request: POST: "+url+"?"+args );
		xmlhttp.open("POST", url, true);
		xmlhttp.setRequestHeader("Content-Type",
															"application/x-www-form-urlencoded" );
 		xmlhttp.onreadystatechange=setRoleForUserRSC;
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
		var user = targetID.substr( 0, iDot );
		var role = targetID.substr( iDot+1 );
		var action = target.checked? "set":"unset";
		//alert( "Calling setRoleForUser( "+role+", "+user+", "+action+")" );
		setRoleForUser( role, user, action );
	}
}
</script>';

$t->assign("script_block", $script_block);

function getRoles(){
	global $db;
  /* Get all the roles */
	$q = "select name from role";
	if( !currUserHasRole( 'Admin' ) ) {
		$q .= " where NOT name like 'Admin'";
	}
	$res =& $db->query($q);
	if (PEAR::isError($res))
		return false;
	$roles = array();
	while ($row = $res->fetchRow()) {
		$roles[] = $row['name'];
	}
	return $roles;
}

function getUserRoles(){
	global $db;
   /* Get all the users and their assigned roles */
	$q = "select u.username user, r.name role from user u "
			." left join user_roles ur on ( u.id=ur.user_id )"
	 		." left join role r on (ur.role_id=r.id)";
	if( !currUserHasRole( 'Admin' ) ) {
		$q .= " where NOT r.name like 'Admin' and NOT u.username like 'admin'";
	} else {
		$q .= " where NOT u.username like 'admin'";
	}
	$q .= " order by u.id";
	$res =& $db->query($q);
	if (PEAR::isError($res))
		return false;
	$userroles = array();
	while ($row =& $res->fetchRow()) {
		$userroles[$row['user']][$row['role']] = 1;
	}
	// Free the result
	$res->free();
	return $userroles;
}

$roles = getRoles();
if($roles){
	$t->assign('roles', $roles);
}

$userroles = getUserRoles();
if($userroles){
	$t->assign('userroles', $userroles);
}

//if($opmsg!="")
//	$t->assign('opmsg', $opmsg);

$t->display('adminUserRoles.tpl');

?>
