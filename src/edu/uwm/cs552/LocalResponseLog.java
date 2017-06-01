package edu.uwm.cs552;

import java.util.ArrayList;
import java.util.List;

import edu.uwm.cs552.User;
import edu.uwm.cs552.gui.UserQuestionDialog;
/**
 * Variant of ResponseLog in which calls to its parent also
 * spawn a number of UserQuestionDialogs specified by the users
 * held in its field.
 */
public class LocalResponseLog extends ResponseLog {

	private List<UserQuestionDialog> users = new ArrayList<>();

	public void addUser(User u) {
		if (!users.contains(u))
			users.add(new UserQuestionDialog(u, this));
	}
	
	@Override
	public void startQuestion(Question q) {
		super.startQuestion(q);
		for (UserQuestionDialog u : users) {
			 u.start(q);
		}
	}

	@Override
	public void abortQuestion() {
		super.abortQuestion();
		for (UserQuestionDialog u : users) {
			if (u.questionInProgress()) u.abort();
		}
	}

	@Override
	public void stopQuestion() {
		super.stopQuestion();
		for (UserQuestionDialog u : users) {
			if (u.questionInProgress()) u.abort();
		}
	}
	
	
}
