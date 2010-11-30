/**
 *
 */
package edu.berkeley.bps.services.corpus;

/**
 * @author pschmitz
 *
 */
public class TEI_Constants {
	public static final String TEI_EL = "TEI";
	public static final String BODY_EL = "body";
	public static final String BACK_EL = "back";
	public static final String PERSNAME_EL = "persName";
	public static final String FORENAME_EL = "forename";
	public static final String ADDNAME_EL = "addName";

	public static final String XMLID_ATTR = "xml:id";
	public static final String TYPE_ATTR = "type";

	public static final String TYPE_GENDER_MASCULINE = "masculine";
	public static final String TYPE_GENDER_FEMININE = "feminine";
	public static final String TYPE_GENDER_UNMARKED = "unmarked";
	public static final String TYPE_PATRONYMIC = "patronymic";
	public static final String TYPE_CLAN = "clan";
	public static final String TYPE_SPOUSE = "spouse";

	public static final String XPATH_ALT_ID ="./teiHeader/fileDesc/titleStmt/title/name[@type='cdlicat:id_text']";
	public static final String XPATH_PRINCIPAL_PERSNAMES =
		"./text[@type='transliteration']/body//persName";
	public static final String XPATH_WITNESS_PERSNAMES =
		"./text[@type='transliteration']/back/div[@subtype='witnesses']//persName";
}
