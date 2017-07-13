package org.proteored.pacom.analysis.gui.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.proteored.miapeapi.cv.Accession;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.cv.ControlVocabularyTerm;
import org.proteored.miapeapi.cv.msi.PeptideModificationName;
import org.proteored.miapeapi.cv.msi.Score;
import org.proteored.miapeapi.factories.msi.MiapeMSIDocumentBuilder;
import org.proteored.miapeapi.factories.msi.MiapeMSIDocumentFactory;
import org.proteored.miapeapi.factories.msi.PeptideModificationBuilder;
import org.proteored.miapeapi.interfaces.msi.IdentifiedPeptide;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProtein;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProteinSet;
import org.proteored.miapeapi.interfaces.msi.PeptideScore;
import org.proteored.miapeapi.text.tsv.msi.IdentifiedPeptideImplFromTSV;
import org.proteored.miapeapi.text.tsv.msi.IdentifiedProteinImplFromTSV;
import org.proteored.miapeapi.util.UniprotId2AccMapping;
import org.proteored.pacom.gui.MainFrame;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;
import org.springframework.core.io.ClassPathResource;

import com.compomics.dbtoolkit.io.implementations.FASTADBLoader;
import com.compomics.util.protein.Enzyme;
import com.compomics.util.protein.Protein;

import edu.scripps.yates.utilities.fasta.FastaParser;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;
import uk.ac.ebi.pridemod.PrideModController;
import uk.ac.ebi.pridemod.slimmod.model.SlimModCollection;

public class IdentificationSetFromFileParserTask extends SwingWorker<Void, String> {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	public static final String PARSER_STARTS = "PARSER_STARTS";
	public static final String PARSER_ERROR = "PARSER_ERROR";
	public static final String PARSER_FINISHED = "PARSER_FINISHED";
	private static final ClassPathResource resource = new ClassPathResource("modification_mappings.xml");
	/*
	 * HEADERS
	 */
	public static final String ACC = "ACC";
	public static final String SEQ = "SEQ";
	public static final String PSMID = "PSMID";
	public static final String CHARGE = "Z";
	public static final String MZ = "MZ";
	public static final String RT = "RT";

	public static String getHeaderNames() {
		StringBuilder sb = new StringBuilder();
		List<String> headerNames = getHeaderNamesList();
		for (String headerName : headerNames) {
			if (!"".equals(sb.toString())) {
				sb.append(", ");
			}
			sb.append(headerName);
		}
		return sb.toString();
	}

	private static List<String> getHeaderNamesList() {
		List<String> headerNames = new ArrayList<String>();
		headerNames.add(CHARGE);
		headerNames.add(MZ);
		headerNames.add(SEQ);
		headerNames.add(ACC);
		headerNames.add(PSMID);
		Collections.sort(headerNames);
		return headerNames;
	}

	/*
	 * END HEADERS
	 */
	private static final String[] SEPARATORS = { ",", ";", "\t" };
	private final File file;
	private final String separator;
	private final String idSetName;
	private SlimModCollection preferredModifications;
	private static JFileChooser fileChooser;
	private static FASTADBLoader fastaLoader;

