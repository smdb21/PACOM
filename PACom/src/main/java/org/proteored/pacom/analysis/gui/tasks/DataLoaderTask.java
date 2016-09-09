package org.proteored.pacom.analysis.gui.tasks;

import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.datamanager.DataManager;
import org.proteored.miapeapi.experiment.model.filters.Filter;
import org.proteored.pacom.analysis.conf.ExperimentListAdapter;
import org.proteored.pacom.analysis.conf.jaxb.CPExperimentList;

public class DataLoaderTask extends SwingWorker<ExperimentList, Void> {
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private final CPExperimentList cpExpList;
	private final Integer minPeptideLength;
	private final List<Filter> filters;
	private final boolean groupingAtExperimentListLevel;
	private final boolean processInParallel;
	public static final String DATA_LOADED_DONE = "data loaded done";
	public static final String DATA_LOADED_START = "data loaded start";
	public static final String DATA_LOADED_ERROR = "data loaded error";

	public DataLoaderTask(CPExperimentList cpExpList, Integer minPeptideLength, boolean groupingAtExperimentListLevel,
			List<Filter> filters, boolean processInParallel) {
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

			log.info("Clearing DataManager static information");
			DataManager.clearStaticInfo();

			log.info("Loading data");

			ExperimentList expList = new ExperimentListAdapter(cpExpList, minPeptideLength,
					groupingAtExperimentListLevel, filters, processInParallel).adapt();
			firePropertyChange(DATA_LOADED_DONE, null, expList);

			return expList;
		} catch (IllegalMiapeArgumentException e) {
			firePropertyChange(DATA_LOADED_ERROR, null, e.getMessage());
			return null;
		}
	}

	@Override
	protected void done() {
		if (isCancelled())
			log.info("Data loading cancelled");
	}
}
