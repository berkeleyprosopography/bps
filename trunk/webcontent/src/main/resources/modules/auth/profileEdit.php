<?php

require_once("../../libs/env.php");
require_once("../../libs/utils.php");


/**
 * Checks for a given user, and returns the info for that user, 
 * or FALSE if user not found.
 */
function getUserInfo(){
	global $db;
	// Get current user info
	$id = $_SESSION['id'];
	if(!is_numeric($id))
		die("Illegal value set for the user ID in _SESSION");

	$sql = "SELECT * FROM user WHERE id=?";
	$stmt = $db->prepare($sql, array('integer'), MDB2_PREPARE_RESULT);
	
	$res =& $stmt->execute($id);
	if (PEAR::isError($res)) {
		die($res->getMessage());
	}
	// If nothing is found, username is available
	if ( $res->numRows() < 1 ){
		return false;
	} else {
		$row = $res->fetchRow();
		return $row;
	}
}

function updateField($field, $value){
	global $db;
	$id = $_SESSION['id'];
	if(!is_numeric($id))
		die("Illegal value set for the user ID in _SESSION");
	$sql = "UPDATE user SET $field=? WHERE id=?";
	$stmt = $db->prepare($sql, array('text','integer'), MDB2_PREPARE_MANIP);
	$res =& $stmt->execute(array($value,$id));

	// check that result is not an error
	if (PEAR::isError($res)) {
		die(print_r($res));
	    return false;
	} else {
		return true;
	}
}

// Errors to show if we find any
$msg = array();
$t->assign('messages', $msg);

// If the user isn't logged in, send to the login page.
if(($login_state != BPS_LOGGED_IN) && ($login_state != BPS_REG_PENDING)){
	header( 'Location: ' . $CFG->wwwroot . '/modules/auth/login.php' );
	die();
}


// Fetch an array of user data to get updated values
$userData = getUserInfo();
if(empty($userData)) {
	$t->assign('email', '');
	$t->assign('real_name', '');
	$t->assign('website_url', '');
	$t->assign('about', '');
} else {
	$t->assign('email', $userData['email']);
	$t->assign('real_name', $userData['real_name']);
	$t->assign('website_url', $userData['website_url']);
	$t->assign('about', $userData['about']);
}

/* If a request has been submitted, handle it.  */
if(isset($_POST['subreq'])){
	
	// clean form input
	$_POST['about'] = cleanFormData($_POST['about']);
	$_POST['website_url'] = cleanFormData($_POST['website_url']);
	$_POST['real_name'] = cleanFormData($_POST['real_name']);
	
	// reassign vars to user input
	$t->assign('email', $_POST['email']);
	$t->assign('real_name', $_POST['real_name']);
	$t->assign('website_url', $_POST['website_url']);
	$t->assign('about', $_POST['about']);
	
	if(isset($_POST['pass']) && (strlen($_POST['pass']) > 0)){
		$md5pass = md5($_POST['pass']);
		if($md5pass != $userData['passwdmd5']) {
			if(!passValid($_POST['pass'],$_POST['pass2'])){
				if(strlen($_POST['pass']) < 6 ){
					array_push($msg, "Your password must be at least 6 characters.");
				}
				if($_POST['pass'] != $_POST['pass2']){
					array_push($msg, "Password does not match confirmation.");
				}
			} else if(!updateField("passwdmd5", $md5pass)){
				array_push($msg, "Error trying to update password.");
			}
		}
	}
	if($_POST['email'] != $userData['email']) {
		if(!emailValid($_POST['email'])){
			array_push($msg, "Email address is not valid.");
			$t->assign('email', cleanFormData($_POST['email']));
		} else if(!updateField("email", $_POST['email'])){
			array_push($msg, "Error trying to update email.");
		}
	}
	if($_POST['real_name'] != $userData['real_name']) {
		if(!real_nameValid($_POST['real_name'])){
			array_push($msg, "Your real name is not valid.");
		} else if(!updateField("real_name", $_POST['real_name'])){
			array_push($msg, "Error trying to update your real name.");
		}	
	}
	if($_POST['website_url'] != $userData['website_url']) {
		
		if(!preg_match('|^http(s)?://|i', $_POST['website_url'])){
			$_POST['website_url'] = "http://" . $_POST['website_url'];
			$t->assign('website_url', $_POST['website_url']);
		}
		if(!website_urlValid($_POST['website_url'])){
			array_push($msg, "Website URL is not valid.");
		} else if(!updateField("website_url", $_POST['website_url'])){
			array_push($msg, "Error trying to update your website.");
		}
	}
	if($_POST['about'] != $userData['about']) {
		if(!aboutValid($_POST['about'])){
			array_push($msg, "About text is not valid.");
		} else if(!updateField("about", $_POST['about'])){
			array_push($msg, "Error trying to update your about text.");
		}
	}
	
	if(count($msg) > 0){
		$t->assign('messages', $msg);
	} else {
		header( 'Location: ' . $CFG->wwwroot . '/modules/auth/profile.php?uid=' . $_SESSION['id'] );
		die();
	}
	
}

$t->display('profileEdit.tpl');
?>

