package org.proteored.pacom.analysis.gui.components;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.proteored.pacom.analysis.conf.jaxb.CPExperiment;
import org.proteored.pacom.analysis.conf.jaxb.CPExperimentList;
import org.proteored.pacom.analysis.conf.jaxb.CPMSI;
import org.proteored.pacom.analysis.conf.jaxb.CPNode;
import org.proteored.pacom.analysis.conf.jaxb.CPReplicate;

public class ComparisonProjectExtendedJTree extends AbstractExtendedJTree<MyProjectTreeNode> {
	private static final Logger log = Logger.getLogger(ComparisonProjectExtendedJTree.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -7539451960766223407L;
	private int selectedDepth;

	public ComparisonProjectExtendedJTree(boolean editable, boolean allowDeletion) {
		super(editable, allowDeletion);
		addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				TreePath path = e.getNewLeadSelectionPath();
				if (path != null) {
					selectedDepth = path.getPathCount();
					log.debug("Selected depth=" + selectedDepth);
				}
			}
		});
	}

	@Override
	public MyProjectTreeNode createNode() {
		return new MyProjectTreeNode();
	}

	@Override
	public MyProjectTreeNode createNode(Object objectNode) {
		return new MyProjectTreeNode((CPNode) objectNode);
	}

	public int getSelectedDepth() {
		return selectedDepth;
	}

	@Override
	public void removeNode(MyProjectTreeNode selNode) {
		if (selNode.getUserObject() instanceof CPExperimentList) {
			return;
		} else if (selNode.getUserObject() instanceof CPExperiment) {
			final CPExperiment cpExp = (CPExperiment) selNode.getUserObject();
			final CPExperimentList cpExpList = (CPExperimentList) this.getRootNode().getUserObject();
			cpExpList.getCPExperiment().remove(cpExp);

		} else if (selNode.getUserObject() instanceof CPReplicate) {
			final CPReplicate cpRep = (CPReplicate) selNode.getUserObject();

			CPExperiment cpExp = (CPExperiment) ((MyProjectTreeNode) selNode.getParent()).getUserObject();
			cpExp.getCPReplicate().remove(cpRep);

		} else if (selNode.getUserObject() instanceof CPMSI) {
			final CPMSI cpMSI = (CPMSI) selNode.getUserObject();
			CPReplicate cpRep = (CPReplicate) ((MyProjectTreeNode) selNode.getParent()).getUserObject();
			cpRep.getCPMSIList().getCPMSI().remove(cpMSI);

		}

		super.removeNode(selNode);

	}

}
