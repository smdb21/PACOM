package org.proteored.miapeExtractor.utils;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.proteored.miapeapi.xml.ms.MiapeMSDocumentImpl;

public class MiapeExtractionParametersUtil {
	public static String getDescription(MiapeExtractionRunParameters params) {
		String ret = "";
		String miape_extraction = "";
		if (params.isMzIdentMLPlusMGFSelected()) {
			ret = ret
					+ "MGF ("
					+ FilenameUtils.getName(new File(params.getMgfFileName())
							.getAbsolutePath())
					+ ") + <br> mzIdentML("
					+ FilenameUtils.getName(new File(params
							.getMzIdentMLFileName()).getAbsolutePath()) + ")";
			miape_extraction = "MS & MSI";
		} else if (params.isMzIdentMLSelected()) {
			ret = ret
					+ "mzIdentML ("
					+ FilenameUtils.getName(new File(params
							.getMzIdentMLFileName()).getAbsolutePath()) + ")";
			miape_extraction = "MSI";
		} else if (params.isMzMLPlusMzIdentMLSelected()) {
			ret = ret
					+ "mzML ("
					+ FilenameUtils.getName(new File(params.getMzMLFileName())
							.getAbsolutePath())
					+ ") + <br> mzIdentML ("
					+ FilenameUtils.getName(new File(params
							.getMzIdentMLFileName()).getAbsolutePath()) + ")";
			miape_extraction = "MS & MSI";
		} else if (params.isMzMLSelected()) {
			ret = ret
					+ "mzML ("
					+ FilenameUtils.getName(new File(params.getMzMLFileName())
							.getAbsolutePath()) + ")";
			miape_extraction = "MS";
		} else if (params.isPRIDESelected()) {
			ret = ret
					+ "PRIDE XML ("
					+ FilenameUtils.getName(new File(params
							.getPRIDEXMLFileName()).getAbsolutePath()) + ")";
			if (params.isMIAPEMSChecked() && params.isMIAPEMSIChecked())
				miape_extraction = "MS & MSI";
			else if (params.isMIAPEMSChecked())
				miape_extraction = "MS";
			else if (params.isMIAPEMSIChecked())
				miape_extraction = "MSI";
		} else if (params.isXTandemSelected()) {
			ret = ret + "X!Tandem out XML";
			miape_extraction = "MSI";
		}

		ret = "MIAPE " + miape_extraction + " from " + ret;
		if (params.getMiapeMSMetadata() != null) {
			ret += " + <br> template (" + params.getTemplateName() + ")";
		}

		if (params.getAssociatedMiapeMSGeneratorJob() != null) {
			ret += " + <br> (MIAPE MS from Job '"
					+ params.getAssociatedMiapeMSGeneratorJob() + "')";
		}
		if (params.getAssociatedMiapeMS() != null) {
			ret += " + <br> (MIAPE MS '" + params.getAssociatedMiapeMS() + "')";
		}

		return ret;
	}

	public static void setNameToMetadataMiapeMS(
			MiapeMSDocumentImpl metadataMiapeMS,
			MiapeExtractionRunParameters params) {
		if (metadataMiapeMS != null) {
			String miapeName = "";

			if (params.isMzIdentMLPlusMGFSelected()) {
				if (params.getMgfFileName() != null)
					miapeName = "MIAPE MS from '"
							+ FilenameUtils.getName(params.getMgfFileName())
							+ "'";
			} else if (params.isMzIdentMLSelected()
					|| params.isXTandemSelected()) {
				miapeName = "MIAPE MS from '" + metadataMiapeMS.getName()
						+ "' metadata";
			} else if (params.isMzMLPlusMzIdentMLSelected()
					|| params.isMzMLSelected()) {
				if (params.getMzMLFileName() != null)
					miapeName = "MIAPE MS from '"
							+ FilenameUtils.getName(params.getMzMLFileName())
							+ "'";
			} else if (params.isPRIDESelected() && params.isMIAPEMSChecked()) {
				miapeName = "MIAPE MS from '"
						+ FilenameUtils.getName(params.getPRIDEXMLFileName())
						+ "'";
			}

			if (!"".equals(miapeName))
				metadataMiapeMS.setName(miapeName);
		}

	}
}
