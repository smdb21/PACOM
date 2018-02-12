package org.proteored.pacom.analysis.gui.components;

import javax.swing.tree.DefaultMutableTreeNode;

public class ExtendedJTree extends AbstractExtendedJTree<DefaultMutableTreeNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5991099559171956740L;

	@Override
	public DefaultMutableTreeNode createNode() {
		return new DefaultMutableTreeNode();
	}

	@Override
	public DefaultMutableTreeNode createNode(Object objectNode) {
		return new DefaultMutableTreeNode(objectNode);
	}

}
