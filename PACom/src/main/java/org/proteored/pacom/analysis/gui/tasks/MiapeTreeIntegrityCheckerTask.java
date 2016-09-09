package org.proteored.pacom.analysis.gui.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.exceptions.MiapeDatabaseException;
import org.proteored.miapeapi.exceptions.MiapeSecurityException;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.interfaces.msi.MiapeMSIDocument;
import org.proteored.miapeapi.xml.msi.MIAPEMSIXmlFile;
import org.proteored.pacom.analysis.conf.jaxb.CPExperiment;
import org.proteored.pacom.analysis.conf.jaxb.CPExperimentList;
import org.proteored.pacom.analysis.conf.jaxb.CPMSI;
import org.proteored.pacom.analysis.conf.jaxb.CPReplicate;
import org.proteored.pacom.analysis.gui.Miape2ExperimentListDialog;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.MainFrame;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;

public class MiapeTreeIntegrityCheckerTask extends SwingWorker<String, Void> {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	public static String INTEGRITY_ERROR = "integrity error";
	public static String INTEGRITY_OK = "integrity_ok";
	private final CPExperimentList expList;
	private final Miape2ExperimentListDialog parent;

	private final boolean processInParallel;

	public MiapeTreeIntegrityCheckerTask(Miape2ExperimentListDialog miape2ExperimentListDialog,
			CPExperimentList expList, boolean processInParallel) {
		this.expList = expList;
		parent = miape2ExperimentListDialog;
		this.processInParallel = processInParallel;
	}

	@Override
	protected String doInBackground() throws Exception {
		if (expList != null) {
			try {
				parent.appendStatus("Checking integrity of Inspection Project: '" + expList.getName() + "'");
				parent.jProgressBar.setIndeterminate(true);
				checkIntegrity();
				parent.appendStatus("Project: '" + expList.getName() + "' is OK.");
			} catch (IllegalMiapeArgumentException e) {
				return e.getMessage();
			}
		}
		return null;
	}

