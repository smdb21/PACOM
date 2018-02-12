package org.proteored.pacom.analysis.exporters.tasks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.interfaces.ms.ResultingData;
import org.proteored.miapeapi.interfaces.msi.MiapeMSIDocument;
import org.proteored.miapeapi.util.URLValidator;
import org.proteored.pacom.analysis.gui.components.AbstractExtendedJTree;
import org.proteored.pacom.analysis.gui.components.MyTreeRenderer;

import gnu.trove.set.hash.THashSet;

public class PEXBulkSubmissionSummaryTreeLoaderTask extends SwingWorker<Void, Void> {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private final ExperimentList expList;
	private final AbstractExtendedJTree jTreeSummary;
	private final boolean includeMSIAttachedFiles;

	private final boolean includeMIAPEReports;
	public static final String SEARCH = "search";
	public static final String OTHER = "other";
	public static final String PEAK = "peak";
	public static final String RESULT = "result";
	public static final String RAW = "raw";

	public static final String BULK_SUBMISSION_TREE_LOADER_STARTS = "bulk submission tree loader starts";
	public static final String BULK_SUBMISSION_TREE_LOADER_FINISH = "bulk submission tree loader finished";
	public static final String BULK_SUBMISSION_TREE_LOADER_EXPERIMENT_LOADED = "bulk submission tree loader experiment loaded";
	public static final String BULK_SUBMISSION_TREE_LOADER_RAW_DATA_PRESENT = "bulk submission tree loader raw data present";;

	private static Set<String> validatedURLs = new THashSet<String>();

