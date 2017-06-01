package edu.uwm.cs552;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.uwm.cs.util.Pair;
import edu.uwm.cs.util.XMLTokenizer;
import edu.uwm.cs.util.XMLWriter;

/**
 * Remembering questions that have been put to responses and the responses they garnered.
 */
public class ResponseLog extends XMLObject {

	private List<Pair<Question,List<Response>>> log = new ArrayList<>();
	
	private Question current = null;
	private List<Response> responses = null;
	
	/**
	 * Start a log entry for this question (provisionally).
	 * @param q question to use, must not be null
	 */
	public void startQuestion(Question q) {
		if (q == null) throw new IllegalArgumentException("cannot start a question that is null");
		if (current != null) throw new IllegalStateException("question already started.");
		current = q;
		responses = new ArrayList<>();
		log.add(Pair.create(current, responses));
		super.noteChange("question");
	}
	
	/**
	 * Add a response to the current question.
	 * @param r response to the current question, must not be null
	 */
	public void addResponse(Response r) {
		if (r == null) throw new IllegalArgumentException("response cannot be null");
		if (current == null) throw new IllegalStateException("no question started");
		responses.add(r);
		super.noteChange("response");
	}
	
	/**
	 * Stop accepting responses (if any).
	 * Doesn't log the question or responses.
	 */
	public void abortQuestion() {
		if (current != null) {
			log.remove(log.size()-1);
		}
		current = null;
		responses = null;
	}
	
	/**
	 * Finish accepting responses for the current question.
	 * The question and responses are logged and then
	 * there is no current question.
	 */
	public void stopQuestion() {
		if (current == null) throw new IllegalStateException("no question started");
		current = null;
		responses = null;
	}
	
	/**
	 * Return a list of all questions in the log.
	 * The result is a copy and can be safely mutated by the client.
	 * @return list of all questions that have been logged, never null.
	 */
	public List<Question> getQuestions() {
		List<Question> result = new ArrayList<>();
		for (Pair<Question,List<Response>> p : log) {
			result.add(p.fst);
		}
		return result;
	}
	
	/**
	 * Return a list of all responses to a question.
	 * The result is a copy and can be safely mutated by the client.
	 * If a question was logged twice in this session, then all
	 * responses from (from all occurrences) are returned.
	 * @param q question (may be null) to look for responses for
	 * @return list of responses, may be empty, will never be null.
	 */
	public List<Response> getResponses(Question q) {
		List<Response> result = new ArrayList<>();
		for (Pair<Question,List<Response>> p : log) {
			if (p.fst == q) { // use object identity!
				result.addAll(p.snd);
			}
		}
		return result;
	}
	
	/**
	 * Return true if the log is empty.
	 * @return whether log is empty.
	 */
	public boolean isEmpty() {
		return log.isEmpty();
	}
	
	/**
	 * Discard all information in the log.
	 */
	public void clear() {
		log.clear();
		current = null;
		responses = null;
		super.noteChange("clear");
	}
	
	// XML reading/writing
	@Override
	protected String getXMLelementName() {
		return "ResponseLog";
	}

	@Override
	protected void writeContents(XMLWriter xw) throws IOException {
		super.writeContents(xw);
		for (Pair<Question,List<Response>> p : log) {
			p.fst.toXML(xw);
			for (Response r : p.snd) {
				r.toXML(xw);
			}
		}
	}

	@Override
	protected void addElement(XMLObject obj) throws ParseException {
		if (obj instanceof Question) {
			current = null;
			responses = null;
			startQuestion((Question)obj);
		} else if (obj instanceof Response) {
			addResponse((Response)obj);
		} else super.addElement(obj);
	}

	@Override
	protected void readXML(XMLTokenizer xt) throws ParseException {
		super.readXML(xt);
		// clean up the current question.
		current = null;
		responses = null;
	}

}
