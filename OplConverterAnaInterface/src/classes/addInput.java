package classes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.awt.BorderLayout;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class addInput {

	private JDialog frame;
	private JTable table;
	private DefaultTableModel tModel;
	private mainWindow mainFrame;
	
	JFileChooser InputFileChooser;
	
	private ArrayList<String> elements;


	/**
	 * Create the application.
	 */
	public addInput() {
		initialize();
		
		elements = new ArrayList<String>();
	}
	
	public void showOpenDialog(mainWindow mainFrame) {
		initialize();
		this.mainFrame = mainFrame;
		
		for (String s : elements) tModel.addRow(getObjectFromString(s));
		frame.setLocationRelativeTo(mainFrame);
		frame.setModal(true);
		frame.setVisible(true);
		
		// selection to first index
		if (table.getRowCount() > 0) table.setRowSelectionInterval(0, 0);
	}
	
	public void closeWindow() {
		frame.setVisible(false);
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JDialog(mainFrame);
		frame.setTitle("Dateien die Konvertiert werden sollen");
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent arg0) {}

			@Override
			public void windowClosed(WindowEvent arg0) {}

			@Override
			public void windowClosing(WindowEvent arg0) {
				closeWindow();
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {}

			@Override
			public void windowDeiconified(WindowEvent arg0) {}

			@Override
			public void windowIconified(WindowEvent arg0) {}

			@Override
			public void windowOpened(WindowEvent arg0) {}
			
		});
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		tModel = new DefaultTableModel(0, 1);
		table = new JTable(tModel);
		table.setTableHeader(null);
		JScrollPane tableScroller = new JScrollPane(table);
		frame.getContentPane().add(tableScroller, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		InputFileChooser = new JFileChooser();
		JButton btnDurchsuchen = new JButton("Durchsuchen...");
		btnDurchsuchen.setHorizontalAlignment(SwingConstants.RIGHT);
		btnDurchsuchen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				InputFileChooser.setMultiSelectionEnabled(true);
				InputFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				
				int selectedRow = table.getSelectedRow();
				if (selectedRow != -1) {
					File selectedFile = new File((String) table.getValueAt(selectedRow, 0));
					if (selectedFile.exists()) {
						if (selectedFile.isDirectory()) InputFileChooser.setCurrentDirectory(selectedFile);
						else InputFileChooser.setCurrentDirectory(selectedFile.getParentFile());
					}
				}
				
				InputFileChooser.showOpenDialog(frame);
				File[] sFiles = InputFileChooser.getSelectedFiles();
				
				if (sFiles != null && sFiles.length != 0) {
					for (File f : sFiles) {
						updateElements();
						if (elements.indexOf(f.getPath()) == -1) { 
							tModel.addRow(getObjectFromString(f.getPath()));
						} else elementAlreadyExists();
					}
				}
			}
		});
		panel.add(btnDurchsuchen, BorderLayout.EAST);
		
		JButton btnEntfernen = new JButton("Entfernen");
		btnEntfernen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int[] selectedRows = table.getSelectedRows();
				
				if (selectedRows != null && selectedRows.length != 0) {
					if (table.isEditing()) table.getCellEditor().cancelCellEditing();
					for (int i = 0; i < selectedRows.length; i++) {
						int index = selectedRows[i] - i;
						tModel.removeRow(index);
					}
					
				} else JOptionPane.showMessageDialog(frame, "Mindestens 1 Objekt muss ausgewählt sein!", "Fehler!", JOptionPane.ERROR_MESSAGE);
				
				// setze die ausgewählte zeile auf diejenige wo der aktuelle selected index ist bzw. ans ende der liste
				if (selectedRows[0] < table.getRowCount()) {
					table.setRowSelectionInterval(selectedRows[0], 0);
				} else if (table.getRowCount() > 0) table.setRowSelectionInterval(table.getRowCount() - 1, 0);
			}
		});
		
		JPanel newElement = new JPanel();
		panel.add(newElement, BorderLayout.CENTER);
		newElement.setLayout(new BorderLayout(0, 0));
		
		JButton btnAdd = new JButton("Neues Element");
		newElement.add(btnAdd);
		
		JPanel upDownButtons = new JPanel();
		newElement.add(upDownButtons, BorderLayout.EAST);
		upDownButtons.setLayout(new BorderLayout(0, 0));
		
		JButton button = new BasicArrowButton(SwingConstants.NORTH);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getSelectedRow() != -1) {
					int selected = table.getSelectedRow();
					
					if (selected == 0) {
						switchRow(table, 0, table.getRowCount()-1);
						table.changeSelection(table.getRowCount()-1, 0, false, false);
					}
					else {
						switchRow(table, table.getSelectedRow(), selected-1);
						table.changeSelection(selected-1, 0, false, false);
					}
				}
			}
		});
		upDownButtons.add(button, BorderLayout.NORTH);
		
		JButton button_1 = new BasicArrowButton(SwingConstants.SOUTH);
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (table.getSelectedRow() != -1) {
					int selected = table.getSelectedRow();
					
					if (selected == table.getRowCount()-1) {
						switchRow(table, 0, table.getRowCount()-1);
						table.changeSelection(0, 0, false, false);
					}
					else {
						switchRow(table, selected, selected+1);
						table.changeSelection(selected + 1, 0, false, false);
					}
				}
			}
		});
		upDownButtons.add(button_1, BorderLayout.SOUTH);
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateElements();
				if (elements.indexOf("") == -1) {
					tModel.addRow(getObjectFromString(""));
				} else elementAlreadyExists();
			}
		});
		panel.add(btnEntfernen, BorderLayout.WEST);
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateElements();
				File[] files = getFilesFromElements();
				for (int i = 0; i < files.length; i++) {
					File f = files[i];
					
					if (f.exists()) {
						if (f.isDirectory()) {
							int answer = JOptionPane.showConfirmDialog(mainFrame, "Eine der eingegebenen Dateien ist ein Ordner (" + f.getPath() + "). Sollen all seine Unterdateien importiert werden?", "Ordner Problem", JOptionPane.YES_NO_OPTION);
							
							if (answer == JOptionPane.YES_OPTION) {
								ArrayList<File> subFiles = getSubFiles(f);
								
								for (File subFile : subFiles) {
									if (!subFile.isDirectory() && elements.indexOf(subFile.getPath()) == -1) elements.add(subFile.getPath());
								}
								elements.set(i, ""); // wenn das element leer gemacht wird ist es als würde man es löschen, da es später ignoriert wird
							}
						}
					} else if (f.getPath() != "") {
						int answer = JOptionPane.showConfirmDialog(mainFrame, "Eine der eingegebenen Dateien existiert nicht! (" + f + "). Soll die Datei aus der Liste gelöscht werden?", "Fehler!", JOptionPane.YES_NO_OPTION);
						if (answer==JOptionPane.YES_OPTION) {
							elements.set(i, "");
						} else return;
					}
				}
				
				mainFrame.updateInput();
				closeWindow();
			}
		});
		panel.add(btnOk, BorderLayout.SOUTH);
	}
	
	private static void switchRow(JTable table, int index1, int index2) {
		TableModel tableModel = table.getModel();
		
		Object obj = tableModel.getValueAt(index2, 0);
		tableModel.setValueAt(tableModel.getValueAt(index1, 0), index2, 0);
		tableModel.setValueAt(obj, index1, 0);
	}
	
	private void elementAlreadyExists() {
		JOptionPane.showMessageDialog(frame, "Das eingegebene Element wurde bereits hinzugefügt.", "Fehler!", JOptionPane.ERROR_MESSAGE);
	}
	
	private ArrayList<File> getSubFiles(File file) {
		ArrayList<File> files = new ArrayList<File>();
		
		for (File f : file.listFiles()) {
			if (f.isDirectory()) {
					files.addAll(getSubFiles(f));
			} else files.add(f);
		}
		
		return files;
	}
	
	private Object[] getObjectFromString(String s) {
		return new Object[] {s};
	}
	
	private void updateElements() {
		elements = new ArrayList<String>();
		
		for (int i = 0; i < tModel.getRowCount(); i++) elements.add((String) tModel.getValueAt(i, 0));
	}
	
	public File[] getFilesFromElements() {
		File[] files = new File[elements.size()];
		
		for (int i = 0; i < files.length; i++) files[i] = new File(elements.get(i));
		
		return files;
	}
	
	public void setElements(ArrayList<String> elements) {
		this.elements = elements;
	}
	
	public ArrayList<String> getElements() {
		return elements;
	}
	
}
