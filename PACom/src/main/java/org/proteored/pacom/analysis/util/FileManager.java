package org.proteored.pacom.analysis.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.exceptions.MiapeDatabaseException;
import org.proteored.miapeapi.exceptions.MiapeSecurityException;
import org.proteored.miapeapi.exceptions.WrongXMLFormatException;
import org.proteored.miapeapi.experiment.msi.MiapeMSIFiltered;
import org.proteored.miapeapi.interfaces.msi.MiapeMSIDocument;
import org.proteored.miapeapi.interfaces.xml.MiapeXmlFile;
import org.proteored.miapeapi.xml.ms.MIAPEMSXmlFile;
import org.proteored.miapeapi.xml.ms.MiapeMSXmlFactory;
import org.proteored.miapeapi.xml.msi.MIAPEMSIXmlFile;
import org.proteored.pacom.analysis.conf.jaxb.CPExperiment;
import org.proteored.pacom.analysis.conf.jaxb.CPExperimentList;
import org.proteored.pacom.analysis.conf.jaxb.CPMS;
import org.proteored.pacom.analysis.conf.jaxb.CPMSI;

import edu.scripps.yates.annotations.uniprot.UniprotProteinLocalRetriever;

public class FileManager {
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private static final String USER_DATA_FOLDER_NAME = "user_data";
	private static final String PROJECTS_DATA_FOLDER_NAME = "projects";
	private static final String MIAPE_DATA_FOLDER_NAME = "miape_data";
	private static final String MIAPE_LOCA_DATA_FOLDER_NAME = "local_datasets";
	private static final String METADATA_FOLDER_NAME = "ms_metadata_templates";
	private static final String CURATED_EXPERIMENTS_FOLDER_NAME = "curated_exps";
	// private static final String MANUAL_ID_SETS_FOLDER_NAME =
	// "manual_id_sets";
	public static final String PATH_SEPARATOR = System.getProperty("file.separator");

	private static final String MIAPE_LOCAL_PREFIX = "Dataset_";

	public static final String MIAPE_MSI_LOCAL_PREFIX = MIAPE_LOCAL_PREFIX + "MSI_";
	private static final String MIAPE_MS_LOCAL_PREFIX = MIAPE_LOCAL_PREFIX + "MS_";

	private static final String UNIPROT_FOLDER = "uniprot";

	private static UniprotProteinLocalRetriever upr;

	/**
	 * Gets the folder (creating it if doesn't exist) APP_FOLDER/user_data/
	 *
	 */
	public static String getUserDataPath() {
		String dir = System.getProperty("user.dir");

		// check if the user_data folder has been previously created or not
		File userDataFolder = new File(dir + PATH_SEPARATOR + FileManager.USER_DATA_FOLDER_NAME);
		// if it doesn't exist, create folder: APP_PATH\USER_DATA_FOLDER_NAME
		if (!userDataFolder.exists()) {
			boolean created = userDataFolder.mkdirs();
			if (!created)
				return null;
		}
		return userDataFolder.getAbsolutePath() + PATH_SEPARATOR;

	}

	/**
	 * Gets the folder (creating it if doesn't exist)
	 * APP_FOLDER/user_data/miape_data/
	 *
	 */
	public static String getMiapeDataPath() {
		String dir = System.getProperty("user.dir");

		// check if the miape_data folder has been previously created or not
		File miapeDataFolder = new File(dir + PATH_SEPARATOR + FileManager.USER_DATA_FOLDER_NAME + PATH_SEPARATOR
				+ FileManager.MIAPE_DATA_FOLDER_NAME);
		// if it doesn't exist, create folder: APP_PATH\USER_DATA_FOLDER_NAME
		if (!miapeDataFolder.exists()) {
			boolean created = miapeDataFolder.mkdirs();
			if (!created)
				return null;
		}
		return miapeDataFolder.getAbsolutePath() + PATH_SEPARATOR;

	}

