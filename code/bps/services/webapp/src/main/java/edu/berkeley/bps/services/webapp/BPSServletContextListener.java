package edu.berkeley.bps.services.webapp;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;

import edu.berkeley.bps.services.common.ServiceContext;
import edu.berkeley.bps.services.corpus.Corpus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BPSServletContextListener
     implements ServletContextListener {
	static final Logger logger = LoggerFactory.getLogger(BPSServletContextListener.class);

  public void contextDestroyed(ServletContextEvent sce) {
    System.out.println("BPS Web app was removed.");
  }

  public void contextInitialized(ServletContextEvent sce) {
    System.out.println("BPS Web app initialized, getting params.");
		ServletContext context = sce.getServletContext();
		if(context == null)
			logger.error("BPS Web app cannot get servlet context!");
		else {
			String dburl = context.getInitParameter("bps.db.dburl");
			logger.debug("BPS DBURL: {}", dburl);
			ServiceContext sc = new ServiceContext(dburl);
			context.setAttribute(ServiceContext.label, sc);
			Corpus.initMaps(sc);
			// Ensure we are using Saxon for XSLT
			System.setProperty("javax.xml.transform.TransformerFactory",
								"net.sf.saxon.TransformerFactoryImpl");
		}
  }

}

