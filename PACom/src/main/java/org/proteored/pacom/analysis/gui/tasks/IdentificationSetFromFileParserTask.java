package org.proteored.pacom.analysis.gui.tasks;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.proteored.miapeapi.cv.Accession;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.cv.msi.Score;
import org.proteored.miapeapi.factories.msi.MiapeMSIDocumentBuilder;
import org.proteored.miapeapi.factories.msi.MiapeMSIDocumentFactory;
import org.proteored.miapeapi.interfaces.msi.IdentifiedPeptide;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProtein;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProteinSet;
import org.proteored.miapeapi.interfaces.msi.PeptideScore;
import org.proteored.miapeapi.util.UniprotId2AccMapping;
import org.proteored.pacom.analysis.exporters.tasks.IdentifiedPeptideImpl;
import org.proteored.pacom.analysis.exporters.tasks.IdentifiedProteinImpl;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;

import com.compomics.dbtoolkit.io.implementations.FASTADBLoader;
import com.compomics.util.protein.Enzyme;
import com.compomics.util.protein.Protein;

public class IdentificationSetFromFileParserTask extends SwingWorker<Void, String> {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	public static final String PARSER_STARTS = "PARSER_STARTS";
	public static final String PARSER_ERROR = "PARSER_ERROR";

	public static final String PARSER_FINISHED = "PARSER_FINISHED";
	private static final String[] SEPARATORS = { ",", ";", "\t" };
	private final File file;
	private final String separator;
	private final String idSetName;
	private static JFileChooser fileChooser;
	private static FASTADBLoader fastaLoader;

