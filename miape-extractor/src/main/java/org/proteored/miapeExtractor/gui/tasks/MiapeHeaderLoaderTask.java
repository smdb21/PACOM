package org.proteored.miapeExtractor.gui.tasks;

import javax.swing.SwingWorker;

import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.interfaces.MiapeHeader;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeDatabaseException_Exception;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeSecurityException_Exception;
import org.proteored.miapeapi.webservice.clients.miapeextractor.MiapeExtractorDelegate;

public class MiapeHeaderLoaderTask extends SwingWorker<Void, String> {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger("log4j.logger.org.proteored");

	private final String miapeType;
	private final String MIAPE_ID_REGEXP = "MIAPE\\s\\w+\\s(\\d+).*";

	private final MiapeAPIWebserviceDelegate miapeAPIWebservice;

	private final String userName;

	private final String password;

	private final MiapeExtractorDelegate miapeExtractorWebservice;

	private final Integer id;
	public static final String MIAPE_HEADER_RETRIEVING_STARTS = "miape_header_retrieving_starts";
	public static final String MIAPE_HEADER_RETRIEVING_FINISH = "miape_header_retrieving_finish";
	public static final String MIAPE_HEADER_RETRIEVING_ERROR = "miape_header_retrieving_error";
	public static final String MIAPE_HEADER_RETRIEVING_RETURN_MSI_HEADER = "miape_header_return_msi_header";
	public static final String MIAPE_HEADER_RETRIEVING_RETURN_MS_HEADER = "miape_header_return_ms_header";
	public static final String MIAPE_HEADER_RETRIEVING_RETURN_GE_HEADER = "miape_header_return_ge_header";
	public static final String MIAPE_HEADER_RETRIEVING_RETURN_GI_HEADER = "miape_header_return_gi_header";

	public MiapeHeaderLoaderTask(int id, String miapeType,
			MiapeAPIWebserviceDelegate miapeAPIWebservice,
			MiapeExtractorDelegate miapeExtractorWebservice, String userName,
			String password) {
		this.id = id;
		this.miapeType = miapeType;
		this.miapeAPIWebservice = miapeAPIWebservice;
		this.miapeExtractorWebservice = miapeExtractorWebservice;
		this.userName = userName;
		this.password = password;
	}

	@Override
	protected Void doInBackground() throws Exception {

		try {
			log.info("Starting getMiapeHeaderTrhead");
			firePropertyChange(MIAPE_HEADER_RETRIEVING_STARTS, null,
					"Retrieving MIAPE " + this.miapeType + " ID:" + this.id
							+ " information ...");
			if (this.miapeType.equals("MS")) {
				this.getMiapeMSInformation(this.id);
			} else if (this.miapeType.equals("MSI")) {
				this.getMiapeMSIInformation(this.id);
			} else if (this.miapeType.equals("GE")) {
				this.getMiapeGEInformation(this.id);
			} else if (this.miapeType.equals("GI")) {
				this.getMiapeGIInformation(this.id);
			}
		} catch (Exception e) {
			if (this.miapeType == null)
				firePropertyChange(
						MiapeHeaderLoaderTask.MIAPE_HEADER_RETRIEVING_ERROR,
						null, "Error retrieving MIAPE Project ID:" + this.id
								+ ": " + e.getMessage());
			else
				firePropertyChange(MIAPE_HEADER_RETRIEVING_ERROR, null,
						"Error retrieving MIAPE " + this.miapeType + " ID:"
								+ this.id + ": " + e.getMessage());
		}
		return null;
	}

	@Override
	protected void done() {

		setProgress(100);

	}

	/**
	 * Retrieve the information of a MIAPE MSI document that have been clicked
	 * in the tree. It also check if some MIAPE MS documents are associated with
	 * it. If so, it retrieves also that information. All the information is
	 * shown in the right panel
	 */
	private void getMiapeMSIInformation(int idMSI) {
		int idMS = -1;

		// if some element of the tree is selected

		try {
			if (idMSI > 0) {

				log.info("Getting MIAPE MSI header ID: " + idMSI);
				// load current MIAPE MSI information
				byte[] miapeMSIHeaderBytes;
				setProgress(0);
				firePropertyChange("notificacion", null,
						"Retrieving MIAPE MSI (ID:" + idMSI + ") information");
				// retrieving the MIAPE MSI information
				miapeMSIHeaderBytes = this.miapeAPIWebservice
						.getMiapeMSIHeaderById(idMSI, this.userName,
								this.password);
				setProgress(50);
				// check if bytes are null
				if (miapeMSIHeaderBytes == null)
					throw new IllegalMiapeArgumentException(
							"Information retrieved from MIAPE MSI:" + idMSI
									+ " is null");
				MiapeHeader miapeMSIHeader = new MiapeHeader(
						miapeMSIHeaderBytes);
				// show MIAPE MSI information
				// this.showMIAPEMSIInformation(miapeMSIHeader);
				firePropertyChange(MIAPE_HEADER_RETRIEVING_RETURN_MSI_HEADER,
						null, miapeMSIHeader);

				miapeMSIHeader.getFile().delete();

				// Check if there is an asociated MIAPE MS
				log.info("Checking associated MIAPE MS documents to MIAPE MSI:"
						+ idMSI);
				idMS = this.miapeExtractorWebservice.getAssociatedMiapeMS(
						idMSI, this.userName, this.password);

				if (idMS > 0) {
					getMiapeMSInformation(idMS);
				}
			}

		} catch (MiapeSecurityException_Exception e) {
			e.printStackTrace();
			firePropertyChange("notificacion", null, e.getMessage());
		} catch (MiapeDatabaseException_Exception e) {
			e.printStackTrace();
			firePropertyChange("notificacion", null, e.getMessage());
		}
	}

