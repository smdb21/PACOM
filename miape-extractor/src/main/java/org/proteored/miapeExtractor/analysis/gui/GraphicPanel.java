package org.proteored.miapeExtractor.analysis.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class GraphicPanel extends JPanel {
	private JComponent graphicPanel;

	public GraphicPanel() {

	}

	public GraphicPanel(JPanel graphicPanel) {
		this.graphicPanel = graphicPanel;
		add(this.graphicPanel);
	}

	public void setGraphicPanel(JComponent graphicPanel) {
		this.removeAll();
		this.graphicPanel = graphicPanel;

		final GridLayout gridLayout = new java.awt.GridLayout(1, 1);
		// GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		// gridBagConstraints.fill = GridBagConstraints.BOTH;
		// gridBagConstraints.gridx = 0;
		// gridBagConstraints.gridy = 0;
		this.setLayout(gridLayout);
		this.add(graphicPanel);
		this.revalidate();
		this.repaint();
	}

	public void addGraphicPanel(List<JPanel> chartList) {
		this.removeAll();
		// GridLayout gridLayoutManager = null;
		final int size = chartList.size();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		// gridLayoutManager = new GridLayout(size, 1);
		// if (size == 1 || size == 2) {
		// gridLayoutManager = new GridLayout(1, size);
		// } else if (size == 3 || size == 4) {
		// gridLayoutManager = new GridLayout(2, 2);
		// } else if (size == 5 || size == 6) {
		// gridLayoutManager = new GridLayout(3, 2);
		// } else if (size == 7 || size == 8 || size == 9) {
		// gridLayoutManager = new GridLayout(3, 3);
		// } else if (size == 10 || size == 11 || size == 12) {
		// gridLayoutManager = new GridLayout(4, 3);
		// } else if (size == 13 || size == 14 || size == 15 || size == 16) {
		// gridLayoutManager = new GridLayout(4, 4);
		// } else if (size >= 17 && size <= 20) {
		// gridLayoutManager = new GridLayout(5, 4);
		// } else if (size >= 21 && size <= 25) {
		// gridLayoutManager = new GridLayout(5, 5);
		// } else if (size > 25) {
		// gridLayoutManager = new GridLayout();
		// }

		// this.setLayout(gridLayoutManager);
		this.setLayout(new GridBagLayout());

		for (JPanel panel : chartList) {
			this.add(panel, c);
			c.gridy++;
			// this.add(panel);
		}

	}
}
