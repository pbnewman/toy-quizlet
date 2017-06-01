package edu.uwm.cs552.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.uwm.cs.util.MultiSelectionModel;
import edu.uwm.cs552.Choice;
import edu.uwm.cs552.ChoiceQuestion;

public class ChoicesFlowComponent extends JPanel implements FlowComponent, Observer {
	/**
	 * KEH
	 */
	private static final long serialVersionUID = 1L;

	private static final float INDEX_SEPARATOR = 0.5f;
	private static final float CHOICE_SEPARATOR = 0.5f;
	private static final Color SELECTED_COLOR = new Color(0x87,0xCE,0xFA); // Light sky blue

	private ChoiceQuestion question;
	private final List<ChoiceFlowComponent> choiceComponents;
	private final StackFlowComponent texts = new StackFlowComponent();
	private MultiSelectionModel selectionModel = new MultiSelectionModel(); // temporary
	private final boolean editable;
	
	public ChoicesFlowComponent(ChoiceQuestion cq, boolean editable) {
		this.editable = editable;
		setBackground(Color.WHITE);
		question = cq;
		question.addObserver(this);
		choiceComponents = new ArrayList<>();
		texts.setPadding(CHOICE_SEPARATOR);
		add(texts);
		updateComponents();
	}
	
	protected void switchIndexToCount(Set<Entry<String, Integer>> s) {
		for (ChoiceFlowComponent cfc : choiceComponents) {
			cfc.switchIndexToCount(0);
			for (Entry<String, Integer> e : s) {
				if (e.getKey().equals(cfc.index + "")) {
					cfc.switchIndexToCount(e.getValue());
					break;
				}
			}
		}
	}
	
	/**
	 * Change the selectionModel "soon" after construction.
	 * This method should only be called once.
	 * @param sm
	 */
	public void setSelectionModel(MultiSelectionModel sm) {
		selectionModel.deleteObserver(this);
		selectionModel = sm;
		selectionModel.addObserver(this);
	}
	
	/**
	 * Make sure that we have the right number of choice
	 * components and that all are associated with the respective choices.
	 */
	protected void updateComponents() {
		int n1 = question.numChoices();
		int n2 = choiceComponents.size();
		while (n2 < n1) {
			++n2;
			final ChoiceFlowComponent cfc = new ChoiceFlowComponent(n2);
			choiceComponents.add(cfc);
		}
		while (n1 < n2) {
			--n2;
			ChoiceFlowComponent cfc = choiceComponents.remove(n2);
			cfc.update();
		}
		for (ChoiceFlowComponent cfc : choiceComponents) {
			cfc.update();
		}
	}
	
	@Override 
	public void update(Observable o, Object obj) {
		if (o == question) updateComponents();
		if (o == selectionModel) {
			for (ChoiceFlowComponent cfc : choiceComponents) {
				cfc.update(o,obj);
			}
			repaint();
		}
	}
	
	protected int getLabelWidth(FontMetrics fm) {
		int labelWidth = 0;
		for (ChoiceFlowComponent cfc : choiceComponents) {
			int w = cfc.getIndexWidth(fm);
			if (w > labelWidth) labelWidth = w;
		}
		labelWidth += Math.round(fm.getFont().getSize()*INDEX_SEPARATOR);
		return labelWidth;
	}
	
	@Override
	public int getPreferredHeight(FontMetrics fm, int width) {
		if (question.numChoices() == 0) return 0;
		int labelWidth = getLabelWidth(fm);
		int height = texts.getPreferredHeight(fm, width-labelWidth);
		return height;
	}

	@Override
	public void setFont(Font f) {
		super.setFont(f);
		for (Component c : getComponents()) {
			c.setFont(f);
		}
	}
	
	@Override
	public String getSelectedContent() {
		StringBuilder sb = new StringBuilder();
		int found = 0;
		int i = 0;
		while (found < selectionModel.numSelected()) {
			if (selectionModel.isSelected(++i)) {
				sb.append(" " + i);
				++found;
			}
		}
		return sb.toString().trim();
	}
	
