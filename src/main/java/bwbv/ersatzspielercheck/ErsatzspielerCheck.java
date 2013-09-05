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

	private String confFilename = "data/ErsatzspielerCheck.properties";

	private Properties config = new Properties();
	private Properties spieltage = new Properties();
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
//		exportSpielerXML("outfile", falschspieler);
//		exportSpielerXML("outfileFestgespielt", festgespielt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ErsatzspielerCheck() throws Exception {
//                System.setProperty("java.util.logging.properties", "logging.properties");
		logger.info("start");
	}

	public void init(String[] args) throws IOException, FileNotFoundException {
		loadConfig(args);
		loadSpieltage();
		loadVRL();
	}

        public void loadVRL() throws IOException {
            String filename = config.getProperty("vrlVrFile");
            spielerMap.load(filename, "V");
            if ("R".equals(config.getProperty("kzVrRr"))) {
                filename = config.getProperty("vrlRrFile");
                spielerMap.load(filename, "R");
            }
        }

	public void loadSpieltage() throws IOException, FileNotFoundException {
		//Spieltage: Zuordnung Datum zu Spieltagsnr
		//(Spieltagsnr: 0=SpT1, ..., 4=SpT4a, 5=SpT5, ..., 8=SpT8, 9=SpT8a)
		String filename = config.getProperty("sptfile");
		logger.info("lade Spieltage aus "+filename);
		spieltage.load(new FileReader(filename));
	}

	public void loadConfig(String[] args) throws IOException, FileNotFoundException {
		if (args.length==1)
			confFilename=args[0];
		if (args.length<=1){
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
		CSVLoader ergebnisLoader = new CSVLoader() {
			@Override
			void processRow(String[] token) {
				getEinsatz(token, 29, 12, 15); // Heim Spielernr1, Vereinsnr, Mannschaftsnr
				getEinsatz(token, 34, 12, 15); // Heim Spielernr2, Vereinsnr, Mannschaftsnr
				getEinsatz(token, 39, 17, 20); // Gast Spielernr1, Vereinsnr, Mannschaftsnr
				getEinsatz(token, 44, 17, 20); // Gast Spielernr2, Vereinsnr, Mannschaftsnr
			}
		};
		ergebnisLoader.load(config.getProperty("infile"), 1);
	}

	private void getEinsatz(String[] token, int iSpielernr, int iVereinsnr, int iMannschaftsnr) {
		String spielernr = token[iSpielernr];
                if (spielernr != null && !"".equals(spielernr) 
                        && !"0".equals(spielernr) && !"00000000".equals(spielernr)
                        && !spielernr.startsWith("NU-")) {
			Spieler spieler = spielerMap.get(spielernr);
			if (spieler == null) {
				logger.warning("Unbekannte Spielernr <" + spielernr +
						"> Name: " + token[iSpielernr + 3] + ", " + token[iSpielernr + 4] +
                                                " Mannschaft: "  + token[iVereinsnr + 1] + " "  + token[iMannschaftsnr]);
//				if (spielernr != null && spielernr.length() > 1){
//					//Sonderbehandlung: "A" vor der Passnr entfernen und nochmal probieren
//					spielernr = spielernr.substring(1);
//					spieler = spielerMap.get(spielernr);
//					if (spieler != null) {
//						logger.info("Spieler gefunden zu Nr <" + spielernr +
//								">  Name: " + token[iSpielernr + 2] + ", " + token[iSpielernr + 3]);
//					}
//				}
			}
			if (spieler != null) {
                                // TODO Vereinswechsel zur Rückrunde berücksichtigen!?

                                // Einsatz dem Spieler zuordnen
				Einsatz einsatz = new Einsatz();
				int mannschaft = Integer.parseInt(token[iMannschaftsnr]);
				einsatz.setMannschaft(mannschaft);
//				einsatz.setDisz(token[28]);
				String ursprTermin = token[9];
				if (ursprTermin != null && !"".equals(ursprTermin)) {
					einsatz.setDatum(ursprTermin);
				} else {
					einsatz.setDatum(token[8]);
				}
				einsatz.setSpieltag(getSpieltagFromDatum(einsatz.getDatum()));
				addEinsatz2Spieler(spieler, einsatz);

				// Spieler in hoeherer Mannschaft als Stammmannschaft eingesetzt?
				if ("VR".equals(token[6])) {
					if (mannschaft < spieler.getStammMannschaftVR()) {
						ersatzspielerMap.put(spielernr, spieler);
                                                spieler.getVereinVR().getErsatzspielerMap().put(spielernr, spieler);
					}
				} else {
					if (mannschaft < spieler.getStammMannschaftRR()) {
						ersatzspielerMap.put(spielernr, spieler);
                                                spieler.getVereinRR().getErsatzspielerMap().put(spielernr, spieler);
					}
				}
			}
		} else {
                    logger.fine("spielernr ignoriert: " + spielernr);
                }
	}

        private void addEinsatz2Spieler(Spieler spieler, Einsatz einsatz) {
            // einsaetze je datum-uhrzeit sammeln --> doppel + einzel nur 1 mannschaftseinsatz
            List<Einsatz> spTEinsaetze = spieler.getSpieltagsEinsaetze().get(einsatz.getDatum());
            if (spTEinsaetze == null) {
                spTEinsaetze = new ArrayList<Einsatz>();
                spieler.getSpieltagsEinsaetze().put(einsatz.getDatum(), spTEinsaetze);
                if (einsatz.getSpieltag() == null) {
                    logger.warning("Einsatz nicht registriert: " + this + ": " + einsatz);
                } else {
                    try {
                        int spTnr = Integer.parseInt(einsatz.getSpieltag()) - 1;
                        if (spieler.getMannschaftseinsatz()[spTnr][0] == 0) {
                            spieler.getMannschaftseinsatz()[spTnr][0] = einsatz.getMannschaft();
                        } else {
                            if (spieler.getMannschaftseinsatz()[spTnr][1] == 0) {
                                spieler.getMannschaftseinsatz()[spTnr][1] = einsatz.getMannschaft();
                            }
                        }
                    } catch (NumberFormatException e) {
                        logger.severe(e.toString());
                    }
                }
            }
            spTEinsaetze.add(einsatz);
        }
                
	private String getSpieltagFromDatum(String datum) {
		String spieltagDatum = datum.substring(0, 10);
		String spieltag = spieltage.getProperty(spieltagDatum);
		if (spieltag == null) {
			logger.warning("unbekannter SpT:" + spieltagDatum);
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
				if(spt==5){
					if(spieler.getStammMannschaftRR() < maxMannschaft){
						maxMannschaft = spieler.getStammMannschaftRR();
					}
					if(spieler.getVereinRR()!=null
                                        && !spieler.getVereinRR().equals(spieler.getVereinVR())){
						//zaehler wieder loeschen bei vereinswechsel
						mannschaftszaehler = new int[9];
						maxMannschaft = spieler.getStammMannschaftRR();
					}
				}
				int m = spieler.getMannschaftseinsatz()[spt][0];
				int m2 = spieler.getMannschaftseinsatz()[spt][1];
				if (m2 > 0) {
					if((m2 < m && m <= maxMannschaft)
					|| (m2 > m && m2 > maxMannschaft)){
						m = m2;
					}
				}
				if(m > 0){
					if(m > maxMannschaft){
						logger.warning("--> achtung FALSCHEINSATZ: " + spieler);
						falschspieler.add(spieler);
                                                if(spt>4){
                                                    if(spieler.getVereinRR()!=null){
                                                        spieler.getVereinRR().getFalschspieler().add(spieler);
                                                    }
                                                } else if(spieler.getVereinVR()!=null){
                                                        spieler.getVereinVR().getFalschspieler().add(spieler);
                                                }
					}
					mannschaftszaehler[m]++;
					if(mannschaftszaehler[m] >= 4 && m < maxMannschaft){
//						logger.info("festgespielt: " + spieler);
						maxMannschaft = m;
						festgespielt.add(spieler);
                                                if(spt>4){
                                                    if(spieler.getVereinRR()!=null){
                                                        spieler.getVereinRR().getFestgespielt().add(spieler);
                                                    }
                                                } else if(spieler.getVereinVR()!=null){
                                                        spieler.getVereinVR().getFestgespielt().add(spieler);
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
            return spieltage;
        }
}
