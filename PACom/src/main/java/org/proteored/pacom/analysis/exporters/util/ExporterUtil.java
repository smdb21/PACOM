package org.proteored.pacom.analysis.exporters.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedPeptide;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedProtein;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.PeptideOccurrence;
import org.proteored.miapeapi.experiment.model.ProteinGroup;
import org.proteored.miapeapi.experiment.model.ProteinGroupOccurrence;
import org.proteored.miapeapi.experiment.model.ProteinMerger;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.experiment.model.grouping.ProteinEvidence;
import org.proteored.miapeapi.interfaces.msi.IdentifiedPeptide;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProtein;
import org.proteored.miapeapi.interfaces.msi.PeptideModification;
import org.proteored.pacom.analysis.genes.ENSGInfo;
import org.proteored.pacom.analysis.genes.GeneDistributionReader;
import org.proteored.pacom.analysis.util.DataLevel;
import org.proteored.pacom.analysis.util.FileManager;

import edu.scripps.yates.annotations.uniprot.UniprotProteinLocalRetriever;
import edu.scripps.yates.annotations.util.UniprotEntryUtil;
import edu.scripps.yates.utilities.annotations.uniprot.xml.Entry;
import edu.scripps.yates.utilities.fasta.FastaParser;
import edu.scripps.yates.utilities.util.Pair;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class ExporterUtil {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private static ExporterUtil instance;
	private static boolean includePeptides;
	private final Map<String, List<ENSGInfo>> proteinGeneMapping;
	private final Set<IdentificationSet> idSets = new THashSet<IdentificationSet>();
	// Protein Score Order
	private List<String> proteinScoreNames;
	// Peptide Score Order
	private List<String> peptideScoreNames;

	private final static NumberFormat threeDigitsDecimal;

	private final static DecimalFormat df;

	private final static DecimalFormat scientificDecimalFormat;
	private static boolean retrieveFromUniprot;
	private static boolean testOntologies;
	public static final String VALUE_SEPARATOR = ",";

	static {
		threeDigitsDecimal = NumberFormat.getInstance();
		threeDigitsDecimal.setMaximumFractionDigits(3);
		threeDigitsDecimal.setGroupingUsed(false);
		df = new DecimalFormat("#.##");
		scientificDecimalFormat = new DecimalFormat("0.00E00");
	}

	private ExporterUtil(Collection<IdentificationSet> idSets, boolean includePeptides, boolean retrieveFromUniprot) {
		// utils

		ExporterUtil.includePeptides = includePeptides;
		this.idSets.addAll(idSets);

		proteinGeneMapping = GeneDistributionReader.getInstance().getProteinGeneMapping(null);

		ExporterUtil.retrieveFromUniprot = retrieveFromUniprot;
	}

	public static void setTestOntologies(boolean testOntologies) {
		ExporterUtil.testOntologies = testOntologies;
	}

	public static ExporterUtil getInstance(Collection<IdentificationSet> idSets, boolean includePeptides,
			boolean retrieveProteinSequences) {
		boolean createNewInstance = false;
		if (instance == null) {
			createNewInstance = true;
		}

		if ((ExporterUtil.includePeptides && !includePeptides) || (!ExporterUtil.includePeptides && includePeptides))
			createNewInstance = true;

		if ((ExporterUtil.retrieveFromUniprot && !retrieveProteinSequences)
				|| (!ExporterUtil.retrieveFromUniprot && retrieveProteinSequences))
			createNewInstance = true;

		if (createNewInstance)
			instance = new ExporterUtil(idSets, includePeptides, retrieveProteinSequences);

		instance.proteinScoreNames = getProteinScoreNames(idSets);
		instance.peptideScoreNames = getPeptideScoreNames(idSets);
		return instance;
	}

	public List<String> getPeptideInfoList(PeptideOccurrence peptideOccurrence, List<String> columns, int index,
			IdentificationSet idSet) {

		final List<String> ret = new ArrayList<String>();
		if (peptideOccurrence == null)
			throw new IllegalMiapeArgumentException("peptide occurrence should be different to null");

		final StringBuilder sb = new StringBuilder();
		final String scoreName = "";
		for (final String column : columns) {
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
					// } else if
					// (column.equals(ExportedColumns.RESEARCHER.toString())) {
					// ret.add(getResearcher(peptideOccurrence));
					// } else if
					// (column.equals(ExportedColumns.GENE_CLASSIFICATION.toString()))
					// {
					// ret.add(getGeneClassification(peptideOccurrence));
				} else if (column.equals(ExportedColumns.CHARGE.toString())) {
					ret.add(getCharge(peptideOccurrence));
				} else if (column.equals(ExportedColumns.RETENTION_TIME_SG.toString())) {
					ret.add(getRt(peptideOccurrence));
				} else if (column.equals(ExportedColumns.IS_UNIQUE.toString())) {
					final ArrayList<String> accs = new ArrayList<String>();
					for (final ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
						for (final ExtendedIdentifiedProtein protein : pep.getProteins()) {
							if (!accs.contains(protein.getAccession()))
								accs.add(protein.getAccession());
						}
					}
					final int numProteins = accs.size();
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
					ret.add(parseFloat(peptideOccurrence.getFirstOccurrence().getExperimentalMassToCharge()));
				} else if (column.equals(ExportedColumns.CALC_MZ.toString())) {
					ret.add(parseFloat(peptideOccurrence.getFirstOccurrence().getCalculatedMassToCharge()));
				} else if (column.equals(ExportedColumns.ERROR_MZ.toString())) {
					ret.add(parseFloat(peptideOccurrence.getFirstOccurrence().getMassError()));
				} else if (column.equals(ExportedColumns.PEPTIDE_OCCURRENCE.toString())) {
					ret.add(String.valueOf(peptideOccurrence.getItemList().size()));
				} else if (column.equals(ExportedColumns.PEPTIDE_LOCAL_FDR.toString())) {
					ret.add(parseFloat(peptideOccurrence.getPeptideLocalFDR()));
				} else if (column.equals(ExportedColumns.PSM_LOCAL_FDR.toString())) {
					ret.add(parseFloat(peptideOccurrence.getBestPeptide().getPSMLocalFDR()));
					// } else if
					// (column.equals(ExportedColumns.PROTEIN_OCCURRENCE.toString()))
					// {
					// ret.add(getProteinOccurrence(peptideOccurrence, idSet));
				} else if (column.equals(ExportedColumns.PROTEIN_GROUP_TYPE.toString())) {
					// ret.add(cleanString(getProteinGroupType(peptideOccurrence)));
				} else if (column.equals(ExportedColumns.PROTEIN_ACC.toString())) {

					ret.add(getProteinAcc(peptideOccurrence));

				} else if (column.equals(ExportedColumns.PROTEIN_COV.toString())) {

					ret.add(getProteinCoverage(peptideOccurrence, idSet));
				} else if (column.equals(ExportedColumns.PROTEIN_DESC.toString())) {

					ret.add(getProteinDescription(peptideOccurrence));
				} else if (column.equals(ExportedColumns.PROTEIN_DIFF_SEQ.toString())) {

					ret.add(getProteinDiffSeq(peptideOccurrence));
				} else if (column.equals(ExportedColumns.PROTEIN_NUM_PEPS.toString())) {

					ret.add(getNumPeptides(peptideOccurrence));
				} else if (column.equals(ExportedColumns.PROTEIN_SCORE.toString())) {
					// This line should not be used
					ret.add(getProteinScore(peptideOccurrence, idSet));
				} else if (column.equals(ExportedColumns.PEPTIDE_SCORE.toString())) {
					// This line should not be used
					ret.add(getPeptideScore(peptideOccurrence));
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
			} catch (final NullPointerException e) {
				e.printStackTrace();
			}
		}

		return ret;

	}

	public List<String> getProteinInfoList(ProteinGroupOccurrence proteinGroupOccurrence, List<String> columns,
			Integer index, IdentificationSet idSet) {

		final List<String> ret = new ArrayList<String>();
		if (proteinGroupOccurrence == null)
			throw new IllegalMiapeArgumentException("protein occurrence should be different to null");

		for (final String column : columns) {

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
					// } else if
					// (column.equals(ExportedColumns.RESEARCHER.toString())) {
					//
					// ret.add(getResearcher(proteinGroupOccurrence));
					//
					// } else if
					// (column.equals(ExportedColumns.GENE_CLASSIFICATION.toString()))
					// {
					//
					// ret.add(getGeneClassification(proteinGroupOccurrence));
				} else if (column.equals(ExportedColumns.PROTEIN_GROUP_TYPE.toString())) {
					ret.add(cleanString(getProteinGroupType(proteinGroupOccurrence)));
					// } else if
					// (column.equals(ExportedColumns.PROTEIN_OCCURRENCE.toString()))
					// {
					//
					// int proteinGroupOccurrenceNumber = idSet
					// .getProteinGroupOccurrenceNumber(proteinGroupOccurrence.getFirstOccurrence());
					// proteinGroupOccurrenceNumber =
					// proteinGroupOccurrence.getItemList().size();
					// ret.add(cleanString(String.valueOf(proteinGroupOccurrenceNumber)));

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
			} catch (final NullPointerException e) {
				e.printStackTrace();
			}
		}

		return ret;

	}

	private String getProteinGroupType(ProteinGroupOccurrence proteinGroupOccurrence) {
		final List<ProteinGroup> groupList = proteinGroupOccurrence.getItemList();
		final List<ProteinEvidence> evidences = new ArrayList<ProteinEvidence>();
		String ret = "";
		for (final ProteinGroup proteinGroup : groupList) {
			for (final ExtendedIdentifiedProtein protein : proteinGroup) {
				final ProteinEvidence evidence = protein.getEvidence();
				if (!evidences.contains(evidence)) {
					evidences.add(evidence);
					if (!"".equals(ret))
						ret = ret + VALUE_SEPARATOR;
					ret = ret + evidence.toString();
				}
			}

		}

		return ret;
	}

	public String getStringFromList(List<String> lineStringList, char separator) {
		final StringBuilder sb = new StringBuilder();
		if (lineStringList != null)
			for (final String obj : lineStringList) {
				if (obj != null)
					sb.append(obj);
				else
					sb.append("-");
				sb.append(separator);
			}
		return sb.toString();
	}

	private String getPeptideModifications(PeptideOccurrence peptideOccurrence) {
		final StringBuilder modifString = new StringBuilder();

		final Set<PeptideModification> modifications = peptideOccurrence.getFirstOccurrence().getModifications();
		if (modifications != null)
			for (final PeptideModification modification : modifications) {
				if (!"".equals(modifString.toString()))
					modifString.append(VALUE_SEPARATOR);
				modifString.append(modification.getName() + "(" + modification.getResidues() + "-"
						+ modification.getPosition() + ")");
			}

		return cleanString(modifString.toString());
	}

	private String getReplicateNames(PeptideOccurrence peptideOccurrence) {
		final StringBuilder sbReplicatesNames = new StringBuilder();
		final Set<String> replicateNames = new THashSet<String>();

		final List<ExtendedIdentifiedPeptide> peptides = peptideOccurrence.getItemList();
		if (peptides != null) {
			for (final ExtendedIdentifiedPeptide extendedIdentifiedPeptide : peptides) {
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

		for (final String replicateName : replicateNames) {
			if (!"".equals(sbReplicatesNames.toString()))
				sbReplicatesNames.append(VALUE_SEPARATOR);
			sbReplicatesNames.append(replicateName);
		}

		return cleanString(sbReplicatesNames.toString());
	}

	private String getReplicateNames(ProteinGroupOccurrence proteinOccurrence) {
		final StringBuilder sbReplicatesNames = new StringBuilder();
		final Set<String> replicateNames = new THashSet<String>();

		final List<ProteinGroup> proteinGroups = proteinOccurrence.getItemList();
		if (proteinGroups != null) {
			for (final ProteinGroup proteinGroup : proteinGroups) {
				for (final ExtendedIdentifiedProtein extendedIdentifiedProtein : proteinGroup) {
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
		for (final String replicateName : replicateNames) {
			if (!"".equals(sbReplicatesNames.toString()))
				sbReplicatesNames.append(VALUE_SEPARATOR);
			sbReplicatesNames.append(replicateName);
		}

		return cleanString(sbReplicatesNames.toString());
	}

	private String getExperimentName(PeptideOccurrence peptideOccurrence) {
		final StringBuilder sbExperimentNames = new StringBuilder();
		final Set<String> experimentNames = new THashSet<String>();

		final List<ExtendedIdentifiedPeptide> peptides = peptideOccurrence.getItemList();
		for (final ExtendedIdentifiedPeptide extendedIdentifiedPeptide : peptides) {
			final String experimentName = extendedIdentifiedPeptide.getExperimentName();
			if (!experimentNames.contains(experimentName))
				experimentNames.add(experimentName);
		}

		for (final String experimentName : experimentNames) {
			if (!"".equals(sbExperimentNames.toString()))
				sbExperimentNames.append(VALUE_SEPARATOR);
			sbExperimentNames.append(experimentName);
		}

		return cleanString(sbExperimentNames.toString());
	}

	private String getExperimentName(ProteinGroupOccurrence proteinOccurrence) {
		final StringBuilder sbExperimentNames = new StringBuilder();
		final Set<String> experimentNames = new THashSet<String>();

		final List<ExtendedIdentifiedProtein> proteins = proteinOccurrence.getProteins();
		for (final ExtendedIdentifiedProtein extendedIdentifiedProtein : proteins) {
			final String experimentName = extendedIdentifiedProtein.getExperimentName();
			if (!experimentNames.contains(experimentName))
				experimentNames.add(experimentName);
		}
		for (final String experimentName : experimentNames) {
			if (!"".equals(sbExperimentNames.toString()))
				sbExperimentNames.append(VALUE_SEPARATOR);
			sbExperimentNames.append(experimentName);
		}

		return cleanString(sbExperimentNames.toString());
	}

	private String getPeptideScore(PeptideOccurrence peptideOccurrence) {

		final Float score = peptideOccurrence.getBestPeptideScore();

		return parseFloat(score);

	}

	private String getProteinScore(PeptideOccurrence peptideOccurrence, IdentificationSet idSet) {

		try {

			final StringBuilder bestScores = new StringBuilder();
			final ArrayList<String> proteinACCs = new ArrayList<String>();
			for (final ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
				final List<IdentifiedProtein> peptideProteins = pep.getIdentifiedProteins();
				for (final IdentifiedProtein identifiedProtein : peptideProteins) {
					final String acc = identifiedProtein.getAccession();
					if (!proteinACCs.contains(acc)) {
						proteinACCs.add(acc);
						final ProteinGroupOccurrence proteinocc = idSet.getProteinGroupOccurrence(acc);
						if (proteinocc != null) {
							if (!"".equals(bestScores.toString()))
								bestScores.append(VALUE_SEPARATOR);
							bestScores.append(proteinocc.getBestProteinScore());
						}
					}
				}
			}

			return cleanString(bestScores.toString());
		} catch (final IllegalMiapeArgumentException e) {
			return null;
		}
	}

	private String getNumPeptides(PeptideOccurrence peptideOccurrence) {
		final StringBuilder proteinsNumPeps = new StringBuilder();

		final Map<String, List<IdentifiedPeptide>> hashProteins = new THashMap<String, List<IdentifiedPeptide>>();

		for (final ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
			final List<IdentifiedProtein> peptideProteins = pep.getIdentifiedProteins();
			for (final IdentifiedProtein protein : peptideProteins) {
				final String acc = protein.getAccession();
				final List<IdentifiedPeptide> peps = protein.getIdentifiedPeptides();
				if (peps != null)
					if (hashProteins.containsKey(acc)) {
						hashProteins.get(acc).addAll(peps);
					} else {
						hashProteins.put(acc, peps);
					}
			}
		}
		for (final List<IdentifiedPeptide> peptides : hashProteins.values()) {
			if (!"".equals(proteinsNumPeps.toString()))
				proteinsNumPeps.append(VALUE_SEPARATOR);
			proteinsNumPeps.append(peptides.size());
		}

		return cleanString(proteinsNumPeps.toString());
	}

	private String getNumPeptides(ProteinGroupOccurrence proteinOccurrence) {
		final StringBuilder proteinsNumPeps = new StringBuilder();

		int numPeptides = 0;

		for (final ProteinGroup proteinGroup : proteinOccurrence.getItemList()) {
			final List<ExtendedIdentifiedPeptide> peptides = proteinGroup.getPeptides();
			if (peptides != null)
				numPeptides = numPeptides + peptides.size();

		}
		proteinsNumPeps.append(numPeptides);

		return cleanString(proteinsNumPeps.toString());
	}

	private String getProteinDiffSeq(PeptideOccurrence peptideOccurrence) {
		final StringBuilder proteinsDiffSeqs = new StringBuilder();

		final Map<String, List<IdentifiedPeptide>> hashProteins = new THashMap<String, List<IdentifiedPeptide>>();

		for (final ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
			final List<IdentifiedProtein> peptideProteins = pep.getIdentifiedProteins();
			for (final IdentifiedProtein protein : peptideProteins) {
				final String acc = protein.getAccession();
				final List<IdentifiedPeptide> peps = protein.getIdentifiedPeptides();
				if (peps != null)
					if (hashProteins.containsKey(acc)) {
						hashProteins.get(acc).addAll(peps);
					} else {
						hashProteins.put(acc, peps);
					}
			}
		}
		for (final List<IdentifiedPeptide> peptides : hashProteins.values()) {
			if (peptides != null) {
				final int numDifPeptides = getNumDifPeptides(peptides);
				if (!"".equals(proteinsDiffSeqs.toString()))
					proteinsDiffSeqs.append(VALUE_SEPARATOR);
				proteinsDiffSeqs.append(numDifPeptides);
			}
		}

		return cleanString(proteinsDiffSeqs.toString());
	}

	private String getProteinDiffSeq(ProteinGroupOccurrence proteinOccurrence) {
		final StringBuilder proteinsDiffSeqs = new StringBuilder();

		proteinsDiffSeqs.append(getNumDifExtendedPeptides(proteinOccurrence.getPeptides()));

		return cleanString(proteinsDiffSeqs.toString());
	}

	private String getProteinDescription(PeptideOccurrence peptideOccurrence) {
		final StringBuilder proteinsDescriptions = new StringBuilder();

		final List<String> descriptions = new ArrayList<String>();

		for (final ExtendedIdentifiedProtein protein : peptideOccurrence.getProteinList()) {

			String description = protein.getDescription();
			if (description == null || "".equals(description)) {
				if (retrieveFromUniprot) {
					final String uniprotACC = FastaParser.getUniProtACC(protein.getAccession());
					if (uniprotACC != null) {
						if (retrieveFromUniprot) {
							final Map<String, Entry> annotatedProtein = FileManager.getUniprotProteinLocalRetriever()
									.getAnnotatedProtein(null, uniprotACC);
							if (annotatedProtein.containsKey(uniprotACC)) {
								final Entry entry = annotatedProtein.get(uniprotACC);
								description = UniprotEntryUtil.getProteinDescription(entry);
							}
						}
					}
				}
			}
			if (!descriptions.contains(description)) {
				descriptions.add(description);
			}

		}
		for (final String description : descriptions) {
			if (!"".equals(proteinsDescriptions.toString()))
				proteinsDescriptions.append(VALUE_SEPARATOR);
			proteinsDescriptions.append(description);
		}

		return cleanString(proteinsDescriptions.toString());
	}

	private String getProteinDescription(ProteinGroupOccurrence proteinOccurrence) {
		final StringBuilder proteinsDescriptions = new StringBuilder();

		final List<String> descriptions = new ArrayList<String>();

		for (final ExtendedIdentifiedProtein protein : proteinOccurrence.getProteins()) {

			String description = protein.getDescription();
			if (description == null || "".equals(description)) {
				if (retrieveFromUniprot) {
					final String uniprotACC = FastaParser.getUniProtACC(protein.getAccession());
					if (uniprotACC != null) {
						if (retrieveFromUniprot) {
							final Map<String, Entry> annotatedProtein = FileManager.getUniprotProteinLocalRetriever()
									.getAnnotatedProtein(null, uniprotACC);
							if (annotatedProtein.containsKey(uniprotACC)) {
								final Entry entry = annotatedProtein.get(uniprotACC);
								description = UniprotEntryUtil.getProteinDescription(entry);
							}
						}
					}
				}
			}
			if (description != null && !"".equals(description) && !descriptions.contains(description)) {
				descriptions.add(description);
			}

		}
		for (final String description : descriptions) {
			if (!"".equals(proteinsDescriptions.toString()))
				proteinsDescriptions.append(VALUE_SEPARATOR);
			proteinsDescriptions.append(description);
		}
		return cleanString(proteinsDescriptions.toString());
	}

	private String getProteinCoverage(PeptideOccurrence peptideOccurrence, IdentificationSet idSet) {
		final StringBuilder proteinsCovs = new StringBuilder();
		final UniprotProteinLocalRetriever uplr = FileManager.getUniprotProteinLocalRetriever();
		final ExtendedIdentifiedPeptide peptide = peptideOccurrence.getFirstOccurrence();
		final List<IdentifiedProtein> peptideProteins = peptide.getIdentifiedProteins();
		for (final IdentifiedProtein identifiedProtein : peptideProteins) {
			String coverage = ProteinMerger.getCoverage(
					idSet.getProteinGroupOccurrence(identifiedProtein.getAccession()), null, retrieveFromUniprot, uplr);
			if (coverage != null) {
				Double cov = Double.valueOf(coverage);
				cov = cov * 100.0;

				coverage = df.format(cov);
				if (!"".equals(proteinsCovs.toString()))
					proteinsCovs.append(VALUE_SEPARATOR);
				proteinsCovs.append(coverage);
			}
		}

		return cleanString(proteinsCovs.toString());
	}

	private String getProteinCoverage(ProteinGroupOccurrence occurrence) {
		final StringBuilder proteinsCovs = new StringBuilder();

		if (occurrence.isDecoy())
			return cleanString("");
		try {
			final UniprotProteinLocalRetriever upr = FileManager.getUniprotProteinLocalRetriever();
			String coverage = ProteinMerger.getCoverage(occurrence, null, retrieveFromUniprot, upr);

			if (coverage != null) {
				Double cov = Double.valueOf(coverage);
				cov = cov * 100.0;
				coverage = df.format(cov);
				if (!"".equals(proteinsCovs.toString()))
					proteinsCovs.append(VALUE_SEPARATOR);
				proteinsCovs.append(coverage);
			}
		} catch (final Exception e) {

		}
		return cleanString(proteinsCovs.toString());
	}

	private String getProteinAcc(PeptideOccurrence peptideOccurrence) {
		final StringBuilder proteinsAccs = new StringBuilder();

		final ArrayList<String> accs = new ArrayList<String>();

		for (final ExtendedIdentifiedProtein protein : peptideOccurrence.getProteinList()) {
			if (!accs.contains(protein.getAccession()))
				accs.add(protein.getAccession());
		}

		for (final String acc : accs) {
			if (!"".equals(proteinsAccs.toString()))
				proteinsAccs.append(VALUE_SEPARATOR);
			proteinsAccs.append(acc);
		}

		return cleanString(proteinsAccs.toString());
	}

	private String getENSGID(PeptideOccurrence peptideOccurrence) {
		final ArrayList<String> accs = new ArrayList<String>();
		for (final ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
			for (final IdentifiedProtein protein : pep.getIdentifiedProteins()) {
				if (!accs.contains(protein.getAccession()))
					accs.add(protein.getAccession());
			}
		}
		return getENSGID(accs);
	}

	private String getENSGID(ProteinGroupOccurrence proteinOccurrence) {

		final List<String> accs = proteinOccurrence.getAccessions();
		return getENSGID(accs);

	}

	private String getENSGID(List<String> accs) {
		final Set<String> dicc = new THashSet<String>();
		final StringBuilder ENSG_IDS = new StringBuilder();
		for (final String acc : accs) {
			if (FastaParser.isContaminant(acc) || FastaParser.isReverse(acc)) {
				continue;
			}
			final String uniprotACC = FastaParser.getUniProtACC(acc);

			boolean added = false;
			if (uniprotACC != null) {
				if (retrieveFromUniprot) {
					final Map<String, Entry> annotatedProtein = FileManager.getUniprotProteinLocalRetriever()
							.getAnnotatedProtein(null, uniprotACC);
					if (annotatedProtein.containsKey(uniprotACC)) {
						final Entry entry = annotatedProtein.get(uniprotACC);
						final Set<String> ensg_ids = UniprotEntryUtil.getENSGIDs(entry);
						if (ensg_ids != null) {
							for (final String ensg_id : ensg_ids) {
								if (!dicc.contains(ensg_id)) {
									dicc.add(ensg_id);
									if (!"".equals(ENSG_IDS.toString()))
										ENSG_IDS.append(VALUE_SEPARATOR);
									added = true;
									ENSG_IDS.append(ensg_id);
								}
							}
						}
					}
				}
				if (proteinGeneMapping.containsKey(uniprotACC)) {
					final List<ENSGInfo> genes = proteinGeneMapping.get(uniprotACC);
					for (final ENSGInfo gene : genes) {
						final String ensg_id = gene.getEnsG_ID();
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

		final List<String> accs = new ArrayList<String>();
		for (final ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
			for (final IdentifiedProtein protein : pep.getIdentifiedProteins()) {
				if (!accs.contains(protein.getAccession()))
					accs.add(protein.getAccession());
			}
		}
		return getGeneName(accs);
	}

	private String getGeneName(ProteinGroupOccurrence proteinOccurrence) {

		// take all the gene names and then, if there is more than one, report
		// all, other wise, report just one
		final List<String> accs = proteinOccurrence.getAccessions();
		return getGeneName(accs);
	}

	private String getGeneName(List<String> accs) {

		final StringBuilder ret = new StringBuilder();
		for (final String acc : accs) {
			if (FastaParser.isContaminant(acc) || FastaParser.isReverse(acc)) {
				continue;
			}
			final List<String> geneNames = new ArrayList<String>();
			// try first in the internet
			final String uniprotACC = FastaParser.getUniProtACC(acc);
			if (uniprotACC != null) {
				if (retrieveFromUniprot) {
					final UniprotProteinLocalRetriever upr = FileManager.getUniprotProteinLocalRetriever();
					final Map<String, Entry> annotatedProtein = upr.getAnnotatedProtein(null, uniprotACC);
					if (annotatedProtein.containsKey(uniprotACC)) {
						final Entry entry = annotatedProtein.get(uniprotACC);
						final List<Pair<String, String>> geneNameList = UniprotEntryUtil.getGeneName(entry, true, true);
						if (!geneNameList.isEmpty()) {
							if (!geneNames.contains(geneNameList.get(0).getFirstelement())) {
								geneNames.add(geneNameList.get(0).getFirstelement());
							}
						}
					}
				}

				if (proteinGeneMapping.containsKey(uniprotACC)) {
					final List<ENSGInfo> genes = proteinGeneMapping.get(uniprotACC);

					for (final ENSGInfo gene : genes) {
						final String geneName = gene.getGeneName();
						if (geneName != null) {
							if (!geneNames.contains(geneName)) {
								geneNames.add(geneName);
							}
						}
					}
				}
			}
			if (!"".equals(ret.toString()))
				ret.append(VALUE_SEPARATOR);
			if (geneNames.isEmpty()) {
				ret.append("-");
			} else {
				if (geneNames.size() > 1) {
					ret.append("[");
				}
				int i = 0;
				for (final String geneName : geneNames) {
					ret.append(geneName);
					if (i > 0) {
						ret.append(",");
					}
					i++;
				}
				if (geneNames.size() > 1) {
					ret.append("]");
				}
			}
		}
		if ("".equals(ret.toString())) {
			return "-";
		}
		return cleanString(ret.toString());
	}

	private String getChromosomeName(PeptideOccurrence peptideOccurrence) {
		final List<String> accs = new ArrayList<String>();
		for (final ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
			for (final ExtendedIdentifiedProtein protein : pep.getProteins()) {
				if (!accs.contains(protein.getAccession()))
					accs.add(protein.getAccession());
			}
		}
		return getChromosomeName(accs);
	}

	private String getChromosomeName(ProteinGroupOccurrence proteinOccurrence) {

		final List<String> accs = proteinOccurrence.getAccessions();

		return getChromosomeName(accs);
	}

	public String getChromosomeName(List<String> accs) {
		final Set<String> dicc = new THashSet<String>();
		final StringBuilder chrNames = new StringBuilder();
		for (final String acc : accs) {
			boolean added = false;
			if (FastaParser.isContaminant(acc) || FastaParser.isReverse(acc)) {
				continue;
			}
			final String uniprotACC = FastaParser.getUniProtACC(acc);
			if (uniprotACC != null) {
				if (retrieveFromUniprot) {
					// try from internet
					final UniprotProteinLocalRetriever upr = FileManager.getUniprotProteinLocalRetriever();
					final Map<String, Entry> annotatedProtein = upr.getAnnotatedProtein(null, uniprotACC);
					if (annotatedProtein.containsKey(uniprotACC)) {
						final Entry entry = annotatedProtein.get(uniprotACC);
						String chrName = UniprotEntryUtil.getChromosomeName(entry);
						if (chrName != null) {
							if (chrName.startsWith("Chromosome")) {
								chrName = chrName.substring("Chromosome".length()).trim();
							}
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
				if (proteinGeneMapping.containsKey(uniprotACC)) {
					final List<ENSGInfo> genes = proteinGeneMapping.get(uniprotACC);
					for (final ENSGInfo gene : genes) {
						final String chrName = gene.getChrName();
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
		final Set<String> dicc = new THashSet<String>();
		final StringBuilder evidences = new StringBuilder();
		if (!retrieveFromUniprot) {
			return cleanString("");
		}
		final List<String> accs = new ArrayList<String>();
		for (final ExtendedIdentifiedPeptide pep : peptideOccurrence.getItemList()) {
			for (final ExtendedIdentifiedProtein protein : pep.getProteins()) {
				if (!accs.contains(protein.getAccession()))
					accs.add(protein.getAccession());
			}
		}
		for (final String acc : accs) {
			if (FastaParser.isContaminant(acc) || FastaParser.isReverse(acc)) {
				continue;
			}
			final String uniprotACC = FastaParser.getUniProtACC(acc);
			if (uniprotACC != null) {
				final Map<String, Entry> annotatedProtein = FileManager.getUniprotProteinLocalRetriever()
						.getAnnotatedProtein(null, uniprotACC);
				if (annotatedProtein.containsKey(uniprotACC)) {
					final Entry entry = annotatedProtein.get(uniprotACC);
					final String evidence = UniprotEntryUtil.getUniprotEvidence(entry);
					if (evidence != null) {
						if (!dicc.contains(evidence)) {
							dicc.add(evidence);
							if (!"".equals(evidences.toString()))
								evidences.append(VALUE_SEPARATOR);
							evidences.append(evidence);
						}
					}
				}
			}

		}

		return cleanString(evidences.toString());
	}

	private String getProteinEvidenceName(ProteinGroupOccurrence proteinOccurrence) {
		final Set<String> dicc = new THashSet<String>();
		final StringBuilder evidences = new StringBuilder();
		if (!retrieveFromUniprot) {
			return cleanString("");
		}
		final List<String> accs = proteinOccurrence.getAccessions();
		for (final String acc : accs) {
			if (FastaParser.isContaminant(acc) || FastaParser.isReverse(acc)) {
				continue;
			}
			final String uniprotACC = FastaParser.getUniProtACC(acc);
			if (uniprotACC != null) {
				final Map<String, Entry> annotatedProtein = FileManager.getUniprotProteinLocalRetriever()
						.getAnnotatedProtein(null, uniprotACC);
				if (annotatedProtein.containsKey(uniprotACC)) {
					final Entry entry = annotatedProtein.get(uniprotACC);
					final String evidence = UniprotEntryUtil.getUniprotEvidence(entry);
					if (evidence != null) {
						if (!dicc.contains(evidence)) {
							dicc.add(evidence);
							if (!"".equals(evidences.toString()))
								evidences.append(VALUE_SEPARATOR);
							evidences.append(evidence);
						}
					}
				}
			}
		}

		return cleanString(evidences.toString());
	}

	private int getNumDifExtendedPeptides(List<ExtendedIdentifiedPeptide> list) {
		if (list != null) {

			final Set<String> seqs = new THashSet<String>();
			for (final IdentifiedPeptide pep : list) {
				final String seq = pep.getSequence();
				if (!seqs.contains(seq))
					seqs.add(seq);
			}
			return seqs.size();
		}
		return 0;
	}

	private int getNumDifPeptides(List<IdentifiedPeptide> list) {
		if (list != null) {

			final Set<String> seqs = new THashSet<String>();
			for (final IdentifiedPeptide pep : list) {
				final String seq = pep.getSequence();
				if (!seqs.contains(seq))
					seqs.add(seq);
			}
			return seqs.size();
		}
		return 0;
	}

	private String getCharge(PeptideOccurrence peptideOccurrence) {
		final Set<String> charges = new THashSet<String>();
		for (final ExtendedIdentifiedPeptide extendedPeptide : peptideOccurrence.getItemList()) {
			final String charge = extendedPeptide.getCharge();
			if (charge != null && !charges.contains(charge)) {
				charges.add(charge);
			}
		}
		return cleanString(charges);
	}

	private String getRt(PeptideOccurrence peptideOccurrence) {
		final Set<String> rts = new THashSet<String>();
		for (final ExtendedIdentifiedPeptide extendedPeptide : peptideOccurrence.getItemList()) {
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
				final Integer i = Integer.valueOf(string);
				return String.valueOf(i);
			} catch (final NumberFormatException e1) {
				try {
					final Double d = Double.valueOf(string);
					return parseDouble(d);
				} catch (final NumberFormatException e) {
					return string.trim();
				}
			}
		}

		return string.trim();

	}

	private String cleanString(Collection<String> objs) {
		final StringBuilder sb = new StringBuilder("");
		if (objs == null || objs.isEmpty())
			return "-";
		for (final String obj : objs) {
			if (!"".equals(sb.toString()))
				sb.append(VALUE_SEPARATOR);
			sb.append(obj);
		}

		return sb.toString().trim();
	}

	private String parseDouble(Double score) {
		if (score == null)
			return "-";
		// String test = DecimalFormat.getNumberInstance().format(score);
		// return test;
		String format = "";
		if (score == 0.0)
			return "0";
		if (score > 0.01) {

			format = threeDigitsDecimal.format(score);
		} else {
			// NumberFormat formater = DecimalFormat.getInstance();
			// formater.setMaximumFractionDigits(30);
			// formater.setMinimumFractionDigits(3);
			// formater.setGroupingUsed(false);
			// format = formater.format(score);

			format = scientificDecimalFormat.format(score);
		}
		// final String format = df.format(score);
		// log.info("Parsed from " + score + " to " + format);
		if ("".equals(format)) {
			return "-";
		}
		try {
			NumberFormat.getInstance().parse(format);

		} catch (final NumberFormatException e) {
			log.info("CUIDADO");
		} catch (final ParseException e) {
			log.info("CUIDADO");
		}
		return format;
	}

	private String parseFloat(Float score) {
		if (score == null)
			return "-";
		// String test = DecimalFormat.getNumberInstance().format(score);
		// return test;
		String format = "";
		if (score == 0.0)
			return "0";
		if (score > 0.01) {
			final NumberFormat formater = NumberFormat.getInstance();
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
			format = scientificDecimalFormat.format(score);
		}
		// final String format = df.format(score);
		// log.info("Parsed from " + score + " to " + format);
		if ("".equals(format)) {
			return "-";
		}
		try {
			NumberFormat.getInstance().parse(format);

		} catch (final NumberFormatException e) {
			log.info("CUIDADO");
		} catch (final ParseException e) {
			log.info("CUIDADO");
		}
		return format;
	}

	public static boolean isNonConclusiveProtein(ProteinGroup proteinGroup) {
		if (proteinGroup != null) {
			for (final ExtendedIdentifiedProtein protein : proteinGroup) {
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
			final List<ExtendedIdentifiedProtein> proteins = proteinGroupOccurrence.getProteins();
			if (proteins != null) {
				for (final ExtendedIdentifiedProtein protein : proteins) {
					if (protein.getEvidence().equals(ProteinEvidence.NONCONCLUSIVE))
						return true;
				}
			}
		}
		return false;

	}

	public static boolean isNonConclusivePeptide(ExtendedIdentifiedPeptide peptide) {
		if (peptide != null) {
			final List<ExtendedIdentifiedProtein> proteins = peptide.getProteins();
			if (proteins != null)
				for (final ExtendedIdentifiedProtein protein : proteins) {
					if (protein.getEvidence() == ProteinEvidence.NONCONCLUSIVE)
						return true;
				}
		}
		return false;
	}

	public static boolean isNonConclusivePeptide(PeptideOccurrence peptideOccurrence) {
		if (peptideOccurrence != null) {
			for (final ExtendedIdentifiedPeptide peptide : peptideOccurrence.getItemList()) {
				final List<ExtendedIdentifiedProtein> proteins = peptide.getProteins();
				if (proteins != null)
					for (final ExtendedIdentifiedProtein protein : proteins) {
						if (protein.getEvidence() == ProteinEvidence.NONCONCLUSIVE)
							return true;
					}
			}
		}
		return false;
	}

	/**
	 * @param idSets
	 * @return
	 */
	public static List<String> getProteinScoreNames(Collection<IdentificationSet> idSets) {
		final Set<String> ret = new THashSet<String>();
		for (final IdentificationSet idSet : idSets) {
			ret.addAll(idSet.getProteinScoreNames());
		}
		final List<String> list = new ArrayList<String>();
		Collections.sort(list);
		return list;
	}

	/**
	 * @param idSets
	 * @return
	 */
	public static List<String> getPeptideScoreNames(Collection<IdentificationSet> idSets) {
		final Set<String> ret = new THashSet<String>();
		for (final IdentificationSet idSet : idSets) {
			ret.addAll(idSet.getPeptideScoreNames());
		}

		final List<String> list = new ArrayList<String>();
		list.addAll(ret);
		Collections.sort(list);
		return list;
	}

	public static Set<IdentificationSet> getSelectedIdentificationSets(IdentificationSet idSet, DataLevel dataLevel) {
		final Set<IdentificationSet> set = new THashSet<IdentificationSet>();
		set.add(idSet);
		return set;
	}

	/**
	 * Gets the {@link IdentificationSet} corresponding to the level of data
	 * that is selected.
	 * 
	 * @param idSets
	 * @return
	 */
	public static Set<IdentificationSet> getSelectedIdentificationSets(Set<IdentificationSet> idSets,
			DataLevel dataLevel) {
		if (idSets.isEmpty()) {
			return Collections.emptySet();
		}
		final Set<IdentificationSet> ret = new THashSet<IdentificationSet>();

		switch (dataLevel) {
		case LEVEL0:
			for (final IdentificationSet idSet : idSets) {
				if (idSet instanceof ExperimentList) {
					try {
						ret.add(idSet);
					} catch (final UnsupportedOperationException e) {

					}
				}
			}
			if (ret.isEmpty()) {
				for (final IdentificationSet idSet : idSets) {
					try {
						ret.addAll(idSet.getNextLevelIdentificationSetList());
					} catch (final UnsupportedOperationException e) {

					}
				}
				return getSelectedIdentificationSets(ret, dataLevel);
			}
			break;
		case LEVEL1:
			for (final IdentificationSet idSet : idSets) {
				if (idSet instanceof Experiment) {
					ret.add(idSet);
				}
			}
			if (ret.isEmpty()) {
				for (final IdentificationSet idSet : idSets) {
					try {
						ret.addAll(idSet.getNextLevelIdentificationSetList());
					} catch (final UnsupportedOperationException e) {

					}
				}
				return getSelectedIdentificationSets(ret, dataLevel);
			}
			break;
		case LEVEL2:
			for (final IdentificationSet idSet : idSets) {
				if (idSet instanceof Replicate) {
					ret.add(idSet);
				}
			}
			if (ret.isEmpty()) {
				for (final IdentificationSet idSet : idSets) {
					ret.addAll(idSet.getNextLevelIdentificationSetList());
				}
				return getSelectedIdentificationSets(ret, dataLevel);
			}
			break;
		default:
			break;
		}
		return ret;
	}
}
