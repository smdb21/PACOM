package org.proteored.pacom.analysis.exporters.tasks;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedPeptide;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedProtein;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.PeptideOccurrence;
import org.proteored.miapeapi.experiment.model.ProteinGroup;
import org.proteored.miapeapi.experiment.model.ProteinGroupOccurrence;
import org.proteored.miapeapi.experiment.model.filters.Filters;
import org.proteored.miapeapi.experiment.model.sort.ProteinGroupComparisonType;
import org.proteored.pacom.analysis.exporters.Exporter;
import org.proteored.pacom.analysis.exporters.ExporterManager;
import org.proteored.pacom.analysis.exporters.util.ExportedColumns;
import org.proteored.pacom.analysis.exporters.util.ExporterUtil;
import org.proteored.pacom.analysis.util.FileManager;

import edu.scripps.yates.annotations.uniprot.UniprotProteinLocalRetriever;
import edu.scripps.yates.annotations.uniprot.xml.Entry;
import edu.scripps.yates.utilities.fasta.FastaParser;
import edu.scripps.yates.utilities.progresscounter.ProgressCounter;
import edu.scripps.yates.utilities.progresscounter.ProgressPrintingType;
import gnu.trove.set.hash.THashSet;

public class TSVExporter extends SwingWorker<Void, String> implements Exporter<File> {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private Set<IdentificationSet> idSets = new THashSet<IdentificationSet>();
	private final char separator;
	private final File file;
	private final boolean includeDecoyHits;
	private final boolean showPeptides;
	private final boolean retrieveProteinSequences;
	private final boolean includeGeneInfo;
	private final boolean showBestPeptides;
	private final boolean showBestProteins;
	private String error = null;

	private final boolean isFDRApplied;

	private final Filters filter;

	private boolean distinguisModificatedPeptides;

	private ProteinGroupComparisonType comparisonType;

	public TSVExporter(ExporterManager expManager, Collection<IdentificationSet> idSets, File file, Filters filter) {

		this.file = file;
		this.separator = TAB;
		this.idSets.addAll(idSets);
		this.filter = filter;
		this.comparisonType = expManager.getComparisonType();
		this.includeDecoyHits = expManager.isDecoyHitsIncluded();
		this.showPeptides = expManager.showPeptides();
		this.includeGeneInfo = expManager.isGeneInfoIncluded();
		this.showBestPeptides = expManager.showBestPeptides();
		this.showBestProteins = expManager.showBestProteins();
		this.retrieveProteinSequences = expManager.retrieveProteinSequences();
		this.isFDRApplied = expManager.isFDRApplied();
	}

