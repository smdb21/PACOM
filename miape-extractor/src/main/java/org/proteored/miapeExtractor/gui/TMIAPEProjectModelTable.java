package org.proteored.miapeExtractor.gui;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.proteored.miapeapi.interfaces.persistence.ProjectFile;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeAPIWebserviceDelegate;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeDatabaseException_Exception;
import org.proteored.miapeapi.webservice.clients.miapeapi.MiapeSecurityException_Exception;
import org.proteored.miapeapi.xml.miapeproject.autogenerated.MIAPEProject;

public class TMIAPEProjectModelTable extends AbstractTableModel {
	protected Object Data[][];
	protected Vector columnshead;
	protected int columns;
	protected int rows;
	private TFrmInputTable tFrmInputTable;
	private String userName;
	private String password;
	private MiapeAPIWebserviceDelegate miapeAPIWebservice;
	private static Map<Integer, MIAPEProject> cachedProjects = new HashMap<Integer, MIAPEProject>();

	public TMIAPEProjectModelTable() {
		// para la tabla vacia.
		columns = 0;
		rows = 0;
		userName = null;
		password = null;
	}

	public TMIAPEProjectModelTable(TFrmInputTable tFrmInputTable,
			String[] headers, Object data, boolean localProjects) {
		this.tFrmInputTable = tFrmInputTable;
		if (tFrmInputTable != null) {
			userName = MainFrame.userName;
			password = MainFrame.password;
			miapeAPIWebservice = MainFrame.getMiapeAPIWebservice();

		}

		try {
			HashMap<Integer, String> miapeProjects = (HashMap<Integer, String>) data;

			columns = headers.length;
			rows = miapeProjects.keySet().size();
			// rows = 3;

			Data = new Object[getRowCount()][getColumnCount()];
			columnshead = new Vector(getColumnCount());
			for (int i = 0; i < getColumnCount(); i++)
				columnshead.addElement(headers[i]);

			EmptyTable();
			// Este seria el metodo propio
			InicializeTable(miapeProjects, localProjects);
			// Por ahora este ...
			// InicializeTable();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void InicializeTable(HashMap<Integer, String> miapeProjects,
			boolean localProjects) {
		String nameOwner;
		int projectNum = 0;
		List<Integer> sortedIds = new ArrayList<Integer>();
		sortedIds.addAll(miapeProjects.keySet());
		Collections.sort(sortedIds);
		for (int i = sortedIds.size() - 1; i >= 0; i--) {
			Integer projectID = sortedIds.get(i);
			if (!localProjects) {
				MIAPEProject project = getProject(projectID);
				if (project != null) {
					nameOwner = project.getOwnerName();
					setValueAt(project.getId(), projectNum, 0);
					setValueAt(project.getName(), projectNum, 1);
					setValueAt(nameOwner, projectNum, 2);
					setValueAt(project.getDate(), projectNum, 3);
					setValueAt(project.getComments(), projectNum, 4);
				}
			} else {
				setValueAt(projectID, projectNum, 0);
				setValueAt(miapeProjects.get(projectID), projectNum, 1);
				setValueAt("-", projectNum, 2);
				setValueAt("-", projectNum, 3);
				setValueAt("(local miape project)", projectNum, 4);
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

	private MIAPEProject getProject(Integer projectID) {
		if (cachedProjects.containsKey(projectID)) {
			return cachedProjects.get(projectID);
		}
		byte[] projectBytes;
		try {
			projectBytes = miapeAPIWebservice.getProjectById(projectID,
					userName, password);
			if (projectBytes != null) {
				JAXBContext jc = JAXBContext
						.newInstance("org.proteored.miapeapi.xml.miapeproject.autogenerated");
				ProjectFile projectFile = new ProjectFile(projectBytes);
				final MIAPEProject miapeProject = (MIAPEProject) jc
						.createUnmarshaller().unmarshal(projectFile.toFile());
				cachedProjects.put(projectID, miapeProject);
				return miapeProject;
			}
		} catch (MiapeSecurityException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MiapeDatabaseException_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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

	public Vector getColumnHead() {
		return columnshead;
	}

	public String getHeadElement(int _pos) {
		return (String) getColumnHead().elementAt(_pos);
	}
}
