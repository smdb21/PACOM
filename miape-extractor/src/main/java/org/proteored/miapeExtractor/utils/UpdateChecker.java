package org.proteored.miapeExtractor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check whether there is a new update
 * <p/>
 * User: rwang Date: 11-Nov-2010 Time: 17:19:36
 */
public class UpdateChecker {

	public static final Logger logger = LoggerFactory
			.getLogger(UpdateChecker.class);
	private static UpdateChecker instance;

	public static UpdateChecker getInstance() {
		if (instance == null)
			instance = new UpdateChecker();
		return instance;
	}

	private UpdateChecker() {

	}

	/**
	 * Check whether there is a new update
	 * 
	 * @return boolean return true if there is a new update.
	 */
	public boolean hasUpdate() {
		boolean toUpdate = false;
		BufferedReader reader = null;
		// if (InternetChecker.check()) {
		// get the url for checking the update

		try {
			// Get properties from resource file
			Properties prop = PropertiesReader.getProperties();
			String version = prop
					.getProperty(PropertiesReader.MIAPE_EXTRACTOR_VERSION);
			String website = prop
					.getProperty(PropertiesReader.MIAPE_EXTRACTOR_WEBSITE);

			URL url = new URL(website + "/v" + version + "/MIAPE_Extractor_v"
					+ version + ".zip");
			// connect to the url
			int response = ((HttpURLConnection) url.openConnection())
					.getResponseCode();
			if (response == 404) {
				toUpdate = true;
			} else {
				// parse the web page
				reader = new BufferedReader(new InputStreamReader(
						url.openStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.toLowerCase().contains("label:deprecated")) {
						toUpdate = true;
						break;
					}
				}
			}
		} catch (Exception e) {
			logger.warn("Failed to check for updates", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.warn("Failed to check for updates");
				}
			}
		}
		// }

		return toUpdate;
	}

	/**
	 * Show update dialog
	 */
	public void showUpdateDialog() {
		int option = JOptionPane
				.showConfirmDialog(
						null,
						"<html><b>A new version of MIAPE Extractor is available</b>.<br><br> "
								+ "<font color=\"red\">It is strongly recommended to install the new version as soon as possible</font>.<br>"
								+ "Would you like to update?</html>",
						"Update Info", JOptionPane.YES_NO_OPTION);

		if (option == JOptionPane.YES_OPTION) {
			// Get properties from resource file
			Properties prop;
			try {
				prop = PropertiesReader.getProperties();
				String website = prop
						.getProperty(PropertiesReader.MIAPE_EXTRACTOR_WEBSITE);
				HttpUtilities.openURL(website);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
