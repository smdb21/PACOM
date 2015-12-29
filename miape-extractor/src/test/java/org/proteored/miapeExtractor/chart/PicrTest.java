package org.proteored.miapeExtractor.chart;

import java.util.List;

import org.junit.Test;
import org.proteored.miapeExtractor.analysis.util.PICRInterfaz;

import uk.ac.ebi.demo.picr.business.PICRClient;
import uk.ac.ebi.demo.picr.soap.CrossReference;
import uk.ac.ebi.demo.picr.soap.UPEntry;

public class PicrTest {
	private static final String SEPARATOR = "\t";

	@Test
	public void testPICR() {
		PICRClient client = new PICRClient();

		String[] accessions = { "ENSG00000003249" };
		Object[] databases = { "SWISSPROT", "TREMBL", "TREMBL_VARSPLIC",
				"SWISSPROT_GENE", "SWISSPROT_ID", "SWISSPROT_VARSPLIC" };
		Object[] ens_databases = { "ENSEMBL_HUMAN", "ENSEMBL" };
		String[] protein_accs = { "Q53FZ2" };
		for (String accession : accessions) {
			System.out.println(accession);
			final List<UPEntry> performAccessionMapping = client
					.performAccessionMapping(accession, ens_databases);
			for (UPEntry upEntry : performAccessionMapping) {
				System.out.println("IDENTICAL");
				final List<uk.ac.ebi.demo.picr.soap.CrossReference> identicalCrossReferences = upEntry
						.getIdenticalCrossReferences();
				// System.out.println("\nSEQ:");
				// System.out.println(upEntry.getSequence());
				if (identicalCrossReferences != null) {
					for (uk.ac.ebi.demo.picr.soap.CrossReference crossReference : identicalCrossReferences) {
						System.out.println(crossReference.getAccession()
								+ " version:"
								+ crossReference.getAccessionVersion()
								+ " from " + crossReference.getDatabaseName());
					}
				}
				System.out.println("LOGICAL");
				final List<uk.ac.ebi.demo.picr.soap.CrossReference> logicalCrossReferences = upEntry
						.getLogicalCrossReferences();
				// System.out.println("\nSEQ:");
				// System.out.println(upEntry.getSequence());
				if (identicalCrossReferences != null) {
					for (uk.ac.ebi.demo.picr.soap.CrossReference crossReference : logicalCrossReferences) {
						System.out.println(crossReference.getAccession()
								+ " version:"
								+ crossReference.getAccessionVersion()
								+ " from " + crossReference.getDatabaseName());
					}
				}
			}
		}
	}

	@Test
	public void testInterfaz() {
		final List<CrossReference> ensGeneCrossReferences = PICRInterfaz
				.getENSGeneCrossReferences("ENSP00000306407");
		for (CrossReference crossReference : ensGeneCrossReferences) {
			System.out.println(crossReference.getAccession() + " version:"
					+ crossReference.getAccessionVersion() + " from "
					+ crossReference.getDatabaseName());
		}
	}

	// @Test
	// public void testCreateFullCrossReferences() throws IOException {
	// final HashMap<String, ENSGInfo> genes =
	// GeneDistributionReader.getInstance().getGeneInfoFromProteoRedDistribution();
	// File outFile = new File(
	// "C:\\Users\\Salva\\Workspaces\\MyEclipse 10\\miape-extractor\\src\\main\\resources\\fullChr16mapping2uniprot.tsv");
	// FileOutputStream outStream = new FileOutputStream(outFile);
	// DataOutputStream dataOutput = new DataOutputStream(outStream);
	//
	// BufferedWriter bw = new BufferedWriter(new
	// OutputStreamWriter(dataOutput));
	// int total = genes.size();
	// int num = 0;
	// bw.write("ensgID" + SEPARATOR + "Accession" + SEPARATOR +
	// "AccessionVersion" + SEPARATOR
	// + "DatabaseName" + SEPARATOR + "DatabaseDescription" + SEPARATOR + "Gi" +
	// SEPARATOR
	// + "TaxonId" + SEPARATOR + "DateAdded" + SEPARATOR + "DateDeleted" +
	// "\n");
	//
	// for (String ensgID : genes.keySet()) {
	// final List<CrossReference> ensGeneCrossReferences = PICRInterfaz
	// .getENSGeneCrossReferences(ensgID);
	// Set<String> uniprotEntries = new HashSet<String>();
	// for (CrossReference crossReference : ensGeneCrossReferences) {
	// if (!uniprotEntries.contains(crossReference.getAccession())) {
	// uniprotEntries.add(crossReference.getAccession());
	// System.out.println(ensgID + " - " + crossReference.getAccession() + " - "
	// + crossReference.getAccessionVersion() + " - "
	// + crossReference.getDatabaseName());
	// bw.write(ensgID + SEPARATOR + crossReference.getAccession() + SEPARATOR
	// + crossReference.getAccessionVersion() + SEPARATOR
	// + crossReference.getDatabaseName() + SEPARATOR
	// + crossReference.getDatabaseDescription() + SEPARATOR
	// + crossReference.getGi() + SEPARATOR + crossReference.getTaxonId()
	// + SEPARATOR + crossReference.getDateAdded() + SEPARATOR
	// + crossReference.getDateDeleted() + "\n");
	// } else {
	// System.out.println(crossReference.getAccession() + " is already taken");
	// }
	// }
	// num++;
	// System.out.println("Progress = " + Double.valueOf(num) * 100 / total);
	// }
	// bw.close();
	// dataOutput.close();
	// outStream.close();
	//
	// }
}
