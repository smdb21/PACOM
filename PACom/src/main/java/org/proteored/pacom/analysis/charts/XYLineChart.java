package org.proteored.pacom.analysis.charts;

import java.awt.Color;
import java.awt.Dimension;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.data.xy.XYDataset;

public class XYLineChart {
	private final JFreeChart chart;
	private final ChartPanel chartPanel;

	/**
	 * Creates a Scatter plot chart
	 * 
	 * @param title
	 * @param subtitle
	 * @param dataset
	 * @param xAxisLabel
	 * @param yAxisLabel
	 */
	public XYLineChart(String title, String subtitle, XYDataset dataset, String xAxisLabel, String yAxisLabel) {
		this.chart = createXYLineChart(title, subtitle, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true,
				true, false);
		this.chart.addSubtitle(new TextTitle(subtitle));

		final XYPlot plot = (XYPlot) this.chart.getPlot();

		this.chartPanel = new ChartPanel(chart);
		this.chartPanel.setFillZoomRectangle(true);
		this.chartPanel.setMouseWheelEnabled(true);

		final Dimension dimension = new Dimension(ChartProperties.DEFAULT_CHART_WIDTH,
				ChartProperties.DEFAULT_CHART_HEIGHT);

		this.chartPanel.setPreferredSize(dimension);
		this.chartPanel.setSize(dimension);
		this.chartPanel.setMaximumDrawWidth(ChartProperties.DEFAULT_CHART_WIDTH);

	}

	private JFreeChart createXYLineChart(String title, String subtitle, String xAxisLabel, String yAxisLabel,
			XYDataset dataset, PlotOrientation orientation, boolean legend, boolean tooltips, boolean urls) {
		if (orientation == null) {
			throw new IllegalArgumentException("Null 'orientation' argument.");
		}
		NumberAxis xAxis = new NumberAxis(xAxisLabel);
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(yAxisLabel);

		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
		// renderer.setShape(new Rectangle(2, 2));

		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		plot.setOrientation(orientation);
		// // Colors
		plot.setBackgroundPaint(Color.white);
		plot.setRangeGridlinePaint(Color.lightGray);
		if (tooltips) {
			renderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		}
		if (urls) {
			renderer.setURLGenerator(new StandardXYURLGenerator());
		}
		ValueAxis domainAxis = renderer.getPlot().getDomainAxis();
		// domainAxis.setRange(new Range(0, 100));
		renderer.getPlot().getRangeAxis().setAutoRange(true);

		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);

		return chart;
	}

	public ChartPanel getChartPanel() {
		return this.chartPanel;
	}

	public void addNewAxis(XYDataset dataset, String axisName) {
		if (chart != null) {
			NumberAxis newAxis = new NumberAxis(axisName);
			newAxis.setAutoRange(true);

			XYPlot plot = (XYPlot) this.chart.getPlot();

			plot.setRangeAxis(1, newAxis);
			plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
			plot.setDataset(1, dataset);
			plot.mapDatasetToRangeAxis(1, 1);

			XYLineAndShapeRenderer newRenderer = new XYLineAndShapeRenderer(true, false);
			plot.setRenderer(1, newRenderer);

		}
	}
}
