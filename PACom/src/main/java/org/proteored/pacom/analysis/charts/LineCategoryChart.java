package org.proteored.pacom.analysis.charts;

import java.awt.Color;
import java.awt.Dimension;
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
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;

public class LineCategoryChart {
	private final JFreeChart chart;
	private final ChartPanel chartPanel;
	private final String title;
	private final String categoryAxisLabel;
	private final String valueAxisLabel;
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
	public LineCategoryChart(String chartTitle, String subtitle, String xAxisLabel, String yAxisLabel,
			CategoryDataset dataset, PlotOrientation plotOrientation) {

		title = chartTitle;
		this.subtitle = new TextTitle(subtitle);
		categoryAxisLabel = xAxisLabel;
		valueAxisLabel = yAxisLabel;

		chart = createChart(dataset, plotOrientation);
		// set horizontal x label if just one series
		if (dataset.getColumnCount() == 1 || !xLabelsExcedLimit())
			setHorizontalXLabel();
		chartPanel = new ChartPanel(chart);
		chartPanel.setFillZoomRectangle(true);
		chartPanel.setMouseWheelEnabled(true);

		final Dimension dimension = new Dimension(ChartProperties.DEFAULT_CHART_WIDTH,
				ChartProperties.DEFAULT_CHART_HEIGHT);
		chartPanel.setPreferredSize(dimension);
		chartPanel.setSize(dimension);

	}

	/**
	 * @return the chartPanel
	 */
	public ChartPanel getChartPanel() {
		return chartPanel;
	}

	public JFreeChart getChart() {
		return chart;
	}

	private JFreeChart createChart(CategoryDataset dataset, PlotOrientation plotOrientation) {

		final JFreeChart chart = ChartFactory.createLineChart(title, categoryAxisLabel, valueAxisLabel, dataset,
				plotOrientation, true, true, false);
		chart.addSubtitle(subtitle);
		// Backgroung color
		// chart.setBackgroundPaint(new Color(229, 229, 229));
		// get plot
		final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		// // Colors
		plot.setBackgroundPaint(Color.white);
		plot.setRangeGridlinePaint(Color.lightGray);

		// set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setAutoRange(true);
		rangeAxis.setLowerMargin(0.0);
		rangeAxis.setUpperMargin(0.05);

		final CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6));
		domainAxis.setCategoryLabelPositionOffset(10);
		final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();

		// renderer.setDrawBarOutline(true);
		renderer.setDefaultShapesVisible(true);
		renderer.setDefaultSeriesVisible(true);
		renderer.setDefaultItemLabelsVisible(true);
		// renderer.setShadowVisible(false);
		// No space between bar of the same category
		renderer.setItemMargin(0.01);
		// item labels
		final StandardCategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
				StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, NumberFormat.getIntegerInstance());
		renderer.setDefaultItemLabelGenerator(generator);
		renderer.setDefaultItemLabelsVisible(true);

		return chart;
	}

	public void setNonIntegerItemLabels() {
		final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		final CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		renderer.setDefaultItemLabelsVisible(true);
	}

	public void setHorizontalXLabel() {
		final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		final CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0));
	}

	public boolean xLabelsExcedLimit() {
		if (chart != null) {
			final CategoryDataset dataset = chart.getCategoryPlot().getDataset();

			int totalLength = 0;
			for (final Object key : dataset.getColumnKeys()) {
				final String name = (String) key;
				totalLength += name.length();
			}
			if (totalLength > 50)
				return true;
		}
		return false;
	}
}
