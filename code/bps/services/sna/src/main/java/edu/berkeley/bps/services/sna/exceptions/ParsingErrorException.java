/* Social Network Analysis Module
 * Exceptions
 * 
 * Written by Davide Semenzin 
 * 
 * */

package edu.berkeley.bps.services.sna.exceptions;

import java.lang.Exception;

public class ParsingErrorException extends Exception{
		public ParsingErrorException (String message){
			super("Parsing error:" + message);
		}
}
