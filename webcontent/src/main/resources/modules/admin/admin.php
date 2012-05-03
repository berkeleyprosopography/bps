<?php 
/* Include Files *********************/
require_once("../../libs/env.php");
/*************************************/
// If the user isn't logged in, send to the login page.
if(($login_state != BPS_LOGGED_IN) && ($login_state != BPS_REG_PENDING)){
	header( 'Location: ' . $CFG->wwwroot .
					'/modules/auth/login.php?redir=' .$_SERVER['REQUEST_URI'] );
	die();
}

// This need not verify perms as it does not expose anything directly. 

$t->assign('page_title', 'BPS: Roles and Permissions Administration');
$t->display('admin.tpl');
?>
