package edu.berkeley.bps.services.common;

import javax.servlet.ServletContext;

/** 
 * Base resource class that supports common behaviors or attributes shared by 
 * all resources. Currently empty, but will centralize info on DB connections. 
 *  
 */  
public abstract class BaseResource {  

	protected ServiceContext getServiceContext(ServletContext srvc) {
		return (ServiceContext)srvc.getAttribute(ServiceContext.label);
	}
}
