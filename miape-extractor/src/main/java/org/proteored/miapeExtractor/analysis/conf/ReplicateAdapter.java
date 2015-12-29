package org.proteored.miapeExtractor.analysis.conf;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.proteored.miapeExtractor.analysis.conf.jaxb.CPMS;
import org.proteored.miapeExtractor.analysis.conf.jaxb.CPMSI;
import org.proteored.miapeExtractor.analysis.conf.jaxb.CPReplicate;
import org.proteored.miapeExtractor.analysis.util.FileManager;
import org.proteored.miapeExtractor.gui.tasks.OntologyLoaderTask;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.exceptions.MiapeDatabaseException;
import org.proteored.miapeapi.exceptions.MiapeSecurityException;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.experiment.model.filters.Filter;
import org.proteored.miapeapi.interfaces.Adapter;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProtein;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProteinSet;
import org.proteored.miapeapi.interfaces.msi.MiapeMSIDocument;
import org.proteored.miapeapi.xml.ms.MIAPEMSXmlFile;
import org.proteored.miapeapi.xml.ms.MiapeMSXmlFactory;
import org.proteored.miapeapi.xml.msi.IdentifiedProteinImpl;
import org.proteored.miapeapi.xml.msi.MIAPEMSIXmlFile;
import org.proteored.miapeapi.xml.msi.MiapeMSIXmlFactory;

import edu.scripps.yates.model.Protein;
import edu.scripps.yates.uniprot.UniprotProteinRetriever;
import edu.scripps.yates.utilities.fasta.FastaParser;

public class ReplicateAdapter implements Adapter<Replicate> {
	private final CPReplicate xmlRep;
	private final ControlVocabularyManager cvManager;
	private final String experimentName;
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private final boolean curated;
	private final Integer minPeptideLength;
	private final List<Filter> filters;
	private final boolean processInParallel;

	public ReplicateAdapter(CPReplicate xmlRep, String experimentName, boolean curated) {
		this(xmlRep, experimentName, curated, null, null, false);
	}

	public ReplicateAdapter(CPReplicate xmlRep, String experimentName, boolean curated, boolean processInParallel) {
		this(xmlRep, experimentName, curated, null, null, processInParallel);
	}

	public ReplicateAdapter(CPReplicate xmlRep, String experimentName, boolean curated, Integer minPeptideLength,
			List<Filter> filters, boolean processInParallel) {
		this.xmlRep = xmlRep;
		cvManager = OntologyLoaderTask.getCvManager();
		this.experimentName = experimentName;
		this.curated = curated;
		this.minPeptideLength = minPeptideLength;
		this.filters = filters;
		this.processInParallel = processInParallel;
	}

