package org.proteored.miapeExtractor.client;

import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.proteored.miapeExtractor.gui.tasks.WebservicesLoaderTask;
import org.proteored.miapeapi.interfaces.Project;
import org.proteored.miapeapi.webservice.clients.miapeapi.IntegerString;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeDatabaseException_Exception;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeSecurityException_Exception;
import org.proteored.miapeapi.webservice.clients.miapeextractor.MiapeExtractorDelegate;
import org.proteored.miapeapi.xml.ProjectXmlFactory;
import org.proteored.miapeapi.xml.miapeproject.ProjectXmlFile;
import org.proteored.miapeapi.zip.ZipManager;

public class PRIDEGenerationTest {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	@Test
	public void prideGeneration() {
		final WebservicesLoaderTask initializeWebservicesTask = WebservicesLoaderTask
				.getInstace();
		final MiapeAPIWebserviceDelegate miapeAPIWebservice = initializeWebservicesTask
				.getMiapeAPIWebservice(true);
		final MiapeExtractorDelegate miapeExtractorWebservice = initializeWebservicesTask
				.getMiapeExtractorWebservice(true);
		final String userName = "sgharbi@cnb.csic.es";
		final String pass = "TripleTofSev";
		int[] projectList = { 902, 903, 896 };
		try {
			for (int idProject : projectList) {

				final List<IntegerString> miapesByProjectID = miapeAPIWebservice
						.getMiapesByProjectID(idProject, userName, pass);
				final byte[] projectById = miapeAPIWebservice.getProjectById(
						idProject, userName, pass);
				final Project project = ProjectXmlFactory.getFactory()
						.toProject(new ProjectXmlFile(projectById), null,
								userName, pass);
				int i = 1;
				List<Integer> ids = new ArrayList<Integer>();

				for (IntegerString integerString : miapesByProjectID) {
					if (integerString.getMiapeType().equals("MSI"))
						ids.add(integerString.getMiapeID());
				}
				java.util.Collections.sort(ids);
				for (Integer miapeID : ids) {
					final int associatedMiapeMS = miapeExtractorWebservice
							.getAssociatedMiapeMS(miapeID, userName, pass);
					if (associatedMiapeMS > 0) {

						final String prideFromMiapeMSMSI = miapeExtractorWebservice
								.getPRIDEFromMiapeMSMSI(associatedMiapeMS,
										miapeID, userName, pass, true, false);
						final String outputName = "Z:\\SERVICIOS\\PROTEOMICACOMPUTACIONAL\\HPP\\PRIDES_HPP_JPR_Sept12_bis\\"
								+ project.getName() + "-" + i++ + ".xml.gz";
						receiveFile(prideFromMiapeMSMSI, outputName);
						final File gzipFile = new File(outputName);
						final File decompressGZipFile = ZipManager
								.decompressGZipFileIfNeccessary(gzipFile);
						gzipFile.delete();
						log.info("File created at: " + decompressGZipFile);

					}
				}
			}
			return;
		} catch (MiapeDatabaseException_Exception e) {
			e.printStackTrace();
		} catch (MiapeSecurityException_Exception e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fail();
	}

	private File receiveFile(String urlString, String outputName)
			throws IOException {
		File outPutFile = null;

		URL inputUrl = new URL(urlString);
		String fileName = getFileNameFromPath(inputUrl.getPath());

		log.info("Input URL string: " + urlString);
		log.info("File name = " + fileName);

		outPutFile = new File(outputName);

		// check if the file already exists and if yes, rename with a sufix

		log.info("Output file: " + outPutFile.getAbsolutePath());

		BufferedOutputStream os = new BufferedOutputStream(
				new FileOutputStream(outPutFile));

		log.info("Retrieving File: " + urlString);
		URLConnection urlc = inputUrl.openConnection();
		BufferedInputStream is = new BufferedInputStream(urlc.getInputStream());
		ZipManager.copyInputStream(is, os);

		log.info("Finished Retriving File " + outPutFile.getAbsolutePath());

		return outPutFile;
	}

	private String getFileNameWithPrefix(String fileName, int prefix) {
		String ret = "";
		log.info("Getting file name with prefix from: " + fileName);
		String fileNameWithoutExtension = FilenameUtils
				.removeExtension(fileName);
		String extension = FilenameUtils.getExtension(fileName);

		String fileNameWithoutExtension2 = FilenameUtils
				.removeExtension(fileNameWithoutExtension);
		String extension2 = FilenameUtils
				.getExtension(fileNameWithoutExtension);
		if (!"".equals(extension2)) {
			ret = fileNameWithoutExtension2 + "_(" + prefix + ")." + extension2
					+ "." + extension;
		} else {
			ret = fileNameWithoutExtension + "_(" + prefix + ")." + extension;
		}
		log.info("Returning: " + ret);
		return ret;
	}

	/**
	 * Gets new File(filePath).getName()
	 * 
	 * @param filePath
	 * @return
	 */
	private String getFileNameFromPath(String filePath) {
		log.info("Getting file name from: " + filePath);
		File ifile = new File(filePath);
		String name = ifile.getName();
		log.info("Returning: " + name);
		return name;
	}
}
