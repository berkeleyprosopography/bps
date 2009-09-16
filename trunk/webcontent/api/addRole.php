<?php
// set up env, DB
require_once('apiSetup.php');

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
			else if( preg_match( "/[^\w\-\s.:'()]/", $roledesc ))
				$errmsg = "Invalid role description (invalid chars): [".$roledesc."]";
		}
	}
	if( $errmsg ) {
		header("HTTP/1.0 400 Bad Request");
		echo $errmsg;
		exit();
	}
	$updateQ = "INSERT IGNORE INTO role(name, description, creation_time)"
		." VALUES ('".mysql_real_escape_string($rolename)."', '"
		.mysql_real_escape_string($roledesc)."', now())";
	$res =& $db->query($updateQ);
	if (PEAR::isError($res)) {
		header("HTTP/1.0 500 Internal Server Error\n"+$res->getMessage());
	}
	else
		header("HTTP/1.0 200 OK");
	//echo "Query:";
	//echo $updateQ;
	exit();
?>
