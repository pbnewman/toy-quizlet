package edu.uwm.cs552.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import edu.uwm.cs552.Question;
import edu.uwm.cs552.ResponseLog;
/**
 * JFrame which displays the options and view for the master user
 * when asking the users of the program a question. Provides the
 * ability to abort an asked question, view the results/close a
 * question, and view the number of responses received in live time.
 */
public class MasterQuestionDialog extends JFrame implements QuestionDialog {
	
	/**
	 * KEH
	 */
	private static final long serialVersionUID = 1L;
	private QuestionPanel questionPane;
	private final ResponseLog log;
	private final JLabel responseCount = new JLabel();
	private boolean questionInProgress = false;
	private boolean showResults = false;
	
	private final JButton abortButton = new JButton("Abort");
	private final JButton resultsButton = new JButton("Show Results");
	private final JButton doneButton = new JButton("Done");

	public MasterQuestionDialog(ResponseLog r) {
		super("Master Question Dialog");
		log = r;
		log.addObserver(this);
		
		setSize(500, 300);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		
		JPanel northPanel = new JPanel();
		northPanel.add(responseCount);
		add(northPanel, BorderLayout.NORTH);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(abortButton);
		buttonPanel.add(resultsButton);
		buttonPanel.add(doneButton);
		setAllButtonsEnabled(false);
		add(buttonPanel, BorderLayout.SOUTH);
		abortButton.addActionListener((ActionEvent ae) -> {
			if (!questionInProgress) {
				JOptionPane.showMessageDialog(this, "There is no question in progress to abort.",
						"No question in Progress", JOptionPane.ERROR_MESSAGE);
				return;
			}
			abort();
		});
		resultsButton.addActionListener((ActionEvent ae) -> {
			if (showResults) {
				JOptionPane.showMessageDialog(this, "Results are already being shown",
						"Results already displayed", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (!questionInProgress) {
				JOptionPane.showMessageDialog(this, "There is no question in progress to show results for.",
						"No question in Progress", JOptionPane.ERROR_MESSAGE);
				return;
			}
			stop();
		});
		doneButton.addActionListener((ActionEvent ae) -> {
			if (questionInProgress) {
				JOptionPane.showMessageDialog(this, "Done cannot be called while a question is in progress.",
						"Question in Progress", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (!showResults) {
				JOptionPane.showMessageDialog(this, "No",
						"Show Results not called", JOptionPane.ERROR_MESSAGE);
				return;
			}
			showResults = false;
			questionPane.dispose();
			remove(questionPane);
			questionPane = null;
			responseCount.setText("Responses: ");
			setAllButtonsEnabled(false);
			setVisible(false);
		});
	}

	@Override
	public void start(Question q) {
		if (questionInProgress)
			throw new IllegalStateException("start cannot be called when a question is in progress");
		questionInProgress = true;
		showResults = false;
		questionPane = QuestionPanel.createQuestionPanel(q, null, false);
		
		add(questionPane, BorderLayout.CENTER);
		log.startQuestion(q);
		int count = log.getResponses(questionPane.question).size();
		responseCount.setText("Responses: " + count);
		setVisible(true);
		setAllButtonsEnabled(true);
		// XXX: not good code, last minute rushed changes :/
		if (questionPane.responseComponent instanceof TextFlowComponent) {
			((TextFlowComponent) questionPane.responseComponent).setEnabled(false);
		}
	}

	@Override
	public void stop() {
		if (!questionInProgress)
			throw new IllegalStateException("stop cannot be called when a question is not in progress");
		log.stopQuestion();
		questionInProgress = false;
		showResults = true;
		questionPane.showResponses(log.getResponses(questionPane.question));
	}

	@Override
	public void abort() {
		if (!questionInProgress && !showResults)
			throw new IllegalStateException("abort cannot be called when a question is not in progress");
		questionInProgress = showResults = false;
		questionPane.dispose();
		remove(questionPane);
		questionPane = null;
		log.abortQuestion();
		responseCount.setText("Responses: ");
		setAllButtonsEnabled(false);
		setVisible(false);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (!questionInProgress) return;
		int count = log.getResponses(questionPane.question).size();
		responseCount.setText("Responses: " + count);
	}

	@Override
	public boolean questionInProgress() {
		return questionInProgress;
	}
	
	private void setAllButtonsEnabled(boolean enabled) {
		abortButton.setEnabled(enabled);
		resultsButton.setEnabled(enabled);
		doneButton.setEnabled(enabled);
	}
}
