package org.proteored.pacom.analysis.charts;

import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;

public class PieChart {
	private final JFreeChart chart;
	private final ChartPanel chartPanel;
	private final String title;
	private final Title subtitle;

	/**
	 * Creates a PieChart but with with preferred size
	 * 
	 * @param chartTitle
	 * @param subtitle
	 * @param dataset
	 */
	public PieChart(String chartTitle, String subtitle, PieDataset dataset) {
		this(chartTitle, subtitle, dataset, true);
	}

	/**
	 * 
	 * @param chartTitle
	 * @param subtitle
	 * @param xAxisLabel
	 * @param yAxisLabel
	 * @param dataset          if it is an {@link StatisticalCategoryDataset}, the
	 *                         bar chart will show the error lines, but not in the
	 *                         case of being a {@link CategoryDataset}
	 * @param setPreferredSite sets the default chart size with determined by
	 *                         ChartProperties.DEFAULT_CHART_WIDTH and HEIGHT
	 * @param plotOrientation
	 */
	public PieChart(String chartTitle, String subtitle, PieDataset dataset, boolean setPreferredSite) {

		this.title = chartTitle;
		this.subtitle = new TextTitle(subtitle);

		chart = createChart(dataset);

		chartPanel = new ChartPanel(chart);

		chartPanel.setFillZoomRectangle(true);
		chartPanel.setMouseWheelEnabled(true);

		if (setPreferredSite) {
			final Dimension dimension = new Dimension(ChartProperties.DEFAULT_CHART_WIDTH,
					ChartProperties.DEFAULT_CHART_HEIGHT);
			this.chartPanel.setPreferredSize(dimension);
			this.chartPanel.setSize(dimension);
		}
	}

	/**
	 * @return the chartPanel
	 */
	public ChartPanel getChartPanel() {
		return chartPanel;
	}

	public JFreeChart getChart() {
		return this.chart;
	}

	private JFreeChart createChart(PieDataset dataset) {

		final JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
		final PiePlot plot = (PiePlot) chart.getPlot();
		// // Colors
		plot.setBackgroundPaint(Color.white);
		chart.addSubtitle(this.subtitle);
		plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {1} ({2})",
				NumberFormat.getIntegerInstance(), NumberFormat.getPercentInstance()));
		return chart;
	}

}
