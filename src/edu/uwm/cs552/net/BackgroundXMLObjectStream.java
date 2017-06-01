package edu.uwm.cs552.net;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import edu.uwm.cs.util.XMLWriter;
import edu.uwm.cs552.XMLObject;

/**
 * A concurrent ADT that allows us to send the sub elements of an XML object
 * (presumably the top-level element) concurrently.
 */
public class BackgroundXMLObjectStream {
	
	final int QUEUE_CAPACITY = 10;
	
	private final XMLWriter xw;
	private final String name;
	private final Map<String, String> attr;
	private final Thread backgroundThread;
	private final BlockingQueue<XMLObject> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
	
	/**
	 * Create the object with the given top-level element name and attributes.
	 * (The elements will be added later.) The given XML writer will be used to write everything, 
	 * but only in the background thread (that the constructor will create and start)
	 * @param xw
	 * @param name
	 * @param attr
	 */
	public BackgroundXMLObjectStream(XMLWriter xw, String name, Map<String, String> attr) {
		this.xw = xw;
		this.name = name;
		this.attr = attr;
		backgroundThread = new Thread(() -> doWrite());
		backgroundThread.start();
	}
	
	/**
	 * Arrange the given XML object to be written as a nested element of the top-level element, 
	 * with the writing happening on the background thread. Return false if the backlog is 
	 * too great to perform the action.
	 * @param obj
	 * @return
	 */
	public boolean write(XMLObject obj) {
		return queue.offer(obj);
	}
	
	/**
	 * Indicate that no more elements will be added to the top-level element. 
	 * Arrange that the XML writer is closed afterwards. Return false if the 
	 * backlog is too great to perform this action.
	 * @return
	 */
	public boolean close() {
		return queue.offer(CLOSE_TOKEN);
	}
	
	/**
	 * Background threads job.
	 */
	private void doWrite() {
		try {
			xw.writeElementStart(name);
			for (Entry<String, String> e : attr.entrySet()) {
				xw.writeAttr(e.getKey(), e.getValue());
			}
			xw.flush();
			for (;;) {
				XMLObject obj = queue.take();
				if (obj == CLOSE_TOKEN) break;
				obj.toXML(xw);
				xw.flush();
			}
			xw.writeElementDone(name);
			xw.flush();
			xw.close();
		} catch (IOException |InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static final XMLObject CLOSE_TOKEN = new XMLObject() {
		@Override
		protected String getXMLelementName() {
			return "CLOSE_TOKEN";
		}
	};
}
