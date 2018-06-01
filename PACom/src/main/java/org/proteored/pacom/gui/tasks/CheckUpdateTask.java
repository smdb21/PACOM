package org.proteored.pacom.gui.tasks;

import javax.swing.SwingWorker;

import org.proteored.pacom.utils.UpdateChecker;

import edu.scripps.yates.utilities.dates.DatesUtil;

public class CheckUpdateTask extends SwingWorker<Void, String> {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	private final long delay;

	/**
	 * Check update thread. Checking by default every 5 min
	 */
	public CheckUpdateTask() {
		delay = 1000 * 60 * 5;// 5 min
	}

	public CheckUpdateTask(long delayInMillisenconds) {
		delay = delayInMillisenconds;
	}

	@Override
	protected Void doInBackground() throws Exception {

		try {
			while (true) {
				log.info("Checking for an update in PACOM...");
				if (UpdateChecker.getInstance().hasUpdate()) {
					log.info("Update found");
					UpdateChecker.getInstance().showUpdateDialog();
				} else {
					log.info("Nothing to update. NExt check in " + DatesUtil.getDescriptiveTimeFromMillisecs(delay));
				}
				Thread.sleep(delay);
			}
		} finally {
		}

	}

	@Override
	protected void done() {
		setProgress(100);
	}

}
