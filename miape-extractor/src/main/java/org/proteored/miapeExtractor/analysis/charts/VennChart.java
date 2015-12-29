package org.proteored.miapeExtractor.analysis.charts;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.proteored.miapeExtractor.analysis.util.ImageUtils;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.miapeapi.experiment.VennData;
import org.proteored.miapeapi.experiment.model.ExtendedIdentifiedProtein;
import org.proteored.miapeapi.experiment.model.IdentificationItemEnum;
import org.proteored.miapeapi.experiment.model.IdentificationSet;
import org.proteored.miapeapi.experiment.model.ProteinGroup;
import org.proteored.miapeapi.experiment.model.ProteinGroupOccurrence;
import org.proteored.miapeapi.xml.util.ProteinGroupComparisonType;

public class VennChart {
	private static final Logger log = Logger
			.getLogger("log4j.logger.org.proteored");
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

	public VennChart(String title, IdentificationSet idset1, String label1,
			IdentificationSet idset2, String label2, IdentificationSet idset3,
			String label3, IdentificationItemEnum plotItem, Boolean distModPep,
			Boolean countNonConclusiveProteins,
			ProteinGroupComparisonType proteinGroupComparisonType) {

		if (title != null)
			title = title.replace(" ", "%20");
		if (label1 != null)
			label1 = label1.replace(" ", "%20");
		if (label2 != null)
			label2 = label2.replace(" ", "%20");
		if (label3 != null)
			label3 = label3.replace(" ", "%20");
		if (idset1 != null)
			name1 = idset1.getFullName();
		if (idset2 != null)
			name2 = idset2.getFullName();
		if (idset3 != null)
			name3 = idset3.getFullName();

		Set<String> list1 = new HashSet<String>();
		Set<String> list2 = new HashSet<String>();
		Set<String> list3 = new HashSet<String>();

		// experiment set
		if (plotItem.equals(IdentificationItemEnum.PROTEIN)) {

			Collection proteinGroupOccurrenceList1 = null;
			Collection proteinGroupOccurrenceList2 = null;
			Collection proteinGroupOccurrenceList3 = null;
			if (idset1 != null)
				proteinGroupOccurrenceList1 = idset1
						.getProteinGroupOccurrenceList().values();
			if (idset2 != null)
				proteinGroupOccurrenceList2 = idset2
						.getProteinGroupOccurrenceList().values();
			if (idset3 != null)
				proteinGroupOccurrenceList3 = idset3
						.getProteinGroupOccurrenceList().values();
			this.vennData = new VennData(proteinGroupOccurrenceList1,
					proteinGroupOccurrenceList2, proteinGroupOccurrenceList3,
					proteinGroupComparisonType, countNonConclusiveProteins);
			URL url;
			try {
				url = createChartURL(title, label1, label2, label3);
				addPicture(url);
				return;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new IllegalMiapeArgumentException(e.getMessage());
			}

		} else {
			if (idset1 != null)
				list1 = idset1.getPeptideOccurrenceList(distModPep).keySet();// getPeptideHash(idset1,
			if (idset2 != null) // distModPep);
				list2 = idset2.getPeptideOccurrenceList(distModPep).keySet();// getPeptideHash(idset2,
			if (idset3 != null) // distModPep);
				list3 = idset3.getPeptideOccurrenceList(distModPep).keySet();// getPeptideHash(idset3,
			// distModPep);
			this.vennData = new VennData(list1, list2, list3, null,
					countNonConclusiveProteins);
			URL url;
			try {
				// url = createChartURL(title, label1, list1, label2, list2,
				// label3, list3);
				url = createChartURL(title, label1, label2, label3);
				addPicture(url);
				return;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new IllegalMiapeArgumentException(e.getMessage());
			}
		}
	}

