<?php

require_once("./libs/env.php");

$style_block = "<style>
p.intro { font-size:1.3em; }
</style>";

$t->assign("style_block", $style_block);

// Display template
$t->display('home.tpl');

?>
