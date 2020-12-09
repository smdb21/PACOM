package org.proteored.pacom.analysis.exporters.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.text.tsv.msi.TableTextFileColumn;

public enum ExportedColumns {
	NUMBER("#", 3, ""), //
	EXPERIMENT("Level 1", 30, "Names of the level 1 nodes in the inspection project"), //
	REPLICATE("Level 2", 30, "Names of the level 2 nodes in the inspection project"), //
	PROTEIN_GROUP_TYPE("protein/group type", 10, "protein evidence type"), //
	PROTEIN_ACC("ACC", 25, "Protein accession"), //
	PROTEIN_DESC("desc", 40, "Protein description"), //
	PROTEIN_SCORE("prot sc", 10, "Protein score"), //
	PROTEIN_COV("coverage (%)", 10, "Protein coverage %"), //
	PROTEIN_DIFF_SEQ("# peptides", 10, "Number of different peptides sequences assigned to the protein"), //
	PROTEIN_NUM_PEPS("# PSMs", 10, "Number of PSMs assigned to the protein"), //
	IS_UNIQUE("unique", 6, "Peptide unique"), //
	EXP_MZ("exp.m/z", 10, "Experimental m/z"), //
	CALC_MZ("calc.m/z", 10, "Theoretical m/z"), //
	ERROR_MZ("m/z error", 10, "Error m/z: difference between theoretical and experimental m/z"), //
	CHARGE("z", 6, "Peptide charge"), //
	MISS("miss", 6, "Number of missedcleavages"), //
	PEPTIDE_SCORE("pep sc1", 10, "Peptide score"), //
	PROTEIN_LOCAL_FDR("Prot. local-FDR (%)", 10, "Local FDR at the current position of the ranked list of proteins"), //
	PEPTIDE_LOCAL_FDR("Pep. local-FDR (%)", 10, "Local FDR at the current position of the ranked list of peptides"), //
	PSM_LOCAL_FDR("PSM. local-FDR (%)", 10, "Local FDR at the current position of the ranked list of PSMs"), //
	RETENTION_TIME_SG("rt", 10, "Retention time (in seconds)"), //
	SEQUENCE("sequence", 60, "Peptide sequence (without modifications)"), //
	MODIF_SEQUENCE("modif.seq.", 60, "Peptide sequence containing modifications"), //
	MODIFICATIONS("modifs", 60, "PTM modifications"), //
	// PROTEIN_OCCURRENCE("prot.occurr.", 6,
	// "Protein occurrence: number of times that the protein has been detected
	// in the dataset"), //
	PEPTIDE_OCCURRENCE("pep.occurr.", 6,
			"Peptide occurrence: number of times that the peptide (with the modifications) has been detected in the dataset"), //
	CHROMOSOME_NAME("chr", 10, "Chromosome name"), //
	ENSG_ID("ENSG", 15, "ENSEMBL gene identifier"), //
	GENE_NAME("gene name", 15, "Gene name"), //
	PROTEIN_EVIDENCE("pe", 6, "Protein evidence (from UniprotKB)"), //
	// RESEARCHER("researcher", 20, "Researcher name (internal data for the
	// Spanish HPP consortium)"), //
	// GENE_CLASSIFICATION("gene class", 15, "Gene classification (internal data
	// for the Spanish HPP consortium)")
	;

	private final String name;
	private final int defaultWidth;
	private final String description;

