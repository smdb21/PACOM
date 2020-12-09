package org.proteored.pacom.analysis.gui.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.VennData;
import org.proteored.miapeapi.experiment.VennDataForPeptides;
import org.proteored.miapeapi.experiment.VennDataForProteins;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedPeptide;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedProtein;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.PeptideOccurrence;
import org.proteored.miapeapi.experiment.model.ProteinGroup;
import org.proteored.miapeapi.experiment.model.ProteinGroupOccurrence;
import org.proteored.miapeapi.experiment.model.datamanager.DataManager;
import org.proteored.miapeapi.experiment.model.filters.FDRFilter;
import org.proteored.miapeapi.experiment.model.grouping.ProteinEvidence;
import org.proteored.miapeapi.experiment.model.sort.ProteinGroupComparisonType;
import org.proteored.miapeapi.experiment.model.sort.SorterUtil;
import org.proteored.miapeapi.interfaces.ms.Spectrometer;
import org.proteored.miapeapi.interfaces.msi.Database;
import org.proteored.miapeapi.interfaces.msi.InputParameter;
import org.proteored.miapeapi.util.ProteinSequenceRetrieval;
import org.proteored.pacom.analysis.charts.MyXYItemLabelGenerator;
import org.proteored.pacom.analysis.exporters.util.ExporterUtil;
import org.proteored.pacom.analysis.genes.ENSGInfo;
import org.proteored.pacom.analysis.genes.GeneDistributionReader;
import org.proteored.pacom.analysis.gui.AdditionalOptionsPanelFactory;
import org.proteored.pacom.analysis.util.FileManager;

import com.compomics.util.protein.AASequenceImpl;
import com.compomics.util.protein.Protein;

import edu.scripps.yates.utilities.annotations.uniprot.UniprotEntryUtil;
import edu.scripps.yates.utilities.annotations.uniprot.xml.Entry;
import edu.scripps.yates.utilities.fasta.FastaParser;
import edu.scripps.yates.utilities.maths.Maths;
import edu.scripps.yates.utilities.util.Pair;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

public class DatasetFactory {
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");

