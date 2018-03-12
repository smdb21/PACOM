package org.proteored.pacom.analysis.exporters.tasks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingWorker;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.interfaces.ms.ResultingData;
import org.proteored.miapeapi.util.URLParamEncoder;
import org.proteored.miapeapi.util.URLValidator;
import org.proteored.pacom.analysis.exporters.ProteomeXchangeFilev2_1;

import edu.scripps.yates.utilities.files.ZipManager;
import gnu.trove.set.hash.THashSet;
import sun.net.www.protocol.ftp.FtpURLConnection;

public class PEXBulkSubmissionFileDownloaderTask extends SwingWorker<Void, String> {

	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	public static final String PEX_FILE_DOWNLOADING_STARTED = "pex_downloading_started";
	public static final String PEX_FILE_DOWNLOADING_DOWNLOADING_FILES = "pex_downloading";
	public static final String PEX_FILE_DOWNLOADED = "pex file downloaded step";
	public static final String PEX_FILE_FOUND_IN_OUTPUT_FOLDER = "pex file found in output folder";
	public static final String PEX_FILE_DOWNLOADING_FINISH = "pex_downloading_finished";
	public static final String PEX_FILE_DOWNLOADING_ERROR = "pex_downloading_error";
	public static final String PEX_FILE_DOWNLOADING_EXPORTING_PRIDE_FILES = "pex_downloading_exporting_pride";
	public static final String PEX_FILE_COMPRESSION_STARTED = "pex_downloading_compression_started";
	public static final String PEX_FILE_COMPRESSION_FINISHED = "pex_downloading_compression_finished";

	private static final String FILE_SEPARATOR = System.getProperty("file.separator");

	public List<PexExportingMessage> messages = new ArrayList<PexExportingMessage>();
	private final ProteomeXchangeFilev2_1 pexFile;

	private int numDownloadedFiles = 0;

	private final boolean includeMSIAttachedFiles;

	private final boolean compressDownloadedData;

	private Map<Replicate, Set<String>> filesToSkip;

	private FTPClient ftp;

	public PEXBulkSubmissionFileDownloaderTask(ProteomeXchangeFilev2_1 pexFile, boolean includeMSIAttachedFiles,
			boolean includeMIAPEReports, boolean compressDownloadedData) {
		this.pexFile = pexFile;
		this.includeMSIAttachedFiles = includeMSIAttachedFiles;
		this.compressDownloadedData = compressDownloadedData;
	}

