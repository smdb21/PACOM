package org.proteored.pacom.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

public class AttachedHelpDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1119185903108814297L;
	private static final Logger log = Logger.getLogger(AttachedHelpDialog.class);
	private final Window parentFrame;
	private JPanel panel;
	private int maxMessageWidth = -1;
	private int leftInset = 20;
	private int rightInset = 20;
	private boolean minimized = false;
	private final int maxCharactersInHelpMessageRow;

	private static final String HTML_START = "<html>";
	private static final String HTML_END = "</html>";
	private static final String HTML_NEW_LINE = "<br>";
	private static final String HTML_BOLD = "<b>";

	/**
	 * @wbp.parser.constructor
	 */
	public AttachedHelpDialog(AbstractJFrameWithAttachedHelpDialog parentFrameWithHelp,
			int maxCharactersInHelpMessageRow) {

		this(parentFrameWithHelp, parentFrameWithHelp.getHelpMessages(), maxCharactersInHelpMessageRow);
	}

	public AttachedHelpDialog(AbstractJDialogWithAttachedHelpDialog parentDialogWithHelp,
			int maxCharactersInHelpMessageRow) {

		this(parentDialogWithHelp, parentDialogWithHelp.getHelpMessages(), maxCharactersInHelpMessageRow);
	}

	public AttachedHelpDialog(Window parentWindow, List<String> helpMessages, int maxCharactersInHelpMessageRow) {
		super();
		this.maxCharactersInHelpMessageRow = maxCharactersInHelpMessageRow;
		getContentPane().setBackground(SystemColor.info);
		setTitle("Help");
		setResizable(false);
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
		getContentPane().add(scrollPane, BorderLayout.NORTH);

		panel = new JPanel();
		panel.setBackground(SystemColor.info);
		panel.setBorder(null);
		scrollPane.setViewportView(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 15, 1000 };
		gbl_panel.rowHeights = new int[] { 0 };
		gbl_panel.columnWeights = new double[] { Double.MIN_VALUE };// 0.25, 0.5
																	// };
		gbl_panel.rowWeights = new double[] { Double.MIN_VALUE };
		panel.setLayout(gbl_panel);
		addWindowListeners();
		// include the delp messages
		GridBagConstraints c = new GridBagConstraints();

		c.insets = new Insets(30, leftInset, 0, rightInset);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
		// c.anchor = GridBagConstraints.LINE_START;
		if (helpMessages != null) {
			for (String helpMessage : helpMessages) {
				if (needsIndentation(helpMessage)) {
					c.gridx = 1;
					c.gridwidth = 1;
				} else {
					c.gridx = 0;
					c.gridwidth = 2;
				}
				JLabel label = new JLabel();
				label.setFont(new Font("Optima", Font.PLAIN, 16));
				String scapedHelpMessage = scapeNewLines(helpMessage, label.getFontMetrics(label.getFont()));
				label.setText(scapedHelpMessage);
				label.setHorizontalAlignment(SwingConstants.LEFT);

				this.panel.add(label, c);
				c.gridy++;
				// 2 inset on top, just the first is 30
				c.insets = new Insets(2, leftInset, 0, rightInset);
			}
		}
	}

	/**
	 * Identify if the message needs to be indented, which is when it starts by
	 * '-' or by '#.' where # is a number
	 * 
	 * @param helpMessage
	 * @return
	 */
	private boolean needsIndentation(String helpMessage) {
		if (helpMessage.trim().startsWith("-")) {
			return true;
		}
		String[] separators = { ".", "-" };
		for (String separator : separators) {

			if (helpMessage.contains(separator)) {
				try {
					final String[] split = helpMessage.split(separator.equals(".") ? "\\." : separator);
					Double.valueOf(split[0]);
					return true;
				} catch (NumberFormatException e) {

				}
			}
		}
		return false;
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
				log.info(e.getID() + " from " + e.getOldState() + " to " + e.getNewState());
				minimized = false;
				AttachedHelpDialog.this.setVisible(true);

			}

			@Override
			public void windowActivated(WindowEvent e) {
				log.info(e.getID() + " from " + e.getOldState() + " to " + e.getNewState() + " minimized=" + minimized);
				AttachedHelpDialog.this.setVisible(true);
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				log.info(e.getID() + " from " + e.getOldState() + " to " + e.getNewState() + " minimized=" + minimized);
				AttachedHelpDialog.this.setVisible(false);
			}

		});
		this.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				log.info(e.getID() + " " + e.getNewState() + " from " + e.getOldState());
			}

			@Override
			public void windowIconified(WindowEvent e) {
				log.info(e.getID() + " " + e.getNewState() + " from " + e.getOldState());
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				log.info(e.getID() + " " + e.getNewState() + " from " + e.getOldState());
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				log.info(e.getID() + " " + e.getNewState() + " from " + e.getOldState());
			}

			@Override
			public void windowClosing(WindowEvent e) {
				log.info(e.getID() + " " + e.getNewState() + " from " + e.getOldState());
				setVisible(false);
				minimized = true;

			}

			@Override
			public void windowClosed(WindowEvent e) {
				log.info(e.getID() + " " + e.getNewState() + " from " + e.getOldState());
			}

			@Override
			public void windowActivated(WindowEvent e) {
				log.info(e.getID() + " " + e.getNewState() + " from " + e.getOldState());
			}
		});
	}

	private String scapeNewLines(String helpMessage, FontMetrics fontMetrics) {
		if (helpMessage == null || "".equals(helpMessage)) {
			return HTML_START + "&nbsp;" + HTML_END;
		} else {
			boolean htmlSTART = false;
			boolean htmlEND = false;
			if (helpMessage.contains("\n")) {
				throw new IllegalArgumentException("\\n is not supported in help messages");
			}
			if (helpMessage.contains(HTML_BOLD)) {
				htmlSTART = true;
				htmlEND = true;
			}

			// check the length of all the lines to see if it is necessary to
			// chop the lines
			int numCharactersSafe = 0;
			if (helpMessage.length() > maxCharactersInHelpMessageRow) {
				htmlSTART = true;
				htmlEND = true;
				StringBuilder sb = new StringBuilder();
				final StringTokenizer stringTokenizer = new StringTokenizer(helpMessage);
				StringBuilder line = new StringBuilder();
				while (stringTokenizer.hasMoreTokens()) {
					final String word = stringTokenizer.nextToken();
					if (!"".equals(sb.toString())) {
						sb.append(" ");
					}
					if (!"".equals(line.toString())) {
						line.append(" ");
					}
					sb.append(word);
					line.append(word);
					if (sb.length() - numCharactersSafe >= maxCharactersInHelpMessageRow) {
						numCharactersSafe = sb.length();
						sb.append(HTML_NEW_LINE);
						maxMessageWidth = Math.max(maxMessageWidth, fontMetrics.stringWidth(line.toString()));
						line = new StringBuilder();
					}
				}
				maxMessageWidth = Math.max(maxMessageWidth, fontMetrics.stringWidth(line.toString()));
				helpMessage = sb.toString();
			} else {
				maxMessageWidth = Math.max(maxMessageWidth, fontMetrics.stringWidth(helpMessage));
			}

			if (htmlSTART && !helpMessage.startsWith(HTML_START)) {
				helpMessage = HTML_START + helpMessage;
			}

			if (htmlEND && !helpMessage.endsWith(HTML_END)) {
				helpMessage = helpMessage + HTML_END;
			}
			return helpMessage;
		}
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			if (!minimized) {
				log.info("setting help dialog visible to " + b);
				super.setVisible(b);
				positionNextToParent();
				parentFrame.requestFocus();
			}
		} else {
			log.info("setting help dialog visible to " + b);
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
		this.setSize(maxMessageWidth + this.leftInset + this.rightInset, this.parentFrame.getHeight());
		log.debug("Setting position next to the parent frame (" + x + "," + y + ")");
	}

	public void setMinimized(boolean b) {
		this.minimized = b;
	}
}
