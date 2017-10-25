package org.proteored.pacom.analysis.gui.components;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.proteored.pacom.analysis.conf.jaxb.CPNode;

public class ExtendedJTree extends JTree {
	public ExtendedJTree() {
		this(false);
	}

	public ExtendedJTree(boolean allowDeletion) {
		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				DefaultMutableTreeNode selectedNode = getSelectedNode();
				if (selectedNode != null) {
					if (allowDeletion
							&& (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
						removeNode(selectedNode);
					}
					if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_SPACE) {
						expandNode(selectedNode);
					}
					if (e.getKeyCode() == KeyEvent.VK_LEFT) {
						collapseNode(selectedNode);
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {

			}
		});
	}

	/**
	 * This method takes the node string and traverses the tree till it finds
	 * the node matching the string. If the match is found the node is returned
	 * else null is returned
	 *
	 * @param nodeStr
	 *            node string to search for
	 * @return tree node
	 */
	public DefaultMutableTreeNode searchNode(String nodeStr) {
		DefaultMutableTreeNode node = null;

		// Get the enumeration
		Enumeration enumer = ((DefaultMutableTreeNode) getModel().getRoot()).breadthFirstEnumeration();

		// iterate through the enumeration
		while (enumer.hasMoreElements()) {
			// get the node
			node = (DefaultMutableTreeNode) enumer.nextElement();

			// match the string with the user-object of the node
			if (node.getUserObject().toString().startsWith(nodeStr)) {
				// tree node with string found
				return node;
			}
		}

		// tree node with string node found return null
		return null;
	}

	public DefaultMutableTreeNode searchNodeByPath(TreePath selectionPath) {
		DefaultMutableTreeNode node = null;

		// Get the enumeration
		Enumeration enumer = ((DefaultMutableTreeNode) getModel().getRoot()).breadthFirstEnumeration();

		// iterate through the enumeration
		while (enumer.hasMoreElements()) {
			// get the node
			node = (DefaultMutableTreeNode) enumer.nextElement();
			TreePath path = new TreePath(node.getPath());
			if (path.equals(selectionPath))
				return node;
		}

		// tree node with string node found return null
		return null;
	}

	/**
	 * This method takes the node string and traverses the tree till it finds
	 * the node matching the string. If the match is found the node is removed
	 * and true is returned. If not is found, false is returned else null is
	 * returned
	 *
	 * @param nodeStr
	 *            node string to search for
	 * @return true or false
	 */
	public boolean removeNodeStartingBy(String nodeStr) {
		DefaultMutableTreeNode node = searchNode(nodeStr);
		if (node != null) {
			removeNode(node);
			return true;
		}
		return false;
	}

	public DefaultMutableTreeNode getRootNode() {
		final Object root = getModel().getRoot();
		return (DefaultMutableTreeNode) root;
	}

