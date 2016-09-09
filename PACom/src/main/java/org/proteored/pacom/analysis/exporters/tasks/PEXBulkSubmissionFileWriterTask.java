package org.proteored.pacom.analysis.exporters.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.proteored.pacom.analysis.exporters.Exporter;
import org.proteored.pacom.analysis.exporters.ProteomeXchangeFilev2_1;
import org.proteored.pacom.utils.MiapeExtractorSoftware;

public class PEXBulkSubmissionFileWriterTask extends SwingWorker<Void, String>
		implements Exporter<File>, PropertyChangeListener {

	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	public static final String PEX_EXPORT_STARTED = "pex_started";
	public static final String PEX_EXPORT_DOWNLOADING_FILES = "pex_downloading";
	public static final String PEX_EXPORT_STEP = "pex_step";
	public static final String PEX_EXPORT_FINISH = "pex_finished";
	public static final String PEX_EXPORT_ERROR = "pex_error";
	public static final String PEX_EXPORT_EXPORTING_PRIDE_FILES = "pex_exporting_pride";
	private static final String COM = ProteomeXchangeFilev2_1.COM;
	private static final char TAB = Exporter.TAB;

	public List<PexExportingMessage> messages = new ArrayList<PexExportingMessage>();
	private final ProteomeXchangeFilev2_1 pexFile;
	private OutputStreamWriter out = null;

	public PEXBulkSubmissionFileWriterTask(ProteomeXchangeFilev2_1 pexFile) {
		this.pexFile = pexFile;
	}

	@Override
	public File export() {

		log.info("Starting pex  exporting");
		out = null;
		try {
			firePropertyChange(PEX_EXPORT_STARTED, null, null);
			// Check if output file is correct
			File outputFile = pexFile.getOutputFile();
			boolean created;
			if (!pexFile.getOutputFolder().exists()) {
				created = pexFile.getOutputFolder().mkdirs();
				log.info("Created:" + created);
			}
			out = new OutputStreamWriter(new FileOutputStream(outputFile),
					Charset.forName("UTF8"));

			// MIAPE EXTRACTOR HEADER
			writeMiapeExtractorHeader();

			// Metadata section
			writeMetadataSection();

			// File Mapping section
			writeFileMappingSection();

			// sample metadata section
			writeSampleMetadataSection();

			// wait if PRIDE xml files are being created in the previous
			// writeFileMappingSection function
			// while (waitingForPRIDECreation()) {
			// try {
			// Thread.sleep(2000);
			// log.info("Waiting for PRIDE creation");
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			// }

			// download files if necessary in the working directory
			// downloadFiles();

			// while (waitingForFileDownloading()) {
			// try {
			// Thread.sleep(2000);
			// log.info("Waiting for file(s) downloading");
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			// }

			if (out != null)
				try {
					out.close();
					log.info("Output stream closed");
					firePropertyChange(PEX_EXPORT_FINISH, null, outputFile);
				} catch (IOException e) {
					e.printStackTrace();
					log.info("Error closing output stream");
				}
			return outputFile;
		} catch (Exception e) {
			e.printStackTrace();
			firePropertyChange(PEX_EXPORT_ERROR, null, e.getMessage());
		} finally {

		}
		return null;
	}

	private void writeMiapeExtractorHeader() throws IOException {
		out.write(COM + TAB
				+ "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
				+ NEWLINE);
		out.write(COM
				+ TAB
				+ "This file has been created using the ProteoRed MIAPE Extractor v"
				+ new MiapeExtractorSoftware().getVersion() + NEWLINE + NEWLINE);
		out.write(COM
				+ TAB
				+ "This file is intended to be used in the ProteomeXchange Submission Tool as a Bulk submission"
				+ NEWLINE);
		out.write(COM
				+ TAB
				+ "Version of the bulk submission file supported: 2.0 (25 July 2013)"
				+ NEWLINE);
		out.write(COM
				+ TAB
				+ "Download the ProteomeXchange submission tool at: http://www.ebi.ac.uk/pride/resources/tools/submission-tool/latest/desktop/px-submission-tool.zip"
				+ NEWLINE);
		out.write(COM
				+ TAB
				+ "Open it and click on 'Bulk submission' in the main page in order to select this file"
				+ NEWLINE);
		out.write(NEWLINE);
		out.write(COM
				+ TAB
				+ "IMPORTANT: Before to use this file, please read it carefully and be sure that no MANDATORY lines are printed. If that is the case, insert the information requested in the appropiate format"
				+ NEWLINE);
		out.write(COM
				+ TAB
				+ "More information about the format of this file can be found at: http://www.proteomexchange.org/submission"
				+ NEWLINE);
		out.write(COM + TAB
				+ "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
				+ NEWLINE + NEWLINE + NEWLINE);
	}

	private void writeFileMappingSection() throws IOException {
		out.write(NEWLINE);

		// header
		out.write(NEWLINE + ProteomeXchangeFilev2_1.FMH + TAB + "file_id" + TAB
				+ "file_type" + TAB + "file_path" + TAB + "file_mapping"
				+ NEWLINE);
		// first, create the PRIDE XML files in the working directory

		// List<File> prideXMLFiles = this.pexFile.getExperimentList()
		// .getPrideXmlFiles();

		// if there is not any pride xml file previously created, we create them
		// here
		// if (prideXMLFiles == null || prideXMLFiles.isEmpty()) {
		// this.waitingForPRIDECreation = true;
		// firePropertyChange(PEX_EXPORT_EXPORTING_PRIDE_FILES, null, null);
		// PRIDEExporter prideExporter = new PRIDEExporter(
		// this.pexFile.getExperimentList(),
		// this.pexFile.getOutputFolder(), true, false);
		// prideExporter.addPropertyChangeListener(this);
		// prideExporter.execute();
		//
		// } else {

		List<String> fileLines = pexFile.getFileLines();
		if (fileLines != null) {
			for (String fileLine : fileLines) {
				out.write(fileLine + NEWLINE);
			}
		} else {
			out.write(COM
					+ TAB
					+ "There is not any file provided. See ProteomeXchange bulk submission documentation to know how to do it."
					+ NEWLINE);
			out.write(COM + TAB + ProteomeXchangeFilev2_1.FME + TAB + "1" + TAB
					+ "result" + TAB + "/path/to/mzIdentML/files/file.xml"
					+ NEWLINE);
			addError(
					"There is not any file provided",
					"Add a new File Mapping lines like: 'FME 1 result /path/to/mzIdentML/files/file.xml   2,3,4'");
		}
		// }
	}

	private void writeSampleMetadataSection() throws IOException {
		out.write(NEWLINE);

		// header
		out.write(NEWLINE + ProteomeXchangeFilev2_1.SMH + TAB + "file_id" + TAB
				+ "species" + TAB + "tissue" + TAB + "cell_type" + TAB
				+ "disease" + TAB + "quantification" + TAB + "instrument" + TAB
				+ "modification" + TAB + "experimental_factor" + NEWLINE);

		List<String> metadataLines = pexFile.getSampleMetadataLines();
		if (metadataLines != null) {
			for (String metadataLine : metadataLines) {
				out.write(metadataLine + NEWLINE);
			}
		} else {
			out.write(COM
					+ TAB
					+ "There is not any sample metadata line provided. See ProteomeXchange bulk submission documentation to know how to do it."
					+ NEWLINE);
			out.write(COM
					+ TAB
					+ ProteomeXchangeFilev2_1.SME
					+ TAB
					+ "1	[NEWT, 9606, Homo sapiens (Human),]	[BRENDA, BTO:0000142, brain,]	[MS, MS:1000447, LTQ,] [MOD,MOD:00394,acetylated residue,] Drug A at 10 mM"
					+ NEWLINE);
			addError(
					"There is not any sample metadata line provided",
					"Add a new File Mapping lines like: 'SME	1	[NEWT, 9606, Homo sapiens (Human),]	[BRENDA, BTO:0000142, brain,]	[MS, MS:1000447, LTQ,] [MOD,MOD:00394,acetylated residue,] Drug A at 10 mM'");
		}
		// }
	}

	private void writeMetadataSection() throws IOException {
		// submitter NAME
		String name = pexFile.getName();
		if (name != null) {
			out.write(name + NEWLINE);
		} else {
			out.write(COM + TAB
					+ "MANDATORY: First name and surname of the submitter."
					+ NEWLINE);
			out.write(COM + TAB + "MTD	submitter_name	John Arthur Smith"
					+ NEWLINE);
			addError("First name and surname of the submitter is empty",
					"Add a new metadata line like: 'MTD name John Arthur Smith'");
		}
		out.write(NEWLINE);

		// submiter Email
		String email = pexFile.getEmail();
		if (email != null) {
			out.write(email + NEWLINE);
		} else {
			out.write(COM
					+ TAB
					+ "MANDATORY: Email address of the submitter, this will also be used as the login user name to ProteomeXchange. This is used to assign dataset ownership"
					+ NEWLINE);
			out.write(COM + TAB + "MTD" + TAB + "submitter_name" + TAB
					+ "John Arthur Smith" + NEWLINE);
			addError("Email of the submitter is empty",
					"Add a new metadata line like: 'MTD" + TAB + "email" + TAB
							+ "john@proteored.org'");
		}
		out.write(NEWLINE);

		// submiter affiliation
		String affiliation = pexFile.getAffiliation();
		if (affiliation != null) {
			out.write(affiliation + NEWLINE);
		} else {
			out.write(COM
					+ TAB
					+ "MANDATORY: Name of the institute or university which submitter is a member."
					+ NEWLINE);
			out.write(COM + TAB + "MTD" + TAB + "submitter_affiliation" + TAB
					+ "ProteoRed" + NEWLINE);
			addError(
					"Affiliation of the submitter is empty",
					"Add a new metadata line like: 'MTD affiliation National Center for Biotechnology - CSIC - ProteoRed'");
		}
		out.write(NEWLINE);

		// lab head name
		String labHeadName = pexFile.getLabHeadName();
		if (labHeadName != null) {
			out.write(COM
					+ TAB
					+ "MANDATORY: Replace your name with the corresponding lab head name (if different, see below):"
					+ NEWLINE);
			out.write(COM
					+ TAB
					+ "(first name and surname of the principal investigator/lab head. If this person is the same one as the submitter, the information needs to be duplicated)."
					+ NEWLINE);
			out.write(labHeadName + NEWLINE);
		} else {
			out.write(COM
					+ TAB
					+ "MANDATORY: First name and surname of the principal investigator/lab head."
					+ NEWLINE);
			out.write(COM + TAB + "MTD" + TAB + "lab_head_name" + TAB
					+ "ProteoRed" + NEWLINE);
			addError("lab head name is empty",
					"Add a new metadata line like: 'MTD lab_head_name Juan Pablo Albar'");
		}
		out.write(NEWLINE);

		// lab head email
		String labHeadEmail = pexFile.getLabHeadEmail();
		if (labHeadEmail != null) {
			out.write(COM
					+ TAB
					+ "MANDATORY: Replace your name with the corresponding lab head email (if different, see below):"
					+ NEWLINE);
			out.write(COM
					+ TAB
					+ "(email address of the principal investigator/lab head. If this person is the same one as the submitter, the information needs to be duplicated)."
					+ NEWLINE);
			out.write(labHeadEmail + NEWLINE);
		} else {
			out.write(COM
					+ TAB
					+ "MANDATORY: Email address of the principal investigator/lab head."
					+ NEWLINE);
			out.write(COM + TAB + "MTD" + TAB + "lab_head_email" + TAB
					+ "ProteoRed" + NEWLINE);
			addError("lab head name is empty",
					"Add a new metadata line like: 'MTD lab_head_email jpalbar@proteored.org'");
		}
		out.write(NEWLINE);

		// lab head affiliation
		String labHeadAffiliation = pexFile.getLabHeadAffiliation();
		if (labHeadAffiliation != null) {
			out.write(COM
					+ TAB
					+ "MANDATORY: Replace your name with the corresponding lab head affiliation (if different, see below):"
					+ NEWLINE);
			out.write(COM
					+ TAB
					+ "(Name of the institute or university which the principal investigator/lab head is member. If this person is the same one as the submitter, the information needs to be duplicated)."
					+ NEWLINE);
			out.write(labHeadAffiliation + NEWLINE);
		} else {
			out.write(COM
					+ TAB
					+ "MANDATORY: Name of the institute or university which the principal investigator/lab head is member."
					+ NEWLINE);
			out.write(COM + TAB + "MTD" + TAB + "lab_head_affiliation" + TAB
					+ "ProteoRed" + NEWLINE);
			addError(
					"lab head name is empty",
					"Add a new metadata line like: 'MTD lab_head_affiliation National Center for Biotechnology'");
		}
		out.write(NEWLINE);

		// submitter pride login
		String login = pexFile.getSubmitterPrideLogin();
		if (login != null) {
			out.write(login + NEWLINE);
		} else {
			out.write(COM
					+ TAB
					+ "MANDATORY: The PRIDE login account user name. Submitters need to have a PRIDE account."
					+ NEWLINE);
			out.write(COM + TAB + "MTD" + TAB + "submitter_pride_login" + TAB
					+ "johnSmith" + NEWLINE);
			addError("There is not login information",
					"Add a new metadata line like: 'MTD submitter_pride_login johnSmith'");
		}
		out.write(NEWLINE);

		// project title
		String title = pexFile.getProjectTitle();
		if (title != null) {
			out.write(title + NEWLINE);
		} else {
			out.write(COM + TAB
					+ "MANDATORY: Title of the project been submitted."
					+ NEWLINE);
			out.write(COM + TAB + "MTD" + TAB + "project_title" + TAB
					+ "Plasma samples from ten male individuals" + NEWLINE);
			addError(
					"Title of the project is empty",
					"Add a new metadata line like: 'MTD project_title Spanish Human Proteome Project'");
		}
		out.write(NEWLINE);

		// project description
		String description = pexFile.getProjectDescription();
		if (description != null) {
			out.write(description + NEWLINE);
		} else {
			out.write(COM
					+ TAB
					+ "MANDATORY: A hort description of the experiment being submitted. This will be made publicly available in ProteomeCentral and serves as an abstract describing the submission, similar in concept to the abstract of a scientific publication."
					+ NEWLINE);
			out.write(COM + TAB + "MTD" + TAB + "project_description" + TAB
					+ "Here we present..." + NEWLINE);

			addError(
					"A short description of the experiment has not provided. This will be made publicly available in ProteomeCentral and serves as an abstract describing the submission, similar in copntact to the abstract of a scientific publication",
					"Add a new metadata line like: 'MTD project_description Here we present...'");
		}
		out.write(NEWLINE);

		// project_tag
		out.write(COM
				+ TAB
				+ "OPTIONAL: If the dataset is part of a larger project, this should be indicated here. The project_tags are assigned byt the repositories. If you would like to propose a new project_tag, please contact to pride-support@ebi.ac.uk"
				+ NEWLINE);
		out.write(COM + TAB + ProteomeXchangeFilev2_1.MTD + TAB + "project_tag"
				+ TAB + "Human proteome project" + NEWLINE);
		out.write(NEWLINE);

		// sample processing protocol
		String sampleProcessingProtocol = pexFile.getSampleProcessingProtocol();
		if (sampleProcessingProtocol != null) {
			// truncate if longer than 5000 characters
			if (sampleProcessingProtocol.length() > 5000)
				sampleProcessingProtocol = sampleProcessingProtocol.substring(
						0, 4999);
			out.write(sampleProcessingProtocol + NEWLINE);
		} else {
			out.write(COM
					+ TAB
					+ "MANDATORY: A short description of the sample processing protocol being followed"
					+ NEWLINE);
			out.write(COM + TAB + ProteomeXchangeFilev2_1.MTD + TAB
					+ "sample_processing_protocol" + TAB
					+ "The sample processing was..." + NEWLINE);

			addError("There is not any sample processing protocol.",
					"Add a new metadata line line: 'MTD	sample_processing_protocol	Not available'");
		}
		out.write(NEWLINE);

		// data processing protocol
		String dataProcessingProtocol = pexFile.getDataProcessingProtocol();
		if (dataProcessingProtocol != null) {
			// truncate if longer than 5000 characters
			if (dataProcessingProtocol.length() > 5000)
				dataProcessingProtocol = dataProcessingProtocol.substring(0,
						4999);
			out.write(dataProcessingProtocol + NEWLINE);
		} else {
			out.write(COM
					+ TAB
					+ "MANDATORY: A short description of the data processing protocol being followed"
					+ NEWLINE);
			out.write(COM + TAB + ProteomeXchangeFilev2_1.MTD + TAB
					+ "data_processing_protocol" + TAB
					+ "The data processing was..." + NEWLINE);
			addError("There is not any data processing protocol.",
					"Add a new metadata line line: 'MTD	data_processing_protocol	Not available'");
		}
		out.write(NEWLINE);

		// other omics link
		out.write(COM
				+ TAB
				+ "OPTIONAL: A short string in which links to ther 'omics' datasets generated by the same study."
				+ NEWLINE);
		out.write(COM
				+ TAB
				+ ProteomeXchangeFilev2_1.MTD
				+ TAB
				+ "other_omics_link"
				+ TAB
				+ "The corresponding lipidomics study is available at MetaboLights (accession number xxxxxx)"
				+ NEWLINE);
		out.write(NEWLINE);

		// keywords
		String keywords = pexFile.getKeywords();
		if (keywords != null) {
			out.write(keywords + NEWLINE);
		} else {
			out.write(COM
					+ TAB
					+ "MANDATORY: A list of keywords that describe the content and type of the experiment being submitted. Multiple entries should be comma separated, it is recommended to provide a minimum of three keywords"
					+ NEWLINE);
			out.write(COM + TAB + ProteomeXchangeFilev2_1.MTD + TAB
					+ "keywords" + TAB + "Human, Plasma, LS-MS" + NEWLINE);
			addError(
					"A list of keywords has not provided. A list of keywords that describe the content and type of the experiment being submitted. Multiple entries should be comma separated, it is recommended to provide a minimum od three separated keywords.",
					"Add a new metadata line like: 'MTD keywords Human, Plasma, LC-MS'");
		}
		out.write(NEWLINE);

		// submission type
		String submissionType = pexFile.getSubmissionType();
		if (submissionType != null) {
			out.write(submissionType + NEWLINE);
		} else {
			out.write(COM
					+ TAB
					+ "MANDATORY: Type of the submission depending on the files to be submitted. Allowed types are COMPLETE OR PARTIAL"
					+ NEWLINE);
			out.write(COM + TAB + ProteomeXchangeFilev2_1.MTD + TAB
					+ "submission_type" + TAB + "COMPLETE" + NEWLINE);
			addError("Submission type has not provided. ",
					"Add a new metadata line like: 'MTD type COMPLETE' or 'MTD type PARTIAL'");
		}
		out.write(NEWLINE);

		// experiment type
		Set<String> experimentTypes = pexFile.getExperimentTypes();
		if (experimentTypes != null) {
			for (String experimentType : experimentTypes) {
				out.write(experimentType + NEWLINE);
			}
		} else {
			out.write(COM
					+ TAB
					+ "MANDATORY: The current experiments types are listed as CVs in the ProteomeXchange bulk submission summary file documentation."
					+ NEWLINE);
			out.write(COM + TAB + ProteomeXchangeFilev2_1.MTD + TAB
					+ "experiment_type" + TAB
					+ "[PRIDE, PRIDE:0000311, SRM/MRM,]" + NEWLINE);

			addError(
					"Experiment type has not provided. ",
					"Add a new metadata line like: 'MTD experiment_type [PRIDE, PRIDE:0000427, Top-down proteomics, ]'");
		}
		out.write(NEWLINE);

		// reason for partial
		out.write(COM
				+ TAB
				+ "OPTIONAL: Comments to describe why a COMPLETE submission was not possible. Here the submitter must mention the search engine (and/or pipeline( used to generate the results."
				+ NEWLINE);
		out.write(COM
				+ TAB
				+ ProteomeXchangeFilev2_1.MTD
				+ TAB
				+ "reason_for_partial"
				+ TAB
				+ "The file was produced by pipeline XXX using the tool YY and ZZZ and the output format FFF was not supported by the PRIDE Converter."
				+ NEWLINE);

		out.write(NEWLINE);

		// species
		List<String> species = pexFile.getSpecies();
		if (species != null && !species.isEmpty()) {
			for (String specie : species) {
				out.write(specie + NEWLINE);
			}
		} else {
			out.write(COM
					+ TAB
					+ "MANDATORY: Controlled vocabulary term to describe a single species. NEWT CV terms are allowed."
					+ NEWLINE);
			out.write(COM + TAB + ProteomeXchangeFilev2_1.MTD + TAB + "species"
					+ TAB + "[NEWT, 9606, Homo Sapiens (Human),]" + NEWLINE);
			addError(
					"There is not any species provided",
					"Add a new metadata line like: 'MTD species [NEWT, 9606, Homo Sapiens (Human),]'");
		}
		out.write(NEWLINE);

		// tissues
		Set<String> tissues = pexFile.getTissues();
		if (tissues != null && !tissues.isEmpty()) {
			for (String tissue : tissues) {
				out.write(tissue + NEWLINE);
			}
		} else {
			out.write(COM
					+ TAB
					+ "MANDATORY: There is not any tissue. Please be sure that this is not applicable for your dataset. Otherwise replace the line below with another line like: "
					+ "'MTD tissue [BRENDA, BTO:0000142, brain,]'" + NEWLINE);

			out.write(ProteomeXchangeFilev2_1.MTD
					+ TAB
					+ "tissue"
					+ TAB
					+ "[PRIDE, PRIDE:0000442, Tissue not applicable to dataset,]"
					+ NEWLINE);
		}
		out.write(NEWLINE);

		// cell type
		Set<String> cellTypes = pexFile.getCellTypes();
		if (cellTypes != null) {
			for (String cellType : cellTypes) {
				out.write(cellType + NEWLINE);
			}
		} else {
			out.write(COM
					+ TAB
					+ "OPTIONAL: There is not any cell type information. If you want to add it, add a new line like: "
					+ "'MTD" + TAB + "cell_type" + TAB
					+ "[CL, CL:0000236, B cell,]'" + NEWLINE);

		}
		out.write(NEWLINE);

		// disease
		Set<String> diseases = pexFile.getDiseases();
		if (diseases != null && !diseases.isEmpty()) {
			for (String disease : diseases) {
				out.write(disease + NEWLINE);
			}
		} else {
			out.write(COM
					+ TAB
					+ "OPTIONAL: There is not any a disease information. If you want to add it, add a new line like: "
					+ "'MTD" + TAB + "disease" + TAB
					+ "[DOID, DOID:1319, brain cancer,]'" + NEWLINE);
		}
		out.write(NEWLINE);

		// quantification
		out.write(COM
				+ TAB
				+ "OPTIONAL: Controlled vocabulary terms to describe the quantification technique used in the experiment. At present, PRIDE ontology CV terms are recommended."
				+ NEWLINE);
		out.write(COM + TAB + ProteomeXchangeFilev2_1.MTD + TAB
				+ "quantification" + TAB + "[PRIDE, PRIDE:0000318, 18O,]"
				+ NEWLINE);
		out.write(NEWLINE);

		// instrument
		List<String> instruments = pexFile.getInstruments();
		if (instruments != null && !instruments.isEmpty()) {
			for (String instrument : instruments) {
				out.write(instrument + NEWLINE);
			}
		} else {
			out.write(COM
					+ TAB
					+ "MANDATORY: There is not any instrument information. It is necessary to provide at least one line like the one below:"
					+ NEWLINE);
			out.write(COM + TAB + ProteomeXchangeFilev2_1.MTD + TAB
					+ "instrument" + TAB + "[MS, MS:1000447, LTQ,]" + NEWLINE);
			addError("There is not any instrument provided ",
					"Add a new metadata line like: 'MTD instrument [MS, MS:1000447, LTQ,]'");
		}
		out.write(NEWLINE);

		// modification
		List<String> ptms = pexFile.getPTMs();
		if (ptms != null && !ptms.isEmpty()) {
			for (String ptm : ptms) {
				out.write(ptm + NEWLINE);
			}
		} else {
			out.write(COM
					+ TAB
					+ "OPTIONAL for complete submission and MANDATORY for partial submission. "
					+ "There is not any modification information. If you want to provide one, write a line like the follow one:"
					+ NEWLINE);
			out.write(COM + TAB + ProteomeXchangeFilev2_1.MTD + TAB
					+ "modification" + TAB
					+ "[MOD, MOD:00394, acetylated residue,]");
			addError(
					"There is not any PTM provided",
					"Add a new metadata line like: 'MTD modification [MOD, MOD:00394,acetylated residue,]'");
		}
		out.write(NEWLINE);

		// additional
		Set<String> additionals = pexFile.getAdditionals();
		if (additionals != null && !additionals.isEmpty()) {
			for (String additional : additionals) {
				out.write(additional + NEWLINE);
			}
		} else {
			out.write(COM
					+ TAB
					+ "OPTIONAL: Additional params that describe the submission. Only a single controlled vocabulary is allowed per line."
					+ NEWLINE);
			out.write(COM + TAB + ProteomeXchangeFilev2_1.MTD + TAB
					+ "additional" + TAB
					+ "[,,Patient, Colorectal cancer petient 1]" + NEWLINE);
		}
		out.write(NEWLINE);

		// pubmed id
		Set<String> pubmeds = pexFile.getPubmeds();
		if (pubmeds != null && !pubmeds.isEmpty()) {
			for (String pubmed : pubmeds) {
				out.write(pubmed + NEWLINE);
			}
		} else {
			out.write(COM
					+ TAB
					+ "OPTIONAL: The PubMed IDs assigned to the publication related to the dataset (if known(. Only a single ID is allowed per line."
					+ NEWLINE);
			out.write(COM + TAB + ProteomeXchangeFilev2_1.MTD + TAB
					+ "pubmed_id" + TAB + "18632595" + NEWLINE);
		}
		out.write(NEWLINE);

		// resubmission_px
		String resubmission_px = pexFile.getResubmissionPX();
		if (resubmission_px != null) {
			out.write(resubmission_px + NEWLINE);

		} else {
			out.write(COM
					+ TAB
					+ "OPTIONAL: If you are resubmitting a previously submitted ProteomeXchange submission, you should reference the original ProteomeXchange accession number. Only one accession is allowed since resubmissions should only happen on a per dataset basis."
					+ NEWLINE);
			out.write(COM + TAB + ProteomeXchangeFilev2_1.MTD + TAB
					+ "resubmission_px" + TAB + "PXD000001" + NEWLINE);
		}
		out.write(NEWLINE);

		// reanalysis_px
		Set<String> reanalyses_px = pexFile.getReanalysesPX();
		if (reanalyses_px != null && !reanalyses_px.isEmpty()) {
			for (String reanalysis_px : reanalyses_px) {
				out.write(reanalysis_px + NEWLINE);
			}
		} else {
			out.write(COM
					+ TAB
					+ "OPTIONAL: If your experiment is a reanalysis of (one or more) previous PX datasets, there should be referenced. Multiple entries are allowed, each referencing one PX accession."
					+ NEWLINE);
			out.write(COM + TAB + ProteomeXchangeFilev2_1.MTD + TAB
					+ "reanalysis_px" + TAB + "PXD000001" + NEWLINE);
		}
		out.write(NEWLINE);

		// comment
		String comment = pexFile.getComment();
		if (comment != null) {
			out.write(comment + NEWLINE);
		}
		out.write(NEWLINE);

	}

	private void addError(String body, String tip) {
		messages.add(new PexExportingMessage(body, tip));
	}

	@Override
	protected Void doInBackground() throws Exception {
		export();
		return null;
	}

	@Override
	protected void done() {

		super.done();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		try {
			if (evt.getPropertyName().equals(
					PRIDEExporterTask.PRIDE_EXPORTER_DONE)) {

				List<File> prideXMLFiles = (List<File>) evt.getNewValue();
				log.info("PRIDE Exporting done. " + prideXMLFiles.size()
						+ " files created.");

				// Then extract the file lines
				List<String> fileLines = pexFile.getFileLines();
				if (fileLines != null) {
					for (String fileLine : fileLines) {

						out.write(fileLine + NEWLINE);

					}
				} else {
					addError(
							"There is not any file provided",
							"Add a new File Mapping lines like: 'FME 1 result /path/to/mzIdentML/files/file.xml   2,3,4'");
				}
			} else if (evt.getPropertyName().equals(
					PRIDEExporterTask.PRIDE_EXPORTER_ERROR)
					|| evt.getPropertyName().equals(
							PRIDEExporterTask.SINGLE_PRIDE_EXPORTED_ERROR)) {
				log.info("PRIDE Exporting error: " + evt.getNewValue());
				firePropertyChange(PEX_EXPORT_ERROR, null, evt.getNewValue());
			} else if (evt.getPropertyName().equals(
					PRIDEExporterTask.SINGLE_PRIDE_EXPORTED)) {
				log.info("SINGLE PRIDE EXPORTED");
				firePropertyChange(
						PEX_EXPORT_STEP,
						null,
						"PRIDE XML saved to: '"
								+ ((File) evt.getNewValue()).getAbsolutePath()
								+ "'");
			} else if (evt.getPropertyName().equals(
					PRIDEExporterTask.SINGLE_PRIDE_EXPORTING_STARTED)) {
				log.info("SINGLE PRIDE EXPORTING STARTED");
				firePropertyChange(
						PEX_EXPORT_STEP,
						null,
						"Creating PRIDE XML for experiment '"
								+ evt.getNewValue() + "'");
			}
		} catch (Exception e) {
			e.printStackTrace();
			firePropertyChange(PEX_EXPORT_ERROR, null, e.getMessage());
		}
	}
}