	ExportedColumns(String name, int defaultWidth, String description) {
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

	private static List<ExportedColumns> getColumns(boolean showPeptides, boolean includeGeneInfo,
			boolean isFDRApplied) {
		final List<ExportedColumns> ret = new ArrayList<ExportedColumns>();
		ret.add(NUMBER);

		ret.add(EXPERIMENT);
		ret.add(REPLICATE);

		if (includeGeneInfo) {
			ret.add(CHROMOSOME_NAME);
			// ret.add(RESEARCHER);
			ret.add(ENSG_ID);
			ret.add(GENE_NAME);
			// ret.add(GENE_CLASSIFICATION);
			ret.add(PROTEIN_EVIDENCE);
		}
		// ALWAYS REPORT PROTEINS ACC, even if just peptides are reported (no
		// make the FDR)
		ret.add(PROTEIN_ACC);
		ret.add(PROTEIN_DESC);

		// For just proteins
		if (!showPeptides) {
			ret.add(PROTEIN_GROUP_TYPE);
			// ret.add(PROTEIN_OCCURRENCE);
			ret.add(PROTEIN_SCORE);
			if (isFDRApplied)
				ret.add(PROTEIN_LOCAL_FDR);
			ret.add(PROTEIN_COV);
			ret.add(PROTEIN_DIFF_SEQ);
			ret.add(PROTEIN_NUM_PEPS);
		}
		// for just peptides
		if (showPeptides) {
			ret.add(SEQUENCE);
			ret.add(PEPTIDE_SCORE);
			if (isFDRApplied) {
				ret.add(PEPTIDE_LOCAL_FDR);
				ret.add(PSM_LOCAL_FDR);
			}
			ret.add(PEPTIDE_OCCURRENCE);
			ret.add(IS_UNIQUE);
			ret.add(EXP_MZ);
			ret.add(CALC_MZ);
			ret.add(ERROR_MZ);
			ret.add(RETENTION_TIME_SG);
			ret.add(CHARGE);
			ret.add(MISS);
			ret.add(MODIF_SEQUENCE);
			ret.add(MODIFICATIONS);
		}

		return ret;
	}

	private static List<ExportedColumns> getColumnsForTable(boolean showPeptides, boolean includeGeneInfo,
			boolean isFDRApplied) {
		final List<ExportedColumns> ret = new ArrayList<ExportedColumns>();
		ret.add(NUMBER);

		ret.add(EXPERIMENT);
		ret.add(REPLICATE);

		if (includeGeneInfo) {
			ret.add(CHROMOSOME_NAME);
			// ret.add(RESEARCHER);
			ret.add(ENSG_ID);
			ret.add(GENE_NAME);
			// ret.add(GENE_CLASSIFICATION);
			// ret.add(PROTEIN_EVIDENCE);
		}
		// ALWAYS REPORT PROTEINS ACC, even if just peptides are reported (no
		// make the FDR)

		ret.add(PROTEIN_ACC);
		ret.add(PROTEIN_DESC);

		if (!showPeptides) {
			ret.add(PROTEIN_GROUP_TYPE);
			ret.add(PROTEIN_EVIDENCE);
			ret.add(PROTEIN_SCORE);
			if (isFDRApplied)
				ret.add(PROTEIN_LOCAL_FDR);
			// ret.add(PROTEIN_OCCURRENCE);
			ret.add(PROTEIN_COV);
			ret.add(PROTEIN_DIFF_SEQ);
			ret.add(PROTEIN_NUM_PEPS);

		}
		if (showPeptides) {
			ret.add(SEQUENCE);
			ret.add(PEPTIDE_SCORE);
			if (isFDRApplied) {
				ret.add(PEPTIDE_LOCAL_FDR);
				ret.add(PSM_LOCAL_FDR);
			}
			ret.add(PEPTIDE_OCCURRENCE);
			ret.add(IS_UNIQUE);
			// ret.add(EXP_MZ);
			// ret.add(CALC_MZ);
			// ret.add(ERROR_MZ);
			ret.add(RETENTION_TIME_SG);
			ret.add(CHARGE);
			// ret.add(MISS);
			ret.add(MODIF_SEQUENCE);
			// ret.add(MODIFICATIONS);
		}

		return ret;
	}

	public static List<String> getColumnsString(boolean showPeptides, boolean includeGeneInfo, boolean isFDRApplied,
			Set<IdentificationSet> idSets, boolean translateForImportCompatibility) {
		final List<String> ret = new ArrayList<String>();
		final List<ExportedColumns> columns = getColumns(showPeptides, includeGeneInfo, isFDRApplied);

		for (final ExportedColumns exportedColumns : columns) {

			String columnName = exportedColumns.toString();
			// for compatibility of importing this file directly
			if (translateForImportCompatibility) {
				if (exportedColumns == ExportedColumns.CHARGE) {
					columnName = TableTextFileColumn.CHARGE.getHeaderName();
				} else if (exportedColumns == ExportedColumns.PROTEIN_ACC) {
					columnName = TableTextFileColumn.ACC.getHeaderName();
				} else if (exportedColumns == ExportedColumns.SEQUENCE) {
					columnName = TableTextFileColumn.SEQ.getHeaderName();
				} else if (exportedColumns == ExportedColumns.RETENTION_TIME_SG) {
					columnName = TableTextFileColumn.RT.getHeaderName();
				} else if (exportedColumns == ExportedColumns.EXP_MZ) {
					columnName = TableTextFileColumn.MZ.getHeaderName();
				} else if (exportedColumns == ExportedColumns.NUMBER) {
					columnName = TableTextFileColumn.PSMID.getHeaderName();
				}
			}
			ret.add(columnName);
		}
		// Substitute the column header of the scores by their names
		final List<String> proteinScoreNames = ExporterUtil.getProteinScoreNames(idSets);
		int proteinScoreIndex = -1;
		int j = 0;
		for (final String proteinScoreName : proteinScoreNames) {
			final int indexOf = ret.indexOf(ExportedColumns.PROTEIN_SCORE.toString());
			if (indexOf >= 0) {
				proteinScoreIndex = indexOf;
				j++;
				ret.set(indexOf, proteinScoreName);
			} else if (proteinScoreIndex + j >= 0) {
				ret.add(proteinScoreIndex + j, proteinScoreName);
			}
		}
		int peptideScoreIndex = -1;
		j = 0;
		final List<String> peptideScoreNames = ExporterUtil.getPeptideScoreNames(idSets);
		for (final String peptideScoreName : peptideScoreNames) {
			final int indexOf = ret.indexOf(ExportedColumns.PEPTIDE_SCORE.toString());
			if (indexOf >= 0) {
				peptideScoreIndex = indexOf;
				j++;
				ret.set(indexOf, peptideScoreName);
			} else if (peptideScoreIndex + j >= 0) {
				ret.add(peptideScoreIndex + j, peptideScoreName);
			}

		}
		return ret;
	}

	public static List<String> getColumnsStringForTable(boolean showPeptides, boolean includeGeneInfo,
			boolean isFDRApplied, Collection<IdentificationSet> idSets) {
		final List<String> ret = new ArrayList<String>();

		final List<ExportedColumns> columns = getColumnsForTable(showPeptides, includeGeneInfo, isFDRApplied);

		for (final ExportedColumns exportedColumns : columns) {
			ret.add(exportedColumns.toString());
		}
		// Substitute the column header of the scores by their names
		final List<String> proteinScoreNames = ExporterUtil.getProteinScoreNames(idSets);
		int proteinScoreIndex = -1;
		int j = 0;
		for (final String proteinScoreName : proteinScoreNames) {
			final int indexOf = ret.indexOf(ExportedColumns.PROTEIN_SCORE.toString());
			if (indexOf >= 0) {
				proteinScoreIndex = indexOf;
				j++;
				ret.set(indexOf, proteinScoreName);
			} else if (proteinScoreIndex > 0) {
				ret.add(proteinScoreIndex + j, proteinScoreName);
			}
		}
		int peptideScoreIndex = -1;
		j = 0;
		final List<String> peptideScoreNames = ExporterUtil.getPeptideScoreNames(idSets);
		for (final String peptideScoreName : peptideScoreNames) {
			final int indexOf = ret.indexOf(ExportedColumns.PEPTIDE_SCORE.toString());
			if (indexOf >= 0) {
				peptideScoreIndex = indexOf;
				j++;
				ret.set(indexOf, peptideScoreName);
			} else if (peptideScoreIndex > 0) {
				ret.add(peptideScoreIndex + j, peptideScoreName);
			}

		}

		return ret;
	}

}
