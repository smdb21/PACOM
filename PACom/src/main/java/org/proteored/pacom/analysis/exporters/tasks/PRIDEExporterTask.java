package org.proteored.pacom.analysis.exporters.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.exceptions.WrongXMLFormatException;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.xml.pride.adapter.ExperimentCollectionAdapterFromExperiment;
import org.proteored.miapeapi.xml.pride.autogenerated.ExperimentCollection;
import org.proteored.miapeapi.zip.ZipManager;
import org.proteored.pacom.analysis.exporters.Exporter;
import org.proteored.pacom.gui.MainFrame;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;

public class PRIDEExporterTask extends SwingWorker<Void, Void> implements Exporter<List<File>> {
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private final ExperimentList experimentList;

	private final File outputFolder;

	private final boolean addPeakLists;
	private final boolean removeNotMatchedSpectra;
	private static JAXBContext jc;

	public static final String SINGLE_PRIDE_EXPORTED = "single PRIDE Exported";
	public static final String SINGLE_PRIDE_EXPORTED_ERROR = "single PRIDE Exported error";
	public static final String SINGLE_PRIDE_COMPRESSING_STARTED = "single PRIDE compressing started";
	public static final String SINGLE_PRIDE_COMPRESSING_FINISHED = "single PRIDE compressing finished";
	public static final String PRIDE_EXPORTER_ERROR = "pride exporter error";
	public static final String PRIDE_EXPORTER_DONE = "pride exporter done";
	public static final String PRIDE_EXPORTER_STARTED = "pride exporter started";

	public static final String SINGLE_PRIDE_EXPORTING_STARTED = "single pride exporting started";

	public static final String SINGLE_PRIDE_ALREADY_PRESENT = "pride already present";
	private List<File> generatedFiles = null;

	private final boolean compressResultingFiles;

	private final boolean excludeNonConclusiveProteins;
	static {
		try {
			jc = JAXBContext.newInstance("org.proteored.miapeapi.xml.pride.autogenerated");
		} catch (JAXBException e) {
			e.printStackTrace();
			log.error(e);
			throw new WrongXMLFormatException(e);
		}
	}

	public PRIDEExporterTask(ExperimentList experimentList, File outputFolder, boolean addPeakLists,
			boolean removeNotMatchedSpectra, boolean compressResultingFiles, boolean excludeNonConclusiveProteins) {
		this.experimentList = experimentList;
		this.outputFolder = outputFolder;
		this.addPeakLists = addPeakLists;
		this.generatedFiles = new ArrayList<File>();
		this.removeNotMatchedSpectra = removeNotMatchedSpectra;
		this.compressResultingFiles = compressResultingFiles;
		this.excludeNonConclusiveProteins = excludeNonConclusiveProteins;
	}

	@Override
	protected Void doInBackground() {
		firePropertyChange(PRIDE_EXPORTER_STARTED, null, null);
		export();
		return null;
	}

	@Override
	protected void done() {
		super.done();
		if (!isCancelled()) {
			firePropertyChange(PRIDE_EXPORTER_DONE, null, generatedFiles);

		}
	}

	@Override
	public List<File> export() {
		log.info("PRIDE Exporter started");
		Marshaller marshaller;
		try {
			marshaller = jc.createMarshaller();

			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));

			for (Experiment experiment : this.experimentList.getExperiments()) {
				try {
					String newFilePath = outputFolder.getAbsolutePath() + File.separator + experiment.getName()
							+ ".xml";
					File xmlFile = new File(newFilePath);
					if (xmlFile.exists()) {
						firePropertyChange(SINGLE_PRIDE_ALREADY_PRESENT, null, xmlFile);

						if (compressResultingFiles) {
							firePropertyChange(SINGLE_PRIDE_COMPRESSING_STARTED, null, xmlFile);
							File compressedXmlFile = ZipManager.compressGZipFile(xmlFile);
							xmlFile.delete();
							firePropertyChange(SINGLE_PRIDE_COMPRESSING_FINISHED, null, compressedXmlFile);
							experiment.setPrideXMLFile(compressedXmlFile);
							generatedFiles.add(compressedXmlFile);
						} else {

							experiment.setPrideXMLFile(xmlFile);
							generatedFiles.add(xmlFile);
						}
						continue;
					}

					firePropertyChange(SINGLE_PRIDE_EXPORTING_STARTED, null, experiment.getFullName());
					log.info("output file for experiment: " + xmlFile.getAbsolutePath());
					ExperimentCollection experimentCollection = new ExperimentCollectionAdapterFromExperiment(
							experiment, OntologyLoaderTask.getCvManager(), this.addPeakLists,
							this.removeNotMatchedSpectra, this.excludeNonConclusiveProteins,
							"PACOM v" + MainFrame.getVersion()).adapt();
					log.info("adapted to XML: " + experimentCollection);

					marshaller.marshal(experimentCollection, xmlFile);

					log.info("created file: " + xmlFile.getAbsolutePath() + " - " + xmlFile.length() / 1024 / 1024
							+ "Mbytes");

					if (compressResultingFiles) {
						firePropertyChange(SINGLE_PRIDE_COMPRESSING_STARTED, null, xmlFile);
						File compressedXmlFile = ZipManager.compressGZipFile(xmlFile);
						xmlFile.delete();
						firePropertyChange(SINGLE_PRIDE_COMPRESSING_FINISHED, null, compressedXmlFile);
						firePropertyChange(SINGLE_PRIDE_EXPORTED, null, compressedXmlFile);
						experiment.setPrideXMLFile(compressedXmlFile);
						generatedFiles.add(compressedXmlFile);
					} else {
						firePropertyChange(SINGLE_PRIDE_EXPORTED, null, xmlFile);
						experiment.setPrideXMLFile(xmlFile);
						generatedFiles.add(xmlFile);
					}
				} catch (Exception e) {
					e.printStackTrace();
					log.warn(e.getMessage());
					firePropertyChange(SINGLE_PRIDE_EXPORTED_ERROR, null, e.getMessage());
				}
			}

		} catch (JAXBException e1) {
			e1.printStackTrace();
			firePropertyChange(PRIDE_EXPORTER_ERROR, null, e1.getMessage());
		}
		return null;
	}

}
