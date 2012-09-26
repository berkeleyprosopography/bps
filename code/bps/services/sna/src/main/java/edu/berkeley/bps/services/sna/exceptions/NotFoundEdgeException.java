/* Social Network Analysis Module
 * Exceptions
 * 
 * Written by Davide Semenzin 
 * 
 * */

package edu.berkeley.bps.services.sna.exceptions;

import java.lang.Exception;

public class NotFoundEdgeException extends Exception{
		public NotFoundEdgeException (String message){
			super("ERROR - Edge not found. \n" + message);
		}
}
