<?php

/* Include Files *********************/
require_once("../../libs/env.php");
require_once("../admin/authUtils.php");
require_once "../../libs/RESTClient.php";
/*************************************/

// If the user isn't logged in, send to the login page.
// FIXME - allow not logged in state - will be all no-edit
if(($login_state != BPS_LOGGED_IN) && ($login_state != BPS_REG_PENDING)){
	header( 'Location: ' . $CFG->wwwroot .
					'/login?redir=' .$_SERVER['REQUEST_URI'] );
	die();
}

$t->assign('page_title', 'Workspace-SNA visualization'.$CFG->page_title_default);

$opts = array('http' =>
	array(
		'method'  => 'GET',
		'header'  => "Content-type: application/xml\n"
						. "Accept: application/json",
	)
);

function getWkspGraphUrl($CFG,$wid,$graphMLFilename){
	$urlVal = $CFG->serverwwwroot.$CFG->svcsbase."/workspaces/".$wid."/graph";
	if(isset($graphMLFilename))
		$urlVal = $urlVal."?stub=".$graphMLFilename;
	return $urlVal;
}

// TODO Rewrite this to use the RESTClient class. 

$output_json = array(
	//'directed' => true,
	'directed' => false,	// GraphML produced by services is undirected. Ideally should get from graphML
	'graph' => array(),
	'nodes' => array(),
	'links' => array(),
	'multigraph' => false,
);

if(!isset($_GET['wid'])) {
	$errmsg = "Missing workspace or document specifier(s).";
} else {
	$wid = $_GET['wid'];
	$graphMLFile = isset($_GET['gmlf'])?$_GET['gmlf']:null;
	$context  = stream_context_create($opts);
	$jjson = file_get_contents(getWkspGraphUrl($CFG, $wid, $graphMLFile), false, $context);

	$json = json_decode($jjson);

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

}

// No good way to return an error - this should be rewritten
print json_encode($output_json);


?>
