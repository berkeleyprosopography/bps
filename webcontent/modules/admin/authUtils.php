<?php 
/* Include Files *********************/
// Depends upon libs/env.php or equivalent to set up DB connection.
// Caller must include.
/*************************************/

function currUserHasPerm( $perm ) {
	if( isset($_SESSION['id']) )
		return userHasPerm( $_SESSION['id'], $perm );
	return false;
}

function userHasPerm($user_id, $perm) {
	global $db;
	if(( !isset($user_id) || $user_id < 0 )
		|| !isset($perm))
		return false;

	$retVal = false;
   /* Get all the users and their assigned perms */
	$q = "select u.username name from user u, permission p, user_roles ur, role_perms rp
		where u.id = ? and u.id=ur.user_id and ur.role_id=rp.role_id and rp.perm_id=p.id
		and p.name like ?";
	$stmt = $db->prepare($q, array('integer', 'text'), MDB2_PREPARE_RESULT);
	$res =& $stmt->execute(array($user_id, $perm));
	if (!(PEAR::isError($res))) {
		if(($row = $res->fetchRow()) && isset( $row['name'] ))
			$retVal = true;
		// Free the result
		$res->free();
	}
	return $retVal;
}

function currUserHasRole( $role ) {
	if( isset($_SESSION['id']) && $_SESSION['id'] >= 0 )
		return userHasRole( $_SESSION['id'], $role );
	return false;
}

function userHasRole($user_id, $role) {
	global $db;
	if(( !isset($user_id) || $user_id < 0 )
		|| !isset($role))
		return false;

	$retVal = false;
   /* Get all the users and their assigned perms */
	$q = "select u.username name from user u, user_roles ur, role r
		where u.id=? and u.id=ur.user_id and ur.role_id=r.id
		and r.name like ?";
	$stmt = $db->prepare($q, array('integer', 'text'), MDB2_PREPARE_RESULT);
	$res =& $stmt->execute(array($user_id, $role));
	if (!(PEAR::isError($res))) {
		if(($row = $res->fetchRow()) && isset( $row['name'] ))
			$retVal = true;
		// Free the result
		$res->free();
	}
	return $retVal;
}
?>
