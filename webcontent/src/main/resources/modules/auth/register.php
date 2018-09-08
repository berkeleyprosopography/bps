<?php

require_once("../../libs/env.php");
require_once($CFG->dirroot."/libs/utils.php");


$t->assign('messages', null);
$t->assign('email', null);
$t->assign('user', null);
$t->assign('supportOpenReg', $CFG->supportOpenReg);
/**
 * Confirms a user's registration, clearing the pending flag in the DB
 */
function confirmRegistration($userid){
   global $db;
	$sql =	"	UPDATE user 
				SET pending = 0
				WHERE id = '$userid'
			";

	$affected =& $db->exec($sql);

	// check that result is not an error
	if (PEAR::isError($affected)) {
	    die($affected->getMessage());
	} else {
		return true;
	}

}

/**
 * Returns true if the username has been taken
 * by another user, false otherwise.
 */
function usernameTaken($username){
   global $db;

   if(!get_magic_quotes_gpc()){
      $username = addslashes($username);
   }

	$sql = "SELECT username FROM user WHERE username=? ";
	$stmt = $db->prepare($sql, array('text'), MDB2_PREPARE_RESULT);
	$res =& $stmt->execute($username);
	if (PEAR::isError($res)) {
	    die($res->getMessage());
	}
	
   	// If nothing is found, username is available
	if ( $res->numRows() < 1 ){
		return false;
	} else {
		return true;
	}
}

function sendRegMail($uid, $username, $email){
	global $CFG;
	if(!$CFG->supportEmail)
		return;

	$confirmUrl = $CFG->wwwroot . '/register?confirm=' . $uid;
	$htmlmsg = 
		'<html><head> <title>BPS registration notification</title></head>
		<body><p>Thank you for registering with BPS!</p><p>Your username is: '.$username.'</p>'
	   .'<p>If you forget your password, you can ask the system' 
	   .' to email it to you. You can also change it on your profile page.</p>'
	   .'<p>Click on the link below '
		 .'or copy and paste the URL into your browser to complete the registration.'
		 .'<br /><br /><a href="'.$confirmUrl.'">'.$confirmUrl.'</a></p></body></html>';
	$subj = 'Berkeley Prosopography Services: BPS registration';
	return sendBPSMail($email, $subj, $htmlmsg);
}

/**
 * Inserts the given (username, password) pair
 * into the database. Returns true on success,
 * false otherwise.
 */
function addNewUser($username, $password, $email){
   global $db;
   $md5pass = md5($password);
	 // Note that 'pending' defaults to true on INSERT
	 $sql = 'INSERT INTO user ( username, passwdmd5, email, creation_time )'
          ." VALUES ('$username', '$md5pass', '$email', now() )";

	$affected =& $db->exec($sql);
	if (PEAR::isError($affected)) {
	    die($res->getMessage());
	}
	
	 if($affected) {
		$sql = "SELECT * FROM user WHERE username=?";
		$stmt = $db->prepare($sql, array('text'), MDB2_PREPARE_RESULT);
		$res =& $stmt->execute($username);
	
		if (PEAR::isError($res)) {
		    die($res->getMessage());
		}
		
		$row = $res->fetchRow();
		$new_uid = $row['id'];

		/* TODO Create a default workspace for the user */
		$name = "My Workspace";
		$description = "This workspace was created for you automatically when you registered. Click the Admin link to change this description.";
		$insertQ = "INSERT IGNORE INTO workspace(name, description, owner_id, creation_time) "
									." VALUES( ?, ?, ?, now())";
		$stmt = $db->prepare($insertQ, array('text', 'text', 'integer'), MDB2_PREPARE_MANIP);
		$res =& $stmt->execute(array($name, $description, $new_uid));
		if (PEAR::isError($res)) {
			die("Problem creating workspace for new user.\n".$res->getMessage());
		}
		
		// Send confirmation email
		sendRegMail($new_uid, $username, $email);
		
		return $new_uid;
		
	 }
	return;
}

