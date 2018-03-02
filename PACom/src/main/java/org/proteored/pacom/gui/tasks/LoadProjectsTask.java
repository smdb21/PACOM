package org.proteored.pacom.gui.tasks;

import java.util.List;

import javax.swing.SwingWorker;

import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.MiapeExtractionFrame;

import gnu.trove.map.hash.TIntObjectHashMap;

public class LoadProjectsTask extends SwingWorker<Void, Void> {

	private TIntObjectHashMap<String> loadedProjects;
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	private final MiapeExtractionFrame parentDialog;
	private final static TIntObjectHashMap<String> cachedLocalProjects = new TIntObjectHashMap<String>();
	private final static TIntObjectHashMap<TIntObjectHashMap<String>> cachedRemoteProjects = new TIntObjectHashMap<TIntObjectHashMap<String>>();
	public final static String PROJECT_LOADED_DONE = "project loaded done";

	public LoadProjectsTask(MiapeExtractionFrame miapeExtractionFrameNEW) {

		parentDialog = miapeExtractionFrameNEW;

	}

	@Override
	protected Void doInBackground() throws Exception {
		parentDialog.setLoadingProjects(true);
		TIntObjectHashMap<String> projects = null;
		if (loadedProjects == null || loadedProjects.size() == 0) {
			projects = loadProjects();
			parentDialog.setLoadedProjects(projects);
		}
		parentDialog.setLoadingProjects(false);
		return null;
	}

	@Override
	protected void done() {
		firePropertyChange(PROJECT_LOADED_DONE, null, null);
		if (isCancelled())
			log.info("Project loading cancelled");
		if (isDone())
			log.info("Project loading is done");
		log.info("Project list loaded");
		super.done();
	}

	private TIntObjectHashMap<String> loadProjects() {

		// look into cached projects
		if (!getLocalCachedProjects().isEmpty())
			return getLocalCachedProjects();
		try {
			log.info("Loading projects from local folder");
			firePropertyChange(MiapeExtractionTask.NOTIFICATION, null, "Loading projects from local file system...");
			final List<String> localMIAPEProjects = FileManager.getlocalMIAPEProjects();
			TIntObjectHashMap<String> projectsHashMap = new TIntObjectHashMap<String>();
			int counter = 1;
			for (String projectName : localMIAPEProjects) {
				projectsHashMap.put(counter++, projectName);
			}
			log.info(localMIAPEProjects.size() + " projects loaded");
			if (!localMIAPEProjects.isEmpty()) {
				firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						localMIAPEProjects.size() + " projects loaded");
			} else {
				firePropertyChange(MiapeExtractionTask.NOTIFICATION, null, "No projects found in the file system.");
			}

			// disabled cache in order to show the projects that have been
			// just created
			// addToLocalCache(projectsHashMap);
			return projectsHashMap;
		} catch (Exception e) {
			firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
					"Error loading projects: " + e.getMessage() + "\n");
			e.printStackTrace();
			return null;
		}

	}

	private static synchronized TIntObjectHashMap<String> getLocalCachedProjects() {
		return cachedLocalProjects;
	}

}
