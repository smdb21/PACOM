package org.proteored.miapeExtractor.analysis.gui.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingWorker.StateValue;

import org.apache.log4j.Logger;
import org.proteored.miapeExtractor.analysis.util.FileManager;
import org.proteored.miapeExtractor.gui.tasks.WebservicesLoaderTask;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.interfaces.MiapeHeader;
import org.proteored.miapeapi.util.IntegerString;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeDatabaseException_Exception;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeSecurityException_Exception;

/**
 * This class manages the miape retriever tasks in a thread safe way
 * 
 * @author Salva
 * 
 */
public class MiapeRetrieverManager implements PropertyChangeListener {

	// private static final int CONCURRENT_RETRIEVINGS = 20;

	public static final int CONCURRENT_RETRIEVINGS = 2;

	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private final HashMap<String, MiapeRetrieverTask> miapeRetrievers = new HashMap<String, MiapeRetrieverTask>();
	private final List<String> queue = new ArrayList<String>();
	private final HashMap<Integer, Integer> miapeAssociations = new HashMap<Integer, Integer>();

	private MiapeAPIWebserviceDelegate miapeAPIWebservice;

	private static MiapeRetrieverManager instance;
	private String userName;
	private String password;
	private final List<PropertyChangeListener> retrievingListeners = new ArrayList<PropertyChangeListener>();
	private final Set<String> failedRetrievings = new HashSet<String>();

	private MiapeRetrieverManager(String userName, String password) {
		this.userName = userName;
		this.password = password;
		WebservicesLoaderTask webserviceLoader = WebservicesLoaderTask
				.getInstace();
		webserviceLoader.addPropertyChangeListener(this);
		webserviceLoader.execute();
	}

	public static MiapeRetrieverManager getInstance(String userName,
			String password) {
		if (instance == null)
			instance = new MiapeRetrieverManager(userName, password);

		instance.userName = userName;
		instance.password = password;
		return instance;
	}

