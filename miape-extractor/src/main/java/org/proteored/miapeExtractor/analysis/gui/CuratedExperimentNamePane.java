package org.proteored.miapeExtractor.analysis.gui;

import java.awt.Component;

import org.proteored.miapeapi.experiment.model.ExperimentList;

public class CuratedExperimentNamePane extends Component {

	public static CuratedExperimentNameCreatorDialog showCurateExperimentNameDialog(
			java.awt.Frame parent, ExperimentList experimentList) {
		CuratedExperimentNameCreatorDialog pane = new CuratedExperimentNameCreatorDialog(parent,
				experimentList);
		pane.setVisible(true);
		return pane;
	}
}
