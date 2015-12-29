package org.proteored.miapeExtractor.chart;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.junit.Before;
import org.junit.Test;
import org.proteored.miapeExtractor.analysis.charts.HeatChart;
import org.proteored.miapeExtractor.analysis.charts.HeatMapChart;
import org.proteored.miapeExtractor.analysis.gui.tasks.DatasetFactory;
import org.proteored.miapeExtractor.analysis.util.Util;
import org.proteored.miapeExtractor.utils.Wrapper;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.experiment.model.sort.Order;
import org.proteored.miapeapi.experiment.model.sort.SortingParameters;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.interfaces.msi.MiapeMSIDocument;
import org.proteored.miapeapi.interfaces.xml.MiapeXmlFile;
import org.proteored.miapeapi.spring.SpringHandler;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceService;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeDatabaseException_Exception;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeSecurityException_Exception;
import org.proteored.miapeapi.webservice.clients.miapeextractor.MiapeExtractorDelegate;
import org.proteored.miapeapi.webservice.clients.miapeextractor.MiapeExtractorService;
import org.proteored.miapeapi.xml.ms.MIAPEMSXmlFile;
import org.proteored.miapeapi.xml.ms.MiapeMSXmlFactory;
import org.proteored.miapeapi.xml.msi.MIAPEMSIXmlFile;
import org.proteored.miapeapi.xml.msi.MiapeMSIXmlFactory;

public class RemoteTests {
	MiapeExtractorDelegate miapeExtractorWebservice;
	MiapeAPIWebserviceDelegate miapeAPIWebservice;
	String ftpPath;
	String version;
	private URL miapeExtractorEndPoint;
	private URL miapeAPIEndPoint;
	private final String userName = "smartinez@cnb.csic.es";
	private final String password = "test";
	private final String path = "C:\\inetpub\\wwwroot\\ISB\\data\\ABRF2011\\MIAPE_MSI_XML\\";
	private ControlVocabularyManager cvManager;
	private static final int minPeptideLength = 7;
	private static boolean emailNotifications;
	private static final String MIAPE_CONVERTER_PROPERTIES_FILE = "miape-extractor.properties";
	private static final Logger log = Logger
			.getLogger("log4j.logger.org.proteored");
	private static final int WEBSERVICE_TIMEOUT = 480000;// 8min = 480000

