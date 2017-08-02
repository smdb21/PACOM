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
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeDatabaseException_Exception;
import org.proteored.miapeapi.xml.SchemaValidator;
import org.proteored.miapeapi.xml.dtaselect.MiapeDTASelectFile;
import org.proteored.miapeapi.xml.dtaselect.msi.MiapeMsiDocumentImpl;
import org.proteored.miapeapi.xml.ms.MIAPEMSXmlFile;
import org.proteored.miapeapi.xml.ms.MiapeMSXmlFactory;
import org.proteored.miapeapi.xml.ms.merge.MiapeMSMerger;
import org.proteored.miapeapi.xml.msi.MIAPEMSIXmlFile;
import org.proteored.miapeapi.xml.msi.MiapeMSIXmlFactory;
import org.proteored.miapeapi.xml.mzidentml.MiapeMzIdentMLFile;
import org.proteored.miapeapi.xml.mzml.MiapeMSDocumentImpl;
import org.proteored.miapeapi.xml.mzml.MiapeMzMLFile;
import org.proteored.miapeapi.xml.mzml.lightParser.utils.MzMLLightParser;
import org.proteored.miapeapi.xml.pride.AbstractDocumentFromPride;
import org.proteored.miapeapi.xml.pride.MSIMiapeFactory;
import org.proteored.miapeapi.xml.pride.MSMiapeFactory;
import org.proteored.miapeapi.xml.pride.MiapeMsPrideXmlFile;
import org.proteored.miapeapi.xml.pride.MiapeMsiPrideXmlFile;
import org.proteored.miapeapi.xml.xtandem.msi.MiapeXTandemFile;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.analysis.util.LocalFilesIndex;

import uk.ac.ebi.jmzml.model.mzml.MzML;

/**
 * Class with the logic of the webservice that transforms a standard XML file to
 * a MIAPE document and MIAPE documents to Standard XML Files
 *
 * @author Salvador
 *
 */
public class MiapeLocalExtractor {
	private static String ftpPath;
	private static boolean schemaValidation;

	public static final String MIAPE_LOCAL_CONVERTER_PROPERTIES_FILE = "miape-extractor.properties";
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private final MiapeAPIWebserviceDelegate miapeAPIWebservice;
	private final SwingWorker<Void, Void> swingWorker;
	private final int idJob;
	private final boolean storeInRepository;
	private final boolean processInParallel;

	static {
		try {
			final Properties properties = getProperties(MIAPE_LOCAL_CONVERTER_PROPERTIES_FILE);
			ftpPath = properties.getProperty("ftp.path");

			schemaValidation = Boolean.valueOf(properties.getProperty("schema.validation"));

		} catch (Exception e) {
			log.info(MIAPE_LOCAL_CONVERTER_PROPERTIES_FILE + " file not found. Trying with defaults");
			// by default, if the properties file fails
			ftpPath = "ftp://proteo.cnb.csic.es/pub/tmp/";
			schemaValidation = false;
		}
		log.info("ftp path =" + ftpPath);
		log.info("schema validation = " + schemaValidation);
	}

