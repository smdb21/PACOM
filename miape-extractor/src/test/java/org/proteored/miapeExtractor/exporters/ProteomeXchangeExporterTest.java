package org.proteored.miapeExtractor.exporters;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.proteored.miapeExtractor.analysis.conf.ExperimentListAdapter;
import org.proteored.miapeExtractor.analysis.conf.jaxb.CPExperimentList;
import org.proteored.miapeExtractor.analysis.exporters.ProteomeXchangeFilev2_1;
import org.proteored.miapeExtractor.analysis.exporters.tasks.PEXBulkSubmissionFileWriterTask;
import org.proteored.miapeExtractor.analysis.exporters.util.PexSubmissionType;
import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.ExperimentList;
import org.proteored.miapeapi.experiment.model.Replicate;
import org.proteored.miapeapi.zip.ZipManager;
import org.springframework.core.io.ClassPathResource;

public class ProteomeXchangeExporterTest {

	@Test
	public void proteomeXchangeTest() {
		try {
			ClassPathResource resource = new ClassPathResource(
					"projectFileTest.xml");
			CPExperimentList cpExpList = new ExperimentListAdapter(
					resource.getFile()).getCpExperimentList();
			ExperimentList experimentList = new ExperimentListAdapter(cpExpList)
					.adapt();
			ProteomeXchangeFilev2_1 pexFile = new ProteomeXchangeFilev2_1(
					new File("C:\\proteomeXchangeExporterTest"),
					experimentList, null);

			for (Experiment experiment : experimentList.getExperiments()) {
				int num = 1;
				for (Replicate replicate : experiment.getReplicates()) {
					List<String> files = new ArrayList<String>();
					files.add("c:\\proteomeXchangeExporterTest\\RAW_File_"
							+ num++ + "_fake.xml");
					// pexFile.addReplicateRawFiles(replicate, files);
				}
			}
			pexFile.setComment("This is a comment from the test");
			pexFile.setPrideLogin("Login to pride");
			pexFile.setProjectShortDescription("Project short description");
			pexFile.addSpecie("Human");
			pexFile.addSpecie("Rat");
			pexFile.addSpecie("Mouse");
			pexFile.addKeyword("MIAPE");
			pexFile.addKeyword("HUPO-PSI");
			pexFile.addKeyword("ProteoRed");
			pexFile.setSubmissionType(PexSubmissionType.COMPLETE);
			pexFile.addPubmed("1234234");
			pexFile.setResubmission_px("234089023");
			pexFile.addReanalyses_px("2234234");
			PEXBulkSubmissionFileWriterTask task = new PEXBulkSubmissionFileWriterTask(
					pexFile);
			File exportedFile = task.export();
			if (exportedFile == null || !exportedFile.exists())
				fail();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	@Test
	public void proteomeXchangePMEQCBulFileGenerator() {
		try {
			File outputFolder = new File(
					"Y:\\ftpEstrellaPolar\\pmeQC\\Final Data");
			File outputFile = new File(outputFolder.getAbsolutePath()
					+ "\\pexSubmission.px");
			FileWriter fw = new FileWriter(outputFile);

			HashMap<String, List<File>> map = new HashMap<String, List<File>>();
			for (File file : outputFolder.listFiles()) {
				String key = FilenameUtils.getBaseName(file.getAbsolutePath());
				key = FilenameUtils.getBaseName(key);
				if (!map.containsKey(key)) {
					List<File> files = new ArrayList<File>();
					files.add(file);
					map.put(key, files);
				} else {
					map.get(key).add(file);
				}
			}

			List<String> sortedKeys = new ArrayList<String>();
			for (String key : map.keySet()) {
				sortedKeys.add(key);
			}
			Collections.sort(sortedKeys);

			int numFile = 1;
			for (String key : sortedKeys) {
				if (key.equals("pexSubmission")
						|| key.equals("submission_file"))
					continue;
				List<File> list = map.get(key);

				File file = getMzIdentML(list);
				if (file == null)
					System.out.println("No hay mzid para: " + key);
				String relationsships = "";
				for (int j = 1; j < list.size() - 1; j++) {
					if (!"".equals(relationsships))
						relationsships += ",";
					relationsships += j + numFile;
				}
				fw.write("FME\t" + numFile++ + "\tsearch\t"
						+ file.getAbsolutePath() + "\t" + relationsships + "\n");

				File protXMLFile = getProtXML(list);
				relationsships = "";
				for (int j = 2; j < list.size() - 1; j++) {
					if (!"".equals(relationsships))
						relationsships += ",";
					relationsships += j + numFile - 1;
				}
				fw.write("FME\t" + numFile++ + "\tsearch\t"
						+ protXMLFile.getAbsolutePath() + "\t" + relationsships
						+ "\n");

				File pepXMLFile = getPepXML(list);
				relationsships = "";
				for (int j = 3; j < list.size() - 1; j++) {
					if (!"".equals(relationsships))
						relationsships += ",";
					relationsships += j + numFile - 2;
				}
				fw.write("FME\t" + numFile++ + "\tsearch\t"
						+ pepXMLFile.getAbsolutePath() + "\t" + relationsships
						+ "\n");

				File rawfile = getRaw(list);
				if (rawfile == null)
					System.out.println("No hay raw para: " + key);
				fw.write("FME\t" + numFile++ + "\traw\t"
						+ rawfile.getAbsolutePath() + "\n");

			}

			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	private File getProtXML(List<File> list) {
		for (File file : list) {
			if (FilenameUtils.getExtension(file.getAbsolutePath())
					.equals("xml")) {
				if (FilenameUtils.getExtension(
						FilenameUtils.getBaseName(file.getAbsolutePath()))
						.equals("prot"))
					return file;
			}
		}
		return null;
	}

	private File getPepXML(List<File> list) {
		for (File file : list) {
			if (FilenameUtils.getExtension(file.getAbsolutePath())
					.equals("xml")) {
				if (FilenameUtils.getExtension(
						FilenameUtils.getBaseName(file.getAbsolutePath()))
						.equals("pep"))
					return file;
			}
		}
		return null;
	}

	private File getRaw(List<File> list) throws IOException {
		for (File file : list) {
			if (FilenameUtils.getExtension(file.getAbsolutePath())
					.equalsIgnoreCase("raw")) {
				return ZipManager.compressGZipFile(file);
			}
		}
		return null;
	}

	private File getMGF(List<File> list) {
		for (File file : list) {
			if (FilenameUtils.getExtension(file.getAbsolutePath())
					.equalsIgnoreCase("mgf")) {
				return file;
			}
		}
		return null;
	}

	private File getMzIdentML(List<File> list) {
		for (File file : list) {
			if (FilenameUtils.getExtension(file.getAbsolutePath())
					.equals("xml")) {
				if (FilenameUtils.getExtension(
						FilenameUtils.getBaseName(file.getAbsolutePath()))
						.equals("mzident"))
					return file;
			}
		}
		return null;
	}
}
