<?php
//Bring in the user's config file
require_once('apiSetup.php');

	unset($errmsg);
	if(empty($_POST['r']))
		$errmsg = "Missing role name.";
	else {
		$rolename = trim($_POST['r']);
		if( strlen( $rolename ) < 4 )
			$errmsg = "Invalid role name: [".$rolename."]";
		else if( preg_match( "/[^\w\s]/", $rolename ))
			$errmsg = "Invalid role name (invalid chars): [".$rolename."]";
		else if(empty($_POST['d']))
			$errmsg = "Missing role description.";
		else {
			$roledesc = trim($_POST['d']);
			if( strlen( $roledesc ) > 255 )
				$errmsg = "Invalid role description (too long);";
			else if( preg_match( "/[^\w\-\s,.:'()]/", $roledesc ))
				$errmsg = "Invalid role description (invalid chars): [".$roledesc."]";
		}
	}
	if(!empty($errmsg)) {
		header("HTTP/1.0 400 Bad Request");
		echo $errmsg;
		exit();
	}
	$updateQ = "UPDATE role set description='"
		.addslashes($roledesc)."' where name='"
		.addslashes($rolename)."'";
	$res =& $db->query($updateQ);
	if (PEAR::isError($res)) {
		header("HTTP/1.0 500 Internal Server Error\n"+$res->getMessage());
	}
	else
		header("HTTP/1.0 200 OK");

	exit();
?>
