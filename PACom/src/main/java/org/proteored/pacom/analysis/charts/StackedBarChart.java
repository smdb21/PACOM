package org.proteored.pacom.analysis.charts;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.NumberFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.category.CategoryDataset;

public class StackedBarChart {
	private final JFreeChart chart;
	private ChartPanel chartPanel;
	private final String title;
	private final String categoryAxisLabel;
	private final String valueAxisLabel;
	private final Title subtitle;

	public StackedBarChart(String chartTitle, String subtitle, String xAxisLabel, String yAxisLabel,
			CategoryDataset dataset, PlotOrientation plotOrientation, boolean asPercentages) {

		this.title = chartTitle;
		this.subtitle = new TextTitle(subtitle);
		this.categoryAxisLabel = xAxisLabel;
		this.valueAxisLabel = yAxisLabel;

		chart = createChart(dataset, plotOrientation, asPercentages);

		chartPanel = new ChartPanel(chart);

		chartPanel.setFillZoomRectangle(true);
		chartPanel.setMouseWheelEnabled(true);

		final Dimension dimension = new Dimension(ChartProperties.DEFAULT_CHART_WIDTH,
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

	private JFreeChart createChart(CategoryDataset dataset, PlotOrientation plotOrientation, boolean asPercentages) {

		JFreeChart chart = ChartFactory.createStackedBarChart(title, categoryAxisLabel, valueAxisLabel, dataset,
				plotOrientation, true, true, false);
		chart.addSubtitle(this.subtitle);

		// Backgroung color
		// chart.setBackgroundPaint(new Color(229, 229, 229));
		// get plot
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		// // Colors
		plot.setBackgroundPaint(Color.white);
		plot.setRangeGridlinePaint(Color.lightGray);

		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setAutoRange(true);
		rangeAxis.setLowerMargin(0.0);
		rangeAxis.setUpperMargin(0.05);

		// bar aspect
		StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
		// to not use GradientBarPainter, which has a bright part that I dont
		// like
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setDrawBarOutline(true);
		renderer.setDefaultOutlinePaint(Color.black);
		renderer.setDefaultSeriesVisible(true);
		renderer.setDefaultItemLabelsVisible(true);
		renderer.setShadowVisible(false);
		renderer.setMaximumBarWidth(0.1);
		// No space between bar of the same category
		renderer.setItemMargin(0.4);
		// percentage values
		renderer.setRenderAsPercentages(asPercentages);

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));

		// item labels
		final StackedLabelGenerator generator = new StackedLabelGenerator(null);
		renderer.setDefaultItemLabelGenerator(generator);
		renderer.setDefaultItemLabelsVisible(true);

		renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.PLAIN, 9));

		return chart;
	}

	public void setNonIntegerItemLabels() {
		CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
		CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		renderer.setDefaultItemLabelsVisible(true);
	}

	public void setIntegerItemLabels() {
		CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
		CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setDefaultItemLabelGenerator(
				new StandardCategoryItemLabelGenerator("{2} ({3})", NumberFormat.getInstance().getIntegerInstance()));
		renderer.setDefaultItemLabelsVisible(true);
	}

	public void setHorizontalXLabel() {
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0));
	}
}
