package classes;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import javax.swing.JButton;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.awt.event.ActionEvent;
import java.awt.Color;

import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.Rectangle;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import java.awt.Component;
import javax.swing.ListSelectionModel;
import javax.swing.JTabbedPane;
import javax.swing.JList;
import java.awt.GridLayout;

@SuppressWarnings("serial")
public class mainWindow extends JFrame {

	/* errorcodes 1 - 99
	 * 0: no error
	 * 
	 * 1: file input error
	 * 2: already running process
	 * 3: information error
	 * 
	 * */
	
	public static final String DEFAULT_OUTPUT_NAME = "output.txt";
	public static final String DATE_FORMAT_INFO = "http://www.sdfonlinetester.info/";
	
	private JPanel contentPane;
	private static JTextField outputField;
	
	private JButton startButton;
	
	private JCheckBox outputinInputFolder;
	
	private JTextArea consoleArea;
	private JScrollPane consoleSP;
	private Console console;
	
	private JCheckBox chckbxTypenInReihenkpfe;
	
	private JTable table;
	private DefaultTableModel tableModel;
	
	// welche delimiter verwendet werden sollen
	private JRadioButton rdbtnTabstop;
	private JRadioButton rdbtnComma;
	private JRadioButton rdbtnSpace;
	private JRadioButton rdbtnSemicolon;
	private JRadioButton rdbtnAndere;
	
	// conflict behavior radio buttons
	private JRadioButton rdbtnTerminateProcess;
	private JRadioButton rdbtnNULLValue;
	private JRadioButton rdbtnSameVariablesOnly;
	
	OplHeader header = new OplHeader();
	convertOPL oplConverter = new convertOPL();
	
	private mainWindow mainFrame = this;
	convertOplThread thread = new convertOplThread(oplConverter, mainFrame);
	private JTextField textField;
	private JTextField dateFormatField;
	
	JList<String> inputList;
	addInput addInput;
	private JTextField NullValue;
	
