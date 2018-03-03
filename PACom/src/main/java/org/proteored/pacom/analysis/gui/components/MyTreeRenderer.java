package org.proteored.pacom.analysis.gui.components;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.pacom.analysis.conf.jaxb.CPExperiment;
import org.proteored.pacom.analysis.conf.jaxb.CPMS;
import org.proteored.pacom.analysis.conf.jaxb.CPMSI;
import org.proteored.pacom.analysis.conf.jaxb.CPNode;
import org.proteored.pacom.analysis.conf.jaxb.CPReplicate;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.ImageManager;

public class MyTreeRenderer extends DefaultTreeCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3897102137870400072L;
	private static final Logger log = Logger.getLogger(MyTreeRenderer.class);
	private final Icon starIcon;
	private final Icon documentIcon;
	private final Icon replicateIcon;
	private final Icon experimentIcon;
	private final Icon searchIcon;
	private final Icon prideIcon;
	private final Icon spectrumIcon;
	private final Icon rawIcon;
	private final Icon replicateIncompleteIcon;
	private final Icon experimentIncompleteIcon;

	public MyTreeRenderer() {
		this.starIcon = getCuratedImageIcon();
		this.documentIcon = getDocumentImageIcon();
		this.replicateIcon = getReplicateImageIcon();
		this.experimentIcon = getExperimentImageIcon();
		this.replicateIncompleteIcon = getReplicateIncompleteImageIcon();
		this.experimentIncompleteIcon = getExperimentIncompleteImageIcon();
		this.searchIcon = getSearchImageIcon();
		this.prideIcon = getPrideImageIcon();
		this.spectrumIcon = getSpectrumImageIcon();
		this.rawIcon = getRawImageIcon();
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		log.debug(node + " class of userObject: " + node.getUserObject().getClass().getName());
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
		if (isMIAPEMSIStringNode(value)) {
			setIcon(documentIcon);
			setToolTipText("<html>Imported dataset:<br><b>" + node.getUserObject() + "</b><br>Located at:<br>"
					+ FileManager.getMiapeLocalDataPath(parent.getUserObject().toString()) + node.getUserObject()
					+ ".xml</html>");
			setText(FileManager.getMiapeMSINameFromName(node.getUserObject().toString()));
		} else if (isCuratedExperiment(value)) {
			setIcon(starIcon);
			setToolTipText("Dataset in level 2 node:<br><b>" + node.getUserObject() + "</b><br>Located at:<br>"
					+ FileManager.getMiapeLocalDataPath(parent.getUserObject().toString()) + node.getUserObject()
					+ ".xml</html>");
		} else if (isMIAPENode(value)) {
			setIcon(documentIcon);
			String name = FileManager.getMiapeMSINameFromName(node.getUserObject().toString());
			setText(name);
			CPMSI cpMSI = (CPMSI) node.getUserObject();
			setToolTipText("<html>Imported dataset:<br><b>" + name + "</b><br>Internal file located at:<br>"
					+ FileManager.getMiapeLocalDataPath(cpMSI.getLocalProjectName()) + node.getUserObject()
					+ ".xml</html>");
		} else if (isReplicateNode(value)) {
			setIcon(hasChildren(node) ? replicateIcon : replicateIncompleteIcon);
			setToolTipText(hasChildren(node)
					? "<html>Level 2 node: <b>" + node.getUserObject() + "</b><br>It contains " + node.getChildCount()
							+ " datasets.</html>"
					: "<html>Incomplete level 2 node:<br>Add datasets to this node</html>");
			if (value instanceof DefaultMutableTreeNode
					&& ((DefaultMutableTreeNode) value).getUserObject() instanceof Replicate) {
				Replicate replicate = (Replicate) ((DefaultMutableTreeNode) value).getUserObject();
				value = replicate.getFullName();
			}
		} else if (isExperimentNode(value)) {
			setIcon(hasChildren(node) ? experimentIcon : experimentIncompleteIcon);
			setToolTipText(hasChildren(node)
					? "<html>Level 1 node: <b>" + node.getUserObject() + "</b><br>It contains " + node.getChildCount()
							+ " level 2 nodes.</html>"
					: "<html>Incomplete level 1 node:<br>Add datasets to this node</html>");
			// } else if (isStringNode(value)
			// &&
			// getUserObjectString(value).startsWith(PEXBulkSubmissionSummaryTreeLoaderTask.SEARCH))
			// {
			// setLeafIcon(searchIcon);
			// setToolTipText("search node");
			// } else if (isStringNode(value)
			// &&
			// getUserObjectString(value).startsWith(PEXBulkSubmissionSummaryTreeLoaderTask.RESULT))
			// {
			// setLeafIcon(prideIcon);
			// setToolTipText("search node");
			// } else if (isStringNode(value)
			// &&
			// getUserObjectString(value).startsWith(PEXBulkSubmissionSummaryTreeLoaderTask.OTHER))
			// {
			// setLeafIcon(documentIcon);
			// setToolTipText("MIAPE report node");
			// } else if (isStringNode(value)
			// &&
			// getUserObjectString(value).startsWith(PEXBulkSubmissionSummaryTreeLoaderTask.PEAK))
			// {
			// setLeafIcon(spectrumIcon);
			// setToolTipText("peak list node");
			// } else if (isStringNode(value)
			// &&
			// getUserObjectString(value).startsWith(PEXBulkSubmissionSummaryTreeLoaderTask.RAW))
			// {
			// setLeafIcon(rawIcon);
			// setToolTipText("raw node");
		} else if (parent != null) {
			setToolTipText("<html>Dataset folder:<br><b>"
					+ FileManager.getMiapeLocalDataPath(node.getUserObject().toString()) + "</b></html>");
		} else if (parent == null) {
			setToolTipText("<html>Comparison project:<br><b>" + node.getUserObject() + "</b><br>"
					+ "Internal file located at: " + FileManager.getProjectXMLFilePath(node.getUserObject().toString())
					+ "</html>");
		}

		return this;
	}

	private boolean hasChildren(DefaultMutableTreeNode node) {
		return node.getChildCount() > 0;
	}

	private String getUserObjectString(Object value) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object nodeInfo = node.getUserObject();
		if (nodeInfo instanceof String) {
			return (String) nodeInfo;
		}
		return "";
	}

	private boolean isStringNode(Object value) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object nodeInfo = node.getUserObject();
		if (nodeInfo instanceof String) {
			return true;
		}
		return false;
	}

	private boolean isMIAPEMSIStringNode(Object value) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object nodeInfo = node.getUserObject();
		if (nodeInfo instanceof String) {
			if (((String) nodeInfo).startsWith("MIAPE MSI"))
				return true;
			if (((String) nodeInfo).startsWith(FileManager.MIAPE_MSI_LOCAL_PREFIX))
				return true;
		}
		return false;
	}

	private boolean isReplicateNode(Object value) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object nodeInfo = node.getUserObject();
		if (nodeInfo instanceof CPReplicate || nodeInfo instanceof Replicate) {
			return true;
		}
		return false;
	}

	private boolean isExperimentNode(Object value) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object nodeInfo = node.getUserObject();
		if (nodeInfo instanceof CPExperiment) {
			return true;

		}
		return false;
	}

	private boolean isMIAPENode(Object value) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object nodeInfo = node.getUserObject();
		if (nodeInfo instanceof CPMSI || nodeInfo instanceof CPMS) {
			return true;
		}
		return false;
	}

	protected boolean isCuratedExperiment(Object value) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		if (node.getUserObject() instanceof CPNode) {
			CPNode nodeInfo = (CPNode) node.getUserObject();
			if (nodeInfo instanceof CPExperiment) {
				CPExperiment cpExp = (CPExperiment) nodeInfo;
				if (cpExp.isCurated()) {
					return true;
				}
			}
		}
		return false;
	}

	private Icon getCuratedImageIcon() {
		return new ImageIcon(ImageManager.getImageIcon(ImageManager.CURATED_EXPERIMENT).getImage());
	}

	private Icon getDocumentImageIcon() {
		return new ImageIcon(ImageManager.getImageIcon(ImageManager.DOC).getImage());
	}

	private Icon getExperimentImageIcon() {
		return new ImageIcon(ImageManager.getImageIcon(ImageManager.EXPERIMENT).getImage());
	}

	private Icon getExperimentIncompleteImageIcon() {
		return new ImageIcon(ImageManager.getImageIcon(ImageManager.EXPERIMENT_INCOMPLETE).getImage());
	}

	private Icon getReplicateImageIcon() {
		return new ImageIcon(ImageManager.getImageIcon(ImageManager.REPLICATE).getImage());
	}

	private Icon getReplicateIncompleteImageIcon() {
		return new ImageIcon(ImageManager.getImageIcon(ImageManager.REPLICATE_INCOMPLETE).getImage());
	}

	private Icon getSpectrumImageIcon() {
		return new ImageIcon(ImageManager.getImageIcon(ImageManager.SPECTRUM).getImage());
	}

	private Icon getPrideImageIcon() {
		return new ImageIcon(ImageManager.getImageIcon(ImageManager.PRIDE).getImage());
	}

	private Icon getSearchImageIcon() {
		return new ImageIcon(ImageManager.getImageIcon(ImageManager.SEARCH).getImage());
	}

	private Icon getRawImageIcon() {
		return new ImageIcon(ImageManager.getImageIcon(ImageManager.RAW).getImage());
	}
}
