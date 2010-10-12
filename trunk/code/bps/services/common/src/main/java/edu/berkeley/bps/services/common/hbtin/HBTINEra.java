package edu.berkeley.bps.services.common.hbtin;

import edu.berkeley.bps.services.common.time.Era;

import java.util.Date;
import java.util.HashMap;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.GregorianCalendar;

public class HBTINEra implements Era {

	private static String eras[][] = {
		{"-100", "-50", "Antiochus", "Antiochus2"  },
		{"-150", "-100", "Antiochus.Seleucus",  },
		{"-200", "-150", "Seleucus",  },
		{"-250", "-200", "Seleucus.Antiochus",  }
	};

	private static HashMap<String, HBTINEra> eraStore = null;
	private String name;
	private long start;			// Start in millis from common Date origin
	private long end;			// Start in millis from common Date origin

	private HBTINEra(String eraName, long eraStart, long eraEnd ) {
		name = eraName;
		start = eraStart;
		end = eraEnd;
	}

	public static Era getEra(String eraName) {
		checkInit();
		return eraStore.get(eraName);
	}

	@Override
	public Date getDate(int year, int doy) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getDate(int year, int month, int day)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getDate(int year, String month, int day)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getEndDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getStartDate() {
		// TODO Auto-generated method stub
		return null;
	}

	private static void checkInit() {
		if(eraStore==null) {
			eraStore = new HashMap<String, HBTINEra>();
			for(String strings[]:eras) {
				try {
					addEraToStore(strings[0], strings[1], strings[2]);
				} catch(ParseException pe) {
					throw new RuntimeException("Problem initializing era store", pe);
				}
				for(int iSyn=3; iSyn < strings.length; iSyn++) {
					addAltNameForEra(strings[0], strings[iSyn]);
				}
			}
		}
	}

	private static void addEraToStore(String name, String startDate, String endDate )
		throws ParseException {
	//Calendar calDate = GregorianCalendar.getInstance();
	DateFormat df = DateFormat.getInstance();
	try {
		java.util.Date date = df.parse(startDate);
		long startTime = date.getTime();
		date = df.parse(endDate);
		long endTime = date.getTime();
		eraStore.put(name, new HBTINEra(name, startTime, endTime));
	} catch(ParseException pe) {
		// TODO Need to add logger and output something here
		throw pe;
	}
}

	private static void addAltNameForEra(String name, String altName ){
		HBTINEra era = eraStore.get(name);
		if(era==null){
			throw new RuntimeException("addAltNameForEra: no era found for primary name: "
					+name+". AltName: "+altName);
		} else {
			eraStore.put(altName, era);
		}
	}

	private static void testNotNull(String label, Object toTest) {

	}

	public static void main(String[] args) {
		getEra("Antiochus");
	}
}
