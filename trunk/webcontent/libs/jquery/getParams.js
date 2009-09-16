jQuery.extend({
/**
* Returns get parameters.
*
* If the desired param does not exist, null will be returned
*
* @example value = $.getURLParam("paramName");
*/ 
 getURLParam: function(strParamName){
	strHref = window.location.href;
	strParamName += "=";

	re = new RegExp(strParamName+"([^&]+)&|"+strParamName+"([^&]+)$");
	matches = re.exec(strHref);
	if(matches){
		if(matches[1]){return matches[1]} else {return matches[2];}
	} else {
		return null;
	}
}
});