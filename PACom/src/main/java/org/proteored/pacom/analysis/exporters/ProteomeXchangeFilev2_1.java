package org.proteored.pacom.analysis.exporters;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;
import uk.ac.ebi.pride.utilities.pridemod.ModReader;
import uk.ac.ebi.pride.utilities.pridemod.model.PTM;

/**
 * This version of the proteomexchange file is according to specifications of
 * ProteomeXchange Submission Summary File Format Version 2.0 (25 July 2013)
 * 
 * @author Salva
 * 
 */
public class ProteomeXchangeFilev2_1 {
	private final static char TAB = Exporter.TAB;
	public final static String MTD = "MTD";
	public final static String FMH = "FMH";
	public final static String FME = "FME";
	public final static String COM = "COM";
	public final static String SMH = "SMH";
	public final static String SME = "SME";
	private final File outputFolder;
	private String outputFileName;
	private final ExperimentList experimentList;
	private Contact submitter;
	private String projectTitle;
	private String projectShortDescription;
	private final Set<String> keywords = new THashSet<String>();
	private PexSubmissionType submissionType;
	private String comment;
	private Set<String> species = new THashSet<String>();
	private Set<String> instruments = new THashSet<String>();
	private Set<String> additionals = new THashSet<String>();
	private Set<String> pubmeds = new THashSet<String>();
	private String resubmission_px;
	private Set<String> reanalyses_px = new THashSet<String>();
	private String login;
	private final Map<Experiment, List<PexFile>> rawFileMapToExperiment = new THashMap<Experiment, List<PexFile>>();
	private final Map<Replicate, List<PexFile>> rawFileMapToReplicate = new THashMap<Replicate, List<PexFile>>();
	private final Map<Replicate, List<PexFile>> peakListFileMap = new THashMap<Replicate, List<PexFile>>();
	private final Map<Replicate, List<PexFile>> miapeMSReports = new THashMap<Replicate, List<PexFile>>();
	private final Map<Replicate, List<PexFile>> miapeMSIReports = new THashMap<Replicate, List<PexFile>>();
	private final Map<Replicate, List<PexFile>> searchEngineOutputFileMap = new THashMap<Replicate, List<PexFile>>();
	ClassPathResource resource = new ClassPathResource("modification_mappings.xml");
	private final Map<Replicate, Set<String>> filesToSkip;
	private Set<String> sampleProcessingProtocols;
	private Set<String> dataProcessingProtocols;
	private Set<String> experimentTypes;
	private Set<String> tissues;
	private Set<String> cellTypes;
	private Set<String> diseases;
	private TIntObjectHashMap<List<String>> speciesByExperiment;
	private TIntObjectHashMap<List<String>> tissuesByExperiment;
	private TIntObjectHashMap<List<String>> cellTypesByExperiment;
	private TIntObjectHashMap<List<String>> diseaseByExperiment;
	private TIntObjectHashMap<List<String>> quantificationByExperiment;
	private TIntObjectHashMap<List<String>> instrumentByExperiment;
	private TIntObjectHashMap<List<String>> modificationByExperiment;
	private TIntObjectHashMap<List<String>> experimental_factorByExperiment;
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

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
	public ProteomeXchangeFilev2_1(File outputFolder, String outputFileName, ExperimentList experimentList,
			String projectTitle, String projectShortDescription, String keyword, String comment, Set<String> species,
			Set<String> instruments, Set<String> additionals, Set<String> pubmeds, String resubmissionPX,
			Set<String> reanalysesPX, String pride_login, Map<Replicate, Set<String>> filesToSkip2) {
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

	public ProteomeXchangeFilev2_1(File outputFolder, ExperimentList experimentList,
			Map<Replicate, Set<String>> filesToSkip2) {
		this.outputFolder = outputFolder;
		if (this.outputFolder == null)
			throw new IllegalMiapeArgumentException("Output folder is null!!");
		if (!this.outputFolder.exists()) {
			this.outputFolder.mkdirs();
		}
		if (!this.outputFolder.isDirectory())
			throw new IllegalMiapeArgumentException("Output folder must be a folder!!");

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
			reanalyses_px = new THashSet<String>();
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
				this.species = new THashSet<String>();
			for (final String specie : species) {

				final String parsedSpecie = PexMiapeParser.parseSpecie(specie, null);
				if (parsedSpecie != null)
					this.species.add(parsedSpecie);
			}
		}

	}

