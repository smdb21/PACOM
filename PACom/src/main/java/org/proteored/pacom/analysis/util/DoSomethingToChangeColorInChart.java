package org.proteored.pacom.analysis.util;

import java.awt.Color;

import edu.scripps.yates.utilities.util.DoSomethingWith3Arguments;

public interface DoSomethingToChangeColorInChart extends DoSomethingWith3Arguments<Void, String, String, Color> {
	@Override
	public Void doSomething(String experimentName, String idSetName, Color color);
}
