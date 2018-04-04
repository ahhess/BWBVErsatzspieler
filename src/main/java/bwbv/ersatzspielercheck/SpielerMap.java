package bwbv.ersatzspielercheck;

import bwbv.ersatzspielercheck.model.Spieler;
import bwbv.ersatzspielercheck.model.Verein;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Hashmap fuer Spieler. Key ist die Passnr.
 */
@SuppressWarnings("serial")
public class SpielerMap extends HashMap<String, Spieler> {

	private static Logger logger = Logger.getLogger(SpielerMap.class.getName());
	
	private HashMap<String, Verein> vereine;

	public void load(Properties conf) throws IOException {
		String kzVrRr = conf.getProperty("kzVrRr"); 
		loadVrlVr(conf);
		if ("R".equalsIgnoreCase(kzVrRr)) {
			loadVrlRr(conf);
		}
	}
	
	private void loadVrlVr(Properties conf) throws IOException {

			String filename = conf.getProperty("vrlVrFile");
			String charSet = conf.getProperty("vrlVrFile.charset");
			int skipRows = ErsatzspielerCheck.getNumericConfigProp(conf, "vrlVrFile.skipRows", 1);

			final int iRlNr = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLVR.SpielerRangEinzel", 15);
			final int iSpielerNr = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLVR.InterneNr", 19);
			final int iNachname = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLVR.Nachname", 22);
			final int iVorname = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLVR.Vorname", 23);
			final int iVNr = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLVR.VereinNr", 2);
			final int iVerein = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLVR.VereinName", 3);
			final int iRegion = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLVR.Region", 1);
			final int iMannschaft = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLVR.SpielerMannschaftNummer", 13);
			final int iGeschlecht = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLVR.Geschlecht", 27);
			
			CSVLoader loader = new CSVLoader() {
				@Override
				void processRow(String[] field) {
					Spieler spieler = getSpieler(field[iRlNr], field[iSpielerNr], field[iNachname], field[iVorname], field[iGeschlecht]);
					Verein verein = getVerein(field[iVNr], field[iVerein], field[iRegion]);					
					try {
						spieler.setStammMannschaftVR(Integer.parseInt(field[iMannschaft]));                                                
						spieler.setVereinVR(verein);
					} catch (NumberFormatException e) {
						logger.warning("ungueltige StammMannschaftVR: " + spieler);
					}
					try {
						spieler.setRangVR(Integer.parseInt(field[iRlNr]));
					} catch (NumberFormatException e) {
						logger.warning("ungueltiger RangVR: " + spieler);
					}
					put(spieler.getPassnr(), spieler);
					verein.getSpielerMap().put(spieler.getPassnr(), spieler);
				}
			};
			loader.load(filename, skipRows, charSet);
	}
	
	private void loadVrlRr(Properties conf) throws IOException {

		String filename = conf.getProperty("vrlRrFile");
		String charSet = conf.getProperty("vrlRrFile.charset");
		int skipRows = ErsatzspielerCheck.getNumericConfigProp(conf, "vrlRrFile.skipRows", 1);
		
		final int iRegion = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLRR.Region", 1);
		final int iVNr = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLRR.VereinNr", 2);
		final int iVerein = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLRR.VereinName", 3);
		final int iMannschaft = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLRR.SpielerMannschaftNummerRR", 14);
		final int iRlNr = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLRR.SpielerRangEinzelRR", 18);
		final int iSpielerNr = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLRR.InterneNr", 23);
		final int iNachname = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLRR.Nachname", 26);
		final int iVorname = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLRR.Vorname", 27);
		final int iGeschlecht = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLRR.Geschlecht", 31);

		final int iMannschaftVR = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLRR.SpielerMannschaftNummerVR", 13);
		final int iRlNrVR = ErsatzspielerCheck.getNumericConfigProp(conf, "VRLRR.SpielerRangEinzelVR", 17);

		CSVLoader loader = new CSVLoader() {
			@Override
			void processRow(String[] field) {
				Spieler spieler = getSpieler(field[iRlNr], field[iSpielerNr], field[iNachname], field[iVorname], field[iGeschlecht]);
				Verein verein = getVerein(field[iVNr], field[iVerein], field[iRegion]);					
				try {
					spieler.setStammMannschaftVR(Integer.parseInt(field[iMannschaftVR]));
					spieler.setVereinVR(verein);
				} catch (NumberFormatException e) {
					// ok, spieler kann nur rr-verein haben
				}
				try {
					spieler.setStammMannschaftRR(Integer.parseInt(field[iMannschaft]));
					spieler.setVereinRR(verein);
				} catch (NumberFormatException e) {
					// ok, spieler kann nur vr-verein haben
				}
				try {
					spieler.setRangVR(Integer.parseInt(field[iRlNrVR]));
				} catch (NumberFormatException e) {
					// ok, spieler kann nur rr-verein haben
				}
				try {
					spieler.setRangRR(Integer.parseInt(field[iRlNr]));
				} catch (NumberFormatException e) {
					// ok, spieler kann nur vr-verein haben
				}
				put(spieler.getPassnr(), spieler);
				verein.getSpielerMap().put(spieler.getPassnr(), spieler);
			}
		};
		loader.load(filename, skipRows, charSet);
	}

	private Spieler getSpieler(String rlnr, String passnr, String nachname, String vorname, String geschlecht) {
		Spieler spieler = get(passnr);
		if(spieler == null){
			spieler = new Spieler(rlnr, passnr, nachname, vorname, geschlecht);
			logger.finest("neuer Spieler: "+spieler.toString());
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
			logger.fine("neuer Verein: "+verein.toString());
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
