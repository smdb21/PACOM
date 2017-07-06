package org.proteored.pacom.utils;

import java.util.List;

import org.proteored.miapeapi.webservice.clients.miapeapi.IntegerString;

import gnu.trove.map.hash.TIntObjectHashMap;

public class Wrapper {

	/**
	 * This function translate the List of
	 * {@link org.proteored.miapeapi.util.IntegerString} to a {@link Map} where
	 * the keys are the {@link Integer}, and the values are the {@link String}
	 * 
	 * @param list
	 *            the list of {@link org.proteored.miapeapi.util.IntegerString}
	 * @return
	 */
	public static TIntObjectHashMap<String> getHashMap(List<IntegerString> list) {
		if (list == null || list.isEmpty())
			return new TIntObjectHashMap<String>();
		TIntObjectHashMap<String> ret = new TIntObjectHashMap<String>();
		for (IntegerString integerString : list) {
			ret.put(integerString.getMiapeID(), integerString.getMiapeType());
		}
		return ret;
	}
}
