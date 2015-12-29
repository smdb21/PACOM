package org.proteored.miapeExtractor.chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.proteored.miapeExtractor.analysis.genes.ENSGInfo;
import org.proteored.miapeExtractor.analysis.genes.GeneDistributionReader;
import org.proteored.miapeExtractor.analysis.genes.Researcher;
import org.proteored.miapeapi.experiment.model.IdentifierParser;

public class Ch16Test {

	@Test
	public void testChr16GenReading() {
		final Collection<List<ENSGInfo>> keySet = GeneDistributionReader
				.getInstance().getProteinGeneMapping("16").values();
		int i = 1;
		List<String> lista = new ArrayList<String>();
		for (List<ENSGInfo> genes : keySet) {
			for (ENSGInfo ensgInfo : genes) {
				final Researcher researcher = ensgInfo.getResearcher();
				final String status = ensgInfo.getKnown();
				if (researcher != null && researcher.getName() != null
						&& status.equals("KNOWN")
						&& !lista.contains(ensgInfo.getEnsG_ID())) {
					lista.add(ensgInfo.getEnsG_ID());
					System.out.println(ensgInfo.getEnsG_ID() + "\t"
							+ researcher.getName());
				}
			}
		}
	}

	@Test
	public void testWholeChromosomesProteinReading() {
		System.out.println("Proteins from Human whole genome");
		final String[] strings = GeneDistributionReader.chromosomeNames;
		int i = 1;
		for (String chrName : strings) {
			final Set<String> uniprotACCs = GeneDistributionReader
					.getInstance().getProteinGeneMapping(chrName).keySet();

			for (String uniprotACC : uniprotACCs) {
				System.out.println(chrName + "\t" + uniprotACC);
			}
		}

	}

	@Test
	public void testWholeChromosomesProteinReadingNoIsoforms() {
		System.out.println("Proteins from Human whole genome");
		final String[] strings = GeneDistributionReader.chromosomeNames;

		Set<String> accs = new HashSet<String>();

		int i = 1;
		for (String chrName : strings) {
			final Set<String> uniprotACCs = GeneDistributionReader
					.getInstance().getProteinGeneMapping(chrName).keySet();
			IdentifierParser.setRemove_acc_version(false);
			for (String uniprotACC : uniprotACCs) {
				String acc = IdentifierParser.parseACC(uniprotACC);
				if (!accs.contains(acc)) {
					accs.add(acc);
					System.out.println(i++ + "\t" + chrName + "\t" + acc);
				}
			}
		}

	}

	@Test
	public void testWholeChromosomesProteinCounterNoIsoforms() {
		System.out.println("Proteins from Human whole genome");
		final String[] strings = GeneDistributionReader.chromosomeNames;

		int i = 1;
		for (String chrName : strings) {
			Set<String> accs = new HashSet<String>();
			final Set<String> uniprotACCs = GeneDistributionReader
					.getInstance().getProteinGeneMapping(chrName).keySet();
			IdentifierParser.setRemove_acc_version(true);
			for (String uniprotACC : uniprotACCs) {
				String acc = IdentifierParser.parseACC(uniprotACC);

				if (!accs.contains(acc)) {
					accs.add(acc);
					// System.out.println(i++ + "\t" + chrName + "\t" + acc);
				}
			}
			System.out.println(chrName + "=" + accs.size());
		}

	}
}
