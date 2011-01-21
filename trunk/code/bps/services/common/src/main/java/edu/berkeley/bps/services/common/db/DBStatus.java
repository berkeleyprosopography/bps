package edu.berkeley.bps.services.common.db;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dbstatus")
public class DBStatus {
	
	private String available;
	private String reason;

	public DBStatus() {
		available = "Unknown";
		this.reason = null;
	}

	public DBStatus(boolean avail, String reason) {
		available = avail?"Available":"NOT Available";
		this.reason = reason;
	}

	public static DBStatus createAvailableStatus() {
		return new DBStatus(true, null);
	}
	
	public static DBStatus createUnavailableStatus(String reason) {
		return new DBStatus(false, reason);
	}
	
	@XmlElement
	public String getAvailable() {
		return available;
	}

	@XmlElement
	public String getReason() {
		return reason;
	}


	
}

