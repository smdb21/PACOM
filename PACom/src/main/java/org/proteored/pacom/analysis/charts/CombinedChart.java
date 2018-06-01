package org.proteored.pacom.analysis.charts;

import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;

public class CombinedChart {
	private final JFreeChart chart;
	private final ChartPanel chartPanel;
	private final String title;
	private final String categoryAxisLabel;
	private final String barValueAxisLabel;
	private final String lineValueAxisLabel;
	private final TextTitle subtitle;

	public CombinedChart(String chartTitle, String subtitle, String xAxisLabel, String yBarAxisLabel,
			String yLineAxisLabel, CategoryDataset barDataSet, CategoryDataset lineDataSet) {
		CategoryDataset lineDataset = null;
		CategoryDataset barDataset = null;

		title = chartTitle;
		this.subtitle = new TextTitle(subtitle);
		categoryAxisLabel = xAxisLabel;
		barValueAxisLabel = yBarAxisLabel;
		lineValueAxisLabel = yLineAxisLabel;
		// if
		// (chartType.equals(ChartType.PROTEIN_OCURRENCE_OVER_EXPERIMENTS_BAR_CHART))
		// {
		barDataset = barDataSet;
		lineDataset = lineDataSet;
		// }

		chart = createChart(barDataset, lineDataset);

		chartPanel = new ChartPanel(chart);
		chartPanel.setFillZoomRectangle(true);
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setPreferredSize(
				new Dimension(ChartProperties.DEFAULT_CHART_WIDTH, ChartProperties.DEFAULT_CHART_HEIGHT));

	}

	public ChartPanel getChartPanel() {
		return chartPanel;
	}

	private JFreeChart createChart(CategoryDataset barDataset, CategoryDataset lineDataset) {
		// Bar chart
		final CategoryDataset dataset1 = lineDataset;
		CategoryDataset dataset2 = barDataset;
		if (true) {

			final NumberAxis rangeAxis1 = new NumberAxis(lineValueAxisLabel);
			rangeAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			final LineAndShapeRenderer renderer1 = new LineAndShapeRenderer();
			renderer1.setDefaultShapesVisible(true);
			renderer1.setDefaultSeriesVisible(true);
			renderer1.setDefaultItemLabelsVisible(true);
			renderer1.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());
			renderer1.setItemMargin(0.01);
			final StandardCategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator(
					StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, NumberFormat.getIntegerInstance());
			renderer1.setDefaultItemLabelGenerator(generator);
			renderer1.setDefaultItemLabelsVisible(true);
			final CategoryPlot subplot1 = new CategoryPlot(dataset1, null, rangeAxis1, renderer1);
			subplot1.setDomainGridlinesVisible(true);

			final NumberAxis rangeAxis2 = new NumberAxis(barValueAxisLabel);
			rangeAxis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			final BarRenderer renderer2 = new BarRenderer();
			renderer2.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());
			renderer2.setItemMargin(0.01);
			renderer2.setBarPainter(new StandardBarPainter());
			renderer2.setDrawBarOutline(true);
			renderer2.setDefaultOutlinePaint(Color.black);
			renderer2.setDefaultSeriesVisible(true);
			renderer2.setDefaultItemLabelsVisible(true);
			renderer2.setShadowVisible(false);
			// No space between bar of the same category
			renderer2.setItemMargin(0.4);
			renderer2.setMaximumBarWidth(0.1);
			final StandardCategoryItemLabelGenerator generator2 = new StandardCategoryItemLabelGenerator(
					StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, NumberFormat.getIntegerInstance());
			renderer2.setDefaultItemLabelGenerator(generator2);
			renderer2.setDefaultItemLabelsVisible(true);
			final CategoryPlot subplot2 = new CategoryPlot(dataset2, null, rangeAxis2, renderer2);
			subplot2.setDomainGridlinesVisible(true);

			final CategoryAxis domainAxis = new CategoryAxis(categoryAxisLabel);
			final CombinedDomainCategoryPlot combinedPlot = new CombinedDomainCategoryPlot(domainAxis);
			combinedPlot.add(subplot1, 2);
			combinedPlot.add(subplot2, 1);
			final JFreeChart chart = new JFreeChart(title, combinedPlot);
			chart.addSubtitle(subtitle);
			return chart;
		} else {
			final NumberAxis rangeAxis1 = new NumberAxis(lineValueAxisLabel);
			rangeAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

			// LineAndShapeRenderer renderer1 = new LineAndShapeRenderer();
			// renderer1.setBaseToolTipGenerator(new
			// StandardCategoryToolTipGenerator());
			final BarRenderer renderer1 = new BarRenderer();
			renderer1.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());

			final CategoryPlot subplot1 = new CategoryPlot(dataset1, null, rangeAxis1, renderer1);
			subplot1.setDomainGridlinesVisible(true);

			// Line Chart
			dataset2 = barDataset;
			final NumberAxis rangeAxis2 = new NumberAxis(barValueAxisLabel);
			rangeAxis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			final BarRenderer renderer2 = new BarRenderer();
			renderer2.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());
			final CategoryPlot subplot2 = new CategoryPlot(dataset2, null, rangeAxis2, renderer2);
			subplot2.setDomainGridlinesVisible(true);

			final CategoryAxis domainAxis = new CategoryAxis(categoryAxisLabel);
			final CombinedDomainCategoryPlot combinedPlot = new CombinedDomainCategoryPlot(domainAxis);
			combinedPlot.add(subplot1, 1);
			combinedPlot.add(subplot2, 1);

			final JFreeChart chart = new JFreeChart(title, combinedPlot);
			chart.addSubtitle(subtitle);
			return chart;
		}
	}
}
