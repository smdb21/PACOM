package org.proteored.pacom.gui.tasks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.SwingWorker;

import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.interfaces.xml.MiapePrideXmlFile;
import org.proteored.miapeapi.xml.mzml.lightParser.utils.MzMLLightParser;
import org.proteored.miapeapi.xml.pride.MSMiapeFactory;
import org.proteored.miapeapi.xml.pride.MiapeFullPrideXMLFile;
import org.proteored.miapeapi.xml.pride.MiapeMsPrideXmlFile;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.MiapeExtractionFrame;
import org.proteored.pacom.gui.miapemsforms.MiapeMSForms;

import uk.ac.ebi.jmzml.model.mzml.MzML;

public class MIAPEMSChecker extends SwingWorker<MiapeMSDocument, Void> {

	public static final String MIAPE_MS_METADATA_EXTRACTION_DONE = "miape ms extraction done";

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");

	public static final String MIAPE_MS_CHECKING_DONE = "checking ms done";
	public static final String MIAPE_MS_CHECKING_ERROR = "checking ms error";
	public static final String MIAPE_MS_CHECKING_IN_PROGRESS = "checking ms";
	public static final String MIAPE_MS_CHECKING_STARTED = "checking ms started";
	public static final String MIAPE_MS_FORMS_OPENING = "ms forms opening";

	public static final String CURRENT_MZML = "current mzML file";
	public static final String CURRENT_PRIDEXML = "current PRIDE XML file";

	private final MiapeExtractionFrame parent;
	private final ControlVocabularyManager cvManager;

	private static MiapeMSForms forms;
	private boolean save = false;

	private boolean reportExtractionDone;

	private final boolean extractMetadataFromStandardFile;

	private static MiapeMSDocument previousMIAPEMS;

	private static MiapeMSDocument miapeMs;

	public MIAPEMSChecker(MiapeExtractionFrame standard2miapeDialog, boolean extractMetadataFromStandardFile) {
		parent = standard2miapeDialog;
		cvManager = OntologyLoaderTask.getCvManager();
		this.extractMetadataFromStandardFile = extractMetadataFromStandardFile;
	}

	public boolean isSave() {
		return save;
	}

	public void setSave(boolean save) {
		this.save = save;
	}

	@Override
	protected MiapeMSDocument doInBackground() throws Exception {
		firePropertyChange(MIAPE_MS_CHECKING_STARTED, null, null);
		reportExtractionDone = true;
		miapeMs = null;

		setProgress(0);
		final String inputFileName = parent.getPrimaryInputFileName();
		if (inputFileName != null && !"".equals(inputFileName) && extractMetadataFromStandardFile) {

			if (parent.isMzMLSelected()) {
				firePropertyChange(MIAPE_MS_CHECKING_IN_PROGRESS, null, null);
				File mzMLFile = new File(inputFileName);
				File metadataMzMLFile = getMetadataMzMLFile(mzMLFile);
				log.info("parsing mzML XML to document MS in the faster method (SAX+DOM method)");
				MzMLLightParser mzMLParser = new MzMLLightParser(metadataMzMLFile.getAbsolutePath());
				MzML mzmlLight = mzMLParser.ParseDocument(false);
				setProgress(50);
				// Create the MIAPE MS document
				miapeMs = org.proteored.miapeapi.xml.mzml.MSMiapeFactory.getFactory().toDocument(mzmlLight, null,
						cvManager, null, null, mzMLFile.getName(), null);

			} else if (parent.isPRIDESelected()) {
				firePropertyChange(MIAPE_MS_CHECKING_IN_PROGRESS, null, null);
				File prideXMLFile = new File(inputFileName);
				MiapePrideXmlFile miapePrideFile = null;
				if (parent.jCheckBoxMS.isSelected() && parent.jCheckBoxMSI.isSelected()) {
					miapePrideFile = new MiapeFullPrideXMLFile(prideXMLFile);
				} else if (parent.jCheckBoxMS.isSelected() && !parent.jCheckBoxMSI.isSelected()) {
					miapePrideFile = new MiapeMsPrideXmlFile(prideXMLFile);
				}
				setProgress(50);
				if (miapePrideFile != null) {
					miapeMs = MSMiapeFactory.getFactory().toDocument(new MiapeMsPrideXmlFile(prideXMLFile), null,
							cvManager, null, null, null);

				}

			}
			// if no input file name, but a selection is selected in the
			// metadata saved combo:

		} else {
			MiapeMSDocument miapeMSMetadata = parent.getMiapeMSMetadata();
			if (miapeMSMetadata != null) {
				miapeMs = miapeMSMetadata;
			}
		}

		if (!save) {
			firePropertyChange(MIAPE_MS_FORMS_OPENING, null, null);
			if (forms == null || (miapeMs != null && previousMIAPEMS == null)
					|| (miapeMs == null && previousMIAPEMS != null)
					|| (miapeMs != null && !miapeMs.equals(previousMIAPEMS))) {
				previousMIAPEMS = miapeMs;
				forms = new MiapeMSForms(parent, this, miapeMs);
			}
			forms.setVisible(true);
		} else {
			String absolutePath = null;
			if (parent.isMzMLSelected() || parent.isMzMLPlusMzIdentMLSelected())
				absolutePath = FileManager.getMetadataFolder() + CURRENT_MZML + ".xml";
			else if (parent.isPRIDESelected() && parent.isMIAPEMSChecked())
				absolutePath = FileManager.getMetadataFolder() + CURRENT_PRIDEXML + ".xml";
			if (absolutePath != null)
				miapeMs.toXml().saveAs(absolutePath);
		}
		setProgress(100);
		return null;
	}

