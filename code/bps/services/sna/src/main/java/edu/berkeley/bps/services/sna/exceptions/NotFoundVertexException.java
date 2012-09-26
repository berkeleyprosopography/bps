/* Social Network Analysis Module
 * Exceptions
 * 
 * Written by Davide Semenzin 
 * 
 * */

package edu.berkeley.bps.services.sna.exceptions;

import java.lang.Exception;

public class NotFoundVertexException extends Exception{
		public NotFoundVertexException (String message){
			super("Not found vertex error: " + message);
		}
}
