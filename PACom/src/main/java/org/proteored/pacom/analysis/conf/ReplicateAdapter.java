package org.proteored.pacom.analysis.conf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.exceptions.InterruptedMIAPEThreadException;
import org.proteored.miapeapi.exceptions.MiapeDataInconsistencyException;
import org.proteored.miapeapi.exceptions.MiapeDatabaseException;
import org.proteored.miapeapi.exceptions.MiapeSecurityException;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.experiment.model.filters.Filter;
import org.proteored.miapeapi.interfaces.Adapter;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.interfaces.msi.MiapeMSIDocument;
import org.proteored.miapeapi.xml.ms.MIAPEMSXmlFile;
import org.proteored.miapeapi.xml.msi.MIAPEMSIXmlFile;
import org.proteored.miapeapi.xml.msi.MiapeMSIXmlFactory;
import org.proteored.pacom.analysis.conf.jaxb.CPMS;
import org.proteored.pacom.analysis.conf.jaxb.CPMSI;
import org.proteored.pacom.analysis.conf.jaxb.CPReplicate;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;

public class ReplicateAdapter implements Adapter<Replicate> {
	private final CPReplicate xmlRep;
	private final ControlVocabularyManager cvManager;
	private final String experimentName;
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private final boolean curated;
	private final Integer minPeptideLength;
	private final List<Filter> filters;
	private final boolean processInParallel;
	private final boolean doNotGroupNonConclusiveProteins;
	private final boolean separateNonConclusiveProteins;

	public ReplicateAdapter(CPReplicate xmlRep, String experimentName, boolean curated, Integer minPeptideLength,
			List<Filter> filters, boolean processInParallel, boolean annotateProteinsInUniprot,
			boolean doNotGroupNonConclusiveProteins, boolean separateNonConclusiveProteins) {
		this.xmlRep = xmlRep;
		cvManager = OntologyLoaderTask.getCvManager();
		this.experimentName = experimentName;
		this.curated = curated;
		this.minPeptideLength = minPeptideLength;
		this.filters = filters;
		this.processInParallel = processInParallel;
		this.doNotGroupNonConclusiveProteins = doNotGroupNonConclusiveProteins;
		this.separateNonConclusiveProteins = separateNonConclusiveProteins;
	}

	@Override
	public Replicate adapt() {
		List<MiapeMSDocument> miapeMSs = new ArrayList<MiapeMSDocument>();
		List<MiapeMSIDocument> miapeMSIs = new ArrayList<MiapeMSIDocument>();
		log.info("Adapting replicate");
		if (xmlRep.getCPMSIList() != null) {
			for (CPMSI cpMsi : xmlRep.getCPMSIList().getCPMSI()) {

				// local

				log.info("Reading locally created MIAPE MSI file: " + cpMsi.getName());
				MiapeMSIDocument miapeMSI = getMIAPEMSIFromFile(cpMsi);
				try {
					// Para que se pueda interrumpir el proceso
					Thread.sleep(1L);
				} catch (InterruptedException e) {
					throw new InterruptedMIAPEThreadException("Task cancelled");
				}
				if (miapeMSI != null) {
					miapeMSIs.add(miapeMSI);
				} else {
					String message = "Error reading MIAPE MSI file: " + cpMsi.getName() + " with ID " + cpMsi.getId();
					log.warn(message);
					throw new MiapeDataInconsistencyException(message);
				}
			}
		}
		if (xmlRep.getCPMSList() != null) {
			for (CPMS cpMs : xmlRep.getCPMSList().getCPMS()) {

				log.info("Reading locally created MIAPE MS file: " + cpMs.getName());
				MiapeMSDocument miapeMS = getMIAPEMSFromFile(cpMs);
				if (miapeMS != null) {
					miapeMSs.add(miapeMS);
				} else {
					log.warn("Error reading MIAPE MS file: " + cpMs.getName() + " with ID " + cpMs.getId());
				}

			}
		}

		log.info("Creating replicate: " + xmlRep.getName());
		Replicate rep = new Replicate(xmlRep.getName(), experimentName, miapeMSs, miapeMSIs, filters,
				doNotGroupNonConclusiveProteins, separateNonConclusiveProteins, minPeptideLength,
				OntologyLoaderTask.getCvManager(), processInParallel);
		log.info("Replicate: " + xmlRep.getName() + " created.");
		return rep;

	}

	private MiapeMSDocument getMIAPEMSFromFile(CPMS cpMs) {
		File file = null;
		try {
			file = new File(FileManager.getMiapeMSXMLFileLocalPathFromMiapeInformation(cpMs));
		} catch (IllegalMiapeArgumentException e) {
			return null;
		}
		if (!file.exists()) {
			return null;
		}
		MiapeMSDocument ret;
		MIAPEMSXmlFile msFile = new MIAPEMSXmlFile(file);
		msFile.setCvUtil(cvManager);
		try {
			ret = msFile.toDocument();

			return ret;
		} catch (MiapeDatabaseException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		} catch (MiapeSecurityException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeDataInconsistencyException("Error reading dataset from file");

	}

	private MiapeMSIDocument getMIAPEMSIFromFile(CPMSI cpMsi) {

		File file = null;
		if (curated) {
			file = new File(FileManager.getMiapeMSICuratedXMLFilePathFromMiapeInformation(cpMsi));
		} else {
			file = new File(FileManager.getMiapeMSIXMLFileLocalPathFromMiapeInformation(cpMsi));
		}
		if (!file.exists()) {
			throw new IllegalMiapeArgumentException("Error loading MIAPE MSI file: " + file.getName()
					+ " not found at: " + FileManager.getMiapeDataPath());
		}
		MiapeMSIDocument ret;

		MIAPEMSIXmlFile msiFile = new MIAPEMSIXmlFile(file);

		try {
			ret = MiapeMSIXmlFactory.getFactory(processInParallel).toDocument(msiFile, cvManager, null, null, null);
			return ret;
		} catch (MiapeDatabaseException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		} catch (MiapeSecurityException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeDataInconsistencyException("Error reading dataset from file");
	}

}
