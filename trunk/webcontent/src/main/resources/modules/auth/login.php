<?php

require_once("../../libs/env.php");

/**
 * Checks to see if the user has submitted his
 * username and password through the login form,
 * if so, checks authenticity in database and
 * creates session.
 */

$t->assign('contactEmail',$CFG->contactEmail);
$t->assign('redir',null);
$t->assign('message',null);

if(isset($_POST['sublogin'])){
	
	// In case of errors, preserve the redir path
	if(isset($_POST['redir'])){
		$t->assign('redir',$_POST['redir']);
	}

   /* Check that all fields were typed in */
	if(!$_POST['user'] || !$_POST['pass']){
		$t->assign('message','You did not fill in a required field.');
		$t->display('login.tpl');
		die();
  }
   /* Spruce up username, check length */
  $_POST['user'] = trim($_POST['user']);
  if(strlen($_POST['user']) > 40){
		$t->assign('message','Sorry, the username is longer than 40 characters, please shorten it.');
		$t->display('login.tpl');
		die();
  }

   /* Checks that username is in database and password is correct 
	  confirmUser lives in checkLogin.php
	*/
   $md5pass = md5($_POST['pass']);
   $result = confirmUser($_POST['user'], $md5pass);
   /* Check error codes */
   if($result == BPS_NO_SUCH_USER){
		$t->assign('message','That username does not exist in our database.');
		$t->display('login.tpl');
		die();
   }
   else if($result == BPS_PASSWD_WRONG){
		$t->assign('message','Incorrect password, please try again.');
		$t->display('login.tpl');
		die();
   }

   /* Username and password correct, register session variables */
   $_SESSION['username'] = stripslashes($_POST['user']);
   $_SESSION['password'] = $md5pass;

   /**
    * This is the cool part: the user has requested that we remember that
    * he's logged in, so we set two cookies. One to hold his username,
    * and one to hold his md5 encrypted password. We set them both to
    * expire in 100 days. Now, next time he comes to our site, we will
    * log him in automatically.
    */
   if(isset($_POST['remember'])){
      setcookie("cookname", $_SESSION['username'], time()+60*60*24*100, "/");
      setcookie("cookpass", $_SESSION['password'], time()+60*60*24*100, "/");
   }

	// If there is a redir path, go there. Else go to home page.
	$goTo = isset($_POST['redir'])?$_POST['redir']:$CFG->wwwroot;
	// redirect to frontpage
	header( 'Location: ' . $goTo );
	die();
}

if(isset($_GET['redir'])){
	$t->assign('redir',$_GET['redir']);
}

// Display template
$t->display('login.tpl');
die();
?>