	public void addSpecie(String specie) {
		if (specie != null) {
			if (species == null)
				species = new THashSet<String>();

			final String parsedSpecie = PexMiapeParser.parseSpecie(specie, null);
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
		final String pathname = outputFolder.getAbsolutePath() + System.getProperty("file.separator")
				+ getOutputFileName();
		log.info(pathname);
		return new File(pathname);
	}

	/**
	 * Gets 'MTD submitter_name John Arthur Smith'
	 * 
	 * @return
	 */
	public String getName() {
		final String submitterName = getSubmitterName();
		if (submitterName != null)
			return ProteomeXchangeFilev2_1.MTD + TAB + "submitter_name" + TAB + submitterName;
		return null;
	}

	/**
	 * Gets 'MTD lab_head_name John Arthur Smith'
	 * 
	 * @return
	 */
	public String getLabHeadName() {
		final String submitterName = getSubmitterName();
		if (submitterName != null)
			return ProteomeXchangeFilev2_1.MTD + TAB + "lab_head_name" + TAB + submitterName;
		return null;
	}

	/**
	 * Gets 'MTD email john@proteored.org'
	 * 
	 * @return
	 */
	public String getEmail() {
		final String submitterEmail = getSubmitterEmail();
		if (submitterEmail != null)
			return ProteomeXchangeFilev2_1.MTD + TAB + "submitter_email" + TAB + submitterEmail;
		return null;
	}

	/**
	 * Gets 'MTD email john@proteored.org'
	 * 
	 * @return
	 */
	public String getLabHeadEmail() {
		final String submitterEmail = getSubmitterEmail();
		if (submitterEmail != null)
			return ProteomeXchangeFilev2_1.MTD + TAB + "lab_head_email" + TAB + submitterEmail;
		return null;
	}

	/**
	 * Gets 'MTD affiliation National Center for Biotechnology - CSIC -
	 * ProteoRed'
	 * 
	 * @return
	 */
	public String getAffiliation() {
		final String submitterAffiliation = getSubmitterAffiliation();
		if (submitterAffiliation != null)
			return ProteomeXchangeFilev2_1.MTD + TAB + "submitter_affiliation" + TAB + submitterAffiliation;
		return null;
	}

	/**
	 * Gets 'MTD lab_head_affiliation National Center for Biotechnology - CSIC -
	 * ProteoRed'
	 * 
	 * @return
	 */
	public String getLabHeadAffiliation() {
		final String submitterAffiliation = getSubmitterAffiliation();
		if (submitterAffiliation != null)
			return ProteomeXchangeFilev2_1.MTD + TAB + "lab_head_affiliation" + TAB + submitterAffiliation;
		return null;
	}

	/**
	 * Gets 'MTD title Spanish Human Proteome Project'
	 * 
	 * @return
	 */
	public String getProjectTitle() {

		if (projectTitle != null)
			return ProteomeXchangeFilev2_1.MTD + TAB + "project_title" + TAB + projectTitle;
		return null;
	}

	/**
	 * Gets 'MTD description Here we present...'
	 * 
	 * @return
	 */
	public String getProjectDescription() {
		if (projectShortDescription != null)
			return ProteomeXchangeFilev2_1.MTD + TAB + "project_description" + TAB + projectShortDescription;
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
			for (final String keyword : keywords) {
				if (!"".equals(keywordsString))
					keywordsString += ",";
				keywordsString += keyword;
			}
			return ProteomeXchangeFilev2_1.MTD + TAB + "keywords" + TAB + keywordsString;
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
			return ProteomeXchangeFilev2_1.MTD + TAB + "submission_type" + TAB + submissionType.toString();
		return null;
	}

	/**
	 * Gets 'MTD comment This file was produced by pipeline XX ...'
	 * 
	 * @return
	 */
	public String getComment() {
		if (comment != null)
			return ProteomeXchangeFilev2_1.COM + TAB
			// + "comment" + TAB
					+ comment;
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
			final List<String> ret = new ArrayList<String>();
			for (final String specie : species) {
				ret.add(ProteomeXchangeFilev2_1.MTD + TAB + "species" + TAB + specie);
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
			final List<String> ret = new ArrayList<String>();
			for (final String instrument : instruments) {
				ret.add(ProteomeXchangeFilev2_1.MTD + TAB + "instrument" + TAB + instrument);
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

		final List<String> modificationStrings = experimentList.getDifferentPeptideModificationNames();
		if (modificationStrings != null && !modificationStrings.isEmpty()) {
			final List<String> ret = new ArrayList<String>();
			final ControlVocabularyManager cvManager = OntologyLoaderTask.getCvManager();
			final ModReader preferredModifications = ModReader.getInstance();
			for (final String modificationString : modificationStrings) {
				String modification = "";
				ControlVocabularyTerm cvTerm = null;
				if (preferredModifications != null) {
					final List<PTM> mods = preferredModifications.getPTMListByEqualName(modificationString);
					if (mods != null && !mods.isEmpty()) {
						final String idPsiMod = mods.get(0).getAccession();
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
						log.info("Converting " + cvTerm.getTermAccession() + " " + cvTerm.getPreferredName()
								+ " to PSI-MOD");
						final PTM ptm = preferredModifications
								.getPTMbyAccession(cvTerm.getTermAccession().getAccession());
						if ((UNIMODOntology.getCVLabel() + ":" + ptm.getAccession())
								.equalsIgnoreCase(cvTerm.getTermAccession().toString())) {
							final ControlVocabularyTerm cvTerm2 = PeptideModificationName.getInstance(cvManager)
									.getCVTermByAccession(new Accession(ptm.getAccession()));
							if (cvTerm2 != null) {
								log.info(cvTerm.getTermAccession() + " " + cvTerm.getPreferredName() + " converter to "
										+ cvTerm2.getTermAccession() + " " + cvTerm2.getPreferredName());
								cvTerm = cvTerm2;
								break;
							}
						}

					}
					modification = "[" + cvTerm.getCVRef() + "," + cvTerm.getTermAccession().toString() + ","
							+ cvTerm.getPreferredName() + ",]";
				} else {
					// userParam
					modification = "[,," + modificationString + ",]";
				}
				ret.add(ProteomeXchangeFilev2_1.MTD + TAB + "modification" + TAB + modification);
			}
			return ret;
		} else {
			final List<String> ret = new ArrayList<String>();
			ret.add(ProteomeXchangeFilev2_1.MTD + TAB + "modification" + TAB
					+ "[PRIDE, PRIDE:0000398,No PTMs are included in the dataset,]");
		}
		return null;
	}

	/**
	 * Gets a list of 'MTD additional [xx,xxx,xx,xx]'
	 * 
	 * @return
	 */
	public Set<String> getAdditionals() {
		if (additionals == null || additionals.isEmpty())
			additionals = getAdditionalsFromMiapes();
		if (additionals != null && !additionals.isEmpty()) {
			final Set<String> ret = new THashSet<String>();
			for (final String additional : additionals) {
				ret.add(ProteomeXchangeFilev2_1.MTD + TAB + "additional" + TAB + additional);
			}
			return ret;
		}
		return null;
	}

	/**
	 * Get a list of 'MTD tissue {xx,xxx,xx,xx]'
	 * 
	 * @return
	 */
	public Set<String> getTissues() {
		if (tissues == null || tissues.isEmpty())
			tissues = getTissuesFromMiapes();
		if (tissues != null) {
			final Set<String> ret = new THashSet<String>();
			for (final String tissue : tissues) {
				ret.add(ProteomeXchangeFilev2_1.MTD + TAB + "tissue" + TAB + tissue);
			}
			return ret;
		}
		return null;
	}

	/**
	 * Get a list of 'MTD cell_type {xx,xxx,xx,xx]'
	 * 
	 * @return
	 */
	public Set<String> getCellTypes() {
		if (cellTypes == null || cellTypes.isEmpty())
			cellTypes = getCellTypesFromMiapes();
		if (cellTypes != null && !cellTypes.isEmpty()) {
			final Set<String> ret = new THashSet<String>();
			for (final String cellType : cellTypes) {
				ret.add(ProteomeXchangeFilev2_1.MTD + TAB + "tissue" + TAB + cellType);
			}
			return ret;
		}
		return null;
	}

	public Set<String> getDiseases() {
		if (diseases == null || diseases.isEmpty())
			diseases = getDiseasesFromMiapes();
		if (diseases != null) {
			final Set<String> ret = new THashSet<String>();
			for (final String tissue : diseases) {
				ret.add(ProteomeXchangeFilev2_1.MTD + TAB + "disease" + TAB + tissue);
			}
			return ret;
		}
		return null;
	}

	/**
	 * Gets 'MTD sample_processing_protocol protocol description string'
	 * 
	 * @return
	 */
	public String getSampleProcessingProtocol() {
		if (sampleProcessingProtocols == null) {
			sampleProcessingProtocols = getSampleProcessingProtocolFromMiapes();
		}
		String ret = "Not available";
		if (sampleProcessingProtocols != null && !sampleProcessingProtocols.isEmpty()) {
			ret = "";
			for (final String sampleProcessingProtocol : sampleProcessingProtocols) {
				ret += sampleProcessingProtocol + " ";
			}
			return ProteomeXchangeFilev2_1.MTD + TAB + "sample_processing_protocol" + TAB + ret.trim();
		}
		return null;

	}

	/**
	 * Gets 'MTD data_processing_protocol protocol description string'
	 * 
	 * @return
	 */
	public String getDataProcessingProtocol() {
		if (dataProcessingProtocols == null) {
			dataProcessingProtocols = getDataProcessingProtocolFromMiapes();
		}
		String ret = "Not available";
		if (dataProcessingProtocols != null && !dataProcessingProtocols.isEmpty()) {
			ret = "";
			for (final String dataProcessingProtocol : dataProcessingProtocols) {
				ret += dataProcessingProtocol + " ";
			}
			return ProteomeXchangeFilev2_1.MTD + TAB + "data_processing_protocol" + TAB + ret.trim();
		}
		return null;

	}

	public Set<String> getExperimentTypes() {
		if (experimentTypes == null) {
			experimentTypes = getExperimentTypesFromMiapes();
		}
		final Set<String> ret = new THashSet<String>();
		if (experimentTypes != null && !experimentTypes.isEmpty()) {
			for (final String experimentType : experimentTypes) {
				ret.add(experimentType.trim());
			}
		} else {
			ret.add(ProteomeXchangeFilev2_1.MTD + TAB + "experiment_type" + TAB
					+ "[PRIDE, PRIDE:0000429, Shotgun proteomics, ]");

		}
		if (!ret.isEmpty())
			return ret;
		return null;
	}

	/**
	 * Gets a set of 'MTD pubmed 18632595'
	 * 
	 * @return
	 */
	public Set<String> getPubmeds() {
		if (pubmeds != null && !pubmeds.isEmpty()) {
			final Set<String> ret = new THashSet<String>();
			for (final String pubmed : pubmeds) {
				ret.add(ProteomeXchangeFilev2_1.MTD + TAB + "pubmed" + TAB + pubmed);
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
			return ProteomeXchangeFilev2_1.MTD + TAB + "resubmission_px" + TAB + resubmission_px;
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
			final Set<String> ret = new THashSet<String>();
			for (final String reanalysis_px : reanalyses_px) {
				ret.add(ProteomeXchangeFilev2_1.MTD + TAB + "reanalysis_px" + TAB + reanalysis_px);
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
	public String getSubmitterPrideLogin() {
		if (login != null) {
			return ProteomeXchangeFilev2_1.MTD + TAB + "submitter_pride_login" + TAB + login;
		}
		return null;
	}

	private Set<String> getAdditionalsFromMiapes() {
		final Set<String> ret = new THashSet<String>();
		final List<MiapeMSDocument> miapeMSs = experimentList.getMiapeMSs();
		for (final MiapeMSDocument miapeMSDocument : miapeMSs) {
			final Set<String> additionals = PexMiapeParser.getAdditionals(miapeMSDocument);
			if (additionals != null && !additionals.isEmpty()) {
				ret.addAll(additionals);
			}
		}
		final List<MiapeMSIDocument> miapeMSIs = experimentList.getMiapeMSIs();
		for (final MiapeMSIDocument miapeMSIDocument : miapeMSIs) {
			final Set<String> additionals = PexMiapeParser.getAdditionals(miapeMSIDocument);
			if (additionals != null && !additionals.isEmpty()) {
				ret.addAll(additionals);
			}
		}

		return ret;
	}

	private Set<String> getDiseasesFromMiapes() {
		final List<MiapeMSDocument> miapeMSs = experimentList.getMiapeMSs();
		return getDiseasesFromMiapes(miapeMSs);
	}

	private Set<String> getDiseasesFromMiapes(List<MiapeMSDocument> miapeMSs) {
		final Set<String> ret = new THashSet<String>();
		for (final MiapeMSDocument miapeMSDocument : miapeMSs) {
			final Set<String> additionals = PexMiapeParser.getDisseases(miapeMSDocument);
			if (additionals != null && !additionals.isEmpty()) {
				ret.addAll(additionals);
			}
		}

		return ret;
	}

	private Set<String> getSampleProcessingProtocolFromMiapes() {
		final Set<String> ret = new THashSet<String>();
		final List<MiapeMSDocument> miapeMSs = experimentList.getMiapeMSs();
		for (final MiapeMSDocument miapeMSDocument : miapeMSs) {
			final Set<String> sampleProcessingProtocols = PexMiapeParser.getSampleProcessingProtocols(miapeMSDocument);
			if (sampleProcessingProtocols != null && !sampleProcessingProtocols.isEmpty()) {
				ret.addAll(sampleProcessingProtocols);
			}
		}
		return ret;
	}

	private Set<String> getDataProcessingProtocolFromMiapes() {
		final Set<String> ret = new THashSet<String>();
		final List<MiapeMSDocument> miapeMSs = experimentList.getMiapeMSs();
		for (final MiapeMSDocument miapeMSDocument : miapeMSs) {
			final Set<String> dataProcessingProtocols = PexMiapeParser.getDataProcessingProtocols(miapeMSDocument);
			if (dataProcessingProtocols != null && !dataProcessingProtocols.isEmpty()) {
				ret.addAll(dataProcessingProtocols);
			}
		}
		final List<MiapeMSIDocument> miapeMSIs = experimentList.getMiapeMSIs();
		for (final MiapeMSIDocument miapeMSIDocument : miapeMSIs) {
			final Set<String> dataProcessingProtocols = PexMiapeParser.getDataProcessingProtocols(miapeMSIDocument);
			if (dataProcessingProtocols != null && !dataProcessingProtocols.isEmpty()) {
				ret.addAll(dataProcessingProtocols);
			}
		}
		return ret;
	}

	private Set<String> getExperimentTypesFromMiapes() {
		final Set<String> ret = new THashSet<String>();
		final List<MiapeMSDocument> miapeMSs = experimentList.getMiapeMSs();
		for (final MiapeMSDocument miapeMSDocument : miapeMSs) {
			final Set<String> experimentTypes = PexMiapeParser.getExperimentTypes(miapeMSDocument);
			if (experimentTypes != null && !experimentTypes.isEmpty()) {
				ret.addAll(experimentTypes);
			}
		}
		final List<MiapeMSIDocument> miapeMSIs = experimentList.getMiapeMSIs();
		for (final MiapeMSIDocument miapeMSIDocument : miapeMSIs) {
			final Set<String> experimentTypes = PexMiapeParser.getExperimentTypes(miapeMSIDocument);
			if (experimentTypes != null && !experimentTypes.isEmpty()) {
				ret.addAll(experimentTypes);
			}
		}
		return ret;
	}

	private Set<String> getTissuesFromMiapes() {
		final List<MiapeMSDocument> miapeMSs = experimentList.getMiapeMSs();
		return getTissuesFromMiapes(miapeMSs);

	}

	private Set<String> getTissuesFromMiapes(List<MiapeMSDocument> miapeMSs) {
		final Set<String> ret = new THashSet<String>();
		for (final MiapeMSDocument miapeMSDocument : miapeMSs) {
			final Set<String> tissues = PexMiapeParser.getTissues(miapeMSDocument);
			if (tissues != null && !tissues.isEmpty()) {
				ret.addAll(tissues);
			} else {
				ret.add(PexMiapeParser.getTissueNotApplicable());
			}
		}
		return ret;

	}

	private Set<String> getCellTypesFromMiapes() {
		final List<MiapeMSDocument> miapeMSs = experimentList.getMiapeMSs();
		return getCellTypesFromMiapes(miapeMSs);

	}

	private Set<String> getCellTypesFromMiapes(List<MiapeMSDocument> miapeMSs) {
		final Set<String> ret = new THashSet<String>();
		for (final MiapeMSDocument miapeMSDocument : miapeMSs) {
			final Set<String> cellTypes = PexMiapeParser.getCellTypes(miapeMSDocument);
			if (cellTypes != null && !cellTypes.isEmpty()) {
				ret.addAll(cellTypes);
			}
		}
		return ret;

	}

	private Set<String> getInstrumentsFromMiapes() {
		final List<MiapeMSDocument> miapeMSs = experimentList.getMiapeMSs();
		return getInstrumentsFromMiapes(miapeMSs);
	}

	private Set<String> getInstrumentsFromMiapes(List<MiapeMSDocument> miapeMSs) {
		final Set<String> ret = new THashSet<String>();
		for (final MiapeMSDocument miapeMSDocument : miapeMSs) {
			final Set<String> instruments = PexMiapeParser.getInstruments(miapeMSDocument);
			if (instruments != null && !instruments.isEmpty()) {
				ret.addAll(instruments);
			}
		}

		return ret;
	}

	private Set<String> getSpeciesFromMiapes() {
		final List<MiapeMSDocument> miapeMSs = experimentList.getMiapeMSs();
		return getSpeciesFromMiapes(miapeMSs);
	}

	private Set<String> getSpeciesFromMiapes(List<MiapeMSDocument> miapeMSs) {
		final Set<String> ret = new THashSet<String>();
		for (final MiapeMSDocument miapeMSDocument : miapeMSs) {
			final Set<String> species = PexMiapeParser.getSpecies(miapeMSDocument);
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
			if (ret != null && !"".equals(ret))
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
			if (ret != null && !"".equals(ret))
				return ret;
		}
		return null;
	}

	private Contact getContact() {
		Contact contact = null;
		final List<MiapeMSDocument> miapeMSs = experimentList.getMiapeMSs();
		if (miapeMSs != null && !miapeMSs.isEmpty()) {
			for (final MiapeMSDocument miapeMSDocument : miapeMSs) {
				contact = miapeMSDocument.getContact();
				if (contact != null)
					return contact;
			}
		}
		final List<MiapeMSIDocument> miapeMSIs = experimentList.getMiapeMSIs();
		if (miapeMSIs != null && !miapeMSIs.isEmpty()) {
			for (final MiapeMSIDocument miapeMSIDocument : miapeMSIs) {
				contact = miapeMSIDocument.getContact();
				if (contact != null)
					return contact;
			}
		}

		return null;
	}

	private void addToMap(TIntObjectHashMap<List<String>> map, int key, String value) {
		if (map.containsKey(key)) {
			final List<String> list = map.get(key);
			if (!list.contains(value))
				list.add(value);
		} else {
			final List<String> list = new ArrayList<String>();
			list.add(value);
			map.put(key, list);
		}
	}

	public List<String> getFileLines() {
		// prepare the sample metadata maps:
		speciesByExperiment = new TIntObjectHashMap<List<String>>();
		tissuesByExperiment = new TIntObjectHashMap<List<String>>();
		cellTypesByExperiment = new TIntObjectHashMap<List<String>>();
		diseaseByExperiment = new TIntObjectHashMap<List<String>>();
		quantificationByExperiment = new TIntObjectHashMap<List<String>>();
		instrumentByExperiment = new TIntObjectHashMap<List<String>>();
		modificationByExperiment = new TIntObjectHashMap<List<String>>();
		experimental_factorByExperiment = new TIntObjectHashMap<List<String>>();

		final List<String> ret = new ArrayList<String>();
		final Map<String, PexFileMapping> fileLocationsMapping = new THashMap<String, PexFileMapping>();
		final List<PexFileMapping> totalFileList = new ArrayList<PexFileMapping>();
		int fileCounter = 1;
		// organize the files by experiments
		for (final Experiment experiment : experimentList.getExperiments()) {

			final File prideXmlFile = experiment.getPrideXMLFile();
			if (prideXmlFile != null) {
				// FILEMAPPINGS
				// PRIDE XML
				final int resultNum = fileCounter;
				final PexFileMapping prideXMLFileMapping = new PexFileMapping("result", fileCounter++,
						prideXmlFile.getAbsolutePath(), null);

				totalFileList.add(prideXMLFileMapping);
				fileLocationsMapping.put(prideXMLFileMapping.getPath(), prideXMLFileMapping);

				// Iterate over replicates
				final List<Replicate> replicates = experiment.getReplicates();
				for (final Replicate replicate : replicates) {
					// sample metadatas
					addSampleMetadatas(resultNum, replicate);

					// PEak lists
					final List<PexFileMapping> peakListFileMappings = new ArrayList<PexFileMapping>();
					// raw files
					final List<PexFileMapping> rawFileMappings = new ArrayList<PexFileMapping>();
					// search engine output lists
					final List<PexFileMapping> outputSearchEngineFileMappings = new ArrayList<PexFileMapping>();
					// MIAPE MS and MSI reports
					final List<PexFileMapping> miapeReportFileMappings = new ArrayList<PexFileMapping>();
					// RAW FILES
					final List<PexFile> rawFiles = getReplicateRawFiles(replicate);

					if (rawFiles != null) {
						for (final PexFile rawFile : rawFiles) {
							if (!fileLocationsMapping.containsKey(rawFile.getFileLocation())) {
								final PexFileMapping rawFileMapping = new PexFileMapping("raw", fileCounter++,
										rawFile.getFileLocation(), null);
								rawFileMappings.add(rawFileMapping);
								fileLocationsMapping.put(rawFile.getFileLocation(), rawFileMapping);

								// PRIDE XML -> RAW file
								prideXMLFileMapping.addRelationship(rawFileMapping.getId());
							}
						}
					}

					// PEAK LISTS
					final List<PexFile> peakListFiles = getPeakListFiles(replicate);
					final List<PexFileMapping> replicatePeakListFileMappings = new ArrayList<PexFileMapping>();
					if (peakListFiles != null) {
						for (final PexFile peakList : peakListFiles) {
							if (!fileLocationsMapping.containsKey(peakList.getFileLocation())) {
								final PexFileMapping peakListFileMapping = new PexFileMapping("peak", fileCounter++,
										peakList.getFileLocation(), null);
								peakListFileMappings.add(peakListFileMapping);
								replicatePeakListFileMappings.add(peakListFileMapping);
								fileLocationsMapping.put(peakList.getFileLocation(), peakListFileMapping);

								// PRIDE XML -> PEAK LIST file
								prideXMLFileMapping.addRelationship(peakListFileMapping.getId());
								// RAW file -> PEAK LIST file
								for (final PexFileMapping rawFileMapping : rawFileMappings) {
									rawFileMapping.addRelationship(peakListFileMapping.getId());
								}

								// prideXMLFileMapping
								// .addRelationship(peakListFileMapping
								// .getId());
							}
						}
					}

					// MIAPE MS REPORTS
					final List<PexFile> miapeMSReportFiles = getMiapeMSReportFiles(replicate);
					if (miapeMSReportFiles != null)
						for (final PexFile miapeMSReportFile : miapeMSReportFiles) {
							if (!fileLocationsMapping.containsKey(miapeMSReportFile.getFileLocation())) {
								final PexFileMapping miapeReportFileMapping = new PexFileMapping("other", fileCounter++,
										miapeMSReportFile.getFileLocation(), null);
								miapeReportFileMappings.add(miapeReportFileMapping);
								fileLocationsMapping.put(miapeMSReportFile.getFileLocation(), miapeReportFileMapping);

								// PRIDE XML -> MIAPE MS report
								prideXMLFileMapping.addRelationship(miapeReportFileMapping.getId());
								// RAW file -> MIAPE MS report
								for (final PexFileMapping rawFileMapping : rawFileMappings) {
									rawFileMapping.addRelationship(miapeReportFileMapping.getId());
								}
								// PEAK LIST file -> MIAPE MS report
								for (final PexFileMapping peakListFileMapping : replicatePeakListFileMappings) {
									peakListFileMapping.addRelationship(miapeReportFileMapping.getId());
								}
							}
						}

					// SEARCH ENGINE OUTPUT FILES
					final List<PexFile> searchEngineOutputFiles = getSearchEngineOutputFiles(replicate);
					final List<PexFileMapping> replicatesearchEngineOutputFilesMappings = new ArrayList<PexFileMapping>();
					if (searchEngineOutputFiles != null) {
						for (final PexFile peakList : searchEngineOutputFiles) {
							if (!fileLocationsMapping.containsKey(peakList.getFileLocation())) {
								final PexFileMapping searchEngineOutputFileMapping = new PexFileMapping("search",
										fileCounter++, peakList.getFileLocation(), null);
								outputSearchEngineFileMappings.add(searchEngineOutputFileMapping);
								replicatesearchEngineOutputFilesMappings.add(searchEngineOutputFileMapping);
								fileLocationsMapping.put(peakList.getFileLocation(), searchEngineOutputFileMapping);
								// PRIDE XML -> SEARCH ENGINE OUTPUT file
								prideXMLFileMapping.addRelationship(searchEngineOutputFileMapping.getId());
								// RAW file -> SEARCH ENGINE OUTPUT file
								for (final PexFileMapping rawFileMapping : rawFileMappings) {
									rawFileMapping.addRelationship(searchEngineOutputFileMapping.getId());
								}
								// PEAK LIST FILE -> SEARCH ENGINE OUTPUT file
								for (final PexFileMapping peakListFileMapping : replicatePeakListFileMappings) {
									peakListFileMapping.addRelationship(searchEngineOutputFileMapping.getId());
								}
							}
						}
					}

					// MIAPE MSI REPORTS
					final List<PexFile> miapeMSIReportFiles = getMiapeMSIReportFiles(replicate);
					if (miapeMSIReportFiles != null)
						for (final PexFile miapeMSIReportFile : miapeMSIReportFiles) {
							if (!fileLocationsMapping.containsKey(miapeMSIReportFile.getFileLocation())) {
								final PexFileMapping miapeReportFileMapping = new PexFileMapping("other", fileCounter++,
										miapeMSIReportFile.getFileLocation(), null);
								miapeReportFileMappings.add(miapeReportFileMapping);
								fileLocationsMapping.put(miapeMSIReportFile.getFileLocation(), miapeReportFileMapping);

								// PRIDE XML -> MIAPE MSI report
								prideXMLFileMapping.addRelationship(miapeReportFileMapping.getId());
								// RAW file -> MIAPE MSI report
								for (final PexFileMapping rawFileMapping : rawFileMappings) {
									rawFileMapping.addRelationship(miapeReportFileMapping.getId());
								}
								// PEAK LIST FILE -> MIAPE MSI report
								for (final PexFileMapping peakListFileMapping : replicatePeakListFileMappings) {
									peakListFileMapping.addRelationship(miapeReportFileMapping.getId());
								}
								// SEARCH ENGINE OUTPUT file -> MIAPE MSI report
								for (final PexFileMapping searchEngineOutputFileMapping : replicatesearchEngineOutputFilesMappings) {
									searchEngineOutputFileMapping.addRelationship(miapeReportFileMapping.getId());
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

				return Integer.valueOf(o1.getId()).compareTo(Integer.valueOf(o2.getId()));

			}

		});
		for (final PexFileMapping pexFileMapping : totalFileList) {
			ret.add(pexFileMapping.toString());
		}
		return ret;
	}

	private void addSampleMetadatas(int fileCounter, Replicate replicate) {
		final List<MiapeMSDocument> miapeMSs = replicate.getMiapeMSs();

		// species
		final Set<String> speciesFromMiapes = getSpeciesFromMiapes(miapeMSs);
		for (final String specie : speciesFromMiapes) {
			addToMap(speciesByExperiment, fileCounter, specie);
		}
		// tissue
		final Set<String> tissuesFromMiapes = getTissuesFromMiapes(miapeMSs);
		for (final String tissue : tissuesFromMiapes) {
			addToMap(tissuesByExperiment, fileCounter, tissue);
		}
		// cellType
		final Set<String> cellTypesFromMiapes = getCellTypesFromMiapes(miapeMSs);
		for (final String cellType : cellTypesFromMiapes) {
			addToMap(cellTypesByExperiment, fileCounter, cellType);
		}
		// disease
		final Set<String> diseasesFromMiapes = getDiseasesFromMiapes(miapeMSs);
		for (final String disease : diseasesFromMiapes) {
			addToMap(diseaseByExperiment, fileCounter, disease);
		}
		// quantification
		// not supported

		// instrument
		final Set<String> instrumentsFromMiapes = getInstrumentsFromMiapes(miapeMSs);
		for (final String instrument : instrumentsFromMiapes) {
			addToMap(instrumentByExperiment, fileCounter, instrument);
		}
		// experimental_factor
		// not supported
	}

	private String listToString(List<String> list, String separator) {
		final StringBuilder sb = new StringBuilder();
		for (final String string : list) {
			if (!"".equals(sb.toString()))
				sb.append(separator);
			sb.append(string);
		}
		return sb.toString();
	}

	public List<String> getSampleMetadataLines() {

		final List<String> ret = new ArrayList<String>();
		for (int expCounter = 1; expCounter < 10000; expCounter++) {

			final StringBuilder sb = new StringBuilder();
			// file id
			if (speciesByExperiment.containsKey(expCounter) || tissuesByExperiment.containsKey(expCounter)
					|| cellTypesByExperiment.containsKey(expCounter) || diseaseByExperiment.containsKey(expCounter)
					|| quantificationByExperiment.containsKey(expCounter)
					|| instrumentByExperiment.containsKey(expCounter)
					|| modificationByExperiment.containsKey(expCounter)
					|| experimental_factorByExperiment.containsKey(expCounter))
				sb.append(SME + TAB + String.valueOf(expCounter) + TAB);
			else
				continue;
			// species
			if (speciesByExperiment.containsKey(expCounter)) {
				final List<String> list = speciesByExperiment.get(expCounter);
				sb.append(listToString(list, ","));
			}
			sb.append(TAB);
			// tissue
			if (tissuesByExperiment.containsKey(expCounter)) {
				final List<String> list = tissuesByExperiment.get(expCounter);
				sb.append(listToString(list, ","));
			}
			sb.append(TAB);
			// celltype
			if (cellTypesByExperiment.containsKey(expCounter)) {
				final List<String> list = cellTypesByExperiment.get(expCounter);
				sb.append(listToString(list, ","));
			}
			sb.append(TAB);
			// disease
			if (diseaseByExperiment.containsKey(expCounter)) {
				final List<String> list = diseaseByExperiment.get(expCounter);
				sb.append(listToString(list, ","));
			}
			sb.append(TAB);

			// quantification
			if (quantificationByExperiment.containsKey(expCounter)) {
				final List<String> list = quantificationByExperiment.get(expCounter);
				sb.append(listToString(list, ","));
			}
			sb.append(TAB);
			// instrument
			if (instrumentByExperiment.containsKey(expCounter)) {
				final List<String> list = instrumentByExperiment.get(expCounter);
				sb.append(listToString(list, ","));
			}
			sb.append(TAB);
			// modifications
			if (modificationByExperiment.containsKey(expCounter)) {
				final List<String> list = modificationByExperiment.get(expCounter);
				sb.append(listToString(list, ","));
			}
			sb.append(TAB);
			// experimental factor
			if (experimental_factorByExperiment.containsKey(expCounter)) {
				final List<String> list = experimental_factorByExperiment.get(expCounter);
				sb.append(listToString(list, ","));
			}
			ret.add(sb.toString());

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
	public void addReplicateMIAPEMSReportFiles(Replicate replicate, List<File> files) {
		if (replicate != null && files != null && !files.isEmpty()) {
			if (miapeMSReports.containsKey(replicate)) {
				for (final File file : files) {
					final PexFile pexFile = new PexFile(file.getAbsolutePath(), replicate);
					miapeMSReports.get(replicate).add(pexFile);
				}
			} else {
				final List<PexFile> pexFiles = new ArrayList<PexFile>();
				for (final File file : files) {
					final PexFile pexFile = new PexFile(file.getAbsolutePath(), replicate);
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
	public void addReplicateMIAPEMSIReportFiles(Replicate replicate, List<File> files) {
		if (replicate != null && files != null && !files.isEmpty()) {
			if (miapeMSIReports.containsKey(replicate)) {
				for (final File file : files) {
					final PexFile pexFile = new PexFile(file.getAbsolutePath(), replicate);
					miapeMSIReports.get(replicate).add(pexFile);
				}
			} else {
				final List<PexFile> pexFiles = new ArrayList<PexFile>();
				for (final File file : files) {
					final PexFile pexFile = new PexFile(file.getAbsolutePath(), replicate);
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
				for (final File file : files) {
					final PexFile pexRawFile = new PexFile(file.getAbsolutePath(), replicate);
					rawFileMapToReplicate.get(replicate).add(pexRawFile);
				}
			} else {
				final List<PexFile> pexRawFiles = new ArrayList<PexFile>();
				for (final File file : files) {
					final PexFile pexRawFile = new PexFile(file.getAbsolutePath(), replicate);
					pexRawFiles.add(pexRawFile);
				}
				rawFileMapToReplicate.put(replicate, pexRawFiles);
			}
		}
	}

	public void removeReplicateRawFile(Replicate replicate, String nodeString) {
		if (replicate != null) {
			if (rawFileMapToReplicate.containsKey(replicate)) {
				final List<PexFile> files = rawFileMapToReplicate.get(replicate);
				while (files.iterator().hasNext()) {
					final PexFile file = files.iterator().next();
					if (FilenameUtils.getName(file.getFileLocation()).equals(nodeString))
						files.iterator().remove();
				}
			}
		}
	}

	public void addReplicatePeakListFiles(Replicate replicate, List<File> files) {
		if (replicate != null && files != null && !files.isEmpty()) {
			if (peakListFileMap.containsKey(replicate)) {
				for (final File file : files) {
					final PexFile pexFile = new PexFile(file.getAbsolutePath(), replicate);
					peakListFileMap.get(replicate).add(pexFile);
				}
			} else {
				final List<PexFile> pexFiles = new ArrayList<PexFile>();
				for (final File file : files) {
					final PexFile pexFile = new PexFile(file.getAbsolutePath(), replicate);
					pexFiles.add(pexFile);
				}
				peakListFileMap.put(replicate, pexFiles);
			}
		}
	}

	public void addReplicateSearchEngineOutputFiles(Replicate replicate, List<File> files) {
		if (replicate != null && files != null && !files.isEmpty()) {
			if (searchEngineOutputFileMap.containsKey(replicate)) {
				for (final File file : files) {
					final PexFile pexFile = new PexFile(file.getAbsolutePath(), replicate);
					searchEngineOutputFileMap.get(replicate).add(pexFile);
				}
			} else {
				final List<PexFile> pexFiles = new ArrayList<PexFile>();
				for (final File file : files) {
					final PexFile pexFile = new PexFile(file.getAbsolutePath(), replicate);
					pexFiles.add(pexFile);
				}
				searchEngineOutputFileMap.put(replicate, pexFiles);
			}
		}

	}

	public Map<Replicate, Set<String>> getFilesToSkip() {
		return filesToSkip;
	}

}
