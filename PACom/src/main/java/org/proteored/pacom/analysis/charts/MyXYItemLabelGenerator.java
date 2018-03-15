package org.proteored.pacom.analysis.charts;

import java.util.Map;

import org.jfree.chart.labels.AbstractXYItemLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

public class MyXYItemLabelGenerator extends AbstractXYItemLabelGenerator implements XYToolTipGenerator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2595987548882955976L;
	private final Map<String, String> dotsIDsByKey;

	public MyXYItemLabelGenerator(Map<String, String> dotsIDsByKey) {
		this.dotsIDsByKey = dotsIDsByKey;
	}

	@Override
	protected Object[] createItemArray(XYDataset dataset, int series, int item) {
		Object[] ret = super.createItemArray(dataset, series, item);
		String key = getKey(series, item);
		if (dotsIDsByKey.containsKey(key)) {
			String dotID = dotsIDsByKey.get(key);
			ret[0] = ret[0] + "<br>" + dotID;
		}
		return ret;
	}

	@Override
	public String generateLabelString(XYDataset dataset, int series, int item) {

		Object[] items = createItemArray(dataset, series, item);
		StringBuilder sb = new StringBuilder();

		sb.append("<html>");
		for (Object object : items) {
			sb.append(object).append("<br>");
		}
		sb.append("</html>");

		return sb.toString();
	}

	@Override
	public String generateToolTip(XYDataset dataset, int series, int item) {
		return generateLabelString(dataset, series, item);
	}

	public static String getKey(int series, int item) {
		return String.valueOf(series) + item;
	}
}
