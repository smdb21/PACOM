package org.proteored.pacom.analysis.exporters;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.proteored.miapeapi.cv.Accession;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.cv.ControlVocabularyTerm;
import org.proteored.miapeapi.cv.UNIMODOntology;
import org.proteored.miapeapi.cv.msi.PeptideModificationName;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.interfaces.Contact;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.interfaces.msi.MiapeMSIDocument;
import org.proteored.pacom.analysis.exporters.util.PexFile;
import org.proteored.pacom.analysis.exporters.util.PexFileMapping;
import org.proteored.pacom.analysis.exporters.util.PexMiapeParser;
import org.proteored.pacom.analysis.exporters.util.PexSubmissionType;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;
import org.springframework.core.io.ClassPathResource;

import uk.ac.ebi.pridemod.PrideModController;
import uk.ac.ebi.pridemod.slimmod.model.SlimModCollection;
import uk.ac.ebi.pridemod.slimmod.model.SlimModification;

public class ProteomeXchangeFilev1 {
	public final static char TAB = Exporter.TAB;
	public final static String MTD = "MTD";
	public final static String FMH = "FMH";
	public final static String FME = "FME";
	public final static String COM = "COM";
	public static final CharSequence NEWLINE = Exporter.NEWLINE;
	private final File outputFolder;
	private String outputFileName;
	private final ExperimentList experimentList;
	private Contact submitter;
	private String projectTitle;
	private String projectShortDescription;
	private final HashSet<String> keywords = new HashSet<String>();
	private PexSubmissionType submissionType;
	private String comment;
	private Set<String> species = new HashSet<String>();
	private Set<String> instruments = new HashSet<String>();
	private Set<String> additionals = new HashSet<String>();
	private Set<String> pubmeds = new HashSet<String>();
	private String resubmission_px;
	private Set<String> reanalyses_px = new HashSet<String>();
	private String login;
	private final HashMap<Experiment, List<PexFile>> rawFileMapToExperiment = new HashMap<Experiment, List<PexFile>>();
	private final HashMap<Replicate, List<PexFile>> rawFileMapToReplicate = new HashMap<Replicate, List<PexFile>>();
	private final HashMap<Replicate, List<PexFile>> peakListFileMap = new HashMap<Replicate, List<PexFile>>();
	private final HashMap<Replicate, List<PexFile>> miapeMSReports = new HashMap<Replicate, List<PexFile>>();
	private final HashMap<Replicate, List<PexFile>> miapeMSIReports = new HashMap<Replicate, List<PexFile>>();
	private final HashMap<Replicate, List<PexFile>> searchEngineOutputFileMap = new HashMap<Replicate, List<PexFile>>();
	ClassPathResource resource = new ClassPathResource(
			"modification_mappings.xml");
	private final HashMap<Replicate, Set<String>> filesToSkip;
	private static final String FILE_SEPARATOR = System
			.getProperty("file.separator");
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private static SlimModCollection preferredModifications;

	/**
	 * Constructor with all parameters
	 * 
	 * @param outputFolder
	 * @param outputFileName
	 * @param experimentList
	 * @param projectTitle
	 * @param projectShortDescription
	 * @param keywords
	 * @param comment
	 * @param species
	 * @param instruments
	 * @param additionals
	 * @param pubmeds
	 * @param resubmissionPX
	 * @param reanalysesPX
	 * @param pride_login
	 */
	public ProteomeXchangeFilev1(File outputFolder, String outputFileName,
			ExperimentList experimentList, String projectTitle,
			String projectShortDescription, String keyword, String comment,
			Set<String> species, Set<String> instruments,
			Set<String> additionals, Set<String> pubmeds,
			String resubmissionPX, Set<String> reanalysesPX,
			String pride_login, HashMap<Replicate, Set<String>> filesToSkip2) {
		this.outputFileName = outputFileName;
		if (this.outputFileName == null)
			this.outputFileName = "submission_file.px";
		this.outputFolder = outputFolder;
		if (this.outputFolder == null)
			throw new IllegalMiapeArgumentException("Output folder is null!!");
		this.experimentList = experimentList;
		if (this.experimentList == null)
			throw new IllegalMiapeArgumentException("Experiment list is null!!");
		submitter = getContact();
		this.projectTitle = projectTitle;
		this.projectShortDescription = projectShortDescription;
		keywords.add(keyword);
		this.comment = comment;
		this.species = species;
		this.instruments = instruments;
		this.additionals = additionals;
		this.pubmeds = pubmeds;
		resubmission_px = resubmissionPX;
		reanalyses_px = reanalysesPX;
		login = pride_login;

		filesToSkip = filesToSkip2;
		submissionType = PexSubmissionType.COMPLETE;

	}

