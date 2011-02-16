package edu.berkeley.bps.services.common;

import java.sql.Connection;
import javax.servlet.ServletContext;

/** 
 * Base resource class that supports common behaviors or attributes shared by 
 * all resources. Currently empty, but will centralize info on DB connections. 
 *  
 */  
public abstract class BaseResource {  

	protected Connection getConnection(ServletContext srvc) {
		return getConnection(srvc, false);
	}
	
	protected Connection getConnection(ServletContext srvc, boolean fOnlyIfAvailable) {
		ServiceContext sc = (ServiceContext)srvc.getAttribute(ServiceContext.label);
		if(sc==null) {
			throw new RuntimeException("BaseResource.getConnection: cannot get ServiceContext!");
		}
		return sc.getConnection(fOnlyIfAvailable);
	}

	protected ServiceContext getServiceContext(ServletContext srvc) {
		return (ServiceContext)srvc.getAttribute(ServiceContext.label);
	}
}
