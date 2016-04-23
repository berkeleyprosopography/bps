package edu.berkeley.bps.services.common.utils;

import java.util.ArrayList;

/**
 * @author pschmitz
 *
 */
public class StringStack extends ArrayList<String> {
	public static final String WILDCARD = "*";
	public static final String ROOT_RELATIVE = ".";
	
	protected boolean allowRootRelative = true; 
	
	public boolean getAllowRootRelative() {
		return allowRootRelative;
	}

	public void setAllowRootRelative(boolean allowRootRelative) {
		this.allowRootRelative = allowRootRelative;
	}

	public void push(String s) {
		add(s);
	}

	public String pop() {
		if (isEmpty())
			throw new IndexOutOfBoundsException();
		return remove(size() - 1);
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public String peek() {
		return peek(1);
	}
	
	/**
	 * Get the indicated String looking from the top of the stack. 
	 * @param offset
	 * @return
	 */
	public String peek(int offset) {
		return get(size() - offset);
	}
	
	public boolean matches(String[] path) {
		if(path.length != size())
			return false;
		// compare end to front, since we expect paths to match
		// more at front, and want to fail fast
		for(int i=path.length-1; i>=0; i--) {
			if(!(path[i].equalsIgnoreCase(get(i))
				|| path[i].equals(WILDCARD)
				|| (allowRootRelative && (i==0) && path[i].equals(ROOT_RELATIVE))))
				return false;
		}
		return true;
	}
}
