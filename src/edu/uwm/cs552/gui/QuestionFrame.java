package edu.uwm.cs552.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.uwm.cs.util.MultiSelectionModel;
import edu.uwm.cs.util.SingleSelectionModel;
import edu.uwm.cs.util.XMLTokenizer;
import edu.uwm.cs.util.XMLWriter;
import edu.uwm.cs552.ChoiceQuestion;
import edu.uwm.cs552.FreeResponseQuestion;
import edu.uwm.cs552.Question;
import edu.uwm.cs552.Script;
import edu.uwm.cs552.User;
import edu.uwm.cs552.XMLObject;
import edu.uwm.cs552.XMLObject.ParseException;
import edu.uwm.cs552.net.NetworkResponseLog;

public class QuestionFrame extends JFrame {
	/**
	 * KEH
	 */
	private static final long serialVersionUID = 1L;

	private Script script = new Script();
	private Question question;
	private JPanel contentPane;
	private QuestionPanel questionPane;
	private SingleSelectionModel questionSelectionModel = new SingleSelectionModel();
	private MultiSelectionModel choiceSelectionModel = new MultiSelectionModel();
	private User.Users allusers = new User.Users();
	private UsersView usersView = new UsersView(allusers);
	private NetworkResponseLog responseLog = new NetworkResponseLog();
	private MasterQuestionDialog masterDialog = new MasterQuestionDialog(responseLog);
	
