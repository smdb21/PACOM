package org.proteored.miapeExtractor.analysis.gui.tasks;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.proteored.miapeExtractor.analysis.util.FileManager;
import org.proteored.miapeapi.exceptions.MiapeDatabaseException;
import org.proteored.miapeapi.exceptions.MiapeSecurityException;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.miapeapi.xml.ge.MIAPEGEXmlFile;
import org.proteored.miapeapi.xml.gi.MIAPEGIXmlFile;
import org.proteored.miapeapi.xml.ms.MIAPEMSXmlFile;
import org.proteored.miapeapi.xml.msi.MIAPEMSIXmlFile;

public class MiapeRetrieverTask extends SwingWorker<Void, String> {
	public static final String MIAPE_LOADER_DONE = "miapeloaderdone";
	public static final String MIAPE_LOADER_ERROR = "miapeloadererror";
	public static final String MESSAGE_SPLITTER = "****";
	public static final String SCAPED_MESSAGE_SPLITTER = "\\*\\*\\*\\*";
	private final int miapeID;

	private final MiapeAPIWebserviceDelegate miapeAPIWebservice;
	private final String userName;
	private final String password;
	private final String miapeType;
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	protected MiapeRetrieverTask(int miapeID, MiapeAPIWebserviceDelegate miapeAPIWebservice, String userName,
			String password, String miapeType) {
		this.miapeID = miapeID;
		this.miapeType = miapeType;
		this.miapeAPIWebservice = miapeAPIWebservice;
		this.userName = userName;
		this.password = password;

	}

	protected int getMiapeID() {
		return miapeID;
	}

	protected String getMiapeType() {
		return miapeType;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		if (!isCancelled()) {
			// notify done
			firePropertyChange(MIAPE_LOADER_DONE, null, miapeID + MESSAGE_SPLITTER + miapeType);
			// log.info("MIAPE " + miapeType + " " + miapeID + " downloaded");
		}
	}

	@Override
	protected Void doInBackground() throws Exception {
		if (miapeAPIWebservice == null || userName == null || password == null)
			return null;
		log.info("Retrieving MIAPE " + miapeType + " " + miapeID);
		final String miapeFilePath = retrieveMIAPE();
		log.info("MIAPE " + miapeType + " " + miapeID + " retrieved in: " + miapeFilePath);
		// File file = new File(miapeMSIFilePath);
		// if (miapeMSIFilePath != null) {
		// // add to the common hahsmap
		// Miape2ExperimentListDialog.addToMiapeRetrievedHashMap(miapeID,
		// file.getName());
		//
		// }
		return null;
	}

	private String retrieveMIAPE() {
		if (miapeAPIWebservice == null)
			return null;

		String error = "";
		try {
			if (miapeType.equals("MSI")) {
				byte[] miapeBytes = miapeAPIWebservice.getMiapeMSIById(miapeID, userName, password);
				if (miapeBytes == null)
					throw new Exception("MIAPE MSI " + miapeID + " doesn't exist in the ProteoRed MIAPE repository");
				MIAPEMSIXmlFile msiFile = new MIAPEMSIXmlFile(miapeBytes);
				final String finalPath = FileManager.getMiapeMSIXMLFileLocalPathFromMiapeInformation(null, miapeID,
						null);
				msiFile.saveAs(finalPath);
				return finalPath;
			} else if (miapeType.equals("MS")) {
				byte[] miapeBytes = miapeAPIWebservice.getMiapeMSById(miapeID, userName, password);
				if (miapeBytes == null)
					throw new Exception("MIAPE MS " + miapeID + " doesn't exist in the ProteoRed MIAPE repository");
				MIAPEMSXmlFile msiFile = new MIAPEMSXmlFile(miapeBytes);
				final String finalPath = FileManager.getMiapeMSXMLFileLocalPath(miapeID, null, null);
				msiFile.saveAs(finalPath);
				return finalPath;
			} else if (miapeType.equals("GE")) {
				byte[] miapeBytes = miapeAPIWebservice.getMiapeGEById(miapeID, userName, password);
				if (miapeBytes == null)
					throw new Exception("MIAPE GE " + miapeID + " doesn't exist in the ProteoRed MIAPE repository");
				MIAPEGEXmlFile msiFile = new MIAPEGEXmlFile(miapeBytes);
				final String finalPath = FileManager.getMiapeGEXMLFilePath(miapeID);
				msiFile.saveAs(finalPath);
				return finalPath;
			} else if (miapeType.equals("GI")) {
				byte[] miapeBytes = miapeAPIWebservice.getMiapeGIById(miapeID, userName, password);
				if (miapeBytes == null)
					throw new Exception("MIAPE GI " + miapeID + " doesn't exist in the ProteoRed MIAPE repository");
				MIAPEGIXmlFile msiFile = new MIAPEGIXmlFile(miapeBytes);
				final String finalPath = FileManager.getMiapeGIXMLFilePath(miapeID);
				msiFile.saveAs(finalPath);
				return finalPath;
			}
		} catch (MiapeSecurityException e) {
			error = e.getMessage();
			log.warn(e.getMessage());
		} catch (MiapeDatabaseException e) {
			error = e.getMessage();
			log.warn(e.getMessage());
		} catch (RemoteException e) {
			error = e.getMessage();
			log.warn(e.getMessage());
		} catch (IOException e) {
			error = e.getMessage();
			log.warn(e.getMessage());
		} catch (Exception e) {
			error = e.getMessage();
			log.warn(e.getMessage());
		}

		// notify done
		firePropertyChange(MIAPE_LOADER_ERROR, null, miapeID + MESSAGE_SPLITTER + miapeType + MESSAGE_SPLITTER + error);

		return null;

	}
}