	public String addRetrieving(Integer miapeID, String miapeType,
			PropertyChangeListener listener) {
		if (listener != null && !retrievingListeners.contains(listener))
			retrievingListeners.add(listener);

		if (miapeID == null || miapeID <= 0)
			throw new IllegalMiapeArgumentException("Error: invalid MIAPE ID: "
					+ miapeID);

		// Check if it is already retrieved
		if (miapeType.equals("MSI")) {
			File file = new File(FileManager.getMiapeMSIXMLFilePath(miapeID));
			if (file.exists()
					&& file.lastModified() > Long.valueOf("1349251004048")) {
				startAssociatedMiapeMSRetrieving(miapeID);
				// log.info("MIAPE MSI " + miapeID + " found in local system");
				return null;
			}
		} else if (miapeType.equals("MS")) {
			File file = new File(FileManager.getMiapeMSXMLFilePath(miapeID));
			if (file.exists()
					&& file.lastModified() > Long.valueOf("1349251004048")) {
				// log.info("MIAPE MS " + miapeID + " found in local system");
				return null;
			}
		} else if (miapeType.equals("GE")) {
			File file = new File(FileManager.getMiapeGEXMLFilePath(miapeID));
			if (file.exists()
					&& file.lastModified() > Long.valueOf("1349251004048")) {
				// log.info("MIAPE GE " + miapeID + " found in local system");
				return null;
			}
		} else if (miapeType.equals("GI")) {
			File file = new File(FileManager.getMiapeGIXMLFilePath(miapeID));
			if (file.exists()
					&& file.lastModified() > Long.valueOf("1349251004048")) {
				// log.info("MIAPE GI " + miapeID + " found in local system");
				return null;
			}
		}

		while (miapeAPIWebservice == null) {
			miapeAPIWebservice = WebservicesLoaderTask.getInstace()
					.getMiapeAPIWebservice(true);
			try {
				Thread.sleep(500);
				log.info("Waiting for the webservce initialization");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (!miapeRetrievers.containsKey(miapeType + miapeID)) {
			MiapeRetrieverTask retriever = new MiapeRetrieverTask(miapeID,
					miapeAPIWebservice, userName, password, miapeType);
			if (retriever != null) {
				retriever.addPropertyChangeListener(this);
				setListenersToRetrieverTask(retriever);
				log.info("adding retrieving of MIAPE " + miapeType + " :"
						+ miapeID);

				miapeRetrievers.put(miapeType + miapeID, retriever);
				queue.add(miapeType + miapeID);
			}
		} else {
			log.info("retrieving of MIAPE MSI : " + miapeID
					+ " is already started");
			// add listener if not already present
			miapeRetrievers.get(miapeType + miapeID)
					.removePropertyChangeListener(listener);
			miapeRetrievers.get(miapeType + miapeID).addPropertyChangeListener(
					listener);
		}
		// execute the following
		return executeRetrieving(null, null);
	}

	private String getKey(String miapeType, Integer miapeID) {
		return miapeType + miapeID;
	}

	private void setListenersToRetrieverTask(MiapeRetrieverTask retriever) {
		for (PropertyChangeListener listener : retrievingListeners) {
			retriever.addPropertyChangeListener(listener);
		}

	}

	public String addRetrievingWithPriority(Integer miapeID, String miapeType,
			PropertyChangeListener listener) {
		if (listener != null && !retrievingListeners.contains(listener))
			retrievingListeners.add(listener);

		String pair = miapeType + miapeID;

		String ret = addRetrieving(miapeID, miapeType, listener);
		MiapeRetrieverTask miapeMsiRetrieverTask = miapeRetrievers
				.get(miapeType + miapeID);

		if (miapeMsiRetrieverTask != null) {
			StateValue state = miapeMsiRetrieverTask.getState();
			if (state.equals(StateValue.PENDING)) {
				// delete from queue
				queue.remove(pair);
				// and insert at first position in the queue
				queue.add(0, pair);
			}
		} else {
			// and insert at first position in the queue
			queue.add(0, pair);
		}

		return enumerate();
	}

	private void removeRetrieving(Integer miapeID, String miapeType) {

		if (miapeID == null || miapeID <= 0 || miapeType == null)
			throw new IllegalMiapeArgumentException("Error: invalid MIAPE ID: "
					+ miapeID);
		String miapeIDTypePair = miapeType + miapeID;
		if (miapeRetrievers.containsKey(miapeIDTypePair)) {
			log.info("Removing retrieving task: " + miapeID + " " + miapeType);
			MiapeRetrieverTask miapeRetrieverTask = miapeRetrievers
					.get(miapeIDTypePair);

			if (miapeRetrieverTask.getState().equals(StateValue.STARTED))
				miapeRetrieverTask.cancel(true);
			miapeRetrievers.remove(miapeIDTypePair);
			int indexToRemove = -1;
			if (queue.contains(miapeIDTypePair)) {
				for (int i = 0; i < queue.size(); i++) {
					String queueElement = queue.get(i);
					if (queueElement.equals(miapeIDTypePair))
						indexToRemove = i;
				}
			}
			if (indexToRemove >= 0)
				queue.remove(indexToRemove);

		}
	}

	/**
	 * Execute a miape retrieving. If miapeID and miapeType is null, an
	 * available task will be started
	 * 
	 * @param miapeID
	 * @return
	 */
	private String executeRetrieving(Integer miapeID, String miapeType) {
		String key = miapeType + miapeID;
		if (miapeID != null && miapeType != null
				&& miapeRetrievers.containsKey(key)) {
			log.info("executing retrieving of MIAPE MSI :" + miapeID);
			MiapeRetrieverTask miapeMsiRetrieverTask = miapeRetrievers.get(key);
			miapeMsiRetrieverTask.execute();
		} else if (miapeRetrievers.size() > 0) {
			log.info("executing pending retrieving tasks");
			int numExecuting = 0;
			final int numStartedRetrievers = getNumRetrievers(StateValue.STARTED);
			if (numStartedRetrievers < CONCURRENT_RETRIEVINGS) {
				for (String pair : queue) {
					MiapeRetrieverTask retr = miapeRetrievers.get(pair);
					if (retr != null) {
						final StateValue state = retr.getState();
						if (StateValue.STARTED.equals(state)) {
							numExecuting++;
						} else if (numExecuting < CONCURRENT_RETRIEVINGS
								&& StateValue.PENDING.equals(state)) {
							log.info("executing PENDING MIAPE "
									+ retr.getMiapeType() + ":"
									+ retr.getMiapeID() + " retrieving task");
							retr.execute();
							numExecuting++;
						}
					}
				}
			}

		}
		// log.info("------------retrievers status:");
		// int i = 1;
		// for (MiapeMsiRetrieverTask retriver : miapeRetrievers.values()) {
		// log.info(i++ + "- retriever : " + retriver.getMiapeID() + " - " +
		// retriver.getState());
		// }

		return enumerate();
	}

	public String enumerate() {
		String ret = "";
		int ret2 = 0;
		// log.info("enumerating retrievers");
		List<String> toDelete = new ArrayList<String>();
		for (String pair : queue) {
			MiapeRetrieverTask retr = miapeRetrievers.get(pair);
			if (retr != null) {
				if (StateValue.STARTED.equals(retr.getState())) {
					if (ret != null && !"".equals(ret))
						ret = ret + ", ";
					ret = ret + retr.getMiapeType() + ":" + retr.getMiapeID();
				} else if (StateValue.PENDING.equals(retr.getState())) {

					ret2++;
				} else if (StateValue.DONE.equals(retr.getState())) {
					toDelete.add(pair);
				}
			}
		}
		for (String pair : toDelete) {
			IntegerString is = getIntegerString(pair);
			removeRetrieving(is.getMiapeID(), is.getMiapeType());
		}
		if (ret2 > 0)
			ret = ret + " (and " + ret2 + " more waiting in the queue)";
		// log.info("enumeration = " + ret);

		// log.info(this.miapeRetrievers.size() + " - " + this.queue.size());
		return ret;
	}

	private IntegerString getIntegerString(String pair) {
		if (pair.startsWith("MSI")) {
			return new IntegerString(Integer.valueOf(pair.substring(pair
					.indexOf("I") + 1)), "MSI");
		}
		if (pair.startsWith("MS")) {
			return new IntegerString(Integer.valueOf(pair.substring(pair
					.indexOf("S") + 1)), "MS");
		}
		if (pair.startsWith("GE")) {
			return new IntegerString(Integer.valueOf(pair.substring(pair
					.indexOf("E") + 1)), "GE");
		}
		if (pair.startsWith("GI")) {
			return new IntegerString(Integer.valueOf(pair.substring(pair
					.indexOf("I") + 1)), "GI");
		}
		return null;
	}

	public void cancelAll() {
		for (String id : queue) {
			MiapeRetrieverTask retr = miapeRetrievers.get(id);
			if (retr != null) {
				log.info("canceling retrieving of MIAPE MSI :"
						+ retr.getMiapeID());
				retr.cancel(true);
			}
		}
	}

	public int getSize() {
		// log.info("\nretrievers status:");
		// for (MiapeMsiRetrieverTask retriver : miapeRetrievers.values()) {
		// log.info("retriever : " + retriver.getMiapeID() + " - " +
		// retriver.getState());
		// }
		int size = miapeRetrievers.size();
		// log.info("returning size=" + size);
		return size;
	}

	private int getNumRetrievers(StateValue state) {
		int ret = 0;
		if (miapeRetrievers != null) {
			for (MiapeRetrieverTask retriever : miapeRetrievers.values()) {
				if (retriever.getState().equals(state))
					ret++;
			}
		}
		return ret;
	}

	@Override
	public synchronized void propertyChange(PropertyChangeEvent evt) {

		if (WebservicesLoaderTask.WEBSERVICES_LOADED.equals(evt
				.getPropertyName())) {
			Object[] ret = (Object[]) evt.getNewValue();
			if (ret.length == 2) {
				miapeAPIWebservice = (MiapeAPIWebserviceDelegate) ret[0];
			}
		} else if (MiapeRetrieverTask.MIAPE_LOADER_DONE.equals(evt
				.getPropertyName())) {
			String message = (String) evt.getNewValue();
			if (message.contains(MiapeRetrieverTask.MESSAGE_SPLITTER)) {
				Object[] splitted = message
						.split(MiapeRetrieverTask.SCAPED_MESSAGE_SPLITTER);

				int miapeID = Integer.valueOf((String) splitted[0]);
				String miapeType = (String) splitted[1];
				log.debug("MIAPE " + miapeType + " " + miapeID + " loaded");

				removeRetrieving(miapeID, miapeType);

				// execute the next retrieving if available
				if (getSize() > 0) {
					log.info("Now trying to execute another retrieving task");
					executeRetrieving(null, null);
				}
				// if it is a MSI miape, check the associated MS
				if ("MSI".equals(miapeType))
					startAssociatedMiapeMSRetrieving(miapeID);
			}
		} else if (MiapeRetrieverTask.MIAPE_LOADER_ERROR.equals(evt
				.getPropertyName())) {
			String message = (String) evt.getNewValue();
			if (message.contains(MiapeRetrieverTask.MESSAGE_SPLITTER)) {
				Object[] splitted = message
						.split(MiapeRetrieverTask.SCAPED_MESSAGE_SPLITTER);

				int miapeID = Integer.valueOf((String) splitted[0]);
				String miapeType = (String) splitted[1];
				String errorMessage = (String) splitted[2];
				log.info("Error downloading MIAPE " + miapeType + " " + miapeID
						+ ": " + errorMessage);
				// remove retrieving
				removeRetrieving(miapeID, miapeType);

				boolean retry = true;
				if (failedRetrievings.contains(getKey(miapeType, miapeID)))
					retry = false;
				if (retry) {
					// add to failed retrieving in order to not repeat the
					// retriving if fails again
					failedRetrievings.add(getKey(miapeType, miapeID));
					// add again to the queue
					addRetrieving(miapeID, miapeType, this);

				}
			}
		}
	}

	private void startAssociatedMiapeMSRetrieving(int miapeID) {
		if (userName == null || password == null)
			return;
		// TODO: parallelizate
		// Search for associated MS
		log.debug("Getting MIAPE MSI Header id = " + miapeID);
		try {
			final File msiFile = new File(
					FileManager.getMiapeMSIXMLFilePath(miapeID));
			MiapeHeader miapeHeader = null;
			if (msiFile.exists()) {
				miapeHeader = new MiapeHeader(msiFile, false);
			} else {

				if (miapeAPIWebservice == null)
					miapeAPIWebservice = WebservicesLoaderTask.getInstace()
							.getMiapeAPIWebservice(true);
				byte[] miapeHeaderBytes = miapeAPIWebservice
						.getMiapeMSIHeaderById(miapeID, userName, password);
				if (miapeHeaderBytes != null) {
					miapeHeader = new MiapeHeader(miapeHeaderBytes);

				}
			}
			if (miapeHeader != null) {
				final int miapeMSId = miapeHeader.getMiapeRef();
				if (miapeMSId > 0) {
					// Download MIAPE MS
					addRetrieving(miapeMSId, "MS", this);
					miapeAssociations.put(miapeID, miapeMSId);
				}
			}
		} catch (MiapeDatabaseException_Exception e) {
			e.printStackTrace();
		} catch (MiapeSecurityException_Exception e) {
			e.printStackTrace();
		}

	}

	public HashMap<Integer, Integer> getMiapeAssociations() {
		return miapeAssociations;
	}

	/**
	 * Gets the state of the {@link MiapeRetrieverTask} if the MIAPE MSI with
	 * that ID is in the queue. Or null if not.
	 * 
	 * @param miapeID
	 * @return
	 */
	public StateValue getDownloadingState(String miapeType, Integer miapeID) {
		if (miapeRetrievers.containsKey(getKey(miapeType, miapeID)))
			return miapeRetrievers.get(getKey(miapeType, miapeID)).getState();

		return null;
	}

	// Sets the listener to all the pending retrievers
	public void setListener(PropertyChangeListener listener) {
		for (MiapeRetrieverTask retriever : miapeRetrievers.values()) {
			retriever.removePropertyChangeListener(listener);
			retriever.addPropertyChangeListener(listener);
		}
		retrievingListeners.add(listener);
	}
}
