function enableElement( elID, sense ) {
	var el = document.getElementById(elID);
	if(el != null)
		el.disabled = !sense;
}

function getNumberFromInput( elID, min, max, description ) {
	var el = document.getElementById(elID);
	if(el == null) {
		alert( "Cannot find element:"+elID+". Please report this internal error to the BPS project." );
		return NaN;
	}
	return checkNumberRange(el.value, min, max, description);
}

function checkNumberRange( value, min, max, description ) {
	var numVal = Number(value);
	if( isNaN(numVal) || numVal < min  || numVal > max ) {
		alert( "You must enter "+description+" of at least "+min+" and no more than "+max+" years." );
		return NaN;
	}
	return numVal;
}


