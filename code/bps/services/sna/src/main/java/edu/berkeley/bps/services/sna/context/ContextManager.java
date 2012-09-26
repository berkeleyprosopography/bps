/* Social Network Analysis Module
 * Context data type handler
 * 
 * Written by Davide Semenzin 
 * 
 * */

package edu.berkeley.bps.services.sna.context;

import edu.berkeley.bps.services.sna.ioutils.IO;
import edu.berkeley.bps.services.sna.context.GraphContext;
import edu.berkeley.bps.services.sna.graph.GraphWrapper;



public class ContextManager {
	
	public static GraphContext getContext(String arg) throws Exception{
		
		IO g = new IO();
		//if POST
		if (arg!=null){
			//preprocessing is done here
			//submits the extracted GraphMl to the graphML processor
			GraphWrapper retGraph = new GraphWrapper(g.parse(arg));
			GraphContext ret = new GraphContext(retGraph);
			return ret;
		}
		//temporary
		else{
			System.out.print("no input provided");
			return null;
		}
		
	}
	
	
}