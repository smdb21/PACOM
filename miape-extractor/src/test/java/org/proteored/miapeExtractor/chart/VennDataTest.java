package org.proteored.miapeExtractor.chart;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.proteored.miapeapi.experiment.VennData;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedPeptide;
import org.proteored.miapeapi.factories.msi.MiapeMSIDocumentFactory;

public class VennDataTest {

	@Test
	public void VenData_Strings_Test() {
		ExtendedIdentifiedPeptide peptide = new ExtendedIdentifiedPeptide(null,
				null, MiapeMSIDocumentFactory.createIdentifiedPeptideBuilder(
						"PEPTIDESEQUENCE").build(), null);
		List<Object> col1 = new ArrayList<Object>();
		col1.add("caca");
		col1.add("casa");
		col1.add("cama");
		col1.add("PATO");
		col1.add(1);
		col1.add(peptide);

		List<Object> col2 = new ArrayList<Object>();
		col2.add("caca");
		col2.add("casa");
		col2.add(peptide);
		col2.add(1);
		col2.add("camota");
		col2.add("PATO");

		List<Object> col3 = new ArrayList<Object>();
		col3.add(1);
		col3.add("caquita");
		col3.add("casita");
		col3.add(peptide);
		col3.add("camita");
		col3.add("PATO");
		col3.add(4.4);
		VennData venn = new VennData(col1, col2, col3, null, false);

		// Intersection
		Collection<Object> intersection = venn.getIntersection123();
		Assert.assertEquals(3, intersection.size());

		System.out.println("Intersection: " + intersection.size());
		for (Object object : intersection) {
			System.out.println(object);
			if (!("PATO".equals(object) || object.equals(1) || object
					.equals(peptide)))
				fail();
		}

		// Union
		Collection<Object> union = venn.getUnion123();
		System.out.println("Union: " + union.size());
		for (Object object : union) {
			System.out.println(object);
		}
		Assert.assertEquals(11, union.size());
	}

	@Test
	public void VenData_Null_Test() {
		ExtendedIdentifiedPeptide peptide = new ExtendedIdentifiedPeptide(null,
				null, MiapeMSIDocumentFactory.createIdentifiedPeptideBuilder(
						"PEPTIDESEQUENCE").build(), null);

		List<Object> col2 = new ArrayList<Object>();
		col2.add("caca");
		col2.add("casa");
		col2.add(peptide);
		col2.add(1);
		col2.add("camota");
		col2.add("PATO");

		List<Object> col3 = new ArrayList<Object>();
		col3.add(1);
		col3.add("caquita");
		col3.add("casita");
		col3.add(peptide);
		col3.add("camita");
		col3.add("PATO");
		col3.add(4.4);
		VennData venn = new VennData(null, col2, col3, null, false);

		// Intersection
		Collection<Object> intersection = venn.getIntersection123();
		Assert.assertEquals(3, intersection.size());

		System.out.println("Intersection: " + intersection.size());
		for (Object object : intersection) {
			System.out.println(object);
			if (!("PATO".equals(object) || object.equals(1) || object
					.equals(peptide)))
				fail();
		}

		// Union
		Collection<Object> union = venn.getUnion123();
		System.out.println("Union: " + union.size());
		for (Object object : union) {
			System.out.println(object);
		}
		Assert.assertEquals(10, union.size());
	}
}
