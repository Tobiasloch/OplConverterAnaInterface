package classes;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	public static final String DEFAULT_DELIMITER = DELIM_SEMICOLON;
	public static final String DEFAULT_DATE_FORMAT = "dd'.'MM'.'yyyy' 'kk':'mm':'ss";
	public static final String DEFAULT_DATE_REGEX = "\\d{2}.\\d{2}.\\d{4} \\d{2}:\\d{2}:\\d{2}";
	private String dateRegex = DEFAULT_DATE_REGEX;
	public static char DEFAULT_START_CHAR = 'c';
	
	public static final String DEFAULT_NULL_VALUE = "-1";
	private String nullValue = DEFAULT_NULL_VALUE;
	
	// for error handling
	public ArrayList<Long> notMatchingLines = new ArrayList<Long>(); // lines that did not match any pattern
	public ArrayList<Long> linesInWrongBlock = new ArrayList<Long>(); // lines that where in the wrong second block
	public ArrayList<Long> idsNotFound = new ArrayList<Long>(); // lines that ids could not be found
	public ArrayList<Long> elementTwiceInBlock = new ArrayList<Long>(); // elements that have be found twice in one block. The first one is being printed
	
	private ByteArrayOutputStream outputStream;
	private OplHeader header; // header of the opl file
	
	private String delimiter; // Trenn String; wird zwischen jeden wert gepackt
	private SimpleDateFormat dateFormat; // convertiert den Zeitstempel in das angegebene Format
	
	private String regex;
	private String varName[];
	
	public convertOPL() {
		this(null);
	}
	
	public convertOPL(File OplFile) {
		this(OplFile, new ByteArrayOutputStream(), DEFAULT_DELIMITER, DEFAULT_DATE_FORMAT, DEFAULT_DATE_REGEX);
	}
	
	public convertOPL(File OplFile, ByteArrayOutputStream fileStream, String delimiter) {
		this(OplFile, fileStream, delimiter, DEFAULT_DATE_FORMAT, DEFAULT_DATE_REGEX);
	}
	
	public convertOPL(File OplFile, ByteArrayOutputStream fileStream, String delimiter, String dateFormat, String dateRegex) {
		loadHeaderFromFile(OplFile);
		
		this.setOutputStream(fileStream);
		
		setDelimiter(delimiter);
		setDateFormat(dateFormat, dateRegex);
		
		regex = "";
		varName = null;
	}
	
	public String generateRegex() {
		if (header != null && header.checkErrorStatus() == 0) {
			ArrayList<OplTypeElement> elements = header.getAllElements();
			
			varName = new String[elements.size()];
			String s = dateRegex + ";(?<SZP>\\d+);"; // date pattern and szp stamp
			
			for (OplTypeElement element : elements) {
				// removing all non alphanumeric charcters from the name, because named groups in java regex cannot have non alphanumeric values in their name
				// also puts a letter at the start if there is none
				s+= "(?<" + convertNameToMatchingName(element.getName()) + ">\\d+);";
			}
			
			regex = s;
			return s;
		} else {
			throw new IllegalArgumentException("Es gab ein Problem mit dem header!");
		}
	}
	
	private String convertNameToMatchingName(String name) {
		String s = name.replaceAll("[^A-Za-z0-9]", ""); // removing all non alphanumeric
		
		// checks if the string starts with a char and if not adds one
		char c = s.charAt(0);
		if (!((c >= 'A' && c <= 'Z') || (c>='a' && c<='b'))) {
			s = DEFAULT_START_CHAR + s;
		}
		
		return s;
	}
	
	public void loadHeaderFromFile(File f) { // loading new opl header from a given opl file f
		if (f != null && f.exists()) {
			OplHeader header = new OplHeader(f);
			try {
				header.extractHeaderInformation();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			this.header = header;
		}
	}

	public int convertToStream() {
		return convertToStream(this.header, outputStream, delimiter);
	}
	
	public int convertToStream(OplHeader header, ByteArrayOutputStream outputStream) {
		return convertToStream(header, outputStream, DEFAULT_DELIMITER);
	}
	
	public int convertToStream(OplHeader header, ByteArrayOutputStream outputStream, String delimiter) throws IllegalArgumentException {
		///////////////////////////////////////////////////////////////////////////////////
		// checking for errors
		if (outputStream == null) {
			throw new IllegalArgumentException("Es wurde kein outputStream übergeben!");
		}
		if (header == null) {
			throw new IllegalArgumentException("Es wurde kein header übergeben!");
		}
		
		int headerStatus = header.checkErrorStatus();
		if (headerStatus != 0) { // wenn es ein Fehlercode beim header gibt
			throw new IllegalArgumentException("Der Header ist fehlerhaft!");
		}
		
		this.outputStream = outputStream;
		
		///////////////////////////////////////////////////////////////////////////////////
		
		// gehe jede einzelne opl datei durch
		File oplFile = header.getOplFile();
		
		FileReader fr;
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
			HashMap<Long, OplTypeElement> blockElements;

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
				blockElements = new HashMap<Long, OplTypeElement>();
					
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
								if (!blockElements.containsKey(element.getId())) {
									blockElements.put(element.getId(), element);
								} else {
									elementTwiceInBlock.add(linecounter);
								}
							} else {
								idsNotFound.add(linecounter);
							}
						} else {
							linesInWrongBlock.add(linecounter);
						}
					} else {
						notMatchingLines.add(linecounter);
					}
					line = br.readLine();
						
					if (line != null) blockHeaderMatcher = blockHeaderPattern.matcher(line);
					linecounter++;
				}

				// printing the line in the given order of types and elements
				for (OplType type : header.getTypes()) {
					for (OplTypeElement element : type.getElements()) {
						if (blockElements.containsKey(element.getId())) pw.write(element.getValue() + delimiter);
						else {
							pw.write(nullValue + delimiter);
						}
					}
				}
				pw.write("\n");
				linecounter++;
			}
			br.close();
			pw.close();
				
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Es gab ein Problem beim lesen der Datei!");
		}
		System.out.println(generateRegex());
		printWrongLines();
		
		return 0;
	}

	public void printWrongLines() {
		for (Long value : notMatchingLines) {
			System.out.println("Folgende Zeilen wurden übersprungen, da kein Zellenelement oder Zeilenkopf erkannt wurde: " + value.toString());
		}
		
		for (Long value : linesInWrongBlock) {
			System.out.println("In folgenden Zeilen wurde eine Zeile im falschen block festgestellt: " + value.toString());
		}
		
		for (Long value : idsNotFound) {
			System.out.println("In folgenden Zeilen wurde die ID nicht erkannt: " + value.toString());
		}
		
		for (Long value : elementTwiceInBlock) {
			System.out.println("In folgenden Zeilen wurde ein Wert zum wiederholten mal in einem Block gefunden: " + value.toString());
		}
	}
	
	// opl file that is being converted
	public File getFile() {
		return header.getOplFile();
	}
	
	public void setFiles(File files) {
		header.setOplFile(files);
	}

	public String getNullValue() {
		return nullValue;
	}

	public void setNullValue(String nullValue) {
		this.nullValue = nullValue;
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

	public String getDateFormat() {
		return dateFormat.toPattern();
	}

	public void setDateFormat(String format, String regex) {
		this.dateFormat = new SimpleDateFormat(format);
		this.dateRegex = regex;
	}
	
	public ByteArrayInputStream getInputStream() {
		return new ByteArrayInputStream(outputStream.toByteArray());
	}

	public String getDateRegex() {
		return dateRegex;
	}
}
