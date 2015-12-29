package org.proteored.miapeExtractor.analysis.gui.pike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.proteored.miapeExtractor.utils.ClientHttpRequest;

public class Miape2PIKETask extends SwingWorker<String, Void> {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private ClientHttpRequest pikeClient;

	public Miape2PIKETask(ClientHttpRequest client) {
		this.pikeClient = client;

	}

	@Override
	protected String doInBackground() throws Exception {
		try {
			log.info("Sending query to PIKE");
			final InputStream post = this.pikeClient.post();

			final String results = convertinputStreamToString(post);
			// final Writer writer = new BufferedWriter(new
			// OutputStreamWriter(new FileOutputStream(
			// new File("c:\\output.txt"))));
			// int charsRead;
			// char[] cbuf = new char[1024];
			// Reader reader = new InputStreamReader(post);
			// while ((charsRead = reader.read(cbuf)) != -1) {
			// writer.write(cbuf, 0, charsRead);
			// }
			// writer.flush();
			// writer.close();
			// reader.close();
			return results;
		} catch (Exception e) {
			return null;
		}
	}

	public String convertinputStreamToString(InputStream post) throws IOException {
		log.info("converting inputStream to String");

		if (post != null) {
			StringBuilder sb = new StringBuilder();

			try {
				BufferedReader r1 = new BufferedReader(new InputStreamReader(post));
				char[] cbuf = new char[1024];
				int numBytes = 0;
				while (numBytes != -1) {
					numBytes = r1.read(cbuf);
					sb.append(cbuf);
				}
			} finally {
				post.close();
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	@Override
	protected void done() {
		try {
			String results = get();
			log.info("Results recevied: " + results);
			final URL resultsURL = getResultsURLFromResults(results);
			log.info("Returning TASKDONE with URL = " + resultsURL);
			firePropertyChange(Miape2PIKEFrame.TASK_DONE, null, resultsURL);

		} catch (InterruptedException e) {
			e.printStackTrace();
			firePropertyChange(Miape2PIKEFrame.TASK_ERROR, null, e.getMessage());
		} catch (ExecutionException e) {
			e.printStackTrace();
			firePropertyChange(Miape2PIKEFrame.TASK_ERROR, null, e.getMessage());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			firePropertyChange(Miape2PIKEFrame.TASK_ERROR, null, e.getMessage());
		}
	}

	private URL getResultsURLFromResults(String results) throws MalformedURLException {
		final Pattern p = Pattern
				.compile(".*href=\"(http://proteo.cnb.csic.es/pike/userdata/\\S+).*");
		Matcher m = p.matcher(results);
		if (m.find()) {
			String urlString = m.group(1);
			if (urlString.endsWith("\""))
				urlString = (String) urlString.subSequence(0, urlString.length() - 1);
			log.info("Found URL: " + urlString);
			return new URL(urlString);
		}
		return null;
	}
}
