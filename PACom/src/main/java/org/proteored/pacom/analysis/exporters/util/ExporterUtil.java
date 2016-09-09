package org.proteored.pacom.analysis.exporters.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedPeptide;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedProtein;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.PeptideOccurrence;
import org.proteored.miapeapi.experiment.model.ProteinGroup;
import org.proteored.miapeapi.experiment.model.ProteinGroupOccurrence;
import org.proteored.miapeapi.experiment.model.ProteinMerger;
import org.proteored.miapeapi.experiment.model.grouping.ProteinEvidence;
import org.proteored.miapeapi.interfaces.msi.IdentifiedPeptide;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProtein;
import org.proteored.miapeapi.interfaces.msi.PeptideModification;
import org.proteored.pacom.analysis.genes.ENSGInfo;
import org.proteored.pacom.analysis.genes.GeneDistributionReader;
import org.proteored.pacom.analysis.genes.Researcher;

public class ExporterUtil {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private static ExporterUtil instance;
	private static boolean includeReplicateAndExperimentOrigin;
	private static boolean includePeptides;
	private static boolean includeGeneInfo;
	private static boolean excludeNonConclusiveProteins;
	private final HashMap<String, List<ENSGInfo>> proteinGeneMapping;
	private final IdentificationSet idSet;
	// Protein Score Order
	private List<String> proteinScoreNames;
	// Peptide Score Order
	private List<String> peptideScoreNames;
	private static boolean retrieveProteinSequences;
	private static boolean testOntologies;
	public static final String VALUE_SEPARATOR = ",";

	private ExporterUtil(IdentificationSet idSet, boolean includeReplicateAndExperimentOrigin, boolean includePeptides,
			boolean includeGeneInfo, boolean retrieveProteinSequences, boolean excludeNonConclusiveProteins) {
		ExporterUtil.includeReplicateAndExperimentOrigin = includeReplicateAndExperimentOrigin;
		ExporterUtil.includePeptides = includePeptides;
		ExporterUtil.includeGeneInfo = includeGeneInfo;
		ExporterUtil.excludeNonConclusiveProteins = excludeNonConclusiveProteins;
		this.idSet = idSet;

		proteinGeneMapping = GeneDistributionReader.getInstance().getProteinGeneMapping(null);

		ExporterUtil.retrieveProteinSequences = retrieveProteinSequences;
	}

	public static void setTestOntologies(boolean testOntologies) {
		ExporterUtil.testOntologies = testOntologies;
	}

	public static ExporterUtil getInstance(IdentificationSet idSet, boolean includeReplicateAndExperimentOrigin,
			boolean includePeptides, boolean includeGeneInfo, boolean retrieveProteinSequences,
			boolean excludeNonConclusiveProteins) {
		boolean createNewInstance = false;
		if (instance == null) {
			createNewInstance = true;
		}
		if ((ExporterUtil.includeReplicateAndExperimentOrigin && !includeReplicateAndExperimentOrigin)
				|| (!ExporterUtil.includeReplicateAndExperimentOrigin && includeReplicateAndExperimentOrigin))
			createNewInstance = true;
		if ((ExporterUtil.includePeptides && !includePeptides) || (!ExporterUtil.includePeptides && includePeptides))
			createNewInstance = true;
		if ((ExporterUtil.includeGeneInfo && !includeGeneInfo) || (!ExporterUtil.includeGeneInfo && includeGeneInfo))
			createNewInstance = true;
		if ((ExporterUtil.retrieveProteinSequences && !retrieveProteinSequences)
				|| (!ExporterUtil.retrieveProteinSequences && retrieveProteinSequences))
			createNewInstance = true;
		if ((ExporterUtil.excludeNonConclusiveProteins && !excludeNonConclusiveProteins)
				|| (!ExporterUtil.excludeNonConclusiveProteins && excludeNonConclusiveProteins))
			createNewInstance = true;

		if (createNewInstance)
			instance = new ExporterUtil(idSet, includeReplicateAndExperimentOrigin, includePeptides, includeGeneInfo,
					retrieveProteinSequences, excludeNonConclusiveProteins);

		instance.proteinScoreNames = idSet.getProteinScoreNames();
		instance.peptideScoreNames = idSet.getPeptideScoreNames();
		return instance;
	}

