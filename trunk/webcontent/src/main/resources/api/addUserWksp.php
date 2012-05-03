<?php
//Bring in the user's config file
require_once('apiSetup.php');

	unset($errmsg);
	if(empty($_POST['u'])) {
		$errmsg = "Missing user.";
	} else {
		$username = $_POST['u'];
	}
	if(!empty($errmsg)) {
		header("HTTP/1.0 400 Bad Request");
		echo $errmsg;
		exit();
	}
	$insertQ = "INSERT IGNORE INTO workspace(owner_id, creation_time) "
								." SELECT u.id, now() FROM user u WHERE u.username=?";
	$stmt = $db->prepare($insertQ, array('text'), MDB2_PREPARE_MANIP);
	$res =& $stmt->execute(array($username));
	if (PEAR::isError($res)) {
		header("HTTP/1.0 500 Internal Server Error");
		echo $res->getMessage();
	}
	else
		header("HTTP/1.0 200 OK");

	exit();
?>
