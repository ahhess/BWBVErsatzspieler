package bwbv.ersatzspielercheck.model;

import bwbv.ersatzspielercheck.SpielerMap;
import java.util.ArrayList;
import java.util.List;

public class Verein implements Comparable<String> {

	private String nummer;
	private String name;
	private String bezirk;
	/** Map mit Spielern dieses Vereins */
	private SpielerMap spielerMap = new SpielerMap();
	private SpielerMap ersatzspielerMap = new SpielerMap();
	private List<Spieler> festgespielt = new ArrayList<Spieler>();
	private List<Spieler> falschspieler = new ArrayList<Spieler>();

	public Verein() {
	}

	public Verein(String nummer, String name, String bezirk) {
		this.nummer = nummer;
		this.name = name;
		this.bezirk = bezirk;
	}

	public String getNummer() {
		return nummer;
	}

	public void setNummer(String nummer) {
		this.nummer = nummer;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBezirk() {
		return bezirk;
	}

	public void setBezirk(String bezirk) {
		this.bezirk = bezirk;
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

	public List<Spieler> getFalschspieler() {
		return falschspieler;
	}

	public void setFalschspieler(List<Spieler> falschspieler) {
		this.falschspieler = falschspieler;
	}

	public List<Spieler> getFestgespielt() {
		return festgespielt;
	}

	public void setFestgespielt(List<Spieler> festgespielt) {
		this.festgespielt = festgespielt;
	}

	@Override
	public String toString() {
		return name + " (" + nummer + ")";
	}

	public String toXML() {
		return "<Verein name=\"" + name
		// + " (" + bezirk + ")"
				+ "\" nr=\"" + nummer + "\"/>";
	}

	@Override
	public int compareTo(String o) {
		if (name == null)
			return -1;
		return name.compareToIgnoreCase(o);
	}
}
