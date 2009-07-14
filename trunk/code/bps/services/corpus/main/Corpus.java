package bps.services.corpus.main;

import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Corpus {
	private static int	nextID = 1;

	private int			id;
	private String		name;
	private String		description;
	// TODO Link to a User instance from bps.services.user.main
	//private User		owner;

	/**
	 * The documents in the corpus
	 */
	private HashMap<Integer, Document> documents;
	/**
	 * The named activities (not instances) seen in this corpus
	 */
	private HashMap<String, Activity> activities;
	/**
	 * The named roles in activities (not instances) for this corpus
	 */
	private HashMap<String, ActivityRole> activityRoles;
	/**
	 * The Names seen in this corpus (not instances)
	 */
	private HashMap<String, Name> names;

	/**
	 * Create a new empty corpus.
	 */
	public Corpus() {
		this(Corpus.nextID++, null, null);
	}

	/**
	 * Create a new corpus with name and description.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public Corpus( String name, String description ) {
		this(Corpus.nextID++, name, description);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @see Corpus( String name, String description )
	 * @param id ID of the corpus to be created. Must be unique.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public Corpus(int id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
		documents = new HashMap<Integer, Document>();
	}

	public static Corpus CreateFromTEI(Node docNode)
		throws XPathExpressionException {
		String name = "unknown";
		String description = null;
	    XPath xpath = XPathFactory.newInstance().newXPath();
       // XPath Query to get to the corpus title
	    try {
		    XPathExpression expr = xpath.compile("//teiHeader/fileDesc/titleStmt/title");
		    Element titleEl = (Element) expr.evaluate(docNode, XPathConstants.NODE);
		    if(titleEl!=null)
		    	name = titleEl.getTextContent().replaceAll("[\\s]+", " ");
		    expr = xpath.compile("//teiHeader/fileDesc/sourceDesc/p");
		    Element descEl = (Element) expr.evaluate(docNode, XPathConstants.NODE);
		    if(descEl!=null)
		    	description = descEl.getTextContent().replaceAll("[\\s]+", " ");
	    } catch (XPathExpressionException xpe) {
	    	// debug complaint
	    	throw xpe;
	    }
	    return new Corpus(name, description);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void addDocument( Document newDoc ) {
		documents.put(newDoc.getId(), newDoc);
	}

	public void generateDependentSQL(String documentsFilename, String activitiesFilename,
			String namesFilename, String activityRolesFilename, String nameRoleActivitiesFilename ) {
		SQLUtils.generateDocumentsSQL(documentsFilename, nameRoleActivitiesFilename, documents);
		SQLUtils.generateActivitiesSQL(activitiesFilename, activities);
		SQLUtils.generateActivityRolesSQL(activitiesFilename, activityRoles);
		SQLUtils.generateNamesSQL(namesFilename, names);
	}

	/**
	 * @return Name.
	 */
	public String toString() {
		return "{"+((name==null)?"(null)":name)+"}";
	}

	/**
	 * Produce SQL loadfile content for this instance
	 * @param sep The separator to use between entries
	 * @return loadfile string with no line terminator or newline.
	 */
	public String toXMLLoadString(String sep) {
		// TODO put in a proper User ID. For now, fixed to the Admin user.
		return id+sep+'"'+name+'"'+sep+'"'+description+'"'+sep+1;
	}


	public static void main(String[] args) {
		int i = 0;
		String arg;
		String corpusFile = null;

        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];
            // Look for corpus arg
            if(arg.equals("-c")) {
                if (i < args.length)
                	corpusFile = args[i++];
                else
                    System.err.println("-c requires a filename");
            } else {
            	System.err.println("Unknown option: "+arg);
            }
        }
        if(corpusFile==null) {
        	System.err.println("Missing corpus file argument.");
        	System.exit(0);
        }
        String outFileBase = corpusFile.replace(".xml", "_");
        String corpusSQLfile = outFileBase+"corpus_load.txt";
        HashMap<Integer, Corpus> corpora = new HashMap<Integer, Corpus>();
        try {
	        org.w3c.dom.Document doc = XMLUtils.OpenXMLFile("file:\\\\"+corpusFile);
			Corpus testCorpus = Corpus.CreateFromTEI(doc);
			corpora.put(testCorpus.getId(), testCorpus);
			SQLUtils.generateCorpusSQL(corpusSQLfile, corpora);
			//Document doc1 = new Document();
			//testCorpus.addDocument(doc1);
        	System.err.println("Corpus test completed.");
        } catch (Exception e) {
        	System.err.println(e);
        }
	}
}
