package bwbv.ersatzspielercheck.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Spieler implements Comparable<String> {

    private static Logger logger = Logger.getLogger(Spieler.class.getName());
    private String nr;
    private String passnr;
    private String nachname;
    private String vorname;
    private String geschlecht;
    private int rangVR = 0;
    private int rangRR = 0;
    private int stammMannschaftVR = 0;
    private int stammMannschaftRR = 0;
    private Verein vereinVR;
    private Verein vereinRR;
    
    /** 
     * SpieltagsEinsaetze
     * key: Spieltagsdatum
     */
    private Map<String, List<Einsatz>> spieltagsEinsaetze = new HashMap<String, List<Einsatz>>();

    /**
     * Mannschaftseinsaetze: Kurzzusammenfassung der Spielereinsaetze in Mannschaft x pro SpT und VR/RR/effektiv nach SpT.
     * 1. index: SpT 1-10 (5=4a, 10=8a; 0=unbenutzt)
     * 2. Index: 0=VR, 1=RR, 3=effektiv (= ggfs. festgespielt) in Mannschaft nach SpT
     */
    private int[][] mannschaftseinsatz = new int[10][3];


    public Spieler(String nr, String passnr, String nachname, String vorname, String geschlecht) {
        super();
        this.nr = nr;
        this.passnr = passnr;
        this.nachname = nachname;
        this.vorname = vorname;
        this.geschlecht = geschlecht;
    }

    @Override
    public String toString() {
        return getGanzerName() + " (" + nr + ", " + rangVR + "/" + rangRR + ")";
    }

    public String mannschaftseinsatzToString(int spt) {
        return "" + mannschaftseinsatz[spt][0]
            + "-" + mannschaftseinsatz[spt][1]
            + " = " + mannschaftseinsatz[spt][2];
    }

    public String toXML() {
        return "<Spieler nr=\"" + nr + "\" passnr=\"" + passnr + "\" nachname=\"" + nachname + "\" vorname=\"" + vorname + "\">"
                + "<VR stammMannschaft=\"" + stammMannschaftVR + "\" rang=\"" + rangVR + "\">" + vereinVR + "</VR>" 
                + "<RR stammMannschaft=\"" + stammMannschaftRR + "\" rang=\"" + rangRR + "\">" + vereinRR + "</RR>" 
                + mannschaftseinsatzToXML() 
                + "<SpieltagsEinsaetze>" + spieltagsEinsaetze + "</SpieltagsEinsaetze>" 
                + "</Spieler>";
    }

    private String mannschaftseinsatzToXML() {
        String s = "<Mannschaftseinsaetze>";
        for (int i = 0; i < 10; i++) {
            s += "<me spt=\"" + (i+1) + "\" m1=\"" + mannschaftseinsatz[i][0] + "\" m2=\"" + mannschaftseinsatz[i][1]
                    + "\"/>";
        }
        return s + "</Mannschaftseinsaetze>";
    }

    public String getNr() {
        return nr;
    }

    public void setNr(String nr) {
        this.nr = nr;
    }

    public String getPassnr() {
        return passnr;
    }

    public void setPassnr(String passnr) {
        this.passnr = passnr;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public int getStammMannschaftVR() {
        return stammMannschaftVR;
    }

    public void setStammMannschaftVR(int mannschaft) {
        this.stammMannschaftVR = mannschaft;
        // this.maxMannschaft = mannschaft;
    }

    public int getStammMannschaftRR() {
        return stammMannschaftRR;
    }

    public void setStammMannschaftRR(int mannschaft) {
        this.stammMannschaftRR = mannschaft;
    }

    public Verein getVereinVR() {
        return vereinVR;
    }

    public void setVereinVR(Verein verein) {
        this.vereinVR = verein;
    }

    public Verein getVereinRR() {
        return vereinRR;
    }

    public void setVereinRR(Verein verein) {
        this.vereinRR = verein;
    }

    public Map<String, List<Einsatz>> getSpieltagsEinsaetze() {
        return spieltagsEinsaetze;
    }

    public int[][] getMannschaftseinsatz() {
        return mannschaftseinsatz;
    }

    public int getRangRR() {
        return rangRR;
    }

    public void setRangRR(int rangRR) {
        this.rangRR = rangRR;
    }

    public int getRangVR() {
        return rangVR;
    }

    public void setRangVR(int rangVR) {
        this.rangVR = rangVR;
    }

    private String getGanzerName() {
         return nachname + ", " + vorname;
    }

    @Override
    public int compareTo(String o) {
        return getGanzerName().compareTo(o);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Spieler){
            Spieler s = (Spieler)obj;
            return getGanzerName().equals(s.getGanzerName());
        } else 
            return super.equals(obj);
    }

	public String getGeschlecht() {
		return geschlecht;
	}

	public void setGeschlecht(String geschlecht) {
		this.geschlecht = geschlecht;
	}

}
