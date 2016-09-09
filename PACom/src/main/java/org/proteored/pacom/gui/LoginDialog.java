/*
 * LoginDialog.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.gui;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.miapeapi.webservice.clients.miapeextractor.MiapeExtractorDelegate;
import org.proteored.pacom.gui.tasks.LoginTask;
import org.proteored.pacom.gui.tasks.UnattendedMiapeMiapeXMLRetriever;
import org.proteored.pacom.gui.tasks.WebservicesLoaderTask;
import org.proteored.pacom.utils.HttpUtilities;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

/**
 * 
 * @author __USER__
 */
public class LoginDialog extends javax.swing.JDialog implements
		PropertyChangeListener {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger("log4j.logger.org.proteored");
	private static LoginDialog instance;
	public final MainFrame mainFrame;
	// Variables declaration - do not modify
	private static final String CREATE_USER_ACCOUNT_PROTEORED_URL = "http://www.proteored.org/ClientesA.asp";

	private LoginTask loginThread;
	private WebservicesLoaderTask webserviceInitilizer;
	private boolean webservicesLoaded = false;
	private boolean loginRequested = false;
	private String userName;
	private String password;
	private boolean startUnattendedRetriever = false;
	private boolean offlineSelected;

	/** Creates new form LoginDialog */
	private LoginDialog(MainFrame parent, boolean modal,
			boolean startUnattendedRetriever) {
		super(parent, modal);
		webservicesLoaded = false;
		loginRequested = false;
		this.startUnattendedRetriever = startUnattendedRetriever;
		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (UnsupportedLookAndFeelException ex) {
		}
		initComponents();
		loadIcons();
		getRootPane().setDefaultButton(jButtonLogin);
		MiapeAPIWebserviceDelegate miapeAPIWebservice = MainFrame
				.getMiapeAPIWebservice();
		MiapeExtractorDelegate miapeExtractorWebservice = MainFrame
				.getMiapeExtractorWebservice();
		if (miapeAPIWebservice == null || miapeExtractorWebservice == null) {
			webserviceInitilizer = WebservicesLoaderTask.getInstace();
			webserviceInitilizer.addPropertyChangeListener(this);
			webserviceInitilizer.execute();
		} else {
			webservicesLoaded = true;
		}
		mainFrame = parent;

		// // autoscroll in the status field
		// MainFrame.autoScroll(jScrollPane1, jTextAreaStatus);
		setTitle("Login to ProteoRed MIAPE repository");

	}

	public static LoginDialog getInstance(MainFrame parent, boolean modal,
			boolean startUnattendedRetriever) {
		if (instance == null)
			instance = new LoginDialog(parent, modal, startUnattendedRetriever);
		instance.jButtonLogin.setText("Login");
		return instance;
	}

	private void loadIcons() {
		setIconImage(ImageManager
				.getImageIcon(ImageManager.PROTEORED_MIAPE_API).getImage());
		jButtonLogin.setIcon(ImageManager.getImageIcon(ImageManager.LOGIN));
		jButtonLogin.setPressedIcon(ImageManager
				.getImageIcon(ImageManager.LOGIN_CLICKED));
		jButtonCreateAccount.setIcon(ImageManager
				.getImageIcon(ImageManager.ADD_USER));
		jButtonCreateAccount.setPressedIcon(ImageManager
				.getImageIcon(ImageManager.ADD_USER_CLICKED));
		jButtonGoOffline
				.setIcon(ImageManager.getImageIcon(ImageManager.FINISH));
		jButtonGoOffline.setPressedIcon(ImageManager
				.getImageIcon(ImageManager.FINISH_CLICKED));

	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jPanel1 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		jTextFieldUserName = new javax.swing.JTextField();
		jPasswordField = new javax.swing.JPasswordField();
		jButtonLogin = new javax.swing.JButton();
		jButtonCreateAccount = new javax.swing.JButton();
		jButtonGoOffline = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jTextAreaStatus = new javax.swing.JTextArea();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Login to MIAPE services");
		setResizable(false);

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createEtchedBorder(),
				"User name and password"));

		jLabel1.setText("user name:");

		jLabel2.setText("password:");

		jTextFieldUserName.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyTyped(java.awt.event.KeyEvent evt) {
				jTextFieldUserNameKeyTyped(evt);
			}
		});

		jPasswordField.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyTyped(java.awt.event.KeyEvent evt) {
				jPasswordFieldKeyTyped(evt);
			}
		});

		jButtonLogin
				.setIcon(new javax.swing.ImageIcon(
						"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\login.png")); // NOI18N
		jButtonLogin.setText("login");
		jButtonLogin.setToolTipText("Login to the ProteoRed MIAPE repository");
		jButtonLogin.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonLoginActionPerformed(evt);
			}
		});

		jButtonCreateAccount
				.setIcon(new javax.swing.ImageIcon(
						"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\add_user.png")); // NOI18N
		jButtonCreateAccount.setText("create account");
		jButtonCreateAccount.setToolTipText("Go to create user account page.");
		jButtonCreateAccount
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jButtonCreateAccountActionPerformed(evt);
					}
				});

		jButtonGoOffline
				.setIcon(new javax.swing.ImageIcon(
						"C:\\Users\\Salva\\workspace\\miape-extractor\\src\\main\\resources\\finish.png")); // NOI18N
		jButtonGoOffline.setText("Offline mode");
		jButtonGoOffline
				.setToolTipText("<html>Offline mode allows the user to inspect your data,<br>\njust in case inspection project was already created beforehand.</html>");
		jButtonGoOffline.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonGoOfflineActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(
				jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout
				.setHorizontalGroup(jPanel1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel1Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																jPanel1Layout
																		.createSequentialGroup()
																		.addGroup(
																				jPanel1Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jLabel1)
																						.addComponent(
																								jLabel2))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				jPanel1Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								jTextFieldUserName,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								230,
																								Short.MAX_VALUE)
																						.addComponent(
																								jPasswordField,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								230,
																								Short.MAX_VALUE)))
														.addComponent(
																jButtonGoOffline,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																293,
																Short.MAX_VALUE)
														.addComponent(
																jButtonLogin,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																293,
																Short.MAX_VALUE)
														.addComponent(
																jButtonCreateAccount,
																javax.swing.GroupLayout.Alignment.TRAILING,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																293,
																Short.MAX_VALUE))
										.addGap(12, 12, 12)));
		jPanel1Layout
				.setVerticalGroup(jPanel1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel1Layout
										.createSequentialGroup()
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel1)
														.addComponent(
																jTextFieldUserName,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(jLabel2)
														.addComponent(
																jPasswordField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jButtonLogin)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jButtonCreateAccount)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jButtonGoOffline)
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createEtchedBorder(), "Status"));

		jTextAreaStatus.setColumns(20);
		jTextAreaStatus.setEditable(false);
		jTextAreaStatus.setFont(new java.awt.Font("Dialog", 0, 10));
		jTextAreaStatus.setLineWrap(true);
		jTextAreaStatus.setRows(5);
		jTextAreaStatus.setToolTipText("Status of the login process");
		jTextAreaStatus.setWrapStyleWord(true);
		jTextAreaStatus.setAutoscrolls(true);
		jScrollPane1.setViewportView(jTextAreaStatus);

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(
				jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				jPanel2Layout
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(jScrollPane1,
								javax.swing.GroupLayout.DEFAULT_SIZE, 293,
								Short.MAX_VALUE).addContainerGap()));
		jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				jPanel2Layout
						.createSequentialGroup()
						.addComponent(jScrollPane1,
								javax.swing.GroupLayout.DEFAULT_SIZE, 80,
								Short.MAX_VALUE).addContainerGap()));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						javax.swing.GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.TRAILING)
												.addComponent(
														jPanel2,
														javax.swing.GroupLayout.Alignment.LEADING,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														Short.MAX_VALUE)
												.addComponent(
														jPanel1,
														javax.swing.GroupLayout.Alignment.LEADING,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														Short.MAX_VALUE))
								.addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(jPanel1,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jPanel2,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addContainerGap(
										javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)));

		pack();
		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit()
				.getScreenSize();
		java.awt.Dimension dialogSize = getSize();
		setLocation((screenSize.width - dialogSize.width) / 2,
				(screenSize.height - dialogSize.height) / 2);
	}// </editor-fold>
		// GEN-END:initComponents

	private void jTextFieldUserNameKeyTyped(java.awt.event.KeyEvent evt) {
		char key = evt.getKeyChar();
		if (evt.getKeyCode() == KeyEvent.KEY_PRESSED)
			if ('\n' == key) {
				startLogin();
			}
	}

	private void jPasswordFieldKeyTyped(java.awt.event.KeyEvent evt) {
		char key = evt.getKeyChar();
		if (evt.getKeyCode() == KeyEvent.KEY_PRESSED)
			if ('\n' == key) {
				startLogin();
			}
	}

	private void jButtonGoOfflineActionPerformed(java.awt.event.ActionEvent evt) {
		offlineSelected = true;
		dispose();
	}

	private void jButtonCreateAccountActionPerformed(
			java.awt.event.ActionEvent evt) {
		createAccount();
	}

	private void createAccount() {
		final int selectedOption = JOptionPane
				.showConfirmDialog(
						this,
						"<html>A web browser will be opened to go to the create user account<br>Do you want to continue?</html>",
						"Go to create user account page",
						JOptionPane.YES_NO_CANCEL_OPTION);
		if (selectedOption == JOptionPane.YES_OPTION) {
			HttpUtilities.openURL(CREATE_USER_ACCOUNT_PROTEORED_URL);
		}
	}

	private void jButtonLoginActionPerformed(java.awt.event.ActionEvent evt) {
		startLogin();
	}

	private void startLogin() {
		if (!webservicesLoaded) {
			log.info("Webservices are being loaded");
			appendStatus("Stablishing connection to server. Please wait...");
			loginRequested = true;
			// if (this.webserviceInitilizer.isDone() ||
			// this.webserviceInitilizer.isCancelled()) {
			webserviceInitilizer = WebservicesLoaderTask.getInstace();
			webserviceInitilizer.addPropertyChangeListener(this);
			webserviceInitilizer.execute();
			// }
			return;
		}

		if (jButtonLogin.getText().equals("Close")) {
			dispose();
		}

		try {
			checkLogin();
		} catch (Exception e) {

			e.printStackTrace();
			appendStatus(e.getMessage());
		}
	}

	private void checkLogin() throws Exception {

		userName = jTextFieldUserName.getText();
		if (userName.equals("")) {
			appendStatus("Please, enter a user name");
			return;
		}
		password = jPasswordField.getText();
		if (password.equals("")) {
			appendStatus("Please, enter a password.");
			return;
		}

		// Do it in background
		loginThread = new LoginTask(userName, password,
				MainFrame.getMiapeAPIWebservice());
		loginThread.addPropertyChangeListener(this);
		loginThread.addPropertyChangeListener(mainFrame);
		loginThread.execute();
	}

	@Override
	public void dispose() {
		if (loginThread != null) {
			if (loginThread.getState() == StateValue.STARTED) {
				loginThread.cancel(true);
			}
		}
		super.dispose();

	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JButton jButtonCreateAccount;
	private javax.swing.JButton jButtonGoOffline;
	private javax.swing.JButton jButtonLogin;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPasswordField jPasswordField;
	private javax.swing.JScrollPane jScrollPane1;
	public javax.swing.JTextArea jTextAreaStatus;
	private javax.swing.JTextField jTextFieldUserName;

	// End of variables declaration//GEN-END:variables

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		MiapeAPIWebserviceDelegate miapeAPIWebservice;
		MiapeExtractorDelegate miapeExtractorWebservice;
		if (WebservicesLoaderTask.WEBSERVICES_LOADED.equals(evt
				.getPropertyName())) {
			log.info("Webservices loaded!");
			appendStatus("Server connection stablished.");
			Object[] webservices = (Object[]) evt.getNewValue();
			miapeAPIWebservice = (MiapeAPIWebserviceDelegate) webservices[0];
			miapeExtractorWebservice = (MiapeExtractorDelegate) webservices[1];
			webservicesLoaded = true;
			MainFrame.setMiapeAPIWebservice(miapeAPIWebservice);
			MainFrame.setMiapeExtractorWebservice(miapeExtractorWebservice);
			if (loginRequested)
				try {
					checkLogin();
				} catch (Exception e) {
					e.printStackTrace();
				}

		} else if (WebservicesLoaderTask.WEBSERVICES_ERROR.equals(evt
				.getPropertyName())) {
			String errorMessage = (String) evt.getNewValue();
			appendStatus("There was a problem in the login: ");
			appendStatus(errorMessage + "\n");
			jButtonLogin.setEnabled(true);
			// just show it if not offline is selected because in that case it
			// doenst matters.
			if (!offlineSelected)
				// TODO
				JOptionPane
						.showMessageDialog(
								this,
								"<html>Close the tool and try again later. If you get the same error, please notify it to miape-support@proteored.org</html>",
								"Error stablishing server connection",
								JOptionPane.ERROR_MESSAGE);
		} else if (LoginTask.LOGIN_STARTED.equals(evt.getPropertyName())) {
			jButtonLogin.setEnabled(false);
			appendStatus("Checking user name and password...");
		} else if (LoginTask.LOGIN_ERROR.equals(evt.getPropertyName())) {
			appendStatus((String) evt.getNewValue());
			jButtonLogin.setEnabled(true);
		} else if (LoginTask.LOGIN_OK.equals(evt.getPropertyName())) {
			jButtonLogin.setText("Close");

			MainFrame.userName = userName;
			MainFrame.password = password;

			int userID = (Integer) evt.getNewValue();
			mainFrame.userID = userID;
			appendStatus("Login OK.\n");

			jButtonLogin.setEnabled(true);

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// launch miape msi unattended retriever
			if (startUnattendedRetriever) {
				UnattendedMiapeMiapeXMLRetriever retriever = new UnattendedMiapeMiapeXMLRetriever(
						MainFrame.getMiapeAPIWebservice(),
						MainFrame.getMiapeExtractorWebservice(), userName,
						password);
				retriever.execute();
			}

			jButtonLogin.setEnabled(true);

			dispose();
		}
	}

	private void appendStatus(String text) {
		jTextAreaStatus.append(text + "\n");
		jTextAreaStatus
				.setCaretPosition(jTextAreaStatus.getText().length() - 1);
	}
}