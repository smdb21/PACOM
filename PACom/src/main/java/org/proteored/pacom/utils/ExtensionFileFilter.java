package org.proteored.pacom.utils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ExtensionFileFilter extends FileFilter {

	private String[] extensions = null;
	private String desc;

	/**
	 * Constructor.
	 *
	 * @param ext
	 *            A list of filename extensions, ie new String[] { "PDF"}.
	 * @param description
	 *            A description of the files.
	 */
	public ExtensionFileFilter(String[] ext, String description) {
		extensions = ext;
		desc = description;
	}

	public ExtensionFileFilter(String ext, String description) {
		extensions = new String[1];
		extensions[0] = ext;
		desc = description;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean accept(File pathname) {
		if (pathname.isDirectory()) {
			return true;
		}
		boolean acceptable = false;
		String name = pathname.getName().toUpperCase();
		for (int i = 0; !acceptable && i < extensions.length; i++) {
			if (name.endsWith(extensions[i].toUpperCase())) {
				acceptable = true;
			}
		}
		return acceptable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return desc;
	}
}
