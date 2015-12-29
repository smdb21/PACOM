package org.proteored.miapeExtractor.analysis.gui.tasks;

import java.text.DecimalFormat;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

public class MemoryCheckerTask extends SwingWorker<Void, Void> {
	private static final Logger log = Logger
			.getLogger("log4j.logger.org.proteored");

	public static final String MEMORY_CHECKER_DATA_REPORT = "memory data checker report";

	@Override
	protected Void doInBackground() throws Exception {

		while (true) {
			DecimalFormat df = new DecimalFormat("#.#");
			Thread.sleep(1000);
			// log.info("Updating memory usage");
			Runtime runtime = Runtime.getRuntime();
			long totalMemory = runtime.totalMemory();
			long maxMemory = runtime.maxMemory();
			long freeMemory = runtime.freeMemory() + (maxMemory - totalMemory);
			// long freeMemory = runtime.freeMemory();

			long usedMemory = totalMemory - runtime.freeMemory();
			double percentage = totalMemory * 100.0 / maxMemory;
			long round = Math.round(percentage);
			setProgress((int) round);

			String usedMb = df.format(totalMemory / 1024.0 / 1024.0);
			String usedUnit = "Mb";
			if (Double.valueOf(usedMb) > 1024.0) {
				usedUnit = "Gb";
				usedMb = df.format(Double.valueOf(usedMb) / 1024.0);
			}

			String totalMb = df.format(maxMemory / 1024 / 1024);
			String totalUnit = "Mb";
			if (Double.valueOf(totalMb) > 1024.0) {
				totalUnit = "Gb";
				totalMb = df.format(Double.valueOf(totalMb) / 1024.0);
			}
			String memoryString = usedMb + usedUnit + " of " + totalMb
					+ totalUnit + " (" + df.format(percentage) + "%)";
			firePropertyChange(MEMORY_CHECKER_DATA_REPORT, null, memoryString);
		}

	}
}