	@Override
	protected void done() {
		if (!isCancelled()) {

			try {
				String message = this.get();
				if (message != null)
					firePropertyChange(INTEGRITY_ERROR, null, message);
				else
					firePropertyChange(INTEGRITY_OK, null, null);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

		}
	}

	private void checkIntegrity() {
		List<String> databaseNames = new ArrayList<String>();
		List<Integer> listofNonExistingFiles = new ArrayList<Integer>();
		if (expList != null) {
			final List<CPExperiment> cpExperiments = expList.getCPExperiment();
			if (cpExperiments != null) {
				int total = cpExperiments.size();
				int n = 1;
				for (CPExperiment cpExperiment : cpExperiments) {
					setProgress(n * 100 / total);
					n++;
					boolean curated = cpExperiment.isCurated();
					if (cpExperiment.getCPReplicate() != null) {
						for (CPReplicate cpReplicate : cpExperiment.getCPReplicate()) {
							List<String> repScoreNames = new ArrayList<String>();
							// key=MIAPEMSI_ID - value=List<scoreNames>:
							HashMap<Integer, List<String>> replicateScoreNames = new HashMap<Integer, List<String>>();
							// just check if more than one MIAPE MSI is in a
							// replicate
							if (!cpReplicate.getCPMSIList().getCPMSI().isEmpty())
								for (CPMSI cpMSI : cpReplicate.getCPMSIList().getCPMSI()) {
									List<MiapeMSIDocument> miapeMSIs = new ArrayList<MiapeMSIDocument>();
									final MiapeMSIDocument miapeMSI = getMIAPEMSIFromFile(cpMSI, curated);
									if (miapeMSI != null) {
										miapeMSIs.add(miapeMSI);
										// create a replicate with one miape msi
										Replicate replicate = new Replicate(cpReplicate.getName(),
												cpExperiment.getName(), null, miapeMSIs, null, Integer.MAX_VALUE, // para
																													// que
																													// no
																													// coja
																													// péptidos
																													// y
																													// vaya
																													// más
																													// rápido,
																													// ya
																													// que
																													// aquí
																													// no
																													// se
																													// necesitan
												OntologyLoaderTask.getCvManager(), processInParallel);

										// check databaseNames
										Set<String> databases = replicate.getDifferentSearchedDatabases();
										if (databases != null) {
											for (String database : databases) {
												if (!databaseNames.contains(database))
													databaseNames.add(database);
											}
										}

										final List<String> peptideScoreNames = replicate.getPeptideScoreNames();
										if (repScoreNames.isEmpty()) {
											for (String scoreName : peptideScoreNames) {
												repScoreNames.add(scoreName);
											}
											Collections.sort(repScoreNames);
										} else {
											String message = "<html>Some MIAPEs seems to have been searched by different search engines in level 2 node "
													+ "'" + cpExperiment.getName()
													+ "'<br>If you continue, you will not be able to apply a valid FDR threshold at level 2."
													+ "<br>Do you want to continue?</html>";

											if (repScoreNames.size() != peptideScoreNames.size()) {
												throw new IllegalMiapeArgumentException(message);
											}
											Collections.sort(peptideScoreNames);
											for (int i = 0; i < peptideScoreNames.size(); i++) {
												if (!peptideScoreNames.get(i).equals(repScoreNames.get(i)))
													throw new IllegalMiapeArgumentException(message);
											}

										}
										if (!replicateScoreNames.isEmpty()) {
											for (Integer miape_msi_id : replicateScoreNames.keySet()) {
												final List<String> scores = replicateScoreNames.get(miape_msi_id);
												if (scores != null && !scores.isEmpty())
													if (!hasOneElementInCommon(scores, peptideScoreNames))
														throw new IllegalMiapeArgumentException("<html>Level 2 node '"
																+ cpReplicate.getName() + "' and '" + miape_msi_id
																+ "' from level 1 node '" + expList.getName()
																+ "' seems to have been searched by different search engines.<br>"
																+ "If you continue, you will not be able to apply a valid FDR threshold."
																+ "<br>Do you want to continue?</html>");
											}
										}
										replicateScoreNames.put(cpMSI.getId(), peptideScoreNames);
									} else {
										listofNonExistingFiles.add(cpMSI.getId());
									}
								}
						}
					}
				}
			}
		}
		// if there is more than one database name, throw a warning
		if (databaseNames.size() > 1) {
			throw new IllegalMiapeArgumentException(
					"<html><b>Warning:</b><br>" + "Not all data have been searched with the same protein database:<br>"
							+ "'" + getCSVStringFromStringList(databaseNames) + "'<br>"
							+ "If you continue it is possible that identifiers from different databases<br>"
							+ "will not have the same format and the comparisons will not be performed correctly.<br>"
							+ "Do you want to continue?</html>");
		}
		if (!listofNonExistingFiles.isEmpty()) {
			// Check if they are in the UnnatendedRetriever waiting to be
			// dowloaded
			List<Integer> miapesNotBeingDownloaded = new ArrayList<Integer>();
			List<Integer> miapesWaitingToBeDownloaded = new ArrayList<Integer>();
			List<Integer> miapesBeingDownloaded = new ArrayList<Integer>();
			for (Integer miapeId : listofNonExistingFiles) {
				javax.swing.SwingWorker.StateValue downloadingState = MiapeRetrieverManager
						.getInstance(MainFrame.userName, MainFrame.password)
						.getDownloadingState("MSI", Integer.valueOf(miapeId));
				if (downloadingState == null) {
					miapesNotBeingDownloaded.add(miapeId);
				} else if (javax.swing.SwingWorker.StateValue.PENDING.equals(downloadingState)) {
					miapesWaitingToBeDownloaded.add(miapeId);
				} else if (javax.swing.SwingWorker.StateValue.STARTED.equals(downloadingState)) {
					miapesBeingDownloaded.add(miapeId);
				} else {
					log.info("MIRAR ESTO: " + miapeId);
				}
			}
			String message = "<html><b>Warning:</b><br>" + "Some MIAPE MSIs are not stored locally in "
					+ FileManager.getMiapeDataPath() + ".<br>" + "Some MIAPEs requested in the comparison project";
			if (!miapesNotBeingDownloaded.isEmpty()) {
				message = message + " ('" + getCSVStringFromIntegerList(miapesNotBeingDownloaded)
						+ "') are not being downloaded from server."
						+ "<br>Please, go back and login in the ProteoRed MIAPE repository.<br>";
				for (Integer integer : miapesNotBeingDownloaded) {
					MiapeRetrieverManager.getInstance(MainFrame.userName, MainFrame.password).addRetrieving(integer,
							"MSI", parent);
				}
			} else {
				if (!miapesBeingDownloaded.isEmpty()) {
					message = message + " ('" + getCSVStringFromIntegerList(miapesBeingDownloaded)
							+ "') are still being downloaded from server."
							+ "<br>Please, wait some minutes and then try again.<br>";
				} else if (!miapesWaitingToBeDownloaded.isEmpty()) {
					message = message + " ('" + getCSVStringFromIntegerList(miapesWaitingToBeDownloaded)
							+ "') are waiting in the queue for being downloaded from server."
							+ "<br>Please, wait some minutes and then try again.<br>";
				}

			}

			message = message + "If you continue some data will be missing.<br>" + "Do you want to continue?</html>";
			throw new IllegalMiapeArgumentException(message);
		}
		// check names
		checkExperimentListNames(expList);

	}

	private String getCSVStringFromIntegerList(List<Integer> list) {
		String ret = "";
		if (list != null) {
			for (Object string : list) {
				if (!"".equals(ret))
					ret = ret + ", ";
				ret = ret + string.toString();
			}
		}
		return ret;
	}

	private String getCSVStringFromStringList(List<String> list) {
		String ret = "";
		if (list != null) {
			for (Object string : list) {
				if (!"".equals(ret))
					ret = ret + ", ";
				ret = ret + string;
			}
		}
		return ret;
	}

	private void checkExperimentListNames(CPExperimentList cpExpList) throws IllegalMiapeArgumentException {
		Set<String> experimentNames = new HashSet<String>();
		if (cpExpList != null) {
			final List<CPExperiment> experiments = cpExpList.getCPExperiment();
			if (experiments != null) {
				boolean thereIsAReplicate = false;
				for (CPExperiment cpExperiment : experiments) {
					String expName = cpExperiment.getName();
					if (experimentNames.contains(expName))
						throw new IllegalMiapeArgumentException(
								"<html>Error in project tree.<br>Experiment name duplicated: '" + expName
										+ "'.<br>If two experiments have the same name, some charts can show not valid data!.<br>Do you want to continue?</html>");
					else
						experimentNames.add(expName);
					final List<CPReplicate> replicates = cpExperiment.getCPReplicate();
					Set<String> replicateNames = new HashSet<String>();
					if (replicates != null) {
						for (CPReplicate cpReplicate : replicates) {
							thereIsAReplicate = true;
							String repName = cpReplicate.getName();
							if (replicateNames.contains(repName))
								throw new IllegalMiapeArgumentException(
										"<html>Error in project tree: Replicate name duplicated: '" + repName
												+ "' in experiment: '" + expName
												+ "'.<br>Do you want to continue?</html>");
							else
								replicateNames.add(repName);
						}
					}
				}
				if (!thereIsAReplicate) {
					throw new IllegalMiapeArgumentException(
							"<html>Error in project tree: A replicate is needed.<br>Do you want to continue?</html>");
				}
			} else {
				throw new IllegalMiapeArgumentException(
						"<html>Error in project tree: An experiment is needed.<br>Do you want to continue?</html>");
			}
		}
	}

	private boolean hasOneElementInCommon(List<String> list1, List<String> list2) {

		for (String string1 : list1) {
			for (String string2 : list2) {
				if (string1.equals(string2))
					return true;
			}
		}
		return false;
	}

	private MiapeMSIDocument getMIAPEMSIFromFile(CPMSI cpMSI, boolean curated) {

		File file = null;
		if (cpMSI.isLocal() == null || !cpMSI.isLocal()) {
			file = new File(FileManager.getMiapeDataPath() + cpMSI);
		} else if (cpMSI.isLocal() != null && cpMSI.isLocal()) {
			if (!curated) {
				file = new File(FileManager.getMiapeMSIXMLFileLocalPathFromMiapeInformation(cpMSI));
			} else {
				file = new File(FileManager.getMiapeMSICuratedXMLFilePathFromMiapeInformation(
						cpMSI.getLocalProjectName(), cpMSI.getId(), cpMSI.getName()));
			}
		} else if (cpMSI.isManuallyCreated() != null && cpMSI.isManuallyCreated()) {
			file = FileManager.getManualIdSetFile(cpMSI.getName());
		}
		if (file == null || !file.exists())
			return null;
		MiapeMSIDocument ret;
		MIAPEMSIXmlFile msiFile = new MIAPEMSIXmlFile(file);
		ControlVocabularyManager cvManager = OntologyLoaderTask.getCvManager();
		msiFile.setCvUtil(cvManager);
		try {
			ret = msiFile.toDocument();

			return ret;
		} catch (MiapeDatabaseException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		} catch (MiapeSecurityException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}