	public ProteomeXchangeFilev1(File outputFolder,
			ExperimentList experimentList,
			HashMap<Replicate, Set<String>> filesToSkip2) {
		this.outputFolder = outputFolder;
		if (this.outputFolder == null)
			throw new IllegalMiapeArgumentException("Output folder is null!!");
		if (!this.outputFolder.isDirectory())
			throw new IllegalMiapeArgumentException(
					"Output folder must be a folder!!");

		this.experimentList = experimentList;
		if (this.experimentList == null)
			throw new IllegalMiapeArgumentException("Experiment list is null!!");
		filesToSkip = filesToSkip2;
		submissionType = PexSubmissionType.COMPLETE;
	}

	public void setPrideLogin(String pride_login) {
		login = pride_login;

	}

	public void setLogin(String login) {
		this.login = login;
	}

	public void setResubmission_px(String resubmission_px) {
		this.resubmission_px = resubmission_px;
	}

	public void setReanalyses_px(Set<String> reanalyses_px) {
		this.reanalyses_px = reanalyses_px;
	}

	public void addReanalyses_px(String reanalysis_px) {
		if (reanalyses_px == null)
			reanalyses_px = new HashSet<String>();
		reanalyses_px.add(reanalysis_px);
	}

	public void setPubmeds(Set<String> pubmeds) {
		if (pubmeds != null)
			this.pubmeds.addAll(pubmeds);
	}

	public void addPubmed(String pubmed) {
		if (pubmed != null)
			pubmeds.add(pubmed);
	}

	public void setResubmissionPX(String resubmissionPX) {
		resubmission_px = resubmissionPX;
	}

	public void setSubmitter(Contact submitter) {
		this.submitter = submitter;
	}

