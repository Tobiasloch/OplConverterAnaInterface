package classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OplHeader {
	
	/*	errorcodes: 100-199
	 * 0: no error	
	 * 
	 * 100: keine OplFile angegeben
	 * 101: br.readline() hat exception geworfen
	 * 102: OplFile, does not exist
	 * 103: Es wurden nicht 4 Gruppen durch den Matcher gefunden
	 * 104: die ID konnte nicht geladen werden
	 * 105: kein Header wurde gefunden
	 * 106: keine Daten wurden bis jetzt ausgelesen (extractHeaderInformation wurde nicht aufgerufen)
	 * 107: Variablen wurden ver�ndert ohne Aufruf der extractHeaderInformation
	 * 108: Problem mit den Intervallgrenzen der Header Teile
	 * 109: Header von verschiedenen Dateien stimmen nicht �berein
	 * 110: Header conflict handling wurde ver�ndert und extractHeaderInformation jedoch noch nicht
	 * 111: conflict handling out of range
	 * 
	 * */
	
	public static final String HEADER_REG_EX = "\\d{10} \\S+\\p{Blank}*"
			+ "\\d{10} (?<fileInformation>(?:\\S+\\s)+) "
			+ "\\p{Blank}*\\d{10}(?:\\s\\S+)+ "
			+ "\\p{Blank}*\\d{10} \\d{10} \\d{10}(?<typeName>(?:\\s\\S+)+) "
			+ "\\p{Blank}*(?<type>(?:\\s\\S+)+) \\p{Blank}*(?<id>\\d{10})";
	
	public static final String[] HEADER_REGEX = {"\\d{10}(?:\\s\\S+)+", // 1
			"\\d{10} (?<fileInformation>(?:\\S+(?:\\s\\S+)*)+)", // 2
			"\\d{10}(?:\\s\\S+)+", // 3
			"\\d{10} \\d{10} (?<id>\\d{10}) (?<typeName>(?:\\S+(?:\\s\\S+)*)+)", // 4
			"(?<type>(?:\\S+(?:\\s\\S+)*)+)",  // 5
			"\\d{10}"}; // 6
	
	public static final int[] HEADER_REGEX_END = {76, 152, 228, 326, 391, 401};
	
	/*
	public static final int HEADER_FIRST_END = 77;
	public static final String HEADER_SECOND_REGEX = "\\d{10} (?<fileInformation>(?:\\S+\\s)+)";
	public static final int HEADER_SECOND_END = 153;
	public static final String HEADER_THIRD_REGEX = "\\d{10}(?:\\s\\S+)+";
	public static final int HEADER_THIRD_END = 229;
	public static final String HEADER_FOURTH_REGEX = "\\d{10} \\d{10} \\d{10}(?<typeName>(?:\\s\\S+)+)";
	public static final int HEADER_FOURTH_END = 327;
	public static final String HEADER_FIFTH_REGEX = "(?<type>(?:\\s\\S+)+)";
	public static final int HEADER_FIFTH_END = 392;
	public static final String HEADER_SIXTH_REGEX = "(?<id>\\d{10})";
	public static final int HEADER_SIXTH_END = 402;
	*/
	
	private ArrayList<OplType> types; // Liste die die Opl Typen enth�lt
	private String fileInformation; // Informationen zur Datei. Meistens Ort der Aufnahme bzw. Dateiname
	
	private long headerStart;
	private long headerEnd;
	
	private int errorStatus;
	
	// Nach welchen verfahren mehrere Header Dateien mit unterschiedlichen Headern behandelt werden sollen
	// 0: Abbruch bei unterschiedlichen Headern [DEFAULT]
	// 1: mit null werten wird gearbeitet
	// 2: nur gleiche Variablen werden �bertragen
	
	private File OplFile; // Datei die ausgelesen werden soll
	
	public OplHeader() {
		this(new File(""));
	}
	
	public OplHeader(File OplFile) {
		this.OplFile = OplFile;
		
		types = new ArrayList<OplType>();
		
		fileInformation = "";
		
		errorStatus = 106;
		
		headerEnd = -1;
		headerStart = -1;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null || !(object instanceof OplHeader)) return false;
		
		OplHeader header = (OplHeader) object;

		if (fileInformation.equals(header.fileInformation) && types.equals(header.getTypes())) {
			return true;
		}
		
		return false;
	}
	
	public OplTypeElement getElementFromId(long id) {
		for (OplType type : types) {
			for (OplTypeElement element : type.getElements()) {
				if (element.getId() == id) return element;
			}
		}
		
		return null;
	}
	
	public ArrayList<OplTypeElement> getAllElements(){
		ArrayList<OplTypeElement> elements = new ArrayList<OplTypeElement>();
		
		for (OplType type : types) {
			elements.addAll(type.getElements());
		}
		
		return elements;
	}
	
	public OplType getType(String type) {
		for (OplType t : types) {
			if (t.getType().equals(type)) return t;
		}
		
		return null;
	}
	
	public int extractHeaderInformation() throws IOException{
		return extractHeaderInformation(OplFile);
		
	}
	
	/**
	 * 
	 * @author t.loch
	 * @param OplFile reads the data from the OplFile
	 * @exception if the buffered reader cant read the line it throws an IOException
	 * @return returns the errorcode
	 * 
	 */
	public int extractHeaderInformation(File OplFile) throws IOException {
		// �berpr�fe ob die Datei existiert
		if (!OplFile.exists()) {
			errorStatus = 102;
			throw new IllegalArgumentException("Die OplDatei existiert nicht! " + errorStatus);
		}
		
		// Muster erstellen, nach dem sp�ter gesucht werden kann
		Pattern[] headerPattern = new Pattern[HEADER_REGEX.length];
		// header pattern f�r jeden einzelnen String laden
		for (int i = 0; i < headerPattern.length; i++) headerPattern[i] = Pattern.compile(HEADER_REGEX[i]);
		
		// wird true wenn header gefunden; wichtig, damit der alg. den Header eindeutig findet
		boolean foundHeader = false;
		
		// f�r das Fehlerfinden
		long startHeader = -1;
		long lineCounter = 0;
		
		try {
			// Die Datei Laden
			FileReader fr = new FileReader(OplFile);
			BufferedReader br = new BufferedReader(fr);
			
			for (String line = br.readLine(); line!=null; line = br.readLine()) {
				boolean found = true;
				Matcher[] m = new Matcher[headerPattern.length];
				
				String[] headerParts = new String[headerPattern.length];
				
				// checks if the line has the header pattern by checking each header partition
				if (line.length() == HEADER_REGEX_END[HEADER_REGEX_END.length-1]) { // checkt ob die headerzeile die richtige l�nge hat
					for (int i = 0; i < headerPattern.length; i++) {
						int start = 0;
						if (i > 0) start = HEADER_REGEX_END[i-1];
						int end = HEADER_REGEX_END[i];
							
						// den aktuellen teil der headerzeile abschneiden
						if (start >= 0 && start < line.length() && end >= 0 && end <= line.length()) {
							headerParts[i] = line.substring(start, end);
						} else {
							br.close();
							throw new OplHeaderException("Es gab ein Problem mit den Teilen einer Header Zeile(" + lineCounter + ") ! errorcode: 108");
						}
							
						// matcher erstellen
						m[i] = headerPattern[i].matcher(headerParts[i]);
			
						// nach muster suchen
						if (!m[i].find()) {
							found = false;
							break;
						}
					}
				} else {
					found = false;
				}
				
				lineCounter++;
				
				if (found) {
					if (startHeader == -1) {
						startHeader = lineCounter;
						foundHeader = true;
					}
					
					// �berpr�ft ob genug Daten gefunden wurden
					if (m[1].groupCount() != 1 || m[3].groupCount() != 2 || m[4].groupCount() != 1) {
						errorStatus = 103;
						br.close();
						throw new OplHeaderException("Es wurden nicht 4 Variablen in der Zeile gefunden! Zeile: " + lineCounter + " errorcode: " + errorStatus);
					}
					
					// load file Information
					fileInformation = m[1].group(1);
					
					OplTypeElement element = new OplTypeElement();
					
					// load typeName
					element.setName(m[3].group(2));
					
					// load type
					String type = m[4].group(1);
					
					// load type ID
					long id = 0;
					
					try {
						id = Long.parseLong(m[3].group(1));
					} catch (NumberFormatException nfe) {
						errorStatus = 104;
						br.close();
						nfe.printStackTrace();
						throw new OplHeaderException("Die ID konnte nicht richtig gelesen werden!; Zeile: " 
								+ lineCounter + " errorstatus: " + errorStatus);
					}
					element.setId(id);
					
					
					OplType typeObject = getType(type);
					
					if (typeObject == null) {
						typeObject = new OplType(type);
	
						addType(typeObject);
					}
					
					element.setType(typeObject);
					typeObject.addElement(element);
				} else if (foundHeader) {
					headerStart = startHeader;
					headerEnd = --lineCounter;
					
					System.out.println("Der Header wurde von Zeile " + headerStart 
							+ " bis Zeile " + headerEnd + " ausgelesen");
					
					break;
				}
			}
			
			br.close();
		} catch (IOException e) {
			System.err.println("Es Gab ein Problem mit dem Lesen der Datei! errorcode: " + errorStatus);
			errorStatus = 101;
			
			throw e;
		}
		
		if (!foundHeader) {
			errorStatus = 105;
			throw new OplHeaderException("Es wurde kein Headerblock gefunden! errorcode: " + errorStatus);
		}
		
		errorStatus = 0;
		return errorStatus;
	}

	public int checkErrorStatus() {
		return errorStatus;
	}
	
	public void addType(OplType type) {
		types.add(type);
	}
	
	public ArrayList<OplType> getTypes() {
		return types;
	}

	public String getFileInformation() {
		return fileInformation;
	}

	public File getOplFile() {
		return OplFile;
	}

	public void setOplFile(File oplFiles) {
		this.OplFile = oplFiles;
		errorStatus = 107;
	}

	public long getHeaderEnd() {
		return headerEnd;
	}
	
	public long getHeaderStart() {
		return headerStart;
	}

}