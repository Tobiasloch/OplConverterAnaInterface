package classes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.UIManager;

public class mainClass {
	
	public static void main(String[] args) {
		try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }
		
		SimpleDateFormat df = new SimpleDateFormat("dd'.'MM'.'yyyy' 'kk':'mm':'ss");
		Pattern p = Pattern.compile(df.toPattern());
		
		Date d = new Date();
		Matcher m = p.matcher(df.format(d));

		System.out.println(m.matches());
		
		mainWindow frame = new mainWindow();
		frame.setVisible(true);
	}
}
