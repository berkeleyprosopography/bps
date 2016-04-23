<?php

/* Include Files *********************/
require_once("../../libs/env.php");
require_once("../admin/authUtils.php");
// require_once("HTTP/Request2.php");
require_once "../../libs/RESTClient.php";
/*************************************/

$style_block = '<link rel="stylesheet" href="/style/SNA/style.css">';
$t->assign("style_block", $style_block);


$script_block = '<script src="/libs/d3.js.v2/d3.v2.js"></script>'."\n".'<script src="/scripts/SNA/libs/modernizr-2.0.6.min.js"></script>';
$t->assign("script_block", $script_block);




$t->display('sna.tpl');

?>






















