package org.proteored.pacom.utils;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JOptionPane;

import org.proteored.miapeapi.util.URLParamEncoder;

/**
 * Created by IntelliJ IDEA. User: rwang Date: 20-Aug-2010 Time: 12:53:03
 */
public class HttpUtilities {

	public static HttpURLConnection createHttpConnection(String url,
			String method) throws Exception {

		HttpURLConnection connection;
		String encodedURL = URLParamEncoder.encode(url);
		URL downloadURL = new URL(encodedURL);
		connection = (HttpURLConnection) downloadURL.openConnection();
		connection.setRequestMethod(method);
		connection.setUseCaches(true);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		return connection;
	}

	/**
	 * Open a browser pointing to the URL passes in the parameter
	 * 
	 * @param url
	 */
	public static void openURL(String url) {
		String encodedURL = URLParamEncoder.encode(url);
		String osName = System.getProperty("os.name");
		try {
			if (osName.startsWith("Mac OS")) {
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL",
						new Class[] { String.class });
				openURL.invoke(null, encodedURL);
			} else if (osName.startsWith("Windows")) {
				Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " + encodedURL);
			} else { // assume Unix or Linux
				String[] browsers = { "firefox", "opera", "konqueror",
						"epiphany", "mozilla", "netscape" };
				String browser = null;

				for (int count = 0; count < browsers.length && browser == null; count++) {
					if (Runtime.getRuntime()
							.exec(new String[] { "which", browsers[count] })
							.waitFor() == 0) {
						browser = browsers[count];
					}
				}

				if (browser == null) {
					throw new Exception("Could not find web browser");
				} else {
					Runtime.getRuntime().exec(
							new String[] { browser, encodedURL });
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
		}
	}
}
