package edu.uwm.cs552.gui;

import java.awt.SystemColor;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

import edu.uwm.cs.util.Histogram;
import edu.uwm.cs.util.XMLTokenizer;
import edu.uwm.cs552.FreeResponseQuestion;
import edu.uwm.cs552.Question;
import edu.uwm.cs552.Response;
import edu.uwm.cs552.XMLObject;
import edu.uwm.cs552.XMLObject.ParseException;

public class FreeResponseQuestionPanel extends QuestionPanel {

	/**
	 * KEH
	 */
	private static final long serialVersionUID = 1L;
	

	public FreeResponseQuestionPanel(FreeResponseQuestion q, boolean editable) {
		super(q, editable);
	}

	
	@Override
	protected FlowComponent createResponseComponent(Question q) {
		TextFlowComponent result = new TextFlowComponent(" ");
		result.setBackground(SystemColor.window);
		result.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		result.setEditable(true);
		result.setEnabled(!editable);
		return result;
	}

	@Override
	public void showResponses(List<Response> li) {
		Histogram<String> h = new Histogram<>();
		for (Response r : li) {
			h.add(r.getText());
		}
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Integer> e : h.entrySet()) {
			if (e.getValue() > 1) {
				sb.append(e.getValue() + " " + e.getKey() + "\n");
			}
		}
		((TextFlowComponent) responseComponent).setText(sb.toString().trim());
	}

	public static void main(String[] args) throws FileNotFoundException, ParseException {
		Reader r = new FileReader("lib/sample-free-question.xml");
		XMLTokenizer tok = new XMLTokenizer(new BufferedReader(r));
		FreeResponseQuestion fq = (FreeResponseQuestion)XMLObject.fromXML(tok);
		SwingUtilities.invokeLater(() -> {
			JFrame f = new JFrame("Test Question");
			f.setSize(500,300);
			f.setContentPane(new FreeResponseQuestionPanel(fq, true));
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			f.setVisible(true);
		});
	}
}
