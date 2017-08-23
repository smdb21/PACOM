package org.proteored.pacom.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.proteored.pacom.analysis.util.FileManager;

import gnu.trove.map.hash.TIntObjectHashMap;

public class TMIAPEProjectModelTable extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7817891784188291749L;
	protected Object Data[][];
	protected Vector<String> columnshead;
	protected int columns;
	protected int rows;

	public TMIAPEProjectModelTable() {
		// para la tabla vacia.
		columns = 0;
		rows = 0;

	}

	public TMIAPEProjectModelTable(String[] headers, TIntObjectHashMap<String> data) {

		columns = headers.length;
		rows = data.keySet().size();
		// rows = 3;

		Data = new Object[getRowCount()][getColumnCount()];
		columnshead = new Vector<String>(getColumnCount());
		for (int i = 0; i < getColumnCount(); i++)
			columnshead.addElement(headers[i]);

		EmptyTable();
		// Este seria el metodo propio
		InicializeTable(data);
		// Por ahora este ...
		// InicializeTable();

	}

	public void InicializeTable(TIntObjectHashMap<String> miapeProjects) {
		int projectNum = 0;
		List<Integer> sortedIds = new ArrayList<Integer>();
		for (int key : miapeProjects.keys()) {
			sortedIds.add(key);
		}

		Collections.sort(sortedIds);
		for (int i = sortedIds.size() - 1; i >= 0; i--) {
			Integer projectID = sortedIds.get(i);

			setValueAt(projectID, projectNum, 0);
			setValueAt(miapeProjects.get(projectID), projectNum, 1);

			Path folder = new File(FileManager.getMiapeLocalDataPath(miapeProjects.get(projectID))).toPath();
			BasicFileAttributes attr;
			try {
				attr = Files.readAttributes(folder, BasicFileAttributes.class);
				Date date = new Date(attr.creationTime().toMillis());
				setValueAt(date, projectNum, 2);
			} catch (IOException e) {
				e.printStackTrace();
				setValueAt("-", projectNum, 2);
			}

			projectNum++;
		}

		// TODO
		/*
		 * setValueAt(String.valueOf("Project 1"), 0,0);
		 * setValueAt(String.valueOf("Pepito Perez"), 0,1);
		 * setValueAt(String.valueOf("Project 2"), 1,0);
		 * setValueAt(String.valueOf("Arturo Fernandez"), 1,1);
		 * setValueAt(String.valueOf("Porject 3"), 2,0);
		 * setValueAt(String.valueOf("Enriqeu Perez"), 2,1); for (int
		 * i=2;i<getColumnCount();i++) {
		 * setValueAt(String.valueOf("Property xx"), 0,i);
		 * setValueAt(String.valueOf("Property yy"), 1,i);
		 * setValueAt(String.valueOf("Property zz"), 2,i); }
		 */

	}

	@Override
	public int getRowCount() {
		return rows;
	}

	@Override
	public int getColumnCount() {
		return columns;
	}

	@Override
	public Object getValueAt(int _row, int _col) {
		// cuidado con esto......
		return Data[_row][_col];
	}

	public void EmptyTable() {
		int i, j;
		for (i = 0; i < getRowCount(); i++) {
			for (j = 0; j < getColumnCount(); j++)
				Data[i][j] = " ";

		}
	}

	@Override
	public void setValueAt(Object _value, int _row, int _col) {
		Data[_row][_col] = _value;
		// Data[_row][_col] = (String)_value;
	}

	@Override
	public String getColumnName(int _pos) {
		return getHeadElement(_pos);
	}

	public Vector<String> getColumnHead() {
		return columnshead;
	}

	public String getHeadElement(int _pos) {
		return getColumnHead().elementAt(_pos);
	}
}
