package classes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

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
		setErrorlevel(converter.convertToStream());
		
		if (errorlevel == 0) {
			console.printConsoleLine("Die Datei wurde erfolgreich umgewandelt!");
			JOptionPane.showMessageDialog(mainFrame, "Die Datei wurde erfolgreich umgewandelt!");
		} else {
			console.printConsoleErrorLine("Der Vorgang wurde abgebrochen!", errorlevel);
			JOptionPane.showMessageDialog(mainFrame, "Der Vorgang wurde abgebrochen! errorcode:" + errorlevel
					, "Fehler!", JOptionPane.ERROR_MESSAGE);
		}
		
		File f = converter.getHeader().getOplFile();
		File fOut = new File(f.getParentFile().getPath() + "\\output stream.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(converter.getInputStream()));
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(fOut);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (fw == null) throw new IllegalArgumentException();
		
		BufferedWriter bw = new BufferedWriter(fw);
		try {
			for (OplType t : converter.getHeader().getTypes()) {
				bw.write(t.getType() + ";");
			}
			bw.write("\n");
			
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				bw.write(line + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			br.close();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
