package org.proteored.miapeExtractor.gui;

import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class TInstrumentModelTable extends AbstractTableModel{

	//Vars...
	protected Object Data[][];
	protected Vector columnshead;
	protected int columns;
	protected int rows;

	public TInstrumentModelTable()
	{
		// para la tabla vacia.
		columns = 0;
		rows = 0;
	}
	public TInstrumentModelTable(String[] headers, Object InputData)
	{
		InstrumentSummary[] instrumentData = (InstrumentSummary[])InputData;
		columns = headers.length;
		rows = instrumentData.length;
		//rows = 3;
		try
		{
			Data = new Object[getRowCount()][getColumnCount()];
			columnshead = new Vector(getColumnCount());
			for(int i = 0; i < getColumnCount(); i++)
				columnshead.addElement(headers[i]);

			this.EmptyTable();
			// Este seria el metodo propio
			InicializeTable(instrumentData);
			// Por ahora este ...
			// InicializeTable();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public int getRowCount()
	{
		return rows;
	}
	@Override
	public int getColumnCount()
	{
		return columns;
	}
	@Override
	public Object getValueAt(int _row, int _col)
	{
		//cuidado con esto......
		return Data[_row][_col];
	}

	public Vector getColumnHead()
	{
		return columnshead;
	}

	public String getHeadElement(int _pos)
	{
		return (String)getColumnHead().elementAt(_pos);
	}

	@Override
	public String getColumnName(int _pos)
	{
		return getHeadElement(_pos);
	}

	public int getColumnIndex(String _tag)
	{
		int _ret = getColumnHead().indexOf(_tag);
		return _ret;
	}

	@Override
	public void setValueAt(Object _value, int _row, int _col)
	{
		Data[_row][_col] = _value;
		//   Data[_row][_col] = (String)_value;
	}

	public void EmptyTable()
	{
		int i,j;
		for(i = 0; i < getRowCount(); i++)
		{
			for(j = 0; j < getColumnCount(); j++)
				Data[i][j] = " ";

		}
	}

	//Metodos propios
	public int InicializeTable(InstrumentSummary[] InputData)
	{
		int i;
		for(i = 0; i < getRowCount(); i++)
			AddRow(InputData[i],i);

		return i;
	}

	public int AddRow(InstrumentSummary _data, int _row)
	{
		int _ret = -1;
		if (_data!=null) {
			setValueAt(_data.getName(), _row, getColumnIndex(TFrmInputTable.instrument_head[0]));
			setValueAt(_data.getManufacturer(), _row, getColumnIndex("Manufacturer"));
			setValueAt(getList(_data.getAnalyzers()), _row, getColumnIndex("Analyzer(s)"));
			setValueAt(getList(_data.getIonSources()), _row, getColumnIndex("IonSource(s)"));
			setValueAt(_data.getActivation(), _row, getColumnIndex("Activation"));
		}
		return _ret;
	}
	private String getList(List<String> list) {
		String ret="";
		if (list!=null) {
			for (String string : list) {
				if (!ret.equals("")) 
					ret += ", ";
				ret += string;
			}
		}
		return ret;
	}

}
