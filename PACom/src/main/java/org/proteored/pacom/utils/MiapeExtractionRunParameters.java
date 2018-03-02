package org.proteored.pacom.utils;

import java.io.File;

import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.text.tsv.msi.TableTextFileSeparator;
import org.proteored.pacom.gui.importjobs.AssociatedMSInputFileType;
import org.proteored.pacom.gui.importjobs.InputFileType;

public interface MiapeExtractionRunParameters {

	public boolean isFastParsing();

	public boolean isMzIdentMLPlusMGFSelected();

	public boolean isMGFSelected();

	public MiapeMSDocument getMiapeMSMetadata();

	public boolean isMzMLPlusMzIdentMLSelected();

	public boolean isMIAPEMSChecked();

	public String getProjectName();

	public boolean isMIAPEMSIChecked();

	public boolean isXTandemSelected();

	public boolean isPRIDESelected();

	public boolean isMzMLSelected();

	public boolean isMzIdentMLSelected();

	public boolean isTSVSelected();

	public String getDescription();

	public File getInputFile();

	public String getInputFileName();

	public File getAssociatedMSFile();

	public String getAssociatedMSFileName();

	public Integer getAssociatedMiapeMS();

	public Integer getAssociatedMiapeMSGeneratorJob();

	public String getMSMetadataTemplateName();

	public boolean isXTandemPlusMGFSelected();

	public boolean isDTASelectSelected();

	public boolean isDTASelectPlusMGFSelected();

	public TableTextFileSeparator getSeparator();

	public boolean isPepXMLPlusMGFSelected();

	public boolean isPepXMLSelected();

	public InputFileType getInputFileType();

	public AssociatedMSInputFileType getAssociatedMSFileType();

}
