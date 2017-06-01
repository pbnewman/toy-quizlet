package edu.uwm.cs552.gui;

import java.util.Observer;

import edu.uwm.cs552.Question;
/**
 * Simple interface used for QuestionDialogs.
 */
public interface QuestionDialog extends Observer{

	/**
	 * Start the specified question.
	 * @param q, question to be asked (must not be null)
	 */
	public void start(Question q);
	
	/**
	 * Stop the question being currently asked.
	 */
	public void stop();
	
	/**
	 * Stop the question being currently asked and throw away the data.
	 */
	public void abort();
	
	/**
	 * Returns true if a question is in progress, false otherwise.
	 * @return boolean, obviously
	 */
	public boolean questionInProgress();
}
