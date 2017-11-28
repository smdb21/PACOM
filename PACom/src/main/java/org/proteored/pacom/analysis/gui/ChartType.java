package org.proteored.pacom.analysis.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.springframework.core.io.ClassPathResource;

public enum ChartType {
	PROTEIN_SCORE_DISTRIBUTION("Protein score Distribution",
			"Histogram of the score values of the proteins per dataset. Change the score from the <i>Protein score</i> dropdown menu. Try <i>Apply logs</i>, specially for p-values or e-values scores."), //
	PEPTIDE_SCORE_DISTRIBUTION("Peptide score Distribution",
			"Histogram of the score values of the peptides per dataset. Change the score from the <i>Peptide score</i> dropdown menu. Try <i>Apply logs</i>, specially for p-values or e-values scores."), //

	PROTEIN_SCORE_COMPARISON("Protein score Comparison",
			"Scatter plot with the score values of the same proteins in two different datasets. Change the score from the <i>Protein score</i> dropdown menu. A regression line and the diagonal are shown by default."), //
	PEPTIDE_SCORE_COMPARISON("Peptide score Comparison",
			"Scatter plot with the score values of the same peptides in two different datasets. Change the score from the <i>Peptide score</i> dropdown menu. A regression line and the diagonal are shown by default."), //
	PROTEIN_NUMBER_HISTOGRAM("Protein number",
			"Bar graph with the number of proteins per dataset. Try <i>Show as pie chart</i>."), //
	PEPTIDE_NUMBER_HISTOGRAM("Peptide number",
			"Bar graph with the number of peptides per dataset. Try <i>Show as pie chart</i>."), //
	PROTEIN_OVERLAPING("Protein overlapping",
			"Select 2 or 3 datasets to visualize the protein overlapping in a Venn diagram. Select the way you want to compare protein groups (when two protein groups are consider equal). Click on color next to dataset to customize it."), //
	PEPTIDE_OVERLAPING("Peptide overlapping",
			"Select 2 or 3 datasets to visualize the peptide overlapping in a Venn diagram. Click on color next to dataset to customize it."), //
	PROTEIN_OCURRENCE_HEATMAP("Protein Heat Map",
			"Heat-map in which each row represent a protein group and each column the number of times that protein has been detected in a particular dataset. Try to change color scale. <i>Do not paint rows with less than (occurrence)</i> means that any row in which the sum of the occurrences of the protein is less than that number will not be shown."), //
	PEPTIDE_OCCURRENCE_HEATMAP("Peptide Heat Map",
			"Heat-map in which each row represent a peptide and each column the number of times that peptide has been detected in a particular dataset. Try to change color scale. <i>Do not paint rows with less than (occurrence)</i> means that any row in which the sum of the occurrences of the peptide is less than that number will not be shown."), //
	PEPTIDE_PRESENCY_HEATMAP("Peptide list ocurrence HeatMap",
			"Add the peptides that you are interested on the list and you will see a heat-map in which each row represents each peptide and each column the number of times that peptide has been detected in a each dataset. Try to change color scale."), //
	PSMS_PER_PEPTIDE_HEATMAP("PSMs per peptide heatmap",
			"Heat-map in which each row represent a peptide and each column the number of PSMs assigned to that peptide sequence in a particular dataset. Try to change color scale. <i>Do not paint rows with less than</i> means that any row in which the sum of the PSMs along all the datasets is less than that number will not be shown."), //
	MODIFICATION_SITES_NUMBER("Number of modified sites",
			"Bar graph showing the number of different modified sites among the PSMs containing the selected modification(s). Try selecting multiple modifications from the list (press CTRL and select more than 1). Try <i>Show as stacked chart</i> and <i>Normalize</i>."), //
	MODIFICATED_PEPTIDE_NUMBER("Number of modified peptides",
			"Bar graph showing the number of different peptides containing each selected modification(s). Try selecting multiple modifications from the list (press CTRL and select more than 1). Try <i>Show as stacked chart</i> and <i>Normalize</i>."), //

	PEPTIDE_MODIFICATION_DISTRIBUTION("Peptide modification distribution",
			"Bar graph showing the number of different peptides (not PSMs) containing a selected modification in each dataset 1 modified site, 2 modified sites, etc. Try selecting more than one modification (press CTRL and select more than 1). Try <i>Show as stacked chart</i>."), //
	PEPTIDE_MONITORING("Peptide monitoring",
			"Bar graph showing the number of a particular peptide sequence + charge state + modification state (depending on <i>distinguish modified peptides</i> in each dataset. The peptide(s) of interest can be selected on the list of all peptides. You can also manually insert a custom list of peptides to monitor. You can also filter that list by a sequence tag that the peptides must contain."), //
	PROTEIN_COVERAGE_DISTRIBUTION("Protein coverage distribution", ""), //
	FDR("False Discovery Rate",
			"Line graph showing the FDR curves at different levels (PSM, peptide and protein levels), that is, the number of PSMs, peptides or proteins per FDR value. This chart is only available if a <i>FDR filter</i> has been applied."), //
	PROTEIN_REPEATABILITY("Protein repeatability",
			"Bar graph showing the number of proteins that have been detected 1, 2, 3, etc times. For proteins, try <i>Repeatibility over next level</i> which will show the number of proteins detected in 1, 2, 3, etc datasets of a lower level."), //
	PEPTIDE_REPEATABILITY("Peptide repeatability",
			"Bar graph showing the number of peptides that have been detected 1, 2, 3, etc times. If <i>Repeatibility over next level</i> is activated the graph will show the number of peptides detected in 1, 2, 3, etc datasets of a lower level. Limit the <i>maximum ocurrence</i> in the dropdown menu."), //

