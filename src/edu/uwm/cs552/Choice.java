package edu.uwm.cs552;

import java.io.IOException;

import edu.uwm.cs.util.RomanNumeral;
import edu.uwm.cs.util.XMLWriter;

public class Choice extends XMLObject {
	public static final Style DEFAULT_STYLE = Style.DOT_ARABIC;
	
	public static enum Render { 
		ARABIC { @Override public String toString(int index) { return ""+index; } }, 
		ROMAN { @Override public String toString(int index) { return RomanNumeral.toString(index).toLowerCase(); } },
		ALPHA { 
			@Override public String toString(int index) { 
				int n = (index-1)/26;
				char l = (char) ('a' + ((index-1)%26));
				StringBuilder sb = new StringBuilder();
				for (int i=0; i <= n; ++i) {
					sb.append(l);
				}
				return sb.toString();
			}
		};

		public abstract String toString(int index);
	}
	
	public static enum Format { 
		PAREN { @Override public String format(String s) { return "(" + s + ")"; } }, 
		DOTTED { @Override public String format(String s) { return s + "."; } };
		
		public abstract String format(String l);
	}
	
	public static enum Style {
		PAR_arabic(Render.ARABIC,false,Format.PAREN),
		DOT_arabic(Render.ARABIC,false,Format.DOTTED),
		PAR_ARABIC(Render.ARABIC,true,Format.PAREN),
		DOT_ARABIC(Render.ARABIC,true,Format.DOTTED),
		PAR_roman(Render.ROMAN,false,Format.PAREN),
		DOT_roman(Render.ROMAN,false,Format.DOTTED),
		PAR_ROMAN(Render.ROMAN,true,Format.PAREN),
		DOT_ROMAN(Render.ROMAN,true,Format.DOTTED),
		PAR_alpha(Render.ALPHA,false,Format.PAREN),
		DOT_alpha(Render.ALPHA,false,Format.DOTTED),
		PAR_ALPHA(Render.ALPHA,true,Format.PAREN),
		DOT_ALPHA(Render.ALPHA,true,Format.DOTTED);
		
		private final Render render;
		private final boolean uppercase;
		private final Format format;
		
		private Style(Render r, boolean u, Format f) {
			render = r;
			uppercase = u;
			format = f;
		}
		
		public String toString(int index) {
			String s = render.toString(index);
			if (uppercase) s = s.toUpperCase();
			return format.format(s);
		}
	}
	
	// #(
	private String text;
	private int index;
	private Style style;

	public Choice() {
		text = "";
		index = 0;
		style = DEFAULT_STYLE;
	}

	public String getText() {
		return text;
	}
	public int getIndex() {
		return index;
	}
	public Style getStyle() {
		return style;
	}
	
	public int length() {
		return text.length();
	}
	
	public String toString() {
		return style.toString(index) + " " + text;
	}
	
	public void setText(String t){
		if (t == null) throw new IllegalArgumentException("Cannot set text to null");
		if (t.equals(text)) return; // nothing to do
		text = t;
		super.noteChange("text");
	}
	
	void setIndex(int i) {
		if (index == i) return; // nothing to do
		index = i;
		super.noteChange("index");
	}
	
	void setStyle(Style s) {
		if (s == null) throw new IllegalArgumentException("cannot set style to null");
		if (s.equals(style)) return; // nothing to do
		style = s;
		super.noteChange("style");
	}
	
	// XML I/O

	@Override
	protected String getXMLelementName() {
		return "Choice";
	}

	@Override
	protected void writeContents(XMLWriter xw) throws IOException {
		xw.writeCDATA(text);
	}

	@Override
	protected void addText(String text) throws ParseException {
		setText(getText()+text);
	}
	// #)
}
