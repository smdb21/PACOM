package org.proteored.miapeExtractor.analysis.gui.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingWorker;

import org.proteored.miapeExtractor.analysis.util.IdentifiedPeptideImplFromDTASelect;
import org.proteored.miapeExtractor.analysis.util.IdentifiedProteinImplFromDTASelectProtein;
import org.proteored.miapeapi.factories.msi.MiapeMSIDocumentBuilder;
import org.proteored.miapeapi.factories.msi.MiapeMSIDocumentFactory;
import org.proteored.miapeapi.interfaces.msi.IdentifiedPeptide;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProtein;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProteinSet;

import edu.scripps.yates.dtaselectparser.DTASelectParser;
import edu.scripps.yates.dtaselectparser.util.DTASelectProtein;

public class IdentificationSetFromDTASelectFileTask extends
		SwingWorker<Void, String> {
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger("log4j.logger.org.proteored");
	public static final String DTASELECT_PARSER_STARTS = "DTA_SELECT_PARSER_STARTS";
	public static final String DTASELECT_PARSER_ERROR = "DTA_SELECT_PARSER_ERROR";

	public static final String DTASELECT_PARSER_FINISHED = "DTA_SELECT_PARSER_FINISHED";
	private final File file;
	private final String idSetName;
	private MiapeMSIDocumentBuilder builder;

	public IdentificationSetFromDTASelectFileTask(File selectedFile,
			String idSetName) {
		file = selectedFile;
		this.idSetName = idSetName;
	}

	@Override
	protected Void doInBackground() throws Exception {
		try {
			log.info(DTASELECT_PARSER_STARTS);
			firePropertyChange(DTASELECT_PARSER_STARTS, null, null);
			// clear map
			IdentifiedPeptideImplFromDTASelect.map.clear();

			List<File> files = new ArrayList<File>();
			files.add(file);
			DTASelectParser parser = new DTASelectParser(files);
			final HashMap<String, DTASelectProtein> dtaSelectProteins = parser
					.getDTASelectProteins();
			HashMap<String, IdentifiedProtein> proteins = new HashMap<String, IdentifiedProtein>();
			List<IdentifiedPeptide> peptides = new ArrayList<IdentifiedPeptide>();
			Set<String> psmIDs = new HashSet<String>();
			for (String acc : dtaSelectProteins.keySet()) {
				final DTASelectProtein dtaSelectProtein = dtaSelectProteins
						.get(acc);
				IdentifiedProtein protein = new IdentifiedProteinImplFromDTASelectProtein(
						dtaSelectProtein);
				final String accession = protein.getAccession();
				if (proteins.containsKey(accession))
					protein = proteins.get(accession);
				proteins.put(accession, protein);
				final List<IdentifiedPeptide> identifiedPeptides = protein
						.getIdentifiedPeptides();
				for (IdentifiedPeptide identifiedPeptide : identifiedPeptides) {
					if (!psmIDs.contains(identifiedPeptide.getSpectrumRef())) {
						psmIDs.add(identifiedPeptide.getSpectrumRef());
						peptides.add(identifiedPeptide);
					}
				}
			}

			log.info("End parsing. Now building MIAPE MSI with "
					+ proteins.size() + " proteins and " + peptides.size()
					+ " peptides");
			builder = MiapeMSIDocumentFactory.createMiapeDocumentMSIBuilder(
					null, idSetName, null);
			builder.identifiedPeptides(peptides);
			Set<IdentifiedProteinSet> proteinSets = new HashSet<IdentifiedProteinSet>();
			IdentifiedProteinSet proteinSet = MiapeMSIDocumentFactory
					.createIdentifiedProteinSetBuilder("Protein set")
					.identifiedProteins(proteins).build();
			proteinSets.add(proteinSet);
			builder.identifiedProteinSets(proteinSets);
			log.info("MIAPE MSI builder created.");
			firePropertyChange(DTASELECT_PARSER_FINISHED, null, builder);
		} catch (Exception e) {
			e.printStackTrace();
			firePropertyChange(DTASELECT_PARSER_ERROR, null, e.getMessage());
		}
		return null;
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
		return seq.matches(".*\\[.*\\].*") || seq.matches(".*\\.(.*)\\..*")
				|| seq.matches(".*\\).*\\(.*");

	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		if (isCancelled()) {
			log.info("DTA SELECT PARSER CANCELED");
		} else {
			log.info("DTA SELECT PARSER DONE");

		}
		super.done();
	}

}
