package org.proteored.pacom.analysis.exporters.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.proteored.pacom.analysis.exporters.util.ExportedColumns;
import org.proteored.pacom.analysis.exporters.util.ExporterUtil;
import org.proteored.pacom.utils.HttpUtilities;

public class ScrollableJTable extends JPanel {
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private static final String UNIPROT_ONE_PROTEIN_QUERY_URL = "http://www.uniprot.org/uniprot/?sort=score&query=";
	private static final String NCBI_ONE_PROTEIN_QUERY_URL = "http://www.ncbi.nlm.nih.gov/protein/";
	private static final String ENSEMBL_HOMO_SAPIENS_QUERY = "http://www.ensembl.org/Multi/Search/Results?db=core;idx=;species=all;q=";

	private enum AccType {
		UNIPROT, GI, ENSG, ENSP
	};

	private JTable table = new MyIdentificationTable();

	private TableRowSorter<TableModel> sorter = null;

	private Comparator comp;

	public ScrollableJTable(JTable jtable, int wide) {
		if (jtable != null) {
			this.table = jtable;
		}
		this.table.setModel(new MyDefaultTableModel());

		// Set renderer for painting different background colors
		this.table.setDefaultRenderer(Object.class, new MyTableCellRenderer());
		initializeUI(wide);
	}

	public ScrollableJTable(JTable jtable) {
		this(jtable, 900);
	}

	public ScrollableJTable(int wide) {

		this.table.setModel(new MyDefaultTableModel());

		// Set renderer for painting different background colors
		this.table.setDefaultRenderer(Object.class, new MyTableCellRenderer());
		initializeUI(wide);
	}

	public ScrollableJTable() {
		this(900);
	}

	public void initializeSorter() {
		this.sorter = new TableRowSorter<TableModel>(this.table.getModel());
		final int columnCount = table.getModel().getColumnCount();
		for (int i = 0; i < columnCount; i++)
			sorter.setComparator(i, getMyComparator2());
		this.table.setRowSorter(sorter);
	}

	private void initializeUI(int wide) {
		setLayout(new BorderLayout());

		setPreferredSize(new Dimension(wide, 400));

		//
		// Turn off JTable's auto resize so that JScrollpane
		// will show a horizontal scroll bar.
		//
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		table.setSize(new Dimension(wide, 400));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		addProteinACCCellListener();

		JScrollPane pane = new JScrollPane(table);
		add(pane, BorderLayout.CENTER);

		super.repaint();
	}

