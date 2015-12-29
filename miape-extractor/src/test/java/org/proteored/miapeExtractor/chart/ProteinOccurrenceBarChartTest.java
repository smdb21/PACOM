package org.proteored.miapeExtractor.chart;

import javax.swing.JFrame;

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.proteored.miapeExtractor.analysis.charts.BarChart;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;

public class ProteinOccurrenceBarChartTest extends JFrame {
	public ProteinOccurrenceBarChartTest(String title, String subtitle,
			String xLabel, String yLabel, IdentificationItemEnum plotItem,
			Boolean distPepMod, CategoryDataset dataset) {
		super(title);

		BarChart chart = new BarChart(title, subtitle, xLabel, yLabel, dataset,
				PlotOrientation.HORIZONTAL);
		setContentPane(chart.getChartPanel());
	}

}
