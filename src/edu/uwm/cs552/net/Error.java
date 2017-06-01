package edu.uwm.cs552.net;

import java.io.IOException;

import edu.uwm.cs.util.XMLWriter;
import edu.uwm.cs552.XMLObject;
/**
 * Information about a problem sent to a client.
 */
public class Error extends XMLObject {

	private String errorText = "";
	
	public Error() { };
	
	public Error(String txt) {
		errorText = txt;
	}
	
	@Override
	protected String getXMLelementName() {
		return "Error";
	}

	@Override
	protected void writeContents(XMLWriter xw) throws IOException {
		super.writeContents(xw);
		xw.writeCDATA(errorText);
	}

	@Override
	protected void addText(String text) throws ParseException {
		errorText = text;
	}
	
	public String getError() {
		return errorText;
	}
}
