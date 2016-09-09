package org.proteored.pacom.gui.tasks;

import javax.swing.SwingWorker;

import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.spring.SpringHandler;

public class OntologyLoaderTask extends SwingWorker<Void, Void> {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	private static ControlVocabularyManager cvManager;
	public final static String ONTOLOGY_LOADING_STARTED = "ontology loaing starting";
	public final static String ONTOLOGY_LOADING_ERROR = "ontology loaing error";
	public final static String ONTOLOGY_LOADING_FINISHED = "ontology loaing finished";
	private long t1;

	public OntologyLoaderTask() {
	}

	@Override
	public Void doInBackground() throws Exception {
		try {
			t1 = System.currentTimeMillis();
			log.info("Running background loader");
			log.info("Loading ontologies");
			firePropertyChange(ONTOLOGY_LOADING_STARTED, null, null);
			cvManager = SpringHandler.getInstance("miape-extractor-spring.xml").getCVManager();

			return null;
		} catch (Exception e) {
			log.info("Error loading ontologies: " + e.getMessage());
			firePropertyChange(ONTOLOGY_LOADING_ERROR, null, e.getMessage());
			throw e;
		}

	}

	@Override
	protected void done() {
		log.info("Ontologies loaded");
		long t2 = System.currentTimeMillis();
		long time = t2 - t1;
		firePropertyChange(ONTOLOGY_LOADING_FINISHED, null, time);
		super.done();
	}

	public static ControlVocabularyManager getCvManager() {
		if (cvManager == null) {
			final SpringHandler instance = SpringHandler.getInstance("miape-extractor-spring.xml");
			cvManager = instance.getCVManager();
		}
		return cvManager;
	}

	public static ControlVocabularyManager getTestCvManager() {
		if (cvManager == null) {
			final SpringHandler instance = SpringHandler.getInstance("miape-extractor-spring-test.xml");
			cvManager = instance.getCVManager();
		}
		return cvManager;
	}
}
