package edu.berkeley.bps.services.webapp;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;

import edu.berkeley.bps.services.common.ServiceContext;

public class BPSServletContextListener
     implements ServletContextListener {

  public void contextDestroyed(ServletContextEvent sce) {
    System.out.println("BPS Web app was removed.");
  }

  public void contextInitialized(ServletContextEvent sce) {
    System.out.println("BPS Web app initialized, getting params.");
		ServletContext context = sce.getServletContext();
		if(context == null)
			System.out.println("BPS Web app cannot get servlet context!");
		else {
			String dburl = context.getInitParameter("bps.db.dburl");
			System.out.println("BPS DBURL: "+dburl);
			ServiceContext sc = new ServiceContext(dburl);
			context.setAttribute(ServiceContext.label, sc);
		}
  }

}

