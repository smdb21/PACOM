package org.proteored.miapeExtractor.analysis.gui.tasks;

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.SwingWorker;

import org.proteored.miapeExtractor.gui.tasks.OntologyLoaderTask;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.factories.msi.MiapeMSIDocumentBuilder;
import org.proteored.miapeapi.xml.dtaselect.msi.MiapeMsiDocumentImpl;

import edu.scripps.yates.dtaselectparser.DTASelectParser;

public class IdentificationSetFromDTASelectFileTask extends SwingWorker<Void, String> {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	public static final String DTASELECT_PARSER_STARTS = "DTA_SELECT_PARSER_STARTS";
	public static final String DTASELECT_PARSER_ERROR = "DTA_SELECT_PARSER_ERROR";

	public static final String DTASELECT_PARSER_FINISHED = "DTA_SELECT_PARSER_FINISHED";
	private final String idSetName;
	private MiapeMSIDocumentBuilder builder;
	private final DTASelectParser parser;

	public IdentificationSetFromDTASelectFileTask(File dtaSelectFile, String idSetName) throws FileNotFoundException {
		this.idSetName = idSetName;
		parser = new DTASelectParser(dtaSelectFile);
	}

	public IdentificationSetFromDTASelectFileTask(DTASelectParser parser, String idSetName)
			throws FileNotFoundException {
		this.idSetName = idSetName;
		this.parser = parser;
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			log.info(DTASELECT_PARSER_STARTS);
			firePropertyChange(DTASELECT_PARSER_STARTS, null, null);
			final ControlVocabularyManager cvManager = OntologyLoaderTask.getCvManager();
			builder = MiapeMsiDocumentImpl.getMIAPEMSIDocumentBuilder(parser, idSetName,
					cvManager, null, idSetName);
			firePropertyChange(DTASELECT_PARSER_FINISHED, null, builder);
		} catch (Exception e) {
			e.printStackTrace();
			firePropertyChange(DTASELECT_PARSER_ERROR, null, e.getMessage());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		if (isCancelled()) {
			log.info("DTA SELECT PARSER CANCELED");
		} else {
			log.info("DTA SELECT PARSER DONE");

		}
		super.done();
	}

}
