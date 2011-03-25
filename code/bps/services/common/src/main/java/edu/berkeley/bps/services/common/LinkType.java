package edu.berkeley.bps.services.common;

import java.util.HashMap;

public class LinkType {
	private static final String LINK_TO_FATHER_S = "father";
	private static final String LINK_TO_MOTHER_S = "mother";
	private static final String LINK_TO_SPOUSE_S = "spouse";
	private static final String LINK_TO_GRANDFATHER_S = "grandfather";
	private static final String LINK_TO_GRANDMOTHER_S = "grandmother";
	private static final String LINK_TO_ANCESTOR_S = "ancestor";
	private static final String LINK_TO_CLAN_S = "clan";
	private static final String LINK_TO_SON_S = "son";
	private static final String LINK_TO_DAUGHTER_S = "daughter";
	private static final String LINK_TO_PERSON_S = "person";
	
	private static HashMap<String, LinkType> typesByNameMap = null;
	private static HashMap<Type, LinkType> typesByTypesMap = null;
	
	public enum Type {
		LINK_TO_FATHER,
		LINK_TO_MOTHER,
		LINK_TO_SPOUSE,
		LINK_TO_GRANDFATHER,
		LINK_TO_GRANDMOTHER,
		LINK_TO_ANCESTOR,
		LINK_TO_CLAN,
		LINK_TO_SON,
		LINK_TO_DAUGHTER,
		LINK_TO_PERSON; }

	public enum InferenceType {
		NONE, 
		INVERSE; }

	private Type type;
	private String name;
	private LinkType inferredFrom;
	private InferenceType inferenceType;
	
	private LinkType(Type type, String name) {
		this(type, name, null, InferenceType.NONE);
	}
	
	private LinkType(Type type, String name, 
			LinkType inferredFrom, InferenceType inferenceType) {
		this.type = type;
		this.name = name;
		this.inferredFrom = inferredFrom;
		if(inferredFrom!=null && inferredFrom.inferenceType!=InferenceType.NONE)
			throw new AssertionError("Cannot infer a LinkType from another inferred LinkType");
		this.inferenceType = inferenceType;
	}
	
	public static String ValueToString(Type value) {
		init();
		return typesByTypesMap.get(value).name;
	}
	
	public static Type ValueFromString(String str) {
		init();
		return typesByNameMap.get(str).type;
	}
	
	public static boolean isInferred(Type type) {
		init();
		return typesByTypesMap.get(type).inferenceType!=InferenceType.NONE;
	}
	
	public static InferenceType getInferenceType(Type type) {
		init();
		return typesByTypesMap.get(type).inferenceType;
	}
	
	public static InferenceType getInferredFromType(Type type) {
		init();
		return typesByTypesMap.get(type).inferredFrom.inferenceType;
	}
	
	private static void init() {
		if(typesByNameMap==null || typesByTypesMap==null) {
			typesByNameMap = new HashMap<String, LinkType>();
			typesByTypesMap = new HashMap<Type, LinkType>();
			LinkType newLT = new LinkType(Type.LINK_TO_FATHER, LINK_TO_FATHER_S);
			typesByNameMap.put(LINK_TO_FATHER_S, newLT);
			typesByTypesMap.put(Type.LINK_TO_FATHER, newLT);
			
			LinkType newLTInf = new LinkType(Type.LINK_TO_SON, LINK_TO_SON_S, 
												newLT, InferenceType.INVERSE);
			typesByNameMap.put(LINK_TO_SON_S, newLTInf);
			typesByTypesMap.put(Type.LINK_TO_SON, newLTInf);
			
			newLTInf = new LinkType(Type.LINK_TO_DAUGHTER, LINK_TO_DAUGHTER_S,
									newLT, InferenceType.INVERSE);
			typesByNameMap.put(LINK_TO_DAUGHTER_S, newLTInf);
			typesByTypesMap.put(Type.LINK_TO_DAUGHTER, newLTInf);
			
			newLT = new LinkType(Type.LINK_TO_MOTHER, LINK_TO_MOTHER_S);
			typesByNameMap.put(LINK_TO_FATHER_S, newLT);
			typesByTypesMap.put(Type.LINK_TO_MOTHER, newLT);

			newLT = new LinkType(Type.LINK_TO_SPOUSE, LINK_TO_SPOUSE_S);
			typesByNameMap.put(LINK_TO_SPOUSE_S, newLT);
			typesByTypesMap.put(Type.LINK_TO_FATHER, newLT);
			
			newLT = new LinkType(Type.LINK_TO_GRANDFATHER, LINK_TO_GRANDFATHER_S);
			typesByNameMap.put(LINK_TO_GRANDFATHER_S, newLT);
			typesByTypesMap.put(Type.LINK_TO_GRANDFATHER, newLT);
			
			newLT = new LinkType(Type.LINK_TO_GRANDMOTHER, LINK_TO_GRANDMOTHER_S);
			typesByNameMap.put(LINK_TO_GRANDMOTHER_S, newLT);
			typesByTypesMap.put(Type.LINK_TO_GRANDMOTHER, newLT);
			
			newLT = new LinkType(Type.LINK_TO_ANCESTOR, LINK_TO_ANCESTOR_S);
			typesByNameMap.put(LINK_TO_ANCESTOR_S, newLT);
			typesByTypesMap.put(Type.LINK_TO_ANCESTOR, newLT);
			
			newLT = new LinkType(Type.LINK_TO_CLAN, LINK_TO_CLAN_S);
			typesByNameMap.put(LINK_TO_CLAN_S, newLT);
			typesByTypesMap.put(Type.LINK_TO_CLAN, newLT);
			
			newLT = new LinkType(Type.LINK_TO_PERSON, LINK_TO_PERSON_S);
			typesByNameMap.put(LINK_TO_FATHER_S, newLT);
			typesByTypesMap.put(Type.LINK_TO_FATHER, newLT);

		}
	}
	
}

