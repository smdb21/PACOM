package org.proteored.pacom.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingWorker.StateValue;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.miapeapi.webservice.clients.miapeextractor.MiapeExtractorDelegate;
import org.proteored.miapeapi.xml.ms.MIAPEMSXmlFile;
import org.proteored.miapeapi.xml.ms.MiapeMSXmlFactory;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.MainFrame;
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
	public static final String TEXT_DATA_TABLE = "TEXT_TABLE";
	public static final String END_MIAPE_EXTRACTION = "END";
	public static final String METADATA = "METADATA";
	public static final String MIAPE_PROJECT = "PROJECT";
	public static final String LOCAL_PROCESSING = "LOCAL";
	public static final String FAST_PARSING = "FAST_PARSING";
	public static final String MS_OUTPUT = "MS_OUTPUT";
	public static final String MSI_OUTPUT = "MSI_OUTPUT";
	public static final String MS_MSI_OUTPUT = "MS_MSI_OUTPUT";
	public static final String MS_JOB_REF = "MS_JOB_REF";
	public static final String MS_ID_REF = "MS_ID_REF";
	private static final Integer NUM_RETRIES = 2;

	private final List<Integer> miapeExtractionQueueOrder = new ArrayList<Integer>();
	private final TIntObjectHashMap<MiapeExtractionTask> miapeExtractionTasks = new TIntObjectHashMap<MiapeExtractionTask>();
	private static int CONCURRENT_MIAPE_EXTRACTIONS = 1;
	private final File inputBatchFile;
	private final PropertyChangeListener listener;
	private final MiapeExtractorDelegate miapeExtractorWebservice;
	private final MiapeAPIWebserviceDelegate miapeAPIWebservice;
	private final ControlVocabularyManager cvManager;
	/**
	 * Indicates how many times a MIAPE Extraction Task has been started
	 */
	private final TIntObjectHashMap<Integer> numStartsMap = new TIntObjectHashMap<Integer>();
	private final TIntHashSet completedJobs = new TIntHashSet();
	private int numTaskRunning;
	private final TIntHashSet failedJobs = new TIntHashSet();
	private final TIntHashSet runningJobs = new TIntHashSet();
	private boolean startFailedJobs = false;
	private boolean cancelAll = false;

	public MiapeExtractionBatchManager(File inputBatchFile, PropertyChangeListener listener,
			MiapeExtractorDelegate miapeExtractorWebservice, MiapeAPIWebserviceDelegate miapeAPIWebservice,
			ControlVocabularyManager cvManager) {
		this.inputBatchFile = inputBatchFile;
		this.miapeAPIWebservice = miapeAPIWebservice;
		this.miapeExtractorWebservice = miapeExtractorWebservice;
		this.listener = listener;

		this.cvManager = cvManager;
		parseInputBatchFile();

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

		try {
			final InputStream fstream = new FileInputStream(inputBatchFile);
			if (fstream != null) {
				DataInputStream in = new DataInputStream(fstream);

				BufferedReader br = new BufferedReader(new InputStreamReader(in));
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
						params.setMzIdentMLFileName(split[1]);
						params.setMIAPEMSIChecked(true);
						File inputFile = new File(params.getMzIdentMLFileName());
						if (!inputFile.exists() || !inputFile.isFile())
							throw new IllegalMiapeArgumentException(
									"File not found: " + params.getMzIdentMLFileName() + " - line:" + numLine);
						params.addInputFile(inputFile);

					} else if (strLine.startsWith(MGF)) {
						String[] split = strLine.split("\t");
						params.setMgfFileName(split[1]);
						File inputFile = new File(params.getMgfFileName());
						if (!inputFile.exists() || !inputFile.isFile())
							throw new IllegalMiapeArgumentException("File not found: " + params.getMgfFileName());
						params.addInputFile(inputFile);
						params.setMIAPEMSChecked(true);
					} else if (strLine.startsWith(MZML)) {
						String[] split = strLine.split("\t");
						params.setMzMLFileName(split[1]);
						File inputFile = new File(params.getMzMLFileName());
						if (!inputFile.exists() || !inputFile.isFile())
							throw new IllegalMiapeArgumentException("File not found: " + params.getMzMLFileName());
						params.addInputFile(inputFile);
						params.setMIAPEMSChecked(true);
					} else if (strLine.startsWith(PRIDE)) {
						String[] split = strLine.split("\t");
						params.setPRIDEXMLFileName(split[1]);
						File inputFile = new File(params.getPRIDEXMLFileName());
						if (!inputFile.exists() || !inputFile.isFile())
							throw new IllegalMiapeArgumentException("File not found: " + params.getPRIDEXMLFileName());
						params.addInputFile(inputFile);
					} else if (strLine.startsWith(XTANDEM)) {
						String[] split = strLine.split("\t");
						params.setxTandemFileName(split[1]);
						File inputFile = new File(params.getXTandemFileName());
						if (!inputFile.exists() || !inputFile.isFile())
							throw new IllegalMiapeArgumentException("File not found: " + params.getXTandemFileName());
						params.addInputFile(inputFile);
						params.setMIAPEMSIChecked(true);
					} else if (strLine.startsWith(DTASELECT)) {
						String[] split = strLine.split("\t");
						params.setDtaSelectFileName(split[1]);
						File inputFile = new File(params.getDtaSelectFileName());
						if (!inputFile.exists() || !inputFile.isFile())
							throw new IllegalMiapeArgumentException("File not found: " + params.getDtaSelectFileName());
						params.addInputFile(inputFile);
						params.setMIAPEMSIChecked(true);
					} else if (strLine.startsWith(TEXT_DATA_TABLE)) {
						String[] split = strLine.split("\t");
						params.setTSVFileName(split[1]);
						File inputFile = new File(params.getTSVSelectFileName());
						if (!inputFile.exists() || !inputFile.isFile())
							throw new IllegalMiapeArgumentException("File not found: " + params.getTSVSelectFileName());
						params.addInputFile(inputFile);
						params.setMIAPEMSIChecked(true);
					} else if (strLine.startsWith(MIAPE_PROJECT)) {
						String[] split = strLine.split("\t");
						params.setProjectName(split[1]);
					} else if (strLine.startsWith(LOCAL_PROCESSING)) {
						params.setLocalProcessing(true);
					} else if (strLine.startsWith(FAST_PARSING)) {
						params.setFastParsing(true);
					} else if (strLine.startsWith(MS_OUTPUT)) {
						params.setMIAPEMSChecked(true);
					} else if (strLine.startsWith(MSI_OUTPUT)) {
						params.setMIAPEMSIChecked(true);
					} else if (strLine.startsWith(MS_MSI_OUTPUT)) {
						params.setMIAPEMSChecked(true);
						params.setMIAPEMSIChecked(true);
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
								miapeExtractorWebservice, miapeAPIWebservice, MainFrame.userName, MainFrame.password,
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
					} else if (strLine.startsWith(MS_ID_REF)) {
						String[] split = strLine.split("\t");
						try {
							Integer associatedMIAPEMSRef = Integer.valueOf(split[1]);
							params.setAssociatedMiapeMS(associatedMIAPEMSRef);
						} catch (NumberFormatException e) {
							throw new IllegalMiapeArgumentException(
									"A positive number should be located after 'MS_ID_REF' tag");
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
				br.close();
				in.close();
				fstream.close();
			}
		} catch (Exception e) {
			if (e instanceof ArrayIndexOutOfBoundsException)
				throw new IllegalMiapeArgumentException("Line " + numLine
						+ " is bad formatted. Please ensure that separation between tags and values is a TAB character.");
			if (!e.getMessage().contains("Please wait"))
				throw new IllegalMiapeArgumentException(e.getMessage() + " - line:" + numLine);
			throw new IllegalMiapeArgumentException(e.getMessage());
		}
	}

	/**
	 * Before to add the task, checks if there is already a task with that
	 * identifier
	 *
	 * @param task
	 */
	private void addTaskToQueue(MiapeExtractionTask task) {
		for (Integer jobID : miapeExtractionQueueOrder) {
			if (jobID == task.getRunIdentifier())
				throw new IllegalMiapeArgumentException(
						"Duplicated MIAPE extraction job identifier: '" + task.getRunIdentifier() + "'");
		}
		miapeExtractionQueueOrder.add(task.getRunIdentifier());
		miapeExtractionTasks.put(task.getRunIdentifier(), task);

	}

	/**
	 * Starts the queue of {@link MiapeExtractionTask}. If failedJobs is true,
	 * just the failed jobs are being to be started.
	 *
	 * @param startFailedJobs
	 */
	public synchronized boolean startMiapeExtractions(boolean startFailedJobs) {
		this.startFailedJobs = startFailedJobs;
		log.info("Start MIAPE Extractions");
		log.info(miapeExtractionQueueOrder.size() + " tasks in the queue");
		log.info(numTaskRunning + " tasks running");
		log.info(completedJobs.size() + " tasks completed");
		if (numTaskRunning < CONCURRENT_MIAPE_EXTRACTIONS) {
			for (Integer jobID : miapeExtractionQueueOrder) {
				MiapeExtractionTask miapeExtractionTask = miapeExtractionTasks.get(jobID);

				if (startFailedJobs && failedJobs.contains(jobID)) {
					boolean started = startMiapeExtraction(jobID);
					if (started)
						// just launch one each time. The second one will be
						// launched when MIAPE_EXTRACTION_START signal is
						// received from this first one
						return true;
				} else {
					if (!completedJobs.contains(jobID)) {
						if (miapeExtractionTask.getState() == StateValue.PENDING) {
							boolean started = startMiapeExtraction(jobID);

							if (started)
								// just launch one each time. The second one
								// will be
								// launched when MIAPE_EXTRACTION_START signal
								// is
								// received from this first one
								return true;
						}
					}
				}
			}
		}
		return false;
	}

	public synchronized boolean startMiapeExtraction(int jobID) {

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
							miapeExtractionTask.getParameters(), miapeExtractorWebservice, miapeAPIWebservice,
							MainFrame.userName, MainFrame.password, miapeExtractionTask.isLocalProcessingInParallel());
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
		for (MiapeExtractionTask miapeExtractionTask : miapeExtractionTasks.valueCollection()) {
			if (miapeExtractionTask.getState() == StateValue.STARTED) {
				while (true) {
					boolean cancelled = miapeExtractionTask.cancel(true);
					if (cancelled) {
						log.info("Task " + miapeExtractionTask.getRunIdentifier() + " cancelled=" + cancelled);
						break;
					}
				}
			}
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

			startMiapeExtractions(startFailedJobs);

		} else if (MiapeExtractionTask.MIAPE_CREATION_ERROR.equals(evt.getPropertyName())) {
			numTaskRunning--;

			MiapeExtractionResult result = (MiapeExtractionResult) evt.getNewValue();
			log.info("Error message: " + result.getErrorMessage());
			log.info(getNumberOfPendingTasks() + " still in the queue");

			// add to failed job set
			failedJobs.add(result.getMiapeExtractionTaskIdentifier());
			// remove from running job set
			runningJobs.remove(result.getMiapeExtractionTaskIdentifier());

			startMiapeExtractions(startFailedJobs);
		} else if (MiapeExtractionTask.MIAPE_CREATION_CANCELED.equals(evt.getPropertyName())) {
			numTaskRunning--;

			int jobID = (Integer) evt.getNewValue();
			// remove from running job set
			runningJobs.remove(jobID);
			// add to failed jobs
			failedJobs.add(jobID);

			log.info(getNumberOfPendingTasks() + " still in the queue");

			if (!cancelAll)
				startMiapeExtractions(startFailedJobs);
		} else if (MiapeExtractionTask.MIAPE_CREATION_WAITING_FOR_OTHER_JOB_COMPLETION.equals(evt.getPropertyName())) {
			MiapeExtractionResult result = (MiapeExtractionResult) evt.getNewValue();
			log.info("Error message: " + result.getErrorMessage());
			log.info(getNumberOfPendingTasks() + " still in the queue");
			numTaskRunning--;

			// remove from running job set
			runningJobs.remove(result.getMiapeExtractionTaskIdentifier());

			startMiapeExtractions(startFailedJobs);
		} else if (MiapeExtractionTask.MIAPE_CREATION_STARTS.equals(evt.getPropertyName())) {
			numTaskRunning++;
			cancelAll = false;

			// add to running job set
			runningJobs.add((Integer) evt.getNewValue());
			startMiapeExtractions(startFailedJobs);

		}

	}

	private void removeFromFailedJobs(int miapeExtractionTaskIdentifier) {
		failedJobs.remove(miapeExtractionTaskIdentifier);
	}

	private void printqueueStatus() {
		log.info("CStatus of the job queue: " + miapeExtractionQueueOrder.size());
		for (Integer jobID : miapeExtractionQueueOrder) {
			MiapeExtractionTask task = miapeExtractionTasks.get(jobID);
			log.info(task.getRunIdentifier() + " -> " + task.getState());
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
			if (miapeTask.getState() == StateValue.PENDING)
				ret++;
			if (miapeTask.getState() == StateValue.DONE && !completedJobs.contains(miapeTask.getRunIdentifier()))
				ret++;

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
		return MZIDENTML + ", " + MZML + ", " + DTASELECT + ", " + XTANDEM + ", " + PRIDE + ", " + MGF;
	}
}