	@Before
	public void initialize() {

		// Get properties from resource file
		Properties prop;
		try {
			prop = getProperties();

			ftpPath = prop.getProperty("ftp.path");
			log.info("Reading properties file: ftpPath: " + ftpPath);

			version = prop.getProperty("miapeextractor.version");
			miapeExtractorEndPoint = new URL(
					prop.getProperty("miapeextractor.webservice.endpoint"));
			log.info("Reading properties file: miapeconverter.webservice.endpoint: "
					+ miapeExtractorEndPoint);

			miapeAPIEndPoint = new URL(
					prop.getProperty("miapeapi.webservice.endpoint"));
			log.info("Reading properties file: miapeapi.webservice.endpoint: "
					+ miapeAPIEndPoint);

			emailNotifications = Boolean.parseBoolean(prop
					.getProperty("email_notifications"));
			log.info("Reading properties file: email_notifications: "
					+ emailNotifications);

			String miapeAPIServiceName = prop
					.getProperty("miapeapi.webservice.servicename");
			log.info("Reading properties file: miapeAPIServiceName: "
					+ miapeAPIServiceName);

			String miapeAPINameSpace = prop
					.getProperty("miapeapi.webservice.namespaceURI");
			log.info("Reading properties file: miapeAPINameSpace: "
					+ miapeAPINameSpace);

			String miapeExtractorServiceName = prop
					.getProperty("miapeextractor.webservice.servicename");
			log.info("Reading properties file: miapeExtractorServiceName: "
					+ miapeExtractorServiceName);

			String miapeExtractorNameSpace = prop
					.getProperty("miapeextractor.webservice.namespaceURI");
			log.info("Reading properties file: miapeExtractorNameSpace: "
					+ miapeExtractorNameSpace);

			// Create the miape_api webservice proxy
			this.miapeAPIWebservice = new MiapeAPIWebserviceService(
					miapeAPIEndPoint, new QName(miapeAPINameSpace,
							miapeAPIServiceName)).getMiapeAPIWebservicePort();
			// ((BindingProvider) miapeAPIWebservice).getRequestContext().put(
			// BindingProviderProperties.REQUEST_TIMEOUT,
			// WEBSERVICE_TIMEOUT);

			// Create the miape_generator webservice proxy
			System.out.println("miapeExtractorEndPoint="
					+ miapeExtractorEndPoint);
			this.miapeExtractorWebservice = new MiapeExtractorService(
					miapeExtractorEndPoint, new QName(miapeExtractorNameSpace,
							miapeExtractorServiceName)).getMiapeExtractorPort();
			// ((BindingProvider) miapeExtractorWebservice).getRequestContext()
			// .put(BindingProviderProperties.REQUEST_TIMEOUT,
			// WEBSERVICE_TIMEOUT);

			this.cvManager = SpringHandler.getInstance().getCVManager();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Properties getProperties() throws Exception {
		ClassLoader cl = this.getClass().getClassLoader();
		InputStream is;

		is = cl.getResourceAsStream(MIAPE_CONVERTER_PROPERTIES_FILE);
		if (is == null)
			throw new Exception(MIAPE_CONVERTER_PROPERTIES_FILE
					+ " file not found");

		Properties prop = new Properties();
		try {
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
		return prop;
	}

	@Test
	public void test1() {
		try {
			ExperimentList experiments = createExperimentFromMiapeProject(809);
			List<String> rowList = null;
			List<String> columnList = null;
			Double min = null;
			Double max = null;
			List<IdentificationSet> listOfIdSets = Util
					.getListOfIdSets(experiments.getExperiments());
			double[][] dataset = DatasetFactory.createHeapMapDataSet(
					experiments, listOfIdSets, rowList, columnList,
					IdentificationItemEnum.PROTEIN, null, 4, min, max, false);
			final HeatMapChart heatMapChart = new HeatMapChart(
					"Protein occurrence", dataset, rowList, columnList,
					HeatChart.SCALE_LINEAR);

			ApplicationFrame app = new ApplicationFrame("Test project");
			app.pack();
			RefineryUtilities.centerFrameOnScreen(app);
			app.setVisible(true);
			app.setContentPane(heatMapChart.getjPanel());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private ExperimentList createExperimentFromMiapeProject(int idProject)
			throws IOException, JAXBException,
			MiapeDatabaseException_Exception, MiapeSecurityException_Exception {

		int[] doneList = {};

		final HashMap<Integer, String> miapesByProjectID = Wrapper
				.getHashMap(this.miapeAPIWebservice.getMiapesByProjectID(
						idProject, this.userName, this.password));
		SortingParameters sprot = new SortingParameters("Mascot:score",
				Order.DESCENDANT);
		SortingParameters spep = new SortingParameters("Mascot:score",
				Order.DESCENDANT);
		List<Replicate> replicates = null;
		List<Experiment> experiments = new ArrayList<Experiment>();
		int i = 0;
		for (Integer miapeId : miapesByProjectID.keySet()) {
			if (i == 0) {
				replicates = new ArrayList<Replicate>();
			}

			String miapeType = miapesByProjectID.get(miapeId);
			MiapeMSDocument miapeMS = null;
			MiapeMSIDocument miapeMSI = null;
			if (miapeType.equals("MSI")) {
				if (!isInList(miapeId, doneList)) {
					miapeMSI = getMIAPEMSI(miapeId);
					List<MiapeMSIDocument> listMiapeMSI = new ArrayList<MiapeMSIDocument>();
					Replicate replicate = new Replicate(miapeMSI.getName(),
							miapeMSI.getName(), null, listMiapeMSI, null, null,
							cvManager);
					replicates.add(replicate);
					i++;
				}

			}

			if (i == 2) {
				Experiment experiment = new Experiment(replicates.iterator()
						.next().getName(), replicates, null, minPeptideLength,
						cvManager);
				experiments.add(experiment);
				i = 0;
			}
		}
		if (i != 0 && !replicates.isEmpty()) {
			Experiment experiment = new Experiment(replicates.iterator().next()
					.getName(), replicates, null, minPeptideLength, cvManager);
			experiments.add(experiment);
			i = 0;
		}
		ExperimentList expList = new ExperimentList("PME6", experiments, false,
				null, minPeptideLength, cvManager);
		return null;

	}

	private boolean isInList(Integer miapeId, int[] doneList) {
		for (int i : doneList) {
			if (i == miapeId)
				return true;
		}
		return false;
	}

	private MiapeMSDocument getMIAPEMS(Integer miapeId) throws JAXBException,
			IOException, MiapeDatabaseException_Exception,
			MiapeSecurityException_Exception {
		log.info("Retrieving MIAPE MS " + miapeId);
		byte[] miapeMSBytes = miapeAPIWebservice.getMiapeMSById(miapeId,
				userName, password);
		log.info("MIAPE MS " + miapeId + " retrieved.");
		MiapeXmlFile<MiapeMSDocument> miapeMSFile = new MIAPEMSXmlFile(
				miapeMSBytes);

		final MiapeMSDocument miapeMS = MiapeMSXmlFactory.getFactory()
				.toDocument(miapeMSFile, cvManager, null, this.userName,
						this.password);
		miapeMSFile.saveAs(this.path + miapeMS.getName() + ".xml");
		return miapeMS;
	}

	private MiapeMSIDocument getMIAPEMSI(Integer miapeId) throws JAXBException,
			IOException, MiapeDatabaseException_Exception,
			MiapeSecurityException_Exception {
		log.info("Retrieving MIAPE MSI " + miapeId);
		long t1 = System.currentTimeMillis();
		byte[] miapeMSIBytes = miapeAPIWebservice.getMiapeMSIById(miapeId,
				userName, password);
		log.info("MIAPE MSI " + miapeId + " retrieved.");
		log.info("Loaded in " + (System.currentTimeMillis() - t1) / 1000
				+ " sg.");
		MiapeXmlFile<MiapeMSIDocument> miapeMSIFile = new MIAPEMSIXmlFile(
				miapeMSIBytes);
		final ControlVocabularyManager cvManager = SpringHandler.getInstance()
				.getCVManager();
		final MiapeMSIDocument miapeMSI = MiapeMSIXmlFactory.getFactory()
				.toDocument(miapeMSIFile, cvManager, null, this.userName,
						this.password);
		miapeMSIFile.saveAs(this.path + "MIAPE_MSI_" + miapeId + ".xml");
		return miapeMSI;
	}
}
