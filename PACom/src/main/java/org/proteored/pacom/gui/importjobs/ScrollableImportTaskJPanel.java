package org.proteored.pacom.gui.importjobs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Comparator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

public class ScrollableImportTaskJPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6952246037072420820L;

	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");

	private final ImportTasksTable table;

	private TableRowSorter<TableModel> sorter = null;

	private Comparator comp;

	private ScrollableImportTaskJPanel(int wide, ImportTasksTable table) {
		this.table = table;
		initializeUI(wide);
	}

	public ScrollableImportTaskJPanel() {
		this(900, new ImportTasksTable());
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

		// setPreferredSize(new Dimension(wide, 400));

		//
		// Turn off JTable's auto resize so that JScrollpane
		// will show a horizontal scroll bar.
		//
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		// table.setSize(new Dimension(wide, 400));
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setFillsViewportHeight(true);
		// table.setPreferredScrollableViewportSize(new Dimension(wide, 400));
		initColumnSizes(table);

		final JScrollPane pane = new JScrollPane(table);
		add(pane, BorderLayout.CENTER);

		super.repaint();
	}

	private void initColumnSizes(JTable table) {
		final ImportTaskDataModel model = (ImportTaskDataModel) table.getModel();
		TableColumn column = null;
		final Component comp = null;
		final int headerWidth = 0;
		final int cellWidth = 0;
		final ImportTaskColumns[] longValues = ImportTaskColumns.values();
		final TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();

		for (int i = 0; i < ImportTaskColumns.values().length; i++) {
			column = table.getColumnModel().getColumn(i);

			// comp = headerRenderer.getTableCellRendererComponent(null,
			// column.getHeaderValue(), false, false, 0, 0);
			// headerWidth = comp.getPreferredSize().width;
			//
			// comp =
			// table.getDefaultRenderer(model.getColumnClass(i)).getTableCellRendererComponent(table,
			// longValues[i].getName(), false, false, 0, i);
			// cellWidth = comp.getPreferredSize().width;

			final int columnDefaultWidth = ImportTaskColumns.getColumns().get(i).getDefaultWidth();
			log.debug("Setting column width of " + ImportTaskColumns.getColumns().get(i).getName() + " [" + headerWidth
					+ "," + cellWidth + "," + columnDefaultWidth + "]");
			column.setPreferredWidth(Math.max(Math.max(headerWidth, cellWidth), columnDefaultWidth));
		}
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
								final TDoubleArrayList doubles = new TDoubleArrayList();
								for (final String string2 : split2) {
									doubles.add(getDouble(string2));
								}
								return String.valueOf(doubles.max());
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

	public ImportTasksTable getTable() {
		return table;
	}

	public InputFileType getSelectedInputFileTypeFromSelectedRow() {
		final int selectedRow = table.getSelectedRow();
		if (selectedRow != -1) {
			final Object valueAt = table.getModel().getValueAt(selectedRow,
					ImportTaskColumns.getColumns().indexOf(ImportTaskColumns.FILETYPE));
			if (valueAt != null && valueAt instanceof InputFileType) {
				return (InputFileType) valueAt;
			}
		}
		return null;
	}

	public void addTableSelectionListener(ListSelectionListener listener) {
		table.getSelectionModel().addListSelectionListener(listener);
	}

}
