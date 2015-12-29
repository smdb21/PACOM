package org.proteored.miapeExtractor.analysis.charts;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

public class HeatMapChart {
	private static final int SIZE_MARGINS = 60;
	private static final Logger log = Logger
			.getLogger("log4j.logger.org.proteored");
	private static final double MIN_CELL_WIDTH = 30;
	private static final double HEIGHT_PERCENTAJE_SCREEN = 0.9;
	private final HeatChart chart;
	private List<String> rowList = new ArrayList<String>();
	private List<String> columnList = new ArrayList<String>();
	private final int numRows;
	private final int numColumns;
	private static final int screenHeight = Toolkit.getDefaultToolkit()
			.getScreenSize().height;
	private final JPanel jPanel = new JPanel();

	/**
	 * 
	 * @param title
	 * @param dataset
	 * @param rowList
	 *            list of labels for the rows
	 * @param columnList
	 *            list of labels for the columns
	 */
	public HeatMapChart(String title, double[][] dataset, List<String> rowList,
			List<String> columnList, double colorScale) {
		this.rowList = rowList;
		this.columnList = columnList;
		this.numColumns = this.columnList.size();
		this.numRows = this.rowList.size();
		this.chart = new HeatChart(dataset);
		this.chart.setXValues(getColumnList());
		this.chart.setYValues(getRowList());

		if (chart != null) {
			this.chart.setTitle(title);
			this.chart.setChartMargin(2);
			this.chart.setHighValueColour(Color.RED);
			this.chart.setLowValueColour(Color.GREEN);
			this.chart.setXValuesHorizontal(false);
			this.chart.setColourScale(colorScale);

			// Fit to screen if greater than screen
			// if (isGreaterThanScreen()) {
			// fitToScreen(HEIGHT_PERCENTAJE_SCREEN);
			// }

			// Add picture to the panel
			addPicture();

		}

	}

	public void setHighValueColor(Color color) {
		if (this.chart != null)
			this.chart.setHighValueColour(color);
		addPicture();
	}

	public void setLowValueColor(Color color) {
		if (this.chart != null)
			this.chart.setLowValueColour(color);
		addPicture();
	}

	private void addPicture() {
		Image image = this.chart.getChartImage();
		// printSize("chart", this.chart.getChartSize());
		ImageIcon imageIcon = new ImageIcon(image);

		JLabel label = new JLabel(imageIcon);
		// printSize("label", label.getSize());
		// Rule rule = new Rule(0, false);
		// Set up the picture
		// ScrollablePicture picture = new ScrollablePicture(imageIcon,
		// rule.getIncrement());

		// Set up the scroll pane.
		// add(picture);

		// final Long round1 = Math.round(this.chart.getChartSize().getWidth());
		// final Long round2 =
		// Math.round(this.chart.getChartSize().getHeight());
		// this.pictureScrollPane.setPreferredSize(new
		// Dimension(Integer.valueOf(round1.toString())
		// + this.SIZE_MARGINS, Integer.valueOf(round2.toString()) +
		// this.SIZE_MARGINS));
		// jPanel.setViewportView(label);
		jPanel.removeAll();
		jPanel.add(label);

		// jPanel.setViewportBorder(BorderFactory.createLineBorder(Color.black));
		// printSize("panel", jPanel.getSize());

	}

	private void printSize(String string, Dimension size) {
		System.out.println(string + " (" + size.getHeight() + " , "
				+ size.getWidth() + ")");

	}

	public String saveImage(File outputFile) throws IOException {

		this.chart.saveToFile(outputFile);
		return outputFile.getAbsolutePath();
	}

	/**
	 * Reduce the chart to the percentage of the size of the screen
	 * 
	 * @param percentage
	 */
	public void fitToScreen(double percentage) {
		if (isGreaterThanScreen()) {
			final Dimension screenSize = Toolkit.getDefaultToolkit()
					.getScreenSize();
			final Dimension chartSize = this.chart.getChartSize();
			// width
			if (chartSize.getWidth() > screenSize.getWidth()) {
				// fit to the 90% of the screen width
				Long t = Math.round(screenSize.getWidth() * percentage
						/ numColumns);
				log.info("Resizing the chart to the screen width ("
						+ screenSize.getWidth() + ")");
				// chartSize.setSize(chartSize.getWidth(),
				// screenSize.getHeight());
				this.chart.setCellWidth(Integer.valueOf(t.toString()));
			}

			// heigth
			if (chartSize.getHeight() > screenSize.getHeight()) {
				// fit to the 90% of the screen height
				Long t = Math.round(screenSize.getHeight() * percentage
						/ numRows);
				if (t == 0)
					t = t + 1;
				log.info("Resizing the chart to the screen height ("
						+ screenSize.getHeight() + ")");
				// chartSize.setSize(chartSize.getWidth(),
				// screenSize.getHeight());
				this.chart.setCellHeight(Integer.valueOf(t.toString()));
			}
			final Dimension cellSize = chart.getCellSize();
			if (cellSize.getWidth() < MIN_CELL_WIDTH) {
				log.info("Resizing cells width to " + MIN_CELL_WIDTH);
				cellSize.setSize(MIN_CELL_WIDTH, cellSize.getHeight());
			}

			// resize the panel accordingly
			jPanel.setSize(new Dimension(chart.getChartWidth(), chart
					.getChartHeight()));
		}
	}

	/**
	 * @return the jPanel
	 */
	public JPanel getjPanel() {
		return jPanel;
	}

	private boolean isGreaterThanScreen() {
		final Dimension screenSize = Toolkit.getDefaultToolkit()
				.getScreenSize();
		final Dimension chartSize = this.chart.getChartSize();
		if (chartSize.getWidth() > screenSize.getWidth())
			return true;
		if (chartSize.getHeight() > screenSize.getHeight())
			return true;
		return false;
	}

	private Object[] getRowList() {
		return rowList.toArray();
	}

	private Object[] getColumnList() {
		return columnList.toArray();

	}

}