	public mainWindow() {
		setTitle("OPL Converter");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		setMinimumSize(new Dimension(500, 500));
		setLocationRelativeTo(null);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		DefaultListModel<String> model = new DefaultListModel<String>();
		inputList = new JList<String>(model);
		inputList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		inputList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JPanel inputOutputArea = new JPanel();
		splitPane.setLeftComponent(inputOutputArea);
		inputOutputArea.setLayout(new BorderLayout(0, 0));
		
		JSplitPane inputOutputSplit = new JSplitPane();
		inputOutputSplit.setContinuousLayout(true);
		inputOutputSplit.setResizeWeight(0.5);
		inputOutputArea.add(inputOutputSplit, BorderLayout.CENTER);
		
		JPanel outputPanel = new JPanel();
		inputOutputSplit.setRightComponent(outputPanel);
		outputPanel.setBackground(Color.WHITE);
		outputPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel mainOutputPanel = new JPanel();
		mainOutputPanel.setBackground(Color.WHITE);
		outputPanel.add(mainOutputPanel, BorderLayout.NORTH);
		mainOutputPanel.setLayout(new BorderLayout(0, 0));
		
		JButton button = new JButton("durchsuchen...");
		mainOutputPanel.add(button, BorderLayout.EAST);
		
		JLabel lblOutput = new JLabel(" Output:");
		mainOutputPanel.add(lblOutput, BorderLayout.NORTH);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				File f = new File(outputField.getText());
				
				if (f.getParentFile() != null) fc.setCurrentDirectory(f.getParentFile());
				else if (f.isDirectory()) fc.setCurrentDirectory(f);
				
				fc.showSaveDialog(mainFrame);
				if (fc.getSelectedFile() != null) outputField.setText(fc.getSelectedFile().getPath());
			}
		});
		
		JPanel inputPanel = new JPanel();
		inputOutputSplit.setLeftComponent(inputPanel);
		inputPanel.setBackground(Color.WHITE);
		inputPanel.setLayout(new BorderLayout(0, 0));
		
		outputField = new JTextField();
		outputField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {changed();}
			@Override
			public void insertUpdate(DocumentEvent arg0) {changed();}
			@Override
			public void removeUpdate(DocumentEvent arg0) {changed();}
			
			private void changed() {
				outputinInputFolder.setSelected(false);
			}
		});
		mainOutputPanel.add(outputField, BorderLayout.CENTER);
		
		outputinInputFolder = new JCheckBox("selber Ordner wie ausgew\u00E4hlte Datei");
		outputinInputFolder.setBackground(Color.WHITE);
		outputinInputFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (outputinInputFolder.isSelected() && inputList.getSelectedIndex() >= 0) {
					File f = new File(inputList.getSelectedValue());
					
					if (f.exists() && f.getParentFile() != null) outputField.setText(f.getParentFile().getAbsolutePath() + "\\" + DEFAULT_OUTPUT_NAME);
				}
			}
		});
		outputPanel.add(outputinInputFolder, BorderLayout.SOUTH);
		
		JLabel lblInput = new JLabel(" Input:");
		inputPanel.add(lblInput, BorderLayout.NORTH);
		
		JPanel panel_1 = new JPanel();
		inputPanel.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new GridLayout(0, 2, 0, 0));
		
		JButton btnTabelleRendern = new JButton("Tabelle rendern");
		panel_1.add(btnTabelleRendern);
		btnTabelleRendern.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// erstelle file array und checke ob die dateien existieren
				File[] files = new File[inputList.getModel().getSize()];
				String[] paths = getListElements(inputList);
				
				for (int i = 0; i < files.length; i++) {
					files[i] = new File(paths[i]);
				}
				
				DefaultTableModel tableModel = new DefaultTableModel();
				table.setModel(tableModel);
				
				header = new OplHeader(files[0], console);
				// den conflict Typ bestimmen
				header.setConflictHandling(getConflictHandling());
	
				int errorcode = header.extractHeaderInformation();
				
				if (errorcode != 0) {
					console.printConsoleErrorLine("Es gab ein Problem beim Tabellen rendern!", errorcode);
					return;
				}
				
				for (OplType item : header.getTypes()) {
					
					if (!chckbxTypenInReihenkpfe.isSelected()) tableModel.addColumn(item.getType());
					
					if (item.getElements().size() > 0) {
						int activeCol = tableModel.getColumnCount()-1;
						
						int activeRow = 0;
						for (OplTypeElement elem : item.getElements()) {
							String text = elem.getName() + "(" + elem.getId() + ")";
							
							if (activeRow >= tableModel.getRowCount()) {
								String[] s = new String[tableModel.getColumnCount()];
								s[activeCol] = text;
								
								tableModel.addRow(s);
							} else {
								tableModel.setValueAt(text, activeRow, activeCol);
							}
							
							activeRow++;
						}
					}
				}
				table.repaint();
			}
		});
		
		JButton btnDurchsuchen = new JButton("Liste Bearbeiten");
		panel_1.add(btnDurchsuchen);
		btnDurchsuchen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addInput = new addInput();
				ArrayList<String> elementList = new ArrayList<String>(Arrays.asList(getListElements(inputList)));
				
				addInput.setElements(elementList);
				addInput.showOpenDialog(mainFrame);
			}
		});
		
		
		JScrollPane inputListScroller = new JScrollPane(inputList);
		inputPanel.add(inputListScroller, BorderLayout.CENTER);
		
		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(1.0);
		splitPane_1.setContinuousLayout(true);
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setRightComponent(splitPane_1);
		
		JPanel settingsArea = new JPanel();
		splitPane_1.setLeftComponent(settingsArea);
		settingsArea.setLayout(new BorderLayout(5, 5));
		
		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.setContinuousLayout(true);
		splitPane_2.setResizeWeight(1.0);
		splitPane_2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		settingsArea.add(splitPane_2, BorderLayout.CENTER);
		
		JPanel tableRenderer = new JPanel();
		splitPane_2.setLeftComponent(tableRenderer);
		tableRenderer.setLayout(new BorderLayout(0, 0));
		
		tableModel = new DefaultTableModel();
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setRowSelectionAllowed(false);
		table.setCellSelectionEnabled(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setSurrendersFocusOnKeystroke(true);
		table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {}
			@Override
			public void columnRemoved(TableColumnModelEvent e) {}
			
			@Override
			public void columnMoved(TableColumnModelEvent e) {
				/*if (e.getToIndex() != e.getFromIndex()) {
					header.swapTypes(e.getFromIndex(), e.getToIndex());
				}*/
			}
			
			@Override
			public void columnMarginChanged(ChangeEvent e) {}
			
			@Override
			public void columnAdded(TableColumnModelEvent e) {}
		});
		  
		table.getTableHeader().setAutoscrolls(true);
		table.getTableHeader().addMouseMotionListener(new MouseMotionAdapter() { 
		    public void mouseDragged(MouseEvent e) { 
		        Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);    
		        table.scrollRectToVisible(r);
		    } 
		});
		table.setFillsViewportHeight(true);
		
		JScrollPane tableRendererScroller = new JScrollPane(table);
		tableRenderer.add(tableRendererScroller, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		tableRenderer.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel settingsPanel = new JPanel();
		splitPane_2.setRightComponent(settingsPanel);
		settingsPanel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblEinstellungen = new JLabel("Einstellungen:");
		settingsPanel.add(lblEinstellungen, BorderLayout.NORTH);
		
		JSplitPane splitPane_3 = new JSplitPane();
		splitPane_3.setContinuousLayout(true);
		splitPane_3.setResizeWeight(1.0);
		settingsPanel.add(splitPane_3, BorderLayout.CENTER);
		
		ButtonGroup delGroup = new ButtonGroup();
		
		JPanel checkBoxPanel = new JPanel();
		splitPane_3.setRightComponent(checkBoxPanel);
		checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
		
		JPanel delimiterPanel = new JPanel();
		checkBoxPanel.add(delimiterPanel);
		delimiterPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		delimiterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		delimiterPanel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblDelimiter = new JLabel("Delimiter:");
		delimiterPanel.add(lblDelimiter, BorderLayout.NORTH);
		
		JPanel delimiterList = new JPanel();
		delimiterPanel.add(delimiterList, BorderLayout.WEST);
		delimiterList.setLayout(new BoxLayout(delimiterList, BoxLayout.Y_AXIS));
		
		JPanel tabPanel = new JPanel();
		delimiterList.add(tabPanel);
		tabPanel.setLayout(new BorderLayout(0, 0));
		
		rdbtnTabstop = new JRadioButton("Tabstop");
		delimiterList.add(rdbtnTabstop);
		rdbtnTabstop.setAlignmentY(0.0f);
		
		rdbtnSemicolon = new JRadioButton("Semikolon");
		rdbtnSemicolon.setSelected(true);
		rdbtnSemicolon.setAlignmentY(0.0f);
		delimiterList.add(rdbtnSemicolon);
		
		rdbtnComma = new JRadioButton("Komma");
		rdbtnComma.setAlignmentY(0.0f);
		delimiterList.add(rdbtnComma);
		
		rdbtnSpace = new JRadioButton("Leerzeichen");
		rdbtnSpace.setAlignmentY(Component.TOP_ALIGNMENT);
		delimiterList.add(rdbtnSpace);
		
		JPanel othersPanel = new JPanel();
		othersPanel.setAlignmentY(0.0f);
		othersPanel.setAlignmentX(0.0f);
		delimiterList.add(othersPanel);
		othersPanel.setLayout(new BorderLayout(0, 0));
		
		rdbtnAndere = new JRadioButton("andere");
		rdbtnAndere.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (rdbtnAndere.isSelected()) textField.setEnabled(true);
				else textField.setEnabled(false);
			}
		});
		othersPanel.add(rdbtnAndere, BorderLayout.WEST);
		delGroup.add(rdbtnSemicolon);
		delGroup.add(rdbtnComma);
		delGroup.add(rdbtnSpace);
		delGroup.add(rdbtnAndere);
		delGroup.add(rdbtnTabstop);
		
		textField = new JTextField();
		textField.setEnabled(false);
		othersPanel.add(textField, BorderLayout.CENTER);
		textField.setColumns(3);
		
		JLabel lblAndere = new JLabel("Andere:");
		checkBoxPanel.add(lblAndere);
		
		chckbxTypenInReihenkpfe = new JCheckBox("in eine Datei exportieren");
		checkBoxPanel.add(chckbxTypenInReihenkpfe);
		
		JTabbedPane mainSettingsPanel = new JTabbedPane(JTabbedPane.TOP);
		splitPane_3.setLeftComponent(mainSettingsPanel);
		
		JPanel variableInformationPanel = new JPanel();
		mainSettingsPanel.addTab("Variableninformationen", null, variableInformationPanel, null);
		variableInformationPanel.setLayout(new BorderLayout(0, 0));
		
		JTextArea txtrNameId = new JTextArea();
		txtrNameId.setEnabled(false);
		txtrNameId.setEditable(false);
		JScrollPane txtrNameIdScroller = new JScrollPane(txtrNameId);
		txtrNameId.setText("Name:\r\nID:\r\nTyp:\r\n*NOT USED YET*");
		variableInformationPanel.add(txtrNameIdScroller);
		
		JPanel timeStampSettings = new JPanel();
		mainSettingsPanel.addTab("Zeit Stempel", null, timeStampSettings, null);
		timeStampSettings.setLayout(new BorderLayout(0, 0));
		
		JPanel dateFormatPanel = new JPanel();
		timeStampSettings.add(dateFormatPanel, BorderLayout.NORTH);
		dateFormatPanel.setLayout(new BorderLayout(0, 0));
		
		dateFormatField = new JTextField();
		dateFormatField.setText(convertOPL.DEFAULT_DATE_FORMAT);
		dateFormatPanel.add(dateFormatField, BorderLayout.CENTER);
		dateFormatField.setColumns(10);
		
		JLabel lblZeitstempelFormat = new JLabel("Zeitstempel Format:");
		dateFormatPanel.add(lblZeitstempelFormat, BorderLayout.NORTH);
		
		JButton btnNewButton = new JButton("?");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					java.awt.Desktop.getDesktop().browse(new URI(DATE_FORMAT_INFO));
				} catch (IOException e) {
					console.printConsoleErrorLine("Es gab ein Fehler mit der URL über die DateFormat Information!", 3);
					e.printStackTrace();
				} catch (URISyntaxException e) {
					console.printConsoleErrorLine("Es gab ein Fehler mit der URL über die DateFormat Information!", 3);
					e.printStackTrace();
				}
			}
		});
		dateFormatPanel.add(btnNewButton, BorderLayout.EAST);
		
		JPanel headerConflictPanel = new JPanel();
		mainSettingsPanel.addTab("Header Konflikte", null, headerConflictPanel, null);
		headerConflictPanel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblConflictDesctiption = new JLabel("Verhalten beim bearbeiten von unterschiedlichen Headerbl\u00F6cken");
		headerConflictPanel.add(lblConflictDesctiption, BorderLayout.NORTH);
		
		JPanel selectBehaviorPanel = new JPanel();
		headerConflictPanel.add(selectBehaviorPanel, BorderLayout.CENTER);
		selectBehaviorPanel.setLayout(new GridLayout(0, 1));
		
		rdbtnTerminateProcess = new JRadioButton("Vorgang abbrechen");
		rdbtnTerminateProcess.setSelected(true);
		selectBehaviorPanel.add(rdbtnTerminateProcess);
		
		JPanel NullValuePanel = new JPanel();
		selectBehaviorPanel.add(NullValuePanel);
		NullValuePanel.setLayout(new BorderLayout(0, 0));
		
		rdbtnNULLValue = new JRadioButton("Nicht vorhandene Variablen werden folgenderma\u00DFen bef\u00FCllt:");
		NullValuePanel.add(rdbtnNULLValue);
		
		
		NullValue = new JTextField();
		NullValue.setHorizontalAlignment(JTextField.CENTER);
		NullValue.setText(OplHeader.DEFAULT_NULL_VALUE);
		NullValuePanel.add(NullValue, BorderLayout.EAST);
		NullValue.setColumns(5);
		
		rdbtnSameVariablesOnly = new JRadioButton("Nur gemeinsame Variablen werden angezeigt und \u00FCbertragen");
		selectBehaviorPanel.add(rdbtnSameVariablesOnly);
		
		// add radiobuttons to their group
		ButtonGroup behaviorGroup = new ButtonGroup();
		behaviorGroup.add(rdbtnSameVariablesOnly);
		behaviorGroup.add(rdbtnTerminateProcess);
		behaviorGroup.add(rdbtnNULLValue);
		
		mainSettingsPanel.setSelectedIndex(2);
		
		JPanel startArea = new JPanel();
		
		splitPane_1.setRightComponent(startArea);
		startArea.setLayout(new BorderLayout(0, 0));
		
		consoleArea = new JTextArea();
		consoleArea.setEditable(false);
		consoleArea.setRows(4);
		
		consoleSP = new JScrollPane(consoleArea);
		startArea.add(consoleSP, BorderLayout.CENTER);
		
		console = new Console(consoleArea, consoleSP);
		
		startArea.setMinimumSize(new Dimension(mainFrame.getWidth(), consoleArea.getPreferredSize().height));
		startArea.setMaximumSize(new Dimension(mainFrame.getWidth(), 200));
		startArea.setPreferredSize(new Dimension(mainFrame.getWidth(), 100));
		
		JPanel buttonPanel = new JPanel();
		startArea.add(buttonPanel, BorderLayout.EAST);
		buttonPanel.setLayout(new BorderLayout(0, 0));
		
		startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!thread.isAlive()) {
					// checks if the header is read correctly
					if (header.checkErrorStatus() != 0) {
						console.printConsoleErrorLine("Die Headerdatei konnte nicht richtig gelesen werden!", header.checkErrorStatus());
						JOptionPane.showMessageDialog(mainFrame, "Die Headerdatei konnte nicht richtig gelesen werden! errorcode:" + header.checkErrorStatus()
								,"Fehler!"
								, JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					File outputFile = new File(outputField.getText());
					
					// checks if the output file is correct
					if (outputFile.getParent() == null || !outputFile.getParentFile().exists() || outputFile.isDirectory()) {
						console.printConsoleErrorLine("Es gab ein Problem mit der output Datei!", 1);
						JOptionPane.showMessageDialog(mainFrame, "Es gab ein Problem mit der output Datei! errorcode:1"
								,"Fehler!"
								, JOptionPane.ERROR_MESSAGE);
						return;
					}

					if (outputFile.exists()) {
						int option = JOptionPane.showConfirmDialog(mainFrame, "Die output Datei existiert bereits, soll sie überschrieben werden?"
								,"Fehler!"
								, JOptionPane.YES_NO_OPTION);
						
						if (option == JOptionPane.YES_OPTION) {
							outputFile.delete();
							outputFile = new File (outputField.getText());
							console.printConsoleLine("die Datei:\"" + outputFile + "\" wurde überschrieben!");
						} else {
							console.printConsole("Der Vorgang wurde abgebrochen!");
							return;
						}
					}
					// setting up converter
					header.setNullValue(NullValue.getText());
					OutputStream fileStream = new ByteArrayOutputStream();
					oplConverter = new convertOPL(header, fileStream, console, getSelectedDelimiter(), dateFormatField.getText());
					oplConverter.setMainFrame(mainFrame);
					
					thread = new convertOplThread(oplConverter, mainFrame);
					thread.setConsole(console);

					thread.start();
				} else {
					console.printConsoleErrorLine("Es wird bereits eine Datei exportiert!", 2);
					JOptionPane.showMessageDialog(mainFrame, "Es wird bereits eine Datei exportiert! errorcode:" + 2
							, "Fehler!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		buttonPanel.add(startButton);
		
		JButton clearConsole = new JButton ("leere Konsole");
		buttonPanel.add(clearConsole, BorderLayout.SOUTH);
		clearConsole.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				console.clearConsole();
			}
		});
	}
	
	public void updateInput() {
		DefaultListModel<String> InputListModel = (DefaultListModel<String>) (inputList.getModel());
		
		ArrayList<String> str = addInput.getElements();
		InputListModel.removeAllElements();
		
		for (String s : str) if (s!="") InputListModel.addElement(s);
		
		if (inputList.getSelectedIndex() == -1 && InputListModel.size() > 0) inputList.setSelectedIndex(0);
	}
	
	private String getSelectedDelimiter() {
		if (rdbtnTabstop.isSelected()) {
			
			return convertOPL.DELIM_TAB;
		}
		else if (rdbtnComma.isSelected()) return convertOPL.DELIM_COMMA;
		else if (rdbtnAndere.isSelected()) return textField.getText();
		else if (rdbtnSpace.isSelected()) return convertOPL.DELIM_SPACE;
		else if (rdbtnSemicolon.isSelected()) return convertOPL.DELIM_SEMICOLON;
		
		return "";
	}
	
	private static String[] getListElements (JList<String> list) {
		ListModel<String> model = list.getModel();
		
		String[] str = new String[model.getSize()];
		
		for (int i = 0; i < model.getSize(); i++) {
			str[i] = model.getElementAt(i);
		}
		
		return str;
	}
	
	public static File[] convertArrayListToArray (ArrayList<File> f) {
		File[] files = new File[f.size()];
		
		for (int i = 0; i < files.length; i++) files[i] = f.get(i);
		
		return files;
	}
	
	public TableColumn[] getColumnsInView(JTable table) {
	    TableColumn[] result = new TableColumn[table.getColumnCount()];

	    // Use an enumeration
	    Enumeration<TableColumn> e = table.getColumnModel().getColumns();
	    for (int i = 0; e.hasMoreElements(); i++) {
	      result[i] = (TableColumn) e.nextElement();
	    }

	    return result;
	  }
	
	public int getConflictHandling() {
		if (rdbtnTerminateProcess.isSelected()) return 0;
		else if (rdbtnNULLValue.isSelected()) return 1;
		else if (rdbtnSameVariablesOnly.isSelected()) return 2;
		
		return -1;
		
	}
}
