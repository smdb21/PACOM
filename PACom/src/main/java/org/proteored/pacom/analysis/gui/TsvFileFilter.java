package org.proteored.pacom.analysis.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;

public class TsvFileFilter extends FileFilter {

	@Override
	public boolean accept(File arg0) {
		String extension = FilenameUtils.getExtension(arg0.getName());
		if (extension.equalsIgnoreCase("tsv"))
			return true;
		if (extension.equalsIgnoreCase("xls"))
			return true;
		if (extension.equalsIgnoreCase("txt"))
			return true;
		if (arg0.isDirectory())
			return true;
		return false;
	}

	@Override
	public String getDescription() {
		return "Tab separated values file (tsv)";
	}

}
