package org.proteored.pacom.analysis.gui.components;

import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.log4j.Logger;

public class MyDefaultTreeCellEditor<T extends DefaultMutableTreeNode> extends DefaultTreeCellEditor {
	private final static Logger log = Logger.getLogger(MyDefaultTreeCellEditor.class);
	private String lastUpdatedName;

	public MyDefaultTreeCellEditor(AbstractExtendedJTree<T> tree, DefaultTreeCellRenderer renderer) {
		super(tree, renderer);
	}

	// @Override
	// public Component getTreeCellEditorComponent(JTree tree, Object value,
	// boolean isSelected, boolean expanded,
	// boolean leaf, int row) {
	//
	// return super.getTreeCellEditorComponent(tree, value, isSelected,
	// expanded, leaf, row);
	// }

	@Override
	public boolean isCellEditable(EventObject e) {
		// do not edit the level 3
		if (e instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) e;
			if (mouseEvent.getClickCount() == 2) {
				log.debug("Is cell editable");
				final ComparisonProjectExtendedJTree tree = (ComparisonProjectExtendedJTree) e.getSource();
				final int selectedDepth = tree.getSelectedDepth();
				if (selectedDepth <= 3) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean shouldSelectCell(EventObject e) {
		final ComparisonProjectExtendedJTree tree = (ComparisonProjectExtendedJTree) e.getSource();
		final int selectedDepth = tree.getSelectedDepth();
		if (selectedDepth <= 3) {
			return true;
		}

		return super.shouldSelectCell(e);
	}

	public String getLastUpdatedName() {
		return lastUpdatedName;
	}

}
