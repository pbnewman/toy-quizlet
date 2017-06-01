package edu.uwm.cs552;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import edu.uwm.cs.util.XMLWriter;

public class Script extends XMLObject implements Observer {

	private List<Question> questions = new ArrayList<>();
	
	public int size() {
		return questions.size();
	}

	/**
	 * Return the question at the 1-based index given.
	 * @param index 1-based index into script.
	 * @return question, if within bounds, never null
	 * @throws IndexOutOfBoundsException if index is zero or bigger than the size of the script.
	 */
	public Question getAtIndex(int index) throws IndexOutOfBoundsException {
		return questions.get(index-1);
	}
	
	/**
	 * Remove and return the question at the 1-based index given.
	 * @param index 1-based index into script.
	 * @return question, if within bounds, never null
	 * @throws IndexOutOfBoundsException if index is zero or bigger than the size of the script.
	 */
	public Question removeAtIndex(int index) throws IndexOutOfBoundsException {
		final Question result = questions.remove(index-1);
		result.deleteObserver(this);
		super.setChanged();
		super.notifyObservers(result);
		return result;
	}
	
	/**
	 * Insert the question at the given index, moving the current (if any) question
	 * and later questions further in the list.
	 * @param q question to add, must not be null
	 * @param index 1-based index to insert question at.
	 */
	public void addAtIndex(Question q, int index) throws IndexOutOfBoundsException {
		if (q == null) throw new NullPointerException("Null Question");
		questions.add(index-1, q);
		q.addObserver(this);
		super.setChanged();
		super.notifyObservers(q);
	}
	
	/**
	 * Add the question into the script at the end.
	 * @param q question to add, must not be null
	 * @return 
	 */
	public int add(Question q) {
		addAtIndex(q,size()+1);
		return questions.size();
	}
	
	/**
	 * Get rid of all current questions, replace with this list of questions.
	 * @param qs questions to use, must not be null
	 * @return previous questions
	 */
	public List<Question> setAll(List<Question> qs) {
		List<Question> result = questions;
		for (Question q : result) {
			q.deleteObserver(this);
		}
		questions = new ArrayList<>(qs);
		for (Question q : questions) {
			q.addObserver(this);
		}
		setChanged();
		notifyObservers();
		return result;
	}
	
	/**
	 * Remove all current questions.
	 * @return what questions we had before.
	 */
	public List<Question> clear() {
		return setAll(Collections.emptyList());
	}
	
	@Override
	public void update(Observable o, Object arg) {
		setChanged();
		notifyObservers(o);
	}

	@Override
	protected String getXMLelementName() {
		return "Script";
	}

	@Override
	protected void writeContents(XMLWriter xw) throws IOException {
		super.writeContents(xw);
		for (Question q : questions) {
			q.toXML(xw);
		}
	}

	@Override
	protected void addElement(XMLObject obj) throws ParseException {
		if (obj instanceof Question) {
			add((Question)obj);
		} else super.addElement(obj);
	}

	{
		XMLObject.register("Choice", () -> { return new Choice(); });
		XMLObject.register("ChoiceQuestion", () -> { return new ChoiceQuestion(); });
		XMLObject.register("FreeResponseQuestion", () -> { return new FreeResponseQuestion(); });
		XMLObject.register("Script", () -> { return new Script(); });
	}
}
