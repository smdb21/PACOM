package org.proteored.miapeExtractor.gui.tasks;

import java.awt.Toolkit;

import javax.swing.SwingWorker;

import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeDatabaseException_Exception;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeSecurityException_Exception;

public class LoginTask extends SwingWorker<Void, Void> {
	private final String userName;
	private final String password;
	private final MiapeAPIWebserviceDelegate miapeAPIWebservice;
	public static String LOGIN_OK = "login ok";
	public static String LOGIN_ERROR = "login error";
	public static String LOGIN_STARTED = "login started";

	public LoginTask(String userName, String password,
			MiapeAPIWebserviceDelegate miapeAPIWebservice2) {
		this.userName = userName;
		this.password = password;
		this.miapeAPIWebservice = miapeAPIWebservice2;
	}

	private void checkLogin() {
		if (this.miapeAPIWebservice == null)
			firePropertyChange(LOGIN_ERROR, null, "Webservice not loaded");
		String error = null;
		try {
			firePropertyChange(LOGIN_STARTED, null, null);

			int userID = miapeAPIWebservice.getUserId(this.userName, this.password);
			if (userID < 1) {
				Toolkit.getDefaultToolkit().beep();
				error = "User name and/or password are wrong\n";
				error = error + "Please, enter a correct user name and password\n";
				firePropertyChange(LOGIN_ERROR, null, error);
			} else {
				firePropertyChange(LOGIN_OK, null, userID);

				return;
			}
		} catch (MiapeSecurityException_Exception e) {
			// log.info(e.getMessage());
			error = "There was a problem in the login: ";
			error = error + "The user name or the password is incorrect\n";

		} catch (MiapeDatabaseException_Exception e) {
			e.printStackTrace();
			error = "There was a problem in the login: ";
			error = error + e.getMessage() + "\n";

		} catch (Exception e) {
			error = error + "Error connecting to webservice\n";
			e.printStackTrace();
			if (e.getMessage().contains("refused")
					|| e.getMessage().contains("Service Unavailable")
					|| e.getMessage().contains("Read timed out")) {
				error = error
						+ "Webservice seems to be down.\nContact to miape_support@proteored.org.\n";
			} else if (e.getMessage().contains("target service")
					|| e.getMessage().contains("Not Found") || e instanceof NullPointerException) {
				error = error
						+ "There is a problem connecting to the webservice.\nContact to miape_support@proteored.org.\n";
			} else if (e.getMessage().contains("No route to host")) {
				error = error
						+ "There is a problem with the Internet connection. Please, check it and try again.\nContact to miape_support@proteored.org.\n";
			} else {
				error = error + e.getMessage() + "\n";

			}
		}
		if (error != null)
			firePropertyChange(LOGIN_ERROR, null, error);

	}

	@Override
	protected Void doInBackground() throws Exception {
		checkLogin();
		return null;
	}
}
