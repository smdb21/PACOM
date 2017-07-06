package org.proteored.pacom.analysis.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedPeptide;

import gnu.trove.map.hash.THashMap;

/**
 * Class that manage the clustering of identification objects
 * 
 * @author Salva
 * 
 */
public class ClustererUtil {
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");

	/**
	 * Cluster peptides by sequence. If distiguishModificatedPeptides is true,
	 * the same sequence but different modifications will be considered as
	 * different sequences.
	 * 
	 * @param peptides
	 * @param distinguishModificatedPeptides
	 * @return
	 */
	public static Map<String, List<ExtendedIdentifiedPeptide>> clusterPeptides(List<ExtendedIdentifiedPeptide> peptides,
			boolean distinguishModificatedPeptides) {
		Map<String, List<ExtendedIdentifiedPeptide>> clusteredPeptides = new THashMap<String, List<ExtendedIdentifiedPeptide>>();
		if (peptides != null) {
			log.info("Clustering. Distinguish modifications=" + distinguishModificatedPeptides);
			for (ExtendedIdentifiedPeptide extendedIdentifiedPeptide : peptides) {
				String key = extendedIdentifiedPeptide.getKey(distinguishModificatedPeptides);
				if (!clusteredPeptides.containsKey(key)) {
					List<ExtendedIdentifiedPeptide> extpeps = new ArrayList<ExtendedIdentifiedPeptide>();
					extpeps.add(extendedIdentifiedPeptide);
					clusteredPeptides.put(key, extpeps);
				} else {
					List<ExtendedIdentifiedPeptide> extpeps = clusteredPeptides.get(key);
					extpeps.add(extendedIdentifiedPeptide);
					clusteredPeptides.remove(key);
					clusteredPeptides.put(key, extpeps);
				}
			}
		}
		return clusteredPeptides;
	}

}
