package org.proteored.pacom.analysis.charts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.jfree.chart.encoders.ImageFormat;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.VennData;
import org.proteored.miapeapi.experiment.VennDataForPeptides;
import org.proteored.miapeapi.experiment.VennDataForProteins;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.ProteinGroup;
import org.proteored.miapeapi.experiment.model.sort.ProteinGroupComparisonType;
import org.proteored.pacom.analysis.util.ImageUtils;

import edu.scripps.yates.utilities.colors.ColorGenerator;

public class VennChart {
	private static final Logger log = Logger.getLogger("log4j.logger.org.proteored");
	private Image image;
	private final JPanel chartPanel = new JPanel();
	private Integer intersection12 = null;
	private Integer intersection13 = null;
	private Integer intersection123 = null;
	private Integer intersection23 = null;
	private String name1;
	private String name2;
	private String name3;

	// public static enum ProteinSelection {
	// ALL_PROTEINS, FIRST_PROTEIN, BEST_PROTEIN, SHARE_ONE_PROTEIN
	// };

	private final org.proteored.miapeapi.experiment.VennData vennData;
	private Color color1 = ColorGenerator.hex2Rgb("FF6342");
	private Color color2 = ColorGenerator.hex2Rgb("ADDE63");
	private Color color3 = ColorGenerator.hex2Rgb("63C6DE");
	private String title;
	private String label1;
	private String label2;
	private String label3;
	private final String originalTitle;

	public VennChart(String title, IdentificationSet idset1, String label1, IdentificationSet idset2, String label2,
			IdentificationSet idset3, String label3, IdentificationItemEnum plotItem, Boolean distModPep,
			ProteinGroupComparisonType proteinGroupComparisonType, Color color1, Color color2, Color color3) {
		this.originalTitle = title;
		if (title != null)
			this.title = title.replace(" ", "%20");
		if (label1 != null)
			this.label1 = label1.replace(" ", "%20");
		if (label2 != null)
			this.label2 = label2.replace(" ", "%20");
		if (label3 != null)
			this.label3 = label3.replace(" ", "%20");
		if (idset1 != null)
			name1 = idset1.getFullName();
		if (idset2 != null)
			name2 = idset2.getFullName();
		if (idset3 != null)
			name3 = idset3.getFullName();
		setColor1(color1);
		setColor2(color2);
		setColor3(color3);
		// experiment set
		if (plotItem.equals(IdentificationItemEnum.PROTEIN)) {

			Collection proteinGroupOccurrenceList1 = null;
			Collection proteinGroupOccurrenceList2 = null;
			Collection proteinGroupOccurrenceList3 = null;
			if (idset1 != null)
				proteinGroupOccurrenceList1 = idset1.getProteinGroupOccurrenceList().values();
			if (idset2 != null)
				proteinGroupOccurrenceList2 = idset2.getProteinGroupOccurrenceList().values();
			if (idset3 != null)
				proteinGroupOccurrenceList3 = idset3.getProteinGroupOccurrenceList().values();
			this.vennData = new VennDataForProteins(proteinGroupOccurrenceList1, proteinGroupOccurrenceList2,
					proteinGroupOccurrenceList3, proteinGroupComparisonType);

			try {
				updateChart();
				return;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new IllegalMiapeArgumentException(e.getMessage());
			}

		} else {
			Collection pepList1 = null;
			Collection pepList2 = null;
			Collection pepList3 = null;
			if (idset1 != null)
				pepList1 = idset1.getPeptideOccurrenceList(distModPep).values();// getPeptideHash(idset1,
			if (idset2 != null) // distModPep);
				pepList2 = idset2.getPeptideOccurrenceList(distModPep).values();// getPeptideHash(idset2,
			if (idset3 != null) // distModPep);
				pepList3 = idset3.getPeptideOccurrenceList(distModPep).values();// getPeptideHash(idset3,
			// distModPep);
			this.vennData = new VennDataForPeptides(pepList1, pepList2, pepList3);
			try {
				updateChart();
				return;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new IllegalMiapeArgumentException(e.getMessage());
			}
		}
	}

	public void updateChart() throws MalformedURLException {
		URL url = createChartURL(title, label1, label2, label3);
		addPicture(url);
	}

	public Collection<Object> getJustIn1() {
		return this.vennData.getUniqueTo1();
	}

