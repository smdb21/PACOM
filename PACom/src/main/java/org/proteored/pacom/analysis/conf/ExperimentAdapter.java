package org.proteored.pacom.analysis.conf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.exceptions.MiapeDataInconsistencyException;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.experiment.model.filters.Filter;
import org.proteored.miapeapi.interfaces.Adapter;
import org.proteored.pacom.analysis.conf.jaxb.CPExperiment;
import org.proteored.pacom.analysis.conf.jaxb.CPReplicate;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;

public class ExperimentAdapter implements Adapter<Experiment> {
	private final CPExperiment xmlExp;
	private final Integer minPeptideLength;
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private final List<Filter> filters;
	private final boolean processInParallel;
	private final boolean annotateProteinsInUniprot;
	private final boolean doNotGroupNonConclusiveProteins;
	private final boolean separateNonConclusiveProteins;

	public ExperimentAdapter(CPExperiment xmlExp, Integer minPeptideLength, List<Filter> filters,
			boolean annotateProteinsInUniprot, boolean doNotGroupNonConclusiveProteins,
			boolean separateNonConclusiveProteins) {
		this(xmlExp, minPeptideLength, filters, false, annotateProteinsInUniprot, doNotGroupNonConclusiveProteins,
				separateNonConclusiveProteins);
	}

	public ExperimentAdapter(CPExperiment xmlExp, Integer minPeptideLength, List<Filter> filters,
			boolean processInParallel, boolean annotateProteinsInUniprot, boolean doNotGroupNonConclusiveProteins,
			boolean separateNonConclusiveProteins) {
		this.xmlExp = xmlExp;
		this.minPeptideLength = minPeptideLength;
		this.filters = filters;
		this.processInParallel = processInParallel;
		this.annotateProteinsInUniprot = annotateProteinsInUniprot;
		this.doNotGroupNonConclusiveProteins = doNotGroupNonConclusiveProteins;
		this.separateNonConclusiveProteins = separateNonConclusiveProteins;
	}

	public ExperimentAdapter(File confFile, boolean annotateProteinsInUniprot, boolean doNotGroupNonConclusiveProteins,
			boolean separateNonConclusiveProteins) {
		this(confFile, null, null, false, annotateProteinsInUniprot, doNotGroupNonConclusiveProteins,
				separateNonConclusiveProteins);
	}

	public ExperimentAdapter(File confFile, boolean processInParallel, boolean annotateProteinsInUniprot,
			boolean doNotGroupNonConclusiveProteins, boolean separateNonConclusiveProteins) {
		this(confFile, null, null, processInParallel, annotateProteinsInUniprot, doNotGroupNonConclusiveProteins,
				separateNonConclusiveProteins);
	}

	public ExperimentAdapter(File confFile, Integer minPeptideLength, List<Filter> filters, boolean processInParallel,
			boolean annotateProteinsInUniprot, boolean doNotGroupNonConclusiveProteins,
			boolean separateNonConclusiveProteins) {
		this.minPeptideLength = minPeptideLength;
		this.processInParallel = processInParallel;
		this.annotateProteinsInUniprot = annotateProteinsInUniprot;
		this.filters = filters;
		this.doNotGroupNonConclusiveProteins = doNotGroupNonConclusiveProteins;
		this.separateNonConclusiveProteins = separateNonConclusiveProteins;
		if (confFile == null)
			throw new IllegalArgumentException("Provide a no null file!");

		// check if exists
		if (!confFile.exists())
			throw new IllegalArgumentException(confFile.getAbsolutePath() + " doesn't exist!");

		try {
			xmlExp = ComparisonProjectFileUtil.getExperimentFromComparisonProjectFile(confFile);
		} catch (JAXBException e) {
			log.warn(e.getMessage());
			throw new MiapeDataInconsistencyException(
					"Error loading " + confFile.getAbsolutePath() + " config file: " + e.getMessage());
		}

	}

	public CPExperiment getCpExperiment() {
		return xmlExp;
	}

	@Override
	public Experiment adapt() {
		log.info("Adapting experiment");
		List<Replicate> replicates = new ArrayList<Replicate>();
		if (xmlExp != null && xmlExp.getCPReplicate() != null) {
			for (CPReplicate xmlRep : xmlExp.getCPReplicate()) {
				replicates.add(new ReplicateAdapter(xmlRep, xmlExp.getName(), xmlExp.isCurated(), minPeptideLength,
						filters, processInParallel, annotateProteinsInUniprot, doNotGroupNonConclusiveProteins,
						separateNonConclusiveProteins).adapt());
			}

		}
		Experiment ret = new Experiment(xmlExp.getName(), replicates, filters, doNotGroupNonConclusiveProteins,
				separateNonConclusiveProteins, minPeptideLength, OntologyLoaderTask.getCvManager(), processInParallel);
		return ret;
	}

}
