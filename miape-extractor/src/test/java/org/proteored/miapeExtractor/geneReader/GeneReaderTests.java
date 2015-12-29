package org.proteored.miapeExtractor.geneReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.proteored.miapeExtractor.analysis.genes.ENSGInfo;
import org.proteored.miapeExtractor.analysis.genes.GeneDistributionReaderOLD;
import org.proteored.miapeExtractor.analysis.genes.GeneDistributionReader;
import org.proteored.miapeapi.experiment.model.IdentifierParser;
import org.proteored.miapeapi.util.ProteinSequenceRetrieval;

import com.compomics.dbtoolkit.io.implementations.FASTADBLoader;
import com.compomics.util.protein.Header;
import com.compomics.util.protein.Protein;

public class GeneReaderTests {

	@Test
	public void compareGeneReaders() throws IOException {

		final HashMap<String, List<ENSGInfo>> proteinGeneMapping = GeneDistributionReaderOLD
				.getInstance().getProteinGeneMapping(null);

		final HashMap<String, List<ENSGInfo>> proteinGeneMapping2 = GeneDistributionReader
				.getInstance().getProteinGeneMapping(null);
		System.out.println(proteinGeneMapping.size() + " -> "
				+ proteinGeneMapping2.size());

		File file = new File("c:\\users\\salva\\desktop\\test_prot.tsv");
		InputStream fstream = new FileInputStream(file);
		if (fstream != null) {
			DataInputStream in = new DataInputStream(fstream);
			String line;
			Set<String> genes1 = new HashSet<String>();
			Set<String> genes2 = new HashSet<String>();
			Set<String> genes1_16 = new HashSet<String>();
			Set<String> genes2_16 = new HashSet<String>();
			while ((line = in.readLine()) != null) {
				String[] split = line.split("\t");
				String accs = split[7];
				String acc = "";
				if (accs.contains(";"))
					acc = accs.split(";")[0];
				else
					acc = accs;

				final List<ENSGInfo> list = proteinGeneMapping.get(acc);
				if (list != null)
					for (ENSGInfo ensgInfo : list) {
						genes1.add(ensgInfo.getEnsG_ID());
						if ("16".equals(ensgInfo.getChrName()))
							genes1_16.add(ensgInfo.getEnsG_ID());
					}
				final List<ENSGInfo> list2 = proteinGeneMapping2.get(acc);
				if (list2 != null)
					for (ENSGInfo ensgInfo : list2) {
						genes2.add(ensgInfo.getEnsG_ID());
						if ("16".equals(ensgInfo.getChrName()))
							genes2_16.add(ensgInfo.getEnsG_ID());
					}
			}

			System.out.println("Genes1=" + genes1.size());
			System.out.println("Genes2=" + genes2.size());

			System.out.println("Genes1 del 16=" + genes1_16.size());
			System.out.println("Genes2 del 16=" + genes2_16.size());

		}
	}

