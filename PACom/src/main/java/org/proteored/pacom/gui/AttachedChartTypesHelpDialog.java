package org.proteored.pacom.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.log4j.Logger;
import org.proteored.pacom.analysis.gui.ChartType;
import org.proteored.pacom.utils.ToolTipUtil;

public class AttachedChartTypesHelpDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1119185903108814297L;
	private static final Logger log = Logger.getLogger(AttachedChartTypesHelpDialog.class);
	private final Window parentFrame;
	private static final String HTML_START = "<html>";
	private static final String HTML_END = "</html>";
	private static final String HTML_NEW_LINE = "<br>";
	private static final String HTML_BOLD_START = "<b>";
	private static final String HTML_BOLD_END = "</b>";
	private static final String HTML_ITALIC_START = "<i>";
	private static final String HTML_ITALIC_END = "</i>";
	private static final String HTML_BLANK_SPACE = "&nbsp;";
	private boolean minimized = false;
	private final int maxWidth;

	private JScrollPane scrollPane;

	private Style regularText;
	private Style indentedText;
	private Style boldText;
	private Style italicText;
	private Style headingText;
	private Style subheadingText;
	private Style biggerRegularText;
	private Style biggerHeadingText;
	private Style defaultRegularText;
	private Style defaultSubheadingText;

	public AttachedChartTypesHelpDialog(Window parentWindow, int maxWidth) {
		super(parentWindow, ModalityType.MODELESS);

		this.maxWidth = maxWidth;
		getContentPane().setBackground(SystemColor.info);
		setTitle("Chart type descriptions");
		setFocusableWindowState(false);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			e.printStackTrace();
		}
		this.parentFrame = parentWindow;

		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(20);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		// contentPanel.setBackground(SystemColor.info);

		addWindowListeners();

	}

	private void scrollToBeginning() {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				JScrollBar bar = scrollPane.getVerticalScrollBar();
				bar.setValue(bar.getMinimum());
			}
		});

	}

	public void loadChartTypes(List<ChartType> chartTypes) {
		log.info("Loading chart types images");
		// clear text
		int inset = 10;
		int textWidth = (this.maxWidth - 30) / 2 - inset * 2;
		int imageSize = 150;

		if (chartTypes.size() < 2) {
			regularText = biggerRegularText;
			subheadingText = biggerHeadingText;
			textWidth = this.maxWidth - 30 - inset;
			imageSize = 300;
		} else {
			regularText = defaultRegularText;
			subheadingText = defaultSubheadingText;
		}
		JPanel contentPanel = new JPanel();
		scrollPane.setViewportView(contentPanel);
		if (chartTypes != null) {
			contentPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.NORTHWEST;
			c.fill = GridBagConstraints.NONE;
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 0;
			c.weighty = 1;

			c.insets = new Insets(10, inset, 0, 0);
			log.info("adding panels");
			for (int i = 0; i < chartTypes.size(); i++) {
				ChartType chartType = chartTypes.get(i);
				final BufferedImage image = chartType.getImage();
				if (image == null) {
					continue;
				}

				final String descriptionHTML = HTML_BOLD_START + chartType.getName() + HTML_BOLD_END + HTML_NEW_LINE
						+ chartType.getDescription();

				StyleContext context = new StyleContext();
				createStyles(context);
				DefaultStyledDocument document = new DefaultStyledDocument(context);
				document.setLogicalStyle(0, regularText);
				JTextPane description = new JTextPane(document);
				final String toolTip = "<html>" + ToolTipUtil.splitWordsInHTMLLines(chartType.getDescription(),
						description.getFontMetrics(description.getFont()), 300) + "</html> ";
				description.setToolTipText(toolTip);
				description.setPreferredSize(new Dimension(textWidth, 150));
				insertTextToDocument(descriptionHTML, document);

				JScrollPane descriptionPanel = new JScrollPane(description);
				descriptionPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				JPanel imagePanel = new JPanel();
				final ImageIcon imageIcon = new ImageIcon(
						image.getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH));
				imageIcon.setDescription(toolTip);
				JLabel chartTypeImage = new JLabel(imageIcon);
				chartTypeImage.setToolTipText(toolTip);
				imagePanel.add(chartTypeImage);

				JPanel panel = new JPanel();
				// panel.setMaximumSize(new Dimension(100, Integer.MAX_VALUE));
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				panel.add(imagePanel);
				panel.add(descriptionPanel);

				contentPanel.add(panel, c);
				if (c.gridx == 0) {
					c.gridx = 1;
				} else {
					c.gridx = 0;
					c.gridy++;
				}

			}

		}
		log.info("Messages added to help window");
	}

	private void insertTextToDocument(String message, DefaultStyledDocument document) {

		List<String> messages = new ArrayList<String>();
		if (message.contains(HTML_NEW_LINE)) {
			for (String messageTMP : message.split(HTML_NEW_LINE)) {
				messages.add(messageTMP);
			}
		} else {
			messages.add(message);
		}
		try {
			boolean lookingForBoldStart = true;
			boolean lookingForItalicStart = true;
			boolean insideBold = false;
			boolean insideItalic = false;
			boolean firstTime = true;
			for (int i = 0; i < messages.size(); i++) {
				String helpMessage = messages.get(i);
				if (firstTime) {
					helpMessage = helpMessage.trim();
					firstTime = false;
				}

				if (helpMessage.startsWith(HTML_BOLD_START) && helpMessage.endsWith(HTML_BOLD_END)) {
					document.insertString(document.getLength(), removeHTMLTags(helpMessage) + "\n", subheadingText);
					firstTime = true;
					continue;
				}

				Style style = null;

				if (i == 0) {
					style = headingText;
				}
				int startBold = helpMessage.indexOf(HTML_BOLD_START);
				if (startBold == -1) {
					startBold = Integer.MAX_VALUE;
				}
				int endBold = helpMessage.indexOf(HTML_BOLD_END);
				int startItalic = helpMessage.indexOf(HTML_ITALIC_START);
				if (startItalic == -1) {
					startItalic = Integer.MAX_VALUE;
				}
				int endItalic = helpMessage.indexOf(HTML_ITALIC_END);
				String tmpMessage;
				if (!insideBold && !insideItalic && lookingForBoldStart && startBold < startItalic
						&& helpMessage.contains(HTML_BOLD_END)) {

					tmpMessage = helpMessage.substring(0, startBold);
					document.insertString(document.getLength(), tmpMessage, style);
					helpMessage = helpMessage.substring(tmpMessage.length() + HTML_BOLD_START.length());
					lookingForBoldStart = false;
					insideBold = true;
					messages.set(i, helpMessage);
					i--;
					continue;
				} else if (insideBold) {
					tmpMessage = helpMessage.substring(0, endBold);
					document.insertString(document.getLength(), tmpMessage, boldText);
					helpMessage = helpMessage.substring(tmpMessage.length() + HTML_BOLD_END.length());
					insideBold = false;
					lookingForBoldStart = true;
					messages.set(i, helpMessage);
					i--;
					continue;
				} else if (!insideBold && !insideItalic && lookingForItalicStart && startItalic < startBold
						&& helpMessage.contains(HTML_ITALIC_END)) {

					tmpMessage = helpMessage.substring(0, startItalic);
					document.insertString(document.getLength(), tmpMessage, style);
					helpMessage = helpMessage.substring(tmpMessage.length() + HTML_ITALIC_START.length());
					lookingForItalicStart = false;
					insideItalic = true;
					messages.set(i, helpMessage);
					i--;
					continue;
				} else if (insideItalic) {
					tmpMessage = helpMessage.substring(0, endItalic);
					document.insertString(document.getLength(), tmpMessage, italicText);
					helpMessage = helpMessage.substring(tmpMessage.length() + HTML_ITALIC_END.length());
					lookingForItalicStart = true;
					insideItalic = false;
					messages.set(i, helpMessage);
					i--;
					continue;
				} else {
					document.insertString(document.getLength(), helpMessage, style);

				}

				document.insertString(document.getLength(), "\n", style);
				firstTime = true;
			}

		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}

	private void addWindowListeners() {
		this.parentFrame.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
				log.info(e.getID());
				AttachedChartTypesHelpDialog.this.setVisible(true);
			}

			@Override
			public void componentResized(ComponentEvent e) {
				log.debug(e.getID());
				AttachedChartTypesHelpDialog.this.setVisible(true);

			}

			@Override
			public void componentMoved(ComponentEvent e) {
				AttachedChartTypesHelpDialog.this.setVisible(true);
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				log.info(e.getID());
				AttachedChartTypesHelpDialog.this.setVisible(false);
			}
		});
		this.parentFrame.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				log.info(e.getID() + " from " + e.getOldState() + " to " + e.getNewState());
				// open by default
				AttachedChartTypesHelpDialog.this.setVisible(true);
			}

			@Override
			public void windowClosing(WindowEvent e) {
				log.info(e.getID() + " from " + e.getOldState() + " to " + e.getNewState());

			}

			@Override
			public void windowClosed(WindowEvent e) {
				log.info(e.getID() + " from " + e.getOldState() + " to " + e.getNewState());
				AttachedChartTypesHelpDialog.this.dispose();

			}

			@Override
			public void windowIconified(WindowEvent e) {
				log.debug(e.getID() + " from " + e.getOldState() + " to " + e.getNewState());
				minimized = true;
				AttachedChartTypesHelpDialog.this.setVisible(false);

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				log.debug(e.getID() + " from " + e.getOldState() + " to " + e.getNewState());
				minimized = false;
				AttachedChartTypesHelpDialog.this.setVisible(true);

			}

			@Override
			public void windowActivated(WindowEvent e) {
				log.debug(
						e.getID() + " from " + e.getOldState() + " to " + e.getNewState() + " minimized=" + minimized);
				AttachedChartTypesHelpDialog.this.setVisible(true);
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				log.debug(
						e.getID() + " from " + e.getOldState() + " to " + e.getNewState() + " minimized=" + minimized);
				// AttachedHelpDialog.this.setVisible(false);
			}

		});
		this.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				log.debug(e.getID() + " " + e.getNewState() + " from " + e.getOldState());
			}

			@Override
			public void windowIconified(WindowEvent e) {
				log.debug(e.getID() + " " + e.getNewState() + " from " + e.getOldState());
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				log.debug(e.getID() + " " + e.getNewState() + " from " + e.getOldState());
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				log.debug(e.getID() + " " + e.getNewState() + " from " + e.getOldState());
			}

			@Override
			public void windowClosing(WindowEvent e) {
				log.debug(e.getID() + " " + e.getNewState() + " from " + e.getOldState());
				setVisible(false);
				minimized = true;

			}

			@Override
			public void windowClosed(WindowEvent e) {
				log.debug(e.getID() + " " + e.getNewState() + " from " + e.getOldState());
			}

			@Override
			public void windowActivated(WindowEvent e) {
				log.debug(e.getID() + " " + e.getNewState() + " from " + e.getOldState());
			}
		});
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			if (!minimized) {
				log.debug("setting help dialog visible to " + b);
				super.setVisible(b);
				positionNextToParent();
				parentFrame.requestFocus();
				// if (!scrolledToBeggining) {
				// }
			}
		} else {
			log.debug("setting help dialog visible to " + b);
			super.setVisible(b);
		}
	}

	public void forceVisible() {
		minimized = false;
		setVisible(true);
		scrollToBeginning();

	}

	@Override
	public void dispose() {
		log.info("Dialog dispose");
		super.dispose();
		this.minimized = true;

	}

	private void positionNextToParent() {

		Point parentLocationOnScreen = this.parentFrame.getLocation();
		int x = parentLocationOnScreen.x + this.parentFrame.getWidth();
		int y = parentLocationOnScreen.y;
		this.setLocation(x, y);
		this.setSize(maxWidth, this.parentFrame.getHeight());
		log.debug("Setting position next to the parent frame (" + x + "," + y + ")");
	}

	public void setMinimized(boolean b) {
		this.minimized = b;
	}

	private void createStyles(StyleContext sc) {
		// default text
		Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
		regularText = sc.addStyle("MainStyle", defaultStyle);
		StyleConstants.setAlignment(regularText, StyleConstants.ALIGN_JUSTIFIED);
		StyleConstants.setFontSize(regularText, 11);
		StyleConstants.setFontFamily(regularText, "optima");
		StyleConstants.setSpaceAbove(regularText, 1);
		StyleConstants.setSpaceBelow(regularText, 3);
		StyleConstants.setLeftIndent(regularText, 5);
		StyleConstants.setRightIndent(regularText, 5);

		defaultRegularText = regularText;
		// StyleConstants.setFirstLineIndent(regularText, 20);

		// biggerRegularText
		this.biggerRegularText = sc.addStyle("biggerRegularText", regularText);
		StyleConstants.setFontSize(biggerRegularText, 13);

		// bold text
		this.boldText = sc.addStyle("boldText", regularText);
		StyleConstants.setBold(boldText, true);

		// heading text
		this.headingText = sc.addStyle("headingText", regularText);
		StyleConstants.setBold(headingText, true);
		StyleConstants.setForeground(headingText, Color.decode("0x000099"));
		StyleConstants.setFontSize(headingText, 12);
		StyleConstants.setSpaceBelow(headingText, 8);

		// subheading
		this.subheadingText = sc.addStyle("subheadingText", regularText);
		StyleConstants.setBold(subheadingText, true);
		StyleConstants.setForeground(subheadingText, Color.decode("0x000099"));
		StyleConstants.setFontSize(subheadingText, 11);
		StyleConstants.setItalic(subheadingText, true);
		StyleConstants.setSpaceBelow(subheadingText, 8);
		StyleConstants.setSpaceAbove(subheadingText, 30);
		defaultSubheadingText = subheadingText;
		// biggerSubHeadingText
		this.biggerHeadingText = sc.addStyle("biggerHeadingText", subheadingText);
		StyleConstants.setFontSize(biggerHeadingText, 14);

		// italic text
		this.italicText = sc.addStyle("italicText", regularText);
		StyleConstants.setItalic(italicText, true);

		// indented text
		indentedText = sc.addStyle("indent text", regularText);
		StyleConstants.setLeftIndent(indentedText, 30);
	}

	private static String removeHTMLTags(String string) {
		return string.replace(HTML_START, "").replace(HTML_END, "").replace(HTML_BOLD_START, "")
				.replace(HTML_BOLD_END, "").replace(HTML_ITALIC_START, "").replace(HTML_ITALIC_END, "")
				.replace(HTML_NEW_LINE, "").replace(HTML_BLANK_SPACE, " ");
	}

}