	public Collection<Object> getJustIn2() {
		return this.vennData.getUniqueTo2();
	}

	public Collection<Object> getJustIn3() {
		return this.vennData.getUniqueTo3();
	}

	/**
	 * @return the panel
	 */
	public JPanel getChartPanel() {
		return chartPanel;
	}

	private URL createChartURL(String title, String label1, String label2, String label3) throws MalformedURLException {
		StringBuilder sb = new StringBuilder();
		sb.append("http://chart.apis.google.com/chart?chs=547x547");
		sb.append("&chd=t:" + getDataString(vennData));
		sb.append("&cht=v");

		sb.append("&chdl=");
		final String listString1 = getListString(label1, vennData.getSize1());
		final String listString2 = getListString(label2, vennData.getSize2());
		final String listString3 = getListString(label3, vennData.getSize3());
		sb.append(listString1);
		if (!"".equals(listString1) && !"".equals(listString2))
			sb.append("|");

		sb.append(listString2);
		if (!"".equals(listString2) && !"".equals(listString3))
			sb.append("|");

		sb.append(listString3);

		// sb.append("&chdl=" + label1 + "(" + this.list1.size() + ")|" + label2
		// + "("
		// + this.list2.size() + ")|" + label3 + "(" + this.list3.size() + ")");
		sb.append("&chtt=" + title);
		sb.append("&chds=0," + vennData.getMaxCollectionSize());
		sb.append("&chdlp=b&chma=|10,10");
		// colors:
		sb.append("&chco=");
		if (label1 != null) {
			sb.append(getRGBHEX(getColor1()));
		}
		if (label2 != null) {
			if (label1 != null) {
				sb.append(",");
			}
			sb.append(getRGBHEX(getColor2()));
		}
		if (label3 != null) {
			if (label2 != null || label3 != null) {
				sb.append(",");
			}
			sb.append(getRGBHEX(getColor3()));
		}
		log.info("URL created=" + sb.toString());
		return new URL(sb.toString());
		// return new URL(
		// "https://chart.googleapis.com/chart?chs=400x300&chd=t:100,80,60,30,30,30,10"
		// +
		// "&cht=v&chl=Hello|World|asdf&chtt=Titulo");
		// }
	}

	private String getRGBHEX(Color color) {

		int transparency = color.getAlpha();
		String format = String.format("%02x", transparency);
		String replace = ColorGenerator.getHexString(color).replace("#", "");
		String string = replace + format;
		return string;
	}

	private Color getColor1() {
		return color1;
	}

	private Color getColor2() {
		return color2;
	}

	private Color getColor3() {
		return color3;
	}

	private String getListString(String label, Integer size) {
		if (size != null)
			return label + "(" + size + ")";
		return "";
	}

	private String getDataString(VennData vennData) {
		StringBuilder sb = new StringBuilder();
		// The first three values specify the sizes of three circles: A, B, and
		// C. For a chart with
		// only two circles, specify zero for the third value.

		Integer size1 = vennData.getSize1();
		Integer size2 = vennData.getSize2();
		Integer size3 = vennData.getSize3();
		if (size1 != null) {
			sb.append(size1);
		} else {
			sb.append(0);
		}
		sb.append(",");
		if (size2 != null) {
			sb.append(size2);
		} else {
			sb.append(0);
		}
		sb.append(",");
		if (size3 != null) {
			sb.append(size3);
		} else {
			sb.append(0);
		}

		// The fourth value specifies the size of the intersection of A and B.
		if (size1 != null && size2 != null) {
			this.intersection12 = vennData.getIntersection12Size();
			sb.append("," + intersection12);
		}
		// The fifth value specifies the size of the intersection of A and C.
		// For a chart with only
		// two circles, do not specify a value here.
		if (size1 != null && size3 != null) {
			this.intersection13 = vennData.getIntersection13Size();
			sb.append("," + intersection13);
		}
		// The sixth value specifies the size of the intersection of B and C.
		// For a chart with only
		// two circles, do not specify a value here.
		if (size2 != null && size3 != null) {
			this.intersection23 = vennData.getIntersection23Size();
			sb.append("," + intersection23);
		}

		// The seventh value specifies the size of the common intersection of A,
		// B, and C. For a
		// chart with only two circles, do not specify a value here.
		this.intersection123 = vennData.getIntersection123Size();
		if (this.intersection123 > 0)
			sb.append("," + intersection123);

		return sb.toString();
	}

