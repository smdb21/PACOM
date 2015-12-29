package org.proteored.miapeExtractor.analysis.util;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.proteored.miapeapi.interfaces.msi.PeptideModification;

import uk.ac.ebi.pridemod.PrideModController;
import uk.ac.ebi.pridemod.slimmod.model.SlimModCollection;
import uk.ac.ebi.pridemod.slimmod.model.SlimModification;
import edu.scripps.yates.dtaselectparser.util.DTASelectModification;

public class PeptideModificationImplFromDTASelect implements
		PeptideModification {
	private final DTASelectModification dtaSelectModification;
	private final SlimModification slimModification;
	private static SlimModCollection preferredModifications;
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(PeptideModificationImplFromDTASelect.class);
	private static Set<String> errorMessages = new HashSet<String>();

	public PeptideModificationImplFromDTASelect(
			DTASelectModification dtaSelectModification) {
		this.dtaSelectModification = dtaSelectModification;
		if (preferredModifications == null) {
			URL url = getClass().getClassLoader().getResource(
					"modification_mappings_dtaSelect.xml");
			if (url != null) {
				preferredModifications = PrideModController
						.parseSlimModCollection(url);
			} else {
				throw new IllegalArgumentException(
						"Could not find preferred modification file");
			}
		}
		double delta = dtaSelectModification.getModificationShift();
		double precision = 0.01;
		// map by delta
		SlimModCollection filteredMods = preferredModifications.getbyDelta(
				delta, precision);
		if (!filteredMods.isEmpty()) {
			slimModification = filteredMods.get(0);
		} else {
			final String message = "Peptide modification with delta mass="
					+ delta
					+ " is not recognized in the system. Please, contact system administrator in order to add it as a supported PTM in the system.";
			if (!errorMessages.contains(message)) {
				log.warn(message);
				errorMessages.add(message);
			}
			slimModification = null;
		}
	}

	@Override
	public String getName() {
		if (slimModification != null)
			return slimModification.getShortNamePsiMod();
		return "unknown";
	}

	@Override
	public int getPosition() {
		return dtaSelectModification.getModPosition();
	}

	@Override
	public String getResidues() {
		return String.valueOf(dtaSelectModification.getAa());
	}

	@Override
	public Double getMonoDelta() {
		return dtaSelectModification.getModificationShift();
	}

	@Override
	public Double getAvgDelta() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getReplacementResidue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getNeutralLoss() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getModificationEvidence() {
		// TODO Auto-generated method stub
		return null;
	}

}
