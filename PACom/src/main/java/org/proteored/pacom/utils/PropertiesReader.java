package org.proteored.pacom.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {
	private static final String PACOM_PROPERTIES_FILE = "pacom.properties";
	// public static final String ENSEMBL_UNIPROT_MAPPING_FILE =
	// "ensembl_uniprot_mapping_file";
	public static final String ENSEMBL_UNIPROT_MAPPING_FILE = "ensembl_uniprot_mapping_file";

	public static final String PACOM_DOWNLOAD_WEBSITE = "pacom.download.website";
	public static final String PACOM_CHECK_VERSION_WEBSITE = "pacom.check.version.website";
	public static final String MIAPE_EXTRACTOR_WEBSERVICE_ENDPOINT = "miapeextractor.webservice.endpoint";
	public static final String MIAPE_API_WEBSERVICE_ENDPOINT = "miapeapi.webservice.endpoint";
	public static final String MIAPE_API_WEBSERVICE_SERVICENAME = "miapeapi.webservice.servicename";
	public static final String MIAPE_API_WEBSERVICE_NAMESPACEURI = "miapeapi.webservice.namespaceURI";
	public static final String MIAPE_EXTRACTOR_WEBSERVICE_SERVICENAME = "miapeextractor.webservice.servicename";
	public static final String MIAPE_EXTRACTOR_WEBSERVICE_NAMESPACEURI = "miapeextractor.webservice.namespaceURI";
	public static final String EMAIL_NOTIFICATIONS = "email_notifications";
	public static final String MIAPE_TOOL_ACCESS_SCRIPT = "miapetool_access_script";
	public static final String MIAPE_EXTRACTOR_UNATENDEDRETRIEVER = "miapeextractor.unatendedretriever";
	public static final String LOCAL_WORKFLOW = "local.workflow";
	public static final String SCHEMA_VALIDATION = "schema.validation";

	public static Properties getProperties() {
		ClassLoader cl = PropertiesReader.class.getClassLoader();
		InputStream is;

		is = cl.getResourceAsStream(PACOM_PROPERTIES_FILE);
		if (is == null)
			throw new IllegalArgumentException(PACOM_PROPERTIES_FILE + " file not found");

		Properties prop = new Properties();
		try {
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
		return prop;
	}

	public static Properties getProperties(String classPathFileName) {
		ClassLoader cl = PropertiesReader.class.getClassLoader();
		InputStream is;

		is = cl.getResourceAsStream(classPathFileName);
		if (is == null)
			throw new IllegalArgumentException(classPathFileName + " file not found");

		Properties prop = new Properties();
		try {
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
		return prop;
	}
}
