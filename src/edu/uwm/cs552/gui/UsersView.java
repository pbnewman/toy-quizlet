package edu.uwm.cs552.gui;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import edu.uwm.cs552.User;
/**
 * Simple class which displays the registered users
 * in a scrollable JFrame. 
 */
public class UsersView extends JFrame {

	/**
	 * KEH
	 */
	private static final long serialVersionUID = 1L;
	
	public UsersView(JList<User> users) {
		super("Users View");
		setContentPane(new JScrollPane(users));
		setSize(250, 150);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	}

}
