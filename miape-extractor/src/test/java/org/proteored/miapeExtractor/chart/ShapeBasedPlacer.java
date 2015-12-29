package org.proteored.miapeExtractor.chart;

import processing.core.PApplet;
import wordcram.WordCram;

public class ShapeBasedPlacer extends PApplet {

	public static void main(String[] args) {
		ShapeBasedPlacer shape = new ShapeBasedPlacer();
		shape.size(700, 700, PDF, "wordcram.pdf");
		shape.background(255);

	}

	public ShapeBasedPlacer() {
		new WordCram(this)
				.fromTextFile(
						"C:\\Users\\Salva\\workspace\\WordCram\\cheatsheet.txt")
				.angledAt(0).withWordPadding(1).drawAll();

	}

}
