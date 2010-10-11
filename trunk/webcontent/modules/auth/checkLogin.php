<?php

/**
 * Checks whether or not the given username is in the
 * database, if so it checks if the given password is
 * the same password in the database for that user.
 * If the user doesn't exist or if the passwords don't
 * match up, it returns an error code (BPS_NO_SUCH_USER or BPS_PASSWD_WRONG). 
 * On success it returns the login_id (any return > 0 is success).
 */
function confirmUser($username, $password){
	global $db;
	
   /* Add slashes if necessary (for query) */
   if(!get_magic_quotes_gpc()) {
		$username = addslashes($username);
   }

   /* Verify that user is in database */
	$sql = "SELECT id, passwdmd5, pending FROM user WHERE username = ?";
	$stmt = $db->prepare($sql, array('text'), MDB2_PREPARE_RESULT);
	$res =& $stmt->execute($username);
	if (PEAR::isError($res)) {
	    die($res->getMessage());
	}

	// If nothing is found...
	if ( $res->numRows() < 1 ){
		return BPS_NO_SUCH_USER; //Indicates username failure
	}

   $password = stripslashes($password);
   /* Retrieve password from result, strip slashes */
   $dbarray = $res->fetchRow();
   $dbpw = stripslashes($dbarray['passwdmd5']);
   $dbpend = stripslashes($dbarray['pending']);

   /* Validate that password is correct */
   if($password == $dbpw){
		 $login_id = stripslashes($dbarray['id']);
		 if(!$dbpend)
      	return $login_id; //Success! Username and password confirmed, and not pending
		 else
      	return BPS_REG_PENDING; //login is okay, but account is still pending
   }
   else
      return BPS_PASSWD_WRONG; //Indicates password failure
}

/**
 * checkLogin - Checks if the user has already previously
 * logged in, and a session with the user has already been
 * established. Also checks to see if user has been remembered.
 * If so, the database is queried to make sure of the user's 
 * authenticity. Returns true if the user has logged in.
 */
function checkLogin(){
	/* Check if user has been remembered */
	if(isset($_COOKIE['cookname']) && isset($_COOKIE['cookpass'])){
		$_SESSION['username'] = $_COOKIE['cookname'];
		$_SESSION['password'] = $_COOKIE['cookpass'];
	}

	$result = BPS_LOGGED_OUT;	// assume not logged in until we find otherwise
	/* Username and password have been set */
	if(isset($_SESSION['username']) && isset($_SESSION['password'])){
		/* Confirm that username and password are valid */
		$result = confirmUser($_SESSION['username'], $_SESSION['password']);
		if( $result >= BPS_LOGGED_IN ) {
			$_SESSION['login_id'] = $result;
			$result = BPS_LOGGED_IN;
		} elseif ( $result != BPS_REG_PENDING ){
			/* Variables are incorrect, user not logged in */
			unset($_SESSION['username']);
			unset($_SESSION['password']);
			unset($_SESSION['login_id']);
		}
	}

	return $result;

}

function getUserDetails($userName){
	global $db;
	// Query DB
	$sql = "SELECT * FROM user WHERE username = ?";
	$stmt = $db->prepare($sql, array('text'), MDB2_PREPARE_RESULT);
	$res =& $stmt->execute($userName);
	if (PEAR::isError($res)) {
	    die($res->getMessage());
	}
	
	$details = $res->fetchRow();

	// Free the result
	$res->free();

	return $details;
}

?>
