package org.proteored.pacom.utils;

import java.util.HashMap;
import java.util.List;

import org.proteored.miapeapi.webservice.clients.miapeapi.IntegerString;

public class Wrapper {

	/**
	 * This function translate the List of
	 * {@link org.proteored.miapeapi.util.IntegerString} to a {@link HashMap}
	 * where the keys are the {@link Integer}, and the values are the
	 * {@link String}
	 * 
	 * @param list
	 *            the list of {@link org.proteored.miapeapi.util.IntegerString}
	 * @return
	 */
	public static HashMap<Integer, String> getHashMap(List<IntegerString> list) {
		if (list == null || list.isEmpty())
			return new HashMap<Integer, String>();
		HashMap<Integer, String> ret = new HashMap<Integer, String>();
		for (IntegerString integerString : list) {
			ret.put(integerString.getMiapeID(), integerString.getMiapeType());
		}
		return ret;
	}
}
