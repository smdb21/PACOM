package org.proteored.pacom.utils;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.proteored.miapeapi.xml.ms.MiapeMSDocumentImpl;

public class MiapeExtractionParametersUtil {
	public static String getDescription(MiapeExtractionRunParameters params) {
		String ret = "";
		// String miape_extraction = "";
		if (params.isMzIdentMLPlusMGFSelected()) {
			ret = ret + "MGF <i>(" + FilenameUtils.getName(new File(params.getAssociatedMSFileName()).getAbsolutePath())
					+ ")</i> + <br> mzIdentML<i>("
					+ FilenameUtils.getName(new File(params.getInputFileName()).getAbsolutePath()) + ")</i>";
			// miape_extraction = "MS & MSI";
		} else if (params.isMzMLPlusMzIdentMLSelected()) {
			ret = ret + "mzML <i>("
					+ FilenameUtils.getName(new File(params.getAssociatedMSFileName()).getAbsolutePath())
					+ ")</i> + <br> mzIdentML <i>("
					+ FilenameUtils.getName(new File(params.getInputFileName()).getAbsolutePath()) + ")</i>";
			// miape_extraction = "MS & MSI";
		} else if (params.isMzIdentMLSelected()) {
			ret = ret + "mzIdentML <i>(" + FilenameUtils.getName(new File(params.getInputFileName()).getAbsolutePath())
					+ ")</i>";
			// miape_extraction = "MSI";
		} else if (params.isMzMLSelected()) {
			ret = ret + "mzML <i>("
					+ FilenameUtils.getName(new File(params.getAssociatedMSFileName()).getAbsolutePath()) + ")</i>";
			// miape_extraction = "MS";
		} else if (params.isPRIDESelected()) {
			ret = ret + "PRIDE XML <i>(" + FilenameUtils.getName(new File(params.getInputFileName()).getAbsolutePath())
					+ ")</i>";
			// if (params.isMIAPEMSChecked() && params.isMIAPEMSIChecked())
			// miape_extraction = "MS & MSI";
			// else if (params.isMIAPEMSChecked())
			// miape_extraction = "MS";
			// else if (params.isMIAPEMSIChecked())
			// miape_extraction = "MSI";
		} else if (params.isXTandemSelected()) {
			ret = ret + "X!Tandem XML <i>("
					+ FilenameUtils.getName(new File(params.getInputFileName()).getAbsolutePath()) + ")</i>";
			// miape_extraction = "MSI";
		} else if (params.isXTandemPlusMGFSelected()) {
			ret = ret + "MGF <i>(" + FilenameUtils.getName(new File(params.getAssociatedMSFileName()).getAbsolutePath())
					+ ")</i> + <br> X!Tandem XML <i>("
					+ FilenameUtils.getName(new File(params.getInputFileName()).getAbsolutePath()) + ")</i>";
			// miape_extraction = "MSI";
		} else if (params.isDTASelectSelected()) {
			ret = ret + "DTASelect <i>(" + FilenameUtils.getName(new File(params.getInputFileName()).getAbsolutePath())
					+ ")</i>";
			// miape_extraction = "MSI";
		} else if (params.isPepXMLSelected()) {
			ret = ret + "pepXML <i>(" + FilenameUtils.getName(new File(params.getInputFileName()).getAbsolutePath())
					+ ")</i>";
			// miape_extraction = "MSI";
		} else if (params.isTSVSelected()) {
			ret = ret + "Text Table <i>(" + FilenameUtils.getName(new File(params.getInputFileName()).getAbsolutePath())
					+ ")</i>";
			// miape_extraction = "MSI";
		} else if (params.isDTASelectPlusMGFSelected()) {
			ret = ret + "MGF <i>(" + FilenameUtils.getName(new File(params.getAssociatedMSFileName()).getAbsolutePath())
					+ ")</i> + <br> DTASelect <i>("
					+ FilenameUtils.getName(new File(params.getInputFileName()).getAbsolutePath()) + ")</i>";
			// miape_extraction = "MSI";
		} else if (params.isPepXMLPlusMGFSelected()) {
			ret = ret + "MGF <i>(" + FilenameUtils.getName(new File(params.getAssociatedMSFileName()).getAbsolutePath())
					+ ")</i> + <br> pepXML <i>("
					+ FilenameUtils.getName(new File(params.getInputFileName()).getAbsolutePath()) + ")</i>";
			// miape_extraction = "MSI";
		}

		ret = ret + " into project <i>" + params.getProjectName() + "</i>";
		if (params.getMiapeMSMetadata() != null) {
			ret += " + <br> template (" + params.getMSMetadataTemplateName() + ")";
		}

		if (params.getAssociatedMiapeMSGeneratorJob() != null) {
			ret += " + <br> (MS dataset from Job '" + params.getAssociatedMiapeMSGeneratorJob() + "')";
		}
		if (params.getAssociatedMiapeMS() != null) {
			ret += " + <br> (MS dataset '" + params.getAssociatedMiapeMS() + "')";
		}

		return ret;
	}

	public static void setNameToMetadataMiapeMS(MiapeMSDocumentImpl metadataMiapeMS,
			MiapeExtractionRunParameters params) {
		if (metadataMiapeMS != null) {
			String miapeName = "";

			if (params.isMzIdentMLPlusMGFSelected() || params.isDTASelectPlusMGFSelected()
					|| params.isXTandemPlusMGFSelected() || params.isPepXMLPlusMGFSelected()) {
				if (params.getAssociatedMSFileName() != null) {
					miapeName = "MS dataset from '" + FilenameUtils.getName(params.getAssociatedMSFileName()) + "'";
				}
			} else if (params.isMzIdentMLSelected() || params.isXTandemSelected() || params.isDTASelectSelected()
					|| params.isPepXMLSelected()) {
				miapeName = "MS dataset from '" + metadataMiapeMS.getName() + "' metadata";
			} else if (params.isMzMLPlusMzIdentMLSelected() || params.isMzMLSelected()) {
				if (params.getAssociatedMSFileName() != null) {
					miapeName = "MS dataset from '" + FilenameUtils.getName(params.getAssociatedMSFileName()) + "'";
				}
			} else if (params.isPRIDESelected() && params.isMIAPEMSChecked()) {
				miapeName = "MS dataset from '" + FilenameUtils.getName(params.getInputFileName()) + "'";
			}

			if (!"".equals(miapeName))
				metadataMiapeMS.setName(miapeName);
		}

	}
}
