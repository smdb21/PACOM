package org.proteored.miapeExtractor.analysis.exporters.util;

import java.util.ArrayList;
import java.util.List;

import org.proteored.miapeapi.experiment.model.Replicate;

public class PexFile {
	private List<Replicate> associatedReplicates = new ArrayList<Replicate>();
	private final String rawFileLocation;

	public PexFile(String rawFileLocation) {
		this.rawFileLocation = rawFileLocation;
	}

	public PexFile(String rawFileLocation, List<Replicate> replicates) {
		this.rawFileLocation = rawFileLocation;
		this.associatedReplicates = replicates;
	}

	public PexFile(String rawFileLocation, Replicate replicate) {
		this.rawFileLocation = rawFileLocation;
		this.associatedReplicates.add(replicate);
	}

	public List<Replicate> getAssociatedReplicates() {
		return associatedReplicates;
	}

	public void setAssociatedReplicates(List<Replicate> associatedReplicates) {
		this.associatedReplicates.addAll(associatedReplicates);
	}

	public void addAssociatedReplicate(Replicate associatedReplicate) {
		this.associatedReplicates.add(associatedReplicate);
	}

	public String getFileLocation() {
		return rawFileLocation;
	}

}
