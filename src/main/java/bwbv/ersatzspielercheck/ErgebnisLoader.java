package bwbv.ersatzspielercheck;

import java.util.Properties;
import java.util.logging.Logger;

public abstract class ErgebnisLoader extends CSVLoader {

    // private static Logger logger = Logger.getLogger(ErgebnisLoader.class.getName());

    // constants from properties:
    private int EINSATZSPNRH1 = 29;
    private int EINSATZSPNRH2 = 34;
    private int EINSATZSPNRG1 = 39;
    private int EINSATZSPNRG2 = 44;
    private int EINSATZVNRH = 12;
    private int EINSATZVNRG = 17;
    private int EINSATZMNRH = 15;
    private int EINSATZMNRG = 20;
    private int EINSATZTERMIN = 8;
    private int EINSATZTERMINURSPR = 9;
    private int EINSATZVR = 6;
    private int EINSATZDISZ = 28;

    // private Properties config;

    public ErgebnisLoader(Properties config) {
        // this.config = config;

        EINSATZVNRH = ErsatzspielerCheck.getNumericConfigProp(config, "EINSATZVNRH", EINSATZVNRH);
        EINSATZVNRG = ErsatzspielerCheck.getNumericConfigProp(config, "EINSATZVNRG", EINSATZVNRG);
        EINSATZMNRH = ErsatzspielerCheck.getNumericConfigProp(config, "EINSATZMNRH", EINSATZMNRH);
        EINSATZMNRG = ErsatzspielerCheck.getNumericConfigProp(config, "EINSATZMNRG", EINSATZMNRG);
        EINSATZSPNRH1 = ErsatzspielerCheck.getNumericConfigProp(config, "EINSATZSPNRH1", EINSATZSPNRH1);
        EINSATZSPNRH2 = ErsatzspielerCheck.getNumericConfigProp(config, "EINSATZSPNRH2", EINSATZSPNRH2);
        EINSATZSPNRG1 = ErsatzspielerCheck.getNumericConfigProp(config, "EINSATZSPNRG1", EINSATZSPNRG1);
        EINSATZSPNRG2 = ErsatzspielerCheck.getNumericConfigProp(config, "EINSATZSPNRG2", EINSATZSPNRG2);
        EINSATZTERMIN = ErsatzspielerCheck.getNumericConfigProp(config, "EINSATZTERMIN", EINSATZTERMIN);
        EINSATZTERMINURSPR = ErsatzspielerCheck.getNumericConfigProp(config, "EINSATZTERMINURSPR", EINSATZTERMINURSPR);
        EINSATZVR = ErsatzspielerCheck.getNumericConfigProp(config, "EINSATZVR", EINSATZVR);
        EINSATZDISZ = ErsatzspielerCheck.getNumericConfigProp(config, "EINSATZDISZ", EINSATZDISZ);
    }

    public ErgebnisRecord getErgebnisRecord(String[] cols) {
        ErgebnisRecord rec = new ErgebnisRecord();

        rec.disziplin = cols[EINSATZDISZ];
        rec.runde = cols[EINSATZVR];

        if ("".equals(cols[EINSATZTERMINURSPR])) {
            rec.termin = cols[EINSATZTERMIN];
        } else {
            rec.termin = cols[EINSATZTERMINURSPR];
        }

        rec.spielerNr[0] = cols[EINSATZSPNRH1];
        rec.spielerNr[1] = cols[EINSATZSPNRH2];
        rec.spielerNr[2] = cols[EINSATZSPNRG1];
        rec.spielerNr[3] = cols[EINSATZSPNRG2];

        rec.mannschaftNr[0] = cols[EINSATZMNRH];
        rec.mannschaftNr[1] = cols[EINSATZMNRH];
        rec.mannschaftNr[2] = cols[EINSATZMNRG];
        rec.mannschaftNr[3] = cols[EINSATZMNRG];

        return rec;
    }
}
