package org.proteored.pacom.gui.importjobs;

import java.util.ArrayList;
import java.util.List;

public enum ImportTaskColumns {
	VALID("Valid", 15, "Is this import task valid or not?"), //
	JOBID("# ID", 15, "Import task identfier"), //
	PROJECT("Dataset folder", 70, "Folder name in which input datasets will be located in the local file system"), //
	FILE("Input file", 80, "Input file name"), //
	FILETYPE("File type", 40, "Input file type"), //
	SEPARATOR("Separator", 40,
			"Text separator (for Input File Type='" + InputFileType.TABLETEXT.getPrimaryFileDescription() + "'"), //
	ASSOCIATEDMSFILE("Assoc. MS file", 80, "Associated MS file (MGF or MzML)"), //
	ASSOCIATEDMSFILETYPE("Assoc. MS file type", 70, "Associated MS file type"), //
	METADATA_TEMPLATE("MS Metadata", 80, "Use of one of the MS metadata templates"), //
	PROGRESS("Progress", 50, "Progress of the import task")//

	;

	private final String name;
	private final int defaultWidth;
	private final String description;

	ImportTaskColumns(String name, int defaultWidth, String description) {
		this.name = name;
		this.defaultWidth = defaultWidth;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public int getDefaultWidth() {
		return defaultWidth;
	}

	public String getDescription() {
		return this.description;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public static List<ImportTaskColumns> getColumns() {
		List<ImportTaskColumns> ret = new ArrayList<ImportTaskColumns>();
		for (ImportTaskColumns importTaskColumn : ImportTaskColumns.values()) {
			ret.add(importTaskColumn);
		}
		return ret;
	}

	public static List<String> getColumnsString() {
		List<String> ret = new ArrayList<String>();

		for (ImportTaskColumns exportedColumns : ImportTaskColumns.values()) {
			ret.add(exportedColumns.getName());
		}

		return ret;
	}

}
