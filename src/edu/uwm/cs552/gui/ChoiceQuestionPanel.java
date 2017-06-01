package edu.uwm.cs552.gui;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.uwm.cs.util.Histogram;
import edu.uwm.cs.util.MultiSelectionModel;
import edu.uwm.cs.util.XMLTokenizer;
import edu.uwm.cs552.ChoiceQuestion;
import edu.uwm.cs552.Question;
import edu.uwm.cs552.Response;
import edu.uwm.cs552.XMLObject;
import edu.uwm.cs552.XMLObject.ParseException;

public class ChoiceQuestionPanel extends QuestionPanel {

	/**
	 *  KEH
	 */
	private static final long serialVersionUID = 1L;

	
	public ChoiceQuestionPanel(ChoiceQuestion q, MultiSelectionModel sm, boolean editable) {
		super(q, editable);
		((ChoicesFlowComponent)responseComponent).setSelectionModel(sm);
	}

	@Override
	protected FlowComponent createResponseComponent(Question q) {
		return new ChoicesFlowComponent((ChoiceQuestion)q, editable);
	}
	
	@Override
	public void showResponses(List<Response> li) {
		Histogram<String> h = new Histogram<>();
		for (Response r : li) {
			for (String s : r.getText().split(" ")) {
				h.add(s);
			}
		}
		// Changes the indices of the choices
		// to the number of responses which 
		// selected them based on the histogram.
		((ChoicesFlowComponent) responseComponent).switchIndexToCount(h.entrySet());
	}

	public static void main(String[] args) throws FileNotFoundException, ParseException {
		Reader r = new FileReader("lib/sample-question.xml");
		XMLTokenizer tok = new XMLTokenizer(new BufferedReader(r));
		ChoiceQuestion cq = (ChoiceQuestion)XMLObject.fromXML(tok);
		SwingUtilities.invokeLater(() -> {
			JFrame f = new JFrame("Test Question");
			f.setSize(500,300);
			f.setContentPane(new ChoiceQuestionPanel(cq, new MultiSelectionModel(), true));
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			f.setVisible(true);
		});
	}
}
