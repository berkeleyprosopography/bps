<?php

require_once("../../libs/env.php");
/**********************************
GET ALL CORPORA IN SYSTEM
**********************************/

$t->assign('page_title', 'Corpora'.$CFG->page_title_default);

// Get all the corpora, with doc counts, and order by when added
$sql = 	'	SELECT c.id, c.name, c.description, c.creation_time, count(*) nDocs'
			 .' FROM corpus c JOIN document d ON d.corpus_id=c.id GROUP BY c.id'
			 .' ORDER BY c.creation_time';

$res =& $db->query($sql);
if (PEAR::isError($res)) {
    die($res->getMessage());
}

// If nothing is found, send to object not found.
if ( $res->numRows() < 1 ){
	$t->assign('heading', "Error");
	$t->assign('message', "No corpora loaded into BPS system!");	
	$t->display('error.tpl');
	die;
}

$corpora = array();

while ($row = $res->fetchRow()) {
  $corpus	= array( 
		'id' = $row['id'],
		...
	
	
	$imageOptions = array(	'img_path' => $img_path,
							'size' => 118,
							'img_ar' => $img_ar,
							'linkURL' => $CFG->shortbase."/set/".$row['id'],
							'vAlign' => "center",
							'hAlign' => "left"
						);
	
	$set = array(	'set_id' => $row['id'], 
					'set_name' => $row['name'], 
					'total_objects' => $total_object,
					'thumb' => outputSimpleImage($imageOptions),
					'setHasObjects' => $setHasObjects
				);
	array_push($sets, $set);
    
}

// Free the result
$res->free();


// Display template
$t->assign('sets', $sets);
$t->assign("templateVarsJSON", json_encode($t->_tpl_vars));
$t->display('mysets.tpl');

?>






















