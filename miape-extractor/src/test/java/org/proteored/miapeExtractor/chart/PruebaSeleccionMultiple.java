package org.proteored.miapeExtractor.chart;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class PruebaSeleccionMultiple extends JFrame {
	private JList listaColores, listaCopia;
	private JButton botonCopiar;
	private final String nombresColores[] = { "Negro", "Azul", "Cyan", "Gris oscuro", "Gris",
			"Verde", "Gris claro", "Magenta", "Naranja", "Rosa", "Rojo", "Blanco", "Amarillo" };

	// configurar GUI
	public PruebaSeleccionMultiple() {
		super("Listas de selección múltiple");

		// obtener panel de contenido y establecer su esquema
		Container contenedor = getContentPane();
		contenedor.setLayout(new FlowLayout());

		// establecer objeto JList listaColores
		listaColores = new JList(nombresColores);
		listaColores.setVisibleRowCount(5);
		listaColores.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		contenedor.add(new JScrollPane(listaColores));

		// crear botón copiar y registrar su componente de escucha
		botonCopiar = new JButton("Copiar >>>");
		botonCopiar.addActionListener(

		new ActionListener() { // clase interna anónima

					// manejar evento de botón
					public void actionPerformed(ActionEvent evento) {
						// colocar valores seleccionados en listaCopia
						listaCopia.setListData(listaColores.getSelectedValues());
					}

				} // fin de clase interna anónima

				); // fin de la llamada a addActionListener

		contenedor.add(botonCopiar);

		// establecer objeto JList listaCopia
		listaCopia = new JList();
		listaCopia.setVisibleRowCount(5);
		listaCopia.setFixedCellWidth(100);
		listaCopia.setFixedCellHeight(15);
		listaCopia.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		contenedor.add(new JScrollPane(listaCopia));

		setSize(325, 130);
		setVisible(true);

	} // fin del constructor PruebaSeleccionMultiple

	public static void main(String args[]) {
		PruebaSeleccionMultiple aplicacion = new PruebaSeleccionMultiple();
		aplicacion.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

} // fin de la clase PruebaSeleccionMultiple