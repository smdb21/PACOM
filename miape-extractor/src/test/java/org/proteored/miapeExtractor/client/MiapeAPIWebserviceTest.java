package org.proteored.miapeExtractor.client;

import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.proteored.miapeExtractor.utils.PropertiesReader;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceService;

public class MiapeAPIWebserviceTest {

	private static final int WEBSERVICE_TIMEOUT = 24000000;// 4h

	@Test
	public void getMiapeMSI() {
		try {

			MiapeAPIWebserviceDelegate webservice = getWebservices();
			byte[] miapeMSIById = webservice.getMiapeMSIById(3998, "smartinez",
					"natjeija");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}

	}

	@Test
	public void getDateLong() {
		System.out.println(System.currentTimeMillis());
		if (Long.valueOf("1349251004048") < System.currentTimeMillis())
			System.out.println("HOLA");
	}

	private MiapeAPIWebserviceDelegate getWebservices() throws Exception {

		// Get properties from resource file
		Properties prop = PropertiesReader.getProperties();

		URL miapeExtractorEndPoint = new URL(
				prop.getProperty("miapeextractor.webservice.endpoint"));

		URL miapeAPIEndPoint = new URL(
				prop.getProperty("miapeapi.webservice.endpoint"));

		String miapeAPIServiceName = prop
				.getProperty("miapeapi.webservice.servicename");

		String miapeAPINameSpace = prop
				.getProperty("miapeapi.webservice.namespaceURI");

		String miapeExtractorServiceName = prop
				.getProperty("miapeextractor.webservice.servicename");

		String miapeExtractorNameSpace = prop
				.getProperty("miapeextractor.webservice.namespaceURI");

		// Create the miape_api webservice proxy
		try {
			QName serviceName = new QName(miapeAPINameSpace,
					miapeAPIServiceName);
			MiapeAPIWebserviceService miapeAPIWebserviceService = new MiapeAPIWebserviceService(
					miapeAPIEndPoint, serviceName);
			MiapeAPIWebserviceDelegate miapeAPIWebservice = miapeAPIWebserviceService
					.getMiapeAPIWebservicePort();
			// ((BindingProvider) miapeAPIWebservice).getRequestContext().put(
			// BindingProviderProperties.REQUEST_TIMEOUT, WEBSERVICE_TIMEOUT);
			return miapeAPIWebservice;
		} catch (Exception e) {
			throw new IllegalMiapeArgumentException(
					"Error loading miape api webservice: " + e.getMessage());
		}

	}

}