	public List<String> getPeptideInfoList(PeptideOccurrence peptideOccurrence, List<String> columns, int index) {

		List<String> ret = new ArrayList<String>();
		if (peptideOccurrence == null)
			throw new IllegalMiapeArgumentException("peptide occurrence should be different to null");

		StringBuilder sb = new StringBuilder();
		String scoreName = "";
		for (String column : columns) {
			try {
				if (column.equals(ExportedColumns.NUMBER.toString())) {
					ret.add(cleanString(String.valueOf(index)));
				} else if (column.equals(ExportedColumns.PROTEIN_EVIDENCE.toString())) {
					ret.add(getProteinEvidenceName(peptideOccurrence));
				} else if (column.equals(ExportedColumns.CHROMOSOME_NAME.toString())) {
					ret.add(getChromosomeName(peptideOccurrence));
				} else if (column.equals(ExportedColumns.GENE_NAME.toString())) {
					ret.add(getGeneName(peptideOccurrence));
				} else if (column.equals(ExportedColumns.ENSG_ID.toString())) {
					ret.add(getENSGID(peptideOccurrence));
				} else if (column.equals(ExportedColumns.RESEARCHER.toString())) {
					ret.add(getResearcher(peptideOccurrence));
				} else if (column.equals(ExportedColumns.GENE_CLASSIFICATION.toString())) {
					ret.add(getGeneClassification(peptideOccurrence));
				} else if (column.equals(ExportedColumns.CHARGE.toString())) {
					ret.add(getCharge(peptideOccurrence));
				} else if (column.equals(ExportedColumns.RETENTION_TIME_SG.toString())) {
					ret.add(getRt(peptideOccurrence));
				} else if (column.equals(ExportedColumns.IS_UNIQUE.toString())) {
					ArrayList<String> accs = new ArrayList<String>();
					for (ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
						for (IdentifiedProtein protein : pep.getIdentifiedProteins()) {
							if (!accs.contains(protein.getAccession()))
								accs.add(protein.getAccession());
						}
					}
					int numProteins = accs.size();
					ret.add(cleanString(numProteins == 1 ? "1" : "0"));
				} else if (column.equals(ExportedColumns.MISS.toString())) {
					ret.add(cleanString(
							String.valueOf(peptideOccurrence.getFirstOccurrence().getNumMissedcleavages())));
				} else if (column.equals(ExportedColumns.MODIF_SEQUENCE.toString())) {
					final String modifSeq = peptideOccurrence.getFirstOccurrence().getKey(true);
					// Just show if it is different from original sequence, that
					// is, if it has modifications
					if (!modifSeq.equals(peptideOccurrence.getFirstOccurrence().getSequence()))
						ret.add(cleanString(modifSeq));
					else
						ret.add(cleanString(""));
				} else if (column.equals(ExportedColumns.MODIFICATIONS.toString())) {

					ret.add(getPeptideModifications(peptideOccurrence));

				} else if (column.equals(ExportedColumns.EXP_MZ.toString())) {
					ret.add(cleanString(peptideOccurrence.getFirstOccurrence().getExperimentalMassToCharge()));
				} else if (column.equals(ExportedColumns.CALC_MZ.toString())) {
					ret.add(cleanString(peptideOccurrence.getFirstOccurrence().getCalculatedMassToCharge()));
				} else if (column.equals(ExportedColumns.ERROR_MZ.toString())) {
					String errorMZ = peptideOccurrence.getFirstOccurrence().getMassError();
					ret.add(cleanString(errorMZ));
				} else if (column.equals(ExportedColumns.PEPTIDE_OCCURRENCE.toString())) {
					ret.add(String.valueOf(peptideOccurrence.getItemList().size()));
				} else if (column.equals(ExportedColumns.PEPTIDE_LOCAL_FDR.toString())) {
					ret.add(parseFloat(peptideOccurrence.getPeptideLocalFDR()));
				} else if (column.equals(ExportedColumns.PSM_LOCAL_FDR.toString())) {
					ret.add(parseFloat(peptideOccurrence.getBestPeptide().getPSMLocalFDR()));
				} else if (column.equals(ExportedColumns.PROTEIN_OCCURRENCE.toString())) {

					ret.add(getProteinOccurrence(peptideOccurrence));
				} else if (column.equals(ExportedColumns.PROTEIN_GROUP_TYPE.toString())) {
					// ret.add(cleanString(getProteinGroupType(peptideOccurrence)));
				} else if (column.equals(ExportedColumns.PROTEIN_ACC.toString())) {

					ret.add(getProteinAcc(peptideOccurrence));

				} else if (column.equals(ExportedColumns.PROTEIN_COV.toString())) {

					ret.add(getProteinCoverage(peptideOccurrence));
				} else if (column.equals(ExportedColumns.PROTEIN_DESC.toString())) {

					ret.add(getProteinDescription(peptideOccurrence));
				} else if (column.equals(ExportedColumns.PROTEIN_DIFF_SEQ.toString())) {

					ret.add(getProteinDiffSeq(peptideOccurrence));
				} else if (column.equals(ExportedColumns.PROTEIN_NUM_PEPS.toString())) {

					ret.add(getNumPeptides(peptideOccurrence));
				} else if (column.equals(ExportedColumns.PROTEIN_SCORE.toString())) {
					// This line should not be used
					scoreName = proteinScoreNames.get(0);
					ret.add(getProteinScore(peptideOccurrence, scoreName));
				} else if (column.equals(ExportedColumns.PEPTIDE_SCORE.toString())) {
					// This line should not be used
					scoreName = peptideScoreNames.get(0);
					ret.add(getPeptideScore(peptideOccurrence, scoreName));
				} else if (column.equals(ExportedColumns.SEQUENCE.toString())) {
					ret.add(cleanString(peptideOccurrence.getFirstOccurrence().getSequence()));
				} else if (column.equals(ExportedColumns.EXPERIMENT.toString())) {

					ret.add(getExperimentName(peptideOccurrence));
				} else if (column.equals(ExportedColumns.REPLICATE.toString())) {

					ret.add(getReplicateNames(peptideOccurrence));
				} else {
					// if it is one of the peptide scores
					if (peptideScoreNames.contains(column)) {
						ret.add(parseFloat(peptideOccurrence.getBestPeptideScore(column)));
					} else {
						throw new IllegalMiapeArgumentException(
								"Column " + column + " is not supported by this exporter");
					}
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		return ret;

	}

	public List<String> getProteinInfoList(ProteinGroupOccurrence proteinGroupOccurrence, List<String> columns,
			Integer index) {

		List<String> ret = new ArrayList<String>();
		if (proteinGroupOccurrence == null)
			throw new IllegalMiapeArgumentException("protein occurrence should be different to null");

		for (String column : columns) {

			try {
				if (column.equals(ExportedColumns.NUMBER.toString())) {
					ret.add(cleanString(String.valueOf(index)));
				} else if (column.equals(ExportedColumns.PROTEIN_EVIDENCE.toString())) {
					ret.add(getProteinEvidenceName(proteinGroupOccurrence));
				} else if (column.equals(ExportedColumns.CHROMOSOME_NAME.toString())) {
					ret.add(getChromosomeName(proteinGroupOccurrence));
				} else if (column.equals(ExportedColumns.GENE_NAME.toString())) {
					ret.add(getGeneName(proteinGroupOccurrence));
				} else if (column.equals(ExportedColumns.ENSG_ID.toString())) {
					ret.add(getENSGID(proteinGroupOccurrence));

				} else if (column.equals(ExportedColumns.RESEARCHER.toString())) {

					ret.add(getResearcher(proteinGroupOccurrence));

				} else if (column.equals(ExportedColumns.GENE_CLASSIFICATION.toString())) {

					ret.add(getGeneClassification(proteinGroupOccurrence));
				} else if (column.equals(ExportedColumns.PROTEIN_GROUP_TYPE.toString())) {
					ret.add(cleanString(getProteinGroupType(proteinGroupOccurrence)));
				} else if (column.equals(ExportedColumns.PROTEIN_OCCURRENCE.toString())) {

					int proteinGroupOccurrenceNumber = idSet
							.getProteinGroupOccurrenceNumber(proteinGroupOccurrence.getFirstOccurrence());
					proteinGroupOccurrenceNumber = proteinGroupOccurrence.getItemList().size();
					ret.add(cleanString(String.valueOf(proteinGroupOccurrenceNumber)));

				} else if (column.equals(ExportedColumns.PROTEIN_ACC.toString())) {
					ret.add(cleanString(
							proteinGroupOccurrence.getAccessionsString().replace(",", ExporterUtil.VALUE_SEPARATOR)));
				} else if (column.equals(ExportedColumns.PROTEIN_COV.toString())) {

					ret.add(getProteinCoverage(proteinGroupOccurrence));

				} else if (column.equals(ExportedColumns.PROTEIN_DESC.toString())) {
					ret.add(cleanString(getProteinDescription(proteinGroupOccurrence)));
				} else if (column.equals(ExportedColumns.PROTEIN_DIFF_SEQ.toString())) {
					ret.add(getProteinDiffSeq(proteinGroupOccurrence));
				} else if (column.equals(ExportedColumns.PROTEIN_NUM_PEPS.toString())) {
					ret.add(getNumPeptides(proteinGroupOccurrence));
				} else if (column.equals(ExportedColumns.PROTEIN_SCORE.toString())) {
					if (!proteinScoreNames.isEmpty())
						ret.add(parseFloat(proteinGroupOccurrence.getBestProteinScore(column)));
					else
						ret.add(parseDouble(null));

				} else if (column.equals(ExportedColumns.PROTEIN_LOCAL_FDR.toString())) {
					if (proteinGroupOccurrence.getProteinLocalFDR() != null)
						ret.add(cleanString(String.valueOf(proteinGroupOccurrence.getProteinLocalFDR())));
					else
						ret.add(cleanString(""));
				} else if (column.equals(ExportedColumns.EXPERIMENT.toString())) {

					ret.add(getExperimentName(proteinGroupOccurrence));
				} else if (column.equals(ExportedColumns.REPLICATE.toString())) {

					ret.add(getReplicateNames(proteinGroupOccurrence));

				} else {
					if (proteinScoreNames.contains(column)) {
						ret.add(parseFloat(proteinGroupOccurrence.getBestProteinScore(column)));
					} else {
						throw new IllegalMiapeArgumentException(
								"Column " + column + " is not supported by this exporter");
					}
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		return ret;

	}

	private String getProteinGroupType(ProteinGroupOccurrence proteinGroupOccurrence) {
		final List<ProteinGroup> groupList = proteinGroupOccurrence.getItemList();
		List<ProteinEvidence> evidences = new ArrayList<ProteinEvidence>();
		String ret = "";
		for (ProteinGroup proteinGroup : groupList) {
			final ProteinEvidence evidence = proteinGroup.getEvidence();
			if (!evidences.contains(evidence) && !ProteinEvidence.CONCLUSIVE.equals(evidence)
					&& !ProteinEvidence.FILTERED.equals(evidence)) {
				evidences.add(evidence);
				if (!"".equals(ret))
					ret = ret + VALUE_SEPARATOR;
				ret = ret + evidence.toString();
			}
		}

		return ret;
	}

	public String getStringFromList(List<String> lineStringList, char separator) {
		StringBuilder sb = new StringBuilder();
		if (lineStringList != null)
			for (String obj : lineStringList) {
				if (obj != null)
					sb.append(obj);
				else
					sb.append("-");
				sb.append(separator);
			}
		return sb.toString();
	}

	private String getProteinOccurrence(PeptideOccurrence peptideOccurrence) {
		StringBuilder occurrenceString = new StringBuilder();
		List<String> proteinACCs = new ArrayList<String>();
		for (ExtendedIdentifiedPeptide extendedIdentifiedPeptide : peptideOccurrence.getItemList()) {
			List<IdentifiedProtein> peptideProteins = extendedIdentifiedPeptide.getIdentifiedProteins();
			if (peptideProteins != null) {
				for (IdentifiedProtein identifiedProtein : peptideProteins) {
					final String accession = identifiedProtein.getAccession();
					if (!proteinACCs.contains(accession)) {
						final ProteinGroupOccurrence proteinOccurrence2 = idSet.getProteinGroupOccurrence(accession);
						if (proteinOccurrence2 != null) {
							final List<ProteinGroup> proteinGroupList = proteinOccurrence2.getItemList();
							if (proteinGroupList != null) {
								proteinACCs.add(accession);
								if (!"".equals(occurrenceString.toString()))
									occurrenceString.append(VALUE_SEPARATOR);
								occurrenceString.append(proteinGroupList.size());
							}
						}
					}
				}
			}
		}

		return cleanString(occurrenceString.toString());
	}

	private String getProteinOccurrence(ProteinGroupOccurrence proteinOccurrence) {
		StringBuilder occurrenceString = new StringBuilder();

		occurrenceString.append(proteinOccurrence.getItemList().size());

		return cleanString(occurrenceString.toString());
	}

	private String getPeptideModifications(PeptideOccurrence peptideOccurrence) {
		StringBuilder modifString = new StringBuilder();

		final Set<PeptideModification> modifications = peptideOccurrence.getFirstOccurrence().getModifications();
		if (modifications != null)
			for (PeptideModification modification : modifications) {
				if (!"".equals(modifString.toString()))
					modifString.append(VALUE_SEPARATOR);
				modifString.append(modification.getName() + "(" + modification.getResidues() + "-"
						+ modification.getPosition() + ")");
			}

		return cleanString(modifString.toString());
	}

	private String getReplicateNames(PeptideOccurrence peptideOccurrence) {
		StringBuilder sbReplicatesNames = new StringBuilder();
		Set<String> replicateNames = new HashSet<String>();

		List<ExtendedIdentifiedPeptide> peptides = peptideOccurrence.getItemList();
		if (peptides != null) {
			for (ExtendedIdentifiedPeptide extendedIdentifiedPeptide : peptides) {
				String replicateName = extendedIdentifiedPeptide.getReplicateName();
				// change on 3 February 2016
				// final String experimentName =
				// extendedIdentifiedPeptide.getExperimentName();
				// if (replicateName.contains(experimentName))
				// replicateName = replicateName.replace(experimentName, "");
				if (replicateName.endsWith("/"))
					replicateName = replicateName.replace("/", "");
				if (!replicateNames.contains(replicateName))
					replicateNames.add(replicateName);
			}
		}

		for (String replicateName : replicateNames) {
			if (!"".equals(sbReplicatesNames.toString()))
				sbReplicatesNames.append(VALUE_SEPARATOR);
			sbReplicatesNames.append(replicateName);
		}

		return cleanString(sbReplicatesNames.toString());
	}

	private String getReplicateNames(ProteinGroupOccurrence proteinOccurrence) {
		StringBuilder sbReplicatesNames = new StringBuilder();
		Set<String> replicateNames = new HashSet<String>();

		List<ProteinGroup> proteinGroups = proteinOccurrence.getItemList();
		if (proteinGroups != null) {
			for (ProteinGroup proteinGroup : proteinGroups) {
				for (ExtendedIdentifiedProtein extendedIdentifiedProtein : proteinGroup) {
					String replicateName = extendedIdentifiedProtein.getReplicateName();

					// change on 3 February 2016
					// final String experimentName =
					// extendedIdentifiedProtein.getExperimentName();
					// if (replicateName.contains(experimentName))
					// replicateName = replicateName.replace(experimentName,
					// "");
					if (replicateName.endsWith("/"))
						replicateName = replicateName.replace("/", "");
					if (!replicateNames.contains(replicateName))
						replicateNames.add(replicateName);
				}
			}
		}
		for (String replicateName : replicateNames) {
			if (!"".equals(sbReplicatesNames.toString()))
				sbReplicatesNames.append(VALUE_SEPARATOR);
			sbReplicatesNames.append(replicateName);
		}

		return cleanString(sbReplicatesNames.toString());
	}

	private String getExperimentName(PeptideOccurrence peptideOccurrence) {
		StringBuilder sbExperimentNames = new StringBuilder();
		Set<String> experimentNames = new HashSet<String>();

		List<ExtendedIdentifiedPeptide> peptides = peptideOccurrence.getItemList();
		for (ExtendedIdentifiedPeptide extendedIdentifiedPeptide : peptides) {
			final String experimentName = extendedIdentifiedPeptide.getExperimentName();
			if (!experimentNames.contains(experimentName))
				experimentNames.add(experimentName);
		}

		for (String experimentName : experimentNames) {
			if (!"".equals(sbExperimentNames.toString()))
				sbExperimentNames.append(VALUE_SEPARATOR);
			sbExperimentNames.append(experimentName);
		}

		return cleanString(sbExperimentNames.toString());
	}

	private String getExperimentName(ProteinGroupOccurrence proteinOccurrence) {
		StringBuilder sbExperimentNames = new StringBuilder();
		Set<String> experimentNames = new HashSet<String>();

		final List<ExtendedIdentifiedProtein> proteins = proteinOccurrence.getProteins();
		for (ExtendedIdentifiedProtein extendedIdentifiedProtein : proteins) {
			final String experimentName = extendedIdentifiedProtein.getExperimentName();
			if (!experimentNames.contains(experimentName))
				experimentNames.add(experimentName);
		}
		for (String experimentName : experimentNames) {
			if (!"".equals(sbExperimentNames.toString()))
				sbExperimentNames.append(VALUE_SEPARATOR);
			sbExperimentNames.append(experimentName);
		}

		return cleanString(sbExperimentNames.toString());
	}

	private String getPeptideScore(PeptideOccurrence peptideOccurrence, String scoreName) {

		Float score = peptideOccurrence.getBestPeptideScore();

		return parseFloat(score);

	}

	private String getProteinScore(PeptideOccurrence peptideOccurrence, String scoreName) {

		try {

			StringBuilder bestScores = new StringBuilder();
			ArrayList<String> proteinACCs = new ArrayList<String>();
			for (ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
				List<IdentifiedProtein> peptideProteins = pep.getIdentifiedProteins();
				for (IdentifiedProtein identifiedProtein : peptideProteins) {
					String acc = identifiedProtein.getAccession();
					if (!proteinACCs.contains(acc)) {
						proteinACCs.add(acc);
						ProteinGroupOccurrence proteinocc = idSet.getProteinGroupOccurrence(acc);
						if (proteinocc != null) {
							if (!"".equals(bestScores.toString()))
								bestScores.append(VALUE_SEPARATOR);
							bestScores.append(proteinocc.getBestProteinScore());
						}
					}
				}
			}

			return cleanString(bestScores.toString());
		} catch (IllegalMiapeArgumentException e) {
			return null;
		}
	}

	private String getProteinScore(ProteinGroupOccurrence occurrence, String scoreName) {

		try {

			StringBuilder bestScores = new StringBuilder();

			final Float bestScore = occurrence.getBestProteinScore();
			if (bestScore != null)
				bestScores.append(bestScore);

			return cleanString(bestScores.toString());
		} catch (IllegalMiapeArgumentException e) {
			return null;
		}
	}

	private String getNumPeptides(PeptideOccurrence peptideOccurrence) {
		StringBuilder proteinsNumPeps = new StringBuilder();

		HashMap<String, List<IdentifiedPeptide>> hashProteins = new HashMap<String, List<IdentifiedPeptide>>();

		for (ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
			List<IdentifiedProtein> peptideProteins = pep.getIdentifiedProteins();
			for (IdentifiedProtein protein : peptideProteins) {
				String acc = protein.getAccession();
				final List<IdentifiedPeptide> peps = protein.getIdentifiedPeptides();
				if (peps != null)
					if (hashProteins.containsKey(acc)) {
						hashProteins.get(acc).addAll(peps);
					} else {
						hashProteins.put(acc, peps);
					}
			}
		}
		for (List<IdentifiedPeptide> peptides : hashProteins.values()) {
			if (!"".equals(proteinsNumPeps.toString()))
				proteinsNumPeps.append(VALUE_SEPARATOR);
			proteinsNumPeps.append(peptides.size());
		}

		return cleanString(proteinsNumPeps.toString());
	}

	private String getNumPeptides(ProteinGroupOccurrence proteinOccurrence) {
		StringBuilder proteinsNumPeps = new StringBuilder();

		HashMap<String, List<IdentifiedPeptide>> hashProteins = new HashMap<String, List<IdentifiedPeptide>>();

		int numPeptides = 0;

		for (ProteinGroup proteinGroup : proteinOccurrence.getItemList()) {
			final List<ExtendedIdentifiedPeptide> peptides = proteinGroup.getPeptides();
			if (peptides != null)
				numPeptides = numPeptides + peptides.size();

		}
		proteinsNumPeps.append(numPeptides);

		return cleanString(proteinsNumPeps.toString());
	}

	private String getProteinDiffSeq(PeptideOccurrence peptideOccurrence) {
		StringBuilder proteinsDiffSeqs = new StringBuilder();

		HashMap<String, List<IdentifiedPeptide>> hashProteins = new HashMap<String, List<IdentifiedPeptide>>();

		for (ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
			List<IdentifiedProtein> peptideProteins = pep.getIdentifiedProteins();
			for (IdentifiedProtein protein : peptideProteins) {
				String acc = protein.getAccession();
				final List<IdentifiedPeptide> peps = protein.getIdentifiedPeptides();
				if (peps != null)
					if (hashProteins.containsKey(acc)) {
						hashProteins.get(acc).addAll(peps);
					} else {
						hashProteins.put(acc, peps);
					}
			}
		}
		for (List<IdentifiedPeptide> peptides : hashProteins.values()) {
			if (peptides != null) {
				int numDifPeptides = getNumDifPeptides(peptides);
				if (!"".equals(proteinsDiffSeqs.toString()))
					proteinsDiffSeqs.append(VALUE_SEPARATOR);
				proteinsDiffSeqs.append(numDifPeptides);
			}
		}

		return cleanString(proteinsDiffSeqs.toString());
	}

	private String getProteinDiffSeq(ProteinGroupOccurrence proteinOccurrence) {
		StringBuilder proteinsDiffSeqs = new StringBuilder();

		proteinsDiffSeqs.append(getNumDifExtendedPeptides(proteinOccurrence.getPeptides()));

		return cleanString(proteinsDiffSeqs.toString());
	}

	private String getProteinDescription(PeptideOccurrence peptideOccurrence) {
		StringBuilder proteinsDescriptions = new StringBuilder();

		List<String> descriptions = new ArrayList<String>();

		for (ExtendedIdentifiedProtein protein : peptideOccurrence.getProteinList()) {

			if (!descriptions.contains(protein.getDescription()))
				descriptions.add(protein.getDescription());

		}
		for (String description : descriptions) {
			if (!"".equals(proteinsDescriptions.toString()))
				proteinsDescriptions.append(VALUE_SEPARATOR);
			proteinsDescriptions.append(description);
		}

		return cleanString(proteinsDescriptions.toString());
	}

	private String getProteinDescription(ProteinGroupOccurrence proteinOccurrence) {
		StringBuilder proteinsDescriptions = new StringBuilder();

		List<String> descriptions = new ArrayList<String>();

		for (ExtendedIdentifiedProtein protein : proteinOccurrence.getProteins()) {
			if (!descriptions.contains(protein.getDescription())) {
				descriptions.add(protein.getDescription());
			}
		}
		for (String description : descriptions) {
			if (!"".equals(proteinsDescriptions.toString()))
				proteinsDescriptions.append(VALUE_SEPARATOR);
			proteinsDescriptions.append(description);
		}
		return cleanString(proteinsDescriptions.toString());
	}

	private String getProteinCoverage(PeptideOccurrence peptideOccurrence) {
		StringBuilder proteinsCovs = new StringBuilder();

		final ExtendedIdentifiedPeptide peptide = peptideOccurrence.getFirstOccurrence();
		List<IdentifiedProtein> peptideProteins = peptide.getIdentifiedProteins();
		for (IdentifiedProtein identifiedProtein : peptideProteins) {
			String coverage = ProteinMerger.getCoverage(
					idSet.getProteinGroupOccurrence(identifiedProtein.getAccession()), null, retrieveProteinSequences);
			if (coverage != null) {
				Double cov = Double.valueOf(coverage);
				cov = cov * 100.0;
				DecimalFormat df = new DecimalFormat("#.##");
				coverage = df.format(cov);
				if (!"".equals(proteinsCovs.toString()))
					proteinsCovs.append(VALUE_SEPARATOR);
				proteinsCovs.append(coverage);
			}
		}

		return cleanString(proteinsCovs.toString());
	}

	private String getProteinCoverage(ProteinGroupOccurrence occurrence) {
		StringBuilder proteinsCovs = new StringBuilder();

		if (occurrence.isDecoy())
			return cleanString("");
		try {
			String coverage = ProteinMerger.getCoverage(occurrence, null, retrieveProteinSequences);

			if (coverage != null) {
				Double cov = Double.valueOf(coverage);
				cov = cov * 100.0;
				DecimalFormat df = new DecimalFormat("#.##");
				coverage = df.format(cov);
				if (!"".equals(proteinsCovs.toString()))
					proteinsCovs.append(VALUE_SEPARATOR);
				proteinsCovs.append(coverage);
			}
		} catch (Exception e) {

		}
		return cleanString(proteinsCovs.toString());
	}

	private String getProteinAcc(PeptideOccurrence peptideOccurrence) {
		StringBuilder proteinsAccs = new StringBuilder();

		ArrayList<String> accs = new ArrayList<String>();

		for (ExtendedIdentifiedProtein protein : peptideOccurrence.getProteinList()) {
			if (!accs.contains(protein.getAccession()))
				accs.add(protein.getAccession());
		}

		for (String acc : accs) {
			if (!"".equals(proteinsAccs.toString()))
				proteinsAccs.append(VALUE_SEPARATOR);
			proteinsAccs.append(acc);
		}

		return cleanString(proteinsAccs.toString());
	}

	private String getGeneClassification(PeptideOccurrence peptideOccurrence) {

		StringBuilder gene_classifications = new StringBuilder();

		ArrayList<String> accs = new ArrayList<String>();
		// Set<String> alreadyPresentGeneClassifications = new
		// HashSet<String>();
		for (ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
			for (IdentifiedProtein protein : pep.getIdentifiedProteins()) {
				if (!accs.contains(protein.getAccession()))
					accs.add(protein.getAccession());
			}
		}
		HashSet<String> dicc = new HashSet<String>();
		for (String acc : accs) {
			boolean added = false;
			if (proteinGeneMapping.containsKey(acc)) {
				List<ENSGInfo> genes = proteinGeneMapping.get(acc);

				for (ENSGInfo gene : genes) {
					String known = gene.getKnown();
					if (known != null && !dicc.contains(known)) {
						dicc.add(known);
						// if
						// (!alreadyPresentGeneClassifications.contains(known))
						// {
						if (!"".equals(gene_classifications.toString()))
							gene_classifications.append(VALUE_SEPARATOR);
						// alreadyPresentGeneClassifications.add(known);
						gene_classifications.append(known);
						// }

					}
				}

			}
			if (!added) {
				if (!"".equals(gene_classifications.toString()))
					gene_classifications.append(VALUE_SEPARATOR);
				gene_classifications.append("-");
			}
		}
		if (dicc.size() == 1)
			return dicc.iterator().next();
		if (dicc.isEmpty())
			return "-";
		return cleanString(gene_classifications.toString());
	}

	private String getGeneClassification(ProteinGroupOccurrence proteinOccurrence) {

		StringBuilder gene_classifications = new StringBuilder();

		List<String> accs = proteinOccurrence.getAccessions();
		Set<String> alreadyPresentGeneClassifications = new HashSet<String>();
		for (String acc : accs) {
			if (proteinGeneMapping.containsKey(acc)) {
				List<ENSGInfo> genes = proteinGeneMapping.get(acc);
				HashSet<String> dicc = new HashSet<String>();
				for (ENSGInfo gene : genes) {
					String known = gene.getKnown();
					if (known != null && !dicc.contains(known)) {
						dicc.add(known);
						if (!alreadyPresentGeneClassifications.contains(known)) {
							if (!"".equals(gene_classifications.toString()))
								gene_classifications.append(VALUE_SEPARATOR);
							alreadyPresentGeneClassifications.add(known);
							gene_classifications.append(known);
						}
					}
				}
			}
		}
		return cleanString(gene_classifications.toString());
	}

	private String getResearcher(PeptideOccurrence peptideOccurrence) {
		HashSet<String> dicc = new HashSet<String>();
		StringBuilder researchers = new StringBuilder();

		ArrayList<String> accs = new ArrayList<String>();

		for (ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
			for (IdentifiedProtein protein : pep.getIdentifiedProteins()) {
				if (!accs.contains(protein.getAccession()))
					accs.add(protein.getAccession());
			}
		}
		for (String acc : accs) {
			if (proteinGeneMapping.containsKey(acc)) {
				List<ENSGInfo> genes = proteinGeneMapping.get(acc);
				for (ENSGInfo gene : genes) {
					Researcher researcher2 = gene.getResearcher();
					if (researcher2 != null) {
						String researcher = researcher2.getName();
						if (!dicc.contains(researcher)) {
							dicc.add(researcher);
							if (!"".equals(researchers.toString()))
								researchers.append(VALUE_SEPARATOR);
							researchers.append(researcher);
						}
					}
				}

			}
		}

		return cleanString(researchers.toString());
	}

	private String getResearcher(ProteinGroupOccurrence proteinOccurrence) {
		HashSet<String> dicc = new HashSet<String>();
		StringBuilder researchers = new StringBuilder();

		List<String> accessions = proteinOccurrence.getAccessions();
		for (String acc : accessions) {

			if (proteinGeneMapping.containsKey(acc)) {
				List<ENSGInfo> genes = proteinGeneMapping.get(acc);
				for (ENSGInfo gene : genes) {
					Researcher researcher2 = gene.getResearcher();
					if (researcher2 != null) {
						String researcher = researcher2.getName();
						if (!dicc.contains(researcher)) {
							dicc.add(researcher);
							if (!"".equals(researchers.toString()))
								researchers.append(VALUE_SEPARATOR);
							researchers.append(researcher);
						}
					}
				}
			}
		}

		return cleanString(researchers.toString());
	}

	private String getENSGID(PeptideOccurrence peptideOccurrence) {
		HashSet<String> dicc = new HashSet<String>();
		StringBuilder ENSG_IDS = new StringBuilder();

		ArrayList<String> accs = new ArrayList<String>();
		for (ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
			for (IdentifiedProtein protein : pep.getIdentifiedProteins()) {
				if (!accs.contains(protein.getAccession()))
					accs.add(protein.getAccession());
			}
		}
		for (String acc : accs) {
			boolean added = false;
			if (proteinGeneMapping.containsKey(acc)) {
				List<ENSGInfo> genes = proteinGeneMapping.get(acc);
				for (ENSGInfo gene : genes) {
					String ensg_id = gene.getEnsG_ID();
					if (ensg_id != null && !"null".equals(ensg_id))
						if (!dicc.contains(ensg_id)) {
							added = true;
							dicc.add(ensg_id);
							if (!"".equals(ENSG_IDS.toString()))
								ENSG_IDS.append(VALUE_SEPARATOR);
							ENSG_IDS.append(ensg_id);
						}
				}
			}
			if (!added) {
				if (!"".equals(ENSG_IDS.toString()))
					ENSG_IDS.append(VALUE_SEPARATOR);
				ENSG_IDS.append("-");
			}
		}
		if (dicc.isEmpty())
			return "-";
		return cleanString(ENSG_IDS.toString());
	}

	private String getENSGID(ProteinGroupOccurrence proteinOccurrence) {
		HashSet<String> dicc = new HashSet<String>();
		StringBuilder ENSG_IDS = new StringBuilder();

		List<String> accs = proteinOccurrence.getAccessions();
		for (String acc : accs) {
			boolean added = false;
			if (proteinGeneMapping.containsKey(acc)) {
				List<ENSGInfo> genes = proteinGeneMapping.get(acc);
				for (ENSGInfo gene : genes) {
					String ensg_id = gene.getEnsG_ID();
					if (ensg_id != null && !"null".equals(ensg_id))
						if (!dicc.contains(ensg_id)) {
							dicc.add(ensg_id);
							if (!"".equals(ENSG_IDS.toString()))
								ENSG_IDS.append(VALUE_SEPARATOR);
							added = true;
							ENSG_IDS.append(ensg_id);
						}
				}
			}
			if (!added) {
				if (!"".equals(ENSG_IDS.toString()))
					ENSG_IDS.append(VALUE_SEPARATOR);
				ENSG_IDS.append("-");
			}
		}
		if (dicc.isEmpty())
			return "-";
		return cleanString(ENSG_IDS.toString());
	}

	private String getGeneName(PeptideOccurrence peptideOccurrence) {
		Set<String> dicc = new HashSet<String>();
		StringBuilder geneNames = new StringBuilder();

		List<String> accs = new ArrayList<String>();
		for (ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
			for (IdentifiedProtein protein : pep.getIdentifiedProteins()) {
				if (!accs.contains(protein.getAccession()))
					accs.add(protein.getAccession());
			}
		}
		for (String acc : accs) {
			boolean added = false;
			if (proteinGeneMapping.containsKey(acc)) {
				List<ENSGInfo> genes = proteinGeneMapping.get(acc);
				for (ENSGInfo gene : genes) {
					String geneName = gene.getGeneName();
					if (geneName != null)
						if (!dicc.contains(geneName)) {
							dicc.add(geneName);
							added = true;
							if (!"".equals(geneNames.toString()))
								geneNames.append(VALUE_SEPARATOR);
							geneNames.append(geneName);
						}
				}

			}
			if (!added) {
				if (!"".equals(geneNames.toString()))
					geneNames.append(VALUE_SEPARATOR);
				geneNames.append("-");
			}
		}
		if (dicc.isEmpty())
			return "-";
		return cleanString(geneNames.toString());
	}

	private String getGeneName(ProteinGroupOccurrence proteinOccurrence) {
		Set<String> dicc = new HashSet<String>();
		StringBuilder geneNames = new StringBuilder();
		// take all the gene names and then, if there is more than one, report
		// all, other wise, report just one
		List<String> accs = proteinOccurrence.getAccessions();
		for (String acc : accs) {
			boolean added = false;
			if (proteinGeneMapping.containsKey(acc)) {
				List<ENSGInfo> genes = proteinGeneMapping.get(acc);
				if (!"".equals(geneNames.toString()))
					geneNames.append(VALUE_SEPARATOR);
				if (genes.size() > 1)
					geneNames.append("[");
				for (ENSGInfo gene : genes) {
					String geneName = gene.getGeneName();
					if (geneName != null) {
						added = true;
						dicc.add(geneName);
						if (!"".equals(geneNames.toString()))
							geneNames.append(VALUE_SEPARATOR);
						geneNames.append(geneName);
					}
				}
				if (genes.size() > 1)
					geneNames.append("]");
			}
			if (!added) {
				if (!"".equals(geneNames.toString()))
					geneNames.append(VALUE_SEPARATOR);
				geneNames.append("-");
			}
		}
		if (dicc.size() == 1)
			return cleanString(dicc.iterator().next());
		if (dicc.isEmpty())
			return "-";
		return cleanString(geneNames.toString());
	}

	private String getChromosomeName(PeptideOccurrence peptideOccurrence) {
		Set<String> dicc = new HashSet<String>();
		StringBuilder chrNames = new StringBuilder();

		List<String> accs = new ArrayList<String>();
		for (ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
			for (IdentifiedProtein protein : pep.getIdentifiedProteins()) {
				if (!accs.contains(protein.getAccession()))
					accs.add(protein.getAccession());
			}
		}
		for (String acc : accs) {
			if (proteinGeneMapping.containsKey(acc)) {
				List<ENSGInfo> genes = proteinGeneMapping.get(acc);
				for (ENSGInfo gene : genes) {
					String chrName = gene.getChrName();
					if (chrName != null)
						if (!dicc.contains(chrName)) {
							dicc.add(chrName);
							if (!"".equals(chrNames.toString()))
								chrNames.append(VALUE_SEPARATOR);
							chrNames.append(chrName);
						}
				}

			}
		}

		return cleanString(chrNames.toString());
	}

	private String getChromosomeName(ProteinGroupOccurrence proteinOccurrence) {
		Set<String> dicc = new HashSet<String>();
		StringBuilder chrNames = new StringBuilder();

		List<String> accs = proteinOccurrence.getAccessions();
		for (String acc : accs) {
			boolean added = false;
			if (proteinGeneMapping.containsKey(acc)) {
				List<ENSGInfo> genes = proteinGeneMapping.get(acc);
				for (ENSGInfo gene : genes) {
					String chrName = gene.getChrName();
					if (chrName != null) {
						if (!dicc.contains(chrName)) {
							added = true;
							dicc.add(chrName);
							if (!"".equals(chrNames.toString()))
								chrNames.append(VALUE_SEPARATOR);
							chrNames.append(chrName);
						}
					}
				}
			}
			if (!added) {
				if (!"".equals(chrNames.toString()))
					chrNames.append(VALUE_SEPARATOR);
				chrNames.append("-");
			}
		}
		if (dicc.isEmpty())
			return "-";
		return cleanString(chrNames.toString());
	}

	private String getProteinEvidenceName(PeptideOccurrence peptideOccurrence) {
		Set<String> dicc = new HashSet<String>();
		StringBuilder evidences = new StringBuilder();

		List<String> accs = new ArrayList<String>();
		for (ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
			for (IdentifiedProtein protein : pep.getIdentifiedProteins()) {
				if (!accs.contains(protein.getAccession()))
					accs.add(protein.getAccession());
			}
		}
		for (String acc : accs) {
			if (proteinGeneMapping.containsKey(acc)) {
				List<ENSGInfo> genes = proteinGeneMapping.get(acc);
				for (ENSGInfo gene : genes) {
					String evidence = gene.getProteinEvidence();
					if (evidence != null)
						if (!dicc.contains(evidence)) {
							dicc.add(evidence);
							if (!"".equals(evidences.toString()))
								evidences.append(VALUE_SEPARATOR);
							evidences.append(evidence);
						}
				}

			}
		}

		return cleanString(evidences.toString());
	}

	private String getProteinEvidenceName(ProteinGroupOccurrence proteinOccurrence) {
		Set<String> dicc = new HashSet<String>();
		StringBuilder evidences = new StringBuilder();

		List<String> accs = proteinOccurrence.getAccessions();
		for (String acc : accs) {

			if (proteinGeneMapping.containsKey(acc)) {
				List<ENSGInfo> genes = proteinGeneMapping.get(acc);
				for (ENSGInfo gene : genes) {
					String evidence = gene.getProteinEvidence();
					if (evidence != null)
						if (!dicc.contains(evidence)) {
							dicc.add(evidence);
							if (!"".equals(evidences.toString()))
								evidences.append(VALUE_SEPARATOR);
							evidences.append(evidence);
						}
				}
			}
		}

		return cleanString(evidences.toString());
	}

	private int getNumDifExtendedPeptides(List<ExtendedIdentifiedPeptide> list) {
		if (list != null) {

			HashSet<String> seqs = new HashSet<String>();
			for (IdentifiedPeptide pep : list) {
				String seq = pep.getSequence();
				if (!seqs.contains(seq))
					seqs.add(seq);
			}
			return seqs.size();
		}
		return 0;
	}

	private int getNumDifPeptides(List<IdentifiedPeptide> list) {
		if (list != null) {

			HashSet<String> seqs = new HashSet<String>();
			for (IdentifiedPeptide pep : list) {
				String seq = pep.getSequence();
				if (!seqs.contains(seq))
					seqs.add(seq);
			}
			return seqs.size();
		}
		return 0;
	}

	private String getCharge(PeptideOccurrence peptideOccurrence) {
		Set<String> charges = new HashSet<String>();
		for (ExtendedIdentifiedPeptide extendedPeptide : peptideOccurrence.getItemList()) {
			final String charge = extendedPeptide.getCharge();
			if (charge != null && !charges.contains(charge)) {
				charges.add(charge);
			}
		}
		return cleanString(charges);
	}

	private String getRt(PeptideOccurrence peptideOccurrence) {
		Set<String> rts = new HashSet<String>();
		for (ExtendedIdentifiedPeptide extendedPeptide : peptideOccurrence.getItemList()) {
			final String rt = extendedPeptide.getRetentionTimeInSeconds();
			if (rt != null && !rts.contains(rt)) {
				rts.add(rt);
			}
		}
		return cleanString(rts);
	}

	private String cleanString(String string) {
		if (string == null)
			return "-";
		if ("null".equals(string))
			return "-";
		if ("".equals(string))
			return "-";

		// if doesn't have a VALUE_SEPARATOR, try to convert to Integer or to a
		// Double:

		if (!string.contains(VALUE_SEPARATOR)) {

			try {
				Integer i = Integer.valueOf(string);
				return String.valueOf(i);
			} catch (NumberFormatException e1) {
				try {
					Double d = Double.valueOf(string);
					return parseDouble(d);
				} catch (NumberFormatException e) {
					return string.trim();
				}
			}
		}

		return string.trim();

	}

	private String cleanString(Collection<String> objs) {
		StringBuilder sb = new StringBuilder("");
		if (objs == null || objs.isEmpty())
			return "-";
		for (String obj : objs) {
			if (!"".equals(sb.toString()))
				sb.append(VALUE_SEPARATOR);
			sb.append(obj);
		}

		return sb.toString().trim();
	}

	private String parseDouble(Double score) {
		if (score == null)
			return "-";
		DecimalFormat df = null;

		// String test = DecimalFormat.getNumberInstance().format(score);
		// return test;
		String format = "";
		if (score == 0.0)
			return "0";
		if (score > 0.01) {
			NumberFormat formater = DecimalFormat.getInstance();
			formater.setMaximumFractionDigits(3);
			formater.setGroupingUsed(false);
			format = formater.format(score);
			// df = new DecimalFormat("0.###");
		} else {
			// NumberFormat formater = DecimalFormat.getInstance();
			// formater.setMaximumFractionDigits(30);
			// formater.setMinimumFractionDigits(3);
			// formater.setGroupingUsed(false);
			// format = formater.format(score);
			format = new DecimalFormat("0.00E00").format(score);
		}
		// final String format = df.format(score);
		// log.info("Parsed from " + score + " to " + format);
		if ("".equals(format)) {
			return "-";
		}
		try {
			Number test = NumberFormat.getInstance().parse(format);

		} catch (NumberFormatException e) {
			log.info("CUIDADO");
		} catch (ParseException e) {
			log.info("CUIDADO");
		}
		return format;
	}

	private String parseFloat(Float score) {
		if (score == null)
			return "-";
		DecimalFormat df = null;

		// String test = DecimalFormat.getNumberInstance().format(score);
		// return test;
		String format = "";
		if (score == 0.0)
			return "0";
		if (score > 0.01) {
			NumberFormat formater = DecimalFormat.getInstance();
			formater.setMaximumFractionDigits(3);
			formater.setGroupingUsed(false);
			format = formater.format(score);
			// df = new DecimalFormat("0.###");
		} else {
			// NumberFormat formater = DecimalFormat.getInstance();
			// formater.setMaximumFractionDigits(30);
			// formater.setMinimumFractionDigits(3);
			// formater.setGroupingUsed(false);
			// format = formater.format(score);
			format = new DecimalFormat("0.00E00").format(score);
		}
		// final String format = df.format(score);
		// log.info("Parsed from " + score + " to " + format);
		if ("".equals(format)) {
			return "-";
		}
		try {
			Number test = NumberFormat.getInstance().parse(format);

		} catch (NumberFormatException e) {
			log.info("CUIDADO");
		} catch (ParseException e) {
			log.info("CUIDADO");
		}
		return format;
	}

	public static boolean isNonConclusiveProtein(ProteinGroup proteinGroup) {
		if (proteinGroup != null) {
			for (ExtendedIdentifiedProtein protein : proteinGroup) {
				if (protein.getEvidence() == ProteinEvidence.NONCONCLUSIVE)
					return true;
			}
		}
		return false;
	}

	public static boolean isNonConclusiveProtein(ProteinGroupOccurrence proteinGroupOccurrence) {
		if (proteinGroupOccurrence.getEvidence().equals(ProteinEvidence.NONCONCLUSIVE))
			return true;
		if (proteinGroupOccurrence != null) {
			List<ExtendedIdentifiedProtein> proteins = proteinGroupOccurrence.getProteins();
			if (proteins != null) {
				for (ExtendedIdentifiedProtein protein : proteins) {
					if (protein.getEvidence().equals(ProteinEvidence.NONCONCLUSIVE))
						return true;
				}
			}
		}
		return false;

	}

	public static boolean isNonConclusivePeptide(ExtendedIdentifiedPeptide peptide) {
		if (peptide != null) {
			List<ExtendedIdentifiedProtein> proteins = peptide.getProteins();
			if (proteins != null)
				for (ExtendedIdentifiedProtein protein : proteins) {
					if (protein.getEvidence() == ProteinEvidence.NONCONCLUSIVE)
						return true;
				}
		}
		return false;
	}

	public static boolean isNonConclusivePeptide(PeptideOccurrence peptideOccurrence) {
		if (peptideOccurrence != null) {
			for (ExtendedIdentifiedPeptide peptide : peptideOccurrence.getItemList()) {
				List<ExtendedIdentifiedProtein> proteins = peptide.getProteins();
				if (proteins != null)
					for (ExtendedIdentifiedProtein protein : proteins) {
						if (protein.getEvidence() == ProteinEvidence.NONCONCLUSIVE)
							return true;
					}
			}
		}
		return false;
	}
}
