package org.proteored.pacom.utils;

import java.io.File;
import java.util.Date;

import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.factories.MiapeDocumentFactory;
import org.proteored.miapeapi.factories.ms.MiapeMSDocumentFactory;
import org.proteored.miapeapi.interfaces.MiapeDate;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.text.tsv.msi.TableTextFileSeparator;
import org.proteored.miapeapi.xml.ms.MiapeMSDocumentImpl;
import org.proteored.miapeapi.xml.ms.merge.MiapeMSMerger;
import org.proteored.pacom.gui.importjobs.AssociatedMSInputFileType;
import org.proteored.pacom.gui.importjobs.InputFileType;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;

public class MiapeExtractionRunParametersImpl implements MiapeExtractionRunParameters {

	private MiapeMSDocument miapeMSMetadata;

	private String projectName;

	private File inputFile;
	private File associatedMSFile;
	private InputFileType inputFileType;
	private Integer id;
	private Integer associatedMiapeMS;
	private Integer associatedMiapeMSGeneratorJob;
	private String templateMSMetadataName;
	private TableTextFileSeparator separator;

	private AssociatedMSInputFileType associatedMSFileType;

	@Override
	public boolean isFastParsing() {
		return true;
	}

	@Override
	public boolean isMzIdentMLPlusMGFSelected() {
		return InputFileType.MZIDENTMLPLUSMGF == inputFileType;
	}

	@Override
	public MiapeMSDocument getMiapeMSMetadata() {
		return miapeMSMetadata;
	}

	@Override
	public boolean isMzMLPlusMzIdentMLSelected() {
		return InputFileType.MZIDENTMLPLUSMZML == inputFileType;
	}

	@Override
	public boolean isMIAPEMSChecked() {
		if (inputFileType != null) {
			switch (inputFileType) {
			case DTASELECTPLUSMGF:
			case MZIDENTMLPLUSMGF:
			case MZIDENTMLPLUSMZML:
			case PEPXMLPLUSMGF:
			case XTANDEMPLUSMGF:

				return true;

			default:
				return false;
			}
		}
		return false;
	}

	@Override
	public String getProjectName() {
		return projectName;
	}

	@Override
	public boolean isMIAPEMSIChecked() {
		return true;
	}

	@Override
	public boolean isXTandemSelected() {
		switch (inputFileType) {
		case XTANDEM:
		case XTANDEMPLUSMGF:
			return true;

		default:
			return false;
		}
	}

	@Override
	public boolean isPRIDESelected() {
		return InputFileType.PRIDEXML == inputFileType;

	}

	@Override
	public boolean isMzMLSelected() {
		return InputFileType.MZIDENTMLPLUSMZML == inputFileType;

	}

	@Override
	public boolean isMzIdentMLSelected() {
		switch (inputFileType) {
		case MZIDENTML:
		case MZIDENTMLPLUSMGF:
		case MZIDENTMLPLUSMZML:
			return true;
		default:
			return false;
		}

	}

	public void setMiapeMSMetadata(MiapeMSDocument miapeMSMetadata) {
		this.miapeMSMetadata = miapeMSMetadata;
	}

	public void setTemplateName(String name) {
		templateMSMetadataName = name;
	}

