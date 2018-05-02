package edu.berkeley.bps.services.workspace;

import edu.berkeley.bps.services.common.LinkType;
import edu.berkeley.bps.services.common.ServiceContext;
import edu.berkeley.bps.services.common.time.EvidenceBasedTimeSpan;
import edu.berkeley.bps.services.common.time.TimeUtils;
import edu.berkeley.bps.services.corpus.CachedEntity;
import edu.berkeley.bps.services.corpus.Corpus;
import edu.berkeley.bps.services.corpus.Document;
import edu.berkeley.bps.services.corpus.Name;
import edu.berkeley.bps.services.corpus.NameRoleActivity;
import edu.berkeley.bps.services.sna.graph.GraphWrapper;
import edu.berkeley.bps.services.sna.graph.components.EdgeFactory;
import edu.berkeley.bps.services.sna.graph.components.Vertex;
import edu.berkeley.bps.services.sna.graph.components.VertexFactory;
import edu.berkeley.bps.services.workspace.collapser.Collapser;
import edu.berkeley.bps.services.workspace.collapser.CollapserBase;
import edu.berkeley.bps.services.workspace.collapser.CollapserRule;
import edu.berkeley.bps.services.workspace.collapser.CollapserRuleBase;
import edu.berkeley.bps.services.workspace.collapser.CollapserRuleBaseWithUI;
import edu.berkeley.bps.services.workspace.collapser.CollapserRulePairMatrixUI;
import edu.berkeley.bps.services.workspace.collapser.CollapserRuleUI;
import edu.berkeley.bps.services.workspace.collapser.FullyQualifiedEqualNameShiftRule;
import edu.berkeley.bps.services.workspace.collapser.MatrixItemInfo;
import edu.berkeley.bps.services.workspace.collapser.PartlyQualifiedCompatibleNameShiftRule;
import edu.berkeley.bps.services.workspace.collapser.PartlyQualifiedEqualNameShiftRule;
import edu.berkeley.bps.services.workspace.collapser.PersonCollapser;
import edu.berkeley.bps.services.workspace.collapser.RoleMatrixDiscountRule;
import edu.berkeley.bps.services.workspace.collapser.UnqualifiedCompatibleNameShiftRule;
import edu.berkeley.bps.services.workspace.collapser.UnqualifiedEqualNameShiftRule;

import java.security.Permissions;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.management.RuntimeErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pschmitz
 *
 */