	public IdentificationSetFromFileParserTask(File selectedFile, String idSetName, String separator) {
		file = selectedFile;
		this.separator = separator;
		this.idSetName = idSetName;
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			// decimal separator
			DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
			DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
			char sep = symbols.getDecimalSeparator();

			firePropertyChange(PARSER_STARTS, null, null);
			DataInputStream dis = new DataInputStream(new FileInputStream(file));
			String line = "";
			log.info("Parsing file " + file.getAbsolutePath());
			HashMap<String, IdentifiedProtein> proteins = new HashMap<String, IdentifiedProtein>();
			List<IdentifiedPeptide> peptides = new ArrayList<IdentifiedPeptide>();
			String previousProteinACC = null;
			String scoreName = null;
			int numLine = 0;
			while ((line = dis.readLine()) != null) {
				numLine++;
				if (line.trim().startsWith("#")) {
					scoreName = getScoreName(line);
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
					String preliminarProteinAcc = split[0].trim();
					if ("".equals(preliminarProteinAcc))
						preliminarProteinAcc = previousProteinACC;
					if (preliminarProteinAcc.startsWith("\"") && preliminarProteinAcc.endsWith("\"")) {
						preliminarProteinAcc = preliminarProteinAcc.substring(1, preliminarProteinAcc.length() - 1);
					}
					previousProteinACC = preliminarProteinAcc;

					// PEPTIDE SEQUENCE
					String seq = null;
					if (split.length > 1) {
						seq = split[1].trim();
					}
					// Parse peptide sequence
					seq = parseSequence(seq);
					IdentifiedPeptideImpl peptide = null;
					if (seq != null) {
						peptide = new IdentifiedPeptideImpl(seq);
						peptides.add(peptide);
					} else {
						log.warn("Peptide is null in line " + numLine + ": " + line);
					}

					// PEPTIDE SCORE
					if (split.length > 2) {
						try {
							if (scoreName == null) {
								scoreName = getScoreName("");
							}
							String trim = split[2].trim();

							trim = trim.replace(",", ".");
							Double score = Double.valueOf(trim);
							PeptideScore peptideScore = MiapeMSIDocumentFactory
									.createPeptideScoreBuilder(scoreName, score.toString()).build();
							peptide.addScore(peptideScore);
						} catch (Exception e) {
							log.info(e.getMessage());
						}
					}

					// Protein NAME
					String proteinDescription = null;
					if (split.length > 3) {
						try {
							proteinDescription = split[3].trim();
						} catch (Exception e) {
							log.info(e.getMessage());
						}
					}

					// if more than one accession is present, get the list
					List<String> accessions = splitAccessions(preliminarProteinAcc);
					for (String proteinAcc : accessions) {

						if (proteins.containsKey(proteinAcc)) {
							final IdentifiedProteinImpl protein = (IdentifiedProteinImpl) proteins.get(proteinAcc);
							if (peptide != null) {
								protein.addPeptide(peptide);
								peptide.addProtein(protein);
							}
						} else {
							IdentifiedProteinImpl protein = new IdentifiedProteinImpl(proteinAcc, proteinDescription);
							if (peptide != null) {
								protein.addPeptide(peptide);
								peptide.addProtein(protein);
							}
							proteins.put(proteinAcc, protein);
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
			// If there is no relationship between peptides and proteins, look
			// in the fasta file
			if (!relationBetweenPeptidesAndProteins(peptides, proteins)) {
				addRelationShips(peptides, proteins);
			}

			log.info("End parsing. Now building MIAPE MSI.");
			final MiapeMSIDocumentBuilder builder = MiapeMSIDocumentFactory.createMiapeDocumentMSIBuilder(null,
					idSetName, null);
			builder.identifiedPeptides(peptides);
			Set<IdentifiedProteinSet> proteinSets = new HashSet<IdentifiedProteinSet>();
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

	private void addRelationShips(List<IdentifiedPeptide> peptides, HashMap<String, IdentifiedProtein> proteinMap)
			throws IOException {

		// ask for a Fasta file
		final int userSelection = JOptionPane.showConfirmDialog(null,
				"<html>The proteins and peptides are not related.<br>Do you want to select a FASTA file to try to get the relationships between them?</html>",
				"No relationships between peptides and proteins", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (userSelection == JOptionPane.YES_OPTION) {
			// convert peptides to hashmaps
			HashMap<String, IdentifiedPeptide> peptideMap = new HashMap<String, IdentifiedPeptide>();
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
					IdentificationSetFromFileParserTask.fileChooser = new JFileChooser(new File("."));
				fileChooser.showDialog(null, "Select FASTA file");
				final File fastaFile = fileChooser.getSelectedFile();

				if (fastaFile.exists()) {
					log.info("Loading fasta...");
					IdentificationSetFromFileParserTask.fastaLoader = new FASTADBLoader();
					fastaLoader.load(fastaFile.getAbsolutePath());
				}
			}
			HashMap<String, List<String>> peptideToProteinsMap = new HashMap<String, List<String>>();
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
					final IdentifiedPeptideImpl identifiedPeptide = (IdentifiedPeptideImpl) peptideMap.get(peptideSeq);
					final List<String> proteinAccs = peptideToProteinsMap.get(peptideSeq);
					for (String proteinAcc : proteinAccs) {
						if (proteinMap.containsKey(proteinAcc)) {
							final IdentifiedProteinImpl identifiedProtein = (IdentifiedProteinImpl) proteinMap
									.get(proteinAcc);
							identifiedPeptide.addProtein(identifiedProtein);
							identifiedProtein.addPeptide(identifiedPeptide);
							log.info("Peptide " + identifiedPeptide.getSequence() + " mapped to protein "
									+ identifiedProtein.getAccession());
						} else {
							// Create the protein if the peptide has been mapped
							// with it
							log.info("Adding new protein: " + proteinAcc + " that supports peptide " + peptideSeq);
							IdentifiedProteinImpl identifiedProtein = new IdentifiedProteinImpl(proteinAcc);
							identifiedPeptide.addProtein(identifiedProtein);
							identifiedProtein.addPeptide(identifiedPeptide);
							proteinMap.put(proteinAcc, identifiedProtein);
						}
					}
				}
			}
		}

	}

	private boolean relationBetweenPeptidesAndProteins(List<IdentifiedPeptide> peptides,
			HashMap<String, IdentifiedProtein> proteins) {
		for (IdentifiedPeptide identifiedPeptide : peptides) {
			if (identifiedPeptide.getIdentifiedProteins() != null
					&& !identifiedPeptide.getIdentifiedProteins().isEmpty())
				return true;
		}
		for (IdentifiedProtein identifiedProtein : proteins.values()) {
			if (identifiedProtein.getIdentifiedPeptides() != null
					&& !identifiedProtein.getIdentifiedPeptides().isEmpty())
				return true;
		}
		return false;
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
