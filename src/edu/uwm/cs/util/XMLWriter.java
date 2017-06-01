package edu.uwm.cs.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * A class that makes it easier to write lexically compliant XML.
 * NB: The class is not quite XML compliant in that it inserts extra
 * spaces (indentation) and newlines to make the result more readable.
 * This extra whitespace is removed by {@link XMLTokenizer}.
 * @author John Boyland <boyland@uwm.edu>
 */
public class XMLWriter extends Writer {

	private final Writer underlying;
	
	/**
	 * Create a new XML output writer.  It needs to take an output
	 * stream because it must specify the character encoding (UTF-8).
	 * @param out stream to which to write, must not be null
	 * @throws IOException if problem from writing the XML header results
	 */
	public XMLWriter(OutputStream out) throws IOException {
		underlying = new OutputStreamWriter(out,"UTF-8");
		writeInternal("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	}
	
	/**
	 * Write the given string directly to the stream, without interpretation.
	 * @param s String to write, must not be null
	 * @throws IOException if an error results
	 */
	protected void writeInternal(String s) throws IOException {
		underlying.write(s,0,s.length());
	}
	
	/**
	 * Write the given character directly to the stream, without interpretation.
	 * @param ch a character to write
	 * @throws IOException if an error results
	 */
	protected void writeInternal(char ch) throws IOException {
		underlying.write(ch);
	}
	
	/**
	 * Write text to the stream while quoting uses of &amp; &lt; or &quot;
	 * if they occur.
	 * @param buf characters to write stored here
	 * @param b starting at this index
	 * @param l number of characters to write
	 * @param quoteSpace if spaces at the beginning and end should be quoted
	 * @throws IOException if problem results from writing.
	 */
	protected void writeQuoted(char[] buf, int b, int l, boolean quoteSpace) throws IOException {
		int fence = b+l, last = b;
		for (int i=b; i < fence; ++i) {
			String special = null;
			switch (buf[i]) {
			case '&': special = "&amp;"; break;
			case '<': special = "&lt;"; break;
			case '"': special = "&quot;"; break;
			case '\n': special = "&Newline;"; break;
			case ' ': 
				if (quoteSpace && i == b || i == fence-1) {
					special = "&sp;";
					break;
				}
				/* FALL THROUGH */
			default:
				break;
			}
			if (special != null) {
				if (i > last) underlying.write(buf,last,i-last);
				last = i+1;
				underlying.write(special);
			}
		}
		if (fence > last) underlying.write(buf,last,fence-last);
	}
	
	/**
	 * Check that the argument is a legal XML name.
	 * @see <a href="https://www.w3.org/TR/xml11/#NT-Names">W3's definition</a> of &quot;Name&quot;
	 * @param name, XML name (must not be null)
	 * @throws IllegalArgumentException if name doesn't match the requirement.
	 */
	protected void checkXMLname(String name) throws IllegalArgumentException {
		int n = name.length();
		for (int i=0; i < n; ++i) {
			char ch = name.charAt(i);
			//XXX: for simplicity, we are somewhat stricter that W3
			if (Character.isAlphabetic(ch)) continue;
			if (ch == '_' || ch == ':') continue;
			if (i > 0) {
				if (Character.isDigit(ch)) continue;
				if (ch == '.' || ch == '-' || ch == '\u00b7') continue;
			}
			throw new IllegalArgumentException("XML names probably cannot include '" + ch + "'");
		}
	}
	
	private int _nesting;
	
	private static final String indents = "                                        ";
	private static final int INDENTSPACES = 2;
	private static final int MAXINDENT = indents.length() / INDENTSPACES;
	
	/**
	 * Write two spaces for every level of nesting current.
	 * @throws IOException if an error results from writing the spaces.
	 */
	public void indent() throws IOException {
		if (_nesting > MAXINDENT) writeInternal(indents);
		else if (_nesting > 0) underlying.write(indents, 0, _nesting*2);
	}
	
	private boolean _inAttrs = false;
	
	/**
	 * Start writing a new element with the given name.
	 * The tag is preceded by appropriate indentation.
	 * @param name element name, must not be null
	 * @see #indent()
	 * @throws IOException if error results from writing text to the stream.
	 * @throws IllegalStateException if attempting to write two top-level elements
	 * to the stream. (XML files can have only one top-level entity.)
	 * @throws IllegalArgumentException if the name is not a legal XML name.
	 */
	public void writeElementStart(String name) throws IOException {
		checkXMLname(name);
		if (_nesting < 0) throw new IllegalStateException("XML files can have only a single element");
		if (_inAttrs) {
			writeInternal(">\n");
		}
		indent();
		writeInternal("<" + name);
		++_nesting;
		_inAttrs = true;
	}

	/**
	 * Write an XML attribute name="value" to the stream.
	 * @param name attribute name (must be a legal XML attribute name 
	 * or resulting XML is badly formed)
	 * @param value string with either no double quotes or no single quotes, must not be null.
	 * @throws IOException if a problem arises in writing
	 * @throws IllegalStateException if we haven't just started an element
	 * @throws IllegalArgumentException if the name is not a legal XML name.
	 */
	public void writeAttr(String name, String value) throws IOException {
		checkXMLname(name);
		if (!_inAttrs) throw new IllegalStateException("not writing attributes currently");
		writeInternal(' ');
		writeInternal(name);
		writeInternal('=');
		writeInternal('"');
		writeQuoted(value.toCharArray(),0,value.length(),false);
		writeInternal('"');
	}
	
	/**
	 * Write an arbitrary string to the file; any special characters (&lt; &amp; &quot;)
	 * are converted into entities.  For compatibility with space swallowing behavior
	 * of {@link XMLTokenizer} some spaces are also converted into (&amp;sp;) entities.
	 * NB: This method simply calls {@link write(String)} which does the work.
	 * @param string string to write, must not null
	 * @throws IOException if error arises while writing text
	 * @throws IllegalStateException if at top-level---CDATA is not allowed at the top level.
	 */
	public void writeCDATA(String string) throws IOException {
		write(string);
	}
	
	/**
	 * Write an XML comment to the output.
	 * @param s comment to write, must not be null (see also illegal argument)
	 * @throws IOException if a write error results
	 * @throws IllegalArgumentException if the string includes -- or ends with a hyphen.
	 */
	public void writeComment(String s) throws IOException {
		if (s.indexOf("--") != -1 || s.endsWith("-")) 
			throw new IllegalArgumentException("Comments may not include -- or end with -");
		if (_inAttrs) {
			writeInternal(">\n");
			_inAttrs = false;
		}
		indent();
		writeInternal("<!--" + s + "-->\n");
	}
	
	/**
	 * Complete an XML element either by ending the current tag with /&gt;, 
	 * or by writing an end tag.
	 * @param name name of element to end, must not be null.
	 *   We currently don't check that it matches the open tag.
	 * @throws IOException if an error results.
	 * @throws IllegalStateException if too many elements are closed.
	 */
	public void writeElementDone(String name) throws IOException {
		if (_nesting <= 0) throw new IllegalStateException("nothing to end");
		--_nesting;
		if (_inAttrs) {
			writeInternal("/>\n");
		} else {
			indent();
			writeInternal("</" + name + ">\n");
		}
		_inAttrs = false;
		if (_nesting == 0) _nesting = -1; // signify done.
	}
	
	@Override
	public void write(char[] buf, int b, int l) throws IOException {
		if (_nesting <= 0) throw new IllegalStateException("text must be nested in an element");
		if (_inAttrs) {
			writeInternal(">\n");
			_inAttrs = false;
		}
		indent();
		writeQuoted(buf,b,l,true);
		writeInternal('\n');
	}

	@Override
	public void flush() throws IOException {
		underlying.flush();
	}

	@Override
	public void close() throws IOException {
		underlying.close();
		// permit emergency close, don't:
		// if (_nesting < 0) throw new IllegalStateException("Closed when in the middle of writing.");
	}
	
}
