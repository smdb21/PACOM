package org.proteored.pacom.analysis.conf;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.proteored.pacom.analysis.conf.jaxb.CPExperiment;
import org.proteored.pacom.analysis.conf.jaxb.CPExperimentList;

public class ComparisonProjectFileUtil {
	// private final static Logger log =
	// Logger.getLogger(ComparisonProjectFileUtil.class);

	public static CPExperimentList getExperimentListFromComparisonProjectFile(File confFile) throws JAXBException {

		JAXBContext jc = JAXBContext.newInstance("org.proteored.pacom.analysis.conf.jaxb");
		CPExperimentList cpExperimentList = (CPExperimentList) jc.createUnmarshaller().unmarshal(confFile);
		return cpExperimentList;

	}

	public static CPExperiment getExperimentFromComparisonProjectFile(File confFile) throws JAXBException {

		JAXBContext jc = JAXBContext.newInstance("org.proteored.pacom.analysis.conf.jaxb");
		CPExperiment xmlExp = (CPExperiment) jc.createUnmarshaller().unmarshal(confFile);
		return xmlExp;
	}
}
