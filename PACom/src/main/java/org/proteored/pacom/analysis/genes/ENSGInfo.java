package org.proteored.pacom.analysis.genes;

import java.util.ArrayList;
import java.util.List;

public class ENSGInfo {
	@Override
	public String toString() {
		return "CHR=" + chrName + " ENSGID=" + ensG_ID + " Gene_Name="
				+ geneName;
	}

	public final static String KNOWN = "KNOWN";
	public final static String UNKNOWN = "UNKNOWN";
	private String ensG_ID;

	private String known;
	private Researcher researcher;
	private String geneName;
	private String description;
	private String chrName;
	private String strand;
	private String band;
	private int transcriptCount;
	private String geneBiotype;
	private String status;
	private boolean uniprot;
	private boolean uniprot_protEvidence;
	private boolean gpmdb;
	private boolean gpmdb_lt_minus_5;
	private boolean gpmdb_lt_minus_2;
	private double loge;
	private boolean hpa;
	private boolean napa;
	private List<String> proteinACC = new ArrayList<String>();
	private List<String> ensPs = new ArrayList<String>();
	private boolean assigned = false;
	private String proteinEvidence;
	private boolean missing;

	public String getKnown() {
		return known;
	}

	public String getEnsG_ID() {
		return ensG_ID;
	}

	public void setEnsG_ID(String ensG_ID) {
		this.ensG_ID = ensG_ID;
	}

	public Researcher getResearcher() {
		return researcher;
	}

	public void setResearcher(Researcher researcher) {
		this.researcher = researcher;
	}

	public String getGeneName() {
		return geneName;
	}

	public void setGeneName(String external_gene_id) {
		geneName = external_gene_id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getChrName() {
		return chrName;
	}

	public void setChrName(String chrName) {
		this.chrName = chrName;
	}

	public String getStrand() {
		return strand;
	}

	public void setStrand(String strand) {
		this.strand = strand;
	}

	public String getBand() {
		return band;
	}

	public void setBand(String band) {
		this.band = band;
	}

	public int getTranscriptCount() {
		return transcriptCount;
	}

	public void setTranscriptCount(int transcriptCount) {
		this.transcriptCount = transcriptCount;
	}

	public String getGeneBiotype() {
		return geneBiotype;
	}

	public void setGeneBiotype(String geneBiotype) {
		this.geneBiotype = geneBiotype;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isUniprot() {
		return uniprot;
	}

	public void setUniprot(boolean uniprot) {
		this.uniprot = uniprot;
	}

	public boolean isUniprot_protEvidence() {
		return uniprot_protEvidence;
	}

	public void setUniprot_protEvidence(boolean uniprot_protEvidence) {
		this.uniprot_protEvidence = uniprot_protEvidence;
	}

	public boolean isGpmdb() {
		return gpmdb;
	}

	public void setGpmdb(boolean gpmdb) {
		this.gpmdb = gpmdb;
	}

	public boolean isGpmdb_lt_minus_5() {
		return gpmdb_lt_minus_5;
	}

	public void setGpmdb_lt_minus_5(boolean gpmdb_lt_minus_5) {
		this.gpmdb_lt_minus_5 = gpmdb_lt_minus_5;
	}

	public boolean isGpmdb_lt_minus_2() {
		return gpmdb_lt_minus_2;
	}

	public void setGpmdb_lt_minus_2(boolean gpmdb_lt_minus_2) {
		this.gpmdb_lt_minus_2 = gpmdb_lt_minus_2;
	}

	public double getLoge() {
		return loge;
	}

	public void setLoge(double loge) {
		this.loge = loge;
	}

	public boolean isHpa() {
		return hpa;
	}

	public void setHpa(boolean hpa) {
		this.hpa = hpa;
	}

	public boolean isNapa() {
		return napa;
	}

	public void setNapa(boolean napa) {
		this.napa = napa;
	}

	public void setKnown(String known) {
		this.known = known;
	}

	public List<String> getProteinACC() {
		return proteinACC;
	}

	public List<String> getENSPs() {
		return ensPs;
	}

	public void setProteinACC(List<String> proteinACC) {
		this.proteinACC = proteinACC;
	}

	public void addProteinACC(String proteinACC) {
		if (this.proteinACC == null)
			this.proteinACC = new ArrayList<String>();
		if (!this.proteinACC.contains(proteinACC))
			this.proteinACC.add(proteinACC);
	}

	public void addENSP(String ensP) {
		if (ensPs == null)
			ensPs = new ArrayList<String>();
		if (!ensPs.contains(ensP))
			ensPs.add(ensP);
	}

	public boolean isAssigned() {
		return assigned;
	}

	public void setAssigned(boolean assigned) {
		this.assigned = assigned;
	}

	public String getProteinEvidence() {
		return proteinEvidence;
	}

	public void setProteinEvidence(String proteinEvidence) {
		this.proteinEvidence = proteinEvidence;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ENSGInfo) {
			ENSGInfo gene = (ENSGInfo) obj;
			if (gene.getGeneName() != null
					&& gene.getGeneName().equals(getGeneName()))
				if (gene.getChrName().equals(chrName))
					if (gene.getEnsG_ID() != null && getEnsG_ID() != null) {
						if (gene.getEnsG_ID().equals(getEnsG_ID()))
							return true;
					} else if (gene.getEnsG_ID() == null
							&& getEnsG_ID() == null)
						return true;
			return false;
		}
		return super.equals(obj);
	}

	public void setMissing(boolean missing) {
		this.missing = missing;

	}

	public boolean isMissing() {
		return missing;
	}

}
