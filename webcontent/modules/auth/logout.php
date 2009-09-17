<?php
require_once("../../libs/env.php");


/**
 * Delete cookies - the time must be in the past,
 * so just negate what you added when creating the
 * cookie.
 */
if(isset($_COOKIE['cookname']) && isset($_COOKIE['cookpass'])){
   setcookie("cookname", "", time()-60*60*24*100, "/");
   setcookie("cookpass", "", time()-60*60*24*100, "/");
}

$_SESSION = array(); // reset session array
session_destroy();   // destroy session.

// redirect to home - count on redirect to do the right thing
header( 'Location: ' . $CFG->wwwroot . '/' );

?>