	PROTEIN_SENSITIVITY_SPECIFICITY("Sensitivity and specificity",
			"Show the percentage values of Sensitivity, Accuracy, Specificity, Precision, NPV and FDR for each dataset. Note that you have to provide a list of <b>known proteins in the sample</b> by clicking on <i>Define proteins in sample</i>. Position the mouse over the different checkboxes of the different statistic parameters to see its description."), //
	MISSEDCLEAVAGE_DISTRIBUTION("Missed cleavages distribution",
			"Bar graph showing the number of PSMs containing none, 1, 2, 3, etc number of missed cleavages. Define the aminoacids in which your enzyme cuts (i.e. <i>KR</i> for Trypsin). "
					+ "Try <i>Show as stacked chart</i> and <i>Normalize</i>."), //
	PEPTIDE_MASS_DISTRIBUTION("Peptide mass distribution",
			"Line graph showing the histogram of the mass distribution of the PSMs of each dataset. Try changing the x-axis to <i>Da</i> or <i>m/z</i>. Try changing the <i>Histogram type</i> to <i>SCALE_AREA_TO_1</i> to compare histograms that are quite different in absolute values."), //
	PEPTIDE_LENGTH_DISTRIBUTION("Peptide length distribution",
			"Bar graph showing the number of PSMs with certain lenghts that are defined in a custom range. Try <i>Show as stacked chart</i> and <i>Normalize</i>."), //
	PROTEIN_COVERAGE("Average protein coverage",
			"Bar graph showing the average protein coverage per each of the datasets. The graph also shows the error showing the standard deviation of the average for each dataset."), //
	PEPTIDE_CHARGE_HISTOGRAM("Peptide charge distribution",
			"Bar graph showing the number of PSMs that have been detected with charge 1, charge 2, charge 3, etc, in each of the datasets. Try <i>Show as stacked chart</i>."), //
	SINGLE_HIT_PROTEINS("Single hit proteins",
			"Bar graph with the number of proteins that have been identified with only one peptide or one PSM (depending on the option <i>single hit peptide or PSM</i>)"), //
	PEPTIDE_NUMBER_IN_PROTEINS("Number of proteins with x peptides",
			"Bar graph showing the number of proteins containing 1, 2, 3, ... x peptides or PSMs (depending on the option <i>PSMs or peptides</i>. Try selecting <i>Normalize</i> and <i>Show as stacked chart</i> and <i>Normalize</i>."), //
	DELTA_MZ_OVER_MZ("Peptide mass error",
			"Scatter plot showing the error between the theoretical peptide m/z and the experimental peptide m/z against the experimental m/z of the selected datasets. A regression line and a R square is shown by default per series."), //
	PSM_PEP_PROT("PSMs/Peptides/Proteins",
			"Line graph with the number of proteins, peptides, peptides plus charge, and PSMs."), //
	FDR_VS_SCORE("FDRs vs Score & num. proteins vs Score",
			"Line graph showing two series: FDR values vs score values (for proteins, peptides or PSMs), and (if <i>Show Score vs Num. proteins</i> is enabled) the score value vs number of proteins, per selected dataset. The score used is the one selected to sort items in the FDR filter. This chart is only available if a <i>FDR filter</i> has been applied."), //
	EXCLUSIVE_PROTEIN_NUMBER("Number of exclusive proteins",
			"Bar graph showing the number of proteins that are <b>only</b> detected in each one of the selected datasets. If <i>show accumulative trend</i> is selected, it also shows the accumulative number of different proteins when sequencially adding the datasets in the shown order. Select the way you want to compare protein groups (when two protein groups are consider equal). "), //
	EXCLUSIVE_PEPTIDE_NUMBER("Number of exclusive peptides",
			"Bar graph showing the number of peptides that are <b>only</b> detected in each one of the selected datasets. If <i>show accumulative trend</i> is selected, it also shows the accumulative number of different peptides when sequencially adding the datasets in the shown order."), //