	private void saveAs(String path) throws IOException {

		BufferedOutputStream out = null;
		out = null;
		// create file
		try {

			if (this.retrieveProteinSequences || this.includeGeneInfo) {

				Set<String> uniprotAccs = new THashSet<String>();
				for (IdentificationSet identificationSet : idSets) {
					List<ExtendedIdentifiedProtein> identifiedProteins = identificationSet.getIdentifiedProteins();
					for (ExtendedIdentifiedProtein protein : identifiedProteins) {
						String uniProtACC = FastaParser.getUniProtACC(protein.getAccession());
						if (uniProtACC != null) {
							uniprotAccs.add(uniProtACC);
						}
					}
				}
				// get all sequences at once first
				if (!uniprotAccs.isEmpty()) {
					try {
						firePropertyChange(PROTEIN_SEQUENCE_RETRIEVAL, null, "Retrieving protein sequences from "
								+ uniprotAccs.size() + " different proteins in UniprotKB");

						UniprotProteinLocalRetriever upr = FileManager.getUniprotProteinLocalRetriever();
						upr.setCacheEnabled(true);
						Map<String, Entry> annotatedProteins = upr.getAnnotatedProteins(null, uniprotAccs);
					} finally {
						firePropertyChange(PROTEIN_SEQUENCE_RETRIEVAL_DONE, null, null);
					}
					// and just keep that in the cache
				}
			}

			out = new BufferedOutputStream(new FileOutputStream(file));

			List<String> columnsStringList = ExportedColumns.getColumnsString(this.showPeptides, this.includeGeneInfo,
					this.isFDRApplied, idSets);
			ExporterUtil exporterUtil = ExporterUtil.getInstance(idSets, showPeptides, includeGeneInfo,
					retrieveProteinSequences);
			String columnsString = exporterUtil.getStringFromList(columnsStringList, separator) + NEWLINE;

			log.info(columnsString);
			out.write(columnsString.getBytes());

			// final Map<String,
			// IdentificationOccurrence<ExtendedIdentifiedProtein>>
			// proteinOccurrenceList = this.idSet
			// .getProteinOccurrenceList();
			int i = 1;
			for (IdentificationSet idSet : idSets) {
				// if (progress == 0)
				// return;
				if (this.showPeptides) {
					if (this.showBestPeptides) {

						Collection<PeptideOccurrence> peptideOccurrenceList = getPeptideOccurrenceListToExport(idSet);

						// firePropertyChange(DATA_EXPORTING_SORTING, null,
						// peptideOccurrenceList.size());

						// SorterUtil.sortPeptideOcurrencesByBestPeptideScore(peptideOccurrenceList);
						// firePropertyChange(DATA_EXPORTING_SORTING_DONE, null,
						// null);
						Iterator<PeptideOccurrence> iterator = peptideOccurrenceList.iterator();

						int total = peptideOccurrenceList.size();
						ProgressCounter counter = new ProgressCounter(total, ProgressPrintingType.PERCENTAGE_STEPS, 0);

						while (iterator.hasNext()) {
							PeptideOccurrence peptideOccurrence = iterator.next();

							Thread.sleep(1);

							final List<String> peptideColumns = exporterUtil.getPeptideInfoList(peptideOccurrence,
									columnsStringList, i++, idSet);
							String lineString = exporterUtil.getStringFromList(peptideColumns, separator) + NEWLINE;
							// log.info(lineString);
							out.write(lineString.getBytes());

							counter.increment();

							final String percentage = counter.printIfNecessary();
							if (!"".equals(percentage)) {
								log.info(percentage);
							}
							setProgress(Double.valueOf(counter.getPercentage()).intValue());
						}
					} else {
						final Collection<ExtendedIdentifiedPeptide> peptidelistToExport = getPeptideListToExport(idSet);
						// sort if there is a FDR Filter activated that tells us
						// which is the score sort
						// firePropertyChange(DATA_EXPORTING_SORTING, null,
						// peptidelistToExport.size());
						// SorterUtil.sortPeptidesByBestPeptideScore(peptidelistToExport,
						// true);
						// firePropertyChange(DATA_EXPORTING_SORTING_DONE, null,
						// null);

						int total = peptidelistToExport.size();
						ProgressCounter counter = new ProgressCounter(total, ProgressPrintingType.PERCENTAGE_STEPS, 0);

						for (ExtendedIdentifiedPeptide peptide : peptidelistToExport) {

							Thread.sleep(1);

							PeptideOccurrence peptideOccurrence = new PeptideOccurrence(
									peptide.getModificationString());
							peptideOccurrence.addOccurrence(peptide);
							final List<String> lineStringList = exporterUtil.getPeptideInfoList(peptideOccurrence,
									columnsStringList, i++, idSet);
							String lineString = exporterUtil.getStringFromList(lineStringList, separator) + NEWLINE;
							// log.info(lineString);
							out.write(lineString.getBytes());

							counter.increment();

							final String percentage = counter.printIfNecessary();
							if (!"".equals(percentage)) {
								log.info(percentage);
							}
							setProgress(Double.valueOf(counter.getPercentage()).intValue());
						}
					}
				} else {
					// JUST PROTEINS
					if (this.showBestProteins) {
						Collection<ProteinGroupOccurrence> proteinGroupOccurrenceList = getProteinGroupOccurrenceToExport(
								idSet);

						// sort if there is a FDR Filter activated that tells us
						// which is the score sort

						// firePropertyChange(DATA_EXPORTING_SORTING, null,
						// proteinGroupOccurrenceList.size());
						try {
							// SorterUtil.sortProteinGroupOcurrencesByBestPeptideScore(proteinGroupOccurrenceList);
						} catch (Exception e) {

						}
						// firePropertyChange(DATA_EXPORTING_SORTING_DONE, null,
						// null);
						Iterator<ProteinGroupOccurrence> iterator = proteinGroupOccurrenceList.iterator();

						int total = proteinGroupOccurrenceList.size();
						ProgressCounter counter = new ProgressCounter(total, ProgressPrintingType.PERCENTAGE_STEPS, 0);

						while (iterator.hasNext()) {
							ProteinGroupOccurrence proteinGroupOccurrence = iterator.next();
							Thread.sleep(1);

							final List<String> proteinStringList = exporterUtil
									.getProteinInfoList(proteinGroupOccurrence, columnsStringList, i++, idSet);
							String proteinString = exporterUtil.getStringFromList(proteinStringList, separator)
									+ NEWLINE;
							// System.out.println(peptideString);
							out.write(proteinString.getBytes());

							counter.increment();

							final String percentage = counter.printIfNecessary();
							if (!"".equals(percentage)) {
								log.info(percentage);
							}
							setProgress(Double.valueOf(counter.getPercentage()).intValue());
						}
					} else {
						final List<ProteinGroup> proteinGroupsToExport = getProteinGroupsToExport(idSet);
						// sort if there is a FDR Filter activated that tells us
						// which is the score sort
						// firePropertyChange(DATA_EXPORTING_SORTING, null,
						// proteinGroupsToExport.size());

						// SorterUtil.sortProteinGroupsByBestPeptideScore(proteinGroupsToExport);
						// firePropertyChange(DATA_EXPORTING_SORTING_DONE, null,
						// null);

						int total = proteinGroupsToExport.size();
						ProgressCounter counter = new ProgressCounter(total, ProgressPrintingType.PERCENTAGE_STEPS, 0);

						for (ProteinGroup proteinGroup : proteinGroupsToExport) {

							Thread.sleep(1);

							ProteinGroupOccurrence proteinOccurrence = new ProteinGroupOccurrence();
							proteinOccurrence.addOccurrence(proteinGroup);

							final List<String> proteinStringList = exporterUtil.getProteinInfoList(proteinOccurrence,
									columnsStringList, i++, idSet);
							String proteinString = exporterUtil.getStringFromList(proteinStringList, separator)
									+ NEWLINE;
							// System.out.println(peptideString);
							out.write(proteinString.getBytes());

							counter.increment();

							final String percentage = counter.printIfNecessary();
							if (!"".equals(percentage)) {
								log.info(percentage);
							}
							setProgress(Double.valueOf(counter.getPercentage()).intValue());
						}
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
			if (error != null) {
				this.cancel(true);
			}
		}

	}

	private Collection<PeptideOccurrence> getPeptideOccurrenceListToExport(IdentificationSet idSet) {
		Collection<PeptideOccurrence> peptideOccurrences = idSet.getPeptideOccurrenceList(distinguisModificatedPeptides)
				.values();
		if (this.filter == null && includeDecoyHits) {
			return peptideOccurrences;
		}
		List<PeptideOccurrence> ret = new ArrayList<PeptideOccurrence>();
		for (PeptideOccurrence peptideOccurrence : peptideOccurrences) {
			if (!includeDecoyHits && peptideOccurrence.isDecoy()) {
				continue;
			}
			if (filter != null) {
				String key = peptideOccurrence.getKey();
				if (filter.canCheck(key)) {
					if (!filter.isValid(key)) {
						continue;
					}
				} else {
					throw new IllegalMiapeArgumentException("Filter " + filter + " cannot check peptide sequences");
				}
			}
			ret.add(peptideOccurrence);
		}
		return ret;

	}

	private Collection<ExtendedIdentifiedPeptide> getPeptideListToExport(IdentificationSet idSet) {
		if (this.filter == null && includeDecoyHits) {
			return idSet.getIdentifiedPeptides();
		}
		List<ExtendedIdentifiedPeptide> peptidelistToExport = new ArrayList<ExtendedIdentifiedPeptide>();

		List<ExtendedIdentifiedPeptide> identifiedPeptides = idSet.getIdentifiedPeptides();
		for (ExtendedIdentifiedPeptide extendedIdentifiedPeptide : identifiedPeptides) {
			if (!includeDecoyHits && extendedIdentifiedPeptide.isDecoy()) {
				continue;
			}
			if (filter != null) {
				String key = extendedIdentifiedPeptide.getKey(distinguisModificatedPeptides);
				if (filter.canCheck(key)) {
					if (!filter.isValid(key)) {
						continue;
					}
				} else {
					throw new IllegalMiapeArgumentException("Filter " + filter + " cannot check peptide sequences");
				}
			}
			peptidelistToExport.add(extendedIdentifiedPeptide);
		}
		return peptidelistToExport;
	}

	private List<ProteinGroup> getProteinGroupsToExport(IdentificationSet idSet) {
		List<ProteinGroup> identifiedProteinGroups = idSet.getIdentifiedProteinGroups();
		if (filter == null && includeDecoyHits) {
			return identifiedProteinGroups;
		}

		List<ProteinGroup> ret = new ArrayList<ProteinGroup>();
		for (ProteinGroup proteinGroup : identifiedProteinGroups) {
			if (!includeDecoyHits && proteinGroup.isDecoy()) {
				continue;
			}
			ProteinGroupOccurrence proteinOccurrence = new ProteinGroupOccurrence();
			proteinOccurrence.addOccurrence(proteinGroup);
			Object key = proteinOccurrence.getKey(comparisonType);
			if (filter != null) {
				if (filter.canCheck(key)) {
					if (!filter.isValid(key)) {
						continue;
					}
				} else {
					throw new IllegalMiapeArgumentException("Filter " + filter + " cannot check protein keys");
				}
			}
			ret.add(proteinGroup);
		}
		return ret;

	}

	private Collection<ProteinGroupOccurrence> getProteinGroupOccurrenceToExport(IdentificationSet idSet) {
		Collection<ProteinGroupOccurrence> proteinGroupOccurrences = idSet.getProteinGroupOccurrenceList().values();
		if (filter == null && includeDecoyHits) {
			return proteinGroupOccurrences;
		}

		List<ProteinGroupOccurrence> ret = new ArrayList<ProteinGroupOccurrence>();
		for (ProteinGroupOccurrence proteinOccurrence : proteinGroupOccurrences) {
			if (!includeDecoyHits && proteinOccurrence.isDecoy()) {
				continue;
			}
			if (filter != null) {
				Object key = proteinOccurrence.getKey(comparisonType);
				if (filter.canCheck(key)) {
					if (!filter.isValid(key)) {
						continue;
					}
				} else {
					throw new IllegalMiapeArgumentException("Filter " + filter + " cannot check protein keys");
				}
			}
			ret.add(proteinOccurrence);
		}
		return ret;

	}

	public void save() throws IOException {
		saveAs(file.getAbsolutePath());
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

	public void setDistinguisModificatedPeptides(boolean distinguisModificatedPeptides) {
		this.distinguisModificatedPeptides = distinguisModificatedPeptides;
	}

}
