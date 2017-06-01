package edu.uwm.cs552.gui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;

/**
 * A GUI component whose preferred size depends on the required width.
 * If the width is smaller, the height may be greater.
 */
public interface FlowComponent {
	/**
	 * If the preferred height returns this value,
	 * it means that the width requested is too small.
	 */
	public static final int TOO_SMALL = 1000000; 
	
	/** Get the height that this component will need to display the
	 * text given the font metrics and the size.
	 * @param fm font metrics to use, must not be null
	 * @param width width of area to fit component in. 
	 * @return height that enables the component to fit, or {@link TOO_SMALL} if
	 * the specified width is too small.
	 */
	public int getPreferredHeight(FontMetrics fm, int width);
	
	// The following are copied over from component
	
	/**
	 * @see {@link java.awt.Component#setBounds(Rectangle)}
	 */
	public void setBounds(Rectangle r);
	
	/**
	 * @see {@link java.awt.Component#setFont(Font)}
	 */
	public void setFont(Font f);
	
	/**
	 * Get the content selected within this FlowComponent.
	 * @return String representation of this components content
	 */
	public String getSelectedContent();
	
	/**
	 * We are done with this flow component.
	 */
	public void dispose();
}
