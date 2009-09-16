<?php

require_once("../../libs/env.php");
require_once("../../libs/utils.php");

// If the user isn't logged in, send to the login page.
if(($login_state != DELPHI_LOGGED_IN) && ($login_state != DELPHI_REG_PENDING)){
	header( 'Location: ' . $CFG->wwwroot .
					'/modules/auth/login.php?redir=' .$_SERVER['REQUEST_URI'] );
	die();
}

// TODO Update the perms for this.
if( !currUserHasPerm( 'EditNewsContent' ) ) {
	$t->assign('heading', "Unauthorized Action.");
	$t->assign('message', "You do not have rights to edit news content. <br />
		Please contact your Delphi administrator for help.");
	$t->display('error.tpl');
	die();
}


$id = -1;
$msg = array();
if(isset($_POST['submit'])){
	/*
	Skip this, allowing form user to clear the item, and stop the function.
	if(empty($_POST['header']) || !strlen($_POST['header']) > 0){
		array_push($msg, "You must enter a headline.");
	}
	if(empty($_POST['content']) || !strlen($_POST['content']) > 0){
		array_push($msg, "You must enter a message.");
	}
	 */
	$header = cleanFormData($_POST['header']);
	$content = cleanFormData($_POST['content']);
	$t->assign('header', $header);
	$t->assign('content', $content);
	if(empty($_POST['id']) || ($_POST['id'] < 0)) {
		$q = "INSERT INTO newsContent(header, content) VALUES(\"$header\",\"$content\")";
	} else {
		$id = $_POST['id'];
		$q = "UPDATE newsContent SET header=\"$header\", content=\"$content\" WHERE id=$id";
	}
	$res =& $db->query($q);
	if(PEAR::isError($res)) {
		array_push($msg, "Error saving news Item to database.");
		array_push($msg, "Query: ".$q);
		array_push($msg, "Error: ".$res->getMessage());
	} else {
		array_push($msg, "News Item saved to database.");
		if($id<0)
			$id = $db->lastInsertId();
	}
} else if(isset($_GET['id'])){
	$id = $_GET['id'];
	if($id >=0 ) {
		$q = "SELECT header, content FROM newsContent WHERE id=$id";
		$res =& $db->query($q);
		if(!PEAR::isError($res) && ($res->numRows()==1) && ($row=$res->fetchRow())) {
			$t->assign('header', $row['header']);
			$t->assign('content', $row['content']);
		} else {
			$id = -1;
			array_push($msg, "Unknown News Item specified.");
		}
	}
}
$t->assign('messages', $msg);
$t->assign('id', $id);
$t->display('editNews.tpl');

?>
