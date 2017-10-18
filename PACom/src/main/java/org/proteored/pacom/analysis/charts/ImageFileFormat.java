package org.proteored.pacom.analysis.charts;

public enum ImageFileFormat {
	// JPEF("JPEG Image File", "jpeg"),
	GIF("GIF Image File", "gif"),
	// BMP("BMP Image File", "bmp"),
	PNG("PNG Image File", "png");// , //
	// WBMP("WBMP Image File", "wbmp");
	private final String description;
	private final String extension;

	private ImageFileFormat(String description, String extension) {
		this.description = description;
		this.extension = extension;
	}

	public String getDescription() {
		return description;
	}

	public String getExtension() {
		return extension;
	}

	public static ImageFileFormat getFromDescription(String description) {
		for (ImageFileFormat imageFileFormat : ImageFileFormat.values()) {
			if (imageFileFormat.getDescription().equalsIgnoreCase(description)) {
				return imageFileFormat;
			}
		}
		return null;
	}

	public static ImageFileFormat getFromExtension(String ext) {
		for (ImageFileFormat imageFileFormat : ImageFileFormat.values()) {
			// if (ext.equalsIgnoreCase("jpg")) {
			// return ImageFileFormat.JPEF;
			// }
			if (imageFileFormat.getExtension().equalsIgnoreCase(ext)) {
				return imageFileFormat;
			}
		}
		return null;
	}
}
