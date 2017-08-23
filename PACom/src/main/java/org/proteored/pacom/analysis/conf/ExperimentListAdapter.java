package org.proteored.pacom.analysis.conf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.exceptions.MiapeDataInconsistencyException;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.filters.Filter;
import org.proteored.miapeapi.interfaces.Adapter;
import org.proteored.pacom.analysis.conf.jaxb.CPExperiment;
import org.proteored.pacom.analysis.conf.jaxb.CPExperimentList;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;

public class ExperimentListAdapter implements Adapter<ExperimentList> {
	private final CPExperimentList cpExperimentList;
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private final Integer minPeptideLength;
	private final boolean groupingAtExperimentListLevel;
	private final boolean annotateProteinsInUniprot;
	private final List<Filter> filters;
	private final boolean processInParallel;
	private final boolean doNotGroupNonConclusiveProteins;
	private final boolean separateNonConclusiveProteins;

	public ExperimentListAdapter(File confFile, boolean annotateProteinsInUniprot,
			boolean doNotGroupNonConclusiveProteins, boolean separateNonConclusiveProteins) {
		this(confFile, null, false, null, false, annotateProteinsInUniprot, doNotGroupNonConclusiveProteins,
				separateNonConclusiveProteins);
	}

	public ExperimentListAdapter(CPExperimentList expList, Integer minPeptideLength,
			boolean groupingAtExperimentListLevel, List<Filter> filters, boolean processInParallel,
			boolean annotateProteinsInUniprot, boolean doNotGroupNonConclusiveProteins,
			boolean separateNonConclusiveProteins) {
		this.minPeptideLength = minPeptideLength;
		cpExperimentList = expList;
		this.filters = filters;
		this.groupingAtExperimentListLevel = groupingAtExperimentListLevel;
		this.processInParallel = processInParallel;
		this.annotateProteinsInUniprot = annotateProteinsInUniprot;
		this.doNotGroupNonConclusiveProteins = doNotGroupNonConclusiveProteins;
		this.separateNonConclusiveProteins = separateNonConclusiveProteins;
	}

	public ExperimentListAdapter(File confFile, Integer minPeptideLength, boolean groupingAtExperimentListLevel,
			List<Filter> filters, boolean processInParallel, boolean annotateProteinsInUniprot,
			boolean doNotGroupNonConclusiveProteins, boolean separateNonConclusiveProteins) {
		this.doNotGroupNonConclusiveProteins = doNotGroupNonConclusiveProteins;
		this.separateNonConclusiveProteins = separateNonConclusiveProteins;
		this.minPeptideLength = minPeptideLength;
		this.groupingAtExperimentListLevel = groupingAtExperimentListLevel;
		this.processInParallel = processInParallel;
		this.annotateProteinsInUniprot = annotateProteinsInUniprot;
		this.filters = filters;
		if (confFile == null)
			throw new IllegalArgumentException("Provide a no null file!");

		// check if exists
		if (!confFile.exists())
			throw new IllegalArgumentException(confFile.getAbsolutePath() + " doesn't exist!");

		try {
			cpExperimentList = ComparisonProjectFileUtil.getExperimentListFromComparisonProjectFile(confFile);
		} catch (JAXBException e) {
			log.warn(e.getMessage());
			throw new MiapeDataInconsistencyException(
					"Error loading " + confFile.getAbsolutePath() + " config file: " + e.getMessage());
		}

	}

	/**
	 * @return the cpExperimentList
	 */
	public CPExperimentList getCpExperimentList() {
		return cpExperimentList;
	}

	@Override
	public ExperimentList adapt() {
		log.info("Adapting experiment list");
		List<Experiment> experimentList = new ArrayList<Experiment>();
		if (cpExperimentList != null && cpExperimentList.getCPExperiment() != null) {
			for (CPExperiment xmlExp : cpExperimentList.getCPExperiment()) {
				experimentList.add(new ExperimentAdapter(xmlExp, minPeptideLength, filters, processInParallel,
						annotateProteinsInUniprot, doNotGroupNonConclusiveProteins, separateNonConclusiveProteins)
								.adapt());
			}
			ExperimentList elist = new ExperimentList(cpExperimentList.getName(), experimentList,
					groupingAtExperimentListLevel, filters, doNotGroupNonConclusiveProteins,
					separateNonConclusiveProteins, minPeptideLength, OntologyLoaderTask.getCvManager(),
					processInParallel);
			return elist;
		}
		throw new MiapeDataInconsistencyException("Experiment list is empty");
	}
}
