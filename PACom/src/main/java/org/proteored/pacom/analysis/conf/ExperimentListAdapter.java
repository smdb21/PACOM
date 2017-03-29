package org.proteored.pacom.analysis.conf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
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
	private JAXBContext jc;
	private final Integer minPeptideLength;
	private final boolean groupingAtExperimentListLevel;
	private final boolean annotateProteinsInUniprot;
	private final List<Filter> filters;
	private final boolean processInParallel;

	public ExperimentListAdapter(File confFile, boolean annotateProteinsInUniprot) {
		this(confFile, null, false, null, false, annotateProteinsInUniprot);
	}

	public ExperimentListAdapter(CPExperimentList expList, Integer minPeptideLength,
			boolean groupingAtExperimentListLevel, List<Filter> filters, boolean processInParallel,
			boolean annotateProteinsInUniprot) {
		this.minPeptideLength = minPeptideLength;
		cpExperimentList = expList;
		this.filters = filters;
		this.groupingAtExperimentListLevel = groupingAtExperimentListLevel;
		this.processInParallel = processInParallel;
		this.annotateProteinsInUniprot = annotateProteinsInUniprot;
	}

	public ExperimentListAdapter(File confFile, Integer minPeptideLength, boolean groupingAtExperimentListLevel,
			List<Filter> filters, boolean processInParallel, boolean annotateProteinsInUniprot) {
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
			jc = JAXBContext.newInstance("org.proteored.pacom.analysis.conf.jaxb");
			cpExperimentList = (CPExperimentList) jc.createUnmarshaller().unmarshal(confFile);
		} catch (JAXBException e) {
			log.warn(e.getMessage());
			// e.printStackTrace();
			throw new IllegalArgumentException(
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
						annotateProteinsInUniprot).adapt());
			}
			ExperimentList elist = new ExperimentList(cpExperimentList.getName(), experimentList,
					groupingAtExperimentListLevel, filters, minPeptideLength, OntologyLoaderTask.getCvManager(),
					processInParallel);
			return elist;
		}
		return null;
	}
}
