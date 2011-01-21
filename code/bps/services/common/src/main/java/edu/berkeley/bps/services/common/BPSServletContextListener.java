package edu.berkeley.bps.services.common;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;

public class BPSServletContextListener
     implements ServletContextListener {

	private static String dburl = null;

  public void contextDestroyed(ServletContextEvent sce) {
    System.out.println("BPS Web app was removed.");
  }

  public void contextInitialized(ServletContextEvent sce) {
    System.out.println("BPS Web app initialized, getting params.");
		ServletContext context = sce.getServletContext();
		if(context == null)
			System.out.println("BPS Web app cannot get servlet context!");
		else {
			dburl = context.getInitParameter("bps.db.dburl");
			System.out.println("BPS DBURL: "+dburl);
		}
  }

	public static String getDBUrl() {
		return dburl;
	}

}

