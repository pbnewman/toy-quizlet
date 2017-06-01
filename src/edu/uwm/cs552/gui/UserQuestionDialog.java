package edu.uwm.cs552.gui;

import java.awt.BorderLayout;
import java.util.Observable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import edu.uwm.cs552.Question;
import edu.uwm.cs552.ResponseLog;
import edu.uwm.cs552.User;
/**
 * JFrame which displays asked Questions to the users of the program
 * when the master user calls to ask users. The user can interact with
 * the JFrame and nested JPanel to respond, and then submit their response.
 */
public class UserQuestionDialog extends JFrame implements QuestionDialog {
	
	/**
	 * KEH
	 */
	private static final long serialVersionUID = 1L;
	
	private final User user;
	private QuestionPanel questionPane;
	private ResponseLog log;
	private boolean questionInProgress = false;
	
	public UserQuestionDialog(User u, ResponseLog r) {
		super(u.toString() + ": Please answer");
		setSize(500, 300);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		user = u;
		log = r;
		
		JButton submit = new JButton("Submit");
		submit.addActionListener((ae) -> {
			if (questionInProgress) {
				if (questionPane.getResponse(user).getText().trim().isEmpty()) {
					JOptionPane.showMessageDialog(this, "Please enter a response to submit.",
							"Empty Response", JOptionPane.ERROR_MESSAGE);
					return;
				} else stop();
			}
		});
		add(submit, BorderLayout.SOUTH);
	}

	@Override
	public void start(Question q) {
		if (questionInProgress) 
			throw new IllegalStateException("Cannot ask start a question while one is in progress");
		questionInProgress = true;
		questionPane = QuestionPanel.createQuestionPanel(q, null, false);
		add(questionPane, BorderLayout.CENTER);
		this.setVisible(true);
	}

	@Override
	public void stop() {
		if (!questionInProgress)
			throw new IllegalStateException("Cannot stop a question when one is not in progress");
		log.addResponse(questionPane.getResponse(user));
		questionInProgress = false;
		remove(questionPane);
		questionPane.dispose();
		questionPane = null;
		this.setVisible(false);
	}

	@Override
	public void abort() {
		if (!questionInProgress)
			throw new IllegalStateException("Cannot abort a question when one is not in progress");
		questionInProgress = false;
		remove(questionPane);
		questionPane.dispose();
		questionPane = null;
		this.setVisible(false);
	}
	
	@Override
	public boolean questionInProgress() {
		return questionInProgress;
	}

	@Override
	public void update(Observable o, Object arg) { }
}