	public PEXBulkSubmissionSummaryTreeLoaderTask(ExperimentList experimentList, AbstractExtendedJTree jTreeSummary,
			boolean includeMSIAttachedFiles, boolean includeMIAPEReports) {
		expList = experimentList;
		this.jTreeSummary = jTreeSummary;
		this.includeMSIAttachedFiles = includeMSIAttachedFiles;
		this.includeMIAPEReports = includeMIAPEReports;
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			firePropertyChange(BULK_SUBMISSION_TREE_LOADER_STARTS, null, null);
			DefaultTreeModel modeloArbol = null;
			DefaultMutableTreeNode nodoRaiz = null;
			if (jTreeSummary != null && expList != null) {
				nodoRaiz = new DefaultMutableTreeNode("PEX Bulk Submission Summary");
				modeloArbol = new DefaultTreeModel(nodoRaiz);
				jTreeSummary.setModel(modeloArbol);
				jTreeSummary.setCellRenderer(new MyTreeRenderer());
				if (expList.getExperiments() != null) {
					setProgress(0);
					int numReplicates = expList.getNumReplicates();
					int num = 0;
					Map<Replicate, List<String>> msiFilesByReplicate = expList.getMSIGeneratedFilesByReplicate();
					Map<Replicate, List<ResultingData>> rawFilesByReplicate = expList
							.getRawFileResultingDataMapByReplicate();
					Map<Replicate, List<ResultingData>> peaklistFilesByReplicate = expList
							.getPeakListResultingDataMapByReplicate();
					for (Experiment exp : expList.getExperiments()) {
						DefaultMutableTreeNode resultNode = new DefaultMutableTreeNode(
								RESULT + ": PRIDE XML " + exp.getName());

						if (exp.getReplicates() != null) {
							for (Replicate rep : exp.getReplicates()) {

								Thread.sleep(1L);
								num++;
								int progress = Math.round(100 * num / numReplicates);
								log.info("Progress: " + progress);
								setProgress(progress);
								DefaultMutableTreeNode repNode = new DefaultMutableTreeNode(rep);

								List<DefaultMutableTreeNode> searchNodes = new ArrayList<DefaultMutableTreeNode>();
								// Attached MSI files
								if (includeMSIAttachedFiles && msiFilesByReplicate != null
										&& msiFilesByReplicate.containsKey(rep)) {
									List<String> msiAttachedFiles = msiFilesByReplicate.get(rep);
									if (msiAttachedFiles != null) {
										for (String msiAttachedFileLocation : msiAttachedFiles) {
											try {
												final URL url = new URL(msiAttachedFileLocation);
												if (!PEXBulkSubmissionSummaryTreeLoaderTask.validatedURLs
														.contains(url.toString()) && !URLValidator.validateURL(url))
													continue;
												PEXBulkSubmissionSummaryTreeLoaderTask.validatedURLs
														.add(url.toString());
												String name = FilenameUtils.getName(url.getFile());
												DefaultMutableTreeNode searchNode = new DefaultMutableTreeNode(
														SEARCH + ": " + name);
												searchNodes.add(searchNode);
												repNode.add(searchNode);
											} catch (MalformedURLException e) {
												// do nothing
											}
										}
									}
								}

								List<DefaultMutableTreeNode> peakNodes = new ArrayList<DefaultMutableTreeNode>();

								// peak List files
								if (peaklistFilesByReplicate != null && peaklistFilesByReplicate.containsKey(rep)) {
									List<ResultingData> resultingDatas = peaklistFilesByReplicate.get(rep);
									if (resultingDatas != null) {
										for (ResultingData resultingdata : resultingDatas) {
											try {
												String dataFileUri = resultingdata.getDataFileUri();

												final URL url = new URL(dataFileUri);
												if (!PEXBulkSubmissionSummaryTreeLoaderTask.validatedURLs
														.contains(url.toString()) && !URLValidator.validateURL(url))
													continue;
												PEXBulkSubmissionSummaryTreeLoaderTask.validatedURLs
														.add(url.toString());
												final String file = url.getFile();
												String name = FilenameUtils.getName(file);
												DefaultMutableTreeNode peakListNode = new DefaultMutableTreeNode(
														PEAK + ": " + name);
												peakNodes.add(peakListNode);
												repNode.add(peakListNode);
											} catch (MalformedURLException e) {
												// do nothing
											}
										}
									}
								}

								List<DefaultMutableTreeNode> rawNodes = new ArrayList<DefaultMutableTreeNode>();

								// raw files
								if (rawFilesByReplicate != null && rawFilesByReplicate.containsKey(rep)) {
									List<ResultingData> resultingDatas = rawFilesByReplicate.get(rep);
									if (resultingDatas != null) {
										for (ResultingData resultingdata : resultingDatas) {
											try {
												String dataFileUri = resultingdata.getDataFileUri();
												URL url = new URL(dataFileUri);
												if (!PEXBulkSubmissionSummaryTreeLoaderTask.validatedURLs
														.contains(url.toString()) && !URLValidator.validateURL(url))
													continue;
												PEXBulkSubmissionSummaryTreeLoaderTask.validatedURLs
														.add(url.toString());
												String urlFile = url.getFile();

												String name = FilenameUtils.getName(urlFile);
												DefaultMutableTreeNode rawFileNode = new DefaultMutableTreeNode(
														RAW + ": " + name);
												rawNodes.add(rawFileNode);
												repNode.add(rawFileNode);
												// Notify that there is a RAW
												// data
												firePropertyChange(BULK_SUBMISSION_TREE_LOADER_RAW_DATA_PRESENT, null,
														null);
											} catch (MalformedURLException e) {
												// do nothing
											}
										}
									}
								}

								if (includeMIAPEReports) {
									// MIAPE MS documents
									List<MiapeMSDocument> miapeMSs = rep.getMiapeMSs();
									if (miapeMSs != null) {
										for (MiapeMSDocument miapeMSDocument : miapeMSs) {
											// if the id of the document is less
											// than 0 is because it is a local
											// created document, so it will have
											// not document reports
											if (miapeMSDocument.getId() > 0) {
												DefaultMutableTreeNode miapeMSNode = new DefaultMutableTreeNode(OTHER
														+ ": MIAPE_MS_" + miapeMSDocument.getId() + "_report.html");
												repNode.add(miapeMSNode);
												for (DefaultMutableTreeNode peaknode : peakNodes) {
													peaknode.add(miapeMSNode);
												}
												for (DefaultMutableTreeNode rawnode : rawNodes) {
													rawnode.add(miapeMSNode);
												}
											}
										}
									}
									// MIAPE MSI documents
									List<MiapeMSIDocument> miapeMSIs = rep.getMiapeMSIs();
									if (miapeMSs != null) {
										for (MiapeMSIDocument miapeMSIDocument : miapeMSIs) {
											if (miapeMSIDocument.getId() > 0) {
												DefaultMutableTreeNode miapeMSINode = new DefaultMutableTreeNode(OTHER
														+ ": MIAPE_MSI_" + miapeMSIDocument.getId() + "_report.html");
												repNode.add(miapeMSINode);
												for (DefaultMutableTreeNode searchnode : searchNodes) {
													searchnode.add(miapeMSINode);
												}
											}
										}
									}
								}
								resultNode.add(repNode);
							}

						}
						if (resultNode != null) {
							nodoRaiz.add(resultNode);
							modeloArbol.nodeStructureChanged(nodoRaiz);
						}
						firePropertyChange(BULK_SUBMISSION_TREE_LOADER_EXPERIMENT_LOADED, null, exp);
					}
				}

			}
			firePropertyChange(BULK_SUBMISSION_TREE_LOADER_FINISH, null, null);
			return null;
		} catch (Exception e) {
			firePropertyChange(BULK_SUBMISSION_TREE_LOADER_FINISH, null, e.getMessage());
		}
		return null;
	}

	@Override
	protected void done() {
		if (isCancelled())
			log.info("submission summary file cancelled");
		super.done();
	}

	public static void main(String[] args) {
		try {
			boolean valid = URLValidator
					.validateURL(new URL("ftp://pme6@estrellapolar.cnb.csic.es/data/CBT/PME6_I_CBT_Orbitrap.raw"));
			System.out.println(valid);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
