package edu.uwm.cs552.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A version of JTextArea that
 * makes it easy to be told when the text has changed.
 * It waits until the user is done editing rather than
 * firing after every edit.
 * It also doesn't notify when the text is changed by the programmer
 * when the component does not have focus.
 */
public class MyTextArea extends JTextArea {
	/**
	 * KEH
	 */
	private static final long serialVersionUID = 1L;
	
	private List<TextChangedListener> listeners = new ArrayList<>();

	public void addTextChangedListener(TextChangedListener l) {
		listeners.add(l);
	}
	
	public void removeTextChangeListener(TextChangedListener l) {
		listeners.remove(l);
	}
	
	/** @see JTextArea#JTextArea() */	
	public MyTextArea() {
		super();
	}

	/** @see JTextArea#JTextArea(int,int) */
	public MyTextArea(int rows, int columns) {
		super(rows, columns);
	}

	/** @see JTextArea#JTextArea(String,int,int) */
	public MyTextArea(String text, int rows, int columns) {
		super(text, rows, columns);
	}

	/** @see JTextArea#JTextArea(String) */
	public MyTextArea(String text) {
		super(text);
	}

	@Override
	public void setText(String newText) {
		if (getText().equals(newText)) return; // important to avoid infinite regress
		super.setText(newText);
	}
	
	private MyDocumentListener myListener = new MyDocumentListener();
	{
		getDocument().addDocumentListener(myListener);
		addFocusListener(myListener);
	}
	
	private class MyDocumentListener implements DocumentListener, FocusListener {
		private String beforeText = null;

		@Override
		public void insertUpdate(DocumentEvent e) {
			fireChange();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			fireChange();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			fireChange();
		}

		@Override
		public void focusGained(FocusEvent e) {
			beforeText = MyTextArea.super.getText();
		}

		@Override
		public void focusLost(FocusEvent e) {
			beforeText = null;
		}

		private void fireChange() {
			if (beforeText == null) return; // change didn't come from us
			String afterText = MyTextArea.super.getText();
			if (beforeText.equals(afterText)) return; // don't generate event
			final TextChangedEvent event = new TextChangedEvent(MyTextArea.this,beforeText,afterText);
			SwingUtilities.invokeLater(() -> {
				for (TextChangedListener l : listeners) {
					l.textChanged(event);
				}
			});
		}
		
	}
}
