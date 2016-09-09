package org.proteored.pacom.gui.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.exceptions.MiapeDatabaseException;
import org.proteored.miapeapi.exceptions.MiapeSecurityException;
import org.proteored.miapeapi.interfaces.msi.MiapeMSIDocument;
import org.proteored.miapeapi.webservice.clients.miapeapi.IntegerString;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.miapeapi.webservice.clients.miapeextractor.MiapeExtractorDelegate;
import org.proteored.miapeapi.xml.msi.MIAPEMSIXmlFile;
import org.proteored.miapeapi.xml.msi.MiapeMSIXmlFactory;
import org.proteored.pacom.analysis.gui.tasks.MiapeRetrieverManager;
import org.proteored.pacom.analysis.gui.tasks.MiapeRetrieverTask;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.analysis.util.MiapeMSAndMSIAssociator;

public class UnattendedMiapeMiapeXMLRetriever extends SwingWorker<Void, Void> implements PropertyChangeListener {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private static UnattendedMiapeMiapeXMLRetriever instance;
	private final MiapeAPIWebserviceDelegate miapeAPIWebservice;
	private final MiapeExtractorDelegate miapeExtractorWebservice;
	private final String userName;
	private final String password;
	private String error;

	public UnattendedMiapeMiapeXMLRetriever(MiapeAPIWebserviceDelegate miapeAPIWebservice,
			MiapeExtractorDelegate miapeExtractorWebservice, String userName, String password) {
		this.miapeAPIWebservice = miapeAPIWebservice;
		this.miapeExtractorWebservice = miapeExtractorWebservice;
		this.userName = userName;
		this.password = password;
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			log.info("MIAPE RETRIVING UNATTENDED HAS BEEN STARTED");
			if (miapeAPIWebservice != null) {
				List<IntegerString> allMiapeMSIs = miapeAPIWebservice.getAllMiapes(userName, password, "MSI");
				List<Integer> msisToRetrieve = new ArrayList<Integer>();
				List<Integer> mssToRetrieve = new ArrayList<Integer>();
				// get a list of identifiers
				List<Integer> msiIdentifiers = new ArrayList<Integer>();
				for (IntegerString integerString : allMiapeMSIs) {
					msiIdentifiers.add(integerString.getMiapeID());
				}
				// sort by number
				java.util.Collections.sort(msiIdentifiers);
				// start by the end

				List<Integer> miapesMSIWithFullPermissions = new ArrayList<Integer>();
				for (int i = msiIdentifiers.size() - 1; i >= 0; i--) {
					// for (int i = 0; i < identifiers.size(); i++) {
					Integer miapeMSIID = msiIdentifiers.get(i);
					log.info(miapeMSIID);
					if (miapeMSIID == 3220)
						log.info("HOLA");
					// check if it is already locally
					String miapeMSIXMLFilePath = FileManager.getMiapeMSIXMLFileLocalPathFromMiapeInformation(null,
							miapeMSIID, null);
					boolean fullPermissions = false;
					File miapeMSIXMLFile = new File(miapeMSIXMLFilePath);
					if (!miapeMSIXMLFile.exists() || miapeMSIXMLFile.lastModified() < Long.valueOf("1349251004048")) {
						// a String with the following structure: "read=value,
						// write=value, delete=value, share=value where value
						// can be
						// 'true' or 'false'
						log.info("Checking permissions of MIAPE MSI: " + miapeMSIID);
						String miapePermissionsString = miapeAPIWebservice.getMiapePermissions(miapeMSIID, "MSI",
								userName, password);
						if (miapePermissionsString != null) {
							if (miapePermissionsString.contains(",")) {
								String[] permissionsSplitted = miapePermissionsString.split(",");
								if (permissionsSplitted.length == 4) {
									if (permissionsSplitted[0].equalsIgnoreCase("read=true")
											&& permissionsSplitted[1].equalsIgnoreCase("write=true")
											&& permissionsSplitted[2].equalsIgnoreCase("delete=true")
											&& permissionsSplitted[3].equalsIgnoreCase("share=true")) {
										fullPermissions = true;
										miapesMSIWithFullPermissions.add(miapeMSIID);
									}
								}
							}
						}
						if (!fullPermissions)
							msisToRetrieve.add(miapeMSIID);
					} else {

						int miapeMSReference = MiapeMSAndMSIAssociator.getInstance().getAssociatedMIAPEMS(miapeMSIID);
						if (miapeMSReference > 0) {
							String miapeMSXMLFilePath = FileManager.getMiapeMSLocalFileName(miapeMSReference, null);
							File miapeMSXMLFile = new File(miapeMSXMLFilePath);
							if (!miapeMSXMLFile.exists()
									|| miapeMSXMLFile.lastModified() < Long.valueOf("1349251004048")) {
								mssToRetrieve.add(miapeMSReference);
							}
						}
					}
				}
				log.info("MIAPE MSIs to retrieve:");
				String tmp = "";
				for (Integer miapeID : miapesMSIWithFullPermissions) {
					tmp = tmp + " " + miapeID;
				}
				for (Integer miapeID : msisToRetrieve) {
					if (!miapesMSIWithFullPermissions.contains(miapeID))
						tmp = tmp + " " + miapeID;
				}
				log.info(tmp);
				tmp = "";
				log.info("MIAPE MSs to retrieve:");
				for (Integer miapeID : mssToRetrieve) {
					tmp = tmp + " " + miapeID;
				}
				log.info(tmp);
				// with full permissions will be downloaded first
				for (Integer miapeID : miapesMSIWithFullPermissions) {
					MiapeRetrieverManager.getInstance(userName, password).addRetrieving(miapeID, "MSI", this);
				}
				for (Integer miapeID : msisToRetrieve) {
					if (!miapesMSIWithFullPermissions.contains(miapeID))
						MiapeRetrieverManager.getInstance(userName, password).addRetrieving(miapeID, "MSI", this);
				}
				for (Integer miapeID : mssToRetrieve) {
					MiapeRetrieverManager.getInstance(userName, password).addRetrieving(miapeID, "MS", this);
				}
			}

		} catch (Exception e) {
			log.info(e.getMessage());
			error = e.getMessage();
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (MiapeRetrieverTask.MIAPE_LOADER_DONE.equals(evt.getPropertyName())) {
			String message = (String) evt.getNewValue();
			if (message.contains(MiapeRetrieverTask.MESSAGE_SPLITTER)) {
				String[] splitted = message.split(MiapeRetrieverTask.SCAPED_MESSAGE_SPLITTER);

				Integer miapeID = Integer.valueOf(splitted[0]);
				String miapeType = splitted[1];
				log.info("MIAPE " + miapeType + " " + miapeID + " retrieved by unattended retriever");
				if (miapeType.equals("MSI")) {
					// if it is a MSI, check if there is a MIAPE MS associated
					// Get Miape MSI Header
					try {
						MiapeMSIDocument document = MiapeMSIXmlFactory.getFactory().toDocument(new MIAPEMSIXmlFile(
								FileManager.getMiapeMSIXMLFileLocalPathFromMiapeInformation(null, miapeID, null)),
								OntologyLoaderTask.getCvManager(), null, null, null);
						int miapeMSReference = document.getMSDocumentReference();
						if (miapeMSReference > 0) {
							String miapeMSXMLFilePath = FileManager.getMiapeMSLocalFileName(miapeMSReference, null);
							File miapeMSXMLFile = new File(miapeMSXMLFilePath);
							if (!miapeMSXMLFile.exists()) {
								MiapeRetrieverManager.getInstance(userName, password).addRetrieving(miapeMSReference,
										"MS", this);
							}
						}
					} catch (MiapeDatabaseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MiapeSecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				log.info(MiapeRetrieverManager.getInstance(userName, password).enumerate());
			}
		} else if (MiapeRetrieverTask.MIAPE_LOADER_ERROR.equals(evt.getPropertyName())) {
			String message = (String) evt.getNewValue();
			if (message.contains(MiapeRetrieverTask.MESSAGE_SPLITTER)) {
				Object[] splitted = message.split(MiapeRetrieverTask.SCAPED_MESSAGE_SPLITTER);

				int miapeID = Integer.valueOf((String) splitted[0]);
				String miapeType = (String) splitted[1];
				String errorMessage = (String) splitted[2];
				log.info("Error downloading MIAPE " + miapeType + " " + miapeID + ": " + errorMessage);
			}

		}

	}

	@Override
	protected void done() {
		if (isCancelled()) {
			log.info("MIAPE MSI unattended retriever CANCELLED");
		} else {
			if (!"".equals(error))
				log.info("MIAPE MSI unattended retriever has launched all MIAPE MSI retrieving: "
						+ MiapeRetrieverManager.getInstance(userName, password).enumerate());
			else
				log.info("MIAPE MSI unattended retriever ERROR: " + error);
		}
	}

}
