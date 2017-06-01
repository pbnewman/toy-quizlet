package edu.uwm.cs552;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import edu.uwm.cs.util.XMLWriter;

public class ChoiceQuestion extends Question implements Iterable<Choice>, Observer {
	private List<Choice> choices = new ArrayList<>();
	private Choice.Style choiceStyle;
	private boolean selectMultiple = false;

	private boolean report(String problem) {
		System.err.println("Invariant error: " + problem);
		return false;
	}
	
	private boolean wellFormed() {
		if (choices == null) return report("choices is null");
		if (choiceStyle == null) return report("style is null");
		int index = 0;
		for (Choice c : choices) {
			++index;
			if (index != c.getIndex()) return report("choice's index inconsistent: " + c.getIndex() + " != "+ index);
			if (choiceStyle != c.getStyle()) return report("choice #" + index + "'s style inconsistent: " + c.getStyle() + " != " + choiceStyle);
		}
		return true;
	}

	/**
	 * Create a choice question with no choices initially.
	 */
	public ChoiceQuestion() {
		choiceStyle = Choice.DEFAULT_STYLE;
		assert wellFormed();
	}
	
	/**
	 * Return a list of all the choices for this question.
	 * This collection is immutable.
	 * @return list of all choices for this question.
	 */
	public List<Choice> getChoices() {
		assert wellFormed();
		return Collections.unmodifiableList(choices);
	}

	@Override
	public Iterator<Choice> iterator() {
		assert wellFormed();
		return getChoices().iterator();
	}

	public Choice.Style getChoiceStyle() {
		return choiceStyle;
	}
	
	/**
	 * Set the styelf or the choices in this question.
	 * @param cs
	 */
	public void setChoiceStyle(Choice.Style cs) {
		assert wellFormed();
		if (cs == null) cs = Choice.DEFAULT_STYLE;
		if (choiceStyle.equals(cs)) return;
		choiceStyle = cs;
		for (Choice c : choices) {
			c.setStyle(cs);
		}
		assert wellFormed();
		super.noteChange("style");
	}
	
	/**
	 * Return the number of choices for the question.
	 * @return number of choices.
	 */
	public int numChoices() {
		assert wellFormed();
		return choices.size();
	}

	/** Add a new choice to the question.
	 * @param text text of choice to add.
	 */
	public void addChoice(String text) {
		assert wellFormed();
		Choice c = new Choice();
		c.setText(text);
		c.setIndex(choices.size() + 1);
		c.setStyle(choiceStyle);
		choices.add(c);
		c.addObserver(this);
		assert wellFormed();
		super.noteChange("add");
	}

	/**
	 * Remove a choice from the question.
	 * @param i index of choice to remove, 1 based.
	 * Must be in range.
	 * @return the choice that was removed.
	 */
	public Choice removeChoice(int i) {
		assert wellFormed();
		Choice result = choices.remove(i-1);
		for (int j = i-1; j < choices.size(); ++j) {
			choices.get(j).setIndex(j + 1);
		}
		assert wellFormed();
		result.deleteObserver(this);
		super.noteChange("remove");
		return result;
	}

	/**
	 * Move the choice at the given index to a lower (earlier) index.
	 * @param i index of choice to move (1 based), must be a valid choice index.
	 * If the choice is already at the top, (the first choice), this method
	 * has no effect.
	 */
	public void moveUp(int i) {
		assert wellFormed();
		if (i == 1)
			return;
		Choice movingUp = choices.remove(i-1);
		movingUp.setIndex(i-1);
		choices.add(i - 2, movingUp);
		Choice movingDown = choices.get(i-1);
		movingDown.setIndex(i);
		super.noteChange("move");
		assert wellFormed();
	}

	/**
	 * Move the choice at the given index to a higher (later) index.
	 * @param i index of choice to move (1 based), must be a valid choice index.
	 * If the choice is already at the bottom (the last choice), this method
	 * has no effect.
	 */
	public void moveDown(int i) {
		assert wellFormed();
		if (i == choices.size())
			return;
		Choice movingDown = choices.remove(i-1);
		movingDown.setIndex(i + 1);
		choices.add(i, movingDown);
		Choice movingUp = choices.get(i-1);
		movingUp.setIndex(i);
		super.noteChange("move");
		assert wellFormed();
	}

	/**
	 * True if this choice question lets one set multiple choices
	 * @return whether this question lets one select multiple choices.
	 */
	public boolean canSelectMultiple() {
		return selectMultiple;
	}
	
	public void setSelectMultiple(boolean sm) {
		if (sm == selectMultiple) return;
		selectMultiple = sm;
		super.noteChange("multiple");
	}
	
	@Override
	protected void printResponse(StringBuilder sb) {
		for (Choice c : choices) {
			sb.append(c.toString());
			sb.append("\n");
		}
	}

	@Override
	protected String getXMLelementName() {
		return "ChoiceQuestion";
	}

	@Override
	protected void writeAttributes(XMLWriter xw) throws IOException {
		super.writeAttributes(xw);
		if (choiceStyle != Choice.DEFAULT_STYLE) {
			xw.writeAttr("style", choiceStyle.name());
		}
		if (selectMultiple) {
			xw.writeAttr("multiple", "true");
		}
	}

	@Override
	protected void writeContents(XMLWriter xw) throws IOException {
		super.writeContents(xw);
		for (Choice c : choices) {
			c.toXML(xw);
		}

	}

	@Override
	protected void addAttribute(String name, String text) throws ParseException {
		if (name.equals("style")) {
			Choice.Style st = Choice.Style.valueOf(text);
			setChoiceStyle(st);
		} else if (name.equals("multiple")) {
			selectMultiple = Boolean.valueOf(text);
		} else {
			super.addAttribute(name, text);
		}
	}

	@Override
	protected void addElement(XMLObject obj) throws ParseException {
		if (obj instanceof Choice) {
			Choice ch = (Choice)obj;
			addChoice(ch.getText());
		} else super.addElement(obj);
	}

	@Override
	public void update(Observable o, Object arg) {
		super.noteChange("choice");
	}

	public static void main(String[] args) throws IOException {
		ChoiceQuestion q = new ChoiceQuestion();
		// Source: Praxis II Computer Science (ETS)
		q.setQuestion("Which of the following best describes the purpose of generating a flowchart as part of the design of a computer program?");
		q.addChoice("To test and maintain the efficiency of the overall program");
		q.addChoice("To present the steps needed to solve the programming problem");
		q.addChoice("To ensure that all methods are appropriately linked");
		q.addChoice("To determine the necessary number of global and local variables");
		q.setChoiceStyle(Choice.Style.PAR_ALPHA);
		System.out.println(q);
		OutputStream os = new FileOutputStream("lib/sample-choice-question.xml");
		XMLWriter xw = new XMLWriter(os);
		q.toXML(xw);
		xw.close();
	}
}
