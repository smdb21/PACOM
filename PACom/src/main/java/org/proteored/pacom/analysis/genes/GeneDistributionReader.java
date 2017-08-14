package org.proteored.pacom.analysis.genes;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedProtein;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.ProteinGroup;
import org.proteored.miapeapi.experiment.model.ProteinGroupOccurrence;
import org.proteored.miapeapi.experiment.model.grouping.ProteinEvidence;
import org.proteored.pacom.utils.PropertiesReader;

import edu.scripps.yates.utilities.fasta.FastaParser;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class GeneDistributionReader {
	public static final String[] chromosomeNames = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
			"13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "MT", "X", "Y" };
	// private static final String chromosomeFileName = "nextprot_chromosome_";
	private final InputStream uniprotEnsemblMapInputStream;
	// private static final String ensg2uniprotFileName =
	// "Chr16_EnsG2Uniprot.tsv";
	// private static final String ensg2uniprotChr16FileName =
	// "genePepUniprotChr16Mapping.tsv";
	private static final String ensg2uniprotFileName = PropertiesReader.getProperties()
			.getProperty(PropertiesReader.ENSEMBL_UNIPROT_MAPPING_FILE);
	// private static final String geneProteinMappingFileName =
	// "GeneXTransXPep_Ensemblv65.tsv";
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private static GeneDistributionReader instance;

	/**
	 * Gets the reader using a custom uniprot and ensembl mapping
	 * 
	 * @param uniprotEnsemblMapFile
	 * @throws FileNotFoundException
	 */
	private GeneDistributionReader(File uniprotEnsemblMapFile) throws FileNotFoundException {
		uniprotEnsemblMapInputStream = new FileInputStream(uniprotEnsemblMapFile);
	}

	/**
	 * Gets the reader using the default mapping between uniprot and ensembl
	 * 
	 * @throws URISyntaxException
	 */
	private GeneDistributionReader() {
		ClassLoader cl = this.getClass().getClassLoader();
		uniprotEnsemblMapInputStream = cl.getResourceAsStream(ensg2uniprotFileName);

	}

	public static GeneDistributionReader getInstance() {
		if (instance == null) {
			instance = new GeneDistributionReader();
		}
		return instance;
	}

	public static GeneDistributionReader getInstance(File uniprotEnsemblMapFile) throws FileNotFoundException {
		if (instance == null) {
			instance = new GeneDistributionReader(uniprotEnsemblMapFile);
		}

		return instance;
	}

	private Map<String, List<ENSGInfo>> proteinGeneMapping;
	private Map<String, Map<String, List<ENSGInfo>>> proteinGeneMappingByChromosome;

	/**
	 * Gets a hasMap with keys=uniprotACC and values=List of {@link ENSGInfo}
	 * 
	 * @Param chrName name of the chromosome that is taken into account. If
	 *        null, all chromosomes will be read.
	 * @return
	 */
	public Map<String, List<ENSGInfo>> getProteinGeneMapping(String chrName) {
		// Get proteins from Chr16 according to the table that comes from
		// ENSEMBL
		if (proteinGeneMapping != null) {
			if (chrName != null) {
				Map<String, List<ENSGInfo>> ret = getChromosomeProteinGeneMapping(chrName);
				return ret;
			} else
				return proteinGeneMapping;
		}

		proteinGeneMapping = new THashMap<String, List<ENSGInfo>>();
		proteinGeneMappingByChromosome = new THashMap<String, Map<String, List<ENSGInfo>>>();
		// Map<String, ENSGInfo> assignedGenes =
		// getGeneInfoFromProteoRedDistribution();
		Map<String, ENSGInfo> assignedGenes = new THashMap<String, ENSGInfo>();
		// log.info("Num assigned genes = " + assignedGenes.size());
		// log.info("Mapping ENSG Ids to Uniprot IDS");
		mapGenesToUniprot(assignedGenes);
		// log.info(proteinGeneMapping.size() + " proteins mapped to a Gene");

		// log.info("Getting Gene information from UNIPROT fasta files");
		// Add information from TREMBL and SWISSPROT FILES
		// addChr16InfoFromOtherFiles(assignedGenes, chrName);
		// log.info(proteinGeneMapping.size() + " proteins mapped to a Gene");
		// log.info("Getting Gene information from other chromosomes (nextprot
		// files)");
		// ADD information from other chromosomes from NextProt mapping

		// addOtherChromosomesInfo(chrName);
		// log.info(proteinGeneMapping.size() + " proteins mapped to a Gene");

		return proteinGeneMapping;
	}

	private Map<String, List<ENSGInfo>> getChromosomeProteinGeneMapping(String chrName) {
		// if (this.proteinGeneMapping != null) {
		if (proteinGeneMappingByChromosome != null) {
			return proteinGeneMappingByChromosome.get(chrName);
		}
		// Map<String, List<ENSGInfo>> ret = new THashMap<String,
		// List<ENSGInfo>>();
		//
		// for (String proteinACC : proteinGeneMapping.keySet()) {
		// List<ENSGInfo> list = this.proteinGeneMapping.get(proteinACC);
		// List<ENSGInfo> newList = new ArrayList<ENSGInfo>();
		// for (ENSGInfo ensgInfo : list) {
		// if (chrName.equals(ensgInfo.getChrName())) {
		// newList.add(ensgInfo);
		// }
		// }
		// if (!newList.isEmpty())
		// ret.put(proteinACC, newList);
		// }
		// return ret;
		// }
		return null;
	}

	/**
	 * Gets a mapping between the uniprot accessions and the list of genes,
	 * adding values to the class variable proteinGeneMapping Map<String,
	 * List<ENSGInfo>>.<br>
	 * The information added is comming from the file
	 * 'Uniprot_Ensembl_Map_06_02_2013.txt' and consist on a table with the
	 * following columns:
	 * <ul>
	 * <li>Uniprot ACC</li>
	 * <li>ENSG identifier</li>
	 * <li>ENSP identifier</li>
	 * <li>Gene name</li>
	 * <li>chromosome number</li>
	 * </ul>
	 * That file is automatically generated by the test "testGetUniprotMappings"
	 * in the GeneReaderTests.java file, reading from fasta uniprot and trembl
	 * files generated by the bioinfo group.
	 * 
	 * @param genes
	 */
	private void mapGenesToUniprot(Map<String, ENSGInfo> genes) {
		try {

			if (uniprotEnsemblMapInputStream != null) {
				// Get the object of DataInputStream
				DataInputStream in = new DataInputStream(uniprotEnsemblMapInputStream);
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String strLine;
					int numLines = 0;
					while ((strLine = br.readLine()) != null) {
						numLines++;
						if (numLines > 1) {
							if (strLine.contains(",")) {
								final String[] split = strLine.split(",");

								String ensG_ID = "";
								String ensP_ID = "";
								// String ensT_ID = "";
								String acc = "";
								String acc_trembl = "";
								String chr = "";
								// String pe = "";
								// String score = "";
								// boolean missing = false;
								// boolean anchor = false;
								// if (split[1].equals("1")) {
								// anchor = true;
								// }
								ensG_ID = split[0];
								// ensT_ID = split[1];
								acc = split[2];
								acc_trembl = split[3];
								chr = split[4];
								ensP_ID = split[5];
								// if (split.length > 4)
								// pe = split[4];
								// if (split.length > 5)
								// missing = split[5].equals("1") ? true :
								// false;
								if (ensG_ID != null) {
									ENSGInfo geneInfo = null;
									if (genes.containsKey(ensG_ID)) {
										geneInfo = genes.get(ensG_ID);
									} else {
										geneInfo = new ENSGInfo();
										geneInfo.setEnsG_ID(ensG_ID);
										genes.put(ensG_ID, geneInfo);
									}
									// geneInfo.setMissing(missing);
									if (!"".equals(acc)) {
										geneInfo.addProteinACC(acc);
									}
									if (!"".equals(acc_trembl)) {
										geneInfo.addProteinACC(acc_trembl);
									}
									if (!"".equals(ensP_ID)) {
										geneInfo.addENSP(ensP_ID);
									}
									if (!"".equals(chr)) {
										geneInfo.setChrName(chr);
									}
									// if (!"".equals(pe)) {
									// geneInfo.setProteinEvidence(pe);
									// }

								}
							} else {
								log.warn("The file doesn't contain tabs");
							}
						}
					}
					log.info("Num genes=" + genes.size() + " in " + ensg2uniprotFileName + " file");
					if (genes != null && !genes.isEmpty()) {
						for (ENSGInfo gene : genes.values()) {
							final List<String> proteinACCList = gene.getProteinACC();
							if (proteinACCList != null) {
								for (String proteinACC : proteinACCList) {
									if (!proteinGeneMapping.containsKey(proteinACC)) {
										List<ENSGInfo> geneList = new ArrayList<ENSGInfo>();
										geneList.add(gene);
										proteinGeneMapping.put(proteinACC, geneList);
									} else {
										proteinGeneMapping.get(proteinACC).add(gene);
									}
									// By chromosome
									String chrName = gene.getChrName();
									if (chrName != null) {
										if (!proteinGeneMappingByChromosome.containsKey(chrName)) {
											Map<String, List<ENSGInfo>> proteinGeneMappingChr = new THashMap<String, List<ENSGInfo>>();
											List<ENSGInfo> geneList = new ArrayList<ENSGInfo>();
											geneList.add(gene);
											proteinGeneMappingChr.put(proteinACC, geneList);
											proteinGeneMappingByChromosome.put(chrName, proteinGeneMappingChr);
										} else {
											Map<String, List<ENSGInfo>> proteinGeneMappingChr = proteinGeneMappingByChromosome
													.get(chrName);
											if (!proteinGeneMappingChr.containsKey(proteinACC)) {
												List<ENSGInfo> geneList = new ArrayList<ENSGInfo>();
												geneList.add(gene);
												proteinGeneMappingChr.put(proteinACC, geneList);
											} else {
												proteinGeneMappingChr.get(proteinACC).add(gene);
											}
										}
									}
								}
							}
						}
					}
				} finally {
					in.close();
					log.info(proteinGeneMapping.size() + " proteins mapped to some gene after reading from "
							+ ensg2uniprotFileName);
				}
			} else {
				throw new IllegalMiapeArgumentException(ensg2uniprotFileName + " not found");
			}
		} catch (FileNotFoundException e) {
			log.info(e.getMessage());
			throw new IllegalMiapeArgumentException(e.getMessage());
		} catch (IOException e) {
			log.info(e.getMessage());
			throw new IllegalMiapeArgumentException(e.getMessage());
		}

	}

	// private void addOtherChromosomesInfo(String chrName) {
	// // log.info("Reading from all Human Chromosome NextProt mapping files");
	//
	// for (String chromosomeName : chromosomeNames) {
	// if (chrName != null && !chromosomeName.equals(chrName))
	// continue;
	// ClassLoader cl = this.getClass().getClassLoader();
	// String fileName = chromosomeFileName + chromosomeName + ".txt";
	// final InputStream fstream = cl.getResourceAsStream(fileName);
	// if (fstream != null) {
	// // Get the object of DataInputStream
	// DataInputStream in = new DataInputStream(fstream);
	// try {
	// BufferedReader br = new BufferedReader(
	// new InputStreamReader(in));
	// String strLine;
	// int numGenes = 0;
	// int i = 1;
	// boolean startGeneInfo = false;
	// while ((strLine = br.readLine()) != null) {
	// String string = "Total number of genes:";
	// if (strLine.startsWith(string)) {
	// numGenes = Integer.valueOf(strLine.substring(
	// string.length()).trim());
	// }
	// if (strLine.startsWith("Gene")) {
	// startGeneInfo = true;
	// continue;
	// }
	// if (startGeneInfo && !strLine.startsWith("__")
	// && !strLine.startsWith("name")) {
	// String[] split = strLine.split("\\s+");
	// String geneName = split[0];
	// String nextProtACC = split[1];
	// IdentifierParser.setRemove_acc_version(false);
	// // log.info(nextProtACC);
	// String uniprotACC = IdentifierParser
	// .parseACC(nextProtACC);
	//
	// String proteinEvidence = split[5] + " " + split[6];
	// // log.info("Readed: " + i + "- " + geneName + " " +
	// // nextProtACC + " "
	// // + proteinEvidence + " " + uniprotACC +
	// // " from chromosome "
	// // + chromosomeName);
	// if (proteinGeneMapping.containsKey(uniprotACC)) {
	// List<ENSGInfo> geneList = proteinGeneMapping
	// .get(uniprotACC);
	// boolean geneFound = false;
	// for (ENSGInfo ensgInfo : geneList) {
	// if (geneName.equals(ensgInfo.getGeneName())) {
	// geneFound = true;
	// ensgInfo.setProteinEvidence(proteinEvidence);
	// ensgInfo.addProteinACC(uniprotACC);
	// }
	// }
	// if (!geneFound) {
	// ENSGInfo geneInfo = new ENSGInfo();
	// geneInfo.addProteinACC(uniprotACC);
	// geneInfo.setChrName(chromosomeName);
	// geneInfo.setGeneName(geneName);
	// geneInfo.setProteinEvidence(proteinEvidence);
	// proteinGeneMapping.get(uniprotACC).add(
	// geneInfo);
	// }
	// } else {
	// ENSGInfo geneInfo = new ENSGInfo();
	// geneInfo.addProteinACC(uniprotACC);
	// geneInfo.setChrName(chromosomeName);
	// geneInfo.setGeneName(geneName);
	// geneInfo.setProteinEvidence(proteinEvidence);
	// List<ENSGInfo> geneList = new ArrayList<ENSGInfo>();
	// geneList.add(geneInfo);
	// proteinGeneMapping.put(uniprotACC, geneList);
	// }
	// i++;
	// if (i > numGenes)
	// break;
	// }
	// }
	// } catch (FileNotFoundException e) {
	// log.info(e.getMessage());
	// throw new IllegalMiapeArgumentException(e.getMessage());
	// } catch (IOException e) {
	// log.info(e.getMessage());
	// throw new IllegalMiapeArgumentException(e.getMessage());
	//
	// }
	// }
	// }
	// }

	public static void main(String args[]) {
		final Map<String, List<ENSGInfo>> proteinGeneMapping = GeneDistributionReader.getInstance()
				.getProteinGeneMapping(null);
		System.out.println(proteinGeneMapping.size());

	}

	public Set<ENSGInfo> getGenesFromProteinGroup(ProteinGroupOccurrence proteinGroupOccurrence, String chrName) {
		List<ENSGInfo> ret = new ArrayList<ENSGInfo>();
		Map<String, List<ENSGInfo>> proteinGeneMapping2 = getProteinGeneMapping(chrName);

		List<String> accessions = proteinGroupOccurrence.getAccessions();
		for (String acc : accessions) {
			if (proteinGeneMapping2.containsKey(acc)) {
				List<ENSGInfo> list = proteinGeneMapping2.get(acc);
				for (ENSGInfo ensgInfo : list) {
					if (!ret.contains(ensgInfo))
						ret.add(ensgInfo);
				}

			}
		}
		Set<ENSGInfo> set = new THashSet<ENSGInfo>();
		set.addAll(ret);
		return set;
	}

	public boolean isProteinGroupFromChromosome(ProteinGroupOccurrence proteinGroupOccurrence, String chrName) {
		Map<String, List<ENSGInfo>> proteinGeneMapping2 = getProteinGeneMapping(chrName);

		List<String> accessions = proteinGroupOccurrence.getAccessions();
		for (String acc : accessions) {
			if (proteinGeneMapping2.containsKey(acc)) {
				return true;
			}
		}
		return false;
	}

	public Set<ENSGInfo> getGenesFromProtein(ExtendedIdentifiedProtein protein, String chrName) {
		Set<ENSGInfo> ret = new THashSet<ENSGInfo>();
		Map<String, List<ENSGInfo>> proteinGeneMapping2 = getProteinGeneMapping(chrName);

		String acc = protein.getAccession();

		if (proteinGeneMapping2.containsKey(acc)) {
			List<ENSGInfo> list = proteinGeneMapping2.get(acc);
			for (ENSGInfo ensgInfo : list) {
				if (!ret.contains(ensgInfo))
					ret.add(ensgInfo);
			}

		}

		return ret;
	}

	public Collection<ENSGInfo> getGenesFromProteinGroup(List<ProteinGroup> identifiedProteinGroups) {
		Map<String, ENSGInfo> ret = new THashMap<String, ENSGInfo>();
		Map<String, List<ENSGInfo>> proteinGeneMapping2 = getProteinGeneMapping(null);

		for (ProteinGroup pGroup : identifiedProteinGroups) {
			if (pGroup.getEvidence() != ProteinEvidence.NONCONCLUSIVE) {
				List<String> accessions = pGroup.getAccessions();
				for (String acc : accessions) {
					String uniprotAcc = FastaParser.getUniProtACC(acc);
					if (uniprotAcc != null) {
						String key = uniprotAcc;
						if (proteinGeneMapping2.containsKey(key)) {
							List<ENSGInfo> list = proteinGeneMapping2.get(key);
							for (ENSGInfo ensgInfo : list) {
								if (ensgInfo.getEnsG_ID() != null) {
									if (!ret.containsKey(ensgInfo.getEnsG_ID()))
										ret.put(ensgInfo.getEnsG_ID(), ensgInfo);

								}
							}
						}
					}
				}
			}
		}

		return ret.values();

	}

	public Collection<ENSGInfo> getFirstGenesFromProteinGroup(List<ProteinGroup> identifiedProteinGroups) {
		Map<String, ENSGInfo> ret = new THashMap<String, ENSGInfo>();
		Map<String, List<ENSGInfo>> proteinGeneMapping2 = getProteinGeneMapping(null);

		for (ProteinGroup pGroup : identifiedProteinGroups) {
			if (pGroup.getEvidence() != ProteinEvidence.NONCONCLUSIVE) {
				String acc = pGroup.getAccessions().get(0);
				if (proteinGeneMapping2.containsKey(acc)) {
					List<ENSGInfo> list = proteinGeneMapping2.get(acc);
					for (ENSGInfo ensgInfo : list) {
						if (ensgInfo.getEnsG_ID() != null) {
							if (!ret.containsKey(ensgInfo.getEnsG_ID()))
								ret.put(ensgInfo.getEnsG_ID(), ensgInfo);

						}
					}

				}
			}
		}

		return ret.values();

	}

	public Set<String> getChromosomesFromProteinGroup(ProteinGroupOccurrence proteinGroupOccurrence) {
		Set<String> ret = new THashSet<String>();
		Map<String, List<ENSGInfo>> proteinGeneMapping2 = getProteinGeneMapping(null);

		List<String> accessions = proteinGroupOccurrence.getAccessions();
		for (String acc : accessions) {
			if (proteinGeneMapping2.containsKey(acc)) {
				List<ENSGInfo> list = proteinGeneMapping2.get(acc);
				for (ENSGInfo ensgInfo : list) {
					if (!ret.contains(ensgInfo.getChrName()))
						ret.add(ensgInfo.getChrName());
				}
			}
		}
		return ret;
	}

	public Set<String> getChromosomesFromProtein(ExtendedIdentifiedProtein protein) {
		Set<String> ret = new THashSet<String>();
		Map<String, List<ENSGInfo>> proteinGeneMapping2 = getProteinGeneMapping(null);

		String acc = protein.getAccession();
		if (proteinGeneMapping2.containsKey(acc)) {
			List<ENSGInfo> list = proteinGeneMapping2.get(acc);
			for (ENSGInfo ensgInfo : list) {
				if (!ret.contains(ensgInfo.getChrName()))
					ret.add(ensgInfo.getChrName());
			}
		}

		return ret;
	}

	/**
	 * The first element in the list is the protein distribution and the second,
	 * the gene distribution.<br>
	 * Genedistribution: ChrName - ensGIDs<br>
	 * PRoteinDistribution: ChrName - ProteinACCs <br>
	 * 
	 * @param idSet
	 * @return
	 */
	public List<Map<String, List<String>>> getGeneAndProteinDistributionByChromosome(IdentificationSet idSet,
			String chrName) {
		List<Map<String, List<String>>> ret = new ArrayList<Map<String, List<String>>>();

		Map<String, List<ENSGInfo>> proteinGeneMapping2 = getProteinGeneMapping(chrName);
		// Chromosomename-ensGID
		Map<String, List<String>> geneDistribution = new THashMap<String, List<String>>();
		// Chromosomename-ProteinACCs
		Map<String, List<String>> proteinDistribution = new THashMap<String, List<String>>();
		Collection<ProteinGroupOccurrence> proteinOccurrenceList = idSet.getProteinGroupOccurrenceList().values();
		for (ProteinGroupOccurrence proteinGroupOccurrence : proteinOccurrenceList) {
			if (proteinGroupOccurrence.getEvidence() != ProteinEvidence.NONCONCLUSIVE) {
				final List<String> accessions = proteinGroupOccurrence.getAccessions();
				for (String proteinACC : accessions) {
					if (proteinGeneMapping2.containsKey(proteinACC)) {
						Set<ENSGInfo> geneList = this.getGenesFromProteinGroup(proteinGroupOccurrence, chrName);
						for (ENSGInfo ensgInfo : geneList) {
							String chromosomeName = ensgInfo.getChrName();
							if (geneDistribution.containsKey(chromosomeName)) {
								List<String> ensGID = geneDistribution.get(chromosomeName);
								if (!ensGID.contains(ensgInfo.getEnsG_ID()))
									geneDistribution.get(chromosomeName).add(ensgInfo.getEnsG_ID());
							} else {
								List<String> ensGIDs = new ArrayList<String>();
								ensGIDs.add(ensgInfo.getEnsG_ID());
								geneDistribution.put(chromosomeName, ensGIDs);
							}
						}
						Set<String> chromosomes = getChromosomesFromProteinGroup(proteinGroupOccurrence);
						for (String chromosomeName : chromosomes) {
							if (proteinDistribution.containsKey(chromosomeName)) {
								List<String> accs = proteinDistribution.get(chromosomeName);
								if (!accs.contains(proteinACC)) {
									proteinDistribution.get(chromosomeName).add(proteinACC);
								}
							} else {
								List<String> list = new ArrayList<String>();
								list.add(proteinACC);
								proteinDistribution.put(chromosomeName, list);
							}
						}
					}
				}
			}
		}
		ret.add(proteinDistribution);
		ret.add(geneDistribution);
		return ret;
	}

	/**
	 * 
	 * @param chrName
	 *            if null, all chromosomes will be returned
	 * @return a has map with key=chromosome name and value=list of
	 *         {@link ENSGInfo}
	 */
	public Map<String, List<ENSGInfo>> getGeneDistributionByChromosome(String chrName) {
		if (chrName == null || "".equals(chrName))
			throw new IllegalMiapeArgumentException("Chromosome name cannot be null");

		Map<String, List<ENSGInfo>> ret = new THashMap<String, List<ENSGInfo>>();

		Map<String, List<ENSGInfo>> genes = proteinGeneMappingByChromosome.get(chrName);
		for (List<ENSGInfo> engInfos : genes.values()) {
			for (ENSGInfo ensgInfo : engInfos) {
				if (ensgInfo.getEnsG_ID() != null)
					if (!ret.containsKey(chrName)) {

						List<ENSGInfo> list = new ArrayList<ENSGInfo>();
						list.add(ensgInfo);
						ret.put(chrName, list);
					} else {
						List<ENSGInfo> list = ret.get(chrName);
						if (!list.contains(ensgInfo))
							list.add(ensgInfo);
					}

			}

		}

		return ret;
	}

	public List<String> getProteinsInAChromosome(IdentificationSet idSet, String chrName) {

		return getGeneAndProteinDistributionByChromosome(idSet, chrName).get(0).get(chrName);
	}

	public List<String> getENSGIDInAChromosome(IdentificationSet idSet, String chrName) {

		return getGeneAndProteinDistributionByChromosome(idSet, chrName).get(1).get(chrName);
	}

	public Collection<ENSGInfo> getENSGIDInAChromosome(String chrName) {
		Collection<ENSGInfo> values = getGeneDistributionByChromosome(chrName).get(chrName);
		return values;
	}
}
