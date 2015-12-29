package org.proteored.miapeExtractor.analysis.exporters.tasks;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.proteored.miapeExtractor.analysis.exporters.Exporter;
import org.proteored.miapeExtractor.analysis.exporters.ExporterManager;
import org.proteored.miapeExtractor.analysis.exporters.util.ExportedColumns;
import org.proteored.miapeExtractor.analysis.exporters.util.ExporterUtil;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedPeptide;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.PeptideOccurrence;
import org.proteored.miapeapi.experiment.model.ProteinGroup;
import org.proteored.miapeapi.experiment.model.ProteinGroupOccurrence;
import org.proteored.miapeapi.experiment.model.sort.SorterUtil;

public class TSVExporter extends SwingWorker<Void, String> implements
		Exporter<File> {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private IdentificationSet idSet;
	private final char separator;
	private final File file;
	private final boolean includeReplicateAndExperimentOrigin;
	private final boolean includeDecoyHits;
	private final boolean showPeptides;
	private final boolean retrieveProteinSequences;
	private final boolean includeGeneInfo;
	private final boolean showBestPeptides;
	private final boolean showBestProteins;
	private final boolean excludeNonConclusiveProteins;
	private String error = null;

	private final boolean isFDRApplied;

	public TSVExporter(ExporterManager expManager, IdentificationSet idSet,
			File file) {

		this.file = file;
		this.separator = TAB;
		this.idSet = idSet;

		this.includeReplicateAndExperimentOrigin = expManager
				.isReplicateAndExperimentOriginIncluded();
		this.includeDecoyHits = expManager.isDecoyHitsIncluded();
		this.showPeptides = expManager.showPeptides();
		this.includeGeneInfo = expManager.isGeneInfoIncluded();
		this.showBestPeptides = expManager.showBestPeptides();
		this.showBestProteins = expManager.showBestProteins();
		this.retrieveProteinSequences = expManager.retrieveProteinSequences();
		this.excludeNonConclusiveProteins = !expManager
				.isNonConclusiveProteinsIncluded();
		this.isFDRApplied = expManager.isFDRApplied();
	}

	private void saveAs(String path) throws IOException {

		BufferedOutputStream out = null;
		out = null;
		// create file
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			List<String> columnsStringList = ExportedColumns.getColumnsString(
					this.includeReplicateAndExperimentOrigin,
					this.showPeptides, this.includeGeneInfo, this.isFDRApplied,
					this.idSet);
			String columnsString = ExporterUtil.getInstance(idSet,
					includeReplicateAndExperimentOrigin, showPeptides,
					includeGeneInfo, retrieveProteinSequences,
					excludeNonConclusiveProteins).getStringFromList(
					columnsStringList, separator)
					+ NEWLINE;

			log.info(columnsString);
			out.write(columnsString.getBytes());
			// final HashMap<String,
			// IdentificationOccurrence<ExtendedIdentifiedProtein>>
			// proteinOccurrenceList = this.idSet
			// .getProteinOccurrenceList();

			int progress = 0;
			// if (progress == 0)
			// return;
			if (this.showPeptides) {
				if (this.showBestPeptides) {
					final HashMap<String, PeptideOccurrence> peptideOccurrenceHashMap = this.idSet
							.getPeptideOccurrenceList(true);
					// sort if there is a FDR Filter activated that tells us
					// which is the score sort

					firePropertyChange(DATA_EXPORTING_SORTING, null,
							peptideOccurrenceHashMap.size());
					ArrayList<PeptideOccurrence> peptideOccurrenceList = new ArrayList<PeptideOccurrence>();
					for (PeptideOccurrence peptideOccurrence : peptideOccurrenceHashMap
							.values()) {
						peptideOccurrenceList.add(peptideOccurrence);
					}

					SorterUtil
							.sortPeptideOcurrencesByBestPeptideScore(peptideOccurrenceList);
					firePropertyChange(DATA_EXPORTING_SORTING_DONE, null, null);
					Iterator<PeptideOccurrence> iterator = peptideOccurrenceList
							.iterator();

					int total = peptideOccurrenceHashMap.size();
					int i = 1;
					while (iterator.hasNext()) {
						PeptideOccurrence peptideOccurrence = iterator.next();

						Thread.sleep(1);
						boolean isdecoy = peptideOccurrence.isDecoy();

						if (includeDecoyHits || (!includeDecoyHits && !isdecoy)) {
							final List<String> lineStringList = ExporterUtil
									.getInstance(
											idSet,
											includeReplicateAndExperimentOrigin,
											showPeptides, includeGeneInfo,
											retrieveProteinSequences,
											excludeNonConclusiveProteins)
									.getPeptideInfoList(peptideOccurrence,
											columnsStringList, i++);
							String lineString = ExporterUtil.getInstance(idSet,
									includeReplicateAndExperimentOrigin,
									showPeptides, includeGeneInfo,
									retrieveProteinSequences,
									excludeNonConclusiveProteins)
									.getStringFromList(lineStringList,
											separator)
									+ NEWLINE;
							// log.info(lineString);
							out.write(lineString.getBytes());
						}

						progress++;

						final int percentage = progress * 100 / total;
						log.info(percentage + " %");
						setProgress(percentage);
					}
				} else {
					final List<ExtendedIdentifiedPeptide> identifiedPeptides = this.idSet
							.getIdentifiedPeptides();
					// sort if there is a FDR Filter activated that tells us
					// which is the score sort

					firePropertyChange(DATA_EXPORTING_SORTING, null,
							identifiedPeptides.size());

					SorterUtil.sortPeptidesByBestPeptideScore(
							identifiedPeptides, true);
					firePropertyChange(DATA_EXPORTING_SORTING_DONE, null, null);

					int total = identifiedPeptides.size();
					int i = 1;
					for (ExtendedIdentifiedPeptide peptide : identifiedPeptides) {
						Thread.sleep(1);
						boolean isdecoy = peptide.isDecoy();
						if (includeDecoyHits || (!includeDecoyHits && !isdecoy)) {

							PeptideOccurrence peptideOccurrence = new PeptideOccurrence(
									peptide.getModificationString());
							peptideOccurrence.addOccurrence(peptide);
							final List<String> lineStringList = ExporterUtil
									.getInstance(
											idSet,
											includeReplicateAndExperimentOrigin,
											showPeptides, includeGeneInfo,
											retrieveProteinSequences,
											excludeNonConclusiveProteins)
									.getPeptideInfoList(peptideOccurrence,
											columnsStringList, i++);
							String lineString = ExporterUtil.getInstance(idSet,
									includeReplicateAndExperimentOrigin,
									showPeptides, includeGeneInfo,
									retrieveProteinSequences,
									excludeNonConclusiveProteins)
									.getStringFromList(lineStringList,
											separator)
									+ NEWLINE;
							// log.info(lineString);
							out.write(lineString.getBytes());
						}

						progress++;
						final int percentage = progress * 100 / total;
						log.info(percentage + " %");
						setProgress(percentage);
					}
				}
			} else {
				// JUST PROTEINS
				if (this.showBestProteins) {
					final Collection<ProteinGroupOccurrence> proteinOccurrenceSet = this.idSet
							.getProteinGroupOccurrenceList().values();
					final List<ProteinGroupOccurrence> proteinGroupOccurrenceList = new ArrayList<ProteinGroupOccurrence>();
					for (ProteinGroupOccurrence proteinGroupOccurrence : proteinOccurrenceSet) {
						proteinGroupOccurrenceList.add(proteinGroupOccurrence);
					}
					// sort if there is a FDR Filter activated that tells us
					// which is the score sort

					firePropertyChange(DATA_EXPORTING_SORTING, null,
							proteinGroupOccurrenceList.size());
					try {
						SorterUtil
								.sortProteinGroupOcurrencesByBestPeptideScore(proteinGroupOccurrenceList);
					} catch (Exception e) {

					}
					firePropertyChange(DATA_EXPORTING_SORTING_DONE, null, null);
					Iterator<ProteinGroupOccurrence> iterator = proteinGroupOccurrenceList
							.iterator();

					int total = proteinGroupOccurrenceList.size();
					int i = 1;
					while (iterator.hasNext()) {
						ProteinGroupOccurrence proteinGroupOccurrence = iterator
								.next();
						Thread.sleep(1);
						boolean isdecoy = proteinGroupOccurrence.isDecoy();
						boolean pass = true;
						if (excludeNonConclusiveProteins
								&& ExporterUtil
										.isNonConclusiveProtein(proteinGroupOccurrence)) {
							pass = false;
							// log.info("Non conclusive protein skipped");
						}
						if (pass
								&& (includeDecoyHits || (!includeDecoyHits && !isdecoy))) {
							final List<String> proteinStringList = ExporterUtil
									.getInstance(
											idSet,
											includeReplicateAndExperimentOrigin,
											showPeptides, includeGeneInfo,
											retrieveProteinSequences,
											excludeNonConclusiveProteins)
									.getProteinInfoList(proteinGroupOccurrence,
											columnsStringList, i++);
							String proteinString = ExporterUtil.getInstance(
									idSet, includeReplicateAndExperimentOrigin,
									showPeptides, includeGeneInfo,
									retrieveProteinSequences,
									excludeNonConclusiveProteins)
									.getStringFromList(proteinStringList,
											separator)
									+ NEWLINE;
							// System.out.println(peptideString);
							out.write(proteinString.getBytes());
						}

						progress++;
						final int percentage = progress * 100 / total;
						log.info(percentage + " %");
						setProgress(percentage);
					}
				} else {
					final List<ProteinGroup> proteinGroups = this.idSet
							.getIdentifiedProteinGroups();
					// sort if there is a FDR Filter activated that tells us
					// which is the score sort

					firePropertyChange(DATA_EXPORTING_SORTING, null,
							proteinGroups.size());

					SorterUtil
							.sortProteinGroupsByBestPeptideScore(proteinGroups);
					firePropertyChange(DATA_EXPORTING_SORTING_DONE, null, null);

					int total = proteinGroups.size();
					int i = 1;
					for (ProteinGroup proteinGroup : proteinGroups) {
						Thread.sleep(1);
						boolean isdecoy = proteinGroup.isDecoy();
						if (includeDecoyHits || (!includeDecoyHits && !isdecoy)) {
							ProteinGroupOccurrence proteinOccurrence = new ProteinGroupOccurrence();
							proteinOccurrence.addOccurrence(proteinGroup);
							final List<String> proteinStringList = ExporterUtil
									.getInstance(
											idSet,
											includeReplicateAndExperimentOrigin,
											showPeptides, includeGeneInfo,
											retrieveProteinSequences,
											excludeNonConclusiveProteins)
									.getProteinInfoList(proteinOccurrence,
											columnsStringList, i++);
							String proteinString = ExporterUtil.getInstance(
									idSet, includeReplicateAndExperimentOrigin,
									showPeptides, includeGeneInfo,
									retrieveProteinSequences,
									excludeNonConclusiveProteins)
									.getStringFromList(proteinStringList,
											separator)
									+ NEWLINE;
							// System.out.println(peptideString);
							out.write(proteinString.getBytes());
						}

						progress++;
						final int percentage = progress * 100 / total;
						log.info(percentage + " %");
						setProgress(percentage);
					}
				}
			}
		} catch (Exception e) {
			if (!(e instanceof InterruptedException)) {
				e.printStackTrace();
				error = e.getMessage();
			}
		} finally {

			// Close the BufferedOutputStream
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			if (error != null)
				this.cancel(true);
		}

	}

	public void save() throws IOException {
		saveAs(file.getAbsolutePath());
	}

	public IdentificationSet getExperiment() {
		return idSet;
	}

	public void setExperiment(Experiment experiment) {
		this.idSet = experiment;
	}

	public char getSeparator() {
		return separator;
	}

	@Override
	protected Void doInBackground() {
		export();
		return null;
	}

	@Override
	protected void done() {

		if (!this.isCancelled())
			firePropertyChange(DATA_EXPORTING_DONE, null, this.file);
		else {
			if (error != null) {
				firePropertyChange(DATA_EXPORTING_ERROR, null, error);
			} else {
				firePropertyChange(DATA_EXPORTING_CANCELED, null, null);
			}
		}
	}

	@Override
	public File export() {
		try {
			save();
		} catch (IOException e) {
			firePropertyChange(DATA_EXPORTING_ERROR, null, e.getMessage());
			e.printStackTrace();
			this.cancel(true);
		}
		return null;
	}

}
