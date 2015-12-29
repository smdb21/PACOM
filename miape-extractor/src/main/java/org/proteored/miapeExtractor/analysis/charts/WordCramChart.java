package org.proteored.miapeExtractor.analysis.charts;

/*
 * Copyright 2010 Daniel Bernier Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedProtein;
import org.proteored.miapeapi.experiment.model.IdentificationSet;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import wordcram.Anglers;
import wordcram.Colorers;
import wordcram.Placers;
import wordcram.Sizers;
import wordcram.SpiralWordNudger;
import wordcram.Word;
import wordcram.WordCounter;
import wordcram.WordCram;

public class WordCramChart extends PApplet {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private WordCram wordcram;
	private final Word[] words;
	private static List<String> wordsToSkip = new ArrayList<String>();
	private int minWordLength = 4;
	private JLabel jLabelSelectedWord;
	private static String noGoodFontNames = "Dingbats|Standard Symbols L";
	private static String blockFontNames = "OpenSymbol|Mallige Bold|Mallige Normal|Lohit Bengali|Lohit Punjabi|Webdings";
	public static final String RANDOM_FONT = "Random font";
	private final HashMap<String, List<ExtendedIdentifiedProtein>> wordMapping = new HashMap<String, List<ExtendedIdentifiedProtein>>();
	private final boolean mapWordsAndProteins = true;

	private JTextArea jTextAreaSelectedProteins;

	private final JPanel panel = new JPanel();

	private GridBagConstraints c;

	private String selectedFont;

	private int maximumNumberOfWords;

	static {

		// this.prefixesToSkip.add("ensp");
		// this.prefixesToSkip.add("ensg");
		wordsToSkip.add("human");
		wordsToSkip.add("chromosome");
		wordsToSkip.add("os");
		wordsToSkip.add("sv");
		wordsToSkip.add("homo");
		wordsToSkip.add("sapiens");
		wordsToSkip.add("gene");
		wordsToSkip.add("gn");
		wordsToSkip.add("pe");
		wordsToSkip.add("isoform");
		wordsToSkip.add("member");
		wordsToSkip.add("protein");
		wordsToSkip.add("decoy");
		// this.charsToReplace.add(",");
		// this.charsToReplace.add("\\");
	}

	// public WordCramChart(IdentificationSet identificationSet) {
	// final List<ExtendedIdentifiedProtein> identifiedProteins =
	// identificationSet
	// .getIdentifiedProteins();
	// log.info(identifiedProteins.size() + " proteins");
	// this.sentences = new ArrayList<Word>();
	// this.initializeSkips();
	// WordCounter wc = new WordCounter();
	// for (ExtendedIdentifiedProtein protein : identifiedProteins) {
	// if (!protein.isDecoy()) {
	// String description = protein.getDescription().toLowerCase();
	//
	// wc.shouldExcludeNumbers(true);
	// final String[] split = description.split(" ");
	// for (String string : split) {
	// String[] split2 = new String[1];
	// split2[0] = string;
	// if (string.contains("=")) {
	// split2 = string.split("=");
	// }
	// for (String string2 : split2) {
	//
	// if (string2.length() >= this.minWordLength) {
	// boolean print = true;
	// for (String prefix : this.prefixesToSkip) {
	// if (string2.startsWith(prefix)) {
	// print = false;
	// break;
	// }
	// }
	// for (String charsToReplace : this.charsToReplace) {
	// if (string2.contains(charsToReplace)) {
	// string2 = string2.replace(charsToReplace,
	// "");
	// if (string2.length() < this.minWordLength) {
	// print = false;
	// break;
	// }
	// }
	// }
	// if (this.wordsToSkip.contains(string2)) {
	// print = false;
	// }
	//
	// if (print) {
	// if (string2.contains("humankeratin")
	// || "kr".equals(string2))
	// System.out.println("asdf");
	// sentences.add(string2);
	// }
	// }
	// }
	// }
	// }
	// }
	// log.info(sentences.size() + " words");
	// this.array = this.sentences.toArray(new String[0]);
	// this.init();
	// }
	public WordCramChart(IdentificationSet identificationSet, List<String> skipWords, int minWordLength) {
		final List<ExtendedIdentifiedProtein> identifiedProteins = identificationSet.getIdentifiedProteins();
		log.info(identifiedProteins.size() + " proteins");

		StringBuilder skipWordsBuffer = new StringBuilder();
		if (skipWords != null) {
			for (String word : skipWords) {
				if (!"".equals(skipWordsBuffer.toString()))
					skipWordsBuffer.append(" ");
				skipWordsBuffer.append(word);
			}
		}
		WordCounter wc = new WordCounter(minWordLength).withExtraStopWords(skipWordsBuffer.toString());
		wc.shouldExcludeNumbers(false);
		StringBuilder sb2 = new StringBuilder();
		for (ExtendedIdentifiedProtein protein : identifiedProteins) {
			if (!protein.isDecoy()) {
				String description = protein.getDescription();
				if (description != null) {
					description = description.toLowerCase();
					sb2.append(" ");
					sb2.append(description);
					if (mapWordsAndProteins) {
						final Word[] proteinWords = wc.count(description);
						for (Word word : proteinWords) {
							if (!wordMapping.containsKey(word.word)) {
								List<ExtendedIdentifiedProtein> proteinList = new ArrayList<ExtendedIdentifiedProtein>();
								proteinList.add(protein);
								wordMapping.put(word.word, proteinList);
							} else {
								wordMapping.get(word.word).add(protein);
							}
						}
					}
				}
			}
		}
		words = wc.count(sb2.toString());

		log.info(words.length + " words");
		log.info(wordMapping.size() + " words mapped to proteins");

		// this.panel.add(papplet, c);
		init();
	}

	public static List<String> getDefaultSkippedWords() {
		return wordsToSkip;
	}

	public static String[] getFonts() {
		List<String> fonts = new ArrayList<String>();
		fonts.add(RANDOM_FONT);
		final String[] list = PFont.list();
		for (String fontName : list) {
			if (blockFontNames.contains(fontName) || noGoodFontNames.contains(fontName))
				continue;
			fonts.add(fontName);
		}

		return fonts.toArray(new String[0]);
	}

	public List<ExtendedIdentifiedProtein> getProteinsByWord(String word) {
		return wordMapping.get(word);
	}

	public WordCramChart selectedWordLabel(JLabel label) {
		jLabelSelectedWord = label;
		return this;
	}

	public WordCramChart selectedProteinsLabel(JTextArea label) {
		jTextAreaSelectedProteins = label;

		return this;
	}

	public WordCramChart font(String fontName) {
		if (!RANDOM_FONT.equals(fontName))
			selectedFont = fontName;
		return this;
	}

	public WordCramChart maximumNumberOfWords(int number) {
		maximumNumberOfWords = number;
		return this;
	}

	public WordCramChart minWordLength(int minLength) {
		minWordLength = minLength;
		return this;
	}

	@Override
	public void setup() {

		// destination.image.getGraphics():
		// P2D -> sun.awt.image.ToolkitImage, JAVA2D ->
		// java.awt.image.BufferedImage.

		// parent.getGraphics():
		// P2D -> sun.java2d.SunGraphics2D, JAVA2D -> same thing.

		// P2D can't draw to destination.image.getGraphics(). Interesting.

		size(550, 550); // (int)random(300, 800)); //1200, 675);
						// //1600,
		// 900);
		smooth();
		colorMode(PConstants.HSB);
		initWordCram();
		// frameRate(1);
	}

	private PFont randomFont() {
		String[] fonts = PFont.list();

		Set<String> noGoodFonts = new HashSet<String>(
				Arrays.asList((noGoodFontNames + "|" + blockFontNames).split("|")));
		String fontName;
		do {
			fontName = fonts[(int) random(fonts.length)];
		} while (fontName == null || noGoodFonts.contains(fontName));
		log.info("Using font: " + fontName);
		return createFont(fontName, 1);
		// return createFont("Molengo", 1);
	}

	// PGraphics pg;

	private void initWordCram() {
		// background(100);

		// pg = createGraphics(800, 600, JAVA2D);
		// pg.beginDraw();

		wordcram = new WordCram(this)
				// .withCustomCanvas(pg)
				// .fromWebPage("http://proteored.org")
				// .fromTextFile(textFilePath())
				// .fromTextFile(getPruebaFile())

		.fromWords(words)
				// .fromTextString(array)
				// .fromTextString("asdf", "asdfk23", "·ASDF", "asdfasdfo", ".")
				// "stop")
				// .fromWords(alphabet())
				// .upperCase()
				.includeNumbers()
				// .excludeNumbers()
				.withFonts(getSelectedFont()).withColorer(Colorers.twoHuesRandomSats(this))
				// .withColorer(Colorers.complement(this, random(255), 200,
				// 220))

		.withAngler(Anglers.mostlyHoriz()).withPlacer(Placers.swirl())
				// .withPlacer(Placers.centerClump())
				.withSizer(Sizers.byWeight(1, 100)).withWordPadding(1).sizedByWeight(5, 100)

		.minShapeSize(1)
				// .MaxAttemptsForPlacement(10)
				.maxNumberOfWordsToDraw(getMaximumNumberOfWords()).withNudger(new SpiralWordNudger())
				// .withNudger(
				// new PlottingWordNudger(this, new SpiralWordNudger()))
				// .withNudger(new RandomWordNudger())

		;
	}

	private int getMaximumNumberOfWords() {
		if (maximumNumberOfWords > 0)
			return maximumNumberOfWords + 1;
		return 500;
	}

	private PFont getSelectedFont() {
		if (selectedFont != null) {
			final PFont createFont = createFont(selectedFont, 1);
			return createFont;
		}
		return randomFont();
	}

	private void finishUp() {
		// pg.endDraw();
		// image(pg, 0, 0);

		// println(wordcram.getSkippedWords());
		log.info("FINISH");
		// println("Done");
		// save("wordcram.png");
		noLoop();
	}

	@Override
	public void draw() {
		// fill(55);
		// rect(0, 0, width, height);

		boolean allAtOnce = false;
		if (allAtOnce) {
			log.info("Drawing words...");
			wordcram.drawAll();
			finishUp();
		} else {
			int wordsPerFrame = 1;
			while (wordcram.hasMore() && wordsPerFrame-- > 0) {
				wordcram.drawNext();
			}

			if (!wordcram.hasMore()) {
				finishUp();
			}
		}
	}

	@Override
	public void mouseMoved() {

		Word word = wordcram.getWordAt(mouseX, mouseY);
		if (word != null) {
			log.info(round(mouseX) + "," + round(mouseY) + " -> " + word.word);
			if (jLabelSelectedWord != null)
				jLabelSelectedWord.setText(word.word);
			if (jTextAreaSelectedProteins != null) {
				if (wordMapping.containsKey(word.word)) {
					final List<ExtendedIdentifiedProtein> proteinList = wordMapping.get(word.word);
					if (proteinList != null && !proteinList.isEmpty()) {
						final List<ExtendedIdentifiedProtein> nonRedundantProteinList = new ArrayList<ExtendedIdentifiedProtein>();
						Set<String> accs = new HashSet<String>();
						for (ExtendedIdentifiedProtein extendedIdentifiedProtein : proteinList) {
							if (!accs.contains(extendedIdentifiedProtein.getAccession())) {
								nonRedundantProteinList.add(extendedIdentifiedProtein);
								accs.add(extendedIdentifiedProtein.getAccession());
							}
						}
						StringBuilder sb = new StringBuilder();
						sb.append(proteinList.size() + " proteins containing: " + word.word + "\n");
						for (ExtendedIdentifiedProtein extendedIdentifiedProtein : nonRedundantProteinList) {
							if (!"".equals(sb.toString()))
								sb.append("\n");
							// sb.append("<b>"
							// + extendedIdentifiedProtein.getAccession()
							// + "</b>: "
							// + extendedIdentifiedProtein
							// .getDescription());
							String description = extendedIdentifiedProtein.getDescription();
							if (description.contains("\\")) {
								final String[] split = description.split("\\\\");
								description = split[0];
							}
							if (description.contains("OS")) {
								final String[] split = description.split("OS");
								description = split[0];
							}
							sb.append(extendedIdentifiedProtein.getAccession() + ": " + description);
						}

						jTextAreaSelectedProteins.setText(sb.toString() + "\n");
						jTextAreaSelectedProteins.setEnabled(true);
					}
				}
			}
		}

	}

	@Override
	public void mouseClicked() {
		log.info("Mouse clicked");
		initWordCram();
		loop();
	}

	@Override
	public void keyPressed() {
		if (keyCode == ' ') {
			log.info("Space pressed");
			saveFrame("wordcram-##.png");
		}
	}

	private String textFilePath() {
		boolean linux = false;
		String projDir = linux ? "/home/dan/projects/" : "C:/Users/Salva/Dropbox/Tesis/TESIS/version_unida/";
		String path = projDir + "tesis.txt";
		return path;
	}

	private String getPruebaFile() {
		return "C:\\Users\\Salva\\Desktop\\Jurkat_RIPA.txt";
	}

	private Word[] alphabet() {
		Word[] w = new Word[26];
		for (int i = 0; i < w.length; i++) {
			w[i] = new Word(new String(new char[] { (char) (i + 65) }), 26 - i);
		}
		return w;
	}
}
