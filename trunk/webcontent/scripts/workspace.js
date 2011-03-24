// Note that cannot set owner_id on update, so do not bother with it
function prepareWorkspaceXML(id, name, desc) {
	var xmlStr = "<workspace>";
	if((""+id) != "") {
		xmlStr += "<id>"+id+"</id>";
	}
	if((""+name) != "") {
		xmlStr += "<name>"+name+"</name>";
	}
	if((""+desc) != "") {
		xmlStr += "<description>"+desc+"</description>";
	}
	xmlStr += "</workspace>";
	return xmlStr;
}

// This should be in some utils bundle
function limitChars( fieldName, field, maxlimit ) {
  if ( field.value.length > maxlimit )
  {
    field.value = field.value.substring( 0, maxlimit-1 );
    alert( fieldName+" can only be "+maxlimit+" characters in length." );
    return false;
  }
	return true;
}

function checkWorkspaceValues( e, nameElId, descElId, limit ) {
	var nameEl = document.getElementById(nameElId);
	var name = nameEl.value;
	var descEl = document.getElementById(descElId);
	var desc = descEl.value;
	if( name.length < 4 ) {
    alert( "Workspace name must be at least 4 characters in length." );
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
