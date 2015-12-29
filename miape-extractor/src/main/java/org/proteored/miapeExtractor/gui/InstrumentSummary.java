package org.proteored.miapeExtractor.gui;

import java.util.ArrayList;
import java.util.List;

public class InstrumentSummary {
	private String name;
	private String manufacturer;
	private final List<String> ionSources;
	private final List<String> analyzers;
	private String activation;

	public void setActivation(String activation) {
		if (this.activation !=null && !this.activation.equals(""))
			this.activation = this.activation + ", " + activation;
		else
			this.activation = activation;
	}

	public String getActivation() {
		return activation;
	}

	public InstrumentSummary() {
		name = "";
		manufacturer = "";
		ionSources = new ArrayList<String>();
		analyzers  = new ArrayList<String>();
		activation = "";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public List<String> getIonSources() {
		return ionSources;
	}

	public void setIonSources(String ionSources) {
		this.ionSources.add(ionSources);
	}

	public List<String> getAnalyzers() {
		return analyzers;
	}

	public void setAnalyzers(String analyzers) {
		this.analyzers.add(analyzers);
	}

}
