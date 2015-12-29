package org.proteored.miapeExtractor.analysis.util;

import java.util.ArrayList;
import java.util.List;

import org.proteored.miapeapi.experiment.model.Experiment;
import org.proteored.miapeapi.experiment.model.IdentificationSet;

public class Util {
	public static List<IdentificationSet> getListOfIdSets(List<Experiment> experimentList) {
		List<IdentificationSet> tmp = new ArrayList<IdentificationSet>();
		for (Experiment identificationSet : experimentList) {
			tmp.add(identificationSet);
		}
		return tmp;
	}
}
