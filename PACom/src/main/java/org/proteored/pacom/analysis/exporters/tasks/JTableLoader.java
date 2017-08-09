package org.proteored.pacom.analysis.exporters.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedPeptide;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedProtein;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.PeptideOccurrence;
import org.proteored.miapeapi.experiment.model.ProteinGroup;
import org.proteored.miapeapi.experiment.model.ProteinGroupOccurrence;
import org.proteored.pacom.analysis.exporters.Exporter;
import org.proteored.pacom.analysis.exporters.ExporterManager;
import org.proteored.pacom.analysis.exporters.gui.MyIdentificationTable;
import org.proteored.pacom.analysis.exporters.util.ExportedColumns;
import org.proteored.pacom.analysis.exporters.util.ExporterUtil;
import org.proteored.pacom.analysis.util.FileManager;

import edu.scripps.yates.annotations.uniprot.UniprotProteinLocalRetriever;
import edu.scripps.yates.annotations.uniprot.xml.Entry;
import edu.scripps.yates.utilities.fasta.FastaParser;
import gnu.trove.set.hash.THashSet;

public class JTableLoader extends SwingWorker<Void, Void> implements Exporter<JTable> {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private JTable table;
	private final boolean includeDecoyHits;
	private final boolean includePeptides;
	private final boolean retrieveProteinSequences;
	private final boolean includeGeneInfo;
	private final boolean collapsePeptides;
	private final boolean collapseProteins;
	private final Set<IdentificationSet> idSets = new THashSet<IdentificationSet>();
	private String error;

	private final boolean isFDRApplied;
	private static boolean running = false;

	public JTableLoader(ExporterManager manager, Collection<IdentificationSet> idSets, JTable table2) {

		if (table2 == null) {
			throw new IllegalMiapeArgumentException("Table is null!!");
		}
		this.table = table2;
		this.includeDecoyHits = manager.isDecoyHitsIncluded();
		this.includePeptides = manager.showPeptides();
		this.includeGeneInfo = manager.isGeneInfoIncluded();
		this.collapsePeptides = manager.showBestPeptides();
		this.collapseProteins = manager.showBestProteins();
		this.retrieveProteinSequences = manager.retrieveProteinSequences();
		this.isFDRApplied = manager.isFDRApplied();
		this.idSets.addAll(idSets);

	}

	public JTableLoader(ExporterManager manager, Collection<IdentificationSet> idSets, JTable table,
			boolean useTestOntologies) {
		this(manager, idSets, table);
		ExporterUtil.setTestOntologies(useTestOntologies);
	}

