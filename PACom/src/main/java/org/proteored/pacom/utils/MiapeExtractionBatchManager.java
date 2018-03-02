package org.proteored.pacom.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingWorker.StateValue;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.text.tsv.msi.TableTextFileSeparator;
import org.proteored.miapeapi.xml.ms.MIAPEMSXmlFile;
import org.proteored.miapeapi.xml.ms.MiapeMSXmlFactory;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.MainFrame;
import org.proteored.pacom.gui.MiapeExtractionFrame;
import org.proteored.pacom.gui.importjobs.InputFileType;
import org.proteored.pacom.gui.tasks.MiapeExtractionTask;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

public class MiapeExtractionBatchManager implements PropertyChangeListener {
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");
	public static final String START_MIAPE_EXTRACTION = "START";
	public static final String MZIDENTML = "MZIDENTML";
	public static final String MGF = "MGF";
	public static final String MZML = "MZML";
	public static final String PRIDE = "PRIDE";
	public static final String XTANDEM = "XTANDEM";
	public static final String DTASELECT = "DTASELECT";
	public static final String PEPXML = "PEPXML";
	public static final String TEXT_DATA_TABLE = "TEXT_TABLE";
	public static final String END_MIAPE_EXTRACTION = "END";
	public static final String METADATA = "METADATA";
	public static final String MIAPE_PROJECT = "PROJECT";
	public static final String MS_JOB_REF = "MS_JOB_REF";
	private static final Integer NUM_RETRIES = 1; // 1 means no retries
	public static final String MIAPE_BATCH_DONE = "Miape batch done";

	private final List<Integer> miapeExtractionQueueOrder = new ArrayList<Integer>();
	private final TIntObjectHashMap<MiapeExtractionTask> miapeExtractionTasks = new TIntObjectHashMap<MiapeExtractionTask>();
	private static int CONCURRENT_MIAPE_EXTRACTIONS = 1;
	private final File inputBatchFile;
	private final MiapeExtractionFrame listener;

	private final ControlVocabularyManager cvManager;
	/**
	 * Indicates how many times a MIAPE Extraction Task has been started
	 */
	private final TIntObjectHashMap<Integer> numStartsMap = new TIntObjectHashMap<Integer>();
	private final TIntHashSet completedJobs = new TIntHashSet();
	private int numTaskRunning;
	private final TIntHashSet failedJobs = new TIntHashSet();
	private final TIntHashSet runningJobs = new TIntHashSet();
	private boolean cancelAll = false;

	public MiapeExtractionBatchManager(File inputBatchFile, MiapeExtractionFrame listener,
			ControlVocabularyManager cvManager) {
		this.inputBatchFile = inputBatchFile;
		this.listener = listener;
		this.cvManager = cvManager;
		if (inputBatchFile != null) {
			parseInputBatchFile();
		}
	}

	public MiapeExtractionBatchManager(MiapeExtractionFrame listener, ControlVocabularyManager cvManager) {
		this(null, listener, cvManager);
	}

	public MiapeExtractionBatchManager(List<File> inputFiles, List<File> attachedMSFiles, InputFileType mode,
			String projectName, MiapeExtractionFrame listener, ControlVocabularyManager cvManager) {
		this(inputFiles, attachedMSFiles, mode, projectName, null, listener, cvManager);
	}

	public MiapeExtractionBatchManager(List<File> inputFiles, List<File> attachedMSFiles, InputFileType mode,
			String projectName, TableTextFileSeparator separator, MiapeExtractionFrame listener,
			ControlVocabularyManager cvManager) {
		this(listener, cvManager);
		createInputBatchTasksFromInputFiles(inputFiles, attachedMSFiles, mode, projectName, separator);

	}

