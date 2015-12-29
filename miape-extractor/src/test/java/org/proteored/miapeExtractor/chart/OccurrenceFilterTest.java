package org.proteored.miapeExtractor.chart;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.filters.Filter;
import org.proteored.miapeapi.experiment.model.filters.OccurrenceFilter;

public class OccurrenceFilterTest {

	@Test
	public void occurrenceFilterTest() {
		ExperimentList createExperiments = ExperimentsUtilTest
				.createExperiments(null);
		OccurrenceFilter ocFilter = new OccurrenceFilter(2,
				IdentificationItemEnum.PEPTIDE, false, false, null);
		List<Experiment> nextLevelIdentificationSetList = createExperiments
				.getNextLevelIdentificationSetList();
		for (Experiment experiment : nextLevelIdentificationSetList) {
			int numPeptides = experiment.getNumDifferentPeptides(false);
			List<Filter> filters = new ArrayList<Filter>();
			filters.add(ocFilter);
			experiment.setFilters(filters);
			int numPeptidesAfterFilter = experiment
					.getNumDifferentPeptides(false);
			Assert.assertTrue(numPeptides > numPeptidesAfterFilter);
		}
	}
}