	@Override
	public String getMSMetadataTemplateName() {
		return templateMSMetadataName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	@Override
	public String getDescription() {
		return MiapeExtractionParametersUtil.getDescription(this);
	}

	@Override
	public String toString() {
		return "MiapeExtractionRunParametersImpl [miapeMSMetadata=" + miapeMSMetadata + ", projectName=" + projectName
				+ ", inputFile=" + inputFile + ", associatedMSFile=" + associatedMSFile + ", inputFileType="
				+ inputFileType + ", id=" + id + ", associatedMiapeMS=" + associatedMiapeMS
				+ ", associatedMiapeMSGeneratorJob=" + associatedMiapeMSGeneratorJob + ", templateMSMetadataName="
				+ templateMSMetadataName + ", separator=" + separator + "]";
	}

	public void consolidate() {
		if (getInputFile() == null) {
			throw new IllegalMiapeArgumentException("Input file is missing");
		}
		if (isMIAPEMSChecked() && getAssociatedMSFile() == null) {
			throw new IllegalMiapeArgumentException("Associated MS File is missing");
		}
		if ((isMzIdentMLPlusMGFSelected() || isXtandemPlusMGFSelected() || isDTASelectPlusMGFSelected()
				|| isPepXMLPlusMGFSelected()) && getMiapeMSMetadata() == null)
			throw new IllegalMiapeArgumentException(
					"Some Mass Spectrometry metadata template is required for importing datasets from MGF + mzIdentML, MGF + XTandem, MGF + DTASelect or MGF + pepXML. Include a METADATA line type in the batch file.");

		if (getProjectName() == null || "".equals(getProjectName())) {
			throw new IllegalMiapeArgumentException("Project name is required");
		}

		if (getMiapeMSMetadata() != null && getAssociatedMiapeMSGeneratorJob() != null)
			throw new IllegalMiapeArgumentException("METADATA and MS_REF cannot be present at the same job");

		// Merge metadata with a MIAPE MS containing only the project name
		if (miapeMSMetadata != null) {

			MiapeExtractionParametersUtil.setNameToMetadataMiapeMS((MiapeMSDocumentImpl) miapeMSMetadata, this);
			// merge with a MIAPE_MS containing only the project
			final MiapeDate today = new MiapeDate(new Date());
			final MiapeMSDocument miapeMSJustWithProject = (MiapeMSDocument) MiapeMSDocumentFactory
					.createMiapeMSDocumentBuilder(MiapeDocumentFactory.createProjectBuilder(getProjectName())
							.date(new MiapeDate(new Date())).build(), miapeMSMetadata.getName(), null)
					.date(today).modificationDate(new Date()).build();
			final MiapeMSDocument ret = MiapeMSMerger.getInstance(OntologyLoaderTask.getCvManager())
					.merge(miapeMSMetadata, miapeMSJustWithProject);
			setMiapeMSMetadata(ret);

		}
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

	public void setAssociatedMIAPEMSGeneratorJob(Integer associatedMiapeMSGeneratorJob) {
		this.associatedMiapeMSGeneratorJob = associatedMiapeMSGeneratorJob;
	}

	@Override
	public Integer getAssociatedMiapeMSGeneratorJob() {
		return associatedMiapeMSGeneratorJob;
	}

	@Override
	public boolean isMGFSelected() {
		switch (inputFileType) {
		case DTASELECTPLUSMGF:
		case MZIDENTMLPLUSMGF:
		case PEPXMLPLUSMGF:
		case XTANDEMPLUSMGF:
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean isXTandemPlusMGFSelected() {
		return InputFileType.XTANDEMPLUSMGF == inputFileType;
	}

	/**
	 * @return the xtandemPlusMGFSelected
	 */
	public boolean isXtandemPlusMGFSelected() {
		return InputFileType.PEPXMLPLUSMGF == inputFileType;
	}

	/**
	 * @param associatedMiapeMSGeneratorJob
	 *            the associatedMiapeMSGeneratorJob to set
	 */
	public void setAssociatedMiapeMSGeneratorJob(Integer associatedMiapeMSGeneratorJob) {
		this.associatedMiapeMSGeneratorJob = associatedMiapeMSGeneratorJob;
	}

	@Override
	public boolean isDTASelectSelected() {
		switch (inputFileType) {
		case DTASELECT:
		case DTASELECTPLUSMGF:
			return true;

		default:
			return false;
		}
	}

	@Override
	public boolean isDTASelectPlusMGFSelected() {
		return InputFileType.DTASELECTPLUSMGF == inputFileType;
	}

	@Override
	public boolean isPepXMLSelected() {
		switch (inputFileType) {
		case PEPXML:
		case PEPXMLPLUSMGF:
			return true;

		default:
			return false;
		}
	}

	@Override
	public boolean isPepXMLPlusMGFSelected() {
		return InputFileType.PEPXMLPLUSMGF == inputFileType;
	}

	@Override
	public boolean isTSVSelected() {
		return InputFileType.TABLETEXT == inputFileType;
	}

	@Override
	public TableTextFileSeparator getSeparator() {
		return separator;
	}

	public void setSeparator(TableTextFileSeparator separator) {
		this.separator = separator;
	}

	@Override
	public InputFileType getInputFileType() {
		return inputFileType;
	}

	public void setInputFileType(InputFileType inputFileType) {
		this.inputFileType = inputFileType;
	}

	@Override
	public File getInputFile() {
		return inputFile;
	}

	@Override
	public String getInputFileName() {
		if (inputFile != null) {
			return inputFile.getAbsolutePath();
		}
		return "";
	}

	@Override
	public File getAssociatedMSFile() {
		return associatedMSFile;
	}

	@Override
	public String getAssociatedMSFileName() {
		if (associatedMSFile != null) {
			return associatedMSFile.getAbsolutePath();
		}
		return "";
	}

	@Override
	public AssociatedMSInputFileType getAssociatedMSFileType() {
		return associatedMSFileType;
	}

	public void setAssociatedMSFileType(AssociatedMSInputFileType associatedMSFileType) {
		this.associatedMSFileType = associatedMSFileType;
	}

	public void setAssociatedMSFile(File file) {
		associatedMSFile = file;
	}

	@Override
	public boolean isProteinPilotSelected() {
		return InputFileType.PROTEINPILOT == inputFileType;
	}

}
