package org.proteored.miapeExtractor.analysis.exporters;

public interface ExporterManager {

	public boolean isReplicateAndExperimentOriginIncluded();

	public boolean isDecoyHitsIncluded();

	public boolean isFDRApplied();

	public boolean showPeptides();

	public boolean isGeneInfoIncluded();

	public boolean showBestPeptides();

	public boolean showBestProteins();

	public boolean retrieveProteinSequences();

	public boolean isNonConclusiveProteinsIncluded();

}