	private void createInputBatchTasksFromInputFiles(List<File> inputFiles, List<File> attachedMSFiles,
			InputFileType mode, String projectName, TableTextFileSeparator separator) {
		int index = 0;
		for (File file : inputFiles) {
			File attachedMSFile = null;
			if (attachedMSFiles != null) {
				if (attachedMSFiles.size() == inputFiles.size()) {
					attachedMSFile = attachedMSFiles.get(index);
				} else if (attachedMSFiles.size() == 1) {
					attachedMSFile = attachedMSFiles.get(0);
				}
			}
			addImportTask(file, mode, attachedMSFile, projectName, separator);
			index++;
		}

	}

	/**
	 * creates and adds a task to the task queue and returns it
	 * 
	 * @param runID
	 * @param inputFile
	 * @param inputFileType
	 * @param attachedMSFile
	 * @param projectName
	 * @param separator
	 * @return
	 */
	public MiapeExtractionTask addImportTask(File inputFile, InputFileType inputFileType, File attachedMSFile,
			String projectName, TableTextFileSeparator separator) {
		MiapeExtractionRunParametersImpl params = new MiapeExtractionRunParametersImpl();
		params.setInputFile(inputFile);
		params.setProjectName(projectName);
		int jobID = getNextJobIDAvailable();

		params.setInputFileType(inputFileType);
		params.setSeparator(separator);

		try {
			params.consolidate();
		} catch (IllegalMiapeArgumentException e) {
			e.printStackTrace();

		}

		MiapeExtractionTask task = new MiapeExtractionTask(jobID, params);
		addTaskToQueue(task);
		log.info(task.getRunIdentifier() + " task added to queue");

		return task;
	}

	public List<MiapeExtractionTask> getMiapeExtractionQueue() {
		List<MiapeExtractionTask> ret = new ArrayList<MiapeExtractionTask>();
		for (Integer index : miapeExtractionQueueOrder) {
			ret.add(miapeExtractionTasks.get(index));
		}
		return ret;
	}

