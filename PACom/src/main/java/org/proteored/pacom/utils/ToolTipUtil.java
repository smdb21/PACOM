package org.proteored.pacom.utils;

import java.awt.FontMetrics;

public class ToolTipUtil {
	public static String splitWordsInLines(String description, FontMetrics fontMetrics, int maxwidth) {
		StringBuilder sb = new StringBuilder();
		if (description.contains(" ")) {
			final String[] split = description.split(" ");
			StringBuilder sb2 = new StringBuilder();
			for (String string : split) {
				sb2.append(string).append(" ");
				if (fontMetrics.stringWidth(sb2.toString()) >= maxwidth) {
					sb.append(sb2).append("<br>");
					sb2 = new StringBuilder();
				}
			}
			sb.append(sb2);
		} else {
			sb.append(description);
		}
		return sb.toString();
	}
}
