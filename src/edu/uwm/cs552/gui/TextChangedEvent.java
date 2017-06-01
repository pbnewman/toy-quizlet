package edu.uwm.cs552.gui;

import javax.swing.JComponent;

/**
 * An event that indicates that the text changed.
 */
public class TextChangedEvent {
	private final JComponent component;
	private final String before, after;
	public TextChangedEvent(JComponent c, String b, String a) {
		component = c;
		before = b;
		after = a;
	}
	
	/**
	 * Get the component whose text has changed.
	 * @return component with changed text.
	 */
	public JComponent getComponent() {
		return component;
	}
	
	/** Get text before the change.
	 * @return previous text
	 */
	public String getBeforeText() {
		return before;
	}
	
	/**
	 * Get text after the change.
	 * @return current text
	 */
	public String getAfterText() {
		return after;
	}
	
	@Override
	public String toString() {
		return "[component=" + component + ", before=" + before + ", after = " + after + "]";
	}
}