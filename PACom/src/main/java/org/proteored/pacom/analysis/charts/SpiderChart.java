package org.proteored.pacom.analysis.charts;

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

		this.title = chartTitle;
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

	private JFreeChart createChart(CategoryDataset dataset) {
		SpiderWebPlot spiderwebplot = new SpiderWebPlot(dataset);
		spiderwebplot.setStartAngle(54D);
		spiderwebplot.setInteriorGap(0.40000000000000002D);
		spiderwebplot.setToolTipGenerator(new StandardCategoryToolTipGenerator());
		JFreeChart jFreeChart = new JFreeChart(this.title, TextTitle.DEFAULT_FONT, spiderwebplot, true);
		LegendTitle legendtitle = new LegendTitle(spiderwebplot);
		legendtitle.setPosition(RectangleEdge.BOTTOM);
		jFreeChart.addSubtitle(this.subtitle);

		StandardCategoryItemLabelGenerator standardCategoryItemLabelGenerator = new StandardCategoryItemLabelGenerator();
		spiderwebplot.setLabelGenerator(standardCategoryItemLabelGenerator);

		// spiderwebplot.setItemLabelsVisible(true);

		return jFreeChart;
	}

	public void setNonIntegerItemLabels() {
		if (this.chart != null) {
			CategoryPlot plot = (CategoryPlot) this.chart.getPlot();
			CategoryItemRenderer renderer = plot.getRenderer();
			renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
			renderer.setDefaultItemLabelsVisible(true);
		}
	}

	public void setHorizontalXLabel() {
		if (chart != null) {
			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			CategoryAxis domainAxis = plot.getDomainAxis();
			domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0));
		}
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