	protected void addIndexLabel(JLabel l) {
		super.add(l);
		invalidate();
	}
	
	@Override
	public void doLayout() {
		int width = getWidth();
		FontMetrics fm = getFontMetrics(getFont());
		int lw = getLabelWidth(fm);
		texts.setBounds(new Rectangle(lw,0,width-lw,getHeight()));
		texts.doLayout(); // needed so that we can layout the index labels.
		for (ChoiceFlowComponent cfc : choiceComponents) {
			cfc.layoutIndex();
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Color savedColor = g.getColor();
		for (int index = 1; index <= choiceComponents.size(); ++index) {
			if (selectionModel.isSelected(index)) {
				ChoiceFlowComponent cfc = choiceComponents.get(index-1);
				Rectangle r = cfc.getBounds();
				g.setColor(SELECTED_COLOR);
				g.fillRect(r.x, r.y, r.width, r.height);
			}
		}
		g.setColor(savedColor);
	}
	
	private class ChoiceFlowComponent implements Observer, TextChangedListener {
		
		private final JLabel itemLabel;
		private final TextFlowComponent textComponent;
		private final int index;
		private Choice choice;
		
		public ChoiceFlowComponent(int i) {
			index = i;
			itemLabel = new JLabel();
			itemLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (!ChoicesFlowComponent.this.editable && ChoicesFlowComponent.this.question.canSelectMultiple()) {
						selectionModel.toggleSelection(index);	// Task #3
					} else {
						selectionModel.setSelected(index);
					}
				}
			});
			textComponent = new TextFlowComponent();
			textComponent.setEditable(ChoicesFlowComponent.this.editable);
			textComponent.addTextChangedListener(this);
			textComponent.setOpaque(true);
			update();
		}
		
		protected void switchIndexToCount(int count) {
			itemLabel.setText(count + " ");
		}
		
		public void update() {
			Choice newChoice = index > question.numChoices() ? null : question.getChoices().get(index-1);
			if (choice != null) {
				choice.deleteObserver(this);
			} else {
				ChoicesFlowComponent.this.addIndexLabel(itemLabel);
				texts.add(textComponent);
			}
			choice = newChoice;
			if (choice != null) {
				itemLabel.setText(question.getChoiceStyle().toString(index));
				textComponent.setText(choice.getText());
				textComponent.invalidate();
				newChoice.addObserver(this);
			} else {
				ChoicesFlowComponent.this.remove(itemLabel);
				texts.remove(textComponent);
			}
		}

		@Override
		public void update(Observable o, Object arg) {
			if (o == choice) update();
			if (o == selectionModel) {
				if (selectionModel.isSelected(index)) {
					textComponent.setBackground(SELECTED_COLOR);
				} else {
					textComponent.setBackground(Color.WHITE);
				}
			}
		}
		
		public int getIndexWidth(FontMetrics fm) {
			String s = choice.getStyle().toString(index);
			return fm.stringWidth(s);
		}

		public void layoutIndex() {
			Rectangle r = textComponent.getBounds(); // relative to stack
			final Dimension d = itemLabel.getPreferredSize();
			itemLabel.setBounds(0,r.y+(r.height-d.height)/2,d.width,d.height);
		}
		
		public Rectangle getBounds() {
			Rectangle r = textComponent.getBounds(); // relative to stack
			return new Rectangle(0,r.y,ChoicesFlowComponent.this.getWidth(),r.height);
		}

		@Override
		public void textChanged(TextChangedEvent e) {
			choice.setText(e.getAfterText());
		}
		
		@Override
		public String toString() {
			return "item #" + index;
		}
	}
	
	@Override
	public void dispose() {
		question.deleteObserver(this);
		selectionModel.deleteObserver(this);
		question = new ChoiceQuestion();
		updateComponents();
		texts.dispose();
	}
}
