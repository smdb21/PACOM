package org.proteored.pacom.analysis.util;

import java.util.ArrayList;
import java.util.List;

import org.proteored.pacom.utils.UpdateChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.demo.picr.business.PICRClient;
import uk.ac.ebi.demo.picr.soap.CrossReference;
import uk.ac.ebi.demo.picr.soap.UPEntry;

public class PICRInterfaz {
	private final static Object[] ens_databases = { "SWISSPROT", "SWISSPROT_VARSPLIC", "TREMBL",
			"TREMBL_VARSPLIC" };
	public static final Logger log = LoggerFactory.getLogger(UpdateChecker.class);

	public static List<CrossReference> getENSGeneCrossReferences(String id) {
		List<CrossReference> ret = new ArrayList<CrossReference>();
		PICRClient client = new PICRClient();
		final List<UPEntry> performAccessionMapping = client.performAccessionMapping(id,
				ens_databases);
		// log.info(performAccessionMapping.size() + " mappings from " +
		// proteinACC);
		for (UPEntry upEntry : performAccessionMapping) {

			final List<uk.ac.ebi.demo.picr.soap.CrossReference> identicalCrossReferences = upEntry
					.getIdenticalCrossReferences();
			// System.out.println("\nSEQ:");
			// System.out.println(upEntry.getSequence());
			if (identicalCrossReferences != null) {
				for (uk.ac.ebi.demo.picr.soap.CrossReference crossReference : identicalCrossReferences) {
					// log.info(crossReference.getAccession() + " version:"
					// + crossReference.getAccessionVersion() + " from "
					// + crossReference.getDatabaseName());
					ret.add(crossReference);
				}
			}

		}

		return ret;
	}
}