	/**
	 * Gets the folder (creating it if doesn't exist)
	 * APP_FOLDER/user_data/miape_local_data/
	 *
	 * @param projectName
	 *
	 */
	public static String getMiapeLocalDataPath() {
		String dir = System.getProperty("user.dir");

		// check if the miape_data folder has been previously created or not
		File miapeLocalDataFolder = new File(dir + PATH_SEPARATOR + FileManager.USER_DATA_FOLDER_NAME + PATH_SEPARATOR
				+ FileManager.MIAPE_LOCA_DATA_FOLDER_NAME);
		// if it doesn't exist, create folder: APP_PATH\USER_DATA_FOLDER_NAME
		if (!miapeLocalDataFolder.exists()) {
			boolean created = miapeLocalDataFolder.mkdirs();
			if (!created)
				return null;
		}

		return miapeLocalDataFolder.getAbsolutePath() + PATH_SEPARATOR;

	}

	/**
	 * Gets the folder (creating it if doesn't exist)
	 * APP_FOLDER/user_data/miape_local_data/projectName/
	 *
	 */
	public static String getMiapeLocalDataPath(String projectName) {
		String dir = System.getProperty("user.dir");
		// check if the miape_data folder has been previously created or not
		File miapeLocalDataFolder = new File(dir + PATH_SEPARATOR + FileManager.USER_DATA_FOLDER_NAME + PATH_SEPARATOR
				+ FileManager.MIAPE_LOCA_DATA_FOLDER_NAME + PATH_SEPARATOR + projectName);
		// if it doesn't exist, create folder: APP_PATH\USER_DATA_FOLDER_NAME
		if (!miapeLocalDataFolder.exists()) {
			boolean created = miapeLocalDataFolder.mkdirs();
			if (!created) {
				return null;
			}
		}
		String string = miapeLocalDataFolder.getAbsolutePath() + PATH_SEPARATOR;
		return string;

	}

	public static List<String> getLocalMiapesByProjectName(String projectName) {
		List<String> ret = new ArrayList<String>();
		final String localProjectPath = getMiapeLocalDataPath(projectName);
		File localProjectFolder = new File(localProjectPath);
		if (localProjectFolder.exists()) {
			for (File manualIdSetFile : localProjectFolder.listFiles()) {
				if (manualIdSetFile.isFile()) {
					if (isMiapeFile(manualIdSetFile)) {
						ret.add(FilenameUtils.getBaseName(manualIdSetFile.getAbsolutePath()));
					}
				}
			}
		}
		if (ret.isEmpty()) {
			boolean deleted = localProjectFolder.delete();
			log.info("Folder " + localProjectFolder.getAbsolutePath() + " deleted " + deleted);
		}

		return ret;
	}

