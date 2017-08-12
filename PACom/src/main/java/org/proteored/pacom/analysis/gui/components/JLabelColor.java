package org.proteored.pacom.analysis.gui.components;

import java.awt.Color;
import java.util.Map;

import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.proteored.pacom.analysis.util.DoSomethingToChangeColorInChart;

public class JLabelColor extends JLabel implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6832215547971395376L;
	private final JColorChooser colorChooser;
	private final Map<String, Color> idSetsColors;
	private final String idSetName;
	private final String vennDataName;
	private final DoSomethingToChangeColorInChart methodToChangeColorInChart;

	public JLabelColor(String text, JColorChooser colorChooser, Map<String, Color> idSetsColors, String vennDataName,
			String idSetName, DoSomethingToChangeColorInChart methodToChangeColorInChart) {
		super(text);
		this.setOpaque(true);
		this.colorChooser = colorChooser;
		// add transparency to the color
		Color selectedColor = colorChooser.getSelectionModel().getSelectedColor();
		if (selectedColor != null && selectedColor.getAlpha() == 0) {
			int alpha = 127;
			Color newColor = new Color(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(),
					alpha);
			colorChooser.getSelectionModel().setSelectedColor(newColor);
		}

		this.colorChooser.getSelectionModel().addChangeListener(this);
		this.setToolTipText("Click here to change the color");
		this.idSetsColors = idSetsColors;
		this.vennDataName = vennDataName;
		this.idSetName = idSetName;
		this.methodToChangeColorInChart = methodToChangeColorInChart;
		setColorToLabel(getColorFromChooser());
	}

	private Color getColorFromChooser() {
		return colorChooser.getSelectionModel().getSelectedColor();
	}

	private void setColorToLabel(Color color) {

		this.setBackground(color);
		this.idSetsColors.put(idSetName, color);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Color color = getColorFromChooser();
		setColorToLabel(color);
		// frame.startShowingChart(this);
		methodToChangeColorInChart.doSomething(vennDataName, idSetName, color);
	}

	public JColorChooser getJColorChooser() {
		return this.colorChooser;
	}
}
