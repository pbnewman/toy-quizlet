package edu.uwm.cs552.net;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;

import javax.swing.SwingUtilities;

import edu.uwm.cs.util.XMLTokenizer;
import edu.uwm.cs.util.XMLWriter;
import edu.uwm.cs552.Choice;
import edu.uwm.cs552.ChoiceQuestion;
import edu.uwm.cs552.FreeResponseQuestion;
import edu.uwm.cs552.Question;
import edu.uwm.cs552.Response;
import edu.uwm.cs552.Script;
import edu.uwm.cs552.User;
import edu.uwm.cs552.User.Users;
import edu.uwm.cs552.XMLObject;
import edu.uwm.cs552.gui.LocalResponseLog;
import edu.uwm.cs552.net.UserResponses;
/**
 * A ResponseLog which supports a client
 * connected to a NetworkResponseLog.
 */
public class ClientResponseLog extends LocalResponseLog {
	private final User user;
	private final String server;
	private final int port;
	
	private Socket connection;
	private Thread inputThread;
	private BackgroundXMLObjectStream outputQueue;
	
	/**
	 * Constructor for ClientResponseLog
	 * @param client: User.Users class holding the client of this
	 * @param s: name of the server to connect with
	 * @param p: port 
	 */
	public ClientResponseLog(Users client, String s, int p) {
		super(client);
		user = client.getElementAt(0);
		server = s;
		port = p;
		try {
			connection = new Socket(server, port);
			outputQueue = new BackgroundXMLObjectStream(new XMLWriter(connection.getOutputStream()), "UserResponses", Collections.singletonMap("user", user.getName()));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		inputThread = new Thread(() -> doInput());
		inputThread.start();
	}

	@Override
	public void addResponse(Response r) {
		super.addResponse(r);
		outputQueue.write(r);
	}

	private void doInput() {
		boolean questionInProgress = false;
		try {
			XMLTokenizer xt = new XMLTokenizer(connection.getInputStream());
			xt.next(); xt.next();
			for (;;) {
				if (xt.hasNext()) {
					XMLObject obj = fromXML(xt);
					if (obj instanceof Question) {
						System.out.println(user.getName() + " received a Question from the server.");
						questionInProgress = true;
						SwingUtilities.invokeLater(() -> startQuestion((Question) obj));
					} else if (obj instanceof ResponseTimeDone) {
						if (!questionInProgress) continue;
						System.out.println(user.getName() + " received a ResponseTimeDone signal from the server.");
						questionInProgress = false;
						SwingUtilities.invokeLater(() -> stopQuestion());
					} else if (obj instanceof Error) {
						System.out.print(user.getName() + " received an Error: ");
						System.out.println(((Error) obj).getError());
					} else {
						throw new IOException();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		outputQueue.close();
		try { Thread.sleep(100); } catch (InterruptedException e) { }
		System.exit(0);
	}
	
	public static void main(String[] args) {
		// Initial default userName, server, port
		// Then process arguments
		// Then create a ClientResponseLog
		// and read a Request object from the server while
		// writing UserResponses in a background thread.
		String userName = "guest";
		String server = "localhost";
		int port = 53129;
		for (String s : args) {
			if (s.startsWith("--user=")) {
				userName = s.substring(7);
			} else if (s.startsWith("--server=")) {
				server = s.substring(9);
			} else if (s.startsWith("--port=")) {
				port = Integer.parseInt(s.substring(7));
			}
		}
		User.Users client = new Users();
		client.get(userName);
		try {
			@SuppressWarnings("unused")
			ClientResponseLog clientLog = new ClientResponseLog(client, server, port);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	static {
		register("UserResponses", () -> { return new UserResponses(); });
		register("Requests", () -> { return new Requests(); });
		register("ResponseTimeDone", () -> { return new ResponseTimeDone(); });
		register("Error", () -> { return new Error(); });
		register("ChoiceQuestion", () -> { return new ChoiceQuestion(); });
		register("FreeResponseQuestion", () -> { return new FreeResponseQuestion(); });
		register("Choice", () -> { return new Choice(); });
		register("Script", () -> { return new Script(); });
	}
}
