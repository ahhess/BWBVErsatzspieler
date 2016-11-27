package bwbv.ersatzspielercheck;

import bwbv.ersatzspielercheck.model.Spieler;
import bwbv.ersatzspielercheck.model.Verein;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Hashmap fuer Spieler. Key ist die Passnr.
 */
@SuppressWarnings("serial")
public class SpielerMap extends HashMap<String, Spieler> {

	private static Logger logger = Logger.getLogger(SpielerMap.class.getName());
	
	private HashMap<String, Verein> vereine;

	public void load(String filename, String kzVrRr, String charSet) throws IOException {
		logger.info("lade Spieler (" + kzVrRr + ") von " + filename);

		CSVLoader loader;
		if ("R".equalsIgnoreCase(kzVrRr)) {
			loader = new CSVLoader() {
				@Override
				void processRow(String[] token) {
					Spieler spieler = getSpieler(token[21], token[22], token[24], token[25]);
					Verein verein = getVerein(token[2], token[3], token[1]);					
					try {
						spieler.setStammMannschaftVR(Integer.parseInt(token[13]));
						spieler.setVereinVR(verein);
					} catch (NumberFormatException e) {
						// ok, spieler kann nur rr-verein haben
					}
					try {
						spieler.setStammMannschaftRR(Integer.parseInt(token[14]));
						spieler.setVereinRR(verein);
					} catch (NumberFormatException e) {
						// ok, spieler kann nur vr-verein haben
					}
					try {
						spieler.setRangVR(Integer.parseInt(token[17]));
					} catch (NumberFormatException e) {
						// ok, spieler kann nur rr-verein haben
					}
					try {
						spieler.setRangRR(Integer.parseInt(token[18]));
					} catch (NumberFormatException e) {
						// ok, spieler kann nur vr-verein haben
					}
					put(spieler.getNr(), spieler);
					verein.getSpielerMap().put(spieler.getNr(), spieler);
				}
			};
		} else {
			loader = new CSVLoader() {
				@Override
				void processRow(String[] token) {
					Spieler spieler = getSpieler(token[19], token[20], token[22], token[23]);
					Verein verein = getVerein(token[2], token[3], token[1]);					
					try {
						spieler.setStammMannschaftVR(Integer.parseInt(token[13]));                                                
						spieler.setVereinVR(verein);
					} catch (NumberFormatException e) {
						logger.warning("ungueltige StammMannschaftVR: " + spieler);
					}
					try {
						spieler.setRangVR(Integer.parseInt(token[15]));
					} catch (NumberFormatException e) {
						logger.warning("ungueltiger RangVR: " + spieler);
					}
					put(spieler.getNr(), spieler);
					verein.getSpielerMap().put(spieler.getNr(), spieler);
				}
			};
		}
		loader.load(filename, 1, charSet);
	}

	private Spieler getSpieler(String nr, String passnr, String nachname, String vorname) {
		Spieler spieler = get(nr);
		if(spieler == null){
			spieler = new Spieler(nr, passnr, nachname, vorname);
			logger.finest("Spieler: "+spieler.toString());
		}
		return spieler;
	}

	private Verein getVerein(String vnr, String name, String bezirk) {
		if(vereine == null){		
			vereine = new HashMap<String, Verein>();
		}

		Verein verein = vereine.get(vnr);
		if (verein == null) {
			verein = new Verein(vnr, name, bezirk);
			vereine.put(vnr, verein);
			logger.fine("Verein: "+verein.toString());
		}
		return verein;
	}

	public HashMap<String, Verein> getVereine() {
		return vereine;
	}

	public void setVereine(HashMap<String, Verein> vereine) {
		this.vereine = vereine;
	}

}
