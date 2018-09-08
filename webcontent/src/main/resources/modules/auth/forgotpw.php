<?php

require_once("../../libs/env.php");
require_once("../../libs/utils.php");


/**
 * Checks for a given user, and returns the email for that user, 
 * or FALSE if user not found.
 */
function checkUser($username){
	global $db;
	$sql = "select email from user where username = ?";
	$stmt = $db->prepare($sql, array('text'), MDB2_PREPARE_RESULT);
	$res =& $stmt->execute($username);
	if (PEAR::isError($res)) {
		return FALSE;
	    die($res->getMessage());
	}

	$row = $res->fetchRow();

	return stripslashes($row['email']);
}

/**
 * Updates the password for a given user.
 * Returns the new cleartext password, or FALSE if user not found.
 */
function synthesizeAndUpdatePassword($username){
	global $db;
	$chars='abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
	$nchars=strlen($chars);
	$newPWClear = '';
	// Generate a random string of 16 chars for the new password.
	for($i = 0; $i <= 15; $i++) 
		$newPWClear .= $chars[rand(0,$nchars-1)]; 
	$newPWmd5 = md5($newPWClear);
	$sql = "update user set passwdmd5='$newPWmd5'where username = '$username'";
	
	$affected =& $db->exec($sql);

	// check that result is not an error
	if (PEAR::isError($affected)) {
		return FALSE;
	    die($affected->getMessage());
	} else {
		return $newPWClear;
	}
}

function sendPWMail($username, $email, $newPW){
	$htmlmsg = 
		'<html><head> <title>BPS password notification</title></head>
		<body><p>Your password has been reset to: "'.$newPW
			.'". You should reset this the next time you log in.</p></body></html>';
	// The message already mentions the password, but advertising it on the
	// subject line seems even more unsafe, so be oblique.
	$subj = 'Berkeley Prosopography Services';
	return sendBPSMail($email, $subj, $htmlmsg);
}

$showErr = FALSE;			// Error to show if we find one

function checkSubmitValues(){
	global $showErr;
	/* Make sure at least one field was entered */
	if(!$_POST['user']){
		$showErr = 'Please provide a valid username.';
		return FALSE;
	}

	/* Check if username is valid */
	$email = checkUser($_POST['user']);
	if(!$email){
		$showErr = 'Sorry, the username: "<strong>'.$_POST['user']
								.'</strong>" could not be found.';
		return FALSE;
	}
	// If we get here, username is valid. Return the email address.
	return $email;
}

/* If a request has been submitted, handle it.  */
if(isset($_POST['subreq'])){
	$email = checkSubmitValues();
	if($email){
		$newPW = synthesizeAndUpdatePassword($_POST['user']);
		sendPWMail($_POST['user'], $email, $newPW);
		$t->assign('message', "Your password has been updated, and the new password will be mailed to the email account associated to your account.");
		$t->display('forgotpw.tpl');
		die();
	}
}

$t->assign('message', "$showErr");
$t->display('forgotpw.tpl');

?>