	// private int getIntersection(List<ProteinGroup> list1,
	// List<ProteinGroup> list2, List<ProteinGroup> list3) {
	// int count = 0;
	// if (list1 == null || list2 == null || list3 == null)
	// return 0;
	// for (ProteinGroup group1 : list1) {
	// for (ProteinGroup group2 : list2) {
	// if (shareOneProtein(group1, group2)) {
	// boolean thereisitnerseccionwithgroup1 = false;
	// boolean thereisitnerseccionwithgroup2 = false;
	// for (ProteinGroup group3 : list3) {
	// if (!thereisitnerseccionwithgroup1
	// && shareOneProtein(group1, group3))
	// thereisitnerseccionwithgroup1 = true;
	// if (!thereisitnerseccionwithgroup2
	// && shareOneProtein(group2, group3))
	// thereisitnerseccionwithgroup2 = true;
	// if (thereisitnerseccionwithgroup1
	// && thereisitnerseccionwithgroup2) {
	// count++;
	// break;
	// }
	// }
	// if (thereisitnerseccionwithgroup1
	// && thereisitnerseccionwithgroup2)
	// break;
	// }
	// }
	// }
	// return count;
	// }

	// private int getIntersection(List<ProteinGroup> list1,
	// List<ProteinGroup> list2) {
	// int count = 0;
	// if (list1 == null || list2 == null)
	// return 0;
	// for (ProteinGroup group1 : list1) {
	// for (ProteinGroup group2 : list2) {
	// if (shareOneProtein(group1, group2)) {
	// count++;
	// break;
	// }
	// }
	// }
	// return count;
	// }

	private boolean shareOneProtein(ProteinGroup group1, ProteinGroup group2) {
		List<String> proteins1 = group1.getAccessions();
		List<String> proteins2 = group2.getAccessions();
		for (String acc1 : proteins1) {
			if (proteins2.contains(acc1))
				return true;
		}
		return false;
	}

	private boolean shareAllProteins(ProteinGroup group1, ProteinGroup group2) {
		List<String> proteins1 = group1.getAccessions();
		List<String> proteins2 = group2.getAccessions();
		for (String acc1 : proteins1) {
			if (!proteins2.contains(acc1))
				return false;
		}
		return true;
	}

	private int getIntersection(Set<String> list1, Set<String> list2) {
		int count = 0;
		if (list1 == null || list2 == null)
			return 0;
		for (String key : list1) {
			if (list2.contains(key))
				count++;
		}
		return count;
	}

	private void addPicture(URL url) {
		this.image = getImageFromURL(url);

		String imageDescription = "Venn diagram";
		ImageIcon imageIcon = new ImageIcon(image, imageDescription);
		// Rule rule = new Rule(0,false);
		// Set up the picture
		// ScrollablePicture picture = new ScrollablePicture(imageIcon,
		// rule.getIncrement());
		// Set up the scroll pane.
		// this.pictureScrollPane = new JScrollPane(picture);
		// this.pictureScrollPane.setPreferredSize(new Dimension(500, 500));
		// this.pictureScrollPane.setViewportBorder(BorderFactory.createLineBorder(Color.black));
		this.chartPanel.removeAll();
		this.chartPanel.setLayout(new BorderLayout());
		ImageLabel label = new ImageLabel("", originalTitle, true);
		label.setIcon(imageIcon);
		this.chartPanel.add(label, BorderLayout.CENTER);
		Container parent = chartPanel.getParent();
		while (!(parent instanceof Window)) {
			if (parent == null) {
				break;
			}
			parent = parent.getParent();
		}
		if (parent != null) {
			((Window) parent).pack();
		}
	}

	private Image getImageFromURL(URL url) {
		try {
			image = Toolkit.getDefaultToolkit().createImage(url);
		} catch (SecurityException e) {
			throw new IllegalMiapeArgumentException(e.getMessage());
		}
		if (image == null)
			throw new IllegalMiapeArgumentException(
					"It is not possible to reach the URL: " + url + ". Check the internet connection.");
		return image;
	}

	public String saveImage(File outputFile) throws IOException {
		this.saveToFile(outputFile);
		return outputFile.getAbsolutePath();
	}

