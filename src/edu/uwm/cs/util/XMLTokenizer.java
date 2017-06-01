package edu.uwm.cs.util;

import static edu.uwm.cs.util.XMLTokenType.ATTR;
import static edu.uwm.cs.util.XMLTokenType.CLOSE;
import static edu.uwm.cs.util.XMLTokenType.ECLOSE;
import static edu.uwm.cs.util.XMLTokenType.ERROR;
import static edu.uwm.cs.util.XMLTokenType.ETAG;
import static edu.uwm.cs.util.XMLTokenType.OPEN;
import static edu.uwm.cs.util.XMLTokenType.TEXT;
import static java.io.StreamTokenizer.TT_EOF;
import static java.io.StreamTokenizer.TT_WORD;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Class for parsing XML.  This class is not fully compliant, 
 * but is more compliant than most XML out there it seems!
 * Use at own risk.  
 * Permission is hereby granted to use/copy/modify this file in any way.
 * @author John Tang Boyland
 */
public class XMLTokenizer implements Iterator<XMLTokenType>, Iterable<XMLTokenType> {
	private StreamTokenizer input;
	private int lineOffset = 0;

	/**
	 * Create a tokenizer over the given stream of characters.
	 * @param r stream to use, must not be null or used after this call
	 */
	public XMLTokenizer(Reader r) {
		input = new StreamTokenizer(r);
		resetSyntax();
		state.startState();
	}
	