	private void addProteinACCCellListener() {
		if (this.table != null)
			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						JTable target = (JTable) e.getSource();
						int row = target.getSelectedRow();
						if (target.getRowSorter() != null)
							row = target.getRowSorter().convertRowIndexToModel(row);
						int column = target.getSelectedColumn();
						log.info("Row=" + row + " Column=" + column);
						if (e.getClickCount() == 2) {
							int proteinACCIndex = -1;
							int ensgIDIndex = -1;
							try {
								proteinACCIndex = target.getColumnModel()
										.getColumnIndex(ExportedColumns.PROTEIN_ACC.toString());
							} catch (IllegalArgumentException ex) {
							}
							try {
								ensgIDIndex = target.getColumnModel()
										.getColumnIndex(ExportedColumns.ENSG_ID.toString());
							} catch (IllegalArgumentException ex) {
							}
							if (column == proteinACCIndex || column == ensgIDIndex) {
								String value = target.getModel().getValueAt(row, column).toString();
								openBrowser(value);

							}
						}
					} catch (IllegalArgumentException ex) {
						ex.printStackTrace();
					}
				}
			});

	}

	protected void openBrowser(String proteinACC) {
		if (proteinACC.contains(ExporterUtil.VALUE_SEPARATOR)) {
			String[] split = proteinACC.split(ExporterUtil.VALUE_SEPARATOR);

			int numValid = 0;
			for (String string : split) {
				if (getACCType(string) != null) {
					numValid++;
				}
			}
			if (numValid > 0) {
				final int selectedOption = JOptionPane.showConfirmDialog(this,
						"<html>The cell contains " + numValid + " different accessions.<br>"
								+ "Click YES to go to UniprotKB in " + split.length + " different browser windows.<br>"
								+ "If you want just to open the first accession, that is, '" + split[0]
								+ "', click on NO.</html>",
						"GO TO UNIPROTKB. More than one accession in the cell", JOptionPane.YES_NO_CANCEL_OPTION);

				if (selectedOption == JOptionPane.YES_OPTION) {
					for (String singleProteinACC : split) {
						String urlText = getURL(singleProteinACC);
						if (!"".equals(urlText))
							HttpUtilities.openURL(urlText);
					}
				} else if (selectedOption == JOptionPane.NO_OPTION) {
					String urlText = getURL(split[0]);
					if (!"".equals(urlText))
						HttpUtilities.openURL(urlText);
				}
			}
		} else {
			final int selectedOption = JOptionPane.showConfirmDialog(this, "<html>Click YES to go to UniprotKB.</html>",
					"GO TO UNIPROTKB", JOptionPane.YES_NO_OPTION);

			if (selectedOption == JOptionPane.YES_OPTION) {
				String urlText = getURL(proteinACC);
				if (!"".equals(urlText))
					HttpUtilities.openURL(urlText);
			}
		}
	}

	private String getURL(String singleProteinACC) {
		String urlText = "";
		String acc = singleProteinACC;
		AccType acc_type = getACCType(singleProteinACC);
		if (acc_type != null) {
			switch (acc_type) {
			case ENSG:
				urlText = ENSEMBL_HOMO_SAPIENS_QUERY;
				break;
			case ENSP:
				urlText = ENSEMBL_HOMO_SAPIENS_QUERY;
				break;
			case GI:
				urlText = NCBI_ONE_PROTEIN_QUERY_URL;
				break;
			case UNIPROT:
				if (singleProteinACC.contains("|")) {
					final String[] split = singleProteinACC.split("\\|");
					if (split.length > 1)
						acc = split[1];
				}
				urlText = UNIPROT_ONE_PROTEIN_QUERY_URL;
				break;
			}
			urlText += acc;
			return urlText;
		}
		return null;
	}

	private AccType getACCType(String proteinACC) {
		if (proteinACC.startsWith("gi|")) {
			return AccType.GI;
		} else if (proteinACC.length() == 6 || proteinACC.length() == 8) {
			return AccType.UNIPROT;
		} else if (proteinACC.startsWith("ENSP")) {
			return AccType.ENSP;
		} else if (proteinACC.startsWith("ENSG")) {
			return AccType.ENSG;
		} else if (proteinACC.contains("|")) {
			final String[] split = proteinACC.split("\\|");
			if (split.length > 1)
				return AccType.UNIPROT;
		} else {
			return AccType.UNIPROT;
		}
		return null;
	}

	public void setFilter(String columnName, String regexp) {

		try {
			// log.info("Filter= " + columnName + " " + regexp);
			// ArrayList<RowFilter<Object, Object>> filters = new
			// ArrayList<RowFilter<Object, Object>>();
			// if (!"".equals(regexp)) {
			// RowFilter<Object, Object> columnFilter = getColumnFilter(
			// columnName, regexp);
			// if (columnFilter != null)
			// filters.add(columnFilter);
			// }
			final RowFilter<Object, Object> paginatorFilter = getColumnFilter(columnName, regexp);
			// if (paginatorFilter != null)
			// filters.add(paginatorFilter);

			if (this.sorter != null) {

				this.sorter.setRowFilter(paginatorFilter);
				this.table.setRowSorter(sorter);

			}
		} catch (java.util.regex.PatternSyntaxException e) {
			return;
		}
	}

	private RowFilter<Object, Object> getColumnFilter(final String columnName, final String regexp) {
		if (regexp != null && !"".equals(regexp)) {
			int columnIndex = getColumnIndex(columnName);
			if (columnIndex >= 0)
				return RowFilter.regexFilter(regexp, columnIndex);
		}
		return null;
	}

	private Comparator<?> getMyComparator2() {
		if (this.comp == null)
			this.comp = new Comparator() {

				@Override
				public int compare(Object obj1, Object obj2) {
					try {
						Number n1 = NumberFormat.getInstance().parse(obj1.toString());
						Number n2 = NumberFormat.getInstance().parse(obj2.toString());
						Double d1 = getDouble(obj1);
						Double d2 = getDouble(obj2);
						return d1.compareTo(d2);
					} catch (java.text.ParseException e1) {

						if (obj1 instanceof String && obj2 instanceof String) {
							String n1 = (String) obj1;
							String n2 = (String) obj2;

							String n3 = getHighesNumberIfAreCommaSeparated(n1);
							String n4 = getHighesNumberIfAreCommaSeparated(n2);
							if (n3 != null && n4 != null)
								return compare(n3, n4);
							return n1.compareTo(n2);

						} else if (obj1 instanceof String && obj2 instanceof Double) {
							String n1 = (String) obj1;
							String n2 = String.valueOf(obj2);
							return n1.compareTo(n2);
						} else if (obj2 instanceof String && obj1 instanceof Double) {
							String n2 = (String) obj2;
							String n1 = String.valueOf(obj1);
							return n1.compareTo(n2);
						} else {
							String n1 = obj1.toString();
							String n2 = obj2.toString();
							return n1.compareTo(n2);
						}

					}

				}

				private String getHighesNumberIfAreCommaSeparated(String string) {
					if (string.contains(";")) {
						String[] split = string.split(";");
						try {
							List<Integer> ints = new ArrayList<Integer>();
							for (String string2 : split) {
								ints.add(Integer.valueOf(string2));
							}
							return String.valueOf(getMaxFromIntegers(ints));
						} catch (NumberFormatException e) {
							try {
								String[] split2 = string.split(";");
								List<Double> doubles = new ArrayList<Double>();
								for (String string2 : split2) {
									doubles.add(getDouble(string2));
								}
								return String.valueOf(getMaxFromDoubles(doubles));
							} catch (NumberFormatException e2) {
							} catch (ParseException e3) {

							}
						}
					}

					return null;
				}

				private Double getDouble(Object value) throws ParseException {
					Number n1 = NumberFormat.getInstance().parse(value.toString());
					return n1.doubleValue();
				}

				private String getMaxFromDoubles(List<Double> doubles) {
					double max = Double.MIN_VALUE;
					for (Double dou : doubles) {
						if (max < dou)
							max = dou;
					}
					return String.valueOf(max);
				}

				private String getMaxFromIntegers(List<Integer> ints) {
					int max = Integer.MIN_VALUE;
					for (Integer integer : ints) {
						if (max < integer)
							max = integer;
					}
					return String.valueOf(max);
				}
			};
		return comp;
	}

	public int getColumnIndex(String columnName) {
		if (table != null)
			for (int i = 0; i < this.table.getColumnCount(); i++) {
				if (this.table.getColumnName(i).equals(columnName))
					return i;
			}
		return -1;
	}

	public JTable getTable() {
		return this.table;
	}
}
