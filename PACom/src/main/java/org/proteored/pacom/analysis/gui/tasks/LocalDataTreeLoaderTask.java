package org.proteored.pacom.analysis.gui.tasks;

import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.proteored.pacom.analysis.gui.components.ExtendedJTree;
import org.proteored.pacom.analysis.util.FileManager;

public class LocalDataTreeLoaderTask extends SwingWorker<Void, String> {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	private final ExtendedJTree tree;
	public static final String LOCAL_TREE_LOADER_STARTS = "local tree loader starts";
	public static final String LOCAL_TREE_LOADER_FINISHED = "local tree loader finished";
	public static final String LOCAL_TREE_LOADER_ERROR = "local tree loader error";

	public LocalDataTreeLoaderTask(ExtendedJTree jTreeManualMIAPEMSIs) {
		tree = jTreeManualMIAPEMSIs;
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			firePropertyChange(LOCAL_TREE_LOADER_STARTS, null, null);
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Imported datasets");
			DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
			tree.setModel(treeModel);

			int numLocalIdSets = 0;
			final List<String> localMIAPEProjects = FileManager.getlocalMIAPEProjects();

			for (String projectName : localMIAPEProjects) {

				// Para que se pueda interrumpir el proceso
				Thread.sleep(1L);

				// String project_label = "'" + projectName + "'";
				String project_label = projectName;
				// Project project = getProject(idProject, userName, password);
				log.info("Getting datasets for local project: " + projectName);
				List<String> miapeList = FileManager.getLocalMiapesByProjectName(projectName);

				if (!miapeList.isEmpty()) {
					DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(project_label);

					for (String miapeName : miapeList) {
						// just add the MIAPE MSIs
						if (miapeName.startsWith(FileManager.MIAPE_MSI_LOCAL_PREFIX)) {
							numLocalIdSets++;
							DefaultMutableTreeNode miapeNode = new DefaultMutableTreeNode(miapeName);
							projectNode.add(miapeNode);
						}
					}
					rootNode.add(projectNode);
				}

			}
			treeModel.nodeStructureChanged(rootNode);
			treeModel.reload();
			firePropertyChange(LOCAL_TREE_LOADER_FINISHED, null, numLocalIdSets);
		} catch (Exception e) {
			firePropertyChange(LOCAL_TREE_LOADER_ERROR, null, e.getMessage());
		}
		return null;
	}
}
