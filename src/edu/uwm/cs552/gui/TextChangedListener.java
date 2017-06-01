package edu.uwm.cs552.gui;

/**
 * A listener that is informed when the user is done with the text area
 * after making changes.
 */
public interface TextChangedListener {
	/**
	 * Indicate that text changed for the component.
	 * @param e event structure with information on the component
	 * and the before and after text.
	 */
	public void textChanged(TextChangedEvent e);
}