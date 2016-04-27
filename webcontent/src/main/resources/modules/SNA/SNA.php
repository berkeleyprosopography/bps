<?php

/* Include Files *********************/
require_once("../../libs/env.php");
require_once("../admin/authUtils.php");
// require_once("HTTP/Request2.php");
require_once "../../libs/RESTClient.php";
/*************************************/

// If the user isn't logged in, send to the login page.
// FIXME - allow not logged in state - will be all no-edit
if(($login_state != BPS_LOGGED_IN) && ($login_state != BPS_REG_PENDING)){
	header( 'Location: ' . $CFG->wwwroot .
					'/login?redir=' .$_SERVER['REQUEST_URI'] );
	die();
}


$style_block = '<link rel="stylesheet" href="/style/SNA/style.css">';
$t->assign("style_block", $style_block);


$script_block = '<script src="/libs/d3.js.v2/d3.v2.js"></script>'."\n".'<script src="/scripts/SNA/libs/modernizr-2.0.6.min.js"></script>';
$t->assign("script_block", $script_block);


if(!isset($_GET['wid'])) {
	$errmsg = "Missing workspace or document specifier(s).";
	$t->display('error.tpl');
	die();
} else {
$t->assign('wkspId', $_GET['wid']);	// Needed for links to SNA
// We should find a way to specify a stub filename and pass it in as well.
}


$t->display('sna.tpl');

?>






















