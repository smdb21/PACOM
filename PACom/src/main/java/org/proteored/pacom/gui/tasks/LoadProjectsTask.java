package org.proteored.pacom.gui.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import org.proteored.miapeapi.interfaces.Permission;
import org.proteored.miapeapi.webservice.clients.miapeapi.IntegerString;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeDatabaseException_Exception;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeSecurityException_Exception;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.MainFrame;
import org.proteored.pacom.gui.MiapeExtractionFrame;
import org.proteored.pacom.utils.Wrapper;

public class LoadProjectsTask extends SwingWorker<Void, Void> {
	private final String userName;
	private final String password;
	private final int userID;
	private HashMap<Integer, String> loadedProjects;
	private boolean loading;
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	private final MiapeExtractionFrame parentDialog;
	private final boolean localProjects;
	private final static HashMap<Integer, String> cachedLocalProjects = new HashMap<Integer, String>();
	private final static HashMap<Integer, Map<Integer, String>> cachedRemoteProjects = new HashMap<Integer, Map<Integer, String>>();

	public LoadProjectsTask(MiapeExtractionFrame standard2miapeDialog, boolean localProjects, int userID,
			String userName, String password) {

		this.userName = userName;
		this.password = password;
		parentDialog = standard2miapeDialog;
		this.localProjects = localProjects;
		this.userID = userID;
	}

	@Override
	protected Void doInBackground() throws Exception {
		parentDialog.setLoadingProjects(true);
		Map<Integer, String> projects = null;
		if (loadedProjects == null || loadedProjects.size() == 0) {
			projects = loadProjects();
			parentDialog.setLoadedProjects(projects);
		}
		parentDialog.setLoadingProjects(false);
		return null;
	}

	@Override
	protected void done() {
		if (isCancelled())
			log.info("Project loading cancelled");
		if (isDone())
			log.info("Project loading is done");
		log.info("Project list loaded");
		super.done();
	}

	private Map<Integer, String> loadProjects() {

		if (localProjects) {
			// look into cached projects
			if (!getLocalCachedProjects().isEmpty())
				return getLocalCachedProjects();
			try {
				log.info("Loading projects from local folder");
				firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Pre-loading projects from local file system...");
				final List<String> localMIAPEProjects = FileManager.getlocalMIAPEProjects();
				HashMap<Integer, String> projectsHashMap = new HashMap<Integer, String>();
				int counter = 1;
				for (String projectName : localMIAPEProjects) {
					projectsHashMap.put(counter++, projectName);
				}
				log.info(localMIAPEProjects.size() + " projects loaded");
				firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						localMIAPEProjects.size() + " projects loaded");
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
		} else {
			// look into cached projects
			if (!getRemoteCachedProjects(userID).isEmpty())
				return getRemoteCachedProjects(userID);
			try {
				log.info("Loading projects from user");
				firePropertyChange(MiapeExtractionTask.NOTIFICATION, null, "Pre-loading projects from repository...");
				final List<IntegerString> allProjects = MainFrame.getMiapeAPIWebservice().getAllProjects(userName,
						password);
				HashMap<Integer, String> miapeProjects = Wrapper.getHashMap(allProjects);
				int numProjects = miapeProjects.size();
				HashMap<Integer, String> ret = new HashMap<Integer, String>();

				// filter the onw that the user has not write permissions
				int counter = 1;
				for (Integer projectId : miapeProjects.keySet()) {
					Permission perm = new Permission(
							MainFrame.getMiapeAPIWebservice().getProjectPermissions(projectId, userName, password));
					setProgress(counter * 100 / numProjects);
					counter++;
					if (perm.canWrite())
						ret.put(projectId, miapeProjects.get(projectId));
				}
				log.info(ret.size() + " projects loaded");
				addToRemoteCache(userID, ret);
				firePropertyChange(MiapeExtractionTask.NOTIFICATION, null, ret.size() + " projects loaded");
				return ret;
			} catch (MiapeDatabaseException_Exception e) {
				firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Error loading projects: " + e.getMessage() + "\n");
				return null;
			} catch (MiapeSecurityException_Exception e) {
				firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Error loading projects: " + e.getMessage() + "\n");
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				firePropertyChange(MiapeExtractionTask.NOTIFICATION, null,
						"Error loading projects: " + e.getMessage() + "\n");
				e.printStackTrace();
				return null;
			}
		}
	}

	private static synchronized void addToLocalCache(HashMap<Integer, String> projectsHashMap) {
		cachedLocalProjects.putAll(projectsHashMap);
	}

	private static synchronized Map<Integer, String> getLocalCachedProjects() {
		return cachedLocalProjects;
	}

	private static synchronized void addToRemoteCache(int userID, HashMap<Integer, String> projectsHashMap) {
		if (cachedRemoteProjects.containsKey(userID)) {
			final Map<Integer, String> map = cachedRemoteProjects.get(userID);
			log.info("This may not happen");
			map.putAll(projectsHashMap);
		} else {
			final Map<Integer, String> map = new HashMap<Integer, String>();
			map.putAll(projectsHashMap);
			log.info("Caching " + map.size() + " projects from user: " + userID);
			cachedRemoteProjects.put(userID, map);
		}
	}

	private static synchronized Map<Integer, String> getRemoteCachedProjects(int userID) {
		log.info("Getting cached projects from user: " + userID);
		if (cachedRemoteProjects.containsKey(userID)) {
			final Map<Integer, String> map = cachedRemoteProjects.get(userID);
			log.info("Returning " + map.size() + " cached projects from user: " + userID);
			return map;
		}
		log.info("No cached projects from user: " + userID);
		return new HashMap<Integer, String>();
	}

}
