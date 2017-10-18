package org.proteored.pacom.analysis.charts;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ImageLabel extends JLabel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4778932924724759828L;
	private Image _myimage;
	private final boolean keepRatioAspect;
	private double imageRatioAspect;
	private final String title;

	public ImageLabel(String text, String title, boolean keepRatioAspect) {
		super(text);
		this.keepRatioAspect = keepRatioAspect;
		this.title = title;
	}

	@Override
	public void setIcon(Icon icon) {

		super.setIcon(icon);
		if (icon instanceof ImageIcon) {
			ImageIcon imageIcon = (ImageIcon) icon;
			_myimage = imageIcon.getImage();
			imageRatioAspect = 1.0 * imageIcon.getIconWidth() / imageIcon.getIconHeight();
		}
	}

	@Override
	public void paint(Graphics g) {
		int width = this.getWidth();
		int height = this.getHeight();
		if (keepRatioAspect) {
			if (imageRatioAspect > 0) { // width > height
				height = Math.min(height, Double.valueOf(width / imageRatioAspect).intValue());
				if (height == this.getHeight()) {
					width = Double.valueOf(height * imageRatioAspect).intValue();
				}
			} else {// height > width
				width = Math.min(width, Double.valueOf(height * imageRatioAspect).intValue());
				if (width == this.getWidth()) {
					height = Double.valueOf(width / imageRatioAspect).intValue();
				}
			}
		}
		g.drawImage(_myimage, 0, 0, width, height, null);
	}

	public String getTitle() {
		return title;
	}
}
