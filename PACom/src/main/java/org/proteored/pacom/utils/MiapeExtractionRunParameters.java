package org.proteored.pacom.utils;

import java.io.File;
import java.util.List;

import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;

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

	public String getMzIdentMLFileName();

	public String getMgfFileName();

	public String getMzMLFileName();

	public String getPRIDEXMLFileName();

	public String getXTandemFileName();

	public String getDescription();

	public List<File> getInputFiles();

	public Integer getAssociatedMiapeMS();

	public Integer getAssociatedMiapeMSGeneratorJob();

	public String getTemplateName();

	public boolean storeMIAPEsInDB();

	public boolean isXTandemPlusMGFSelected();

	public String getDtaSelectFileName();

	public boolean isDTASelectSelected();

	public boolean isDTASelectPlusMGFSelected();

	public boolean isLocalProcessing();
}
