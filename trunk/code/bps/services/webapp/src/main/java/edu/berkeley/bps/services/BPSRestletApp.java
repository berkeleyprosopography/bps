package edu.berkeley.bps.services;

import edu.berkeley.bps.services.corpus.CorporaResource;
import edu.berkeley.bps.services.corpus.CorpusResource;
public class BPSRestletApp {   }
/*
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Router;
public class BPSRestletApp extends Application {  
  
	public BPSRestletApp() {
		super();
		System.out.println("BPSRestletApp Ctr called...");
	}
    /** 
     * Creates a root Restlet that will receive all incoming calls. 
     */  
    /*
		@Override  
    public Restlet createRoot() {  
        // Create a router Restlet that routes each call to a  
        // new instance of HelloWorldResource.  
        Router router = new Router(getContext());
  
        // TODO define a default handler
        //router.attachDefault(CorporaResource.class);  

        // Defines a route for the corpora (list) resource
        router.attach("/corpora", CorporaResource.class);  
        // Defines a route for a corpus resource
        router.attach("/corpora/{corpusid}", CorpusResource.class);  
  
        return router;  
    }  
}  
  */

