package edu.uwm.cs552.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import edu.uwm.cs.util.XMLTokenType;
import edu.uwm.cs.util.XMLTokenizer;
import edu.uwm.cs.util.XMLWriter;
import edu.uwm.cs552.Choice;
import edu.uwm.cs552.ChoiceQuestion;
import edu.uwm.cs552.FreeResponseQuestion;
import edu.uwm.cs552.Question;
import edu.uwm.cs552.Response;
import edu.uwm.cs552.ResponseLog;
import edu.uwm.cs552.Script;
import edu.uwm.cs552.User;
import edu.uwm.cs552.XMLObject;

public class NetworkResponseLog extends ResponseLog {
	public static final int PORT = 53129;
	private ServerSocket serverSocket;
	private Thread serverThread;
	
	public NetworkResponseLog() {
		super();
		try {
			serverSocket = new ServerSocket(PORT);
			serverThread = new Thread(() -> runServer());
			serverThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean questionInProgress = false;
	private LocalDateTime questionTimestamp;
	
	@Override
	public void startQuestion(Question q) {
		super.startQuestion(q);
		questionInProgress = true;
		questionTimestamp = LocalDateTime.now();
		writeToClients(q);
	}

	@Override
	public void stopQuestion() {
		super.stopQuestion();
		questionInProgress = false;
		writeToClients(new ResponseTimeDone());
	}
	
	private void writeToClient(ClientHandler ch, XMLObject obj) {
		clients.get(clients.indexOf(ch)).write(obj);
	}

	private void writeToClients(XMLObject obj) {
		for (ClientHandler ch : clients) {
			ch.write(obj);
		}
	}

	protected void handleResponse(ClientHandler ch, Response r) {
		if (!questionInProgress)
			writeToClient(ch, new Error("Response received with no question in progress."));
		else if (r.getTimestamp().isBefore(questionTimestamp))
			writeToClient(ch, new Error("Response received out of order."));
		else
			addResponse(r);
	}

	private static final User.Users users = new User.Users();
	private List<ClientHandler> clients = new ArrayList<>();
	
	private void runServer() {
		for (;;) {
			try {
				Socket client = serverSocket.accept();
				SwingUtilities.invokeLater(() -> clients.add(new ClientHandler(client)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class ClientHandler {
		private final Socket connection;
		private BackgroundXMLObjectStream outputQueue;
		private Thread inputThread;
		private User user;
		
		public ClientHandler(Socket client){
			connection = client;
			try {
				outputQueue = new BackgroundXMLObjectStream(new XMLWriter(connection.getOutputStream()), "Requests", Collections.emptyMap());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			inputThread = new Thread(() -> doInput());
			inputThread.start();
		}
		
		public boolean write(XMLObject obj) {
			return outputQueue.write(obj);
		}
		
		private void doInput() {
			try {
				XMLTokenizer xt = new XMLTokenizer(connection.getInputStream());
				if (xt.next() != XMLTokenType.OPEN || !xt.getCurrentName().equals("UserResponses") ||
					xt.next() != XMLTokenType.ATTR || !xt.getCurrentName().equals("user")) {
					outputQueue.write(new Error("Malformed/Illegal request response."));
					throw new IOException("Initial client response illegal.");
				}
				if (users.find(xt.getCurrentText()) != null) {
					outputQueue.write(new Error("Duplicate user."));
					throw new IOException("Duplicate user.");
				}
				user = users.get(xt.getCurrentText());
				xt.next();
				for (;;) {
					if (xt.hasNext()) {
						XMLObject obj = fromXML(xt);
						if (obj instanceof Response) {
							SwingUtilities.invokeLater(() -> handleResponse(this, (Response) obj));
						} else {
							throw new IOException("Illegal token received from client " + user.getName());
						}
					}
				}
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
			killClient();
		}
		
		private void killClient() {
			outputQueue.close();
			SwingUtilities.invokeLater(() -> clients.remove(this));
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
		register("Response", () -> { return new Response(users); });
	}
}
