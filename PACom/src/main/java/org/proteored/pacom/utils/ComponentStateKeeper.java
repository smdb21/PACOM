package org.proteored.pacom.utils;

import java.util.Collection;
import java.util.Map;

import javax.swing.JComponent;

import gnu.trove.map.hash.THashMap;

public class ComponentStateKeeper {
	private final Map<JComponent, Boolean> booleanComponentStatus = new THashMap<JComponent, Boolean>();

	public ComponentStateKeeper(Collection<JComponent> components) {

	}
}