	/**
	 * This parser reads a separated values file, in which the first line
	 * (excepting any comment started by #) contains predefined table HEADERS
	 * such as:
	 * <ul>
	 * <li>PSMID</li>
	 * <li>ACC</li>
	 * <li>SEQ</li>
	 * <li>CHARGE</li>
	 * <li>M/Z</li>
	 * </ul>
	 * <br>
	 * And any other column will be considered as a new score with the name of
	 * the header as the name of the score
	 * 
	 * @param selectedFile
	 * @param idSetName
	 * @param separator
	 */
	public IdentificationSetFromFileParserTask(File selectedFile, String idSetName, String separator) {
		file = selectedFile;
		this.separator = separator;
		this.idSetName = idSetName;
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			TObjectIntHashMap<String> indexesByHeaders = new TObjectIntHashMap<String>();
			TObjectIntHashMap<String> indexesByScoreNames = new TObjectIntHashMap<String>();

			firePropertyChange(PARSER_STARTS, null, null);
			BufferedReader dis = new BufferedReader(new FileReader(file));
			String line = "";
			log.info("Parsing file " + file.getAbsolutePath());
			Map<String, IdentifiedProtein> proteins = new THashMap<String, IdentifiedProtein>();
			Map<String, IdentifiedPeptide> peptides = new THashMap<String, IdentifiedPeptide>();
			String previousProteinACC = null;
			// String scoreName = null;
			int numLine = 0;
			while ((line = dis.readLine()) != null) {
				numLine++;
				// COMMENTS STARTING BY #
				if (line.trim().startsWith("#")) {
					// scoreName = getScoreName(line);
					continue;
				}
				String[] split;
				if (line.contains(separator)) {
					split = line.split(separator);
				} else {
					split = new String[1];
					split[0] = line;
				}
				if (split.length > 0) {
					if (indexesByHeaders.isEmpty()) {
						parseHeader(split, indexesByHeaders, indexesByScoreNames);
						if (!indexesByHeaders.containsKey(ACC)) {
							String message = "ACC column for protein accessions is missing in input file '"
									+ file.getAbsolutePath() + "'";
							firePropertyChange(PARSER_ERROR, null, message);
							throw new IllegalArgumentException(message);
						}
						if (!indexesByHeaders.containsKey(SEQ)) {
							String message = "SEQ column for peptide sequences is missing in input file '"
									+ file.getAbsolutePath() + "'";
							firePropertyChange(PARSER_ERROR, null, message);
							throw new IllegalArgumentException(message);
						}
					} else {
						int accIndex = indexesByHeaders.get(ACC);
						String preliminarProteinAcc = split[accIndex].trim();
						if ("".equals(preliminarProteinAcc)) {
							preliminarProteinAcc = previousProteinACC;
						}
						if (preliminarProteinAcc.startsWith("\"") && preliminarProteinAcc.endsWith("\"")) {
							preliminarProteinAcc = preliminarProteinAcc.substring(1, preliminarProteinAcc.length() - 1);
						}
						previousProteinACC = preliminarProteinAcc;

						// PEPTIDE SEQUENCE
						int seqIndex = indexesByHeaders.get(SEQ);
						String rawSeq = split[seqIndex].trim();
						String seq = FastaParser.cleanSequence(rawSeq);
						// seq = parseSequence(seq);

						// PSMID
						String psmID = String.valueOf(peptides.size() + 1);
						if (indexesByHeaders.containsKey(PSMID)) {
							psmID = split[indexesByHeaders.get(PSMID)];
						}
						// create or get the peptide (PSM)
						IdentifiedPeptideImplFromTSV peptide = null;
						if (!peptides.containsKey(psmID)) {
							peptide = new IdentifiedPeptideImplFromTSV(seq);
							peptides.put(psmID, peptide);
							if (FastaParser.somethingExtrangeInSequence(rawSeq)) {
								TIntDoubleHashMap pTMsByPosition = FastaParser.getPTMsFromSequence(rawSeq);
								for (int position : pTMsByPosition.keys()) {
									String aa = String.valueOf(peptide.getSequence().charAt(position - 1));
									double deltaMass = pTMsByPosition.get(position);
									PeptideModificationBuilder ptmBuilder = MiapeMSIDocumentFactory
											.createPeptideModificationBuilder(
													getModificationNameFromResidueAndMass(aa, deltaMass))
											.monoDelta(deltaMass).position(position).residues(aa);
									peptide.addModification(ptmBuilder.build());
								}
							}

						} else {
							peptide = (IdentifiedPeptideImplFromTSV) peptides.get(psmID);
						}

						// CHARGE
						Integer charge = null;
						if (indexesByHeaders.containsKey(CHARGE)) {
							try {
								charge = Integer.valueOf(split[indexesByHeaders.get(CHARGE)]);
							} catch (NumberFormatException e) {
								log.warn("Error parsing charge state from column " + (indexesByHeaders.get(CHARGE) + 1)
										+ " in file '" + file.getAbsolutePath() + "'");
								log.warn(e.getMessage());
							}
						}
						peptide.setCharge(String.valueOf(charge));

						// precursor MZ
						Double mz = null;
						if (indexesByHeaders.containsKey(MZ)) {
							try {
								mz = Double.valueOf(split[indexesByHeaders.get(MZ)]);
							} catch (NumberFormatException e) {
								log.warn("Error parsing precursor M/Z from column " + (indexesByHeaders.get(MZ) + 1)
										+ " in file '" + file.getAbsolutePath() + "'");
								log.warn(e.getMessage());
							}
						}
						peptide.setPrecursorMZ(mz);

						// RT
						Double rt = null;
						if (indexesByHeaders.containsKey(RT)) {
							try {
								rt = Double.valueOf(split[indexesByHeaders.get(RT)]);
							} catch (NumberFormatException e) {
								log.warn("Error parsing retention time rom column " + (indexesByHeaders.get(RT) + 1)
										+ " in file '" + file.getAbsolutePath() + "'");
								log.warn(e.getMessage());
							}
						}
						peptide.setRetentionTime(rt);

						// PEPTIDE SCORES
						for (String scoreName : indexesByScoreNames.keySet()) {
							String scoreString = split[indexesByScoreNames.get(scoreName)].trim();
							Double score = null;
							try {
								score = Double.valueOf(scoreString);
								PeptideScore peptideScore = MiapeMSIDocumentFactory
										.createPeptideScoreBuilder(scoreName, score.toString()).build();
								peptide.addScore(peptideScore);
							} catch (NumberFormatException e) {
								log.warn("Error parsing score value for column "
										+ (indexesByScoreNames.get(scoreName) + 1) + " in file '"
										+ file.getAbsolutePath() + "'");
								log.warn(e.getMessage());
							}
						}

						// if more than one accession is present, get the list
						List<String> accessions = splitAccessions(preliminarProteinAcc);
						for (String proteinAcc : accessions) {

							if (proteins.containsKey(proteinAcc)) {
								final IdentifiedProteinImplFromTSV protein = (IdentifiedProteinImplFromTSV) proteins
										.get(proteinAcc);
								if (peptide != null) {
									protein.addPeptide(peptide);
									peptide.addProtein(protein);
								}
							} else {
								IdentifiedProteinImplFromTSV protein = new IdentifiedProteinImplFromTSV(proteinAcc,
										null);
								if (peptide != null) {
									protein.addPeptide(peptide);
									peptide.addProtein(protein);
								}
								proteins.put(proteinAcc, protein);
							}
						}
					}
				}

			}

			dis.close();
			if (proteins.isEmpty() && peptides.isEmpty()) {
				firePropertyChange(PARSER_ERROR, null,
						"No proteins or peptides have been captured. Check the separator and try again");
				return null;
			}

			checkRelationBetweenPeptidesAndProteins(peptides, proteins);

			log.info("End parsing. Now building MIAPE MSI.");
			log.info(
					peptides.size() + " PSMs and " + proteins.size() + " proteins from file " + file.getAbsolutePath());
			final MiapeMSIDocumentBuilder builder = MiapeMSIDocumentFactory.createMiapeDocumentMSIBuilder(null,
					idSetName, null);
			List<IdentifiedPeptide> peptideList = new ArrayList<IdentifiedPeptide>();
			peptideList.addAll(peptides.values());
			builder.identifiedPeptides(peptideList);
			Set<IdentifiedProteinSet> proteinSets = new THashSet<IdentifiedProteinSet>();
			IdentifiedProteinSet proteinSet = MiapeMSIDocumentFactory.createIdentifiedProteinSetBuilder("Protein set")
					.identifiedProteins(proteins).build();
			proteinSets.add(proteinSet);
			builder.identifiedProteinSets(proteinSets);
			log.info("MIAPE MSI builder created.");
			firePropertyChange(PARSER_FINISHED, null, builder);
		} catch (Exception e) {
			e.printStackTrace();
			firePropertyChange(PARSER_ERROR, null, e.getMessage());
		}
		return null;
	}

	private SlimModCollection getModificationMapping() {
		if (preferredModifications == null) {
			URL url;
			try {
				url = resource.getURL();
				preferredModifications = PrideModController.parseSlimModCollection(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return preferredModifications;
	}

	private String getModificationNameFromResidueAndMass(String aa, double deltaMass) {
		try {
			// try first with the PRIDE mapping
			SlimModCollection modificationMapping = getModificationMapping();

			SlimModCollection slimMods = modificationMapping.getbyDelta(deltaMass, 0.03);
			if (slimMods != null && !slimMods.isEmpty()) {
				return slimMods.get(0).getPsiModDesc();
			}
			// TODO add more modifications!
			// read from a file?
			ControlVocabularyManager cvManager = OntologyLoaderTask.getCvManager();
			if (aa.equals("C") && compareWithError(deltaMass, 57.022)) {

				final ControlVocabularyTerm cvTerm = cvManager.getCVTermByAccession(
						new Accession(PeptideModificationName.UNIMOD4), PeptideModificationName.getInstance(cvManager));
				if (cvTerm != null)
					return cvTerm.getPreferredName();
				else
					return "Carbamidomethyl";
			}
			if (aa.equals("E") && compareWithError(deltaMass, -18.0106)) {
				final ControlVocabularyTerm cvTerm = cvManager.getCVTermByAccession(
						new Accession(PeptideModificationName.UNIMOD27),
						PeptideModificationName.getInstance(cvManager));
				if (cvTerm != null)
					return cvTerm.getPreferredName();
				else
					return "Glu->pyro-Glu";
			}
			final ControlVocabularyTerm pepModifDetailsTerm = PeptideModificationName.getPepModifDetailsTerm(cvManager);
			if (pepModifDetailsTerm != null)
				return pepModifDetailsTerm.getPreferredName();
		} catch (Exception e) {

		}
		return "peptide modification details";
	}

	private boolean compareWithError(double num1, double num2) {
		double tolerance = 0.001;
		if (num1 > num2)
			if (num1 - num2 < tolerance)
				return true;
		if (num2 > num1)
			if (num2 - num1 < tolerance)
				return true;
		if (num1 == num2)
			return true;
		return false;
	}

	private void parseHeader(String[] splittedLine, TObjectIntHashMap<String> indexesByHeaders,
			TObjectIntHashMap<String> indexesByScoreNames) {
		int index = 0;
		for (String element : splittedLine) {
			element = element.trim();
			if (element.equalsIgnoreCase(ACC)) {
				indexesByHeaders.put(ACC, index);
			} else if (element.equalsIgnoreCase(PSMID)) {
				indexesByHeaders.put(PSMID, index);
			} else if (element.equalsIgnoreCase(SEQ)) {
				indexesByHeaders.put(SEQ, index);
			} else if (element.equalsIgnoreCase(MZ)) {
				indexesByHeaders.put(MZ, index);
			} else if (element.equalsIgnoreCase(CHARGE)) {
				indexesByHeaders.put(CHARGE, index);
			} else {
				indexesByScoreNames.put(element, index);
			}
			index++;
		}

	}

	private void addRelationShips(List<IdentifiedPeptide> peptides, Map<String, IdentifiedProtein> proteinMap)
			throws IOException {

		// ask for a Fasta file
		final int userSelection = JOptionPane.showConfirmDialog(null,
				"<html>The proteins and peptides are not related.<br>Do you want to select a FASTA file to try to get the relationships between them?</html>",
				"No relationships between peptides and proteins", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (userSelection == JOptionPane.YES_OPTION) {
			// convert peptides to Maps
			Map<String, IdentifiedPeptide> peptideMap = new THashMap<String, IdentifiedPeptide>();
			for (IdentifiedPeptide identifiedPeptide : peptides) {
				peptideMap.put(identifiedPeptide.getSequence(), identifiedPeptide);
			}
			int userSelection2 = Integer.MIN_VALUE;
			boolean loadNewFastaFile = true;
			Enzyme trypsin = new Enzyme("Trypsin", "RK", "", "Cterm", 2);
			if (IdentificationSetFromFileParserTask.fastaLoader != null) {
				userSelection2 = JOptionPane.showConfirmDialog(null,
						"<html>There is already a fasta loaded.<br>Do you want to continue with this data (yes) or do you wnat to load another fasta file (no)?</html>",
						"Fasta file already loaded", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (userSelection2 == JOptionPane.YES_OPTION)
					loadNewFastaFile = false;
			}
			if (loadNewFastaFile) {
				if (IdentificationSetFromFileParserTask.fileChooser == null)
					IdentificationSetFromFileParserTask.fileChooser = new JFileChooser(MainFrame.currentFolder);
				fileChooser.showDialog(null, "Select FASTA file");
				final File fastaFile = fileChooser.getSelectedFile();
				MainFrame.currentFolder = fastaFile.getParentFile();

				if (fastaFile.exists()) {
					log.info("Loading fasta...");
					IdentificationSetFromFileParserTask.fastaLoader = new FASTADBLoader();
					fastaLoader.load(fastaFile.getAbsolutePath());
				}
			}
			Map<String, List<String>> peptideToProteinsMap = new THashMap<String, List<String>>();
			final long countNumberOfEntries = fastaLoader.countNumberOfEntries();
			log.info("Readed " + countNumberOfEntries + " entries in the fasta file");
			for (int i = 0; i < countNumberOfEntries; i++) {
				final Protein nextProtein = fastaLoader.nextProtein();
				if (nextProtein == null)
					break;
				String proteinACC = nextProtein.getHeader().getAccession();

				final Protein[] cleave = trypsin.cleave(nextProtein);
				for (Protein protein : cleave) {
					final String seq = protein.getSequence().getSequence();
					// skip sequence if not present in the input peptides
					if (!peptideMap.containsKey(seq))
						continue;
					if (peptideToProteinsMap.containsKey(seq)) {
						final List<String> proteinAccs = peptideToProteinsMap.get(seq);
						if (!proteinAccs.contains(proteinACC))
							proteinAccs.add(proteinACC);
					} else {
						List<String> proteinAccs = new ArrayList<String>();
						proteinAccs.add(proteinACC);
						peptideToProteinsMap.put(seq, proteinAccs);
					}
				}
			}
			log.info(peptideToProteinsMap.size() + " peptides mapped");
			for (String peptideSeq : peptideToProteinsMap.keySet()) {
				if (peptideMap.containsKey(peptideSeq)) {
					final IdentifiedPeptideImplFromTSV identifiedPeptide = (IdentifiedPeptideImplFromTSV) peptideMap
							.get(peptideSeq);
					final List<String> proteinAccs = peptideToProteinsMap.get(peptideSeq);
					for (String proteinAcc : proteinAccs) {
						if (proteinMap.containsKey(proteinAcc)) {
							final IdentifiedProteinImplFromTSV identifiedProtein = (IdentifiedProteinImplFromTSV) proteinMap
									.get(proteinAcc);
							identifiedPeptide.addProtein(identifiedProtein);
							identifiedProtein.addPeptide(identifiedPeptide);
							log.info("Peptide " + identifiedPeptide.getSequence() + " mapped to protein "
									+ identifiedProtein.getAccession());
						} else {
							// Create the protein if the peptide has been mapped
							// with it
							log.info("Adding new protein: " + proteinAcc + " that supports peptide " + peptideSeq);
							IdentifiedProteinImplFromTSV identifiedProtein = new IdentifiedProteinImplFromTSV(
									proteinAcc);
							identifiedPeptide.addProtein(identifiedProtein);
							identifiedProtein.addPeptide(identifiedPeptide);
							proteinMap.put(proteinAcc, identifiedProtein);
						}
					}
				}
			}
		}

	}

	/**
	 * Throws exception if some protein has no peptides or if some peptide has
	 * no proteins
	 * 
	 * @param peptides
	 * @param proteins
	 * @return
	 */
	private void checkRelationBetweenPeptidesAndProteins(Map<String, IdentifiedPeptide> peptides,
			Map<String, IdentifiedProtein> proteins) {
		for (String psmID : peptides.keySet()) {
			IdentifiedPeptide identifiedPeptide = peptides.get(psmID);
			if (identifiedPeptide.getIdentifiedProteins() == null
					|| identifiedPeptide.getIdentifiedProteins().isEmpty()) {
				String message = "Peptide " + identifiedPeptide.getSequence() + " with ID " + psmID
						+ " has no linked to any protein";
				log.warn(message);
				firePropertyChange(PARSER_ERROR, null, message);
				throw new IllegalArgumentException(message);

			}

		}
		for (IdentifiedProtein identifiedProtein : proteins.values()) {
			if (identifiedProtein.getIdentifiedPeptides() == null
					|| identifiedProtein.getIdentifiedPeptides().isEmpty()) {
				String message = "Protein " + identifiedProtein.getAccession() + " has no linked to any peptide";
				log.warn(message);
				firePropertyChange(PARSER_ERROR, null, message);
				throw new IllegalArgumentException(message);
			}
		}
	}

	private String getScoreName(String firstLine) {
		final ControlVocabularyManager cvManager = OntologyLoaderTask.getCvManager();
		String scoreName = firstLine;
		if (firstLine.toLowerCase().contains("mascot")) {
			// Mascot:score
			scoreName = cvManager.getControlVocabularyName(new Accession("MS:1001171"), Score.getInstance(cvManager));
		} else if (firstLine.toLowerCase().contains("phenyx")) {
			// Phenyx:Pepzscore
			scoreName = cvManager.getControlVocabularyName(new Accession("MS:1001395"), Score.getInstance(cvManager));
		} else if (firstLine.toLowerCase().contains("sequest")) {
			// SEQUEST:xcorr
			scoreName = cvManager.getControlVocabularyName(new Accession("MS:1001155"), Score.getInstance(cvManager));
		} else if (firstLine.toLowerCase().contains("plgs") || firstLine.toLowerCase().contains("lynx")) {
			// ProteinLynx:Ladder Score
			scoreName = cvManager.getControlVocabularyName(new Accession("MS:1001571"), Score.getInstance(cvManager));
		} else if (firstLine.toLowerCase().contains("tandem")) {
			// X!Tandem:hyperscore
			scoreName = cvManager.getControlVocabularyName(new Accession("MS:1001331"), Score.getInstance(cvManager));
		} else if (firstLine.toLowerCase().contains("pilot")) {
			// Paragon score
			scoreName = cvManager.getControlVocabularyName(new Accession("MS:1001166"), Score.getInstance(cvManager));
		} else if (firstLine.toLowerCase().contains("prophet") || firstLine.toLowerCase().contains("probability")) {
			// Peptide Prophet probability
			scoreName = cvManager.getControlVocabularyName(new Accession("MS:1001870"), Score.getInstance(cvManager));
		} else if (firstLine.toLowerCase().contains("mill")) {
			// SpectrumMill:score
			scoreName = cvManager.getControlVocabularyName(new Accession("MS:1001572"), Score.getInstance(cvManager));
		} else if (firstLine.toLowerCase().contains("omssa") && firstLine.toLowerCase().contains("evalue")) {
			// OMSSA EVALUE
			scoreName = cvManager.getControlVocabularyName(new Accession("MS:1001328"), Score.getInstance(cvManager));
		} else if (firstLine.toLowerCase().contains("omssa") && firstLine.toLowerCase().contains("pvalue")) {
			// OMSSA pvalue
			scoreName = cvManager.getControlVocabularyName(new Accession("MS:1001329"), Score.getInstance(cvManager));
		} else if (firstLine.toLowerCase().contains("score")) {
			// confidence score
			scoreName = cvManager.getControlVocabularyName(new Accession("MS:1001193"), Score.getInstance(cvManager));
		} else if (firstLine.toLowerCase().contains("paragon:confidence")) {
			// paragon confidence
			scoreName = cvManager.getControlVocabularyName(new Accession("MS:1001167"), Score.getInstance(cvManager));
		} else {
			// confidence score
			scoreName = cvManager.getControlVocabularyName(new Accession("MS:1001193"), Score.getInstance(cvManager));
		}
		return scoreName;
	}

	/**
	 * Splits the protein accession in a list if they are separated by other
	 * separator than the separator of the class.<br>
	 * It also converts a OKAK_HUMAN uniprot ID to a uniprot ACCESSION like
	 * P123122
	 *
	 * @param proteinAcc
	 * @return
	 */
	private List<String> splitAccessions(String proteinAcc) {
		List<String> ret = new ArrayList<String>();
		if (proteinAcc == null)
			return ret;
		for (String separator2 : SEPARATORS) {
			if (!separator2.equals(separator)) {
				if (proteinAcc.contains(separator2)) {

					String[] split = proteinAcc.split(separator2);
					for (String string : split) {
						ret.add(string);
					}
					ret = convertUniprotIDToAcc(ret);
					return ret;
				}
			}
		}
		ret.add(proteinAcc);
		ret = convertUniprotIDToAcc(ret);
		return ret;
	}

	private List<String> convertUniprotIDToAcc(List<String> accs) {

		try {

			List<String> ret = new ArrayList<String>();
			for (String string : accs) {
				final UniprotId2AccMapping instance = UniprotId2AccMapping.getInstance();
				if (instance != null) {
					final String accFromID = instance.getAccFromID(string);
					if (accFromID != null) {
						log.info("Uniprot ID: " + string + " changed by ACC: " + accFromID);
						ret.add(accFromID);
					} else {
						ret.add(string);
					}
				}
			}
			return ret;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return accs;
	}

	/**
	 * This function allow to get the peptide sequence as <br>
	 * <ul>
	 * <li>K.VDLSFSPSQSLPASHAHLR.V -> VDLSFSPSQSLPASHAHLR</li>
	 * <li>R.LLLQQVSLPELPGEYSMK.V + Oxidation (M) -> LLLQQVSLPELPGEYSMK</li>
	 * <li>(-)TVAAPSVFIFPPSDEQLK(S) -> TVAAPSVFIFPPSDEQLK</li>
	 * <li>K.EKS[167.00]KESAIASTEVK.L -> EKSKESAIASTEVK</li>
	 * </ul>
	 * getting just the sequence without modifications and between the pre and
	 * post AA if available
	 *
	 * @param seq
	 * @return
	 */
	public static String parseSequence(String seq) {
		if (seq == null)
			return null;
		try {
			while (somethingExtrangeInSequence(seq)) {
				if (seq.matches(".*\\[.*\\].*")) {
					int indexOf = seq.indexOf("[");
					String left = seq.substring(0, indexOf);
					indexOf = seq.indexOf("]");
					String rigth = seq.substring(indexOf + 1);
					seq = left + rigth;

				}
				if (seq.matches(".*\\.(.*)\\..*")) {
					int indexOf = seq.indexOf(".");
					seq = seq.substring(indexOf + 1);
					indexOf = seq.indexOf(".");
					seq = seq.substring(0, indexOf);
				}
				if (seq.matches(".*\\).*\\(.*")) {
					int indexOf = seq.indexOf(")");
					seq = seq.substring(indexOf + 1);
					indexOf = seq.indexOf("(");
					seq = seq.substring(0, indexOf);
				}
			}
			return seq.toUpperCase();
		} catch (Exception e) {
			return seq;
		}
	}

	private static boolean somethingExtrangeInSequence(String seq) {
		return seq.matches(".*\\[.*\\].*") || seq.matches(".*\\.(.*)\\..*") || seq.matches(".*\\).*\\(.*");

	}

	public static void main(String[] args) {
		String parseSequence = IdentificationSetFromFileParserTask.parseSequence("K.EKS[167.00]KESAIASTEVK.L");
		System.out.println(parseSequence);
		parseSequence = IdentificationSetFromFileParserTask.parseSequence("(K)CDEWSVNSVGKIECVSAETTEDCIAK(I)");
		System.out.println(parseSequence);
		parseSequence = IdentificationSetFromFileParserTask.parseSequence("K.VDLSFSPSQSLPASHAHLR.V");
		System.out.println(parseSequence);
		parseSequence = IdentificationSetFromFileParserTask.parseSequence("R.LLLQQVSLPELPGEYSMK.V + Oxidation (M)");
		System.out.println(parseSequence);
	}
}
