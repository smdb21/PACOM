package org.proteored.miapeExtractor.miapeLinks;

import java.net.URL;

import org.junit.Test;
import org.proteored.miapeExtractor.utils.HttpUtilities;
import org.proteored.miapeapi.util.MiapeReportsLinkGenerator;

public class MiapeLinksTest {

	@Test
	public void createLinkFromMIAPE() {

		URL url = MiapeReportsLinkGenerator.getMiapePublicLink(15, 4994, "MSI");
		HttpUtilities.openURL(url.toString());
	}
}
