package org.proteored.miapeExtractor.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.proteored.miapeExtractor.gui.tasks.OntologyLoaderTask;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.factories.MiapeDocumentFactory;
import org.proteored.miapeapi.factories.ms.MiapeMSDocumentFactory;
import org.proteored.miapeapi.interfaces.MiapeDate;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.xml.ms.MiapeMSDocumentImpl;
import org.proteored.miapeapi.xml.ms.merge.MiapeMSMerger;

public class MiapeExtractionRunParametersImpl implements
		MiapeExtractionRunParameters {

	private boolean isFastParsing;
	private boolean isMzIdentMLPlusMGFSelected;
	private MiapeMSDocument miapeMSMetadata;
	private boolean isMzMLPlusMzIdentMLSelected;
	private boolean isMIAPEMSChecked;
	private String projectName;
	private boolean isMIAPEMSIChecked;
	private boolean isXTandemSelected;
	private boolean isPRIDESelected;
	private boolean isMzMLSelected;
	private boolean isMzIdentMLSelected;
	private boolean isLocalProcessing = false;
	private String mzIdentMLFileName;
	private String mzMLFileName;
	private String mgfFileName;
	private String PRIDEXMLFileName;
	private String xTandemFileName;
	private List<File> inputFiles = new ArrayList<File>();
	private Integer id;
	private Integer associatedMiapeMS;
	private Integer associatedMiapeMSGeneratorJob;
	private String templateName;
	private boolean isMgfSelected;
	private boolean storeMIAPEsInDB;
	private boolean xtandemPlusMGFSelected;

	@Override
	public boolean isFastParsing() {
		return isFastParsing;
	}

	@Override
	public boolean isMzIdentMLPlusMGFSelected() {
		return isMzIdentMLPlusMGFSelected;
	}

	@Override
	public MiapeMSDocument getMiapeMSMetadata() {
		return miapeMSMetadata;
	}

	@Override
	public boolean isMzMLPlusMzIdentMLSelected() {
		return isMzMLPlusMzIdentMLSelected;
	}

	@Override
	public boolean isMIAPEMSChecked() {
		return isMIAPEMSChecked;
	}

	@Override
	public String getProjectName() {
		return projectName;
	}

	@Override
	public boolean isMIAPEMSIChecked() {
		return isMIAPEMSIChecked;
	}

	@Override
	public boolean isXTandemSelected() {
		return isXTandemSelected;
	}

	@Override
	public boolean isPRIDESelected() {
		return isPRIDESelected;
	}

	@Override
	public boolean isMzMLSelected() {
		return isMzMLSelected;
	}

	@Override
	public boolean isMzIdentMLSelected() {
		return isMzIdentMLSelected;
	}

	@Override
	public boolean isLocalProcessing() {
		return isLocalProcessing;
	}

	@Override
	public String getMzIdentMLFileName() {
		return mzIdentMLFileName;
	}

	@Override
	public String getMgfFileName() {
		return mgfFileName;
	}

	@Override
	public String getMzMLFileName() {
		return mzMLFileName;
	}

	@Override
	public String getPRIDEXMLFileName() {
		return PRIDEXMLFileName;
	}

	@Override
	public String getXTandemFileName() {
		return xTandemFileName;
	}

	public void setFastParsing(boolean isFastParsing) {
		this.isFastParsing = isFastParsing;
	}

	public void setMzIdentMLPlusMGFSelected(boolean isMzIdentMLPlusMGFSelected) {
		this.isMzIdentMLPlusMGFSelected = isMzIdentMLPlusMGFSelected;
	}

	public void setMiapeMSMetadata(MiapeMSDocument miapeMSMetadata) {
		this.miapeMSMetadata = miapeMSMetadata;
	}

	public void setTemplateName(String name) {
		templateName = name;
	}

	@Override
	public String getTemplateName() {
		return templateName;
	}

	public void setMzMLPlusMzIdentMLSelected(boolean isMzMLPlusMzIdentMLSelected) {
		this.isMzMLPlusMzIdentMLSelected = isMzMLPlusMzIdentMLSelected;
	}

	public void setMIAPEMSChecked(boolean isMIAPEMSChecked) {
		this.isMIAPEMSChecked = isMIAPEMSChecked;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void setMIAPEMSIChecked(boolean isMIAPEMSIChecked) {
		this.isMIAPEMSIChecked = isMIAPEMSIChecked;
	}

	public void setXTandemSelected(boolean isXTandemSelected) {
		this.isXTandemSelected = isXTandemSelected;
	}

	public void setPRIDESelected(boolean isPRIDESelected) {
		this.isPRIDESelected = isPRIDESelected;
	}

	public void setMzMLSelected(boolean isMzMLSelected) {
		this.isMzMLSelected = isMzMLSelected;
	}

	public void setMzIdentMLSelected(boolean isMzIdentMLSelected) {
		this.isMzIdentMLSelected = isMzIdentMLSelected;
	}

	public void setLocalProcessing(boolean isLocalProcessing) {
		this.isLocalProcessing = isLocalProcessing;
	}

	public void setMzIdentMLFileName(String mzIdentMLFileName) {
		this.mzIdentMLFileName = mzIdentMLFileName;
	}

	public void setMzMLFileName(String mzMLFileName) {
		this.mzMLFileName = mzMLFileName;
	}

	public void setMgfFileName(String mgfFileName) {
		this.mgfFileName = mgfFileName;
	}

	public void setPRIDEXMLFileName(String pRIDEXMLFileName) {
		PRIDEXMLFileName = pRIDEXMLFileName;
	}

	public void setxTandemFileName(String xTandemFileName) {
		this.xTandemFileName = xTandemFileName;
	}

	public void addInputFile(File inputFile) {
		inputFiles.add(inputFile);
	}

	public void setInputFile(Collection<File> inputFiles) {
		this.inputFiles.addAll(inputFiles);
	}

	@Override
	public String getDescription() {
		return MiapeExtractionParametersUtil.getDescription(this);
	}

	@Override
	public String toString() {
		return "MiapeExtractionRunParametersImpl [isFastParsing="
				+ isFastParsing + ", isMzIdentMLPlusMGFSelected="
				+ isMzIdentMLPlusMGFSelected + ", miapeMSMetadata="
				+ miapeMSMetadata + ", isMzMLPlusMzIdentMLSelected="
				+ isMzMLPlusMzIdentMLSelected + ", isMIAPEMSChecked="
				+ isMIAPEMSChecked + ", projectName=" + projectName
				+ ", isMIAPEMSIChecked=" + isMIAPEMSIChecked
				+ ", isXTandemSelected=" + isXTandemSelected
				+ ", isPRIDESelected=" + isPRIDESelected + ", isMzMLSelected="
				+ isMzMLSelected + ", isMzIdentMLSelected="
				+ isMzIdentMLSelected + ", isLocalProcessing="
				+ isLocalProcessing + ", mzIdentMLFileName="
				+ mzIdentMLFileName + ", mzMLFileName=" + mzMLFileName
				+ ", mgfFileName=" + mgfFileName + ", PRIDEXMLFileName="
				+ PRIDEXMLFileName + ", xTandemFileName=" + xTandemFileName
				+ "]";
	}

	public void consolidate() {
		boolean someOptionIsCorrect = false;
		if (getMgfFileName() != null && getMzIdentMLFileName() != null) {
			setMzIdentMLPlusMGFSelected(true);
			someOptionIsCorrect = true;
		} else if (getMgfFileName() != null && getXTandemFileName() != null) {
			setXtandemPlusMGFSelected(true);
			someOptionIsCorrect = true;
		} else if (getMgfFileName() != null && getMzIdentMLFileName() == null
				&& getxTandemFileName() == null) {
			setMGFSelected(true);
			someOptionIsCorrect = true;
		} else if (getMzMLFileName() != null && getMzIdentMLFileName() != null) {
			setMzMLPlusMzIdentMLSelected(true);
			someOptionIsCorrect = true;
		} else if (getMzIdentMLFileName() != null && getMzMLFileName() == null) {
			setMzIdentMLSelected(true);
			someOptionIsCorrect = true;
		} else if (getXTandemFileName() != null) {
			setXTandemSelected(true);
			someOptionIsCorrect = true;
		} else if (getPRIDEXMLFileName() != null) {
			setPRIDESelected(true);
			someOptionIsCorrect = true;
		} else if (getMzMLFileName() != null && getMiapeMSMetadata() != null) {
			setMzMLSelected(true);
			someOptionIsCorrect = true;
		}
		if (!someOptionIsCorrect)
			throw new IllegalMiapeArgumentException(
					"Some error has been detected on input batch file");

		if ((isMzIdentMLPlusMGFSelected() || isXtandemPlusMGFSelected())
				&& getMiapeMSMetadata() == null)
			throw new IllegalMiapeArgumentException(
					"Some MIAPE MS metadata template is required for MGF + mzIdentML or MGF + XTandem MIAPE Extraction. Include a METADATA line type in the batch file.");

		if (!isLocalProcessing() && isFastParsing())
			throw new IllegalMiapeArgumentException(
					"Fast parsing is not applicable for remote processing (not LOCAL PROCESSING)");

		if (getMzMLFileName() == null && isFastParsing())
			throw new IllegalMiapeArgumentException(
					"Fast parsing is only applicable for processing MZML files");

		if (getPRIDEXMLFileName() != null
				&& (!isMIAPEMSIChecked() && !isMIAPEMSChecked()))
			throw new IllegalMiapeArgumentException(
					"MS OUTPUT, MSI OUTPUT or MS MSI OUTPUT is required for processing PRIDE XML files");

		if (getProjectName() == null || "".equals(getProjectName())) {
			throw new IllegalMiapeArgumentException("Project name is required");
		}

		if (getInputFiles().isEmpty())
			throw new IllegalMiapeArgumentException("No input files defined!");

		if (getMiapeMSMetadata() != null
				&& getAssociatedMiapeMSGeneratorJob() != null)
			throw new IllegalMiapeArgumentException(
					"METADATA and MS_REF cannot be present at the same job");

		// Merge metadata with a MIAPE MS containing only the project name
		if (miapeMSMetadata != null) {

			MiapeExtractionParametersUtil.setNameToMetadataMiapeMS(
					(MiapeMSDocumentImpl) miapeMSMetadata, this);
			// merge with a MIAPE_MS containing only the project
			MiapeDate today = new MiapeDate(new Date());
			MiapeMSDocument miapeMSJustWithProject = (MiapeMSDocument) MiapeMSDocumentFactory
					.createMiapeMSDocumentBuilder(
							MiapeDocumentFactory
									.createProjectBuilder(getProjectName())
									.date(new MiapeDate(new Date())).build(),
							miapeMSMetadata.getName(), null).date(today)
					.modificationDate(new Date()).build();
			MiapeMSDocument ret = MiapeMSMerger.getInstance(
					OntologyLoaderTask.getCvManager()).merge(miapeMSMetadata,
					miapeMSJustWithProject);
			setMiapeMSMetadata(ret);

		}
	}

	@Override
	public List<File> getInputFiles() {
		return inputFiles;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public Integer getAssociatedMiapeMS() {
		return associatedMiapeMS;
	}

	public void setAssociatedMiapeMS(Integer associatedMiapeMS) {
		this.associatedMiapeMS = associatedMiapeMS;
	}

	public void setAssociatedMIAPEMSGeneratorJob(
			Integer associatedMiapeMSGeneratorJob) {
		this.associatedMiapeMSGeneratorJob = associatedMiapeMSGeneratorJob;
	}

	@Override
	public Integer getAssociatedMiapeMSGeneratorJob() {
		return associatedMiapeMSGeneratorJob;
	}

	@Override
	public boolean isMGFSelected() {
		return isMgfSelected;
	}

	public void setMGFSelected(boolean mgfSelected) {
		isMgfSelected = mgfSelected;
	}

	@Override
	public boolean storeMIAPEsInDB() {
		return storeMIAPEsInDB;
	}

	public void setStoreMIAPEsInDB(boolean storeMIAPEsInDB) {
		this.storeMIAPEsInDB = storeMIAPEsInDB;
	}

	@Override
	public boolean isXTandemPlusMGFSelected() {
		return xtandemPlusMGFSelected;
	}

	/**
	 * @return the isMgfSelected
	 */
	public boolean isMgfSelected() {
		return isMgfSelected;
	}

	/**
	 * @param isMgfSelected
	 *            the isMgfSelected to set
	 */
	public void setMgfSelected(boolean isMgfSelected) {
		this.isMgfSelected = isMgfSelected;
	}

	/**
	 * @return the xtandemPlusMGFSelected
	 */
	public boolean isXtandemPlusMGFSelected() {
		return xtandemPlusMGFSelected;
	}

	/**
	 * @param xtandemPlusMGFSelected
	 *            the xtandemPlusMGFSelected to set
	 */
	public void setXtandemPlusMGFSelected(boolean xtandemPlusMGFSelected) {
		this.xtandemPlusMGFSelected = xtandemPlusMGFSelected;
	}

	/**
	 * @return the xTandemFileName
	 */
	public String getxTandemFileName() {
		return xTandemFileName;
	}

	/**
	 * @return the storeMIAPEsInDB
	 */
	public boolean isStoreMIAPEsInDB() {
		return storeMIAPEsInDB;
	}

	/**
	 * @param inputFiles
	 *            the inputFiles to set
	 */
	public void setInputFiles(List<File> inputFiles) {
		this.inputFiles = inputFiles;
	}

	/**
	 * @param associatedMiapeMSGeneratorJob
	 *            the associatedMiapeMSGeneratorJob to set
	 */
	public void setAssociatedMiapeMSGeneratorJob(
			Integer associatedMiapeMSGeneratorJob) {
		this.associatedMiapeMSGeneratorJob = associatedMiapeMSGeneratorJob;
	}

}