	@Override
	public Replicate adapt() {
		List<MiapeMSDocument> miapeMSs = new ArrayList<MiapeMSDocument>();
		List<MiapeMSIDocument> miapeMSIs = new ArrayList<MiapeMSIDocument>();
		log.info("Adapting replicate");
		if (xmlRep.getCPMSIList() != null) {
			for (CPMSI cpMsi : xmlRep.getCPMSIList().getCPMSI()) {
				if (cpMsi.isManuallyCreated() != null && cpMsi.isManuallyCreated()) {
					if (cpMsi.getName() != null && !"".equals(cpMsi.getName())) {
						log.info("Reading Manually created MIAPE MSI file: " + cpMsi.getName());
						MiapeMSIDocument miapeMSI = getMIAPEMSIFromManuallyCreatedFile(cpMsi.getName());
						try {
							// Para que se pueda interrumpir el proceso
							Thread.sleep(1L);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (miapeMSI != null)
							miapeMSIs.add(miapeMSI);
						else
							log.warn("Error reading manually created MIAPE MSI file: " + cpMsi.getName());
					}
				} else if (cpMsi.isLocal() != null && cpMsi.isLocal() && !"".equals(cpMsi.getName())) {
					log.info("Reading locally created MIAPE MSI file: " + cpMsi.getName());
					MiapeMSIDocument miapeMSI = getMIAPEMSIFromLocallyCreatedFile(cpMsi.getId(),
							cpMsi.getLocalProjectName());
					try {
						// Para que se pueda interrumpir el proceso
						Thread.sleep(1L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (miapeMSI != null)
						miapeMSIs.add(miapeMSI);
					else
						log.warn("Error reading manually created MIAPE MSI file: " + cpMsi.getName());

				} else {
					if (cpMsi.getName() == null) {
						cpMsi.setName(FileManager.getMiapeMSIFileName(cpMsi.getId()));
					}
					if (cpMsi.getName() != null && !"".equals(cpMsi.getName())) {
						log.info("Reading MIAPE MSI file: " + cpMsi.getName());
						MiapeMSIDocument miapeMSI = getMIAPEMSIFromFile(cpMsi.getId());
						try {
							// Para que se pueda interrumpir el proceso
							Thread.sleep(1L);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (miapeMSI != null)
							miapeMSIs.add(miapeMSI);
						else
							log.warn("Error reading MIAPE MSI file: " + cpMsi.getName());
					}
				}
			}
		}
		if (xmlRep.getCPMSList() != null) {
			for (CPMS cpMs : xmlRep.getCPMSList().getCPMS()) {
				if (cpMs != null) {
					if (cpMs.isLocal() != null && cpMs.isLocal()) {
						cpMs.setName(FileManager.getMiapeMSLocalFileName(cpMs.getId()));
						log.info("Reading locally created MIAPE MS file: " + cpMs.getName());
						MiapeMSDocument miapeMS = getMIAPEMSFromLocallyCreatedFile(cpMs.getId(),
								cpMs.getLocalProjectName());
						if (miapeMS != null)
							miapeMSs.add(miapeMS);
						else
							log.warn("Error reading MIAPE MS file: " + cpMs.getName());

					} else {
						cpMs.setName(FileManager.getMiapeMSFileName(cpMs.getId()));
						log.info("Reading MIAPE MS file: " + cpMs.getName());
						MiapeMSDocument miapeMS = getMIAPEMSFromFile(cpMs.getId());
						if (miapeMS != null)
							miapeMSs.add(miapeMS);
						else
							log.warn("Error reading MIAPE MS file: " + cpMs.getName());
					}
				}
			}
		}

		log.info("Creating replicate: " + xmlRep.getName());
		Replicate rep = new Replicate(xmlRep.getName(), experimentName, miapeMSs, miapeMSIs, filters, minPeptideLength,
				OntologyLoaderTask.getCvManager());
		log.info("Replicate: " + xmlRep.getName() + " created.");
		return rep;

	}

	private MiapeMSDocument getMIAPEMSFromFile(int id) {
		File file = new File(FileManager.getMiapeMSXMLFilePath(id));
		if (!file.exists())
			return null;
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
		return null;
	}

	private MiapeMSIDocument getMIAPEMSIFromFile(int id) {

		File file = null;
		if (curated)
			file = new File(FileManager.getMiapeMSICuratedXMLFilePath(id, experimentName));
		else
			file = new File(FileManager.getMiapeMSIXMLFilePath(id));
		if (!file.exists())
			throw new IllegalMiapeArgumentException("Error loading MIAPE MSI file: " + file.getName()
					+ " not found at: " + FileManager.getMiapeDataPath());
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
		} catch (Exception e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private MiapeMSIDocument getMIAPEMSIFromManuallyCreatedFile(String name) {

		File file = FileManager.getManualIdSetFile(name);
		if (file != null && !file.exists())
			throw new IllegalMiapeArgumentException("Error loading manually created MIAPE MSI file: " + file.getName()
					+ " not found at: " + FileManager.getManualIdSetPath());
		MiapeMSIDocument ret;

		MIAPEMSIXmlFile msiFile = new MIAPEMSIXmlFile(file);

		try {
			ret = MiapeMSIXmlFactory.getFactory(processInParallel).toDocument(msiFile, cvManager, null, null, null);
			addProteinDescriptionFromUniprot(ret);
			return ret;
		} catch (MiapeDatabaseException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		} catch (MiapeSecurityException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private MiapeMSIDocument getMIAPEMSIFromLocallyCreatedFile(int id, String projectName) {

		File file = new File(FileManager.getMiapeMSIXMLFileLocalPath(id, projectName));
		if (!file.exists())
			throw new IllegalMiapeArgumentException("Error loading locally created MIAPE MSI file: " + file.getName()
					+ " not found at: " + FileManager.getMiapeMSIXMLFileLocalPath(id, projectName));
		MiapeMSIDocument ret;

		MIAPEMSIXmlFile msiFile = new MIAPEMSIXmlFile(file);

		try {
			ret = MiapeMSIXmlFactory.getFactory(processInParallel).toDocument(msiFile, cvManager, null, null, null);

			addProteinDescriptionFromUniprot(ret);

			return ret;
		} catch (

		MiapeDatabaseException e)

		{
			log.warn(e.getMessage());
			e.printStackTrace();
		} catch (

		MiapeSecurityException e)

		{
			log.warn(e.getMessage());
			e.printStackTrace();
		} catch (

		Exception e)

		{
			log.warn(e.getMessage());
			e.printStackTrace();
		}
		return null;

	}

	private void addProteinDescriptionFromUniprot(MiapeMSIDocument ret) {
		// complete the information of the proteins with the Uniprot
		// information
		Set<String> accessionsToLookUp = new HashSet<String>();
		for (IdentifiedProteinSet proteinSet : ret.getIdentifiedProteinSets()) {
			for (String proteinAcc : proteinSet.getIdentifiedProteins().keySet()) {
				final IdentifiedProtein protein = proteinSet.getIdentifiedProteins().get(proteinAcc);
				if (protein instanceof IdentifiedProteinImpl) {
					if (protein.getDescription() == null || "".equals(protein.getDescription())) {
						if (FastaParser.isUniProtACC(protein.getAccession())
								&& !protein.getAccession().contains("Reverse")) {
							accessionsToLookUp.add(protein.getAccession());

						}
					}

				}
			}
		}
		if (!accessionsToLookUp.isEmpty()) {
			log.info("Trying to recover protein descriptions for " + accessionsToLookUp.size() + " proteins");
			File uniprotReleasesFolder = FileManager.getUniprotFolder();
			UniprotProteinRetriever upr = new UniprotProteinRetriever(null, uniprotReleasesFolder, true);
			final Map<String, Protein> annotatedProteins = upr.getAnnotatedProteins(accessionsToLookUp);
			for (IdentifiedProteinSet proteinSet : ret.getIdentifiedProteinSets()) {
				for (String proteinAcc : proteinSet.getIdentifiedProteins().keySet()) {
					if (annotatedProteins.containsKey(proteinAcc)) {
						String description = null;
						final Protein uniprotProtein = annotatedProteins.get(proteinAcc);
						if (uniprotProtein.getPrimaryAccession() != null) {
							description = uniprotProtein.getPrimaryAccession().getDescription();
						}
						if (description != null) {
							final IdentifiedProtein protein = proteinSet.getIdentifiedProteins().get(proteinAcc);
							if (protein instanceof IdentifiedProteinImpl) {
								((IdentifiedProteinImpl) protein).setDescription(description);
							}
						}
					}
				}
			}
		}

	}

	private MiapeMSDocument getMIAPEMSFromLocallyCreatedFile(int id, String projectName) {

		File file = new File(FileManager.getMiapeMSXMLFileLocalPath(id, projectName));
		if (!file.exists())
			throw new IllegalMiapeArgumentException("Error loading locally created MIAPE MS file: " + file.getName()
					+ " not found at: " + FileManager.getMiapeMSXMLFileLocalPath(id, projectName));
		MiapeMSDocument ret;

		MIAPEMSXmlFile msFile = new MIAPEMSXmlFile(file);

		try {
			ret = MiapeMSXmlFactory.getFactory().toDocument(msFile, cvManager, null, null, null);
			return ret;
		} catch (MiapeDatabaseException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		} catch (MiapeSecurityException e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			log.warn(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}
