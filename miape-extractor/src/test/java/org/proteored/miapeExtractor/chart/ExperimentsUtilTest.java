package org.proteored.miapeExtractor.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedProtein;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.ProteinGroup;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.experiment.model.filters.FDRFilter;
import org.proteored.miapeapi.experiment.model.filters.Filter;
import org.proteored.miapeapi.experiment.model.sort.Order;
import org.proteored.miapeapi.experiment.model.sort.SortingParameters;
import org.proteored.miapeapi.factories.MiapeDocumentFactory;
import org.proteored.miapeapi.factories.msi.MiapeMSIDocumentBuilder;
import org.proteored.miapeapi.factories.msi.MiapeMSIDocumentFactory;
import org.proteored.miapeapi.factories.msi.PeptideScoreBuilder;
import org.proteored.miapeapi.factories.msi.ProteinScoreBuilder;
import org.proteored.miapeapi.interfaces.Project;
import org.proteored.miapeapi.interfaces.msi.IdentifiedPeptide;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProtein;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProteinSet;
import org.proteored.miapeapi.interfaces.msi.MiapeMSIDocument;
import org.proteored.miapeapi.interfaces.msi.PeptideScore;
import org.proteored.miapeapi.interfaces.msi.ProteinScore;

public class ExperimentsUtilTest {
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");

	final static Random random = new Random(System.currentTimeMillis());
	private static final int MAX_NUM_PROTEINS = 1000;
	private static final String decoyPrefix = "rev_";
	public static final String PROTEIN_SCORE_NAME = "Mascot:score";
	public static final Order PROTEIN_SCORE_ORDER = Order.DESCENDANT;
	public static final String PEPTIDE_SCORE_NAME = "Mascot:expectation value";
	public static final Order PEPTIDE_SCORE_ORDER = Order.ASCENDANT;
	private static final int MAX_NUM_PEPTIDES_PER_PROTEIN = 5;
	private static final int MAX_NUM_PEPTIDES_TOTAL = 50;
	private static final int MAX_PEPTIDE_LENGTH = 20;
	private static final int REPLICATES_PER_EXPERIMENT = 3;
	private static final int EXPERIMENT_NUMBER = 1;
	private static int identifier = 0;
	private static SortingParameters defaultSortingProteins = new SortingParameters(PROTEIN_SCORE_NAME,
			PROTEIN_SCORE_ORDER);
	private static SortingParameters defaultSortingPeptides = new SortingParameters(PEPTIDE_SCORE_NAME,
			PEPTIDE_SCORE_ORDER);
	private static final int minPeptideLength = 7;

	public static SortingParameters getDefaultSortingProteins() {
		return defaultSortingProteins;
	}

	public static SortingParameters getDefaultSortingPeptides() {
		return defaultSortingPeptides;
	}

	public static ExperimentList createExperiments(List<Filter> filters) {
		SortingParameters spProteins = new SortingParameters(PROTEIN_SCORE_NAME, PROTEIN_SCORE_ORDER);
		SortingParameters spPeptides = new SortingParameters(PEPTIDE_SCORE_NAME, PEPTIDE_SCORE_ORDER);

		List<Experiment> experimentList = new ArrayList<Experiment>();
		for (int i = 0; i < EXPERIMENT_NUMBER; i++) {
			experimentList.add(createExperiment(getRandomString(4), filters));
		}

		return new ExperimentList("Experiment list", experimentList, false, filters, minPeptideLength, null, false);
	}

	public static Experiment createExperiment(String experimentName, List<Filter> filters) {

		final List<Replicate> replicates = createReplicates(experimentName, filters);
		Experiment experiment = new Experiment(experimentName, replicates, filters, minPeptideLength, null, false);

		return experiment;
	}

	private static List<Replicate> createReplicates(String experimentName, List<Filter> filters) {
		List<Replicate> replicates = new ArrayList<Replicate>();
		for (int i = 0; i < REPLICATES_PER_EXPERIMENT; i++) {
			final String name = "REP" + (i + 1);
			replicates.add(createReplicate(experimentName, name, filters));
		}
		return replicates;
	}

	public static Replicate createReplicate(String experimentName, String replicateName, List<Filter> filters) {
		final MiapeMSIDocument createMiapeMSI = createMiapeMSI(experimentName, replicateName);
		List<MiapeMSIDocument> miapeList = new ArrayList<MiapeMSIDocument>();
		miapeList.add(createMiapeMSI);
		return new Replicate(replicateName, experimentName, null, miapeList, filters, null, null, false);
	}

	private static MiapeMSIDocument createMiapeMSI(String experimentName, String replicateName) {
		Project project = MiapeDocumentFactory.createProjectBuilder("ReplicateTestProject").build();
		final MiapeMSIDocumentBuilder msiBuilder = MiapeMSIDocumentFactory.createMiapeDocumentMSIBuilder(project,
				experimentName + " " + replicateName, null, null, null, null);
		msiBuilder.identifiedProteinSets(createIdentifiedProteinSet());
		msiBuilder.identifiedPeptides(createIdentifiedPeptides(MAX_NUM_PEPTIDES_TOTAL));
		return msiBuilder.build();
	}

