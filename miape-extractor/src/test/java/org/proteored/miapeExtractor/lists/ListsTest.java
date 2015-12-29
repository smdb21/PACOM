package org.proteored.miapeExtractor.lists;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ListsTest {

	@Test
	public void testLists() {
		List<Persona> list1 = new ArrayList<Persona>();
		for (int i = 0; i < 10; i++) {
			list1.add(new Persona("nombre" + i, i));
		}
		List<Persona> list2 = new ArrayList<Persona>();
		list2.addAll(list1);

		list2.add(new Persona("23423", 99));
		list2.add(list1.get(0));
		// list2.set(1, "HOLA");
		// list2.get(0).setEdad(9999);
		list1.get(0).setNombre("PEPE");
		printList(list1);
		printList(list2);
	}

	private void printList(List<Persona> list) {
		for (Persona string : list) {
			System.out.print(string + ", ");
		}
		System.out.println();
	}

	public class Persona {
		private String nombre;
		private int edad;

		public Persona(String nom, int ed) {
			this.edad = ed;
			this.nombre = nom;
		}

		public String getNombre() {
			return nombre;
		}

		public int getEdad() {
			return edad;
		}

		public void setNombre(String nombre) {
			this.nombre = nombre;
		}

		public void setEdad(int edad) {
			this.edad = edad;
		}

		@Override
		public String toString() {
			return "[" + nombre + ", " + edad + "]";
		}

	}
}
