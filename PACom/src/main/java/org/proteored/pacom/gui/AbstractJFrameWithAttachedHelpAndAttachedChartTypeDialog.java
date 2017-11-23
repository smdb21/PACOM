package org.proteored.pacom.gui;

import java.util.ArrayList;
import java.util.List;

import org.proteored.pacom.analysis.gui.ChartManagerFrame;
import org.proteored.pacom.analysis.gui.ChartType;

public abstract class AbstractJFrameWithAttachedHelpAndAttachedChartTypeDialog
		extends AbstractJFrameWithAttachedHelpDialog implements HasChartTypeHelp {
	private AttachedChartTypesHelpDialog chartHelp;
	/**
	 * 
	 */
	private static final long serialVersionUID = 8313252409686378398L;

	public AbstractJFrameWithAttachedHelpAndAttachedChartTypeDialog(int maxWidth) {
		super(maxWidth);

	}

	@Override
	public void showAttachedHelpDialog(List<String> chartTypeNames) {
		final AttachedChartTypesHelpDialog chartTypesHelpDialog = getChartTypesHelpDialog();
		List<ChartType> chartTypes = new ArrayList<ChartType>();
		for (String chartTypeName : chartTypeNames) {
			if (chartTypeName.equals(ChartManagerFrame.MENU_SEPARATION)) {
				continue;
			}
			final ChartType chartType = ChartType.getFromName(chartTypeName);
			if (chartType != null) {
				chartTypes.add(chartType);
			}
		}
		chartTypesHelpDialog.loadChartTypes(chartTypes);
		chartTypesHelpDialog.forceVisible();
	}

	@Override
	public AttachedChartTypesHelpDialog getChartTypesHelpDialog() {
		if (chartHelp == null) {
			chartHelp = new AttachedChartTypesHelpDialog(this, this.maxWidth);
		}
		return chartHelp;
	}

}
