<?php

require_once("../../libs/env.php");
require_once("../common/imgthumb.php");

// If there is no id param in the url, send to object not found.
if( isset( $_GET['sid'] ) ) {
	$setId = $_GET['sid'];
} else {
	$t->assign('heading', "Whoops!");
	$t->assign('message', "We could not find the set you were looking for!");
	$t->display('error.tpl');
	die;
}

/**********************************
FETCH SET DETAILS
***********************************/

// Query DB
$sql = 	"	SELECT sets.id, sets.name, sets.description, user.username, sets.owner_id, sets.policy
			FROM sets
			LEFT JOIN user
			ON sets.owner_id = user.id
			WHERE sets.id = $setId
			LIMIT 1
		";

$res =& $db->query($sql);
if (PEAR::isError($res)) {
    die($res->getMessage());
}

// If nothing is found...
if ( $res->numRows() < 1 ){
	$t->assign('heading', "Whoops!");
	$t->assign('message', "We could not find the set you were looking for.");
	$t->display('error.tpl');
	die;
}

// Assign vars to template
while ($row = $res->fetchRow()) {
    $t->assign('setId', $row['id']);
    $t->assign('setName', $row['name']);
    $t->assign('page_title', 'Set: '.$row['name'].' - '.$CFG->page_title_default);
    $t->assign('setDescription', $row['description']);
    $t->assign('username', $row['username']);
	$t->assign('owner_id', $row['owner_id']);
	$t->assign('policy', $row['policy']);
}

if(isset($_SESSION['id']) && $row['owner_id'] == $_SESSION['id']){
	$t->assign('ownSet', true);
} else {
	$t->assign('ownSet', false);
}


// Free the result
$res->free();

/**********************************
FETCH SET OBJECTS
***********************************/

// Query DB

$sql =	"	SELECT objects.id, objects.name, objects.description, objects.img_path, objects.img_ar, set_objs.order_num
			FROM set_objs 
			LEFT JOIN objects
			ON objects.id = set_objs.obj_id
			WHERE set_objs.set_id = $setId
			ORDER BY set_objs.order_num
		";

$res =& $db->query($sql);
if (PEAR::isError($res)) {
    die($res->getMessage());
}

// If nothing is found...
if ( $res->numRows() < 1 ){
	$t->assign('setHasObjects', false);
	$t->assign('objectCount', null);
	$t->assign('objects', null); 
	$t->assign('firstObjectID', null);
} else {
	$t->assign('setHasObjects', true);
	$objects = array();
	while ($row = $res->fetchRow()) {
		$imageOptions = array(	'img_path' => $row['img_path'],
								'size' => 50,
								'img_ar' => $row['img_ar'],
								'vAlign' => "center",
								'hAlign' => "left"
							);
		$object = array(	'id' => $row['id'], 
							'name' => $row['name'],
							'description' => $row['description'],
							'thumb' => outputSimpleImage($imageOptions)
						);
		array_push($objects, $object);
	}
	$t->assign('objectCount', $res->numRows());
	$t->assign('objects', $objects); 
	$t->assign('firstObjectID', $objects[0]['id']);
}

$res->free();


/**********************************
DISPLAY TEMPLATE
***********************************/
$t->assign("templateVarsJSON", json_encode($t->_tpl_vars));
$t->display('viewset.tpl');

?>







