package edu.berkeley.bps.services.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SystemProperties {
	
	public static final String CORPUS_DIR = "corpus_dir";
	
	protected static SystemProperties instance = null;
	
	protected static final String PROPS_FILENAME = "bps.properties";
	
	protected Properties props = null;
	
	SystemProperties() {
		props = new Properties();
		InputStream in = getClass().getClassLoader().getResourceAsStream(PROPS_FILENAME);
		try {
			props.load(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Error loading properties file: "+PROPS_FILENAME+ " :"+e.getLocalizedMessage());
		}
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Error closing file: "+PROPS_FILENAME+ " :"+e.getLocalizedMessage());
		}
	}
	
	protected static void CheckInit() {
		if(instance==null) {
			instance = new SystemProperties();
		}
	}
	
	public static String getProperty(String key) {
		CheckInit();
		return instance.props.getProperty(key);
	}

	
}
