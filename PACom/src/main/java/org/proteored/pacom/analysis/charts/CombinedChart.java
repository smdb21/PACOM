package org.proteored.pacom.analysis.charts;

import java.awt.Dimension;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
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

		this.title = chartTitle;
		this.subtitle = new TextTitle(subtitle);
		this.categoryAxisLabel = xAxisLabel;
		this.barValueAxisLabel = yBarAxisLabel;
		this.lineValueAxisLabel = yLineAxisLabel;
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
		CategoryDataset dataset1 = lineDataset;
		CategoryDataset dataset2 = barDataset;
		if (true) {

			NumberAxis rangeAxis1 = new NumberAxis(lineValueAxisLabel);
			rangeAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			LineAndShapeRenderer renderer1 = new LineAndShapeRenderer();
			renderer1.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());
			CategoryPlot subplot1 = new CategoryPlot(dataset1, null, rangeAxis1, renderer1);
			subplot1.setDomainGridlinesVisible(true);

			NumberAxis rangeAxis2 = new NumberAxis(barValueAxisLabel);
			rangeAxis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			BarRenderer renderer2 = new BarRenderer();
			renderer2.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());
			CategoryPlot subplot2 = new CategoryPlot(dataset2, null, rangeAxis2, renderer2);
			subplot2.setDomainGridlinesVisible(true);

			CategoryAxis domainAxis = new CategoryAxis(this.categoryAxisLabel);
			CombinedDomainCategoryPlot combinedPlot = new CombinedDomainCategoryPlot(domainAxis);
			combinedPlot.add(subplot1, 2);
			combinedPlot.add(subplot2, 1);
			JFreeChart chart = new JFreeChart(title, combinedPlot);
			chart.addSubtitle(this.subtitle);
			return chart;
		} else {
			NumberAxis rangeAxis1 = new NumberAxis(lineValueAxisLabel);
			rangeAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

			// LineAndShapeRenderer renderer1 = new LineAndShapeRenderer();
			// renderer1.setBaseToolTipGenerator(new
			// StandardCategoryToolTipGenerator());
			BarRenderer renderer1 = new BarRenderer();
			renderer1.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());

			CategoryPlot subplot1 = new CategoryPlot(dataset1, null, rangeAxis1, renderer1);
			subplot1.setDomainGridlinesVisible(true);

			// Line Chart
			dataset2 = barDataset;
			NumberAxis rangeAxis2 = new NumberAxis(barValueAxisLabel);
			rangeAxis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			BarRenderer renderer2 = new BarRenderer();
			renderer2.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());
			CategoryPlot subplot2 = new CategoryPlot(dataset2, null, rangeAxis2, renderer2);
			subplot2.setDomainGridlinesVisible(true);

			CategoryAxis domainAxis = new CategoryAxis(categoryAxisLabel);
			CombinedDomainCategoryPlot combinedPlot = new CombinedDomainCategoryPlot(domainAxis);
			combinedPlot.add(subplot1, 1);
			combinedPlot.add(subplot2, 1);

			JFreeChart chart = new JFreeChart(title, combinedPlot);
			chart.addSubtitle(this.subtitle);
			return chart;
		}
	}
}
