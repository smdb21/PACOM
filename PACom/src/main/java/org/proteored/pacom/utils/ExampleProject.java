package org.proteored.pacom.utils;

public enum ExampleProject {
	EMPTY("", ""), PME6_REANALYSIS("PME6_Reanalysis", "PME6_Reanalysis"), PME6_PEP_1FDR(
			"PME6_Reanalysis - Peptide FDR 1 percnt",
			"PME6 1% FDR pept"), PME6_PROT_1FDR("PME6_Reanalysis - Protein FDR 1 percnt", "PME6 1% FDR prot");
	private final String name;
	private final String abbreviation;

	private ExampleProject(String name, String abbreviation) {
		this.name = name;
		this.abbreviation = abbreviation;
	}

	public String getName() {
		return name;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public static String[] getAbbreviations() {
		String[] ret = new String[values().length];
		int i = 0;
		for (ExampleProject exampleProject : values()) {
			ret[i++] = exampleProject.getAbbreviation();
		}
		return ret;
	}

	public static ExampleProject getByAbbreviation(String abbreviation) {
		for (ExampleProject exampleProject : values()) {
			if (exampleProject.getAbbreviation().equals(abbreviation)) {
				return exampleProject;
			}
		}
		return null;
	}
}
