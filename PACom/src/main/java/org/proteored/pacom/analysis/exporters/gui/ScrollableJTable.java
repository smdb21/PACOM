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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import gnu.trove.list.array.TIntArrayList;

public class ScrollableJTable extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 725114162692158140L;

	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private JTable table = new MyIdentificationTable();

	private TableRowSorter<TableModel> sorter = null;

	private Comparator comp;

	public ScrollableJTable(JTable jtable, int wide) {
		if (jtable != null) {
			table = jtable;
		}
		table.setModel(new MyDefaultTableModel());

		// Set renderer for painting different background colors
		table.setDefaultRenderer(Object.class, new MyTableCellRenderer());
		initializeUI(wide);
	}

	public ScrollableJTable(JTable jtable) {
		this(jtable, 600);
	}

	public ScrollableJTable(int wide) {

		table.setModel(new MyDefaultTableModel());

		// Set renderer for painting different background colors
		table.setDefaultRenderer(Object.class, new MyTableCellRenderer());
		initializeUI(wide);
	}

	public ScrollableJTable() {
		this(900);
	}

	public void initializeSorter() {
		sorter = new TableRowSorter<TableModel>(table.getModel());
		final int columnCount = table.getModel().getColumnCount();
		for (int i = 0; i < columnCount; i++)
			sorter.setComparator(i, getMyComparator2());
		table.setRowSorter(sorter);
	}

	private void initializeUI(int wide) {
		setLayout(new BorderLayout());

		setPreferredSize(new Dimension(wide, 400));

		//
		// Turn off JTable's auto resize so that JScrollpane
		// will show a horizontal scroll bar.
		//
		// table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		table.setSize(new Dimension(wide, 400));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		addProteinACCCellListener();

		final JScrollPane pane = new JScrollPane(table);
		pane.setViewportBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		add(pane, BorderLayout.CENTER);

		super.repaint();
	}

	private void addProteinACCCellListener() {
		if (table != null)
			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						final JTable target = (JTable) e.getSource();
						int row = target.getSelectedRow();
						if (target.getRowSorter() != null)
							row = target.getRowSorter().convertRowIndexToModel(row);
						final int column = target.getSelectedColumn();
						log.info("Row=" + row + " Column=" + column);
						if (e.getClickCount() == 2) {
							// int proteinACCIndex = -1;
							// int ensgIDIndex = -1;
							// try {
							// proteinACCIndex = target.getColumnModel()
							// .getColumnIndex(ExportedColumns.PROTEIN_ACC.toString());
							// } catch (IllegalArgumentException ex) {
							// }
							// try {
							// ensgIDIndex = target.getColumnModel()
							// .getColumnIndex(ExportedColumns.ENSG_ID.toString());
							// } catch (IllegalArgumentException ex) {
							// }
							// if (column == proteinACCIndex || column ==
							// ensgIDIndex) {
							// String value = target.getModel().getValueAt(row,
							// column).toString();
							// openBrowser(value);
							//
							// }
						}
					} catch (final IllegalArgumentException ex) {
						ex.printStackTrace();
					}
				}
			});

	}

	public void setFilter(String columnName, String regexp) {

		try {

			final RowFilter<Object, Object> paginatorFilter = getColumnFilter(columnName, regexp);
			// if (paginatorFilter != null)
			// filters.add(paginatorFilter);

			if (sorter != null) {

				sorter.setRowFilter(paginatorFilter);
				table.setRowSorter(sorter);

			}
		} catch (final java.util.regex.PatternSyntaxException e) {
			return;
		}
	}

	private RowFilter<Object, Object> getColumnFilter(final String columnName, final String regexp) {
		if (regexp != null && !"".equals(regexp)) {
			final int columnIndex = getColumnIndex(columnName);
			if (columnIndex >= 0)
				return RowFilter.regexFilter(regexp, columnIndex);
		}
		return null;
	}

	private Comparator<?> getMyComparator2() {
		if (comp == null)
			comp = new Comparator() {

				@Override
				public int compare(Object obj1, Object obj2) {
					try {
						final Number n1 = NumberFormat.getInstance().parse(obj1.toString());
						final Number n2 = NumberFormat.getInstance().parse(obj2.toString());
						final Double d1 = getDouble(obj1);
						final Double d2 = getDouble(obj2);
						return d1.compareTo(d2);
					} catch (final java.text.ParseException e1) {

						if (obj1 instanceof String && obj2 instanceof String) {
							final String n1 = (String) obj1;
							final String n2 = (String) obj2;

							final String n3 = getHighesNumberIfAreCommaSeparated(n1);
							final String n4 = getHighesNumberIfAreCommaSeparated(n2);
							if (n3 != null && n4 != null)
								return compare(n3, n4);
							return n1.compareTo(n2);

						} else if (obj1 instanceof String && obj2 instanceof Double) {
							final String n1 = (String) obj1;
							final String n2 = String.valueOf(obj2);
							return n1.compareTo(n2);
						} else if (obj2 instanceof String && obj1 instanceof Double) {
							final String n2 = (String) obj2;
							final String n1 = String.valueOf(obj1);
							return n1.compareTo(n2);
						} else {
							final String n1 = obj1.toString();
							final String n2 = obj2.toString();
							return n1.compareTo(n2);
						}

					}

				}

				private String getHighesNumberIfAreCommaSeparated(String string) {
					if (string.contains(";")) {
						final String[] split = string.split(";");
						try {
							final TIntArrayList ints = new TIntArrayList();
							for (final String string2 : split) {
								ints.add(Integer.valueOf(string2));
							}
							return String.valueOf(ints.max());
						} catch (final NumberFormatException e) {
							try {
								final String[] split2 = string.split(";");
								final List<Double> doubles = new ArrayList<Double>();
								for (final String string2 : split2) {
									doubles.add(getDouble(string2));
								}
								return String.valueOf(getMaxFromDoubles(doubles));
							} catch (final NumberFormatException e2) {
							} catch (final ParseException e3) {

							}
						}
					}

					return null;
				}

				private Double getDouble(Object value) throws ParseException {
					final Number n1 = NumberFormat.getInstance().parse(value.toString());
					return n1.doubleValue();
				}

				private String getMaxFromDoubles(List<Double> doubles) {
					double max = Double.MIN_VALUE;
					for (final Double dou : doubles) {
						if (max < dou)
							max = dou;
					}
					return String.valueOf(max);
				}

				private String getMaxFromIntegers(List<Integer> ints) {
					int max = Integer.MIN_VALUE;
					for (final Integer integer : ints) {
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
			for (int i = 0; i < table.getColumnCount(); i++) {
				if (table.getColumnName(i).equals(columnName))
					return i;
			}
		return -1;
	}

	public JTable getTable() {
		return table;
	}
}
