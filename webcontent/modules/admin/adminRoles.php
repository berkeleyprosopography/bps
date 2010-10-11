<?php 
/* Include Files *********************/
require_once("../../libs/env.php");
require_once("authUtils.php");
/*************************************/
// If the user isn't logged in, send to the login page.
if(($login_state != BPS_LOGGED_IN) && ($login_state != BPS_REG_PENDING)){
	header( 'Location: ' . $CFG->wwwroot .
					'/modules/auth/login.php?redir=' .$_SERVER['REQUEST_URI'] );
	die();
}

$t->assign('page_title', 'BPS: Edit Role Definitions');

// This needs to verify perms. 
if( !currUserHasPerm( 'EditRoles' ) ) {
	$opmsg = "You do not have rights to Edit roles. <br />
		Please contact your BPS administrator for help.";
	$t->assign('perm_error', $opmsg);

	$t->display('adminPermissions.tpl');
	die();
}

$style_block = "<style>
td.title { border-bottom: 2px solid black; font-weight:bold; text-align:left; 
		font-style:italic; color:#777777; }
td.label { font-weight:bold; }
td.rolename { font-weight:bold; }
td.role { border-bottom: 1px solid black; }
td.roledesc textarea { font-family: Arial, Helvetica, sans-serif; }
form.form_row  { padding:0px; margin:0px;}
</style>";

$t->assign("style_block", $style_block);

$script_block = '
<script type="text/javascript" src="/scripts/setupXMLHttpObj.js"></script>
<script>

// The ready state change callback method that waits for a response.
function updateRoleRSC() {
  if (xmlhttp.readyState==4) {
		if( xmlhttp.status == 200 ) {
			// Maybe this should change the cursor or something
			window.status = "Role updated.";
	    //alert( "Response: " + xmlhttp.status + " Body: " + xmlhttp.responseText );
		} else {
			alert( "Error encountered when trying to update role.\nResponse: "
			 				+ xmlhttp.status + "\nBody: " + xmlhttp.responseText );
		}
	}
}
function updateRole(roleName) {
	// Could change cursor and disable button until get response
	var descTextEl = document.getElementById("D_"+roleName);
	var desc = descTextEl.value;
	if( desc.length < 3 )
		alert( "You must enter a description that is at least 3 characters long" );
	else if( !xmlhttp )
		alert( "Cannot update role - no http obj!\n Please advise BPS support." );
	else {
		var url = "/api/updateRole.php";
		var args = "r="+roleName+"&d="+desc;
		//alert( "Preparing request: POST: "+url+"?"+args );
		xmlhttp.open("POST", url, true);
		xmlhttp.setRequestHeader("Content-Type",
															"application/x-www-form-urlencoded" );
		xmlhttp.onreadystatechange=updateRoleRSC;
		xmlhttp.send(args);
		//window.status = "request sent: POST: "+url+"?"+args;
		var el = document.getElementById("U_"+roleName);
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
    alert( "Role name must be at least 4 characters in length." );
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

$opmsg = "";

if(isset($_POST['delete'])){
	if(empty($_POST['role']))
		$opmsg = "Problem deleting role.";
	else {
		$rolename = $_POST['role'];
		$deleteQ = "DELETE FROM role WHERE name=?";
		$stmt = $db->prepare($deleteQ, array('text'), MDB2_PREPARE_MANIP);
		$res =& $stmt->execute($rolename);
		if (PEAR::isError($res)) {
			$opmsg = "Problem deleting role \"".$rolename."\".<br />".$res->getMessage();
		} else {
			$opmsg = "Role \"".$rolename."\" deleted.";
		}
		$stmt->free();
	}
}
else if(isset($_POST['add'])){
	unset($errmsg);
	if(empty($_POST['role']))
		$errmsg = "Missing role name.";
	else {
		$rolename = trim($_POST['role']);
		if( strlen( $rolename ) < 4 )
			$errmsg = "Invalid role name: [".$rolename."]";
		else if( preg_match( "/[^\w\s]/", $rolename ))
			$errmsg = "Invalid role name (invalid chars): [".$rolename."]";
		else if(empty($_POST['desc']))
			$errmsg = "Missing role description.";
		else {
			$roledesc = trim($_POST['desc']);
			if( strlen( $roledesc ) > 255 )
				$errmsg = "Invalid role description (too long);";
			else {
				$wksp_role = (empty($_POST['wr'])||($_POST['wr']=='0'))?'0':'1';
			}
		}
	}
	if(!empty($errmsg))
		$opmsg = $errmsg;
	else {
		$addQ = "INSERT IGNORE INTO role(name, wksp_role, description, creation_time)"
			." VALUES (?,?,?, now())";
		$stmt = $db->prepare($addQ, array('text', 'boolean', 'text'), MDB2_PREPARE_MANIP);
		$res =& $stmt->execute(array($rolename, $wksp_role, $roledesc));
		
		if (PEAR::isError($res)) {
			$opmsg = "Problem adding role \"".$rolename."\".<br />".$res->getMessage();
		} else {
			$opmsg = "Role \"".$rolename."\" added.";
		}
	}
}

function getFullRoles(){
	global $db;
   /* Get all the users and their assigned roles */
	$q = "select name, wksp_role, description from role order by wksp_role";
	$res =& $db->query($q);
	if (PEAR::isError($res))
		return false;
	$roles = array();
	while ($row = $res->fetchRow()) {
		$role = array(	'name' => $row['name'], 
						'wksp_role' => $row['wksp_role'],
						'description' => $row['description']);
		
		array_push($roles, $role);
	}
	// Free the result
	$res->free();
	return $roles;
}

$roles = getFullRoles();
if($roles){
	$t->assign('roles', $roles);
}
if($opmsg!="")
	$t->assign('opmsg', $opmsg);

$t->display('adminRoles.tpl');
?>
