package org.proteored.pacom.gui.importjobs;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

public class ImportTasksUtil {
	/**
	 * Try to guess the format of the input file by the name
	 * 
	 * @param inputFile
	 * @return
	 */
	public static InputFileType getSuggestedFileTypeByFileName(File inputFile) {
		String fileName = FilenameUtils.getName(inputFile.getAbsolutePath()).toLowerCase();
		if (fileName.contains("pepxml") || fileName.contains("pep.xml")) {
			return InputFileType.PEPXML;
		}
		if (fileName.contains("pride")) {
			return InputFileType.PRIDEXML;
		}
		if (fileName.contains("mzid") || fileName.contains("mzidentml")) {
			return InputFileType.MZIDENTML;
		}
		if (fileName.contains("tandem")) {
			return InputFileType.XTANDEM;
		}
		if (fileName.contains("dtaselect") || fileName.contains("dta-select")) {
			return InputFileType.DTASELECT;
		}
		String extension = FilenameUtils.getExtension(fileName).toLowerCase();
		if (extension.contains("tsv") || extension.contains("csv") || extension.contains("txt")) {
			return InputFileType.TABLETEXT;
		}
		return null;
	}
}
