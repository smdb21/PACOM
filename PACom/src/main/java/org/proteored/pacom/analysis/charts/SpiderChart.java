package org.proteored.pacom.analysis.charts;

import java.awt.Color;
import java.awt.Dimension;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;

public class SpiderChart {
	private final JFreeChart chart;
	private final ChartPanel chartPanel;
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
	public SpiderChart(String chartTitle, String subtitle, CategoryDataset dataset) {

		title = chartTitle;
		this.subtitle = new TextTitle(subtitle);

		chart = createChart(dataset);

		// set horizontal x label if just one series
		// if (dataset.getColumnCount() == 1 || !this.xLabelsExcedLimit())
		// this.setHorizontalXLabel();

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

	private JFreeChart createChart(CategoryDataset dataset) {
		final SpiderWebPlot spiderwebplot = new SpiderWebPlot(dataset);
		spiderwebplot.setStartAngle(54D);
		spiderwebplot.setInteriorGap(0.40000000000000002D);
		spiderwebplot.setToolTipGenerator(new StandardCategoryToolTipGenerator());
		final JFreeChart jFreeChart = new JFreeChart(title, TextTitle.DEFAULT_FONT, spiderwebplot, true);
		final LegendTitle legendtitle = new LegendTitle(spiderwebplot);
		legendtitle.setPosition(RectangleEdge.BOTTOM);
		jFreeChart.addSubtitle(subtitle);

		final StandardCategoryItemLabelGenerator standardCategoryItemLabelGenerator = new StandardCategoryItemLabelGenerator();
		spiderwebplot.setLabelGenerator(standardCategoryItemLabelGenerator);

		// spiderwebplot.setItemLabelsVisible(true);
		// Backgroung color
		jFreeChart.setBackgroundPaint(Color.white);

		// // Colors
		spiderwebplot.setBackgroundPaint(Color.white);
		return jFreeChart;
	}

	public void setNonIntegerItemLabels() {
		if (chart != null) {
			final CategoryPlot plot = (CategoryPlot) chart.getPlot();
			final CategoryItemRenderer renderer = plot.getRenderer();
			renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
			renderer.setDefaultItemLabelsVisible(true);
		}
	}

	public void setHorizontalXLabel() {
		if (chart != null) {
			final CategoryPlot plot = (CategoryPlot) chart.getPlot();
			final CategoryAxis domainAxis = plot.getDomainAxis();
			domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0));
		}
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
