package org.proteored.miapeExtractor.analysis.gui.tasks;

import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.proteored.miapeExtractor.analysis.conf.ExperimentListAdapter;
import org.proteored.miapeExtractor.analysis.conf.jaxb.CPExperimentList;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.filters.Filter;

public class DataLoaderTask extends SwingWorker<ExperimentList, Void> {
	private static final Logger log = Logger
			.getLogger("log4j.logger.org.proteored");
	private final CPExperimentList cpExpList;
	private final Integer minPeptideLength;
	private final List<Filter> filters;
	private final boolean groupingAtExperimentListLevel;
	private final boolean processInParallel;
	public static final String DATA_LOADED_DONE = "data loaded done";
	public static final String DATA_LOADED_START = "data loaded start";
	public static final String DATA_LOADED_ERROR = "data loaded error";

	public DataLoaderTask(CPExperimentList cpExpList, Integer minPeptideLength,
			boolean groupingAtExperimentListLevel, List<Filter> filters,
			boolean processInParallel) {
		this.cpExpList = cpExpList;
		this.minPeptideLength = minPeptideLength;
		this.filters = filters;
		this.groupingAtExperimentListLevel = groupingAtExperimentListLevel;
		this.processInParallel = processInParallel;
	}

	public DataLoaderTask(CPExperimentList cpExpList, boolean processInParallel) {
		this(cpExpList, null, false, null, processInParallel);
	}

	@Override
	protected ExperimentList doInBackground() throws Exception {

		try {
			firePropertyChange(DATA_LOADED_START, null, null);

			log.info("Loading data");
			ExperimentList expList = new ExperimentListAdapter(cpExpList,
					minPeptideLength, groupingAtExperimentListLevel,
					this.filters, this.processInParallel).adapt();
			firePropertyChange(DATA_LOADED_DONE, null, expList);

			return expList;
		} catch (IllegalMiapeArgumentException e) {
			firePropertyChange(DATA_LOADED_ERROR, null, e.getMessage());
			return null;
		}
	}

	@Override
	protected void done() {
		if (this.isCancelled())
			log.info("Data loading cancelled");
	}
}
