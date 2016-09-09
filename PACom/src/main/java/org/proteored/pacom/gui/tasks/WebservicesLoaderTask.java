package org.proteored.pacom.gui.tasks;

import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceService;
import org.proteored.miapeapi.webservice.clients.miapeextractor.MiapeExtractorDelegate;
import org.proteored.miapeapi.webservice.clients.miapeextractor.MiapeExtractorService;
import org.proteored.pacom.utils.PropertiesReader;

public class WebservicesLoaderTask extends SwingWorker<String, Void> {
	public static final String WEBSERVICES_LOADED = "WEBSERVICES_LOADED";
	private static final int WEBSERVICE_TIMEOUT = 24000000;// 4h
	public static final String WEBSERVICES_ERROR = "WEBSERVICES_ERROR";

	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private static WebservicesLoaderTask instance;
	private static final Object[] webservices = new Object[2];

	private WebservicesLoaderTask() {

	}

	public static WebservicesLoaderTask getInstace() {
		if (instance == null)
			instance = new WebservicesLoaderTask();
		return instance;
	}

	public MiapeAPIWebserviceDelegate getMiapeAPIWebservice(boolean waitIfNotLoaded) {
		if (waitIfNotLoaded && (webservices == null || webservices[0] == null))
			try {
				getWebservices();

			} catch (Exception e) {
				e.printStackTrace();
			}
		return (MiapeAPIWebserviceDelegate) webservices[0];
	}

	public MiapeExtractorDelegate getMiapeExtractorWebservice(boolean waitIfNotLoaded) {
		if (waitIfNotLoaded && (webservices == null || webservices[1] == null))
			try {
				getWebservices();
			} catch (Exception e) {
				e.printStackTrace();
			}
		return (MiapeExtractorDelegate) webservices[1];
	}

	@Override
	protected String doInBackground() throws Exception {

		// log.info("Initializing webservices");
		try {
			if (webservices == null || webservices.length != 2 || webservices[0] == null || webservices[1] == null)
				getWebservices();
			return null;
		} catch (Exception e) {
			return e.getMessage();
		}

	}

	private Void getWebservices() throws Exception {

		// Get properties from resource file
		Properties prop = PropertiesReader.getProperties();

		URL miapeExtractorEndPoint = new URL(prop.getProperty(PropertiesReader.MIAPE_EXTRACTOR_WEBSERVICE_ENDPOINT));
		log.info("Reading properties file: " + PropertiesReader.MIAPE_EXTRACTOR_WEBSERVICE_ENDPOINT + ": "
				+ miapeExtractorEndPoint);

		URL miapeAPIEndPoint = new URL(prop.getProperty(PropertiesReader.MIAPE_API_WEBSERVICE_ENDPOINT));
		log.info(
				"Reading properties file: " + PropertiesReader.MIAPE_API_WEBSERVICE_ENDPOINT + ": " + miapeAPIEndPoint);

		String miapeAPIServiceName = prop.getProperty(PropertiesReader.MIAPE_API_WEBSERVICE_SERVICENAME);
		log.info("Reading properties file: " + PropertiesReader.MIAPE_API_WEBSERVICE_SERVICENAME + ": "
				+ miapeAPIServiceName);

		String miapeAPINameSpace = prop.getProperty(PropertiesReader.MIAPE_API_WEBSERVICE_NAMESPACEURI);
		log.info("Reading properties file: " + PropertiesReader.MIAPE_API_WEBSERVICE_NAMESPACEURI + ": "
				+ miapeAPINameSpace);

		String miapeExtractorServiceName = prop.getProperty(PropertiesReader.MIAPE_EXTRACTOR_WEBSERVICE_SERVICENAME);
		log.info("Reading properties file: " + PropertiesReader.MIAPE_EXTRACTOR_WEBSERVICE_SERVICENAME + ": "
				+ miapeExtractorServiceName);

		String miapeExtractorNameSpace = prop.getProperty(PropertiesReader.MIAPE_EXTRACTOR_WEBSERVICE_NAMESPACEURI);
		log.info("Reading properties file: " + PropertiesReader.MIAPE_EXTRACTOR_WEBSERVICE_NAMESPACEURI + ": "
				+ miapeExtractorNameSpace);

		// Create the miape_api webservice proxy
		try {
			QName serviceName = new QName(miapeAPINameSpace, miapeAPIServiceName);
			MiapeAPIWebserviceService miapeAPIWebserviceService = new MiapeAPIWebserviceService(miapeAPIEndPoint,
					serviceName);
			MiapeAPIWebserviceDelegate miapeAPIWebservice = miapeAPIWebserviceService.getMiapeAPIWebservicePort();
			// ((BindingProvider) miapeAPIWebservice).getRequestContext().put(
			// BindingProviderProperties.REQUEST_TIMEOUT,
			// WEBSERVICE_TIMEOUT);
			webservices[0] = miapeAPIWebservice;

		} catch (Exception e) {
			throw new IllegalMiapeArgumentException("Error loading miape api webservice: " + e.getMessage());
		}

		// Create the miape_generator webservice proxy

		try {
			// log.info("miapeExtractorEndPoint=" + miapeExtractorEndPoint);
			QName serviceName = new QName(miapeExtractorNameSpace, miapeExtractorServiceName);
			MiapeExtractorService miapeExtractorService = new MiapeExtractorService(miapeExtractorEndPoint,
					serviceName);
			MiapeExtractorDelegate miapeExtractorWebservice = miapeExtractorService.getMiapeExtractorPort();
			// ((BindingProvider) miapeExtractorWebservice).getRequestContext()
			// .put(BindingProviderProperties.REQUEST_TIMEOUT,
			// WEBSERVICE_TIMEOUT);
			webservices[1] = miapeExtractorWebservice;

		} catch (Exception e) {
			throw new IllegalMiapeArgumentException("Error loading miape extractor webservice: " + e.getMessage());
		}
		// log.info("Webservice initialized");
		return null;

	}

	@Override
	protected void done() {
		setProgress(100);
		String string;
		try {
			string = get();
		} catch (InterruptedException e) {
			e.printStackTrace();
			string = e.getMessage();
		} catch (ExecutionException e) {
			e.printStackTrace();
			string = e.getMessage();
		}
		if (!isCancelled() && string == null && webservices[0] != null && webservices[1] != null)
			firePropertyChange(WEBSERVICES_LOADED, null, webservices);
		else
			firePropertyChange(WEBSERVICES_ERROR, null, string);
	}

}