	private Comparator getShareOneProteinComparator() {
		return new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1 instanceof ProteinGroup && o2 instanceof ProteinGroup)
					if (shareOneProtein((ProteinGroup) o2, (ProteinGroup) o2))
						return 0;
					else
						return 1;
				return 0;
			}
		};
	}

	private Comparator getShareBestProteinComparator() {
		return new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				ProteinGroup pg1 = null;
				ProteinGroup pg2 = null;
				if (o1 instanceof ProteinGroupOccurrence
						&& o2 instanceof ProteinGroupOccurrence) {
					pg1 = ((ProteinGroupOccurrence) o1).getFirstOccurrence();
					pg2 = ((ProteinGroupOccurrence) o2).getFirstOccurrence();
				}
				if (o1 instanceof ProteinGroup && o2 instanceof ProteinGroup) {
					pg1 = (ProteinGroup) o1;
					pg2 = (ProteinGroup) o2;
				}
				if (pg1 != null && pg2 != null) {
					ExtendedIdentifiedProtein bestProtein1 = pg1
							.getBestProtein();
					ExtendedIdentifiedProtein bestProtein2 = pg2
							.getBestProtein();
					if (bestProtein1.equals(bestProtein2))
						return 0;
					else
						return 1;
				}

				return 0;
			}

		};
	}

	private Comparator getFirstProteinComparator() {
		return new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				ProteinGroup pg1 = null;
				ProteinGroup pg2 = null;
				if (o1 instanceof ProteinGroupOccurrence
						&& o2 instanceof ProteinGroupOccurrence) {
					pg1 = ((ProteinGroupOccurrence) o1).getFirstOccurrence();
					pg2 = ((ProteinGroupOccurrence) o2).getFirstOccurrence();
				}
				if (o1 instanceof ProteinGroup && o2 instanceof ProteinGroup) {
					pg1 = (ProteinGroup) o1;
					pg2 = (ProteinGroup) o2;
				}
				if (pg1 != null && pg2 != null)
					if (pg1.getAccessions().get(0)
							.equals(pg2.getAccessions().get(0)))
						return 0;
					else
						return 1;

				return 0;
			}

		};
	}

	private Comparator getShareAllProteinsComparator() {
		return new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				ProteinGroup pg1 = null;
				ProteinGroup pg2 = null;

				if (o1 instanceof ProteinGroup && o2 instanceof ProteinGroup) {
					pg1 = (ProteinGroup) o1;
					pg2 = (ProteinGroup) o2;
				}
				if (o1 instanceof ProteinGroupOccurrence
						&& o2 instanceof ProteinGroupOccurrence) {
					pg1 = ((ProteinGroupOccurrence) o1).getFirstOccurrence();
					pg2 = ((ProteinGroupOccurrence) o2).getFirstOccurrence();
				}
				if (pg1 != null && pg2 != null)
					if (shareOneProtein(pg1, pg2))
						return 0;
					else
						return 1;
				return 0;
			}

		};
	}

	/**
	 * @return the panel
	 */
	public JPanel getChartPanel() {
		return chartPanel;
	}

	// private HashMap<String, T> getPeptideHash(IdentificationSetGrouping
	// idset1,
	// Boolean distModPep) {
	//
	// HashMap<String, T> ret = new HashMap<String, T>();
	// if (idset1 != null) {
	// List<IdentificationOccurrence<ExtendedIdentifiedPeptide>>
	// identifiedPeptides = idset1
	// .getPeptideOccurrenceList(distModPep);
	// for (IdentificationOccurrence<ExtendedIdentifiedPeptide> object :
	// identifiedPeptides) {
	// IdentificationOccurrence<ExtendedIdentifiedPeptide> peptideOc =
	// (IdentificationOccurrence<ExtendedIdentifiedPeptide>) object;
	// ExtendedIdentifiedPeptide peptide =
	// peptideOc.getIdentificationItemList().get(0);
	// if (!ret.containsKey(peptide.getKey(distModPep)))
	// ret.put(peptide.getKey(distModPep), (T) peptide);
	// }
	// }
	// return ret;
	//
	// }

	// private HashMap<String, T> getProteinHash(IdentificationSetGrouping
	// idset1) {
	//
	// HashMap<String, T> ret = new HashMap<String, T>();
	// if (idset1 != null) {
	// final List<IdentificationOccurrence<IdentifiedProtein>>
	// identifiedProteins = idset1
	// .getProteinOccurrenceList();
	// for (IdentificationOccurrence<IdentifiedProtein> object :
	// identifiedProteins) {
	// IdentificationOccurrence<IdentifiedProtein> proteinOc =
	// (IdentificationOccurrence<IdentifiedProtein>) object;
	// IdentifiedProtein protein = proteinOc.getIdentificationItemList().get(0);
	// if (!ret.containsKey(protein.getAccession()))
	// ret.put(protein.getAccession(), (T) protein);
	// }
	// }
	// return ret;
	//
	// }

	// private URL createChartURL(String title, String label1, Set<String>
	// list1,
	// String label2, Set<String> list2, String label3, Set<String> list3)
	// throws MalformedURLException {
	// StringBuilder sb = new StringBuilder();
	// sb.append("http://chart.apis.google.com/chart?chs="
	// + ChartProperties.DEFAULT_CHART_WIDTH + "x"
	// + ChartProperties.DEFAULT_CHART_HEIGHT);
	// sb.append("&chd=t:" + getDataString(list1, list2, list3));
	// sb.append("&cht=v");
	// getListString(label1, list1);
	//
	// sb.append("&chdl=");
	// final String listString1 = getListString(label1, list1);
	// final String listString2 = getListString(label2, list2);
	// final String listString3 = getListString(label3, list3);
	// sb.append(listString1);
	// if (!"".equals(listString1) && !"".equals(listString2))
	// sb.append("|");
	//
	// sb.append(listString2);
	// if (!"".equals(listString2) && !"".equals(listString3))
	// sb.append("|");
	//
	// sb.append(listString3);
	//
	// // sb.append("&chdl=" + label1 + "(" + this.list1.size() + ")|" + label2
	// // + "("
	// // + this.list2.size() + ")|" + label3 + "(" + this.list3.size() + ")");
	// sb.append("&chtt=" + title);
	// sb.append("&chds=0," + max(list1, list2, list3));
	// sb.append("&chdlp=b&chma=|10,10");
	// log.info("URL created=" + sb.toString());
	// return new URL(sb.toString());
	// // return new URL(
	// //
	// "https://chart.googleapis.com/chart?chs=400x300&chd=t:100,80,60,30,30,30,10"
	// // +
	// // "&cht=v&chl=Hello|World|asdf&chtt=Titulo");
	// // }
	// }

	private URL createChartURL(String title, String label1, String label2,
			String label3) throws MalformedURLException {
		StringBuilder sb = new StringBuilder();
		sb.append("http://chart.apis.google.com/chart?chs="
				+ ChartProperties.DEFAULT_CHART_WIDTH + "x"
				+ ChartProperties.DEFAULT_CHART_HEIGHT);
		sb.append("&chd=t:" + getDataString(vennData));
		sb.append("&cht=v");

		sb.append("&chdl=");
		final String listString1 = getListString(label1,
				vennData.getCollection1());
		final String listString2 = getListString(label2,
				vennData.getCollection2());
		final String listString3 = getListString(label3,
				vennData.getCollection3());
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
		sb.append("&chds=0," + vennData.getMaxCollection().size());
		sb.append("&chdlp=b&chma=|10,10");
		log.info("URL created=" + sb.toString());
		return new URL(sb.toString());
		// return new URL(
		// "https://chart.googleapis.com/chart?chs=400x300&chd=t:100,80,60,30,30,30,10"
		// +
		// "&cht=v&chl=Hello|World|asdf&chtt=Titulo");
		// }
	}

	private String getListString(String label, Collection list) {
		if (list != null && !list.isEmpty())
			return label + "(" + list.size() + ")";
		return "";
	}

	private int max(Set<String> list1, Set<String> list2, Set<String> list3) {
		int maximum = 0;
		int int1 = 0;
		int int2 = 0;
		int int3 = 0;
		if (list1 != null)
			int1 = list1.size();
		if (list2 != null)
			int2 = list2.size();
		if (list3 != null)
			int3 = list3.size();

		maximum = Math.max(int1, int2);
		maximum = Math.max(maximum, int3);

		return maximum;
	}

	// private String getDataString(Set<String> list1, Set<String> list2,
	// Set<String> list3) {
	// StringBuilder sb = new StringBuilder();
	// // The first three values specify the sizes of three circles: A, B, and
	// // C. For a chart with
	// // only two circles, specify zero for the third value.
	// int size1 = 0;
	// if (list1 != null)
	// size1 = list1.size();
	// int size2 = 0;
	// if (list2 != null)
	// size2 = list2.size();
	// int size3 = 0;
	// if (list3 != null)
	// size3 = list3.size();
	// sb.append(size1 + "," + size2 + "," + size3);
	// // The fourth value specifies the size of the intersection of A and B.
	// this.intersection12 = VenData.getIntersection(list1, list2, null)
	// .size();
	// sb.append("," + intersection12);
	// // The fifth value specifies the size of the intersection of A and C.
	// // For a chart with only
	// // two circles, do not specify a value here.
	// if (list3 != null) {
	// this.intersection13 = VenData.getIntersection(list1, list3, null)
	// .size();
	// sb.append("," + intersection13);
	// }
	// // The sixth value specifies the size of the intersection of B and C.
	// // For a chart with only
	// // two circles, do not specify a value here.
	// if (list3 != null) {
	// this.intersection23 = getIntersection(list2, list3);
	// sb.append("," + intersection23);
	// }
	// // The seventh value specifies the size of the common intersection of A,
	// // B, and C. For a
	// // chart with only two circles, do not specify a value here.
	// if (list1 != null && list2 != null && list3 != null) {
	// this.intersection123 = VenData.getIntersection(list1, list2, list3)
	// .size();
	// sb.append("," + intersection123);
	// }
	// return sb.toString();
	// }

	private String getDataString(VennData vennData) {
		StringBuilder sb = new StringBuilder();
		// The first three values specify the sizes of three circles: A, B, and
		// C. For a chart with
		// only two circles, specify zero for the third value.

		int size1 = vennData.getSize1();
		int size2 = vennData.getSize2();
		int size3 = vennData.getSize3();
		sb.append(size1 + "," + size2 + "," + size3);
		// The fourth value specifies the size of the intersection of A and B.
		this.intersection12 = vennData.getIntersection12().size();
		sb.append("," + intersection12);
		// The fifth value specifies the size of the intersection of A and C.
		// For a chart with only
		// two circles, do not specify a value here.
		this.intersection13 = vennData.getIntersection13().size();
		sb.append("," + intersection13);
		// The sixth value specifies the size of the intersection of B and C.
		// For a chart with only
		// two circles, do not specify a value here.

		this.intersection23 = vennData.getIntersection23().size();
		sb.append("," + intersection23);

		// The seventh value specifies the size of the common intersection of A,
		// B, and C. For a
		// chart with only two circles, do not specify a value here.
		this.intersection123 = vennData.getIntersection123().size();
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

		ImageIcon imageIcon = new ImageIcon(image);
		// Rule rule = new Rule(0,false);
		// Set up the picture
		// ScrollablePicture picture = new ScrollablePicture(imageIcon,
		// rule.getIncrement());
		// Set up the scroll pane.
		// this.pictureScrollPane = new JScrollPane(picture);
		// this.pictureScrollPane.setPreferredSize(new Dimension(500, 500));
		// this.pictureScrollPane.setViewportBorder(BorderFactory.createLineBorder(Color.black));

		JLabel label = new JLabel();
		label.setIcon(imageIcon);
		this.chartPanel.add(label);
	}

	private Image getImageFromURL(URL url) {
		try {
			image = java.awt.Toolkit.getDefaultToolkit().getDefaultToolkit()
					.createImage(url);
		} catch (SecurityException e) {
			throw new IllegalMiapeArgumentException(e.getMessage());
		}
		if (image == null)
			throw new IllegalMiapeArgumentException(
					"It is not possible to reach the URL: " + url
							+ ". Check the internet connection.");
		return image;
	}

	public String saveImage(File outputFile) throws IOException {
		this.saveToFile(outputFile);
		return outputFile.getAbsolutePath();
	}

	private void saveGraphicJpeg(BufferedImage chart, File outputFile,
			float quality) throws IOException {
		// Setup correct compression for jpeg.
		Iterator<ImageWriter> iter = ImageIO
				.getImageWritersByFormatName("jpeg");
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
		if (ext.toLowerCase().equals("jpg") || ext.toLowerCase().equals("jpeg")) {
			BufferedImage chart = ImageUtils.toBufferedImage(this.image);
			// BufferedImage chart = (BufferedImage) getChartImage(false);

			// Save our graphic.
			saveGraphicJpeg(chart, outputFile, 1.0f);
		} else {
			BufferedImage chart = ImageUtils.toBufferedImage(this.image);
			// BufferedImage chart = (BufferedImage) getChartImage(true);

			ImageIO.write(chart, ext, outputFile);
		}
	}

	public String getIntersectionsText(String experiment) {
		StringBuilder sb = new StringBuilder();
		DecimalFormat df = new DecimalFormat("#.#");

		if (experiment != null)
			sb.append("<b>" + experiment + "</b><br>");
		int union = this.vennData.getUnion123().size();

		if (name1 != null)
			sb.append("<br> 1 -> "
					+ name1
					+ " = "
					+ this.vennData.getSize1()
					+ " ("
					+ df.format(Double.valueOf(this.vennData.getSize1() * 100.0
							/ union)) + "% of union)");
		if (name2 != null)
			sb.append("<br> 2 -> "
					+ name2
					+ " = "
					+ this.vennData.getSize2()
					+ " ("
					+ df.format(Double.valueOf(this.vennData.getSize2() * 100.0
							/ union)) + "% of union)");
		if (name3 != null)
			sb.append("<br> 3 -> "
					+ name3
					+ " = "
					+ this.vennData.getSize3()
					+ " ("
					+ df.format(Double.valueOf(this.vennData.getSize3() * 100.0
							/ union)) + "% of union)");
		sb.append("<br>");
		sb.append("<br>Union=" + union + " (100%)");

		if (name1 != null && name2 != null) {
			if (!"".equals(sb.toString()))
				sb.append("<br>");
			sb.append("Overlap (1,2) = "
					+ intersection12
					+ " ("
					+ df.format(Double.valueOf(intersection12 * 100.0
							/ this.vennData.getUnion12().size()))
					+ "% of union)");
		}
		if (name1 != null && name3 != null) {
			if (!"".equals(sb.toString()))
				sb.append("<br>");
			sb.append("Overlap (1,3) = "
					+ intersection13
					+ " ("
					+ df.format(Double.valueOf(intersection13 * 100.0
							/ this.vennData.getUnion13().size()))
					+ "% of union)");
		}
		if (name3 != null && name2 != null) {
			if (!"".equals(sb.toString()))
				sb.append("<br>");
			sb.append("Overlap (2,3) = "
					+ intersection23
					+ " ("
					+ df.format(intersection23 * 100.0
							/ this.vennData.getUnion23().size())
					+ "% of union)");
		}

		if (name1 != null && name2 != null && name3 != null) {
			if (!"".equals(sb.toString()))
				sb.append("<br>");
			sb.append("Overlap (1,2,3) = " + intersection123 + " ("
					+ df.format(intersection123 * 100.0 / union)
					+ "% of union)");
		}
		if (name1 != null) {
			if (!"".equals(sb.toString()))
				sb.append("<br>");
			Double just1 = this.vennData.getUniqueTo1().size() * 100.0 / union;
			int overlappedTo1 = this.vennData.getIntersection12().size()
					+ this.vennData.getIntersection13().size()
					- this.vennData.getIntersection123().size();
			sb.append("Just in 1 = "
					+ this.vennData.getUniqueTo1().size()
					+ " ("
					+ df.format(just1)
					+ "% of union) ("
					+ df.format((overlappedTo1) * 100.0
							/ this.vennData.getSize1()) + "% overlapped)");
		}
		if (name2 != null) {
			if (!"".equals(sb.toString()))
				sb.append("<br>");
			Double just2 = this.vennData.getUniqueTo2().size() * 100.0 / union;
			int overlappedTo2 = this.vennData.getIntersection12().size()
					+ this.vennData.getIntersection23().size()
					- this.vennData.getIntersection123().size();
			sb.append("Just in 2 = "
					+ this.vennData.getUniqueTo2().size()
					+ " ("
					+ df.format(just2)
					+ "% of union) ("
					+ df.format((overlappedTo2) * 100.0
							/ this.vennData.getSize2()) + "% overlapped)");
		}
		if (name3 != null) {
			if (!"".equals(sb.toString()))
				sb.append("<br>");
			Double just3 = this.vennData.getUniqueTo3().size() * 100.0 / union;
			int overlappedTo3 = this.vennData.getIntersection13().size()
					+ this.vennData.getIntersection23().size()
					- this.vennData.getIntersection123().size();
			sb.append("Just in 3 = "
					+ this.vennData.getUniqueTo3().size()
					+ " ("
					+ df.format(just3)
					+ "% of union) ("
					+ df.format((overlappedTo3) * 100.0
							/ this.vennData.getSize3()) + "% overlapped)");
		}
		sb.append("<br><br>");

		return sb.toString();
	}

}
