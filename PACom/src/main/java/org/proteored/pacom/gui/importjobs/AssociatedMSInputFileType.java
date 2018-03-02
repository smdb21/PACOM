package org.proteored.pacom.gui.importjobs;

public enum AssociatedMSInputFileType {
	// NULL(""), //
	MGF("mgf"), //
	MZML("mzML");
	private final String description;

	private AssociatedMSInputFileType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public static AssociatedMSInputFileType getFromName(String name) {
		// if (name == null) {
		// return AssociatedMSInputFileType.NULL;
		// }
		for (AssociatedMSInputFileType item : values()) {
			if (item.getDescription().equals(name)) {
				return item;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return description;
	}

	public static AssociatedMSInputFileType[] valuesWithBlank() {
		AssociatedMSInputFileType[] ret = new AssociatedMSInputFileType[values().length + 1];
		ret[0] = null;
		for (int i = 0; i < values().length; i++) {
			ret[i + 1] = values()[i];
		}
		return ret;
	}
}
