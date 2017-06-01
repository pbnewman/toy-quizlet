package edu.uwm.cs552.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;

import javax.swing.JPanel;

/**
 * A flow component that stacks flow components vertically 
 * with padding between them.  The padding expands with the font size,
 * it is a fraction of the line height.
 */
public class StackFlowComponent extends JPanel implements FlowComponent {

	/**
	 * KEH
	 */
	private static final long serialVersionUID = 1L;

	private float paddingFraction;
	
	public StackFlowComponent() {
		super();
		setOpaque(false);
	}
	
	/**
	 * Change the padding between items.
	 * @param pad fraction of line height to place between items.
	 */
	public void setPadding(float pad) {
		paddingFraction = pad;
		invalidate();
	}

	@Override
	public void setFont(Font f) {
		super.setFont(f);
		for (Component c : getComponents()) {
			c.setFont(f);
		}
	}
	
	@Override
	public int getPreferredHeight(FontMetrics fm, int width) {
		if (this.getComponentCount() == 0) return 0;
		float result = -paddingFraction*fm.getHeight();
		for (Component c : getComponents()) {
			if (c instanceof FlowComponent) {
				FlowComponent fc = (FlowComponent)c;
				result += fc.getPreferredHeight(fm, width);
			} else {
				result += c.getPreferredSize().height;
			}
			result += paddingFraction*fm.getHeight();
		}
		return Math.round(result);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getWidth(), getPreferredHeight(getFontMetrics(getFont()),getWidth()));
	}
	
	@Override
	public void doLayout() {
		if (this.getComponentCount() == 0) return;
		int w = super.getWidth();
		final FontMetrics fm = getFontMetrics(getFont());
		int y = 0;
		for (Component c : getComponents()) {
			int h;
			if (c instanceof FlowComponent) {
				FlowComponent fc = (FlowComponent)c;
				h = fc.getPreferredHeight(fm, w);
			} else {
				h = c.getPreferredSize().height;
			}
			c.setBounds(new Rectangle(0,y,w,h));
			y += h;
			y += Math.round(paddingFraction * fm.getHeight());
		}
	}

	@Override
	public void dispose() {
		for (Component c : getComponents()) {
			if (c instanceof FlowComponent) {
				((FlowComponent)c).dispose();
			}
		}
	}

	@Override
	public String getSelectedContent() {
		// FlowComponents cannot currently be selected,
		// thus this method doesn't make much sense in
		// the context of a StackFlowComponent.
		throw new UnsupportedOperationException();
	}
}
