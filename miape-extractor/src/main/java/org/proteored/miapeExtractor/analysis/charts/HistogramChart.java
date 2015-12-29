package org.proteored.miapeExtractor.analysis.charts;

import java.awt.Dimension;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.data.Range;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.IntervalXYDataset;

public class HistogramChart {
	private final JFreeChart chart;
	private ChartPanel chartPanel;

	public HistogramChart(String chartTitle, String subtitle,
			HistogramDataset dataset, String xAxisLabel) {

		this(chartTitle, subtitle, dataset, xAxisLabel, false);
	}

	public HistogramChart(String chartTitle, String subtitle,
			HistogramDataset dataset, String xAxisLabel, boolean showAsBarChart) {
		// HistogramDataset dataset = null;
		//
		// dataset = DatasetFactory.createScoreCorrelationDataSet(idSets,
		// scoreName, plotItem, bins,
		// addZeroZeroValue);
		// dataset.setType(type);
		String yAxisLabel = "";
		final HistogramType type = dataset.getType();
		if (type.equals(HistogramType.FREQUENCY))
			yAxisLabel = "frequency";
		else if (type.equals(HistogramType.RELATIVE_FREQUENCY))
			yAxisLabel = "relative frequency";
		else if (type.equals(HistogramType.SCALE_AREA_TO_1))
			yAxisLabel = "normalized frequency";

		this.chart = HistogramChart.createHistogram(chartTitle, subtitle,
				xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL,
				true, true, false, showAsBarChart);

		this.chartPanel = new ChartPanel(chart);
		this.chartPanel.setFillZoomRectangle(true);
		this.chartPanel.setMouseWheelEnabled(true);

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

	private static JFreeChart createHistogram(String title, String subtitle,
			String xAxisLabel, String yAxisLabel, IntervalXYDataset dataset,
			PlotOrientation orientation, boolean legend, boolean tooltips,
			boolean urls, boolean bars) {
		if (orientation == null) {
			throw new IllegalArgumentException("Null 'orientation' argument.");
		}
		NumberAxis xAxis = new NumberAxis(xAxisLabel);
		xAxis.setAutoRangeIncludesZero(true);
		NumberAxis yAxis = new NumberAxis(yAxisLabel);
		yAxis.setAutoRange(true);

		XYItemRenderer renderer = null;
		if (bars) {
			renderer = new XYBarRenderer();
		} else {
			// Line = true, shape = false
			renderer = new XYSplineRenderer();
			((XYLineAndShapeRenderer) renderer).setBaseShapesVisible(false);
		}
		if (tooltips) {
			renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		}
		if (urls) {
			renderer.setURLGenerator(new StandardXYURLGenerator());
		}

		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		plot.setOrientation(orientation);
		plot.setDomainZeroBaselineVisible(true);
		plot.setRangeZeroBaselineVisible(true);
		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
				plot, legend);
		chart.addSubtitle(new TextTitle(subtitle));
		// currentTheme.apply(chart);
		return chart;
	}

	public void setXRangeValues(double i, double j) {
		this.chart.getXYPlot().getRangeAxis().setRange(i, j);

	}

	public void centerRangeAxisOnZero() {
		final XYPlot plot = this.chart.getXYPlot();
		// Center by zero
		final ValueAxis domainAxis = plot.getDomainAxis();
		final Range dataRangeX = plot.getDataRange(domainAxis);
		if (dataRangeX == null)
			return;
		double min = dataRangeX.getLowerBound();
		double max = dataRangeX.getUpperBound();
		double tmp = 0;
		if (0 - min > max)
			tmp = min;
		else
			tmp = max;
		if (tmp < 0) {
			domainAxis.setUpperBound(-tmp);
			domainAxis.setLowerBound(tmp);
		} else {
			domainAxis.setUpperBound(tmp);
			domainAxis.setLowerBound(-tmp);
		}
	}
}
