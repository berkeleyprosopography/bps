package edu.berkeley.bps.services.corpus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

import edu.berkeley.bps.services.common.ServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlAccessorType(XmlAccessType.NONE)
public class CachedEntity {
	final static Logger logger = LoggerFactory.getLogger(CachedEntity.class);
			
	public static final int UNSET_ID_VALUE = -1;
	public static final boolean SHALLOW_PERSIST = true;
	public static final boolean DEEP_PERSIST = false;
	
	@XmlElement
	protected int id = -1;
	@XmlElement
	protected String name = null;

	protected static final String NAME_MAP_SUFFIX = ".NameMap";
	protected static final String ID_MAP_SUFFIX = ".IdMap";
	private static String myClass = "CachedEntity";
	
	public CachedEntity() {
		super();
	}

	public static boolean Exists(Connection dbConn, String tablename, int id) {
		boolean exists = false;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(getExistsSelectString(tablename));
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				if(rs.getString("id")!=null)
					exists = true;
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			// Just absorb it
			String tmp = myClass+".Exists: Problem querying DB.\n"+ se.getMessage();
			logger.debug(tmp);
		}
		return exists;
	}

	public static boolean NameUsed(Connection dbConn, String tablename, String name) {
		boolean exists = false;
		try {
			PreparedStatement stmt = dbConn.prepareStatement(getNameUsedSelectString(tablename));
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				if(rs.getInt("id")>0)
					exists = true;
			}
			rs.close();
			stmt.close();
		} catch(SQLException se) {
			// Just absorb it
			String tmp = myClass+".NameUsed: Problem querying DB.\n"+ se.getMessage();
			logger.debug(tmp);
		}
	
		return exists;
	}
	
	protected static Map<String, Object> getNameMap(ServiceContext sc, String entityName) {
		return (HashMap<String, Object>)sc.getProperty(entityName+NAME_MAP_SUFFIX);
	}
	
	protected static void setNameMap(ServiceContext sc, String entityName, Map<String, Object> map) {
		sc.setProperty(entityName+NAME_MAP_SUFFIX, map);
	}
	
	protected static Map<Integer, Object> getIdMap(ServiceContext sc, String entityName) {
		return (HashMap<Integer, Object>)sc.getProperty(entityName+ID_MAP_SUFFIX);
	}
	
	protected static void setIdMap(ServiceContext sc, String entityName, Map<Integer, Object> map) {
		sc.setProperty(entityName+ID_MAP_SUFFIX, map);
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

	protected static String getExistsSelectString(String tablename) {
		return "SELECT id FROM "+tablename+" WHERE id = ?";
	}

	protected static String getNameUsedSelectString(String tablename) {
		return "SELECT id FROM "+tablename+" WHERE name = ?";
	}

}