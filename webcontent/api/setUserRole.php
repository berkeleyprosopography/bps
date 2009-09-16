<?php
//Bring in the user's config file
require_once('apiSetup.php');

	$badarg = false;
	if(empty($_POST['r']) || empty($_POST['u']) || empty($_POST['a']))
		$badarg = true;
	else {
		$rolename = $_POST['r'];
		$username = $_POST['u'];
		if(isset($_POST['ap']))
			$approver_id = $_POST['ap'];
		$action = $_POST['a'];
		if( $action!='set' && $action!='unset' ) 
			$badarg = true;
	}
	if( $badarg ) {
		header("HTTP/1.0 400 Bad Request");
		echo "Bad Args: r[".$rolename."] p[".$username."] a[".$action."]";
		echo "POST: ";
		print_r( $_POST );
		exit();
	}
	if( $action == 'set' ) {
		$updateQ = "insert ignore into user_roles(user_id, role_id, ";
		if( isset($approver_id)){
			$updateQ .= "approver_id, creation_time)"
				." select u.id, r.id, ".$approver_id.", now()";
		} else {
			$updateQ .= "creation_time) select u.id, r.id, now()";
		}
		$updateQ .= " from role r, user u"
								." where r.name='".$rolename."' and u.username='".$username."'";
	} else {
		$updateQ = "delete from ur using user_roles ur, role r, user u"
			." where ur.role_id=r.id and ur.user_id=u.id"
			." and r.name='".$rolename."' and u.username='".$username."'";
	}
  $res =& $db->query($updateQ);
	if (PEAR::isError($res)) {
		header("HTTP/1.0 500 Internal Server Error\n"+$res->getMessage());
	}
	else
		header("HTTP/1.0 200 OK");

	exit();
?>
