package org.proteored.pacom.gui.tasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.swing.SwingWorker;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.cv.ms.MSFileType;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.exceptions.MiapeSecurityException;
import org.proteored.miapeapi.exceptions.WrongXMLFormatException;
import org.proteored.miapeapi.factories.ms.MiapeMSDocumentFactory;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.interfaces.ms.ResultingData;
import org.proteored.miapeapi.interfaces.msi.MiapeMSIDocument;
import org.proteored.miapeapi.interfaces.xml.MiapeXmlFile;
import org.proteored.miapeapi.text.tsv.MiapeTSVFile;
import org.proteored.miapeapi.text.tsv.msi.TableTextFileSeparator;
import org.proteored.miapeapi.xml.SchemaValidator;
import org.proteored.miapeapi.xml.dtaselect.MiapeDTASelectFile;
import org.proteored.miapeapi.xml.dtaselect.MiapeMsiDocumentImpl;
import org.proteored.miapeapi.xml.ms.MIAPEMSXmlFile;
import org.proteored.miapeapi.xml.ms.MiapeMSXmlFactory;
import org.proteored.miapeapi.xml.ms.merge.MiapeMSMerger;
import org.proteored.miapeapi.xml.msi.MIAPEMSIXmlFile;
import org.proteored.miapeapi.xml.msi.MiapeMSIXmlFactory;
import org.proteored.miapeapi.xml.mzidentml.MiapeMzIdentMLFile;
import org.proteored.miapeapi.xml.mzml.MiapeMSDocumentImpl;
import org.proteored.miapeapi.xml.mzml.MiapeMzMLFile;
import org.proteored.miapeapi.xml.mzml.lightParser.utils.MzMLLightParser;
import org.proteored.miapeapi.xml.pepxml.MiapePepXMLFile;
import org.proteored.miapeapi.xml.pride.AbstractDocumentFromPride;
import org.proteored.miapeapi.xml.pride.MSIMiapeFactory;
import org.proteored.miapeapi.xml.pride.MSMiapeFactory;
import org.proteored.miapeapi.xml.pride.MiapeMsPrideXmlFile;
import org.proteored.miapeapi.xml.pride.MiapeMsiPrideXmlFile;
import org.proteored.miapeapi.xml.xtandem.MiapeXTandemFile;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.analysis.util.LocalFilesIndex;
import org.proteored.pacom.exceptions.MiapeExtractionException;
import org.proteored.pacom.utils.PropertiesReader;

import uk.ac.ebi.jmzml.model.mzml.MzML;

/**
 * Class that transforms a standard XML file to a MIAPE document and MIAPE
 * documents to Standard XML Files
 *
 * @author Salvador
 *
 */
public class MiapeLocalExtractor {
	private boolean schemaValidation;

	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private final SwingWorker<Void, Void> swingWorker;
	private final int idJob;
	private final boolean processInParallel;

	public MiapeLocalExtractor(SwingWorker<Void, Void> standard2miapeTaskManager, int idJob,
			boolean processInParallel) {
		swingWorker = standard2miapeTaskManager;
		this.idJob = idJob;
		this.processInParallel = processInParallel;
		try {
			schemaValidation = Boolean
					.valueOf(PropertiesReader.getProperties().getProperty(PropertiesReader.SCHEMA_VALIDATION));
		} catch (Exception e) {
			// by default, if the properties file fails
			schemaValidation = false;
		}
		log.info("schema validation = " + schemaValidation);
	}

	protected ControlVocabularyManager getControlVocabularyManager() {
		return OntologyLoaderTask.getCvManager();
		// return DefaultControlVocabularyManager.getInstance();
	}

