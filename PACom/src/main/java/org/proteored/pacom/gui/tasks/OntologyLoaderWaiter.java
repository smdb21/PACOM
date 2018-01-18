package org.proteored.pacom.gui.tasks;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.springframework.beans.factory.BeanDefinitionStoreException;

public class OntologyLoaderWaiter extends SwingWorker<ControlVocabularyManager, Void> {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	public static String ONTOLOGY_LOADED = "ontology_loaded";
	public static String ONTOLOGY_LOADING_ERROR = "ontology_loading_error";
	public static String ONTOLOGY_LOADING_NETWORK_ERROR = "ontology_loading_network_error";

	@Override
	protected ControlVocabularyManager doInBackground() throws Exception {
		try {
			while (OntologyLoaderTask.getCvManager() == null) {
				try {
					log.info("Waiting for ontology loading");
					Thread.sleep(1000);
				} catch (InterruptedException e) {

				}
			}
			return OntologyLoaderTask.getCvManager();
		} catch (BeanDefinitionStoreException e) {
			e.printStackTrace();
			firePropertyChange(ONTOLOGY_LOADING_NETWORK_ERROR, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			firePropertyChange(ONTOLOGY_LOADING_ERROR, null, null);
		}
		return null;
	}

	@Override
	protected void done() {
		if (!this.isCancelled()) {
			try {
				ControlVocabularyManager controlVocabularyManager = get();
				if (controlVocabularyManager != null) {
					firePropertyChange(ONTOLOGY_LOADED, null, controlVocabularyManager);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

		}
	}

}
