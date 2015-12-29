package org.proteored.miapeExtractor.analysis.exporters.tasks;

public class PexExportingMessage {
	private final String body;
	private final String tip;

	public PexExportingMessage(String body, String tip) {
		super();
		this.body = body;
		this.tip = tip;
	}

	public String getBody() {
		return body;
	}

	public String getTip() {
		return tip;
	}

}
