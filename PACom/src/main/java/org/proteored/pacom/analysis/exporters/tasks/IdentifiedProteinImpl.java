package org.proteored.pacom.analysis.exporters.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.proteored.miapeapi.interfaces.msi.IdentifiedPeptide;
import org.proteored.miapeapi.interfaces.msi.IdentifiedProtein;
import org.proteored.miapeapi.interfaces.msi.ProteinScore;

public class IdentifiedProteinImpl implements IdentifiedProtein {
	private final String acc;
	private final int id;
	private final List<IdentifiedPeptide> peptides = new ArrayList<IdentifiedPeptide>();
	private final String description;

	public IdentifiedProteinImpl(String acc) {
		this.acc = acc;
		description = null;
		id = getRandomInt();
	}

	public IdentifiedProteinImpl(String acc, String description) {
		this.acc = acc;
		this.description = description;
		id = getRandomInt();
	}

	public void addPeptide(IdentifiedPeptide peptide) {
		peptides.add(peptide);
	}

	private int getRandomInt() {
		Random generator = new Random();
		int i = generator.nextInt(Integer.MAX_VALUE);
		return i;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getAccession() {
		return acc;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Set<ProteinScore> getScores() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPeptideNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCoverage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPeaksMatchedNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUnmatchedSignals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAdditionalInformation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean getValidationStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValidationType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValidationValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IdentifiedPeptide> getIdentifiedPeptides() {
		return peptides;
	}

}
