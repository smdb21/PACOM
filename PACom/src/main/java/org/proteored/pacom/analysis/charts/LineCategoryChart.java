package org.proteored.pacom.analysis.charts;

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

		this.title = chartTitle;
		this.subtitle = new TextTitle(subtitle);
		this.categoryAxisLabel = xAxisLabel;
		this.valueAxisLabel = yAxisLabel;

		chart = createChart(dataset, plotOrientation);
		// set horizontal x label if just one series
		if (dataset.getColumnCount() == 1 || !this.xLabelsExcedLimit())
			this.setHorizontalXLabel();
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

	public JFreeChart getChart() {
		return this.chart;
	}

	private JFreeChart createChart(CategoryDataset dataset, PlotOrientation plotOrientation) {

		JFreeChart chart = ChartFactory.createLineChart(title, categoryAxisLabel, valueAxisLabel, dataset,
				plotOrientation, true, true, false);
		chart.addSubtitle(this.subtitle);
		// Backgroung color
		// chart.setBackgroundPaint(new Color(229, 229, 229));
		// get plot
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		// // Colors
		// plot.setBackgroundPaint(Color.white);
		// plot.setRangeGridlinePaint(Color.lightGray);

		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setAutoRange(true);
		rangeAxis.setLowerMargin(0.0);
		rangeAxis.setUpperMargin(0.05);

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 2));

		LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();

		// renderer.setDrawBarOutline(true);
		renderer.setShapesVisible(true);
		renderer.setBaseSeriesVisible(true);
		renderer.setBaseItemLabelsVisible(true);
		// renderer.setShadowVisible(false);
		// No space between bar of the same category
		renderer.setItemMargin(0.01);
		// item labels
		final StandardCategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
				StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, NumberFormat.getIntegerInstance());
		renderer.setItemLabelGenerator(generator);
		renderer.setItemLabelsVisible(true);

		return chart;
	}

	public void setNonIntegerItemLabels() {
		CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
		CategoryItemRenderer renderer = plot.getRenderer();
		renderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		renderer.setItemLabelsVisible(true);
	}

	public void setHorizontalXLabel() {
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0));
	}

	public boolean xLabelsExcedLimit() {
		if (this.chart != null) {
			CategoryDataset dataset = this.chart.getCategoryPlot().getDataset();

			int totalLength = 0;
			for (Object key : dataset.getColumnKeys()) {
				String name = (String) key;
				totalLength += name.length();
			}
			if (totalLength > 50)
				return true;
		}
		return false;
	}
}
