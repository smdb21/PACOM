package org.proteored.pacom.gui;

import java.net.URL;

import javax.swing.ImageIcon;

public class ImageManager {
	public static final String STAR = "star.png";
	public static final String STAR_CLICKED = "star_clicked.png";
	public static final String FUNNEL = "funnel.png";
	public static final String FUNNEL_CLICKED = "funnel_clicked.png";
	public static final String TRASH = "trash.png";
	public static final String TRASH_CLICKED = "trash_clicked.png";
	public static final String TABLE = "table.png";
	public static final String TABLE_CLICKED = "table_clicked.png";
	// public static final String PROTEORED_MIAPE_API = "APIproteoredSmall.gif";
	public static final String PACOM_LOGO = "pacom.png";
	public static final String PACOM_LOGO_128 = "pacom_128.png";
	public static final String PACOM_LOGO_128_CLICKED = "pacom_128_clicked.png";
	public static final String PACOM_LOGO_128_HOVER = "pacom_128_hover.png";

	public static final String PACOM_LOGO_64 = "pacom_64.png";
	public static final String PACOM_LOGO_64_CLICKED = "pacom_64_clicked.png";
	public static final String PACOM_LOGO_64_HOVER = "pacom_64_hover.png";

	public static final String LOAD_LOGO_128 = "load_128.png";
	public static final String LOAD_LOGO_128_CLICKED = "load_128_clicked.png";
	public static final String LOAD_LOGO_128_HOVER = "load_128_hover.png";
	public static final String BATCH_LOAD_LOGO_128 = "pacom_batch_import_128.png";
	public static final String BATCH_LOAD_LOGO_128_CLICKED = "pacom_batch_import_128_clicked.png";
	public static final String BATCH_LOAD_LOGO_128_HOVER = "pacom_batch_import_128_hover.png";
	public static final String CURATED_EXPERIMENT = "curated_experiment.png";
	public static final String EXPERIMENT = "experiment.png";
	public static final String REPLICATE = "replicate.png";
	public static final String DOC = "doc.png";
	public static final String PRIDE_LOGO = "pride_logo_peq.jpg";
	public static final String PRIDE_LOGO_CLICKED = "pride_logo_peq_clicked.jpg";
	public static final String RELOAD = "reload.png";
	public static final String RELOAD_CLICKED = "reload_clicked.png";
	public static final String STOP = "stop.png";
	public static final String STOP_CLICKED = "stop_clicked.png";
	public static final String SAVE = "save.png";
	public static final String SAVE_CLICKED = "save_clicked.png";
	public static final String FINISH = "finish.png";
	public static final String FINISH_CLICKED = "finish_clicked.png";
	public static final String LOAD = "load.png";
	public static final String LOAD_CLICKED = "load_clicked.png";
	public static final String CLEAR = "clear.png";
	public static final String CLEAR_CLICKED = "clear_clicked.png";
	public static final String DELETE = "delete.png";
	public static final String DELETE_CLICKED = "delete_clicked.png";
	public static final String ADD = "add.png";
	public static final String ADD_CLICKED = "add_clicked.png";
	public static final String LOGIN = "login.png";
	public static final String LOGIN_CLICKED = "login_clicked.png";
	public static final String ADD_USER = "add_user.png";
	public static final String ADD_USER_CLICKED = "add_user_clicked.png";
	public static final String PEX = "pex.png";
	public static final String FOLDER = "folder.png";
	public static final String EXCEL_TABLE = "excel_table.png";
	public static final String EXCEL_TABLE_CLICKED = "excel_table_clicked.png";
	private static final ClassLoader cl = ImageManager.class.getClassLoader();
	public static final String SPECTRUM = "spectrum.jpg";
	public static final String PRIDE = "pride_logo_muypeq.jpg";
	public static final String SEARCH = "search.png";
	public static final String RAW = "raw.png";
	public static final String HELP_ICON = "icon-help.png";
	public static final String HELP_ICON_HOVER = "icon-help_hover.png";
	public static final String HELP_ICON_CLICKED = "icon-help_clicked.png";
	public static final String HELP_ICON_64 = "icon-help_64.png";
	public static final String HELP_ICON_64_HOVER = "icon-help_64_hover.png";
	public static final String HELP_ICON_64_CLICKED = "icon-help_64_clicked.png";

	public static ImageIcon getImageIcon(String name) {
		final URL resource = cl.getResource(name);
		if (resource != null) {
			ImageIcon icon = new ImageIcon(resource);
			return icon;
		}
		return null;
	}
}