/**
 * @author pschmitz
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="workspace")
public class Workspace extends CachedEntity {
	static final Logger logger = LoggerFactory.getLogger(Workspace.class);
	
	private final static String myClass = "Workspace";
	private static int nextId = CachedEntity.UNSET_ID_VALUE;	// temp IDs before we serialize

	@XmlElement
	private String		description;
	@XmlElement
	private int			owner_id;			// Each workspace is tied to a user
	// Note that we do not serialize these millisecond values, but rather the year equivalents
	private double		activeLifeWindow;	// Active life parameter, in millis (for ease of date math)
	private double		activeLifeStdDev;	// Active life std deviation, in millis (for ease of date math)
	// Not clear yet how we'll use this
	//@XmlElement
	//private double		fatherhoodStdDev;
	// Need clear use-cases befoer we can properly define this.
	// @XmlElement
	// private double		wholeLifeStdDev;
	
	/**
	 * Represents the assumed offset from a child's timespan to a
	 * declared parent's timespan. Used for deriving a parent's timespan
	 * from an actual date of activity for the child.
	 * Note that we do not serialize the millisecond value, but rather the year equivalent
	 * @see getGenerationOffsetYears
	 */
	private long		generationOffset;
	
	private Corpus		corpus;
	
	// The collapser used to create and disambiguate persons. Not serialized as part of workspace.
	private PersonCollapser collapser;
	
	// We hold Persons in lists, where each list shares a forename (Name ID).
	// One set of lists is by document, and another is for the entire
	// corpus. We do not build the corpus list until we have completed
	// work coalescing the per-document lists.
	
	// Map indexed by docId to Map indexed by Name (forename), to list of persons
	private HashMap<Integer, HashMap<Integer, ArrayList<Person>>> personListsByNameByDoc;
	// Map indexed by Name (forename), of lists of persons (workspace wide)
	private HashMap<Integer, ArrayList<Person>> personListsByName;
	// Map indexed by Name, of Clans in the workspace
	private HashMap<Integer, Clan> clansByName;
	// Map indexed by docId to Map indexed by Name (forename), to list of clans
	private HashMap<Integer, ArrayList<Clan>> clanListsByDoc;

	// Map indexed by nrad, of the weights to persons or clans
	private HashMap<Integer, EntityLinkSet<NameRoleActivity>> nradToEntityLinks;
	// Map indexed by personId, of lists of EntityLinkSets for the NRADs
	// that point to this person
	private HashMap<Person, List<EntityLinkSet<NameRoleActivity>>> 
												personToEntityLinkSets;

	// Map of person-pair strings (id-pairs) to constructed Graph links
	private HashMap<String, GraphPersonsLink> graphLinks;
	
	public Workspace() {
		this(Workspace.nextId--,null,null,0);
	}

	/**
	 * @param id
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 * @param owner_id The user that owns this workspace
	 */
	private Workspace(int id, String name, String description, int owner_id) {
		this(id, name, description, owner_id,
				Person.DEFAULT_ACTIVE_LIFE_WINDOW, Person.DEFAULT_ACTIVE_LIFE_STDDEV, 
				Person.DEFAULT_GENERATION_OFFSET);
	}

	/**
	 * @param id
	 * @param name A shorthand name for use in UI, etc.
	 * @param description Any description useful to users.
	 * @param owner_id The user that owns this workspace
	 * @param activeLifeWindow  The default activeLife for persons in milliseconds. If <=0, set to default.
	 * @param activeLifeStdDev  The default standard deviation for activeLife for persons in milliseconds. 
	 * 							If <=0, set to default.
	 * @param generationOffset  The default offset between generations in milliseconds. If <=0, set to default.
	 */
	private Workspace(int id, String name, String description, int owner_id,
						double activeLifeWindow, double activeLifeStdDev, long generationOffset ) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.owner_id = owner_id;
		this.corpus = null;
		// We need to handle legacy workspaces, which may have default 0 values for these settings.
		this.activeLifeWindow = (activeLifeWindow>0)?activeLifeWindow:Person.DEFAULT_ACTIVE_LIFE_WINDOW;
		this.activeLifeStdDev = (activeLifeStdDev>0)?activeLifeStdDev:
									TimeUtils.getDefaultStdDevForActiveLife(this.activeLifeWindow);
		this.generationOffset = (generationOffset>0)?generationOffset:Person.DEFAULT_GENERATION_OFFSET;
		this.personListsByNameByDoc = 
			new HashMap<Integer, HashMap<Integer, ArrayList<Person>>>();
		this.personListsByName =
			new HashMap<Integer, ArrayList<Person>>();
		this.clansByName = new HashMap<Integer, Clan>();
		this.clanListsByDoc = new HashMap<Integer, ArrayList<Clan>>(); 
		this.nradToEntityLinks = 
			new HashMap<Integer, EntityLinkSet<NameRoleActivity>>();
		this.personToEntityLinkSets = 
			new HashMap<Person, List<EntityLinkSet<NameRoleActivity>>>();
		this.graphLinks = new HashMap<String, GraphPersonsLink>();

		logger.debug("Workspace.ctor, created: "+this.toString());
	}
	
	private void init(ServiceContext sc, boolean fSetupCollapser) {
		AddToMaps(sc, this);
		if(fSetupCollapser && corpus!=null)
			setupCollapser(sc);
	}

	protected static void AddToMaps(ServiceContext sc, Workspace workspace) {
		Map<Integer, Object> idMap = getIdMap(sc, myClass);
		Map<String, Object> nameMap = getNameMap(sc, myClass);
		AddToMaps(idMap, nameMap, workspace);
	}

	private static void AddToMaps(
			Map<Integer, Object> idMap, Map<String, Object> nameMap,
			Workspace workspace) {
		String name = workspace.getName();
		if(name!=null&&!name.isEmpty())
			nameMap.put(name, workspace);
		idMap.put(workspace.getId(), workspace);
	}

	private static void RemoveFromMaps(ServiceContext sc, Workspace workspace) {
		Map<Integer, Object> idMap = getIdMap(sc, myClass);
		Map<String, Object> nameMap = getNameMap(sc, myClass);
		String name = workspace.getName();
		if(name!=null&&!name.isEmpty())
			nameMap.remove(name);
		idMap.remove(workspace.getId());
	}
	
	public static void initMaps(ServiceContext sc) {
		Map<Integer, Object> idMap = getIdMap(sc, myClass);
		if(idMap==null) {
			idMap = new HashMap<Integer, Object>();
			setIdMap(sc, myClass, idMap);
		}

		Map<String, Object> nameMap = getNameMap(sc, myClass);
		if(nameMap==null) {
			nameMap = new HashMap<String, Object>();
			setNameMap(sc, myClass, nameMap);
		}
	}


	/*
	public void CreateAndPersist(ServiceContext sc) {
		//final String myName = ".CreateAndPersist: ";
		id = persistNew(sc.getConnection(), name, description, owner_id);
	}
	*/
		
	public static Workspace CreateAndPersist(ServiceContext sc, 
			String name, String description, int owner_id) {
		//final String myName = ".CreateAndPersist: ";
		// This will create with default life windows and generation values.
		Workspace workspace = new Workspace(CachedEntity.UNSET_ID_VALUE, name, description, owner_id); 
		workspace.persist(sc.getConnection(), CachedEntity.SHALLOW_PERSIST);
		return workspace;
	}
	
	private static int persistNew(Connection dbConn, 
			String name, String description, int owner_id, 
			double activeLifeWindow, double activeLifeStdDev, long generationOffset) {
		final String myName = ".persistNew: ";
		final String INSERT_STMT = 
			"INSERT INTO workspace(name, description, owner_id, "
			+ " activeLifeWindow, activeLifeStdDev, generationOffset, creation_time)"
			+" VALUES(?,?,?,?,?,?,now())";
		int newId = 0;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(INSERT_STMT, 
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, name);
			stmt.setString(2, description);
			stmt.setInt(3, owner_id);
			stmt.setDouble(4, activeLifeWindow);
			stmt.setDouble(5, activeLifeStdDev);
			stmt.setLong(6, generationOffset);
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
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return newId;
	}
	
	public void persist(Connection dbConn, boolean shallow) {
		final String myName = ".persist: ";
		if(id<=CachedEntity.UNSET_ID_VALUE) {
			id = persistNew(dbConn, name, description, owner_id, activeLifeWindow, activeLifeStdDev, generationOffset);
		} else {
			// Note that we do not update the owner_id - moving them is not allowed 
			final String UPDATE_STMT = 
				"UPDATE workspace SET name=?, description=?, "
				+" activeLifeWindow=?, activeLifeStdDev=?, generationOffset=?"
				+" WHERE id=?";
			try {
				PreparedStatement stmt = dbConn.prepareStatement(UPDATE_STMT);
				stmt.setString(1, name);
				stmt.setString(2, description);
				stmt.setDouble(3, activeLifeWindow);
				stmt.setDouble(4, activeLifeStdDev);
				stmt.setLong(5, generationOffset);
				stmt.setInt(6, id);
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
	
	/**
	 * This ensures that a CollapserRule persistence is created, or if it exists, updates
	 * the passed rule.
	 * @param dbConn
	 * @param rule	The rule to create or update-from-DB
	 */
	protected void persistCheckCollapserRule(Connection dbConn, CollapserRuleBase rule) {
		final String myName = ".persistCheckCollapserRule: ";
		// First, try to find the rule in the DB, and if found, update the passed rule from the DB
		final String SELECT_STMT = 
				"SELECT weight FROM wksp_collapser_rule"
				+" WHERE wksp_id=? AND name=? AND item='"+CollapserRule.NO_ITEM_SPEC+"'";
		// If we have to persist, set the rule weight, associating it to the workspace and the rule name
		final String INSERT_STMT = 
			"INSERT INTO wksp_collapser_rule(wksp_id, name, item, weight, creation_time)"
			+" VALUES(?,?,'"+CollapserRule.NO_ITEM_SPEC+"',?,now())";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_STMT);
			stmt.setInt(1, this.id);
			stmt.setString(2, rule.getName());
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				rule.setWeight(rs.getDouble("weight"));
				return;
			}
			rs.close();	// Release this before doing more DB work.
			stmt.close();
			// We found no row for this rule, so create one.
			stmt = dbConn.prepareStatement(INSERT_STMT);
			stmt.setInt(1, this.id);
			stmt.setString(2, rule.getName());
			stmt.setDouble(3, rule.getWeight());
			stmt.executeUpdate();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return;
	}
	
	protected void persistCollapserRule(Connection dbConn, CollapserRuleBase rule) {
		// We persist the rule weight, keying on the workspace and the rule name
		final String myName = ".persistCollapserRule: ";
		final String UPDATE_STMT = 
				"UPDATE wksp_collapser_rule SET weight=?"
				+" WHERE wksp_id=? AND name=? AND item='"+CollapserRule.NO_ITEM_SPEC+"'";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(UPDATE_STMT);
			stmt.setDouble(1, rule.getWeight());
			stmt.setInt(2, this.id);
			stmt.setString(3, rule.getName());
			stmt.executeUpdate();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return;
	}
	
	protected void persistCheckCollapserMatrixRuleItemPairs(Connection dbConn, CollapserRuleBase rule) {
		final String myName = ".persistCheckCollapserMatrixRuleItemPairs: ";
		// First, try to find the rule pairs in the DB, and if any found, update the passed rule pairs from the DB
		final String SELECT_STMT = 
				"SELECT weight, item FROM wksp_collapser_rule"
				//Need to exclude the one entry for the rule weight, inthe same table 
				+" WHERE wksp_id=? AND name=? AND item!='"+CollapserRule.NO_ITEM_SPEC+"'";
		final String INSERT_STMT = 
			"INSERT INTO wksp_collapser_rule(wksp_id, name, item, weight, creation_time)"
			+" VALUES(?,?,?,?,now())";
		
		if(!(rule instanceof RoleMatrixDiscountRule)) {
			String tmp = myClass+myName+"Rule: "+ rule.getName()+" is not a Matrix rule!";
			throw new RuntimeException(tmp);
		}
		// Get the matrix pairs, and either update them from the DB, or insert them. 
		// We have to add any we find that are missing, since adding a new corpus to a
		// workspace could introduce new rows/columns
		List<MatrixItemInfo> pairWeights = ((RoleMatrixDiscountRule)rule).getMatrixValues();
		try {
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_STMT);
			stmt.setInt(1, this.id);
			stmt.setString(2, rule.getName());
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				String itemSpec = rs.getString("item");
				double weight = rs.getDouble("weight");
				findAndUpdateMatrixPair(((RoleMatrixDiscountRule)rule), pairWeights, itemSpec, weight);	// Will remove matched matrixPair
			}
			if(pairWeights.isEmpty())	// We found all the pairs in the DB - we're done!
				return;
			logger.debug(myClass+myName+"Matrix Rule: "+ rule.getName()+" needs to persist some pairs.");
			rs.close();	// Release this before doing more DB work.
			stmt.close();
			// Now we have to add pair weights we did not already find
			// We found no row for this rule, so create one.
			stmt = dbConn.prepareStatement(INSERT_STMT);
			stmt.setInt(1, this.id);
			stmt.setString(2, rule.getName());
			for(MatrixItemInfo miInfo : pairWeights) {
				String key = RoleMatrixDiscountRule.getKeyForRowCol(miInfo.getRow(), miInfo.getCol());
				stmt.setString(3, key);
				stmt.setDouble(4, rule.getWeight());
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return;
	}
	
	protected void persistClearCollapserMatrixRuleItemPairs(Connection dbConn, CollapserRuleBase rule) {
		final String myName = ".persistClearCollapserMatrixRuleItemPairs: ";
		// First, try to find the rule pairs in the DB, and if any found, update the passed rule pairs from the DB
		final String DELETE_STMT = 
				"DELETE FROM wksp_collapser_rule"
				//Need to exclude the one entry for the rule weight, in the same table 
				+" WHERE wksp_id=? AND name=? AND item!='"+CollapserRule.NO_ITEM_SPEC+"'";
		
		if(!(rule instanceof RoleMatrixDiscountRule)) {
			String tmp = myClass+myName+"Rule: "+ rule.getName()+" is not a Matrix rule!";
			throw new RuntimeException(tmp);
		}
		try {
			PreparedStatement stmt = dbConn.prepareStatement(DELETE_STMT);
			stmt.setInt(1, this.id);
			stmt.setString(2, rule.getName());
			int rows = stmt.executeUpdate();
			logger.debug(myClass+myName+" cleared "+rows+" ItemPairs for Matrix Rule: "+ rule.getName());
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return;
	}
	
	// TODO Make rule be a MatrixBaseClass once this is refactored
	private void findAndUpdateMatrixPair(RoleMatrixDiscountRule rule, List<MatrixItemInfo> pairWeights, 
			String itemSpec, double weight) {
		final String myName = ".findAndUpdateMatrixPair: ";
		for(MatrixItemInfo miInfo : pairWeights) {
			String[] substrings = RoleMatrixDiscountRule.getRowColFromKey(itemSpec);
			if(substrings.length != 2) {
				String tmp = myClass+myName+"itemSpec did produce row-col names!: "+ itemSpec;
				throw new RuntimeException(tmp);
			}
			if(miInfo.getRow().equalsIgnoreCase(substrings[0]) 
				&& miInfo.getCol().equalsIgnoreCase(substrings[1])) {
				rule.setPairWeight(substrings[0], substrings[1], weight);
				pairWeights.remove(miInfo);	// So caller knows we found this one
				return;
			}
		}
	}
	
	protected void persistCollapserMatrixRuleItemPair(Connection dbConn, 
			CollapserRuleBase rule, String item, double itemWeight ) {
		// We persist the rule weight, keying on the workspace and the rule name
		final String myName = ".persistCollapserMatrixRuleItemPair: ";
		final String UPDATE_STMT = 
				"UPDATE wksp_collapser_rule SET weight=?"
				+" WHERE wksp_id=? AND name=? AND item=?";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(UPDATE_STMT);
			stmt.setDouble(1, itemWeight);
			stmt.setInt(2, this.id);
			stmt.setString(3, rule.getName());
			stmt.setString(4, item);
			stmt.executeUpdate();
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new WebApplicationException( 
					Response.status(
							Response.Status.INTERNAL_SERVER_ERROR).entity(tmp).build());
		}
		return;
	}
	
	public void persistAttachedEntities(Connection dbConn) {
	}
	
	public static List<Workspace> ListAll(ServiceContext sc) {
		// TODO Add pagination support
		return ListAllForUser(sc, -1);
	}
	
	public static List<Workspace> ListAllForUser(ServiceContext sc, int user_id) {
		// TODO Add pagination support
		final String SELECT_ALL = 
				"SELECT w.id wid, w.owner_id, w.name, w.description, c.id cid,"
				+" w.activeLifeWindow, w.activeLifeStdDev, w.generationOffset"
				+" FROM workspace w LEFT JOIN corpus c ON c.wksp_id=w.id";
		final String WHERE_USER = " WHERE w.owner_id=?";
		ArrayList<Workspace> wkspList = new ArrayList<Workspace>();
		try {
			initMaps(sc);
			Map<Integer, Object> idMap = getIdMap(sc, myClass);
			Connection dbConn = sc.getConnection();
			String query = SELECT_ALL;
			if(user_id>=0)
				query += WHERE_USER;
			PreparedStatement stmt = dbConn.prepareStatement(query);
			if(user_id>=0)
				stmt.setInt(1, user_id);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				// Look for the workspace in the cache. If not found, create
				// and add it
				int wid = rs.getInt("wid");
				Workspace workspace = (Workspace)idMap.get(wid);
				if(workspace==null) {
					workspace = new Workspace(wid, 
						rs.getString("name"), 
						rs.getString("description"),
						rs.getInt("owner_id"),
						rs.getDouble("activeLifeWindow"),
						rs.getDouble("activeLifeStdDev"),
						rs.getLong("generationOffset") );
					int cid = rs.getInt("cid");
					if(cid>0)
						workspace.corpus = Corpus.FindByID(sc, cid);
					workspace.init(sc, cid>0);	// If have a corpus, set up a collapser
				}
				wkspList.add(workspace);
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".ListAllForUser(): Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new WebApplicationException( 
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							"Problem creating workspace\n"+se.getLocalizedMessage()).build());
		}
		//for(Workspace workspace:wkspList)
		//	workspace.findAndLoadCorpus(sc);
		return wkspList;
	}
	
	public static boolean Exists(ServiceContext sc, int id) {
		boolean exists = false;
		final String SELECT_BY_ID = 
			"SELECT name FROM workspace WHERE id = ?";
		try {
			Connection dbConn = sc.getConnection();
			PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_ID);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				if(rs.getString("name")!=null)
					exists = true;
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			// Just absorb it
			String tmp = myClass+".Exists: Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
		}
		return exists;
	}

	public static Workspace FindByID(ServiceContext sc, int id) {
		final String myName = ".FindByID: ";
		final String SELECT_BY_ID = 
			"SELECT w.id wid, w.name, w.description, w.owner_id, c.id cid,"
			+" w.activeLifeWindow, w.activeLifeStdDev, w.generationOffset"
			+" FROM workspace w LEFT JOIN corpus c ON c.wksp_id=w.id"
			+" WHERE w.id=?";
		Workspace workspace = null;
		try {
			initMaps(sc);
			Map<Integer, Object> idMap = getIdMap(sc, myClass);
			workspace = (Workspace)idMap.get(id);
			if(workspace==null) {
				Connection dbConn = sc.getConnection();
				PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_ID);
				stmt.setInt(1, id);
				ResultSet rs = stmt.executeQuery();
				if(rs.next()){
					workspace = new Workspace(
							rs.getInt("wid"), 
							rs.getString("name"), 
							rs.getString("description"), 
							rs.getInt("owner_id"),
							rs.getDouble("activeLifeWindow"),
							rs.getDouble("activeLifeStdDev"),
							rs.getLong("generationOffset") );
					int cid = rs.getInt("cid");
					if(cid>0)
						workspace.corpus = Corpus.FindByID(sc, cid);
				}
				rs.close();
				stmt.close();
				workspace.init(sc, true);
			}
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new RuntimeException( tmp );
		}
		//workspace.findAndLoadCorpus(sc);
		return workspace;
	}

	public static Workspace FindByName(ServiceContext sc, int user_id, String name) {
		final String myName = ".FindByName: ";
		final String SELECT_BY_NAME = 
			"SELECT w.id wid, w.name, w.description, c.id cid,"
			+" w.activeLifeWindow, w.activeLifeStdDev, w.generationOffset"
			+" FROM workspace w LEFT JOIN corpus c ON c.wksp_id=w.id"
			+" WHERE w.name=? AND w.owner_id=?";
		Workspace workspace = null;
		try {
			initMaps(sc);
			Map<String, Object> nameMap = getNameMap(sc, myClass);
			workspace = (Workspace)nameMap.get(name);
			if(workspace==null) {
				Connection dbConn = sc.getConnection();
				PreparedStatement stmt = dbConn.prepareStatement(SELECT_BY_NAME);
				stmt.setString(1, name);
				stmt.setInt(2, user_id);
				ResultSet rs = stmt.executeQuery();
				if(rs.next()){
					workspace = new Workspace(
							rs.getInt("wid"), 
							rs.getString("name"), 
							rs.getString("description"), 
							user_id,
							rs.getDouble("activeLifeWindow"),
							rs.getDouble("activeLifeStdDev"),
							rs.getLong("generationOffset") );
					int cid = rs.getInt("cid");
					if(cid>0)
						workspace.corpus = Corpus.FindByID(sc, cid);
				}
				rs.close();
				stmt.close();
				workspace.init(sc, true);
			}
		} catch(SQLException se) {
			String tmp = myClass+myName+"Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new RuntimeException( tmp );
		}
		//workspace.findAndLoadCorpus(sc);
		return workspace;
	}
	
	public void deletePersistence(Connection dbConn) {
		deleteAttachedEntities(dbConn);
		DeletePersistence(dbConn, id);
	}
	
	public static void DeletePersistence(Connection dbConn, int id) {
		final String DELETE_STMT = "DELETE FROM workspace WHERE id=?";
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
	
	public void deleteAttachedEntities(Connection dbConn) { // TODO delete all the collapser rules
		final String DELETE_STMT = "DELETE FROM wksp_collapser_rule WHERE wksp_id=?";
		try {
			PreparedStatement stmt = dbConn.prepareStatement(DELETE_STMT);
			stmt.setInt(1, id);
			stmt.executeUpdate();
			stmt.close();
		} catch(SQLException se) {
			String tmp = myClass+".deleteAttachedEntities: Problem querying DB.\n"+ se.getMessage();
			logger.error(tmp);
			throw new RuntimeException( tmp );
		}
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	@XmlElement(name="generationOffset")
	public double getGenerationOffsetYears() {
		return TimeUtils.convertMillisToYears(generationOffset);
	}

	public void setGenerationOffsetYears(double years) {
		generationOffset = TimeUtils.convertYearsToMillis(Math.abs(years));
	}

	@XmlElement(name="activeLife")
	public double getActiveLifeYears() {
		return TimeUtils.computeActiveLifeYearsFromWindow(activeLifeWindow);
	}

	public void setActiveLifeWindowYears(double years) {
		years = Math.abs(years);
		activeLifeWindow = TimeUtils.getDefaultWindowForActiveLife(years);
		activeLifeStdDev = TimeUtils.getDefaultStdDevForActiveLife(years);
	}

	// @XmlElement(name="activeLifeStdDev") Not clear why UI needs this
	public double getActiveLifeStdDevYears() {
		return TimeUtils.convertMillisToYears(Math.round(activeLifeStdDev));
	}

	/* This is dangerous - we compute it from the Life Window 
	public void setActiveLifeStdDevYears(double years) {
		activeLifeStdDev = TimeUtils.convertYearsToMillis(years);
	} */

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the builtFromCorpus
	 */
	@XmlElement(name="builtFromCorpus")
	public int getBuiltFromCorpus() {
		return (corpus==null)?0:corpus.getCloneOfId();
	}

	/**
	 * @return the owner_id
	 */
	public int getOwner_id() {
		return owner_id;
	}

	@XmlElement(name="importedCorpusName")
	public String getImportedCorpusName() {
		if(corpus!=null) {
			return corpus.getName();
		} else {
			return null;
		}
	}

	@XmlElement(name="medianDocDate")
	public String getMedianDocumentDateStr() {
		if(corpus!=null) {
			return corpus.getMedianDocumentDateStr();
		} else {
			return null;
		}
	}

	public long getMedianDocumentDate() {
		if(corpus!=null) {
			return corpus.getMedianDocumentDate();
		} else {
			return 0;
		}
	}

	/**
	 * @return Name.
	 */
	public String toString() {
		return "{id:"+id
		+", name:"+((name==null)?"(null)":name)
		+"}";
	}

	/**
	 * @return current local corpus
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * Clears all existing resources associated with any current corpus, and then
	 * sets the new corpus.
	 * @param corpus the new corpus to use
	 */
	public void setCorpus(ServiceContext sc, Corpus newCorpus) {
		final String myName = ".setCorpus: ";
		// Delete any existing corpus clone and all of its attachedEntities
		if(this.corpus!=null && newCorpus!=null
				&& this.corpus.getId()==newCorpus.getId() ) {
			String tmp = myClass+myName+
				"newCorpus same as current - ignoring setCorpus() call.";
			logger.debug(tmp);
			return;	// Do not update if the same corpus
		}
		if(newCorpus!=null) {
			if( newCorpus.getCloneOfId()<=0) { 
				String tmp = myClass+myName+"newCorpus must be a clone";
				logger.error(tmp);
				throw new RuntimeException(tmp);
			}
			if( newCorpus.getWkspId()!=id ) {
				String tmp = myClass+myName+"newCorpus appears to be owned by another workspace";
				logger.error(tmp);
				throw new RuntimeException(tmp);
			}
		}
		if(this.corpus!=null)
			this.corpus.deletePersistence(sc);
		clearEntityMaps();
		this.corpus = newCorpus;
		if(this.corpus!=null) {
			if(collapser==null) {
				setupCollapser(sc);
			} else {
				// Make sure any rules that are corpus dependent get reinitialized and persisted
				updateCollapserFromCorpus(sc);
			}
			rebuildEntitiesFromCorpus(sc);
		}
	}
	
	public CollapserBase getCollapser() {
		return collapser;
	}
	
	public void updateCollapser(CollapserBase updatedCollapser) {
		collapser = new PersonCollapser(updatedCollapser);
	}
	
	public void setupCollapser(ServiceContext sc) {
		
		Connection dbConn = sc.getConnection();
		// Create our collapser instance.
		collapser = new PersonCollapser();
		// Add the basic rules - 
		// TODO - this should be configured somehow, but how?
		
		collapser.addUIGroup("Step1A", true, "Step 1A: Consider equally qualified names");
		collapser.addUIGroup("Step1B", true, "Step 1B: Consider compatible, but not equally qualified names");
		collapser.addUIGroup("Step1C", true, "Step 1C: Consider the roles of persons");
		collapser.addUIGroup("Step2A", false, "Step 2A: Consider equally qualified names");
		collapser.addUIGroup("Step2B", false, "Step 2B: Consider compatible, but not equally qualified names");
		// If we make the role matrix work across docs, add this 
		// collapser.addUIGroup("Step2C", "Step 2C: Consider the roles of persons");
		
		// TODO - we need to think about collapse rules for missing forename cases
		CollapserRuleBase rule;
		rule = new FullyQualifiedEqualNameShiftRule(CollapserRuleBaseWithUI.WEIGHT_ALWAYS, 
													CollapserRule.WITHIN_DOCUMENTS);
		rule.initialize(this);
		collapser.addRule(rule);
		// TODO consider refactoring this to get all the rules for this workspace in one query,
		// and setting the weights. If not found, can create them all. 
		persistCheckCollapserRule(dbConn, rule);
		
		rule = new FullyQualifiedEqualNameShiftRule(CollapserRuleBaseWithUI.WEIGHT_ALWAYS,
													CollapserRule.ACROSS_DOCUMENTS);
		rule.initialize(this);
		collapser.addRule(rule);
		persistCheckCollapserRule(dbConn, rule);
		
		rule = new PartlyQualifiedEqualNameShiftRule(CollapserRuleBaseWithUI.WEIGHT_AGGRESSIVE,
													CollapserRule.WITHIN_DOCUMENTS);
		rule.initialize(this);
		collapser.addRule(rule);
		persistCheckCollapserRule(dbConn, rule);
		
		rule = new PartlyQualifiedEqualNameShiftRule(CollapserRuleBaseWithUI.WEIGHT_AGGRESSIVE,
													CollapserRule.ACROSS_DOCUMENTS);
		rule.initialize(this);
		collapser.addRule(rule);
		persistCheckCollapserRule(dbConn, rule);
		
		rule = new UnqualifiedEqualNameShiftRule(CollapserRuleBaseWithUI.WEIGHT_AGGRESSIVE,
													CollapserRule.WITHIN_DOCUMENTS);
		rule.initialize(this);
		collapser.addRule(rule);
		persistCheckCollapserRule(dbConn, rule);
		
		rule = new UnqualifiedEqualNameShiftRule(CollapserRuleBaseWithUI.WEIGHT_CONSERVATIVE,
													CollapserRule.ACROSS_DOCUMENTS);
		rule.initialize(this);
		collapser.addRule(rule);
		persistCheckCollapserRule(dbConn, rule);
		
		rule = new PartlyQualifiedCompatibleNameShiftRule(CollapserRuleBaseWithUI.WEIGHT_CONSERVATIVE,
													CollapserRule.WITHIN_DOCUMENTS);
		rule.initialize(this);
		collapser.addRule(rule);
		persistCheckCollapserRule(dbConn, rule);
		
		rule = new PartlyQualifiedCompatibleNameShiftRule(CollapserRuleBaseWithUI.WEIGHT_AGGRESSIVE,
													CollapserRule.ACROSS_DOCUMENTS);
		rule.initialize(this);
		collapser.addRule(rule);
		persistCheckCollapserRule(dbConn, rule);
		
		rule = new UnqualifiedCompatibleNameShiftRule(CollapserRuleBaseWithUI.WEIGHT_AGGRESSIVE,
													CollapserRule.WITHIN_DOCUMENTS);
		rule.initialize(this);
		collapser.addRule(rule);
		persistCheckCollapserRule(dbConn, rule);
		
		rule = new UnqualifiedCompatibleNameShiftRule(CollapserRuleBaseWithUI.WEIGHT_CONSERVATIVE,
													CollapserRule.ACROSS_DOCUMENTS);
		rule.initialize(this);
		collapser.addRule(rule);
		
		RoleMatrixDiscountRule rmdRule = new RoleMatrixDiscountRule();	// Only applies within docs now...
		rmdRule.initialize(this);
		// RoleMatrixDiscountRule.init tries to find and add roles for witness and preclude
		// collapsing with other non-family roles.
		collapser.addRule(rmdRule);
		persistCheckCollapserRule(dbConn, rmdRule);
		// Persist the matrix cell settings - will update the rule if they already exist. 
		persistCheckCollapserMatrixRuleItemPairs(dbConn, rmdRule);
		
	}
	
	/*
	 * If we ever refresh from a corpus, or augment a corpus, we need to clean up rules
	 * that depend upon the corpus. This should probably invoke an interface specifically for this. 
	 */ 
	public void updateCollapserFromCorpus(ServiceContext sc) {
		Connection dbConn = sc.getConnection();
		List<CollapserRuleBase> ruleList = 
				collapser.getRules(CollapserRule.ALL_RULES, CollapserRule.WITHIN_DOCUMENTS);
		for(CollapserRuleBase rule:ruleList) {
			if(rule instanceof RoleMatrixDiscountRule) {
				// Clear out any previous ItemPairs so we do not have stale info
				persistClearCollapserMatrixRuleItemPairs(dbConn, rule);
				// Now determine the new pairs needed for this corpus
				rule.initialize(this);
				// Persist the matrix cell settings. 
				persistCheckCollapserMatrixRuleItemPairs(dbConn, (RoleMatrixDiscountRule)rule);
			}
		}
	}
	
	public void updateWeightForCollapserRule(ServiceContext sc, double weight, String collapserRuleName ) {
		final String myName = ".updateWeightForCollapserRule: ";
		if(collapser == null) {
			String tmp = myClass+myName+"Workspace has no collapser.";
			logger.error(tmp);
			throw new RuntimeException(tmp);
		}
		CollapserRuleBase rule = collapser.findRuleByName(collapserRuleName);
		if(rule == null) {
			String tmp = myClass+myName+"Workspace collapser has no rule named: "+collapserRuleName;
			logger.error(tmp);
			throw new RuntimeException(tmp);
		}
		rule.setWeight(weight);
		persistCollapserRule(sc.getConnection(), rule);
	}
	
	/**
	 * @param sc
	 * @param itemWeight The weight to apply when for the combination of row and column
	 * @param collapserRuleName	The name of the Matrix rule
	 * @param rowName  The name of the row for the item to be set
	 * @param colName  The name of the column for the item to be set
	 */
	public void updateItemWeightForCollapserMatrixRule(ServiceContext sc, double itemWeight, 
						String collapserRuleName, String rowName, String colName ) {
		final String myName = ".updateItemWeightForCollapserMatrixRule: ";
		if(collapser == null) {
			String tmp = myClass+myName+"Workspace has no collapser.";
			logger.error(tmp);
			throw new RuntimeException(tmp);
		}
		CollapserRuleBase rule = collapser.findRuleByName(collapserRuleName);
		if(rule == null) {
			String tmp = myClass+myName+"Workspace collapser has no rule named: "+collapserRuleName;
			logger.error(tmp);
			throw new RuntimeException(tmp);
		}
		if(!(rule instanceof CollapserRulePairMatrixUI)) {
			String tmp = myClass+myName+"Workspace collapser rule: "+collapserRuleName+
							" is not a matrix rule.";
			logger.error(tmp);
			throw new RuntimeException(tmp);
		}
		((CollapserRulePairMatrixUI)rule).setPairWeight(rowName, colName, itemWeight);
		String itemSpec = RoleMatrixDiscountRule.getKeyForRowCol(rowName, colName);
		persistCollapserMatrixRuleItemPair(sc.getConnection(), rule, itemSpec, itemWeight);
	}
	
	public void collapseWithinDocuments() {
		// This needs to run before we call collapseAcrossDocuments
		if(collapser==null)
			return;
		if(corpus==null)
			return;
		List<Integer> docIDList = new ArrayList<Integer>(personListsByNameByDoc.keySet());
		// Must have at least 2 Persons to collapse
		if(docIDList==null || docIDList.isEmpty())
			return;
		// Let's run through in doc order for easier understanding.
		Collections.sort(docIDList);
		for(Integer docID:docIDList) {
			HashMap<Integer, ArrayList<Person>> personListMapForDoc = 
												getPersonListMapForDoc(docID);
			for(ArrayList<Person> personsByName:personListMapForDoc.values()) {
				// Do not bother if there is only 1 of a given name
				if(personsByName.size() > 1)
					collapser.evaluateList(personsByName,
						personToEntityLinkSets, CollapserRule.WITHIN_DOCUMENTS);
			}
		}
	}
	
	public void collapseAcrossDocuments() {
		// This needs to run before we call collapseAcrossDocuments
		if(collapser==null)
			return;
		if(corpus==null)
			return;
		for(ArrayList<Person> personsByName:personListsByName.values()) {
			// Do not bother if there is only 1 of a given name
			// Note that evaluateList will skip all the intraDoc cases in this list
			if(personsByName.size() > 1)
				collapser.evaluateList(personsByName, // UNUSED nradToEntityLinks, 
					personToEntityLinkSets, CollapserRule.ACROSS_DOCUMENTS);
		}
	}
	
	public GraphWrapper getFullGraph(boolean fUseQNameForNames, double minWeightOnLink) {
		//	It appears that some code in the SNA libs assume that the ids for vertices and edges
		// are 0 based, so we must create factories each time we build a graphWrapper graph. 
		VertexFactory vertexFactory= new VertexFactory();  
		EdgeFactory edgeFactory= new EdgeFactory();
		GraphWrapper graph = new GraphWrapper();
		HashMap<String, Vertex> existingVertices = new HashMap<String, Vertex>(); 
		for(GraphPersonsLink gpl:graphLinks.values()) {
			if(gpl.weight >=  minWeightOnLink )
				gpl.addToGraph(graph, existingVertices, vertexFactory, edgeFactory, fUseQNameForNames);
		}
		return graph;
	}
	
	public void buildGraphLinks() {
		// We loop over the docs, getting sets of nrads. 
		// Then for each nrad we consider each Person it is linked to
		// From this we build a set of Persons linked to the doc, 
		//   and where we see multiple links for the same Person, keep the highest weight (and associated role)
		// Then we build the combinatorial set of GraphPersonsLinks through the Persons
		List<Document> docList = corpus.getDocuments();
		// Map of person Links, indexed by the Person id
		Map<Integer, EntityLink<NameRoleActivity>> personLinksToDoc = new HashMap<Integer, EntityLink<NameRoleActivity>>(); 
		for(Document doc:docList) {
			personLinksToDoc.clear();
			List<NameRoleActivity> nrads = doc.getNonFamilyNameRoleActivities();
			for(NameRoleActivity nrad:nrads) {
				// For the nrad, we need to get the Person Links
				EntityLinkSet<NameRoleActivity> personsForNRAD = nradToEntityLinks.get(nrad.getId());
				if(personsForNRAD!=null) {	// This would be strange, but be careful anyway
					Collection<EntityLink<NameRoleActivity>> links = personsForNRAD.values();
					if(links!=null) {		// Again, just for safety
						for(EntityLink<NameRoleActivity> linkToPerson:links) {
							if(linkToPerson.getType() == LinkType.Type.LINK_TO_PERSON) { // skip clans, etc.
								// Check the Person linked to, see if we have a stronger weight already
								int personId = linkToPerson.entity.getId();
								EntityLink<NameRoleActivity> existingLink = personLinksToDoc.get(personId);
								if((existingLink == null) 	// None yet, so add this one
										|| (existingLink.getWeight() < linkToPerson.getWeight())) {
									// OR the existing one is less weight => replace with the new one
									personLinksToDoc.put(personId, linkToPerson);
								}
							}
						}
					}
				}
			} // close loop over all nrads in doc
			// Now we consider each pair of persons
			ArrayList<Integer> personList = new ArrayList<Integer>(personLinksToDoc.keySet());
			// Sort by personID so we get consistent pairs
			Collections.sort(personList);	// Will use the natural integer sorting
			int listLen = personList.size();
			for(int i=0; i<listLen-1; i++) {	// Note that we do not iterate for the last item
				int fromId = personList.get(i);
				EntityLink<NameRoleActivity> fromELink = personLinksToDoc.get(fromId);
				// Loop over the following persons
				for(int j=i+1; j<listLen; j++) {
					int toId = personList.get(j);
					EntityLink<NameRoleActivity> toELink = personLinksToDoc.get(toId);
					// Now we can add a link for these two
					addGraphLink(fromELink, toELink);
				}
			}
		}
	}
	
	protected void addGraphLink(EntityLink<NameRoleActivity> nradLink1, 
								EntityLink<NameRoleActivity> nradLink2 ) {
		/* Assume that the two persons are ordered for undirected hashing (first id < second)
		// We order by ID, since the links or undirected, and this simplifies lookup.
		if( pers1.getId() > pers2.getId()) {
			Person ptemp = pers1;
			pers1 = pers2;
			pers2 = ptemp;
			EntityLink<NameRoleActivity> nradLinkTmp = nradLink1;
			nradLink1 = nradLink2;
			nradLink2 = nradLinkTmp;
		} */
		double weight = nradLink1.getWeight() * nradLink2.getWeight();
		// Filter out persons that have no more weight
		if(weight==0)
			return;
		String role1 = nradLink1.fromObj.getRoleString();
		String role2 = nradLink2.fromObj.getRoleString();
		Person pers1 = (Person)nradLink1.getEntity();
		Person pers2 = (Person)nradLink2.getEntity();
		String gplHash = GraphPersonsLink.createARIntHash(pers1, pers2);
		GraphPersonsLink gpl = graphLinks.get(gplHash);
		if(gpl == null) {
			gpl = new GraphPersonsLink(pers1, role1, pers2, role2, weight);
			graphLinks.put(gplHash, gpl);
		} else {
			// Already there, so we add in the additional weight and role-interaction 
			gpl.addLink(role1, role2, weight);
		}
	}
	
	private void clearEntityMaps() {
		// GC will deal with the lists, etc.
		personListsByNameByDoc.clear();
		personListsByName.clear();
		personToEntityLinkSets.clear();
		clanListsByDoc.clear();
		clansByName.clear();
		nradToEntityLinks.clear();
		graphLinks.clear();
	}
	
	/**
	 * @return the activeLifeStdDev
	 */
	public double getActiveLifeStdDev() {
		return activeLifeStdDev;
	}

	/** This should be set from the ActiveLife.
	 * @param activeLifeStdDev the activeLifeStdDev to set
	public void setActiveLifeStdDev(double activeLifeStdDev) {
		this.activeLifeStdDev = activeLifeStdDev;
	}
	 */

	/**
	 * @return the activeLifeWindow
	 */
	public double getActiveLifeWindow() {
		return activeLifeWindow;
	}

	/**
	 * @param activeLifeWindow the activeLifeWindow to set
	 * @see   setActiveLifeWindowYears for the typical setter from a UI or config.
	 */
	public void setActiveLifeWindow(double activeLifeWindow) {
		this.activeLifeWindow = activeLifeWindow;
	}

	/**
	 * Fetches the map from the main map indexed by docId. If the main map
	 * does not yet have such a map, creates one and adds it to the main map.
	 * @param docId		the document of interest
	 * @return 			hashmap of lists of Persons, indexed by Name (id) 
	 */
	protected HashMap<Integer, ArrayList<Person>> getPersonListMapForDoc(int docId) {
		HashMap<Integer, ArrayList<Person>> personListMapForDoc = 
			personListsByNameByDoc.get(docId);
		if(personListMapForDoc==null) {
			personListMapForDoc = new HashMap<Integer, ArrayList<Person>>();
			personListsByNameByDoc.put(docId, personListMapForDoc);
		}
		return personListMapForDoc;
	}

	/**
	 * Fetches the map from the main map indexed by docId. If the main map
	 * does not yet have such a map, creates one and adds it to the main map.
	 * @param docId		the document of interest
	 * @return 			hashmap of lists of Persons, indexed by Name (id) 
	 */
	protected ArrayList<Clan> getClanListForDoc(int docId) {
		ArrayList<Clan> clanListForDoc = clanListsByDoc.get(docId);
		if(clanListForDoc==null) {
			clanListForDoc = new ArrayList<Clan>();
			clanListsByDoc.put(docId, clanListForDoc);
		}
		return clanListForDoc;
	}

	/**
	 * Fetches the List of Persons for a given forename, from a map of such lists. If the map
	 * does not yet have such a list, creates one and adds it to the main map.
	 * @param personListMap	the map of Lists of Persons for each forename
	 * @param nameId		the id of the forename of interest
	 * @return				the list of Persons
	 */
	protected ArrayList<Person> getPersonListName(
			HashMap<Integer, ArrayList<Person>> personListMap, int nameId) {
		ArrayList<Person> personList = personListMap.get(nameId);
		if(personList == null) {
			personList = new ArrayList<Person>(); 
			personListMap.put(nameId, personList);
		}
		return personList;
	}
	
	/**
	 * Fetches the List of Persons associated to NRADS in a doc.
	 * @param docId			the document of interest
	 * @return				the list of Persons
	 */
	public List<Person> getPersonsForDoc(int docId) {
		List<Person> personList = new ArrayList<Person>(); 
		
		// Need to get unique persons, so we build a map indexed by the original NRAD id
		// Various names could be multiply linked to the same persons, so can't just aggregate
		// all the lists.
		HashMap<Integer, Person> personsForDoc = new HashMap<Integer, Person>();

		// Get all the maps of Persons linked to NRADs (forenames) in this doc
		HashMap<Integer, ArrayList<Person>> personListMapForDoc = 
				getPersonListMapForDoc(docId);
		// Loop across all the forenames
		for(ArrayList<Person> personsByName:personListMapForDoc.values()) {
			// Loop across all the Persons linked to that name
			for(Person person:personsByName) {
				int origNRAD = person.getOriginalNRAD().getId();
				if(!personsForDoc.containsKey(origNRAD)) {
					personsForDoc.put(origNRAD, person);
				}
			}
		}
		// Add all the collected Persons to the array list and sort by displayName
		personList.addAll(personsForDoc.values());
		Collections.sort(personList, new Person.DisplayNameComparator());		

		return personList;
	}

	/**
	 * Fetches the List of Persons associated to NRADS in all docs in the workspace.
	 * @param docId			the document of interest
	 * @return				the list of Persons
	 */
	public List<Person> getPersonsForAllDocs() {
		List<Person> personList = new ArrayList<Person>(); 
		
		// Need to get unique persons, so we build a map indexed by the original NRAD id
		// Various names could be multiply linked to the same persons, so can't just aggregate
		// all the lists.
		HashMap<Integer, Person> personsInWorkspace = new HashMap<Integer, Person>();

		// Get all the maps of Persons linked to NRADs (forenames) in this doc
		for(HashMap<Integer, ArrayList<Person>> personListMapForDoc:personListsByNameByDoc.values()) {
			// Loop across all the forenames
			for(ArrayList<Person> personsByName:personListMapForDoc.values()) {
				// Loop across all the Persons linked to that name
				for(Person person:personsByName) {
					int origNRAD = person.getOriginalNRAD().getId();
					if(!personsInWorkspace.containsKey(origNRAD)) {
						personsInWorkspace.put(origNRAD, person);
					}
				}
			}
		}
		// Add all the collected Persons to the array list and sort by displayName
		personList.addAll(personsInWorkspace.values());
		Collections.sort(personList, new Person.DisplayNameComparator());		

		return personList;
	}

	/**
	 * Fetches the List of Clans associated to NRADS in a doc.
	 * @param docId			the document of interest
	 * @return				the list of Persons
	public List<Clan> getClansForDoc(int docId) {
		List<Clan> clanList = new ArrayList<Clan>(); 
		
		// Need to get unique clans, so we build a map indexed by the original NRAD id
		// Various names could be multiply linked to the same clans, so can't just aggregate
		// all the lists.
		HashMap<Integer, Clan> clansForDoc = new HashMap<Integer, Clan>();

		// Get all the maps of Clans linked to NRADs (forenames) in this doc
		HashMap<Integer, ArrayList<Clan>> clanListMapForDoc = 
				getClanListMapForDoc(docId);
		// Loop across all the forenames
		for(ArrayList<Clan> clansByName:clanListMapForDoc.values()) {
			// Loop across all the Clans linked to that name
			for(Clan clan:clansByName) {
				int origNRAD = clan.getOriginalNRAD().getId();
				if(!clansForDoc.containsKey(origNRAD)) {
					clansForDoc.put(origNRAD, clan);
				}
			}
		}
		// Add all the collected Clans to the array list and sort by displayName
		clanList.addAll(clansForDoc.values());
		Collections.sort(clanList, new Clan.DisplayNameComparator());		

		return clanList;
	}
	 */

	/**
	 * Fetches the List of Clans associated to NRADS in all docs in the workspace.
	 * @return				the list of Clans
	 */
	public List<Clan> getClansForAllDocs() {
		List<Clan> clanList = new ArrayList<Clan>(); 
		
		// Need to get unique clans, so we build a map indexed by the original NRAD id
		// Various names could be multiply linked to the same clans, so can't just aggregate
		// all the lists.
		HashMap<Integer, Clan> clansInWorkspace = new HashMap<Integer, Clan>();

		// Get all the maps of clans linked to NRADs (forenames) in this doc
		for(ArrayList<Clan> clanListForDoc:clanListsByDoc.values()) {
			// Loop across all the Clans linked to that name
			for(Clan clan:clanListForDoc) {
				int origNRAD = clan.getOriginalNRAD().getId();
				if(!clansInWorkspace.containsKey(origNRAD)) {
					clansInWorkspace.put(origNRAD, clan);
				}
			}
		}
		// Add all the collected Clans to the array list and sort by displayName
		clanList.addAll(clansInWorkspace.values());
		Collections.sort(clanList, new Clan.DisplayNameComparator());		

		return clanList;
	}

	/*
	protected void addPersonToDocList(Person person) {
		int docID = person.getOriginalDocument().getId();
		int nameID = person.getDeclaredName().getId();
		// Add to the per-document list
		HashMap<Integer, ArrayList<Person>> personListMapForDoc = 
			getPersonListMapForDoc(docID);
		ArrayList<Person> personList = getPersonListName(personListMapForDoc, nameID);
		personList.add(person);
	}
	*/
		
	/**
	 * Creates a Person with an EvidenceBasedTimeSpan,
	 * adds the person to the personListMapForDoc, and creates
	 * a link from the nrad to the new Person with full weight
	 * @param nrad
	 * @param center the center value for the new time span
	 * @param personListMapForDoc
	 * @return the new person
	 */
	private Person addPersonForNRAD(NameRoleActivity nrad, long center,
			HashMap<Integer, ArrayList<Person>> personListMapForDoc) {
		
		Name name = nrad.getName();
		int forenameId = (name==null)?-1:name.getId();	// get Name
		// Build a timespan for the new person. Center it on the
		// document date.
		EvidenceBasedTimeSpan ts = 
			new EvidenceBasedTimeSpan(center, 
					activeLifeStdDev, activeLifeWindow);
		Person person = new Person(nrad, ts);
		if(forenameId>=0) {
			// If no declared forename, no list to add to.
			// TODO figure out what to do about unknowns, since they are
			// essentially compatible (on forename) with everything.
			// OTOH, is it really worth collapsing? Maybe only if qualified...
			ArrayList<Person> docPersonList = 
					getPersonListName(personListMapForDoc, forenameId);
				docPersonList.add(person);
			ArrayList<Person> globalPersonList = 
				getPersonListName(personListsByName, forenameId);
			globalPersonList.add(person);
			// Now, we'll remap the displayName of the person to something more sensible.
			{
				String forename = (name==null)?"(unknown)":name.getName();
				String displayName = forename+
								"["+nrad.getDocument().getAlt_id()+"."+docPersonList.size()+"]";
				person.setDisplayName(displayName);
			}
		}
		EntityLinkSet<NameRoleActivity> links = nradToEntityLinks.get(nrad.getId());
		if(links==null) {
			links = new EntityLinkSet<NameRoleActivity>(nrad, 
										LinkType.Type.LINK_TO_PERSON);
			nradToEntityLinks.put(nrad.getId(), links);
		}
		NRADEntityLink link = 
			new NRADEntityLink(nrad, person, 1.0, LinkType.Type.LINK_TO_PERSON);
		links.put(person, link);
		// Now add a link from the Person back to the linkSet for when we
		// do weight shifting
		List<EntityLinkSet<NameRoleActivity>> linkSetsList =
									personToEntityLinkSets.get(person);
		if(linkSetsList==null) {
			linkSetsList = new ArrayList<EntityLinkSet<NameRoleActivity>>();
			personToEntityLinkSets.put(person, linkSetsList);
		}
		linkSetsList.add(links);
		return person;
	}
	
	/**
	 * Creates a Person for the father with a derived time span,
	 * adds the father to the personListMapForDoc, and creates
	 * a link from the nrad to the new Person with full weight
	 * @param child
	 * @param nradFather
	 * @param personListMapForDoc
	 * @return the new father Person
	 */
	private Person addFatherForPerson(Person child, NameRoleActivity nradFather,
			HashMap<Integer, ArrayList<Person>> personListMapForDoc) {
		Name name = nradFather.getName();
		int forenameId = (name==null)?-1:name.getId();	// get Name
		Person father = child.createPersonForDeclaredFather(generationOffset, 
				activeLifeStdDev, activeLifeWindow, true);
		if(forenameId>=0) {
			// If no declared forename, no list to add to.
			// TODO figure out what to do about unknowns, since they are
			// essentially compatible (on forename) with everything.
			// OTOH, is it really worth collapsing? Maybe only if qualified...
			ArrayList<Person> docPersonList = 
				getPersonListName(personListMapForDoc, forenameId);
			docPersonList.add(father);
			ArrayList<Person> globalPersonList = 
				getPersonListName(personListsByName, forenameId);
			globalPersonList.add(father);
			// Now, we'll remap the displayName of the person to something more sensible.
			{
				String forename = (name==null)?"(unknown)":name.getName();
				String displayName = forename+
								"["+nradFather.getDocument().getAlt_id()+"."+docPersonList.size()+"]";
				father.setDisplayName(displayName);
			}
		}
		EntityLinkSet<NameRoleActivity> links = nradToEntityLinks.get(nradFather.getId());
		if(links==null) {
			links = new EntityLinkSet<NameRoleActivity>(nradFather, LinkType.Type.LINK_TO_PERSON);
			nradToEntityLinks.put(nradFather.getId(), links);
		}
		NRADEntityLink link = 
			new NRADEntityLink(nradFather, father, 1.0, 
										LinkType.Type.LINK_TO_PERSON);
		links.put(father, link);
		// Now add a link from the father Person back to the linkSet for when we
		// do weight shifting
		List<EntityLinkSet<NameRoleActivity>> linkSetsList =
									personToEntityLinkSets.get(father);
		if(linkSetsList==null) {
			linkSetsList = new ArrayList<EntityLinkSet<NameRoleActivity>>();
			personToEntityLinkSets.put(father, linkSetsList);
		}
		linkSetsList.add(links);
		return father;
	}
	
	private Clan addClanForNRAD(NameRoleActivity nrad,
			ArrayList<Clan> clanListForDoc) {
		Clan clan = findOrCreateClan(nrad);
		EntityLinkSet<NameRoleActivity> links = nradToEntityLinks.get(nrad.getId());
		if(links==null) {
			links = new EntityLinkSet<NameRoleActivity>(nrad, LinkType.Type.LINK_TO_CLAN);
			nradToEntityLinks.put(nrad.getId(), links);
		}
		NRADEntityLink link = 
			new NRADEntityLink(nrad, clan, 1.0, LinkType.Type.LINK_TO_CLAN);
		links.put(clan, link);

		if(!clanListForDoc.contains(clan))
			clanListForDoc.add(clan);
		return clan;
	}
	
	private Clan findOrCreateClan(NameRoleActivity nrad) {
		Name name = nrad.getName();
		if(name==null)
			throw new RuntimeException("Cannot find Clan for NRAD with no Name:"
											+nrad.getDisplayName());
		int clannameId = name.getId();	// get Name
		Clan clan = clansByName.get(clannameId);
		
		if(clan==null) {
			clan = new Clan(nrad);
			clansByName.put(clannameId, clan);
		}
		return clan;
	}
	
	/**
	 * Builds the list of all possible Persons and Clans cited in the corpus,
	 * and then runs the collapser to disambiguate among them. 
	 * @param sc
	 */
	public void rebuildEntitiesFromCorpus(ServiceContext sc) {
		createEntitiesFromCorpus(sc);
		collapseWithinDocuments();
		collapseAcrossDocuments();
		buildGraphLinks();
	}

	public void createEntitiesFromCorpus(ServiceContext sc) {
		// We iterate over the documents first, building persons for each
		// NRAD, and assembling the persons into lists by forename.
		// For each NRAD, we build a link to the new person, with all
		// its weight on that person
		
		clearEntityMaps();

		for(Document doc:corpus.getDocuments()) {
			long center = doc.getDate_norm();
			if(center==0) {
				center = getMedianDocumentDate();
			}
			// Get the non-family NRADs - we'll pick up family links from them
			List<NameRoleActivity> baseNRADs = doc.getNonFamilyNameRoleActivities();
			if(baseNRADs.size()>0) {
				// Ensure we have a map for this doc
				HashMap<Integer, ArrayList<Person>> personListMapForDoc = 
						getPersonListMapForDoc(doc.getId());
				ArrayList<Clan> clanListForDoc = 
								getClanListForDoc(doc.getId());
				for(NameRoleActivity nrad:baseNRADs) {
					Person person = addPersonForNRAD(nrad, center, personListMapForDoc);
					// Now we have to add persons for the Family-linked NRADs
					// Note that we chain fathers so this will pick up grandfathers.
					// TODO What about ancestors???
					NameRoleActivity fatherNRAD = nrad.getFather();
					while(fatherNRAD!=null) {
						Person father = addFatherForPerson(person, fatherNRAD,
								personListMapForDoc);
						fatherNRAD = fatherNRAD.getFather();
						person = father;
					}
					NameRoleActivity clanNRAD = nrad.getClan();
					if(clanNRAD!=null)
						addClanForNRAD(clanNRAD, clanListForDoc);
				}
			}
		}
	}
	
	// This will actually return a list of NRADEntityLinks
	public List<NRADEntityLink> getEntityLinksForDoc(int docId) {
		Document doc = corpus.getDocument(docId);
		if(doc==null) 
			throw new IllegalArgumentException("No document found for id: "+docId);
		List<NRADEntityLink> nrad2PLinkList = new ArrayList<NRADEntityLink>();
		List<NameRoleActivity> nrads = doc.getNameRoleActivities(true);
		for(NameRoleActivity nrad:nrads) {
			EntityLinkSet<NameRoleActivity> elset = 
				nradToEntityLinks.get(nrad.getId());
			if(elset!=null) {
				for(EntityLink<NameRoleActivity> link:elset.values()) {
					nrad2PLinkList.add((NRADEntityLink)link);
				}
			}
		}
		return nrad2PLinkList;
	}
	
	// Do we need this?
	// public List<NRADEntityLink> getEntityLinksForPerson(Person person) {
	// }
	
}
