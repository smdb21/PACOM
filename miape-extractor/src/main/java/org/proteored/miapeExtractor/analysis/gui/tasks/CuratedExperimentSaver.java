package org.proteored.miapeExtractor.analysis.gui.tasks;

import java.awt.Frame;
import java.io.IOException;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.proteored.miapeExtractor.analysis.conf.jaxb.CPExperiment;
import org.proteored.miapeExtractor.analysis.conf.jaxb.CPMS;
import org.proteored.miapeExtractor.analysis.conf.jaxb.CPMSI;
import org.proteored.miapeExtractor.analysis.conf.jaxb.CPMSIList;
import org.proteored.miapeExtractor.analysis.conf.jaxb.CPMSList;
import org.proteored.miapeExtractor.analysis.conf.jaxb.CPReplicate;
import org.proteored.miapeExtractor.analysis.gui.CuratedExperimentNameCreatorDialog;
import org.proteored.miapeExtractor.analysis.gui.CuratedExperimentNamePane;
import org.proteored.miapeExtractor.analysis.util.FileManager;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.experiment.msi.MiapeMSIFiltered;
import org.proteored.miapeapi.interfaces.msi.MiapeMSIDocument;

public class CuratedExperimentSaver extends SwingWorker<Void, Void> {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");
	public static final String CURATED_EXP_SAVER_START = "curated exp saver start";
	public static final String CURATED_EXP_SAVER_PROGRESS = "curated exp saver progress";
	public static final String CURATED_EXP_SAVER_END = "curated exp saver end";
	public static final String CURATED_EXP_SAVER_ERROR = "curated exp saver error";
	private final ExperimentList experimentList;
	private final ControlVocabularyManager cvManager;
	private final Frame parentFrame;

	public CuratedExperimentSaver(Frame parentFrame, ExperimentList idSet,
			ControlVocabularyManager cvManager) {
		this.experimentList = idSet;
		this.cvManager = cvManager;
		this.parentFrame = parentFrame;
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			log.info("Creating curated experiments");
			firePropertyChange(CURATED_EXP_SAVER_START, null, null);

			final CuratedExperimentNameCreatorDialog showCurateExperimentNameDialog = CuratedExperimentNamePane
					.showCurateExperimentNameDialog(this.parentFrame,
							experimentList);

			final List<Experiment> experimentList2 = experimentList
					.getExperiments();

			if (experimentList2 != null) {
				for (Experiment experiment : experimentList2) {

					CPExperiment cpExp = new CPExperiment();
					final String experimentName = showCurateExperimentNameDialog
							.getExperimentName(experiment.getName());
					cpExp.setName(experimentName);
					cpExp.setCurated(true);
					if (FileManager.existsCuratedExperimentXMLFile(cpExp
							.getName()))
						log.info("The curated experiment " + cpExp.getName()
								+ " is going to be overwrited");
					final List<Replicate> replicates = experiment
							.getReplicates();
					int i = 1;
					int total = replicates.size();
					for (Replicate replicate : replicates) {

						setProgress(i++ * 100 / total);
						firePropertyChange(
								CURATED_EXP_SAVER_PROGRESS,
								null,
								"Saving: '" + replicate.getName()
										+ "' from experiment '"
										+ cpExp.getName() + "'");
						final List<MiapeMSIDocument> miapeMSIs = replicate
								.getMiapeMSIs();
						if (miapeMSIs != null) {

							CPReplicate cpRep = new CPReplicate();
							cpRep.setName(replicate.getName());

							cpExp.getCPReplicate().add(cpRep);

							CPMSIList cpMsiList = new CPMSIList();
							CPMSList cpMsList = new CPMSList();
							for (MiapeMSIDocument miapeMSIDocument : miapeMSIs) {
								CPMSI cpMSI = new CPMSI();
								cpMSI.setId(miapeMSIDocument.getId());

								// TODO fireproperty(saving idset.getname
								MiapeMSIFiltered miapeMSIFiltered = new MiapeMSIFiltered(
										miapeMSIDocument, replicate, cvManager);
								final int msDocumentReference = miapeMSIDocument
										.getMSDocumentReference();
								if (msDocumentReference > 0) {
									CPMS cpMS = new CPMS();
									cpMS.setId(msDocumentReference);
									cpMS.setName(FileManager
											.getMiapeMSFileName(msDocumentReference));
									cpMsList.getCPMS().add(cpMS);
								}

								try {
									String savedPath = FileManager
											.saveCuratedMiapeMSI(
													cpExp.getName(),
													miapeMSIFiltered);
									log.info("MIAPE MSI filtered saved at: "
											+ savedPath);
									cpMSI.setName(FileManager
											.getMiapeMSIFileName(miapeMSIDocument
													.getId()));
									cpMsiList.getCPMSI().add(cpMSI);
								} catch (IOException ex) {
									ex.printStackTrace();
									log.warn(ex.getMessage());
								}
							}
							cpRep.setCPMSIList(cpMsiList);
							cpRep.setCPMSList(cpMsList);
						}
					}
					// save configuration file
					FileManager.saveCuratedExperimentFile(cpExp);
				}
			}

			firePropertyChange(CURATED_EXP_SAVER_END, null, null);
		} catch (Exception e) {
			firePropertyChange(CURATED_EXP_SAVER_ERROR, null, e.getMessage());
		}
		return null;

	}
}
