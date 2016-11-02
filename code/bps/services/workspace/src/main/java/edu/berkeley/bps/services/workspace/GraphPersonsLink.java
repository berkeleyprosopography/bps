/**
 * 
 */
package edu.berkeley.bps.services.workspace;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;

import edu.berkeley.bps.services.common.LinkType;
import edu.berkeley.bps.services.corpus.ActivityRole;
import edu.berkeley.bps.services.corpus.Name;
import edu.berkeley.bps.services.corpus.NameRoleActivity;
import edu.berkeley.bps.services.sna.graph.GraphWrapper;
import edu.berkeley.bps.services.sna.graph.components.Edge;
import edu.berkeley.bps.services.sna.graph.components.EdgeFactory;
import edu.berkeley.bps.services.sna.graph.components.Vertex;
import edu.berkeley.bps.services.sna.graph.components.VertexFactory;

/**
 * @author pschmitz
 *
 */
public class GraphPersonsLink extends EntityLink<Person> {
	
	protected String hashIndex = null;
	protected String activityRolesInteraction = null;
	
	// Properties for the nodes/vertices
	public static final String GML_NAME_KEY = "name";
	public static final String GML_QNAME_KEY = "qname";		// qualified name
	public static final String GML_FATHER_KEY = "father";
	public static final String GML_GRANDFATHER_KEY = "grandfather";
	public static final String GML_CLAN_KEY = "clan";
	public static final String GML_GENDER_KEY = "gender";
	public static final String GML_FLORUIT_KEY = "floruit";
	public static final String GML_STATUS_KEY = "status";

	// Properties for the links/edges
	public static final String GML_DOCID_KEY = "docID";
	public static final String GML_TYPE_KEY = "type";
	// This is a huge hack in Edge, and I cannot believe we do it this way.
	public static final String GML_WEIGHT_KEY = "weight";

	public GraphPersonsLink() {
		super();
	}
	
	public GraphPersonsLink(Person fromPers, String fromActRole,  
			Person toPers, String toActRole, double weight) {
		super(fromPers, toPers, weight, LinkType.Type.LINK_TO_PERSON);
		if(fromActRole == null || fromActRole.isEmpty())
			fromActRole = ActivityRole.ROLE_UNKNOWN;
		if(toActRole == null || toActRole.isEmpty())
			toActRole = ActivityRole.ROLE_UNKNOWN;
		hashIndex = createARIntHash(fromPers, toPers);
		activityRolesInteraction =  buildARIntString(fromActRole, toActRole);
	}
	
	private String buildARIntString(String fromActRole, String toActRole) {
		return "("+fromActRole+"-"+toActRole+")";
	}
	
	public static String createARIntHash(Person fromPers, Person toPers) {
		return fromPers.getId()+":"+toPers.getId();
	}
	
	/**
	 * @return the hashIndex
	 */
	public String getHashIndex() {
		return hashIndex;
	}

	/**
	 * @return the activityRolesInteraction
	 */
	public String getActivityRolesInteraction() {
		return activityRolesInteraction;
	}
	
	public void addLink(String fromActRole, String toActRole, double weight) {
		String newARInt= buildARIntString(fromActRole, toActRole);
		// See if this activity role interaction is already represented.
		if(!activityRolesInteraction.contains(newARInt)) {
			// Add this one to the mix
			activityRolesInteraction = activityRolesInteraction+","+newARInt;
		}
		adjustWeight(weight);
	}