	@Override
	protected Void doInBackground() throws Exception {
		startFileDownloading();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		if (isCancelled()) {
			if (ftp != null) {
				try {
					ftp.abort();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		super.done();
	}

	private void startFileDownloading() {
		log.info("Starting pex downloading");

		try {
			filesToSkip = pexFile.getFilesToSkip();
			ExperimentList experimentList = pexFile.getExperimentList();
			Map<Replicate, List<ResultingData>> rawFilesByReplicate = experimentList
					.getRawFileResultingDataMapByReplicate();
			Map<Replicate, List<ResultingData>> peaklistFilesByReplicate = experimentList
					.getPeakListResultingDataMapByReplicate();
			Map<Replicate, List<String>> msiFilesByReplicate = null;
			if (includeMSIAttachedFiles) {
				msiFilesByReplicate = experimentList.getMSIGeneratedFilesByReplicate();
			}

			if ((rawFilesByReplicate != null && !rawFilesByReplicate.isEmpty())
					|| (peaklistFilesByReplicate != null && !peaklistFilesByReplicate.isEmpty())
					|| (msiFilesByReplicate != null && !msiFilesByReplicate.isEmpty())) {

				// peak lists
				if (peaklistFilesByReplicate != null && !peaklistFilesByReplicate.isEmpty()) {

					for (Replicate rep : peaklistFilesByReplicate.keySet()) {
						Set<String> filesToSkip = this.filesToSkip.get(rep);
						List<URL> urls = new ArrayList<URL>();
						List<ResultingData> resultingDatas = peaklistFilesByReplicate.get(rep);
						urls.addAll(getFileUrlsFromResultingDatas(resultingDatas, filesToSkip));
						List<File> downloadedFiles = downloadFiles(urls);
						pexFile.addReplicatePeakListFiles(rep, downloadedFiles);
					}
				}
				// rawFiles
				if (rawFilesByReplicate != null && !rawFilesByReplicate.isEmpty()) {
					for (Replicate rep : rawFilesByReplicate.keySet()) {
						Set<String> filesToSkip = this.filesToSkip.get(rep);
						List<URL> urls = new ArrayList<URL>();
						List<ResultingData> resultingDatas = rawFilesByReplicate.get(rep);
						urls.addAll(getFileUrlsFromResultingDatas(resultingDatas, filesToSkip));
						List<File> downloadedFiles = downloadFiles(urls);
						pexFile.addReplicateRawFiles(rep, downloadedFiles);
					}
				}

				// search engine output files
				if (msiFilesByReplicate != null && !msiFilesByReplicate.isEmpty()) {
					for (Replicate rep : msiFilesByReplicate.keySet()) {
						Set<String> filesToSkip = this.filesToSkip.get(rep);
						List<URL> urls = new ArrayList<URL>();
						List<String> attachedFiles = msiFilesByReplicate.get(rep);
						urls.addAll(getFileUrlsFromInputDatas(attachedFiles, filesToSkip));
						List<File> downloadedFiles = downloadFiles(urls);
						pexFile.addReplicateSearchEngineOutputFiles(rep, downloadedFiles);
					}
				}
			}

			firePropertyChange(PEX_FILE_DOWNLOADING_FINISH, null, numDownloadedFiles);
		} catch (Exception e) {
			e.printStackTrace();
			firePropertyChange(PEX_FILE_DOWNLOADING_ERROR, null, e.getMessage());
		}
	}

	private List<File> downloadFiles(List<URL> urls) {
		List<File> downloadedFiles = new ArrayList<File>();
		if (urls != null) {
			for (URL url : urls) {
				File downloadedFile = downloadFile(url);
				if (downloadedFile != null)
					downloadedFiles.add(downloadedFile);
			}
		}
		return downloadedFiles;
	}

	private Collection<URL> getFileUrlsFromResultingDatas(List<ResultingData> resultingDatas, Set<String> filesToSkip) {
		if (resultingDatas != null) {
			Collection<URL> ret = new THashSet<URL>();
			for (ResultingData resultingData : resultingDatas) {
				String dataFileUri = resultingData.getDataFileUri();
				if (filesToSkip != null && filesToSkip.contains(FilenameUtils.getName(dataFileUri)))
					continue;
				log.info("File url=" + dataFileUri);
				try {
					URL url = new URL(dataFileUri);
					if (!URLValidator.validateURL(url))
						continue;
					ret.add(url);
				} catch (MalformedURLException e) {
					log.warn("Error dowloading file from " + dataFileUri);
					log.warn(e.getMessage());
				}
			}
			return ret;
		}
		return null;
	}

	private Collection<URL> getFileUrlsFromInputDatas(List<String> inputDatas, Set<String> filesToSkip) {
		if (inputDatas != null) {
			Collection<URL> ret = new THashSet<URL>();
			for (String dataFileUri : inputDatas) {
				if (filesToSkip != null && filesToSkip.contains(FilenameUtils.getName(dataFileUri)))
					continue;
				log.info("File url=" + dataFileUri);
				try {
					URL url = new URL(dataFileUri);
					if (!URLValidator.validateURL(url))
						continue;
					ret.add(url);
				} catch (MalformedURLException e) {
					e.printStackTrace();
					log.warn("Error dowloading file from " + dataFileUri);
					log.warn(e.getMessage());
				}
			}
			return ret;
		}
		return null;
	}

	/**
	 * Download file to the output folder defined by the PEXfile object.<br>
	 * If the file already exists on the output folder, skip that download.<br>
	 * 
	 * @param url
	 * @return
	 */
	private File downloadFile(URL url) {
		try {
			firePropertyChange(PEX_FILE_DOWNLOADING_STARTED, null, url.toString());
			URL encodedURL = new URL(URLParamEncoder.encode(url.toString()));
			String localName = FilenameUtils.getName(encodedURL.getFile());
			localName = localName.replace("%20", " ");
			File downloadedFile = new File(pexFile.getOutputFolder() + FILE_SEPARATOR + localName);
			if (downloadedFile.exists() && downloadedFile.length() > 0) {
				log.info("File " + downloadedFile.getAbsolutePath() + " found in the output folder");
				firePropertyChange(PEX_FILE_FOUND_IN_OUTPUT_FOLDER, null, downloadedFile);
				numDownloadedFiles++;
				return downloadedFile;
			}

			log.info("Downloading file: " + encodedURL);
			URLConnection inputConnection = encodedURL.openConnection();
			final BufferedOutputStream local = new BufferedOutputStream(new FileOutputStream(downloadedFile));
			if (inputConnection instanceof FtpURLConnection) {
				String userName = null;
				String password = null;
				if (url.getUserInfo() != null) {
					if (url.getUserInfo().contains(":")) {
						String[] split = url.getUserInfo().split(":");
						userName = split[0];
						password = split[1];
					} else {
						userName = url.getUserInfo();
					}
				}
				ftp = new org.apache.commons.net.ftp.FTPClient();
				try {
					ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
					ftp.setConnectTimeout(5000);
					ftp.setDataTimeout(5000);
					ftp.setDefaultTimeout(5000);
					ftp.connect(url.getHost());
					int reply = ftp.getReplyCode();
					if (FTPReply.isPositiveCompletion(reply)) {
						boolean logged = true;
						if (userName != null)
							logged = ftp.login(userName, password);

						if (logged) {
							// ftp.enterRemotePassiveMode();
							log.info("getting inputstream");
							try {
								ftp.retrieveFile(url.getFile(), local);
							} finally {
								local.close();
							}
						}

					}
				} finally {
					ftp.logout();
					if (ftp.isConnected())
						ftp.disconnect();
				}
			} else {
				BufferedInputStream inputStream = new BufferedInputStream(inputConnection.getInputStream());
				ZipManager.copyInputStream(inputStream, local);
				local.close();
			}

			log.info("File downloaded to: " + downloadedFile.getAbsolutePath());
			firePropertyChange(PEX_FILE_DOWNLOADED, null, downloadedFile);
			numDownloadedFiles++;

			String extension = FilenameUtils.getExtension(downloadedFile.getAbsolutePath());
			if (compressDownloadedData && !"gz".equalsIgnoreCase(extension) && !"rar".equalsIgnoreCase(extension)
					&& !"zip".equalsIgnoreCase(extension) && !"gzip".equalsIgnoreCase(extension)) {
				firePropertyChange(PEX_FILE_COMPRESSION_STARTED, null, downloadedFile);
				log.info("Compressing file");
				File compressedFile = ZipManager.compressGZipFile(downloadedFile);
				firePropertyChange(PEX_FILE_COMPRESSION_FINISHED, null, compressedFile);
				return compressedFile;
			}

			return downloadedFile;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

}
