package org.proteored.miapeExtractor.chart;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.ui.RefineryUtilities;
import org.proteored.miapeExtractor.analysis.charts.WordCramChart;
import org.proteored.miapeapi.experiment.model.ExperimentList;

public class WordCramChartTest {

	public static void main(String args[]) {
		List<String> list = new ArrayList<String>();
		list.add("Figura");
		list.add("cada");
		// list.add("caca");
		// list.add("pedo");
		final ExperimentList createExperiments = ExperimentsUtilTest
				.createExperiments(null);

		for (int i = 0; i < 3; i++) {

			WordCramChart chart = new WordCramChart(createExperiments, list, 6)
					.maximumNumberOfWords(2000);
			JFrame frame = new JFrame();
			frame.add(chart);
			frame.pack();
			RefineryUtilities.centerFrameOnScreen(frame);
			frame.setVisible(true);
		}
	}
}
