<?php

//Bring in the user's config file
require_once('/var/www/config.php');
// This uses the XSLT BPSTEINames.xsl to process an uploaded TEI file, 
// returning an HTML summary of the names and dates found in the file. 
// The uploaded file file is passed in $_FILES['teifile']

$err = null;
$maxK = 10000;
$maxsize = $maxK * 1024;

if(!isset($_POST['MAX_FILE_SIZE'])) {
	$err = "API must be called from a file upload form.";
} else {
	//print_r($_FILES['teifile']);
	if($_FILES['teifile']['error'] == 2) {
		$err = "The uploaded file is too large - files cannot exceed ".$maxK." Kilobytes";
	} else if($_FILES['teifile']['type'] != 'text/xml') {
		$err = "You must specify an XML file to process (the uploaded file does not appear to be XML)";
	} else if($_FILES['teifile']['error'] != 0) {
		$err = "There was an unknown problem uploading the file.";
	} else {
		// Copy the file to the known corpus location
		$copyToDir = $CFG->corpusdir.'/'.$_POST['id'].'/tei';
		$copyDest = $copyToDir.'/corpus.xml';

		if(!is_dir($copyToDir) && !mkdir($copyToDir, 0777, true)) {
			$err = "BPS not properly installed: Cannot set up the corpora directory: ".$copyToDir;
		} else if(!move_uploaded_file($_FILES["teifile"]["tmp_name"], $copyDest )) {
			$err = "Could not copy uploaded file to: ".$copyDest.
				" Ensure BPS is properly installed, and there is capacity on the server.";
		} else {
			//echo $CFG->wwwroot.'/corpora/corpus?id='.$_POST['id'];
			header('Location: '.$CFG->wwwroot.'/corpora/corpus?id='.$_POST['id']);
		}
	}
}

if(!empty($err)) {
	echo '<html><body><h3 class="error">'.$err.'</h3></body></html>'; 
}
?>






















