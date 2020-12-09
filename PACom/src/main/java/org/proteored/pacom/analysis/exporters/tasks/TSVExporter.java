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
import edu.scripps.yates.utilities.annotations.uniprot.xml.Entry;
import edu.scripps.yates.utilities.fasta.FastaParser;
import edu.scripps.yates.utilities.progresscounter.ProgressCounter;
import edu.scripps.yates.utilities.progresscounter.ProgressPrintingType;
import gnu.trove.set.hash.THashSet;

public class TSVExporter extends SwingWorker<Void, String> implements Exporter<File> {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private final Set<IdentificationSet> idSets = new THashSet<IdentificationSet>();
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

	private final ProteinGroupComparisonType comparisonType;

	public TSVExporter(ExporterManager expManager, Collection<IdentificationSet> idSets, File file, Filters filter) {

		this.file = file;
		this.separator = TAB;
		this.idSets.addAll(idSets);
		this.filter = filter;
		this.comparisonType = expManager.getComparisonType();
		this.includeDecoyHits = expManager.isDecoyHitsIncluded();
		this.showPeptides = expManager.showPeptides();
		this.includeGeneInfo = expManager.showGeneInfo();
		this.showBestPeptides = expManager.showBestPeptides();
		this.showBestProteins = expManager.showBestProteins();
		this.retrieveProteinSequences = expManager.retrieveFromUniprotKB();
		this.isFDRApplied = expManager.isFDRApplied();
	}

