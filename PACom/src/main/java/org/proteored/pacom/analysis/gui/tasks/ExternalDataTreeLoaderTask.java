package org.proteored.pacom.analysis.gui.tasks;

import java.util.Collections;
import java.util.List;

import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.proteored.pacom.analysis.gui.components.ExtendedJTree;
import org.proteored.pacom.analysis.util.FileManager;

public class ExternalDataTreeLoaderTask extends SwingWorker<Void, String> {
	private final ExtendedJTree tree;
	public static final String EXTERNAL_TREE_LOADER_STARTS = "external tree loader starts";
	public static final String EXTERNAL_TREE_LOADER_FINISHED = "external tree loader finished";
	public static final String EXTERNAL_TREE_LOADER_ERROR = "external tree loader error";

	public ExternalDataTreeLoaderTask(ExtendedJTree jTreeManualMIAPEMSIs) {
		tree = jTreeManualMIAPEMSIs;
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			firePropertyChange(EXTERNAL_TREE_LOADER_STARTS, null, null);
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("External protein lists");
			DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
			tree.setModel(treeModel);

			int numLocalIdSets = 0;
			final List<String> manualIdSetList = FileManager.getManualIdSetList();
			Collections.sort(manualIdSetList);
			for (String manualIdsetName : manualIdSetList) {
				numLocalIdSets++;
				DefaultMutableTreeNode idSetNode = new DefaultMutableTreeNode(manualIdsetName);
				rootNode.add(idSetNode);
				treeModel.nodeStructureChanged(rootNode);
				treeModel.reload();
			}

			firePropertyChange(EXTERNAL_TREE_LOADER_FINISHED, null, numLocalIdSets);
		} catch (Exception e) {
			firePropertyChange(EXTERNAL_TREE_LOADER_ERROR, null, e.getMessage());
		}
		return null;
	}

}
