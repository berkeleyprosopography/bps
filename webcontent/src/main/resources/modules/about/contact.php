<?php

require_once("../../libs/env.php");
require_once($CFG->dirroot."/libs/utils.php");

$mailto = base64_encode("mailto:".$CFG->contactEmail."&subject=Feedback/Question on BPS");
$script_block = '
<script>
function contact_cl() {
	var us = "'.$mailto.'";
	window.location.href = ""+window.atob(us);
}
</script>';
$t->assign("script_block", $script_block);

$t->display('contact.tpl');

?>