	public void setSubmissionType(PexSubmissionType submissionType) {
		this.submissionType = submissionType;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setSpecies(Set<String> species) {
		if (species != null) {
			if (this.species == null)
				this.species = new HashSet<String>();
			for (String specie : species) {

				final String parsedSpecie = PexMiapeParser.parseSpecie(specie,
						null);
				if (parsedSpecie != null)
					this.species.add(parsedSpecie);
			}
		}

	}

	public void addSpecie(String specie) {
		if (specie != null) {
			if (species == null)
				species = new HashSet<String>();

			final String parsedSpecie = PexMiapeParser
					.parseSpecie(specie, null);
			if (parsedSpecie != null)
				species.add(parsedSpecie);

		}

	}

	public void setInstruments(Set<String> instruments) {
		if (instruments != null)
			this.instruments = instruments;
	}

	public void setAdditionals(Set<String> additionals) {
		this.additionals = additionals;
	}

	public void addKeyword(String keyword) {
		keywords.add(keyword);
	}

	public void setKeyword(Set<String> keywords) {
		if (keywords != null)
			this.keywords.addAll(keywords);
	}

	public void setProjectShortDescription(String projectShortDescription) {
		this.projectShortDescription = projectShortDescription;
	}

	public void setProjectTitle(String projectTitle) {
		this.projectTitle = projectTitle;
	}

	public void setSubmitterContact(Contact contact) {
		submitter = contact;
	}

	public File getOutputFolder() {
		return outputFolder;
	}

	public String getOutputFileName() {
		if (outputFileName == null)
			outputFileName = "submission_file.px";
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public ExperimentList getExperimentList() {
		return experimentList;
	}

	public File getOutputFile() {
		String pathname = outputFolder.getAbsolutePath()
				+ System.getProperty("file.separator") + getOutputFileName();
		log.info(pathname);
		return new File(pathname);
	}

	/**
	 * Gets 'MTD name John Arthur Smith'
	 * 
	 * @return
	 */
	public String getName() {
		String submitterName = getSubmitterName();
		if (submitterName != null)
			return MTD + TAB + "name" + TAB + submitterName;
		return null;
	}

	/**
	 * Gets 'MTD email john@proteored.org'
	 * 
	 * @return
	 */
	public String getEmail() {
		String submitterEmail = getSubmitterEmail();
		if (submitterEmail != null)
			return MTD + TAB + "email" + TAB + submitterEmail;
		return null;
	}

	/**
	 * Gets 'MTD affiliation National Center for Biotechnology - CSIC -
	 * ProteoRed'
	 * 
	 * @return
	 */
	public String getAffiliation() {
		String submitterAffiliation = getSubmitterAffiliation();
		if (submitterAffiliation != null)
			return MTD + TAB + "affiliation" + TAB + submitterAffiliation;
		return null;
	}

	/**
	 * Gets 'MTD title Spanish Human Proteome Project'
	 * 
	 * @return
	 */
	public String getTitle() {

		if (projectTitle != null)
			return MTD + TAB + "title" + TAB + projectTitle;
		return null;
	}

	/**
	 * Gets 'MTD description Here we present...'
	 * 
	 * @return
	 */
	public String getDescription() {
		if (projectShortDescription != null)
			return MTD + TAB + "description" + TAB + projectShortDescription;
		return null;
	}

	/**
	 * Gets 'MTD keywords Human, Plasma, LC-MS'
	 * 
	 * @return
	 */
	public String getKeywords() {
		if (keywords != null) {
			String keywordsString = "";
			for (String keyword : keywords) {
				if (!"".equals(keywordsString))
					keywordsString += ",";
				keywordsString += keyword;
			}
			return MTD + TAB + "keywords" + TAB + keywordsString;
		}
		return null;
	}

	/**
	 * Gets 'MTD type SUPPORTED/UNSUPPORTED'
	 * 
	 * @return
	 */
	public String getSubmissionType() {
		if (submissionType != null)
			return MTD + TAB + "type" + TAB + submissionType.toString();
		return null;
	}

	/**
	 * Gets 'MTD comment This file was produced by pipeline XX ...'
	 * 
	 * @return
	 */
	public String getComment() {
		if (comment != null)
			return MTD + TAB + "comment" + TAB + comment;
		return null;
	}

	/**
	 * Gets a list of 'MTD species [NEWT, 9606, Homo Sapiens (Human),]'
	 * 
	 * @return
	 */
	public List<String> getSpecies() {
		if (species == null || species.isEmpty())
			species = getSpeciesFromMiapes();
		if (species != null) {
			List<String> ret = new ArrayList<String>();
			for (String specie : species) {
				ret.add(MTD + TAB + "species" + TAB + specie);
			}
			return ret;
		}
		return null;
	}

	/**
	 * Gets a list of 'MTD instrument [MS, MS:1000447, LTQ,]'
	 * 
	 * @return
	 */
	public List<String> getInstruments() {
		if (instruments == null || instruments.isEmpty())
			instruments = getInstrumentsFromMiapes();
		if (instruments != null) {
			List<String> ret = new ArrayList<String>();
			for (String instrument : instruments) {
				ret.add(MTD + TAB + "instrument" + TAB + instrument);
			}
			return ret;
		}
		return null;
	}

	/**
	 * Gets a list of 'MTD modification [MOD, MOD:00394,acetylated residue,]'
	 * 
	 * @return
	 */
	public List<String> getPTMs() {

		List<String> modificationStrings = experimentList
				.getDifferentPeptideModificationNames();
		if (modificationStrings != null && !modificationStrings.isEmpty()) {
			List<String> ret = new ArrayList<String>();
			ControlVocabularyManager cvManager = OntologyLoaderTask
					.getCvManager();
			SlimModCollection preferredModifications = getModificationMapping();
			for (String modificationString : modificationStrings) {
				String modification = "";
				ControlVocabularyTerm cvTerm = null;
				if (preferredModifications != null) {
					SlimModification mod = preferredModifications
							.getbyName(modificationString);
					if (mod != null) {
						final String idPsiMod = mod.getIdPsiMod();
						cvTerm = PeptideModificationName.getInstance(cvManager)
								.getCVTermByAccession(new Accession(idPsiMod));
					}
				}
				if (cvTerm == null)
					cvTerm = PeptideModificationName.getInstance(cvManager)
							.getCVTermByPreferredName(modificationString);

				if (cvTerm != null) {
					// if it is from UNIMOD, try to convert to PSI MOD
					if (cvTerm.getCVRef().equals(UNIMODOntology.getCVLabel())) {
						log.info("Converting " + cvTerm.getTermAccession()
								+ " " + cvTerm.getPreferredName()
								+ " to PSI-MOD");
						for (SlimModification slimModification : preferredModifications) {
							if ((UNIMODOntology.getCVLabel() + ":" + slimModification
									.getIdUnimod()).equalsIgnoreCase(cvTerm
									.getTermAccession().toString())) {
								ControlVocabularyTerm cvTerm2 = PeptideModificationName
										.getInstance(cvManager)
										.getCVTermByAccession(
												new Accession(slimModification
														.getIdPsiMod()));
								if (cvTerm2 != null) {
									log.info(cvTerm.getTermAccession() + " "
											+ cvTerm.getPreferredName()
											+ " converter to "
											+ cvTerm2.getTermAccession() + " "
											+ cvTerm2.getPreferredName());
									cvTerm = cvTerm2;
									break;
								}
							}
						}
					}
					modification = "[" + cvTerm.getCVRef() + ","
							+ cvTerm.getTermAccession().toString() + ","
							+ cvTerm.getPreferredName() + ",]";
				} else {
					// userParam
					modification = "[,," + modificationString + ",]";
				}
				ret.add(MTD + TAB + "modification" + TAB + modification);
			}
			return ret;
		} else {
			List<String> ret = new ArrayList<String>();
			ret.add(MTD
					+ TAB
					+ "modification"
					+ TAB
					+ "[PRIDE, PRIDE:0000398,No PTMs are included in the dataset,]");
		}
		return null;
	}

	private SlimModCollection getModificationMapping() {
		if (preferredModifications == null) {
			URL url;
			try {
				url = resource.getURL();
				preferredModifications = PrideModController
						.parseSlimModCollection(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return preferredModifications;
	}

	/**
	 * Gets a list of 'MTD additional [xx,xxx,xx,xx]'
	 * 
	 * @return
	 */
	public Set<String> getAdditionals() {
		if (additionals == null || additionals.isEmpty())
			additionals = getAdditionalsFromMiapes();
		if (additionals != null) {
			Set<String> ret = new HashSet<String>();
			for (String additional : additionals) {
				ret.add(MTD + TAB + "additional" + TAB + additional);
			}
			return ret;
		}
		return null;
	}

	/**
	 * Gets a set of 'MTD pubmed 18632595'
	 * 
	 * @return
	 */
	public Set<String> getPubmeds() {
		if (pubmeds != null && !pubmeds.isEmpty()) {
			Set<String> ret = new HashSet<String>();
			for (String pubmed : pubmeds) {
				ret.add(MTD + TAB + "pubmed" + TAB + pubmed);
			}
			return ret;
		}
		return null;
	}

	/**
	 * Gets 'MTD resubmission_px PXD0000001'
	 * 
	 * @return
	 */
	public String getResubmissionPX() {
		if (resubmission_px != null) {
			return MTD + TAB + "resubmission_px" + TAB + resubmission_px;
		}
		return null;
	}

	/**
	 * Gets a set of 'MTD reanalysis_px PXD0000001'
	 * 
	 * @return
	 */
	public Set<String> getReanalysesPX() {
		if (reanalyses_px != null && !reanalyses_px.isEmpty()) {
			Set<String> ret = new HashSet<String>();
			for (String reanalysis_px : reanalyses_px) {
				ret.add(MTD + TAB + "reanalysis_px" + TAB + reanalysis_px);
			}
			return ret;
		}
		return null;
	}

	/**
	 * Gets 'MTD pride_login johnSmith'
	 * 
	 * @return
	 */
	public String getPXLogin() {
		if (login != null) {
			return MTD + TAB + "pride_login" + TAB + login;
		}
		return null;
	}

	private Set<String> getAdditionalsFromMiapes() {
		Set<String> ret = new HashSet<String>();
		List<MiapeMSDocument> miapeMSs = experimentList.getMiapeMSs();
		for (MiapeMSDocument miapeMSDocument : miapeMSs) {
			Set<String> additionals = PexMiapeParser
					.getAdditionals(miapeMSDocument);
			if (additionals != null && !additionals.isEmpty()) {
				ret.addAll(additionals);
			}
		}
		List<MiapeMSIDocument> miapeMSIs = experimentList.getMiapeMSIs();
		for (MiapeMSIDocument miapeMSIDocument : miapeMSIs) {
			Set<String> additionals = PexMiapeParser
					.getAdditionals(miapeMSIDocument);
			if (additionals != null && !additionals.isEmpty()) {
				ret.addAll(additionals);
			}
		}

		return ret;
	}

	private Set<String> getInstrumentsFromMiapes() {
		Set<String> ret = new HashSet<String>();
		List<MiapeMSDocument> miapeMSs = experimentList.getMiapeMSs();
		for (MiapeMSDocument miapeMSDocument : miapeMSs) {
			Set<String> instruments = PexMiapeParser
					.getInstruments(miapeMSDocument);
			if (instruments != null && !instruments.isEmpty()) {
				ret.addAll(instruments);
			}
		}

		return ret;
	}

	private Set<String> getSpeciesFromMiapes() {
		Set<String> ret = new HashSet<String>();
		List<MiapeMSDocument> miapeMSs = experimentList.getMiapeMSs();
		for (MiapeMSDocument miapeMSDocument : miapeMSs) {
			Set<String> species = PexMiapeParser.getSpecies(miapeMSDocument);
			if (species != null && !species.isEmpty()) {
				ret.addAll(species);
			}
		}

		return ret;
	}

	private String getSubmitterAffiliation() {
		if (submitter == null)
			submitter = getContact();
		String ret = "";
		if (submitter != null) {
			ret = submitter.getInstitution();
			if (submitter.getDepartment() != null)
				ret = ret + " " + submitter.getDepartment();
			return ret;
		}
		return null;
	}

	private String getSubmitterEmail() {
		if (submitter == null)
			submitter = getContact();
		String ret = "";
		if (submitter != null) {
			ret = submitter.getEmail();

			return ret;
		}
		return null;
	}

	private String getSubmitterName() {
		if (submitter == null)
			submitter = getContact();
		String ret = "";
		if (submitter != null) {
			ret = submitter.getName();
			if (submitter.getLastName() != null)
				ret = ret + " " + submitter.getLastName();
			return ret;
		}
		return null;
	}

	private Contact getContact() {
		Contact contact = null;
		List<MiapeMSDocument> miapeMSs = experimentList.getMiapeMSs();
		if (miapeMSs != null && !miapeMSs.isEmpty()) {
			for (MiapeMSDocument miapeMSDocument : miapeMSs) {
				contact = miapeMSDocument.getContact();
				if (contact != null)
					return contact;
			}
		}
		List<MiapeMSIDocument> miapeMSIs = experimentList.getMiapeMSIs();
		if (miapeMSIs != null && !miapeMSIs.isEmpty()) {
			for (MiapeMSIDocument miapeMSIDocument : miapeMSIs) {
				contact = miapeMSIDocument.getContact();
				if (contact != null)
					return contact;
			}
		}

		return null;
	}

	public List<String> getFileLines() {
		List<String> ret = new ArrayList<String>();
		HashMap<String, PexFileMapping> fileLocationsMapping = new HashMap<String, PexFileMapping>();
		List<PexFileMapping> totalFileList = new ArrayList<PexFileMapping>();
		int fileCounter = 1;
		// organize the files by experiments
		for (Experiment experiment : experimentList.getExperiments()) {
			File prideXmlFile = experiment.getPrideXMLFile();
			if (prideXmlFile != null) {
				// FILEMAPPINGS
				// PRIDE XML
				PexFileMapping prideXMLFileMapping = new PexFileMapping(
						"result", fileCounter++,
						prideXmlFile.getAbsolutePath(), null);

				totalFileList.add(prideXMLFileMapping);
				fileLocationsMapping.put(prideXMLFileMapping.getPath(),
						prideXMLFileMapping);

				// Iterate over replicates
				List<Replicate> replicates = experiment.getReplicates();
				for (Replicate replicate : replicates) {
					// PEak lists
					List<PexFileMapping> peakListFileMappings = new ArrayList<PexFileMapping>();
					// raw files
					List<PexFileMapping> rawFileMappings = new ArrayList<PexFileMapping>();
					// search engine output lists
					List<PexFileMapping> outputSearchEngineFileMappings = new ArrayList<PexFileMapping>();
					// MIAPE MS and MSI reports
					List<PexFileMapping> miapeReportFileMappings = new ArrayList<PexFileMapping>();
					// RAW FILES
					List<PexFile> rawFiles = getReplicateRawFiles(replicate);

					if (rawFiles != null) {
						for (PexFile rawFile : rawFiles) {
							if (!fileLocationsMapping.containsKey(rawFile
									.getFileLocation())) {
								PexFileMapping rawFileMapping = new PexFileMapping(
										"raw", fileCounter++,
										rawFile.getFileLocation(), null);
								rawFileMappings.add(rawFileMapping);
								fileLocationsMapping.put(
										rawFile.getFileLocation(),
										rawFileMapping);

								// PRIDE XML -> RAW file
								prideXMLFileMapping
										.addRelationship(rawFileMapping.getId());
							}
						}
					}

					// PEAK LISTS
					List<PexFile> peakListFiles = getPeakListFiles(replicate);
					List<PexFileMapping> replicatePeakListFileMappings = new ArrayList<PexFileMapping>();
					if (peakListFiles != null) {
						for (PexFile peakList : peakListFiles) {
							if (!fileLocationsMapping.containsKey(peakList
									.getFileLocation())) {
								PexFileMapping peakListFileMapping = new PexFileMapping(
										"peak", fileCounter++,
										peakList.getFileLocation(), null);
								peakListFileMappings.add(peakListFileMapping);
								replicatePeakListFileMappings
										.add(peakListFileMapping);
								fileLocationsMapping.put(
										peakList.getFileLocation(),
										peakListFileMapping);

								// PRIDE XML -> PEAK LIST file
								prideXMLFileMapping
										.addRelationship(peakListFileMapping
												.getId());
								// RAW file -> PEAK LIST file
								for (PexFileMapping rawFileMapping : rawFileMappings) {
									rawFileMapping
											.addRelationship(peakListFileMapping
													.getId());
								}

								// prideXMLFileMapping
								// .addRelationship(peakListFileMapping
								// .getId());
							}
						}
					}

					// MIAPE MS REPORTS
					List<PexFile> miapeMSReportFiles = getMiapeMSReportFiles(replicate);
					if (miapeMSReportFiles != null)
						for (PexFile miapeMSReportFile : miapeMSReportFiles) {
							if (!fileLocationsMapping
									.containsKey(miapeMSReportFile
											.getFileLocation())) {
								PexFileMapping miapeReportFileMapping = new PexFileMapping(
										"other", fileCounter++,
										miapeMSReportFile.getFileLocation(),
										null);
								miapeReportFileMappings
										.add(miapeReportFileMapping);
								fileLocationsMapping.put(
										miapeMSReportFile.getFileLocation(),
										miapeReportFileMapping);

								// PRIDE XML -> MIAPE MS report
								prideXMLFileMapping
										.addRelationship(miapeReportFileMapping
												.getId());
								// RAW file -> MIAPE MS report
								for (PexFileMapping rawFileMapping : rawFileMappings) {
									rawFileMapping
											.addRelationship(miapeReportFileMapping
													.getId());
								}
								// PEAK LIST file -> MIAPE MS report
								for (PexFileMapping peakListFileMapping : replicatePeakListFileMappings) {
									peakListFileMapping
											.addRelationship(miapeReportFileMapping
													.getId());
								}
							}
						}

					// SEARCH ENGINE OUTPUT FILES
					List<PexFile> searchEngineOutputFiles = getSearchEngineOutputFiles(replicate);
					List<PexFileMapping> replicatesearchEngineOutputFilesMappings = new ArrayList<PexFileMapping>();
					if (searchEngineOutputFiles != null) {
						for (PexFile peakList : searchEngineOutputFiles) {
							if (!fileLocationsMapping.containsKey(peakList
									.getFileLocation())) {
								PexFileMapping searchEngineOutputFileMapping = new PexFileMapping(
										"search", fileCounter++,
										peakList.getFileLocation(), null);
								outputSearchEngineFileMappings
										.add(searchEngineOutputFileMapping);
								replicatesearchEngineOutputFilesMappings
										.add(searchEngineOutputFileMapping);
								fileLocationsMapping.put(
										peakList.getFileLocation(),
										searchEngineOutputFileMapping);
								// PRIDE XML -> SEARCH ENGINE OUTPUT file
								prideXMLFileMapping
										.addRelationship(searchEngineOutputFileMapping
												.getId());
								// RAW file -> SEARCH ENGINE OUTPUT file
								for (PexFileMapping rawFileMapping : rawFileMappings) {
									rawFileMapping
											.addRelationship(searchEngineOutputFileMapping
													.getId());
								}
								// PEAK LIST FILE -> SEARCH ENGINE OUTPUT file
								for (PexFileMapping peakListFileMapping : replicatePeakListFileMappings) {
									peakListFileMapping
											.addRelationship(searchEngineOutputFileMapping
													.getId());
								}
							}
						}
					}

					// MIAPE MSI REPORTS
					List<PexFile> miapeMSIReportFiles = getMiapeMSIReportFiles(replicate);
					if (miapeMSIReportFiles != null)
						for (PexFile miapeMSIReportFile : miapeMSIReportFiles) {
							if (!fileLocationsMapping
									.containsKey(miapeMSIReportFile
											.getFileLocation())) {
								PexFileMapping miapeReportFileMapping = new PexFileMapping(
										"other", fileCounter++,
										miapeMSIReportFile.getFileLocation(),
										null);
								miapeReportFileMappings
										.add(miapeReportFileMapping);
								fileLocationsMapping.put(
										miapeMSIReportFile.getFileLocation(),
										miapeReportFileMapping);

								// PRIDE XML -> MIAPE MSI report
								prideXMLFileMapping
										.addRelationship(miapeReportFileMapping
												.getId());
								// RAW file -> MIAPE MSI report
								for (PexFileMapping rawFileMapping : rawFileMappings) {
									rawFileMapping
											.addRelationship(miapeReportFileMapping
													.getId());
								}
								// PEAK LIST FILE -> MIAPE MSI report
								for (PexFileMapping peakListFileMapping : replicatePeakListFileMappings) {
									peakListFileMapping
											.addRelationship(miapeReportFileMapping
													.getId());
								}
								// SEARCH ENGINE OUTPUT file -> MIAPE MSI report
								for (PexFileMapping searchEngineOutputFileMapping : replicatesearchEngineOutputFilesMappings) {
									searchEngineOutputFileMapping
											.addRelationship(miapeReportFileMapping
													.getId());
								}

							}
						}
					// Add all to the same list and then sort by id
					totalFileList.addAll(outputSearchEngineFileMappings);
					totalFileList.addAll(miapeReportFileMappings);
					totalFileList.addAll(peakListFileMappings);
					totalFileList.addAll(rawFileMappings);
				}
			}
		}

		// Sort the list of files
		Collections.sort(totalFileList, new Comparator<PexFileMapping>() {

			@Override
			public int compare(PexFileMapping o1, PexFileMapping o2) {

				return Integer.valueOf(o1.getId()).compareTo(
						Integer.valueOf(o2.getId()));

			}

		});
		for (PexFileMapping pexFileMapping : totalFileList) {
			ret.add(pexFileMapping.toString());
		}
		return ret;
	}

	private List<PexFile> getMiapeMSReportFiles(Replicate replicate) {
		if (miapeMSReports.containsKey(replicate))
			return miapeMSReports.get(replicate);
		return Collections.emptyList();
	}

	private List<PexFile> getMiapeMSIReportFiles(Replicate replicate) {
		if (miapeMSIReports.containsKey(replicate))
			return miapeMSIReports.get(replicate);
		return Collections.emptyList();
	}

	// private List<PexFile> getExperimentRawFiles(Experiment experiment) {
	// if (this.rawFileMapToExperiment.containsKey(experiment))
	// return this.rawFileMapToExperiment.get(experiment);
	// return Collections.emptyList();
	// }

	private List<PexFile> getReplicateRawFiles(Replicate rep) {
		if (rawFileMapToReplicate.containsKey(rep))
			return rawFileMapToReplicate.get(rep);
		return Collections.emptyList();
	}

	private List<PexFile> getPeakListFiles(Replicate replicate) {
		if (peakListFileMap.containsKey(replicate))
			return peakListFileMap.get(replicate);
		return Collections.emptyList();
	}

	private List<PexFile> getSearchEngineOutputFiles(Replicate replicate) {
		if (searchEngineOutputFileMap.containsKey(replicate))
			return searchEngineOutputFileMap.get(replicate);
		return Collections.emptyList();
	}

	/**
	 * Associate a list of MIAPE MS reports to a certain replicate
	 * 
	 * @param replicate
	 * @param files
	 */
	public void addReplicateMIAPEMSReportFiles(Replicate replicate,
			List<File> files) {
		if (replicate != null && files != null && !files.isEmpty()) {
			if (miapeMSReports.containsKey(replicate)) {
				for (File file : files) {
					PexFile pexFile = new PexFile(file.getAbsolutePath(),
							replicate);
					miapeMSReports.get(replicate).add(pexFile);
				}
			} else {
				List<PexFile> pexFiles = new ArrayList<PexFile>();
				for (File file : files) {
					PexFile pexFile = new PexFile(file.getAbsolutePath(),
							replicate);
					pexFiles.add(pexFile);
				}
				miapeMSReports.put(replicate, pexFiles);
			}
		}
	}

	/**
	 * Associate a list of MIAPE MSI reports to a certain replicate
	 * 
	 * @param replicate
	 * @param files
	 */
	public void addReplicateMIAPEMSIReportFiles(Replicate replicate,
			List<File> files) {
		if (replicate != null && files != null && !files.isEmpty()) {
			if (miapeMSIReports.containsKey(replicate)) {
				for (File file : files) {
					PexFile pexFile = new PexFile(file.getAbsolutePath(),
							replicate);
					miapeMSIReports.get(replicate).add(pexFile);
				}
			} else {
				List<PexFile> pexFiles = new ArrayList<PexFile>();
				for (File file : files) {
					PexFile pexFile = new PexFile(file.getAbsolutePath(),
							replicate);
					pexFiles.add(pexFile);
				}
				miapeMSIReports.put(replicate, pexFiles);
			}
		}
	}

	// /**
	// * Associate a list of rawFile paths to a certain experiment
	// *
	// * @param replicate
	// * @param files
	// */
	// public void addExperimentRawFiles(Replicate replicate, List<File> files)
	// {
	// Experiment experiment = replicate.getPreviousLevelIdentificationSet();
	// if (experiment != null && files != null && !files.isEmpty()) {
	// if (this.rawFileMapToExperiment.containsKey(experiment)) {
	// for (File file : files) {
	// PexFile pexRawFile = new PexFile(file.getAbsolutePath(),
	// replicate);
	// this.rawFileMapToExperiment.get(experiment).add(pexRawFile);
	// }
	// } else {
	// List<PexFile> pexRawFiles = new ArrayList<PexFile>();
	// for (File file : files) {
	// PexFile pexRawFile = new PexFile(file.getAbsolutePath(),
	// replicate);
	// pexRawFiles.add(pexRawFile);
	// }
	// this.rawFileMapToExperiment.put(experiment, pexRawFiles);
	// }
	// }
	// }

	/**
	 * Associate a list of rawFile paths to a certain replicate
	 * 
	 * @param replicate
	 * @param files
	 */
	public void addReplicateRawFiles(Replicate replicate, List<File> files) {
		if (replicate != null && files != null && !files.isEmpty()) {
			if (rawFileMapToReplicate.containsKey(replicate)) {
				for (File file : files) {
					PexFile pexRawFile = new PexFile(file.getAbsolutePath(),
							replicate);
					rawFileMapToReplicate.get(replicate).add(pexRawFile);
				}
			} else {
				List<PexFile> pexRawFiles = new ArrayList<PexFile>();
				for (File file : files) {
					PexFile pexRawFile = new PexFile(file.getAbsolutePath(),
							replicate);
					pexRawFiles.add(pexRawFile);
				}
				rawFileMapToReplicate.put(replicate, pexRawFiles);
			}
		}
	}

	public void removeReplicateRawFile(Replicate replicate, String nodeString) {
		if (replicate != null) {
			if (rawFileMapToReplicate.containsKey(replicate)) {
				final List<PexFile> files = rawFileMapToReplicate
						.get(replicate);
				while (files.iterator().hasNext()) {
					PexFile file = files.iterator().next();
					if (FilenameUtils.getName(file.getFileLocation()).equals(
							nodeString))
						files.iterator().remove();
				}
			}
		}
	}

	public void addReplicatePeakListFiles(Replicate replicate, List<File> files) {
		if (replicate != null && files != null && !files.isEmpty()) {
			if (peakListFileMap.containsKey(replicate)) {
				for (File file : files) {
					PexFile pexFile = new PexFile(file.getAbsolutePath(),
							replicate);
					peakListFileMap.get(replicate).add(pexFile);
				}
			} else {
				List<PexFile> pexFiles = new ArrayList<PexFile>();
				for (File file : files) {
					PexFile pexFile = new PexFile(file.getAbsolutePath(),
							replicate);
					pexFiles.add(pexFile);
				}
				peakListFileMap.put(replicate, pexFiles);
			}
		}
	}

	public void addReplicateSearchEngineOutputFiles(Replicate replicate,
			List<File> files) {
		if (replicate != null && files != null && !files.isEmpty()) {
			if (searchEngineOutputFileMap.containsKey(replicate)) {
				for (File file : files) {
					PexFile pexFile = new PexFile(file.getAbsolutePath(),
							replicate);
					searchEngineOutputFileMap.get(replicate).add(pexFile);
				}
			} else {
				List<PexFile> pexFiles = new ArrayList<PexFile>();
				for (File file : files) {
					PexFile pexFile = new PexFile(file.getAbsolutePath(),
							replicate);
					pexFiles.add(pexFile);
				}
				searchEngineOutputFileMap.put(replicate, pexFiles);
			}
		}

	}

	public HashMap<Replicate, Set<String>> getFilesToSkip() {
		return filesToSkip;
	}

}
