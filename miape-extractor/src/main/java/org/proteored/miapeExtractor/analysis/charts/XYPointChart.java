package org.proteored.miapeExtractor.analysis.charts;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.statistics.Regression;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataset;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;

public class XYPointChart {
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
	public XYPointChart(String title, String subtitle, XYDataset dataset,
			String xAxisLabel, String yAxisLabel) {

		this.chart = ChartFactory.createScatterPlot(title, xAxisLabel,
				yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true,
				false);
		this.chart.addSubtitle(new TextTitle(subtitle));
		final XYPlot plot = (XYPlot) this.chart.getPlot();
		final XYItemRenderer renderer = plot.getRenderer();
		final ValueAxis rangeAxis = plot.getRangeAxis();
		final ValueAxis domainAxis = plot.getDomainAxis();
		final double upperBound1 = rangeAxis.getUpperBound();
		final double upperBound2 = domainAxis.getUpperBound();
		if (upperBound1 > upperBound2)
			domainAxis.setUpperBound(upperBound1);
		if (upperBound2 > upperBound1)
			rangeAxis.setUpperBound(upperBound2);
		final double lowerBound1 = rangeAxis.getLowerBound();
		final double lowerBound2 = domainAxis.getLowerBound();
		if (lowerBound1 > lowerBound2)
			rangeAxis.setLowerBound(lowerBound2);
		if (lowerBound2 > lowerBound1)
			domainAxis.setLowerBound(lowerBound1);

		Shape shape = new Ellipse2D.Double(-1.0, -1.0, 2.0, 2.0);
		for (int i = 0; i < plot.getSeriesCount(); i++) {
			renderer.setSeriesShape(i, shape);
		}

		this.chartPanel = new ChartPanel(chart);
		this.chartPanel.setFillZoomRectangle(true);
		this.chartPanel.setMouseWheelEnabled(true);

		final Dimension dimension = new Dimension(
				ChartProperties.DEFAULT_CHART_WIDTH,
				ChartProperties.DEFAULT_CHART_HEIGHT);
		this.chartPanel.setPreferredSize(dimension);
		this.chartPanel.setSize(dimension);

	}

	public void setSeriesShape(Shape shape) {
		final XYPlot plot = (XYPlot) this.chart.getPlot();
		final XYItemRenderer renderer = plot.getRenderer();
		for (int i = 0; i < plot.getSeriesCount(); i++) {
			renderer.setSeriesShape(i, shape);
		}
	}

	public void setTinnySeriesShape() {
		final XYPlot plot = (XYPlot) this.chart.getPlot();
		final XYItemRenderer renderer = plot.getRenderer();
		Shape shape = new Ellipse2D.Double(-1, -1, 2, 2);
		for (int i = 0; i < plot.getSeriesCount(); i++) {
			renderer.setSeriesShape(i, shape);
		}
	}

	public void setAutomaticScales() {
		XYPlot plot = (XYPlot) this.chart.getPlot();
		plot.getRangeAxis().setAutoRange(true);
		plot.getDomainAxis().setAutoRange(true);
	}

	public ChartPanel getChartPanel() {
		return this.chartPanel;
	}

