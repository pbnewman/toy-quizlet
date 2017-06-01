package edu.uwm.cs552.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.uwm.cs.util.XMLWriter;
import edu.uwm.cs552.Question;
import edu.uwm.cs552.XMLObject;
/**
 * Top-level XML element sent from the server to the client. 
 * It has Question and ResponseTimeDone nested elements.
 */
public class Requests extends XMLObject {

	private List<XMLObject> elements = new ArrayList<>();
	
	@Override
	protected String getXMLelementName() {
		return "Requests";
	}

	@Override
	protected void writeContents(XMLWriter xw) throws IOException {
		super.writeContents(xw);
		for (XMLObject x : elements) {
			x.toXML(xw);
		}
	}

	@Override
	protected void addElement(XMLObject obj) throws ParseException {
		if (obj instanceof Question || obj instanceof ResponseTimeDone)
			elements.add(obj);
		else
			super.addElement(obj);
	}
	
	public int size() {
		return elements.size();
	}
	
	public XMLObject get(int index) {
		return elements.get(index);
	}
}