	public void addToGraph(GraphWrapper graph, HashMap<String, Vertex> existingVertices,
							VertexFactory vertexFactory, EdgeFactory edgeFactory,
							boolean fUseQnameForName) {

		// Build and add a vertex for the FROM Person
		// Start with the properties (key-value pairs)
		HashMap<String, String> fromProps = new HashMap<String, String>();
		// Output option to use simple or qualified name as the name on the node
		String fromGMLID = getGMLIDForPerson(fromObj);
		Vertex from = existingVertices.get(fromGMLID);
		if(from ==null) {
			setPropsForPerson(fromProps, fromObj, fUseQnameForName);

			from = vertexFactory.create(fromProps, fromGMLID); 
			graph.addVertex(from);
			existingVertices.put(fromGMLID, from);
		}

		// Build and add a vertex for the TO Person
		HashMap<String, String> toProps = new HashMap<String, String>();
		String toGMLID = getGMLIDForPerson((Person)getEntity());
		Vertex to = existingVertices.get(toGMLID);
		if(to == null) {
			setPropsForPerson(toProps, (Person)getEntity(), fUseQnameForName);

			to = vertexFactory.create(toProps, toGMLID); 
			graph.addVertex(to);
			existingVertices.put(toGMLID, to);
		}
		
		
		// Build and add an edge that links them.
		// We use UNDIRECTED links at this point.
		final boolean UNDIRECTED = false;
		HashMap<String, String> edgeProps = new HashMap<String, String>();
		setPropsForLink(edgeProps);
		Edge e = edgeFactory.create(UNDIRECTED, fromGMLID, toGMLID, edgeProps);
		graph.addEdge(e, from, to);
	}
	
	/**
	 * @param properties	properties map to set into
	 * @param person		person to get props from
	 * @return				synthesized GML ID for the person (id prefix and displayName suffix)
	 */
	private static void setPropsForPerson(HashMap<String, String> properties, Person person,
							boolean fUseQnameForName ) {

		Name nameTmp = person.getDeclaredName();
		String nameStr = null;
		if(fUseQnameForName)
			nameStr = person.getDisplayName();

		if(nameTmp==null) {
			properties.put(GML_NAME_KEY, "???");
			properties.put(GML_GENDER_KEY, Name.GENDER_UNKNOWN_S);
		} else {
			if(nameStr == null)	// Fall back from displayName to simple name
				nameStr = nameTmp.getName();
			properties.put(GML_NAME_KEY, nameStr);
			properties.put(GML_GENDER_KEY, nameTmp.getGenderString());
		}
		String strTmp = person.getFloruit();
		if(strTmp!=null)
			properties.put(GML_FLORUIT_KEY, strTmp);
		nameTmp = person.getDeclaredFather();
		if(nameTmp!=null)
			properties.put(GML_FATHER_KEY, nameTmp.getName());
		nameTmp = person.getDeclaredGrandFather();
		if(nameTmp!=null)
			properties.put(GML_GRANDFATHER_KEY, nameTmp.getName());
		nameTmp = person.getDeclaredClan();
		if(nameTmp!=null)
			properties.put(GML_CLAN_KEY, nameTmp.getName());

		// We do not yet support features like status
		// properties.put(GML_STATUS_KEY, fromObj.getDisplayName());
		
	}

	private String getGMLIDForPerson(Person person ) {
		// Create a GML id from the underlying ID for uniqueness, and the displayName for ease of reading
		return String.format("P%04d_%s", person.getId(), person.getDisplayName());
	}

	/**
	 * @param properties	properties map to set into
	 */
	private void setPropsForLink(HashMap<String, String> properties) {
		// We do not model the docID (yet). 
		// properties.put(GML_DOCID_KEY, ???);

		// We use the activityRolesInteraction as the type. It may get long for aggregated graphs 
		if(activityRolesInteraction==null) {
			properties.put(GML_TYPE_KEY, ActivityRole.ROLE_UNKNOWN);
		} else {
			properties.put(GML_TYPE_KEY, activityRolesInteraction);
		}
		String strTmp = Double.toString(getWeight());
		properties.put(GML_WEIGHT_KEY, strTmp);
	}

	/**
	 * @return the nradId
	 */
	@XmlElement(name="nradId")
	public int getFromId() {
		return fromObj.getId();
	}

}
