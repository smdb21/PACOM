package org.proteored.pacom.analysis.exporters;

public interface Exporter<T> {
	// CONSTANTS
	public static final String DATA_EXPORTING_DONE = "data exporting done";
	public static final String DATA_EXPORTING_ERROR = "data exporting error";
	public static final String DATA_EXPORTING_CANCELED = "data exporting canceled";
	public static final String DATA_EXPORTING_SORTING = "data exporting sorting";
	public static final String DATA_EXPORTING_STARTING = "data exporting starting";

	public static final String DATA_EXPORTING_SORTING_DONE = "data exporting sorting done";
	public static final String NEWLINE = System.getProperty("line.separator");
	public final char TAB = '\u0009';
	public final String COMMA = ",";

	public T export();

}
