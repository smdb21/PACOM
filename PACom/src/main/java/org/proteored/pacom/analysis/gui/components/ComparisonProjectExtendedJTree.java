package org.proteored.pacom.analysis.gui.components;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.proteored.pacom.analysis.conf.jaxb.CPNode;

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
					log.info("Selected depth=" + selectedDepth);
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

}