	/**
	 * This method removes the passed tree node from the tree and selects
	 * appropiate node
	 *
	 * @param selNode
	 *            node to be removed
	 */
	public void removeNode(DefaultMutableTreeNode selNode) {
		if (selNode != null) {

			// get the parent of the selected node
			// MutableTreeNode parent = (MutableTreeNode) (selNode.getParent());
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) (selNode.getParent());

			// if the parent is not null
			if (parent != null) {
				// get the sibling node to be selected after removing the
				// selected node
				MutableTreeNode toBeSelNode = getSibling(selNode);

				// if there are no siblings select the parent node after
				// removing the node
				if (toBeSelNode == null) {
					toBeSelNode = parent;
				}

				final DefaultTreeModel model = (DefaultTreeModel) getModel();

				// remove the node from the parent
				model.removeNodeFromParent(selNode);

				this.scrollToNode(toBeSelNode);
			}
		}
	}

	public void scrollToNode(MutableTreeNode toBeSelNode) {
		// this.expandAll();
		final DefaultTreeModel model = (DefaultTreeModel) getModel();
		// make the node visible by scroll to it
		TreeNode[] nodes = model.getPathToRoot(toBeSelNode);
		TreePath path = new TreePath(nodes);
		scrollPathToVisible(path);
		setSelectionPath(path);
	}

	public boolean scrollToNode(String nodeStr) {
		final DefaultMutableTreeNode searchNode = searchNode(nodeStr);
		if (searchNode != null) {
			scrollToNode(searchNode);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method returns the previous sibling node if there is no previous
	 * sibling it returns the next sibling if there are no siblings it returns
	 * null
	 *
	 * @param selNode
	 *            selected node
	 * @return previous or next sibling, or parent if no sibling
	 */
	private MutableTreeNode getSibling(DefaultMutableTreeNode selNode) {
		// get previous sibling
		MutableTreeNode sibling = selNode.getPreviousSibling();

		if (sibling == null) {
			// if previous sibling is null, get the next sibling
			sibling = selNode.getNextSibling();
		}

		return sibling;
	}

	public String getStringFromSelection(String regex) {
		if (getSelectionCount() > 0) {
			String code = getLastSelectedComponent();
			final String string = getString(regex, code);
			return string;
		}
		return "";
	}

	public String getStringFromSelection() {
		if (getSelectionCount() > 0) {
			String code = getLastSelectedComponent();

			return code;
		}
		return "";
	}

	public String getStringFromParentOfSelection(String regex) {
		if (getSelectionCount() > 0) {
			String code = getParentOfLastSelectedComponent();

			return getString(regex, code);
		}
		return "";
	}

	public static String getString(String regex, String code) {
		Pattern p = Pattern.compile(regex);
		Matcher m;

		m = p.matcher(code);
		if (m.find()) {
			return m.group(1);
		}
		return "";
	}

	// public int getProjectIDFromSelection() {
	// if (getSelectionCount() > 0) {
	// final String REGEX = "(\\d+):\\s(\\w+)";
	// Pattern p = Pattern.compile(REGEX);
	// Matcher m;
	// String code = null;
	//
	// code = getLastSelectedComponent();
	//
	// m = p.matcher(code);
	// if (m.find()) {
	// return Integer.valueOf(m.group(1));
	// }
	// }
	// return -1;
	// }

	private String getLastSelectedComponent() {
		final TreePath selectionPath = getSelectionPath();
		if (selectionPath != null) {
			final Object lastSelectedComponent = selectionPath.getLastPathComponent();
			if (lastSelectedComponent != null)
				return lastSelectedComponent.toString();
		}
		return null;
	}

	private String getParentOfLastSelectedComponent() {
		final TreePath selectionPath = getSelectionPath();
		if (selectionPath != null) {
			final TreePath parentPath = selectionPath.getParentPath();
			final Object obj = parentPath.getLastPathComponent();
			if (obj != null)
				return obj.toString();
		}
		return null;
	}

	/**
	 * Returns true if one or more elements of the tree are selected and they
	 * are at a certain level
	 *
	 * @return
	 */
	public boolean isSomeNodeSelected(int level) {
		final TreePath[] selectionPaths = getSelectionPaths();
		if (selectionPaths != null) {
			for (int i = 0; i < selectionPaths.length; i++) {
				if (selectionPaths[i].getPathCount() == level)
					return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if one and only one element of the tree is selected and it
	 * is in a vertain level
	 *
	 * @return
	 */
	public boolean isOnlyOneNodeSelected(int level) {
		int pathCount = -1;
		final TreePath selectionPath = getSelectionPath();
		if (selectionPath != null)
			pathCount = selectionPath.getPathCount();

		final int selectionCount = getSelectionCount();

		if (selectionCount == 1 && pathCount == level)
			return true;

		return false;
	}

	public void deselect(TreePath tp) {
		final TreePath[] selectionPaths = getSelectionPaths();
		for (int i = 0; i < selectionPaths.length; i++) {
			if (tp.equals(selectionPaths[i])) {
				removeSelectionPath(tp);
			}
		}
	}

	public boolean isSelected(TreePath tp) {
		final TreePath[] selectionPaths = getSelectionPaths();
		if (selectionPaths != null) {
			for (int i = 0; i < selectionPaths.length; i++) {
				if (tp.equals(selectionPaths[i])) {
					return true;
				}
			}
		}
		return false;
	}

	public void selectNode(String nodeName) {
		final DefaultMutableTreeNode foundNode = searchNode(nodeName);
		if (foundNode != null) {
			setSelectionPath(new TreePath(foundNode.getPath()));
		}
	}

	public void selectNode(DefaultMutableTreeNode node) {
		if (node != null) {
			setSelectionPath(new TreePath(node.getPath()));
		}
		// this.expandAll();
	}

	public void selectNode(TreePath selectionPath) {
		if (selectionPath != null) {
			setSelectionPath(selectionPath);
		}
		// this.expandAll();
	}

	public void deselectAll() {
		removeSelectionPaths(getSelectionPaths());

	}

	public DefaultMutableTreeNode addNewNode(Object nodeObject, DefaultMutableTreeNode parentNode) {
		final DefaultTreeModel model = (DefaultTreeModel) getModel();

		DefaultMutableTreeNode newnode = new DefaultMutableTreeNode(nodeObject);
		int index = parentNode.getChildCount();

		model.insertNodeInto(newnode, parentNode, index);
		model.reload();
		return newnode;
	}

	public DefaultMutableTreeNode getSelectedNode() {
		final TreePath selectionPath = getSelectionPath();
		if (selectionPath != null) {
			return searchNodeByPath(selectionPath);
		}
		return null;
	}

	public void renameSelectedNode(String nodeName) {
		final TreePath selectionPath = getSelectionPath();
		if (selectionPath != null) {
			DefaultMutableTreeNode node = searchNodeByPath(selectionPath);
			CPNode cpNode = (CPNode) node.getUserObject();
			cpNode.setName(nodeName);
		}
		((DefaultTreeModel) getModel()).reload();

	}

	private void expandAll() {
		for (int i = 0; i < getRowCount(); i++) {
			expandRow(i);
		}
	}

	public void reload() {
		final DefaultTreeModel model = (DefaultTreeModel) getModel();
		model.reload();
	}

	public void clear() {
		TreeNode rootNode = new DefaultMutableTreeNode();
		setModel(new DefaultTreeModel(rootNode));
	}

	/**
	 * Remove the selected node of the tree
	 *
	 */
	public void removeSelectedNode() {
		final DefaultMutableTreeNode node = getSelectedNode();
		if (node != null)
			removeNode(node);
		// reload();
	}

	/**
	 * Expand all the children of this node
	 * 
	 * @param node
	 */
	public void expandNode(DefaultMutableTreeNode node) {
		if (node.isLeaf()) {
			this.expandPath(new TreePath(node.getPath()));
		} else {
			for (int i = 0; i < node.getChildCount(); i++) {
				expandNode((DefaultMutableTreeNode) node.getChildAt(i));
			}
		}

	}

	public void collapseNode(DefaultMutableTreeNode node) {
		this.collapsePath(new TreePath(node.getPath()));
	}

}
