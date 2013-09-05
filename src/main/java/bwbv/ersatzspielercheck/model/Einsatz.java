package bwbv.ersatzspielercheck.model;

public class Einsatz {
	
	private String datum;
//	private String disz;
	private int mannschaft;
	private String spieltag;
	
	@Override
	public String toString() {
		return datum + " SpT " + spieltag + " M " + mannschaft;
	}

	public String toXML() {
		return "<Einsatz spt=\"" + spieltag
		+ "\" datum=\"" + datum 
		+ "\" mannschaft=\"" + mannschaft 
		//+ ", disz=" + disz 
		+ "\"/>";
	}
	
	public String getDatum() {
		return datum;
	}

	public void setDatum(String datum) {
		this.datum = datum;
	}
//
//	public String getDisz() {
//		return disz;
//	}
//
//	public void setDisz(String disz) {
//		this.disz = disz;
//	}

	public int getMannschaft() {
		return mannschaft;
	}

	public void setMannschaft(int mannschaft) {
		this.mannschaft = mannschaft;
	}

	public String getSpieltag() {
		return spieltag;
	}

	public void setSpieltag(String spieltag) {
		this.spieltag = spieltag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datum == null) ? 0 : datum.hashCode());
//		result = prime * result + ((disz == null) ? 0 : disz.hashCode());
		result = prime * result + mannschaft;
		result = prime * result + ((spieltag == null) ? 0 : spieltag.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Einsatz other = (Einsatz) obj;
		if (datum == null) {
			if (other.datum != null)
				return false;
		} else if (!datum.equals(other.datum))
			return false;
//		if (disz == null) {
//			if (other.disz != null)
//				return false;
//		} else if (!disz.equals(other.disz))
//			return false;
		if (mannschaft != other.mannschaft)
			return false;
		if (spieltag == null) {
			if (other.spieltag != null)
				return false;
		} else if (!spieltag.equals(other.spieltag))
			return false;
		return true;
	}

}
