package edu.berkeley.bps.services.common;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name="version")
public class Version {
	
    final Logger logger = LoggerFactory.getLogger(Version.class);
	private String services;

	private static Version singleton = null;

	public static Version getSingleton() {
		if(singleton==null) {
			singleton = new Version();
			singleton.services = "0.1";
		}
		return singleton;
	}
	
	@XmlElement
	public String getServices() {
		logger.trace("BPS Version called. Version is: {}", services);
		return services;
	}


	
}

