package org.proteored.pacom.analysis.gui.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.proteored.miapeapi.experiment.model.sort.ProteinComparatorKey;
import org.proteored.miapeapi.experiment.model.sort.ProteinGroupComparisonType;
import org.proteored.miapeapi.experiment.model.sort.SorterUtil;
import org.proteored.miapeapi.interfaces.ms.Spectrometer;
import org.proteored.miapeapi.interfaces.msi.Database;
import org.proteored.miapeapi.interfaces.msi.InputParameter;
import org.proteored.pacom.analysis.exporters.util.ExporterUtil;
import org.proteored.pacom.analysis.genes.ENSGInfo;
import org.proteored.pacom.analysis.genes.GeneDistributionReader;
import org.proteored.pacom.analysis.gui.AdditionalOptionsPanelFactory;

import com.compomics.util.protein.AASequenceImpl;
import com.compomics.util.protein.Protein;

import edu.scripps.yates.utilities.maths.Maths;

public class DatasetFactory {
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");

	/**
	 * Each category belongs to one of the {@link IdentificationSet} of the
	 * list, and has the number of items identified in that
	 * {@link IdentificationSet}<br>
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
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (IdentificationSet idSet : idSets) {
				String experimentName = idSet.getFullName();
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

		} catch (IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createNumberSingleHitProteinsCategoryDataSet(List<IdentificationSet> idSets,
			boolean differentIdentificationsShown, Boolean countNonConclusiveProteins) {
		// create the dataset...
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (IdentificationSet idSet : idSets) {
				String experimentName = idSet.getFullName();

				int numProteins = 0;
				if (differentIdentificationsShown) {
					numProteins = getNumDifferentSingleHitProteins(idSet, countNonConclusiveProteins);
				} else {
					numProteins = getTotalNumSingleHitProteins(idSet, countNonConclusiveProteins);
				}
				dataset.addValue(numProteins, idSet.getFullName(), "");
			}

		} catch (IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createPeptideNumberInProteinsCategoryDataSet(List<IdentificationSet> idSets,
			int maximum, boolean differentIdentificationsShown, Boolean countNonConclusiveProteins) {
		// create the dataset...

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {
			HashMap<String, HashMap<Integer, Integer>> distributions = new HashMap<String, HashMap<Integer, Integer>>();
			for (IdentificationSet idSet : idSets) {
				String experimentName = idSet.getFullName();

				double numProteins = 0;
				// key: number of peptides - value=number of proteins with that
				// number of peptides
				HashMap<Integer, Integer> distribution = new HashMap<Integer, Integer>();
				if (differentIdentificationsShown) {
					Collection<ProteinGroupOccurrence> proteinOccurrenceList = idSet.getProteinGroupOccurrenceList()
							.values();
					for (ProteinGroupOccurrence proteinOccurrence : proteinOccurrenceList) {
						if (proteinOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE
								&& !countNonConclusiveProteins)
							continue;
						List<ExtendedIdentifiedPeptide> peptides = proteinOccurrence.getPeptides();

						int numPeptides = getNumDifferentPeptides(peptides);
						if (numPeptides > maximum)
							numPeptides = maximum;
						if (!distribution.containsKey(numPeptides)) {
							distribution.put(numPeptides, 1);
						} else {
							int newNum = distribution.get(numPeptides) + 1;
							distribution.remove(numPeptides);
							distribution.put(numPeptides, newNum);
						}

					}

				} else {
					List<ProteinGroup> proteinList = idSet.getIdentifiedProteinGroups();
					for (ProteinGroup proteinGroup : proteinList) {
						if (proteinGroup.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
							continue;

						List<ExtendedIdentifiedPeptide> peptides = proteinGroup.getPeptides();
						int numPeptides = getNumDifferentPeptides(peptides);
						if (numPeptides > maximum)
							numPeptides = maximum;
						if (!distribution.containsKey(numPeptides)) {
							distribution.put(numPeptides, 1);
						} else {
							int newNum = distribution.get(numPeptides) + 1;
							distribution.remove(numPeptides);
							distribution.put(numPeptides, newNum);
						}
					}

				}
				if (!distribution.isEmpty()) {
					distributions.put(idSet.getFullName(), distribution);
				} else {
					log.info("Distribution for idSet: " + idSet.getName() + " is empty");
				}
			}
			if (!distributions.isEmpty()) {
				for (int i = 1; i <= maximum; i++) {
					for (IdentificationSet idSet : idSets) {
						HashMap<Integer, Integer> distribution = distributions.get(idSet.getFullName());
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
		} catch (IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	private static int getNumDifferentPeptides(List<ExtendedIdentifiedPeptide> peptides) {
		int ret = 0;
		if (peptides != null) {
			Set<String> sequences = new HashSet<String>();
			for (ExtendedIdentifiedPeptide extendedIdentifiedPeptide : peptides) {
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
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (IdentificationSet idSet : idSets) {
				String experimentName = idSet.getFullName();
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

		} catch (IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createNumberIdentificationStatisticalCategoryDataSet(List<IdentificationSet> idSets,
			IdentificationItemEnum itemType, Boolean distModPeptides, boolean differentIdentificationsShown,
			boolean countNonConclusiveProteins) {
		// create the dataset...
		DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
		try {

			for (IdentificationSet idSet : idSets) {
				String experimentName = idSet.getFullName();
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

		} catch (IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createTotalVsDifferentNumberIdentificationStatisticalCategoryDataSet(
			List<IdentificationSet> idSets, IdentificationItemEnum itemType, Boolean distModPeptides,
			boolean countNonConclusiveProteins) {
		DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
		try {
			// TODO
			for (IdentificationSet idSet : idSets) {
				String experimentName = idSet.getFullName();
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

		} catch (IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static PieDataset createNumberIdentificationPieDataSet(List<IdentificationSet> idSets,
			IdentificationItemEnum plotItem, Boolean distinguishModifiedPeptides, Boolean average,
			boolean differentIdentificationsShown, boolean countNonConclusiveProteins) {
		DefaultPieDataset dataset = new DefaultPieDataset();

		for (IdentificationSet identificationSet : idSets) {
			if (plotItem.equals(plotItem.PROTEIN)) {

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
			} else if (plotItem.equals(plotItem.PEPTIDE)) {
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
		DefaultPieDataset dataset = new DefaultPieDataset();

		for (IdentificationSet identificationSet : idSets) {

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
		Collection<ProteinGroupOccurrence> proteinOccurrenceList2 = idSet.getProteinGroupOccurrenceList().values();
		for (ProteinGroupOccurrence proteinOccurrence : proteinOccurrenceList2) {
			if (proteinOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;

			List<ExtendedIdentifiedPeptide> peptides = proteinOccurrence.getPeptides();
			if (peptides != null && peptides.size() == 1)
				num++;

		}
		return num;
	}

	public static int getTotalNumSingleHitProteins(IdentificationSet idSet, Boolean countNonConclusiveProteins) {
		int num = 0;
		List<ProteinGroup> proteinGroups = idSet.getIdentifiedProteinGroups();
		for (ProteinGroup proteinGroup : proteinGroups) {
			if (proteinGroup.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;
			List<ExtendedIdentifiedPeptide> peptides = proteinGroup.getPeptides();
			if (peptides != null && peptides.size() == 1)
				num++;

		}
		return num;
	}

	public static PieDataset createTotalVsDifferentNumberIdentificationPieDataSet(List<IdentificationSet> idSets,
			IdentificationItemEnum plotItem, Boolean distinguishModifiedPeptides, boolean average,
			boolean countNonConclusiveProteins) {
		DefaultPieDataset dataset = new DefaultPieDataset();

		for (IdentificationSet identificationSet : idSets) {
			if (plotItem.equals(plotItem.PROTEIN)) {

				double numProteins = 0;

				if (average)
					numProteins = identificationSet
							.getAverageTotalVsDifferentNumProteinGroups(countNonConclusiveProteins);
				else
					numProteins = identificationSet.getTotalVsDifferentNumProteinGroups(countNonConclusiveProteins);

				if (numProteins > 0)
					dataset.setValue(identificationSet.getName(), numProteins);
			} else if (plotItem.equals(plotItem.PEPTIDE)) {
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
		DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
		try {

			for (IdentificationSet idSet : idSets) {
				String experimentName = idSet.getFullName();
				double coverage = 0;
				final double[] proteinCoverages = getProteinCoverages(idSet, retrieveProteinSeq,
						countNonConclusiveProteins);
				if (proteinCoverages != null) {
					Number[] coverageNums = new Number[proteinCoverages.length];
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

		} catch (IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static DefaultCategoryDataset createRepeatabilityCategoryDataSet(List<IdentificationSet> idSets,
			IdentificationItemEnum itemType, Boolean distModPeptides, int maximum, Boolean countNonConclusiveProteins) {
		// create the dataset...
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		try {

			for (IdentificationSet idSet : idSets) {
				String experimentName = idSet.getFullName();
				HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

				if (itemType.equals(IdentificationItemEnum.PEPTIDE)) {
					HashMap<String, PeptideOccurrence> peptideOccurrences = idSet
							.getPeptideOccurrenceList(distModPeptides);

					for (PeptideOccurrence occurrence : peptideOccurrences.values()) {

						Integer num = occurrence.getItemList().size();
						if (num > maximum)
							num = maximum;
						if (!map.containsKey(num)) {
							if (num > 0)
								map.put(num, 1);
						} else {
							Integer newNum = map.get(num) + 1;
							map.remove(num);
							map.put(num, newNum);
						}

					}
				} else if (itemType.equals(IdentificationItemEnum.PROTEIN)) {
					Collection<ProteinGroupOccurrence> proteinGroupOccurrences = idSet.getProteinGroupOccurrenceList()
							.values();

					for (ProteinGroupOccurrence occurrence : proteinGroupOccurrences) {
						if (occurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
							continue;
						Integer num = occurrence.getItemList().size();
						if (num > maximum)
							num = maximum;
						if (!map.containsKey(num)) {
							if (num > 0)
								map.put(num, 1);
						} else {
							Integer newNum = map.get(num) + 1;
							map.remove(num);
							map.put(num, newNum);
						}

					}
				}
				List<Integer> keys = new ArrayList<Integer>();
				for (Integer key : map.keySet())
					keys.add(key);
				Collections.sort(keys);
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

		} catch (IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static DefaultCategoryDataset createRepeatabilityOverReplicatesCategoryDataSet(
			List<IdentificationSet> idSets, IdentificationItemEnum itemType, Boolean distModPeptides, int maximum,
			Boolean countNonConclusiveProteins) {
		// create the dataset...
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		try {

			for (IdentificationSet parentIdSet : idSets) {
				HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
				final List<IdentificationSet> nextLevelIdentificationSetList = parentIdSet
						.getNextLevelIdentificationSetList();
				log.info(parentIdSet.getFullName() + " -> " + nextLevelIdentificationSetList.size());
				if (itemType.equals(IdentificationItemEnum.PROTEIN)) {
					Collection<ProteinGroupOccurrence> occurrences = parentIdSet.getProteinGroupOccurrenceList()
							.values();
					for (ProteinGroupOccurrence occurrence : occurrences) {
						if (occurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
							continue;
						int occurrencesOverReplicates = 0;
						try {
							for (IdentificationSet idSet : nextLevelIdentificationSetList) {
								// System.out.println(occurrence.getKey() +
								// ":"
								// +
								// occurrence.getIdentificationItemList().size());
								boolean isThere = false;

								isThere = idSet.hasProteinGroup(occurrence.getFirstOccurrence());

								if (isThere)
									occurrencesOverReplicates++;

							}
						} catch (UnsupportedOperationException e) {
							// do nothing
						}
						if (occurrencesOverReplicates > maximum)
							occurrencesOverReplicates = maximum;
						if (!map.containsKey(occurrencesOverReplicates)) {
							if (occurrencesOverReplicates > 0)
								map.put(occurrencesOverReplicates, 1);
						} else {
							Integer newNum = map.get(occurrencesOverReplicates) + 1;
							map.remove(occurrencesOverReplicates);
							map.put(occurrencesOverReplicates, newNum);
						}

					}
				} else if (itemType.equals(IdentificationItemEnum.PEPTIDE)) {
					Collection<PeptideOccurrence> occurrences = parentIdSet.getPeptideOccurrenceList(distModPeptides)
							.values();
					for (PeptideOccurrence occurrence : occurrences) {
						int occurrencesOverReplicates = 0;
						try {
							for (IdentificationSet idSet : nextLevelIdentificationSetList) {
								// System.out.println(occurrence.getKey() + ":"
								// +
								// occurrence.getIdentificationItemList().size());
								boolean isThere = false;

								isThere = idSet.hasPeptide(occurrence.getKey(), distModPeptides);

								if (isThere)
									occurrencesOverReplicates++;

							}
						} catch (UnsupportedOperationException e) {
							// do nothing
						}

						if (occurrencesOverReplicates > maximum)
							occurrencesOverReplicates = maximum;
						if (!map.containsKey(occurrencesOverReplicates)) {
							if (occurrencesOverReplicates > 0)
								map.put(occurrencesOverReplicates, 1);
						} else {
							Integer newNum = map.get(occurrencesOverReplicates) + 1;
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

		} catch (UnsupportedOperationException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createNumberModificationSitesCategoryDataSet(List<IdentificationSet> idSets,
			String[] modifications) {

		// create the dataset...
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (IdentificationSet idSet : idSets) {

				String experimentName = idSet.getFullName();
				for (String modification : modifications) {
					final int modificationOccurrence = idSet.getModificatedSiteOccurrence(modification);
					dataset.addValue(modificationOccurrence, experimentName, modification);
				}

			}

		} catch (IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createNumberModificatedPeptidesCategoryDataSet(List<IdentificationSet> idSets,
			String[] modifications) {

		// create the dataset...
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (IdentificationSet idSet : idSets) {

				String experimentName = idSet.getFullName();
				for (String modification : modifications) {
					final int modificationOccurrence = idSet.getPeptideModificatedOccurrence(modification);

					dataset.addValue(modificationOccurrence, experimentName, modification);
				}

			}

		} catch (IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createModificationDistributionCategoryDataSet(List<IdentificationSet> idSets,
			String[] modifications, int maximum) {

		// create the dataset...
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (IdentificationSet idSet : idSets) {
				String experimentName = idSet.getFullName();
				int maximumOccurrence = 0;
				for (String modification : modifications) {

					final HashMap<Integer, Integer> modificationOccurrence = idSet
							.getModificationOccurrenceDistribution(modification);
					if (modificationOccurrence != null && !modificationOccurrence.isEmpty()) {
						final Set<Integer> keySet = modificationOccurrence.keySet();
						List<Integer> keyList = toSortedList(keySet);
						for (Integer integer : keyList) {
							if (integer >= maximum)
								maximumOccurrence += modificationOccurrence.get(integer);

							else
								dataset.addValue(modificationOccurrence.get(integer),
										integer.toString() + " modif. sites", experimentName);
						}
						if (maximumOccurrence > 0)
							dataset.addValue(maximumOccurrence, String.valueOf(maximum) + " or +" + " modif. sites",
									experimentName);
					} else {
						dataset.addValue(0, "1" + " modif. site", experimentName);
					}
				}
			}

		} catch (IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createMissedCleavagesDistributionCategoryDataSet(List<IdentificationSet> idSets,
			int maximum) {
		int i = 0;

		// create the dataset...
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (IdentificationSet idSet : idSets) {
				String experimentName = idSet.getFullName();
				int maximumOccurrence = 0;
				final HashMap<Integer, Integer> missCleavageOccurrence = idSet
						.getMissedCleavagesOccurrenceDistribution();
				if (missCleavageOccurrence != null && !missCleavageOccurrence.isEmpty()) {
					final Set<Integer> keySet = missCleavageOccurrence.keySet();
					List<Integer> keyList = toSortedList(keySet);
					for (Integer integer : keyList) {
						if (integer >= maximum)
							maximumOccurrence += missCleavageOccurrence.get(integer);

						else
							dataset.addValue(missCleavageOccurrence.get(integer), integer.toString(), experimentName);
					}
					if (maximumOccurrence > 0)
						dataset.addValue(maximumOccurrence, String.valueOf(maximum) + " or +", experimentName);
				}
			}

		} catch (IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createProteinGroupTypesDistributionCategoryDataSet(List<IdentificationSet> idSets) {
		int i = 0;

		// create the dataset...
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (IdentificationSet idSet : idSets) {
				HashMap<ProteinEvidence, Integer> proteinEvidenceMap = new HashMap<ProteinEvidence, Integer>();

				String idSetName = idSet.getFullName();
				int maximumOccurrence = 0;
				final HashMap<String, ProteinGroupOccurrence> proteinGroupOccurrences = idSet
						.getProteinGroupOccurrenceList();
				if (proteinGroupOccurrences != null && !proteinGroupOccurrences.isEmpty()) {
					for (ProteinGroupOccurrence proteinGroupOcc : proteinGroupOccurrences.values()) {
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

		} catch (IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	public static CategoryDataset createPeptideMonitoringCategoryDataSet(List<IdentificationSet> idSets,
			List<String> peps, Boolean distinguishModPep) {

		// create the dataset...
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (IdentificationSet idSet : idSets) {
				String experimentName = idSet.getFullName();
				for (String originalSequence : peps) {
					final int occurrence = idSet.getPeptideChargeOccurrenceNumber(originalSequence, distinguishModPep);
					if (occurrence > 0)
						dataset.addValue(occurrence, experimentName, originalSequence);

				}

			}

		} catch (IllegalMiapeArgumentException ex) {

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
			List<ProteinGroupOccurrence> proteinOccurrenceList = new ArrayList<ProteinGroupOccurrence>();
			for (ProteinGroupOccurrence proteinGroupOccurrence : proteinOccurrenceSet) {
				proteinOccurrenceList.add(proteinGroupOccurrence);
			}
			dataset = new double[parentIdSet.getNumDifferentProteinGroups(countNonConclusiveProteins)][idSets.size()];
			int proteinIndex = 0;
			// sort from high occurrence to low occurrence
			SorterUtil.sortProteinGroupOcurrencesByOccurrence(proteinOccurrenceList);

			for (ProteinGroupOccurrence po : proteinOccurrenceList) {
				if (po.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
					continue;

				log.info(po.getItemList().size());
				int identSetIndex = 0;
				String key = po.getAccessionsString();
				rowList.add(key);
				int numOccurrence = 0;
				for (IdentificationSet nextLevelIdentSet : idSets) {
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
			List<PeptideOccurrence> peptideOccurrenceList = new ArrayList<PeptideOccurrence>();
			for (PeptideOccurrence occurrence : peptideOccurrenceCollection) {
				peptideOccurrenceList.add(occurrence);
			}
			// sort from high occurrence to low occurrence
			SorterUtil.sortPeptideOcurrencesByOccurrence(peptideOccurrenceList);
			log.info("Peptide occurrence list with " + peptideOccurrenceList.size() + " elements");
			dataset = new double[peptideOccurrenceList.size()][idSets.size()];
			int peptideIndex = 0;

			for (PeptideOccurrence po : peptideOccurrenceList) {
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
				for (IdentificationSet nextLevelIdentSet : idSets) {
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
		for (String rowName : rowList) {
			int j = 0;
			for (String colName : columnList) {
				double d = dataset[i][j];
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
	 * @param peptidesPerProtein
	 *            only applicable in case of proteins
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
		HashMap<Integer, List<String>> occurrenceRankingPep = new HashMap<Integer, List<String>>();
		// Firstly, to iterate over parent elements and fill the
		// occurrenceRanking, where the key is the number of idSets that
		// contains the item<br>
		// Then, from 1 to numberOfIdSets, build the heatmap
		final Collection<PeptideOccurrence> peptideOccurrenceSet = parentIdSet
				.getPeptideOccurrenceList(distiguishModificatedPeptides).values();
		dataset = new double[peptideOccurrenceSet.size()][idSets.size()];
		for (PeptideOccurrence peptideOccurrence : peptideOccurrenceSet) {
			int present = 0;
			for (IdentificationSet idSet : idSets) {
				if (idSet.getPeptideOccurrenceNumber(peptideOccurrence.getFirstOccurrence().getSequence(),
						distiguishModificatedPeptides) > 0) {
					present++;
				}
			}
			if (present > 0 && present >= minOccurrenceThreshold)
				if (!occurrenceRankingPep.containsKey(present)) {
					List<String> list = new ArrayList<String>();
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
				for (String peptideKey : peptideKeys) {
					int identSetIndex = 0;
					rowList.add(peptideKey);
					int numOccurrence = 0;
					for (IdentificationSet idSet : idSets) {
						final int peptideOcurrence = idSet.getPeptideOccurrenceNumber(peptideKey,
								distiguishModificatedPeptides);
						if (peptideOcurrence > 0)
							someValueIsMoreThanZero = true;
						final String columnName = idSet.getFullName();
						if (!columnList.contains(columnName))
							columnList.add(columnName);
						numOccurrence += peptideOcurrence;
						dataset[peptideIndex][identSetIndex] = peptideOcurrence;
						identSetIndex++;
					}
					peptideIndex++;
				}
		}

		if (!someValueIsMoreThanZero)
			throw new IllegalMiapeArgumentException("There is not data to show");
		double[][] ret = new double[rowList.size()][columnList.size()];
		for (int row = 0; row < rowList.size(); row++) {
			for (int column = 0; column < columnList.size(); column++) {
				ret[row][column] = dataset[row][column];
			}
		}

		// if there is a proteinACC filter, then, return just the data from the
		// proteins of the filter and in the appropriate order
		if (peptideSequenceOrder != null && !peptideSequenceOrder.isEmpty()) {
			double[][] newRet = getNewSortedData(peptideSequenceOrder, ret, rowList, columnList);
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
	 * @param peptidesPerProtein
	 *            only applicable in case of proteins
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
		HashMap<Integer, List<String>> occurrenceRankingPep = new HashMap<Integer, List<String>>();
		// Firstly, to iterate over parent elements and fill the
		// occurrenceRanking, where the key is the number of idSets that
		// contains the item<br>
		// Then, from 1 to numberOfIdSets, build the heatmap
		HashMap<Integer, List<ProteinGroup>> occurrenceRankingProt = new HashMap<Integer, List<ProteinGroup>>();

		final Collection<ProteinGroupOccurrence> proteinOccurrenceSet = parentIdSet.getProteinGroupOccurrenceList()
				.values();
		dataset = new double[proteinOccurrenceSet.size()][idSets.size()];

		for (ProteinGroupOccurrence proteinGroupOccurrence : proteinOccurrenceSet) {

			if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;
			int present = 0;
			for (IdentificationSet idSet : idSets) {
				if (idSet.getProteinGroupOccurrenceNumber(proteinGroupOccurrence.getFirstOccurrence()) > 0) {
					someValueIsMoreThanZero = true;
					present++;
				}
			}
			if (present > 0 && present >= minOccurrenceThreshold)
				if (!occurrenceRankingProt.containsKey(present)) {
					List<ProteinGroup> list = new ArrayList<ProteinGroup>();
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
				for (ProteinGroup proteinGroup : proteinGroups) {
					if (proteinGroup.getKey().contains("P01375"))
						System.out.println("ASDF");
					int identSetIndex = 0;
					List<String> accs = proteinGroup.getAccessions();
					String key = "";
					for (String acc : accs) {
						if (!"".equals(key))
							key += ",";
						key += acc;
					}
					rowList.add(key);
					for (IdentificationSet idSet : idSets) {
						int proteinOcurrence = idSet.getProteinGroupOccurrenceNumber(proteinGroup);
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

		if (!someValueIsMoreThanZero)
			throw new IllegalMiapeArgumentException("There is not data to show");
		double[][] ret = new double[rowList.size()][columnList.size()];
		for (int row = 0; row < rowList.size(); row++) {
			for (int column = 0; column < columnList.size(); column++) {
				ret[row][column] = dataset[row][column];
			}
		}
		// TODO put this variable in method parameters
		boolean sortAsACCFilter = true;
		// if there is a proteinACC filter, then, return just the data from the
		// proteins of the filter and in the appropriate order
		if (sortAsACCFilter) {
			if (proteinACCOrder != null && !proteinACCOrder.isEmpty()) {
				double[][] newRet = getNewSortedData(proteinACCOrder, ret, rowList, columnList);
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
	 * @param minOccurrenceThreshold
	 * @param countNonConclusiveProteins
	 * @param peptidesPerProtein
	 *            only applicable in case of proteins
	 * @return
	 */
	public static double[][] createPeptidesPerProteinHeapMapDataSet(IdentificationSet parentIdSet,
			List<IdentificationSet> idSets, List<String> rowList, List<String> columnList, List<String> proteinACCOrder,
			Boolean distiguishModificatedPeptides, int minOccurrenceThreshold, Boolean countNonConclusiveProteins,
			boolean isPSM) {

		if (rowList == null)
			rowList = new ArrayList<String>();

		if (columnList == null)
			columnList = new ArrayList<String>();
		double[][] dataset = null;

		boolean someValueIsMoreThanZero = false;
		HashMap<Integer, List<String>> occurrenceRankingPep = new HashMap<Integer, List<String>>();
		// Firstly, to iterate over parent elements and fill the
		// occurrenceRanking, where the key is the number of idSets that
		// contains the item<br>
		// Then, from 1 to numberOfIdSets, build the heatmap
		// ranking the proteins by the number of peptides they have
		HashMap<Integer, List<ProteinGroup>> peptideNumberRankingProt = new HashMap<Integer, List<ProteinGroup>>();
		log.info("Creating peptides per protein heatmap: PSM=" + isPSM);
		final Collection<ProteinGroupOccurrence> proteinOccurrenceSet = parentIdSet.getProteinGroupOccurrenceList()
				.values();
		dataset = new double[proteinOccurrenceSet.size()][idSets.size()];
		int maxNumberOfPeptidesPerProtein = 0;
		for (ProteinGroupOccurrence proteinGroupOccurrence : proteinOccurrenceSet) {

			if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;
			int totalPeptidesForThatProtein = 0;
			for (IdentificationSet idSet : idSets) {
				ProteinGroupOccurrence proteinGroupOccurrenceIdSet = idSet
						.getProteinGroupOccurrence(proteinGroupOccurrence.getFirstOccurrence());
				if (proteinGroupOccurrenceIdSet != null && proteinGroupOccurrence.getPeptides() != null) {
					someValueIsMoreThanZero = true;
					int size = 0;
					if (isPSM) {
						size = proteinGroupOccurrence.getPeptides().size();
					} else {
						size = proteinGroupOccurrence.getPeptides().size();
						size = DataManager.createPeptideOccurrenceList(proteinGroupOccurrence.getPeptides(),
								distiguishModificatedPeptides).size();
					}
					totalPeptidesForThatProtein += size;
				}
			}
			if (totalPeptidesForThatProtein > 0 && totalPeptidesForThatProtein >= minOccurrenceThreshold)
				if (maxNumberOfPeptidesPerProtein < totalPeptidesForThatProtein)
					maxNumberOfPeptidesPerProtein = totalPeptidesForThatProtein;
			if (!peptideNumberRankingProt.containsKey(totalPeptidesForThatProtein)) {
				List<ProteinGroup> list = new ArrayList<ProteinGroup>();
				list.add(proteinGroupOccurrence.getFirstOccurrence());
				peptideNumberRankingProt.put(totalPeptidesForThatProtein, list);
			} else {
				peptideNumberRankingProt.get(totalPeptidesForThatProtein)
						.add(proteinGroupOccurrence.getFirstOccurrence());
			}

		}
		int proteinIndex = 0;
		for (int peptidesPerprotein = maxNumberOfPeptidesPerProtein; peptidesPerprotein > 0; peptidesPerprotein--) {
			final List<ProteinGroup> proteinGroups = peptideNumberRankingProt.get(peptidesPerprotein);
			if (proteinGroups != null)
				for (ProteinGroup proteinGroup : proteinGroups) {
					int identSetIndex = 0;
					List<String> accs = proteinGroup.getAccessions();
					String key = "";
					for (String acc : accs) {
						if (!"".equals(key))
							key += ",";
						key += acc;
					}
					rowList.add(key);
					for (IdentificationSet idSet : idSets) {
						ProteinGroupOccurrence proteinGroupOccurrence = idSet.getProteinGroupOccurrence(proteinGroup);
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

		if (!someValueIsMoreThanZero)
			throw new IllegalMiapeArgumentException("There is not data to show");
		double[][] ret = new double[rowList.size()][columnList.size()];
		for (int row = 0; row < rowList.size(); row++) {
			for (int column = 0; column < columnList.size(); column++) {
				ret[row][column] = dataset[row][column];
			}
		}

		// if there is a proteinACC filter, then, return just the data from the
		// proteins of the filter and in the appropriate order
		if (proteinACCOrder != null && !proteinACCOrder.isEmpty()) {
			double[][] newRet = getNewSortedData(proteinACCOrder, ret, rowList, columnList);
			rowList.clear();
			rowList.addAll(proteinACCOrder);
			return newRet;
		}
		return ret;
	}

	private static double[][] getNewSortedData(List<String> newOrder, double[][] ret, List<String> rowList,
			List<String> columnList) {
		double[][] newRet = new double[newOrder.size()][columnList.size()];
		int newRow = 0;

		// search each new proteinKey in the ret array and put it in the new
		// one
		for (String proteinKey : newOrder) {

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
	 * @param binary
	 *            : if true, the heatmap will have only two values (1 or 0)
	 *            depending on if the peptide is present or not. If false, the
	 *            number of each cell will be the number of occurrences of each
	 *            peptide
	 * @return
	 */
	public static double[][] createPeptidePresencyHeapMapDataSet(List<IdentificationSet> idSets, List<String> sequences,
			Boolean distinguishModificatedPeptides, boolean binary) {

		// one columns per idSet and one row per peptide string in rowList
		double[][] dataset = new double[sequences.size()][idSets.size()];
		boolean atLeastOneData = false;
		int numRow = 0;
		for (String peptideString : sequences) {
			int numColumn = 0;
			for (IdentificationSet idSet : idSets) {
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
			boolean applyLog, boolean separateDecoyHits, Boolean countNonConclusiveProteins) {
		HistogramDataset dataset = new HistogramDataset();

		for (IdentificationSet idSet : idSets) {
			List<double[]> values = null;
			if (plotItem.equals(IdentificationItemEnum.PROTEIN)) {
				values = getProteinScores(idSet, scoreName, addZeroZeroValue, applyLog, separateDecoyHits,
						countNonConclusiveProteins);
			} else if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
				values = getPeptideScores(idSet, scoreName, addZeroZeroValue, applyLog, separateDecoyHits);
			}
			if (values != null) {
				for (int i = 0; i < values.size(); i++) {
					double[] ds = values.get(i);
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
		HistogramDataset dataset = new HistogramDataset();

		for (IdentificationSet idSet : idSets) {
			double[] values = getProteinCoverages(idSet, retrieveProteinSeq, countNonConclusiveProteins);
			if (values != null && values.length > 0)
				dataset.addSeries(idSet.getFullName(), values, bins);
		}
		dataset.setType(histogramType);
		return dataset;
	}

	public static HistogramDataset createNumPeptidesPerProteinMass(List<IdentificationSet> idSets, int bins,
			HistogramType histogramType, boolean retrieveFromInternet, Boolean countNonConclusiveProteins) {
		HistogramDataset dataset = new HistogramDataset();

		for (IdentificationSet idSet : idSets) {
			double[] values = getNumPeptidesPerProteinMass(idSet, retrieveFromInternet, countNonConclusiveProteins);
			if (values != null && values.length > 0)
				dataset.addSeries(idSet.getFullName(), values, bins);
		}
		dataset.setType(histogramType);
		return dataset;
	}

	private static double[] getNumPeptidesPerProteinMass(IdentificationSet idSet, boolean retrieveFromInternet,
			Boolean countNonConclusiveProteins) {
		List<Double> values = new ArrayList<Double>();
		List<ProteinGroup> identifiedProteinGroups = idSet.getIdentifiedProteinGroups();
		if (identifiedProteinGroups != null) {

			int i = 0;
			for (ProteinGroup proteinGroup : identifiedProteinGroups) {
				if (proteinGroup.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
					continue;
				List<Integer> numPeptideList = new ArrayList<Integer>();
				for (ExtendedIdentifiedProtein protein : proteinGroup) {
					List<ExtendedIdentifiedPeptide> peptides = protein.getPeptides();
					if (peptides != null)
						numPeptideList.add(peptides.size());
				}

				if (numPeptideList != null && !numPeptideList.isEmpty()) {
					Double numPeptidesPerProtein = getMeanFromList(numPeptideList);
					String proteinSequence = proteinGroup.getProteinSequence(retrieveFromInternet);
					if (proteinSequence != null) {
						Protein prot = new Protein(new AASequenceImpl(proteinSequence));
						double mass = prot.getMass();

						double log10 = Math.log(numPeptidesPerProtein / mass);
						// log.info(numPeptidesPerProtein + " " + mass + " "
						// + log10);
						values.add(log10);

					}
				}

			}
		}
		double[] ret = new double[values.size()];
		int i = 0;
		for (double d : values) {
			ret[i] = d;
			i++;
		}
		return ret;
	}

	private static Double getMeanFromList(List<Integer> list) {
		int sum = 0;
		if (list != null && !list.isEmpty()) {
			for (Integer integer : list) {
				sum = sum + integer;
			}
			return Double.valueOf(sum / list.size());
		}
		return null;
	}

	public static HistogramDataset createPeptideMassHistogramDataSet(List<IdentificationSet> idSets, int bins,
			HistogramType histogramType, boolean mOverz) {
		HistogramDataset dataset = new HistogramDataset();

		for (IdentificationSet idSet : idSets) {
			double[] values = getPeptideMasses(idSet, mOverz);
			if (values != null && values.length > 0)
				dataset.addSeries(idSet.getFullName(), values, bins);
		}
		dataset.setType(histogramType);
		return dataset;
	}

	public static DefaultCategoryDataset createPeptideLengthHistogramDataSet(List<IdentificationSet> idSets,
			int minimum, int maximum) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		HashMap<IdentificationSet, Map<Integer, Integer>> map = new HashMap<IdentificationSet, Map<Integer, Integer>>();
		for (IdentificationSet idSet : idSets) {
			Map<Integer, Integer> values = getPeptideLengths(idSet);
			map.put(idSet, values);
		}

		for (IdentificationSet idSet : idSets) {
			int maxLength = 0;
			int minLength = 0;
			Map<Integer, Integer> values = map.get(idSet);
			if (values != null) {
				for (int length = 1; length < 100; length++) {
					if (values.containsKey(length)) {
						final Integer numPeptidesWithThisLength = values.get(length);
						if (length >= maximum) {
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
			for (ExtendedIdentifiedPeptide peptide : peptides) {
				try {
					final String experimentalMassToCharge = peptide.getExperimentalMassToCharge();
					if (experimentalMassToCharge != null) {
						if (mOverz) {
							Double mz = Double.valueOf(experimentalMassToCharge);
							ret[i] = mz;
						} else {
							try {
								int charge = Integer.valueOf(peptide.getCharge());
								Double mz = Double.valueOf(experimentalMassToCharge);
								if (charge > 0)

									ret[i] = mz * Double.valueOf(charge);
							} catch (NumberFormatException e) {

							}
						}
					} else {
						try {
							if (mOverz) {
								try {
									int charge = Integer.valueOf(peptide.getCharge());
									Double mz = Double.valueOf(peptide.getTheoreticMass());
									if (charge > 0)

										ret[i] = mz / Double.valueOf(charge);
								} catch (NumberFormatException e) {

								}
							} else {
								ret[i] = peptide.getTheoreticMass();

							}
						} catch (IllegalArgumentException e) {

						}
					}
				} catch (NumberFormatException ex) {
					// do nothing
				}
				i++;
			}
		}
		return ret;
	}

	private static Map<Integer, Integer> getPeptideLengths(IdentificationSet idSet) {
		Map<Integer, Integer> ret = new HashMap<Integer, Integer>();

		final List<ExtendedIdentifiedPeptide> peptides = idSet.getIdentifiedPeptides();
		if (peptides != null) {
			for (ExtendedIdentifiedPeptide peptide : peptides) {
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
		return ret;
	}

	public static DefaultCategoryDataset createPeptideChargeHistogramDataSet(List<IdentificationSet> idSets) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		List<HashMap<Integer, Integer>> totalList = new ArrayList<HashMap<Integer, Integer>>();
		for (IdentificationSet idSet : idSets) {
			int[] values = getPeptideCharges(idSet);
			HashMap<Integer, Integer> chargeHash = new HashMap<Integer, Integer>();
			for (int charge : values) {
				if (charge > 0)
					if (chargeHash.containsKey(charge)) {
						Integer chargeOccurrence = chargeHash.get(charge);
						chargeOccurrence++;
						chargeHash.remove(charge);
						chargeHash.put(charge, chargeOccurrence);
					} else {
						chargeHash.put(charge, 1);
					}
			}
			List<Integer> chargeList = new ArrayList<Integer>();
			for (Integer integer : chargeHash.keySet()) {
				chargeList.add(integer);
			}
			Collections.sort(chargeList);
			for (int charge : chargeList) {
				dataset.addValue(chargeHash.get(charge), String.valueOf(charge), idSet.getFullName());
			}

		}
		return dataset;
	}

	private static int[] getPeptideCharges(IdentificationSet idSet) {
		int[] ret = null;

		final List<ExtendedIdentifiedPeptide> peptides = idSet.getIdentifiedPeptides();
		if (peptides != null) {
			ret = new int[peptides.size()];
			int i = 0;
			for (ExtendedIdentifiedPeptide peptide : peptides) {
				try {
					int charge = Integer.valueOf(peptide.getCharge());
					if (charge > 0)
						ret[i] = charge;
				} catch (NumberFormatException ex) {

				}
				i++;
			}
		}
		return ret;
	}

	private static double[] getProteinCoverages(IdentificationSet idSet, boolean retrieveProteinSeq,
			Boolean countNonConclusiveProteins) {

		double[] ret = null;
		final Collection<ProteinGroupOccurrence> proteinOccurrences = idSet.getProteinGroupOccurrenceList().values();
		if (proteinOccurrences != null && !proteinOccurrences.isEmpty()) {
			ret = new double[idSet.getNumDifferentProteinGroups(countNonConclusiveProteins)];
			int i = 0;
			for (ProteinGroupOccurrence proteinGroupOccurrence : proteinOccurrences) {
				if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE
						&& !countNonConclusiveProteins)
					continue;
				try {
					if (proteinGroupOccurrence.getMeanProteinCoverage(retrieveProteinSeq) != null) {
						ret[i] = 100 * proteinGroupOccurrence.getMeanProteinCoverage(retrieveProteinSeq);
					}
				} catch (Exception ex) {
					// do nothing
				}
				i++;

			}
		}
		return ret;
	}

	private static double max(double[] vals) {
		double max = Double.MIN_VALUE;
		for (double d : vals) {
			if (d > max)
				max = d;
		}
		return max;
	}

	private static double min(double[] vals) {
		double min = Double.MAX_VALUE;
		for (double d : vals) {
			if (d < min)
				min = d;
		}
		return min;
	}

	/**
	 * Creates a {@link XYDataset} od the scores
	 *
	 * @param idsets
	 *            list of identification datasets
	 * @param scoreName
	 *            the score name
	 * @param plotItem
	 *            peptide or protein
	 * @param applyLog
	 * @param parent
	 *            needed to know which is the best score from each
	 *            identification occurrence
	 * @return
	 */
	public static XYDataset createScoreXYDataSet(List<IdentificationSet> idsets, String scoreName,
			IdentificationItemEnum plotItem, boolean distinguish, boolean applyLog, boolean separateDecoyHits,
			Boolean countNonConclusiveProteins) {

		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		if (idsets.size() < 2)
			throw new IllegalMiapeArgumentException("At least two series are needed to paint this chart");
		boolean thereisData = false;
		for (int i = 0; i < idsets.size(); i++) {
			for (int j = i + 1; j < idsets.size(); j++) {
				IdentificationSet idSet1 = idsets.get(i);
				IdentificationSet idSet2 = idsets.get(j);
				List<XYSeries> series = getXYScoreSeries(idSet1, idSet2, scoreName, plotItem, distinguish, applyLog,
						separateDecoyHits, countNonConclusiveProteins);
				for (XYSeries xySeries : series) {
					if (xySeries.getItems() != null && xySeries.getItems().size() > 0)
						thereisData = true;
					xySeriesCollection.addSeries(xySeries);
				}

			}
		}
		if (!thereisData)
			throw new IllegalMiapeArgumentException(
					"There is not data to show. Please, be sure that the datasets contains the score '" + scoreName
							+ "' and that the overlapping is not zero.");
		return xySeriesCollection;
	}

	public static XYDataset createDeltaMzOverMzXYDataSet(List<IdentificationSet> idsets) {

		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();

		for (IdentificationSet identificationSet : idsets) {
			List<ExtendedIdentifiedPeptide> identifiedPeptides = identificationSet.getIdentifiedPeptides();
			XYSeries serie = new XYSeries(identificationSet.getName());
			for (ExtendedIdentifiedPeptide extendedIdentifiedPeptide : identifiedPeptides) {
				try {
					Double experimentalMZ = Double.valueOf(extendedIdentifiedPeptide.getExperimentalMassToCharge());
					Double theoreticalMZ = Double.valueOf(extendedIdentifiedPeptide.getCalculatedMassToCharge());
					if (experimentalMZ != 0.0 && theoreticalMZ != 0.0) {
						Double deltaMZ = (experimentalMZ - theoreticalMZ);
						if (deltaMZ < 1 && deltaMZ > -1)
							serie.add(experimentalMZ, deltaMZ);
					}
				} catch (Exception e) {

				}

			}

			xySeriesCollection.addSeries(serie);

		}

		return xySeriesCollection;
	}

	/**
	 *
	 * @param idSets
	 * @param plotItem
	 * @param filter
	 * @param option
	 *            in case of plotItem==PROTEIN, this parameter indicate if a the
	 *            FDR will take into account the best hit of each protein (YES)
	 *            or just all the cases (NO) or both (BOTH)
	 * @return
	 */
	public static XYDataset createFDRDataSet(List<IdentificationSet> idSets, boolean showProteinLevel,
			boolean showPeptideLevel, boolean showPSMLevel, Boolean countNonConclusiveProteins) {
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		String error = null;
		if (!showPeptideLevel && !showProteinLevel && !showPSMLevel)
			throw new IllegalMiapeArgumentException("<html>Select either protein, peptide or PSM level</html>");
		for (IdentificationSet idSet : idSets) {

			FDRFilter filter = idSet.getFDRFilter();
			if (filter == null) {
				final String errorMessage = "<html>Error:  The FDR cannot be calculated or an FDR filter is not defined:<br>"
						+ "<ul><li>If you have already applied an FDR filter, the global FDR cannot be calculated.<br>"
						+ "You can see the reasons by placing the coursor above the message on left-top of the window: 'Global FDR cannot be calculated'</li>"
						+ "<li>If you have not applied an FDR filter, apply it in order to select the score name as well<br>"
						+ "as the way to select the DECOY hits in which the FDR calculation in going to be based</li></ul></html>";

				throw new IllegalMiapeArgumentException(errorMessage);
			}

			// log.info("calculating FDR from :" + idSet.getFullName());
			double[] values = null;
			// if they are proteins and we have to paint both redundant and non
			// redundant FDR lines:
			try {
				final boolean autoSort = false;
				final boolean allowDuplicateXValues = true;
				log.info("Getting XYFDRSerie from " + idSet.getFullName());

				if (showPSMLevel) {
					XYSeries xySeries = new XYSeries(idSet.getFullName() + "(PSM)", autoSort, allowDuplicateXValues);
					xySeries.add(0, 0);
					List<Double> fdrArray = null;

					List<ExtendedIdentifiedPeptide> peptides = idSet.getIdentifiedPeptides();

					SorterUtil.sortPeptidesByPeptideScore(peptides, filter.getSortingParameters().getScoreName(), true);

					long numFWHits = 0; // forward hits
					long numDCHits = 0; // decoy hits
					double previousFDRPoint = 0;
					int i = 1;
					for (ExtendedIdentifiedPeptide peptide : peptides) {
						// System.out.println(proteinACC);
						// if (peptideOccurrence.isDecoy(filter)) {
						if (peptide.isDecoy(filter)) {
							numDCHits++;
							peptide.setDecoy(true);
						} else {
							numFWHits++;
							peptide.setDecoy(false);
						}

						float currentFDR = filter.calculateFDR(numFWHits, numDCHits);
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
					XYSeries xySeries = new XYSeries(idSet.getFullName() + "(Pep)", autoSort, allowDuplicateXValues);
					xySeries.add(0, 0);
					List<Double> fdrArray = null;

					// Collection<PeptideOccurrence> peptideOccurrenceCollection
					// = DataManager
					// .createPeptideOccurrenceList(peptides, false)
					// .values();

					Collection<PeptideOccurrence> peptideOccurrenceCollection = idSet.getPeptideOccurrenceList(false)
							.values();
					List<PeptideOccurrence> peptideOccurrenceList = new ArrayList<PeptideOccurrence>();
					for (PeptideOccurrence identificationOccurrence : peptideOccurrenceCollection) {
						peptideOccurrenceList.add(identificationOccurrence);
					}
					SorterUtil.sortPeptideOcurrencesByPeptideScore(peptideOccurrenceList,
							filter.getSortingParameters().getScoreName());

					long numFWHits = 0; // forward hits
					long numDCHits = 0; // decoy hits
					double previousFDRPoint = 0;
					int i = 1;
					for (PeptideOccurrence peptideOccurrence : peptideOccurrenceList) {
						ExtendedIdentifiedPeptide bestPeptideByScore = peptideOccurrence.getBestPeptide();

						// System.out.println(proteinACC);
						// if (peptideOccurrence.isDecoy(filter)) {
						if (bestPeptideByScore.isDecoy(filter)) {
							numDCHits++;
							peptideOccurrence.setDecoy(true);
						} else {
							numFWHits++;
							peptideOccurrence.setDecoy(false);
						}

						float currentFDR = filter.calculateFDR(numFWHits, numDCHits);
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
					XYSeries xySeries = new XYSeries(idSet.getFullName() + "(Prot)", autoSort, allowDuplicateXValues);
					xySeries.add(0, 0);
					List<Double> fdrArray = null;
					// List<ProteinGroup> proteinGroups = idSet
					// .getIdentifiedProteinGroups();
					// Collection<ProteinGroupOccurrence>
					// proteinGroupOccurrencesSet = DataManager
					// .createProteinGroupOccurrenceList(proteinGroups)
					// .values();

					Collection<ProteinGroupOccurrence> proteinGroupOccurrencesSet = idSet
							.getProteinGroupOccurrenceList().values();

					List<ProteinGroupOccurrence> proteinGroupOccurrences = new ArrayList<ProteinGroupOccurrence>();
					for (ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrencesSet) {
						proteinGroupOccurrences.add(proteinGroupOccurrence);
					}

					SorterUtil.sortProteinGroupOcurrencesByPeptideScore(proteinGroupOccurrences,
							idSet.getFDRFilter().getSortingParameters().getScoreName());

					long numFWHits = 0; // forward hits
					long numDCHits = 0; // decoy hits
					double previousFDRPoint = 0;
					List<ProteinGroup> proteinGroupList = new ArrayList<ProteinGroup>();
					int i = 1;
					for (ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrences) {
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

						float currentFDR = filter.calculateFDR(numFWHits, numDCHits);
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

			} catch (IllegalMiapeArgumentException e) {
				error = e.getMessage();
			}
		}
		if (xySeriesCollection.getSeries() == null || xySeriesCollection.getSeries().isEmpty())
			throw new IllegalMiapeArgumentException(error);
		return xySeriesCollection;
	}

	private static List<XYSeries> getXYScoreSeries(IdentificationSet idSet1, IdentificationSet idSet2, String scoreName,
			IdentificationItemEnum plotItem, boolean distinguish, boolean applyLog, boolean separateDecoyHits,
			Boolean countNonConclusiveProteins) {

		XYSeries normalSeries = new XYSeries(idSet1.getName() + " vs " + idSet2.getName());
		XYSeries decoySeries = null;
		if (separateDecoyHits) {
			decoySeries = new XYSeries(idSet1.getName() + " vs " + idSet2.getName() + " (decoy)");
		}

		// Foreach protein in replicate2, look it in the hashmap and add an XY
		// point to the
		// series

		if (plotItem.equals(IdentificationItemEnum.PEPTIDE)) {
			HashMap<String, PeptideOccurrence> peptideOccurrences1 = idSet1.getPeptideOccurrenceList(distinguish);
			HashMap<String, PeptideOccurrence> peptideOccurrences2 = idSet2.getPeptideOccurrenceList(distinguish);
			if (separateDecoyHits) {
				peptideOccurrences1 = DataManager
						.createPeptideOccurrenceListInParallel(idSet1.getNonFilteredIdentifiedPeptides(), true);
				peptideOccurrences2 = DataManager
						.createPeptideOccurrenceListInParallel(idSet2.getNonFilteredIdentifiedPeptides(), true);
			}

			for (PeptideOccurrence occurrence2 : peptideOccurrences2.values()) {

				if (peptideOccurrences1.containsKey(occurrence2.getKey())) {
					try {
						final PeptideOccurrence occurrence1 = peptideOccurrences1.get(occurrence2.getKey());
						Float x = occurrence1.getBestPeptideScore(scoreName);
						if (x != null && applyLog)
							x = Float.valueOf(String.valueOf(Math.log10(x)));
						Float y = occurrence2.getBestPeptideScore(scoreName);
						if (y != null && applyLog)
							y = Float.valueOf(String.valueOf(Math.log10(y)));
						if (x != null && y != null) {
							if (separateDecoyHits && (occurrence1.isDecoy() || occurrence2.isDecoy()))
								decoySeries.add(x, y);
							else
								normalSeries.add(x, y);
						}
					} catch (IllegalMiapeArgumentException e) {
						// do nothing, not plot it
					}
				}
			}
		} else {
			Collection<ProteinGroupOccurrence> occurrenceList1 = idSet1.getProteinGroupOccurrenceList().values();
			HashMap<String, ProteinGroupOccurrence> occurrenceMap1 = new HashMap<String, ProteinGroupOccurrence>();
			for (ProteinGroupOccurrence proteinGroupOccurrence : occurrenceList1) {
				occurrenceMap1.put(proteinGroupOccurrence.toString(), proteinGroupOccurrence);
			}
			Collection<ProteinGroupOccurrence> occurrenceList2 = idSet2.getProteinGroupOccurrenceList().values();
			HashMap<String, ProteinGroupOccurrence> occurrenceMap2 = new HashMap<String, ProteinGroupOccurrence>();
			for (ProteinGroupOccurrence proteinGroupOccurrence : occurrenceList2) {
				occurrenceMap2.put(proteinGroupOccurrence.toString(), proteinGroupOccurrence);
			}

			for (String key : occurrenceMap2.keySet()) {
				ProteinGroupOccurrence occurrence2 = occurrenceMap2.get(key);
				if (occurrence2.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
					continue;
				if (occurrenceMap1.containsKey(key)) {
					try {
						final ProteinGroupOccurrence occurrence1 = occurrenceMap1.get(key);
						Float x = occurrence1.getBestProteinScore(scoreName);
						Float y = occurrence2.getBestProteinScore(scoreName);
						if (x != null && y != null) {
							if (separateDecoyHits && (occurrence1.isDecoy() || occurrence2.isDecoy()))
								decoySeries.add(x, y);
							else
								normalSeries.add(x, y);
						}
					} catch (IllegalMiapeArgumentException e) {
						// do nothing, not plot it
					}
				}
			}
		}

		List<XYSeries> ret = new ArrayList<XYSeries>();
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

		List<Double> scores = new ArrayList<Double>();
		List<Double> scoresDecoy = new ArrayList<Double>();
		if (addZeroZeroValue) {
			scores.add(0.0);
			scoresDecoy.add(0.0);
		}
		for (Object object : idSet.getIdentifiedProteinGroups()) {
			ProteinGroup proteinGroup = (ProteinGroup) object;
			if (proteinGroup.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;
			Float score = proteinGroup.getBestProteinScore(scoreName);
			if (score != null) {
				double doubleValue = score.doubleValue();
				if (applyLog)
					doubleValue = Math.log10(doubleValue);

				if (separateDecoyHits && proteinGroup.isDecoy()) {
					scoresDecoy.add(doubleValue);
				} else {
					scores.add(doubleValue);
				}
			}

		}
		List<double[]> ret = new ArrayList<double[]>();
		double[] retNormal = new double[scores.size()];
		int i = 0;
		for (Double d : scores) {
			retNormal[i] = d;
			i++;
		}
		ret.add(retNormal);
		if (separateDecoyHits) {
			double[] retDecoy = new double[scoresDecoy.size()];
			int j = 0;
			for (Double d : scoresDecoy) {
				retDecoy[j] = d;
				j++;
			}
			ret.add(retDecoy);
		}
		return ret;
	}

	private static List<double[]> getPeptideScores(IdentificationSet idSet, String scoreName, boolean addZeroZeroValue,
			boolean applyLog, boolean separateDecoyHits) {

		List<Double> scores = new ArrayList<Double>();
		List<Double> scoresDecoy = new ArrayList<Double>();
		if (addZeroZeroValue) {
			scores.add(0.0);
			scoresDecoy.add(0.0);
		}
		for (Object object : idSet.getIdentifiedPeptides()) {
			ExtendedIdentifiedPeptide peptide = (ExtendedIdentifiedPeptide) object;
			try {
				Float score = peptide.getScore(scoreName);
				if (score != null) {
					double doubleValue = score.doubleValue();
					if (applyLog)
						doubleValue = Math.log10(doubleValue);
					if (separateDecoyHits && peptide.isDecoy()) {
						scoresDecoy.add(doubleValue);
					} else {
						scores.add(doubleValue);
					}
				}
			} catch (Exception e) {
				// do nothign
			}
		}
		List<double[]> ret = new ArrayList<double[]>();
		double[] retNormal = new double[scores.size()];
		int i = 0;
		for (Double d : scores) {
			retNormal[i] = d;
			i++;
		}
		ret.add(retNormal);
		if (separateDecoyHits) {
			double[] retDecoy = new double[scoresDecoy.size()];
			int j = 0;
			for (Double d : scoresDecoy) {
				retDecoy[j] = d;
				j++;
			}
			ret.add(retDecoy);
		}
		return ret;
	}

	public static List<Integer> toSortedList(Set<Integer> list) {
		if (list == null)
			return null;
		List<Integer> ret = new ArrayList<Integer>();
		for (Integer integer : list) {
			ret.add(integer);
		}
		Collections.sort(ret);
		return ret;
	}

	public static int[] toArray(List<Integer> list) {
		int[] ret = null;
		if (list != null && !list.isEmpty()) {
			ret = new int[list.size()];
			int i = 0;
			for (int value : list) {
				ret[i] = value;
			}
		}
		return ret;

	}

	public static double arrayAverage(Object[] nums) {
		List<Integer> integrs = new ArrayList<Integer>();
		for (Object num : nums) {
			integrs.add((Integer) num);
		}

		double result = 0.0;

		for (Integer integer : integrs) {
			result = result + integer;
		}

		result = result / integrs.size();
		log.info("Average is =" + result);
		return result;
	}

	public static String toStringList(Object[] modifications, String separator) {
		String ret = "";
		if (modifications != null && modifications.length > 0) {
			for (Object element : modifications) {
				if (!"".equals(ret))
					ret = ret + separator;
				ret = ret + element;
			}
		}
		return ret;
	}

	public static List<String> toSortedList(String[] array) {
		log.info("Sorting array");
		List<String> list = new ArrayList<String>();

		if (array != null && array.length > 0) {
			for (String string : array) {
				list.add(string);
			}
			Collections.sort(list);

		}
		log.info("Array sorted");
		return list;
	}

	public static String[] toSortedArray(String[] array) {
		List<String> list = toSortedList(array);
		String[] ret = null;
		if (list != null && !list.isEmpty()) {
			ret = new String[list.size()];
			int i = 0;
			for (String string : list) {
				ret[i] = string;
				i++;
			}
		}
		return ret;
	}

	public static CategoryDataset createProteinSensitivityCategoryDataSet(List<IdentificationSet> idSets,
			HashSet<String> proteinsInSample, boolean countNonConclusiveProteins, boolean sensitivity, boolean accuracy,
			boolean specificity, boolean precision, boolean npv, boolean fdr) {
		// create the dataset...
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String error = null;
		try {

			for (IdentificationSet idSet : idSets) {
				String experimentName = idSet.getFullName();
				int tp = idSet.getProteinGroupTP(proteinsInSample, countNonConclusiveProteins);
				int fn = idSet.getProteinGroupFN(proteinsInSample, countNonConclusiveProteins);
				int tn = idSet.getProteinGroupTN(proteinsInSample, countNonConclusiveProteins);
				int fp = idSet.getProteinGroupFP(proteinsInSample, countNonConclusiveProteins);
				if (sensitivity && (tp + fn) > 0) {
					double value = tp / Double.valueOf(tp + fn);
					dataset.addValue(value, "sensitivity", experimentName);
				}

				if (accuracy && (tp + tn + fp + fn) > 0) {
					double value = (tp + tn) / Double.valueOf(tp + tn + fp + fn);
					dataset.addValue(value, "accuracy", experimentName);
				}
				if (specificity && tn + fp > 0) {
					double value = tn / Double.valueOf(tn + fp);
					dataset.addValue(value, "specifity", experimentName);
				}
				if (precision && (tp + fp) > 0) {
					double value = Double.valueOf(tp) / Double.valueOf(tp + fp);
					dataset.addValue(value, "precision", experimentName);
				}
				if (npv && (tn + fn) > 0) {
					double value = Double.valueOf(tn) / Double.valueOf(tn + fn);
					dataset.addValue(value, "npv", experimentName);
				}
				if (fdr && (fp + tp) > 0) {
					double value = Double.valueOf(fp) / Double.valueOf(fp + tp);
					dataset.addValue(value, "fdr", experimentName);
				}
			}

		} catch (IllegalMiapeArgumentException ex) {
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
			HashSet<String> peptidesInSample, boolean distinguisModificatedPeptides, boolean sensitivity,
			boolean accuracy, boolean specificity, boolean precision, boolean npv, boolean fdr) {
		// create the dataset...
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String error = null;
		try {

			for (IdentificationSet idSet : idSets) {
				String experimentName = idSet.getFullName();
				int tp = idSet.getPeptideTP(peptidesInSample, distinguisModificatedPeptides);
				int fn = idSet.getPeptideFN(peptidesInSample, distinguisModificatedPeptides);
				int tn = idSet.getPeptideTN(peptidesInSample, distinguisModificatedPeptides);
				int fp = idSet.getPeptideFP(peptidesInSample, distinguisModificatedPeptides);
				if (sensitivity && (tp + fn) > 0) {
					double value = tp / Double.valueOf(tp + fn);
					dataset.addValue(value, "sensitivity", experimentName);
				}

				if (accuracy && (tp + tn + fp + fn) > 0) {
					double value = (tp + tn) / Double.valueOf(tp + tn + fp + fn);
					dataset.addValue(value, "accuracy", experimentName);
				}
				if (specificity && tn + fp > 0) {
					double value = tn / Double.valueOf(tn + fp);
					dataset.addValue(value, "specifity", experimentName);
				}
				if (precision && (tp + fp) > 0) {
					double value = Double.valueOf(tp) / Double.valueOf(tp + fp);
					dataset.addValue(value, "precision", experimentName);
				}
				if (npv && (tn + fn) > 0) {
					double value = Double.valueOf(tn) / Double.valueOf(tn + fn);
					dataset.addValue(value, "npv", experimentName);
				}
				if (fdr && (fp + tp) > 0) {
					double value = Double.valueOf(fp) / Double.valueOf(fp + tp);
					dataset.addValue(value, "fdr", experimentName);
				}
			}

		} catch (IllegalMiapeArgumentException ex) {
			log.info("error getting dataset: " + ex.getMessage());
			error = ex.getMessage();
		}
		if (dataset.getRowCount() > 0)
			return dataset;
		throw new IllegalMiapeArgumentException(
				"No dataset can be extracted to plot specificity and sensitivity. Please, review your input data. "
						+ error);
	}

	public static CategoryDataset createChr16MappingCategoryDataSet(IdentificationSet idSet, List<String> groupsToShow,
			boolean showNotAssigned, String proteinOrGene, String knownUnknown,
			boolean takeGeneFromFirstProteinSelected, Boolean countNonConclusiveProteins) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		int totalProteins = 0;
		int totalGenes = 0;
		List<String> assignedGroupsNames = GeneDistributionReader.getInstance().getAssignedGroupsNames();
		Collections.sort(assignedGroupsNames);

		boolean isThereAnyValue = false;
		// keys=uniprotACC and values=ENSGInfo

		if (proteinOrGene.equals(AdditionalOptionsPanelFactory.PROTEIN)
				|| proteinOrGene.equals(AdditionalOptionsPanelFactory.BOTH)) {
			if (knownUnknown.equals(AdditionalOptionsPanelFactory.KNOWN)
					|| knownUnknown.equals(AdditionalOptionsPanelFactory.BOTH)) {
				HashMap<String, Integer> numKnownProteinsHashMap = getAssignedChr16NumProteins(idSet, true,
						countNonConclusiveProteins);

				for (String researcher : assignedGroupsNames) {
					if (groupsToShow.contains(researcher)) {
						Integer num = numKnownProteinsHashMap.get(researcher);
						if (num != null) {
							totalProteins += num;
							dataset.addValue(num, "known proteins", researcher);
							if (num > 0)
								isThereAnyValue = true;
						}
					}

				}
			}
		}
		if (proteinOrGene.equals(AdditionalOptionsPanelFactory.GENES)
				|| proteinOrGene.equals(AdditionalOptionsPanelFactory.BOTH)) {
			if (knownUnknown.equals(AdditionalOptionsPanelFactory.KNOWN)
					|| knownUnknown.equals(AdditionalOptionsPanelFactory.BOTH)) {
				HashMap<String, Integer> numKnownGenesHashMap = getAssignedChr16NumGenes(idSet, true,
						takeGeneFromFirstProteinSelected, countNonConclusiveProteins);

				for (String researcher : assignedGroupsNames) {
					if (groupsToShow.contains(researcher)) {
						Integer num = numKnownGenesHashMap.get(researcher);
						if (num != null) {
							totalGenes += num;
							dataset.addValue(num, "known genes", researcher);
							if (num > 0)
								isThereAnyValue = true;
						}
					}

				}
			}
		}
		if (proteinOrGene.equals(AdditionalOptionsPanelFactory.PROTEIN)
				|| proteinOrGene.equals(AdditionalOptionsPanelFactory.BOTH)) {
			if (knownUnknown.equals(AdditionalOptionsPanelFactory.UNKNOWN)
					|| knownUnknown.equals(AdditionalOptionsPanelFactory.BOTH)) {
				HashMap<String, Integer> numUnKnownProteinsHashMap = getAssignedChr16NumProteins(idSet, false,
						countNonConclusiveProteins);
				for (String researcher : assignedGroupsNames) {
					if (groupsToShow.contains(researcher)) {
						Integer num = numUnKnownProteinsHashMap.get(researcher);
						if (num != null) {
							totalProteins += num;
							dataset.addValue(num, "Unknown proteins", researcher);
							if (num > 0)
								isThereAnyValue = true;
						}
					}

				}
			}
		}
		if (proteinOrGene.equals(AdditionalOptionsPanelFactory.GENES)
				|| proteinOrGene.equals(AdditionalOptionsPanelFactory.BOTH)) {
			if (knownUnknown.equals(AdditionalOptionsPanelFactory.UNKNOWN)
					|| knownUnknown.equals(AdditionalOptionsPanelFactory.BOTH)) {
				HashMap<String, Integer> numUnKnownGenesHashMap = getAssignedChr16NumGenes(idSet, false,
						takeGeneFromFirstProteinSelected, countNonConclusiveProteins);
				for (String researcher : assignedGroupsNames) {
					if (groupsToShow.contains(researcher)) {
						Integer num = numUnKnownGenesHashMap.get(researcher);
						if (num != null) {
							totalGenes += num;
							dataset.addValue(num, "Unknown genes", researcher);
							if (num > 0)
								isThereAnyValue = true;
						}
					}

				}
			}
		}
		if (proteinOrGene.equals(AdditionalOptionsPanelFactory.PROTEIN)
				|| proteinOrGene.equals(AdditionalOptionsPanelFactory.BOTH)) {
			if (showNotAssigned) {
				int num = getNonAssignedChr16Proteins(idSet, countNonConclusiveProteins);
				totalProteins += num;
				dataset.addValue(num, "not assigned proteins", "not assigned");
				if (num > 0)
					isThereAnyValue = true;
			}
		}
		if (proteinOrGene.equals(AdditionalOptionsPanelFactory.GENES)
				|| proteinOrGene.equals(AdditionalOptionsPanelFactory.BOTH)) {
			if (showNotAssigned) {
				int num = getNonAssignedChr16NumGenes(idSet, takeGeneFromFirstProteinSelected,
						countNonConclusiveProteins);
				totalGenes += num;
				dataset.addValue(num, "not assigned genes", "not assigned");
				if (num > 0)
					isThereAnyValue = true;
			}
		}

		if (proteinOrGene.equals(AdditionalOptionsPanelFactory.PROTEIN)
				|| proteinOrGene.equals(AdditionalOptionsPanelFactory.BOTH)) {
			dataset.addValue(totalProteins, "total proteins", "total");
			if (totalProteins > 0)
				isThereAnyValue = true;
		}
		if (proteinOrGene.equals(AdditionalOptionsPanelFactory.GENES)
				|| proteinOrGene.equals(AdditionalOptionsPanelFactory.BOTH)) {
			dataset.addValue(totalGenes, "total genes", "total");
			if (totalProteins > 0)
				isThereAnyValue = true;
		}
		if (!isThereAnyValue)
			throw new IllegalMiapeArgumentException(
					"<html>No proteins mapping to any human chromosome 16.<br>Note that this mapping is made from uniprot entries<br>so check if your proteins has the uniprot accession or not.</html>");
		return dataset;
	}

	public static CategoryDataset createAllHumanChromosomePeptideMappingCategoryDataSet(IdentificationSet idSet,
			String peptideOrPSM, boolean onlyOneIdSet, boolean distinguishModPep) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		HashMap<String, PeptideOccurrence> peptideOccurrences = idSet.getPeptideOccurrenceList(distinguishModPep);

		int totalNum = 0;
		for (String chromosomeName : GeneDistributionReader.chromosomeNames) {
			int numPeptides = 0;
			int numPSMs = 0;
			List<ENSGInfo> genes = new ArrayList<ENSGInfo>();
			HashMap<String, List<ENSGInfo>> proteinGeneMapping = GeneDistributionReader.getInstance()
					.getProteinGeneMapping(chromosomeName);
			for (PeptideOccurrence peptideOccurrence : peptideOccurrences.values()) {
				final Set<ExtendedIdentifiedProtein> proteinList = peptideOccurrence.getProteinList();
				Set<String> accs = new HashSet<String>();
				for (ExtendedIdentifiedProtein protein : proteinList) {
					accs.add(protein.getAccession());
				}
				boolean found = false;
				// if any protein of the peptides is in the chromosome, sum the
				// numbers of peptides and PSMs
				for (String proteinACC : accs) {
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
		DefaultPieDataset dataset = new DefaultPieDataset();
		HashMap<String, PeptideOccurrence> peptideOccurrences = idSet.getPeptideOccurrenceList(distinguishModPep);

		if (AdditionalOptionsPanelFactory.BOTH.equals(peptideOrPSM))
			throw new IllegalMiapeArgumentException(
					"Peptides and PSMs distributions cannot been seen in the same pie chart. Select PEPTIDE or PSM in the combobox.");

		int totalNum = 0;
		for (String chromosomeName : GeneDistributionReader.chromosomeNames) {
			int numPeptides = 0;
			int numPSMs = 0;
			List<ENSGInfo> genes = new ArrayList<ENSGInfo>();
			HashMap<String, List<ENSGInfo>> proteinGeneMapping = GeneDistributionReader.getInstance()
					.getProteinGeneMapping(chromosomeName);
			for (PeptideOccurrence peptideOccurrence : peptideOccurrences.values()) {
				final Set<ExtendedIdentifiedProtein> proteinList = peptideOccurrence.getProteinList();
				Set<String> accs = new HashSet<String>();
				for (ExtendedIdentifiedProtein protein : proteinList) {
					accs.add(protein.getAccession());
				}
				boolean found = false;
				// if any protein of the peptides is in the chromosome, sum the
				// numbers of peptides and PSMs
				for (String proteinACC : accs) {
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
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		Collection<ProteinGroupOccurrence> proteinGroupOccurrences = idSet.getProteinGroupOccurrenceList().values();

		int totalNum = 0;
		for (String chromosomeName : GeneDistributionReader.chromosomeNames) {
			int numProteins = 0;
			int numGenes = 0;
			List<ENSGInfo> genes = new ArrayList<ENSGInfo>();
			HashMap<String, List<ENSGInfo>> proteinGeneMapping = GeneDistributionReader.getInstance()
					.getProteinGeneMapping(chromosomeName);
			int numProteinsToCount = 0;
			for (ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrences) {
				if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE
						&& !countNonConclusiveProteins)
					continue;
				numProteinsToCount++;
				if (!takeGeneFromFirstProteinSelected) {
					List<String> accessions = proteinGroupOccurrence.getAccessions();
					for (String acc : accessions) {
						if (proteinGeneMapping.containsKey(acc)) {
							numProteins++;
							break;
						}
					}
					Iterator<String> iterator = proteinGroupOccurrence.getAccessions().iterator();
					while (iterator.hasNext()) {
						String acc = iterator.next();
						if (proteinGeneMapping.containsKey(acc)) {
							List<ENSGInfo> list = proteinGeneMapping.get(acc);
							for (ENSGInfo ensgInfo : list) {
								if (!genes.contains(ensgInfo))
									genes.add(ensgInfo);
							}
						}
					}
				} else {
					String acc = proteinGroupOccurrence.getAccessions().get(0);
					if (proteinGeneMapping.containsKey(acc)) {
						numProteins++;
					}

					if (proteinGeneMapping.containsKey(acc)) {
						List<ENSGInfo> list = proteinGeneMapping.get(acc);
						for (ENSGInfo ensgInfo : list) {
							if (!genes.contains(ensgInfo))
								genes.add(ensgInfo);
						}
					}

				}
			}
			if (chromosomeName.equals("16"))
				log.info(numProteinsToCount + " proteins valid in chr16");

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
			throw new IllegalMiapeArgumentException(
					"<html>No proteins have been mapped to any human chromosome.<br>Note that this mapping is made from uniprot entries<br>so check if your proteins has the uniprot accession or not.</html>");

		return dataset;
	}

	public static CategoryDataset createAllHumanChromosomeGeneCoverageCategoryDataSet(List<IdentificationSet> idSets,
			boolean spiderPlot, boolean justPercentage, boolean showTotal, boolean takeGeneFromFirstProteinSelected,
			Boolean countNonConclusiveProteins) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		int totalNum = 0;
		for (IdentificationSet idSet : idSets) {

			Collection<ProteinGroupOccurrence> proteinGroupOccurrences = idSet.getProteinGroupOccurrenceList().values();

			for (String chromosomeName : GeneDistributionReader.chromosomeNames) {
				if (chromosomeName.equalsIgnoreCase("y"))
					continue;
				Collection<ENSGInfo> chromosomeGeneMapping = GeneDistributionReader.getInstance()
						.getENSGIDInAChromosome(chromosomeName);
				if (chromosomeGeneMapping == null) {
					throw new IllegalMiapeArgumentException(
							"Error reading mapping between proteins and chromosome " + chromosomeName);
				}
				int numProteins = 0;
				List<ENSGInfo> genes = new ArrayList<ENSGInfo>();
				HashMap<String, List<ENSGInfo>> proteinGeneMapping = GeneDistributionReader.getInstance()
						.getProteinGeneMapping(chromosomeName);
				for (ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrences) {
					if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE
							&& !countNonConclusiveProteins)
						continue;
					if (!takeGeneFromFirstProteinSelected) {
						Iterator<String> iterator = proteinGroupOccurrence.getAccessions().iterator();
						while (iterator.hasNext()) {
							String acc = iterator.next();
							if (proteinGeneMapping.containsKey(acc)) {
								List<ENSGInfo> list = proteinGeneMapping.get(acc);
								for (ENSGInfo ensgInfo : list) {
									if (!genes.contains(ensgInfo))
										genes.add(ensgInfo);
								}
							}
						}
					} else {
						String acc = proteinGroupOccurrence.getAccessions().get(0);
						if (proteinGeneMapping.containsKey(acc)) {
							List<ENSGInfo> list = proteinGeneMapping.get(acc);
							for (ENSGInfo ensgInfo : list) {
								if (!genes.contains(ensgInfo))
									genes.add(ensgInfo);
							}
						}

					}

				}
				int numGenes = genes.size();
				int numTotalGenes = chromosomeGeneMapping.size();

				// log.info(numGenes + " over " + numTotalGenes + " for chr "
				// + chromosomeName);
				String label = "";
				if (idSets.size() > 1)
					label = " (" + idSet.getName() + ")";
				if (justPercentage) {
					final double percentage = numGenes * 100.0 / numTotalGenes;
					dataset.addValue(percentage, "Gene coverage (%)" + label, "Chr" + chromosomeName);
					if (showTotal && spiderPlot)
						dataset.addValue(100, "Total genes (100%)" + label, "Chr" + chromosomeName);
				} else {
					dataset.addValue(numGenes, "Detected genes" + label, "Chr" + chromosomeName);
					if (showTotal)
						dataset.addValue(numTotalGenes, "Total chromosome genes" + label, "Chr" + chromosomeName);
				}

				totalNum += numGenes;

			}
		}
		if (totalNum == 0)
			throw new IllegalMiapeArgumentException(
					"<html>No proteins have been mapped to any human chromosome.<br>Note that this mapping is made from uniprot entries<br>so check if your proteins has the uniprot accession or not.</html>");

		return dataset;
	}

	public static PieDataset createAllHumanChromosomeMappingPieDataSet(IdentificationSet idSet, String proteinOrGene,
			boolean takeGeneFromFirstProteinInGroup, Boolean countNonConclusiveProteins) {
		boolean onlyOneIdSet = false;
		if (AdditionalOptionsPanelFactory.BOTH.equals(proteinOrGene))
			throw new IllegalMiapeArgumentException(
					"Proteins and genes distributions cannot been seen in the same pie chart. Select PROTEIN or GENES in the combobox.");

		DefaultPieDataset dataset = new DefaultPieDataset();
		Collection<ProteinGroupOccurrence> proteinGroupOccurrences = idSet.getProteinGroupOccurrenceList().values();

		int totalNum = 0;
		for (String chromosomeName : GeneDistributionReader.chromosomeNames) {
			int numProteins = 0;
			int numGenes = 0;
			List<ENSGInfo> genes = new ArrayList<ENSGInfo>();
			HashMap<String, List<ENSGInfo>> proteinGeneMapping = proteinGeneMapping = GeneDistributionReader
					.getInstance().getProteinGeneMapping(chromosomeName);

			int numProteinsToCount = 0;
			for (ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrences) {
				if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE
						&& !countNonConclusiveProteins)
					continue;
				numProteinsToCount++;
				if (!takeGeneFromFirstProteinInGroup) {
					List<String> accessions = proteinGroupOccurrence.getAccessions();
					for (String acc : accessions) {
						if (proteinGeneMapping.containsKey(acc)) {
							numProteins++;
							break;
						}
					}
					Iterator<String> iterator = proteinGroupOccurrence.getAccessions().iterator();
					while (iterator.hasNext()) {
						String acc = iterator.next();
						if (proteinGeneMapping.containsKey(acc)) {
							List<ENSGInfo> list = proteinGeneMapping.get(acc);
							for (ENSGInfo ensgInfo : list) {
								if (!genes.contains(ensgInfo))
									genes.add(ensgInfo);
							}
						}
					}
				} else {
					String acc = proteinGroupOccurrence.getAccessions().get(0);
					if (proteinGeneMapping.containsKey(acc)) {
						numProteins++;
					}
					if (proteinGeneMapping.containsKey(acc)) {
						List<ENSGInfo> list = proteinGeneMapping.get(acc);
						for (ENSGInfo ensgInfo : list) {
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
		int total = proteinGroupOccurrenceList.size();
		int numProteinToCount = 0;

		for (ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrenceList) {
			if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;
			numProteinToCount++;
			Set<ENSGInfo> genesFromProteinGroup = GeneDistributionReader.getInstance()
					.getGenesFromProteinGroup(proteinGroupOccurrence, "16");
			for (ENSGInfo ensgInfo : genesFromProteinGroup) {
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

	private static HashMap<String, Integer> getAssignedChr16NumProteins(IdentificationSet idSet, boolean known,
			Boolean countNonConclusiveProteins) {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();

		final Collection<ProteinGroupOccurrence> proteinGroupOccurrenceList = idSet.getProteinGroupOccurrenceList()
				.values();
		int total = proteinGroupOccurrenceList.size();
		int numProteinToCount = 0;
		List<String> accs = new ArrayList<String>();
		for (ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrenceList) {
			if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;
			numProteinToCount++;
			Set<ENSGInfo> genesFromProteinGroup = GeneDistributionReader.getInstance()
					.getGenesFromProteinGroup(proteinGroupOccurrence, "16");
			for (ENSGInfo ensgInfo : genesFromProteinGroup) {
				if (ensgInfo.isAssigned()) {
					if ((!known && ensgInfo.getKnown().equals(ENSGInfo.UNKNOWN))
							|| (known && ensgInfo.getKnown().equals(ENSGInfo.KNOWN))) {
						String researcher = ensgInfo.getResearcher().getName();
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

	private static HashMap<String, Integer> getAssignedChr16NumGenes(IdentificationSet idSet, boolean known,
			boolean takeGeneFromFirstProteinSelected, Boolean countNonConclusiveProteins) {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		List<ENSGInfo> genes = new ArrayList<ENSGInfo>();

		final Collection<ProteinGroupOccurrence> proteinGroupOccurrenceList = idSet.getProteinGroupOccurrenceList()
				.values();

		for (ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrenceList) {
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
			for (ENSGInfo geneInfo : genesFromProteinGroup) {
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
		List<ENSGInfo> genes = new ArrayList<ENSGInfo>();

		final Collection<ProteinGroupOccurrence> proteinGroupOccurrenceList = idSet.getProteinGroupOccurrenceList()
				.values();
		for (ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrenceList) {
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
			for (ENSGInfo ensgInfo : genesFromProteinGroup) {
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
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String error = null;
		try {
			for (IdentificationSet idSet : idSets) {
				if (showPSMs) {
					int numPSMs = idSet.getTotalNumPeptides();
					dataset.addValue(numPSMs, "PSM", idSet.getFullName());
				}
				if (showPeptides) {
					int numPeptides = idSet.getNumDifferentPeptides(distinguishModificatedPeptides);
					dataset.addValue(numPeptides, "Peptides", idSet.getFullName());
				}
				if (showPeptidesPlusCharge) {
					int numPeptidesPlusCharge = idSet.getNumDifferentPeptidesPlusCharge(distinguishModificatedPeptides);
					dataset.addValue(numPeptidesPlusCharge, "Peptides (diff z)", idSet.getFullName());
				}
				if (showProteins) {
					int numProteins = idSet.getNumDifferentProteinGroups(countNonConclusiveProteins);
					dataset.addValue(numProteins, "Proteins", idSet.getFullName());
				}
			}
		} catch (IllegalMiapeArgumentException ex) {

		}

		return dataset;
	}

	public static XYDataset createFDRvsScoreDataSet(List<IdentificationSet> idSets, boolean showPSMs,
			boolean showPeptides, boolean showProteins, Boolean countNonConclusiveProteins) {
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		String error = null;
		for (IdentificationSet idSet : idSets) {
			log.info("calculating FDR from :" + idSet.getFullName());

			try {
				if (showPSMs) {
					XYSeries xySeries = getXY_PSM_FDRvsScoreSerie(idSet, idSet.getFullName() + " (PSM FDR)");
					xySeriesCollection.addSeries(xySeries);
				}
				if (showPeptides) {
					XYSeries xySeries = getXY_Peptide_FDRvsScoreSerie(idSet, idSet.getFullName() + " (pep FDR)");
					xySeriesCollection.addSeries(xySeries);
				}
				if (showProteins) {
					XYSeries xySeries = getXY_Protein_FDRvsScoreSerie(idSet, idSet.getFullName() + " (prot FDR)",
							countNonConclusiveProteins);
					xySeriesCollection.addSeries(xySeries);
				}
			} catch (IllegalMiapeArgumentException e) {
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

		XYSeries xySeries = new XYSeries(serieName, autoSort, allowDuplicateXValues);

		List<Double> fdrArray = null;

		FDRFilter filter = idSet.getFDRFilter();
		if (filter == null)
			throw new IllegalMiapeArgumentException(
					"<html>Error: Apply an FDR filter in order to select the score name as well<br> as the way to select the DECOY hits, in which the FDR calculation in going to be based</html>");

		List<ExtendedIdentifiedPeptide> identifiedPeptides = idSet.getIdentifiedPeptides();
		SorterUtil.sortPeptidesByPeptideScore(identifiedPeptides, filter.getSortingParameters().getScoreName(), true);

		int numFW = 0;
		int numDC = 0;
		float previousFDRValue = 0.0f;
		for (ExtendedIdentifiedPeptide extendedIdentifiedPeptide : identifiedPeptides) {
			boolean decoy = extendedIdentifiedPeptide.isDecoy(filter);
			if (decoy) {
				numDC++;
			} else {
				numFW++;
			}
			Float fdrValue = filter.calculateFDR(numFW, numDC);
			Float peptideScore = extendedIdentifiedPeptide.getScore(filter.getSortingParameters().getScoreName());

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

		XYSeries xySeries = new XYSeries(serieName, autoSort, allowDuplicateXValues);

		List<Double> fdrArray = null;

		FDRFilter filter = idSet.getFDRFilter();
		if (filter == null)
			throw new IllegalMiapeArgumentException(
					"<html>Error: Apply an FDR filter in order to select the score name as well<br> as the way to select the DECOY hits, in which the FDR calculation in going to be based</html>");

		Collection<PeptideOccurrence> peptideOccurrenceColl = idSet.getPeptideOccurrenceList(false).values();
		List<PeptideOccurrence> peptideOccurrenceList = new ArrayList<PeptideOccurrence>();
		for (PeptideOccurrence peptideOccurrence : peptideOccurrenceColl) {
			peptideOccurrenceList.add(peptideOccurrence);
		}

		SorterUtil.sortPeptideOcurrencesByPeptideScore(peptideOccurrenceList,
				filter.getSortingParameters().getScoreName());

		int numFW = 0;
		int numDC = 0;
		float previousFDRValue = 0.0f;
		for (PeptideOccurrence peptideOccurrence : peptideOccurrenceList) {
			boolean decoy = peptideOccurrence.isDecoy(filter);
			if (decoy) {
				numDC++;
			} else {
				numFW++;
			}
			Float fdrValue = filter.calculateFDR(numFW, numDC);
			Float bestScore = peptideOccurrence.getBestPeptideScore(filter.getSortingParameters().getScoreName());

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

		XYSeries xySeries = new XYSeries(serieName, autoSort, allowDuplicateXValues);

		List<Double> fdrArray = null;

		FDRFilter filter = idSet.getFDRFilter();
		if (filter == null)
			throw new IllegalMiapeArgumentException(
					"<html>Error: Apply an FDR filter in order to select the score name as well<br> as the way to select the DECOY hits, in which the FDR calculation in going to be based</html>");

		Collection<ProteinGroupOccurrence> proteinGroupOccurrencesSet = idSet.getProteinGroupOccurrenceList().values();
		List<ProteinGroupOccurrence> proteinGroupOccurrences = new ArrayList<ProteinGroupOccurrence>();
		for (ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrencesSet) {
			proteinGroupOccurrences.add(proteinGroupOccurrence);
		}
		SorterUtil.sortProteinGroupOcurrencesByPeptideScore(proteinGroupOccurrences,
				filter.getSortingParameters().getScoreName());

		int numFW = 0;
		int numDC = 0;
		float previousFDRValue = 0.0f;

		for (ProteinGroupOccurrence proteinGroupOccurrence : proteinGroupOccurrences) {
			if (proteinGroupOccurrence.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
				continue;

			ExtendedIdentifiedPeptide bestPeptide = proteinGroupOccurrence
					.getBestPeptide(filter.getSortingParameters().getScoreName());

			if (bestPeptide != null) {
				boolean decoy = proteinGroupOccurrence.isDecoy(filter);
				if (decoy) {
					numDC++;
				} else {
					numFW++;
				}
				Float fdrValue = filter.calculateFDR(numFW, numDC);
				Float bestScore = proteinGroupOccurrence
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
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		String error = null;
		for (IdentificationSet idSet : idSets) {
			try {
				FDRFilter filter = idSet.getFDRFilter();
				log.info(filter);
				if (filter == null) {
					throw new IllegalMiapeArgumentException(
							"<html>Error: Apply an FDR filter in order to select the score name as well<br> as the way to select the DECOY hits, in which the FDR calculation in going to be based</html>");
				}
				final boolean autoSort = false;
				final boolean allowDuplicateXValues = true;

				XYSeries xySeries = new XYSeries(idSet.getFullName() + " (num proteins vs '"
						+ filter.getSortingParameters().getScoreName() + "')", autoSort, allowDuplicateXValues);
				List<ProteinGroup> proteinGroups = idSet.getIdentifiedProteinGroups();
				SorterUtil.sortProteinGroupsByPeptideScore(proteinGroups, filter.getSortingParameters().getScoreName());
				int numProteins = 1;
				for (ProteinGroup proteinGroup : proteinGroups) {
					if (proteinGroup.getEvidence() == ProteinEvidence.NONCONCLUSIVE && !countNonConclusiveProteins)
						continue;

					Float bestScore = proteinGroup.getBestPeptideScore(filter.getSortingParameters().getScoreName());

					if (bestScore != null) {
						// log.info(bestScore + " - " + numProteins);
						xySeries.add(bestScore, (Number) numProteins);
					}
					numProteins++;

				}
				xySeriesCollection.addSeries(xySeries);

			} catch (IllegalMiapeArgumentException e) {
				error = e.getMessage();
			}
		}
		if (xySeriesCollection.getSeries() == null || xySeriesCollection.getSeries().isEmpty())
			throw new IllegalMiapeArgumentException(error);
		return xySeriesCollection;
	}

	/**
	 * Gets two datasets, the fisrt is the normal and then second is the
	 * accumulative (if accumulativeTrend is true)
	 *
	 * @param idSets
	 * @param itemType
	 * @param distModPeptides
	 * @param proteinGroupComparisonType
	 * @return
	 */
	public static List<DefaultCategoryDataset> createExclusiveNumberIdentificationCategoryDataSet(
			List<IdentificationSet> idSets, IdentificationItemEnum itemType, Boolean distModPeptides,
			ProteinGroupComparisonType proteinGroupComparisonType, boolean accumulativeTrend,
			Boolean countNonConclusiveProteins) {
		// create the dataset...

		List<DefaultCategoryDataset> datasets = new ArrayList<DefaultCategoryDataset>();
		DefaultCategoryDataset defaultDataSet = new DefaultCategoryDataset();
		DefaultCategoryDataset accumulativeDataSet = new DefaultCategoryDataset();

		try {
			HashMap<String, Set<ProteinComparatorKey>> hashMapKeys = getHashMapKeys(idSets, itemType, distModPeptides,
					proteinGroupComparisonType);

			VennData globalVenn = null;
			Collection<Object> union = null;

			for (int i = 0; i < idSets.size(); i++) {
				IdentificationSet idSet = idSets.get(i);
				String idSetName = idSet.getFullName();
				Set<ProteinComparatorKey> keys = hashMapKeys.get(idSet.getFullName());

				if (accumulativeTrend) {
					globalVenn = new VennData(keys, union, null, proteinGroupComparisonType,
							countNonConclusiveProteins);
					union = globalVenn.getUnion12();
					int num = 0;
					for (Object obj : union) {
						String key = (String) obj;
						if (key.contains(ProteinEvidence.NONCONCLUSIVE.toString()) && !countNonConclusiveProteins)
							continue;
						// if (!key.contains(ProteinEvidence.NONCONCLUSIVE
						// .toString()))
						num++;
					}
					accumulativeDataSet.addValue(num, "Accumulative # proteins", idSetName);
				}

				int numExclusive = 0;
				Collection<Object> unique = null;
				// Look into the other datasets to see how many elements are
				// unique/exclusive to the isSet
				boolean moreThanOne = false;
				for (int j = 0; j < idSets.size(); j++) {
					if (j == i)
						continue;
					IdentificationSet idSet2 = idSets.get(j);

					moreThanOne = true;
					final String idSetName2 = idSet2.getFullName();
					log.info("Comparing " + idSet.getFullName() + " with " + idSetName2);
					Set<ProteinComparatorKey> keys2 = hashMapKeys.get(idSet2.getFullName());
					VennData venn = null;
					if (unique == null) {
						venn = new VennData(keys, keys2, null, null, countNonConclusiveProteins);
					} else {
						venn = new VennData(unique, keys2, null, null, countNonConclusiveProteins);
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
		} catch (IllegalMiapeArgumentException ex) {

		}
		datasets.add(defaultDataSet);
		if (accumulativeTrend)
			datasets.add(accumulativeDataSet);

		return datasets;
	}

	private static HashMap<String, Set<ProteinComparatorKey>> getHashMapKeys(List<IdentificationSet> idSets,
			IdentificationItemEnum itemType, Boolean distModPeptides,
			ProteinGroupComparisonType proteinGroupComparisonType) {
		HashMap<String, Set<ProteinComparatorKey>> ret = new HashMap<String, Set<ProteinComparatorKey>>();
		if (IdentificationItemEnum.PROTEIN.equals(itemType)) {
			for (IdentificationSet identificationSet : idSets) {
				final Set<ProteinComparatorKey> proteinAccSet = new HashSet<ProteinComparatorKey>();
				for (Object object : identificationSet.getProteinGroupOccurrenceList().values()) {
					ProteinGroupOccurrence pgo = (ProteinGroupOccurrence) object;
					// if (pgo.getEvidence() != ProteinEvidence.NONCONCLUSIVE)
					proteinAccSet.add(pgo.getKey(proteinGroupComparisonType));
				}
				ret.put(identificationSet.getFullName(), proteinAccSet);
			}
		} else if (IdentificationItemEnum.PEPTIDE.equals(itemType)) {
			for (IdentificationSet identificationSet : idSets) {
				final Set<String> peptideKeySet = identificationSet.getPeptideOccurrenceList(distModPeptides).keySet();
				final String fullName = identificationSet.getFullName();
				Set<ProteinComparatorKey> proteinComparatorKeySet = new HashSet<ProteinComparatorKey>();
				for (String peptideKey : peptideKeySet) {
					ProteinComparatorKey pck = new ProteinComparatorKey(peptideKey,
							ProteinGroupComparisonType.BEST_PROTEIN);
					proteinComparatorKeySet.add(pck);
				}
				ret.put(identificationSet.getFullName(), proteinComparatorKeySet);
				log.info(ret.size() + "-> " + fullName + ": " + proteinComparatorKeySet.size() + " - "
						+ peptideKeySet.size());
			}
		}
		return ret;
	}

	public static HistogramDataset createPeptideRTHistogramDataSet(List<IdentificationSet> idSets, int bins,
			HistogramType histogramType, boolean inMinutes) {
		HistogramDataset dataset = new HistogramDataset();

		for (IdentificationSet idSet : idSets) {
			double[] values = getPeptideRT(idSet, inMinutes);
			if (values != null && values.length > 0)
				dataset.addSeries(idSet.getFullName(), values, bins);
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
			for (ExtendedIdentifiedPeptide peptide : peptides) {
				final String rtInSeconds = peptide.getRetentionTimeInSeconds();
				if (rtInSeconds != null) {
					try {
						Double rtInSecondsDbl = Double.valueOf(rtInSeconds);
						if (inMinutes)
							rtInSecondsDbl = rtInSecondsDbl / 60.0;
						ret[i] = rtInSecondsDbl;
						validData = true;
					} catch (NumberFormatException e) {

					}
				}
				i++;
			}
		}
		return ret;
	}

	public static XYDataset createRTXYDataSet(List<IdentificationSet> idSets, boolean inMinutes) {
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		if (idSets.size() < 2)
			throw new IllegalMiapeArgumentException("At least two series are needed to paint this chart");
		boolean thereisData = false;
		for (int i = 0; i < idSets.size(); i++) {
			for (int j = i + 1; j < idSets.size(); j++) {
				IdentificationSet idSet1 = idSets.get(i);
				IdentificationSet idSet2 = idSets.get(j);
				List<XYSeries> series = getXYRTSeries(idSet1, idSet2, inMinutes);
				for (XYSeries xySeries : series) {
					if (xySeries.getItems() != null && xySeries.getItems().size() > 0)
						thereisData = true;
					xySeriesCollection.addSeries(xySeries);
				}

			}
		}
		if (!thereisData)
			throw new IllegalMiapeArgumentException(
					"There is not data to show. Please, be sure that the overlapping is not zero or that retention times are captured.");
		return xySeriesCollection;
	}

	private static List<XYSeries> getXYRTSeries(IdentificationSet idSet1, IdentificationSet idSet2, boolean inMinutes) {
		XYSeries normalSeries = new XYSeries(idSet1.getName() + " (x) vs " + idSet2.getName() + " (y)");

		HashMap<String, PeptideOccurrence> peptideOccurrences1 = idSet1.getPeptideChargeOccurrenceList(true);
		HashMap<String, PeptideOccurrence> peptideOccurrences2 = idSet2.getPeptideChargeOccurrenceList(true);

		for (PeptideOccurrence occurrence2 : peptideOccurrences2.values()) {

			if (peptideOccurrences1.containsKey(occurrence2.getKey())) {
				try {
					final PeptideOccurrence occurrence1 = peptideOccurrences1.get(occurrence2.getKey());
					// get the average of the retention times
					List<Double> rt1 = new ArrayList<Double>();
					for (ExtendedIdentifiedPeptide pep : occurrence1.getItemList()) {
						if (pep.getRetentionTimeInSeconds() != null) {
							try {
								rt1.add(Double.valueOf(pep.getRetentionTimeInSeconds()));
							} catch (NumberFormatException e) {

							}
						}
					}
					Double mean1 = 0.0;
					if (!rt1.isEmpty()) {
						mean1 = Maths.mean(rt1.toArray(new Double[0]));
					}
					List<Double> rt2 = new ArrayList<Double>();
					for (ExtendedIdentifiedPeptide pep : occurrence2.getItemList()) {
						if (pep.getRetentionTimeInSeconds() != null) {
							try {
								rt2.add(Double.valueOf(pep.getRetentionTimeInSeconds()));
							} catch (NumberFormatException e) {

							}
						}
					}
					Double mean2 = 0.0;
					if (!rt2.isEmpty()) {
						mean2 = Maths.mean(rt2.toArray(new Double[0]));
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
						normalSeries.add(x, y);
					}
				} catch (IllegalMiapeArgumentException e) {
					// do nothing, not plot it
				} catch (NumberFormatException e) {

				} catch (NullPointerException e) {

				}
			}
		}

		List<XYSeries> ret = new ArrayList<XYSeries>();
		ret.add(normalSeries);
		return ret;
	}

	public static CategoryDataset createSinglePeptideRTMonitoringCategoryDataSet(List<IdentificationSet> idSets,
			List<String> sequences, Boolean showInMinutes) {

		// create the dataset...
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		try {

			for (IdentificationSet idSet : idSets) {
				String experimentName = idSet.getFullName();
				for (String originalSequence : sequences) {
					PeptideOccurrence peptideOccurrence = idSet.getPeptideChargeOccurrence(originalSequence, true);
					if (peptideOccurrence != null) {
						List<Double> rts = new ArrayList<Double>();
						for (ExtendedIdentifiedPeptide peptide : peptideOccurrence.getItemList()) {
							if (peptide.getRetentionTimeInSeconds() != null) {
								try {
									double rt = Double.valueOf(peptide.getRetentionTimeInSeconds());
									rts.add(rt);
								} catch (NumberFormatException e) {

								}
							}
						}
						double rtMean = mean(rts);
						if (showInMinutes) {
							rtMean = rtMean / 60.0;
						}
						if (rtMean > 0) {
							dataset.addValue(rtMean, experimentName, originalSequence);
						}
					}

				}

			}

		} catch (IllegalMiapeArgumentException ex) {

		}
		return dataset;
	}

	private static double mean(List<Double> rts) {
		double total = 0;
		if (rts != null && !rts.isEmpty()) {
			for (Double double1 : rts) {
				if (double1 != null)
					total += double1;
			}
			return total / rts.size();
		}
		return 0;
	}

	public static List<List<String>> createSpectrometersTableData(List<IdentificationSet> idSets) {
		List<List<String>> matrix1 = new ArrayList<List<String>>();
		List<String> columns = new ArrayList<String>();
		columns.add("Ident. Set Name");
		columns.add("Name");
		columns.add("Model");
		columns.add("Version");
		columns.add("Manufacturer");
		columns.add("Customizations");
		columns.add("Parameters");
		columns.add("URI");

		matrix1.add(columns);
		for (IdentificationSet idSet : idSets) {
			Set<Spectrometer> spectrometers = idSet.getSpectrometers();
			for (Spectrometer spectrometer : spectrometers) {
				List<String> cols = new ArrayList<String>();
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
		return transpose(matrix1);

	}

	private static List<List<String>> transpose(List<List<String>> matrix) {

		// transponse
		List<List<String>> ret = new ArrayList<List<String>>();
		// add a row for each property
		for (String col : matrix.get(0)) {
			ret.add(new ArrayList<String>());
		}

		for (int x = 0; x < matrix.get(0).size(); x++) {
			for (int y = 0; y < matrix.size(); y++) {
				String value = matrix.get(y).get(x);
				ret.get(x).add(value);
			}
		}
		return ret;
	}

	public static List<List<String>> createInputParametersTableData(List<IdentificationSet> idSets) {
		List<List<String>> matrix1 = new ArrayList<List<String>>();
		List<String> columns = new ArrayList<String>();
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
		for (IdentificationSet idSet : idSets) {
			Set<InputParameter> inputParameters = idSet.getInputParameters();
			for (InputParameter inputParameter : inputParameters) {
				List<String> cols = new ArrayList<String>();
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

		return transpose(matrix1);
	}

	private static String parseDatabases(Set<Database> databases) {
		StringBuilder sb = new StringBuilder();
		if (databases != null) {
			for (Database database : databases) {
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
		StringBuilder sb = new StringBuilder();
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
		StringBuilder sb = new StringBuilder();
		if (string1 != null)
			sb.append(string1);

		return sb.toString();
	}

	public static HistogramDataset createPeptideCountingHistogramDataSet(IdentificationSet idSet1,
			IdentificationSet idSet2, ProteinGroupComparisonType proteinGroupComparisonType,
			HistogramType histogramType, boolean distinguish, Boolean countNonConclusiveProteins, int bins) {

		HistogramDataset serie = new HistogramDataset();

		double[] values = getRatioHistogram(idSet1, idSet2, proteinGroupComparisonType, countNonConclusiveProteins);
		if (values != null && values.length > 0)
			serie.addSeries(idSet1.getName() + " vs " + idSet2.getName(), values, bins);

		serie.setType(histogramType);
		if (serie.getSeriesCount() < 1)
			throw new IllegalMiapeArgumentException(
					"There is not data to show. Please, be sure that the datasets contains common proteins were the overlapping is not zero.");
		return serie;
	}

	private static double[] getRatioHistogram(IdentificationSet idSet1, IdentificationSet idSet2,
			ProteinGroupComparisonType proteinGroupComparisonType, Boolean countNonConclusiveProteins) {

		VennData venn = new VennData(idSet1.getProteinGroupOccurrenceList().values(),
				idSet2.getProteinGroupOccurrenceList().values(), null, proteinGroupComparisonType,
				countNonConclusiveProteins);
		final Collection<Object> intersection = venn.getIntersection12();
		log.info(intersection.size() + " proteins in common");
		double[] logRatios = new double[intersection.size()];
		int i = 0;
		for (Object object : intersection) {
			if (object instanceof ProteinGroupOccurrence) {
				ProteinGroupOccurrence pgo = (ProteinGroupOccurrence) object;
				final ProteinGroupOccurrence pgo1 = idSet1.getProteinGroupOccurrence(pgo.getFirstOccurrence());

				final ProteinGroupOccurrence pgo2 = idSet2.getProteinGroupOccurrence(pgo.getFirstOccurrence());

				if (pgo1 != null && pgo2 != null) {
					if (!countNonConclusiveProteins && pgo.getEvidence() == ProteinEvidence.NONCONCLUSIVE)
						continue;
					if (!countNonConclusiveProteins && pgo2.getEvidence() == ProteinEvidence.NONCONCLUSIVE)
						continue;
					final List<ExtendedIdentifiedPeptide> peptides1 = pgo1.getPeptides();
					final List<ExtendedIdentifiedPeptide> peptides2 = pgo2.getPeptides();
					if (peptides1 != null && peptides2 != null) {
						final int numPeptides1 = peptides1.size();
						final int numPeptides2 = peptides2.size();
						if (numPeptides2 > 0) {
							double ratio = (double) numPeptides1 / (double) numPeptides2;
							double logratio = Math.log(ratio);
							log.debug("Ratio= " + numPeptides1 + "/" + numPeptides2 + "=" + logratio);
							logRatios[i++] = logratio;
						}
					}
				}
			}
		}
		return logRatios;
	}

	public static XYSeriesCollection createPeptideCountingVsScoreXYDataSet(IdentificationSet idSet1,
			IdentificationSet idSet2, ProteinGroupComparisonType proteinGroupComparisonType, boolean distinguish,
			Boolean countNonConclusiveProteins, String scoreName) {
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		XYSeries serie = new XYSeries(idSet1.getName() + " vs " + idSet2.getName());
		xySeriesCollection.addSeries(serie);

		VennData venn = new VennData(idSet1.getProteinGroupOccurrenceList().values(),
				idSet2.getProteinGroupOccurrenceList().values(), null, proteinGroupComparisonType,
				countNonConclusiveProteins);
		final Collection<Object> intersection = venn.getIntersection12();
		log.info(intersection.size() + " proteins in common");
		double[] logRatios = new double[intersection.size()];
		int i = 0;
		for (Object object : intersection) {
			if (object instanceof ProteinGroupOccurrence) {
				ProteinGroupOccurrence pgo = (ProteinGroupOccurrence) object;
				final ProteinGroupOccurrence pgo1 = idSet1.getProteinGroupOccurrence(pgo.getFirstOccurrence());

				final ProteinGroupOccurrence pgo2 = idSet2.getProteinGroupOccurrence(pgo.getFirstOccurrence());

				if (pgo1 != null && pgo2 != null) {
					if (!countNonConclusiveProteins && pgo.getEvidence() == ProteinEvidence.NONCONCLUSIVE)
						continue;
					if (!countNonConclusiveProteins && pgo2.getEvidence() == ProteinEvidence.NONCONCLUSIVE)
						continue;
					final List<ExtendedIdentifiedPeptide> peptides1 = pgo1.getPeptides();
					final List<ExtendedIdentifiedPeptide> peptides2 = pgo2.getPeptides();
					if (peptides1 != null && peptides2 != null) {
						final int numPeptides1 = peptides1.size();
						final int numPeptides2 = peptides2.size();
						if (numPeptides2 > 0) {
							double ratio = (double) numPeptides1 / (double) numPeptides2;
							double logratio = Math.log(ratio);

							Float score1 = pgo1.getBestPeptideScore(scoreName);
							if (score1 != null)
								serie.add(logratio, score1);
							Float score2 = pgo2.getBestPeptideScore(scoreName);
							if (score2 != null)
								serie.add(logratio, score2);
						}
					}
				}
			}
		}
		if (serie.getItemCount() < 1)
			throw new IllegalMiapeArgumentException(
					"There is not data to show. Please, be sure that the datasets contains common proteins were the overlapping is not zero.");
		return xySeriesCollection;
	}
}