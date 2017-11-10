package org.proteored.pacom.analysis.gui.tasks;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import edu.scripps.yates.utilities.memory.MemoryUsageReport;

public class MemoryCheckerTask extends SwingWorker<Void, Void> {
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");

	public static final String MEMORY_CHECKER_DATA_REPORT = "memory data checker report";

	@Override
	protected Void doInBackground() throws Exception {

		while (true) {
			// DecimalFormat df = new DecimalFormat("#.#");
			Thread.sleep(1000);
			// Runtime runtime = Runtime.getRuntime();
			// long totalMemory = runtime.totalMemory();
			// long maxMemory = runtime.maxMemory();
			// long freeMemory = runtime.freeMemory() + (maxMemory -
			// totalMemory);
			//
			// long usedMemory = totalMemory - runtime.freeMemory();
			// double percentage = totalMemory * 100.0 / maxMemory;
			// long round = Math.round(percentage);
			final int intValue = Double.valueOf(MemoryUsageReport.getUsedMemoryPercentage()).intValue();
			setProgress(intValue);
			//
			// String usedMb = df.format(totalMemory / 1024.0 / 1024.0);
			// String usedUnit = "Mb";
			// if (Double.valueOf(usedMb) > 1024.0) {
			// usedUnit = "Gb";
			// usedMb = df.format(Double.valueOf(usedMb) / 1024.0);
			// }
			//
			// String totalMb = df.format(maxMemory / 1024 / 1024);
			// String totalUnit = "Mb";
			// if (Double.valueOf(totalMb) > 1024.0) {
			// totalUnit = "Gb";
			// totalMb = df.format(Double.valueOf(totalMb) / 1024.0);
			// }
			// String memoryString = usedMb + usedUnit + " of " + totalMb
			// + totalUnit + " (" + df.format(percentage) + "%)";
			// firePropertyChange(MEMORY_CHECKER_DATA_REPORT, null,
			// memoryString);
			firePropertyChange(MEMORY_CHECKER_DATA_REPORT, null, MemoryUsageReport.getMemoryUsageReport());
		}

	}
}