	private static Reader createUTF8Reader(InputStream is) {
		try {
			return new InputStreamReader(is,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// Impossible
			e.printStackTrace();
			return null;
		}
	}
	public XMLTokenizer(InputStream is) {
		this(createUTF8Reader(is));
	}

	protected void resetSyntax() {
		input.resetSyntax();
		input.wordChars(0, 0xFF);
		input.whitespaceChars(0x08, 0x0d);
		input.ordinaryChar('&');
		input.ordinaryChar('<');
		input.ordinaryChar('>');
		input.ordinaryChar('/');
		input.quoteChar('"');
		input.quoteChar('\'');
		input.slashSlashComments(false);
		input.slashStarComments(false);
	}

	protected void outerSyntax() {
		input.wordChars(0x08,0x0d);
		input.wordChars(' ',' ');
		input.wordChars('\'','\'');
		input.wordChars('"','"');
		input.wordChars('/', '/');
		input.wordChars(';',';');
		input.wordChars('=','=');
		input.wordChars('-','-');
		input.wordChars('>', '>');
	}
	protected void innerSyntax() {
		input.whitespaceChars(0x08, 0x0d);
		input.whitespaceChars(' ',' ');
		input.ordinaryChar('/');
		input.ordinaryChar('=');
		input.ordinaryChar('>');
		input.quoteChar('"');
		input.quoteChar('\'');
		input.wordChars(';',';');
	}
	private void entitySyntax() {
		input.ordinaryChar(' '); // handle frequent error: "& "
		input.ordinaryChar(';');
	}
	private void clearEntitySyntax() {
		input.wordChars(';',';');
		input.wordChars(' ',' ');
	}
	private void commentSyntax() {
		input.wordChars(0x00, 0xFF);
		input.whitespaceChars('\n', '\n');
		input.ordinaryChar('-');
		input.ordinaryChar('>');
	}
	private void cdataSyntax(boolean hasTag) {
		input.wordChars(0x00, 0xFF);
		input.whitespaceChars('\n', '\n');
		if (hasTag) {
			input.ordinaryChar('<');
			input.ordinaryChar('/');
		}
		input.ordinaryChar(']');
		input.ordinaryChar('>');
	}

	static Map<String,String> entityMap = new HashMap<String,String>();
	static {
		entityMap.put("amp", "&");
		entityMap.put("lt", "<");
		entityMap.put("gt", ">");
		entityMap.put("quot", "\"");
		entityMap.put("apos", "'");
		entityMap.put("sp", " ");
		entityMap.put("nbsp", " ");
		entityMap.put("NewLine", "\n");
	}

	static String getEntity(String name) {
		String result = entityMap.get(name);
		if (result == null && name.startsWith("#x")) {
			try {
				char ch = (char)Integer.parseInt(name.substring(2), 16);
				result = Character.toString(ch);
				entityMap.put(name, result);
			} catch (NumberFormatException ex) {
				// muffle
			}
		} else if (result == null && name.startsWith("#")) {
			try {
				char ch = (char)Integer.parseInt(name.substring(1));
				result = Character.toString(ch);
				entityMap.put(name, result);
			} catch (NumberFormatException ex) {
				// muffle
			}
		}
		if (result == null) result = "Unknown entity \"&" + name + ";\"";
		return result;
	}
	
	static String convert(String name) {
		StringBuilder result = new StringBuilder();
		int n = name.length();
		for (int i=0; i < n; ++i) { // changed in loop too
			char ch = name.charAt(i);
			if (ch == '&') {
				int j;
				for (j=i+1; j < n; ++j) {
					ch = name.charAt(j);
					if (ch == '#' || ch == '_' || ch == ':' || ch == '.' || ch == '-') continue;
					if (Character.isAlphabetic(ch) || Character.isDigit(ch)) continue;
					break;
				}
				result.append(getEntity(name.substring(i+1, j-i)));
				if (ch != ';') result.append("(Missing ';' after '&')");
				i = j-1; // about to be incremented
			} else result.append(ch);
		}
		return result.toString();
	}

	private XMLTokenType currentToken;
	private String currentName;
	private String currentText;
	private boolean saved = false;

	/**
	 * Returns the most recently returned token.
	 */
	public XMLTokenType current() { return currentToken; }

	/**
	 * Returns the tag name if the current token is OPEN or ETAG, 
	 * the attribute key if the current token is ATTR,
	 * or null otherwise.
	 */
	public String getCurrentName() { return currentName; }

	/**
	 * Returns the attribute value if the current token is ATTR,
	 * the text content as a string if the current token is TEXT, 
	 * the error message if the current token is ERROR,
	 * or null otherwise.
	 */
	public String getCurrentText() { return currentText; }

	/**
	 * Return the current line number.
	 * (This number is not always accurate.)
	 */
	public int getLineNumber() { return input.lineno() + lineOffset; }
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("XMLTokenizer(");
		sb.append(input);
		if (saved) sb.append("; next=");
		else sb.append("; current=");
		sb.append(currentToken);
		switch (currentToken) {
		case OPEN:
		case ETAG:
		case ATTR:
			sb.append(" " + currentName);
			break;
		case CLOSE:
		case ECLOSE:
		case ERROR:
		case TEXT:
			break;
		}
		switch (currentToken) {
		case OPEN:
		case ETAG:
		case CLOSE:
		case ECLOSE:
			break;
		case ATTR:
		case ERROR:
		case TEXT:
			sb.append(" " + currentText);
			break;
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Puts back the most-recently returned token. This ensures that the next
	 * time {@link #next()} is called, it will return the current token.
	 */	
	public void saveToken() {
		if (saved == true || currentToken == null) throw new IllegalStateException("cannot save");
		saved = true;
	}

	private interface State {
		abstract void startState();
		abstract XMLTokenType getNext();
	}

	private State outerState = new OuterState();
	private State innerState = new InnerState();
	private State cdataState = new CDataState();
	private State state = outerState;
	private State contentState = null;

	private final Map<String,State> cdataElements = new HashMap<String,State>();

	/**
	 * Elements with this name will have contents parsed as CDATA.
	 * This is the case with "script" in HTML.
	 * @param elementName name of element which has CDATA contents.  Must not be null.
	 */
	public void addCDATA(String elementName) {
		if (elementName == null) throw new NullPointerException("CDATA element name must not be null");
		cdataElements.put(elementName,new CDataState(elementName));
	}

	/**
	 * Return true if the given element name has been declared as a CDATA
	 * element: one not including normal XML contents, usually a script or style marker.
	 * @param elementName name of element to query.  Must not be null
	 * @return whether this elementName has been declared previous as a CDATA element.
	 */
	public boolean isCDATA(String elementName) {
		return cdataElements.containsKey(elementName);
	}
	
	public boolean hasNext() {
		if (!saved) {
			currentToken = state.getNext();
			saved = true;
		}
		return currentToken != null;
	}

	public XMLTokenType next() {
		if (!hasNext()) throw new NoSuchElementException("at end of stream");
		saved = false;
		return currentToken;
	}

	public void remove() {
		throw new UnsupportedOperationException("remove not supported");
	}

	public Iterator<XMLTokenType> iterator() {
		return this;
	}
	
	/**
   * @param s
   */
  protected void updateLineOffset(String s) {
    for (int i=0; i < s.length(); ++i) {
    	if (s.charAt(i) == '\n') ++lineOffset;
    }
  }

  private class OuterState implements State {

		public void startState() {
			outerSyntax();
		}

		public XMLTokenType getNext() {
			try {
				switch (input.nextToken()) {
				case TT_EOF: return null;
				case TT_WORD:
				{ 
					String s = input.sval;
					s = s.replaceAll("\n *", ""); // ignore newlines followed by indent.
					updateLineOffset(s);
					currentText = s;
				}
				return TEXT;
				default:
					currentText = "Unexpected " + (char)input.ttype;
					return ERROR;
				case '<':
					state = innerState;
					state.startState();
					switch (input.nextToken()) {
					default:
						currentText = "<" + (char)input.ttype + " illegal";
						return ERROR;
					case TT_EOF: 
						currentText = "< at EOF";
						return ERROR;
					case TT_WORD:
						if (input.sval.startsWith("!--")) {
							eatComment();
							state = outerState;
							state.startState();
							return getNext();
						} else if (input.sval.startsWith("![CDATA[")) {
							currentText = input.sval.substring(8);
							state = cdataState;
							state.startState();
							return TEXT;
						} else if (input.sval.equalsIgnoreCase("!DOCTYPE")) {
							eatDOCTYPE();
							state = outerState;
							state.startState();
							return getNext();
						} else if (input.sval.equalsIgnoreCase("![if")) {
						  eatIfComment();
						  state = outerState;
						  state.startState();
						  return getNext();
						} else if (input.sval.equals("?xml")) {
						  if (!eatXMLdeclaration()) {
						    currentText = "BAD <?xml?> tag";
						    return ERROR;
						  }
						  state = outerState;
						  state.startState();
						  return getNext();
						} else if (input.sval.startsWith("!")) {
							state = outerState;
							state.startState();
							currentText = "BAD TAG: <" + input.sval + ">";
							return ERROR;
						}
						currentName = input.sval;
						contentState = cdataElements.get(currentName);
						return OPEN;
					case '/':
						if (input.nextToken() != TT_WORD) {
							currentText = "Expected element name after '</'";
							return ERROR;
						}
						currentName = input.sval;
						if (input.nextToken() != '>') {
							currentText = "Expected > to close tag";
							return ERROR;
						}
						state = outerState;
						state.startState();
						return ETAG;
					}
				case '&':
					entitySyntax();
					if (input.nextToken() != TT_WORD) {
						if (input.ttype == ' ') {
							currentText = "& ";
							clearEntitySyntax();
							return TEXT;
						}
						currentText = "Expected word after &";
						clearEntitySyntax();
						return ERROR;
					}
					currentText = input.sval;
					if (input.nextToken() != ';') {
						currentText = "Expected ';' to end entity";
						clearEntitySyntax();
						return ERROR;
					}
					clearEntitySyntax();
					currentText = getEntity(currentText);
					return TEXT;
				}
			} catch (IOException e) {
				currentText = e.toString();
				return ERROR;
			}
		}

		private void eatDOCTYPE() {
			try {
				while (input.nextToken() != '>') {
					if (input.ttype == TT_EOF) return;
				}
			} catch (IOException e) {
				// MUFFLE
				return;
			}
		}
		
		private void eatComment() {
			commentSyntax();
			try {
				for (;;) {
					while (input.nextToken() != '-') {
						//System.out.println("discarding " + input.toString());
						if (input.ttype == TT_EOF) return;
					}
					if (input.nextToken() == '-' &&
							input.nextToken() == '>') {
						resetSyntax();
						outerSyntax();
						return;
					}
					//System.out.println("discarding " + input.toString());
				}
			} catch (IOException e) {
				// muffle warning
				return;
			}
		}
		
		private boolean eatXMLdeclaration() {
		  try {
        while (input.nextToken() != TT_WORD || !input.sval.equals("?")) {
          // skip
        }
        if (input.nextToken() != '>') {
          return false;
        }
      } catch (IOException e) {
        return false;
      }
		  return true;
		}
		private void eatIfComment() {
		  try {
		    for (;;) {
		      while (input.nextToken() != '<') {
		        if (input.ttype == TT_WORD) {
		          updateLineOffset(input.sval);
		        }
		      }
		      if (input.nextToken() == TT_WORD && input.sval.equals("![endif]")) {
		        if (input.nextToken() == '>') break;
		      }
		    }
		  } catch(IOException e) {
		    // muffle warning
		  }
		}
	}

	private class InnerState implements State {

		public XMLTokenType getNext() {
			try {
				switch (input.nextToken()) {
				case TT_EOF: 
					currentText = "EOF inside tag";
					return ERROR;
				case TT_WORD:
					currentName = input.sval;
					if (input.nextToken() != '=') {
						currentText= "Expected '='";
						return ERROR;
					}
					switch (input.nextToken()) {
					default:
						currentText = "Expected attribute value";
						return ERROR;
					case TT_WORD:
					case '"':
					case '\'':
						currentText = convert(input.sval);
						return ATTR;
					}
				case '>':
					state = contentState;
					if (state == null) state = outerState;
					contentState = null;
					state.startState();
					return CLOSE;
				case '/':
					state = outerState;
					state.startState();
					if (input.nextToken() != '>') {
						currentText = "Expected '>', got " + input.toString();
						return ERROR;
					}
					return ECLOSE;
				default:
					currentText = "Illegal " + (char)input.ttype + " inside of tag";
					return ERROR;
				}
			} catch (IOException e) {
				currentText = e.toString();
				return ERROR;
			}
		}

		public void startState() {
			innerSyntax();
		}

	}

	private class CDataState implements State {

		private final String tagName;

		public CDataState(String tag) {
			tagName = tag;
		}

		public CDataState() {
			this(null);
		}

		@Override
		public void startState() {
			cdataSyntax(tagName != null);
		}

		@Override
		public XMLTokenType getNext() {
			try {
				switch (input.nextToken()) {
				default:
					currentText = input.sval;
					return TEXT;
				case '>':
					currentText = ">";
					return TEXT;
				case '/':
					currentText = "/";
					return TEXT;
				case ']':
					if (input.nextToken() == ']') {
						int extra = 0;
						while (input.nextToken() == ']') {
							++extra;
						}
						if (input.ttype == '>') {
							state = outerState;
							state.startState();
							return state.getNext();
						}
						input.pushBack();
						currentText = "]]";
						for (int j=0; j < extra; ++j) {
							currentText += "]";
						}
						return TEXT;
					}
					input.pushBack();
					currentText = "]";
					return TEXT;
				case '<':
					if (input.nextToken() == '/') {
						state = innerState;
						state.startState();
						if (input.nextToken() != TT_WORD) {
							//XXX Not quite right:
							// lost whitespace
							state = this;
							state.startState();
							currentText = "</" + (char)input.ttype;
							return TEXT;
						}
						if (!input.sval.equals(tagName)) {
							// XXX Not quite right because lost whitespace
							state = this;
							state.startState();
							currentText = "</" + input.sval;
							return TEXT;
						}
						currentName = tagName;
						if (input.nextToken() != '>') {
							// XXX not sure this is an error
							currentText = "Expected > to close tag";
							return ERROR;
						}
						state = outerState;
						state.startState();
						return ETAG;
					}
					input.pushBack();
					currentText = "<";
					return TEXT;
				}
			} catch (IOException e) {
				currentText = e.toString();
				return ERROR;
			} 
		}
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			test_tokenizer(new InputStreamReader(System.in));
		} else {
			for (String arg : args) {
				try {
					System.out.println("\n *************** " + arg + "\n");
					test_tokenizer(new FileReader(arg));
				} catch (FileNotFoundException e) {
					System.out.println("Cannot read file: " + arg);
				}
			}
		}
	}

	private static void test_tokenizer(Reader r) {
		XMLTokenizer tokenizer = new XMLTokenizer(r);
		int i = 0;
		for (XMLTokenType t : tokenizer) {
		  if (++i > 1000) break;
		  System.out.print(tokenizer.getLineNumber() + ": ");
			switch (t) {
			case ERROR:
				System.out.println("ERROR: " + tokenizer.getCurrentText());
				return;
			case OPEN:
				System.out.println("START TAG: " + tokenizer.getCurrentName());
				break;
			case ATTR:
				System.out.println("  ATTR: " + tokenizer.getCurrentName() + " = " + tokenizer.getCurrentText());
				break;
			case CLOSE:
				System.out.println("START TAG ENDS");
				break;
			case ECLOSE:
				System.out.println("START TAG ENDS and END TAG");
				break;
			case ETAG:
				System.out.println("END TAG: " + tokenizer.getCurrentName());
				break;
			case TEXT:
				System.out.println("TEXT: '" + tokenizer.getCurrentText() + "'");
				break;
			default:
				break;
			}

		}
	}

}
