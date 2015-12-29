package org.proteored.miapeExtractor.gui.tasks;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.proteored.miapeapi.cv.ControlVocabularyManager;

public class OntologyLoaderWaiter extends SwingWorker<ControlVocabularyManager, Void> {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger("log4j.logger.org.proteored");
	public static String ONTOLOGY_LOADED = "ontology_loaded";

	@Override
	protected ControlVocabularyManager doInBackground() throws Exception {
		while (OntologyLoaderTask.getCvManager() == null) {
			try {
				log.info("Waiting for ontology loading");
				Thread.sleep(1000);
			} catch (InterruptedException e) {

			}
		}
		return OntologyLoaderTask.getCvManager();
	}

	@Override
	protected void done() {
		if (!this.isCancelled()) {
			try {
				ControlVocabularyManager controlVocabularyManager = get();
				firePropertyChange(ONTOLOGY_LOADED, null, controlVocabularyManager);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

		}
	}

}
