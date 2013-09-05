package bwbv.ersatzspielercheck;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class CSVLoader {

	private static Logger logger = Logger.getLogger(CSVLoader.class.getName());

	void load(String filename, int skipRows) throws IOException {
		logger.info("lade "+filename);
		int anz = 0;
		int j = skipRows - 1;
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		for (int i = 0; line != null; i++) {
			// Ueberschriftszeile(n) ueberspringen
			if (i > j) {
				processRow(line.split(";"));
				anz++;
			}
			line = br.readLine();
		}
		logger.info("" + anz + " Zeilen geladen.");
	}

	abstract void processRow(String[] token);
}
