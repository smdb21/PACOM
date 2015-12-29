package org.proteored.miapeExtractor.chart;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.filters.ComparatorOperator;
import org.proteored.miapeapi.experiment.model.filters.FDRFilter;
import org.proteored.miapeapi.experiment.model.filters.Filter;
import org.proteored.miapeapi.experiment.model.filters.OccurrenceFilter;
import org.proteored.miapeapi.experiment.model.filters.ScoreFilter;
import org.proteored.miapeapi.experiment.model.sort.SorterUtil;

public class FilterSorting {

	@Test
	public void sortingFilters() {
		int numFilters = 20;
		List<Filter> filters = new ArrayList<Filter>();
		for (int i = 0; i < numFilters; i++) {
			int filterType = ExperimentsUtilTest.random.nextInt(2);
			if (filterType == 0) {
				filters.add(getFDRFilter());
			} else if (filterType == 2) {
				filters.add(getScoreFilter());
			}
		}
		Assert.assertFalse(isOrdered(filters));
		printFilters(filters);
		SorterUtil.sortFilters(filters);
		Assert.assertTrue(isOrdered(filters));
		printFilters(filters);
	}

	private boolean isOrdered(List<Filter> filters) {
		Filter previousFilter = null;
		StringBuilder sb = new StringBuilder();
		for (Filter filter : filters) {
			if (previousFilter != null) {
				if (filter instanceof FDRFilter) {
					if (previousFilter instanceof OccurrenceFilter
							|| previousFilter instanceof ScoreFilter)
						return false;
				} else if (filter instanceof ScoreFilter) {
					if (previousFilter instanceof OccurrenceFilter)
						return false;

				}
			}
			previousFilter = filter;
		}
		return true;
	}

	private void printFilters(List<Filter> filters) {
		StringBuilder sb = new StringBuilder();
		for (Filter filter : filters) {

			if (filter instanceof FDRFilter) {

				sb.append(" FDR-");
			} else if (filter instanceof ScoreFilter) {

				sb.append(" SCO-");
			} else {
				sb.append(" OCCU-");
			}

		}
		System.out.println(sb.toString());

	}

	private FDRFilter getFDRFilter() {
		return new FDRFilter(4, "prefix", false, null,
				IdentificationItemEnum.PEPTIDE, null, null, null);
	}

	private OccurrenceFilter getOccurrenceFilter() {
		return new OccurrenceFilter(4, IdentificationItemEnum.PEPTIDE, false,
				false, null);
	}

	private ScoreFilter getScoreFilter() {
		return new ScoreFilter(5, "scoreName", ComparatorOperator.EQUAL,
				IdentificationItemEnum.PEPTIDE, null);
	}
}
