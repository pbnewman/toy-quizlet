package edu.uwm.cs552;

import java.io.IOException;
import java.time.LocalDateTime;

import edu.uwm.cs.util.XMLWriter;

/**
 * A response to a question.
 */
public class Response extends XMLObject {
	private User.Users users; 
	
	private User user;
	private String response= "";
	private LocalDateTime timestamp;

	/**
	 * Create a new response at this time point.
	 * @param u user responding, not null
	 * @param r text of response, not null
	 */
	public Response(User u, String r) {
		user = u;
		response = r;
		timestamp = LocalDateTime.now();
	}
	
	/**
	 * Start to read in a historical response.
	 * @param list dictionary to lookup up user name in, must not be null
	 */
	public Response(User.Users list) {
		users = list;
	}
	
	public User getUser() {
		return user;
	}
	
	public String getText() {
		return response;
	}
		
	@Override
	protected String getXMLelementName() {
		return "Response";
	}
	
	@Override
	protected void writeAttributes(XMLWriter xw) throws IOException {
		super.writeAttributes(xw);
		xw.writeAttr("user",user.getName());
		xw.writeAttr("timestamp", timestamp.toString());
	}

	@Override
	protected void writeContents(XMLWriter xw) throws IOException {
		xw.writeCDATA(response);
	}

	@Override
	protected void addAttribute(String name, String text) throws ParseException {
		if (name.equals("user")) {
			user = users.get(text);
			users = null; // not needed any more
		} else if (name.equals("timestamp")) {
			timestamp = LocalDateTime.parse(text);
		} else super.addAttribute(name, text);
	}

	@Override
	protected void addText(String text) throws ParseException {
		response += text;
	}

	
}
