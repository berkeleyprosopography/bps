<?php

// This is a test of XSLT functionality.
//require_once("../../../libs/env.php");
//
$showForm = true;
$err = null;
$maxK = 10000;
$maxsize = $maxK * 1024;

if(isset($_POST['MAX_FILE_SIZE'])) {
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
		} // if 
	}
}
 
if($showForm){ ?>
<html><body>
<?php if($err!=null){ echo '<p class="error">'.$err.'</h3>'; } ?>
<p>Set the file to upload</p>
<form enctype="multipart/form-data" action="showNamesAndDates.php" method="POST">
    <!-- MAX_FILE_SIZE must precede the file input field -->
		<input type="hidden" name="MAX_FILE_SIZE" value="<?php echo $maxsize; ?>" />
    <!-- Name of input element determines name in $_FILES array -->
    Send this file: <input name="teifile" type="file" />
    <input type="submit" value="Send File" />
</form>
</body></html>
<?php	}
	
?>






















