package org.proteored.miapeExtractor.analysis.exporters.tasks;

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

public class IdentifiedPeptideImpl implements IdentifiedPeptide {
	private final String sequence;
	private final int id;
	private List<IdentifiedProtein> proteins = new ArrayList<IdentifiedProtein>();
	private Set<PeptideScore> scores = new HashSet<PeptideScore>();

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCharge() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMassDesviation() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	public void addScore(PeptideScore peptideScore) {
		this.scores.add(peptideScore);

	}

}
