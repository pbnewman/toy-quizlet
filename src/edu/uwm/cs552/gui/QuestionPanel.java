package edu.uwm.cs552.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;

import edu.uwm.cs.util.MultiSelectionModel;
import edu.uwm.cs552.ChoiceQuestion;
import edu.uwm.cs552.FreeResponseQuestion;
import edu.uwm.cs552.Question;
import edu.uwm.cs552.Response;
import edu.uwm.cs552.User;

public abstract class QuestionPanel extends FlowFillingPanel implements Observer {
	/**
	 * KEH
	 */
	private static final long serialVersionUID = 1L;

	private static final float MARGIN_WIDTH = 0.5f; // left/right margin in font size points
	private static final float MARGIN_HEIGHT = 0.5f; // how many lines above question, below response
	private static final float QUESTION_SEPARATOR_HEIGHT = 0.5f; // how many lines between question and response

	protected final Question question;
	protected final boolean editable;
	private final TextFlowComponent questionComponent;
	protected final FlowComponent responseComponent;
	
	public QuestionPanel(Question q, boolean editable) {
		super(new StackFlowComponent());
		super.setBackground(Color.WHITE);
		question = q;
		this.editable = editable;
		questionComponent = new TextFlowComponent();
		questionComponent.setEditable(editable);
		questionComponent.setText(q.getQuestion());
		responseComponent = createResponseComponent(q);
		StackFlowComponent whole = (StackFlowComponent)getContents();
		whole.setPadding(QUESTION_SEPARATOR_HEIGHT);
		whole.add(questionComponent);
		whole.add((JComponent)responseComponent);
		whole.setBackground(Color.WHITE);
		setMarginHeight(MARGIN_HEIGHT);
		setMarginWidth(MARGIN_WIDTH);
		
		// make consistent by back and forth changes
		q.addObserver(this);
		questionComponent.addTextChangedListener((e) -> {
			question.setQuestion(e.getAfterText());
		});
	}
	
	protected abstract FlowComponent createResponseComponent(Question q);
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(getForeground());
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == question && "question".equals(arg)) {
			questionComponent.setText(question.getQuestion());
		}
	}

	@Override
	public void dispose() {
		question.deleteObserver(this);
		super.dispose();
	}
	
	/**
	 * Create and return a response for the passed User using
	 * this QuestionPanel.
	 * @param user (must not be null)
	 * @return Response constructed for passed user
	 */
	public Response getResponse(User u) {
		return new Response(u, responseComponent.getSelectedContent());
	}
	
	/**
	 * Display the passes responses in this QuestionPanel.
	 * @param li list of Response's to display
	 */
	public abstract void showResponses(List<Response> li);
	
	/**
	 * Instantiate and return a QuestionPanel for the passed parameters.
	 * @param q, must not be null
	 * @param msm, only necessary if q requires a SelectionModel
	 * @param editable
	 * @return QuestionPanel for Question q
	 */
	public static QuestionPanel createQuestionPanel(Question q, MultiSelectionModel msm, boolean editable) {
		msm = msm != null ? msm : new MultiSelectionModel();
		if (q instanceof ChoiceQuestion) {
			return new ChoiceQuestionPanel((ChoiceQuestion) q, msm, editable); 
		} else if (q instanceof FreeResponseQuestion) {
			return new FreeResponseQuestionPanel((FreeResponseQuestion) q, editable);	
		}
		return null;
	}

}
