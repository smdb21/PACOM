package org.proteored.miapeExtractor.cv;

import org.junit.Test;
import org.proteored.miapeapi.spring.SpringHandler;

public class OntologyManagerTest {

	@Test
	public void testOBO() {
		SpringHandler.getInstance("miape-extractor-spring.xml").getCVManager();
	}
}