	@Override
	public JTable export() {
		synchronized (this) {
			if (running) {
				log.info("TableLoader already running. Cancelling this thread.");
				return this.table;
			}
			running = true;
		}
		if (this.idSets.isEmpty()) {
			return this.table;
		}

		if (this.retrieveProteinSequences || includeGeneInfo) {

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
					firePropertyChange(PROTEIN_SEQUENCE_RETRIEVAL, null, null);

					firePropertyChange(MESSAGE, null, "Retrieving protein sequences from " + uniprotAccs.size()
							+ " different proteins in UniprotKB");
					UniprotProteinLocalRetriever upr = FileManager.getUniprotProteinLocalRetriever();
					upr.setCacheEnabled(true);
					Map<String, Entry> annotatedProteins = upr.getAnnotatedProteins(null, uniprotAccs);
				} finally {
					firePropertyChange(PROTEIN_SEQUENCE_RETRIEVAL_DONE, null, null);
				}
				// and just keep that in the cache
			}
		}

		log.info("Starting JTable exporting");
		try {
			// ((DefaultTableModel)
			// this.table.getModel()).getDataVector().removeAllElements();
			((MyIdentificationTable) this.table).clearData();

			List<String> columnsStringList = ExportedColumns.getColumnsStringForTable(this.includePeptides,
					this.includeGeneInfo, this.isFDRApplied, this.idSets);

			addColumnsInTable(columnsStringList);
			int progress = 0;
			if (this.includePeptides) {
				if (this.collapsePeptides) {
					int total = 0;
					progress = 0;
					for (IdentificationSet idSet : idSets) {
						total += idSet.getPeptideOccurrenceList(true).size();
					}
					for (IdentificationSet idSet : idSets) {
						final Map<String, PeptideOccurrence> peptideOccurrenceHashMap = idSet
								.getPeptideOccurrenceList(true);
						// sort if there is a FDR Filter activated that tells us
						// which is the score sort

						firePropertyChange(DATA_EXPORTING_SORTING, null, peptideOccurrenceHashMap.size());
						ArrayList<PeptideOccurrence> peptideOccurrenceList = new ArrayList<PeptideOccurrence>();
						for (PeptideOccurrence peptideOccurrence : peptideOccurrenceHashMap.values()) {
							peptideOccurrenceList.add(peptideOccurrence);
						}

						// SorterUtil.sortPeptideOcurrencesByBestPeptideScore(peptideOccurrenceList);
						firePropertyChange(DATA_EXPORTING_SORTING_DONE, null, null);
						Iterator<PeptideOccurrence> iterator = peptideOccurrenceList.iterator();

						int i = 1;
						while (iterator.hasNext()) {
							PeptideOccurrence peptideOccurrence = iterator.next();
							Thread.sleep(1);
							boolean isdecoy = peptideOccurrence.isDecoy();

							boolean pass = true;

							if (pass && (includeDecoyHits || (!includeDecoyHits && !isdecoy))) {
								final List<String> lineStringList = ExporterUtil
										.getInstance(idSets, includePeptides, includeGeneInfo, retrieveProteinSequences)
										.getPeptideInfoList(peptideOccurrence, columnsStringList, i++, idSet);
								addNewRow(lineStringList);
							}

							progress++;
							if (total > 0) {
								final int percentage = progress * 100 / total;
								// log.info(percentage + " %");
								setProgress(percentage);
							}
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									JTableLoader.this.table.repaint();
								}
							});

						}
					}
				} else {
					int total = 0;
					progress = 0;
					for (IdentificationSet idSet : idSets) {
						total += idSet.getIdentifiedPeptides().size();
					}
					for (IdentificationSet idSet : idSets) {
						final List<ExtendedIdentifiedPeptide> identifiedPeptides = idSet.getIdentifiedPeptides();
						// sort if there is a FDR Filter activated that tells us
						// which is the score sort

						firePropertyChange(DATA_EXPORTING_SORTING, null, identifiedPeptides.size());

						// SorterUtil.sortPeptidesByBestPeptideScore(identifiedPeptides,
						// true);
						firePropertyChange(DATA_EXPORTING_SORTING_DONE, null, null);

						int i = 1;
						for (ExtendedIdentifiedPeptide peptide : identifiedPeptides) {
							Thread.sleep(1);
							boolean isdecoy = peptide.isDecoy();
							boolean pass = true;
							if (pass && (includeDecoyHits || (!includeDecoyHits && !isdecoy))) {
								PeptideOccurrence peptideOccurrence = new PeptideOccurrence(
										peptide.getModificationString());
								peptideOccurrence.addOccurrence(peptide);
								final List<String> lineStringList = ExporterUtil
										.getInstance(idSets, includePeptides, includeGeneInfo, retrieveProteinSequences)
										.getPeptideInfoList(peptideOccurrence, columnsStringList, i++, idSet);

								// log.info(lineString);
								addNewRow(lineStringList);
							}

							progress++;
							final int percentage = progress * 100 / total;
							// log.info(percentage + " %");
							setProgress(percentage);
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									JTableLoader.this.table.repaint();
								}
							});
						}
					}
				}
			} else {
				// JUST PROTEINS
				if (this.collapseProteins) {
					int i = 1;
					int total = 0;
					progress = 0;
					for (IdentificationSet idSet : idSets) {
						total += idSet.getProteinGroupOccurrenceList().size();
					}
					firePropertyChange(DATA_EXPORTING_STARTING, null, total);
					for (IdentificationSet idSet : idSets) {
						final Collection<ProteinGroupOccurrence> proteinOccurrenceSet = idSet
								.getProteinGroupOccurrenceList().values();
						final List<ProteinGroupOccurrence> proteinOccurrenceList = new ArrayList<ProteinGroupOccurrence>();
						for (ProteinGroupOccurrence proteinGroupOccurrence : proteinOccurrenceSet) {
							proteinOccurrenceList.add(proteinGroupOccurrence);
						}
						// sort if there is a FDR Filter activated that tells us
						// which is the score sort

						firePropertyChange(DATA_EXPORTING_SORTING, null, proteinOccurrenceList.size());
						try {
							// SorterUtil.sortProteinGroupOcurrencesByBestPeptideScore(proteinOccurrenceList);
						} catch (Exception e) {
							log.info(e.getMessage());
						}
						firePropertyChange(DATA_EXPORTING_SORTING_DONE, null, null);
						Iterator<ProteinGroupOccurrence> iterator = proteinOccurrenceList.iterator();

						while (iterator.hasNext()) {
							if (i % 10 == 0)
								log.info(i + " / " + total);
							ProteinGroupOccurrence proteinGroupOccurrence = iterator.next();
							Thread.sleep(1L);

							boolean isdecoy = proteinGroupOccurrence.isDecoy();
							boolean pass = true;
							if (pass && (includeDecoyHits || (!includeDecoyHits && !isdecoy))) {

								final List<String> lineStringList = ExporterUtil
										.getInstance(idSets, includePeptides, includeGeneInfo, retrieveProteinSequences)
										.getProteinInfoList(proteinGroupOccurrence, columnsStringList, i++, idSet);

								// System.out.println(peptideString);
								addNewRow(lineStringList);
							}

							progress++;
							final int percentage = progress * 100 / total;
							// log.info(percentage + " %");
							setProgress(percentage);
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									JTableLoader.this.table.repaint();
								}
							});
						}
					}
				} else {
					int total = 0;
					progress = 0;
					for (IdentificationSet idSet : idSets) {
						total += idSet.getIdentifiedProteinGroups().size();
					}
					for (IdentificationSet idSet : idSets) {
						final List<ProteinGroup> proteinGroups = idSet.getIdentifiedProteinGroups();
						// sort if there is a FDR Filter activated that tells us
						// which is the score sort

						firePropertyChange(DATA_EXPORTING_SORTING, null, proteinGroups.size());

						// SorterUtil.sortProteinGroupsByBestPeptideScore(proteinGroups);
						firePropertyChange(DATA_EXPORTING_SORTING_DONE, null, null);

						int i = 1;
						for (ProteinGroup proteinGroup : proteinGroups) {
							Thread.sleep(1);
							boolean isdecoy = proteinGroup.isDecoy();
							boolean pass = true;
							if (pass && (includeDecoyHits || (!includeDecoyHits && !isdecoy))) {
								ProteinGroupOccurrence proteinGroupOccurrence = new ProteinGroupOccurrence();
								proteinGroupOccurrence.addOccurrence(proteinGroup);
								final List<String> lineStringList = ExporterUtil
										.getInstance(idSets, includePeptides, includeGeneInfo, retrieveProteinSequences)
										.getProteinInfoList(proteinGroupOccurrence, columnsStringList, i++, idSet);

								// System.out.println(peptideString);
								addNewRow(lineStringList);
							}

							progress++;
							final int percentage = progress * 100 / total;
							// log.info(percentage + " %");
							setProgress(percentage);
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									JTableLoader.this.table.repaint();
								}
							});
						}
					}
				}
			}
		} catch (Exception e) {
			if (!(e instanceof InterruptedException)) {
				e.printStackTrace();
				error = e.getMessage();
			} else {
				log.info("Table Loading stopped");
			}
		} finally {
			this.table.repaint();
			if (error != null)
				this.cancel(true);
		}

		return table;
	}

	private void addColumnsInTable(List<String> columnsStringList) {
		DefaultTableModel defaultModel = getTableModel();
		log.info("Adding colums " + columnsStringList.size() + " columns");
		if (columnsStringList != null) {

			for (String columnName : columnsStringList) {
				defaultModel.addColumn(columnName);
			}
			log.info("Added " + this.table.getColumnCount() + " colums");
			for (int i = 0; i < this.table.getColumnCount(); i++) {
				TableColumn column = this.table.getColumnModel().getColumn(i);
				final ExportedColumns[] columHeaders = ExportedColumns.values();
				for (ExportedColumns header : columHeaders) {
					if (column.getHeaderValue().equals(header.getName()))
						column.setPreferredWidth(header.getDefaultWidth());
				}
				column.setResizable(true);
			}
		}
	}

	private void addNewRow(List<String> lineStringList) {
		DefaultTableModel defaultModel = getTableModel();
		try {
			defaultModel.addRow(lineStringList.toArray());
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			log.error(lineStringList);
		}
		// this.table.repaint();

	}

	private DefaultTableModel getTableModel() {
		if (this.table == null) {
			return null;
		}
		TableModel model = this.table.getModel();
		if (model == null) {
			model = new DefaultTableModel();
		}
		final DefaultTableModel defaultModel = (DefaultTableModel) model;
		return defaultModel;
	}

	@Override
	protected Void doInBackground() throws Exception {
		export();
		return null;
	}

	@Override
	protected void done() {
		synchronized (this) {
			running = false;
		}
		if (!this.isCancelled())
			firePropertyChange(DATA_EXPORTING_DONE, null, this.table.getRowCount());
		else {
			if (error != null) {
				firePropertyChange(DATA_EXPORTING_ERROR, null, error);
			} else {
				int num = 0;
				if (this.table != null)
					num = this.table.getRowCount();
				firePropertyChange(DATA_EXPORTING_CANCELED, null, num);
			}
		}
	}

	public JTable getTable() {
		return this.table;
	}

}