	private static Set<IdentifiedProteinSet> createIdentifiedProteinSet() {
		Set<IdentifiedProteinSet> identifiedProteinSet = new HashSet<IdentifiedProteinSet>();
		List<ExtendedIdentifiedProtein> createIdentifiedProteins = createIdentifiedProteins(
				getRandomInt(MAX_NUM_PROTEINS), true);
		HashMap<String, IdentifiedProtein> hashproteins = new HashMap<String, IdentifiedProtein>();
		for (IdentifiedProtein identifiedProtein : createIdentifiedProteins) {
			hashproteins.put(identifiedProtein.getAccession(), identifiedProtein);
		}
		identifiedProteinSet.add(MiapeMSIDocumentFactory.createIdentifiedProteinSetBuilder("protein set")
				.identifiedProteins(hashproteins).build());
		return identifiedProteinSet;
	}

	public static List<ExtendedIdentifiedProtein> createIdentifiedProteins(int number, boolean createAlsoPeptides) {

		List<ExtendedIdentifiedProtein> identifiedProteins = new ArrayList<ExtendedIdentifiedProtein>();
		for (int i = 0; i < number; i++) {
			Set<ProteinScore> scores = new HashSet<ProteinScore>();
			final String random = getRandomDouble().toString();
			scores.add(new ProteinScoreBuilder(PROTEIN_SCORE_NAME, random).build());
			String prefix = "";
			if (random.endsWith("9"))
				prefix = decoyPrefix;
			List<IdentifiedPeptide> peptides = null;
			if (createAlsoPeptides) {
				peptides = createIdentifiedPeptides(getRandomInt(MAX_NUM_PEPTIDES_PER_PROTEIN));
			}
			final IdentifiedProtein protein = MiapeMSIDocumentFactory
					.createIdentifiedProteinBuilder(prefix + "Protein" + getRandomInt(number / 2)).scores(scores)
					.identifiedPeptides(peptides).id(identifier++).build();

			identifiedProteins.add(new ExtendedIdentifiedProtein(null, protein, null));
		}
		// log.info(identifiedProteins.size() + " proteins created");
		return identifiedProteins;
	}

	public static List<IdentifiedPeptide> createIdentifiedPeptides(int number) {

		List<IdentifiedPeptide> peptides = new ArrayList<IdentifiedPeptide>();
		for (int i = 0; i < number; i++) {
			Set<PeptideScore> scores = new HashSet<PeptideScore>();
			final String random = getRandomDouble().toString();
			scores.add(new PeptideScoreBuilder(PEPTIDE_SCORE_NAME, random).build());

			int length = getRandomInt(MAX_PEPTIDE_LENGTH);
			String sequence = getRandomString(length);

			List<ExtendedIdentifiedProtein> createIdentifiedProteins = createIdentifiedProteins(
					ExperimentsUtilTest.random.nextInt(10) + 1, false);
			List<IdentifiedProtein> proteins = new ArrayList<IdentifiedProtein>();
			for (ExtendedIdentifiedProtein identifiedProtein : createIdentifiedProteins) {
				proteins.add(identifiedProtein.getProtein());
			}
			final IdentifiedPeptide peptide = MiapeMSIDocumentFactory.createIdentifiedPeptideBuilder(sequence)
					.score(scores).identifiedProteins(proteins).id(identifier++).build();
			peptides.add(peptide);
		}
		return peptides;
	}

	public static String getRandomString(int length) {
		StringBuilder sequence = new StringBuilder("");
		for (int j = 0; j < length; j++) {
			char ch = (char) (ExperimentsUtilTest.random.nextInt(24) + 'A');
			sequence.append(ch);
		}
		return sequence.toString();
	}

	public static String getDecoyPrefix() {
		return decoyPrefix;
	}

	private static int getRandomInt(int j) {
		return random.nextInt(j + 1);
	}

	private static int getRandomInt() {
		return random.nextInt();
	}

	private static Double getRandomDouble() {
		return random.nextDouble();
	}

	public static FDRFilter getFDRFilter(float threshold, boolean concatenatedDecoySearch, SortingParameters sorting,
			IdentificationItemEnum item) {
		return new FDRFilter(threshold, getDecoyPrefix(), concatenatedDecoySearch, sorting, item, null, null, null);
	}

	public static void printProteinList(List<ProteinGroup> identifiedProteins) {
		System.out.println("----");
		for (ProteinGroup identifiedProtein : identifiedProteins) {
			System.out.print(identifiedProtein.getAccessions().iterator().next() + ":"
					+ identifiedProtein.getScores().iterator().next().getValue() + " ");
		}
		System.out.println("----");
	}
}
