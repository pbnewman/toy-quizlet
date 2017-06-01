package edu.uwm.cs552;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import edu.uwm.cs.util.XMLTokenType;
import edu.uwm.cs.util.XMLTokenizer;
import edu.uwm.cs.util.XMLWriter;

/**
 * A class for objects that be read or written using XML,
 * and which require observers on changes.
 */
public abstract class XMLObject extends Observable {

	/**
	 * Write an element out in XML format.
	 * @param xw writer to write to, must not be null
	 * @throws IOException if problems arise in writing.
	 */
	public void toXML(XMLWriter xw) throws IOException {
		xw.writeElementStart(getXMLelementName());
		writeAttributes(xw);
		writeContents(xw);
		xw.writeElementDone(getXMLelementName());
	}
	
	public String toXMLString() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int prefixLength = 0;
		try {
			XMLWriter xw = new XMLWriter(baos);
			xw.flush();
			prefixLength = baos.size();
			toXML(xw);
			xw.close();
		} catch (IOException e) {
			System.out.println("Shouldn't happen: not using actual I/O:");
			e.printStackTrace();
			return "<error occurred>";
		}		
		try {			
			return baos.toString("UTF-8").substring(prefixLength);
		} catch (UnsupportedEncodingException e) {
			return "<Error: UTF-8 not supported?>";
		}
	}
	
	/**
	 * Return the element name to use for this object.
	 * @return element name (must not be null).
	 */
	protected abstract String getXMLelementName ();
	
	/**
	 * Write out any XML attributes for this object.
	 * @param xw writer to use (must not be null)
	 */
	protected void writeAttributes(XMLWriter xw) throws IOException {}
	
	/**
	 * Write out other elements/CDATA in this object.
	 * @param xw writer to use (must not be null)
	 */
	protected void writeContents(XMLWriter xw) throws IOException {}
	
	/**
	 * Read an XML element from the tokenizer.
	 * @param xt tokenizer to use
	 * @return element read from the input stream
	 * @throws ParseException if a problem is found.
	 */
	public static XMLObject fromXML(XMLTokenizer xt) throws ParseException {
		switch (xt.next()) {
		case OPEN:
			XMLObject result;
			String name = xt.getCurrentName();
			result = create(name);
			result.readXML(xt);
			return result;
		default:
			throw new ParseException("malformed XML: expected <OPEN> tag, but got " + xt);		
		}
	}

	/**
	 * Finish reading an XML element after the initial open tag.
	 * @param xt tokenizer to use, must not be null, must have just read an &lt;OPEN tag.
	 * @throws ParseException if a parse error is encountered
	 */
	protected void readXML(XMLTokenizer xt) throws ParseException {
		String name = xt.getCurrentName();
		while (xt.next() == XMLTokenType.ATTR) {
			addAttribute(xt.getCurrentName(),xt.getCurrentText());
		}
		if (xt.current() != XMLTokenType.ECLOSE) {
			while (true) {
				switch (xt.next()) {
				case OPEN:
					xt.saveToken();
					addElement(fromXML(xt));
					break;
				case TEXT:
					addText(xt.getCurrentText());
					break;
				case ETAG:
					if (!name.equals(xt.getCurrentName())) {
						throw new ParseException("expected </" + name + ">, not " + xt);
					}
					return;
				default:
					throw new ParseException("malformed XML: expected element or CDATA, got " + xt);
				}
			}
		}
	}
	
	protected void addAttribute(String name, String text) throws ParseException {
		throw new ParseException("unexpected attribute: " + name);
	}
	
	protected void addElement(XMLObject obj) throws ParseException {
		throw new ParseException("unexpected nested element: " + obj);
	}
	
	protected void addText(String text) throws ParseException {
		throw new ParseException("unexpected text " + text);
	}
	
	public static class ParseException extends Exception {
		/**
		 * KEH
		 */
		private static final long serialVersionUID = 1L;

		public ParseException(String s) {
			super(s);
		}
	}
	
	/**
	 * Inform observers that a change has happened to this class.
	 * @param description description of change
	 */
	protected void noteChange(String description) {
		super.setChanged();
		super.notifyObservers(description);
	}
	
	public static interface Factory {
		/**
		 * Create a fresh instance of an XML Object class.
		 * @return
		 */
		public XMLObject create();
	}
	
	private static Map<String,Factory> factories = new HashMap<>();
	
	/**
	 * Make available a factory for XML elements of the given name.
	 * @param name element name, must not be null.
	 * @param f factory, if null, this removes a factory.
	 */
	public static void register(String name, Factory f) {
		if (f == null) factories.remove(name);
		else factories.put(name,f);
	}

	/**
	 * Using the factory, create an instance of an XML element given its name.
	 * @param name element name, must not be null
	 * @return new XML object.
	 */
	public static XMLObject create(String name) {
		Factory f= factories.get(name);
		if (f == null) throw new IllegalArgumentException("No registered factory for this class");
		final XMLObject result = f.create();
		assert (result.getXMLelementName().equals(name)); // sanity check
		return result;
	}
}
