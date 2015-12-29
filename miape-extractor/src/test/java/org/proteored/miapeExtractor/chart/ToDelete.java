package org.proteored.miapeExtractor.chart;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class ToDelete {
	@Test
	public void testListas() {
		List<String> lista = new ArrayList<String>();
		Assert.assertTrue(lista.size() == 0);
		aniade(lista);
		Assert.assertTrue(lista.size() == 1);
		borra(lista);
		Assert.assertTrue(lista.size() == 0);
	}

	private void borra(List<String> lista) {
		lista.remove(0);

	}

	private void aniade(List<String> lista) {
		lista.add("hola");

	}

	@Test
	public void testHashMap() {
		HashMap<String, String> hasmap = new HashMap<String, String>();
		Assert.assertTrue(hasmap.size() == 0);
		aniade(hasmap);
		Assert.assertTrue(hasmap.size() == 1);
		borra(hasmap);
		Assert.assertTrue(hasmap.size() == 0);
	}

	private void borra(HashMap<String, String> hash) {
		hash.remove("hola");

	}

	private void aniade(HashMap<String, String> hash) {
		hash.put("hola", "pepe");

	}

	@Test
	public void testLinkedHashMap() {
		try {
			LinkedHashMap<String, String> hash = new LinkedHashMap<String, String>();
			hash.put("hola", "adios");
			hash.put("hola2", "adios2");
			System.out.println(hash.values());
			Assert.assertEquals(2, hash.size());
			hash.put("hola", "hola");
			System.out.println(hash.values());
			Assert.assertEquals(2, hash.size());
			System.out.println(hash.values());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testListas2() {
		List<String> lista1 = new ArrayList<String>();
		List<String> lista2 = new ArrayList<String>();

		lista1.add("hola lista1");
		lista1.add("adios lista1");

		System.out.println("Lista 1: " + lista1);

		lista2 = lista1;

		lista2.add("hola lista2");

		Assert.assertEquals(3, lista1.size());
		System.out.println("Lista 1: " + lista1);
		System.out.println("Lista 2: " + lista2);
	}

	@Test
	public void testListas3() {
		List<String> lista1 = new ArrayList<String>();
		List<String> lista2 = new ArrayList<String>();

		lista1.add("hola lista1");
		lista1.add("adios lista1");

		System.out.println("Lista 1: " + lista1);

		lista2.addAll(lista1);

		lista2.add("hola lista2");

		Assert.assertEquals(2, lista1.size());
		System.out.println("Lista 1: " + lista1);
		System.out.println("Lista 2: " + lista2);
	}

	@Test
	public void testListas5() {
		List<String> lista1 = new ArrayList<String>();
		List<String> lista2 = new ArrayList<String>();

		lista1.add("hola lista1");
		lista1.add("adios lista1");

		System.out.println("Lista 1: " + lista1);

		lista2.addAll(lista1);

		lista2.add("hola lista2");

		Assert.assertEquals(2, lista1.size());
		Assert.assertEquals(3, lista2.size());
		System.out.println("Lista 1: " + lista1);
		System.out.println("Lista 2: " + lista2);

		lista1.remove(0);
		Assert.assertEquals(1, lista1.size());
		Assert.assertEquals(3, lista2.size());
		System.out.println("Lista 1: " + lista1);
		System.out.println("Lista 2: " + lista2);
	}

	@Test
	public void testListas4() {
		List<String> lista1 = new ArrayList<String>();

		lista1.add("hola lista1");

		listaMethod(lista1, lista1);

	}

	private void listaMethod(List<String> lista1, List<String> lista2) {
		lista1.add("adios");
		Assert.assertEquals(2, lista1.size());
		Assert.assertEquals(2, lista2.size());
		System.out.println("Lista 1: " + lista1);
		System.out.println("Lista 2: " + lista2);

	}

}
