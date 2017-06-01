package edu.uwm.cs552;

import java.io.IOException;

import edu.uwm.cs.util.XMLWriter;


public abstract class Question extends XMLObject {
	private static final String DEFAULT_QUESTION = "";
	protected String question = DEFAULT_QUESTION;

	public Question() {
		super();
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String q) {
		if (q == null) q = "";
		if (q.equals(question)) return;
		question = q;
		super.noteChange("question");
	}

	// #(
	/**
	 * Add the responses to the text for the question.
	 * @param sb
	 */
	protected abstract void printResponse(StringBuilder sb);

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(question);
		sb.append("\n");
		printResponse(sb);
		return sb.toString();
	}
	
	
	/// XML I/O

	@Override
	protected void writeContents(XMLWriter xw) throws IOException {
		super.writeContents(xw);
		if (!question.equals(DEFAULT_QUESTION)) {
			xw.writeCDATA(question);
		}
	}

	@Override
	protected void addText(String text) throws ParseException {
		setQuestion(getQuestion()+text);
	}
	
	// #)
	// TODO: toString() // using Template
	// TODO: XML hook methods
}