package org.proteored.pacom.analysis.gui.components;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.bind.JAXBException;

import org.proteored.pacom.analysis.conf.jaxb.CPExperiment;
import org.proteored.pacom.analysis.conf.jaxb.CPMSI;
import org.proteored.pacom.analysis.conf.jaxb.CPNode;
import org.proteored.pacom.analysis.conf.jaxb.CPReplicate;
import org.proteored.pacom.analysis.util.FileManager;

public class MyProjectTreeNode extends DefaultMutableTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8922374921912465168L;

	public MyProjectTreeNode() {
		super();
	}

	public MyProjectTreeNode(CPNode cpNode) {
		super(cpNode);
	}

	@Override
	public void setUserObject(Object userObject) {
		if (userObject instanceof String) {
			setName((String) userObject);
		} else {
			super.setUserObject(userObject);
		}
	}

	private void setName(String name) {
		final Object userObject2 = getUserObject();
		if (userObject2 != null && userObject2 instanceof CPNode) {
			CPNode cpNode = (CPNode) userObject2;
			if (cpNode instanceof CPExperiment) {
				CPExperiment cpExp = (CPExperiment) cpNode;
				if (cpExp.isCurated()) {
					String previousExpName = cpExp.getName();
					String newExpName = name;
					if (!previousExpName.equals(newExpName)) {
						// set also the local_project_name to all the MSIs
						// of the replicates because we are renaming the
						// folder in which they are stored
						for (CPReplicate cpRep : cpExp.getCPReplicate()) {
							if (cpRep.getCPMSIList() != null) {
								for (CPMSI cpMSI : cpRep.getCPMSIList().getCPMSI()) {
									cpMSI.setLocalProjectName(newExpName);
								}
							}
						}
						// RENAME THE FOLDER
						File curatedExpFolder = new File(FileManager.getCuratedExperimentFolderPath(previousExpName));
						File newCuratedExpFolder = new File(FileManager.getCuratedExperimentFolderPath(newExpName));
						curatedExpFolder.renameTo(newCuratedExpFolder);

						// RENAME THE CURATED EXPERIMENT FILE
						File curatedExpFile = new File(
								FileManager.getCuratedExperimentsDataPath() + FileManager.PATH_SEPARATOR + newExpName
										+ FileManager.PATH_SEPARATOR + previousExpName + ".xml");
						File newCuratedExpFile = new File(FileManager.getCuratedExperimentXMLFilePath(newExpName));
						curatedExpFile.renameTo(newCuratedExpFile);
						CPExperiment cpExp2 = FileManager.getCPExperiment(newCuratedExpFile);
						if (cpExp2 != null) {
							cpExp2.setName(newExpName);
							// set also the local_project_name to all the MSIs
							// of the replicates because we are renaming the
							// folder in which they are stored
							for (CPReplicate cpRep : cpExp2.getCPReplicate()) {
								if (cpRep.getCPMSIList() != null) {
									for (CPMSI cpMSI : cpRep.getCPMSIList().getCPMSI()) {
										cpMSI.setLocalProjectName(newExpName);
									}
								}
							}
							try {
								FileManager.saveCuratedExperimentFile(cpExp2);
								setUserObject(cpExp2);
							} catch (JAXBException e) {
								e.printStackTrace();
							}
						}

					}
				}
			}
			cpNode.setName(name);
		}
	}

}