	PEPTIDES_PER_PROTEIN_HEATMAP("Peptides per protein heatMap",
			"Heat-map in which each row represent a protein group and each column the number of peptide sequences (not PSMs) assigned to that protein group in a particular dataset. Try to change color scale. <i>Do not paint rows with less than</i> means that any row in which the sum of the peptides of that protein along all the datasets is less than that number will not be shown."), //
	PSMS_PER_PROTEIN_HEATMAP("PSMs per protein heatmap",
			"Heat-map in which each row represent a protein group and each column the number of PSMs assigned to that protein group in a particular dataset. Try to change color scale. <i>Do not paint rows with less than</i> means that any row in which the sum of the PSMs of that protein along all the datasets is less than that number will not be shown."), //
	PEPTIDE_NUM_PER_PROTEIN_MASS("Number of peptides / protein molecular weight",
			"Line graph showing an histogram of the ratio (log2) between the number of peptides per protein molecular weight (in Daltons). "), //
	HUMAN_CHROMOSOME_COVERAGE("Human chromosome coverage",
			"Bar graph or spider plot containing the number of Human genes per chromosome in each dataset. Activate <i>Normalize</i> to see the  percentage of the total Human genes in each chromosome that has been detected in each dataset."),
	// CHR16_MAPPING ( "Human chromosome 16 mapping
	// (SPanish-HPP)","",""),
	CHR_MAPPING("Proteins and genes per chromosome",
			"Bar graph (or pie chart) containing the number of proteins encoded in each Human chromosome, or number of genes in each Human chromosome, (or both), per dataset. Try <i>Show as pie chart</i>. Try filter by Human taxonomy, or by an specific Human chromosome. To remove the filter, disable the Protein ACC filter."), //
	CHR_PEPTIDES_MAPPING("Peptides and PSMs per chromosome",
			"Bar graph (or pie chart) containning the number of peptides, or PSMs, or both, that are mapped to protein encoded in genes present in each of the Human chromosomes. Try filter by Human taxonomy, or by an specific Human chromosome. To remove the filter, disable the Protein ACC filter."), //
	PROTEIN_NAME_CLOUD("Protein words cloud",
			"Graph showing a word cloud in which the words are collected from the protein descriptions, and the size of the words are proportional to the number of occurrencies of each word among the whole dataset. "
					+ "Try using different font types, and include words in the <i>Words to skip</i> list to remove them in the graph. Try to modify the maximum number of words to show. After changing any parameter, click on <i>Redraw chart</i> button.<br>Click on <i>Redraw chart</i> button again if the first time the chart is not showed properly."), //
	PROTEIN_GROUP_TYPES("Protein group type distribution",
			"Bar graph with the number of proteins of each of the 4 different protein group classifications made by PAnalyzer grouping algorithm. Try <i>Show as stacked chart</i> and <i>Normalize</i>."), //
	PEPTIDE_RT("Peptide Retention Times distribution",
			"Histogram distribution of the retention times of the PSMs for the selected datasets. Try to change the <i>Histogram type</i> to <i>SCALE_AREA_TO_1</i> to compare retention times distributions that are quite different in absolute values."), //
	PEPTIDE_RT_COMPARISON("Peptide Retention Times Comparison",
			"Scatter plot comparing the retention times for each peptide in each dataset. The retention time for each peptide corresponds to the average of the retention times of all the PSMs with the same sequence and charge state and modification state. Regression and diagonal lines are shown by default."), //
	SINGLE_RT_COMPARISON("Single peptide Retention Time Comparison",
			"Bar graph showing the retention times for each of the peptides selected by the user in each dataset. Try selecting more than one peptide (press CTRL and select more than 1)."), //
	SPECTROMETERS("Spectrometers",
			"Table containing the metadata referring to the spectrometers used in each dataset. This information will be only present if the datasets were imported together with a MS metadata template"), //
	INPUT_PARAMETERS("Input parameters",
			"Table containing the metadata referring to the input parameters of the search engine used in each dataset. This information will be only present if the datasets were imported together with a MS metadata template"), //
	PEPTIDE_COUNTING_HISTOGRAM("Peptide counting ratio histogram",
			"Bar graph showing an histogram of the ratios of the PSMs of the same protein in two different datasets (in log2 scale). Select only two datasets to show the graph. Define how two protein groups are considered the same in two different datasets."), //
	PEPTIDE_COUNTING_VS_SCORE("Peptide counting ratio vs score",
			"Scatter plot to show the ratios of the PSMs of the same protein in two different datasets (in log2 scale) against the value of the selected score of the protein in each dataset. Therefore, for each protein, it will be 2 different dots (score in dataset 1 vs PSM ratio, and score in dataset 2 vs PSM ratio). Define how two protein groups are considered the same in two different datasets.");

	private static final ClassLoader cl = ChartType.class.getClassLoader();

	private final String name;
	private final String description;
	private BufferedImage image;

	ChartType(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getImageName() {
		return name() + ".png";
	}

	public BufferedImage getImage() {
		if (image != null) {
			return image;
		}
		final String imageName = getImageName();

		final String fileName = "charttypes" + File.separator + imageName;
		try {
			final URL resource = new ClassPathResource(fileName).getURL();
			if (resource != null) {
				image = ImageIO.read(resource);
				return image;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ChartType getFromName(String name) {
		for (ChartType chartType : values()) {
			if (chartType.getName().equals(name)) {
				return chartType;
			}
		}
		return null;
	}

}
