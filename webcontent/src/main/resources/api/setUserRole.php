<?php
//Bring in the user's config file
require_once('apiSetup.php');

	$badarg = false;
	if(empty($_POST['r']) || empty($_POST['u']) || empty($_POST['a']))
		$badarg = true;
	else {
		$rolename = $_POST['r'];
		$username = $_POST['u'];
		if(isset($_POST['ap'])) {
			$approver_id = intval($_POST['ap']);
			if($approver_id <=0 )
				unset($approver_id);
		}
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
		$insertQ = "INSERT IGNORE INTO user_roles(user_id, role_id, ";
		if( isset($approver_id)){
			$insertQ ." approver_id, creation_time)"
								." SELECT u.id, r.id, $approver_id, now() FROM role r, user u";
		} else {
			$insertQ .= " creation_time) SELECT u.id, r.id, now() FROM role r, user u";
		}
		$insertQ .= " WHERE r.name=? and u.username=?";
		$stmt = $db->prepare($insertQ, array('text','text'), MDB2_PREPARE_MANIP);
		$res =& $stmt->execute(array($rolename,$username));
	} else {
		$deleteQ = "delete from ur using user_roles ur, role r, user u"
			." where ur.role_id=r.id and ur.user_id=u.id"
			." and r.name=? and u.username=?";
		$stmt = $db->prepare($deleteQ, array('text','text'), MDB2_PREPARE_MANIP);
		$res =& $stmt->execute(array($rolename,$username));
	}
	if (PEAR::isError($res)) {
		header("HTTP/1.0 500 Internal Server Error");
		echo $res->getMessage();
		echo "\n\nQuery was:\n".$updateQ;
	}
	else
		header("HTTP/1.0 200 OK");

	exit();
?>
