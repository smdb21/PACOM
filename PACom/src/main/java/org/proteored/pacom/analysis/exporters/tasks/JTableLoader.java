package org.proteored.pacom.analysis.exporters.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.PeptideOccurrence;
import org.proteored.miapeapi.experiment.model.ProteinGroup;
import org.proteored.miapeapi.experiment.model.ProteinGroupOccurrence;
import org.proteored.miapeapi.experiment.model.sort.SorterUtil;
import org.proteored.pacom.analysis.exporters.Exporter;
import org.proteored.pacom.analysis.exporters.ExporterManager;
import org.proteored.pacom.analysis.exporters.util.ExportedColumns;
import org.proteored.pacom.analysis.exporters.util.ExporterUtil;

public class JTableLoader extends SwingWorker<Void, Void> implements Exporter<JTable> {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private JTable table;
	private final boolean includeReplicateAndExperimentOrigin;
	private final boolean includeDecoyHits;
	private final boolean includePeptides;
	private final boolean retrieveProteinSequences;
	private final boolean includeGeneInfo;
	private final boolean collapsePeptides;
	private final boolean collapseProteins;
	private final boolean excludeNonConclusiveProteins;
	private final Set<IdentificationSet> idSets = new HashSet<IdentificationSet>();
	private String error;

	private final boolean isFDRApplied;

