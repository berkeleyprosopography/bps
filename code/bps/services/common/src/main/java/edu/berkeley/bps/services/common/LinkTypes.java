package edu.berkeley.bps.services.common;

import java.util.HashMap;

public class LinkTypes {
	private static final String LINK_TO_FATHER_S = "father";
	private static final String LINK_TO_MOTHER_S = "mother";
	private static final String LINK_TO_SPOUSE_S = "spouse";
	private static final String LINK_TO_GRANDFATHER_S = "grandfather";
	private static final String LINK_TO_GRANDMOTHER_S = "grandmother";
	private static final String LINK_TO_ANCESTOR_S = "ancestor";
	private static final String LINK_TO_CLAN_S = "clan";
	private static final String LINK_TO_SON_S = "son";
	private static final String LINK_TO_PERSON_S = "person";
	
	private static HashMap<String, Values> stringToValueMap = null;
	private static HashMap<Values, String> valueToStringMap = null;
	
	public enum Values {
	LINK_TO_FATHER,
	LINK_TO_MOTHER,
	LINK_TO_SPOUSE,
	LINK_TO_GRANDFATHER,
	LINK_TO_GRANDMOTHER,
	LINK_TO_ANCESTOR,
	LINK_TO_CLAN,
	LINK_TO_SON,
	LINK_TO_PERSON; }
	
	public static String ValueToString(Values value) {
		init();
		String str = valueToStringMap.get(value);
		return str;
	}
	
	public static Values ValueFromString(String str) {
		init();
		Values value = stringToValueMap.get(str);
		return value;
	}
	
	private static void init() {
		if(stringToValueMap==null) {
			stringToValueMap = new HashMap<String, Values>();
			stringToValueMap.put(LINK_TO_FATHER_S, Values.LINK_TO_FATHER);
			stringToValueMap.put(LINK_TO_MOTHER_S, Values.LINK_TO_MOTHER);
			stringToValueMap.put(LINK_TO_SPOUSE_S, Values.LINK_TO_SPOUSE);
			stringToValueMap.put(LINK_TO_GRANDFATHER_S, Values.LINK_TO_GRANDFATHER);
			stringToValueMap.put(LINK_TO_GRANDMOTHER_S, Values.LINK_TO_GRANDMOTHER);
			stringToValueMap.put(LINK_TO_ANCESTOR_S, Values.LINK_TO_ANCESTOR);
			stringToValueMap.put(LINK_TO_CLAN_S, Values.LINK_TO_CLAN);
			stringToValueMap.put(LINK_TO_SON_S, Values.LINK_TO_SON);
			stringToValueMap.put(LINK_TO_PERSON_S, Values.LINK_TO_PERSON);
		}
		if(valueToStringMap==null) {
			valueToStringMap = new HashMap<Values, String>();
			valueToStringMap.put(Values.LINK_TO_FATHER, LINK_TO_FATHER_S);
			valueToStringMap.put(Values.LINK_TO_MOTHER, LINK_TO_MOTHER_S);
			valueToStringMap.put(Values.LINK_TO_SPOUSE, LINK_TO_SPOUSE_S);
			valueToStringMap.put(Values.LINK_TO_GRANDFATHER, LINK_TO_GRANDFATHER_S);
			valueToStringMap.put(Values.LINK_TO_GRANDMOTHER, LINK_TO_GRANDMOTHER_S);
			valueToStringMap.put(Values.LINK_TO_ANCESTOR, LINK_TO_ANCESTOR_S);
			valueToStringMap.put(Values.LINK_TO_CLAN, LINK_TO_CLAN_S);
			valueToStringMap.put(Values.LINK_TO_SON, LINK_TO_SON_S);
			valueToStringMap.put(Values.LINK_TO_PERSON, LINK_TO_PERSON_S);

		}
	}
	
}
