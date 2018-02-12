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

import org.apache.log4j.Logger;
import org.proteored.pacom.analysis.conf.jaxb.CPNode;

public abstract class AbstractExtendedJTree<T extends DefaultMutableTreeNode> extends JTree {
	private final static Logger log = Logger.getLogger(AbstractExtendedJTree.class);

	public AbstractExtendedJTree() {
		this(false, false);
		super.setEditable(editable);
	}

	public AbstractExtendedJTree(boolean editable, boolean allowDeletion) {
		super();
		super.setEditable(editable);
		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				T selectedNode = getSelectedNode();
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
	public T searchNode(String nodeStr) {
		T node = null;

		// Get the enumeration
		Enumeration enumer = ((T) getModel().getRoot()).breadthFirstEnumeration();

		// iterate through the enumeration
		while (enumer.hasMoreElements()) {
			// get the node
			node = (T) enumer.nextElement();

			// match the string with the user-object of the node
			if (node.getUserObject().toString().startsWith(nodeStr)) {
				// tree node with string found
				return node;
			}
		}

		// tree node with string node found return null
		return null;
	}

	/**
	 * This method takes the node string and traverses the tree till it finds
	 * the node matching the string. If the match is found the node is returned
	 * else null is returned. It keeps looking for the node until it gets a node
	 * with the same string and with the same level.
	 *
	 * @param nodeStr
	 *            node string to search for
	 * @param level
	 *            level, starting by 0 as the root level
	 * @return tree node
	 */
	public T searchNode(String nodeStr, int level) {
		T node = null;

		// Get the enumeration
		Enumeration enumer = ((T) getModel().getRoot()).breadthFirstEnumeration();

		// iterate through the enumeration
		while (enumer.hasMoreElements()) {
			// get the node
			node = (T) enumer.nextElement();
			// match the string with the user-object of the node
			if (node.getUserObject().toString().startsWith(nodeStr)) {
				final int levelFromNode = getLevelFromNode(node);
				if (levelFromNode == level) {
					// tree node with string found
					return node;
				}
			}
		}

		// tree node with string node found return null
		return null;
	}

	private int getLevelFromNode(T node) {
		final TreeNode[] path = node.getPath();
		return path.length - 1;
	}

	public T searchNodeByPath(TreePath selectionPath) {
		T node = null;

		// Get the enumeration
		Enumeration enumer = ((T) getModel().getRoot()).breadthFirstEnumeration();

		// iterate through the enumeration
		while (enumer.hasMoreElements()) {
			// get the node
			node = (T) enumer.nextElement();
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
		T node = searchNode(nodeStr);
		if (node != null) {
			removeNode(node);
			return true;
		}
		return false;
	}

	public T getRootNode() {
		final Object root = getModel().getRoot();
		return (T) root;
	}

	/**
	 * This method removes the passed tree node from the tree and selects
	 * appropiate node
	 *
	 * @param selNode
	 *            node to be removed
	 */
	public void removeNode(T selNode) {
		if (selNode != null) {

			// get the parent of the selected node
			// MutableTreeNode parent = (MutableTreeNode) (selNode.getParent());
			T parent = (T) (selNode.getParent());

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
		final T searchNode = searchNode(nodeStr);
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
	private MutableTreeNode getSibling(T selNode) {
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

	public boolean selectNode(String nodeName) {
		final T foundNode = searchNode(nodeName);
		if (foundNode != null) {
			setSelectionPath(new TreePath(foundNode.getPath()));
			return true;
		}
		return false;
	}

	public boolean selectNode(String nodeName, int level) {
		final T foundNode = searchNode(nodeName, level);
		if (foundNode != null) {
			setSelectionPath(new TreePath(foundNode.getPath()));
			return true;
		}
		return false;
	}

	public boolean selectRoot() {
		T rootNode = (T) getModel().getRoot();
		return selectNode(rootNode);
	}

	public boolean selectNode(T node) {
		if (node != null) {
			setSelectionPath(new TreePath(node.getPath()));
			return true;
		}
		return false;
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

	public T getSelectedNode() {
		final TreePath selectionPath = getSelectionPath();
		if (selectionPath != null) {
			return searchNodeByPath(selectionPath);
		}
		return null;
	}

	public void renameSelectedNode(String nodeName) {
		final TreePath selectionPath = getSelectionPath();
		if (selectionPath != null) {
			T node = searchNodeByPath(selectionPath);
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
		T rootNode = createNode();
		setModel(new DefaultTreeModel(rootNode));
	}

	public T addNewNode(Object nodeObject, T parentNode) {
		final DefaultTreeModel model = (DefaultTreeModel) getModel();

		T newnode = createNode(nodeObject);
		int index = parentNode.getChildCount();

		model.insertNodeInto(newnode, parentNode, index);
		model.reload();
		return newnode;
	}

	public abstract T createNode();

	public abstract T createNode(Object objectNode);

	/**
	 * Remove the selected node of the tree
	 *
	 */
	public void removeSelectedNode() {
		final T node = getSelectedNode();
		if (node != null)
			removeNode(node);
		// reload();
	}

	/**
	 * Expand all the children of this node
	 * 
	 * @param node
	 */
	public void expandNode(T node) {
		if (node.isLeaf()) {
			this.expandPath(new TreePath(node.getPath()));
		} else {
			for (int i = 0; i < node.getChildCount(); i++) {
				expandNode((T) node.getChildAt(i));
			}
		}

	}

	public void collapseNode(T node) {
		this.collapsePath(new TreePath(node.getPath()));
	}

}
