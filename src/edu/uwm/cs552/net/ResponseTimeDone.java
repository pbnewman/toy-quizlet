package edu.uwm.cs552.net;

import edu.uwm.cs552.XMLObject;
/**
 * This element signifies that the most recent 
 * question is no longer accepting responses.
 */
public class ResponseTimeDone extends XMLObject {

	@Override
	protected String getXMLelementName() {
		return "ResponseTimeDone";
	}
}
