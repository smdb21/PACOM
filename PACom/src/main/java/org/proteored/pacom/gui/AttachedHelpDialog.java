package org.proteored.pacom.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.JDialog;
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

public class AttachedHelpDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1119185903108814297L;
	private static final Logger log = Logger.getLogger(AttachedHelpDialog.class);
	private final Window parentFrame;
	private int maxMessageWidth = -1;

	private boolean minimized = false;
	private final int maxWidth;

	private static final String HTML_START = "<html>";
	private static final String HTML_END = "</html>";
	private static final String HTML_NEW_LINE = "<br>";
	private static final String HTML_BOLD_START = "<b>";
	private static final String HTML_BOLD_END = "</b>";
	private static final String HTML_ITALIC_START = "<i>";
	private static final String HTML_ITALIC_END = "</i>";
	private static final String HTML_BLANK_SPACE = "&nbsp;";
	private DefaultStyledDocument document;
	private Style regularText;
	private Style indentedText;
	private Style boldText;
	private Style italicText;
	private Style headingText;
	private JTextPane textPane;
	private Style subheadingText;

	/**
	 * @wbp.parser.constructor
	 */
	public AttachedHelpDialog(AbstractJFrameWithAttachedHelpDialog parentFrameWithHelp, int maxWidth) {

		this(parentFrameWithHelp, parentFrameWithHelp.getHelpMessages(), maxWidth);
	}

	public AttachedHelpDialog(AbstractJDialogWithAttachedHelpDialog parentDialogWithHelp,
			int maxCharactersInHelpMessageRow) {

		this(parentDialogWithHelp, parentDialogWithHelp.getHelpMessages(), maxCharactersInHelpMessageRow);
	}

	public AttachedHelpDialog(Window parentWindow, List<String> helpMessages, int maxWidth) {
		super();

		this.maxWidth = maxWidth;
		getContentPane().setBackground(SystemColor.info);
		setTitle("Help");
		setFocusableWindowState(false);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			e.printStackTrace();
		}
		this.parentFrame = parentWindow;

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		scrollPane.setViewportBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		StyleContext context = new StyleContext();
		createStyles(context);
		document = new DefaultStyledDocument(context);
		document.setLogicalStyle(0, regularText);

		textPane = new JTextPane(document);
		textPane.setBackground(SystemColor.info);
		textPane.setForeground(SystemColor.desktop);
		textPane.setEditable(false);
		scrollPane.setViewportView(textPane);
		addWindowListeners();
		// include the delp messages

		// c.anchor = GridBagConstraints.LINE_START;
		if (helpMessages != null) {
			try {
				boolean lookingForBoldStart = true;
				boolean lookingForItalicStart = true;
				boolean insideBold = false;
				boolean insideItalic = false;
				boolean firstTime = true;
				for (int i = 0; i < helpMessages.size(); i++) {
					String helpMessage = helpMessages.get(i);
					if (firstTime) {
						helpMessage = helpMessage.trim();
						firstTime = false;
					}

					if (helpMessage.startsWith(HTML_BOLD_START) && helpMessage.endsWith(HTML_BOLD_END)) {
						this.document.insertString(document.getLength(), removeHTMLTags(helpMessage) + "\n",
								subheadingText);
						firstTime = true;
						continue;
					}

					Style style = null;
					if (needsIndentation(helpMessage)) {
						style = indentedText;
					}
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
						this.document.insertString(document.getLength(), tmpMessage, style);
						helpMessage = helpMessage.substring(tmpMessage.length() + HTML_BOLD_START.length());
						lookingForBoldStart = false;
						insideBold = true;
						helpMessages.set(i, helpMessage);
						i--;
						continue;
					} else if (insideBold) {
						tmpMessage = helpMessage.substring(0, endBold);
						this.document.insertString(document.getLength(), tmpMessage, boldText);
						helpMessage = helpMessage.substring(tmpMessage.length() + HTML_BOLD_END.length());
						insideBold = false;
						lookingForBoldStart = true;
						helpMessages.set(i, helpMessage);
						i--;
						continue;
					} else if (!insideBold && !insideItalic && lookingForItalicStart && startItalic < startBold
							&& helpMessage.contains(HTML_ITALIC_END)) {

						tmpMessage = helpMessage.substring(0, startItalic);
						this.document.insertString(document.getLength(), tmpMessage, style);
						helpMessage = helpMessage.substring(tmpMessage.length() + HTML_ITALIC_START.length());
						lookingForItalicStart = false;
						insideItalic = true;
						helpMessages.set(i, helpMessage);
						i--;
						continue;
					} else if (insideItalic) {
						tmpMessage = helpMessage.substring(0, endItalic);
						this.document.insertString(document.getLength(), tmpMessage, italicText);
						helpMessage = helpMessage.substring(tmpMessage.length() + HTML_ITALIC_END.length());
						lookingForItalicStart = true;
						insideItalic = false;
						helpMessages.set(i, helpMessage);
						i--;
						continue;
					} else {
						this.document.insertString(document.getLength(), helpMessage, style);

					}

					this.document.insertString(document.getLength(), "\n", style);
					firstTime = true;
				}

			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

	}

	private void scrollToBeginning() {
		// scroll to beginning
		Rectangle r;
		try {
			r = textPane.modelToView(1);
			if (r != null) {
				textPane.scrollRectToVisible(r);
				textPane.repaint();
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}

	private void createStyles(StyleContext sc) {
		// default text
		Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
		regularText = sc.addStyle("MainStyle", defaultStyle);
		StyleConstants.setAlignment(regularText, StyleConstants.ALIGN_JUSTIFIED);
		StyleConstants.setFontSize(regularText, 13);
		StyleConstants.setFontFamily(regularText, "optima");
		StyleConstants.setSpaceAbove(regularText, 1);
		StyleConstants.setSpaceBelow(regularText, 3);
		StyleConstants.setLeftIndent(regularText, 10);
		StyleConstants.setRightIndent(regularText, 10);
		// StyleConstants.setFirstLineIndent(regularText, 20);

		// bold text
		this.boldText = sc.addStyle("boldText", regularText);
		StyleConstants.setBold(boldText, true);

		// heading text
		this.headingText = sc.addStyle("headingText", regularText);
		StyleConstants.setBold(headingText, true);
		StyleConstants.setForeground(headingText, Color.decode("0x000099"));
		StyleConstants.setFontSize(headingText, 15);
		StyleConstants.setSpaceBelow(headingText, 8);

		// subheading
		this.subheadingText = sc.addStyle("subheadingText", regularText);
		StyleConstants.setBold(subheadingText, true);
		StyleConstants.setForeground(subheadingText, Color.decode("0x000099"));
		StyleConstants.setFontSize(subheadingText, 14);
		StyleConstants.setItalic(subheadingText, true);
		StyleConstants.setSpaceBelow(subheadingText, 8);
		StyleConstants.setSpaceAbove(subheadingText, 30);
		// italic text
		this.italicText = sc.addStyle("italicText", regularText);
		StyleConstants.setItalic(italicText, true);

		// indented text
		indentedText = sc.addStyle("indent text", regularText);
		StyleConstants.setLeftIndent(indentedText, 30);
	}

	private String getIndentation() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			sb.append(HTML_BLANK_SPACE);
		}
		return sb.toString();
	}

	/**
	 * Identify if the message needs to be indented, which is when it starts by
	 * '-' or by '#.' where # is a number
	 * 
	 * @param helpMessage
	 * @return
	 */
	private boolean needsIndentation(String helpMessage) {
		return false;

		// if (helpMessage.trim().startsWith("-")) {
		// return true;
		// }
		// String[] separators = { ".", "-" };
		// for (String separator : separators) {
		//
		// if (helpMessage.contains(separator)) {
		// try {
		// final String[] split = helpMessage.split(separator.equals(".") ?
		// "\\." : separator);
		// Double.valueOf(split[0]);
		// return true;
		// } catch (NumberFormatException e) {
		//
		// }
		// }
		// }
		// return false;
	}

	private void addWindowListeners() {
		this.parentFrame.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
				log.info(e.getID());
				AttachedHelpDialog.this.setVisible(true);
			}

			@Override
			public void componentResized(ComponentEvent e) {
				log.info(e.getID());
				AttachedHelpDialog.this.setVisible(true);

			}

			@Override
			public void componentMoved(ComponentEvent e) {
				AttachedHelpDialog.this.setVisible(true);
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				log.info(e.getID());
				AttachedHelpDialog.this.setVisible(false);
			}
		});
		this.parentFrame.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				log.info(e.getID() + " from " + e.getOldState() + " to " + e.getNewState());
				// open by default
				AttachedHelpDialog.this.setVisible(true);
			}

			@Override
			public void windowClosing(WindowEvent e) {
				log.info(e.getID() + " from " + e.getOldState() + " to " + e.getNewState());

			}

			@Override
			public void windowClosed(WindowEvent e) {
				log.info(e.getID() + " from " + e.getOldState() + " to " + e.getNewState());
				AttachedHelpDialog.this.dispose();

			}

			@Override
			public void windowIconified(WindowEvent e) {
				log.info(e.getID() + " from " + e.getOldState() + " to " + e.getNewState());
				minimized = true;
				AttachedHelpDialog.this.setVisible(false);

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				log.debug(e.getID() + " from " + e.getOldState() + " to " + e.getNewState());
				minimized = false;
				AttachedHelpDialog.this.setVisible(true);

			}

			@Override
			public void windowActivated(WindowEvent e) {
				log.debug(
						e.getID() + " from " + e.getOldState() + " to " + e.getNewState() + " minimized=" + minimized);
				AttachedHelpDialog.this.setVisible(true);
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				log.debug(
						e.getID() + " from " + e.getOldState() + " to " + e.getNewState() + " minimized=" + minimized);
				AttachedHelpDialog.this.setVisible(false);
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

	// private String scapeNewLines(String helpMessage, FontMetrics fontMetrics)
	// {
	// if (helpMessage == null || "".equals(helpMessage)) {
	// return HTML_START + HTML_BLANK_SPACE + HTML_END;
	// } else {
	// boolean htmlSTART = false;
	// boolean htmlEND = false;
	// if (helpMessage.contains("\n")) {
	// throw new IllegalArgumentException("\\n is not supported in help
	// messages");
	// }
	// if (helpMessage.contains(HTML_BOLD)) {
	// htmlSTART = true;
	// htmlEND = true;
	// }
	// if (helpMessage.contains(HTML_BLANK_SPACE)) {
	// htmlSTART = true;
	// htmlEND = true;
	// }
	//
	// // check the length of all the lines to see if it is necessary to
	// // chop the lines
	// int numCharactersSafe = 0;
	// if (fontMetrics.stringWidth(removeHTMLTags(helpMessage)) > maxWidth) {
	// htmlSTART = true;
	// htmlEND = true;
	// StringBuilder sb = new StringBuilder();
	// final StringTokenizer stringTokenizer = new StringTokenizer(helpMessage);
	// StringBuilder line = new StringBuilder();
	// while (stringTokenizer.hasMoreTokens()) {
	// final String word = stringTokenizer.nextToken();
	// if (!"".equals(sb.toString())) {
	// sb.append(" ");
	// }
	// if (!"".equals(line.toString())) {
	// line.append(" ");
	// }
	// sb.append(word);
	// line.append(word);
	// if (fontMetrics.stringWidth(removeHTMLTags(line.toString())) >= maxWidth)
	// {
	// numCharactersSafe = sb.length();
	// sb.append(HTML_NEW_LINE);
	// int lineWidth = fontMetrics.stringWidth(removeHTMLTags(line.toString()));
	// maxMessageWidth = Math.max(maxMessageWidth, lineWidth);
	// log.info("max width " + maxMessageWidth + " line width " + lineWidth);
	// line = new StringBuilder();
	// }
	// }
	// maxMessageWidth = Math.max(maxMessageWidth,
	// fontMetrics.stringWidth(line.toString()));
	// log.info("max width " + maxMessageWidth + " " + line.toString());
	// helpMessage = sb.toString();
	// } else {
	// maxMessageWidth = Math.max(maxMessageWidth,
	// fontMetrics.stringWidth(helpMessage));
	// log.info("max width " + maxMessageWidth + " " + helpMessage.toString());
	// }
	//
	// if (htmlSTART && !helpMessage.startsWith(HTML_START)) {
	// helpMessage = HTML_START + helpMessage;
	// }
	//
	// if (htmlEND && !helpMessage.endsWith(HTML_END)) {
	// helpMessage = helpMessage + HTML_END;
	// }
	// return helpMessage;
	// }
	// }

	private static String removeHTMLTags(String string) {
		return string.replace(HTML_START, "").replace(HTML_END, "").replace(HTML_BOLD_START, "")
				.replace(HTML_BOLD_END, "").replace(HTML_ITALIC_START, "").replace(HTML_ITALIC_END, "")
				.replace(HTML_NEW_LINE, "").replace(HTML_BLANK_SPACE, " ");
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
				scrollToBeginning();
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

}
