<?php

/* Include Files *********************/
require_once("../../libs/env.php");
/*************************************/


if ($_GET["file"] == "fb"){
	$opts = array('http' =>
	    array(
	        'method'  => 'POST',
	        'header'  => "Content-type: application/xml\n"
	                   . "Accept: application/json",
	        'content' => file_get_contents('/Applications/MAMP/htdocs/data/demlong.graphml')
	    )
	);
}
else if ($_GET["file"] == "bignet"){
	$opts = array('http' =>
	    array(
	        'method'  => 'POST',
	        'header'  => "Content-type: application/xml\n"
	                   . "Accept: application/json",
	        'content' => file_get_contents('/Applications/MAMP/htdocs/data/bignet.graphml')
	    )
	);
}
else if ($_GET["file"] == "stuff"){
	$opts = array('http' =>
	    array(
	        'method'  => 'POST',
	        'header'  => "Content-type: application/xml\n"
	                   . "Accept: application/json",
	        'content' => file_get_contents('/Applications/MAMP/htdocs/data/stuff.graphml')
	    )
	);
}
else if ($_GET["file"] == "neuro"){
	$opts = array('http' =>
	    array(
	        'method'  => 'POST',
	        'header'  => "Content-type: application/xml\n"
	                   . "Accept: application/json",
	        'content' => file_get_contents('/Applications/MAMP/htdocs/data/neuro.graphml')
	    )
	);
}
else if ($_GET["file"] == "collaboration"){
	$opts = array('http' =>
	    array(
	        'method'  => 'POST',
	        'header'  => "Content-type: application/xml\n"
	                   . "Accept: application/json",
	        'content' => file_get_contents('/Applications/MAMP/htdocs/data/collaboration.graphml')
	    )
	);
}

else if ($_GET["file"] == "myfb"){
	$opts = array('http' =>
	    array(
	        'method'  => 'POST',
	        'header'  => "Content-type: application/xml\n"
	                   . "Accept: application/json",
	        'content' => file_get_contents('/Applications/MAMP/htdocs/data/myfb.graphml')
	    )
	);
}
else{
	$opts = array('http' =>
	    array(
	        'method'  => 'POST',
	        'header'  => "Content-type: application/json\n"
	                   . "Accept: application/json",
	        'content' => file_get_contents('/Applications/MAMP/htdocs/data/graph.graphml')
	    )
	);
}

$context  = stream_context_create($opts);

header('Content-Type: application/json');



if ($_GET["id"] != null){
	
	
	$json = file_get_contents($CFG->wwwroot.$CFG->svcsbase."/sna/cluster/neighborhood/".$_GET["id"], false, $context);
}
else{$json = file_get_contents($CFG->wwwroot.$CFG->svcsbase."/sna/all", false, $context);}

$json = json_decode($json);

$output_json = array(
	'directed' => true,
	'graph' => array(),
	'nodes' => array(),
	'links' => array(),
	'multigraph' => false,
);

$internal_ids = array();

foreach ($json->graph->node as $node) {
	$output_node = array(
		'id' => (int) $node->{'@id'},
	);
	foreach ($node->data as $node_data) {
		$output_node[$node_data->{'@key'}] = $node_data->{'$'};
		
		if ($node_data->{'@key'} == 'GMLid') {
			$internal_ids[$node_data->{'$'}] = (int) $node->{'@id'};
		}
	}
	$output_json['nodes'][] = $output_node;
}

foreach ($json->graph->edge as $edge) {
	$output_edge = array(
		'source' => $internal_ids[$edge->{'@source'}],
		'target' => $internal_ids[$edge->{'@target'}],
		'directed' => $edge->{'@directed'},
	);
	foreach ($edge->data as $edge_data) {
		$output_edge[$edge_data->{'@key'}] = $edge_data->{'$'};
	}
	$output_json['links'][] = $output_edge;
}

print json_encode($output_json);
	

?>