<?php
/**
 * Returns true if the email looks valid
 */
function emailValid($email){
	if (preg_match("/^[_a-z0-9-+]+(\.[_a-z0-9-+]+)*(\.{0,1})@[_a-z0-9-]+(\.[_a-z0-9-]+)*(\.[a-z]{2,3}|\.info|\.gouv|\.name|\.museum)$/i", $email)){
		return true;
	} else{
		return false;		
	}
}

function website_urlValid($website_url){
	if (preg_match('|^http(s)?://[a-z0-9-]+(.[a-z0-9-]+)*(:[0-9]+)?(/.*)?$|i', $website_url)){
		return true;
	} else{
		return false;		
	}
}

function real_nameValid($realName){
	return true;
}

function aboutValid($about){
	return true;
}

function passValid($pass, $pass2){
	if(strlen($pass) >= 6 && $pass == $pass2){
		return true;
	} else {
		return false;
	}
}

function cleanFormData($data){
	return htmlentities(stripslashes(trim($data)), ENT_QUOTES, "UTF-8");
}

// This is like cleanFormData except that it will allow certain html tags.
// It will alow strip out all content within certain dangerous tags (script, etc.).
// function cleanFormDataAllowHTML($str, $allow_safeFormat, $allow_hyperlink, $allow_tags )
function cleanFormDataAllowHTML($str) {
	// TODO Tags for which we should remove all content as well as the tags.
	//$dangerousTags = array('script', 'style', 'title', 'xml' );

	// Tags we'll allow.
	$safeFormatTags = array('b', 'i', 'strong', 'em', 'br', 'hr', 'strike' );
	// $hyperLinkTag = 'a';
	// $safeHyperLinkProtocols = array('http', 'mailto' );

	// $keys = array_keys($safeFormatTags);

	$str = stripslashes($str);
	// First, close up all space around the angle brackets.
	$str = eregi_replace("<[[:space:]]*([^>]*)[[:space:]]*>","<\\1>",$str);
	// TODO If we allow hyperlinks, then tighten up the format and constrain args. REVIEW 
	//$str = eregi_replace("<a([^>]*)href=\"?([^\"]*)\"?([^>]*)>","<a href=\"\\2\">", $str);

	$tmp = '';
	// Find the first tag in the string
	while(eregi("<([^> ]*)([^>]*)>",$str,$reg))	{
		$i = strpos($str,$reg[0]);
		$l = strlen($reg[0]);
		// Fold tag string for compare
		if($reg[1][0] == "/") {	// Close tag?
			$tag = strtolower(substr($reg[1],1));
			$closetag = true;
		}	else {
			$tag = strtolower($reg[1]);
			$closetag = false;
		}

		if(in_array($tag, $safeFormatTags)) {
		//if( false ) {
			if($closetag)
				$tag = "</$tag>";
			else
				$tag = "<$tag>";
		// TODO - put in code to catch dangerous tags and remove them and content.
		} elseif( $tag == 'script' ) {
			$tag = '';	// elide the tag
			$pos = strpos( $str, '</script>' );
			if( $pos === false ) {
				// No closing tag, so toss the rest of the string
				$l = 0;
				$str = substr($str,0,$i);
			} else {
				// Strip everything up to the end of the closing tag
				$l = ($pos-$i)+strlen( '</script>' );
			}
		} else
			$tag = '';	// elide the tag

		// Append the string up to the tag and the filtered tag string
		// Need to ensure we safely store entities in the DB. 
		// But html_entity_decode misses some important ones, including
		//  mdash, ndash.
		$tmp .= htmlentities(allEntitiesDecode(substr($str,0,$i), ENT_QUOTES, ""), ENT_QUOTES, "UTF-8") . $tag;
		
		// Reset the string
		$str = substr($str,$i+$l);
	}

	// Append the end of the string
	$str = $tmp .  htmlentities(allEntitiesDecode($str, ENT_QUOTES, ""), ENT_QUOTES, "UTF-8");

	// Squash PHP tags unconditionally
	$str = ereg_replace("<\?","",$str);

	// Squash comment tags unconditionally
	$str = ereg_replace("<!--","",$str);

	return $str;
}

// Since PHP's html_entity_decode misses some things, we'll work around it
function allEntitiesDecode( $str, $mode, $charset ) {
	$s = html_entity_decode( $str, $mode, $charset );
	if(!(strpos( $s, '&' )===false)) {
		$s = str_replace('&mdash;', chr(151), $s);
		$s = str_replace('&ndash;', chr(150), $s);
		$s = str_replace('&rdquo;', chr(148), $s);
		$s = str_replace('&ldquo;', chr(147), $s);
		$s = str_replace('&rsquo;', chr(146), $s);
		$s = str_replace('&lsquo;', chr(145), $s);
		$s = str_replace('&egrave;', chr(232), $s);
	}
	return $s;
}


function sendBPSMail($nameTo, $emailTo, $subj, $plaintextmsg, $htmlmsg, $emailFrom = "bps_feedback@lists.berkeley.edu", $nameFrom = "BPS"){
	require_once 'XPM/XPM3_MAIL.php';
	
	$mail = new XPM3_MAIL;
	$mail->Delivery('local');
	$mail->From($emailFrom, $nameFrom);
	$mail->AddTo($emailTo, $nameTo);
	$mail->Text($plaintextmsg);
	$mail->Html($htmlmsg);
	return $mail->Send($subj);
}

function convert_smart_quotes($string){ 
	$search = array(chr(145),
                    chr(146),
                    chr(147), 
                    chr(148),
                    chr(151));
	$replace = array("'", 
                     "'", 
                     '"', 
                     '"', 
                     '-');

	return str_replace($search, $replace, $string); 
}

?>
