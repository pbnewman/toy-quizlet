package edu.uwm.cs552.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.uwm.cs.util.XMLWriter;
import edu.uwm.cs552.Response;
import edu.uwm.cs552.XMLObject;
/**
 * The top-level XML element sent from the client to the server. It has a “user”
 * attribute giving the user name of the client. It has Response elements as its nested elements.
 */
public class UserResponses extends XMLObject {

	private String user = "guest";
	private List<Response> responses = new ArrayList<>();
	
	@Override
	protected String getXMLelementName() {
		return "UserResponses";
	}
	
	@Override
	protected void addAttribute(String name, String text) throws ParseException {
		if (name.equals("user") && text != null) 
			user = text; 
		else
			super.addAttribute(name, text); 
	}

	@Override
	protected void addElement(XMLObject obj) throws ParseException {
		if (obj instanceof Response) {
			responses.add((Response) obj);
		} else { super.addElement(obj); }
	}

	@Override
	protected void writeAttributes(XMLWriter xw) throws IOException {
		super.writeAttributes(xw);
		xw.writeAttr("user", user);
	}

	@Override
	protected void writeContents(XMLWriter xw) throws IOException {
		super.writeContents(xw);
		for (Response r : responses) {
			r.toXML(xw);
		}
	}
}