	/**
	 * Each category belongs to one of the {@link IdentificationSet} of the list,
	 * and has the number of items identified in that {@link IdentificationSet}<br>
	 *
	 *
	 * @param idSets
	 * @param itemType
	 * @param distModPeptides
	 * @param differentIdentificationsShown
	 * @param takeThirdLevel
	 * @return
	 */
	public static CategoryDataset createNumberIdentificationCategoryDataSet(List<IdentificationSet> idSets,
			IdentificationItemEnum itemType, Boolean distModPeptides, Boolean average,
			boolean differentIdentificationsShown, boolean countNonConclusiveProteins) {
		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (final IdentificationSet idSet : idSets) {
				final String experimentName = idSet.getFullName();
				if (itemType.equals(IdentificationItemEnum.PROTEIN)) {

					double numProteins = 0;
					if (differentIdentificationsShown) {
						if (average)
							numProteins = idSet.getAverageNumDifferentProteinGroups(countNonConclusiveProteins);
						else
							numProteins = idSet.getNumDifferentProteinGroups(countNonConclusiveProteins);
					} else {
						if (average)
							numProteins = idSet.getAverageTotalNumProteinGroups(countNonConclusiveProteins);
						else
							numProteins = idSet.getTotalNumProteinGroups(countNonConclusiveProteins);
					}
					dataset.addValue(numProteins, idSet.getFullName(), "");
				} else if (itemType.equals(IdentificationItemEnum.PEPTIDE)) {
					double numPeptides = 0;
					if (differentIdentificationsShown) {
						if (average)
							numPeptides = idSet.getAverageNumDifferentPeptides(distModPeptides);
						else
							numPeptides = idSet.getNumDifferentPeptides(distModPeptides);
					} else {
						if (average)
							numPeptides = idSet.getAverageTotalNumPeptides();
						else
							numPeptides = idSet.getTotalNumPeptides();
					}
					dataset.addValue(numPeptides, idSet.getFullName(), "");
				}
			}

		} catch (final IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createNumberSingleHitProteinsCategoryDataSet(List<IdentificationSet> idSets,
			boolean differentIdentificationsShown, Boolean countNonConclusiveProteins) {
		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (final IdentificationSet idSet : idSets) {
				final String experimentName = idSet.getFullName();

				int numProteins = 0;
				if (differentIdentificationsShown) {
					numProteins = getNumDifferentSingleHitProteins(idSet, countNonConclusiveProteins);
				} else {
					numProteins = getTotalNumSingleHitProteins(idSet, countNonConclusiveProteins);
				}
				dataset.addValue(numProteins, idSet.getFullName(), "");
			}

		} catch (final IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createPeptideNumberInProteinsCategoryDataSet(List<IdentificationSet> idSets,
			int maximum, boolean psmsOrPeptides, Boolean countNonConclusiveProteins) {
		// create the dataset...

		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {
			final Map<String, TIntObjectHashMap<Integer>> distributions = new THashMap<String, TIntObjectHashMap<Integer>>();
			for (final IdentificationSet idSet : idSets) {
				final String experimentName = idSet.getFullName();

				final double numProteins = 0;
				// key: number of peptides - value=number of proteins with that
				// number of peptides
				final TIntObjectHashMap<Integer> distribution = new TIntObjectHashMap<Integer>();
				// if (psmsOrPeptides) {
				final Collection<ProteinGroupOccurrence> proteinOccurrenceList = idSet.getProteinGroupOccurrenceList()
						.values();
				for (final ProteinGroupOccurrence proteinOccurrence : proteinOccurrenceList) {
					if (proteinOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
						continue;
					final List<ExtendedIdentifiedPeptide> peptides = proteinOccurrence.getPeptides();

					int numPeptides = peptides.size();
					if (!psmsOrPeptides) {
						numPeptides = getNumDifferentPeptides(peptides);
					}
					if (numPeptides > maximum)
						numPeptides = maximum;
					if (!distribution.containsKey(numPeptides)) {
						distribution.put(numPeptides, 1);
					} else {
						final int newNum = distribution.get(numPeptides) + 1;
						distribution.remove(numPeptides);
						distribution.put(numPeptides, newNum);
					}

				}

				// }
				// else {
				// List<ProteinGroup> proteinList =
				// idSet.getIdentifiedProteinGroups();
				// for (ProteinGroup proteinGroup : proteinList) {
				// if (proteinGroup.getEvidence() ==
				// ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				// continue;
				//
				// List<ExtendedIdentifiedPeptide> peptides =
				// proteinGroup.getPeptides();
				// int numPeptides = getNumDifferentPeptides(peptides);
				// if (numPeptides > maximum)
				// numPeptides = maximum;
				// if (!distribution.containsKey(numPeptides)) {
				// distribution.put(numPeptides, 1);
				// } else {
				// int newNum = distribution.get(numPeptides) + 1;
				// distribution.remove(numPeptides);
				// distribution.put(numPeptides, newNum);
				// }
				// }
				//
				// }
				if (!distribution.isEmpty()) {
					distributions.put(idSet.getFullName(), distribution);
				} else {
					log.info("Distribution for idSet: " + idSet.getName() + " is empty");
				}
			}
			if (!distributions.isEmpty()) {
				for (int i = 1; i <= maximum; i++) {
					for (final IdentificationSet idSet : idSets) {
						final TIntObjectHashMap<Integer> distribution = distributions.get(idSet.getFullName());
						if (distribution == null) {
							dataset.addValue(0, "0", idSet.getFullName());
							continue;
						}
						String numPeptides = String.valueOf(i);
						if (i == maximum)
							numPeptides = i + " or more";
						if (distribution.containsKey(i)) {
							dataset.addValue(distribution.get(i), numPeptides, idSet.getFullName());
						} else {
							// dataset.addValue(0, numPeptides,
							// idSet.getFullName());
						}
					}
				}
			}
		} catch (final IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	private static int getNumDifferentPeptides(List<ExtendedIdentifiedPeptide> peptides) {
		int ret = 0;
		if (peptides != null) {
			final Set<String> sequences = new THashSet<String>();
			for (final ExtendedIdentifiedPeptide extendedIdentifiedPeptide : peptides) {
				if (!sequences.contains(extendedIdentifiedPeptide.getSequence())) {
					sequences.add(extendedIdentifiedPeptide.getSequence());
				}
			}
			ret = sequences.size();
		}

		return ret;
	}

	public static CategoryDataset createTotalVsDifferentNumberIdentificationCategoryDataSet(
			List<IdentificationSet> idSets, IdentificationItemEnum itemType, Boolean distinguishModifiedPeptides,
			boolean countNonConclusiveProteins, boolean average) {
		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (final IdentificationSet idSet : idSets) {
				final String experimentName = idSet.getFullName();
				if (itemType.equals(IdentificationItemEnum.PROTEIN)) {

					double numProteins = 0;

					if (average)
						numProteins = idSet.getAverageTotalVsDifferentNumProteinGroups(countNonConclusiveProteins);
					else
						numProteins = idSet.getTotalVsDifferentNumProteinGroups(countNonConclusiveProteins);

					dataset.addValue(numProteins, idSet.getFullName(), "");
				} else if (itemType.equals(IdentificationItemEnum.PEPTIDE)) {
					double numPeptides = 0;

					if (average)
						numPeptides = idSet.getAverageTotalVsDifferentNumPeptides(distinguishModifiedPeptides);
					else
						numPeptides = idSet.getTotalVsDifferentNumPeptides(distinguishModifiedPeptides);

					dataset.addValue(numPeptides, idSet.getFullName(), "");
				}
			}

		} catch (final IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createNumberIdentificationStatisticalCategoryDataSet(List<IdentificationSet> idSets,
			IdentificationItemEnum itemType, Boolean distModPeptides, boolean differentIdentificationsShown,
			boolean countNonConclusiveProteins) {
		// create the dataset...
		final DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
		try {

			for (final IdentificationSet idSet : idSets) {
				final String experimentName = idSet.getFullName();
				if (itemType.equals(IdentificationItemEnum.PROTEIN)) {
					double meanNumProteins = 0;
					double stdNumProteins = 0;
					if (differentIdentificationsShown) {
						final int numProteins = idSet.getNumDifferentProteinGroups(countNonConclusiveProteins);
						if (numProteins > 0) {
							meanNumProteins = idSet.getAverageNumDifferentProteinGroups(countNonConclusiveProteins);
							stdNumProteins = idSet.getStdNumDifferentProteinGroups(countNonConclusiveProteins);
						}
					} else {
						final int numProteins = idSet.getTotalNumProteinGroups(countNonConclusiveProteins);
						if (numProteins > 0) {
							meanNumProteins = idSet.getAverageTotalNumProteinGroups(countNonConclusiveProteins);
							stdNumProteins = idSet.getStdTotalNumProteinGroups(countNonConclusiveProteins);
						}
					}
					dataset.add(meanNumProteins, stdNumProteins, idSet.getFullName(), "");
				} else if (itemType.equals(IdentificationItemEnum.PEPTIDE)) {
					double meanNumPeptides = 0;
					double stdNumPeptides = 0;
					if (differentIdentificationsShown) {
						final int numPeptides = idSet.getNumDifferentPeptides(distModPeptides);
						if (numPeptides > 0) {
							meanNumPeptides = idSet.getAverageNumDifferentPeptides(distModPeptides);
							stdNumPeptides = idSet.getStdNumDifferentPeptides(distModPeptides);
						}
					} else {
						final int numPeptides = idSet.getTotalNumPeptides();
						if (numPeptides > 0) {
							meanNumPeptides = idSet.getAverageTotalNumPeptides();
							stdNumPeptides = idSet.getStdTotalNumPeptides();
						}
					}
					dataset.add(meanNumPeptides, stdNumPeptides, idSet.getFullName(), "");
				}
			}

		} catch (final IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createTotalVsDifferentNumberIdentificationStatisticalCategoryDataSet(
			List<IdentificationSet> idSets, IdentificationItemEnum itemType, Boolean distModPeptides,
			boolean countNonConclusiveProteins) {
		final DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
		try {
			// TODO
			for (final IdentificationSet idSet : idSets) {
				final String experimentName = idSet.getFullName();
				if (itemType.equals(IdentificationItemEnum.PROTEIN)) {
					double meanNumProteins = 0;
					double stdNumProteins = 0;
					final double numProteins = idSet.getTotalVsDifferentNumProteinGroups(countNonConclusiveProteins);
					if (numProteins > 0) {
						meanNumProteins = idSet.getAverageTotalVsDifferentNumProteinGroups(countNonConclusiveProteins);
						stdNumProteins = idSet.getStdTotalVsDifferentNumProteinGroups(countNonConclusiveProteins);
					}
					dataset.add(meanNumProteins, stdNumProteins, idSet.getFullName(), "");
				} else if (itemType.equals(IdentificationItemEnum.PEPTIDE)) {
					double meanNumPeptides = 0;
					double stdNumPeptides = 0;
					final double numPeptides = idSet.getTotalVsDifferentNumPeptides(distModPeptides);
					if (numPeptides > 0) {
						meanNumPeptides = idSet.getAverageTotalVsDifferentNumPeptides(distModPeptides);
						stdNumPeptides = idSet.getStdTotalVsDifferentNumPeptides(distModPeptides);
					}
					dataset.add(meanNumPeptides, stdNumPeptides, idSet.getFullName(), "");
				}
			}

		} catch (final IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static PieDataset createNumberIdentificationPieDataSet(List<IdentificationSet> idSets,
			IdentificationItemEnum plotItem, Boolean distinguishModifiedPeptides, Boolean average,
			boolean differentIdentificationsShown, boolean countNonConclusiveProteins) {
		final DefaultPieDataset dataset = new DefaultPieDataset();

		for (final IdentificationSet identificationSet : idSets) {
			if (plotItem.equals(IdentificationItemEnum.PROTEIN)) {

				double numProteins = 0;
				if (differentIdentificationsShown) {
					if (average)
						numProteins = identificationSet.getAverageNumDifferentProteinGroups(countNonConclusiveProteins);
					else
						numProteins = identificationSet.getNumDifferentProteinGroups(countNonConclusiveProteins);
				} else {
					if (average)
						numProteins = identificationSet.getAverageTotalNumProteinGroups(countNonConclusiveProteins);
					else
						numProteins = identificationSet.getTotalNumProteinGroups(countNonConclusiveProteins);
				}
				if (numProteins > 0)
					dataset.setValue(identificationSet.getName(), numProteins);
			} else if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
				double numPeptides = 0;
				if (differentIdentificationsShown) {
					if (average)
						numPeptides = identificationSet.getAverageNumDifferentPeptides(distinguishModifiedPeptides);
					else
						numPeptides = identificationSet.getNumDifferentPeptides(distinguishModifiedPeptides);
				} else {
					if (average)
						numPeptides = identificationSet.getAverageTotalNumPeptides();
					else
						numPeptides = identificationSet.getTotalNumPeptides();
				}

				if (numPeptides > 0)
					dataset.setValue(identificationSet.getName(), numPeptides);
			}
		}

		return dataset;
	}

	public static PieDataset createNumberSingleHitProteinsPieDataSet(List<IdentificationSet> idSets,
			boolean differentIdentificationsShown, Boolean countNonConclusiveProteins) {
		final DefaultPieDataset dataset = new DefaultPieDataset();

		for (final IdentificationSet identificationSet : idSets) {

			double numProteins = 0;
			if (differentIdentificationsShown) {
				numProteins = getNumDifferentSingleHitProteins(identificationSet, countNonConclusiveProteins);
			} else {
				numProteins = getTotalNumSingleHitProteins(identificationSet, countNonConclusiveProteins);
			}
			if (numProteins > 0)
				dataset.setValue(identificationSet.getName(), numProteins);

		}

		return dataset;
	}

	public static int getNumDifferentSingleHitProteins(IdentificationSet idSet, Boolean countNonConclusiveProteins) {
		int num = 0;
		final Collection<ProteinGroupOccurrence> proteinOccurrenceList2 = idSet.getProteinGroupOccurrenceList()
				.values();
		for (final ProteinGroupOccurrence proteinOccurrence : proteinOccurrenceList2) {
			if (proteinOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;

			final List<ExtendedIdentifiedPeptide> peptides = proteinOccurrence.getPeptides();
			if (peptides != null && peptides.size() == 1)
				num++;

		}
		return num;
	}

	public static int getTotalNumSingleHitProteins(IdentificationSet idSet, Boolean countNonConclusiveProteins) {
		int num = 0;
		final List<ProteinGroup> proteinGroups = idSet.getIdentifiedProteinGroups();
		for (final ProteinGroup proteinGroup : proteinGroups) {
			if (proteinGroup.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;
			final List<ExtendedIdentifiedPeptide> peptides = proteinGroup.getPeptides();
			if (peptides != null && peptides.size() == 1)
				num++;

		}
		return num;
	}

	public static PieDataset createTotalVsDifferentNumberIdentificationPieDataSet(List<IdentificationSet> idSets,
			IdentificationItemEnum plotItem, Boolean distinguishModifiedPeptides, boolean average,
			boolean countNonConclusiveProteins) {
		final DefaultPieDataset dataset = new DefaultPieDataset();

		for (final IdentificationSet identificationSet : idSets) {
			if (plotItem.equals(IdentificationItemEnum.PROTEIN)) {

				double numProteins = 0;

				if (average)
					numProteins = identificationSet
							.getAverageTotalVsDifferentNumProteinGroups(countNonConclusiveProteins);
				else
					numProteins = identificationSet.getTotalVsDifferentNumProteinGroups(countNonConclusiveProteins);

				if (numProteins > 0)
					dataset.setValue(identificationSet.getName(), numProteins);
			} else if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
				double numPeptides = 0;

				if (average)
					numPeptides = identificationSet.getAverageTotalVsDifferentNumPeptides(distinguishModifiedPeptides);
				else
					numPeptides = identificationSet.getTotalVsDifferentNumPeptides(distinguishModifiedPeptides);

				if (numPeptides > 0)
					dataset.setValue(identificationSet.getName(), numPeptides);
			}
		}

		return dataset;
	}

	public static CategoryDataset createAverageProteinCoverageStatisticalCategoryDataSet(List<IdentificationSet> idSets,
			boolean retrieveProteinSeq, Boolean countNonConclusiveProteins) {
		// create the dataset...
		final DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
		try {

			for (final IdentificationSet idSet : idSets) {
				final double[] proteinCoverages = getProteinCoverages(idSet, retrieveProteinSeq,
						countNonConclusiveProteins);
				if (proteinCoverages != null) {
					final Number[] coverageNums = new Number[proteinCoverages.length];
					for (int i = 0; i < proteinCoverages.length; i++) {
						coverageNums[i] = proteinCoverages[i];
					}
					if (proteinCoverages != null && proteinCoverages.length > 0) {
						final double averageCoverage = Statistics.calculateMean(coverageNums);
						final double stdCoverage = Statistics.getStdDev(coverageNums);
						dataset.add(averageCoverage, stdCoverage, idSet.getFullName(), "");
					} else {
						dataset.add(0, 0, idSet.getFullName(), "");
					}
				} else {
					dataset.add(0, 0, idSet.getFullName(), "");
				}

			}

		} catch (final IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static DefaultCategoryDataset createRepeatabilityCategoryDataSet(List<IdentificationSet> idSets,
			IdentificationItemEnum itemType, Boolean distModPeptides, int maximum, Boolean countNonConclusiveProteins) {
		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		try {

			for (final IdentificationSet idSet : idSets) {
				final String experimentName = idSet.getFullName();
				final TIntObjectHashMap<Integer> map = new TIntObjectHashMap<Integer>();

				if (itemType.equals(IdentificationItemEnum.PEPTIDE)) {
					final Map<String, PeptideOccurrence> peptideOccurrences = idSet
							.getPeptideOccurrenceList(distModPeptides);

					for (final PeptideOccurrence occurrence : peptideOccurrences.values()) {

						Integer num = occurrence.getItemList().size();
						if (num > maximum)
							num = maximum;
						if (!map.containsKey(num)) {
							if (num > 0)
								map.put(num, 1);
						} else {
							final Integer newNum = map.get(num) + 1;
							map.remove(num);
							map.put(num, newNum);
						}

					}
				} else if (itemType.equals(IdentificationItemEnum.PROTEIN)) {
					final Collection<ProteinGroupOccurrence> proteinGroupOccurrences = idSet
							.getProteinGroupOccurrenceList().values();

					for (final ProteinGroupOccurrence occurrence : proteinGroupOccurrences) {
						if (occurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
							continue;
						Integer num = occurrence.getItemList().size();
						if (num > maximum)
							num = maximum;
						if (!map.containsKey(num)) {
							if (num > 0)
								map.put(num, 1);
						} else {
							final Integer newNum = map.get(num) + 1;
							map.remove(num);
							map.put(num, newNum);
						}

					}
				}
				final TIntArrayList keys = new TIntArrayList();

				keys.addAll(map.keySet());

				keys.sort();

				int numFound = 0;
				for (int i = 1; i < 100; i++) {
					String orMore = "";
					String times = String.valueOf(i) + " times";
					if (i >= maximum)
						orMore = " or +";
					if (i == 1)
						times = "once";
					if (i == 2)
						times = "twice";
					// for (Integer i : keys) {
					if (map.containsKey(i)) {
						dataset.addValue(map.get(i), "detected " + times + orMore, experimentName);
						numFound++;
					} else if (numFound < keys.size()) {
						dataset.addValue(0, "detected " + times + orMore, experimentName);
					}
					// }
				}

			}

		} catch (final IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static DefaultCategoryDataset createRepeatabilityOverReplicatesCategoryDataSet(
			List<IdentificationSet> idSets, IdentificationItemEnum itemType, Boolean distModPeptides, int maximum,
			Boolean countNonConclusiveProteins) {
		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		try {

			for (final IdentificationSet parentIdSet : idSets) {
				final TIntObjectHashMap<Integer> map = new TIntObjectHashMap<Integer>();
				final List<IdentificationSet> nextLevelIdentificationSetList = parentIdSet
						.getNextLevelIdentificationSetList();
				log.info(parentIdSet.getFullName() + " -> " + nextLevelIdentificationSetList.size());
				if (itemType.equals(IdentificationItemEnum.PROTEIN)) {
					final Collection<ProteinGroupOccurrence> occurrences = parentIdSet.getProteinGroupOccurrenceList()
							.values();
					for (final ProteinGroupOccurrence occurrence : occurrences) {
						if (occurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
							continue;
						int occurrencesOverReplicates = 0;
						try {
							for (final IdentificationSet idSet : nextLevelIdentificationSetList) {
								// System.out.println(occurrence.getKey() +
								// ":"
								// +
								// occurrence.getIdentificationItemList().size());
								boolean isThere = false;

								isThere = idSet.hasProteinGroup(occurrence.getFirstOccurrence());

								if (isThere)
									occurrencesOverReplicates++;

							}
						} catch (final UnsupportedOperationException e) {
							// do nothing
						}
						if (occurrencesOverReplicates > maximum)
							occurrencesOverReplicates = maximum;
						if (!map.containsKey(occurrencesOverReplicates)) {
							if (occurrencesOverReplicates > 0)
								map.put(occurrencesOverReplicates, 1);
						} else {
							final Integer newNum = map.get(occurrencesOverReplicates) + 1;
							map.remove(occurrencesOverReplicates);
							map.put(occurrencesOverReplicates, newNum);
						}

					}
				} else if (itemType.equals(IdentificationItemEnum.PEPTIDE)) {
					final Collection<PeptideOccurrence> occurrences = parentIdSet
							.getPeptideOccurrenceList(distModPeptides).values();
					for (final PeptideOccurrence occurrence : occurrences) {
						int occurrencesOverReplicates = 0;
						try {
							for (final IdentificationSet idSet : nextLevelIdentificationSetList) {
								// System.out.println(occurrence.getKey() + ":"
								// +
								// occurrence.getIdentificationItemList().size());
								boolean isThere = false;

								isThere = idSet.hasPeptide(occurrence.getKey(), distModPeptides);

								if (isThere)
									occurrencesOverReplicates++;

							}
						} catch (final UnsupportedOperationException e) {
							// do nothing
						}

						if (occurrencesOverReplicates > maximum)
							occurrencesOverReplicates = maximum;
						if (!map.containsKey(occurrencesOverReplicates)) {
							if (occurrencesOverReplicates > 0)
								map.put(occurrencesOverReplicates, 1);
						} else {
							final Integer newNum = map.get(occurrencesOverReplicates) + 1;
							map.remove(occurrencesOverReplicates);
							map.put(occurrencesOverReplicates, newNum);
						}
					}
				}

				int numFound = 0;
				for (int i = 1; i < 100; i++) {
					String orMore = "";
					if (i >= maximum)
						orMore = " or +";

					if (map.containsKey(i)) {
						dataset.addValue(map.get(i), "detected in " + String.valueOf(i) + " replicates" + orMore,
								parentIdSet.getFullName());
						numFound++;
					} else if (numFound < map.size()) {
						dataset.addValue(0, "detected in " + String.valueOf(i) + " replicates" + orMore,
								parentIdSet.getFullName());
					}
				}
			}

		} catch (final UnsupportedOperationException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createNumberModificationSitesCategoryDataSet(List<IdentificationSet> idSets,
			String[] modifications) {

		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (final IdentificationSet idSet : idSets) {

				final String experimentName = idSet.getFullName();
				for (final String modification : modifications) {
					final int modificationOccurrence = idSet.getModificatedSiteOccurrence(modification);
					dataset.addValue(modificationOccurrence, experimentName, modification);
				}

			}

		} catch (final IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createNumberModificatedPeptidesCategoryDataSet(List<IdentificationSet> idSets,
			String[] modifications) {

		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (final IdentificationSet idSet : idSets) {

				final String experimentName = idSet.getFullName();
				for (final String modification : modifications) {
					final int modificationOccurrence = idSet.getPeptideModificatedOccurrence(modification);

					dataset.addValue(modificationOccurrence, experimentName, modification);
				}

			}

		} catch (final IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createModificationDistributionCategoryDataSet(List<IdentificationSet> idSets,
			String[] modifications, int maximum) {

		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (final IdentificationSet idSet : idSets) {
				final String experimentName = idSet.getFullName();
				int maximumOccurrence = 0;
				for (final String modification : modifications) {

					final TIntIntHashMap modificationOccurrence = idSet
							.getModificationOccurrenceDistribution(modification);
					if (modificationOccurrence != null && !modificationOccurrence.isEmpty()) {
						final int[] keySet = modificationOccurrence.keys();
						final TIntArrayList keyList = toSortedList(keySet);
						for (final int integer : keyList.toArray()) {
							if (integer >= maximum)
								maximumOccurrence += modificationOccurrence.get(integer);

							else
								dataset.addValue(modificationOccurrence.get(integer), integer + " modif. sites",
										experimentName);
						}
						if (maximumOccurrence > 0)
							dataset.addValue(maximumOccurrence, String.valueOf(maximum) + " or +" + " modif. sites",
									experimentName);
					} else {
						dataset.addValue(0, "1" + " modif. site", experimentName);
					}
				}
			}

		} catch (final IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createMissedCleavagesDistributionCategoryDataSet(List<IdentificationSet> idSets,
			String cleavageAminoacids, int maximum) {
		final int i = 0;

		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (final IdentificationSet idSet : idSets) {
				final String experimentName = idSet.getFullName();
				int maximumOccurrence = 0;
				final TIntIntHashMap missCleavageOccurrence = idSet
						.getMissedCleavagesOccurrenceDistribution(cleavageAminoacids);
				if (missCleavageOccurrence != null && !missCleavageOccurrence.isEmpty()) {
					final int[] keySet = missCleavageOccurrence.keys();
					final TIntArrayList keyList = toSortedList(keySet);
					for (final Integer integer : keyList.toArray()) {
						if (integer >= maximum)
							maximumOccurrence += missCleavageOccurrence.get(integer);

						else
							dataset.addValue(missCleavageOccurrence.get(integer), integer, experimentName);
					}
					if (maximumOccurrence > 0)
						dataset.addValue(maximumOccurrence, String.valueOf(maximum) + " or +", experimentName);
				}
			}

		} catch (final IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createProteinGroupTypesDistributionCategoryDataSet(List<IdentificationSet> idSets) {
		final int i = 0;

		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (final IdentificationSet idSet : idSets) {
				final Map<ProteinEvidence, Integer> proteinEvidenceMap = new THashMap<ProteinEvidence, Integer>();

				final String idSetName = idSet.getFullName();
				final int maximumOccurrence = 0;
				final Map<String, ProteinGroupOccurrence> proteinGroupOccurrences = idSet
						.getProteinGroupOccurrenceList();
				if (proteinGroupOccurrences != null && !proteinGroupOccurrences.isEmpty()) {
					for (final ProteinGroupOccurrence proteinGroupOcc : proteinGroupOccurrences.values()) {
						final ProteinEvidence evidence = proteinGroupOcc.getEvidence();
						if (!proteinEvidenceMap.containsKey(evidence)) {
							proteinEvidenceMap.put(evidence, 1);
						} else {
							proteinEvidenceMap.put(evidence, proteinEvidenceMap.get(evidence) + 1);
						}
					}

				}
				int numConclusive = 0;
				if (proteinEvidenceMap.containsKey(ProteinEvidence.CONCLUSIVE))
					numConclusive = proteinEvidenceMap.get(ProteinEvidence.CONCLUSIVE);
				dataset.addValue(numConclusive, "Conclusive", idSetName);

				int numAmbiguous = 0;
				if (proteinEvidenceMap.containsKey(ProteinEvidence.AMBIGUOUSGROUP))
					numAmbiguous = proteinEvidenceMap.get(ProteinEvidence.AMBIGUOUSGROUP);
				dataset.addValue(numAmbiguous, "Ambiguous", idSetName);

				int numIndistinguible = 0;
				if (proteinEvidenceMap.containsKey(ProteinEvidence.INDISTINGUISHABLE))
					numIndistinguible = proteinEvidenceMap.get(ProteinEvidence.INDISTINGUISHABLE);
				dataset.addValue(numIndistinguible, "Indistinguishable", idSetName);

				int numNonConclusive = 0;
				if (proteinEvidenceMap.containsKey(ProteinEvidence.NONCONCLUSIVE))
					numNonConclusive = proteinEvidenceMap.get(ProteinEvidence.NONCONCLUSIVE);
				dataset.addValue(numNonConclusive, "Non-Conclusive", idSetName);

			}

		} catch (final IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createPeptideMonitoringCategoryDataSet(List<IdentificationSet> idSets,
			List<String> peps, Boolean distinguishModPep) {

		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (final IdentificationSet idSet : idSets) {
				final String experimentName = idSet.getFullName();
				for (final String originalSequence : peps) {
					final int occurrence = idSet.getPeptideChargeOccurrenceNumber(originalSequence, distinguishModPep);
					if (occurrence > 0)
						dataset.addValue(occurrence, experimentName, originalSequence);

				}

			}

		} catch (final IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	/**
	 * The first element is the row, that is, the protein accession the second
	 * element is the occurrence of that protein over each replicate
	 *
	 * @param idSets
	 *
	 * @return
	 */
	public static double[][] createHeapMapDataSet(IdentificationSet parentIdSet, List<IdentificationSet> idSets,
			List<String> rowList, List<String> columnList, IdentificationItemEnum plotItem,
			Boolean distiguishModificatedPeptides, int minOccurrenceThreshold, Double min, Double max,
			Boolean countNonConclusiveProteins) {

		if (rowList == null)
			rowList = new ArrayList<String>();

		if (columnList == null)
			columnList = new ArrayList<String>();
		double[][] dataset = null;
		double[][] ret = null;
		boolean someValueIsMoreThanZero = false;
		if (plotItem.equals(IdentificationItemEnum.PROTEIN)) {
			final Collection<ProteinGroupOccurrence> proteinOccurrenceSet = parentIdSet.getProteinGroupOccurrenceList()
					.values();
			final List<ProteinGroupOccurrence> proteinOccurrenceList = new ArrayList<ProteinGroupOccurrence>();
			for (final ProteinGroupOccurrence proteinGroupOccurrence : proteinOccurrenceSet) {
				proteinOccurrenceList.add(proteinGroupOccurrence);
			}
			dataset = new double[parentIdSet.getNumDifferentProteinGroups(countNonConclusiveProteins)][idSets.size()];
			int proteinIndex = 0;
			// sort from high occurrence to low occurrence
			SorterUtil.sortProteinGroupOcurrencesByOccurrence(proteinOccurrenceList);

			for (final ProteinGroupOccurrence po : proteinOccurrenceList) {
				if (po.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
					continue;

				log.info(po.getItemList().size());
				int identSetIndex = 0;
				final String key = po.getAccessionsString();
				rowList.add(key);
				int numOccurrence = 0;
				for (final IdentificationSet nextLevelIdentSet : idSets) {
					final int proteinOcurrence = nextLevelIdentSet
							.getProteinGroupOccurrenceNumber(po.getFirstOccurrence());
					if (proteinOcurrence > 0)
						someValueIsMoreThanZero = true;
					final String columnName = nextLevelIdentSet.getName() + " / "
							+ nextLevelIdentSet.getDataManager().getExperimentName();
					if (!columnList.contains(columnName))
						columnList.add(columnName);
					numOccurrence += proteinOcurrence;
					dataset[proteinIndex][identSetIndex] = proteinOcurrence;
					identSetIndex++;
				}
				if (numOccurrence < minOccurrenceThreshold)
					break;
				proteinIndex++;

			}
		} else if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
			final Collection<PeptideOccurrence> peptideOccurrenceCollection = parentIdSet
					.getPeptideOccurrenceList(distiguishModificatedPeptides).values();
			final List<PeptideOccurrence> peptideOccurrenceList = new ArrayList<PeptideOccurrence>();
			for (final PeptideOccurrence occurrence : peptideOccurrenceCollection) {
				peptideOccurrenceList.add(occurrence);
			}
			// sort from high occurrence to low occurrence
			SorterUtil.sortPeptideOcurrencesByOccurrence(peptideOccurrenceList);
			log.info("Peptide occurrence list with " + peptideOccurrenceList.size() + " elements");
			dataset = new double[peptideOccurrenceList.size()][idSets.size()];
			int peptideIndex = 0;

			for (final PeptideOccurrence po : peptideOccurrenceList) {
				int identSetIndex = 0;
				final String key = po.getKey();
				// if (distiguishModificatedPeptides) {
				// String modifiedSequence =
				// po.getIdentificationItemList().get(0)
				// .getModificationString();
				// rowList.add(modifiedSequence);
				// } else {
				// rowList.add(sequence);
				// }
				rowList.add(key);
				int numOccurrence = 0;
				for (final IdentificationSet nextLevelIdentSet : idSets) {
					final int peptideOccurrence = nextLevelIdentSet.getPeptideOccurrenceNumber(key,
							distiguishModificatedPeptides);
					if (peptideOccurrence > 0)
						someValueIsMoreThanZero = true;
					final String columnName = nextLevelIdentSet.getName();
					if (!columnList.contains(columnName))
						columnList.add(columnName);
					dataset[peptideIndex][identSetIndex] = peptideOccurrence;

					numOccurrence += peptideOccurrence;
					identSetIndex++;
				}
				// System.out.println(dataset[peptideIndex][0] + " " +
				// dataset[peptideIndex][1]);
				if (numOccurrence < minOccurrenceThreshold) {
					// rowList.remove(rowList.size());
					break;
				}
				peptideIndex++;
			}
		}
		if (!someValueIsMoreThanZero)
			throw new IllegalMiapeArgumentException("There is not data to show");
		ret = new double[rowList.size()][columnList.size()];
		int i = 0;
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
		for (final String rowName : rowList) {
			int j = 0;
			for (final String colName : columnList) {
				final double d = dataset[i][j];
				if (d < min)
					min = d;
				if (d > max)
					max = d;
				ret[i][j] = d;
				j++;
			}
			i++;
		}
		return ret;
	}

	/**
	 *
	 * @param parentIdSet
	 * @param idSets
	 * @param rowList
	 * @param columnList
	 * @param peptideSequenceOrder
	 * @param plotItem
	 * @param distiguishModificatedPeptides
	 * @param minOccurrenceThreshold
	 * @param countNonConclusiveProteins
	 * @param peptidesPerProtein            only applicable in case of proteins
	 * @return
	 */
	public static double[][] createPSMsPerPeptidesHeapMapDataSet(IdentificationSet parentIdSet,
			List<IdentificationSet> idSets, List<String> rowList, List<String> columnList,
			List<String> peptideSequenceOrder, Boolean distiguishModificatedPeptides, int minOccurrenceThreshold) {

		if (rowList == null)
			rowList = new ArrayList<String>();

		if (columnList == null)
			columnList = new ArrayList<String>();
		double[][] dataset = null;

		boolean someValueIsMoreThanZero = false;
		final TIntObjectHashMap<List<String>> occurrenceRankingPep = new TIntObjectHashMap<List<String>>();
		// Firstly, to iterate over parent elements and fill the
		// occurrenceRanking, where the key is the number of idSets that
		// contains the item<br>
		// Then, from 1 to numberOfIdSets, build the heatmap
		final Collection<PeptideOccurrence> peptideOccurrenceSet = parentIdSet
				.getPeptideOccurrenceList(distiguishModificatedPeptides).values();
		dataset = new double[peptideOccurrenceSet.size()][idSets.size()];
		for (final PeptideOccurrence peptideOccurrence : peptideOccurrenceSet) {
			int present = 0;
			for (final IdentificationSet idSet : idSets) {
				present += idSet.getNumPSMsForAPeptide(peptideOccurrence.getKey());
			}
			if (present > 0 && present >= minOccurrenceThreshold)
				if (!occurrenceRankingPep.containsKey(present)) {
					final List<String> list = new ArrayList<String>();
					list.add(peptideOccurrence.getKey());
					occurrenceRankingPep.put(present, list);
				} else {
					occurrenceRankingPep.get(present).add(peptideOccurrence.getKey());
				}
		}
		int peptideIndex = 0;
		for (int numPresent = idSets.size(); numPresent >= 1; numPresent--) {
			final List<String> peptideKeys = occurrenceRankingPep.get(numPresent);
			if (peptideKeys != null)
				for (final String peptideKey : peptideKeys) {
					int identSetIndex = 0;
					rowList.add(peptideKey);
					int numOccurrence = 0;
					for (final IdentificationSet idSet : idSets) {
						final int numPSMs = idSet.getNumPSMsForAPeptide(peptideKey);
						if (numPSMs > 0)
							someValueIsMoreThanZero = true;
						final String columnName = idSet.getFullName();
						if (!columnList.contains(columnName))
							columnList.add(columnName);
						numOccurrence += numPSMs;
						dataset[peptideIndex][identSetIndex] = numPSMs;
						identSetIndex++;
					}
					peptideIndex++;
				}
		}

		if (!someValueIsMoreThanZero)
			throw new IllegalMiapeArgumentException(
					"<html>There is not data to show.<br>Try to lower the number in 'Do not paint rows with less than' text box,<br> which in this case means the number of psms per peptide across all datasets.</html>");
		final double[][] ret = new double[rowList.size()][columnList.size()];
		for (int row = 0; row < rowList.size(); row++) {
			for (int column = 0; column < columnList.size(); column++) {
				ret[row][column] = dataset[row][column];
			}
		}

		// if there is a proteinACC filter, then, return just the data from the
		// proteins of the filter and in the appropriate order
		if (peptideSequenceOrder != null && !peptideSequenceOrder.isEmpty()) {
			final double[][] newRet = getNewSortedData(peptideSequenceOrder, ret, rowList, columnList);
			rowList.clear();
			rowList.addAll(peptideSequenceOrder);
			return newRet;
		}
		return ret;
	}

	/**
	 *
	 * @param parentIdSet
	 * @param idSets
	 * @param rowList
	 * @param columnList
	 * @param peptideSequenceOrder
	 * @param plotItem
	 * @param distiguishModificatedPeptides
	 * @param minOccurrenceThreshold
	 * @param countNonConclusiveProteins
	 * @param peptidesPerProtein            only applicable in case of proteins
	 * @return
	 */
	public static double[][] createPeptideOccurrenceHeapMapDataSet(IdentificationSet parentIdSet,
			List<IdentificationSet> idSets, List<String> rowList, List<String> columnList,
			List<String> peptideSequenceOrder, Boolean distiguishModificatedPeptides, int minOccurrenceThreshold) {

		if (rowList == null)
			rowList = new ArrayList<String>();

		if (columnList == null)
			columnList = new ArrayList<String>();
		double[][] dataset = null;

		boolean someValueIsMoreThanZero = false;
		final TIntObjectHashMap<List<String>> occurrenceRankingPep = new TIntObjectHashMap<List<String>>();
		// Firstly, to iterate over parent elements and fill the
		// occurrenceRanking, where the key is the number of idSets that
		// contains the item<br>
		// Then, from 1 to numberOfIdSets, build the heatmap
		final Collection<PeptideOccurrence> peptideOccurrenceSet = parentIdSet
				.getPeptideOccurrenceList(distiguishModificatedPeptides).values();
		dataset = new double[peptideOccurrenceSet.size()][idSets.size()];
		int maxOccurrence = 0;
		for (final PeptideOccurrence peptideOccurrence : peptideOccurrenceSet) {
			int present = 0;
			for (final IdentificationSet idSet : idSets) {
				final int peptideOccurrenceNumber = idSet.getPeptideOccurrenceNumber(
						peptideOccurrence.getFirstOccurrence().getSequence(), distiguishModificatedPeptides);
				present += peptideOccurrenceNumber;
			}
			if (present > 0 && present >= minOccurrenceThreshold) {
				if (maxOccurrence < present) {
					maxOccurrence = present;
				}
				if (!occurrenceRankingPep.containsKey(present)) {
					final List<String> list = new ArrayList<String>();
					list.add(peptideOccurrence.getKey());
					occurrenceRankingPep.put(present, list);
				} else {
					occurrenceRankingPep.get(present).add(peptideOccurrence.getKey());
				}
			}
		}
		int peptideIndex = 0;
		for (int numPresent = maxOccurrence; numPresent >= 0; numPresent--) {
			final List<String> peptideKeys = occurrenceRankingPep.get(numPresent);
			if (peptideKeys != null)
				for (final String peptideKey : peptideKeys) {
					int identSetIndex = 0;
					rowList.add(peptideKey);
					for (final IdentificationSet idSet : idSets) {
						final int peptideOcurrence = idSet.getPeptideOccurrenceNumber(peptideKey,
								distiguishModificatedPeptides);
						if (peptideOcurrence > 0) {
							someValueIsMoreThanZero = true;
						}
						final String columnName = idSet.getFullName();
						if (!columnList.contains(columnName)) {
							columnList.add(columnName);
						}
						dataset[peptideIndex][identSetIndex] = peptideOcurrence;
						identSetIndex++;
					}
					peptideIndex++;
				}
		}

		if (!someValueIsMoreThanZero)
			throw new IllegalMiapeArgumentException("There is not data to show");
		final double[][] ret = new double[rowList.size()][columnList.size()];
		for (int row = 0; row < rowList.size(); row++) {
			for (int column = 0; column < columnList.size(); column++) {
				ret[row][column] = dataset[row][column];
			}
		}

		// if there is a proteinACC filter, then, return just the data from the
		// proteins of the filter and in the appropriate order
		if (peptideSequenceOrder != null && !peptideSequenceOrder.isEmpty()) {
			final double[][] newRet = getNewSortedData(peptideSequenceOrder, ret, rowList, columnList);
			rowList.clear();
			rowList.addAll(peptideSequenceOrder);
			return newRet;
		}
		return ret;
	}

	/**
	 *
	 * @param parentIdSet
	 * @param idSets
	 * @param rowList
	 * @param columnList
	 * @param proteinACCOrder
	 * @param plotItem
	 * @param distiguishModificatedPeptides
	 * @param minOccurrenceThreshold
	 * @param countNonConclusiveProteins
	 * @param peptidesPerProtein            only applicable in case of proteins
	 * @return
	 */
	public static double[][] createProteinOccurrenceHeapMapDataSet(IdentificationSet parentIdSet,
			List<IdentificationSet> idSets, List<String> rowList, List<String> columnList, List<String> proteinACCOrder,
			int minOccurrenceThreshold, Boolean countNonConclusiveProteins) {

		if (rowList == null)
			rowList = new ArrayList<String>();

		if (columnList == null)
			columnList = new ArrayList<String>();
		double[][] dataset = null;

		boolean someValueIsMoreThanZero = false;
		final TIntObjectHashMap<List<String>> occurrenceRankingPep = new TIntObjectHashMap<List<String>>();
		// Firstly, to iterate over parent elements and fill the
		// occurrenceRanking, where the key is the number of idSets that
		// contains the item<br>
		// Then, from 1 to numberOfIdSets, build the heatmap
		final TIntObjectHashMap<List<ProteinGroup>> occurrenceRankingProt = new TIntObjectHashMap<List<ProteinGroup>>();

		final Collection<ProteinGroupOccurrence> proteinOccurrenceSet = parentIdSet.getProteinGroupOccurrenceList()
				.values();
		dataset = new double[proteinOccurrenceSet.size()][idSets.size()];

		for (final ProteinGroupOccurrence proteinGroupOccurrence : proteinOccurrenceSet) {

			if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;
			int present = 0;
			for (final IdentificationSet idSet : idSets) {
				if (idSet.getProteinGroupOccurrenceNumber(proteinGroupOccurrence.getFirstOccurrence()) > 0) {
					someValueIsMoreThanZero = true;
					present++;
				}
			}
			if (present > 0 && present >= minOccurrenceThreshold)
				if (!occurrenceRankingProt.containsKey(present)) {
					final List<ProteinGroup> list = new ArrayList<ProteinGroup>();
					list.add(proteinGroupOccurrence.getFirstOccurrence());
					occurrenceRankingProt.put(present, list);
				} else {
					occurrenceRankingProt.get(present).add(proteinGroupOccurrence.getFirstOccurrence());
				}

		}
		int proteinIndex = 0;
		for (int numPresent = idSets.size(); numPresent >= 0; numPresent--) {
			final List<ProteinGroup> proteinGroups = occurrenceRankingProt.get(numPresent);
			if (proteinGroups != null)
				for (final ProteinGroup proteinGroup : proteinGroups) {

					int identSetIndex = 0;
					final List<String> accs = proteinGroup.getAccessions();
					String key = "";
					for (final String acc : accs) {
						if (!"".equals(key))
							key += ",";
						key += acc;
					}
					rowList.add(key);
					for (final IdentificationSet idSet : idSets) {
						final int proteinOcurrence = idSet.getProteinGroupOccurrenceNumber(proteinGroup);
						if (proteinOcurrence > 0)
							someValueIsMoreThanZero = true;
						final String columnName = idSet.getFullName();
						if (!columnList.contains(columnName))
							columnList.add(columnName);
						dataset[proteinIndex][identSetIndex] = proteinOcurrence;

						// dataset[proteinIndex][identSetIndex] = 1;
						identSetIndex++;
					}
					proteinIndex++;
				}
		}

		if (!someValueIsMoreThanZero) {
			throw new IllegalMiapeArgumentException("There is not data to show");
		}
		if (rowList.isEmpty() || columnList.isEmpty()) {
			throw new IllegalMiapeArgumentException(
					"<html>There is not data to show.<br> Check the occurrence filter and write a lower value.</html>");
		}
		final double[][] ret = new double[rowList.size()][columnList.size()];
		for (int row = 0; row < rowList.size(); row++) {
			for (int column = 0; column < columnList.size(); column++) {
				ret[row][column] = dataset[row][column];
			}
		}
		// TODO put this variable in method parameters
		final boolean sortAsACCFilter = true;
		// if there is a proteinACC filter, then, return just the data from the
		// proteins of the filter and in the appropriate order
		if (sortAsACCFilter) {
			if (proteinACCOrder != null && !proteinACCOrder.isEmpty()) {
				final double[][] newRet = getNewSortedData(proteinACCOrder, ret, rowList, columnList);
				rowList.clear();
				rowList.addAll(proteinACCOrder);
				return newRet;
			}
		}
		return ret;
	}

	/**
	 *
	 * @param parentIdSet
	 * @param idSets
	 * @param rowList
	 * @param columnList
	 * @param plotItem
	 * @param distiguishModificatedPeptides
	 * @param minThreshold
	 * @param countNonConclusiveProteins
	 * @param peptidesPerProtein            only applicable in case of proteins
	 * @return
	 */
	public static double[][] createPeptidesPerProteinHeapMapDataSet(IdentificationSet parentIdSet,
			List<IdentificationSet> idSets, List<String> rowList, List<String> columnList, List<String> proteinACCOrder,
			Boolean distiguishModificatedPeptides, int minThreshold, Boolean countNonConclusiveProteins,
			boolean isPSM) {

		if (rowList == null)
			rowList = new ArrayList<String>();

		if (columnList == null)
			columnList = new ArrayList<String>();
		double[][] dataset = null;

		boolean someValueIsMoreThanZero = false;
		// Firstly, to iterate over parent elements and fill the
		// occurrenceRanking, where the key is the number of idSets that
		// contains the item<br>
		// Then, from 1 to numberOfIdSets, build the heatmap
		// ranking the proteins by the number of peptides they have
		final TIntObjectHashMap<List<ProteinGroup>> peptideNumberRankingProt = new TIntObjectHashMap<List<ProteinGroup>>();
		log.info("Creating peptides per protein heatmap: PSM=" + isPSM);
		final Collection<ProteinGroupOccurrence> proteinOccurrenceSet = parentIdSet.getProteinGroupOccurrenceList()
				.values();
		dataset = new double[proteinOccurrenceSet.size()][idSets.size()];
		int maxNumberOfPeptidesPerProtein = 0;
		for (final ProteinGroupOccurrence proteinGroupOccurrence : proteinOccurrenceSet) {

			if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;
			int totalPeptidesForThatProtein = 0;
			for (final IdentificationSet idSet : idSets) {
				final ProteinGroupOccurrence proteinGroupOccurrenceIdSet = idSet
						.getProteinGroupOccurrence(proteinGroupOccurrence.getFirstOccurrence());
				if (proteinGroupOccurrenceIdSet != null && proteinGroupOccurrence.getPeptides() != null) {
					someValueIsMoreThanZero = true;
					int size = 0;
					if (isPSM) {
						size = proteinGroupOccurrence.getPeptides().size();
					} else {
						size = DataManager.createPeptideOccurrenceList(proteinGroupOccurrence.getPeptides(),
								distiguishModificatedPeptides).size();
					}
					totalPeptidesForThatProtein += size;
				}
			}
			if (totalPeptidesForThatProtein < minThreshold) {
				continue;
			}
			if (totalPeptidesForThatProtein > 0 && totalPeptidesForThatProtein >= minThreshold) {
				if (maxNumberOfPeptidesPerProtein < totalPeptidesForThatProtein) {
					maxNumberOfPeptidesPerProtein = totalPeptidesForThatProtein;
				}
			}
			if (!peptideNumberRankingProt.containsKey(totalPeptidesForThatProtein)) {
				final List<ProteinGroup> list = new ArrayList<ProteinGroup>();
				list.add(proteinGroupOccurrence.getFirstOccurrence());
				peptideNumberRankingProt.put(totalPeptidesForThatProtein, list);
			} else {
				peptideNumberRankingProt.get(totalPeptidesForThatProtein)
						.add(proteinGroupOccurrence.getFirstOccurrence());
			}

		}
		int proteinIndex = 0;
		for (int peptidesPerprotein = maxNumberOfPeptidesPerProtein; peptidesPerprotein >= 0; peptidesPerprotein--) {
			final List<ProteinGroup> proteinGroups = peptideNumberRankingProt.get(peptidesPerprotein);
			if (proteinGroups != null) {
				for (final ProteinGroup proteinGroup : proteinGroups) {
					int identSetIndex = 0;
					rowList.add(proteinGroup.getAccessionsString());
					for (final IdentificationSet idSet : idSets) {
						final ProteinGroupOccurrence proteinGroupOccurrence = idSet
								.getProteinGroupOccurrence(proteinGroup);
						int peptidesPerThisProtein = 0;
						if (proteinGroupOccurrence != null) {
							if (isPSM) {
								peptidesPerThisProtein = proteinGroupOccurrence.getPeptides().size();
							} else {
								peptidesPerThisProtein = DataManager.createPeptideOccurrenceList(
										proteinGroupOccurrence.getPeptides(), distiguishModificatedPeptides).size();
							}
							if (peptidesPerThisProtein > 0)
								someValueIsMoreThanZero = true;

						}
						final String columnName = idSet.getFullName();
						if (!columnList.contains(columnName)) {
							columnList.add(columnName);
						}
						dataset[proteinIndex][identSetIndex] = peptidesPerThisProtein;
						identSetIndex++;
					}
					proteinIndex++;
				}
			}
		}

		if (!someValueIsMoreThanZero)
			throw new IllegalMiapeArgumentException("There is not data to show");
		final double[][] ret = new double[rowList.size()][columnList.size()];
		for (int row = 0; row < rowList.size(); row++) {
			for (int column = 0; column < columnList.size(); column++) {
				ret[row][column] = dataset[row][column];
			}
		}

		// if there is a proteinACC filter, then, return just the data from the
		// proteins of the filter and in the appropriate order
		if (proteinACCOrder != null && !proteinACCOrder.isEmpty()) {
			final double[][] newRet = getNewSortedData(proteinACCOrder, ret, rowList, columnList);
			rowList.clear();
			rowList.addAll(proteinACCOrder);
			return newRet;
		}
		return ret;
	}

	private static double[][] getNewSortedData(List<String> newOrder, double[][] ret, List<String> rowList,
			List<String> columnList) {
		final double[][] newRet = new double[newOrder.size()][columnList.size()];
		int newRow = 0;

		// search each new proteinKey in the ret array and put it in the new
		// one
		for (final String proteinKey : newOrder) {

			for (int row = 0; row < rowList.size(); row++) {
				if (rowList.get(row).equals(proteinKey)) {
					for (int column = 0; column < columnList.size(); column++) {
						newRet[newRow][column] = ret[row][column];
					}
				}
			}
			newRow++;
		}
		return newRet;
	}

	/**
	 * The first element is the row, that is, the protein accession the second
	 * element is the occurrence of that protein over each replicate
	 *
	 * @param idSets
	 * @param binary : if true, the heatmap will have only two values (1 or 0)
	 *               depending on if the peptide is present or not. If false, the
	 *               number of each cell will be the number of occurrences of each
	 *               peptide
	 * @return
	 */
	public static double[][] createPeptidePresencyHeapMapDataSet(List<IdentificationSet> idSets, List<String> sequences,
			Boolean distinguishModificatedPeptides, boolean binary) {

		// one columns per idSet and one row per peptide string in rowList
		final double[][] dataset = new double[sequences.size()][idSets.size()];
		boolean atLeastOneData = false;
		int numRow = 0;
		for (final String peptideString : sequences) {
			int numColumn = 0;
			for (final IdentificationSet idSet : idSets) {
				final int peptideOccurrence = idSet.getPeptideOccurrenceNumber(peptideString,
						distinguishModificatedPeptides);
				if (binary) {
					if (peptideOccurrence == 0)
						dataset[numRow][numColumn] = 0;
					else {
						dataset[numRow][numColumn] = 1;
						atLeastOneData = true;
					}
				} else {
					dataset[numRow][numColumn] = peptideOccurrence;
					if (peptideOccurrence > 0)
						atLeastOneData = true;
				}

				numColumn++;
			}
			numRow++;
		}
		if (!atLeastOneData)
			throw new IllegalMiapeArgumentException(
					"<html>No data available. If you have included modificated peptides, please,<br>be sure of enabling the 'distinguish mod. and unmod. peptides' checkbox</html>");

		return dataset;
	}

	public static HistogramDataset createScoreHistogramDataSet(List<IdentificationSet> idSets, String scoreName,
			IdentificationItemEnum plotItem, int bins, boolean addZeroZeroValue, HistogramType histogramType,
			boolean applyLog, boolean separateDecoyHits, Boolean countNonConclusiveProteins, boolean psmsOrPeptides,
			boolean distinguishModPep) {
		final HistogramDataset dataset = new HistogramDataset();

		for (final IdentificationSet idSet : idSets) {
			List<double[]> values = null;
			if (plotItem.equals(IdentificationItemEnum.PROTEIN)) {
				values = getProteinScores(idSet, scoreName, addZeroZeroValue, applyLog, separateDecoyHits,
						countNonConclusiveProteins);
			} else if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
				values = getPeptideScores(idSet, scoreName, addZeroZeroValue, applyLog, separateDecoyHits,
						psmsOrPeptides, distinguishModPep);
			}
			if (values != null) {
				for (int i = 0; i < values.size(); i++) {
					final double[] ds = values.get(i);
					String name = idSet.getName();
					if (i == 1)
						name = name + "(decoy)";
					if (ds.length > 0)
						dataset.addSeries(name, ds, bins);
				}
			}

		}
		dataset.setType(histogramType);
		return dataset;
	}

	/**
	 * Create a dataset with the coverages of the proteins. One serie per
	 * {@link IdentificationSet} in the list.
	 *
	 * @param idSets
	 * @param bins
	 * @param histogramType
	 * @return
	 */
	public static HistogramDataset createProteinCoverageHistogramDataSet(List<IdentificationSet> idSets, int bins,
			HistogramType histogramType, boolean retrieveProteinSeq, Boolean countNonConclusiveProteins) {
		final HistogramDataset dataset = new HistogramDataset();

		for (final IdentificationSet idSet : idSets) {
			final double[] values = getProteinCoverages(idSet, retrieveProteinSeq, countNonConclusiveProteins);
			if (values != null && values.length > 0)
				dataset.addSeries(idSet.getFullName(), values, bins);
		}
		dataset.setType(histogramType);
		return dataset;
	}

	public static HistogramDataset createNumPeptidesPerProteinMassDistribution(List<IdentificationSet> idSets, int bins,
			HistogramType histogramType, boolean retrieveFromInternet, Boolean countNonConclusiveProteins) {
		final HistogramDataset dataset = new HistogramDataset();
		boolean someDataset = false;
		for (final IdentificationSet idSet : idSets) {
			final double[] values = getNumPeptidesPerProteinMass(idSet, retrieveFromInternet,
					countNonConclusiveProteins);

			if (values != null && values.length > 0) {
				dataset.addSeries(idSet.getFullName(), values, bins);
				someDataset = true;
			}
		}
		if (!someDataset) {
			throw new IllegalMiapeArgumentException(
					"<html>No data to show.<br>Please be sure that the proteins in your datasets have Uniprot accessions in order to be retrieved in the internet</html>");
		}
		dataset.setType(histogramType);
		return dataset;
	}

	public static CategoryDataset createNumPeptidesPerProteinMassHistogram(List<IdentificationSet> idSets, int bins,
			HistogramType histogramType, boolean retrieveFromInternet, Boolean countNonConclusiveProteins) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		for (final IdentificationSet idSet : idSets) {
			retrieveUniprotProteins(idSet);
			final HistogramDataset histogram = new HistogramDataset();

			final List<ProteinGroup> identifiedProteinGroups = idSet.getIdentifiedProteinGroups();
			if (identifiedProteinGroups != null) {
				final double[] massValues = new double[identifiedProteinGroups.size()];
				int i = 0;
				for (final ProteinGroup proteinGroup : identifiedProteinGroups) {
					if (proteinGroup.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
						continue;
					final ExtendedIdentifiedProtein protein = proteinGroup.get(0);
					final Double mass = getMassFromProtein(protein);
					if (mass != null) {
						massValues[i] = mass;
					}
					i++;
				}

				histogram.addSeries(idSet.getName(), massValues, bins);
			}
			for (int numBin = 0; numBin < histogram.getItemCount(1); numBin++) {
				final Number rowKey = histogram.getX(0, numBin);
				// double value=histogram;
				// dataset.setValue(value, rowKey, idSet.getName());
			}
		}
		return dataset;
	}

	private static Double getMassFromProtein(ExtendedIdentifiedProtein protein) {
		final String uniprotAcc = FastaParser.getUniProtACC(protein.getAccession());
		Double mass = protein.getProteinMass();
		if (mass == null && uniprotAcc != null) {
			final Map<String, Entry> annotatedProtein = FileManager.getUniprotProteinLocalRetriever()
					.getAnnotatedProtein(null, uniprotAcc);
			if (annotatedProtein.containsKey(uniprotAcc)) {
				mass = UniprotEntryUtil.getMolecularWeightInDalton(annotatedProtein.get(uniprotAcc));
				protein.setProteinMass(mass);
			}
		}
		// if still is null, try with the protein sequence
		String proteinSequence = protein.getProteinSequence();
		if (mass == null && proteinSequence == null && uniprotAcc != null) {
			proteinSequence = ProteinSequenceRetrieval.getProteinSequence(uniprotAcc, true,
					FileManager.getUniprotProteinLocalRetriever());
		}
		if (mass == null && proteinSequence != null) {
			final Protein prot = new Protein(new AASequenceImpl(proteinSequence));
			mass = prot.getMass();
			protein.setProteinMass(mass);
		}
		return mass;
	}

	private static double[] getNumPeptidesPerProteinMass(IdentificationSet idSet, boolean retrieveFromInternet,
			Boolean countNonConclusiveProteins) {
		final TDoubleArrayList values = new TDoubleArrayList();
		retrieveUniprotProteins(idSet);
		final List<ProteinGroup> identifiedProteinGroups = idSet.getIdentifiedProteinGroups();
		if (identifiedProteinGroups != null) {

			final int i = 0;
			for (final ProteinGroup proteinGroup : identifiedProteinGroups) {
				if (proteinGroup.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
					continue;
				final TIntArrayList numPeptideList = new TIntArrayList();
				for (final ExtendedIdentifiedProtein protein : proteinGroup) {
					final List<ExtendedIdentifiedPeptide> peptides = protein.getPeptides();
					if (peptides != null)
						numPeptideList.add(peptides.size());
				}

				if (numPeptideList != null && !numPeptideList.isEmpty()) {
					final Double numPeptidesPerProtein = 1.0 * numPeptideList.sum() / numPeptideList.size();

					final ExtendedIdentifiedProtein protein = proteinGroup.get(0);
					final String uniprotAcc = FastaParser.getUniProtACC(protein.getAccession());
					final Double mass = getMassFromProtein(protein);
					if (mass != null) {
						// double log10 = Math.log(numPeptidesPerProtein /
						// mass);
						final double log2 = Math.log(numPeptidesPerProtein / mass) / Math.log(2);
						if (Double.isInfinite(log2)) {
							continue;
						}
						// log.info(numPeptidesPerProtein + " " + mass + " "
						// + log10);
						values.add(log2);
					}
				}

			}
		}

		return values.toArray();
	}

	public static HistogramDataset createPeptideMassHistogramDataSet(List<IdentificationSet> idSets, int bins,
			HistogramType histogramType, boolean mOverz) {
		final HistogramDataset dataset = new HistogramDataset();

		for (final IdentificationSet idSet : idSets) {
			final double[] values = getPeptideMasses(idSet, mOverz);
			if (values != null && values.length > 0)
				dataset.addSeries(idSet.getFullName(), values, bins);
		}
		dataset.setType(histogramType);
		return dataset;
	}

	public static DefaultCategoryDataset createPeptideLengthHistogramDataSet(List<IdentificationSet> idSets,
			int minimum, int maximum, boolean showPSMsorPeptides, boolean distinguishModPep) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		final Map<IdentificationSet, TIntObjectHashMap<Integer>> map = new THashMap<IdentificationSet, TIntObjectHashMap<Integer>>();
		for (final IdentificationSet idSet : idSets) {
			final TIntObjectHashMap<Integer> values = getPeptideLengths(idSet, showPSMsorPeptides, distinguishModPep);
			map.put(idSet, values);
		}

		for (final IdentificationSet idSet : idSets) {
			int maxLength = 0;
			int minLength = 0;
			final TIntObjectHashMap<Integer> values = map.get(idSet);
			if (values != null) {
				for (int length = 1; length < 100000; length++) {
					if (values.containsKey(length)) {
						final Integer numPeptidesWithThisLength = values.get(length);
						if (length >= maximum) {
							if (minLength > 0) {
								dataset.addValue(minLength, "<=" + String.valueOf(minimum), idSet.getFullName());
								minLength = 0;
							}
							maxLength += numPeptidesWithThisLength;
						} else if (length <= minimum) {
							minLength += numPeptidesWithThisLength;
						} else {
							if (minLength > 0) {
								dataset.addValue(minLength, "<=" + String.valueOf(minimum), idSet.getFullName());
								minLength = 0;
							}
							dataset.addValue(numPeptidesWithThisLength, String.valueOf(length), idSet.getFullName());
						}
					} else if (length > minimum && length < maximum) {
						dataset.addValue(0, String.valueOf(length), idSet.getFullName());
					}
				}
				if (maxLength > 0) {
					dataset.addValue(maxLength, ">=" + String.valueOf(maximum), idSet.getFullName());
				}

			}
		}
		return dataset;
	}

	private static double[] getPeptideMasses(IdentificationSet idSet, boolean mOverz) {
		double[] ret = null;

		final List<ExtendedIdentifiedPeptide> peptides = idSet.getIdentifiedPeptides();
		if (peptides != null) {
			ret = new double[peptides.size()];
			int i = 0;
			for (final ExtendedIdentifiedPeptide peptide : peptides) {
				try {
					final Float experimentalMassToCharge = peptide.getExperimentalMassToCharge();
					if (experimentalMassToCharge != null) {
						if (mOverz) {
							final Double mz = Double.valueOf(experimentalMassToCharge);
							ret[i] = mz;
						} else {
							try {
								final int charge = Integer.valueOf(peptide.getCharge());
								final Double mz = Double.valueOf(experimentalMassToCharge);
								if (charge > 0)

									ret[i] = mz * Double.valueOf(charge);
							} catch (final NumberFormatException e) {

							}
						}
					} else {
						try {
							if (mOverz) {
								try {
									final int charge = Integer.valueOf(peptide.getCharge());
									final Double mz = Double.valueOf(peptide.getTheoreticMass());
									if (charge > 0)

										ret[i] = mz / Double.valueOf(charge);
								} catch (final NumberFormatException e) {

								}
							} else {
								ret[i] = peptide.getTheoreticMass();

							}
						} catch (final IllegalArgumentException e) {

						}
					}
				} catch (final NumberFormatException ex) {
					// do nothing
				}
				i++;
			}
		}
		return ret;
	}

	private static TIntObjectHashMap<Integer> getPeptideLengths(IdentificationSet idSet, boolean showPSMsorPeptides,
			boolean distinguishModPep) {
		final TIntObjectHashMap<Integer> ret = new TIntObjectHashMap<Integer>();

		if (showPSMsorPeptides) {
			final List<ExtendedIdentifiedPeptide> peptides = idSet.getIdentifiedPeptides();
			if (peptides != null) {
				for (final ExtendedIdentifiedPeptide peptide : peptides) {
					final String peptideSequence = peptide.getSequence();
					if (peptideSequence != null) {

						final int length = peptideSequence.length();
						if (ret.containsKey(length)) {
							ret.put(length, ret.get(length) + 1);
						} else {
							ret.put(length, 1);
						}

					}
				}
			}
		} else {
			final Map<String, PeptideOccurrence> peptideOccurrenceList = idSet
					.getPeptideOccurrenceList(distinguishModPep);
			if (peptideOccurrenceList != null) {
				for (final PeptideOccurrence peptide : peptideOccurrenceList.values()) {
					final String peptideSequence = peptide.getFirstOccurrence().getSequence();
					if (peptideSequence != null) {
						final int length = peptideSequence.length();
						if (ret.containsKey(length)) {
							ret.put(length, ret.get(length) + 1);
						} else {
							ret.put(length, 1);
						}
					}
				}
			}
		}
		return ret;
	}

	public static DefaultCategoryDataset createPeptideChargeHistogramDataSet(List<IdentificationSet> idSets,
			boolean showPSMsorPeptides, boolean distinguishModPep) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		final List<TIntObjectHashMap<Integer>> totalList = new ArrayList<TIntObjectHashMap<Integer>>();
		for (final IdentificationSet idSet : idSets) {
			final int[] charges = getPeptideCharges(idSet, showPSMsorPeptides, distinguishModPep);
			final TIntObjectHashMap<Integer> chargeHash = new TIntObjectHashMap<Integer>();
			for (final int charge : charges) {
				if (charge > 0)
					if (chargeHash.containsKey(charge)) {
						Integer chargeOccurrence = chargeHash.get(charge);
						chargeOccurrence++;
						chargeHash.put(charge, chargeOccurrence);
					} else {
						chargeHash.put(charge, 1);
					}
			}
			final TIntArrayList chargeList = new TIntArrayList();
			chargeList.addAll(chargeHash.keySet());
			chargeList.sort();
			for (final int charge : chargeList.toArray()) {
				dataset.addValue(chargeHash.get(charge), String.valueOf(charge), idSet.getFullName());
			}

		}
		return dataset;
	}

	/**
	 * Returns an array of charges, one per PSM in the dataset
	 * 
	 * @param idSet
	 * @return
	 */
	private static int[] getPeptideCharges(IdentificationSet idSet, boolean showPSMsorPeptides,
			boolean distinguishModPep) {
		int[] ret = null;

		if (showPSMsorPeptides) {
			final List<ExtendedIdentifiedPeptide> peptides = idSet.getIdentifiedPeptides();
			if (peptides != null) {
				ret = new int[peptides.size()];
				int i = 0;
				for (final ExtendedIdentifiedPeptide peptide : peptides) {
					try {
						final int charge = Integer.valueOf(peptide.getCharge());
						if (charge > 0)
							ret[i] = charge;
					} catch (final NumberFormatException ex) {

					}
					i++;
				}
			}
		} else {
			final Map<String, PeptideOccurrence> peptides = idSet.getPeptideOccurrenceList(distinguishModPep);
			if (peptides != null) {
				ret = new int[peptides.size()];
				int i = 0;
				for (final PeptideOccurrence peptideOcurrence : peptides.values()) {
					final ExtendedIdentifiedPeptide peptide = peptideOcurrence.getFirstOccurrence();
					try {
						final int charge = Integer.valueOf(peptide.getCharge());
						if (charge > 0)
							ret[i] = charge;
					} catch (final NumberFormatException ex) {

					}
					i++;
				}
			}
		}
		return ret;
	}

	private static double[] getProteinCoverages(IdentificationSet idSet, boolean retrieveProteinSeq,
			Boolean countNonConclusiveProteins) {

		final TDoubleArrayList ret = new TDoubleArrayList();
		retrieveUniprotProteins(idSet);
		final Collection<ProteinGroupOccurrence> proteinOccurrences = idSet.getProteinGroupOccurrenceList().values();
		if (proteinOccurrences != null && !proteinOccurrences.isEmpty()) {

			int i = 0;
			for (final ProteinGroupOccurrence proteinGroupOccurrence : proteinOccurrences) {
				if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE
						&& !countNonConclusiveProteins)
					continue;
				try {
					final Float meanProteinCoverage = proteinGroupOccurrence.getMeanProteinCoverage(retrieveProteinSeq,
							FileManager.getUniprotProteinLocalRetriever());
					if (meanProteinCoverage != null) {
						ret.add(100 * meanProteinCoverage);
					}
				} catch (final Exception ex) {
					// do nothing
				}
				i++;

			}
		}
		return ret.toArray();
	}

	/**
	 * Retrieves from Uniprot the proteins in the {@link IdentificationSet}. They
	 * will be available for later queries in the cache and index.
	 * 
	 * @param idSet
	 */
	private static void retrieveUniprotProteins(IdentificationSet idSet) {
		// retrieve the information from uniprot first, all at once
		final Set<String> uniprotACCs = new THashSet<String>();
		final Collection<ProteinGroupOccurrence> collection = idSet.getProteinGroupOccurrenceList().values();
		for (final ProteinGroupOccurrence proteinGroupOccurrence : collection) {
			for (final String acc : proteinGroupOccurrence.getAccessions()) {
				final String uniprotACC = FastaParser.getUniProtACC(acc);
				if (uniprotACC != null) {
					uniprotACCs.add(uniprotACC);
				}
			}
		}
		FileManager.getUniprotProteinLocalRetriever().getAnnotatedProteins(null, uniprotACCs);

	}

	private static double max(double[] vals) {
		double max = Double.MIN_VALUE;
		for (final double d : vals) {
			if (d > max)
				max = d;
		}
		return max;
	}

	private static double min(double[] vals) {
		double min = Double.MAX_VALUE;
		for (final double d : vals) {
			if (d < min)
				min = d;
		}
		return min;
	}

	/**
	 * Creates a {@link XYDataset} od the scores
	 *
	 * @param idsets    list of identification datasets
	 * @param scoreName the score name
	 * @param plotItem  peptide or protein
	 * @param applyLog
	 * @param parent    needed to know which is the best score from each
	 *                  identification occurrence
	 * @return
	 */
	public static Pair<XYDataset, MyXYItemLabelGenerator> createScoreXYDataSet(List<IdentificationSet> idsets,
			String scoreName, IdentificationItemEnum plotItem, boolean distinguish, boolean applyLog,
			boolean separateDecoyHits, Boolean countNonConclusiveProteins) {

		final XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		if (idsets.size() < 2)
			throw new IllegalMiapeArgumentException("At least two series are needed to paint this chart");
		boolean thereisData = false;
		int numSeries = 0;
		final Map<String, String> tooltipValues = new THashMap<String, String>();
		for (int i = 0; i < idsets.size(); i++) {
			for (int j = i + 1; j < idsets.size(); j++) {
				final IdentificationSet idSet1 = idsets.get(i);
				final IdentificationSet idSet2 = idsets.get(j);
				final List<XYSeries> series = getXYScoreSeries(numSeries++, idSet1, idSet2, scoreName, plotItem,
						distinguish, applyLog, separateDecoyHits, countNonConclusiveProteins, tooltipValues);
				for (final XYSeries xySeries : series) {
					if (xySeries.getItems() != null && xySeries.getItems().size() > 0)
						thereisData = true;
					xySeriesCollection.addSeries(xySeries);
				}

			}
		}
		if (!thereisData)
			throw new IllegalMiapeArgumentException(
					"<html>There is not data to show.<br>Please, be sure that the datasets contains the score '"
							+ scoreName + "' <br>and that the overlapping is not zero.</html>");
		return new Pair<XYDataset, MyXYItemLabelGenerator>(xySeriesCollection,
				new MyXYItemLabelGenerator(tooltipValues));
	}

	public static Pair<XYDataset, MyXYItemLabelGenerator> createDeltaMzOverMzXYDataSet(List<IdentificationSet> idsets) {

		final XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		final Map<String, String> tooltips = new THashMap<String, String>();
		int numSeries = -1;
		int numItem = -1;
		for (final IdentificationSet identificationSet : idsets) {
			final List<ExtendedIdentifiedPeptide> identifiedPeptides = identificationSet.getIdentifiedPeptides();
			final XYSeries serie = new XYSeries(identificationSet.getName());
			numSeries++;
			for (final ExtendedIdentifiedPeptide extendedIdentifiedPeptide : identifiedPeptides) {
				try {
					final Double experimentalMZ = Double
							.valueOf(extendedIdentifiedPeptide.getExperimentalMassToCharge());
					final Double theoreticalMZ = Double.valueOf(extendedIdentifiedPeptide.getCalculatedMassToCharge());
					if (experimentalMZ != 0.0 && theoreticalMZ != 0.0) {
						final Double deltaMZ = (experimentalMZ - theoreticalMZ);
						if (deltaMZ < 1 && deltaMZ > -1) {
							serie.add(experimentalMZ, deltaMZ);
							tooltips.put(MyXYItemLabelGenerator.getKey(numSeries, numItem),
									extendedIdentifiedPeptide.getSequence());
							numItem++;
						}
					}
				} catch (final Exception e) {

				}

			}

			xySeriesCollection.addSeries(serie);

		}

		final Pair<XYDataset, MyXYItemLabelGenerator> pair = new Pair<XYDataset, MyXYItemLabelGenerator>(
				xySeriesCollection, new MyXYItemLabelGenerator(tooltips));
		return pair;
	}

	/**
	 *
	 * @param idSets
	 * @param plotItem
	 * @param filter
	 * @param option   in case of plotItem==PROTEIN, this parameter indicate if a
	 *                 the FDR will take into account the best hit of each protein
	 *                 (YES) or just all the cases (NO) or both (BOTH)
	 * @return
	 */
	public static XYDataset createFDRDataSet(List<IdentificationSet> idSets, boolean showProteinLevel,
			boolean showPeptideLevel, boolean showPSMLevel, Boolean countNonConclusiveProteins) {
		final XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		String error = null;
		if (!showPeptideLevel && !showProteinLevel && !showPSMLevel)
			throw new IllegalMiapeArgumentException("<html>Select either protein, peptide or PSM level</html>");
		for (final IdentificationSet idSet : idSets) {

			final FDRFilter filter = idSet.getFDRFilter();
			if (filter == null) {
				final String errorMessage = "<html>Error:  The FDR cannot be calculated or an FDR filter is not defined:<br>"
						+ "<ul><li>If you have already applied an FDR filter and the global FDR cannot be calculated,<br>"
						+ "you can see the reasons by placing the coursor above the message on left-top of the window:<br> 'Global FDR cannot be calculated'</li>"
						+ "<li>If you have not applied an FDR filter, apply it in order to select the score name as well<br>"
						+ "as the way to select the DECOY hits in which the FDR calculation in going to be based</li></ul></html>";

				throw new IllegalMiapeArgumentException(errorMessage);
			}

			// log.info("calculating FDR from :" + idSet.getFullName());
			final double[] values = null;
			// if they are proteins and we have to paint both redundant and non
			// redundant FDR lines:
			try {
				final boolean autoSort = false;
				final boolean allowDuplicateXValues = true;
				log.info("Getting XYFDRSerie from " + idSet.getFullName());

				if (showPSMLevel) {
					final XYSeries xySeries = new XYSeries(idSet.getFullName() + "(PSM)", autoSort,
							allowDuplicateXValues);
					xySeries.add(0, 0);

					final List<ExtendedIdentifiedPeptide> peptides = idSet.getIdentifiedPeptides();

					SorterUtil.sortPeptidesByPeptideScore(peptides, filter.getSortingParameters().getScoreName(), true);

					long numFWHits = 0; // forward hits
					long numDCHits = 0; // decoy hits
					double previousFDRPoint = 0;
					final int i = 1;
					for (final ExtendedIdentifiedPeptide peptide : peptides) {
						// System.out.println(proteinACC);
						// if (peptideOccurrence.isDecoy(filter)) {
						if (peptide.isDecoy(filter)) {
							numDCHits++;
							peptide.setDecoy(true);
						} else {
							numFWHits++;
							peptide.setDecoy(false);
						}

						final float currentFDR = filter.calculateFDR(numFWHits, numDCHits);
						peptide.setPSMLocalFDR(currentFDR);
						// System.out.println(i++ + "\t"
						// + peptideOccurrence.getBestPeptideScore() + "\t"
						// + numDCHits + "\t" + numFWHits + "\t" + currentFDR
						// + "\t" + peptideOccurrence.getKey());

						// log.info("DC=" + numDCHits + " FW=" + numFWHits +
						// " "
						// + filter.calculateFDR(numFWHits, numDCHits));
						// System.out.println(numFWHits + "-" + numDCHits +
						// " -> " +
						// currentFDR + " -> "
						// + peptideOccurrence.getKey() + " "
						// +
						// peptideOccurrence.getBestScore(filter.getSortingParameters()));

						if (currentFDR <= previousFDRPoint && previousFDRPoint != 0.0) {
							// delete last point
							xySeries.remove(xySeries.getItemCount() - 1);
						}
						previousFDRPoint = currentFDR;
						xySeries.add(currentFDR, numFWHits + numDCHits);

					}
					xySeriesCollection.addSeries(xySeries);
				}
				if (showPeptideLevel) {
					final XYSeries xySeries = new XYSeries(idSet.getFullName() + "(Pep)", autoSort,
							allowDuplicateXValues);
					xySeries.add(0, 0);

					// Collection<PeptideOccurrence> peptideOccurrenceCollection
					// = DataManager
					// .createPeptideOccurrenceList(peptides, false)
					// .values();

					final Collection<PeptideOccurrence> peptideOccurrenceCollection = idSet
							.getPeptideOccurrenceList(false).values();
					final List<PeptideOccurrence> peptideOccurrenceList = new ArrayList<PeptideOccurrence>();
					for (final PeptideOccurrence identificationOccurrence : peptideOccurrenceCollection) {
						peptideOccurrenceList.add(identificationOccurrence);
					}
					SorterUtil.sortPeptideOcurrencesByPeptideScore(peptideOccurrenceList,
							filter.getSortingParameters().getScoreName());

					long numFWHits = 0; // forward hits
					long numDCHits = 0; // decoy hits
					double previousFDRPoint = 0;
					final int i = 1;
					for (final PeptideOccurrence peptideOccurrence : peptideOccurrenceList) {
						final ExtendedIdentifiedPeptide bestPeptideByScore = peptideOccurrence.getBestPeptide();

						// System.out.println(proteinACC);
						// if (peptideOccurrence.isDecoy(filter)) {
						if (bestPeptideByScore.isDecoy(filter)) {
							numDCHits++;
							peptideOccurrence.setDecoy(true);
						} else {
							numFWHits++;
							peptideOccurrence.setDecoy(false);
						}

						final float currentFDR = filter.calculateFDR(numFWHits, numDCHits);
						peptideOccurrence.setPeptideLocalFDR(currentFDR);

						// System.out.println(i++ + "\t"
						// + peptideOccurrence.getBestPeptideScore() + "\t"
						// + numDCHits + "\t" + numFWHits + "\t" + currentFDR
						// + "\t" + peptideOccurrence.getKey());

						// log.info("DC=" + numDCHits + " FW=" + numFWHits +
						// " "
						// + filter.calculateFDR(numFWHits, numDCHits));
						// System.out.println(numFWHits + "-" + numDCHits +
						// " -> " +
						// currentFDR + " -> "
						// + peptideOccurrence.getKey() + " "
						// +
						// peptideOccurrence.getBestScore(filter.getSortingParameters()));

						if (currentFDR <= previousFDRPoint && previousFDRPoint != 0.0) {
							// delete last point
							xySeries.remove(xySeries.getItemCount() - 1);
						}
						previousFDRPoint = currentFDR;
						xySeries.add(currentFDR, numFWHits + numDCHits);

					}
					xySeriesCollection.addSeries(xySeries);
				}
				// PROTEINS:
				if (showProteinLevel) {
					final XYSeries xySeries = new XYSeries(idSet.getFullName() + "(Prot)", autoSort,
							allowDuplicateXValues);
					xySeries.add(0, 0);
					// List<ProteinGroup> proteinGroups = idSet
					// .getIdentifiedProteinGroups();
					// Collection<ProteinGroupOccurrence>
					// proteinGroupOccurrencesSet = DataManager
					// .createProteinGroupOccurrenceList(proteinGroups)
					// .values();

					final Collection<ProteinGroupOccurrence> proteinGroupOccurrencesSet = idSet
							.getProteinGroupOccurrenceList().values();

					final List<ProteinGroupOccurrence> proteinGroupOccurrences = new ArrayList<ProteinGroupOccurrence>();
					for (final ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrencesSet) {
						proteinGroupOccurrences.add(proteinGroupOccurrence);
					}

					SorterUtil.sortProteinGroupOcurrencesByPeptideScore(proteinGroupOccurrences,
							idSet.getFDRFilter().getSortingParameters().getScoreName());

					long numFWHits = 0; // forward hits
					long numDCHits = 0; // decoy hits
					double previousFDRPoint = 0;
					final List<ProteinGroup> proteinGroupList = new ArrayList<ProteinGroup>();
					final int i = 1;
					for (final ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrences) {
						if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE
								&& !countNonConclusiveProteins)
							continue;
						// if
						// (!proteinGroupList.contains(proteinGroupOccurrence))
						// {
						// proteinGroupList.add(proteinGroupOccurrence);
						// if (proteinGroup.getEvidence() !=
						// ProteinEvidence.NONCONCLUSIVE) {
						if (proteinGroupOccurrence.isDecoy(filter)) {
							numDCHits++;
						} else {
							numFWHits++;
						}
						// }
						// } else {
						// log.info("protein group" + proteinGroup
						// + " already present");
						// }

						final float currentFDR = filter.calculateFDR(numFWHits, numDCHits);
						proteinGroupOccurrence.setProteinLocalFDR(currentFDR);
						// log.info(i++ + "\t" + numDCHits + "\t" +
						// numFWHits + "\t"
						// + currentFDR + "\t" + proteinGroup.getKey());
						// System.out.println(numFWHits + "-" + numDCHits +
						// " -> "
						// + filter.calculateFDR(numFWHits, numDCHits)
						// + " -> " + peptideOccurrence.getKey());

						if (currentFDR <= previousFDRPoint && previousFDRPoint != 0.0) {
							// delete last point
							xySeries.remove(xySeries.getItemCount() - 1);
						}
						previousFDRPoint = currentFDR;
						xySeries.add(currentFDR, numFWHits + numDCHits);
					}

					xySeriesCollection.addSeries(xySeries);
				}

			} catch (final IllegalMiapeArgumentException e) {
				error = e.getMessage();
			}
		}
		if (xySeriesCollection.getSeries() == null || xySeriesCollection.getSeries().isEmpty())
			throw new IllegalMiapeArgumentException(error);
		return xySeriesCollection;
	}

	private static List<XYSeries> getXYScoreSeries(int numSeries, IdentificationSet idSet1, IdentificationSet idSet2,
			String scoreName, IdentificationItemEnum plotItem, boolean distinguish, boolean applyLog,
			boolean separateDecoyHits, Boolean countNonConclusiveProteins, Map<String, String> tooltipValues) {

		final XYSeries normalSeries = new XYSeries(idSet1.getName() + " vs " + idSet2.getName());
		XYSeries decoySeries = null;
		if (separateDecoyHits) {
			decoySeries = new XYSeries(idSet1.getName() + " vs " + idSet2.getName() + " (decoy)");
		}

		// Foreach protein in replicate2, look it in the Map and add an XY
		// point to the
		// series
		int numItem = 0;
		if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
			Map<String, PeptideOccurrence> peptideOccurrences1 = idSet1.getPeptideOccurrenceList(distinguish);
			Map<String, PeptideOccurrence> peptideOccurrences2 = idSet2.getPeptideOccurrenceList(distinguish);
			if (separateDecoyHits) {
				peptideOccurrences1 = DataManager
						.createPeptideOccurrenceListInParallel(idSet1.getNonFilteredIdentifiedPeptides(), true);
				peptideOccurrences2 = DataManager
						.createPeptideOccurrenceListInParallel(idSet2.getNonFilteredIdentifiedPeptides(), true);
			}

			for (final PeptideOccurrence occurrence2 : peptideOccurrences2.values()) {

				if (peptideOccurrences1.containsKey(occurrence2.getKey())) {
					try {
						final PeptideOccurrence occurrence1 = peptideOccurrences1.get(occurrence2.getKey());
						Float x = occurrence1.getBestPeptideScore(scoreName);
						if (x != null && applyLog) {
							x = Double.valueOf(Math.log10(x)).floatValue();
							if (Double.isInfinite(x)) {
								continue;
							}
						}
						Float y = occurrence2.getBestPeptideScore(scoreName);
						if (y != null && applyLog) {
							y = Double.valueOf(Math.log10(y)).floatValue();
							if (Double.isInfinite(y)) {
								continue;
							}
						}
						if (x != null && y != null) {
							if (separateDecoyHits && (occurrence1.isDecoy() || occurrence2.isDecoy())) {
								decoySeries.add(x, y);
							} else {
								normalSeries.add(x, y);
							}
							tooltipValues.put(MyXYItemLabelGenerator.getKey(numSeries, numItem++),
									occurrence1.getKey());
						}
					} catch (final IllegalMiapeArgumentException e) {
						// do nothing, not plot it
					}
				}
			}
		} else {
			final Collection<ProteinGroupOccurrence> occurrenceList1 = idSet1.getProteinGroupOccurrenceList().values();
			final Map<String, ProteinGroupOccurrence> occurrenceMap1 = new THashMap<String, ProteinGroupOccurrence>();
			for (final ProteinGroupOccurrence proteinGroupOccurrence : occurrenceList1) {
				occurrenceMap1.put(proteinGroupOccurrence.toString(), proteinGroupOccurrence);
			}
			final Collection<ProteinGroupOccurrence> occurrenceList2 = idSet2.getProteinGroupOccurrenceList().values();
			final Map<String, ProteinGroupOccurrence> occurrenceMap2 = new THashMap<String, ProteinGroupOccurrence>();
			for (final ProteinGroupOccurrence proteinGroupOccurrence : occurrenceList2) {
				occurrenceMap2.put(proteinGroupOccurrence.toString(), proteinGroupOccurrence);
			}

			for (final String key : occurrenceMap2.keySet()) {
				final ProteinGroupOccurrence occurrence2 = occurrenceMap2.get(key);
				if (occurrence2.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
					continue;
				if (occurrenceMap1.containsKey(key)) {
					try {
						final ProteinGroupOccurrence occurrence1 = occurrenceMap1.get(key);
						final Float x = occurrence1.getBestProteinScore(scoreName);
						final Float y = occurrence2.getBestProteinScore(scoreName);
						if (x != null && y != null) {
							if (separateDecoyHits && (occurrence1.isDecoy() || occurrence2.isDecoy())) {
								decoySeries.add(x, y);
							} else {
								normalSeries.add(x, y);
							}
							tooltipValues.put(MyXYItemLabelGenerator.getKey(numSeries, numItem++),
									occurrence1.getAccessionsString());
						}
					} catch (final IllegalMiapeArgumentException e) {
						// do nothing, not plot it
					}
				}
			}
		}

		final List<XYSeries> ret = new ArrayList<XYSeries>();
		ret.add(normalSeries);
		if (separateDecoyHits)
			ret.add(decoySeries);
		return ret;
	}

	/**
	 * @param idSet
	 * @param scoreName
	 * @param addZeroZeroValue
	 * @param applyLog
	 * @param separateDecoyHits
	 * @return
	 */
	private static List<double[]> getProteinScores(IdentificationSet idSet, String scoreName, boolean addZeroZeroValue,
			boolean applyLog, boolean separateDecoyHits, Boolean countNonConclusiveProteins) {

		final TDoubleArrayList scores = new TDoubleArrayList();
		final TDoubleArrayList scoresDecoy = new TDoubleArrayList();
		if (addZeroZeroValue) {
			scores.add(0.0);
			scoresDecoy.add(0.0);
		}
		for (final Object object : idSet.getIdentifiedProteinGroups()) {
			final ProteinGroup proteinGroup = (ProteinGroup) object;
			if (proteinGroup.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;
			final Float score = proteinGroup.getBestProteinScore(scoreName);
			if (score != null) {
				double doubleValue = score.doubleValue();
				if (applyLog) {
					doubleValue = Math.log10(doubleValue);
					if (Double.isInfinite(doubleValue)) {
						continue;
					}
				}
				if (separateDecoyHits && proteinGroup.isDecoy()) {
					scoresDecoy.add(doubleValue);
				} else {
					scores.add(doubleValue);
				}
			}

		}
		final List<double[]> ret = new ArrayList<double[]>();

		ret.add(scores.toArray());
		if (separateDecoyHits) {
			ret.add(scoresDecoy.toArray());
		}
		return ret;
	}

	private static List<double[]> getPeptideScores(IdentificationSet idSet, String scoreName, boolean addZeroZeroValue,
			boolean applyLog, boolean separateDecoyHits, boolean psmsOrPeptides, boolean distinguishModPep) {

		final TDoubleArrayList scores = new TDoubleArrayList();
		final TDoubleArrayList scoresDecoy = new TDoubleArrayList();
		if (addZeroZeroValue) {
			scores.add(0.0);
			scoresDecoy.add(0.0);
		}
		if (psmsOrPeptides) {
			for (final Object object : idSet.getIdentifiedPeptides()) {
				final ExtendedIdentifiedPeptide peptide = (ExtendedIdentifiedPeptide) object;
				try {
					final Float score = peptide.getScore(scoreName);
					if (score != null) {
						double doubleValue = score.doubleValue();
						if (applyLog) {
							doubleValue = Math.log10(doubleValue);
							if (Double.isInfinite(doubleValue)) {
								continue;
							}
						}
						if (separateDecoyHits && peptide.isDecoy()) {
							scoresDecoy.add(doubleValue);
						} else {
							scores.add(doubleValue);
						}
					}
				} catch (final Exception e) {
					// do nothign
				}
			}
		} else {
			final Map<String, PeptideOccurrence> peptideOcurrences = idSet.getPeptideOccurrenceList(distinguishModPep);
			for (final PeptideOccurrence peptideOcurrence : peptideOcurrences.values()) {
				final ExtendedIdentifiedPeptide peptide = peptideOcurrence.getBestPeptide(scoreName);
				try {
					final Float score = peptide.getScore(scoreName);
					if (score != null) {
						double doubleValue = score.doubleValue();
						if (applyLog) {
							doubleValue = Math.log10(doubleValue);
							if (Double.isInfinite(doubleValue)) {
								continue;
							}
						}
						if (separateDecoyHits && peptide.isDecoy()) {
							scoresDecoy.add(doubleValue);
						} else {
							scores.add(doubleValue);
						}
					}
				} catch (final Exception e) {
					// do nothign
				}
			}
		}
		final List<double[]> ret = new ArrayList<double[]>();
		ret.add(scores.toArray());
		if (separateDecoyHits) {
			ret.add(scoresDecoy.toArray());
		}
		return ret;
	}

	public static TIntArrayList toSortedList(TIntHashSet list) {
		if (list == null)
			return null;
		final TIntArrayList ret = new TIntArrayList();
		for (final int integer : list._set) {
			ret.add(integer);
		}
		ret.sort();
		return ret;
	}

	public static TIntArrayList toSortedList(int[] list) {
		if (list == null)
			return null;
		final TIntArrayList ret = new TIntArrayList(list);
		ret.sort();
		return ret;
	}

	public static double arrayAverage(Object[] nums) {
		final TIntArrayList integrs = new TIntArrayList();
		for (final Object num : nums) {
			integrs.add((Integer) num);
		}

		double result = 0.0;

		for (final int integer : integrs.toArray()) {
			result = result + integer;
		}

		result = result / integrs.size();
		log.info("Average is =" + result);
		return result;
	}

	public static String toStringList(Object[] modifications, String separator) {
		String ret = "";
		if (modifications != null && modifications.length > 0) {
			for (final Object element : modifications) {
				if (!"".equals(ret))
					ret = ret + separator;
				ret = ret + element;
			}
		}
		return ret;
	}

	public static List<String> toSortedList(String[] array) {
		log.info("Sorting array");
		final List<String> list = new ArrayList<String>();

		if (array != null && array.length > 0) {
			for (final String string : array) {
				list.add(string);
			}
			Collections.sort(list);

		}
		log.info("Array sorted");
		return list;
	}

	public static String[] toSortedArray(String[] array) {
		final List<String> list = toSortedList(array);
		String[] ret = null;
		if (list != null && !list.isEmpty()) {
			ret = new String[list.size()];
			int i = 0;
			for (final String string : list) {
				ret[i] = string;
				i++;
			}
		}
		return ret;
	}

	public static CategoryDataset createProteinSensitivityCategoryDataSet(List<IdentificationSet> idSets,
			Set<String> proteinsInSample, boolean countNonConclusiveProteins, boolean sensitivity, boolean accuracy,
			boolean specificity, boolean precision, boolean npv, boolean fdr) {
		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String error = null;
		try {

			for (final IdentificationSet idSet : idSets) {
				final String experimentName = idSet.getFullName();
				final int tp = idSet.getProteinGroupTP(proteinsInSample, countNonConclusiveProteins);
				final int fn = idSet.getProteinGroupFN(proteinsInSample, countNonConclusiveProteins);
				final int tn = idSet.getProteinGroupTN(proteinsInSample, countNonConclusiveProteins);
				final int fp = idSet.getProteinGroupFP(proteinsInSample, countNonConclusiveProteins);
				if (sensitivity && (tp + fn) > 0) {
					final double value = 100.0 * tp / Double.valueOf(tp + fn);
					dataset.addValue(value, "sensitivity", experimentName);
				}

				if (accuracy && (tp + tn + fp + fn) > 0) {
					final double value = 100.0 * (tp + tn) / Double.valueOf(tp + tn + fp + fn);
					dataset.addValue(value, "accuracy", experimentName);
				}
				if (specificity && tn + fp > 0) {
					final double value = 100.0 * tn / Double.valueOf(tn + fp);
					dataset.addValue(value, "specifity", experimentName);
				}
				if (precision && (tp + fp) > 0) {
					final double value = 100.0 * Double.valueOf(tp) / Double.valueOf(tp + fp);
					dataset.addValue(value, "precision", experimentName);
				}
				if (npv && (tn + fn) > 0) {
					final double value = 100.0 * Double.valueOf(tn) / Double.valueOf(tn + fn);
					dataset.addValue(value, "npv", experimentName);
				}
				if (fdr && (fp + tp) > 0) {
					final double value = 100.0 * Double.valueOf(fp) / Double.valueOf(fp + tp);
					dataset.addValue(value, "fdr", experimentName);
				}
			}

		} catch (final IllegalMiapeArgumentException ex) {
			log.info("error getting dataset: " + ex.getMessage());
			error = ex.getMessage();
		}
		if (dataset.getRowCount() > 0)
			return dataset;
		throw new IllegalMiapeArgumentException(
				"No dataset can be extracted to plot specificity and sensitivity. Please, review your input data. "
						+ error);
	}

	public static CategoryDataset createPeptideSensitivityCategoryDataSet(List<IdentificationSet> idSets,
			Set<String> peptidesInSample, boolean distinguisModificatedPeptides, boolean sensitivity, boolean accuracy,
			boolean specificity, boolean precision, boolean npv, boolean fdr) {
		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String error = null;
		try {

			for (final IdentificationSet idSet : idSets) {
				final String experimentName = idSet.getFullName();
				final int tp = idSet.getPeptideTP(peptidesInSample, distinguisModificatedPeptides);
				final int fn = idSet.getPeptideFN(peptidesInSample, distinguisModificatedPeptides);
				final int tn = idSet.getPeptideTN(peptidesInSample, distinguisModificatedPeptides);
				final int fp = idSet.getPeptideFP(peptidesInSample, distinguisModificatedPeptides);
				if (sensitivity && (tp + fn) > 0) {
					final double value = tp / Double.valueOf(tp + fn);
					dataset.addValue(value, "sensitivity", experimentName);
				}

				if (accuracy && (tp + tn + fp + fn) > 0) {
					final double value = (tp + tn) / Double.valueOf(tp + tn + fp + fn);
					dataset.addValue(value, "accuracy", experimentName);
				}
				if (specificity && tn + fp > 0) {
					final double value = tn / Double.valueOf(tn + fp);
					dataset.addValue(value, "specifity", experimentName);
				}
				if (precision && (tp + fp) > 0) {
					final double value = Double.valueOf(tp) / Double.valueOf(tp + fp);
					dataset.addValue(value, "precision", experimentName);
				}
				if (npv && (tn + fn) > 0) {
					final double value = Double.valueOf(tn) / Double.valueOf(tn + fn);
					dataset.addValue(value, "npv", experimentName);
				}
				if (fdr && (fp + tp) > 0) {
					final double value = Double.valueOf(fp) / Double.valueOf(fp + tp);
					dataset.addValue(value, "fdr", experimentName);
				}
			}

		} catch (final IllegalMiapeArgumentException ex) {
			log.info("error getting dataset: " + ex.getMessage());
			error = ex.getMessage();
		}
		if (dataset.getRowCount() > 0)
			return dataset;
		throw new IllegalMiapeArgumentException(
				"No dataset can be extracted to plot specificity and sensitivity. Please, review your input data. "
						+ error);
	}

	public static CategoryDataset createAllHumanChromosomePeptideMappingCategoryDataSet(IdentificationSet idSet,
			String peptideOrPSM, boolean onlyOneIdSet, boolean distinguishModPep) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		final Map<String, PeptideOccurrence> peptideOccurrences = idSet.getPeptideOccurrenceList(distinguishModPep);

		int totalNum = 0;
		for (final String chromosomeName : GeneDistributionReader.chromosomeNames) {
			int numPeptides = 0;
			int numPSMs = 0;
			final List<ENSGInfo> genes = new ArrayList<ENSGInfo>();
			final Map<String, List<ENSGInfo>> proteinGeneMapping = GeneDistributionReader.getInstance()
					.getProteinGeneMapping(chromosomeName);
			for (final PeptideOccurrence peptideOccurrence : peptideOccurrences.values()) {
				final Set<ExtendedIdentifiedProtein> proteinList = peptideOccurrence.getProteinList();
				final Set<String> accs = new THashSet<String>();
				for (final ExtendedIdentifiedProtein protein : proteinList) {
					accs.add(protein.getAccession());
				}
				boolean found = false;
				// if any protein of the peptides is in the chromosome, sum the
				// numbers of peptides and PSMs
				for (final String proteinACC : accs) {
					if (proteinGeneMapping.containsKey(proteinACC)) {
						found = true;
						break;
					}
				}
				if (found) {
					numPeptides += 1;
					numPSMs += peptideOccurrence.getItemList().size();
				}
			}

			String key = idSet.getName();
			if (AdditionalOptionsPanelFactory.BOTH.equals(peptideOrPSM))
				key += "-peptides";
			if (onlyOneIdSet)
				key = "peptides";
			if (AdditionalOptionsPanelFactory.BOTH.equals(peptideOrPSM)
					|| AdditionalOptionsPanelFactory.PEPTIDE.equals(peptideOrPSM)) {
				dataset.addValue(numPeptides, key, "Chr" + chromosomeName);
				totalNum += numPeptides;
			}

			key = idSet.getName();
			if (AdditionalOptionsPanelFactory.BOTH.equals(peptideOrPSM))
				key += "-psm";
			if (onlyOneIdSet)
				key = "psm";
			if (AdditionalOptionsPanelFactory.BOTH.equals(peptideOrPSM)
					|| AdditionalOptionsPanelFactory.PSM.equals(peptideOrPSM)) {
				dataset.addValue(numPSMs, key, "Chr" + chromosomeName);
				totalNum += numPSMs;
			}
		}
		if (onlyOneIdSet && totalNum == 0)
			throw new IllegalMiapeArgumentException(
					"<html>No peptides have been mapped to any human chromosome.<br>Note that this mapping is made from uniprot entries<br>so check if your proteins has the uniprot accession or not.</html>");

		return dataset;
	}

	public static PieDataset createAllHumanChromosomePeptideMappingPieDataSet(IdentificationSet idSet,
			String peptideOrPSM, boolean distinguishModPep) {
		final DefaultPieDataset dataset = new DefaultPieDataset();
		final Map<String, PeptideOccurrence> peptideOccurrences = idSet.getPeptideOccurrenceList(distinguishModPep);

		if (AdditionalOptionsPanelFactory.BOTH.equals(peptideOrPSM))
			throw new IllegalMiapeArgumentException(
					"Peptides and PSMs distributions cannot been seen in the same pie chart. Select PEPTIDE or PSM in the combobox.");

		int totalNum = 0;
		for (final String chromosomeName : GeneDistributionReader.chromosomeNames) {
			int numPeptides = 0;
			int numPSMs = 0;
			final List<ENSGInfo> genes = new ArrayList<ENSGInfo>();
			final Map<String, List<ENSGInfo>> proteinGeneMapping = GeneDistributionReader.getInstance()
					.getProteinGeneMapping(chromosomeName);
			for (final PeptideOccurrence peptideOccurrence : peptideOccurrences.values()) {
				final Set<ExtendedIdentifiedProtein> proteinList = peptideOccurrence.getProteinList();
				final Set<String> accs = new THashSet<String>();
				for (final ExtendedIdentifiedProtein protein : proteinList) {
					accs.add(protein.getAccession());
				}
				boolean found = false;
				// if any protein of the peptides is in the chromosome, sum the
				// numbers of peptides and PSMs
				for (final String proteinACC : accs) {
					if (proteinGeneMapping.containsKey(proteinACC)) {
						found = true;
						break;
					}
				}
				if (found) {
					numPeptides += 1;
					numPSMs += peptideOccurrence.getItemList().size();
				}
			}

			if (AdditionalOptionsPanelFactory.PEPTIDE.equals(peptideOrPSM)) {
				dataset.setValue("Chr" + chromosomeName, numPeptides);
				totalNum += numPeptides;
			}

			if (AdditionalOptionsPanelFactory.PSM.equals(peptideOrPSM)) {
				dataset.setValue("Chr" + chromosomeName, numPSMs);
				totalNum += numPSMs;
			}
		}
		if (totalNum == 0)
			throw new IllegalMiapeArgumentException(
					"<html>No peptides have been mapped to any human chromosome.<br>Note that this mapping is made from uniprot entries<br>so check if your proteins has the uniprot accession or not.</html>");

		return dataset;
	}

	public static CategoryDataset createAllHumanChromosomeMappingCategoryDataSet(IdentificationSet idSet,
			String proteinOrGene, boolean onlyOneIdSet, boolean takeGeneFromFirstProteinSelected,
			Boolean countNonConclusiveProteins) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		final Collection<ProteinGroupOccurrence> proteinGroupOccurrences = idSet.getProteinGroupOccurrenceList()
				.values();

		int totalNum = 0;
		for (final String chromosomeName : GeneDistributionReader.chromosomeNames) {
			int numProteins = 0;
			int numGenes = 0;
			final List<ENSGInfo> genes = new ArrayList<ENSGInfo>();
			final Map<String, List<ENSGInfo>> proteinGeneMapping = GeneDistributionReader.getInstance()
					.getProteinGeneMapping(chromosomeName);
			int numProteinsToCount = 0;
			for (final ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrences) {
				if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE
						&& !countNonConclusiveProteins)
					continue;
				numProteinsToCount++;
				if (!takeGeneFromFirstProteinSelected) {
					final List<String> accessions = proteinGroupOccurrence.getAccessions();
					for (final String acc : accessions) {
						if (proteinGeneMapping.containsKey(acc)) {
							numProteins++;
							break;
						}
					}
					final Iterator<String> iterator = proteinGroupOccurrence.getAccessions().iterator();
					while (iterator.hasNext()) {
						final String acc = iterator.next();
						if (proteinGeneMapping.containsKey(acc)) {
							final List<ENSGInfo> list = proteinGeneMapping.get(acc);
							for (final ENSGInfo ensgInfo : list) {
								if (!genes.contains(ensgInfo))
									genes.add(ensgInfo);
							}
						}
					}
				} else {
					final String acc = proteinGroupOccurrence.getAccessions().get(0);
					if (proteinGeneMapping.containsKey(acc)) {
						numProteins++;
					}

					if (proteinGeneMapping.containsKey(acc)) {
						final List<ENSGInfo> list = proteinGeneMapping.get(acc);
						for (final ENSGInfo ensgInfo : list) {
							if (!genes.contains(ensgInfo))
								genes.add(ensgInfo);
						}
					}

				}
			}

			numGenes = genes.size();

			String key = idSet.getName();
			if (AdditionalOptionsPanelFactory.BOTH.equals(proteinOrGene))
				key += "-proteins";
			if (onlyOneIdSet)
				key = "proteins";
			if (AdditionalOptionsPanelFactory.BOTH.equals(proteinOrGene)
					|| AdditionalOptionsPanelFactory.PROTEIN.equals(proteinOrGene)) {
				dataset.addValue(numProteins, key, "Chr" + chromosomeName);
				totalNum += numProteins;
			}

			key = idSet.getName();
			if (AdditionalOptionsPanelFactory.BOTH.equals(proteinOrGene))
				key += "-genes";
			if (onlyOneIdSet)
				key = "genes";
			if (AdditionalOptionsPanelFactory.BOTH.equals(proteinOrGene)
					|| AdditionalOptionsPanelFactory.GENES.equals(proteinOrGene)) {
				dataset.addValue(numGenes, key, "Chr" + chromosomeName);
				totalNum += numGenes;
			}
		}
		if (onlyOneIdSet && totalNum == 0)
			throw new IllegalMiapeArgumentException("<html>No proteins have been mapped to any human chromosome.<br>"
					+ "Note that this mapping is made from uniprot entries<br>"
					+ "so check if your proteins has the uniprot accession and are human proteins.</html>");

		return dataset;
	}

	public static CategoryDataset createAllHumanChromosomeGeneCoverageCategoryDataSet(List<IdentificationSet> idSets,
			boolean spiderPlot, boolean justPercentage, boolean showTotal, boolean takeGeneFromFirstProteinSelected,
			Boolean countNonConclusiveProteins) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		int totalNum = 0;
		for (final IdentificationSet idSet : idSets) {
			boolean totalShown = false;
			final Collection<ProteinGroupOccurrence> proteinGroupOccurrences = idSet.getProteinGroupOccurrenceList()
					.values();

			for (final String chromosomeName : GeneDistributionReader.chromosomeNames) {
				if (chromosomeName.equalsIgnoreCase("y"))
					continue;
				final Collection<ENSGInfo> chromosomeGeneMapping = GeneDistributionReader.getInstance()
						.getENSGIDInAChromosome(chromosomeName);
				if (chromosomeGeneMapping == null) {
					throw new IllegalMiapeArgumentException(
							"Error reading mapping between proteins and chromosome " + chromosomeName);
				}
				final int numProteins = 0;
				final List<ENSGInfo> genes = new ArrayList<ENSGInfo>();
				final Map<String, List<ENSGInfo>> proteinGeneMapping = GeneDistributionReader.getInstance()
						.getProteinGeneMapping(chromosomeName);
				for (final ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrences) {
					if (!countNonConclusiveProteins
							&& proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE)
						continue;
					if (!takeGeneFromFirstProteinSelected) {
						final Iterator<String> iterator = proteinGroupOccurrence.getAccessions().iterator();
						while (iterator.hasNext()) {
							final String acc = iterator.next();
							if (proteinGeneMapping.containsKey(acc)) {
								final List<ENSGInfo> list = proteinGeneMapping.get(acc);
								for (final ENSGInfo ensgInfo : list) {
									if (!genes.contains(ensgInfo)) {
										genes.add(ensgInfo);
									}
								}
							}
						}
					} else {
						final String acc = proteinGroupOccurrence.getAccessions().get(0);
						if (proteinGeneMapping.containsKey(acc)) {
							final List<ENSGInfo> list = proteinGeneMapping.get(acc);
							for (final ENSGInfo ensgInfo : list) {
								if (!genes.contains(ensgInfo)) {
									genes.add(ensgInfo);
								}
							}
						}

					}

				}
				final int numGenes = genes.size();
				final int numTotalGenes = chromosomeGeneMapping.size();

				// log.info(numGenes + " over " + numTotalGenes + " for chr "
				// + chromosomeName);
				String label = "";
				if (idSets.size() > 1)
					label = " (" + idSet.getName() + ")";
				if (justPercentage) {
					final double percentage = numGenes * 100.0 / numTotalGenes;
					dataset.addValue(percentage, "Gene coverage (%)" + label, "Chr" + chromosomeName);
					if (showTotal && spiderPlot && !totalShown) {
						dataset.addValue(100, "Total genes (100%)", "Chr" + chromosomeName);
					}
				} else {
					dataset.addValue(numGenes, "Detected genes" + label, "Chr" + chromosomeName);
					if (showTotal && !totalShown) {
						dataset.addValue(numTotalGenes, "Total chromosome genes", "Chr" + chromosomeName);
					}
				}

				totalNum += numGenes;

			}
			if (showTotal) {
				totalShown = true;
			}
		}
		if (totalNum == 0)
			throw new IllegalMiapeArgumentException(
					"<html>No proteins have been mapped to any human chromosome.<br>Note that this mapping is made from uniprot entries<br>so check if your proteins has the uniprot accession or not.</html>");

		return dataset;
	}

	public static PieDataset createAllHumanChromosomeMappingPieDataSet(IdentificationSet idSet, String proteinOrGene,
			boolean takeGeneFromFirstProteinInGroup, Boolean countNonConclusiveProteins) {
		final boolean onlyOneIdSet = false;
		if (AdditionalOptionsPanelFactory.BOTH.equals(proteinOrGene))
			throw new IllegalMiapeArgumentException(
					"Proteins and genes distributions cannot been seen in the same pie chart.\n"
							+ "Select PROTEIN or GENES in the combobox.");

		final DefaultPieDataset dataset = new DefaultPieDataset();
		final Collection<ProteinGroupOccurrence> proteinGroupOccurrences = idSet.getProteinGroupOccurrenceList()
				.values();

		int totalNum = 0;
		for (final String chromosomeName : GeneDistributionReader.chromosomeNames) {
			int numProteins = 0;
			int numGenes = 0;
			final List<ENSGInfo> genes = new ArrayList<ENSGInfo>();
			Map<String, List<ENSGInfo>> proteinGeneMapping = proteinGeneMapping = GeneDistributionReader.getInstance()
					.getProteinGeneMapping(chromosomeName);

			int numProteinsToCount = 0;
			for (final ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrences) {
				if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE
						&& !countNonConclusiveProteins)
					continue;
				numProteinsToCount++;
				if (!takeGeneFromFirstProteinInGroup) {
					final List<String> accessions = proteinGroupOccurrence.getAccessions();
					for (final String acc : accessions) {
						if (proteinGeneMapping.containsKey(acc)) {
							numProteins++;
							break;
						}
					}
					final Iterator<String> iterator = proteinGroupOccurrence.getAccessions().iterator();
					while (iterator.hasNext()) {
						final String acc = iterator.next();
						if (proteinGeneMapping.containsKey(acc)) {
							final List<ENSGInfo> list = proteinGeneMapping.get(acc);
							for (final ENSGInfo ensgInfo : list) {
								if (!genes.contains(ensgInfo))
									genes.add(ensgInfo);
							}
						}
					}
				} else {
					final String acc = proteinGroupOccurrence.getAccessions().get(0);
					if (proteinGeneMapping.containsKey(acc)) {
						numProteins++;
					}
					if (proteinGeneMapping.containsKey(acc)) {
						final List<ENSGInfo> list = proteinGeneMapping.get(acc);
						for (final ENSGInfo ensgInfo : list) {
							if (!genes.contains(ensgInfo))
								genes.add(ensgInfo);
						}
					}
				}

			}
			if (chromosomeName.equals("16"))
				log.info(numProteinsToCount + " proteins valid in chr16");

			numGenes = genes.size();

			if (AdditionalOptionsPanelFactory.PROTEIN.equals(proteinOrGene)) {
				dataset.setValue("Chr" + chromosomeName, numProteins);
				totalNum += numProteins;
			}

			if (AdditionalOptionsPanelFactory.GENES.equals(proteinOrGene)) {
				dataset.setValue("Chr" + chromosomeName, numGenes);
				totalNum += numGenes;
			}
		}
		if (onlyOneIdSet && totalNum == 0)
			throw new IllegalMiapeArgumentException(
					"<html>No proteins have been mapped to any human chromosome.<br>Note that this mapping is made from uniprot entries<br>so check if your proteins has the uniprot accession or not.</html>");

		return dataset;

	}

	private static int getNonAssignedChr16Proteins(IdentificationSet idSet, Boolean countNonConclusiveProteins) {
		int ret = 0;
		final Collection<ProteinGroupOccurrence> proteinGroupOccurrenceList = idSet.getProteinGroupOccurrenceList()
				.values();
		final int total = proteinGroupOccurrenceList.size();
		int numProteinToCount = 0;

		for (final ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrenceList) {
			if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;
			numProteinToCount++;
			final Set<ENSGInfo> genesFromProteinGroup = GeneDistributionReader.getInstance()
					.getGenesFromProteinGroup(proteinGroupOccurrence, "16");
			for (final ENSGInfo ensgInfo : genesFromProteinGroup) {
				if (!ensgInfo.isAssigned()) {
					ret++;
					// just count once for each proteinGroup
					break;
				}
			}

		}
		log.info(numProteinToCount + " proteins NonAssigned from chr16");
		return ret;
	}

	private static Map<String, Integer> getAssignedChr16NumProteins(IdentificationSet idSet, boolean known,
			Boolean countNonConclusiveProteins) {
		final Map<String, Integer> ret = new THashMap<String, Integer>();

		final Collection<ProteinGroupOccurrence> proteinGroupOccurrenceList = idSet.getProteinGroupOccurrenceList()
				.values();
		final int total = proteinGroupOccurrenceList.size();
		int numProteinToCount = 0;
		final List<String> accs = new ArrayList<String>();
		for (final ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrenceList) {
			if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;
			numProteinToCount++;
			final Set<ENSGInfo> genesFromProteinGroup = GeneDistributionReader.getInstance()
					.getGenesFromProteinGroup(proteinGroupOccurrence, "16");
			for (final ENSGInfo ensgInfo : genesFromProteinGroup) {
				if (ensgInfo.isAssigned()) {
					if ((!known && ensgInfo.getKnown().equals(ENSGInfo.UNKNOWN))
							|| (known && ensgInfo.getKnown().equals(ENSGInfo.KNOWN))) {
						final String researcher = ensgInfo.getResearcher().getName();
						if (!ret.containsKey(researcher)) {
							ret.put(researcher, 1);
						} else {
							final Integer integer = ret.get(researcher);
							ret.remove(researcher);
							ret.put(researcher, integer + 1);
						}
						// just count once for each proteinGroup
						break;
					}
				}

			}
		}

		log.info(numProteinToCount + " proteins Assigned from chr16");

		return ret;
	}

	private static Map<String, Integer> getAssignedChr16NumGenes(IdentificationSet idSet, boolean known,
			boolean takeGeneFromFirstProteinSelected, Boolean countNonConclusiveProteins) {
		final Map<String, Integer> ret = new THashMap<String, Integer>();
		final List<ENSGInfo> genes = new ArrayList<ENSGInfo>();

		final Collection<ProteinGroupOccurrence> proteinGroupOccurrenceList = idSet.getProteinGroupOccurrenceList()
				.values();

		for (final ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrenceList) {
			if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;
			Set<ENSGInfo> genesFromProteinGroup = null;
			if (!takeGeneFromFirstProteinSelected) {
				genesFromProteinGroup = GeneDistributionReader.getInstance()
						.getGenesFromProteinGroup(proteinGroupOccurrence, "16");
			} else {
				genesFromProteinGroup = GeneDistributionReader.getInstance()
						.getGenesFromProtein(proteinGroupOccurrence.getProteins().get(0), "16");
			}
			for (final ENSGInfo geneInfo : genesFromProteinGroup) {
				if (!geneInfo.getChrName().equals("16"))
					log.warn("No puede ser");
				if (geneInfo != null && geneInfo.isAssigned()) {
					if ((!known && geneInfo.getKnown().equals(ENSGInfo.UNKNOWN))
							|| (known && geneInfo.getKnown().equals(ENSGInfo.KNOWN))) {
						if (!genes.contains(geneInfo)) {
							genes.add(geneInfo);
							final String researcher = geneInfo.getResearcher().getName();
							if (!ret.containsKey(researcher)) {
								ret.put(researcher, 1);
							} else {
								final Integer integer = ret.get(researcher);
								ret.remove(researcher);
								ret.put(researcher, integer + 1);
							}
						}
					}
				}
			}

		}
		return ret;
	}

	private static int getNonAssignedChr16NumGenes(IdentificationSet idSet, boolean takeGeneFromFirstProteinSelected,
			Boolean countNonConclusiveProteins) {
		int ret = 0;
		final List<ENSGInfo> genes = new ArrayList<ENSGInfo>();

		final Collection<ProteinGroupOccurrence> proteinGroupOccurrenceList = idSet.getProteinGroupOccurrenceList()
				.values();
		for (final ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrenceList) {
			if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;

			Set<ENSGInfo> genesFromProteinGroup = null;
			if (!takeGeneFromFirstProteinSelected) {
				genesFromProteinGroup = GeneDistributionReader.getInstance()
						.getGenesFromProteinGroup(proteinGroupOccurrence, "16");
			} else {
				genesFromProteinGroup = GeneDistributionReader.getInstance()
						.getGenesFromProtein(proteinGroupOccurrence.getProteins().get(0), "16");
			}
			for (final ENSGInfo ensgInfo : genesFromProteinGroup) {
				if (!genes.contains(ensgInfo)) {
					genes.add(ensgInfo);
					if (!ensgInfo.isAssigned()) {
						ret++;
					}
				}
			}

		}
		return ret;
	}

	public static CategoryDataset createPSM_PEP_PROT_DataSet(List<IdentificationSet> idSets, boolean showPSMs,
			boolean showPeptides, boolean showPeptidesPlusCharge, boolean showProteins,
			boolean distinguishModificatedPeptides, boolean countNonConclusiveProteins) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		final String error = null;
		try {
			for (final IdentificationSet idSet : idSets) {
				if (showPSMs) {
					final int numPSMs = idSet.getTotalNumPeptides();
					dataset.addValue(numPSMs, "PSM", idSet.getFullName());
				}
				if (showPeptides) {
					final int numPeptides = idSet.getNumDifferentPeptides(distinguishModificatedPeptides);
					dataset.addValue(numPeptides, "Peptides", idSet.getFullName());
				}
				if (showPeptidesPlusCharge) {
					final int numPeptidesPlusCharge = idSet
							.getNumDifferentPeptidesPlusCharge(distinguishModificatedPeptides);
					dataset.addValue(numPeptidesPlusCharge, "Peptides (diff z)", idSet.getFullName());
				}
				if (showProteins) {
					final int numProteins = idSet.getNumDifferentProteinGroups(countNonConclusiveProteins);
					dataset.addValue(numProteins, "Proteins", idSet.getFullName());
				}
			}
		} catch (final IllegalMiapeArgumentException ex) {

		}

		return dataset;
	}

	public static XYDataset createFDRvsScoreDataSet(List<IdentificationSet> idSets, boolean showPSMs,
			boolean showPeptides, boolean showProteins, Boolean countNonConclusiveProteins) {
		final XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		String error = null;
		for (final IdentificationSet idSet : idSets) {
			log.info("calculating FDR from :" + idSet.getFullName());

			try {
				if (showPSMs) {
					final XYSeries xySeries = getXY_PSM_FDRvsScoreSerie(idSet, idSet.getFullName() + " (PSM FDR)");
					xySeriesCollection.addSeries(xySeries);
				}
				if (showPeptides) {
					final XYSeries xySeries = getXY_Peptide_FDRvsScoreSerie(idSet, idSet.getFullName() + " (pep FDR)");
					xySeriesCollection.addSeries(xySeries);
				}
				if (showProteins) {
					final XYSeries xySeries = getXY_Protein_FDRvsScoreSerie(idSet, idSet.getFullName() + " (prot FDR)",
							countNonConclusiveProteins);
					xySeriesCollection.addSeries(xySeries);
				}
			} catch (final IllegalMiapeArgumentException e) {
				error = e.getMessage();
			}
		}
		if (xySeriesCollection.getSeries() == null || xySeriesCollection.getSeries().isEmpty())
			throw new IllegalMiapeArgumentException(error);
		return xySeriesCollection;
	}

	private static XYSeries getXY_PSM_FDRvsScoreSerie(IdentificationSet idSet, String serieName) {
		final boolean autoSort = false;
		final boolean allowDuplicateXValues = true;

		final XYSeries xySeries = new XYSeries(serieName, autoSort, allowDuplicateXValues);

		final FDRFilter filter = idSet.getFDRFilter();
		if (filter == null)
			throw new IllegalMiapeArgumentException(
					"<html>Error: Apply an FDR filter in order to select the score name as well<br> as the way to select the DECOY hits, in which the FDR calculation in going to be based</html>");

		final List<ExtendedIdentifiedPeptide> identifiedPeptides = idSet.getIdentifiedPeptides();
		SorterUtil.sortPeptidesByPeptideScore(identifiedPeptides, filter.getSortingParameters().getScoreName(), true);

		int numFW = 0;
		int numDC = 0;
		float previousFDRValue = 0.0f;
		for (final ExtendedIdentifiedPeptide extendedIdentifiedPeptide : identifiedPeptides) {
			final boolean decoy = extendedIdentifiedPeptide.isDecoy(filter);
			if (decoy) {
				numDC++;
			} else {
				numFW++;
			}
			final Float fdrValue = filter.calculateFDR(numFW, numDC);
			final Float peptideScore = extendedIdentifiedPeptide.getScore(filter.getSortingParameters().getScoreName());

			if (peptideScore != null && fdrValue <= previousFDRValue && fdrValue != 0.0) {
				// delete previous point
				xySeries.remove(xySeries.getItemCount() - 1);
			}
			previousFDRValue = fdrValue;
			xySeries.add(peptideScore, fdrValue);
		}

		return xySeries;
	}

	private static XYSeries getXY_Peptide_FDRvsScoreSerie(IdentificationSet idSet, String serieName) {
		final boolean autoSort = false;
		final boolean allowDuplicateXValues = true;

		final XYSeries xySeries = new XYSeries(serieName, autoSort, allowDuplicateXValues);

		final FDRFilter filter = idSet.getFDRFilter();
		if (filter == null)
			throw new IllegalMiapeArgumentException(
					"<html>Error: Apply an FDR filter in order to select the score name as well<br> as the way to select the DECOY hits, in which the FDR calculation in going to be based</html>");

		final Collection<PeptideOccurrence> peptideOccurrenceColl = idSet.getPeptideOccurrenceList(false).values();
		final List<PeptideOccurrence> peptideOccurrenceList = new ArrayList<PeptideOccurrence>();
		for (final PeptideOccurrence peptideOccurrence : peptideOccurrenceColl) {
			peptideOccurrenceList.add(peptideOccurrence);
		}

		SorterUtil.sortPeptideOcurrencesByPeptideScore(peptideOccurrenceList,
				filter.getSortingParameters().getScoreName());

		int numFW = 0;
		int numDC = 0;
		float previousFDRValue = 0.0f;
		for (final PeptideOccurrence peptideOccurrence : peptideOccurrenceList) {
			final boolean decoy = peptideOccurrence.isDecoy(filter);
			if (decoy) {
				numDC++;
			} else {
				numFW++;
			}
			final Float fdrValue = filter.calculateFDR(numFW, numDC);
			final Float bestScore = peptideOccurrence.getBestPeptideScore(filter.getSortingParameters().getScoreName());

			if (bestScore != null && fdrValue <= previousFDRValue && fdrValue != 0.0) {
				// delete previous point
				xySeries.remove(xySeries.getItemCount() - 1);
			}
			previousFDRValue = fdrValue;
			xySeries.add(bestScore, fdrValue);

		}

		return xySeries;
	}

	private static XYSeries getXY_Protein_FDRvsScoreSerie(IdentificationSet idSet, String serieName,
			Boolean countNonConclusiveProteins) {
		final boolean autoSort = false;
		final boolean allowDuplicateXValues = true;

		final XYSeries xySeries = new XYSeries(serieName, autoSort, allowDuplicateXValues);

		final FDRFilter filter = idSet.getFDRFilter();
		if (filter == null)
			throw new IllegalMiapeArgumentException(
					"<html>Error: Apply an FDR filter in order to select the score name as well<br> as the way to select the DECOY hits, in which the FDR calculation in going to be based</html>");

		final Collection<ProteinGroupOccurrence> proteinGroupOccurrencesSet = idSet.getProteinGroupOccurrenceList()
				.values();
		final List<ProteinGroupOccurrence> proteinGroupOccurrences = new ArrayList<ProteinGroupOccurrence>();
		for (final ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrencesSet) {
			proteinGroupOccurrences.add(proteinGroupOccurrence);
		}
		SorterUtil.sortProteinGroupOcurrencesByPeptideScore(proteinGroupOccurrences,
				filter.getSortingParameters().getScoreName());

		int numFW = 0;
		int numDC = 0;
		float previousFDRValue = 0.0f;

		for (final ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrences) {
			if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;

			final ExtendedIdentifiedPeptide bestPeptide = proteinGroupOccurrence
					.getBestPeptide(filter.getSortingParameters().getScoreName());

			if (bestPeptide != null) {
				final boolean decoy = proteinGroupOccurrence.isDecoy(filter);
				if (decoy) {
					numDC++;
				} else {
					numFW++;
				}
				final Float fdrValue = filter.calculateFDR(numFW, numDC);
				final Float bestScore = proteinGroupOccurrence
						.getBestPeptideScore(filter.getSortingParameters().getScoreName());

				if (bestScore != null && fdrValue <= previousFDRValue && fdrValue != 0.0) {
					// delete previous point
					xySeries.remove(xySeries.getItemCount() - 1);
				}
				previousFDRValue = fdrValue;
				xySeries.add(bestScore, fdrValue);
			}

		}

		return xySeries;
	}

	public static XYDataset createScoreVsNumProteinsDataSet(List<IdentificationSet> idSets,
			Boolean countNonConclusiveProteins) {
		final XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		String error = null;
		for (final IdentificationSet idSet : idSets) {
			try {
				final FDRFilter filter = idSet.getFDRFilter();
				log.info(filter);
				if (filter == null) {
					throw new IllegalMiapeArgumentException(
							"<html>Error: Apply an FDR filter in order to select the score name as well<br> as the way to select the DECOY hits, in which the FDR calculation in going to be based</html>");
				}
				final boolean autoSort = false;
				final boolean allowDuplicateXValues = true;

				final XYSeries xySeries = new XYSeries(idSet.getFullName() + " (num proteins vs '"
						+ filter.getSortingParameters().getScoreName() + "')", autoSort, allowDuplicateXValues);
				final List<ProteinGroup> proteinGroups = idSet.getIdentifiedProteinGroups();
				SorterUtil.sortProteinGroupsByPeptideScore(proteinGroups, filter.getSortingParameters().getScoreName());
				int numProteins = 1;
				for (final ProteinGroup proteinGroup : proteinGroups) {
					if (proteinGroup.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
						continue;

					final Float bestScore = proteinGroup
							.getBestPeptideScore(filter.getSortingParameters().getScoreName());

					if (bestScore != null) {
						// log.info(bestScore + " - " + numProteins);
						xySeries.add(bestScore, (Number) numProteins);
					}
					numProteins++;

				}
				xySeriesCollection.addSeries(xySeries);

			} catch (final IllegalMiapeArgumentException e) {
				error = e.getMessage();
			}
		}
		if (xySeriesCollection.getSeries() == null || xySeriesCollection.getSeries().isEmpty())
			throw new IllegalMiapeArgumentException(error);
		return xySeriesCollection;
	}

	/**
	 * Gets two datasets, the first is the normal and then second is the
	 * accumulative (if accumulativeTrend is true)
	 *
	 * @param idSets
	 * @param itemType
	 * @param distModPeptides
	 * @param proteinGroupComparisonType
	 * @return
	 */
	public static List<DefaultCategoryDataset> createExclusiveNumberIdentificationCategoryDataSetForPeptides(
			List<IdentificationSet> idSets, Boolean distModPeptides, boolean accumulativeTrend) {
		// create the dataset...

		final List<DefaultCategoryDataset> datasets = new ArrayList<DefaultCategoryDataset>();
		final DefaultCategoryDataset defaultDataSet = new DefaultCategoryDataset();
		final DefaultCategoryDataset accumulativeDataSet = new DefaultCategoryDataset();

		try {
			final Map<String, Set<String>> peptidesPerDataset = getHashMapKeysForPeptides(idSets, distModPeptides);

			VennData globalVenn = null;
			Collection<Object> union = null;

			for (int i = 0; i < idSets.size(); i++) {
				final IdentificationSet idSet = idSets.get(i);
				final String idSetName = idSet.getFullName();
				final Set<String> keys = peptidesPerDataset.get(idSetName);

				if (accumulativeTrend) {
					globalVenn = new VennDataForPeptides(keys, union, null);
					union = globalVenn.getUnion12();
					final int num = globalVenn.getUnion12Size();
					accumulativeDataSet.addValue(num, "Accumulative # peptides", idSetName);
				}

				final int numExclusive = 0;
				Collection<Object> unique = null;
				// Look into the other datasets to see how many elements are
				// unique/exclusive to the isSet
				boolean moreThanOne = false;
				for (int j = 0; j < idSets.size(); j++) {
					if (j == i)
						continue;
					final IdentificationSet idSet2 = idSets.get(j);

					moreThanOne = true;
					final String idSetName2 = idSet2.getFullName();
					log.info("Comparing " + idSet.getFullName() + " with " + idSetName2);
					final Set<String> keys2 = peptidesPerDataset.get(idSet2.getFullName());
					VennData venn = null;
					if (unique == null) {
						venn = new VennDataForPeptides(keys, keys2, null);
					} else {
						venn = new VennDataForPeptides(unique, keys2, null);
					}
					unique = venn.getUniqueTo1();
					log.info(unique.size() + " remaining exclusive");

				}
				if (unique != null)
					defaultDataSet.setValue(unique.size(), "# peptides", idSetName);
				if (!moreThanOne)
					defaultDataSet.setValue(keys.size(), "# peptides", idSetName);
			}
			// }
		} catch (final IllegalMiapeArgumentException ex) {

		}
		datasets.add(defaultDataSet);
		if (accumulativeTrend)
			datasets.add(accumulativeDataSet);

		return datasets;
	}

	/**
	 * Gets two datasets, the first is the normal and then second is the
	 * accumulative (if accumulativeTrend is true)
	 *
	 * @param idSets
	 * @param itemType
	 * @param distModPeptides
	 * @param proteinGroupComparisonType
	 * @return
	 */
	public static List<DefaultCategoryDataset> createExclusiveNumberIdentificationCategoryDataSetForProteins(
			List<IdentificationSet> idSets, ProteinGroupComparisonType proteinGroupComparisonType,
			boolean accumulativeTrend) {
		// create the dataset...

		final List<DefaultCategoryDataset> datasets = new ArrayList<DefaultCategoryDataset>();
		final DefaultCategoryDataset defaultDataSet = new DefaultCategoryDataset();
		final DefaultCategoryDataset accumulativeDataSet = new DefaultCategoryDataset();

		try {
			final Map<String, Set<Object>> mapKeys = getHashMapKeysForProteins(idSets, proteinGroupComparisonType);

			VennData globalVenn = null;
			Collection<Object> union = null;

			for (int i = 0; i < idSets.size(); i++) {
				final IdentificationSet idSet = idSets.get(i);
				final String idSetName = idSet.getFullName();
				final Set<Object> keys = mapKeys.get(idSet.getFullName());

				if (accumulativeTrend) {
					globalVenn = new VennDataForProteins(keys, union, null, proteinGroupComparisonType);
					union = globalVenn.getUnion12();
					final int num = globalVenn.getUnion12Size();

					accumulativeDataSet.addValue(num, "Accumulative # proteins", idSetName);
				}

				final int numExclusive = 0;
				Collection<Object> unique = null;
				// Look into the other datasets to see how many elements are
				// unique/exclusive to the isSet
				boolean moreThanOne = false;
				for (int j = 0; j < idSets.size(); j++) {
					if (j == i)
						continue;
					final IdentificationSet idSet2 = idSets.get(j);

					moreThanOne = true;
					final String idSetName2 = idSet2.getFullName();
					log.info("Comparing " + idSet.getFullName() + " with " + idSetName2);
					final Set<Object> keys2 = mapKeys.get(idSet2.getFullName());
					VennData venn = null;
					if (unique == null) {
						venn = new VennDataForProteins(keys, keys2, null, proteinGroupComparisonType);
					} else {
						venn = new VennDataForProteins(unique, keys2, null, proteinGroupComparisonType);
					}
					unique = venn.getUniqueTo1();
					log.info(unique.size() + " remaining exclusive");

				}
				if (unique != null)
					defaultDataSet.setValue(unique.size(), "# proteins", idSetName);
				if (!moreThanOne)
					defaultDataSet.setValue(keys.size(), "# proteins", idSetName);
			}
			// }
		} catch (final IllegalMiapeArgumentException ex) {

		}
		datasets.add(defaultDataSet);
		if (accumulativeTrend)
			datasets.add(accumulativeDataSet);

		return datasets;
	}

	/**
	 * returns a map in which the key is the full name of the idSet and the value a
	 * set of peptide sequences
	 * 
	 * @param idSets
	 * @param distModPeptides
	 * @return
	 */
	private static Map<String, Set<String>> getHashMapKeysForPeptides(List<IdentificationSet> idSets,
			Boolean distModPeptides) {
		final Map<String, Set<String>> ret = new THashMap<String, Set<String>>();
		for (final IdentificationSet identificationSet : idSets) {
			final Set<String> peptideKeySet = identificationSet.getPeptideOccurrenceList(distModPeptides).keySet();
			ret.put(identificationSet.getFullName(), peptideKeySet);
		}

		return ret;
	}

	private static Map<String, Set<Object>> getHashMapKeysForProteins(List<IdentificationSet> idSets,
			ProteinGroupComparisonType proteinGroupComparisonType) {
		final Map<String, Set<Object>> ret = new THashMap<String, Set<Object>>();
		for (final IdentificationSet identificationSet : idSets) {
			final Set<Object> proteinAccSet = new THashSet<Object>();
			for (final Object object : identificationSet.getProteinGroupOccurrenceList().values()) {
				final ProteinGroupOccurrence pgo = (ProteinGroupOccurrence) object;
				// if (pgo.getEvidence() != ProteinEvidence.NONCONCLUSIVE)
				proteinAccSet.add(pgo.getKey(proteinGroupComparisonType));
			}
			ret.put(identificationSet.getFullName(), proteinAccSet);
		}
		return ret;
	}

	public static HistogramDataset createPeptideRTHistogramDataSet(List<IdentificationSet> idSets, int bins,
			HistogramType histogramType, boolean inMinutes) {
		final HistogramDataset dataset = new HistogramDataset();
		boolean someValidData = false;
		for (final IdentificationSet idSet : idSets) {
			final double[] values = getPeptideRT(idSet, inMinutes);
			if (values != null && values.length > 0) {
				someValidData = true;
				dataset.addSeries(idSet.getFullName(), values, bins);
			}
		}
		if (!someValidData) {
			throw new IllegalMiapeArgumentException(
					"There is no data to display. Be sure that your imported datasets contain RT information.");
		}
		dataset.setType(histogramType);
		return dataset;
	}

	private static double[] getPeptideRT(IdentificationSet idSet, boolean inMinutes) {
		double[] ret = null;

		final List<ExtendedIdentifiedPeptide> peptides = idSet.getIdentifiedPeptides();
		boolean validData = false;
		if (peptides != null) {
			ret = new double[peptides.size()];
			int i = 0;
			for (final ExtendedIdentifiedPeptide peptide : peptides) {
				final String rtInSeconds = peptide.getRetentionTimeInSeconds();
				if (rtInSeconds != null) {
					try {
						Double rtInSecondsDbl = Double.valueOf(rtInSeconds);
						if (inMinutes)
							rtInSecondsDbl = rtInSecondsDbl / 60.0;
						ret[i] = rtInSecondsDbl;
						validData = true;
					} catch (final NumberFormatException e) {

					}
				}
				i++;
			}
		}
		if (!validData) {
			return null;
		}
		return ret;
	}

	public static Pair<XYDataset, MyXYItemLabelGenerator> createRTXYDataSet(List<IdentificationSet> idSets,
			boolean inMinutes) {
		final XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		final Map<String, String> tooltipValues = new THashMap<String, String>();
		if (idSets.size() < 2)
			throw new IllegalMiapeArgumentException("At least two series are needed to paint this chart");
		boolean thereisData = false;
		int numSeries = 0;
		for (int i = 0; i < idSets.size(); i++) {
			for (int j = i + 1; j < idSets.size(); j++) {
				final IdentificationSet idSet1 = idSets.get(i);
				final IdentificationSet idSet2 = idSets.get(j);
				final XYSeries xySeries = getXYRTSeries(numSeries++, idSet1, idSet2, inMinutes, tooltipValues);
				if (xySeries.getItems() != null && xySeries.getItems().size() > 0)
					thereisData = true;
				xySeriesCollection.addSeries(xySeries);

			}
		}
		if (!thereisData)
			throw new IllegalMiapeArgumentException(
					"<html>There is not data to show.<br>Please, be sure that the overlapping is not zero<br> or that retention times are captured.</html>");

		final Pair<XYDataset, MyXYItemLabelGenerator> pair = new Pair<XYDataset, MyXYItemLabelGenerator>(
				xySeriesCollection, new MyXYItemLabelGenerator(tooltipValues));
		return pair;
	}

	private static XYSeries getXYRTSeries(int seriesNumber, IdentificationSet idSet1, IdentificationSet idSet2,
			boolean inMinutes, Map<String, String> tooltipValues) {
		final XYSeries normalSeries = new XYSeries(idSet1.getName() + " (x) vs " + idSet2.getName() + " (y)");

		final Map<String, PeptideOccurrence> peptideOccurrences1 = idSet1.getPeptideChargeOccurrenceList(true);
		final Map<String, PeptideOccurrence> peptideOccurrences2 = idSet2.getPeptideChargeOccurrenceList(true);
		boolean someValidData = false;
		int numValue = 0;
		for (final PeptideOccurrence occurrence2 : peptideOccurrences2.values()) {

			if (peptideOccurrences1.containsKey(occurrence2.getKey())) {
				try {
					final PeptideOccurrence occurrence1 = peptideOccurrences1.get(occurrence2.getKey());

					// get the average of the retention times
					final TDoubleArrayList rt1 = new TDoubleArrayList();
					for (final ExtendedIdentifiedPeptide pep : occurrence1.getItemList()) {
						if (pep.getRetentionTimeInSeconds() != null) {
							try {
								rt1.add(Double.valueOf(pep.getRetentionTimeInSeconds()));
								someValidData = true;
							} catch (final NumberFormatException e) {

							}
						}
					}
					Double mean1 = 0.0;
					if (!rt1.isEmpty()) {
						mean1 = Maths.mean(rt1);
					}
					final TDoubleArrayList rt2 = new TDoubleArrayList();
					for (final ExtendedIdentifiedPeptide pep : occurrence2.getItemList()) {
						if (pep.getRetentionTimeInSeconds() != null) {
							try {
								rt2.add(Double.valueOf(pep.getRetentionTimeInSeconds()));
							} catch (final NumberFormatException e) {

							}
						}
					}
					Double mean2 = 0.0;
					if (!rt2.isEmpty()) {
						mean2 = Maths.mean(rt2);
					}
					Double x = mean1;
					Double y = mean2;
					if (x != null && y != null) {
						if (inMinutes) {
							x = x / 60.0;
						}
						if (inMinutes) {
							y = y / 60.0;
						}
						tooltipValues.put(MyXYItemLabelGenerator.getKey(seriesNumber, numValue++),
								occurrence1.getItemList().get(0).getSequence());
						normalSeries.add(x, y);
					}
				} catch (final IllegalMiapeArgumentException e) {
					// do nothing, not plot it
				} catch (final NumberFormatException e) {

				} catch (final NullPointerException e) {

				}
			}
		}
		if (!someValidData) {
			throw new IllegalMiapeArgumentException(
					"There is no data to display. Be sure that your imported datasets contain RT information.");
		}

		return normalSeries;
	}

	public static CategoryDataset createSinglePeptideRTMonitoringCategoryDataSet(List<IdentificationSet> idSets,
			List<String> sequences, Boolean showInMinutes) {
		boolean someValidData = false;
		// create the dataset...
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (final IdentificationSet idSet : idSets) {
				final String experimentName = idSet.getFullName();
				for (final String originalSequence : sequences) {
					final PeptideOccurrence peptideOccurrence = idSet.getPeptideChargeOccurrence(originalSequence,
							true);
					if (peptideOccurrence != null) {
						final TDoubleArrayList rts = new TDoubleArrayList();
						for (final ExtendedIdentifiedPeptide peptide : peptideOccurrence.getItemList()) {
							if (peptide.getRetentionTimeInSeconds() != null) {
								try {
									final double rt = Double.valueOf(peptide.getRetentionTimeInSeconds());
									someValidData = true;
									rts.add(rt);
								} catch (final NumberFormatException e) {

								}
							}
						}
						double rtMean = Maths.mean(rts);
						if (showInMinutes) {
							rtMean = rtMean / 60.0;
						}
						if (rtMean > 0) {
							dataset.addValue(rtMean, experimentName, originalSequence);
						}
					}

				}

			}

		} catch (final IllegalMiapeArgumentException ex) {

		}
		if (!someValidData) {
			throw new IllegalMiapeArgumentException(
					"No data to display. Be sure that the imported datasets contain RT information.");
		}
		return dataset;
	}

	public static List<List<String>> createSpectrometersTableData(List<IdentificationSet> idSets) {
		final List<List<String>> matrix1 = new ArrayList<List<String>>();
		final List<String> columns = new ArrayList<String>();
		columns.add("Ident. Set Name");
		columns.add("Name");
		columns.add("Model");
		columns.add("Version");
		columns.add("Manufacturer");
		columns.add("Customizations");
		columns.add("Parameters");
		columns.add("URI");

		matrix1.add(columns);
		for (final IdentificationSet idSet : idSets) {
			final Set<Spectrometer> spectrometers = idSet.getSpectrometers();
			final List<String> cols = new ArrayList<String>();
			if (spectrometers.isEmpty()) {
				cols.add(parseString(idSet.getFullName()));

				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				matrix1.add(cols);
			} else {
				for (final Spectrometer spectrometer : spectrometers) {
					cols.add(parseString(idSet.getFullName()));

					cols.add(parseString(spectrometer.getName()));
					cols.add(parseString(spectrometer.getModel()));
					cols.add(parseString(spectrometer.getVersion()));
					cols.add(parseString(spectrometer.getManufacturer()));
					cols.add(parseString(spectrometer.getCustomizations()));
					cols.add(parseString(spectrometer.getParameters()));
					cols.add(parseString(spectrometer.getUri()));

					matrix1.add(cols);
				}
			}
		}
		return transpose(matrix1);

	}

	private static List<List<String>> transpose(List<List<String>> matrix) {

		// transponse
		final List<List<String>> ret = new ArrayList<List<String>>();
		// add a row for each property
		for (final String col : matrix.get(0)) {
			ret.add(new ArrayList<String>());
		}

		for (int x = 0; x < matrix.get(0).size(); x++) {
			for (int y = 0; y < matrix.size(); y++) {
				final String value = matrix.get(y).get(x);
				ret.get(x).add(value);
			}
		}
		return ret;
	}

	public static List<List<String>> createInputParametersTableData(List<IdentificationSet> idSets) {
		final List<List<String>> matrix1 = new ArrayList<List<String>>();
		final List<String> columns = new ArrayList<String>();
		columns.add("Ident. Set Name");
		columns.add("Software");
		columns.add("Database(s)");
		columns.add("Search type");
		columns.add("Precursor mass tolerance");
		columns.add("Fragment mass tolerance");
		columns.add("PMF mass tolerance");
		columns.add("Num missecleavages");
		columns.add("Enzyme name");
		columns.add("Cleavage rules");
		columns.add("Additional params for cleavages");
		columns.add("AA modif");
		columns.add("Min score");
		columns.add("Num seq searched");
		columns.add("Scoring Algorithm");
		columns.add("Taxonomy");

		matrix1.add(columns);
		for (final IdentificationSet idSet : idSets) {
			final Set<InputParameter> inputParameters = idSet.getInputParameters();
			if (inputParameters.isEmpty()) {
				final List<String> cols = new ArrayList<String>();
				cols.add(idSet.getFullName());
				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				cols.add("-");
				matrix1.add(cols);
			} else {
				for (final InputParameter inputParameter : inputParameters) {
					final List<String> cols = new ArrayList<String>();
					cols.add(parseString(idSet.getFullName()));
					if (inputParameter.getSoftware() != null) {
						cols.add(parseString(inputParameter.getSoftware().getName(), ExporterUtil.VALUE_SEPARATOR,
								inputParameter.getSoftware().getManufacturer() + ExporterUtil.VALUE_SEPARATOR
										+ inputParameter.getSoftware().getVersion()));
					} else {
						cols.add(null);
					}
					cols.add(parseString(parseDatabases(inputParameter.getDatabases())));
					cols.add(parseString(inputParameter.getSearchType()));
					cols.add(parseString(inputParameter.getPrecursorMassTolerance(), ExporterUtil.VALUE_SEPARATOR,
							inputParameter.getPrecursorMassToleranceUnit()));
					cols.add(parseString(inputParameter.getFragmentMassTolerance(), " ",
							inputParameter.getFragmentMassToleranceUnit()));

					cols.add(parseString(parseString(inputParameter.getPmfMassTolerance(), ExporterUtil.VALUE_SEPARATOR,
							inputParameter.getPmfMassToleranceUnit())));
					cols.add(parseString(inputParameter.getMisscleavages()));
					cols.add(parseString(inputParameter.getCleavageName()));
					cols.add(parseString(inputParameter.getCleavageRules()));
					cols.add(parseString(inputParameter.getAdditionalCleavages()));
					cols.add(parseString(inputParameter.getAaModif()));
					cols.add(parseString(inputParameter.getMinScore()));
					cols.add(parseString(inputParameter.getNumEntries()));
					cols.add(parseString(inputParameter.getScoringAlgorithm()));
					cols.add(parseString(inputParameter.getTaxonomy()));

					matrix1.add(cols);
				}
			}
		}

		return transpose(matrix1);
	}

	private static String parseDatabases(Set<Database> databases) {
		final StringBuilder sb = new StringBuilder();
		if (databases != null) {
			for (final Database database : databases) {
				if (!"".equals(sb.toString()))
					sb.append(ExporterUtil.VALUE_SEPARATOR);
				sb.append(database.getName());
				if (database.getNumVersion() != null)
					sb.append(ExporterUtil.VALUE_SEPARATOR + database.getNumVersion());
			}
		}
		return sb.toString();
	}

	private static String parseString(String string1, String separator, String string2) {
		final StringBuilder sb = new StringBuilder();
		if (string1 != null)
			sb.append(string1);
		if (string2 != null) {
			if (separator != null)
				sb.append(separator);
			sb.append(string2);
		}
		return sb.toString();
	}

	private static String parseString(String string1) {
		final StringBuilder sb = new StringBuilder();
		if (string1 != null) {
			sb.append(string1);
		} else {
			sb.append("-");
		}
		return sb.toString();
	}

	public static HistogramDataset createPeptideCountingHistogramDataSet(IdentificationSet idSet1,
			IdentificationSet idSet2, ProteinGroupComparisonType proteinGroupComparisonType,
			HistogramType histogramType, boolean distinguish, Boolean countNonConclusiveProteins, int bins) {

		final HistogramDataset serie = new HistogramDataset();

		final double[] values = getRatioHistogram(idSet1, idSet2, proteinGroupComparisonType);
		if (values != null && values.length > 0)
			serie.addSeries(idSet1.getName() + " vs " + idSet2.getName(), values, bins);

		serie.setType(histogramType);
		if (serie.getSeriesCount() < 1)
			throw new IllegalMiapeArgumentException(
					"<html>There is not data to show.<br>Please, be sure that the datasets contains common proteins<br>were the overlapping is not zero.</html>");
		return serie;
	}

	private static double[] getRatioHistogram(IdentificationSet idSet1, IdentificationSet idSet2,
			ProteinGroupComparisonType proteinGroupComparisonType) {

		final VennData venn = new VennDataForProteins(idSet1.getProteinGroupOccurrenceList().values(),
				idSet2.getProteinGroupOccurrenceList().values(), null, proteinGroupComparisonType);
		final Collection<Object> intersection = venn.getIntersection12();
		log.info(intersection.size() + " proteins in common");
		final double[] logRatios = new double[intersection.size()];
		int i = 0;
		for (final Object object : intersection) {
			if (object instanceof ProteinGroupOccurrence) {
				final ProteinGroupOccurrence pgo = (ProteinGroupOccurrence) object;
				final ProteinGroupOccurrence pgo1 = idSet1.getProteinGroupOccurrence(pgo.getFirstOccurrence());

				final ProteinGroupOccurrence pgo2 = idSet2.getProteinGroupOccurrence(pgo.getFirstOccurrence());

				if (pgo1 != null && pgo2 != null) {

					final List<ExtendedIdentifiedPeptide> peptides1 = pgo1.getPeptides();
					final List<ExtendedIdentifiedPeptide> peptides2 = pgo2.getPeptides();
					if (peptides1 != null && peptides2 != null) {
						final int numPeptides1 = peptides1.size();
						final int numPeptides2 = peptides2.size();
						if (numPeptides2 > 0) {
							final double ratio = (double) numPeptides1 / (double) numPeptides2;
							final double log2ratio = Math.log(ratio) / Math.log(2);
							if (Double.isInfinite(log2ratio)) {
								continue;
							}
							log.debug("Ratio= " + numPeptides1 + "/" + numPeptides2 + "=" + log2ratio);
							logRatios[i++] = log2ratio;
						}
					}
				}
			}
		}
		return logRatios;
	}

	public static Pair<XYDataset, MyXYItemLabelGenerator> createPeptideCountingVsScoreXYDataSet(
			IdentificationSet idSet1, IdentificationSet idSet2, ProteinGroupComparisonType proteinGroupComparisonType,
			boolean distinguish, String scoreName) {
		final XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		final XYSeries serie = new XYSeries(idSet1.getName() + " vs " + idSet2.getName());
		xySeriesCollection.addSeries(serie);
		final Map<String, String> tooltipValues = new THashMap<String, String>();
		final int numSeries = 0;
		final VennData venn = new VennDataForProteins(idSet1.getProteinGroupOccurrenceList().values(),
				idSet2.getProteinGroupOccurrenceList().values(), null, proteinGroupComparisonType);
		final Collection<Object> intersection = venn.getIntersection12();
		log.info(intersection.size() + " proteins in common");
		final double[] logRatios = new double[intersection.size()];
		final int i = 0;
		int numItem = 0;
		for (final Object object : intersection) {
			if (object instanceof ProteinGroupOccurrence) {
				final ProteinGroupOccurrence pgo = (ProteinGroupOccurrence) object;
				final ProteinGroupOccurrence pgo1 = idSet1.getProteinGroupOccurrence(pgo.getFirstOccurrence());

				final ProteinGroupOccurrence pgo2 = idSet2.getProteinGroupOccurrence(pgo.getFirstOccurrence());

				if (pgo1 != null && pgo2 != null) {
					final List<ExtendedIdentifiedPeptide> peptides1 = pgo1.getPeptides();
					final List<ExtendedIdentifiedPeptide> peptides2 = pgo2.getPeptides();
					if (peptides1 != null && peptides2 != null) {
						final int numPeptides1 = peptides1.size();
						final int numPeptides2 = peptides2.size();
						if (numPeptides2 > 0) {
							final double ratio = (double) numPeptides1 / (double) numPeptides2;
							final double logratio = Math.log10(ratio);
							if (Double.isInfinite(logratio)) {
								continue;
							}
							final Float score1 = pgo1.getBestPeptideScore(scoreName);
							if (score1 != null) {
								serie.add(logratio, score1);
								tooltipValues.put(MyXYItemLabelGenerator.getKey(numSeries, numItem++),
										pgo.getAccessionsString());
							}
							final Float score2 = pgo2.getBestPeptideScore(scoreName);
							if (score2 != null) {
								serie.add(logratio, score2);
								tooltipValues.put(MyXYItemLabelGenerator.getKey(numSeries, numItem++),
										pgo.getAccessionsString());
							}
						}
					}
				}
			}
		}
		if (serie.getItemCount() < 1)
			throw new IllegalMiapeArgumentException(
					"<html>There is not data to show.<br>Please, be sure that the datasets contains common proteins<br>were the overlapping is not zero.</html>");

		return new Pair<XYDataset, MyXYItemLabelGenerator>(xySeriesCollection,
				new MyXYItemLabelGenerator(tooltipValues));
	}
}