/**
 * Displays the appropriate message to the user
 * after the registration attempt. It displays a 
 * success or failure status depending on a
 * session variable set during registration.
 */
function displayStatus(){
	global $t;
	if ( $_SESSION['reguid'] > 0 ) {
		$t->assign('header','Registered!');	
		$t->assign('message','Thank you, your information has been added to the database. You will receive an email with a confirmation link. Click on the link, or copy the URL into your browser to confirm your registration.');
		$t->display('registerConfirm.tpl');
		die();
	} else {
		unset($_SESSION['reguname']);
		unset($_SESSION['registered']);
		unset($_SESSION['reguid']);
		$t->assign('header','Registration Failed');	
		$t->assign('message','We are sorry, but an error has occurred and your registration could not be completed. Please try again at a later time.');
		$t->display('registerConfirm.tpl');
		die();
	}

}


function checkSubmitValues(){
	// Errors to show if we find any
	$msg = array();
	
	global $t;

	// reassign vars to user input in case we need to send them back to fix something.
	$t->assign('email', cleanFormData($_POST['email']));
	$t->assign('user', cleanFormData($_POST['user']));
	
	if(strlen($_POST['pass']) < 6 ){
		array_push($msg, "Your password must be at least 6 characters.");
	}

	if(strlen($_POST['pass']) > 25 ){
		array_push($msg, "Your password cannot be more than 25 characters.");
	}

	if($_POST['pass'] != $_POST['pass2']){
		array_push($msg, "Your retyped password did not match the first typed password.");
	}

	/* Spruce up username, check length */
	if(strlen(stripslashes($_POST['user'])) > 40 || strlen(stripslashes($_POST['user'])) < 3){
		array_push($msg, "Username must be between 3 and 40 characters.");
	} elseif(!preg_match('|^[a-zA-Z0-9-_]+$|i', $_POST['user'])){
		array_push($msg, "Username can only contain letters, numbers, hyphens, and underscores");
	} elseif(usernameTaken($_POST['user'])){
		array_push($msg, "The username \"".cleanFormData($_POST['user'])."\"is already taken. Please pick another one.");
	}
	
	/* Check if email is valid */
	if(!emailValid($_POST['email'])){
		array_push($msg, "Email address is not valid.");
	}


	
	if(count($msg) > 0){
		$t->assign('messages', $msg);
		$t->display('register.tpl');
		die();
	} else {
		return true;
	}
}

/**
 * Determines whether or not to show to sign-up form
 * based on whether the form has been submitted, if it
 * has, check the database for consistency and create
 * the new account.
 */
if(isset($_POST['subjoin'])){
	if( checkSubmitValues() ) {
		/* Add the new account to the database */
		$_SESSION['username'] = $_POST['user'];
		$_SESSION['password'] = md5($_POST['pass']);
		$_SESSION['reguid'] = addNewUser(trim($_POST['user']), trim($_POST['pass']), trim($_POST['email']));
		$_SESSION['registered'] = true;
		// Redirect to the home page
		header( 'Location: ' . $CFG->wwwroot . '/' );
		return;
	}
	// Otherwise will fall through to show the form, with the error set.
}
else if(isset($_GET['confirm'])){
	if( confirmRegistration($_GET['confirm']) ) {
		$t->assign('message','Thank you for confirming your registration.');
		$t->display('registerConfirm.tpl');
		die();
	} else {
		$t->assign('message','This registration is no longer valid. Please begin your registration again.');
		$t->display('registerConfirm.tpl');
		die();
	}
}
else if(isset($_SESSION['registered'])){
	/**
	 * This is the page that will be displayed after the
	 * registration has been attempted.
	 * This isn't being used at the moment. Rather user is just redirected.
	 */
	displayStatus();
}

// Display template
$t->display('register.tpl');
die();

?>
