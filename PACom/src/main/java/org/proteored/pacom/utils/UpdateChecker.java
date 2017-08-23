package org.proteored.pacom.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.proteored.pacom.gui.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.yates.utilities.util.versioning.AppVersion;

/**
 * Check whether there is a new update
 * <p/>
 * User: rwang Date: 11-Nov-2010 Time: 17:19:36
 */
public class UpdateChecker {

	public static final Logger log = LoggerFactory.getLogger(UpdateChecker.class);
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
			AppVersion version = MainFrame.getVersion();
			String downloadWebsite = prop.getProperty(PropertiesReader.PACOM_DOWNLOAD_WEBSITE);
			String versionCheckWebsite = prop.getProperty(PropertiesReader.PACOM_CHECK_VERSION_WEBSITE);

			URL url = new URL(versionCheckWebsite);
			// connect to the url
			int response = ((HttpURLConnection) url.openConnection()).getResponseCode();
			if (response == 404) {
				toUpdate = true;
			} else {
				// parse the web page
				reader = new BufferedReader(new InputStreamReader(url.openStream()));
				String lineString = "";
				String line;
				while ((line = reader.readLine()) != null) {
					lineString += line;
				}
				String[] split = new String[1];
				if (lineString.contains(",")) {
					split = lineString.split(",");
				} else {
					split[0] = lineString;
				}
				List<AppVersion> versions = new ArrayList<AppVersion>();
				for (String pacomRelease : split) {
					AppVersion version2 = getVersionFromPacomReleaseFileName(pacomRelease);
					if (version2 != null) {
						versions.add(version2);
					}
				}
				Collections.sort(versions);
				// take the latest
				AppVersion newest = versions.get(versions.size() - 1);
				if (!newest.equals(version)) {
					// see what is
					int compareTo = version.compareTo(newest);
					if (compareTo == -1) {
						toUpdate = true;
					}
				}
			}
		} catch (Exception e) {
			log.warn("Failed to check for updates", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		// }

		return toUpdate;
	}

	private AppVersion getVersionFromPacomReleaseFileName(String pacomRelease) {
		try {
			return new AppVersion(pacomRelease);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Show update dialog
	 */
	public void showUpdateDialog() {
		int option = JOptionPane.showConfirmDialog(null,
				"<html><b>A new version of PACom is available</b>.<br><br> "
						+ "<font color=\"red\">It is strongly recommended to install the new version as soon as possible</font>.<br>"
						+ "Would you like to update?</html>",
				"Update Info", JOptionPane.YES_NO_OPTION);

		if (option == JOptionPane.YES_OPTION) {
			// Get properties from resource file
			Properties prop;
			try {
				prop = PropertiesReader.getProperties();
				String website = prop.getProperty(PropertiesReader.PACOM_DOWNLOAD_WEBSITE);
				HttpUtilities.openURL(website);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
