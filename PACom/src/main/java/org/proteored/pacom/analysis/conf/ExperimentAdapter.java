package org.proteored.pacom.analysis.conf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
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
	private JAXBContext jc;
	private final List<Filter> filters;
	private final boolean processInParallel;
	private final boolean annotateProteinsInUniprot;

	public ExperimentAdapter(CPExperiment xmlExp, Integer minPeptideLength, List<Filter> filters,
			boolean annotateProteinsInUniprot) {
		this(xmlExp, minPeptideLength, filters, false, annotateProteinsInUniprot);
	}

	public ExperimentAdapter(CPExperiment xmlExp, Integer minPeptideLength, List<Filter> filters,
			boolean processInParallel, boolean annotateProteinsInUniprot) {
		this.xmlExp = xmlExp;
		this.minPeptideLength = minPeptideLength;
		this.filters = filters;
		this.processInParallel = processInParallel;
		this.annotateProteinsInUniprot = annotateProteinsInUniprot;
	}

	public ExperimentAdapter(File confFile, boolean annotateProteinsInUniprot) {
		this(confFile, null, null, false, annotateProteinsInUniprot);
	}

	public ExperimentAdapter(File confFile, boolean processInParallel, boolean annotateProteinsInUniprot) {
		this(confFile, null, null, processInParallel, annotateProteinsInUniprot);
	}

	public ExperimentAdapter(File confFile, Integer minPeptideLength, List<Filter> filters, boolean processInParallel,
			boolean annotateProteinsInUniprot) {
		this.minPeptideLength = minPeptideLength;
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
			xmlExp = (CPExperiment) jc.createUnmarshaller().unmarshal(confFile);
		} catch (JAXBException e) {
			log.warn(e.getMessage());
			// e.printStackTrace();
			throw new IllegalArgumentException(
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
						filters, processInParallel, annotateProteinsInUniprot).adapt());
			}

		}
		Experiment ret = new Experiment(xmlExp.getName(), replicates, filters, minPeptideLength,
				OntologyLoaderTask.getCvManager(), processInParallel);
		return ret;
	}

}