	private void saveAs(String path) throws IOException {

		BufferedOutputStream out = null;
		out = null;
		// create file
		try {

			if (this.retrieveProteinSequences) {

				final Set<String> uniprotAccs = new THashSet<String>();
				for (final IdentificationSet identificationSet : idSets) {
					final List<ExtendedIdentifiedProtein> identifiedProteins = identificationSet
							.getIdentifiedProteins();
					for (final ExtendedIdentifiedProtein protein : identifiedProteins) {
						final String uniProtACC = FastaParser.getUniProtACC(protein.getAccession());
						if (uniProtACC != null) {
							uniprotAccs.add(uniProtACC);
						}
					}
				}
				// get all sequences at once first
				if (!uniprotAccs.isEmpty()) {
					try {
						firePropertyChange(PROTEIN_SEQUENCE_RETRIEVAL, null,
								"Retrieving protein annotations from " + uniprotAccs.size() + " proteins in UniprotKB");

						final UniprotProteinLocalRetriever upr = FileManager.getUniprotProteinLocalRetriever();
						upr.setCacheEnabled(true);
						final Map<String, Entry> annotatedProteins = upr.getAnnotatedProteins(null, uniprotAccs);
					} finally {
						firePropertyChange(PROTEIN_SEQUENCE_RETRIEVAL_DONE, null, null);
					}
					// and just keep that in the cache
				}
			}

			out = new BufferedOutputStream(new FileOutputStream(file));

			List<String> columnsStringList = ExportedColumns.getColumnsString(this.showPeptides, this.includeGeneInfo,
					this.isFDRApplied, idSets, true);
			final ExporterUtil exporterUtil = ExporterUtil.getInstance(idSets, showPeptides, retrieveProteinSequences);
			final String columnsString = exporterUtil.getStringFromList(columnsStringList, separator) + NEWLINE;

			log.info(columnsString);
			out.write(columnsString.getBytes());

			// final Map<String,
			// IdentificationOccurrence<ExtendedIdentifiedProtein>>
			// proteinOccurrenceList = this.idSet
			// .getProteinOccurrenceList();
			int i = 1;
			// now get the columns for the exporter, without translating them to be
			// cmpatible with importing.
			columnsStringList = ExportedColumns.getColumnsString(this.showPeptides, this.includeGeneInfo,
					this.isFDRApplied, idSets, false);
			for (final IdentificationSet idSet : idSets) {
				// if (progress == 0)
				// return;
				if (this.showPeptides) {
					if (this.showBestPeptides) {

						final Collection<PeptideOccurrence> peptideOccurrenceList = getPeptideOccurrenceListToExport(
								idSet);

						// firePropertyChange(DATA_EXPORTING_SORTING, null,
						// peptideOccurrenceList.size());

						// SorterUtil.sortPeptideOcurrencesByBestPeptideScore(peptideOccurrenceList);
						// firePropertyChange(DATA_EXPORTING_SORTING_DONE, null,
						// null);
						final Iterator<PeptideOccurrence> iterator = peptideOccurrenceList.iterator();

						final int total = peptideOccurrenceList.size();
						final ProgressCounter counter = new ProgressCounter(total,
								ProgressPrintingType.PERCENTAGE_STEPS, 0);

						while (iterator.hasNext()) {
							final PeptideOccurrence peptideOccurrence = iterator.next();

							Thread.sleep(1);

							final List<String> peptideColumns = exporterUtil.getPeptideInfoList(peptideOccurrence,
									columnsStringList, i++, idSet);
							final String lineString = exporterUtil.getStringFromList(peptideColumns, separator)
									+ NEWLINE;
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

						final int total = peptidelistToExport.size();
						final ProgressCounter counter = new ProgressCounter(total,
								ProgressPrintingType.PERCENTAGE_STEPS, 0);

						for (final ExtendedIdentifiedPeptide peptide : peptidelistToExport) {

							Thread.sleep(1);

							final PeptideOccurrence peptideOccurrence = new PeptideOccurrence(
									peptide.getModificationString());
							peptideOccurrence.addOccurrence(peptide);
							final List<String> lineStringList = exporterUtil.getPeptideInfoList(peptideOccurrence,
									columnsStringList, i++, idSet);
							final String lineString = exporterUtil.getStringFromList(lineStringList, separator)
									+ NEWLINE;
							// log.info(lineString);
							out.write(lineString.getBytes());

							counter.increment();

							final String percentage = counter.printIfNecessary();
							if (!"".equals(percentage)) {
								log.info(percentage);
								setProgress(Double.valueOf(counter.getPercentage()).intValue());
							}

						}
					}
				} else {
					// JUST PROTEINS
					if (this.showBestProteins) {
						final Collection<ProteinGroupOccurrence> proteinGroupOccurrenceList = getProteinGroupOccurrenceToExport(
								idSet);

						// sort if there is a FDR Filter activated that tells us
						// which is the score sort

						// firePropertyChange(DATA_EXPORTING_SORTING, null,
						// proteinGroupOccurrenceList.size());
						try {
							// SorterUtil.sortProteinGroupOcurrencesByBestPeptideScore(proteinGroupOccurrenceList);
						} catch (final Exception e) {

						}
						// firePropertyChange(DATA_EXPORTING_SORTING_DONE, null,
						// null);
						final Iterator<ProteinGroupOccurrence> iterator = proteinGroupOccurrenceList.iterator();

						final int total = proteinGroupOccurrenceList.size();
						final ProgressCounter counter = new ProgressCounter(total,
								ProgressPrintingType.PERCENTAGE_STEPS, 0);

						while (iterator.hasNext()) {
							final ProteinGroupOccurrence proteinGroupOccurrence = iterator.next();
							Thread.sleep(1);

							final List<String> proteinStringList = exporterUtil
									.getProteinInfoList(proteinGroupOccurrence, columnsStringList, i++, idSet);
							final String proteinString = exporterUtil.getStringFromList(proteinStringList, separator)
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

						final int total = proteinGroupsToExport.size();
						final ProgressCounter counter = new ProgressCounter(total,
								ProgressPrintingType.PERCENTAGE_STEPS, 0);

						for (final ProteinGroup proteinGroup : proteinGroupsToExport) {

							Thread.sleep(1);

							final ProteinGroupOccurrence proteinOccurrence = new ProteinGroupOccurrence();
							proteinOccurrence.addOccurrence(proteinGroup);

							final List<String> proteinStringList = exporterUtil.getProteinInfoList(proteinOccurrence,
									columnsStringList, i++, idSet);
							final String proteinString = exporterUtil.getStringFromList(proteinStringList, separator)
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
		} catch (final Exception e) {
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
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
			if (error != null) {
				this.cancel(true);
			}
		}

	}

	private Collection<PeptideOccurrence> getPeptideOccurrenceListToExport(IdentificationSet idSet) {
		final Collection<PeptideOccurrence> peptideOccurrences = idSet
				.getPeptideOccurrenceList(distinguisModificatedPeptides).values();
		if (this.filter == null && includeDecoyHits) {
			return peptideOccurrences;
		}
		final List<PeptideOccurrence> ret = new ArrayList<PeptideOccurrence>();
		for (final PeptideOccurrence peptideOccurrence : peptideOccurrences) {
			if (!includeDecoyHits && peptideOccurrence.isDecoy()) {
				continue;
			}
			if (filter != null) {
				final String key = peptideOccurrence.getKey();
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
		final List<ExtendedIdentifiedPeptide> peptidelistToExport = new ArrayList<ExtendedIdentifiedPeptide>();

		final List<ExtendedIdentifiedPeptide> identifiedPeptides = idSet.getIdentifiedPeptides();
		for (final ExtendedIdentifiedPeptide extendedIdentifiedPeptide : identifiedPeptides) {
			if (!includeDecoyHits && extendedIdentifiedPeptide.isDecoy()) {
				continue;
			}
			if (filter != null) {
				final String key = extendedIdentifiedPeptide.getKey(distinguisModificatedPeptides);
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
		final List<ProteinGroup> identifiedProteinGroups = idSet.getIdentifiedProteinGroups();
		if (filter == null && includeDecoyHits) {
			return identifiedProteinGroups;
		}

		final List<ProteinGroup> ret = new ArrayList<ProteinGroup>();
		for (final ProteinGroup proteinGroup : identifiedProteinGroups) {
			if (!includeDecoyHits && proteinGroup.isDecoy()) {
				continue;
			}
			final ProteinGroupOccurrence proteinOccurrence = new ProteinGroupOccurrence();
			proteinOccurrence.addOccurrence(proteinGroup);
			final Object key = proteinOccurrence.getKey(comparisonType);
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
		final Collection<ProteinGroupOccurrence> proteinGroupOccurrences = idSet.getProteinGroupOccurrenceList()
				.values();
		if (filter == null && includeDecoyHits) {
			return proteinGroupOccurrences;
		}

		final List<ProteinGroupOccurrence> ret = new ArrayList<ProteinGroupOccurrence>();
		for (final ProteinGroupOccurrence proteinOccurrence : proteinGroupOccurrences) {
			if (!includeDecoyHits && proteinOccurrence.isDecoy()) {
				continue;
			}
			if (filter != null) {
				final Object key = proteinOccurrence.getKey(comparisonType);
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
		firePropertyChange(DATA_EXPORTING_STARTING, null, null);
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
		} catch (final IOException e) {
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