	private static boolean isMiapeFile(File manualIdSetFile) {
		if (manualIdSetFile.exists()) {
			String name = FilenameUtils.getBaseName(manualIdSetFile.getName());
			if (name.startsWith(MIAPE_LOCAL_PREFIX)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the folder (creating it if doesn't exist)
	 * APP_FOLDER/user_data/projects/
	 *
	 * @param userName
	 * @return
	 */
	public static String getProjectsDataPath() {

		String userDataPath = FileManager.getUserDataPath();
		File projectFolder = new File(userDataPath + PROJECTS_DATA_FOLDER_NAME);
		if (!projectFolder.exists()) {
			final boolean created = projectFolder.mkdirs();
			if (!created)
				return null;
		}
		return projectFolder.getAbsolutePath() + PATH_SEPARATOR;
	}

	/**
	 * Gets the folder (creating it if doesn't exist)
	 * APP_FOLDER/user_data/curated_exps/
	 *
	 * @param userName
	 * @return
	 */
	public static String getCuratedExperimentsDataPath() {

		String userDataPath = FileManager.getUserDataPath();
		File curatedExpsFolder = new File(userDataPath + CURATED_EXPERIMENTS_FOLDER_NAME);
		if (!curatedExpsFolder.exists()) {
			final boolean created = curatedExpsFolder.mkdirs();
			if (!created)
				return null;
		}
		return curatedExpsFolder.getAbsolutePath() + PATH_SEPARATOR;
	}

	/**
	 * Gets the full file path APP_FOLDER/user_data/projects/PROJECT_NAME.XML
	 *
	 * @return
	 */
	public static String getProjectXMLFilePath(String projectName) {

		String projectsDataPath = FileManager.getProjectsDataPath();
		if (projectsDataPath != null)
			return projectsDataPath + projectName + ".xml";
		return null;
	}

	/**
	 * Remove the project XML file if found as
	 * APP_FOLDER/user_data/projects/PROJECT_NAME.XML
	 *
	 * @param projectName
	 * @return
	 */
	public static boolean removeProjectXMLFile(String projectName) {
		String projectsDataPath = FileManager.getProjectsDataPath();
		if (projectsDataPath != null) {
			String name = projectsDataPath + projectName + ".xml";
			File xmlFile = new File(name);
			if (xmlFile.exists()) {
				boolean deleted = xmlFile.delete();
				log.info("Project XML file " + projectName + " deleted: " + deleted);
				return deleted;
			}
		}
		return false;
	}

	/**
	 * Gets the full file path
	 * APP_FOLDER/user_data/curated_exps/projectName/projectName.XML
	 *
	 * @return
	 */
	public static String getCuratedExperimentXMLFilePath(String curatedExperimentName) {

		String curatedExperimentsDataPath = FileManager.getCuratedExperimentsDataPath();
		if (curatedExperimentsDataPath != null)
			return curatedExperimentsDataPath + curatedExperimentName + PATH_SEPARATOR + curatedExperimentName + ".xml";
		return null;
	}

	/**
	 * Gets the full file path APP_FOLDER/user_data/curated_exps/projectName/
	 *
	 * @return
	 */
	public static String getCuratedExperimentFolderPath(String curatedExperimentName) {

		String curatedExperimentsDataPath = FileManager.getCuratedExperimentsDataPath();
		if (curatedExperimentsDataPath != null)
			return curatedExperimentsDataPath + curatedExperimentName + PATH_SEPARATOR;
		return null;
	}

	// /**
	// * Gets the full file path
	// APP_FOLDER/user_data/miape_data/MIAPE_MSI_??.XML
	// *
	// * @return
	// */
	// public static String getMiapeMSIXMLFilePath(int miapeID) {
	//
	// String projectsDataPath = FileManager.getMiapeDataPath();
	// if (projectsDataPath != null)
	// return projectsDataPath + getMiapeMSIFileName(miapeID);
	//
	// return null;
	// }
	/**
	 * Gets the full file path
	 * APP_FOLDER/user_data/curated_exps/curated_exp_name/MIAPE_MSI_??.XML
	 *
	 * @param fullFileName
	 *
	 * @return
	 */
	public static String getMiapeMSICuratedXMLFilePathFromFullName(String projectName, String fullFileName) {

		String path = FileManager.getCuratedExperimentFolderPath(projectName);
		// String name = getMiapeMSILocalFileName(miapeLocalID, fileName);
		// final String finalFileName = path + name;
		final String finalFileName = path + FilenameUtils.getBaseName(fullFileName) + ".xml";
		return finalFileName;
	}

	/**
	 * Gets the full file path
	 * APP_FOLDER/user_data/curated_exps/curated_exp_name/MIAPE_MSI_??_fileName.XML
	 *
	 * @param miapeName
	 *
	 * @return
	 */
	public static String getMiapeMSICuratedXMLFilePathFromMiapeInformation(CPMSI cpMsi) {
		return getMiapeMSICuratedXMLFilePathFromMiapeInformation(cpMsi.getLocalProjectName(), cpMsi.getName());
	}

	public static String getMiapeMSICuratedXMLFilePathFromMiapeInformation(String projectName, String fileName) {
		String path = FileManager.getCuratedExperimentFolderPath(projectName);
		final String finalFileName = path + FilenameUtils.getBaseName(fileName) + ".xml";
		// final String finalFileName = path + MIAPE_MSI_LOCAL_PREFIX +
		// FilenameUtils.getBaseName(fileName) + ".xml";
		return finalFileName;

	}

	// /**
	// * Gets the full file path APP_FOLDER/user_data/miape_data/MIAPE_MS_??.XML
	// *
	// * @return
	// */
	// public static String getMiapeMSXMLFilePath(int miapeID) {
	//
	// String projectsDataPath = FileManager.getMiapeDataPath();
	// if (projectsDataPath != null)
	// return projectsDataPath + MIAPE_MS_PREFIX + miapeID + ".xml";
	// else {
	// projectsDataPath = FileManager.getMiapeDataPath();
	// if (projectsDataPath != null)
	// return projectsDataPath + getMiapeMSFileName(miapeID);
	// }
	// return null;
	// }

	// public static String getMiapeMSIFileName(int miapeID) {
	// return MIAPE_MSI_PREFIX + miapeID + ".xml";
	// }
	//
	// public static String getMiapeMSFileName(int miapeID) {
	// return MIAPE_MS_PREFIX + miapeID + ".xml";
	// }

	public static String getMiapeMSILocalFileName(int miapeID, String name) {

		String string = MIAPE_MSI_LOCAL_PREFIX + miapeID;
		if (name != null) {
			string += "_" + name;
		}
		string += ".xml";
		return string;
	}

	public static Integer getMiapeMSIdentifierFromName(String name) {
		if (name.startsWith(MIAPE_MS_LOCAL_PREFIX)) {
			try {

				String substring = name.substring(name.indexOf(MIAPE_MS_LOCAL_PREFIX) + MIAPE_MS_LOCAL_PREFIX.length());
				Integer ret = Integer.valueOf(substring);
				return ret;
			} catch (NumberFormatException e) {

			}
		}
		return null;
	}

	public static Integer getMiapeMSIIdentifierFromName(String name) {
		if (name.startsWith(MIAPE_MSI_LOCAL_PREFIX)) {
			try {
				String substring = name
						.substring(name.indexOf(MIAPE_MSI_LOCAL_PREFIX) + MIAPE_MSI_LOCAL_PREFIX.length());
				if (substring.contains("_")) {
					substring = substring.substring(0, substring.indexOf("_"));
				}
				Integer ret = Integer.valueOf(substring);
				return ret;
			} catch (NumberFormatException e) {

			}
		}
		return null;
	}

	public static String getMiapeMSLocalFileName(int miapeID) {

		String string = MIAPE_MS_LOCAL_PREFIX + miapeID + ".xml";
		return string;

	}

	/**
	 * Gets a reference to a project file. It is doens't exist, creates it
	 *
	 * @param cpExperimentList
	 * @return
	 * @throws JAXBException
	 */
	public static File saveProjectFile(CPExperimentList cpExperimentList) throws JAXBException {
		log.info("saving cfg file for experiment list: " + cpExperimentList.getName());
		File xmlFile = new File(FileManager.getProjectXMLFilePath(cpExperimentList.getName()));
		JAXBContext jc = JAXBContext.newInstance("org.proteored.pacom.analysis.conf.jaxb");
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
		try {
			marshaller.setProperty("com.sun.xml.bind.indentString", "\t");
		} catch (PropertyException e) {
			marshaller.setProperty("com.sun.xml.internal.bind.indentString", "\t");
		}
		marshaller.marshal(cpExperimentList, xmlFile);
		log.info("project file saved at: " + xmlFile.getAbsolutePath());
		return xmlFile;
	}

	public static File saveCuratedExperimentFile(CPExperiment cpExperiment) throws JAXBException {
		log.info("saving cfg file for experiment: " + cpExperiment.getName());
		File xmlFile = new File(FileManager.getCuratedExperimentXMLFilePath(cpExperiment.getName()));
		JAXBContext jc = JAXBContext.newInstance("org.proteored.pacom.analysis.conf.jaxb");
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
		try {
			marshaller.setProperty("com.sun.xml.bind.indentString", "\t");
		} catch (PropertyException e) {
			marshaller.setProperty("com.sun.xml.internal.bind.indentString", "\t");
		}
		marshaller.marshal(cpExperiment, xmlFile);
		log.info("curated experiment file saved at: " + xmlFile.getAbsolutePath());
		return xmlFile;
	}

	/**
	 * Gets a reference to a project file.
	 *
	 * @param cpExperimentList
	 * @return
	 * @throws JAXBException
	 */
	public static File getProjectFile(String name) {
		log.info("getting cfg file: " + name);
		File xmlFile = new File(FileManager.getProjectXMLFilePath(name));
		return xmlFile;
	}

	/**
	 * Get a list of project names looking at /APP_FOLDER/user_data/projects/
	 *
	 * @return
	 */
	public static List<String> getProjectList() {
		List<String> ret = new ArrayList<String>();
		final String projectsFolderName = getProjectsDataPath();
		log.info(projectsFolderName);
		File projectsFolder = new File(projectsFolderName);
		if (projectsFolder.exists()) {
			for (File projectFile : projectsFolder.listFiles()) {
				if (projectFile.isFile()) {
					ret.add(FilenameUtils.getBaseName(projectFile.getAbsolutePath()));
				}
				// File projectFile = new File(projectsFolderName +
				// pathSeparator + projectFileName);
				// try {
				// ExperimentListAdapter expList = new
				// ExperimentListAdapter(projectFile);
				// ret.add(expList.getCpExperimentList().getName());
				// } catch (IllegalArgumentException e) {
				// log.warn(e.getMessage());
				// }

			}
		}
		if (ret.isEmpty()) {
			boolean deleted = projectsFolder.delete();
			log.info("Folder " + projectsFolder.getAbsolutePath() + " deleted " + deleted);
			// final File userFolder = projectsFolder.getParentFile();
			// deleted = userFolder.delete();
			// log.info("Folder " + userFolder.getAbsolutePath() + " deleted " +
			// deleted);
		}

		return ret;
	}

	/**
	 * Get a list of curated experiments names looking at
	 * /APP_FOLDER/user_data/curated_exps/
	 *
	 * @return
	 */
	public static List<String> getCuratedExperimentList() {
		List<String> ret = new ArrayList<String>();
		final String curatedExperimentFolderName = getCuratedExperimentsDataPath();
		log.info(curatedExperimentFolderName);
		File curatedExperimentsFolder = new File(curatedExperimentFolderName);
		if (curatedExperimentsFolder.exists()) {
			for (File curatedExperimentFolder : curatedExperimentsFolder.listFiles()) {
				if (curatedExperimentFolder.isDirectory())
					ret.add(FilenameUtils.getBaseName(curatedExperimentFolder.getAbsolutePath()));
			}
		}
		if (ret.isEmpty()) {
			boolean deleted = curatedExperimentsFolder.delete();
			log.info("Folder " + curatedExperimentsFolder.getAbsolutePath() + " deleted " + deleted);
		}

		return ret;
	}

	public static boolean existsProjectXMLFile(String name) {
		final String projectXMLFilePath = getProjectXMLFilePath(name);
		File file = new File(projectXMLFilePath);
		return file.exists();
	}

	public static boolean existsCuratedExperimentXMLFile(String name) {
		final String curatedExpXMLFilePath = getCuratedExperimentXMLFilePath(name);
		File file = new File(curatedExpXMLFilePath);
		return file.exists();
	}

	private static String removeFileExtension(String fileName, String extension) {
		String ret = fileName;
		if (ret.endsWith(extension)) {
			ret = ret.substring(0, ret.indexOf(extension));
		}
		return ret;
	}

	public static List<String> getMetadataList(ControlVocabularyManager cvManager) {
		List<String> ret = new ArrayList<String>();
		final String metadataFolderName = getMetadataFolder();
		File metadataFolder = new File(metadataFolderName);
		if (metadataFolder.exists()) {
			for (String metadataFileName : metadataFolder.list()) {
				File metadataFile = new File(metadataFolderName + PATH_SEPARATOR + metadataFileName);
				try {
					MiapeMSXmlFactory.getFactory().toDocument(new MIAPEMSXmlFile(metadataFile), cvManager, null, null,
							null);
					// if not exception, the miape ms is valid
					String name = removeFileExtension(metadataFileName, ".xml");

					ret.add(name);
				} catch (IllegalArgumentException e) {
					log.warn(e.getMessage());
				} catch (WrongXMLFormatException e) {
					log.warn(e.getMessage());
				} catch (MiapeDatabaseException e) {
					log.warn(e.getMessage());
					e.printStackTrace();
				} catch (MiapeSecurityException e) {
					log.warn(e.getMessage());
					e.printStackTrace();
				}

			}
		}
		if (ret.isEmpty()) {
			boolean deleted = metadataFolder.delete();
			log.info("Folder " + metadataFolder.getAbsolutePath() + " deleted " + deleted);
			// final File userFolder = metadataFolder.getParentFile();
			// deleted = userFolder.delete();
			// log.info("Folder " + userFolder.getAbsolutePath() + " deleted " +
			// deleted);
		}

		return ret;
	}

	public static String getMetadataFolder() {
		String dir = System.getProperty("user.dir");

		// check if the miape_data folder has been previously created or not
		File metadataFolder = new File(dir + PATH_SEPARATOR + FileManager.USER_DATA_FOLDER_NAME + PATH_SEPARATOR
				+ FileManager.METADATA_FOLDER_NAME);
		// if it doesn't exist, create folder: APP_PATH\USER_DATA_FOLDER_NAME
		if (!metadataFolder.exists()) {
			boolean created = metadataFolder.mkdirs();
			if (!created)
				return null;
		}
		return metadataFolder.getAbsolutePath() + PATH_SEPARATOR;
	}

	public static File getMetadataFile(String fileName) {

		String metadataDataPath = FileManager.getMetadataFolder();
		if (metadataDataPath != null) {
			File file = new File(metadataDataPath + fileName + ".xml");
			if (file.exists() && file.isFile())
				return file;
		}
		return null;
	}

	public static boolean deleteMetadataFile(String fileName) {

		String metadataDataPath = FileManager.getMetadataFolder();
		if (metadataDataPath != null) {
			File file = new File(metadataDataPath + fileName + ".xml");
			if (file.exists())
				return file.delete();
		}
		return true;
	}

	public static String saveCuratedMiapeMSI(String expName, MiapeMSIFiltered miapeMSIFiltered, String miapeName)
			throws IOException {
		log.info("saving curated MIAPE MSI:" + miapeMSIFiltered.getName());
		final String msiFilePath = FileManager.getMiapeMSICuratedXMLFilePathFromMiapeInformation(expName, miapeName);
		final File file = new File(msiFilePath);
		if (!file.exists())
			file.mkdirs();
		final MiapeXmlFile<MiapeMSIDocument> xml = miapeMSIFiltered.toXml();
		xml.saveAs(msiFilePath);
		return msiFilePath;
	}

	public static CPExperimentList getCPExperimentList(File confFile) {
		try {
			JAXBContext jc = JAXBContext.newInstance("org.proteored.pacom.analysis.conf.jaxb");
			CPExperimentList cpExperimentList = (CPExperimentList) jc.createUnmarshaller().unmarshal(confFile);
			return cpExperimentList;
		} catch (JAXBException e) {
			log.warn(e.getMessage());
			return null;
		}
	}

	public static CPExperiment getCPExperiment(File confFile) {
		try {
			JAXBContext jc = JAXBContext.newInstance("org.proteored.pacom.analysis.conf.jaxb");
			CPExperiment cpExperiment = (CPExperiment) jc.createUnmarshaller().unmarshal(confFile);
			return cpExperiment;
		} catch (JAXBException e) {
			log.warn(e.getMessage());
			return null;
		}
	}

	/**
	 * Removes the entire folder containing the curated experiment files.
	 *
	 * @param curatedExperimentName
	 * @return
	 */
	public static boolean removeCuratedExperimentFiles(String curatedExperimentName) {
		File curatedExpFolder = new File(FileManager.getCuratedExperimentFolderPath(curatedExperimentName));
		if (curatedExpFolder.exists() && curatedExpFolder.isDirectory()) {
			try {
				FileUtils.deleteDirectory(curatedExpFolder);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/**
	 * gets
	 * APP_FOLDER/user_data/miape_local_data/project_Name/LOCAL_MIAPE_MSI_id.xml
	 *
	 * @param miapeLocalID
	 * @param projectName
	 * @return
	 */
	public static String getMiapeMSIXMLFileLocalPathFromMiapeInformation(CPMSI cpMSI) {

		return getMiapeXMLFileLocalPathFromFullName(cpMSI.getLocalProjectName(), cpMSI.getName());
	}

	/**
	 * gets
	 * APP_FOLDER/user_data/miape_local_data/project_Name/LOCAL_MIAPE_MS_id.xml
	 *
	 * @param miapeLocalID
	 * @param projectName
	 * @return
	 */
	public static String getMiapeMSXMLFileLocalPathFromMiapeInformation(CPMS cpMS) {

		return getMiapeXMLFileLocalPathFromFullName(cpMS.getLocalProjectName(), cpMS.getName());
	}

	/**
	 * gets
	 * APP_FOLDER/user_data/miape_local_data/project_Name/LOCAL_MIAPE_MSI_id.xml
	 *
	 * @param miapeLocalID
	 * @param projectName
	 * @return
	 */
	public static String getMiapeMSIXMLFileLocalPathFromMiapeInformation(String projectName, int miapeLocalID,
			String fileName) {

		String path = FileManager.getMiapeLocalDataPath(projectName);
		final String finalFileName = path + MIAPE_MSI_LOCAL_PREFIX + miapeLocalID + "_"
				+ FilenameUtils.getBaseName(fileName) + ".xml";
		return finalFileName;
	}

	/**
	 * gets
	 * APP_FOLDER/user_data/miape_local_data/project_Name/LOCAL_MIAPE_MSI_id.xml
	 *
	 * @param miapeLocalID
	 * @param projectName
	 * @return
	 */
	public static String getMiapeMSXMLFileLocalPathFromMiapeInformation(String projectName, int miapeLocalID) {

		String path = FileManager.getMiapeLocalDataPath(projectName);
		final String finalFileName = path + MIAPE_MS_LOCAL_PREFIX + miapeLocalID + ".xml";
		return finalFileName;
	}

	/**
	 * gets
	 * APP_FOLDER/user_data/miape_local_data/project_Name/LOCAL_MIAPE_MSI_id.xml
	 *
	 * @param miapeLocalID
	 * @param projectName
	 * @return
	 */
	public static String getMiapeXMLFileLocalPathFromFullName(String projectName, String fileName) {
		if (projectName == null || "".equals(projectName)) {
			throw new IllegalMiapeArgumentException(
					"'local_project_name' attribute in project is null or empty. Project file seems to be malformed");
		}
		String path = getMiapeLocalDataPath(projectName);
		// String name = getMiapeMSILocalFileName(miapeLocalID, fileName);
		// final String finalFileName = path + name;
		final String finalFileName = path + FilenameUtils.getBaseName(fileName) + ".xml";
		return finalFileName;
	}

	/**
	 * gets
	 * APP_FOLDER/user_data/miape_local_data/project_Name/LOCAL_MIAPE_MSI_id.xml
	 *
	 * @param miapeLocalID
	 * @param projectName
	 * @return
	 */
	public static String getCuratedMiapeXMLFileLocalPathFromFullName(String projectName, String fileName) {

		String path = FileManager.getCuratedExperimentFolderPath(projectName);
		// String name = getMiapeMSILocalFileName(miapeLocalID, fileName);
		// final String finalFileName = path + name;
		final String finalFileName = path + FilenameUtils.getBaseName(fileName) + ".xml";
		return finalFileName;
	}

	/**
	 *
	 * gets
	 * APP_FOLDER/user_data/miape_local_data/project_Name/LOCAL_MIAPE_MS_id.xml
	 *
	 * @param miapeLocalID
	 * @param projectName
	 * @return
	 */
	public static String getMiapeMSXMLFileLocalPath(int miapeLocalID, String projectName) {
		String path = FileManager.getMiapeLocalDataPath(projectName);
		String name = getMiapeMSLocalFileName(miapeLocalID);
		final String finalFileName = path + name;

		return finalFileName;
	}

	/**
	 * Saves the miape file in a folder like:
	 * APP_FOLDER/user_data/miape_local_data/project_Name/LOCAL_MIAPE_MSI_id.xml
	 *
	 * @param miapeID
	 * @param miapeXML
	 * @param projectName
	 * @throws IOException
	 */
	public static File saveLocalMiapeMSI(int miapeID, MIAPEMSIXmlFile miapeXML, String projectName, String dataName)
			throws IOException {
		log.info("saving local  MIAPE MSI:");
		String finalFileName = getMiapeMSIXMLFileLocalPathFromMiapeInformation(projectName, miapeID, dataName);
		// String name = getMiapeMSILocalFileName(miapeID, dataName);
		// String path = FileManager.getMiapeLocalDataPath(projectName);
		// final String finalFileName = path + name;
		return miapeXML.saveAs(finalFileName);

	}

	/**
	 * Saves the miape file in a folder like:
	 * APP_FOLDER/user_data/miape_local_data/project_Name/LOCAL_MIAPE_MS_id.xml
	 *
	 * @param miapeID
	 * @param miapeXML
	 * @param projectName
	 * @throws IOException
	 */
	public static File saveLocalMiapeMS(int miapeID, MIAPEMSXmlFile miapeXML, String projectName) throws IOException {
		log.info("saving local MIAPE MS");

		String finalFileName = getMiapeMSXMLFileLocalPathFromMiapeInformation(projectName, miapeID);
		// String name = getMiapeMSLocalFileName(miapeID, dataName);
		// String path = FileManager.getMiapeLocalDataPath(projectName);
		// final String finalFileName = path + name;
		return miapeXML.saveAs(finalFileName);

	}

	/**
	 * Saves the file locally.<br>
	 * Note that this doesn't index the file.
	 *
	 * @param miapeID
	 * @param file
	 * @param projectName
	 * @return
	 * @throws IOException
	 */
	public static File saveLocalFile(File file, String projectName) throws IOException {
		if (file != null) {
			log.info("Saving local file " + file.getAbsolutePath());
			String fileName = FilenameUtils.getBaseName(file.getAbsolutePath());
			String extension = FilenameUtils.getExtension(file.getAbsolutePath());

			String pathname = getMiapeLocalDataPath(projectName) + fileName;
			if (extension != null && !"".equals(extension)) {
				pathname += "." + extension;
			}
			File outputFile = new File(pathname);
			log.info("Copying file from " + file.getAbsolutePath() + " to " + outputFile.getAbsolutePath());
			FileUtils.copyFile(file, outputFile);
			log.info("file copied : " + outputFile.getAbsolutePath() + " length " + outputFile.length());
			log.info("File "
					+ edu.scripps.yates.utilities.files.FileUtils.getDescriptiveSizeFromBytes(outputFile.length())
					+ " copied to " + outputFile.getAbsolutePath());
			return outputFile;
		}
		String message = "file to copy is null";
		log.warn(message);
		throw new FileNotFoundException(message);
	}

	/**
	 * Saves the file locally.<br>
	 * Note that this doesn't index the file.
	 *
	 * @param miapeID
	 * @param file
	 * @param projectName
	 * @return
	 * @throws IOException
	 */
	public static File saveLocalFile(String fileName, String projectName) throws IOException {
		return saveLocalFile(new File(fileName), projectName);
	}

	/**
	 * Gets the list of folders corresponding to local miape projects located at
	 * APP_PATH/local_miape_files/
	 *
	 * @return
	 */
	public static List<String> getlocalMIAPEProjects() {
		List<String> ret = new ArrayList<String>();
		File miapeLocalFolder = new File(FileManager.getMiapeLocalDataPath());
		if (miapeLocalFolder.isDirectory()) {
			final File[] projectFolders = miapeLocalFolder.listFiles();

			if (projectFolders != null) {
				for (File projectFolder : projectFolders) {
					if (projectFolder.isDirectory()) {
						ret.add(FilenameUtils.getName(projectFolder.getAbsolutePath()));
					}
				}
			}
		}
		// sort in alphabetic order
		Collections.sort(ret);
		return ret;
	}

	public static File getUniprotFolder() {
		String dir = System.getProperty("user.dir");

		// check if the miape_data folder has been previously created or not
		File ret = new File(
				dir + PATH_SEPARATOR + FileManager.USER_DATA_FOLDER_NAME + PATH_SEPARATOR + FileManager.UNIPROT_FOLDER);
		// if it doesn't exist, create folder: APP_PATH\USER_DATA_FOLDER_NAME
		if (!ret.exists()) {
			boolean created = ret.mkdirs();
			if (!created)
				return null;
		}
		return ret;
	}

	public static UniprotProteinLocalRetriever getUniprotProteinLocalRetriever() {
		if (upr == null) {
			File uniprotReleasesFolder = FileManager.getUniprotFolder();
			upr = new UniprotProteinLocalRetriever(uniprotReleasesFolder, true);
		}
		return upr;
	}

}
