package org.proteored.pacom.analysis.charts;

import java.text.DateFormat;

import org.jfree.chart.labels.AbstractCategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

public class IntegerCategoryItemLabelGenerator extends AbstractCategoryItemLabelGenerator implements
		CategoryItemLabelGenerator {

	protected IntegerCategoryItemLabelGenerator(String labelFormat, DateFormat formatter) {
		super(labelFormat, formatter);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String generateLabel(CategoryDataset dataset, int row, int column) {
		// TODO Auto-generated method stub
		return null;
	}

}
