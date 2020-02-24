package bwbv.ersatzspielercheck;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import bwbv.ersatzspielercheck.model.Einsatz;
import bwbv.ersatzspielercheck.model.Spieler;

public class ErsatzspielerCheck {

	private static Logger logger = Logger.getLogger(ErsatzspielerCheck.class.getName());

	private String confFilename = "ErsatzspielerCheck.properties";

	// constants from properties:
	private String IGNORESPNR1 = "0";
	private String IGNORESPNR2 = "00000000";
	private String IGNORESPNR3 = "NU-";

	private Properties config = new Properties();
	private Properties spieltage = new Properties();
	private Properties spieltageUnbekannt = new Properties();
	private SpielerMap spielerMap = new SpielerMap();
	private SpielerMap ersatzspielerMap = new SpielerMap();
	private List<Spieler> festgespielt = new ArrayList<Spieler>();
	private List<Spieler> falschspieler = new ArrayList<Spieler>();

	public static void main(String[] args) {
		try {
			ErsatzspielerCheck e = new ErsatzspielerCheck();
			e.init(args);
			e.processEinsaetze();
			e.checkErsatzspieler();
			e.exportSpielerXML("outfile", new ArrayList<Spieler>(e.getErsatzspielerMap().values()));
			// exportSpielerXML("outfile", falschspieler);
			// exportSpielerXML("outfileFestgespielt", festgespielt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ErsatzspielerCheck() throws Exception {
		logger.info("start");
	}

	public void init(String[] args) throws IOException, FileNotFoundException {

		loadConfig(args);

		// init "constants"
		IGNORESPNR1 = config.getProperty("IGNORESPNR1", IGNORESPNR1);
		IGNORESPNR2 = config.getProperty("IGNORESPNR2", IGNORESPNR2);
		IGNORESPNR3 = config.getProperty("IGNORESPNR3", IGNORESPNR3);

		loadSpieltage();
		loadVRL();
	}

	public static int getNumericConfigProp(Properties config, String propName, int def) {
        String s = config.getProperty(propName);
        try {
            if (s != null)
                return Integer.parseInt(s);
        } catch (Exception e) {
        }
        return def;
    }

	public void loadVRL() throws IOException {
		spielerMap.load(config);
	}

	public void loadSpieltage() throws IOException, FileNotFoundException {
		// Spieltage: Zuordnung Datum zu Spieltagsnr
		// (Spieltagsnr: 0=SpT1, ..., 4=SpT4a, 5=SpT5, ..., 8=SpT8, 9=SpT8a)
		String filename = config.getProperty("sptfile");
		logger.info("lade Spieltage aus " + filename);
		spieltage.load(new FileReader(filename));
	}

	public void loadConfig(String[] args) throws IOException, FileNotFoundException {
		if (args.length == 1)
			confFilename = args[0];
		if (args.length <= 1) {
			logger.info("lade config aus " + confFilename);
			config.load(new FileReader(confFilename));
		} else {
			for (int i = 0; i < args.length; i++) {
				if (i == 0)
					config.setProperty("infile", args[i]);
				else if (i == 1)
					config.setProperty("vrlVrFile", args[i]);
				else if (i == 2)
					config.setProperty("vrlRrFile", args[i]);
				else if (i == 3)
					config.setProperty("kzVrRr", args[i]);
				else if (i == 4)
					config.setProperty("sptfile", args[i]);
				else if (i == 5)
					config.setProperty("outfile", args[i]);
			}
		}
	}

	public void processEinsaetze() throws IOException {
		logger.info("start");
		ErgebnisLoader ergebnisLoader = new ErgebnisLoader(config) {
			@Override
			void processRow(String[] cols) {
				ErgebnisRecord record = getErgebnisRecord(cols);
				for (int i = 0; i < 4; i++) {
					createEinsatz(record, i);
				}
			}
		};
		ergebnisLoader.load(config.getProperty("infile"),
				getNumericConfigProp(config, "infile.skipRows", 1),
				config.getProperty("infile.charset"));
	}

	private void createEinsatz(ErgebnisRecord spielerRec, int i) {
		String spielerNr = spielerRec.spielerNr[i];
		if (spielerNr != null && !"".equals(spielerNr)) {
			// leere spielernr ist bei Einzeln normal --> ignorieren
			if (IGNORESPNR1.equals(spielerNr) || IGNORESPNR2.equals(spielerNr) || spielerNr.startsWith(IGNORESPNR3)) {
				logger.finer("spielernr ignoriert: " + spielerNr);
			} else {
				Spieler spieler = spielerMap.get(spielerNr);
				if (spieler == null) {
					logger.warning("Unbekannte Spielernr: " + spielerNr);
				} else {
					// TODO Vereinswechsel zur Rueckrunde beruecksichtigen!?
		
					// Einsatz dem Spieler zuordnen
					Einsatz einsatz = new Einsatz();
					einsatz.setMannschaft(Integer.parseInt(spielerRec.mannschaftNr[i]));
					einsatz.setDisz(spielerRec.disziplin);
					einsatz.setDatum(spielerRec.termin);
					einsatz.setSpieltag(getSpieltagFromDatum(einsatz.getDatum()));
					addEinsatz2Spieler(spieler, einsatz);

					// Spieler in hoeherer Mannschaft als Stammmannschaft
					// eingesetzt?
					if ("VR".equals(spielerRec.runde)) {
						if (einsatz.getMannschaft() < spieler.getStammMannschaftVR()) {
							ersatzspielerMap.put(spielerNr, spieler);
							spieler.getVereinVR().getErsatzspielerMap().put(spielerNr, spieler);
						}
					} else {
						if (einsatz.getMannschaft() < spieler.getStammMannschaftRR()) {
							ersatzspielerMap.put(spielerNr, spieler);
							spieler.getVereinRR().getErsatzspielerMap().put(spielerNr, spieler);
						}
					}
				}
			}
		}
	}

	private void addEinsatz2Spieler(Spieler spieler, Einsatz einsatz) {
		// einsaetze je datum-uhrzeit sammeln --> doppel + einzel nur 1
		// mannschaftseinsatz
		List<Einsatz> spTEinsaetze = spieler.getSpieltagsEinsaetze().get(einsatz.getDatum());
		if (spTEinsaetze == null) {
			spTEinsaetze = new ArrayList<Einsatz>();
			spieler.getSpieltagsEinsaetze().put(einsatz.getDatum(), spTEinsaetze);
			if (einsatz.getSpieltag() == null) {
				logger.warning("Einsatz nicht registriert: " + this + ": " + einsatz);
			} else {
				int spTnr = Integer.parseInt(einsatz.getSpieltag()) - 1;
				if (spieler.getMannschaftseinsatz()[spTnr][0] == 0) {
					spieler.getMannschaftseinsatz()[spTnr][0] = einsatz.getMannschaft();
				} else {
					if (spieler.getMannschaftseinsatz()[spTnr][1] == 0) {
						spieler.getMannschaftseinsatz()[spTnr][1] = einsatz.getMannschaft();
					}
				}
			}
		}
		spTEinsaetze.add(einsatz);
	}

	private String getSpieltagFromDatum(String datum) {
		String spieltagDatum = datum.substring(0, 10);
		String spieltag = spieltage.getProperty(spieltagDatum);
		if (spieltag == null && spieltageUnbekannt.getProperty(spieltagDatum) == null) {
			logger.warning("unbekannter SpT:" + spieltagDatum);
			spieltageUnbekannt.setProperty(spieltagDatum, "0");
		}
		return spieltag;
	}

	/**
	 * mehr als 4 Einsaetze in hoeheren Mannschaften sind kritisch
	 */
	public void checkErsatzspieler() throws IOException {

		for (Spieler spieler : ersatzspielerMap.values()) {
			int mannschaftszaehler[] = new int[9];
			int maxMannschaft = spieler.getStammMannschaftVR();
			for (int spt = 0; spt < 10; spt++) {
				if (spt == 5) {
					if (spieler.getStammMannschaftRR() < maxMannschaft) {
						maxMannschaft = spieler.getStammMannschaftRR();
					}
					if (spieler.getVereinRR() != null && !spieler.getVereinRR().equals(spieler.getVereinVR())) {
						// zaehler wieder loeschen bei vereinswechsel
						mannschaftszaehler = new int[9];
						maxMannschaft = spieler.getStammMannschaftRR();
					}
				}
				int m = spieler.getMannschaftseinsatz()[spt][0];
				int m2 = spieler.getMannschaftseinsatz()[spt][1];
				spieler.getMannschaftseinsatz()[spt][2] = maxMannschaft;
				if (m2 > 0) {
					if ((m2 < m && m <= maxMannschaft) || (m2 > m && m2 > maxMannschaft)) {
						m = m2;
					}
				}
				if (m > 0) {
					if (m > maxMannschaft) {
						logger.warning("--> achtung FALSCHEINSATZ: " + spieler);
						falschspieler.add(spieler);
						if (spt > 4) {
							if (spieler.getVereinRR() != null) {
								spieler.getVereinRR().getFalschspieler().add(spieler);
							}
						} else if (spieler.getVereinVR() != null) {
							spieler.getVereinVR().getFalschspieler().add(spieler);
						}
					}
					// alle Mannschaften von Einsatzmannschaft bis Stammmannschaft inkrementieren
					for (int i = m; i < maxMannschaft; i++) {
						mannschaftszaehler[i]++;
						if (mannschaftszaehler[i] >= 4) {
							logger.fine("festgespielt: " + spieler);
							maxMannschaft = i;
							spieler.getMannschaftseinsatz()[spt][2] = maxMannschaft;
							festgespielt.add(spieler);
							if (spt >= 5) {
								if (spieler.getVereinRR() != null) {
									spieler.getVereinRR().getFestgespielt().add(spieler);
								}
							} else if (spieler.getVereinVR() != null) {
								spieler.getVereinVR().getFestgespielt().add(spieler);
							}
							break;
						}
					}
				}
			}
		}
	}

	public void exportSpielerXML(String outfile, List<Spieler> spielerList) throws IOException {
		FileWriter writer = new FileWriter(config.getProperty(outfile));
		writer.write("<?xml version=\"1.0\" encoding=\"" + writer.getEncoding() + "\"?>\n");
		writer.write("<ErsatzspielerCheck>\n");
		for (Spieler spieler : spielerList) {
			writer.write(spieler.toXML() + "\n");
		}
		writer.write("</ErsatzspielerCheck>\n");
		writer.close();
	}

	public SpielerMap getSpielerMap() {
		return spielerMap;
	}

	public void setSpielerMap(SpielerMap spielerMap) {
		this.spielerMap = spielerMap;
	}

	public SpielerMap getErsatzspielerMap() {
		return ersatzspielerMap;
	}

	public void setErsatzspielerMap(SpielerMap ersatzspielerMap) {
		this.ersatzspielerMap = ersatzspielerMap;
	}

	public List<Spieler> getFestgespielt() {
		return festgespielt;
	}

	public void setFestgespielt(List<Spieler> festgespielt) {
		this.festgespielt = festgespielt;
	}

	public List<Spieler> getFalschspieler() {
		return falschspieler;
	}

	public void setFalschspieler(List<Spieler> falschspieler) {
		this.falschspieler = falschspieler;
	}

	public String getConfFilename() {
		return confFilename;
	}

	public void setConfFilename(String confFilename) {
		this.confFilename = confFilename;
	}

	public Properties getConfig() {
		return config;
	}

	public Properties getSpieltage() {
		return spieltageUnbekannt;
	}
}
