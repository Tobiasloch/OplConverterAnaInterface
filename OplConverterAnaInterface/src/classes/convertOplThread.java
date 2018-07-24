package classes;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class convertOplThread extends Thread implements Runnable  {

	private convertOPL converter;
	private JFrame mainFrame;
	private int errorlevel;
	
	private Console console;
	
	convertOplThread(convertOPL converter, JFrame mainFrame) {
		this.converter = converter;
	}
	
	@Override
	public void run() {
		setErrorlevel(converter.convertToTextFile());
		
		if (errorlevel == 0) {
			console.printConsoleLine("Die Datei wurde erfolgreich umgewandelt!");
			JOptionPane.showMessageDialog(mainFrame, "Die Datei wurde erfolgreich umgewandelt!");
		} else {
			console.printConsoleErrorLine("Der Vorgang wurde abgebrochen!", errorlevel);
			JOptionPane.showMessageDialog(mainFrame, "Der Vorgang wurde abgebrochen! errorcode:" + errorlevel
					, "Fehler!", JOptionPane.ERROR_MESSAGE);
		}
		
	}

	public int getErrorlevel() {
		return errorlevel;
	}

	public void setErrorlevel(int errorlevel) {
		this.errorlevel = errorlevel;
	}

	public convertOPL getConverter() {
		return converter;
	}

	public void setConverter(convertOPL converter) {
		this.converter = converter;
	}

	public JFrame getMainFrame() {
		return mainFrame;
	}

	public void setMainFrame(JFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public Console getConsole() {
		return console;
	}

	public void setConsole(Console console) {
		this.console = console;
	}

}
