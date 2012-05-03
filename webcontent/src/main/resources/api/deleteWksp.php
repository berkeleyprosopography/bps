<?php
//Bring in the user's config file
require_once('apiSetup.php');

	unset($errmsg);
	if(empty($_POST['wid'])) {
		$errmsg = "Missing workspace id.";
	} else {
		$wkspid = $_POST['wid'];
	}
	if(!empty($errmsg)) {
		header("HTTP/1.0 400 Bad Request");
		echo $errmsg;
		exit();
	}
	$insertQ = "delete from workspace WHERE id=?";
	$stmt = $db->prepare($insertQ, array('integer'), MDB2_PREPARE_MANIP);
	$res =& $stmt->execute(array($wkspid));
	if (PEAR::isError($res)) {
		header("HTTP/1.0 500 Internal Server Error");
		echo $res->getMessage();
	}
	else
		header("HTTP/1.0 200 OK");

	exit();
?>