	private void getMiapeMSInformation(int idMS) {
		if (idMS > 0) {
			log.info("Getting MIAPE MS header ID: " + idMS);
			setProgress(0);
			firePropertyChange("notificacion", null, "Retrieving MIAPE MS (ID:"
					+ idMS + ") information");
			// load current MIAPE MSI information
			byte[] miapeMSHeaderBytes;
			try {
				miapeMSHeaderBytes = this.miapeAPIWebservice
						.getMiapeMSHeaderById(idMS, this.userName,
								this.password);
				setProgress(50);
				if (miapeMSHeaderBytes != null) {
					MiapeHeader miapeMSHeader = new MiapeHeader(
							miapeMSHeaderBytes);
					firePropertyChange(
							MIAPE_HEADER_RETRIEVING_RETURN_MS_HEADER, null,
							miapeMSHeader);
				}
			} catch (MiapeSecurityException_Exception e) {
				e.printStackTrace();
				return;
			} catch (MiapeDatabaseException_Exception e) {
				e.printStackTrace();
				return;
			}

		}
	}

	private void getMiapeGIInformation(int idGI) {
		int idMS = -1;

		// if some element of the tree is selected

		try {
			if (idGI > 0) {

				log.info("Getting MIAPE GI header ID: " + idGI);
				// load current MIAPE MSI information
				byte[] miapeGIHeaderBytes;
				setProgress(0);
				firePropertyChange("notificacion", null,
						"Retrieving MIAPE GI (ID:" + idGI + ") information");
				// retrieving the MIAPE MSI information
				miapeGIHeaderBytes = this.miapeAPIWebservice
						.getMiapeGIHeaderById(idGI, this.userName,
								this.password);
				setProgress(50);
				// check if bytes are null
				if (miapeGIHeaderBytes == null)
					throw new IllegalMiapeArgumentException(
							"Information retrieved from MIAPE GI:" + idGI
									+ " is null");
				MiapeHeader miapeGIHeader = new MiapeHeader(miapeGIHeaderBytes);
				// show MIAPE MSI information
				// this.showMIAPEMSIInformation(miapeMSIHeader);
				firePropertyChange(MIAPE_HEADER_RETRIEVING_RETURN_GI_HEADER,
						null, miapeGIHeader);

				miapeGIHeader.getFile().delete();

				// Check if there is an asociated MIAPE MS
				log.info("Checking associated MIAPE GE documents to MIAPE GI:"
						+ idGI);
				idMS = this.miapeExtractorWebservice.getAssociatedMiapeGE(idGI,
						this.userName, this.password);

				if (idMS > 0) {
					getMiapeMSInformation(idMS);
				}
			}

		} catch (MiapeSecurityException_Exception e) {
			e.printStackTrace();
			firePropertyChange("notificacion", null, e.getMessage());
		} catch (MiapeDatabaseException_Exception e) {
			e.printStackTrace();
			firePropertyChange("notificacion", null, e.getMessage());
		}
	}

	private void getMiapeGEInformation(int idGE) {
		if (idGE > 0) {
			log.info("Getting MIAPE GE header ID: " + idGE);
			setProgress(0);
			firePropertyChange("notificacion", null, "Retrieving MIAPE GE (ID:"
					+ idGE + ") information");
			// load current MIAPE MSI information
			byte[] miapeGEHeaderBytes;
			try {
				miapeGEHeaderBytes = this.miapeAPIWebservice
						.getMiapeGEHeaderById(idGE, this.userName,
								this.password);
				setProgress(50);
				if (miapeGEHeaderBytes != null) {
					MiapeHeader miapeGEHeader = new MiapeHeader(
							miapeGEHeaderBytes);
					firePropertyChange(
							MIAPE_HEADER_RETRIEVING_RETURN_GE_HEADER, null,
							miapeGEHeader);
				}
			} catch (MiapeSecurityException_Exception e) {
				e.printStackTrace();
				return;
			} catch (MiapeDatabaseException_Exception e) {
				e.printStackTrace();
				return;
			}

		}
	}

}
