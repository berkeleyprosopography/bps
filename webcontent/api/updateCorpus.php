<?php
//Bring in the user's config file
require_once('apiSetup.php');

	unset($errmsg);
	if(empty($_POST['id']))
		$errmsg = "Missing corpus id.";
	else {
		$corpusid = trim($_POST['id']);
		if(empty($_POST['d']))
			$errmsg = "Missing corpus description.";
		else {
			$desc = trim($_POST['d']);
			if( strlen( $desc ) > 255 )
				$errmsg = "Invalid corpus description (too long);";
			else if( preg_match( "/[^\w\-\s.,;:'()]/", $desc ))
				$errmsg = "Invalid corpus description (invalid chars): [".$desc."]";
		}
	}
	if(!empty($errmsg)) {
		header("HTTP/1.0 400 Bad Request");
		echo $errmsg;
		exit();
	}
	$updateQ = "UPDATE corpus set description='"
		.addslashes($desc)."' where id='"
		.addslashes($corpusid)."'";
	$res =& $db->query($updateQ);
	if (PEAR::isError($res)) {
		header("HTTP/1.0 500 Internal Server Error");
		echo $res->getMessage();
	}
	else
		header("HTTP/1.0 200 OK");

	exit();
?>
