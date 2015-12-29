package org.proteored.miapeExtractor.miapeRetriever;

import org.junit.Test;
import org.proteored.miapeExtractor.analysis.gui.tasks.MiapeRetrieverManager;

public class MiapeRetrieverTest {

	@Test
	public void miapeMSIRetriever() {
		MiapeRetrieverManager.getInstance("smartinez", "natjeija").addRetrieving(4109, "MSI", null);
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

			}
		}
	}
}
