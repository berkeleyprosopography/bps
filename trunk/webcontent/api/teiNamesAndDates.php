<?php

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
		$xp = new XsltProcessor();
		// create a DOM document and load the XSL stylesheet
		$xsl = new DomDocument;
		$xsl->load('BPSTEINames.xsl');
		
		// import the XSL styelsheet into the XSLT process
		$xp->importStylesheet($xsl);

		// create a DOM document and load the XML datat
		$xml_doc = new DomDocument;
		//$xml_doc->load('../../../data/corpus.xml');
		$xml_doc->load($_FILES['teifile']['tmp_name']);

		// transform the XML into HTML using the XSL file
		if ($html = $xp->transformToXML($xml_doc)) {
				$showForm = false;
				echo $html;
		} else {
				trigger_error('XSL transformation failed.', E_USER_ERROR);
		} 
	}
}

if(!empty($err)) {
	echo '<html><body><h3 class="error">'.$err.'</h3></body></html>'; 
}
?>






















