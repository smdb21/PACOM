package org.proteored.miapeExtractor.analysis.charts;

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
	private ChartPanel chartPanel;
	private final String title;
	private final Title subtitle;

	/**
	 * 
	 * @param chartTitle
	 * @param subtitle
	 * @param xAxisLabel
	 * @param yAxisLabel
	 * @param dataset
	 *            if it is an {@link StatisticalCategoryDataset}, the bar chart
	 *            will show the error lines, but not in the case of being a
	 *            {@link CategoryDataset}
	 * @param plotOrientation
	 */
	public PieChart(String chartTitle, String subtitle, PieDataset dataset) {

		this.title = chartTitle;
		this.subtitle = new TextTitle(subtitle);

		chart = createChart(dataset);

		chartPanel = new ChartPanel(chart);

		chartPanel.setFillZoomRectangle(true);
		chartPanel.setMouseWheelEnabled(true);

		final Dimension dimension = new Dimension(
				ChartProperties.DEFAULT_CHART_WIDTH,
				ChartProperties.DEFAULT_CHART_HEIGHT);
		this.chartPanel.setPreferredSize(dimension);
		this.chartPanel.setSize(dimension);

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

		JFreeChart chart = ChartFactory.createPieChart(title, dataset, true,
				true, false);
		PiePlot plot = (PiePlot) chart.getPlot();

		chart.addSubtitle(this.subtitle);
		// Backgroung color
		// chart.setBackgroundPaint(new Color(229, 229, 229));
		// get plot

		// // Colors
		// plot.setBackgroundPaint(Color.white);
		// plot.setRangeGridlinePaint(Color.lightGray);

		plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
				"{0} = {1} ({2})", NumberFormat.getInstance()
						.getIntegerInstance(), NumberFormat.getInstance()
						.getPercentInstance()));
		return chart;
	}

	// public void setNonIntegerItemLabels() {
	// CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
	// CategoryItemRenderer renderer = plot.getRenderer();
	// renderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator());
	// renderer.setItemLabelsVisible(true);
	// }
}
