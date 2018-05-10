package org.proteored.pacom.analysis.exporters.util;

import org.proteored.pacom.analysis.exporters.Exporter;
import org.proteored.pacom.analysis.exporters.ProteomeXchangeFilev2_1;

import gnu.trove.list.array.TIntArrayList;

public class PexFileMapping {
	private final String fileType;
	private final int id;
	private final String path;
	private TIntArrayList relationships = new TIntArrayList();

	public PexFileMapping(String fileType, int id, String path, TIntArrayList relationships) {
		super();
		this.fileType = fileType;
		this.id = id;
		this.path = path;
		if (relationships != null)
			this.relationships = relationships;
	}

	public TIntArrayList getRelationships() {
		return relationships;
	}

	@Override
	public String toString() {
		return ProteomeXchangeFilev2_1.FME + Exporter.TAB + removeNotAllowedCharacteres(id) + Exporter.TAB
				+ removeNotAllowedCharacteres(fileType) + Exporter.TAB + removeNotAllowedCharacteres(path)
				+ Exporter.TAB + removeNotAllowedCharacteres(getRelationshipsAsString());
	}

	private String removeNotAllowedCharacteres(Object obj) {
		if (obj != null) {
			final String string = obj.toString();
			String ret = "";
			ret = string.replace(Exporter.NEWLINE, " ");
			ret = ret.replace(String.valueOf(Exporter.TAB), " ");
			return ret;
		}
		return null;
	}

	private String getRelationshipsAsString() {
		String ret = "";
		for (final int id : relationships.toArray()) {
			if (!"".equals(ret))
				ret += ",";
			ret += id;
		}
		return ret;
	}

	public void setRelationships(TIntArrayList relationships) {
		this.relationships = relationships;
	}

	public void addRelationships(TIntArrayList relationships) {
		if (relationships == null)
			this.relationships = new TIntArrayList();
		this.relationships.addAll(relationships);
	}

	public void addRelationship(Integer relationship) {
		if (relationships == null)
			relationships = new TIntArrayList();
		if (!relationships.contains(relationship))// do not duplicate
													// relationships
			relationships.add(relationship);
	}

	public String getFileType() {
		return fileType;
	}

	public int getId() {
		return id;
	}

	public String getPath() {
		return path;
	}

}
