<?php

require_once("../../libs/env.php");

$tempToUse = 'corpora_params.tpl';
if( isset( $_GET['v'] ) ) {
	$variant = $_GET['v'];
	if($variant=='2') {
		$tempToUse = 'corpora_params2.tpl';
	} else if($variant=='3') {
		$tempToUse = 'corpora_params3.tpl';
	}
} 
// Display template
$t->display($tempToUse);

?>
