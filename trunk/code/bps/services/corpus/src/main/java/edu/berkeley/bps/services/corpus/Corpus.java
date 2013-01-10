package edu.berkeley.bps.services.corpus;

import edu.berkeley.bps.services.common.ServiceContext;
import edu.berkeley.bps.services.common.time.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="corpus")
public class Corpus extends CachedEntity {
	static final Logger logger = LoggerFactory.getLogger(CachedEntity.class);
			
	final static String myClass = "Corpus";
	public final static int NO_WKSP_ID = 0;
	public final static int ANY_WKSP_ID = -1;
	public final static int NO_CLONE_ID = 0;
	public final static int ORDER_DOCS_BY_ALT_ID = 0;
	public final static int ORDER_DOCS_BY_DATE = 1;
	public static final String ORDER_DOCS_BY_ALT_ID_PARAM = "altId";
	public static final String ORDER_DOCS_BY_DATE_PARAM = "date";

	
	@XmlElement 
	String		description;
	@XmlElement 
	int			ownerId;
	
	int			wkspId;
	int			cloneOfId;
	
	private static String myTablename = "corpus";

	private TimeSpan	defaultDocTimeSpan = null;
	// TODO Link to a User instance from bps.services.user.main
	//private User		owner;
	
	// Not multi-instance safe, but these should
	// only be used until docs are persisted.
	private static int nextId = UNSET_ID_VALUE;	// temp IDs before we serialize
	
	private HashMap<Integer, Document> documentsById = null;
	private HashMap<String, Document> documentsByAltId = null;
	
	int fetchedDocumentCount = 0; 
	/**
	 * The named activities (not instances) seen in this corpus
	 */
	private HashMap<String, Activity> activitiesByName = null;
	private HashMap<Integer, Activity> activitiesById = null;

	/**
	 * The named roles in activities (not instances) for this corpus
	 */
	private HashMap<String, ActivityRole> activityRolesByName = null;
	private HashMap<Integer, ActivityRole> activityRolesById = null;
	
	/**
	 * The names seen in this corpus
	 */
	private HashMap<String, Name> namesByName = null;
	private HashMap<Integer, Name> namesById = null;
	private HashMap<String, Name> namesByNymId = null;

	/**
	 * Create a new empty corpus.
	 */
	public Corpus() {
		this(Corpus.nextId--, null, null, -1, NO_WKSP_ID, NO_CLONE_ID, null);
	}