	private void saveGraphicByFormat(BufferedImage chart, File outputFile, float quality, String format)
			throws IOException {
		// Setup correct compression for jpeg.
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(format);
		ImageWriter writer = iter.next();
		ImageWriteParam iwp = writer.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(quality);

		// Output the image.
		FileImageOutputStream output = new FileImageOutputStream(outputFile);
		writer.setOutput(output);
		IIOImage image = new IIOImage(chart, null, null);
		writer.write(null, image, iwp);
		writer.dispose();

	}

	/**
	 * Generates a new chart <code>Image</code> based upon the currently held
	 * settings and then attempts to save that image to disk, to the location
	 * provided as a File parameter. The image type of the saved file will equal
	 * the extension of the filename provided, so it is essential that a
	 * suitable extension be included on the file name.
	 * 
	 * <p>
	 * All supported <code>ImageIO</code> file types are supported, including
	 * PNG, JPG and GIF.
	 * 
	 * <p>
	 * No chart will be generated until this or the related
	 * <code>getChartImage()</code> method are called. All successive calls will
	 * result in the generation of a new chart image, no caching is used.
	 * 
	 * @param outputFile
	 *            the file location that the generated image file should be
	 *            written to. The File must have a suitable filename, with an
	 *            extension of a valid image format (as supported by
	 *            <code>ImageIO</code>).
	 * @throws IOException
	 *             if the output file's filename has no extension or if there
	 *             the file is unable to written to. Reasons for this include a
	 *             non-existant file location (check with the File exists()
	 *             method on the parent directory), or the permissions of the
	 *             write location may be incorrect.
	 */
	private void saveToFile(File outputFile) throws IOException {
		String filename = outputFile.getName();

		int extPoint = filename.lastIndexOf('.');

		if (extPoint < 0) {
			throw new IOException("Illegal filename, no extension used.");
		}

		// Determine the extension of the filename.
		String ext = filename.substring(extPoint + 1);

		// Handle jpg without transparency.

		if (ext.toLowerCase().equals(ImageFormat.GIF) || ext.toLowerCase().equals(ImageFormat.JPEG)
				|| ext.equals(ImageFormat.PNG)) {
			BufferedImage bufferedImage = ImageUtils.toBufferedImage(this.image);
			// BufferedImage chart = (BufferedImage) getChartImage(false);

			// Save our graphic.
			saveGraphicByFormat(bufferedImage, outputFile, 1.0f, ext);
		} else {
			BufferedImage bufferedImage = ImageUtils.toBufferedImage(this.image);
			// BufferedImage chart = (BufferedImage) getChartImage(true);

			ImageIO.write(bufferedImage, ext, outputFile);
		}
	}

