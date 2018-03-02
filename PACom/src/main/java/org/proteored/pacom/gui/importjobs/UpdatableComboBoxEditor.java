package org.proteored.pacom.gui.importjobs;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;

/**
 * This is a cell editor showing a comboBox which items can be updated by method
 * setComboItems.<br>
 * Use method addCellEditorListener if you want to make something when a new
 * element is selected by the editor
 * 
 * @author Salva
 *
 */
public class UpdatableComboBoxEditor<T> extends AbstractCellEditor implements TableCellEditor {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5542477352772241561L;
	private JComboBox<T> jComboBox = new JComboBox<T>();
	private List<T> items = new ArrayList<T>();
	private boolean cellEditingStopped;

	public UpdatableComboBoxEditor() {

	}

	public UpdatableComboBoxEditor(Collection<T> items) {
		for (T t : items) {
			this.items.add(t);
		}
	}

	public UpdatableComboBoxEditor(T[] values) {
		for (T t : values) {
			this.items.add(t);
		}
	}

	@Override
	public Object getCellEditorValue() {
		return jComboBox.getSelectedItem();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (items != null && !items.isEmpty()) {
			if (jComboBox.getItemCount() != items.size()) {
				for (T t : items) {
					jComboBox.addItem(t);
				}
			}
		} else {
			// this should not happen because it is only editable when items has
			// elements
			return null;
		}

		if (value == null) {
			// select the first one if value is null
			jComboBox.setSelectedIndex(0);
		} else {
			jComboBox.setSelectedItem(value);
		}
		jComboBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					fireEditingStopped();
				}
			}
		});
		jComboBox.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				cellEditingStopped = false;
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				cellEditingStopped = true;
				fireEditingCanceled();
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {

			}
		});
		return jComboBox;
	}

	@Override
	public boolean stopCellEditing() {
		return cellEditingStopped;
	}

	@Override
	public boolean isCellEditable(EventObject e) {
		if (items != null && !items.isEmpty()) {
			return true;
		}
		return false;
	}

	public void setComboItems(List<T> items) {
		this.items = items;
	}
}