	@Test
	public void testGetCHRsFromACCList() {
		try {
			HashMap<String, List<ENSGInfo>> proteinGeneMapping = GeneDistributionReaderOLD
					.getInstance().getProteinGeneMapping(null);

			String file = "C:\\Users\\Salva\\Desktop\\ACCs.txt";
			String fileOutput = "C:\\Users\\Salva\\Desktop\\ACCs_out.txt";
			ClassLoader cl = this.getClass().getClassLoader();
			final InputStream fstream = new FileInputStream(new File(file));
			final OutputStream fostream = new FileOutputStream(new File(
					fileOutput));
			if (fstream != null) {
				DataInputStream in = new DataInputStream(fstream);
				DataOutputStream on = new DataOutputStream(fostream);

				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						on));
				String strLine;
				int numLines = 0;

				while ((strLine = br.readLine()) != null) {
					String geneNames = "";
					String chr = "";
					Set<String> chrNames = new HashSet<String>();
					if (proteinGeneMapping.containsKey(strLine)) {

						List<ENSGInfo> list = proteinGeneMapping.get(strLine);

						for (ENSGInfo ensgInfo : list) {
							if (ensgInfo.getGeneName() != null) {
								if (!"".equals(geneNames))
									geneNames += ",";
								geneNames += ensgInfo.getGeneName();
							}
						}
						for (ENSGInfo ensgInfo : list) {
							String chrName = ensgInfo.getChrName();
							if (!chrNames.contains(chrName)) {
								chrNames.add(chrName);
								if (!"".equals(chr) && chr != null)
									chr += ",";
								chr += chrName;
							}
						}

					}
					bw.write(strLine + "\t" + chr + "\t" + geneNames);
					bw.newLine();
					numLines++;
				}
				bw.close();
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCarmen() {
		Set<String> cannonicalProteinAccs = new HashSet<String>();
		HashMap<String, List<ENSGInfo>> proteinGeneMapping = GeneDistributionReaderOLD
				.getInstance().getProteinGeneMapping(null);
		IdentifierParser.setRemove_acc_version(true);
		for (String proteinACC : proteinGeneMapping.keySet()) {
			String cannonicalProteinACC = IdentifierParser.parseACC(proteinACC);
			cannonicalProteinAccs.add(cannonicalProteinACC);
		}

		// for (String string : cannonicalProteinAccs) {
		// // System.out.println(string);
		// }
		List<ENSGInfo> list = proteinGeneMapping.get("P0C0L5");
		for (ENSGInfo ensgInfo : list) {
			System.out.println(ensgInfo);
		}
		System.out.println(cannonicalProteinAccs.size());
	}

	/**
	 * This test is designed to get the mapping file for use it in the MIAPE
	 * Extractor.<br>
	 * Gets the fasta files located in the downloads folder and parse it to
	 * create a TSV file.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetUniprotMappings() throws IOException {
		String fileOutput = "C:\\Users\\Salva\\Desktop\\Uniprot_Ensembl_Map.txt";
		final OutputStream fostream = new FileOutputStream(new File(fileOutput));

		DataOutputStream on = new DataOutputStream(fostream);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(on));
		File files = new File("C:\\Users\\Salva\\Downloads");

		HashMap<String, ENSGInfo> geneMap = new HashMap<String, ENSGInfo>();
		for (File file : files.listFiles()) {
			if (file.getAbsolutePath().endsWith("fasta")) {
				FASTADBLoader loader = new FASTADBLoader();
				loader.load(file.getAbsolutePath());
				Protein nextProtein = null;
				while ((nextProtein = loader.nextProtein()) != null) {
					Header header = nextProtein.getHeader();
					String description = header.getDescription();
					String ensP = "";
					String ensG = "";
					String geneName = "";
					String chr = "";
					String[] split = description.split("\\s+");
					for (String string : split) {
						if (string.contains("\\Gene=")) {
							String[] split2 = string.split("=");
							ensG = split2[1];
						} else if (string.contains("\\Gname=")) {
							String[] split2 = string.split("=");
							geneName = split2[1];
						} else if (string.contains("\\Protein=")) {
							String[] split2 = string.split("=");
							ensP = split2[1];
						} else if (string.contains("\\Chromosome=")) {
							String[] split2 = string.split("=");
							chr = split2[1];
						}
					}
					if (!"".equals(ensG)) {
						if (!geneMap.containsKey(ensG)) {
							ENSGInfo gene = new ENSGInfo();
							gene.setChrName(chr);
							gene.setEnsG_ID(ensG);
							gene.setGeneName(geneName);
							gene.addProteinACC(nextProtein.getHeader()
									.getAccession());
							gene.addENSP(ensP);
							geneMap.put(ensG, gene);
						} else {
							ENSGInfo gene = geneMap.get(ensG);
							if (!"".equals(chr)) {
								if (!gene.getChrName().equals(chr)) {
									if (gene.getChrName().contains(",")) {
										final String[] split2 = gene
												.getChrName().split(",");
										boolean found = false;
										for (String string : split2) {
											if (string.equals(chr))
												found = true;
										}
										if (!found)
											gene.setChrName(gene.getChrName()
													+ "," + chr);
									} else {
										gene.setChrName(gene.getChrName() + ","
												+ chr);
									}
								}
								gene.setChrName(chr);
							}
							gene.setEnsG_ID(ensG);
							if (!"".equals(geneName)) {
								if (!gene.getGeneName().equals(geneName)) {
									if (gene.getGeneName().contains(",")) {
										final String[] split2 = gene
												.getGeneName().split(",");
										boolean found = false;
										for (String string : split2) {
											if (string.equals(geneName))
												found = true;
										}
										if (!found)
											gene.setGeneName(gene.getGeneName()
													+ "," + geneName);
									} else {
										gene.setGeneName(gene.getGeneName()
												+ "," + geneName);
									}
								} else {
									gene.setGeneName(geneName);
								}
							}
							gene.addProteinACC(nextProtein.getHeader()
									.getAccession());
							gene.addENSP(ensP);
						}
					}
				}
			}
		}

		for (ENSGInfo gene : geneMap.values()) {
			String outputLine = gene.getEnsG_ID() + "\t"
					+ getString(gene.getENSPs()) + "\t"
					+ getString(gene.getGeneName()) + "\t"
					+ getString(gene.getChrName()) + "\t"
					+ getString(gene.getProteinACC());
			if (gene.getProteinACC().size() != gene.getENSPs().size())
				System.out.println(gene.getEnsG_ID());
			// System.out.println(outputLine);
			bw.write(outputLine);
			bw.newLine();

		}

		bw.close();

	}

	private String getString(String string) {
		if (string == null || "".equals(string))
			return "-";
		return string;
	}

	private String getString(List<String> strings) {
		String ret = "";
		for (String string : strings) {
			if (!"".equals(ret))
				ret += ",";
			ret += string;
		}
		return ret;
	}

	@Test
	public void getProteinsFromGeneNames() {
		String[] geneNames = { "NAALAD2", "REG4", "CD36", "GPC3", "MLLT6",
				"unacharacterized_protein_Ip9056.", "F10", "AFP", "LYZ",
				"SPINK1", "CARM1", "VIL1", "THOC5", "CYB5B", "FAM169A", "CST1",
				"VSNL1", "KIAA1199", "SORBS1", "LGALS4", "PRAP1", "CCL15",
				"KITLG", "F5", "SERPIND1", "MESDC2", "XPC",
				"cdna_flj57640,_highly_similar_to_serpin_b5.", "CPOX",
				"SERPINA4", "OXR1" };
		Set<String> geneNameSet = new HashSet<String>();
		for (String geneName : geneNames) {
			geneNameSet.add(geneName);
		}
		HashMap<String, List<String>> geneNameHashMap = new HashMap<String, List<String>>();
		HashMap<String, List<ENSGInfo>> proteinGeneMapping = GeneDistributionReaderOLD
				.getInstance().getProteinGeneMapping(null);
		for (String proteinACC : proteinGeneMapping.keySet()) {
			List<ENSGInfo> ensgList = proteinGeneMapping.get(proteinACC);
			for (ENSGInfo ensgInfo : ensgList) {
				final String geneName = ensgInfo.getGeneName();
				if (geneNameSet.contains(geneName)) {
					// System.out.println(geneName + " -> " + proteinACC);
					if (!geneNameHashMap.containsKey(geneName)) {
						List<String> list = new ArrayList<String>();
						list.add(proteinACC);
						geneNameHashMap.put(geneName, list);
					} else {
						geneNameHashMap.get(geneName).add(proteinACC);
					}
				}
			}
		}
		for (String geneName : geneNames) {
			final List<String> proteinAccs = geneNameHashMap.get(geneName);
			if (proteinAccs != null) {

				for (String proteinACC : proteinAccs) {
					String proteinName = ProteinSequenceRetrieval
							.getProteinNameFromUniprot(proteinACC);
					System.out.println(geneName + "\t" + proteinACC + "\t"
							+ proteinName);
				}
			} else {
				System.out.println(geneName);
			}
		}
	}

}
