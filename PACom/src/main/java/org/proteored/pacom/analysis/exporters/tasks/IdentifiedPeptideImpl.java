package org.proteored.pacom.analysis.exporters.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.proteored.miapeapi.interfaces.msi.IdentifiedPeptide;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProtein;
import org.proteored.miapeapi.interfaces.msi.InputData;
import org.proteored.miapeapi.interfaces.msi.PeptideModification;
import org.proteored.miapeapi.interfaces.msi.PeptideScore;
import org.proteored.miapeapi.util.ModificationMapping;

import com.compomics.util.protein.AASequenceImpl;

public class IdentifiedPeptideImpl implements IdentifiedPeptide {
	private final String sequence;
	private final int id;
	private List<IdentifiedProtein> proteins = new ArrayList<IdentifiedProtein>();
	private Set<PeptideScore> scores = new HashSet<PeptideScore>();
	private Set<PeptideModification> modifications = new HashSet<PeptideModification>();
	private String charge;
	private Double precursorMZ;
	private String retentionTime;

	public IdentifiedPeptideImpl(String seq) {
		this.sequence = seq;
		this.id = getRandomInt();
	}

	public void addProtein(IdentifiedProtein protein) {
		this.proteins.add(protein);
	}

	private int getRandomInt() {
		Random generator = new Random();
		int i = generator.nextInt(Integer.MAX_VALUE);
		return i;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public String getSequence() {
		return sequence;
	}

	@Override
	public Set<PeptideScore> getScores() {
		return this.scores;
	}

	@Override
	public Set<PeptideModification> getModifications() {
		return modifications;
	}

	@Override
	public String getCharge() {
		return this.charge;
	}

	public void setCharge(String charge) {
		this.charge = charge;
	}

	@Override
	public String getMassDesviation() {
		Double theoreticalMZ = getTheoreticalMZ();
		if (theoreticalMZ != null && precursorMZ != null) {
			return String.valueOf(theoreticalMZ - precursorMZ);
		}
		return null;
	}

	private Double getTheoreticalMZ() {
		if (!getModifications().isEmpty()) {
			System.out.println("as df");
		}
		AASequenceImpl seq = ModificationMapping.getAASequenceImpl(getSequence(), getModifications());
		try {
			int z = Integer.valueOf(getCharge());
			double mz = seq.getMz(z);
			return mz;
		} catch (NumberFormatException e) {

		} catch (Exception e) {

		}
		return null;
	}

	public void setPrecursorMZ(Double mz) {
		this.precursorMZ = mz;
	}

	@Override
	public String getSpectrumRef() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputData getInputData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRank() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<IdentifiedProtein> getIdentifiedProteins() {
		return this.proteins;
	}

	@Override
	public String getRetentionTimeInSeconds() {
		return retentionTime;
	}

	public void addScore(PeptideScore peptideScore) {
		this.scores.add(peptideScore);

	}

	public void addModification(PeptideModification modification) {
		this.modifications.add(modification);
	}

	public void setRetentionTime(Double rt) {
		if (rt != null) {
			this.retentionTime = String.valueOf(rt);
		}
	}

}
