package edu.uwm.cs552.gui;

import java.awt.FontMetrics;

/**
 * A component that displays text to fit within a given
 * width by breaking lines and increasing the height.
 */
public class TextFlowComponent extends MyTextArea implements FlowComponent {

	/**
	 * KEH
	 */
	private static final long serialVersionUID = 1L;
	
	private String text;
		
	public TextFlowComponent() {
		super();
		super.setEditable(false);
		super.setLineWrap(true);
		super.setWrapStyleWord(true);
	}
	
	public TextFlowComponent(String s) {
		this();
		setText(s);
	}
	
	@Override
	public void setText(String s) {
		text = s;
		super.setText(s);
	}
	
	@Override
	public String getSelectedContent() {
		try {
			return getText();
		} catch (NullPointerException e) {
			return "";
		}
	}
	
	/**
	 * Get the height that this component will need to display the
	 * text given the font metrics and the size.
	 * @param fm
	 * @param width
	 * @return preferred height, or -1 if the width is too small
	 */
	public int getPreferredHeight(FontMetrics fm, int width) {
		String[] lines = linebreak(text,fm,width);
		if (lines == null) return TOO_SMALL;
		if (lines.length == 0) return fm.getHeight();
		return fm.getHeight() * lines.length;		
	}
	
	/**
	 * Line break the text so that it can fit within the given width with the given
	 * font metrics.
	 * @param text text to line-break at spaces as needed.  Newline characters force a line break.
	 * @param fm font metrics to use, must not be null
	 * @param width available width to use
	 * @return array of lines, or null if one of the words has width greater than the
	 * 'available width.
	 */
	protected static String[] linebreak(String text, FontMetrics fm, int width) {
		text = text + "\n"; // sentinel
		StringBuilder result = new StringBuilder();
		int n = text.length();
		int lastSpace = -1;
		int startLine = 0;
		for (int i=0; i < n; ++i) {
			char ch = text.charAt(i);
			result.append(ch);
			switch (ch) {
			case ' ': lastSpace = i; break;
			// fall through
			default:
				if (startLine < i && lastSpace >= startLine &&
				    fm.stringWidth(result.substring(startLine)) > width) {
					// convert last space into a NL
					result.setCharAt(lastSpace, '\n');
					int sw = fm.stringWidth(result.substring(startLine,lastSpace));
					while (sw > width) {
						return null;
					}
					startLine = lastSpace+1;
				}
				break;
			case '\n':
				if (i > startLine) {
					int sw = fm.stringWidth(result.substring(startLine,i));
					if (sw > width) return null;
				}
				startLine = i+1;
				break;
			}
		}
		return result.toString().split("\n");
	}

	public void dispose() {}
}
