<?php
//Bring in the user's config file
require_once('apiSetup.php');

	unset($errmsg);
	if(empty($_POST['p']))
		$errmsg = "Missing Permission name.";
	else {
		$permname = trim($_POST['p']);
		if( strlen( $permname ) < 4 )
			$errmsg = "Invalid permission name: [".$permname."]";
		else if( preg_match( "/[^\w\s]/", $permname ))
			$errmsg = "Invalid permission name (invalid chars): [".$permname."]";
		else if(empty($_POST['d']))
			$errmsg = "Missing permission description.";
		else {
			$permdesc = trim($_POST['d']);
			if( strlen( $permdesc ) > 255 )
				$errmsg = "Invalid permission description (too long);";
			else if( preg_match( "/[^\w\-\s.:'()]/", $permdesc ))
				$errmsg = "Invalid permission description (invalid chars): [".$permdesc."]";
		}
	}
	if(!empty($errmsg)) {
		header("HTTP/1.0 400 Bad Request");
		echo $errmsg;
		exit();
	}
	$updateQ = "UPDATE permission set description='"
		.mysql_real_escape_string($permdesc)."' where name='"
		.mysql_real_escape_string($permname)."'";
	$res =& $db->query($updateQ);
	if (PEAR::isError($res)) {
		header("HTTP/1.0 500 Internal Server Error\n"+$res->getMessage());
	}
	else
		header("HTTP/1.0 200 OK");

	exit();
?>
