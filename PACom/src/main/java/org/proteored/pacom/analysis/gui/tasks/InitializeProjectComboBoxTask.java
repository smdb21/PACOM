package org.proteored.pacom.analysis.gui.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.proteored.pacom.analysis.gui.Miape2ExperimentListDialog;
import org.proteored.pacom.analysis.util.FileManager;

public class InitializeProjectComboBoxTask extends SwingWorker<Void, Void> {
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private final JComboBox savedProjectsCombo;
	private final JComboBox curatedExperimentsCombo;

	private final Miape2ExperimentListDialog dialog;

	public InitializeProjectComboBoxTask(Miape2ExperimentListDialog dialog) {
		this.dialog = dialog;
		this.savedProjectsCombo = this.dialog.getProjectComboBox();
		this.curatedExperimentsCombo = this.dialog.getCuratedComboBox();
	}

	@Override
	protected Void doInBackground() throws Exception {
		log.info("Initializing projects combo box");
		List<String> projectList = new ArrayList<String>();
		if (projectList.isEmpty()) {

			// Load user specific projects
			projectList.addAll(FileManager.getProjectList());

		}
		log.info(projectList.size() + " projects");
		// Sort by name
		Collections.sort(projectList, (project1, project2) -> project1.compareToIgnoreCase(project2));
		// this.jComboBox1 = new JComboBox();
		this.savedProjectsCombo.removeAllItems();
		this.savedProjectsCombo.addItem("");
		for (String projectName : projectList) {
			this.savedProjectsCombo.addItem(projectName);
		}
		this.savedProjectsCombo.setSelectedIndex(0);

		log.info("Initializing curated experiments combo box");
		List<String> curatedExpsList = new ArrayList<String>();
		if (curatedExpsList.isEmpty()) {
			curatedExpsList.addAll(FileManager.getCuratedExperimentList());

		}
		log.info(curatedExpsList.size() + " curated experiments");

		if (!curatedExpsList.isEmpty()) {

			this.curatedExperimentsCombo.removeAllItems();
			Thread.sleep(1000);
			// sort by name
			Collections.sort(curatedExpsList);
			this.curatedExperimentsCombo.addItem("");
			for (String projectName : curatedExpsList) {
				this.curatedExperimentsCombo.addItem(projectName);
			}
			this.curatedExperimentsCombo.setSelectedIndex(0);
			firePropertyChange("notificacion", null, curatedExpsList.size() + " curated datasets loaded");
		} else {
			this.curatedExperimentsCombo.addItem("No curated experiments available");
		}
		log.info("Previously saved combo is loaded");
		dialog.setCorrectlyInitialized(true);
		return null;
	}

}