	public QuestionFrame() {
		super("Question Editor");
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		setContentPane(contentPane);
		createButtons();
	    createMenus();
	    final JScrollPane scrollpane = new JScrollPane(new PageSelectionPanel(script,questionSelectionModel));
	    scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	    scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	    contentPane.add(scrollpane,BorderLayout.WEST);
	    questionSelectionModel.addObserver((ob,obj) -> {
	    	if (ob == questionSelectionModel) makeContentPane();
	    });
	    usersView.setSize(200,200);
	    masterDialog.setLocationRelativeTo(this);
	    masterDialog.setSize(500,300);
	    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
		      @Override
		      public void windowClosing(WindowEvent e) {
		        doQuit();
		      }
		});
	}
	
	private void makeContentPane() {
		int current = questionSelectionModel.getSelected();
		question = current == 0 ? null : script.getAtIndex(current);
		QuestionPanel content = QuestionPanel.create(question,choiceSelectionModel, true);
		choiceSelectionModel.clearSelection();
		if (questionPane != null) {
			questionPane.dispose();
			contentPane.remove(questionPane);
		}
		questionPane = content;
		if (questionPane != null) {
			contentPane.add(content,BorderLayout.CENTER);
		}
		contentPane.revalidate();
		contentPane.repaint();
	}

	JButton upButton = new JButton("\u21D1");
	JButton removeButton = new JButton("-");
	JButton downButton = new JButton("\u21D3");
	JButton addButton = new JButton("+");
	
	private void enableButtons() {
		addButton.setEnabled(question instanceof ChoiceQuestion);
		boolean anything = choiceSelectionModel.numSelected() > 0;
		upButton.setEnabled(choiceSelectionModel.getSelected() > 1);
		downButton.setEnabled(anything && choiceSelectionModel.getSelected() < ((ChoiceQuestion)question).numChoices());
		removeButton.setEnabled(anything);
	}

	{
		choiceSelectionModel.addObserver((o,obj) -> {
			enableButtons();
		});
	}
	
	protected void createButtons() {
		JPanel rightButtons = new JPanel();
		rightButtons.setLayout(new BoxLayout(rightButtons, BoxLayout.Y_AXIS));
		rightButtons.add(Box.createVerticalGlue());
		rightButtons.add(upButton);
		rightButtons.add(removeButton);
		rightButtons.add(downButton);
		rightButtons.add(Box.createVerticalGlue());
		rightButtons.add(addButton);
		contentPane.add(rightButtons,BorderLayout.EAST);
				
		enableButtons();
		
		upButton.addActionListener((e)-> {
			doUp();
		});
		downButton.addActionListener((e)-> {
			doDown();
		});
		addButton.addActionListener((e)-> {
			doAdd();
		});
		removeButton.addActionListener((e)-> {
			doRemove();
		});
	}
	
	private void doAdd() {
		ChoiceQuestion cq = (ChoiceQuestion)question;
		cq.addChoice("");
		choiceSelectionModel.setSelected(cq.numChoices());
	}
	
	private void doRemove() {
		int index = choiceSelectionModel.getSelected();
		if (index == 0) {
			JOptionPane.showMessageDialog(this, "No choice selected", "Remove Error", JOptionPane.ERROR_MESSAGE);
		} else {
			((ChoiceQuestion)question).removeChoice(index);
			choiceSelectionModel.clearSelection();
		}
	}
	
	private void doUp() {
		int index = choiceSelectionModel.getSelected();
		if (index == 0) {
			JOptionPane.showMessageDialog(this, "No choice selected", "Move Up Error", JOptionPane.ERROR_MESSAGE);
		} else if (index == 1) {
			JOptionPane.showMessageDialog(this, "Already at top", "Move Up Error", JOptionPane.ERROR_MESSAGE);
		} else {
			((ChoiceQuestion)question).moveUp(index);
			choiceSelectionModel.setSelected(index-1);
		}
	}
	
	private void doDown() {
		final ChoiceQuestion choiceQuestion = (ChoiceQuestion)question;
		int index = choiceSelectionModel.getSelected();
		if (index == 0) {
			JOptionPane.showMessageDialog(this, "No choice selected", "Move Down Error", JOptionPane.ERROR_MESSAGE);
		} else if (index == choiceQuestion.numChoices()) {
			JOptionPane.showMessageDialog(this, "Already at bottom", "Move Down Error", JOptionPane.ERROR_MESSAGE);
		} else {
			choiceQuestion.moveDown(index);
			choiceSelectionModel.setSelected(index+1);
		}
	}
	
	protected void createMenus() {
	    JMenuBar mb = new JMenuBar();
	    this.setJMenuBar(mb);
	    JMenu fileMenu = new JMenu("File");
	    mb.add(fileMenu);
	    JMenuItem newItem = new JMenuItem("New");
	    JMenuItem openItem = new JMenuItem("Open ...");
	    JMenuItem saveItem = new JMenuItem("Save");
	    JMenuItem saveAsItem = new JMenuItem("Save As ...");
	    JMenuItem revertItem = new JMenuItem("Revert");
	    JMenuItem quitItem = new JMenuItem("Quit");
	    JMenu editMenu = new JMenu("Edit");
	    mb.add(editMenu);
	    JMenuItem cutItem = new JMenuItem("Cut");
	    JMenuItem copyItem = new JMenuItem("Copy");
	    JMenuItem pasteItem = new JMenuItem("Paste");
	    JMenuItem deleteItem = new JMenuItem("Delete");
	    JMenuItem addMenu = new JMenu("New Question");
	    JMenuItem newChoiceItem = new JMenuItem("Choice Question");
	    JMenuItem newFreeItem = new JMenuItem("Free Response Question");
	    addMenu.add(newChoiceItem);
	    addMenu.add(newFreeItem);
	    fileMenu.add(newItem);
	    fileMenu.add(openItem);
	    fileMenu.addSeparator();
	    fileMenu.add(saveItem);
	    fileMenu.add(saveAsItem);
	    fileMenu.add(revertItem);
	    fileMenu.addSeparator();
	    fileMenu.add(quitItem);
	    editMenu.add(cutItem);
	    editMenu.add(copyItem);
	    editMenu.add(pasteItem);
	    editMenu.addSeparator();
	    editMenu.add(addMenu);
	    editMenu.add(deleteItem);
	    JMenu sessionMenu = new JMenu("Session");
	    JMenuItem newLogItem = new JMenuItem("New Log");
	    JMenuItem readUsersItem = new JMenuItem("Read Users");
	    JMenuItem addUserItem = new JMenuItem("Add User");
	    JMenuItem askItem = new JMenuItem("Ask Users");
	    JMenuItem writeLogItem = new JMenuItem("Write Log");
	    mb.add(sessionMenu);
	    sessionMenu.add(newLogItem);
	    sessionMenu.add(readUsersItem);
	    sessionMenu.add(addUserItem);
	    sessionMenu.addSeparator();
	    sessionMenu.add(askItem);
	    sessionMenu.addSeparator();
	    sessionMenu.add(writeLogItem);
	    newItem.addActionListener((ae) -> {
	    	doNew();
	    });
	    openItem.addActionListener((ActionEvent ae) -> {
	    	doOpen();
	    });
	    saveItem.addActionListener((ActionEvent ae) -> {
	      doSave();
	    });
	    saveAsItem.addActionListener((ActionEvent ae) -> {
	      doSaveAs();
	    });
	    revertItem.addActionListener((ActionEvent ae) -> {
	      doRevert();
	    });
	    quitItem.addActionListener((ActionEvent ae) -> {
	      doQuit();
	    });
	    cutItem.addActionListener((ActionEvent ae) -> {
		      doCut();
		    });
	    copyItem.addActionListener((ActionEvent ae) -> {
		      doCopy();
		    });
	    pasteItem.addActionListener((ActionEvent ae) -> {
		      doPaste();
		    });
	    newChoiceItem.addActionListener((ActionEvent ae) -> {
	    	doAdd(new ChoiceQuestion());
	    });
	    newFreeItem.addActionListener((ActionEvent ae) -> {
	    	doAdd(new FreeResponseQuestion());
	    });
	    deleteItem.addActionListener((ActionEvent ae) -> {
		      doDelete();
		    });
	    
	    newLogItem.addActionListener((ae) -> doNewLog());
	    readUsersItem.addActionListener((ae) -> doReadUsers());
	    addUserItem.addActionListener((ae) -> doAddUser());
	    askItem.addActionListener((ae) -> doAskQuestion());
	    writeLogItem.addActionListener((ae) -> doWriteLog());
	}
	
	  private JFileChooser myFileChooser = new JFileChooser();
	  private File myFile = null;
	  private boolean dirty = false;
	  
	  {
		  script.addObserver((o,obj) -> {
			  dirty = true;
		  });
	  }
	  
	  protected boolean okToOperate(String opName) {
		  if (!dirty) return true;
		  int result = JOptionPane.showConfirmDialog(this, "OK to "+opName + " without saving?", opName, JOptionPane.YES_NO_OPTION);
		  return result == JOptionPane.YES_OPTION;
	  }

	  /**
	   * Clear the file section in the chooser.
	   * Unfortunately {@link JFileChooser#setSelectedFile(File)} with <code>null</code>
	   * has no effect.  I attempted to use
	   * http://stackoverflow.com/questions/12736880/clear-jfilechooser-selection-after-adding-files-to-a-jlist
	   * to no avail.
	   */
	  protected void clearFileSelection() {
		  final File currentDir = myFileChooser.getCurrentDirectory();
		  myFileChooser.setSelectedFile(new File(""));
		  myFileChooser.setCurrentDirectory(currentDir);
	  }

	  private void doNew() {
		  if (!okToOperate("Start Over")) return;
		  script.clear();
		  questionSelectionModel.clearSelection();
	  }

	  private void doOpen() {
		  if (!okToOperate("Use New File")) return;
		  switch (myFileChooser.showOpenDialog(this)) {
		  case JFileChooser.APPROVE_OPTION:
			  break;
		  case JFileChooser.CANCEL_OPTION:
		  default:
			  return;
		  }
		  File f = myFileChooser.getSelectedFile();
		  readFromFile(f);
	  }

	  /**
	   * Read a script from a file.
	   * @param f
	   */
	  protected void readFromFile(File f) {
		  List<Question> questions = null;
		  try {
			  InputStream is = new BufferedInputStream(new FileInputStream(f));
			  XMLTokenizer xt = new XMLTokenizer(is);
			  XMLObject obj = XMLObject.fromXML(xt);
			  if (obj instanceof Question) questions = Collections.singletonList((Question)obj);
			  else questions = ((Script)obj).clear();
		  } catch (FileNotFoundException|ParseException e) {
			  JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Open Error", JOptionPane.ERROR_MESSAGE);
		  } catch (ClassCastException e) {
			  JOptionPane.showMessageDialog(this,"File contents not a question","Open Error",JOptionPane.ERROR_MESSAGE);
		  }
		  if (questions != null) {
			  script.setAll(questions);
			  myFile = f;
			  dirty = false;
			  if (questions.size() > 0) questionSelectionModel.setSelected(1);
		  }
	  }

	  private void writeToFile(File f) {
		  try {
			  OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
			  XMLWriter xw = new XMLWriter(os);
			  if (script.size() == 1) {
				  script.getAtIndex(1).toXML(xw);
			  } else {
				  script.toXML(xw);
			  }
			  xw.close();
			  myFile = f;
			  dirty = false;
		  } catch (Exception e) {
			  JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
		  }
	  }

	  private void doSave() {
		  if (myFile == null) doSaveAs();
		  else {
			  writeToFile(myFile);
		  }
	  }

	  private void doSaveAs() {
		  switch (myFileChooser.showSaveDialog(this)) {
		  case JFileChooser.APPROVE_OPTION:
			  break;
		  case JFileChooser.CANCEL_OPTION:
		  default:
			  return;
		  }
		  final File file = myFileChooser.getSelectedFile();
		  writeToFile(file);
	  }

	  private void doRevert() {
		  if (!okToOperate("Revert")) return;
		  readFromFile(myFile);
	  }
	  
	  private void doQuit() {
		  if (!okToOperate("Quit")) return;
		  if (!responseLog.isEmpty()) {
			  switch (JOptionPane.showConfirmDialog(this, "Write log before quitting?", "Quit Check", JOptionPane.YES_NO_CANCEL_OPTION)) {
			  case JOptionPane.YES_OPTION:
				  doWriteLog();
				  if (!responseLog.isEmpty()) return; // write failed.
				  break;
			  case JOptionPane.NO_OPTION:
				  break;
			  case JOptionPane.CANCEL_OPTION:
			  default:
				  return;
			  }
		  }
		  System.exit(0);
	  }
	  
	  private void doCut() {		 
		  JOptionPane.showMessageDialog(this, "'Cut' Not implemented", "Implementation Deficiency", JOptionPane.ERROR_MESSAGE);
	  }
	  
	  private void doCopy() {
		  JOptionPane.showMessageDialog(this, "'Copy' Not implemented", "Implementation Deficiency", JOptionPane.ERROR_MESSAGE);		  
	  }
	  
	  private void doPaste() {
		  JOptionPane.showMessageDialog(this, "'Paste' Not implemented", "Implementation Deficiency", JOptionPane.ERROR_MESSAGE);		  
	  }
	  
	  private void doDelete() {
		  int current = questionSelectionModel.getSelected();
		  if (current == 0) {
			  JOptionPane.showMessageDialog(this, "Nothing to delete", "Delete Error", JOptionPane.ERROR_MESSAGE);
		  } else {
			  script.removeAtIndex(current);
			  if (current > script.size()) {
				  questionSelectionModel.setSelected(script.size());
			  }
			  makeContentPane();
		  }
	  }
	  
	  private void doAdd(Question q) {
		  int current = questionSelectionModel.getSelected();
		  script.addAtIndex(q, current+1);
		  questionSelectionModel.setSelected(current+1);
	  }

	  private void doNewLog() {
		  responseLog.clear();
	  }
	  
	  private void doReadUsers() {
		  switch (myFileChooser.showOpenDialog(this)) {
		  case JFileChooser.APPROVE_OPTION:
			  break;
		  case JFileChooser.CANCEL_OPTION:
		  default:
			  return;
		  }
		  try {
			  File usersFile = myFileChooser.getSelectedFile();
			  BufferedReader br = new BufferedReader(new FileReader(usersFile));
			  String name;
			  while ((name = br.readLine()) != null) {
				  allusers.get(name);
			  }
			  br.close();
		  } catch (IOException e) {
			  JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Read Users Error", JOptionPane.ERROR_MESSAGE);
			  return;
		  }
		  usersView.setVisible(true);
	  }
	  
	  private void doAddUser() {
		  String userName = JOptionPane.showInputDialog(this, "User name (without @uwm.edu)");
		  if (userName != null) {
			  allusers.get(userName);
		  }
		  usersView.setVisible(true);
	  }
	  
	  private void doAskQuestion() {
		  masterDialog.setQuestion(question);
		  masterDialog.setVisible(true);
	  }
	  
	  private JFileChooser sessionChooser;
	  private void doWriteLog() {
		  if (sessionChooser == null) {
			  sessionChooser = new JFileChooser();
			  sessionChooser.setCurrentDirectory(myFileChooser.getCurrentDirectory());
		  }
		  switch (sessionChooser.showSaveDialog(this)) {
		  case JFileChooser.APPROVE_OPTION:
			  break;
		  case JFileChooser.CANCEL_OPTION:
		  default:
			  return;
		  }
		  try {
			  File logFile = sessionChooser.getSelectedFile();
			  OutputStream os = new FileOutputStream(logFile);
			  XMLWriter xw = new XMLWriter(os);
			  responseLog.toXML(xw);
			  xw.close();
		  } catch (IOException e) {
			  JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Write Log Error", JOptionPane.ERROR_MESSAGE);
			  return;
		  }
		  responseLog.clear();
	  }

	public static void main(String[] args) {
		  SwingUtilities.invokeLater(() -> {
			  JFrame f = new QuestionFrame();
			  f.setSize(500,300);
			  f.setVisible(true);
		  });
	  }
}
