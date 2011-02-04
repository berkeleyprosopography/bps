function prepareCorpusXML(id, name, desc, owner) {
	var xmlStr = "<corpus>";
	if((""+id) != "") {
		xmlStr += "<id>"+id+"</id>";
	}
	if((""+name) != "") {
		xmlStr += "<name>"+name+"</name>";
	}
	if((""+desc) != "") {
		xmlStr += "<description>"+desc+"</description>";
	}
	if((""+owner) != "") {
		xmlStr += "<ownerId>"+owner+"</ownerId>";
	}
	xmlStr += "</corpus>";
	return xmlStr;
}


function limitChars( fieldName, field, maxlimit ) {
  if ( field.value.length > maxlimit )
  {
    field.value = field.value.substring( 0, maxlimit-1 );
    alert( fieldName+" can only be "+maxlimit+" characters in length." );
    return false;
  }
	return true;
}

function checkCorpusValues( e, nameElId, descElId, limit ) {
	var nameEl = document.getElementById(nameElId);
	var name = nameEl.value;
	var descEl = document.getElementById(descElId);
	var desc = descEl.value;
	if( name.length < 4 ) {
    alert( "Corpus name must be at least 4 characters in length." );
		e.returnValue = false;
		if( e.preventDefault )
			e.preventDefault();
    return false;
  }
	if( !limitChars( "Description", descEl, limit ) ) {
		e.returnValue = false;
		if( e.preventDefault )
			e.preventDefault();
    return false;
  }
	return true;
}
