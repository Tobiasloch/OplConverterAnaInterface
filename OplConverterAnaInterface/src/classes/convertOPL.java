package classes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.ProgressMonitor;

public class convertOPL {
	
	/* errorleves: 200-299
	 * 
	 * 0: no error
	 * 
	 * 200: übergebene Referenzen sind nicht vorhanden
	 * 201: der fehlerstatus des headers ist nicht 0
	 * 202: FileNotFoundException
	 * 203: outputFile already exists
	 * 204: outputFile could not be created
	 * 205: id in block element not found
	 * 206: cancelled progress
	 * 
	 * */
	
	private final String BLOCK_HEADER = "(?<time>\\d{10}) \\d{10} (?<SZP>\\d{3}) \\w{3} \\d{10} \\w{3} \\w{3}";
	private final String BLOCK_ELEMENT = "(?<time>\\d{10}) (?<id>\\d{10}) \\w{1} \\w{2} \\w{1} (?<value>\\d{10})";
	
	public static final String DELIM_TAB = "\t";
	public static final String DELIM_COMMA = ",";
	public static final String DELIM_SEMICOLON = ";";
	public static final String DELIM_SPACE = " ";
	
	private JFrame mainFrame;
	
	public static final String DEFAULT_DELIMITER = DELIM_SEMICOLON;
	public static final String DEFAULT_DATE_FORMAT = "dd'.'MM'.'yyyy' 'kk':'mm':'ss";
	
	// for error handling
	String notMatchingLines; // lines that did not match any pattern
	String linesInWrongBlock; // lines that where in the wrong second block
	String idsNotFound;
	
	private ByteArrayOutputStream outputStream;
	private OplHeader header; // header of the opl file
	
	private String delimiter; // Trenn String; wird zwischen jeden wert gepackt
	private SimpleDateFormat dateFormat; // convertiert den Zeitstempel in das angegebene Format
	
	private Console console;
	
	public convertOPL() {
		this(null, null);
	}
	
	public convertOPL(OplHeader header, ByteArrayOutputStream outputStream) {
		this(header, outputStream, new Console());
	}
	
	public convertOPL(OplHeader header, ByteArrayOutputStream outputStream, Console console) {
		this(header, outputStream, console, DEFAULT_DELIMITER, DEFAULT_DATE_FORMAT);
	}
	
	public convertOPL(OplHeader header, ByteArrayOutputStream fileStream, Console console, String delimiter, String dateFormat) {
		this.setHeader(header);
		this.setOutputStream(fileStream);
		this.setConsole(console);
		
		notMatchingLines = "";
		linesInWrongBlock = "";
		idsNotFound = "";
		
		setDelimiter(delimiter);
		setDateFormat(dateFormat);
		setMainFrame(null);
	}

	public int convertToTextFile() {
		return convertToTextFile(this.header, outputStream, this.console, delimiter);
	}
	
	public int convertToTextFile(OplHeader header, OutputStream outputFile) {
		return convertToTextFile(header, outputFile, new Console(), DEFAULT_DELIMITER);
	}
	
