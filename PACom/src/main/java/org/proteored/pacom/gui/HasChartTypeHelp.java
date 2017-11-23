package org.proteored.pacom.gui;

import java.util.List;

public interface HasChartTypeHelp {

	public void showAttachedHelpDialog(List<String> chartTypes);

	public AttachedChartTypesHelpDialog getChartTypesHelpDialog();
}
