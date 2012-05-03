<?php
require_once("../../libs/env.php");
// require_once("../common/imgthumb.php");

if(isset($_GET['uid']) && is_numeric($_GET['uid'])){
	$profileId = $_GET['uid'];
} else {
	$t->assign('heading', "No such user");
	$t->assign('message', "We could not find any users in the system that matched your query.");
	$t->display('error.tpl');
	die();
}

/**********************************
Get current user info
**********************************/

$sql = "SELECT * FROM user WHERE id = ? LIMIT 1";
$stmt = $db->prepare($sql, array('integer'), MDB2_PREPARE_RESULT);

$res =& $stmt->execute($profileId);
if (PEAR::isError($res)) {die($res->getMessage());}
if ( $res->numRows() < 1 ){
	$t->assign('heading', "No such user");
	$t->assign('message', "We could not find any users in the system that matched your query.");
	$t->display('error.tpl');
	die();
} else {
	$row = $res->fetchRow();

	$t->assign('email', $row['email']);
	$t->assign('real_name', $row['real_name']);
	$t->assign('website_url', $row['website_url']);
	$t->assign('about', $row['about']);
	$t->assign('member_since', strftime("%B %e, %Y", strtotime($row['creation_time'])));
	$t->assign('username', $row['username']);

	// Free the result
	$res->free();

	// Check if this user is the current user
	if( isset($_SESSION['id']) && $profileId == $_SESSION['id']){
		$t->assign('ownProfile', true);
	} else {
		$t->assign('ownProfile', false);
	}

	/**********************************
	GET USER'S SHARED STUFF (TBD)
	**********************************/

	$t->display('profile.tpl');
}
?>