	public String getIntersectionsText(String experiment) {
		StringBuilder sb = new StringBuilder();
		DecimalFormat df = new DecimalFormat("#.#");

		if (experiment != null)
			sb.append("<b>" + experiment + "</b><br>");
		int union = this.vennData.getUnion123Size();

		if (name1 != null)
			sb.append("<br> A -> " + name1 + " = " + this.vennData.getSize1() + " ("
					+ df.format(Double.valueOf(this.vennData.getSize1() * 100.0 / union)) + "% of union)");
		if (name2 != null)
			sb.append("<br> B -> " + name2 + " = " + this.vennData.getSize2() + " ("
					+ df.format(Double.valueOf(this.vennData.getSize2() * 100.0 / union)) + "% of union)");
		if (name3 != null)
			sb.append("<br> C -> " + name3 + " = " + this.vennData.getSize3() + " ("
					+ df.format(Double.valueOf(this.vennData.getSize3() * 100.0 / union)) + "% of union)");
		sb.append("<br>");
		sb.append("<br>Union=" + union + " (100%)");

		if (name1 != null && name2 != null) {
			if (!"".equals(sb.toString()))
				sb.append("<br>");
			Double intersection12Percentage = 0.0;
			if (this.vennData.getUnion12Size() > 0) {
				intersection12Percentage = intersection12 * 100.0 / this.vennData.getUnion12Size();
			}
			sb.append("Overlap (A,B) = " + intersection12 + " (" + df.format(intersection12Percentage) + "% of union)");
		}
		if (name1 != null && name3 != null) {
			if (!"".equals(sb.toString()))
				sb.append("<br>");
			double intersection13Percentage = 0.0;
			if (vennData.getUnion13Size() > 0) {
				intersection13Percentage = intersection13 * 100.0 / this.vennData.getUnion13Size();
			}
			sb.append("Overlap (A,C) = " + intersection13 + " (" + df.format(intersection13Percentage) + "% of union)");
		}
		if (name3 != null && name2 != null) {
			if (!"".equals(sb.toString()))
				sb.append("<br>");
			double intersection23Percentage = 0.0;
			if (vennData.getUnion23Size() > 0) {
				intersection23Percentage = intersection23 * 100.0 / this.vennData.getUnion23Size();
			}
			sb.append("Overlap (B,C) = " + intersection23 + " (" + df.format(intersection23Percentage) + "% of union)");
		}

		if (name1 != null && name2 != null && name3 != null) {
			if (!"".equals(sb.toString()))
				sb.append("<br>");
			sb.append("Overlap (A,B,C) = " + intersection123 + " (" + df.format(intersection123 * 100.0 / union)
					+ "% of union)");
		}
		if (name1 != null) {
			if (!"".equals(sb.toString()))
				sb.append("<br>");
			Double just1 = this.vennData.getUniqueTo1Num() * 100.0 / union;
			int overlappedTo1 = this.vennData.getIntersection12Size() + this.vennData.getIntersection13Size()
					- this.vennData.getIntersection123Size();
			double overlappedTo1Percentage = 0.0;
			if (this.vennData.getSize1() > 0) {
				overlappedTo1Percentage = overlappedTo1 * 100.0 / this.vennData.getSize1();
			}
			sb.append("Just in A = " + this.vennData.getUniqueTo1Num() + " (" + df.format(just1) + "% of union) ("
					+ df.format(overlappedTo1Percentage) + "% overlapped)");
		}
		if (name2 != null) {
			if (!"".equals(sb.toString()))
				sb.append("<br>");
			Double just2 = this.vennData.getUniqueTo2Num() * 100.0 / union;
			int overlappedTo2 = this.vennData.getIntersection12Size() + this.vennData.getIntersection23Size()
					- this.vennData.getIntersection123Size();
			double overlappedTo2Percentage = 0.0;
			if (this.vennData.getSize2() > 0) {
				overlappedTo2Percentage = overlappedTo2 * 100.0 / this.vennData.getSize2();
			}
			sb.append("Just in B = " + this.vennData.getUniqueTo2Num() + " (" + df.format(just2) + "% of union) ("
					+ df.format(overlappedTo2Percentage) + "% overlapped)");
		}
		if (name3 != null)

		{
			if (!"".equals(sb.toString()))
				sb.append("<br>");
			Double just3 = this.vennData.getUniqueTo3Num() * 100.0 / union;
			int overlappedTo3 = this.vennData.getIntersection13Size() + this.vennData.getIntersection23Size()
					- this.vennData.getIntersection123Size();
			double overlappedTo3Percentage = 0.0;
			if (this.vennData.getSize3() > 0) {
				overlappedTo3Percentage = overlappedTo3 * 100.0 / this.vennData.getSize3();
			}
			sb.append("Just in C = " + this.vennData.getUniqueTo3Num() + " (" + df.format(just3) + "% of union) ("
					+ df.format(overlappedTo3Percentage) + "% overlapped)");
		}
		sb.append("<br><br>");

		return sb.toString();
	}

	/**
	 * @return
	 */
	public VennData getVennData() {
		return this.vennData;
	}

	public void setColor1(Color color1) {
		if (color1 != null)
			this.color1 = color1;
	}

	public void setColor2(Color color2) {
		if (color2 != null)
			this.color2 = color2;
	}

	public void setColor3(Color color3) {
		if (color3 != null)
			this.color3 = color3;
	}

	public void setColorToSeries(String seriesName, Color color) {
		if (this.name1 != null && name1.equals(seriesName)) {
			setColor1(color);
		}
		if (this.name2 != null && name2.equals(seriesName)) {
			setColor2(color);
		}
		if (this.name3 != null && name3.equals(seriesName)) {
			setColor3(color);
		}
	}

	public String getOriginalTitle() {
		return originalTitle;
	}
}