	public void addRegressionLine(boolean showError) {
		try {
			final XYPlot plot = (XYPlot) this.chart.getPlot();
			final ValueAxis rangeAxis = plot.getRangeAxis();
			final ValueAxis domainAxis = plot.getDomainAxis();
			List<XYDataset> lineDataSets = new ArrayList<XYDataset>();
			List<Paint> colors = new ArrayList<Paint>();
			for (int i = 0; i < plot.getDataset().getSeriesCount(); i++) {
				// Regression line:
				try {
					double ad[] = Regression.getOLSRegression(
							plot.getDataset(), i);

					LineFunction2D linefunction2d = new LineFunction2D(ad[0],
							ad[1]);
					double start = plot.getDataRange(domainAxis)
							.getLowerBound();
					double end = plot.getDataRange(domainAxis).getUpperBound();
					int numSamples = plot.getDataset().getItemCount(i);
					DecimalFormat df = new DecimalFormat("#.####");
					// Calculate the error of the regression

					// XYDataset xydataset =
					// DatasetUtilities.sampleFunction2D(linefunction2d, start,
					// end,
					// numSamples,
					// "Fitted Regression Line (" + df.format(ad[1]) + "+" +
					// df.format(ad[0]) + "*x)");
					String serieName = "regression";
					if (showError) {
						double error = calculateCorrelation(plot.getDataset(),
								i, linefunction2d);
						serieName = "R^2=" + df.format(error);
					}

					XYDataset xydataset = DatasetUtilities.sampleFunction2D(
							linefunction2d, start, end, numSamples, serieName);
					lineDataSets.add(xydataset);
					colors.add(plot.getRenderer().getItemPaint(i, 0));
				} catch (IllegalArgumentException e) {

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			int i = plot.getDatasetCount();
			int j = 0;
			for (XYDataset xyDataset : lineDataSets) {
				i++;
				plot.setDataset(i, xyDataset);
				XYLineAndShapeRenderer xylineandshaperenderer1 = new XYLineAndShapeRenderer(
						true, false);
				xylineandshaperenderer1.setSeriesPaint(0, colors.get(j));
				plot.setRenderer(i, xylineandshaperenderer1);
				j++;
			}
		} catch (IllegalArgumentException e) {
			throw new IllegalMiapeArgumentException(e.getMessage());
		}
	}

	private double calculateError(XYDataset dataset, int seriesIndex,
			LineFunction2D linefunction2d) {
		final int itemCount = dataset.getItemCount(seriesIndex);
		double totalError = 0;
		for (int i = 0; i < itemCount; i++) {
			Double x = null;
			try {
				x = (Double) dataset.getX(seriesIndex, i);
			} catch (ClassCastException e) {
				x = Double
						.valueOf(String.valueOf(dataset.getX(seriesIndex, i)));
			}
			double theoreticalYValue = linefunction2d.getValue(x);
			double experimentalYValue = dataset.getYValue(seriesIndex, i);
			double error = (experimentalYValue - theoreticalYValue)
					* (experimentalYValue - theoreticalYValue);
			totalError += error;
		}
		return Math.sqrt(totalError / itemCount);

	}

	private double calculateCorrelation(XYDataset dataset, int seriesIndex,
			LineFunction2D linefunction2d) {
		final int itemCount = dataset.getItemCount(seriesIndex);
		Double[] data1 = new Double[itemCount];
		Double[] data2 = new Double[itemCount];
		for (int i = 0; i < itemCount; i++) {
			Double x = null;

			try {
				x = (Double) dataset.getX(seriesIndex, i);
			} catch (ClassCastException e) {
				x = Double
						.valueOf(String.valueOf(dataset.getX(seriesIndex, i)));
			}
			double theoreticalYValue = linefunction2d.getValue(x);
			double experimentalYValue = dataset.getYValue(seriesIndex, i);
			data1[i] = theoreticalYValue;
			data2[i] = experimentalYValue;

		}

		final double correlation = Statistics.getCorrelation(data1, data2);
		return Math.pow(correlation, 2);

	}

	public void addDiagonalLine() {
		final XYPlot plot = (XYPlot) this.chart.getPlot();
		final ValueAxis rangeAxis = plot.getRangeAxis();
		final ValueAxis domainAxis = plot.getDomainAxis();
		// Diagonal line:
		LineFunction2D linefunction2d = new LineFunction2D(0, 1);
		if (plot.getDataRange(domainAxis) != null) {
			double start = plot.getDataRange(domainAxis).getLowerBound();
			double end = plot.getDataRange(domainAxis).getUpperBound();

			int numSamples = plot.getDataset(0).getItemCount(0);
			int numDataSet = plot.getDatasetCount() + 1;
			if (numSamples > 1) {
				XYDataset xydataset = DatasetUtilities.sampleFunction2D(
						linefunction2d, start, end, numSamples, "Diagonal");

				plot.setDataset(numDataSet, xydataset);
			}
			XYLineAndShapeRenderer xylineandshaperenderer1 = new XYLineAndShapeRenderer(
					true, false);
			xylineandshaperenderer1.setSeriesPaint(0, Color.BLACK);
			plot.setRenderer(numDataSet, xylineandshaperenderer1);

		}
	}

	public void addHorizontalLine(double y) {
		final XYPlot plot = (XYPlot) this.chart.getPlot();
		final ValueAxis rangeAxis = plot.getRangeAxis();
		final ValueAxis domainAxis = plot.getDomainAxis();
		// Diagonal line:
		LineFunction2D linefunction2d = new LineFunction2D(0, 0);
		final Range dataRange = plot.getDataRange(domainAxis);
		if (dataRange == null)
			return;
		double start = dataRange.getLowerBound();
		double end = dataRange.getUpperBound();
		int numSamples = plot.getDataset(0).getItemCount(0);
		XYDataset xydataset = DatasetUtilities.sampleFunction2D(linefunction2d,
				start, end, numSamples, "Zero horizontal");
		int numDataSet = plot.getDatasetCount() + 1;
		plot.setDataset(numDataSet, xydataset);
		XYLineAndShapeRenderer xylineandshaperenderer1 = new XYLineAndShapeRenderer(
				true, false);
		xylineandshaperenderer1.setSeriesPaint(0, Color.BLACK);
		plot.setRenderer(numDataSet, xylineandshaperenderer1);
	}

	public void centerRangeAxisOnZero() {
		final XYPlot plot = (XYPlot) this.chart.getPlot();
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
		domainAxis.setUpperBound(tmp);
		domainAxis.setLowerBound(-tmp);
	}
}
