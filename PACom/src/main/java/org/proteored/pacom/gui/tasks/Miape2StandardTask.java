package org.proteored.pacom.gui.tasks;

import java.awt.Toolkit;
import java.rmi.RemoteException;

import javax.swing.SwingWorker;

import org.proteored.miapeapi.webservice.clients.miapeextractor.MiapeExtractorDelegate;
import org.proteored.pacom.gui.MainFrame;

public class Miape2StandardTask extends SwingWorker<Void, Void> {
	private static final int CHECKING_INPUTS = 1;

	private static final int CONVERTING = 2;

	public static final String PRIDE_EXPORTER_STARTED = "PRIDE_EXPORTER_STARTED";
	public static final String PRIDE_EXPORTER_FINISH = "PRIDE_EXPORTER_FINISH";
	public static final String PRIDE_EXPORTER_ERROR = "PRIDE_EXPORTER_ERROR";
	public static final String PRIDE_EXPORTER_INFO = "PRIDE_EXPORTER_INFO";
	private final String MIAPE_ID_REGEXP = "MIAPE\\s\\w+\\s(\\d+).*";
	private final MiapeExtractorDelegate miapeExtractorWebservice;
	private String prideURL = null;

	/*
	 * Main task. Executed in background thread.
	 */
	// private Thread thread;
	private final String userName;
	private final String password;
	private int taskStatus;
	private long initialTime;
	private final Integer idMS;
	private final Integer idMSI;

	private String error = "";

	public Miape2StandardTask(Integer idMS, Integer idMSI,
			MiapeExtractorDelegate miapeExtractorWebservice, String userName,
			String password) {

		this.miapeExtractorWebservice = miapeExtractorWebservice;
		this.userName = userName;
		this.password = password;
		this.idMS = idMS;
		this.idMSI = idMSI;
	}

	private void convert2Standard() throws RemoteException {

		boolean parseMGF = false; // TODO preguntarle al usuario
		Integer idMSI = 0;
		Integer idMS = 0;

		// TODO mirar si se han seleccionado varios de un mismo tipo de miape en
		// cuyo caso, dar error

		// MSI selected

		firePropertyChange("notificacion", null,
				"Waiting for server response...");
		firePropertyChange(PRIDE_EXPORTER_STARTED, null,
				"PRIDE creation started. Waiting for server response...");

		if (idMS != 0 && idMSI != 0) {
			// this.parentDialog.statusField.append("Converting MIAPE MS (ID:"
			// + idMS + ") and MIAPE MSI (ID:" + idMSI +
			// ") to a PRIDE file\n");
			prideURL = this.miapeExtractorWebservice.getPRIDEFromMiapeMSMSI(
					idMS, idMSI, this.userName, this.password, true,
					MainFrame.emailNotifications);
			// MS conversion
		} else if (idMS != 0) {
			// this.parentDialog.statusField.append("Converting MIAPE MS (ID:"
			// + idMS + ") to a PRIDE file\n");
			prideURL = this.miapeExtractorWebservice.getPRIDEFromMiapeMS(idMS,
					this.userName, this.password, true,
					MainFrame.emailNotifications);
			// MSI conversion
		} else if (idMSI != 0) {
			// this.parentDialog.statusField.append("Converting MIAPE MSI (ID:"
			// + idMSI + ") to a PRIDE file\n");
			prideURL = this.miapeExtractorWebservice.getPRIDEFromMiapeMSI(
					idMSI, this.userName, this.password,
					MainFrame.emailNotifications);
		}

		// parentDialog.statusField.append("PRIDE file created\nYou can download it here: "
		// + prideURL + "\n");

	}

	@Override
	public Void doInBackground() {

		try {

			this.taskStatus = CONVERTING;
			// thread.start();
			initialTime = System.currentTimeMillis();
			convert2Standard();

		} catch (RemoteException e) {
			e.printStackTrace();
			if (e.getMessage().contains("SocketTimeoutException")) {
				firePropertyChange(
						PRIDE_EXPORTER_ERROR,
						null,
						"This process will take more time, but don't worry. You will receive an e-mail when the process is finished.");
			} else {
				if (e.getMessage() != null) {
					firePropertyChange(PRIDE_EXPORTER_ERROR, null,
							e.getMessage());
					this.error = e.getMessage();
				}
			}
			this.cancel(true);
		} catch (Exception e) {
			e.printStackTrace();
			this.error = e.getMessage();
			this.cancel(true);
		}
		// setProgress(100);
		return null;
	}

	/*
	 * Executed in event dispatching thread
	 */
	@Override
	public void done() {

		setProgress(100);

		Toolkit.getDefaultToolkit().beep();
		// thread.interrupt();

		if (prideURL != null) {
			firePropertyChange(PRIDE_EXPORTER_FINISH, null,
					"PRIDE file created here: " + prideURL);
			long duration = (System.currentTimeMillis() - initialTime) / 60000;
			String unit = "minutes";
			if (duration == 0) {
				duration = (System.currentTimeMillis() - initialTime) / 1000;
				unit = "seconds";
			}
			firePropertyChange(PRIDE_EXPORTER_INFO, null, "The operation took "
					+ duration + " " + unit);

		} else {
			if (!"".equals(this.error)) {
				firePropertyChange(PRIDE_EXPORTER_ERROR, null, "ERROR: "
						+ error);
			} else if (MainFrame.emailNotifications)
				firePropertyChange(PRIDE_EXPORTER_INFO, null,
						"You will receive a notification e-mail as soon as the process is finished.");
		}

		// parentDialog.jButton_go.setEnabled(true);

		if (taskStatus == CHECKING_INPUTS) {
			return;
		}
	}
}
