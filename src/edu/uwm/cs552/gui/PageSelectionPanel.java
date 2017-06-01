package edu.uwm.cs552.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.Scrollable;

import edu.uwm.cs.util.SingleSelectionModel;
import edu.uwm.cs552.Question;
import edu.uwm.cs552.Script;

public class PageSelectionPanel extends JComponent implements Observer, Scrollable {

	/**
	 * KEH
	 */
	private static final long serialVersionUID = 1L;

	private static final Color SELECTION_COLOR = Color.CYAN;
	
	private final Script script;
	private final SingleSelectionModel selectionModel;
	
	private static final int MARGIN = 10;
	
	public PageSelectionPanel(Script scr, SingleSelectionModel sm) {
		script = scr;
		selectionModel = sm;
		script.addObserver(this);
		selectionModel.addObserver(this);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectFromPoint(e.getPoint());
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				moveToPoint(e.getPoint());
			}
		});
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setFont(getFont());
		FontMetrics fm = g.getFontMetrics();
		int h = fm.getHeight();
		int y = fm.getAscent();
		int sel = selectionModel.getSelected();
		if (sel > 0) {
			Color saved = g.getColor();
			g.setColor(SELECTION_COLOR);
			g.fillRect(0, (sel-1)*h, getWidth(), h);
			g.setColor(saved);
		}
		for (int i=1; i <= script.size(); ++i) {
			String s = ""+i;
			g.drawString(s, MARGIN, y);
			y += h;
		}
	}

	private void selectFromPoint(Point p) {
		FontMetrics fm = getFontMetrics(getFont());
		int i = p.y / fm.getHeight();
		if (i < script.size()) {
			selectionModel.setSelected(i+1);
		}
	}
	
	private void moveToPoint(Point p) {
		FontMetrics fm = getFontMetrics(getFont());
		int i = 1 + p.y / fm.getHeight();
		if (i > script.size()) i = script.size();
		int j = selectionModel.getSelected();
		if (j != 0 && i != j) {
			Question q = script.removeAtIndex(j);
			if (j < i) --i;
			script.addAtIndex(q, i);
			selectionModel.setSelected(i);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == script) {
			invalidate();
		}
		repaint();
	}
	
	@Override
	public Dimension getPreferredSize() {
		FontMetrics fm = getFontMetrics(getFont());
		int numSize = fm.stringWidth(""+script.size());
		return new Dimension(numSize+2*MARGIN,fm.getHeight()*script.size());
		
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		// only interested in vertical scrolling
		return getFontMetrics(getFont()).getHeight();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		// only interested in vertical scrolling
		return visibleRect.height - getScrollableUnitIncrement(visibleRect,orientation,direction);
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}