	public MiapeLocalExtractor(MiapeAPIWebserviceDelegate miapeAPIWebservice,
			SwingWorker<Void, Void> standard2miapeTaskManager, int idJob, boolean storeInRepository,
			boolean processInParallel) {
		this.miapeAPIWebservice = miapeAPIWebservice;
		swingWorker = standard2miapeTaskManager;
		this.idJob = idJob;
		this.storeInRepository = storeInRepository;
		this.processInParallel = processInParallel;
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
	public String storeMiapeMSFromPRIDE(String prideURL, String userName, String password, String projectName) {

		int id_ms = -1;
		StringBuilder sb = new StringBuilder();
		try {
			log.info("fileName " + prideURL);
			log.info("userName " + userName);
			log.info("password " + password);
			log.info("projectName " + projectName);

			MiapeMSDocument msDocument = null;

			File inputFile = new File(prideURL);

			// Validate file
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputFile, SchemaValidator.prideXML);

			if (!storeInRepository) {
				// save the file to local folders
				inputFile = FileManager.saveLocalFile(inputFile, projectName);
				// index file by project name
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputFile);
			}

			log.info("parsing PRIDE XML to document MS");
			msDocument = MSMiapeFactory.getFactory().toDocument(new MiapeMsPrideXmlFile(inputFile), null,
					getControlVocabularyManager(), userName, password, projectName);
			((AbstractDocumentFromPride) msDocument).setAttachedFileLocation(inputFile.getAbsolutePath());

			log.info("Storing document MS");
			log.info("Converting document to xml");
			final MiapeXmlFile<MiapeMSDocument> msDocumentXML = msDocument.toXml();

			if (!storeInRepository) {
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				saveLocally(id_ms, msDocumentXML, projectName, "MS", FilenameUtils.getBaseName(prideURL));
				// index by miape ID
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputFile);
			} else {
				log.info("Converting document to bytes");
				final byte[] msBytes = msDocumentXML.toBytes();
				log.info("Sending bytes to webservice");

				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);
				id_ms = miapeAPIWebservice.storeMiapeMS(userName, password, msBytes);
				log.info("MIAPE MS stored. ID=" + id_ms);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MS stored. ID=" + id_ms + "\n");
				if (id_ms > 0) {
					return String.valueOf(id_ms);
				}
			}
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		return "error: " + sb.toString();
	}

	/**
	 *
	 * @param miapeXML
	 * @param projectName
	 * @param miapeType
	 * @return the identifier of the MIAPE saved in String format
	 * @throws IOException
	 */
	private void saveLocally(int miapeID, MiapeXmlFile miapeXML, String projectName, String miapeType, String fileName)
			throws IOException {

		if (miapeType.equals("MS")) {
			FileManager.saveLocalMiapeMS(miapeID, (MIAPEMSXmlFile) miapeXML, projectName, fileName);

		} else if (miapeType.equals("MSI")) {
			FileManager.saveLocalMiapeMSI(miapeID, (MIAPEMSIXmlFile) miapeXML, projectName, fileName);

		} else {
			throw new IllegalMiapeArgumentException("Not supported miape type: " + miapeType);
		}

	}

	public String storeMiapeMSFromPRIDEAndMetadata(String prideURL, String miapeMSMetadataURL, String userName,
			String password, String projectName) {

		int id_ms = -1;
		StringBuilder sb = new StringBuilder();
		try {
			log.info("fileName " + prideURL);
			log.info("userName " + userName);
			log.info("password " + password);
			log.info("projectName " + projectName);

			MiapeMSDocument msDocument = null;
			File inputFile = new File(prideURL);

			// Validate file
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputFile, SchemaValidator.prideXML);

			if (!storeInRepository) {
				inputFile = FileManager.saveLocalFile(inputFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputFile);
			}

			log.info("parsing PRIDE XML to document MS");
			msDocument = MSMiapeFactory.getFactory().toDocument(new MiapeMsPrideXmlFile(inputFile), null,
					getControlVocabularyManager(), userName, password, projectName);
			((AbstractDocumentFromPride) msDocument).setAttachedFileLocation(inputFile.getAbsolutePath());

			log.info("Getting MIAPE MS Metadata");
			MiapeMSDocument miapeMSMetadata = MiapeMSXmlFactory.getFactory().toDocument(
					new MIAPEMSXmlFile(miapeMSMetadataURL), getControlVocabularyManager(), null, userName, password);
			log.info("Merging MIAPE MS from mzML and MIAPE MS METADATA FILE");
			final MiapeMSDocument miapeMSMerged = MiapeMSMerger
					.getInstance(getControlVocabularyManager(), null, userName, password)
					.merge(msDocument, miapeMSMetadata);

			log.info("Storing document MS");
			log.info("Converting document to xml");
			final MiapeXmlFile<MiapeMSDocument> msDocumentXML = miapeMSMerged.toXml();
			if (!storeInRepository) {
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				saveLocally(id_ms, msDocumentXML, projectName, "MS", FilenameUtils.getBaseName(prideURL));
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputFile);
				return String.valueOf(id_ms);
			} else {
				log.info("Converting document to bytes");
				final byte[] msBytes = msDocumentXML.toBytes();
				log.info("Sending bytes to webservice");

				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				id_ms = miapeAPIWebservice.storeMiapeMS(userName, password, msBytes);
				log.info("MIAPE MS stored. ID=" + id_ms);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MS stored. ID=" + id_ms + "\n");
				if (id_ms > 0) {
					return String.valueOf(id_ms);
				}
			}
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		return "error: " + sb.toString();
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
	public String storeMiapeMSIFromPRIDE(String prideURL, String userName, String password, String projectName) {
		int id_msi = -1;
		StringBuilder sb = new StringBuilder();
		log.info("fileName " + prideURL);
		log.info("userName " + userName);
		log.info("password " + password);
		log.info("projectName " + projectName);

		MiapeMSIDocument msiDocument = null;
		File inputFile = null;

		try {
			inputFile = new File(prideURL);
			// Validate file
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputFile, SchemaValidator.prideXML);

			if (!storeInRepository) {
				inputFile = FileManager.saveLocalFile(inputFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputFile);
			}
			log.info("createDocument");

			log.info("parsing PRIDE to document MSI");
			msiDocument = MSIMiapeFactory.getFactory().create(new MiapeMsiPrideXmlFile(inputFile), null,
					getControlVocabularyManager(), userName, password, projectName);
			((AbstractDocumentFromPride) msiDocument).setAttachedFileLocation(inputFile.getAbsolutePath());

			log.info("Storing document MSI");
			log.info("Converting document to xml");
			if (!storeInRepository) {
				id_msi = LocalFilesIndex.getInstance().getFreeIndex();
				msiDocument.setId(id_msi);
				final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
				saveLocally(id_msi, msiDocumentXML, projectName, "MSI", FilenameUtils.getBaseName(prideURL));
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputFile);
				return String.valueOf(id_msi);
			} else {
				log.info("Converting document to bytes");
				final byte[] msiBytes = msiDocument.toXml().toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, msiBytes);
				log.info("MIAPE MSI stored. ID=" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored. ID=" + id_msi + "\n");

				if (id_msi > 0) {
					return String.valueOf(id_msi);
				}
			}
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		return "error: " + sb.toString();
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
	public String[] storeMiapeMSMSIFromPRIDE(String prideURL, String userName, String password, String projectName) {
		String[] identifiers = new String[2];
		int id_ms = -1;
		int id_msi = -1;
		StringBuilder sb = new StringBuilder();
		try {
			log.info("fileName " + prideURL);
			log.info("userName " + userName);
			log.info("password " + password);

			log.info("projectName " + projectName);

			MiapeMSDocument msDocument = null;
			MiapeMSIDocument msiDocument = null;
			File inputFile = new File(prideURL);

			// Validate file
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputFile, SchemaValidator.prideXML);

			if (!storeInRepository) {
				inputFile = FileManager.saveLocalFile(inputFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputFile);
			}
			log.info("createDocument");

			log.info("parsing XML to document MS");
			msDocument = MSMiapeFactory.getFactory().toDocument(new MiapeMsPrideXmlFile(inputFile), null,
					getControlVocabularyManager(), userName, password, projectName);
			((AbstractDocumentFromPride) msDocument).setAttachedFileLocation(inputFile.getAbsolutePath());

			log.info("parsing XML to document MSI");
			msiDocument = MSIMiapeFactory.getFactory().create(new MiapeMsiPrideXmlFile(inputFile), null,
					getControlVocabularyManager(), userName, password, projectName);
			((AbstractDocumentFromPride) msiDocument).setAttachedFileLocation(inputFile.getAbsolutePath());
			// Associate the MIAPE MS with the already existing MIAPE MSI

			log.info("Associating MIAPE MS (still not stored) with MIAPE MSI (still not stored)");
			((org.proteored.miapeapi.xml.pride.msi.MiapeMSIDocumentImpl) msiDocument)
					.setReferencedMSDocument(msDocument.getId());

			log.info("Storing document MS");
			log.info("Converting document to xml");
			final MiapeXmlFile<MiapeMSDocument> msDocumentXML = msDocument.toXml();
			if (!storeInRepository) {
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				saveLocally(id_ms, msDocumentXML, projectName, "MS", FilenameUtils.getBaseName(prideURL));
				identifiers[0] = String.valueOf(id_ms);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputFile);
			} else {
				log.info("Converting document to bytes");
				final byte[] msBytes = msDocumentXML.toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				id_ms = miapeAPIWebservice.storeMiapeMS(userName, password, msBytes);
				log.info("MIAPE MS stored. ID=" + id_ms);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MS stored. ID=" + id_ms + "\n");
			}
			log.info("Storing document MSI");
			log.info("Converting document to xml");

			// System.out.println(msiDocumentXML);
			if (!storeInRepository) {
				id_msi = LocalFilesIndex.getInstance().getFreeIndex();
				msiDocument.setId(id_msi);
				final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
				saveLocally(id_msi, msiDocumentXML, projectName, "MSI", FilenameUtils.getBaseName(prideURL));
				identifiers[1] = String.valueOf(id_msi);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputFile);
			} else {
				log.info("Converting document to bytes");
				final byte[] msiBytes = msiDocument.toXml().toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, msiBytes);
				log.info("MIAPE MSI stored. ID=" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored. ID=" + id_msi + "\n");

				if (id_ms > 0) {
					identifiers[0] = String.valueOf(id_ms);
				} else {
					identifiers[0] = "";
				}
				if (id_msi > 0)
					identifiers[1] = String.valueOf(id_msi);
			}
			return identifiers;

		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		identifiers[0] = "error: " + sb.toString();
		return identifiers;
	}

	public String[] storeMiapeMSMSIFromPRIDEAndMetadata(String prideURL, String miapeMSMetadataURL, String userName,
			String password, String projectName) {
		String[] identifiers = new String[2];
		int id_ms = -1;
		int id_msi = -1;
		StringBuilder sb = new StringBuilder();
		try {
			log.info("fileName " + prideURL);
			log.info("userName " + userName);
			log.info("password " + password);

			log.info("projectName " + projectName);

			MiapeMSDocument msDocument = null;
			MiapeMSIDocument msiDocument = null;
			File inputFile = new File(prideURL);

			// Validate file
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputFile, SchemaValidator.prideXML);

			log.info("createDocument");
			if (!storeInRepository) {
				inputFile = FileManager.saveLocalFile(inputFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputFile);
			}

			log.info("parsing XML to document MS");
			msDocument = MSMiapeFactory.getFactory().toDocument(new MiapeMsPrideXmlFile(inputFile), null,
					getControlVocabularyManager(), userName, password, projectName);
			((AbstractDocumentFromPride) msDocument).setAttachedFileLocation(inputFile.getAbsolutePath());

			log.info("Getting MIAPE MS Metadata");
			MiapeMSDocument miapeMSMetadata = MiapeMSXmlFactory.getFactory().toDocument(
					new MIAPEMSXmlFile(miapeMSMetadataURL), getControlVocabularyManager(), null, userName, password);
			log.info("Merging MIAPE MS from mzML and MIAPE MS METADATA FILE");
			final MiapeMSDocument miapeMSMerged = MiapeMSMerger
					.getInstance(getControlVocabularyManager(), null, userName, password)
					.merge(msDocument, miapeMSMetadata);

			log.info("parsing XML to document MSI");
			msiDocument = MSIMiapeFactory.getFactory().create(new MiapeMsiPrideXmlFile(inputFile), null,
					getControlVocabularyManager(), userName, password, projectName);
			((AbstractDocumentFromPride) msiDocument).setAttachedFileLocation(inputFile.getAbsolutePath());
			// Associate the MIAPE MS with the already existing MIAPE MSI

			log.info("Associating MIAPE MS (still not stored) with MIAPE MSI (still not stored)");
			((org.proteored.miapeapi.xml.pride.msi.MiapeMSIDocumentImpl) msiDocument)
					.setReferencedMSDocument(msDocument.getId());

			log.info("Storing document MS");
			log.info("Converting document to xml");
			final MiapeXmlFile<MiapeMSDocument> msDocumentXML = miapeMSMerged.toXml();
			if (!storeInRepository) {
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				saveLocally(id_ms, msDocumentXML, projectName, "MS", FilenameUtils.getBaseName(prideURL));
				identifiers[0] = String.valueOf(id_ms);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputFile);
			} else {
				log.info("Converting document to bytes");
				final byte[] msBytes = msDocumentXML.toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				id_ms = miapeAPIWebservice.storeMiapeMS(userName, password, msBytes);
				log.info("MIAPE MS stored. ID=" + id_ms);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MS stored. ID=" + id_ms + "\n");
			}
			log.info("Storing document MSI");
			log.info("Converting document to xml");
			// System.out.println(msiDocumentXML);
			if (!storeInRepository) {
				id_msi = LocalFilesIndex.getInstance().getFreeIndex();
				msiDocument.setId(id_msi);
				final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
				saveLocally(id_msi, msiDocumentXML, projectName, "MSI", FilenameUtils.getBaseName(prideURL));
				identifiers[1] = String.valueOf(id_msi);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputFile);
			} else {
				log.info("Converting document to bytes");
				final byte[] msiBytes = msiDocument.toXml().toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, msiBytes);
				log.info("MIAPE MSI stored. ID=" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored. ID=" + id_msi + "\n");

				if (id_ms > 0) {
					identifiers[0] = String.valueOf(id_ms);
				} else {
					identifiers[0] = "";
				}
				if (id_msi > 0)
					identifiers[1] = String.valueOf(id_msi);
			}
			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		identifiers[0] = "error: " + sb.toString();
		return identifiers;
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
	public String storeMiapeMSFromMzML(String mzMLURL, String userName, String password, String projectName,
			boolean fastParsing) {
		int id_ms = -1;

		log.info("fileName " + mzMLURL);
		log.info("userName " + userName);
		log.info("password " + password);
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
			if (!storeInRepository) {
				inputFile = FileManager.saveLocalFile(inputFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputFile);
			}
			if (fastParsing) {
				log.info("parsing mzML XML to document MS in the faster method (SAX+DOM method)");
				MzMLLightParser mzMLParser = new MzMLLightParser(inputFile.getAbsolutePath());
				MzML mzmlLight = mzMLParser.ParseDocument(MiapeLocalExtractor.schemaValidation);

				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(mzmlLight, null,
						getControlVocabularyManager(), userName, password, inputFile.getName(), projectName);

			} else {
				log.info("parsing mzML XML to document MS in the slower method (jmzML API)");
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(
						new MiapeMzMLFile(inputFile), null, getControlVocabularyManager(), userName, password,
						projectName);

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
			if (!storeInRepository) {
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				saveLocally(id_ms, msDocumentXML, projectName, "MS", FilenameUtils.getBaseName(mzMLURL));
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputFile);
				return String.valueOf(id_ms);
			} else {
				log.info("Converting document to bytes");
				final byte[] msBytes = msDocumentXML.toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_ms = miapeAPIWebservice.storeMiapeMS(userName, password, msBytes);
				log.info("MIAPE MS stored. ID=" + id_ms);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MS stored. ID=" + id_ms + "\n");
				if (id_ms > 0) {
					return String.valueOf(id_ms);
				} else {
					// an exception has been thrown
				}
			}
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		return "error: " + sb.toString();
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
	public String storeMiapeMSFromMzMLAndMetadata(String mzMLURL, String miapeMSMetadataURL, String userName,
			String password, String projectName, boolean fastParsing) {
		int id_ms = -1;

		log.info("fileName " + mzMLURL);
		log.info("userName " + userName);
		log.info("password " + password);
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
			if (!storeInRepository) {
				inputFile = FileManager.saveLocalFile(inputFile, projectName);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputFile);
			}
			if (fastParsing) {
				log.info("parsing mzML XML to document MS in the faster method (SAX+DOM method)");
				MzMLLightParser mzMLParser = new MzMLLightParser(inputFile.getAbsolutePath());
				MzML mzmlLight = mzMLParser.ParseDocument(MiapeLocalExtractor.schemaValidation);

				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(mzmlLight, null,
						getControlVocabularyManager(), userName, password, inputFile.getName(), projectName);

			} else {
				log.info("parsing mzML XML to document MS in the slower method (jmzML API)");
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(
						new MiapeMzMLFile(inputFile), null, getControlVocabularyManager(), userName, password,
						projectName);

			}
			// add mzML to attached file
			((MiapeMSDocumentImpl) msDocument).setAttachedFileLocation(inputFile.getAbsolutePath());
			// add mzML to resulting data
			List<ResultingData> resultingDatas = msDocument.getResultingDatas();
			ResultingData resultingData = getResultingDataFromMZML(mzMLURL);
			((MiapeMSDocumentImpl) msDocument).addResultingData(resultingData);
			resultingDatas = msDocument.getResultingDatas();

			log.info("Getting MIAPE MS Metadata");
			MiapeMSDocument miapeMSMetadata = MiapeMSXmlFactory.getFactory().toDocument(
					new MIAPEMSXmlFile(miapeMSMetadataURL), getControlVocabularyManager(), null, userName, password);
			resultingDatas = miapeMSMetadata.getResultingDatas();
			log.info("Merging MIAPE MS from mzML and MIAPE MS METADATA FILE");
			final MiapeMSDocument miapeMSMerged = MiapeMSMerger
					.getInstance(getControlVocabularyManager(), null, userName, password)
					.merge(msDocument, miapeMSMetadata);

			log.info("MIAPE created");
			log.info("Storing document MS");
			log.info("Converting document to xml");
			final MiapeXmlFile<MiapeMSDocument> msDocumentXML = miapeMSMerged.toXml();
			if (!storeInRepository) {
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				saveLocally(id_ms, msDocumentXML, projectName, "MS", FilenameUtils.getBaseName(mzMLURL));
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputFile);
			} else {
				log.info("Converting document to bytes");
				final byte[] msBytes = msDocumentXML.toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_ms = miapeAPIWebservice.storeMiapeMS(userName, password, msBytes);
				log.info("MIAPE MS stored. ID=" + id_ms);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MS stored. ID=" + id_ms + "\n");
				if (id_ms > 0) {
					return String.valueOf(id_ms);
				} else {
					// an exception has been thrown
				}
			}
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		return "error: " + sb.toString();
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
	public String[] storeMiapeMSMSIFromMzMLAndMzIdentML(String mzMLURL, String mzIdentMLURL, String userName,
			String password, String projectName, boolean fastParsing) {
		int id_ms = -1;
		int id_msi = -1;
		String[] identifiers = new String[2];
		StringBuilder sb = new StringBuilder();

		log.info("fileName " + mzMLURL);
		log.info("fileName2 " + mzIdentMLURL);
		log.info("userName " + userName);
		log.info("password " + password);
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
			if (!storeInRepository) {
				inputmzMLFile = FileManager.saveLocalFile(inputmzMLFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputmzMLFile);
			}

			if (fastParsing) {
				log.info("parsing mzML XML to document MS in the faster method (SAX+DOM method)");
				MzMLLightParser mzMLParser = new MzMLLightParser(inputmzMLFile.getAbsolutePath());
				MzML mzmlLight = mzMLParser.ParseDocument(MiapeLocalExtractor.schemaValidation);
				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(mzmlLight, null,
						getControlVocabularyManager(), userName, password, inputmzMLFile.getName(), projectName);
			} else {
				log.info("parsing mzML XML to document MS in the slower method (jmzML API)");
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(
						new MiapeMzMLFile(inputmzMLFile), null, getControlVocabularyManager(), userName, password,
						projectName);
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
			if (!storeInRepository) {
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				saveLocally(id_ms, msDocumentXML, projectName, "MS", FilenameUtils.getBaseName(mzMLURL));
				identifiers[0] = String.valueOf(id_ms);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputmzMLFile);
			} else {
				log.info("Converting document to bytes");
				final byte[] msBytes = msDocumentXML.toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE MS in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				id_ms = miapeAPIWebservice.storeMiapeMS(userName, password, msBytes);
				log.info("MIAPE MS stored. ID=" + id_ms);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MS stored. ID=" + id_ms + "\n");
			}
			// ////////////////////////////////////
			log.info("createFile");
			File inputmzIdentMLFile = new File(mzIdentMLURL);
			// Validate file
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputmzIdentMLFile, SchemaValidator.mzIdentML_1_0);

			if (!storeInRepository) {
				inputmzIdentMLFile = FileManager.saveLocalFile(inputmzIdentMLFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputmzIdentMLFile);
			}

			log.info("create MIAPE MSI from mzIdentML");
			MiapeMzIdentMLFile xmlFile = new MiapeMzIdentMLFile(inputmzIdentMLFile);
			msiDocument = org.proteored.miapeapi.xml.mzidentml.MSIMiapeFactory.getFactory().toDocument(xmlFile, null,
					getControlVocabularyManager(), userName, password, projectName, processInParallel);
			((org.proteored.miapeapi.xml.mzidentml.MiapeMSIDocumentImpl) msiDocument)
					.setAttachedFileURL(inputmzIdentMLFile.getAbsolutePath());
			if (id_ms > 0) {
				((org.proteored.miapeapi.xml.mzidentml.MiapeMSIDocumentImpl) msiDocument)
						.setReferencedMSDocument(Integer.valueOf(id_ms));
			}
			log.info("MIAPE MSI created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");
			if (!storeInRepository) {
				id_msi = LocalFilesIndex.getInstance().getFreeIndex();
				msiDocument.setId(id_msi);
				final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();

				saveLocally(id_msi, msiDocumentXML, projectName, "MSI", FilenameUtils.getBaseName(mzIdentMLURL));
				identifiers[1] = String.valueOf(id_msi);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputmzIdentMLFile);
			} else {
				log.info("Converting document to bytes");
				final byte[] msiBytes = msiDocument.toXml().toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE MSI in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, msiBytes);
				log.info("MIAPE MSI stored. ID=" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored. ID=" + id_msi + "\n");
				// ///////////////////

				if (id_ms > 0) {
					identifiers[0] = String.valueOf(id_ms);
				} else {
					identifiers[0] = "";
				}
				if (id_msi > 0)
					identifiers[1] = String.valueOf(id_msi);
			}
			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		identifiers[0] = "error: " + sb.toString();
		return identifiers;
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
	public String[] storeMiapeMSMSIFromMzMLAndMzIdentMLAndMetadata(String mzMLURL, String miapeMSMetadataURL,
			String mzIdentMLURL, String userName, String password, String projectName, boolean fastParsing) {
		int id_ms = -1;
		int id_msi = -1;
		String[] identifiers = new String[2];
		StringBuilder sb = new StringBuilder();

		log.info("fileName " + mzMLURL);
		log.info("fileName2 " + mzIdentMLURL);
		log.info("userName " + userName);
		log.info("password " + password);
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
			if (!storeInRepository) {
				inputMzMLFile = FileManager.saveLocalFile(inputMzMLFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputMzMLFile);
			}
			if (fastParsing) {
				log.info("parsing mzML XML to document MS in the faster method (SAX+DOM method)");
				MzMLLightParser mzMLParser = new MzMLLightParser(inputMzMLFile.getAbsolutePath());
				MzML mzmlLight = mzMLParser.ParseDocument(MiapeLocalExtractor.schemaValidation);
				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(mzmlLight, null,
						getControlVocabularyManager(), userName, password, inputMzMLFile.getName(), projectName);
			} else {
				log.info("parsing mzML XML to document MS in the slower method (jmzML API)");
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(
						new MiapeMzMLFile(inputMzMLFile), null, getControlVocabularyManager(), userName, password,
						projectName);
			}

			// add mzML to attached file
			((MiapeMSDocumentImpl) msDocument).setAttachedFileLocation(inputMzMLFile.getAbsolutePath());
			// add mzML to resulting data
			ResultingData resultingData = getResultingDataFromMZML(inputMzMLFile.getAbsolutePath());
			((MiapeMSDocumentImpl) msDocument).addResultingData(resultingData);
			log.info("Getting MIAPE MS Metadata");
			MiapeMSDocument miapeMSMetadata = MiapeMSXmlFactory.getFactory().toDocument(
					new MIAPEMSXmlFile(miapeMSMetadataURL), getControlVocabularyManager(), null, userName, password);
			log.info("Merging MIAPE MS from mzML and MIAPE MS METADATA FILE");
			final MiapeMSDocument miapeMSMerged = MiapeMSMerger
					.getInstance(getControlVocabularyManager(), null, userName, password)
					.merge(msDocument, miapeMSMetadata);
			log.info("MIAPE MS created");
			log.info("Storing document MS");
			log.info("Converting document to xml");
			final MiapeXmlFile<MiapeMSDocument> msDocumentXML = miapeMSMerged.toXml();
			if (!storeInRepository) {
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				saveLocally(id_ms, msDocumentXML, projectName, "MS", FilenameUtils.getBaseName(mzMLURL));
				identifiers[0] = String.valueOf(id_ms);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputMzMLFile);
			} else {
				log.info("Converting document to bytes");
				final byte[] msBytes = msDocumentXML.toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE MS in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				id_ms = miapeAPIWebservice.storeMiapeMS(userName, password, msBytes);
				log.info("MIAPE MS stored. ID=" + id_ms);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MS stored. ID=" + id_ms + "\n");
			}
			// ////////////////////////////////////
			log.info("createFile");
			File inputMzIdentMLFile = new File(mzIdentMLURL);
			// Validate file
			if (schemaValidation)
				SchemaValidator.validateXMLFile(inputMzIdentMLFile, SchemaValidator.mzIdentML_1_0);

			if (!storeInRepository) {
				inputMzIdentMLFile = FileManager.saveLocalFile(inputMzIdentMLFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputMzIdentMLFile);
			}
			log.info("create MIAPE MSI from mzIdentML");
			MiapeMzIdentMLFile xmlFile = new MiapeMzIdentMLFile(inputMzIdentMLFile);
			msiDocument = org.proteored.miapeapi.xml.mzidentml.MSIMiapeFactory.getFactory().toDocument(xmlFile, null,
					getControlVocabularyManager(), userName, password, projectName, processInParallel);
			((org.proteored.miapeapi.xml.mzidentml.MiapeMSIDocumentImpl) msiDocument)
					.setAttachedFileURL(inputMzIdentMLFile.getAbsolutePath());
			if (id_ms > 0) {
				((org.proteored.miapeapi.xml.mzidentml.MiapeMSIDocumentImpl) msiDocument)
						.setReferencedMSDocument(Integer.valueOf(id_ms));
			}
			log.info("MIAPE MSI created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");
			if (!storeInRepository) {
				id_msi = LocalFilesIndex.getInstance().getFreeIndex();
				msiDocument.setId(id_msi);
				final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
				saveLocally(id_msi, msiDocumentXML, projectName, "MSI", FilenameUtils.getBaseName(mzIdentMLURL));
				identifiers[1] = String.valueOf(id_msi);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputMzIdentMLFile);
			} else {
				log.info("Converting document to bytes");
				final byte[] msiBytes = msiDocument.toXml().toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE MSI in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, msiBytes);
				log.info("MIAPE MSI stored. ID=" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored. ID=" + id_msi + "\n");
				// ///////////////////

				if (id_ms > 0) {
					identifiers[0] = String.valueOf(id_ms);
				} else {
					identifiers[0] = "";
				}
				if (id_msi > 0)
					identifiers[1] = String.valueOf(id_msi);
			}
			return identifiers;

		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		identifiers[0] = "error: " + sb.toString();
		return identifiers;
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
	public String[] storeMiapeMSMSIFromMzML(String mzMLFileURL, byte[] miapeMSIXML, String userName, String password,
			String projectName, boolean fastParsing) {
		int id_msi = -1;
		int id_ms = -1;
		String[] identifiers = new String[2];
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
			if (!storeInRepository) {
				inputMzMLFile = FileManager.saveLocalFile(inputMzMLFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputMzMLFile);
			}
			if (fastParsing) {
				log.info("parsing mzML XML to document MS in the faster method (SAX+DOM method)");
				MzMLLightParser mzMLParser = new MzMLLightParser(inputMzMLFile.getAbsolutePath());
				MzML mzmlLight = mzMLParser.ParseDocument(MiapeLocalExtractor.schemaValidation);

				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(mzmlLight, null,
						getControlVocabularyManager(), userName, password, inputMzMLFile.getName(), projectName);
			} else {
				log.info("parsing mzML XML to document MS in the slower method (jmzML API)");
				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(
						new MiapeMzMLFile(inputMzMLFile), null, getControlVocabularyManager(), userName, password,
						projectName);

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
			if (!storeInRepository) {
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				saveLocally(id_ms, msDocumentXML, projectName, "MS", FilenameUtils.getBaseName(mzMLFileURL));
				identifiers[0] = String.valueOf(id_ms);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputMzMLFile);
			} else {
				id_ms = miapeAPIWebservice.storeMiapeMS(userName, password, msDocumentXML.toBytes());
				log.info("MIAPE MS stored ID:" + id_ms);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MS stored ID:" + id_ms + "\n");
			}
			if (miapeMSIXML != null) {
				log.info("Storing MIAPE MSI document from received MIAPE MSI XML file");
				final MIAPEMSIXmlFile xmlFile = new MIAPEMSIXmlFile(miapeMSIXML);
				if (id_ms > 0) {
					log.info("Associating MIAPE MSI in memory with MIAPE MS document (" + id_ms + ")");
					final MiapeMSIDocument miapeMSIDocument = MiapeMSIXmlFactory.getFactory().toDocument(xmlFile,
							getControlVocabularyManager(), null, userName, password);
					miapeMSIDocument.setReferencedMSDocument(id_ms);

					log.info("MIAPE created");
					log.info("Storing document MS");
					log.info("Converting document to xml");
					if (!storeInRepository) {
						id_msi = LocalFilesIndex.getInstance().getFreeIndex();
						miapeMSIDocument.setId(id_msi);
						final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = miapeMSIDocument.toXml();
						saveLocally(id_msi, msiDocumentXML, projectName, "MSI", null);
						identifiers[1] = String.valueOf(id_msi);
					} else {
						log.info("Converting document to bytes");
						final byte[] msiBytes = miapeMSIDocument.toXml().toBytes();
						log.info("Sending bytes to webservice");
						swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
								"Storing MIAPE in the ProteoRed MIAPE repository\n");
						swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
								"Waiting for server response...\n");
						swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null,
								idJob);

						id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, msiBytes);
					}
				} else {
					if (!storeInRepository) {
						MIAPEMSIXmlFile msiDocumentXML = xmlFile;
						id_msi = LocalFilesIndex.getInstance().getFreeIndex();
						saveLocally(id_ms, msiDocumentXML, projectName, "MSI", null);
						identifiers[1] = String.valueOf(id_msi);
					} else {
						log.info("Sending bytes to webservice");
						swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
								"Storing MIAPE in the ProteoRed MIAPE repository\n");
						swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
								"Waiting for server response...\n");
						swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null,
								idJob);

						id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, miapeMSIXML);
					}
				}
				log.info("MIAPE MSI stored ID:" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored ID:" + id_msi + "\n");
			}

			if (id_ms > 0) {
				identifiers[0] = String.valueOf(id_ms);
			} else {
				identifiers[0] = "";
			}
			if (id_msi > 0)
				identifiers[1] = String.valueOf(id_msi);

			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		identifiers[0] = "error: " + sb.toString();
		return identifiers;
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
	public String[] storeMiapeMSMSIFromMzMLMetadata(String mzMLFileURL, String miapeMSMetadataURL, byte[] miapeMSIXML,
			String userName, String password, String projectName, boolean fastParsing) {
		int id_msi = -1;
		int id_ms = -1;
		String[] identifiers = new String[2];
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
			if (!storeInRepository) {
				inputMzMLFile = FileManager.saveLocalFile(inputMzMLFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputMzMLFile);
			}
			if (fastParsing) {
				log.info("parsing mzML XML to document MS in the faster method (SAX+DOM method)");
				MzMLLightParser mzMLParser = new MzMLLightParser(inputMzMLFile.getAbsolutePath());
				MzML mzmlLight = mzMLParser.ParseDocument(MiapeLocalExtractor.schemaValidation);

				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(mzmlLight, null,
						getControlVocabularyManager(), userName, password, inputMzMLFile.getName(), projectName);
			} else {
				log.info("parsing mzML XML to document MS in the slower method (jmzML API)");
				// Create the MIAPE MS document
				msDocument = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(
						new MiapeMzMLFile(inputMzMLFile), null, getControlVocabularyManager(), userName, password,
						projectName);

			}
			// set mzML file to the attached file
			((MiapeMSDocumentImpl) msDocument).setAttachedFileLocation(inputMzMLFile.getAbsolutePath());
			// set mzML to the resulting data
			ResultingData resultingData = getResultingDataFromMZML(inputMzMLFile.getAbsolutePath());
			((MiapeMSDocumentImpl) msDocument).addResultingData(resultingData);

			log.info("Getting MIAPE MS Metadata");
			MiapeMSDocument miapeMSMetadata = MiapeMSXmlFactory.getFactory().toDocument(
					new MIAPEMSXmlFile(miapeMSMetadataURL), getControlVocabularyManager(), null, userName, password);
			log.info("Merging MIAPE MS from mzML and MIAPE MS METADATA FILE");
			final MiapeMSDocument miapeMSMerged = MiapeMSMerger
					.getInstance(getControlVocabularyManager(), null, userName, password)
					.merge(msDocument, miapeMSMetadata);

			log.info("MIAPE MS document created in memory");
			log.info("Storing that MIAPE MS");
			swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
					"Storing MIAPE in the ProteoRed MIAPE repository\n");
			swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null, "Waiting for server response...\n");
			swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

			MiapeXmlFile<MiapeMSDocument> msDocumentXML = miapeMSMerged.toXml();
			if (!storeInRepository) {
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				saveLocally(id_ms, msDocumentXML, projectName, "MS", FilenameUtils.getBaseName(mzMLFileURL));
				identifiers[0] = String.valueOf(id_ms);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_ms, inputMzMLFile);
			} else {
				id_ms = miapeAPIWebservice.storeMiapeMS(userName, password, msDocumentXML.toBytes());
				log.info("MIAPE MS stored ID:" + id_ms);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MS stored ID:" + id_ms + "\n");
			}
			if (miapeMSIXML != null) {
				log.info("Storing MIAPE MSI document from received MIAPE MSI XML file");
				if (id_ms > 0) {
					log.info("Associating MIAPE MSI in memory with MIAPE MS document (" + id_ms + ")");
					final MiapeMSIDocument miapeMSIDocument = MiapeMSIXmlFactory.getFactory().toDocument(
							new MIAPEMSIXmlFile(miapeMSIXML), getControlVocabularyManager(), null, userName, password);
					miapeMSIDocument.setReferencedMSDocument(id_ms);

					log.info("MIAPE created");
					log.info("Storing document MS");
					log.info("Converting document to xml");
					if (!storeInRepository) {
						id_msi = LocalFilesIndex.getInstance().getFreeIndex();
						miapeMSIDocument.setId(id_msi);
						final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = miapeMSIDocument.toXml();
						saveLocally(id_msi, msiDocumentXML, projectName, "MSI", null);
						identifiers[1] = String.valueOf(id_msi);
					} else {
						log.info("Converting document to bytes");
						final byte[] msiBytes = miapeMSIDocument.toXml().toBytes();
						log.info("Sending bytes to webservice");
						swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
								"Storing MIAPE in the ProteoRed MIAPE repository\n");
						swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
								"Waiting for server response...\n");
						swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null,
								idJob);

						id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, msiBytes);
					}
				} else {
					if (!storeInRepository) {
						id_msi = LocalFilesIndex.getInstance().getFreeIndex();
						MiapeXmlFile msiDocumentXML = new MIAPEMSIXmlFile(miapeMSIXML);
						saveLocally(id_msi, msiDocumentXML, projectName, "MSI", null);
						// convert the xml file to document to set the id
						final MiapeMSIDocument miapeMSI = MiapeMSIXmlFactory.getFactory().toDocument(msiDocumentXML,
								getControlVocabularyManager(), null, userName, password);
						miapeMSI.setId(id_msi);
						// save again
						saveLocally(id_msi, miapeMSI.toXml(), projectName, "MSI", null);
						identifiers[1] = String.valueOf(id_msi);
					} else {
						log.info("Sending bytes to webservice");
						swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
								"Storing MIAPE in the ProteoRed MIAPE repository\n");
						swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
								"Waiting for server response...\n");
						swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null,
								idJob);

						id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, miapeMSIXML);
					}
				}
				log.info("MIAPE MSI stored ID:" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored ID:" + id_msi + "\n");
			}

			if (id_ms > 0) {
				identifiers[0] = String.valueOf(id_ms);
			} else {
				identifiers[0] = "";
			}
			if (id_msi > 0)
				identifiers[1] = String.valueOf(id_msi);

			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		identifiers[0] = "error: " + sb.toString();
		return identifiers;
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
	public String[] storeMiapeMSIFromMzIdentML(String mzIdentMLURL, int idMiapeMS, String userName, String password,
			String projectName) {
		String[] identifiers = new String[2];
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
					SchemaValidator.validateXMLFile(inputMzIdentMLFile, SchemaValidator.mzIdentML_1_1);
				}
			}
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		log.info("create MIAPE MSI from mzIdentML");
		try {
			try {
				if (!storeInRepository) {
					inputMzIdentMLFile = FileManager.saveLocalFile(inputMzIdentMLFile, projectName);
					LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputMzIdentMLFile);
				}
				MiapeMzIdentMLFile xmlFile = new MiapeMzIdentMLFile(inputMzIdentMLFile);
				msiDocument = org.proteored.miapeapi.xml.mzidentml.MSIMiapeFactory.getFactory().toDocument(xmlFile,
						null, getControlVocabularyManager(), userName, password, projectName, processInParallel);
				((org.proteored.miapeapi.xml.mzidentml.MiapeMSIDocumentImpl) msiDocument)
						.setAttachedFileURL(inputMzIdentMLFile.getAbsolutePath());
				if (idMiapeMS > 0) {
					((org.proteored.miapeapi.xml.mzidentml.MiapeMSIDocumentImpl) msiDocument)
							.setReferencedMSDocument(Integer.valueOf(idMiapeMS));
				}
			} catch (WrongXMLFormatException ex) {
				log.info("Error trying to read as mzIdentML 1.0. Trying now as mzIdentML 1.1 or 1.2...");
				org.proteored.miapeapi.xml.mzidentml_1_1.MiapeMzIdentMLFile xmlFile = new org.proteored.miapeapi.xml.mzidentml_1_1.MiapeMzIdentMLFile(
						inputMzIdentMLFile);
				msiDocument = org.proteored.miapeapi.xml.mzidentml_1_1.MSIMiapeFactory.getFactory().toDocument(xmlFile,
						null, getControlVocabularyManager(), userName, password, projectName, processInParallel);
				((org.proteored.miapeapi.xml.mzidentml_1_1.MiapeMSIDocumentImpl) msiDocument)
						.setAttachedFileURL(inputMzIdentMLFile.getAbsolutePath());
				if (idMiapeMS > 0) {
					((org.proteored.miapeapi.xml.mzidentml_1_1.MiapeMSIDocumentImpl) msiDocument)
							.setReferencedMSDocument(Integer.valueOf(idMiapeMS));
				}
			}

			log.info("MIAPE created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");
			if (!storeInRepository) {
				if (idMiapeMS > 0)
					identifiers[0] = String.valueOf(idMiapeMS);
				else {
					identifiers[0] = "";
				}
				id_msi = LocalFilesIndex.getInstance().getFreeIndex();
				msiDocument.setId(id_msi);
				final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
				saveLocally(id_msi, msiDocumentXML, projectName, "MSI", FilenameUtils.getBaseName(mzIdentMLURL));
				identifiers[1] = String.valueOf(id_msi);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputMzIdentMLFile);
				return identifiers;
			} else {
				log.info("Converting document to bytes");
				final byte[] msiBytes = msiDocument.toXml().toBytes();
				log.info("Sending " + msiBytes.length / 1000 + " Kbytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, msiBytes);

				log.info("document stored. ID=" + id_msi);

				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored ID=" + id_msi + "\n");

				if (id_msi > 0) {
					if (idMiapeMS > 0)
						identifiers[0] = String.valueOf(idMiapeMS);
					else {
						identifiers[0] = "";
					}
					identifiers[1] = String.valueOf(id_msi);
					return identifiers;
				}
			}

		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		identifiers[0] = new StringBuilder().append("error:").append(sb.toString()).toString();
		return identifiers;
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
	public String[] storeMiapeMSMSIFromMzIdentML(String mzIdentFileURL, byte[] miapeMSXMLBytes, String userName,
			String password, String projectName) {
		int id_ms = -1;
		int id_msi = -1;
		String[] identifiers = new String[2];
		MiapeMSIDocument msiDocument = null;
		File inputMzIdentMLFile = null;
		StringBuilder sb = new StringBuilder();
		try {
			if (miapeMSXMLBytes != null) {
				if (!storeInRepository) {
					MiapeXmlFile msDocumentXML = new MIAPEMSXmlFile(miapeMSXMLBytes);
					id_ms = LocalFilesIndex.getInstance().getFreeIndex();
					saveLocally(id_ms, msDocumentXML, projectName, "MS", null);
					identifiers[0] = String.valueOf(id_ms);
				} else {
					log.info("Storing MIAPE MS document from received MIAPE MS XML file");
					swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
							"Storing MIAPE MS in the ProteoRed MIAPE repository\n");
					id_ms = miapeAPIWebservice.storeMiapeMS(userName, password, miapeMSXMLBytes);
					log.info("MIAPE MS stored ID:  " + id_ms);
					swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
							"MIAPE MS stored ID:  " + id_ms + "\n");
				}
			}

			log.info("create MIAPE MSI from mzIdentML");
			// copy mzIdentML file to a temp file
			inputMzIdentMLFile = new File(mzIdentFileURL);

			// Validate file
			if (schemaValidation) {
				try {
					SchemaValidator.validateXMLFile(inputMzIdentMLFile, SchemaValidator.mzIdentML_1_0);
				} catch (WrongXMLFormatException ex) {
					SchemaValidator.validateXMLFile(inputMzIdentMLFile, SchemaValidator.mzIdentML_1_1);
				}
			}
			try {
				if (!storeInRepository) {
					inputMzIdentMLFile = FileManager.saveLocalFile(inputMzIdentMLFile, projectName);
					LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputMzIdentMLFile);
				}
				MiapeMzIdentMLFile xmlFile = new MiapeMzIdentMLFile(inputMzIdentMLFile);

				msiDocument = org.proteored.miapeapi.xml.mzidentml.MSIMiapeFactory.getFactory().toDocument(xmlFile,
						null, getControlVocabularyManager(), userName, password, projectName, processInParallel);
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
						null, getControlVocabularyManager(), userName, password, projectName, processInParallel);
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
			if (!storeInRepository) {
				id_msi = LocalFilesIndex.getInstance().getFreeIndex();
				msiDocument.setId(id_msi);
				final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
				saveLocally(id_msi, msiDocumentXML, projectName, "MSI", FilenameUtils.getBaseName(mzIdentFileURL));
				identifiers[1] = String.valueOf(id_msi);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputMzIdentMLFile);
			} else {
				log.info("Converting document to bytes");
				final byte[] msiBytes = msiDocument.toXml().toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, msiBytes);
				log.info("MIAPE MSI stored ID:" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored ID:" + id_msi + "\n");

				if (id_ms > 0) {
					identifiers[0] = String.valueOf(id_ms);
				} else {
					identifiers[0] = "";
				}
				if (id_msi > 0)
					identifiers[1] = String.valueOf(id_msi);
			}
			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		identifiers[0] = "error: " + sb.toString();
		return identifiers;
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
	public String[] storeMiapeMSIFromXTandemXML(String xTandemXMLURI, int idMiapeMS, String userName, String password,
			String projectName) {
		String[] identifiers = new String[2];
		int id_msi = -1;
		MiapeMSIDocument msiDocument = null;
		File inputXTandemXMLFile = null;
		StringBuilder sb = new StringBuilder();
		log.info("createFile");

		try {
			inputXTandemXMLFile = new File(xTandemXMLURI);
			if (!storeInRepository) {
				inputXTandemXMLFile = FileManager.saveLocalFile(inputXTandemXMLFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputXTandemXMLFile);
			}
			// Dont Validate file because we don't have the schema
			// if (schemaValidation)
			// SchemaValidator.validateXMLFile(mzIdentMLFile,
			// SchemaValidator.mzIdentML);

			log.info("create MIAPE MSI from xTandem XML");
			MiapeXTandemFile xmlFile = new MiapeXTandemFile(inputXTandemXMLFile);
			msiDocument = org.proteored.miapeapi.xml.xtandem.msi.MSIMiapeFactory.getFactory().toDocument(xmlFile, null,
					getControlVocabularyManager(), userName, password, projectName);
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
			if (!storeInRepository) {
				if (idMiapeMS > 0)
					identifiers[0] = String.valueOf(idMiapeMS);
				else {
					identifiers[0] = "";
				}
				id_msi = LocalFilesIndex.getInstance().getFreeIndex();
				msiDocument.setId(id_msi);
				final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
				identifiers[1] = String.valueOf(id_msi);
				saveLocally(id_msi, msiDocumentXML, projectName, "MSI", FilenameUtils.getBaseName(xTandemXMLURI));
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputXTandemXMLFile);
				return identifiers;
			} else {
				log.info("Converting document to bytes");
				final byte[] msiBytes = msiDocument.toXml().toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, msiBytes);
				log.info("MIAPE MSI stored ID=" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored ID=" + id_msi + "\n");
				if (id_msi > 0) {
					if (idMiapeMS > 0)
						identifiers[0] = String.valueOf(idMiapeMS);
					else {
						identifiers[0] = "";
					}
					identifiers[1] = String.valueOf(id_msi);
					return identifiers;
				}
			}
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		identifiers[0] = new StringBuilder().append("error:").append(sb.toString()).toString();
		return identifiers;

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
	public String[] storeMiapeMSIFromTSV(String tsvURI, TableTextFileSeparator separator, int idMiapeMS,
			String userName, String password, String projectName) {
		String[] identifiers = new String[2];
		int id_msi = -1;
		MiapeMSIDocument msiDocument = null;
		File inputTSVFile = null;
		StringBuilder sb = new StringBuilder();
		log.info("createFile");

		try {
			inputTSVFile = new File(tsvURI);
			if (!storeInRepository) {
				inputTSVFile = FileManager.saveLocalFile(inputTSVFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputTSVFile);
			}
			// Dont Validate file because we don't have the schema
			// if (schemaValidation)
			// SchemaValidator.validateXMLFile(mzIdentMLFile,
			// SchemaValidator.mzIdentML);

			log.info("create MIAPE MSI from TSV file");
			MiapeTSVFile xmlFile = new MiapeTSVFile(inputTSVFile, separator);
			msiDocument = org.proteored.miapeapi.text.tsv.msi.MSIMiapeFactory.getFactory().toDocument(xmlFile, null,
					getControlVocabularyManager(), userName, password, projectName);
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
			if (!storeInRepository) {
				if (idMiapeMS > 0)
					identifiers[0] = String.valueOf(idMiapeMS);
				else {
					identifiers[0] = "";
				}
				id_msi = LocalFilesIndex.getInstance().getFreeIndex();
				msiDocument.setId(id_msi);
				final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
				identifiers[1] = String.valueOf(id_msi);
				saveLocally(id_msi, msiDocumentXML, projectName, "MSI", FilenameUtils.getBaseName(tsvURI));
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputTSVFile);
				return identifiers;
			} else {
				log.info("Converting document to bytes");
				final byte[] msiBytes = msiDocument.toXml().toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, msiBytes);
				log.info("MIAPE MSI stored ID=" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored ID=" + id_msi + "\n");
				if (id_msi > 0) {
					if (idMiapeMS > 0)
						identifiers[0] = String.valueOf(idMiapeMS);
					else {
						identifiers[0] = "";
					}
					identifiers[1] = String.valueOf(id_msi);
					return identifiers;
				}
			}
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		identifiers[0] = new StringBuilder().append("error:").append(sb.toString()).toString();
		return identifiers;

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
	public String[] storeMiapeMSMSIFromXTandemXML(String xTandemXMLFileURL, byte[] miapeMSXMLBytes, String userName,
			String password, String projectName) {
		int id_ms = -1;
		int id_msi = -1;
		StringBuilder sb = new StringBuilder();
		String[] identifiers = new String[2];
		MiapeMSIDocument msiDocument = null;
		File inputXTandemXMLFile = null;

		try {
			if (miapeMSXMLBytes != null) {
				if (!storeInRepository) {
					MiapeXmlFile msDocumentXML = new MIAPEMSXmlFile(miapeMSXMLBytes);
					id_ms = LocalFilesIndex.getInstance().getFreeIndex();
					saveLocally(id_ms, msDocumentXML, projectName, "MS", null);
				} else {
					log.info("Storing MIAPE MS document from received MIAPE MS XML file");
					swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
							"Storing MIAPE MS in the ProteoRed MIAPE repository\n");
					swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null,
							idJob);

					id_ms = miapeAPIWebservice.storeMiapeMS(userName, password, miapeMSXMLBytes);
					log.info("MIAPE MS stored ID:  " + id_ms);
					swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
							"MIAPE MS stored ID:  " + id_ms + "\n");
				}
			}

			log.info("create MIAPE MSI from xTandem XML");
			// copy mzIdentML file to a temp file
			inputXTandemXMLFile = new File(xTandemXMLFileURL);

			if (!storeInRepository) {
				inputXTandemXMLFile = FileManager.saveLocalFile(inputXTandemXMLFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, inputXTandemXMLFile);
			}
			MiapeXTandemFile xmlFile = new MiapeXTandemFile(inputXTandemXMLFile);

			msiDocument = org.proteored.miapeapi.xml.xtandem.msi.MSIMiapeFactory.getFactory().toDocument(xmlFile, null,
					getControlVocabularyManager(), userName, password, projectName);
			((org.proteored.miapeapi.xml.xtandem.msi.MiapeMsiDocumentImpl) msiDocument)
					.setAttachedFileURL(inputXTandemXMLFile.getAbsolutePath());

			if (id_ms > 0) {
				identifiers[0] = String.valueOf(id_ms);
				log.info("Associating the MIAPE MSI document to the previously created MIAPE MS document");
				((org.proteored.miapeapi.xml.xtandem.msi.MiapeMsiDocumentImpl) msiDocument)
						.setReferencedMSDocument(Integer.valueOf(id_ms));
			}
			log.info("MIAPE created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");
			if (!storeInRepository) {
				id_msi = LocalFilesIndex.getInstance().getFreeIndex();
				msiDocument.setId(id_msi);
				final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
				saveLocally(id_msi, msiDocumentXML, projectName, "MSI", FilenameUtils.getBaseName(xTandemXMLFileURL));
				identifiers[1] = String.valueOf(id_msi);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, inputXTandemXMLFile);
			} else {
				log.info("Converting document to bytes");
				final byte[] msiBytes = msiDocument.toXml().toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, msiBytes);
				log.info("MIAPE MSI stored ID:" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored ID:" + id_msi + "\n");

				if (id_ms > 0) {
					identifiers[0] = String.valueOf(id_ms);
				} else {
					identifiers[0] = "";
				}
				if (id_msi > 0)
					identifiers[1] = String.valueOf(id_msi);
			}
			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		identifiers[0] = "error: " + sb.toString();
		return identifiers;
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

	public String storeMiapeMS(MiapeMSDocument msDocument, String userName, String password, String projectName) {
		int id_ms = -1;

		log.info("userName " + userName);
		log.info("password " + password);
		log.info("projectName " + projectName);
		StringBuilder sb = new StringBuilder();
		try {

			log.info("MIAPE created");
			log.info("Storing document MS");
			log.info("Converting document to xml");
			final MiapeXmlFile<MiapeMSDocument> msDocumentXML = msDocument.toXml();
			if (!storeInRepository) {
				id_ms = LocalFilesIndex.getInstance().getFreeIndex();
				saveLocally(id_ms, msDocumentXML, projectName, "MS", null);
				return String.valueOf(id_ms);
			} else {
				log.info("Converting document to bytes");
				final byte[] msBytes = msDocumentXML.toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_ms = miapeAPIWebservice.storeMiapeMS(userName, password, msBytes);
				log.info("MIAPE MS stored. ID=" + id_ms);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MS stored. ID=" + id_ms + "\n");
				if (id_ms > 0) {
					return String.valueOf(id_ms);
				} else {
					// an exception has been thrown
				}
			}
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		return "error: " + sb.toString();
	}

	public String[] storeMiapeMSIFromDTASelect(String dtaSelectFileURI, int idMiapeMS, String userName, String password,
			String projectName) {
		String[] identifiers = new String[2];
		int id_msi = -1;
		MiapeMSIDocument msiDocument = null;
		File dtaSelectFile = null;
		StringBuilder sb = new StringBuilder();
		log.info("createFile");

		try {
			dtaSelectFile = new File(dtaSelectFileURI);
			if (!storeInRepository) {
				dtaSelectFile = FileManager.saveLocalFile(dtaSelectFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, dtaSelectFile);
			}
			// Dont Validate file because we don't have the schema
			// if (schemaValidation)
			// SchemaValidator.validateXMLFile(mzIdentMLFile,
			// SchemaValidator.mzIdentML);

			log.info("create MIAPE MSI from dtaSelect file");
			MiapeDTASelectFile xmlFile = new MiapeDTASelectFile(dtaSelectFile);
			msiDocument = org.proteored.miapeapi.xml.dtaselect.msi.MSIMiapeFactory.getFactory().toDocument(xmlFile,
					null, getControlVocabularyManager(), userName, password, projectName);
			((org.proteored.miapeapi.xml.dtaselect.msi.MiapeMsiDocumentImpl) msiDocument)
					.setAttachedFileURL(dtaSelectFile.getAbsolutePath());

			if (idMiapeMS > 0) {
				((org.proteored.miapeapi.xml.dtaselect.msi.MiapeMsiDocumentImpl) msiDocument)
						.setReferencedMSDocument(Integer.valueOf(idMiapeMS));
			}
			log.info("MIAPE created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");
			if (!storeInRepository) {
				if (idMiapeMS > 0)
					identifiers[0] = String.valueOf(idMiapeMS);
				else {
					identifiers[0] = "";
				}
				id_msi = LocalFilesIndex.getInstance().getFreeIndex();
				msiDocument.setId(id_msi);
				final MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();

				saveLocally(id_msi, msiDocumentXML, projectName, "MSI", FilenameUtils.getBaseName(dtaSelectFileURI));
				identifiers[1] = String.valueOf(id_msi);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, dtaSelectFile);
				return identifiers;
			} else {
				log.info("Converting document to bytes");
				final byte[] msiBytes = msiDocument.toXml().toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, msiBytes);
				log.info("MIAPE MSI stored ID=" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored ID=" + id_msi + "\n");
				if (id_msi > 0) {
					if (idMiapeMS > 0)
						identifiers[0] = String.valueOf(idMiapeMS);
					else {
						identifiers[0] = "";
					}
					identifiers[1] = String.valueOf(id_msi);
					return identifiers;
				}
			}
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		identifiers[0] = new StringBuilder().append("error:").append(sb.toString()).toString();
		return identifiers;

	}

	public String[] storeMiapeMSMSIFromDTASelect(String dtaSelectFileURL, byte[] miapeMSXMLBytes, String userName,
			String password, String projectName) {
		int id_ms = -1;
		int id_msi = -1;
		StringBuilder sb = new StringBuilder();
		String[] identifiers = new String[2];
		MiapeMSIDocument msiDocument = null;
		File dtaSelectFile = null;

		try {
			if (miapeMSXMLBytes != null) {
				if (!storeInRepository) {
					MiapeXmlFile msDocumentXML = new MIAPEMSXmlFile(miapeMSXMLBytes);
					id_ms = LocalFilesIndex.getInstance().getFreeIndex();
					saveLocally(id_ms, msDocumentXML, projectName, "MS", null);
				} else {
					log.info("Storing MIAPE MS document from received MIAPE MS XML file");
					swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
							"Storing MIAPE MS in the ProteoRed MIAPE repository\n");
					swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null,
							idJob);

					id_ms = miapeAPIWebservice.storeMiapeMS(userName, password, miapeMSXMLBytes);
					log.info("MIAPE MS stored ID:  " + id_ms);
					swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
							"MIAPE MS stored ID:  " + id_ms + "\n");
				}
			}

			log.info("create MIAPE MSI from DTASelect file");
			// copy dtaselect file to a temp file
			dtaSelectFile = new File(dtaSelectFileURL);

			if (!storeInRepository) {
				dtaSelectFile = FileManager.saveLocalFile(dtaSelectFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, dtaSelectFile);
			}

			MiapeDTASelectFile dtaSelectMIAPEFile = new MiapeDTASelectFile(dtaSelectFile);

			msiDocument = org.proteored.miapeapi.xml.dtaselect.msi.MSIMiapeFactory.getFactory().toDocument(
					dtaSelectMIAPEFile, null, getControlVocabularyManager(), userName, password, projectName);
			((MiapeMsiDocumentImpl) msiDocument).setAttachedFileURL(dtaSelectFile.getAbsolutePath());

			if (id_ms > 0) {
				identifiers[0] = String.valueOf(id_ms);
				log.info("Associating the MIAPE MSI document to the previously created MIAPE MS document");
				((MiapeMsiDocumentImpl) msiDocument).setReferencedMSDocument(Integer.valueOf(id_ms));
			}
			log.info("MIAPE created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");

			if (!storeInRepository) {
				id_msi = LocalFilesIndex.getInstance().getFreeIndex();
				msiDocument.setId(id_msi);
				MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
				saveLocally(id_msi, msiDocumentXML, projectName, "MSI", FilenameUtils.getBaseName(dtaSelectFileURL));
				identifiers[1] = String.valueOf(id_msi);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, dtaSelectFile);
			} else {
				log.info("Converting document to bytes");
				final byte[] msiBytes = msiDocument.toXml().toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, msiBytes);
				log.info("MIAPE MSI stored ID:" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored ID:" + id_msi + "\n");

				if (id_ms > 0) {
					identifiers[0] = String.valueOf(id_ms);
				} else {
					identifiers[0] = "";
				}
				if (id_msi > 0)
					identifiers[1] = String.valueOf(id_msi);
			}
			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		identifiers[0] = "error: " + sb.toString();
		return identifiers;
	}

	public String[] storeMiapeMSMSIFromTSV(String tsvFileURL, TableTextFileSeparator separator, byte[] miapeMSXMLBytes,
			String userName, String password, String projectName) {
		int id_ms = -1;
		int id_msi = -1;
		StringBuilder sb = new StringBuilder();
		String[] identifiers = new String[2];
		MiapeMSIDocument msiDocument = null;
		File tsvFile = null;

		try {
			if (miapeMSXMLBytes != null) {
				if (!storeInRepository) {
					MiapeXmlFile msDocumentXML = new MIAPEMSXmlFile(miapeMSXMLBytes);
					id_ms = LocalFilesIndex.getInstance().getFreeIndex();
					saveLocally(id_ms, msDocumentXML, projectName, "MS", null);
				} else {
					log.info("Storing MIAPE MS document from received MIAPE MS XML file");
					swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
							"Storing MIAPE MS in the ProteoRed MIAPE repository\n");
					swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null,
							idJob);

					id_ms = miapeAPIWebservice.storeMiapeMS(userName, password, miapeMSXMLBytes);
					log.info("MIAPE MS stored ID:  " + id_ms);
					swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
							"MIAPE MS stored ID:  " + id_ms + "\n");
				}
			}

			log.info("create MIAPE MSI from TSV text file");
			// copy dtaselect file to a temp file
			tsvFile = new File(tsvFileURL);

			if (!storeInRepository) {
				tsvFile = FileManager.saveLocalFile(tsvFile, projectName);
				LocalFilesIndex.getInstance().indexFileByProjectName(projectName, tsvFile);
			}

			MiapeTSVFile tsvMIAPEFile = new MiapeTSVFile(tsvFile, separator);

			msiDocument = org.proteored.miapeapi.text.tsv.msi.MSIMiapeFactory.getFactory().toDocument(tsvMIAPEFile,
					null, getControlVocabularyManager(), userName, password, projectName);
			((org.proteored.miapeapi.text.tsv.msi.MiapeMsiDocumentImpl) msiDocument)
					.setAttachedFileURL(tsvFile.getAbsolutePath());

			if (id_ms > 0) {
				identifiers[0] = String.valueOf(id_ms);
				log.info("Associating the MIAPE MSI document to the previously created MIAPE MS document");
				((MiapeMsiDocumentImpl) msiDocument).setReferencedMSDocument(Integer.valueOf(id_ms));
			}
			log.info("MIAPE created");
			log.info("Storing document MSI");
			log.info("Converting document to xml");

			if (!storeInRepository) {
				id_msi = LocalFilesIndex.getInstance().getFreeIndex();
				msiDocument.setId(id_msi);
				MiapeXmlFile<MiapeMSIDocument> msiDocumentXML = msiDocument.toXml();
				saveLocally(id_msi, msiDocumentXML, projectName, "MSI", FilenameUtils.getBaseName(tsvFileURL));
				identifiers[1] = String.valueOf(id_msi);
				LocalFilesIndex.getInstance().indexFileByMiapeID(id_msi, tsvFile);
			} else {
				log.info("Converting document to bytes");
				final byte[] msiBytes = msiDocument.toXml().toBytes();
				log.info("Sending bytes to webservice");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Storing MIAPE in the ProteoRed MIAPE repository\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Waiting for server response...\n");
				swingWorker.firePropertyChange(MiapeExtractionTask.MIAPE_CREATION_SENDING_MIAPE_TO_SERVER, null, idJob);

				id_msi = miapeAPIWebservice.storeMiapeMSI(userName, password, msiBytes);
				log.info("MIAPE MSI stored ID:" + id_msi);
				swingWorker.firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"MIAPE MSI stored ID:" + id_msi + "\n");

				if (id_ms > 0) {
					identifiers[0] = String.valueOf(id_ms);
				} else {
					identifiers[0] = "";
				}
				if (id_msi > 0)
					identifiers[1] = String.valueOf(id_msi);
			}
			return identifiers;
		} catch (Exception e) {
			log.error(e);
			sb.append(e.getMessage());
			e.printStackTrace();
		}
		identifiers[0] = "error: " + sb.toString();
		return identifiers;
	}
}
