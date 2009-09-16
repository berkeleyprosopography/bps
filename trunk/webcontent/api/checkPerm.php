<?php
require_once "apiSetup.php";
require_once "../modules/admin/authUtils.php";

?>
<HTML>
<BODY>
<?php
if(empty($_GET['p'])) {
	echo "<h2>No Permission specified...</h2>";
} else if(empty($_GET['u'])) {
	echo "<h2>No User specified...</h2>";
} else {
	$user_id = $_GET['u'];
	$permname = $_GET['p'];
	echo "<h2>User DOES".(userHasPerm( $user_id, $permname )?" ":" NOT ")."HAVE Permission: ".$permname." </h2>";
}
?>
</BODY>
</HTML>