	private void parseInputBatchFile() {
		int numLine = 0;
		BufferedReader br = null;

		try {

			br = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(inputBatchFile))));
			if (br != null) {

				String strLine;
				MiapeExtractionRunParametersImpl params = null;
				while ((strLine = br.readLine()) != null) {
					strLine = strLine.trim();
					if (strLine.contains("#")) {
						String[] split = strLine.split("#");
						strLine = split[0];
					}

					numLine++;
					if (strLine.startsWith(START_MIAPE_EXTRACTION)) {

						params = new MiapeExtractionRunParametersImpl();

						String[] split = strLine.split("\t");

						params.setId(Integer.valueOf(split[1]));
					} else if (strLine.startsWith(MZIDENTML)) {
						String[] split = strLine.split("\t");
						File inputFile = new File(getLocationOfFile(split[1]));
						params.setInputFile(inputFile);
						if (params.isMGFSelected()) {
							params.setInputFileType(InputFileType.MZIDENTMLPLUSMGF);
						} else if (params.isMzMLSelected()) {
							params.setInputFileType(InputFileType.MZIDENTMLPLUSMZML);
						} else {
							params.setInputFileType(InputFileType.MZIDENTML);
						}
						if (!inputFile.exists() || !inputFile.isFile()) {
							throw new IllegalMiapeArgumentException(
									"File not found: " + params.getInputFileName() + " - line:" + numLine);
						}

					} else if (strLine.startsWith(MGF)) {
						String[] split = strLine.split("\t");
						if (params.isDTASelectSelected()) {
							params.setInputFileType(InputFileType.DTASELECTPLUSMGF);
						} else if (params.isMzIdentMLSelected()) {
							params.setInputFileType(InputFileType.MZIDENTMLPLUSMGF);
						} else if (params.isPepXMLSelected()) {
							params.setInputFileType(InputFileType.PEPXMLPLUSMGF);
						} else if (params.isXTandemSelected()) {
							params.setInputFileType(InputFileType.XTANDEMPLUSMGF);
						}
						File inputFile = new File(getLocationOfFile(split[1]));
						params.setInputFile(inputFile);
						if (!inputFile.exists() || !inputFile.isFile())
							throw new IllegalMiapeArgumentException("File not found: " + params.getInputFileName());

					} else if (strLine.startsWith(MZML)) {
						String[] split = strLine.split("\t");
						params.setInputFileType(InputFileType.MZIDENTMLPLUSMZML);
						File inputFile = new File(getLocationOfFile(split[1]));
						params.setInputFile(inputFile);
						if (!inputFile.exists() || !inputFile.isFile())
							throw new IllegalMiapeArgumentException("File not found: " + params.getInputFileName());

					} else if (strLine.startsWith(PRIDE)) {
						String[] split = strLine.split("\t");
						params.setInputFileType(InputFileType.PRIDEXML);
						File inputFile = new File(getLocationOfFile(split[1]));
						params.setInputFile(inputFile);
						if (!inputFile.exists() || !inputFile.isFile())
							throw new IllegalMiapeArgumentException("File not found: " + params.getInputFileName());

					} else if (strLine.startsWith(XTANDEM)) {
						String[] split = strLine.split("\t");
						if (params.isMGFSelected()) {
							params.setInputFileType(InputFileType.XTANDEMPLUSMGF);
						} else {
							params.setInputFileType(InputFileType.XTANDEM);
						}
						File inputFile = new File(getLocationOfFile(split[1]));
						params.setInputFile(inputFile);
						if (!inputFile.exists() || !inputFile.isFile())
							throw new IllegalMiapeArgumentException("File not found: " + params.getInputFileName());

					} else if (strLine.startsWith(DTASELECT)) {
						String[] split = strLine.split("\t");
						if (params.isMGFSelected()) {
							params.setInputFileType(InputFileType.DTASELECTPLUSMGF);
						} else {
							params.setInputFileType(InputFileType.DTASELECT);
						}
						File inputFile = new File(getLocationOfFile(split[1]));
						params.setInputFile(inputFile);
						if (!inputFile.exists() || !inputFile.isFile())
							throw new IllegalMiapeArgumentException("File not found: " + params.getInputFileName());

					} else if (strLine.startsWith(PEPXML)) {
						String[] split = strLine.split("\t");
						if (params.isMGFSelected()) {
							params.setInputFileType(InputFileType.PEPXMLPLUSMGF);
						} else {
							params.setInputFileType(InputFileType.PEPXML);
						}
						File inputFile = new File(getLocationOfFile(split[1]));
						params.setInputFile(inputFile);
						if (!inputFile.exists() || !inputFile.isFile())
							throw new IllegalMiapeArgumentException("File not found: " + params.getInputFileName());

					} else if (strLine.startsWith(TEXT_DATA_TABLE)) {
						String[] split = strLine.split("\t");
						params.setInputFileType(InputFileType.TABLETEXT);

						File inputFile = new File(getLocationOfFile(split[1]));
						params.setInputFile(inputFile);
						if (!inputFile.exists() || !inputFile.isFile())
							throw new IllegalMiapeArgumentException("File not found: " + params.getInputFileName());

					} else if (strLine.startsWith(MIAPE_PROJECT)) {
						String[] split = strLine.split("\t");
						params.setProjectName(split[1]);

					} else if (strLine.startsWith(METADATA)) {
						String[] split = strLine.split("\t");
						String metadataFileName = split[1];
						File metadataFile = FileManager.getMetadataFile(metadataFileName);
						if (metadataFile == null)
							throw new IllegalMiapeArgumentException("Metadata template '" + metadataFileName
									+ "' is not present at /userdata/miape_ms_metadata/ folder");
						if (cvManager == null)
							throw new IllegalMiapeArgumentException(
									"Please wait until ontologies are internally loaded. It will take just some seconds...");
						MIAPEMSXmlFile xmlFile = new MIAPEMSXmlFile(metadataFile);
						MiapeMSDocument metadataMiapeMS = MiapeMSXmlFactory.getFactory().toDocument(xmlFile, cvManager,
								null, null, null);

						params.setMiapeMSMetadata(metadataMiapeMS);
						params.setTemplateName(metadataFileName);
					} else if (strLine.startsWith(END_MIAPE_EXTRACTION)) {
						params.consolidate();
						MiapeExtractionTask task = new MiapeExtractionTask(params.getId(), params,

								MainFrame.parallelProcessingOnExtraction);
						addTaskToQueue(task);
					} else if (strLine.startsWith(MS_JOB_REF)) {
						String[] split = strLine.split("\t");
						try {
							Integer associatedMIAPEMSGeneratorJobID = Integer.valueOf(split[1]);
							params.setAssociatedMIAPEMSGeneratorJob(associatedMIAPEMSGeneratorJobID);
						} catch (NumberFormatException e) {
							throw new IllegalMiapeArgumentException(
									"A positive number should be located after 'MS_REF' tag");
						}
					} else if ("".equals(strLine.trim())) {
						log.info("Skipping blank line");
						continue;
					} else if (strLine.trim().startsWith("#")) {
						log.info("Skipiing comment:" + strLine);
						continue;
					} else {

						throw new IllegalMiapeArgumentException("Line not allowed: '" + strLine + "'");

					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof ArrayIndexOutOfBoundsException)
				throw new IllegalMiapeArgumentException("Error in batch import file: Line " + numLine
						+ " is bad formatted. Please ensure that separation between tags and values is a TAB character.");
			if (e.getMessage() != null && !e.getMessage().contains("Please wait"))
				throw new IllegalMiapeArgumentException(
						"Error in batch import file: " + e.getMessage() + " - line:" + numLine);
			throw new IllegalMiapeArgumentException("Error in batch import file: " + e.getMessage());
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * It checks whether the file exists or not. If exists, it returns the file
	 * path. If not, it will try it to localize the file as a relative path to
	 * the application.
	 * 
	 * @param string
	 * @return
	 */
	private String getLocationOfFile(String filePath) {
		if (filePath != null) {
			File file = new File(filePath.trim());
			if (file.exists()) {
				return file.getAbsolutePath();
			} else {
				file = new File(System.getProperty("user.dir") + File.separator + filePath);
				if (file.exists()) {
					log.info("File '" + filePath + "' found at application folder " + file.getParent());
					return file.getAbsolutePath();
				}
			}
		}
		// do nothing and return the same as input
		return filePath;
	}

	/**
	 * Before to add the task, checks if there is already a task with that
	 * identifier
	 *
	 * @param task
	 */
	public void addTaskToQueue(MiapeExtractionTask task) {
		for (Integer jobID : miapeExtractionQueueOrder) {
			if (jobID == task.getRunIdentifier())
				throw new IllegalMiapeArgumentException(
						"Duplicated MIAPE extraction job identifier: '" + task.getRunIdentifier() + "'");
		}
		miapeExtractionQueueOrder.add(task.getRunIdentifier());
		miapeExtractionTasks.put(task.getRunIdentifier(), task);

	}

	/**
	 * Starts the queue of {@link MiapeExtractionTask}.
	 * 
	 * @param startFailedJobs
	 */
	public synchronized boolean startMiapeExtractionNextInQueue() {

		log.info("Start MIAPE Extractions");
		log.info(miapeExtractionQueueOrder.size() + " tasks in the queue");
		log.info(getRunningJobs().size() + " tasks running");
		log.info(completedJobs.size() + " tasks completed");
		boolean allDone = true;
		if (getRunningJobs().size() < CONCURRENT_MIAPE_EXTRACTIONS) {
			for (Integer jobID : miapeExtractionQueueOrder) {
				MiapeExtractionTask miapeExtractionTask = miapeExtractionTasks.get(jobID);

				if (!completedJobs.contains(jobID)) {
					if (miapeExtractionTask.getState() == StateValue.PENDING) {
						boolean started = startMiapeExtraction(jobID);
						if (started) {
							allDone = false;
							return true;
						}
					}
					if (miapeExtractionTask.getState() != StateValue.DONE) {
						allDone = false;
					}
				}

			}
		}
		// if all are done, fire COMPLETION
		if (allDone) {
			checkFinishSignal();
		}
		return false;
	}

	private synchronized boolean startMiapeExtraction(int jobID) {

		MiapeExtractionTask miapeTask = null;
		if (numTaskRunning < CONCURRENT_MIAPE_EXTRACTIONS) {
			if (miapeExtractionTasks.containsKey(jobID)) {
				MiapeExtractionTask miapeExtractionTask = miapeExtractionTasks.get(jobID);

				// if (miapeExtractionTask.getRunIdentifier() == jobID) {
				if (miapeExtractionTask.getState() == StateValue.PENDING) {
					log.info("Executing job " + miapeExtractionTask.getRunIdentifier());
					miapeExtractionTask.addPropertyChangeListener(this);
					miapeExtractionTask.addPropertyChangeListener(listener);
					miapeExtractionTask.execute();

					// label this job as started once more
					increaseStartNumber(jobID);
					return true;
				} else if (miapeExtractionTask.getState() == StateValue.DONE) {
					if (numStartsMap.containsKey(jobID)) {
						Integer numStarts = numStartsMap.get(jobID);
						// if the task has been retried before twice, do not
						// start again
						if (numStarts > NUM_RETRIES) {
							log.info("Job " + jobID + " cannot be started more than " + NUM_RETRIES + " times");
							return false;
						}
					}
					log.info("The task " + jobID + " is going to be reanalyzer");
					miapeTask = new MiapeExtractionTask(miapeExtractionTask.getRunIdentifier(),
							miapeExtractionTask.getParameters(), miapeExtractionTask.isLocalProcessingInParallel());
					miapeTask.addPropertyChangeListener(this);
					miapeTask.addPropertyChangeListener(listener);
					Iterator<Integer> iterator = miapeExtractionQueueOrder.iterator();
					while (iterator.hasNext()) {
						Integer jobID2 = iterator.next();
						if (jobID2 == jobID)
							iterator.remove();
					}
					miapeExtractionTasks.remove(jobID);
					addTaskToQueue(miapeTask);
					miapeTask.execute();

					// label this job as started once more
					increaseStartNumber(jobID);
					return true;

				} else if (miapeExtractionTask.getState() == StateValue.STARTED) {
					log.info("Task " + jobID + " is already running");
				}
			}
		} else {
			log.info("Only " + MiapeExtractionBatchManager.CONCURRENT_MIAPE_EXTRACTIONS
					+ " are allowed to be running at once");
		}
		return false;
	}

	private void increaseStartNumber(int jobID) {

		if (numStartsMap.containsKey(jobID)) {
			Integer num = numStartsMap.get(jobID);
			num++;
			numStartsMap.remove(jobID);
			numStartsMap.put(jobID, num);
		} else {
			numStartsMap.put(jobID, 1);
		}
	}

	public synchronized void cancelMiapeExtractions() {
		log.info("Cancelling all tasks");
		cancelAll = true;
		// if none where started, fire MIAPE_BATCHDONE
		boolean someStarted = false;
		for (MiapeExtractionTask miapeExtractionTask : miapeExtractionTasks.valueCollection()) {
			if (miapeExtractionTask.getState() == StateValue.STARTED) {
				someStarted = true;
				while (true) {
					boolean cancelled = miapeExtractionTask.cancel(true);
					if (cancelled) {
						log.info("Task " + miapeExtractionTask.getRunIdentifier() + " cancelled=" + cancelled);
						break;
					}
				}
			}
		}
		if (!someStarted) {
			listener.propertyChange(new PropertyChangeEvent(this, MIAPE_BATCH_DONE, null, getStatisticsOnTasks()));
		}
	}

	public synchronized void cancelMiapeExtraction(int runID) {
		log.info("Cancelling task " + runID);
		MiapeExtractionTask miapeExtractionTask = miapeExtractionTasks.get(runID);
		if (miapeExtractionTask.getState() == StateValue.STARTED && miapeExtractionTask.getRunIdentifier() == runID) {
			miapeExtractionTask.cancel(true);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (MiapeExtractionTask.MIAPE_CREATION_TOTAL_DONE.equals(evt.getPropertyName())) {

			MiapeExtractionResult result = (MiapeExtractionResult) evt.getNewValue();
			log.info("Job " + result.getMiapeExtractionTaskIdentifier() + " MIAPE CREATION DONE MS:"
					+ result.getMiapeMS_Identifier() + " and MSI:" + result.getMiapeMSI_Identifier() + " in "
					+ result.getMilliseconds());
			log.info(getNumberOfPendingTasks() + " still in the queue");
			// remove from running job set
			runningJobs.remove(result.getMiapeExtractionTaskIdentifier());
			// print queue status
			printqueueStatus();
			// tag job a correctly completed
			completedJobs.add(result.getMiapeExtractionTaskIdentifier());

			// update, if any, any reference to MIAPE MS creation of this task
			updateMiapeMSGeneratorTaskReference(result);

			// update if the completed job was a failed one
			removeFromFailedJobs(result.getMiapeExtractionTaskIdentifier());

			numTaskRunning--;

			startMiapeExtractionNextInQueue();
			printqueueStatus();
			// if no more tasks are launched, fire the end
			checkFinishSignal();

		} else if (MiapeExtractionTask.MIAPE_CREATION_ERROR.equals(evt.getPropertyName())) {
			numTaskRunning--;

			MiapeExtractionResult result = (MiapeExtractionResult) evt.getNewValue();
			log.info("Error in task " + result.getMiapeExtractionTaskIdentifier() + " message: "
					+ result.getErrorMessage());
			log.info(getNumberOfPendingTasks() + " still in the queue");

			// add to failed job set
			failedJobs.add(result.getMiapeExtractionTaskIdentifier());
			// remove from running job set
			runningJobs.remove(result.getMiapeExtractionTaskIdentifier());

			startMiapeExtractionNextInQueue();

		} else if (MiapeExtractionTask.MIAPE_CREATION_CANCELED.equals(evt.getPropertyName())) {
			numTaskRunning--;

			int jobID = (Integer) evt.getNewValue();
			// remove from running job set
			runningJobs.remove(jobID);
			// add to failed jobs
			failedJobs.add(jobID);

			log.info(getNumberOfPendingTasks() + " still in the queue");

			if (!cancelAll)
				startMiapeExtractionNextInQueue();

		} else if (MiapeExtractionTask.MIAPE_CREATION_WAITING_FOR_OTHER_JOB_COMPLETION.equals(evt.getPropertyName())) {
			MiapeExtractionResult result = (MiapeExtractionResult) evt.getNewValue();
			log.info("Error message: " + result.getErrorMessage());
			log.info(getNumberOfPendingTasks() + " still in the queue");
			numTaskRunning--;

			// remove from running job set
			runningJobs.remove(result.getMiapeExtractionTaskIdentifier());

			startMiapeExtractionNextInQueue();

		} else if (MiapeExtractionTask.MIAPE_CREATION_STARTS.equals(evt.getPropertyName())) {
			numTaskRunning++;
			cancelAll = false;

			// add to running job set
			int jobID = (Integer) evt.getNewValue();
			runningJobs.add(jobID);
			startMiapeExtractionNextInQueue();

		}

	}

	private void checkFinishSignal() {
		if (getNumberOfPendingTasks() == 0 && getRunningJobs().size() == 0) {
			listener.propertyChange(new PropertyChangeEvent(this, MIAPE_BATCH_DONE, null, getStatisticsOnTasks()));
		} else {
			log.info("Still not finished!");
		}
	}

	private String getStatisticsOnTasks() {
		StringBuilder sb = new StringBuilder();
		int total = this.miapeExtractionTasks.size();
		int completed = this.completedJobs.size();
		int failedJobs = this.failedJobs.size();
		sb.append(total + " datasets to import: " + completed + " datasets imported. " + failedJobs + " failed.");
		return sb.toString();
	}

	private void removeFromFailedJobs(int miapeExtractionTaskIdentifier) {
		failedJobs.remove(miapeExtractionTaskIdentifier);
	}

	private void printqueueStatus() {
		log.info("Status of the job queue: " + miapeExtractionQueueOrder.size());
		for (Integer jobID : miapeExtractionQueueOrder) {
			MiapeExtractionTask task = miapeExtractionTasks.get(jobID);
			log.info(task.getRunIdentifier() + " -> " + task.getState() + " cancelled: " + task.isCancelled());
		}
	}

	private void updateMiapeMSGeneratorTaskReference(MiapeExtractionResult result) {
		for (MiapeExtractionTask task : miapeExtractionTasks.valueCollection()) {
			MiapeExtractionRunParameters taskParameters = task.getParameters();
			if (taskParameters != null) {
				Integer associatedMiapeMSGeneratorJob = taskParameters.getAssociatedMiapeMSGeneratorJob();
				if (associatedMiapeMSGeneratorJob != null) {
					if (associatedMiapeMSGeneratorJob == result.getMiapeExtractionTaskIdentifier()) {
						if (taskParameters instanceof MiapeExtractionRunParametersImpl) {
							((MiapeExtractionRunParametersImpl) taskParameters)
									.setAssociatedMiapeMS(result.getMiapeMS_Identifier());
							log.info("Associating result from Job :" + result.getMiapeExtractionTaskIdentifier()
									+ " to job " + task.getRunIdentifier() + " MIAPE MS="
									+ result.getMiapeMS_Identifier());
						}
					}
				}
			}

		}
	}

	private synchronized int getNumberOfPendingTasks() {
		int ret = 0;
		for (MiapeExtractionTask miapeTask : miapeExtractionTasks.valueCollection()) {
			if (miapeTask.getState() == StateValue.PENDING) {
				ret++;
			}
		}
		return ret;
	}

	public boolean isProcessing(int jobID) {
		if (miapeExtractionTasks.containsKey(jobID)) {
			MiapeExtractionTask miapeExtractionTask = miapeExtractionTasks.get(jobID);
			if (miapeExtractionTask.getState() == StateValue.STARTED)
				return true;
		}
		return false;
	}

	public TIntHashSet getFailedJobs() {
		return failedJobs;
	}

	public TIntHashSet getRunningJobs() {
		return runningJobs;
	}

	public static String getInputTypesString() {
		return "'" + MZIDENTML + "', '" + MZML + "', '" + DTASELECT + "', '" + XTANDEM + "', '" + PEPXML + "', '"
				+ PRIDE + "', '" + MGF + "'";
	}

	public MiapeExtractionTask getMiapeExtractionTaskByJobId(int jobID) {
		if (miapeExtractionTasks.containsKey(jobID)) {
			return miapeExtractionTasks.get(jobID);
		}
		return null;
	}

	private int getNextJobIDAvailable() {
		int jobID = miapeExtractionTasks.size();
		if (jobID == 0) {// starts by 1
			jobID = 1;
		}

		while (true)

		{
			boolean found = false;
			for (Integer id : miapeExtractionQueueOrder) {
				if (id == jobID) {
					found = true;
					break;
				}
			}
			if (found) {
				jobID++;
			} else {
				break;
			}
		}
		return jobID;
	}

}