	public JTableLoader(ExporterManager manager, Collection<IdentificationSet> idSets, JTable table2) {

		if (table2 != null)
			this.table = table2;
		else
			throw new IllegalMiapeArgumentException("Table is null!!");

		this.includeReplicateAndExperimentOrigin = manager.isReplicateAndExperimentOriginIncluded();
		this.includeDecoyHits = manager.isDecoyHitsIncluded();
		this.includePeptides = manager.showPeptides();
		this.includeGeneInfo = manager.isGeneInfoIncluded();
		this.collapsePeptides = manager.showBestPeptides();
		this.collapseProteins = manager.showBestProteins();
		this.retrieveProteinSequences = manager.retrieveProteinSequences();
		this.excludeNonConclusiveProteins = !manager.isNonConclusiveProteinsIncluded();
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
		if (this.idSets.isEmpty()) {
			return this.table;
		}
		log.info("Starting JTable exporting");
		try {
			((DefaultTableModel) this.table.getModel()).getDataVector().removeAllElements();

			List<String> columnsStringList = ExportedColumns.getColumnsStringForTable(
					this.includeReplicateAndExperimentOrigin, this.includePeptides, this.includeGeneInfo,
					this.isFDRApplied, this.idSets);

			addColumns(columnsStringList);
			int progress = 0;
			if (this.includePeptides) {
				if (this.collapsePeptides) {
					for (IdentificationSet idSet : idSets) {
						final HashMap<String, PeptideOccurrence> peptideOccurrenceHashMap = idSet
								.getPeptideOccurrenceList(true);
						// sort if there is a FDR Filter activated that tells us
						// which is the score sort

						firePropertyChange(DATA_EXPORTING_SORTING, null, peptideOccurrenceHashMap.size());
						ArrayList<PeptideOccurrence> peptideOccurrenceList = new ArrayList<PeptideOccurrence>();
						for (PeptideOccurrence peptideOccurrence : peptideOccurrenceHashMap.values()) {
							peptideOccurrenceList.add(peptideOccurrence);
						}

						SorterUtil.sortPeptideOcurrencesByBestPeptideScore(peptideOccurrenceList);
						firePropertyChange(DATA_EXPORTING_SORTING_DONE, null, null);
						Iterator<PeptideOccurrence> iterator = peptideOccurrenceList.iterator();

						int total = peptideOccurrenceHashMap.size();
						int i = 1;
						while (iterator.hasNext()) {
							PeptideOccurrence peptideOccurrence = iterator.next();
							Thread.sleep(1);
							boolean isdecoy = peptideOccurrence.isDecoy();
							if (isdecoy)
								System.out.println("HOLA");
							boolean pass = true;
							if (excludeNonConclusiveProteins && ExporterUtil.isNonConclusivePeptide(peptideOccurrence))
								pass = false;
							if (pass && (includeDecoyHits || (!includeDecoyHits && !isdecoy))) {
								final List<String> lineStringList = ExporterUtil
										.getInstance(idSets, includeReplicateAndExperimentOrigin, includePeptides,
												includeGeneInfo, retrieveProteinSequences, excludeNonConclusiveProteins)
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
					for (IdentificationSet idSet : idSets) {
						final List<ExtendedIdentifiedPeptide> identifiedPeptides = idSet.getIdentifiedPeptides();
						// sort if there is a FDR Filter activated that tells us
						// which is the score sort

						firePropertyChange(DATA_EXPORTING_SORTING, null, identifiedPeptides.size());

						SorterUtil.sortPeptidesByBestPeptideScore(identifiedPeptides, true);
						firePropertyChange(DATA_EXPORTING_SORTING_DONE, null, null);

						int total = identifiedPeptides.size();
						int i = 1;
						for (ExtendedIdentifiedPeptide peptide : identifiedPeptides) {
							Thread.sleep(1);
							boolean isdecoy = peptide.isDecoy();
							boolean pass = true;
							if (excludeNonConclusiveProteins && ExporterUtil.isNonConclusivePeptide(peptide))
								pass = false;
							if (pass && (includeDecoyHits || (!includeDecoyHits && !isdecoy))) {
								PeptideOccurrence peptideOccurrence = new PeptideOccurrence(
										peptide.getModificationString());
								peptideOccurrence.addOccurrence(peptide);
								final List<String> lineStringList = ExporterUtil
										.getInstance(idSets, includeReplicateAndExperimentOrigin, includePeptides,
												includeGeneInfo, retrieveProteinSequences, excludeNonConclusiveProteins)
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
					for (IdentificationSet idSet : idSets) {
						final Collection<ProteinGroupOccurrence> proteinOccurrenceSet = idSet
								.getProteinGroupOccurrenceList().values();
						final List<ProteinGroupOccurrence> proteinOccurrenceList = new ArrayList<ProteinGroupOccurrence>();
						for (ProteinGroupOccurrence proteinGroupOccurrence : proteinOccurrenceSet) {
							proteinOccurrenceList.add(proteinGroupOccurrence);
						}
						// sort if there is a FDR Filter activated that tells us
						// which is the score sort
						firePropertyChange(DATA_EXPORTING_STARTING, null, proteinOccurrenceList.size());
						firePropertyChange(DATA_EXPORTING_SORTING, null, proteinOccurrenceList.size());
						try {
							SorterUtil.sortProteinGroupOcurrencesByBestPeptideScore(proteinOccurrenceList);
						} catch (Exception e) {
							log.info(e.getMessage());
						}
						firePropertyChange(DATA_EXPORTING_SORTING_DONE, null, null);
						Iterator<ProteinGroupOccurrence> iterator = proteinOccurrenceList.iterator();

						int total = proteinOccurrenceList.size();
						int i = 1;
						while (iterator.hasNext()) {
							if (i % 10 == 0)
								log.info(i + " / " + total);
							ProteinGroupOccurrence proteinGroupOccurrence = iterator.next();
							Thread.sleep(1L);

							boolean isdecoy = proteinGroupOccurrence.isDecoy();
							boolean pass = true;
							if (excludeNonConclusiveProteins
									&& ExporterUtil.isNonConclusiveProtein(proteinGroupOccurrence)) {
								pass = false;
								// log.info("Non conclusive protein skipped");
							}
							if (pass && (includeDecoyHits || (!includeDecoyHits && !isdecoy))) {

								final List<String> lineStringList = ExporterUtil
										.getInstance(idSets, includeReplicateAndExperimentOrigin, includePeptides,
												includeGeneInfo, retrieveProteinSequences, excludeNonConclusiveProteins)
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
					for (IdentificationSet idSet : idSets) {
						final List<ProteinGroup> proteinGroups = idSet.getIdentifiedProteinGroups();
						// sort if there is a FDR Filter activated that tells us
						// which is the score sort

						firePropertyChange(DATA_EXPORTING_SORTING, null, proteinGroups.size());

						SorterUtil.sortProteinGroupsByBestPeptideScore(proteinGroups);
						firePropertyChange(DATA_EXPORTING_SORTING_DONE, null, null);

						int total = proteinGroups.size();
						int i = 1;
						for (ProteinGroup proteinGroup : proteinGroups) {
							Thread.sleep(1);
							boolean isdecoy = proteinGroup.isDecoy();
							boolean pass = true;
							if (excludeNonConclusiveProteins && ExporterUtil.isNonConclusiveProtein(proteinGroup))
								pass = false;
							if (pass && (includeDecoyHits || (!includeDecoyHits && !isdecoy))) {
								ProteinGroupOccurrence proteinGroupOccurrence = new ProteinGroupOccurrence();
								proteinGroupOccurrence.addOccurrence(proteinGroup);
								final List<String> lineStringList = ExporterUtil
										.getInstance(idSets, includeReplicateAndExperimentOrigin, includePeptides,
												includeGeneInfo, retrieveProteinSequences, excludeNonConclusiveProteins)
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
				log.info("Table Loading stoped");
			}
		} finally {
			this.table.repaint();
			if (error != null)
				this.cancel(true);
		}

		return table;
	}

	private void addColumns(List<String> columnsStringList) {
		DefaultTableModel defaultModel = getTableModel();
		log.info("Adding colums " + columnsStringList.size() + " columns");
		if (columnsStringList != null) {

			for (Object columnName : columnsStringList) {
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

		defaultModel.addRow(lineStringList.toArray());

		// this.table.repaint();

	}

	private DefaultTableModel getTableModel() {
		if (this.table == null)
			return null;

		TableModel model = this.table.getModel();
		if (model == null)
			model = new DefaultTableModel();
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
