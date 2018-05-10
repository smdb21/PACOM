package org.proteored.pacom.gui.importjobs;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingWorker.StateValue;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.proteored.pacom.gui.tasks.MiapeExtractionTask;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ProgressCellRender implements TableCellRenderer {
	private static final Logger log = Logger.getLogger(ProgressCellRender.class);
	private final TIntObjectHashMap<JProgressBar> bars = new TIntObjectHashMap<JProgressBar>();
	private Color defaultColor;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		JProgressBar progressBar = null;
		if (!bars.containsKey(row)) {
			progressBar = new JProgressBar(1, 100);
			bars.put(row, progressBar);
			defaultColor = progressBar.getForeground();
		}

		progressBar = bars.get(row);
		progressBar.setStringPainted(true);
		// get task
		if (table instanceof ImportTasksTable) {
			if (value != null && value instanceof Integer) {
				progressBar.setValue((int) value);
				final MiapeExtractionTask task = ((ImportTasksTable) table).getImportTaskTableModel()
						.getTaskByRowIndex(row);
				final StateValue taskState = task.getState();
				if (taskState == StateValue.PENDING) {
					// not started yet
					progressBar.setString("");
					progressBar.setToolTipText("<html>Import job progress bar.<br>Start import to activate it.</html>");
					progressBar.setForeground(defaultColor);
					progressBar.setStringPainted(false);
				} else if (taskState == StateValue.STARTED) {
					progressBar.setString(getNextStringProgress(progressBar.getString()));
					progressBar.setToolTipText("<html>Import task in progress...</html>");
					progressBar.setForeground(defaultColor);
					progressBar.setStringPainted(false);
				} else if (taskState == StateValue.DONE) {
					progressBar.setValue(100);
					if (task.isCancelled()) {
						progressBar.setToolTipText("<html>Import task cancelled</html>");
						progressBar.setString("Cancelled");
						progressBar.setStringPainted(true);
						progressBar.setForeground(new Color(214, 146, 11));
					} else if (task.getError() != null) {
						progressBar.setToolTipText("<html>Import task with an error:<br><b>"
								+ task.getError().replace("\n", "<br>") + "</b></html>");
						progressBar.setString("Error");
						progressBar.setStringPainted(true);
						progressBar.setForeground(Color.red);
					} else if (task.getResult() != null && task.getResult().getErrorMessage() != null) {
						progressBar.setToolTipText("<html>Import task with an error:<br><b>"
								+ task.getResult().getErrorMessage() + "</b></html>");
						progressBar.setString("Error");
						progressBar.setStringPainted(true);
						progressBar.setForeground(Color.red);
					} else {
						progressBar.setToolTipText("<html>Import task finished</html>");
						progressBar.setString("Done");
						progressBar.setStringPainted(true);
						progressBar.setForeground(new Color(11, 214, 55));
					}
				}
			}
		}

		return progressBar;
	}

	private String getNextStringProgress(String string) {
		// if ("".equals(string)) {
		// return "Importing";
		// } else if ("Importing".equals(string) || "Importing.".equals(string)
		// || "Importing..".equals(string)) {
		// return string + ".";
		// } else if ("Importing...".equals(string)) {
		// return "Importing";
		// }
		return string;
	}
}
