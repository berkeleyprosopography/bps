package edu.berkeley.bps.services.common.sax;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import edu.berkeley.bps.services.common.utils.StringStack;

public class StackedContentHandler extends DefaultHandler implements
		ContentHandler {
	protected StringBuffer accumulator;		// Accumulate text
	protected StringStack elPath;				// Keep track of path in this context
	protected XMLReader parser;
	protected ContentHandler previous;
	protected Locator locator = null;

	public StackedContentHandler(XMLReader parser, ContentHandler previous) {
		super();
		accumulator = new StringBuffer();
		elPath = new StringStack();
		this.parser = parser;
		this.previous = previous;
	}

	protected void pop() {
		elPath.clear();
		parser.setContentHandler(previous);
	}

	public void characters(char[] chars, int start, int length) {
		accumulator.append(chars, start, length);
	}
	
	/**
	 * @return accumulated text if within an element. Will generally be empty otherwise
	 */
	public String getCurrentText() {
		return accumulator.toString();
	}

	public boolean pathMatches(String[] path) {
		return elPath.matches(path);
	}
	
	public XMLReader getParser() {
		return parser;
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, 
			Attributes attrList) {
		accumulator.setLength(0);
		elPath.push(localName);
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) {
		String popped = elPath.pop();
		if(!localName.equalsIgnoreCase(popped)) {
			String err = "StackedContentHandler.endElement name does not match stack!"
				+"\nExpecting: "+localName+" popped: "+popped
				+" at: "+generateErrorContext();
			throw new RuntimeException(err);
		}
		if(elPath.isEmpty()) {
			pop();
		}
	}

	@Override
	public void endDocument() {
		throw new RuntimeException("StackedContentHandler.endDocument reached without popping stack!");
	}
	
	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}
	
	protected String generateErrorContext() {
		if(locator!=null) {
			return "Line["+locator.getLineNumber()+"] Col["+locator.getColumnNumber()+"]";
		} else {
			return "(No location info)";
		}
	}
}
