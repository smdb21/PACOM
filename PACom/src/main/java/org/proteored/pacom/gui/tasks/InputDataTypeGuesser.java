package org.proteored.pacom.gui.tasks;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.proteored.pacom.gui.importjobs.InputFileType;

import edu.scripps.yates.dtaselectparser.DTASelectParser;
import edu.scripps.yates.utilities.files.FileUtils;
import gnu.trove.map.hash.THashMap;
import umich.ms.fileio.filetypes.pepxml.PepXmlParser;
import umich.ms.fileio.filetypes.pepxml.jaxb.standard.MsmsRunSummary;

/**
 * This class will try to read the files to guess their type. It will be a quick
 * reading
 * 
 * @author Salva
 *
 */
public class InputDataTypeGuesser extends SwingWorker<Void, Void> {
	private final static Logger log = Logger.getLogger(InputDataTypeGuesser.class);
	public static final String INPUT_DATA_TYPE_GUESSED = "Input data type guessed";
	public static final String INPUT_DATA_TYPE_GUESSING_STARTED = "Input data type guessing started";
	public static final String INPUT_DATA_TYPE_GUESSING_FINISHED = "Input data type guessing finished";
	public static final String INPUT_DATA_TYPE_GUESSING_ERROR = "Input data type guessing error";
	public static final String INPUT_DATA_TYPE_GUESSING_CANCELLED = "Input data type guessing cancelled";

	private final File[] files;
	private final MiapeExtractionTask associatedTask;
	private THashMap<File, InputFileType> map;

	public InputDataTypeGuesser(File[] files) {
		this.files = files;
		associatedTask = null;
	}

	public InputDataTypeGuesser(File file, MiapeExtractionTask associatedTask) {
		files = new File[1];
		files[0] = file;
		this.associatedTask = associatedTask;
	}

	private InputFileType guessInputDataType(File file) {
		final List<String> fiveFirstLines = FileUtils.readFirstLines(file, 5l);
		if (contains(fiveFirstLines, "N\tUnused\tTotal\t")) {
			return InputFileType.PROTEINPILOT;
		}
		// PEP XML
		if (contains(fiveFirstLines, "<msms_pipeline_analysis ")) {
			return InputFileType.PEPXML;
		} else if (contains(fiveFirstLines, "<MzIdentML ")) {
			return InputFileType.MZIDENTML;
		} else if (contains(fiveFirstLines, "<bioml ")) {
			return InputFileType.XTANDEM;
		} else if (contains(fiveFirstLines, "<ExperimentCollection ")) {
			return InputFileType.PRIDEXML;
		} else if (contains(fiveFirstLines, "DTASelect")) {
			return InputFileType.DTASELECT;
		}

		try {
			// PEPXML
			final Iterator<MsmsRunSummary> iterator = PepXmlParser.parse(FileUtils.getInputStream(file));
			if (iterator != null) {
				if (iterator.hasNext()) {
					return InputFileType.PEPXML;
				}
			}
		} catch (final Exception e) {
			log.debug("It is not a PEPXML " + e.getMessage());
		}
		try {
			// DTASELECT
			if (new DTASelectParser(file).canRead(file)) {
				return InputFileType.DTASELECT;
			}
		} catch (final Exception e) {
			log.debug("It is not a DTASelect file " + e.getMessage());
		}

		return InputFileType.TABLETEXT;
	}

	private boolean contains(List<String> fiveFirstLines, String toFind) {
		if (fiveFirstLines != null) {
			for (final String string : fiveFirstLines) {
				if (string.toLowerCase().contains(toFind.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			map = new THashMap<File, InputFileType>();
			firePropertyChange(INPUT_DATA_TYPE_GUESSING_STARTED, null, this);

			for (int i = 0; i < files.length; i++) {
				final File file = files[i];
				final InputFileType guessedInputDataType = guessInputDataType(file);
				// fire signal even if it is null
				firePropertyChange(INPUT_DATA_TYPE_GUESSED, file, this);

				map.put(file, guessedInputDataType);
				setProgress(i + 1);
			}
			firePropertyChange(INPUT_DATA_TYPE_GUESSING_FINISHED, null, this);

		} catch (final Exception e) {
			firePropertyChange(INPUT_DATA_TYPE_GUESSING_ERROR, null, e);
		}
		return null;
	}

	@Override
	protected void done() {
		if (isCancelled()) {
			firePropertyChange(INPUT_DATA_TYPE_GUESSING_CANCELLED, null, null);
		}
		super.done();
	}

	public MiapeExtractionTask getAssociatedTask() {
		return associatedTask;
	}

	public THashMap<File, InputFileType> getGuessedTypes() {
		return map;
	}

	public File[] getFiles() {
		return files;
	}
}