	/**
	 * Function that returns a file with all elements of the mzML excepting the
	 * mzML element
	 * 
	 * @param mzMLFile
	 * @return
	 */
	private File getMetadataMzMLFile(File mzMLFile) {
		BufferedWriter bufferedWriter = null;
		BufferedReader bufferedReader = null;
		boolean indexed = false;
		try {
			File ret = File.createTempFile("metadataMzML", ".mzML");
			ret.deleteOnExit();

			bufferedWriter = new BufferedWriter(new FileWriter(ret));
			bufferedReader = new BufferedReader(new FileReader(mzMLFile));
			try {
				while (true) {
					String line = bufferedReader.readLine();
					if (line == null) {
						log.warn("'<run' not found in the file " + mzMLFile.getAbsolutePath());
						break;
					}

					if (line.trim().contains("<indexedmzML"))
						indexed = true;
					if (line.trim().startsWith("<run")) {
						bufferedWriter.write("</mzML>");
						if (indexed)
							bufferedWriter.write("</indexedmzML>");
						log.info("'<run' found in the file " + mzMLFile.getAbsolutePath());
						break;
					}
					line = line + System.getProperty("line.separator");
					bufferedWriter.write(line);
				}
			} finally {
				// CLose streams
				if (bufferedReader != null)
					bufferedReader.close();
				if (bufferedWriter != null)
					bufferedWriter.close();
			}
			return ret;
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			firePropertyChange(MIAPE_MS_CHECKING_ERROR, null, e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			firePropertyChange(MIAPE_MS_CHECKING_ERROR, null, e.getMessage());

		}

		return null;
	}

	/**
	 * Pass the {@link MiapeMSDocument} to the {@link MiapeExtractionFrame}
	 * 
	 * @param miapeMS
	 */
	public void finished(String selectedConfigurationName) {

		firePropertyChange(MIAPE_MS_CHECKING_DONE, null, null);
		// In this case, cancel task to not go to the "done"
		reportExtractionDone = false;
		parent.setVisible(true);
		parent.initMetadataCombo(selectedConfigurationName, OntologyLoaderTask.getCvManager());
	}

	@Override
	protected void done() {
		if (!isCancelled()) {
			log.info("MIAPEMSChecker done");
			if (reportExtractionDone) {
				firePropertyChange(MIAPE_MS_METADATA_EXTRACTION_DONE, null, null);
			}
		}
		super.done();
	}

	public MiapeMSDocument getMiapeMSMetadata(String projectName) {
		if (forms != null) {
			return forms.getMiapeMSMetadata(projectName);
		}
		return null;
	}

}
