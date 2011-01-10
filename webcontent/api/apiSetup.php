<?php
// Turn this off at some point.
ini_set('display_errors', "On");

//Bring in the user's config file
require_once('/var/www/config.php');

// Include pear database handler
ini_set('include_path',$CFG->dirroot."/libs/:".ini_get('include_path'));
//require_once "$CFG->dirroot/libs/pear/MDB2.php";
require_once "MDB2.php";

// Connect to the database
$dsn = "$CFG->dbtype://$CFG->dbuser:$CFG->dbpass@$CFG->dbhost/$CFG->dbname";

$db =& MDB2::factory($dsn);
if (PEAR::isError($db)) {
		header("HTTP/1.0 503 Service Unavailable:\n"+$db->getMessage());
    die();
}

$db->setFetchMode(MDB2_FETCHMODE_ASSOC);
?>
