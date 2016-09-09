package org.proteored.pacom.gui.tasks;

import javax.swing.SwingWorker;

import org.proteored.pacom.utils.UpdateChecker;

public class CheckUpdateTask extends SwingWorker<Void, String> {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger("log4j.logger.org.proteored");

	public CheckUpdateTask() {

	}

	@Override
	protected Void doInBackground() throws Exception {

		try {
			while (true) {
				log.info("Checking for an update");
				if (UpdateChecker.getInstance().hasUpdate()) {
					log.info("Update found");
					UpdateChecker.getInstance().showUpdateDialog();
				} else {
					log.info("Nothing to update");
				}
				Thread.sleep(1000 * 60 * 30); // 30 min
			}
		} finally {
		}

	}

	@Override
	protected void done() {
		setProgress(100);
	}

}
