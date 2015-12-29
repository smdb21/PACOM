package org.proteored.miapeExtractor.gui.tasks;

import javax.swing.SwingWorker;

import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;

public class MiapeDeleterTask extends SwingWorker<Void, Void> {
	private final MiapeAPIWebserviceDelegate miapeAPIWebservice;
	private final String userName;
	private final String password;
	private final int id;
	private final String miapeType;
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger("log4j.logger.org.proteored");

	public static String DELETING_STARTED = "deleting_started";
	public static String DELETING_FINISHED = "deleting_finished";
	public static String DELETING_ERROR = "deleting_error";

	public MiapeDeleterTask(MiapeAPIWebserviceDelegate miapeAPIWebservice,
			String userName, String password, int miapeId, String miapeType) {
		this.miapeAPIWebservice = miapeAPIWebservice;
		this.userName = userName;
		this.password = password;
		this.id = miapeId;
		this.miapeType = miapeType;
	}

	public MiapeDeleterTask(MiapeAPIWebserviceDelegate miapeAPIWebservice,
			String userName, String password, int idProject) {
		this.miapeAPIWebservice = miapeAPIWebservice;
		this.userName = userName;
		this.password = password;
		this.id = idProject;
		this.miapeType = null;
	}

	@Override
	protected Void doInBackground() throws Exception {

		try {
			if (miapeType == null) {
				log.info("Starting deleting: MIAPE project id=" + id);
				firePropertyChange(DELETING_STARTED, null,
						"Deleting MIAPE Project ID:" + this.id + " ...");
				miapeAPIWebservice.deleteProject(id, userName, password);
				firePropertyChange(DELETING_FINISHED, null,
						"MIAPE Project ID: " + this.id
								+ " deleted successfully");
			} else {
				log.info("Starting deleting: MIAPEType=" + this.miapeType
						+ " id=" + id);
				if (miapeType.equals("GE")) {
					firePropertyChange(DELETING_STARTED, null,
							"Deleting MIAPE GE ID:" + this.id + " ...");
					miapeAPIWebservice.deleteMiapeGE(id, userName, password);
					firePropertyChange(DELETING_FINISHED, null, "MIAPE GE ID: "
							+ this.id + " deleted successfully");
				} else if (miapeType.equals("GI")) {
					firePropertyChange(DELETING_STARTED, null,
							"Deleting MIAPE GI ID:" + this.id + " ...");
					miapeAPIWebservice.deleteMiapeGI(id, userName, password);
					firePropertyChange(DELETING_FINISHED, null, "MIAPE GE ID: "
							+ this.id + " deleted successfully");
				} else if (miapeType.equals("MS")) {
					firePropertyChange(DELETING_STARTED, null,
							"Deleting MIAPE MS ID:" + this.id + " ...");
					miapeAPIWebservice.deleteMiapeMS(id, userName, password);
					firePropertyChange(DELETING_FINISHED, null, "MIAPE MS ID: "
							+ this.id + " deleted successfully");
				} else if (miapeType.equals("MSI")) {
					firePropertyChange(DELETING_STARTED, null,
							"Deleting MIAPE MSI ID:" + this.id + " ...");
					miapeAPIWebservice.deleteMiapeMSI(id, userName, password);
					firePropertyChange(DELETING_FINISHED, null,
							"MIAPE MSI ID: " + this.id
									+ " deleted successfully");
				}
			}
		} catch (Exception e) {
			if (this.miapeType == null)
				firePropertyChange(
						DELETING_ERROR,
						null,
						"Error deleting MIAPE Project ID:" + this.id + ": "
								+ e.getMessage());
			else
				firePropertyChange(DELETING_ERROR, null,
						"Error deleting MIAPE " + this.miapeType + " ID:"
								+ this.id + ": " + e.getMessage());
		}
		return null;
	}

	public static String getMiapeType(String finishMessage) {
		try {
			String[] split = finishMessage.split(" ");
			return split[1];
		} catch (Exception e) {
			return null;
		}
	}

	public static int getMiapeIdentifier(String finishMessage) {
		try {
			String[] split = finishMessage.split(" ");
			return Integer.valueOf(split[3]);
		} catch (Exception e) {
			return -1;
		}
	}

	@Override
	protected void done() {
		log.info("Deleting MIAPE type:" + this.miapeType + " id:" + this.id
				+ " done");
		super.done();
	}

}