	/**
	 * Create a new corpus with name and description.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public Corpus( String name, String description, TimeSpan defaultDocTimeSpan ) {
		this(Corpus.nextId--, name, description, -1, 
				NO_WKSP_ID, NO_CLONE_ID, defaultDocTimeSpan);
	}

	/**
	 * Ctor with all params - not generally used.
	 * @see Corpus( String name, String description )
	 * @param id ID of the corpus to be created. Must be unique.
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 */
	public Corpus(int id, String name, String description, 
			int ownerId, int wkspId, int cloneOfId, TimeSpan defaultDocTimeSpan) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.ownerId = ownerId;
		this.wkspId = wkspId;
		this.cloneOfId = cloneOfId;
		this.defaultDocTimeSpan = defaultDocTimeSpan;
		documentsById = new HashMap<Integer, Document>();
		documentsByAltId = new HashMap<String, Document>();
		activitiesByName = new HashMap<String, Activity>();
		activitiesById = new HashMap<Integer, Activity>();
		activityRolesByName = new HashMap<String, ActivityRole>();
		activityRolesById = new HashMap<Integer, ActivityRole>();
		namesByName = new HashMap<String, Name>();
		namesById = new HashMap<Integer, Name>();
		namesByNymId = new HashMap<String, Name>();
	}
	
	public Corpus cloneForWorkspace(ServiceContext sc, 
			int inWkspId, int wkspOwnerId) {
		final String myName = ".cloneInWorkspace: ";
		if(this.wkspId!=NO_WKSP_ID) {
			String tmp = myClass+myName+
				"Cannot clone a workspace-owned Corpus.\n";
			logger.error(tmp);
			throw new RuntimeException(tmp);
		}
		Corpus newCorpus = CreateAndPersist(sc, 
				name+"(Clone)", description, wkspOwnerId,
				inWkspId, id, defaultDocTimeSpan);
		// For each of the attached entities, we clone and preserve the 
		// same ordering of id values. This is important for some sorts
		// Clone Activities, and build maps
		Connection dbConn = sc.getConnection();
		ArrayList<Integer> idList = new ArrayList<Integer>(activitiesById.keySet());
		Collections.sort(idList);
		for(int id:idList) {
			Activity act = activitiesById.get(id);
			Activity clone = act.cloneInCorpus(dbConn,newCorpus);
			newCorpus.activitiesById.put(clone.getId(), clone);
			newCorpus.activitiesByName.put(clone.getName(), clone);
		}
		// Clone Roles, and build maps
		idList = new ArrayList<Integer>(activityRolesById.keySet());
		Collections.sort(idList);
		for(int id:idList) {
			ActivityRole actRole = activityRolesById.get(id);
			ActivityRole clone = actRole.cloneInCorpus(dbConn,newCorpus);
			newCorpus.activityRolesById.put(clone.getId(), clone);
			newCorpus.activityRolesByName.put(clone.getName(), clone);
		}
		// Clone Names, and build maps
		// TODO Need to first clone all normal forms, build an association map, 
		// and then pass that to the clone method, to associated to the clone of the normal.
		// If we order the keySet by id, that should be sufficient, just as we ordered the 
		// original query to build the list with links in Name.ListAllInCorpus()
		idList = new ArrayList<Integer>(namesById.keySet());
		Collections.sort(idList);
		HashMap<Integer, Name> oldNormalNameIdsToNewNames = new HashMap<Integer, Name>();
		// we Loop over the ids twice, once for those with no Normal form (Normals), 
		// building up our map of old to new Normal Ids. Then we can consider the
		// Names with normal forms, and map them.
		for(int id:idList) {
			Name nameItem = namesById.get(id);
			if(nameItem.getNormal()==null) { // Has no normal, so IS normal
				Name clone = nameItem.cloneInCorpus(dbConn,newCorpus, null);
				int newId = clone.getId();
				oldNormalNameIdsToNewNames.put(id, clone);
				newCorpus.namesById.put(newId, clone);
				newCorpus.namesByName.put(clone.getName(), clone);
				String nymId = clone.getNymId();
				if(nymId!=null)
					newCorpus.namesByNymId.put(nymId, clone);
			}
		}
		for(int id:idList) {
			Name nameItem = namesById.get(id);
			if(nameItem.getNormal()!=null) { // Has normal, so clone passing in the map
				Name clone = nameItem.cloneInCorpus(dbConn,newCorpus, oldNormalNameIdsToNewNames);
				int newId = clone.getId();
				oldNormalNameIdsToNewNames.put(id, clone);
				newCorpus.namesById.put(newId, clone);
				newCorpus.namesByName.put(clone.getName(), clone);
				String nymId = clone.getNymId();
				if(nymId!=null)
					newCorpus.namesByNymId.put(nymId, clone);
			}
		}
		// Clone Documents, and build maps
		idList = new ArrayList<Integer>(documentsById.keySet());
		Collections.sort(idList);
		for(int id:idList) {
			Document doc = documentsById.get(id);
			Document clone = doc.cloneInCorpus(dbConn,newCorpus);
			newCorpus.documentsById.put(clone.getId(), clone);
			newCorpus.documentsByAltId.put(clone.getAlt_id(), clone);
		}
		AddToMaps(sc, newCorpus);

		return newCorpus;
	}
	
	protected static void AddToMaps(ServiceContext sc, Corpus corpus) {
		Map<Integer, Object> idMap = getIdMap(sc, myClass);
		Map<String, Object> nameMap = getNameMap(sc, myClass);
		AddToMaps(idMap, nameMap, corpus);
	}

	private static void AddToMaps(
			Map<Integer, Object> idMap, Map<String, Object> nameMap,
			Corpus corpus) {
		String name = corpus.getName();
		if(name!=null&&!name.isEmpty())
			nameMap.put(name, corpus);
		idMap.put(corpus.getId(), corpus);
	}

	private static void RemoveFromMaps(ServiceContext sc, Corpus corpus) {
		Map<Integer, Object> idMap = getIdMap(sc, myClass);
		Map<String, Object> nameMap = getNameMap(sc, myClass);
		String name = corpus.getName();
		if(name!=null&&!name.isEmpty())
			nameMap.remove(name);
		idMap.remove(corpus.getId());
	}

	
	public static void initMaps(ServiceContext sc) {
		HashMap<String, Object> nameMap = new HashMap<String, Object>();
		HashMap<Integer, Object> idMap = new HashMap<Integer, Object>();

		Connection dbConn = sc.getConnection(false);
		List<Corpus> corpora = LoadAll(dbConn);
		for(Corpus corpus:corpora) {
			AddToMaps(idMap, nameMap, corpus);
			corpus.initAttachedEntityMaps(dbConn);
		}
		setNameMap(sc, myClass, nameMap);
		setIdMap(sc, myClass, idMap);
	}
	
	protected void initAttachedEntityMaps(Connection dbConn) {
		// If this is a proper corpus from the DB, then set up the 
		// attached elements as hashmaps
		if(id<=0)
			return;
		logger.trace("Corpus.initAttachedEntityMaps() called...");
		// Handle Activities Map
		activitiesByName.clear();
		activitiesById.clear();
		List<Activity> activitiesList = Activity.ListAllInCorpus(dbConn, this);
		for(Activity act:activitiesList) {
			activitiesByName.put(act.getName(), act);
			activitiesById.put(act.getId(), act);
		}
		logger.trace("Corpus.initAEMaps() Built activities list. Count: "+activitiesByName.size());
		// Handle Activity Roles Map
		activityRolesByName.clear();
		activityRolesById.clear();
		List<ActivityRole> activityRolesList = ActivityRole.ListAllInCorpus(dbConn, this);
		for(ActivityRole actRole:activityRolesList) {
			activityRolesByName.put(actRole.getName(), actRole);
			activityRolesById.put(actRole.getId(), actRole);
		}
		logger.trace("Corpus.initAEMaps() Built roles list. Count: "+activityRolesByName.size());
		// Handle Names Maps
		namesByName.clear();
		namesById.clear();
		namesByNymId.clear();
		List<Name> namesList = Name.ListAllInCorpus(dbConn, this);
		for(Name name:namesList) {
			namesByName.put(name.getName(), name);
			namesById.put(name.getId(), name);
			String nymId = name.getNymId();
			if(nymId!=null)
				namesByNymId.put(nymId, name);
		}
		logger.trace("Corpus.initAEMaps() Built names list. Count: "+namesByName.size());
		// Handle Documents Map
		documentsById.clear();
		documentsByAltId.clear();
		List<Document> docList = Document.ListAllInCorpus(dbConn, this);
		for(Document doc:docList) {
			documentsById.put(doc.getId(), doc);
			doc.initAttachedEntityMaps(dbConn);
			String altId = doc.getAlt_id();
			if(altId!=null&&!altId.isEmpty()) {
				documentsByAltId.put(altId, doc);
			}
		}
		logger.trace("Corpus.initAEMaps() Built doc list. Count: "+documentsById.size());
	}
	
	private void clearMaps() {
		documentsById.clear();
		documentsByAltId.clear();
		activitiesByName.clear();
		activitiesById.clear();
		activityRolesByName.clear();
		activityRolesById.clear();
		namesByName.clear();
		namesById.clear();
		namesByNymId.clear();
	}
	
	
	public static boolean Exists(Connection dbConn, int id) {
		return CachedEntity.Exists(dbConn, myTablename, id);
	}

	public static boolean NameUsed(ServiceContext sc, String name) {
		return null!=FindByName(sc, name);
	}

	public static Corpus FindByID(ServiceContext sc, int id) {
		Map<Integer, Object> idMap = getIdMap(sc, myClass);
		return (Corpus)idMap.get(id);
	}
	
	/*
	private static Corpus FindByID(Connection dbConn, int id) {
		final String myName = ".FindByID: ";
		final String SELECT_BY_ID = 
			"SELECT c.id, c.name, c.description, c.owner_id, c.wksp_id, c.clone_of_id, count(*) nDocs, d.id docId"
			+" FROM corpus c LEFT JOIN document d ON c.id=d.corpus_id"
			+" WHERE c.id = ?"
			+" GROUP BY c.id";
		Corpus corpus = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_ID);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				corpus = new Corpus(rs.getInt("id"), rs.getString("name"), 
							rs.getString("description"), 
							rs.getInt("owner_id"), rs.getInt("wksp_id"), 
							rs.getInt("clone_of_id"), null);
				corpus.fetchedDocumentCount = 
					(rs.getInt("docId")==0)?0:rs.getInt("nDocs");
				corpus.initAttachedEntityMaps(dbConn);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new RuntimeException( tmp );
		}
		return corpus;
	}
	*/

	public static Corpus FindByName(ServiceContext sc, String name) {
		Map<String, Object> nameMap = getNameMap(sc, myClass);
		return (Corpus)nameMap.get(name);
	}
	
	/*
	public static Corpus FindByName(Connection dbConn, String name) {
		final String myName = ".FindByName: ";
		final String SELECT_BY_NAME = 
			"SELECT c.id, c.name, c.description, c.owner_id, c.wksp_id, c.clone_of_id, count(*) nDocs, d.id docId"
			+" FROM corpus c LEFT JOIN document d ON c.id=d.corpus_id"
			+" WHERE c.name = ? AND c.wksp_id=" + NO_WKSP_ID
			+" GROUP BY c.id";
		Corpus corpus = null;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_NAME);
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				corpus = new Corpus(rs.getInt("id"), rs.getString("name"), 
							rs.getString("description"), 
							rs.getInt("owner_id"), rs.getInt("wksp_id"), 
							rs.getInt("clone_of_id"), null);
				corpus.fetchedDocumentCount = 
					(rs.getInt("docId")==0)?0:rs.getInt("nDocs");
				corpus.initAttachedEntityMaps(dbConn);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new RuntimeException( tmp );
		}
		return corpus;
	}
	*/

	public static List<Corpus> ListAll(ServiceContext sc, int wksp_id) {
		Map<Integer, Object> idMap = getIdMap(sc, myClass);
		ArrayList<Corpus> list = new ArrayList<Corpus>();
		for(Object obj:idMap.values()) {
			Corpus corpus = (Corpus)obj;
			if(wksp_id==ANY_WKSP_ID
				|| wksp_id==corpus.wkspId)
				list.add(corpus);
		}
		return list;
	}
	
	
	private static List<Corpus> LoadAll(Connection dbConn) {
		// TODO Add pagination support
		final String SELECT_ALL = 
			"SELECT c.id, c.name, c.description, c.owner_id, c.wksp_id, c.clone_of_id, count(*) nDocs, d.id docId"
			+" FROM corpus c LEFT JOIN document d ON c.id=d.corpus_id GROUP BY c.id";
		// Generate the right representation according to its media type.
		ArrayList<Corpus> corpusList = new ArrayList<Corpus>();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_ALL);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				Corpus corpus = new Corpus(rs.getInt("id"), rs.getString("name"), 
						rs.getString("description"), 
						rs.getInt("owner_id"), rs.getInt("wksp_id"), 
						rs.getInt("clone_of_id"), null);
				// If no docId, count should actually be 0, not 1
				corpus.fetchedDocumentCount = 
					(rs.getInt("docId")==0)?0:rs.getInt("nDocs");
				corpusList.add(corpus);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".ListAll(): Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new WebApplicationException( 
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							"Problem creating corpus\n"+se.getLocalizedMessage()).build());
		}
		return corpusList;
	}	

	public static Corpus CreateAndPersist(ServiceContext sc, 
			String name, String description, int owner_id, TimeSpan defaultDocTimeSpan) {
		return CreateAndPersist(sc, 
				name, description, owner_id, NO_WKSP_ID, NO_CLONE_ID, defaultDocTimeSpan);
	}
	
	protected static Corpus CreateAndPersist(ServiceContext sc, 
			String name, String description, int owner_id, int wksp_id, int clone_of_id, TimeSpan defaultDocTimeSpan) {
		int id = persistNew(sc.getConnection(), name, description, owner_id, wksp_id, clone_of_id, defaultDocTimeSpan);
		Corpus corpus = new Corpus(id, name, description, 
									owner_id, wksp_id, clone_of_id, defaultDocTimeSpan);
		AddToMaps(sc, corpus);
		return corpus;
	}
	
	private static int persistNew(Connection dbConn, 
			String name, String description, int owner_id, int wksp_id, int clone_of_id,
			TimeSpan defaultDocTimeSpan) {
		final String myName = ".persistNew: ";
		// Note that wksp_id defaults to NULL/0
		final String INSERT_STMT = 
			"INSERT INTO corpus(name, description, owner_id, wksp_id, clone_of_id, creation_time)"
			+" VALUES(?,?,?,?,?,now())";
		int newId = 0;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
												Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);
			stmt.setString(2, description);
			stmt.setInt(3, owner_id);
			stmt.setInt(4, wksp_id);
			stmt.setInt(5, clone_of_id);
			int nRows = stmt.executeUpdate();
			if(nRows==1){
				ResultSet rs = stmt.getGeneratedKeys();
				if(rs.next()){
					newId = rs.getInt(1); 
				}
				rs.close();
			}
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new RuntimeException( tmp );
		}
		return newId;
	}

	
	public void persist(Connection dbConn, boolean shallow) {
		final String myName = ".persist: ";
		if(id<=UNSET_ID_VALUE) {
			id = persistNew(dbConn, name, description, ownerId, wkspId, cloneOfId, defaultDocTimeSpan);
		} else {
			final String UPDATE_STMT = 
				"UPDATE corpus SET name=?, description=? WHERE id=?";
			try {
				PreparedStatement stmt = dbConn.prepareStatement(UPDATE_STMT);
				stmt.setString(1, name);
				stmt.setString(2, description);
				stmt.setInt(3, id);
				stmt.executeUpdate();
			} catch(SQLException se) {
				String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
				logger.error(tmp);
				throw new RuntimeException( tmp );
			}
		}
		if(shallow==CachedEntity.DEEP_PERSIST)
			persistAttachedEntities(dbConn);
	}
	
	protected void persistNames(Connection dbConn) {
		// Since the maps are parallel, we only iterate over one
		for(Name name:namesByName.values()) {
			boolean fResetId = (name.getId()<=CachedEntity.UNSET_ID_VALUE);
			if(fResetId) {
				namesById.remove(name.getId());
			}
			name.persist(dbConn);
			if(fResetId) {
				namesById.put(name.getId(), name);
			}
		}
	}

	protected void persistActivities(Connection dbConn) {
		for(Activity activity:activitiesByName.values()) {
			boolean fResetId = (activity.getId()<=CachedEntity.UNSET_ID_VALUE);
			if(fResetId) {
				activitiesById.remove(activity.getId());
			}
			activity.persist(dbConn);
			if(fResetId) {
				activitiesById.put(activity.getId(), activity);
			}
		}
	}
	
	protected void persistActivityRoles(Connection dbConn) {
		for(ActivityRole activityRole:activityRolesByName.values()) {
			boolean fResetId = (activityRole.getId()<=CachedEntity.UNSET_ID_VALUE);
			if(fResetId) {
				activityRolesById.remove(activityRole.getId());
			}
			activityRole.persist(dbConn);
			if(fResetId) {
				activityRolesById.put(activityRole.getId(), activityRole);
			}
		}
	}
	
	protected void persistDocuments(Connection dbConn) {
		for(Document doc:documentsById.values()) {
			doc.persist(dbConn, CachedEntity.DEEP_PERSIST);
		}
	}
	
	public void persistAttachedEntities(Connection dbConn) {
		persistNames(dbConn);
		persistActivities(dbConn);
		persistActivityRoles(dbConn);
		persistDocuments(dbConn);
	}
	
	public void deletePersistence(ServiceContext sc) {
		RemoveFromMaps(sc, this);
		Connection dbConn = sc.getConnection();
		deleteAttachedEntities(dbConn);
		DeletePersistence(dbConn, id);
	}
	
	private static void DeletePersistence(Connection dbConn, int id) {
		final String DELETE_STMT = "DELETE FROM corpus WHERE id=?";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(DELETE_STMT);
			stmt.setInt(1, id);
			stmt.executeUpdate();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".deletePersistence: Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new RuntimeException( tmp );
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	/**
	 * @return the wkspId
	 */
	@XmlElement(name="wkspId")
	public int getWkspId() {
		return wkspId;
	}

	/**
	 * @return the cloneOfId
	 */
	@XmlElement(name="cloneOfId")
	public int getCloneOfId() {
		return cloneOfId;
	}

	@XmlElement(name="ndocs")
	public int getNDocuments() {
		int nDocs = 0;
		if(documentsById != null) {
			nDocs = documentsById.size();
		}
		if(nDocs==0){
			nDocs = fetchedDocumentCount;
		}
		return nDocs;
	}
	
	@XmlElement(name="medianDocDate")
	public String getMedianDocumentDateStr() {
		long medianDate = getMedianDocumentDate();
		return (medianDate==0)?null:
			TimeUtils.millisToSimpleYearString(medianDate);
	}
	
	public long getMedianDocumentDate() {
		long medianDate = 0;
		if(documentsById != null) {
			ArrayList<Long> validDates = new ArrayList<Long>(documentsById.size());
			for(Document doc:documentsById.values()) {
				long docDate = doc.getDate_norm();
				if(docDate != 0) {
					validDates.add(docDate);
				}
			}
			if(!validDates.isEmpty()) {
				Collections.sort(validDates);
				if (validDates.size() % 2 == 1) {
					medianDate = validDates.get((validDates.size()+1)/2-1);
				} else {
					long lower = validDates.get(validDates.size()/2-1);
					long upper = validDates.get(validDates.size()/2);
					medianDate = (lower + upper) / 2L;
			    }	
			}
		}
		return medianDate;
	}
	
	public List<Document> getDocuments() {
		return getDocuments(ORDER_DOCS_BY_ALT_ID);
	}
	
	public List<Document> getDocuments(int orderBy) {
		ArrayList<Document> list = new ArrayList<Document>(documentsById.values());
		Comparator<Document> c = orderBy==ORDER_DOCS_BY_ALT_ID?
										new Document.AltIdComparator()
										: new Document.DateComparator();
		Collections.sort(list, c);
		return list;
	}
	
	public List<Document> getDocuments(ServiceContext sc, Name nameFilter,
			ActivityRole roleFilter, int orderBy) {
		List<Document> list;
		if(nameFilter==null && roleFilter==null) {
			list = getDocuments(orderBy);
		} else {
			list = Document.getFilteredDocuments(sc.getConnection(), this, 
											nameFilter, roleFilter, orderBy);
		}
		return list;
	}
	
	public Document getDocument(int docId) {
		Document doc = documentsById.get(docId);
		return doc;
	}
	
	public Document getDocumentByAltId(String altId) {
		Document doc = documentsByAltId.get(altId);
		return doc;
	}
	
	public List<Activity> getActivities() {
		ArrayList<Activity> list = new ArrayList<Activity>(activitiesByName.values());
		return list;
	}
	
	public List<ActivityRole> getActivityRoles() {
		ArrayList<ActivityRole> list = 
			new ArrayList<ActivityRole>(activityRolesByName.values());
		return list;
	}
	
	public List<Name> getNames() {
		return getNames(null, null, null, "name", null);
	}
	
	public List<Name> getNames(String typeFilter, ActivityRole roleFilter, 
				String genderFilter, String orderBy, Connection dbConn) {
		List<Name> list;
		if(typeFilter==null && roleFilter==null && genderFilter==null) {
			list = new ArrayList<Name>(namesByName.values());
		} else if(roleFilter==null) {
			list = new ArrayList<Name>();
			int gender = (genderFilter==null)?-1:Name.GenderStringToValue(genderFilter);
			int type = (typeFilter==null)?-1:Name.NameTypeStringToValue(typeFilter);
			for( Name name:namesByName.values()) {
				if((gender<0 || name.getGender()==gender)
					&& (type<0 || name.getNameType()==type)) 
					list.add(name);
			}
		} else {	// need to run a query to figure out Names by role
			list = Name.getFilteredNames(this, typeFilter, roleFilter, genderFilter, dbConn);
		}
		if(orderBy==null)	// default to "name";
			orderBy="name";
		Comparator<Name> comp = null;
		if("name".equals(orderBy)) { 
			comp = new Name.NameComparator();
		} else if("gender".equals(orderBy)) {
			comp = new Name.GenderComparator();
		} else if("docCount".equals(orderBy)) {
			comp = new Name.DocCountComparator();
		} else if("totalCount".equals(orderBy)) {
			comp = new Name.TotalCountComparator();
		} else {
			throw new RuntimeException("Unknown orderBy variant: "+orderBy);
		}
		Collections.sort(list, comp);		
		return list;
	}
	
	/*
	private void fetchDocumentCount(Connection dbConn) {
		final String SELECT_N_DOCS = 
			"SELECT count(*) nDocs FROM document WHERE corpus_id = ?";
		int nDocs = 0;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_N_DOCS);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				fetchedDocumentCount = rs.getInt("nDocs");
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			// Just absorb it
			String tmp = myClass+".getNDocuments: Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
		}
	}
	*/
	
	public void loadDocuments(Connection dbConn) {
		throw new RuntimeException("Not Yet Implemented");
	}

	public void addDocument( Document newDoc ) {
		documentsById.put(newDoc.getId(), newDoc);
		String altId = newDoc.getAlt_id();
		if(altId!=null&&!altId.isEmpty()) {
			documentsByAltId.put(altId, newDoc);
		}

	}
	
	public void deleteAttachedEntities(Connection dbConn) {
		// Clear out the existing documents, Names, etc. 
        deleteDocuments(dbConn);
        // This is probably not necessary, but is cheap
        deleteActivities(dbConn);
        // This is probably not necessary, but is cheap
        deleteActivityRoles(dbConn);
        deleteNames(dbConn);

	}
	
	public void deleteDocuments(Connection dbConn) {
		Document.DeleteAllInCorpus(dbConn, this);
		if(documentsById!=null)
			documentsById.clear();
		if(documentsByAltId!=null)
			documentsByAltId.clear();
	}

	public void deleteActivities(Connection dbConn) {
		Activity.DeleteAllInCorpus(dbConn, this);
		if(activitiesByName!=null)
			activitiesByName.clear();
		if(activitiesById!=null)
			activitiesById.clear();
	}

	public void deleteNames(Connection dbConn) {
		Name.DeleteAllInCorpus(dbConn, this);
		if(namesByName!=null)
			namesByName.clear();
		if(namesByNymId!=null)
			namesByNymId.clear();
		if(namesById!=null)
			namesById.clear();
	}

	public void deleteActivityRoles(Connection dbConn) {
		ActivityRole.DeleteAllInCorpus(dbConn, this);
		if(activityRolesByName!=null)
			activityRolesByName.clear();
		if(activityRolesById!=null)
			activityRolesById.clear();
	}

	/*
	 * Should we deprecate this, or will we have corpora with no Name (nym) declarations?
	 */
	public Name findOrCreateName(String name, int nametype,
			int gender, Connection dbConn) {
		return findOrCreateName(name, null, nametype, gender, dbConn);
	}
	
	public Name findOrCreateName(String name, String nymId, int nametype,
			int gender, Connection dbConn) {
		if(name==null||name.isEmpty()) {
			String tmp = myClass+".findOrCreateName("+name
					+","+nymId
					+") Emtpy name!";
			logger.error(tmp);
			throw new RuntimeException(tmp);
		}
		Name instance = namesByName.get(name);
		if(instance == null) {
			instance = Name.CreateAndPersist(dbConn, id, name, nymId, 
					nametype, gender, null, null);
			addName(instance);
		} else if( instance.getNameType()!=nametype) {
			String tmp = myClass+".findOrCreateName("+name
				+","+Name.NameTypeToString(nametype)
				+") Found name match with inconsistent type:"
				+instance.getNameTypeString();
			logger.warn(tmp);
			//throw new RuntimeException(tmp);
		} else if( nymId!=null && instance.getNymId()!=nymId) {
			String tmp = myClass+".findOrCreateName("+name
				+","+nymId
				+") Found name match with inconsistent nymId (duplicate orthography?):"
				+instance.getNymId();
			logger.warn(tmp);
			//throw new RuntimeException(tmp);
		} else if(Name.typeHasGender(nametype)) {
			instance.checkAndUpdateGender(gender);
				//if not compatible, throw new RuntimeException(tmp);?
		}
		return instance;
	}
	
	public void addName(Name toAdd) {
		String name = toAdd.getName();
		if(namesByName.get(name)!=null)
			throw new RuntimeException("Corpus.addName: duplicate (name): "+name);
		int id = toAdd.getId();
		if(namesById.get(id)!=null)
			throw new RuntimeException("Corpus.addName: duplicate (id): "+id);
		String nymId = toAdd.getNymId();
		if(nymId != null) {
			if(namesByNymId.get(nymId)!=null) {
				throw new RuntimeException("Corpus.addName: duplicate (nymId): "+nymId);
			}
			namesByNymId.put(nymId, toAdd);
		}
		namesByName.put(name, toAdd);
		namesById.put(id, toAdd);
	}

	public Name findName(String name) {
		Name instance = namesByName.get(name);
		return instance;
	}

	public Name findNym(String nymId) {
		Name instance = namesByNymId.get(nymId);
		return instance;
	}

	public Name findName(int nid) {
		Name instance = namesById.get(nid);
		return instance;
	}

	public Activity findOrCreateActivity(String name, Connection dbConn) {
		Activity instance = activitiesByName.get(name);
		if(instance == null) {
			instance = Activity.CreateAndPersist(dbConn, this, name, null);
			addActivity(instance);
		}
		return instance;
	}

	public void addActivity(Activity toAdd) {
		if(activitiesByName.get(toAdd.getName())!=null)
			throw new RuntimeException("Corpus.addActivity: duplicate (name).");
		if(activitiesById.get(toAdd.getId())!=null)
			throw new RuntimeException("Corpus.addActivity: duplicate (id).");
		activitiesByName.put(toAdd.getName(), toAdd);
		activitiesById.put(toAdd.getId(), toAdd);
	}

	public Activity findActivity(String name) {
		Activity instance = activitiesByName.get(name);
		return instance;
	}

	public Activity findActivity(int aid) {
		Activity instance = activitiesById.get(aid);
		return instance;
	}

	public ActivityRole findOrCreateActivityRole(String name, Connection dbConn) {
		ActivityRole instance = activityRolesByName.get(name);
		if(instance == null) {
			instance = ActivityRole.CreateAndPersist(dbConn, this, name, null);
			instance.persist(dbConn);
			addActivityRole(instance);
		}
		return instance;
	}

	public void addActivityRole(ActivityRole toAdd) {
		if(activityRolesByName.get(toAdd.getName())!=null)
			throw new RuntimeException("addActivityRole: duplicate (name).");
		if(activityRolesById.get(toAdd.getId())!=null)
			throw new RuntimeException("addActivityRole: duplicate (id).");
		activityRolesByName.put(toAdd.getName(), toAdd);
		activityRolesById.put(toAdd.getId(), toAdd);
	}

	public ActivityRole findActivityRole(String name) {
		ActivityRole instance = activityRolesByName.get(name);
		return instance;
	}

	public ActivityRole findActivityRole(int arid) {
		ActivityRole instance = activityRolesById.get(arid);
		return instance;
	}

	/**
	 * @return Name.
	 */
	public String toString() {
		return "{"+((name==null)?"(null)":name)+"}";
	}

	/**
	 * @return the defaultDocTimeSpan
	 */
	@XmlTransient
	public TimeSpan getDefaultDocTimeSpan() {
		return defaultDocTimeSpan;
	}

	/**
	 * @param defaultDocTimeSpan the defaultDocTimeSpan to set
	 */
	public void setDefaultDocTimeSpan(TimeSpan defaultDocTimeSpan) {
		this.defaultDocTimeSpan = defaultDocTimeSpan;
	}
}
