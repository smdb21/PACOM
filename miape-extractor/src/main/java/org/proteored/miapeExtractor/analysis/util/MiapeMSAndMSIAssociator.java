package org.proteored.miapeExtractor.analysis.util;

import java.util.HashMap;
import java.util.Map;

import org.proteored.miapeExtractor.gui.MainFrame;
import org.proteored.miapeExtractor.gui.tasks.WebservicesLoaderTask;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.webservice.clients.miapeextractor.MiapeExtractorDelegate;

public class MiapeMSAndMSIAssociator {
	private static MiapeMSAndMSIAssociator instance;
	// key = miape MSI identifier
	// value = miape MS identier
	private static Map<Integer, Integer> map = new HashMap<Integer, Integer>();
	private static MiapeExtractorDelegate webservice;
	private static String userName;
	private static String pass;

	public static MiapeMSAndMSIAssociator getInstance() {
		if (instance == null)
			instance = new MiapeMSAndMSIAssociator();

		if (MiapeMSAndMSIAssociator.webservice == null) {
			try {

				MiapeMSAndMSIAssociator.webservice = WebservicesLoaderTask
						.getInstace().getMiapeExtractorWebservice(false);
			} catch (IllegalMiapeArgumentException ex) {

			}
		}

		userName = MainFrame.userName;
		pass = MainFrame.password;
		return instance;
	}

	public int getAssociatedMIAPEMS(int miapeMSIID) {
		if (map.containsKey(miapeMSIID))
			return map.get(miapeMSIID);
		else {
			if (webservice != null) {
				int miapeRef = webservice.getAssociatedMiapeMS(miapeMSIID,
						userName, pass);
				if (miapeRef > 0) {
					map.put(miapeMSIID, miapeRef);
					return miapeRef;
				}
			}
		}
		return -1;
	}

	public void associate(int miapeMSId, int miapeMSIId) {
		if (map.containsKey(miapeMSIId)) {
			map.remove(miapeMSIId);
		}
		map.put(miapeMSIId, miapeMSId);

	}
}
