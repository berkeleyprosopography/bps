<?php
//Bring in the user's config file
require_once('apiSetup.php');

	$badarg = false;
	if(empty($_POST['r']) || empty($_POST['p']) || empty($_POST['a']))
		$badarg = true;
	else {
		$rolename = $_POST['r'];
		$permname = $_POST['p'];
		$action = $_POST['a'];
		if( $action!='set' && $action!='unset' ) 
			$badarg = true;
	}
	if( $badarg ) {
		header("HTTP/1.0 400 Bad Request");
		echo "Bad Args: r[".$rolename."] p[".$permname."] a[".$action."]";
		echo "POST: ";
		print_r( $_POST );
		exit();
	}
	if( $action == 'set' ) {
		$updateQ = "insert ignore into role_perms(role_id, perm_id, creation_time)"
			." select r.id, p.id, now() from role r, permission p"
			." where r.name=? and p.name=?";
		$stmt = $db->prepare($updateQ, array('text','text'), MDB2_PREPARE_MANIP);
		$res =& $stmt->execute(array($rolename,$permname));
	} else {
		$deleteQ = "delete from rp using role_perms rp, role r, permission p"
			." where rp.role_id=r.id and rp.perm_id=p.id"
			." and r.name=? and p.name=?";
		$stmt = $db->prepare($deleteQ, array('text','text'), MDB2_PREPARE_MANIP);
		$res =& $stmt->execute(array($rolename,$permname));
	}
	if (PEAR::isError($res)) {
		header("HTTP/1.0 500 Internal Server Error\n"+$res->getMessage());
	}
	else
		header("HTTP/1.0 200 OK");
	//echo "Query:";
	//echo $updateQ;
	exit();
?>