	public int convertToTextFile(OplHeader header, OutputStream outputStream, Console console, String delimiter) {
		///////////////////////////////////////////////////////////////////////////////////
		// checking for errors
		if (console == null) {
			console = new Console();
			console.printConsoleWarningLine("Es wurde keine Konsole übergeben, also wird eine neue erstellt.", 200);
		}
		if (outputStream == null) {
			console.printConsoleErrorLine("Es wurde keine outputFile übergeben!", 200);
			return 200;
		}
		if (header == null) {
			console.printConsoleErrorLine("Es wurde keine header übergeben! pointer:" + header, 200);
			return 200;
		}
		
		int headerStatus = header.checkErrorStatus();
		if (headerStatus != 0) { // wenn es ein Fehlercode beim header gibt
			console.printConsoleErrorLine("Der Header ist fehlerhaft!", headerStatus);
			return headerStatus;
		}
		
		///////////////////////////////////////////////////////////////////////////////////
		
		// gehe jede einzelne opl datei durch
		File oplFile = header.getOplFile();
		
		FileReader fr;
		FileWriter fw;
		try {
			// open input file
			fr = new FileReader(oplFile);
			BufferedReader br = new BufferedReader(fr);
			
			// set up pattern
			Pattern blockHeaderPattern = Pattern.compile(BLOCK_HEADER);
			Pattern blockElementPattern = Pattern.compile(BLOCK_ELEMENT);
				
			// open output file
			PrintWriter pw = new PrintWriter(outputStream);
				
			// linecounter
			long linecounter = 1;
				
			// declare line string
			String line;
				
			// defining time values to check if the header has the same time like the body
			long headerTime = -1;
			long elementTime = -1;
			
			// define arraylist that has elements that will be in the lane
			ArrayList<OplTypeElement> blockElements;
				
			// jump to end of header
			long headerEnd = header.getHeaderEnd();
			for(; linecounter < headerEnd+1; linecounter++) br.readLine();
				
			line = br.readLine();
			while(line!=null) {
				// set up matcher
				Matcher blockHeaderMatcher = blockHeaderPattern.matcher(line);
					
					if (blockHeaderMatcher.find()) { // wenn eine block header gefunden wurde
					// get time of header
					headerTime = Long.parseLong(blockHeaderMatcher.group(1));
	
					// convert to date
					Date time = new Date();
					time.setTime(headerTime * 1000); // date needs time in miliseconds and unix timestamp has time in seconds
					
					pw.write(dateFormat.format(time) + delimiter); 				// Zeitstempel im vorher definierten Format
					pw.write(blockHeaderMatcher.group(2) + delimiter);	// SZP Wert
				}
				line = br.readLine();
				
				// set up elements in line
				blockElements = new ArrayList<OplTypeElement>();
					
				// schleife geht solange wie die datei nicht leer ist und keine weitere headerzeile gefunden wird
				while (!blockHeaderMatcher.find() && line!= null) {
					Matcher blockElementMatcher = blockElementPattern.matcher(line);
	
					if (blockElementMatcher.find()) {
						// extract unix time stamp
						elementTime = Long.parseLong(blockElementMatcher.group(1));
						
						if (elementTime == headerTime) {
							// extract id and value
							long id = Long.parseLong(blockElementMatcher.group(2));
							String value = String.valueOf(Long.parseLong(blockElementMatcher.group(3)));
								
							OplTypeElement element = header.getElementFromId(id);
							if (element != null) {
								element.setValue(value);
								blockElements.add(element);
							} else {
								idsNotFound+= ", " + linecounter;
							}
						} else {
							linesInWrongBlock+= ", " + linecounter;
						}
					} else {
						notMatchingLines+= ", " + linecounter;
					}
					line = br.readLine();
						
					if (line != null) blockHeaderMatcher = blockHeaderPattern.matcher(line);
					linecounter++;
				}
					
				// wenn das conflict behavior von opl header auf 1 also Mit Null werten gesetzt wird, 
				// dann werden alle noch nicht gefundenen Types eingefügt und mit null werten versehen
				if (header.getConflictHandling() == OplHeader.USE_NULL_VALUES) {
					ArrayList<OplTypeElement> comp = AComplementsB(header.getAllElements(), blockElements);
					
					for (OplTypeElement elem : comp) {
						elem.setValue(header.getNullValue());
							
						blockElements.add(elem);
					}
				}
					
					
				// wenn der Block zu ende ist, dann wird er ausgegeben
				Collections.sort(blockElements);

				for (OplTypeElement element : blockElements) {
					pw.write(element.getValue() + delimiter);
				}			
				pw.write("\n");
				linecounter++;
			}
			br.close();
			pw.close();
				
		} catch (IOException e) {
			e.printStackTrace();
			console.printConsoleErrorLine("Es gab ein Problem beim lesen der Datei!", 202);
			return 202;
		}
		
		printWrongLines();
		
		return 0;
	}
	
	private void printWrongLines() {
		if (!notMatchingLines.equals("")) {
			console.printConsoleLine("Folgende Zeilen wurden übersprungen, da kein Zellenelement oder Zeilenkopf erkannt wurde: " + notMatchingLines);
		}
		
		if (!linesInWrongBlock.equals("")) {
			console.printConsoleLine("In folgenden Zeilen wurde eine Zeile im falschen block festgestellt: " + linesInWrongBlock);
		}
		
		if (!idsNotFound.equals("")) {
			console.printConsoleErrorLine("In folgenden Zeilen wurde die ID nicht erkannt: " + idsNotFound, 205);
		}
	}
	
	private ArrayList<OplTypeElement> AComplementsB(ArrayList<OplTypeElement> A, ArrayList<OplTypeElement> B) {
		ArrayList<OplTypeElement> result = new ArrayList<OplTypeElement>(A);
		result.removeAll(B);
		
		return result;
	}
	
	// opl file that is being converted
	public File getFile() {
		return header.getOplFile();
	}
	
	public void setFiles(File files) {
		header.setOplFile(files);
	}

	public OplHeader getHeader() {
		return header;
	}

	public void setHeader(OplHeader header) {
		this.header = header;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(ByteArrayOutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public Console getConsole() {
		return console;
	}

	public void setConsole(Console console) {
		this.console = console;
	}

	public JFrame getMainFrame() {
		return mainFrame;
	}

	public void setMainFrame(JFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public String getDateFormat() {
		return dateFormat.toPattern();
	}

	public void setDateFormat(String format) {
		this.dateFormat = new SimpleDateFormat(format);
	}
	
	public String getTimeZone() {
		return dateFormat.getTimeZone().getID();
	}

	public void setTimeZone(String timeZoneID) {
		dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneID));
	}
	
	public ByteArrayInputStream getInputStream() {
		return new ByteArrayInputStream(outputStream.toByteArray());
	}
}
