package bwbv.ersatzspielercheck;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Logger;

public abstract class CSVLoader {

	private static Logger logger = Logger.getLogger(CSVLoader.class.getName());

	void load(String filename, int skipRows) throws IOException {
		load(filename, skipRows, null);
	}
	
	void load(String filename, int skipRows, String charSet) throws IOException {
		logger.info("lade " + filename + " als " + charSet);
		int anz = 0;
		int j = skipRows - 1;

		// open file
		FileInputStream is = new FileInputStream(filename);
		Reader r;
		if (charSet == null || charSet.length() == 0)
			r = new InputStreamReader(is);
		else
			r = new InputStreamReader(is, charSet);
		BufferedReader br = new BufferedReader(r);
		
		// read line by line
		String line = br.readLine();
		for (int i = 0; line != null; i++) {
			// Ueberschriftszeile(n) ueberspringen
			if (i > j) {
				processRow(line.split(";"));
				anz++;
			}
			line = br.readLine();
		}

		br.close();
		r.close();
		is.close();
		logger.info(String.format("%d Zeilen geladen.", anz));
	}

	abstract void processRow(String[] cols);
}
