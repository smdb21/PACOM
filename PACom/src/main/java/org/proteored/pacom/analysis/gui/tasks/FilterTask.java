package org.proteored.pacom.analysis.gui.tasks;

import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.experiment.model.filters.Filter;

public class FilterTask extends SwingWorker<Void, Void> {
	private static final Logger log = Logger
			.getLogger("log4j.logger.org.proteored");

	private final List<Filter> filters;
	private final ExperimentList experimentList;
	private final boolean filterReplicates;
	public static final String FILTER_DONE = "filter done";

	public FilterTask(List<Filter> filters, ExperimentList experimentList,
			boolean filterReplicates) {
		this.filters = filters;
		this.experimentList = experimentList;
		this.filterReplicates = filterReplicates;
	}

	@Override
	protected Void doInBackground() throws Exception {
		log.info("FILTER TASK STARTED!!!!");
		if (filters != null && experimentList != null) {
			experimentList.setFilters(filters);
			experimentList.getPeptideOccurrenceList(true);
			experimentList.getProteinGroupOccurrenceList();
			for (Experiment idSet : experimentList.getExperiments()) {

				idSet.getPeptideOccurrenceList(true);
				idSet.getProteinGroupOccurrenceList();
				try {
					if (filterReplicates)
						for (Replicate replicate : idSet
								.getNextLevelIdentificationSetList()) {
							replicate.getProteinGroupOccurrenceList();
							replicate.getPeptideOccurrenceList(true);
						}
				} catch (Exception e) {

				}
			}
		}
		return null;
	}

	@Override
	protected void done() {
		super.done();
		if (!isCancelled())
			firePropertyChange(FilterTask.FILTER_DONE, null, null);
	}

}