	/**
	 * Extracts MIAPE MS information from a PRIDE file and stores it in a MIAPE
	 * MS document in the repository.
	 *
	 * @param prideURL
	 *            accessible URL that points to the PRIDE file
	 * @param userName
	 *            username for authentication in the repository
	 * @param password
	 *            password for authentication in the repository
	 * @param projectName
	 *            the project in which the MIAPE will be created. If it doesn't
	 *            exist, the project is created.
	 * @return an array with two elements: the first (index=0) is the ID of the
	 *         MIAPE MS. If something is wrong, an explanatory string from the
	 *         error.
	 * @throws IllegalMiapeArgumentException
	 * @throws MiapeSecurityException
	 * @throws MiapeDatabaseException_Exception
	 * @throws IOException
	 */
	public File storeMiapeMSFromPRIDE(String prideURL, String projectName) {

		int id_ms = -1;
		StringBuilder sb = new StringBuilder();
		try {
			log.info("fileName " + prideURL);

			log.info("projectName " + projectName);

			MiapeMSDocument msDocument = null;

			File inputFile = new File(prideURL);

			// Validate file
			if (schemaValidation) {
				SchemaValidator.validateXMLFile(inputFile, SchemaValidator.prideXML);
			}

			// save the file to local folders
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputFile.getAbsolutePath()));
			inputFile = FileManager.saveLocalFile(inputFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputFile.getAbsolutePath());
			// index file by project name
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputFile);

			log.info("parsing PRIDE XML to document MS");
			msDocument = MSMiapeFactory.getFactory().toDocument(new MiapeMsPrideXmlFile(inputFile), null,
					getControlVocabularyManager(), null, null, projectName);
			((AbstractDocumentFromPride) msDocument).setAttachedFileLocation(inputFile.getAbsolutePath());

			log.info("Storing document MS");
			log.info("Converting document to xml");
			final MiapeXmlFile<MiapeMSDocument> msDocumentXML = msDocument.toXml();

			id_ms = LocalFilesIndex.getInstance().getFreeIndex();
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputFile);
			return saveMSLocally(id_ms, msDocumentXML, projectName);
			// index by miape ID

		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());
	}

	/**
	 *
	 * @param miapeXML
	 * @param projectName
	 * @param miapeType
	 * @return the identifier of the MIAPE saved in String format
	 * @throws IOException
	 */
	private File saveMSILocally(int miapeID, MiapeXmlFile miapeXML, String projectName, String fileName)
			throws IOException {

		return FileManager.saveLocalMiapeMSI(miapeID, (MIAPEMSIXmlFile) miapeXML, projectName, fileName);

	}

	private File saveMSLocally(int miapeID, MiapeXmlFile miapeXML, String projectName) throws IOException {

		return FileManager.saveLocalMiapeMS(miapeID, (MIAPEMSXmlFile) miapeXML, projectName);

	}

	public File storeMiapeMSFromPRIDEAndMetadata(String prideURL, String miapeMSMetadataURL, String projectName) {

		int id_ms = -1;
		StringBuilder sb = new StringBuilder();
		try {
			log.info("fileName " + prideURL);

			log.info("projectName " + projectName);

			MiapeMSDocument msDocument = null;
			File inputFile = new File(prideURL);

			// Validate file
			if (schemaValidation) {
				SchemaValidator.validateXMLFile(inputFile, SchemaValidator.prideXML);
			}
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputFile.getAbsolutePath()));
			inputFile = FileManager.saveLocalFile(inputFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputFile);

			log.info("parsing PRIDE XML to document MS");
			msDocument = MSMiapeFactory.getFactory().toDocument(new MiapeMsPrideXmlFile(inputFile), null,
					getControlVocabularyManager(), null, null, projectName);
			((AbstractDocumentFromPride) msDocument).setAttachedFileLocation(inputFile.getAbsolutePath());

			log.info("Getting MIAPE MS Metadata");
			MiapeMSDocument miapeMSMetadata = MiapeMSXmlFactory.getFactory().toDocument(
					new MIAPEMSXmlFile(miapeMSMetadataURL), getControlVocabularyManager(), null, null, null);
			log.info("Merging MIAPE MS from mzML and MIAPE MS METADATA FILE");
			final MiapeMSDocument miapeMSMerged = MiapeMSMerger
					.getInstance(getControlVocabularyManager(), null, null, null).merge(msDocument, miapeMSMetadata);

			log.info("Storing document MS");
			log.info("Converting document to xml");
			final MiapeXmlFile<MiapeMSDocument> msDocumentXML = miapeMSMerged.toXml();

			id_ms = LocalFilesIndex.getInstance().getFreeIndex();
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputFile);
			return saveMSLocally(id_ms, msDocumentXML, projectName);

		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());
	}

	/**
	 * Extracts MIAPE MSI information from a PRIDE file and stores it in a MIAPE
	 * MSI document in the repository.
	 *
	 * @param prideURL
	 *            accessible URL that points to the PRIDE file
	 * @param userName
	 *            username for authentication in the repository
	 * @param password
	 *            password for authentication in the repository
	 * @param projectName
	 *            the project in which the MIAPE will be created. If it doesn't
	 *            exist, the project is created.
	 * @return the ID of the MIAPE MSI (if applicable). If something is wrong,
	 *         an explanatory string from the error.
	 */
	public File storeMiapeMSIFromPRIDE(String prideURL, String projectName) {
		int id_msi = -1;
		StringBuilder sb = new StringBuilder();
		log.info("fileName " + prideURL);

		log.info("projectName " + projectName);

		MiapeMSIDocument msiDocument = null;
		File inputFile = null;

		try {
			inputFile = new File(prideURL);
			// Validate file
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputFile, SchemaValidator.prideXML);

			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputFile.getAbsolutePath()));
			inputFile = FileManager.saveLocalFile(inputFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputFile);
			log.info("createDocument");

			log.info("parsing PRIDE to document MSI");
			msiDocument = MSIMiapeFactory.getFactory().create(new MiapeMsiPrideXmlFile(inputFile), null,
					getControlVocabularyManager(), null, null, projectName);
			((AbstractDocumentFromPride) msiDocument).setAttachedFileLocation(inputFile.getAbsolutePath());

			log.info("Storing document MSI");
			log.info("Converting document to xml");
			id_msi = LocalFilesIndex.getInstance().getFreeIndex();
			msiDocument.setId(id_msi);
			final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputFile);
			return saveMSILocally(id_msi, msiDocumentXML, projectName, FilenameUtils.getBaseName(prideURL));

		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());
	}

	/**
	 * Extracts MIAPE MS and MSI information from a PRIDE file and stores it in
	 * a MIAPE MS and MSI documents in the repository.
	 *
	 * @param prideURL
	 *            accessible URL that points to the PRIDE file
	 * @param userName
	 *            username for authentication in the repository
	 * @param password
	 *            password for authentication in the repository
	 * @param projectName
	 *            the project in which the MIAPEs will be created. If it doesn't
	 *            exist, the project is created.
	 * @return an array with two elements: the first (index=0) is the ID of the
	 *         MIAPE MS (if applicable) the second (index=1) is the ID of the
	 *         MIAPE MSI (if applicable). If something is wrong, the first
	 *         element is an explanatory string from the error.
	 */
	public File[] storeMiapeMSMSIFromPRIDE(String prideURL, String projectName) {
		File[] identifiers = new File[2];
		int id_ms = -1;
		int id_msi = -1;
		StringBuilder sb = new StringBuilder();
		try {
			log.info("fileName " + prideURL);

			log.info("projectName " + projectName);

			MiapeMSDocument msDocument = null;
			MiapeMSIDocument msiDocument = null;
			File inputFile = new File(prideURL);

			// Validate file
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputFile, SchemaValidator.prideXML);

			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputFile.getAbsolutePath()));
			inputFile = FileManager.saveLocalFile(inputFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputFile);
			log.info("createDocument");

			log.info("parsing XML to document MS");
			msDocument = MSMiapeFactory.getFactory().toDocument(new MiapeMsPrideXmlFile(inputFile), null,
					getControlVocabularyManager(), null, null, projectName);
			((AbstractDocumentFromPride) msDocument).setAttachedFileLocation(inputFile.getAbsolutePath());

			log.info("parsing XML to document MSI");
			msiDocument = MSIMiapeFactory.getFactory().create(new MiapeMsiPrideXmlFile(inputFile), null,
					getControlVocabularyManager(), null, null, projectName);
			((AbstractDocumentFromPride) msiDocument).setAttachedFileLocation(inputFile.getAbsolutePath());
			// Associate the MIAPE MS with the already existing MIAPE MSI

			log.info("Associating MIAPE MS (still not stored) with MIAPE MSI (still not stored)");
			((org.proteored.miapeapi.xml.pride.msi.MiapeMSIDocumentImpl) msiDocument)
					.setReferencedMSDocument(msDocument.getId());

			log.info("Storing document MS");
			log.info("Converting document to xml");
			final MiapeXmlFile<MiapeMSDocument> msDocumentXML = msDocument.toXml();
			id_ms = LocalFilesIndex.getInstance().getFreeIndex();
			identifiers[0] = saveMSLocally(id_ms, msDocumentXML, projectName);
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputFile);

			log.info("Storing document MSI");
			log.info("Converting document to xml");

			// System.out.println(msiDocumentXML);

			id_msi = LocalFilesIndex.getInstance().getFreeIndex();
			msiDocument.setId(id_msi);
			final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
			identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName, FilenameUtils.getBaseName(prideURL));
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputFile);

			return identifiers;

		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());
	}

	public File[] storeMiapeMSMSIFromPRIDEAndMetadata(String prideURL, String miapeMSMetadataURL, String projectName) {
		File[] identifiers = new File[2];
		int id_ms = -1;
		int id_msi = -1;
		StringBuilder sb = new StringBuilder();
		try {
			log.info("fileName " + prideURL);

			log.info("projectName " + projectName);

			MiapeMSDocument msDocument = null;
			MiapeMSIDocument msiDocument = null;
			File inputFile = new File(prideURL);

			// Validate file
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputFile, SchemaValidator.prideXML);

			log.info("createDocument");
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputFile.getAbsolutePath()));
			inputFile = FileManager.saveLocalFile(inputFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputFile);

			log.info("parsing XML to document MS");
			msDocument = MSMiapeFactory.getFactory().toDocument(new MiapeMsPrideXmlFile(inputFile), null,
					getControlVocabularyManager(), null, null, projectName);
			((AbstractDocumentFromPride) msDocument).setAttachedFileLocation(inputFile.getAbsolutePath());

			log.info("Getting MIAPE MS Metadata");
			MiapeMSDocument miapeMSMetadata = MiapeMSXmlFactory.getFactory().toDocument(
					new MIAPEMSXmlFile(miapeMSMetadataURL), getControlVocabularyManager(), null, null, null);
			log.info("Merging MIAPE MS from mzML and MIAPE MS METADATA FILE");
			final MiapeMSDocument miapeMSMerged = MiapeMSMerger
					.getInstance(getControlVocabularyManager(), null, null, null).merge(msDocument, miapeMSMetadata);

			log.info("parsing XML to document MSI");
			msiDocument = MSIMiapeFactory.getFactory().create(new MiapeMsiPrideXmlFile(inputFile), null,
					getControlVocabularyManager(), null, null, projectName);
			((AbstractDocumentFromPride) msiDocument).setAttachedFileLocation(inputFile.getAbsolutePath());
			// Associate the MIAPE MS with the already existing MIAPE MSI

			log.info("Associating MIAPE MS (still not stored) with MIAPE MSI (still not stored)");
			((org.proteored.miapeapi.xml.pride.msi.MiapeMSIDocumentImpl) msiDocument)
					.setReferencedMSDocument(msDocument.getId());

			log.info("Storing document MS");
			log.info("Converting document to xml");
			final MiapeXmlFile<MiapeMSDocument> msDocumentXML = miapeMSMerged.toXml();

			id_ms = LocalFilesIndex.getInstance().getFreeIndex();
			identifiers[0] = saveMSLocally(id_ms, msDocumentXML, projectName);
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputFile);

			log.info("Storing document MSI");
			log.info("Converting document to xml");
			// System.out.println(msiDocumentXML);

			id_msi = LocalFilesIndex.getInstance().getFreeIndex();
			msiDocument.setId(id_msi);
			final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
			identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName, FilenameUtils.getBaseName(prideURL));
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputFile);

			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());
	}

	/**
	 * Extracts MIAPE MS information from a mzML file and stores it in a MIAPE
	 * MS document in the repository.
	 *
	 * @param mzMLURL
	 *            accessible URL that points to the mzML file
	 * @param userName
	 *            username for authentication in the repository
	 * @param password
	 *            password for authentication in the repository
	 * @param projectName
	 *            the project in which the MIAPE will be created. If it doesn't
	 *            exist, the project is created.
	 * @param fastParsing
	 *            indicate if the mzML is parsed using a faster method. Some
	 *            minimal information could be missed, but huge mzML files will
	 *            be parsed in seconds, instead of minutes.
	 * @return the ID of the MIAPE MS. If something is wrong, an explanatory
	 *         string from the error.
	 */
	public File storeMiapeMSFromMzML(String mzMLURL, String projectName, boolean fastParsing) {
		int id_ms = -1;

		log.info("fileName " + mzMLURL);

		log.info("projectName " + projectName);
		StringBuilder sb = new StringBuilder();
		MiapeMSDocument msDocument = null;
		File inputFile = new File(mzMLURL);
		// Validate file
		try {
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputFile, SchemaValidator.mzMLIdx);
		} catch (Exception ex) {
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputFile, SchemaValidator.mzML);
		}

		try {
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputFile.getAbsolutePath()));
			inputFile = FileManager.saveLocalFile(inputFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputFile);

			if (fastParsing) {
				log.info("parsing mzML XML to document MS in the faster method (SAX+DOM method)");
				MzMLLightParser mzMLParser = new MzMLLightParser(inputFile.getAbsolutePath());
				MzML mzmlLight = mzMLParser.ParseDocument(schemaValidation);

				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(mzmlLight, null,
						getControlVocabularyManager(), null, null, inputFile.getName(), projectName);

			} else {
				log.info("parsing mzML XML to document MS in the slower method (jmzML API)");
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(
						new MiapeMzMLFile(inputFile), null, getControlVocabularyManager(), null, null, projectName);

			}
			// add mzML to attached file
			((MiapeMSDocumentImpl) msDocument).setAttachedFileLocation(inputFile.getAbsolutePath());
			// add mzML to resulting data
			ResultingData resultingData = getResultingDataFromMZML(mzMLURL);
			((MiapeMSDocumentImpl) msDocument).addResultingData(resultingData);

			log.info("MIAPE created");
			log.info("Storing document MS");
			log.info("Converting document to xml");
			final MiapeXmlFile<MiapeMSDocument> msDocumentXML = msDocument.toXml();
			id_ms = LocalFilesIndex.getInstance().getFreeIndex();
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputFile);
			return saveMSLocally(id_ms, msDocumentXML, projectName);

		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());
	}

	/**
	 * Extracts MIAPE MS information from a mzML file and stores it in a MIAPE
	 * MS document in the repository.
	 *
	 * @param mzMLURL
	 *            accessible URL that points to the mzML file
	 * @param userName
	 *            username for authentication in the repository
	 * @param password
	 *            password for authentication in the repository
	 * @param projectName
	 *            the project in which the MIAPE will be created. If it doesn't
	 *            exist, the project is created.
	 * @param fastParsing
	 *            indicate if the mzML is parsed using a faster method. Some
	 *            minimal information could be missed, but huge mzML files will
	 *            be parsed in seconds, instead of minutes.
	 * @return the ID of the MIAPE MS. If something is wrong, an explanatory
	 *         string from the error.
	 */
	public File storeMiapeMSFromMzMLAndMetadata(String mzMLURL, String miapeMSMetadataURL, String projectName,
			boolean fastParsing) {
		int id_ms = -1;

		log.info("fileName " + mzMLURL);

		log.info("projectName " + projectName);
		StringBuilder sb = new StringBuilder();
		MiapeMSDocument msDocument = null;
		File inputFile = new File(mzMLURL);
		// Validate file
		try {
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputFile, SchemaValidator.mzMLIdx);
		} catch (Exception ex) {
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputFile, SchemaValidator.mzML);
		}

		try {
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputFile.getAbsolutePath()));
			inputFile = FileManager.saveLocalFile(inputFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputFile);

			if (fastParsing) {
				log.info("parsing mzML XML to document MS in the faster method (SAX+DOM method)");
				MzMLLightParser mzMLParser = new MzMLLightParser(inputFile.getAbsolutePath());
				MzML mzmlLight = mzMLParser.ParseDocument(schemaValidation);

				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(mzmlLight, null,
						getControlVocabularyManager(), null, null, inputFile.getName(), projectName);

			} else {
				log.info("parsing mzML XML to document MS in the slower method (jmzML API)");
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(
						new MiapeMzMLFile(inputFile), null, getControlVocabularyManager(), null, null, projectName);

			}
			// add mzML to attached file
			((MiapeMSDocumentImpl) msDocument).setAttachedFileLocation(inputFile.getAbsolutePath());
			// add mzML to resulting data
			List<ResultingData> resultingDatas = msDocument.getResultingDatas();
			ResultingData resultingData = getResultingDataFromMZML(mzMLURL);
			((MiapeMSDocumentImpl) msDocument).addResultingData(resultingData);

			log.info("Getting MIAPE MS Metadata");
			MiapeMSDocument miapeMSMetadata = MiapeMSXmlFactory.getFactory().toDocument(
					new MIAPEMSXmlFile(miapeMSMetadataURL), getControlVocabularyManager(), null, null, null);
			List<ResultingData> resultingDatas2 = miapeMSMetadata.getResultingDatas();
			if (resultingDatas2 != null && !resultingDatas2.isEmpty()) {
				resultingDatas.addAll(resultingDatas2);
			}
			log.info("Merging MIAPE MS from mzML and MIAPE MS METADATA FILE");
			final MiapeMSDocument miapeMSMerged = MiapeMSMerger
					.getInstance(getControlVocabularyManager(), null, null, null).merge(msDocument, miapeMSMetadata);

			log.info("MIAPE created");
			log.info("Storing document MS");
			log.info("Converting document to xml");
			final MiapeXmlFile<MiapeMSDocument> msDocumentXML = miapeMSMerged.toXml();
			id_ms = LocalFilesIndex.getInstance().getFreeIndex();
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputFile);
			return saveMSLocally(id_ms, msDocumentXML, projectName);

		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());
	}

	/**
	 * Extracts MIAPE MS information from a mzML file and stores it in a MIAPE
	 * MS document in the repository.
	 *
	 * @param mzMLURL
	 *            accessible URL that points to the mzML file
	 * @param userName
	 *            username for authentication in the repository
	 * @param password
	 *            password for authentication in the repository
	 * @param projectName
	 *            the project in which the MIAPE will be created. If it doesn't
	 *            exist, the project is created.
	 * @param fastParsing
	 *            indicate if the mzML is parsed using a faster method. Some
	 *            minimal information could be missed, but huge mzML files will
	 *            be parsed in seconds, instead of minutes.
	 * @return the ID of the MIAPE MS. If something is wrong, an explanatory
	 *         string from the error.
	 */
	public File[] storeMiapeMSMSIFromMzMLAndMzIdentML(String mzMLURL, String mzIdentMLURL, String projectName,
			boolean fastParsing) {
		int id_ms = -1;
		int id_msi = -1;
		File[] identifiers = new File[2];
		StringBuilder sb = new StringBuilder();

		log.info("fileName " + mzMLURL);
		log.info("fileName2 " + mzIdentMLURL);

		log.info("projectName " + projectName);

		MiapeMSDocument msDocument = null;
		MiapeMSIDocument msiDocument = null;
		File inputmzMLFile = new File(mzMLURL);
		// Validate file
		try {
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputmzMLFile, SchemaValidator.mzMLIdx);
		} catch (Exception ex) {
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputmzMLFile, SchemaValidator.mzML);
		}
		try {

			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputmzMLFile.getAbsolutePath()));
			inputmzMLFile = FileManager.saveLocalFile(inputmzMLFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputmzMLFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputmzMLFile);

			if (fastParsing) {
				log.info("parsing mzML XML to document MS in the faster method (SAX+DOM method)");
				MzMLLightParser mzMLParser = new MzMLLightParser(inputmzMLFile.getAbsolutePath());
				MzML mzmlLight = mzMLParser.ParseDocument(schemaValidation);
				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(mzmlLight, null,
						getControlVocabularyManager(), null, null, inputmzMLFile.getName(), projectName);
			} else {
				log.info("parsing mzML XML to document MS in the slower method (jmzML API)");
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(
						new MiapeMzMLFile(inputmzMLFile), null, getControlVocabularyManager(), null, null, projectName);
			}

			// add mzML to attached file
			((MiapeMSDocumentImpl) msDocument).setAttachedFileLocation(inputmzMLFile.getAbsolutePath());
			// add mzML to resulting data
			ResultingData resultingData = getResultingDataFromMZML(inputmzMLFile.getAbsolutePath());
			((MiapeMSDocumentImpl) msDocument).addResultingData(resultingData);
			log.info("MIAPE MS created");
			log.info("Storing document MS");
			log.info("Converting document to xml");
			final MiapeXmlFile<MiapeMSDocument> msDocumentXML = msDocument.toXml();
			id_ms = LocalFilesIndex.getInstance().getFreeIndex();
			identifiers[0] = saveMSLocally(id_ms, msDocumentXML, projectName);
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputmzMLFile);

			// ////////////////////////////////////
			log.info("createFile");
			File inputmzIdentMLFile = new File(mzIdentMLURL);
			// Validate file
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputmzIdentMLFile, SchemaValidator.mzIdentML_1_0);

			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputmzIdentMLFile.getAbsolutePath()));
			inputmzIdentMLFile = FileManager.saveLocalFile(inputmzIdentMLFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputmzIdentMLFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputmzIdentMLFile);

			log.info("create MIAPE MSI from mzIdentML");
			MiapeMzIdentMLFile xmlFile = new MiapeMzIdentMLFile(inputmzIdentMLFile);
			msiDocument = org.proteored.miapeapi.xml.mzidentml.MSIMiapeFactory.getFactory().toDocument(xmlFile, null,
					getControlVocabularyManager(), null, null, projectName, processInParallel);
			((org.proteored.miapeapi.xml.mzidentml.MiapeMSIDocumentImpl) msiDocument)
					.setAttachedFileURL(inputmzIdentMLFile.getAbsolutePath());
			if (id_ms > 0) {
				((org.proteored.miapeapi.xml.mzidentml.MiapeMSIDocumentImpl) msiDocument)
						.setReferencedMSDocument(Integer.valueOf(id_ms));
			}
			log.info("MIAPE MSI created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");

			id_msi = LocalFilesIndex.getInstance().getFreeIndex();
			msiDocument.setId(id_msi);
			final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();

			identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName,
					FilenameUtils.getBaseName(mzIdentMLURL));
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputmzIdentMLFile);

			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());

	}

	/**
	 * Extracts MIAPE MS information from a mzML file and stores it in a MIAPE
	 * MS document in the repository.
	 *
	 * @param mzMLURL
	 *            accessible URL that points to the mzML file
	 * @param userName
	 *            username for authentication in the repository
	 * @param password
	 *            password for authentication in the repository
	 * @param projectName
	 *            the project in which the MIAPE will be created. If it doesn't
	 *            exist, the project is created.
	 * @param fastParsing
	 *            indicate if the mzML is parsed using a faster method. Some
	 *            minimal information could be missed, but huge mzML files will
	 *            be parsed in seconds, instead of minutes.
	 * @return the ID of the MIAPE MS. If something is wrong, an explanatory
	 *         string from the error.
	 */
	public File[] storeMiapeMSMSIFromMzMLAndMzIdentMLAndMetadata(String mzMLURL, String miapeMSMetadataURL,
			String mzIdentMLURL, String projectName, boolean fastParsing) {
		int id_ms = -1;
		int id_msi = -1;
		File[] identifiers = new File[2];
		StringBuilder sb = new StringBuilder();

		log.info("fileName " + mzMLURL);
		log.info("fileName2 " + mzIdentMLURL);

		log.info("projectName " + projectName);

		MiapeMSDocument msDocument = null;
		MiapeMSIDocument msiDocument = null;
		File inputMzMLFile = new File(mzMLURL);
		// Validate file
		try {
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputMzMLFile, SchemaValidator.mzMLIdx);
		} catch (Exception ex) {
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputMzMLFile, SchemaValidator.mzML);
		}
		try {
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputMzMLFile.getAbsolutePath()));
			inputMzMLFile = FileManager.saveLocalFile(inputMzMLFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputMzMLFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputMzMLFile);

			if (fastParsing) {
				log.info("parsing mzML XML to document MS in the faster method (SAX+DOM method)");
				MzMLLightParser mzMLParser = new MzMLLightParser(inputMzMLFile.getAbsolutePath());
				MzML mzmlLight = mzMLParser.ParseDocument(schemaValidation);
				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(mzmlLight, null,
						getControlVocabularyManager(), null, null, inputMzMLFile.getName(), projectName);
			} else {
				log.info("parsing mzML XML to document MS in the slower method (jmzML API)");
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(
						new MiapeMzMLFile(inputMzMLFile), null, getControlVocabularyManager(), null, null, projectName);
			}

			// add mzML to attached file
			((MiapeMSDocumentImpl) msDocument).setAttachedFileLocation(inputMzMLFile.getAbsolutePath());
			// add mzML to resulting data
			ResultingData resultingData = getResultingDataFromMZML(inputMzMLFile.getAbsolutePath());
			((MiapeMSDocumentImpl) msDocument).addResultingData(resultingData);
			log.info("Getting MIAPE MS Metadata");
			MiapeMSDocument miapeMSMetadata = MiapeMSXmlFactory.getFactory().toDocument(
					new MIAPEMSXmlFile(miapeMSMetadataURL), getControlVocabularyManager(), null, null, null);
			log.info("Merging MIAPE MS from mzML and MIAPE MS METADATA FILE");
			final MiapeMSDocument miapeMSMerged = MiapeMSMerger
					.getInstance(getControlVocabularyManager(), null, null, null).merge(msDocument, miapeMSMetadata);
			log.info("MIAPE MS created");
			log.info("Storing document MS");
			log.info("Converting document to xml");
			final MiapeXmlFile<MiapeMSDocument> msDocumentXML = miapeMSMerged.toXml();

			id_ms = LocalFilesIndex.getInstance().getFreeIndex();
			identifiers[0] = saveMSLocally(id_ms, msDocumentXML, projectName);
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputMzMLFile);

			// ////////////////////////////////////
			log.info("createFile");
			File inputMzIdentMLFile = new File(mzIdentMLURL);
			// Validate file
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputMzIdentMLFile, SchemaValidator.mzIdentML_1_0);

			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputMzIdentMLFile.getAbsolutePath()));
			inputMzIdentMLFile = FileManager.saveLocalFile(inputMzIdentMLFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputMzIdentMLFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputMzIdentMLFile);

			log.info("create MIAPE MSI from mzIdentML");
			MiapeMzIdentMLFile xmlFile = new MiapeMzIdentMLFile(inputMzIdentMLFile);
			msiDocument = org.proteored.miapeapi.xml.mzidentml.MSIMiapeFactory.getFactory().toDocument(xmlFile, null,
					getControlVocabularyManager(), null, null, projectName, processInParallel);
			((org.proteored.miapeapi.xml.mzidentml.MiapeMSIDocumentImpl) msiDocument)
					.setAttachedFileURL(inputMzIdentMLFile.getAbsolutePath());
			if (id_ms > 0) {
				((org.proteored.miapeapi.xml.mzidentml.MiapeMSIDocumentImpl) msiDocument)
						.setReferencedMSDocument(Integer.valueOf(id_ms));
			}
			log.info("MIAPE MSI created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");
			id_msi = LocalFilesIndex.getInstance().getFreeIndex();
			msiDocument.setId(id_msi);
			final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
			identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName,
					FilenameUtils.getBaseName(mzIdentMLURL));
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputMzIdentMLFile);

			return identifiers;

		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());

	}

	private ResultingData getResultingDataFromMZML(String mzMLURL) {
		String resultingDataName = "mzML file";
		String mzMLFileType = getControlVocabularyManager().getControlVocabularyName(MSFileType.MZML_ACC,
				MSFileType.getInstance(getControlVocabularyManager()));
		return MiapeMSDocumentFactory.createResultingDataBuilder(resultingDataName).dataFileURI(mzMLURL)
				.dataFileType(mzMLFileType).build();
	}

	/**
	 * Stores a MIAPE MS document from a mzML file and a MIAPE MSI document from
	 * an array of bytes
	 *
	 * @param mzMLFileURL
	 * @param miapeMSIXML
	 *            an XML file that represents a MIAPE MSI document. The XML has
	 *            to be compliant with the schema located at
	 *            {@link "http://proteo.cnb.csic.es/miape-api/schemas"}
	 * @param userName
	 * @param password
	 * @param projectName
	 *            name of the project in which the MIAPE MSI document will be
	 *            created. If the project already exists in the database (and
	 *            the user has access to it) the document will be created there.
	 *            If the project doesn't exist, it will be created as a new
	 *            project.
	 * @param fastParsing
	 *            indicate if the mzML is parsed using a faster method. Some
	 *            minimal information could be missed, but huge mzML files will
	 *            be parsed in seconds, instead of minutes.
	 * @return an array with two elements, the ID of the MIAPE MS and the ID of
	 *         the MIAPE MSI that have been created. An error string if
	 *         something is wrong.
	 */
	public File[] storeMiapeMSMSIFromMzML(String mzMLFileURL, byte[] miapeMSIXML, String projectName,
			boolean fastParsing) {
		int id_msi = -1;
		int id_ms = -1;
		File[] identifiers = new File[2];
		StringBuilder sb = new StringBuilder();
		MiapeMSDocument msDocument = null;
		File inputMzMLFile = new File(mzMLFileURL);
		// Validate file
		try {
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputMzMLFile, SchemaValidator.mzMLIdx);
		} catch (Exception ex) {
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputMzMLFile, SchemaValidator.mzML);
		}
		try {
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputMzMLFile.getAbsolutePath()));
			inputMzMLFile = FileManager.saveLocalFile(inputMzMLFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputMzMLFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputMzMLFile);

			if (fastParsing) {
				log.info("parsing mzML XML to document MS in the faster method (SAX+DOM method)");
				MzMLLightParser mzMLParser = new MzMLLightParser(inputMzMLFile.getAbsolutePath());
				MzML mzmlLight = mzMLParser.ParseDocument(schemaValidation);

				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(mzmlLight, null,
						getControlVocabularyManager(), null, null, inputMzMLFile.getName(), projectName);
			} else {
				log.info("parsing mzML XML to document MS in the slower method (jmzML API)");
				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(
						new MiapeMzMLFile(inputMzMLFile), null, getControlVocabularyManager(), null, null, projectName);

			}
			// set mzML file to the attached file
			((MiapeMSDocumentImpl) msDocument).setAttachedFileLocation(inputMzMLFile.getAbsolutePath());
			// set mzML to the resulting data
			ResultingData resultingData = getResultingDataFromMZML(inputMzMLFile.getAbsolutePath());
			((MiapeMSDocumentImpl) msDocument).addResultingData(resultingData);

			log.info("MIAPE MS document created in memory");
			log.info("Storing that MIAPE MS");
			swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
					"Storing MIAPE in the ProteoRed MIAPE repository\n");
			swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null, "Waiting for server response...\n");
			MiapeXmlFile<MiapeMSDocument> msDocumentXML = msDocument.toXml();

			id_ms = LocalFilesIndex.getInstance().getFreeIndex();
			identifiers[0] = saveMSLocally(id_ms, msDocumentXML, projectName);
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputMzMLFile);

			if (miapeMSIXML != null) {

				log.info("Storing MIAPE MSI document from received MIAPE MSI XML file");
				final MIAPEMSIXmlFile xmlFile = new MIAPEMSIXmlFile(miapeMSIXML);
				final MiapeMSIDocument miapeMSIDocument = MiapeMSIXmlFactory.getFactory().toDocument(xmlFile,
						getControlVocabularyManager(), null, null, null);
				if (id_ms > 0) {
					log.info("Associating MIAPE MSI in memory with MIAPE MS document (" + id_ms + ")");

					miapeMSIDocument.setReferencedMSDocument(id_ms);

					log.info("MIAPE created");
					log.info("Storing document MS");
					log.info("Converting document to xml");
					id_msi = LocalFilesIndex.getInstance().getFreeIndex();
					miapeMSIDocument.setId(id_msi);
					final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = miapeMSIDocument.toXml();
					identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName, miapeMSIDocument.getName());

				} else {

					MIAPEMSIXmlFile msiDocumentXML = xmlFile;
					id_msi = LocalFilesIndex.getInstance().getFreeIndex();
					identifiers[1] = saveMSILocally(id_ms, msiDocumentXML, projectName, miapeMSIDocument.getName());

				}
				log.info("MIAPE MSI stored ID:" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored ID:" + id_msi + "\n");
			}

			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());

	}

	/**
	 * Stores a MIAPE MS document from a mzML file and a MIAPE MSI document from
	 * an array of bytes
	 *
	 * @param mzMLFileURL
	 * @param miapeMSIXML
	 *            an XML file that represents a MIAPE MSI document. The XML has
	 *            to be compliant with the schema located at
	 *            {@link "http://proteo.cnb.csic.es/miape-api/schemas"}
	 * @param userName
	 * @param password
	 * @param projectName
	 *            name of the project in which the MIAPE MSI document will be
	 *            created. If the project already exists in the database (and
	 *            the user has access to it) the document will be created there.
	 *            If the project doesn't exist, it will be created as a new
	 *            project.
	 * @param fastParsing
	 *            indicate if the mzML is parsed using a faster method. Some
	 *            minimal information could be missed, but huge mzML files will
	 *            be parsed in seconds, instead of minutes.
	 * @return an array with two elements, the ID of the MIAPE MS and the ID of
	 *         the MIAPE MSI that have been created. An error string if
	 *         something is wrong.
	 */
	public File[] storeMiapeMSMSIFromMzMLMetadata(String mzMLFileURL, String miapeMSMetadataURL, byte[] miapeMSIXML,
			String projectName, boolean fastParsing) {
		int id_msi = -1;
		int id_ms = -1;
		File[] identifiers = new File[2];
		StringBuilder sb = new StringBuilder();
		MiapeMSDocument msDocument = null;
		File inputMzMLFile = new File(mzMLFileURL);
		// Validate file
		try {
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputMzMLFile, SchemaValidator.mzMLIdx);
		} catch (Exception ex) {
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputMzMLFile, SchemaValidator.mzML);
		}
		try {
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputMzMLFile.getAbsolutePath()));
			inputMzMLFile = FileManager.saveLocalFile(inputMzMLFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputMzMLFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputMzMLFile);

			if (fastParsing) {
				log.info("parsing mzML XML to document MS in the faster method (SAX+DOM method)");
				MzMLLightParser mzMLParser = new MzMLLightParser(inputMzMLFile.getAbsolutePath());
				MzML mzmlLight = mzMLParser.ParseDocument(schemaValidation);

				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(mzmlLight, null,
						getControlVocabularyManager(), null, null, inputMzMLFile.getName(), projectName);
			} else {
				log.info("parsing mzML XML to document MS in the slower method (jmzML API)");
				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(
						new MiapeMzMLFile(inputMzMLFile), null, getControlVocabularyManager(), null, null, projectName);

			}
			// set mzML file to the attached file
			((MiapeMSDocumentImpl) msDocument).setAttachedFileLocation(inputMzMLFile.getAbsolutePath());
			// set mzML to the resulting data
			ResultingData resultingData = getResultingDataFromMZML(inputMzMLFile.getAbsolutePath());
			((MiapeMSDocumentImpl) msDocument).addResultingData(resultingData);

			log.info("Getting MIAPE MS Metadata");
			MiapeMSDocument miapeMSMetadata = MiapeMSXmlFactory.getFactory().toDocument(
					new MIAPEMSXmlFile(miapeMSMetadataURL), getControlVocabularyManager(), null, null, null);
			log.info("Merging MIAPE MS from mzML and MIAPE MS METADATA FILE");
			final MiapeMSDocument miapeMSMerged = MiapeMSMerger
					.getInstance(getControlVocabularyManager(), null, null, null).merge(msDocument, miapeMSMetadata);

			log.info("MIAPE MS document created in memory");
			log.info("Storing that MIAPE MS");
			swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
					"Storing MIAPE in the ProteoRed MIAPE repository\n");
			swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null, "Waiting for server response...\n");
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

			MiapeXmlFile<MiapeMSDocument> msDocumentXML = miapeMSMerged.toXml();
			id_ms = LocalFilesIndex.getInstance().getFreeIndex();
			identifiers[0] = saveMSLocally(id_ms, msDocumentXML, projectName);
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputMzMLFile);

			if (miapeMSIXML != null) {
				log.info("Storing MIAPE MSI document from received MIAPE MSI XML file");
				MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = new MIAPEMSIXmlFile(miapeMSIXML);
				final MiapeMSIDocument miapeMSIDocument = MiapeMSIXmlFactory.getFactory().toDocument(msiDocumentXML,
						getControlVocabularyManager(), null, null, null);

				if (id_ms > 0) {
					log.info("Associating MIAPE MSI in memory with MIAPE MS document (" + id_ms + ")");

					miapeMSIDocument.setReferencedMSDocument(id_ms);

					log.info("MIAPE created");
					log.info("Storing document MS");
					log.info("Converting document to xml");
					id_msi = LocalFilesIndex.getInstance().getFreeIndex();
					miapeMSIDocument.setId(id_msi);
					identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName, miapeMSIDocument.getName());

				} else {
					id_msi = LocalFilesIndex.getInstance().getFreeIndex();

					// save again
					identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName, miapeMSIDocument.getName());

				}
				log.info("MIAPE MSI stored ID:" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Dataset MSI stored ID:" + id_msi + "\n");
			}

			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());

	}

	/**
	 * Extracts MIAPE MSI information from a mzIdentML file and stores it in a
	 * MIAPE MSI documents in the repository.
	 *
	 * @param mzIdentMLURL
	 * @param idMiapeMS
	 *            if it is > 0, identifier of an already existing MIAPE MS
	 *            document that you want to associate with the new MIAPE MSI.
	 *
	 * @param userName
	 * @param password
	 * @param projectName
	 *            name of the project in which the MIAPE MSI document will be
	 *            created. If the project already exists in the database (and
	 *            the userName has access to it) the document will be created
	 *            there. If the project doesn't exist, it will be created as a
	 *            new project.
	 * @return the ID of the MIAPE MSI (if applicable). If something is wrong,
	 *         an explanatory string from the error.
	 * @throws IllegalMiapeArgumentException
	 * @throws IOException
	 */
	public File[] storeMiapeMSIFromMzIdentML(String mzIdentMLURL, int idMiapeMS, String projectName) {
		File[] identifiers = new File[2];
		int id_msi = -1;
		MiapeMSIDocument msiDocument = null;
		StringBuilder sb = new StringBuilder();
		File inputMzIdentMLFile = new File(mzIdentMLURL);
		// Validate file
		try {
			if (schemaValidation) {
				try {
					SchemaValidator.validateXMLFile(inputMzIdentMLFile, SchemaValidator.mzIdentML_1_0);
				} catch (WrongXMLFormatException e) {
					try {
						SchemaValidator.validateXMLFile(inputMzIdentMLFile, SchemaValidator.mzIdentML_1_1);
					} catch (WrongXMLFormatException e2) {
						SchemaValidator.validateXMLFile(inputMzIdentMLFile, SchemaValidator.mzIdentML_1_2);
					}
				}

			}
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		log.info("create Dataset MSI from mzIdentML");
		try {
			try {
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
						FilenameUtils.getName(inputMzIdentMLFile.getAbsolutePath()));
				inputMzIdentMLFile = FileManager.saveLocalFile(inputMzIdentMLFile, projectName);
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
						"File copied to " + inputMzIdentMLFile.getAbsolutePath());
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputMzIdentMLFile);

				org.proteored.miapeapi.xml.mzidentml_1_1.MiapeMzIdentMLFile xmlFile = new org.proteored.miapeapi.xml.mzidentml_1_1.MiapeMzIdentMLFile(
						inputMzIdentMLFile);
				msiDocument = org.proteored.miapeapi.xml.mzidentml_1_1.MSIMiapeFactory.getFactory().toDocument(xmlFile,
						null, getControlVocabularyManager(), null, null, projectName, processInParallel);
				((org.proteored.miapeapi.xml.mzidentml_1_1.MiapeMSIDocumentImpl) msiDocument)
						.setAttachedFileURL(inputMzIdentMLFile.getAbsolutePath());
				if (idMiapeMS > 0) {
					((org.proteored.miapeapi.xml.mzidentml_1_1.MiapeMSIDocumentImpl) msiDocument)
							.setReferencedMSDocument(Integer.valueOf(idMiapeMS));
				}
			} catch (WrongXMLFormatException ex) {
				log.info("Error trying to read as mzIdentML 1.2 or 1.1. Trying now as mzIdentML 1.0...");

				MiapeMzIdentMLFile xmlFile = new MiapeMzIdentMLFile(inputMzIdentMLFile);
				msiDocument = org.proteored.miapeapi.xml.mzidentml.MSIMiapeFactory.getFactory().toDocument(xmlFile,
						null, getControlVocabularyManager(), null, null, projectName, processInParallel);
				((org.proteored.miapeapi.xml.mzidentml.MiapeMSIDocumentImpl) msiDocument)
						.setAttachedFileURL(inputMzIdentMLFile.getAbsolutePath());
				if (idMiapeMS > 0) {
					((org.proteored.miapeapi.xml.mzidentml.MiapeMSIDocumentImpl) msiDocument)
							.setReferencedMSDocument(Integer.valueOf(idMiapeMS));
				}
			}

			log.info("Dataset created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");

			if (idMiapeMS > 0) {
				identifiers[0] = new File(FileManager.getMiapeMSXMLFileLocalPath(idMiapeMS, projectName));
			}
			id_msi = LocalFilesIndex.getInstance().getFreeIndex();
			msiDocument.setId(id_msi);
			final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
			identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName,
					FilenameUtils.getBaseName(mzIdentMLURL));
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputMzIdentMLFile);
			return identifiers;

		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());

	}

	/**
	 * Stores a MIAPE MSI document from a mzIdentML file and a MIAPE MS document
	 * from an array of bytes
	 *
	 * @param mzIdentFileURL
	 * @param miapeMSXML
	 *            an XML file that represents a MIAPE MS document. The XML has
	 *            to be compliant with the schema located at
	 *            {@link "http://proteo.cnb.csic.es/miape-api/schemas"}
	 * @param userName
	 * @param password
	 * @param projectName
	 *            name of the project in which the MIAPE MSI document will be
	 *            created. If the project already exists in the database (and
	 *            the user has access to it) the document will be created there.
	 *            If the project doesn't exist, it will be created as a new
	 *            project.
	 *
	 * @return an array with two elements, the ID of the MIAPE MS and the ID of
	 *         the MIAPE MSI that have been created. An error string if
	 *         something is wrong.
	 */
	public File[] storeMiapeMSMSIFromMzIdentML(String mzIdentFileURL, byte[] miapeMSXMLBytes, String projectName) {
		int id_ms = -1;
		int id_msi = -1;
		File[] identifiers = new File[2];
		MiapeMSIDocument msiDocument = null;
		File inputMzIdentMLFile = null;
		StringBuilder sb = new StringBuilder();
		try {
			if (miapeMSXMLBytes != null) {
				MiapeXmlFile<MiapeMSDocument> msDocumentXML = new MIAPEMSXmlFile(miapeMSXMLBytes);
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				identifiers[0] = saveMSLocally(id_ms, msDocumentXML, projectName);
			}

			log.info("create MIAPE MSI from mzIdentML");
			// copy mzIdentML file to a temp file
			inputMzIdentMLFile = new File(mzIdentFileURL);

			// Validate file
			if (schemaValidation) {
				try {
					SchemaValidator.validateXMLFile(inputMzIdentMLFile, SchemaValidator.mzIdentML_1_0);
				} catch (WrongXMLFormatException ex) {
					try {
						SchemaValidator.validateXMLFile(inputMzIdentMLFile, SchemaValidator.mzIdentML_1_1);
					} catch (WrongXMLFormatException ex2) {
						SchemaValidator.validateXMLFile(inputMzIdentMLFile, SchemaValidator.mzIdentML_1_2);
					}
				}
			}
			try {
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
						FilenameUtils.getName(inputMzIdentMLFile.getAbsolutePath()));
				inputMzIdentMLFile = FileManager.saveLocalFile(inputMzIdentMLFile, projectName);
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
						"File copied to " + inputMzIdentMLFile.getAbsolutePath());
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputMzIdentMLFile);

				MiapeMzIdentMLFile xmlFile = new MiapeMzIdentMLFile(inputMzIdentMLFile);

				msiDocument = org.proteored.miapeapi.xml.mzidentml.MSIMiapeFactory.getFactory().toDocument(xmlFile,
						null, getControlVocabularyManager(), null, null, projectName, processInParallel);
				((org.proteored.miapeapi.xml.mzidentml.MiapeMSIDocumentImpl) msiDocument)
						.setAttachedFileURL(inputMzIdentMLFile.getAbsolutePath());
				if (id_ms > 0) {
					log.info("Associating the MIAPE MSI document to the previously created MIAPE MS document");
					((org.proteored.miapeapi.xml.mzidentml.MiapeMSIDocumentImpl) msiDocument)
							.setReferencedMSDocument(Integer.valueOf(id_ms));
				}
			} catch (WrongXMLFormatException ex) {
				org.proteored.miapeapi.xml.mzidentml_1_1.MiapeMzIdentMLFile xmlFile = new org.proteored.miapeapi.xml.mzidentml_1_1.MiapeMzIdentMLFile(
						inputMzIdentMLFile);

				msiDocument = org.proteored.miapeapi.xml.mzidentml_1_1.MSIMiapeFactory.getFactory().toDocument(xmlFile,
						null, getControlVocabularyManager(), null, null, projectName, processInParallel);
				((org.proteored.miapeapi.xml.mzidentml_1_1.MiapeMSIDocumentImpl) msiDocument)
						.setAttachedFileURL(inputMzIdentMLFile.getAbsolutePath());
				if (id_ms > 0) {
					log.info("Associating the MIAPE MSI document to the previously created MIAPE MS document");
					((org.proteored.miapeapi.xml.mzidentml_1_1.MiapeMSIDocumentImpl) msiDocument)
							.setReferencedMSDocument(Integer.valueOf(id_ms));
				}
			}

			log.info("MIAPE created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");
			id_msi = LocalFilesIndex.getInstance().getFreeIndex();
			msiDocument.setId(id_msi);
			final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
			identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName,
					FilenameUtils.getBaseName(mzIdentFileURL));
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputMzIdentMLFile);

			return identifiers;
		} catch (

		Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());

	}

	/**
	 * Extracts MIAPE MSI information from a XTandem XML results file and stores
	 * it in a MIAPE MSI documents in the repository.
	 *
	 * @param xTandemXMLURI
	 * @param idMiapeMS
	 *            if it is > 0, identifier of an already existing MIAPE MS
	 *            document that you want to associate with the new MIAPE MSI.
	 * @param userName
	 * @param password
	 * @param projectName
	 *            name of the project in which the MIAPE MSI document will be
	 *            created. If the project already exists in the database (and
	 *            the userName has access to it) the document will be created
	 *            there. If the project doesn't exist, it will be created as a
	 *            new project.
	 * @return an array with two elements: the first (index=0) is the ID of the
	 *         MIAPE MS (if applicable) the second (index=1) is the ID of the
	 *         MIAPE MSI (if applicable). If something is wrong, the first
	 *         element is an explanatory string from the error.
	 */
	public File[] storeMiapeMSIFromXTandemXML(String xTandemXMLURI, int idMiapeMS, String projectName) {
		File[] identifiers = new File[2];
		int id_msi = -1;
		MiapeMSIDocument msiDocument = null;
		File inputXTandemXMLFile = null;
		StringBuilder sb = new StringBuilder();
		log.info("createFile");

		try {
			inputXTandemXMLFile = new File(xTandemXMLURI);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputXTandemXMLFile.getAbsolutePath()));
			inputXTandemXMLFile = FileManager.saveLocalFile(inputXTandemXMLFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputXTandemXMLFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputXTandemXMLFile);

			// Dont Validate file because we don't have the schema
			// if (schemaValidation)
			// SchemaValidator.validateXMLFile(mzIdentMLFile,
			// SchemaValidator.mzIdentML);

			log.info("create MIAPE MSI from xTandem XML");
			MiapeXTandemFile xmlFile = new MiapeXTandemFile(inputXTandemXMLFile);
			msiDocument = org.proteored.miapeapi.xml.xtandem.msi.MSIMiapeFactory.getFactory().toDocument(xmlFile, null,
					getControlVocabularyManager(), null, null, projectName);
			((org.proteored.miapeapi.xml.xtandem.msi.MiapeMsiDocumentImpl) msiDocument)
					.setAttachedFileURL(inputXTandemXMLFile.getAbsolutePath());

			if (idMiapeMS > 0) {
				((org.proteored.miapeapi.xml.xtandem.msi.MiapeMsiDocumentImpl) msiDocument)
						.setReferencedMSDocument(Integer.valueOf(idMiapeMS));
			}
			log.info("MIAPE created");
			log.info("MIAPE created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");

			if (idMiapeMS > 0) {
				identifiers[0] = new File(FileManager.getMiapeMSXMLFileLocalPath(idMiapeMS, projectName));
			}
			id_msi = LocalFilesIndex.getInstance().getFreeIndex();
			msiDocument.setId(id_msi);
			final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
			identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName,
					FilenameUtils.getBaseName(xTandemXMLURI));
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputXTandemXMLFile);
			return identifiers;

		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());

	}

	/**
	 * Extracts MIAPE MSI information from a TSV results file and stores it in a
	 * MIAPE MSI documents in the repository.
	 *
	 * @param tsvURI
	 * @param idMiapeMS
	 *            if it is > 0, identifier of an already existing MIAPE MS
	 *            document that you want to associate with the new MIAPE MSI.
	 * @param userName
	 * @param password
	 * @param projectName
	 *            name of the project in which the MIAPE MSI document will be
	 *            created. If the project already exists in the database (and
	 *            the userName has access to it) the document will be created
	 *            there. If the project doesn't exist, it will be created as a
	 *            new project.
	 * @return an array with two elements: the first (index=0) is the ID of the
	 *         MIAPE MS (if applicable) the second (index=1) is the ID of the
	 *         MIAPE MSI (if applicable). If something is wrong, the first
	 *         element is an explanatory string from the error.
	 */
	public File[] storeMiapeMSIFromTSV(String tsvURI, TableTextFileSeparator separator, int idMiapeMS,
			String projectName) {
		File[] identifiers = new File[2];
		int id_msi = -1;
		MiapeMSIDocument msiDocument = null;
		File inputTSVFile = null;
		StringBuilder sb = new StringBuilder();
		log.info("createFile");

		try {
			inputTSVFile = new File(tsvURI);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputTSVFile.getAbsolutePath()));
			inputTSVFile = FileManager.saveLocalFile(inputTSVFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputTSVFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputTSVFile);

			// Dont Validate file because we don't have the schema
			// if (schemaValidation)
			// SchemaValidator.validateXMLFile(mzIdentMLFile,
			// SchemaValidator.mzIdentML);

			log.info("create MIAPE MSI from TSV file");
			MiapeTSVFile xmlFile = new MiapeTSVFile(inputTSVFile, separator);
			msiDocument = org.proteored.miapeapi.text.tsv.msi.MSIMiapeFactory.getFactory().toDocument(xmlFile, null,
					getControlVocabularyManager(), null, null, projectName);
			((org.proteored.miapeapi.text.tsv.msi.MiapeMsiDocumentImpl) msiDocument)
					.setAttachedFileURL(inputTSVFile.getAbsolutePath());

			if (idMiapeMS > 0) {
				((org.proteored.miapeapi.text.tsv.msi.MiapeMsiDocumentImpl) msiDocument)
						.setReferencedMSDocument(Integer.valueOf(idMiapeMS));
			}
			log.info("MIAPE created");
			log.info("MIAPE created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");

			if (idMiapeMS > 0) {
				identifiers[0] = new File(FileManager.getMiapeMSXMLFileLocalPath(idMiapeMS, projectName));
			}
			id_msi = LocalFilesIndex.getInstance().getFreeIndex();
			msiDocument.setId(id_msi);
			final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
			identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName, FilenameUtils.getBaseName(tsvURI));
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputTSVFile);
			return identifiers;

		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());

	}

	/**
	 * Stores a MIAPE MSI document from a XTandem XML results file and a MIAPE
	 * MS document from an array of bytes
	 *
	 * @param xTandemXMLFileURL
	 * @param miapeMSXML
	 *            an XML file that represents a MIAPE MS document. The XML has
	 *            to be compliant with the schema located at
	 *            {@link "http://proteo.cnb.csic.es/miape-api/schemas"}
	 * @param userName
	 * @param password
	 * @param projectName
	 *            name of the project in which the MIAPE MSI document will be
	 *            created. If the project already exists in the database (and
	 *            the user has access to it) the document will be created there.
	 *            If the project doesn't exist, it will be created as a new
	 *            project.
	 * @return an array with two elements, the ID of the MIAPE MS and the ID of
	 *         the MIAPE MSI that have been created. An error string if
	 *         something is wrong.
	 */
	public File[] storeMiapeMSMSIFromXTandemXML(String xTandemXMLFileURL, byte[] miapeMSXMLBytes, String projectName) {
		int id_ms = -1;
		int id_msi = -1;
		StringBuilder sb = new StringBuilder();
		File[] identifiers = new File[2];
		MiapeMSIDocument msiDocument = null;
		File inputXTandemXMLFile = null;

		try {
			if (miapeMSXMLBytes != null) {
				MiapeXmlFile<MiapeMSDocument> msDocumentXML = new MIAPEMSXmlFile(miapeMSXMLBytes);
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				identifiers[0] = saveMSLocally(id_ms, msDocumentXML, projectName);

			}

			log.info("create MIAPE MSI from xTandem XML");
			// copy mzIdentML file to a temp file
			inputXTandemXMLFile = new File(xTandemXMLFileURL);

			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(inputXTandemXMLFile.getAbsolutePath()));
			inputXTandemXMLFile = FileManager.saveLocalFile(inputXTandemXMLFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + inputXTandemXMLFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputXTandemXMLFile);

			MiapeXTandemFile xmlFile = new MiapeXTandemFile(inputXTandemXMLFile);

			msiDocument = org.proteored.miapeapi.xml.xtandem.msi.MSIMiapeFactory.getFactory().toDocument(xmlFile, null,
					getControlVocabularyManager(), null, null, projectName);
			((org.proteored.miapeapi.xml.xtandem.msi.MiapeMsiDocumentImpl) msiDocument)
					.setAttachedFileURL(inputXTandemXMLFile.getAbsolutePath());

			if (id_ms > 0) {
				log.info("Associating the MIAPE MSI document to the previously created MIAPE MS document");
				((org.proteored.miapeapi.xml.xtandem.msi.MiapeMsiDocumentImpl) msiDocument)
						.setReferencedMSDocument(Integer.valueOf(id_ms));
			}
			log.info("MIAPE created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");

			id_msi = LocalFilesIndex.getInstance().getFreeIndex();
			msiDocument.setId(id_msi);
			final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
			identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName,
					FilenameUtils.getBaseName(xTandemXMLFileURL));
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputXTandemXMLFile);

			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());

	}

	public static Properties getProperties(String propFile) throws Exception {
		InputStream is;

		is = ClassLoader.getSystemResourceAsStream(propFile);
		if (is == null)
			throw new Exception(propFile + " file not found");

		Properties prop = new Properties();
		try {
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
		return prop;
	}

	public File storeMiapeMS(MiapeMSDocument msDocument, String projectName) throws IOException {

		log.info("projectName " + projectName);

		log.info("MIAPE created");
		log.info("Storing document MS");
		log.info("Converting document to xml");
		final MiapeXmlFile<MiapeMSDocument> msDocumentXML = msDocument.toXml();

		int id = LocalFilesIndex.getInstance().getFreeIndex();
		return saveMSLocally(id, msDocumentXML, projectName);

	}

	public File[] storeMiapeMSIFromDTASelect(String dtaSelectFileURI, int idMiapeMS, String projectName) {
		File[] identifiers = new File[2];
		int id_msi = -1;
		MiapeMSIDocument msiDocument = null;
		File dtaSelectFile = null;
		StringBuilder sb = new StringBuilder();
		log.info("storeMiapeMSIFromDTASelect from " + dtaSelectFileURI);

		try {
			dtaSelectFile = new File(dtaSelectFileURI);

			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(dtaSelectFile.getAbsolutePath()));
			dtaSelectFile = FileManager.saveLocalFile(dtaSelectFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + dtaSelectFile.getAbsolutePath());
			log.info("file saved to " + dtaSelectFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, dtaSelectFile);
			log.info("indexed ok");
			// Dont Validate file because we don't have the schema
			// if (schemaValidation)
			// SchemaValidator.validateXMLFile(mzIdentMLFile,
			// SchemaValidator.mzIdentML);

			log.info("create MIAPE MSI from dtaSelect file");
			MiapeDTASelectFile xmlFile = new MiapeDTASelectFile(dtaSelectFile);
			msiDocument = org.proteored.miapeapi.xml.dtaselect.MSIMiapeFactory.getFactory().toDocument(xmlFile, null,
					getControlVocabularyManager(), null, null, projectName);
			((org.proteored.miapeapi.xml.dtaselect.MiapeMsiDocumentImpl) msiDocument)
					.setAttachedFileURL(dtaSelectFile.getAbsolutePath());

			if (idMiapeMS > 0) {
				((org.proteored.miapeapi.xml.dtaselect.MiapeMsiDocumentImpl) msiDocument)
						.setReferencedMSDocument(Integer.valueOf(idMiapeMS));
			}
			log.info("MIAPE created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");

			if (idMiapeMS > 0) {
				identifiers[0] = new File(FileManager.getMiapeMSXMLFileLocalPath(idMiapeMS, projectName));
			}
			id_msi = LocalFilesIndex.getInstance().getFreeIndex();
			msiDocument.setId(id_msi);
			final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();

			identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName,
					FilenameUtils.getBaseName(dtaSelectFileURI));
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, dtaSelectFile);
			return identifiers;

		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());

	}

	public File[] storeMiapeMSIFromPepXML(String pepXMLFileURI, int idMiapeMS, String projectName) {
		File[] identifiers = new File[2];
		int id_msi = -1;
		MiapeMSIDocument msiDocument = null;
		File pepXMLFile = null;
		StringBuilder sb = new StringBuilder();
		log.info("storeMiapeMSIFromPEPXML from " + pepXMLFileURI);

		try {
			pepXMLFile = new File(pepXMLFileURI);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(pepXMLFile.getAbsolutePath()));
			pepXMLFile = FileManager.saveLocalFile(pepXMLFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + pepXMLFile.getAbsolutePath());

			log.info("file saved to " + pepXMLFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, pepXMLFile);
			log.info("indexed ok");
			// Dont Validate file because we don't have the schema
			// if (schemaValidation)
			// SchemaValidator.validateXMLFile(mzIdentMLFile,
			// SchemaValidator.mzIdentML);

			log.info("create MIAPE MSI from pepXML file");
			MiapePepXMLFile xmlFile = new MiapePepXMLFile(pepXMLFile);
			msiDocument = org.proteored.miapeapi.xml.pepxml.MSIMiapeFactory.getFactory().toDocument(xmlFile, null,
					getControlVocabularyManager(), null, null, projectName);
			((org.proteored.miapeapi.xml.pepxml.MiapeMsiDocumentImpl) msiDocument)
					.setAttachedFileURL(pepXMLFile.getAbsolutePath());

			if (idMiapeMS > 0) {
				((org.proteored.miapeapi.xml.pepxml.MiapeMsiDocumentImpl) msiDocument)
						.setReferencedMSDocument(Integer.valueOf(idMiapeMS));
			}
			log.info("MIAPE created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");

			if (idMiapeMS > 0) {
				identifiers[0] = new File(FileManager.getMiapeMSXMLFileLocalPath(idMiapeMS, projectName));
			}
			id_msi = LocalFilesIndex.getInstance().getFreeIndex();
			msiDocument.setId(id_msi);
			final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();

			identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName,
					FilenameUtils.getBaseName(pepXMLFileURI));
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, pepXMLFile);
			return identifiers;

		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());

	}

	public File[] storeMiapeMSMSIFromDTASelect(String dtaSelectFileURL, byte[] miapeMSXMLBytes, String projectName) {
		int id_ms = -1;
		int id_msi = -1;
		StringBuilder sb = new StringBuilder();
		File[] identifiers = new File[2];
		MiapeMSIDocument msiDocument = null;
		File dtaSelectFile = null;

		try {
			if (miapeMSXMLBytes != null) {

				MiapeXmlFile<MiapeMSDocument> msDocumentXML = new MIAPEMSXmlFile(miapeMSXMLBytes);
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				identifiers[0] = saveMSLocally(id_ms, msDocumentXML, projectName);

			}

			log.info("create MIAPE MSI from DTASelect file");
			// copy dtaselect file to a temp file
			dtaSelectFile = new File(dtaSelectFileURL);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(dtaSelectFile.getAbsolutePath()));

			dtaSelectFile = FileManager.saveLocalFile(dtaSelectFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + dtaSelectFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, dtaSelectFile);

			MiapeDTASelectFile dtaSelectMIAPEFile = new MiapeDTASelectFile(dtaSelectFile);

			msiDocument = org.proteored.miapeapi.xml.dtaselect.MSIMiapeFactory.getFactory()
					.toDocument(dtaSelectMIAPEFile, null, getControlVocabularyManager(), null, null, projectName);
			((MiapeMsiDocumentImpl) msiDocument).setAttachedFileURL(dtaSelectFile.getAbsolutePath());

			if (id_ms > 0) {
				log.info("Associating the MIAPE MSI document to the previously created MIAPE MS document");
				((MiapeMsiDocumentImpl) msiDocument).setReferencedMSDocument(Integer.valueOf(id_ms));
			}
			log.info("MIAPE created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");

			id_msi = LocalFilesIndex.getInstance().getFreeIndex();
			msiDocument.setId(id_msi);
			MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
			identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName,
					FilenameUtils.getBaseName(dtaSelectFileURL));
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, dtaSelectFile);

			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());

	}

	public File[] storeMiapeMSMSIFromPepXML(String pepXMLFileURL, byte[] miapeMSXMLBytes, String projectName) {
		int id_ms = -1;
		int id_msi = -1;
		StringBuilder sb = new StringBuilder();
		File[] identifiers = new File[2];
		MiapeMSIDocument msiDocument = null;
		File pepXMLFile = null;

		try {
			if (miapeMSXMLBytes != null) {

				MiapeXmlFile<MiapeMSDocument> msDocumentXML = new MIAPEMSXmlFile(miapeMSXMLBytes);
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				identifiers[0] = saveMSLocally(id_ms, msDocumentXML, projectName);

			}

			log.info("create MIAPE MSI from pepXML file");
			// copy dtaselect file to a temp file
			pepXMLFile = new File(pepXMLFileURL);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(pepXMLFile.getAbsolutePath()));

			pepXMLFile = FileManager.saveLocalFile(pepXMLFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + pepXMLFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, pepXMLFile);

			MiapePepXMLFile pepXMLMIAPEFile = new MiapePepXMLFile(pepXMLFile);

			msiDocument = org.proteored.miapeapi.xml.pepxml.MSIMiapeFactory.getFactory().toDocument(pepXMLMIAPEFile,
					null, getControlVocabularyManager(), null, null, projectName);
			((MiapeMsiDocumentImpl) msiDocument).setAttachedFileURL(pepXMLFile.getAbsolutePath());

			if (id_ms > 0) {
				log.info("Associating the MIAPE MSI document to the previously created MIAPE MS document");
				((MiapeMsiDocumentImpl) msiDocument).setReferencedMSDocument(Integer.valueOf(id_ms));
			}
			log.info("MIAPE created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");

			id_msi = LocalFilesIndex.getInstance().getFreeIndex();
			msiDocument.setId(id_msi);
			MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
			identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName,
					FilenameUtils.getBaseName(pepXMLFileURL));
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, pepXMLFile);

			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());

	}

	public File[] storeMiapeMSMSIFromTSV(String tsvFileURL, TableTextFileSeparator separator, byte[] miapeMSXMLBytes,
			String projectName) {
		int id_ms = -1;
		int id_msi = -1;
		StringBuilder sb = new StringBuilder();
		File[] identifiers = new File[2];
		MiapeMSIDocument msiDocument = null;
		File tsvFile = null;

		try {
			if (miapeMSXMLBytes != null) {

				MiapeXmlFile<MiapeMSDocument> msDocumentXML = new MIAPEMSXmlFile(miapeMSXMLBytes);
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				identifiers[0] = saveMSLocally(id_ms, msDocumentXML, projectName);

			}

			log.info("create MIAPE MSI from TSV text file");
			// copy dtaselect file to a temp file
			tsvFile = new File(tsvFileURL);

			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE, null,
					FilenameUtils.getName(tsvFileURL));
			tsvFile = FileManager.saveLocalFile(tsvFile, projectName);
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE, null,
					"File copied to " + tsvFile.getAbsolutePath());
			LocalFilesIndex.getInstance().indexFileByProjectName(projectName, tsvFile);

			MiapeTSVFile tsvMIAPEFile = new MiapeTSVFile(tsvFile, separator);

			msiDocument = org.proteored.miapeapi.text.tsv.msi.MSIMiapeFactory.getFactory().toDocument(tsvMIAPEFile,
					null, getControlVocabularyManager(), null, null, projectName);
			((org.proteored.miapeapi.text.tsv.msi.MiapeMsiDocumentImpl) msiDocument)
					.setAttachedFileURL(tsvFile.getAbsolutePath());

			if (id_ms > 0) {
				log.info("Associating the MIAPE MSI document to the previously created MIAPE MS document");
				((MiapeMsiDocumentImpl) msiDocument).setReferencedMSDocument(Integer.valueOf(id_ms));
			}
			log.info("MIAPE created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");

			id_msi = LocalFilesIndex.getInstance().getFreeIndex();
			msiDocument.setId(id_msi);
			MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
			identifiers[1] = saveMSILocally(id_msi, msiDocumentXML, projectName, FilenameUtils.getBaseName(tsvFileURL));
			LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, tsvFile);

			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		throw new MiapeExtractionException(sb.toString());

	}
}
