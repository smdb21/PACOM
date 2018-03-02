package org.proteored.pacom.gui.importjobs;

import java.util.ArrayList;
import java.util.List;

public enum InputFileType {
	// NULL(""), //
	MZIDENTML("mzIdentML"), //
	PRIDEXML("PRIDE xml"), //
	DTASELECT("DTASelect output file"), //
	XTANDEM("X!Tandem output file"), //
	PEPXML("pepXML"), //
	TABLETEXT("table text file"), //
	MZIDENTMLPLUSMGF("mzIdentML", true), //
	MZIDENTMLPLUSMZML("mzIdentML", true), //
	DTASELECTPLUSMGF("DTASelect output file", true), //
	XTANDEMPLUSMGF("X!Tandem output file", true), //
	PEPXMLPLUSMGF("pepXML", true);//

	private final String primaryFileDescription;
	private final boolean hasAssociatedMS;

	private InputFileType(String primaryFileDescription) {
		this(primaryFileDescription, false);
	}

	private InputFileType(String primaryFileDescription, boolean hasAssociatedMS) {
		this.primaryFileDescription = primaryFileDescription;
		this.hasAssociatedMS = hasAssociatedMS;
	}

	public String getPrimaryFileDescription() {
		return primaryFileDescription;
	}

	public static InputFileType getFromName(String name) {
		if (name == null) {
			return null;
		}
		for (InputFileType value : values()) {
			if (value.getPrimaryFileDescription().equals(name)) {
				return value;
			}
		}
		return null;
	}

	public static List<InputFileType> primaryFileTypesNames() {
		List<InputFileType> ret = new ArrayList<InputFileType>();
		for (InputFileType value : values()) {
			if (!value.hasAssociatedMS) {
				ret.add(value);
			}
		}
		return ret;
	}

	public InputFileType getCorrespondingInputFileType(AssociatedMSInputFileType associatedMSInputFileType) {
		switch (this) {
		case DTASELECT:
			if (AssociatedMSInputFileType.MGF == associatedMSInputFileType) {
				return InputFileType.DTASELECTPLUSMGF;
			}
			break;
		case MZIDENTML:
			if (AssociatedMSInputFileType.MGF == associatedMSInputFileType) {
				return InputFileType.MZIDENTMLPLUSMGF;
			} else if (AssociatedMSInputFileType.MZML == associatedMSInputFileType) {
				return InputFileType.MZIDENTMLPLUSMZML;
			}
			break;
		case PEPXML:
			if (AssociatedMSInputFileType.MGF == associatedMSInputFileType) {
				return InputFileType.PEPXMLPLUSMGF;
			}
			break;
		case XTANDEM:
			if (AssociatedMSInputFileType.MGF == associatedMSInputFileType) {
				return InputFileType.XTANDEMPLUSMGF;
			}
			break;
		case DTASELECTPLUSMGF:
			if (AssociatedMSInputFileType.MGF != associatedMSInputFileType) {
				return DTASELECT;
			}
			break;
		case MZIDENTMLPLUSMGF:
			if (AssociatedMSInputFileType.MGF != associatedMSInputFileType) {
				return MZIDENTML;
			}
			break;
		case MZIDENTMLPLUSMZML:
			if (null == associatedMSInputFileType) {
				return MZIDENTML;
			} else if (AssociatedMSInputFileType.MGF == associatedMSInputFileType) {
				return MZIDENTMLPLUSMGF;
			}
			break;
		case PEPXMLPLUSMGF:
			if (AssociatedMSInputFileType.MGF != associatedMSInputFileType) {
				return PEPXML;
			}
			break;
		case XTANDEMPLUSMGF:
			if (AssociatedMSInputFileType.MGF != associatedMSInputFileType) {
				return XTANDEM;
			}
			break;
		default:
			break;
		}
		return this;
	}

	@Override
	public String toString() {
		return this.primaryFileDescription;
	}

	public boolean isHasAssociatedMS() {
		return hasAssociatedMS;
	}

	public static InputFileType[] valuesWithBlank() {
		InputFileType[] ret = new InputFileType[values().length + 1];
		ret[0] = null;
		for (int i = 0; i < values().length; i++) {
			ret[i - 1] = values()[i];
		}
		return ret;
	}
}
