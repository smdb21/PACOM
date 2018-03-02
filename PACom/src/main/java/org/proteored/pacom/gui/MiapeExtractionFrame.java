/*
 * Standard2MIAPEDialog.java Created on __DATE__, __TIME__
 */

package org.proteored.pacom.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;

import org.apache.commons.io.FilenameUtils;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.exceptions.MiapeDataInconsistencyException;
import org.proteored.miapeapi.text.tsv.msi.TableTextFileColumn;
import org.proteored.pacom.analysis.util.FileManager;
import org.proteored.pacom.gui.importjobs.AssociatedMSInputFileType;
import org.proteored.pacom.gui.importjobs.ImportTaskColumns;
import org.proteored.pacom.gui.importjobs.ImportTaskDataModel;
import org.proteored.pacom.gui.importjobs.ImportTasksTable;
import org.proteored.pacom.gui.importjobs.ImportTasksUtil;
import org.proteored.pacom.gui.importjobs.InputFileType;
import org.proteored.pacom.gui.importjobs.ScrollableImportTaskJPanel;
import org.proteored.pacom.gui.importjobs.UpdatableComboBoxEditor;
import org.proteored.pacom.gui.tasks.InputDataTypeGuesser;
import org.proteored.pacom.gui.tasks.LoadProjectsTask;
import org.proteored.pacom.gui.tasks.MIAPEMSChecker;
import org.proteored.pacom.gui.tasks.MiapeExtractionTask;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;
import org.proteored.pacom.gui.tasks.OntologyLoaderWaiter;
import org.proteored.pacom.utils.AppVersion;
import org.proteored.pacom.utils.ComponentEnableStateKeeper;
import org.proteored.pacom.utils.MiapeExtractionBatchManager;
import org.proteored.pacom.utils.MiapeExtractionResult;
import org.proteored.pacom.utils.MiapeExtractionRunParametersImpl;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 *
 * @author __USER__
 */
public class MiapeExtractionFrame extends AbstractJFrameWithAttachedHelpDialog implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2685612413712062973L;
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("log4j.logger.org.proteored");
	private static MiapeExtractionFrame instance;

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	private final ComponentEnableStateKeeper enableStateKeeper = new ComponentEnableStateKeeper();
	private MiapeExtractionBatchManager miapeExtractorBatchManager;

	@Override
	public void dispose() {
		if (miapeExtractorBatchManager != null && !miapeExtractorBatchManager.getRunningJobs().isEmpty()) {
			miapeExtractorBatchManager.cancelMiapeExtractions();
		}
		if (this.inputDataTypeGuesser != null) {
			inputDataTypeGuesser.cancel(true);
		}
		if (mainFrame != null) {
			mainFrame.setEnabled(true);
			mainFrame.setVisible(true);
		}
		super.dispose();
	}

	// for mzML conversion
	public boolean isFastParsing = false;
	public boolean isShallowParsing = false;
	private boolean showProjectTable;
	private ScrollableImportTaskJPanel scrollableImportTaskTable;
	private File[] selectedInputFiles;
	// private AutoSuggestor autoSuggestor;
	private JButton jButtonOpenMSFormDialogButton;
	private InputDataTypeGuesser inputDataTypeGuesser;

	public static MiapeExtractionFrame getInstance(MainFrame mainFrame2, boolean b) {
		if (instance == null) {
			instance = new MiapeExtractionFrame(mainFrame2, b);
		}
		instance.mainFrame = mainFrame2;
		instance.initializeFrame();

		return instance;
	}

	/** Creates new form Standard2MIAPEDialog */
	private MiapeExtractionFrame(MainFrame parent, boolean modal) {
		super(400);
		setPreferredSize(new Dimension(1000, 650));
		setSize(new Dimension(990, 650));
		getContentPane().setSize(new Dimension(800, 600));
		getContentPane().setMaximumSize(new Dimension(800, 600));
		// super(parent, modal);
		initComponents();

		if (parent != null) {

			mainFrame = parent;
			mainFrame.setEnabled(false);
			mainFrame.setVisible(false);

			// autoscroll in the status field
			// this.mainFrame.autoScroll(jScrollPane1, jTextAreaStatus);
		} else {

		}

		// Load projects in background
		loadProjects(false, true);

		FileManager.deleteMetadataFile(MIAPEMSChecker.CURRENT_MZML);
		FileManager.deleteMetadataFile(MIAPEMSChecker.CURRENT_PRIDEXML);

		// set icon image
		setIconImage(ImageManager.getImageIcon(ImageManager.LOAD_LOGO_128).getImage());
		jButtonSubmit.setIcon(ImageManager.getImageIcon(ImageManager.ADD));
		jButtonSubmit.setPressedIcon(ImageManager.getImageIcon(ImageManager.ADD_CLICKED));
		jButtonCancel.setIcon(ImageManager.getImageIcon(ImageManager.STOP));
		jButtonCancel.setPressedIcon(ImageManager.getImageIcon(ImageManager.STOP_CLICKED));
		jButtonGoToData.setIcon(ImageManager.getImageIcon(ImageManager.PACOM_LOGO_32));
		jButtonGoToData.setPressedIcon(ImageManager.getImageIcon(ImageManager.PACOM_LOGO_32_CLICKED));

		// wait for the ontology loading. When done, it will notify to this
		// class and metadata combo will be able to be filled
		appendStatus("Loading ontologies...");
		OntologyLoaderWaiter waiter = new OntologyLoaderWaiter();
		waiter.addPropertyChangeListener(this);
		waiter.execute();

		AppVersion version = MainFrame.getVersion();
		if (version != null) {
			String suffix = " (v" + version.toString() + ")";
			this.setTitle(getTitle() + suffix);
		}
		getContentPane().setLayout(new BorderLayout(5, 5));
		getContentPane().add(jPanelSouth, BorderLayout.SOUTH);
		getContentPane().add(jPanelNorth, BorderLayout.NORTH);
		getContentPane().add(jPanelCenter);
		jPanelCenter.setLayout(new BorderLayout(0, 10));
		jPanelCenter.add(panel_2, BorderLayout.NORTH);

		panel = new JPanel();
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setBorder(new TitledBorder(null, "Select input files to import", TitledBorder.LEADING, TitledBorder.TOP,
				null, null));

		panel_1 = new JPanel();
		panel_1.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_1.setBorder(new TitledBorder(null, "Associate an MS file to the selected input file",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));

		panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(null, "Delete selected import task", TitledBorder.LEADING, TitledBorder.TOP,
				null, null));

		panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(null, "Add or modify Mass Spectrometry metadata", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		panel_4.setLayout(new GridLayout(0, 1, 0, 0));
		jButtonOpenMSFormDialogButton = new JButton("Manage MS metadata");
		jButtonOpenMSFormDialogButton.setEnabled(false);
		panel_4.add(jButtonOpenMSFormDialogButton);
		jButtonOpenMSFormDialogButton.setToolTipText(
				"Click here to add or edit the metadata templates that you can use to complement associated MS files");
		jButtonOpenMSFormDialogButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				MiapeMSFormsDialog.getInstance(MiapeExtractionFrame.this, getControlVocabularyManager())
						.setVisible(true);
			}
		});
		panel_3.setLayout(new GridLayout(0, 1, 0, 0));

		btnDeleteImportTask = new JButton("Delete import task");
		btnDeleteImportTask.setToolTipText("Delete the selected import task from the table");
		panel_3.add(btnDeleteImportTask);
		btnDeleteImportTask.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteImportTaskAction(e);
			}
		});
		btnDeleteImportTask.setEnabled(false);
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		jButtonInputFile = new javax.swing.JButton();
		panel.add(jButtonInputFile);

		jButtonInputFile.setText("Select input files");
		jButtonInputFile.setToolTipText("Select one or more input data files to import");
		jButtonInputFile.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonInputFileActionPerformed(evt);
			}
		});
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));
		panel_2.add(panel);
		panel_2.add(panel_1);
		panel_1.setLayout(new GridLayout(0, 1, 0, 0));
		jButtonInputFileAttach = new javax.swing.JButton();
		jButtonInputFileAttach.setEnabled(false);
		jButtonInputFileAttach.setText("Associate MS file");
		jButtonInputFileAttach
				.setToolTipText("<html>Select one or more MS files to associate with the created import jobs.<br>"
						+ "You can also select them by clicking on the corresponding cell<br>" + "of the column '"
						+ ImportTaskColumns.ASSOCIATEDMSFILE.getName() + "'</html>");
		panel_1.add(jButtonInputFileAttach);
		jButtonInputFileAttach.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonInputfileAttachActionPerformed(evt);
			}
		});
		panel_2.add(panel_3);
		panel_2.add(panel_4);
		// the problem is that the metadata templates takes time, because it is
		// parsing all files in the folder
		scrollableImportTaskTable = new ScrollableImportTaskJPanel();
		jPanelCenter.add(scrollableImportTaskTable, BorderLayout.CENTER);

		JPanel jPanelEast = new JPanel();
		getContentPane().add(jPanelEast, BorderLayout.EAST);

		// setup the autosuggestor
		// autoSuggestor = new AutoSuggestor(jTextFieldProjectName, this, null,
		// Color.WHITE.brighter(), Color.blue,
		// Color.red, 0.75f);
		// loadProjectsFromDisk();

		/// listener to change the radio buttons depending on the file type of
		/// the selected row
		// and to enable or disable the button for row deletion
		ListSelectionListener listener = new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				if (e.getSource() instanceof DefaultListSelectionModel) {
					DefaultListSelectionModel model = (DefaultListSelectionModel) e.getSource();

					int firstIndex = model.getMinSelectionIndex();
					if (firstIndex >= 0) {
						// enable button for row deletion
						MiapeExtractionFrame.this.btnDeleteImportTask.setEnabled(true);
					} else {
						// disable button for row deletion
						MiapeExtractionFrame.this.btnDeleteImportTask.setEnabled(false);
					}
				}
			}
		};
		scrollableImportTaskTable.addTableSelectionListener(listener);

		addTableMouseListeners();
		addKeyListener();
		asynchronouslyLoadMetadataTemplateNames();
		enableStateKeeper.addReverseComponent(jButtonCancel);

		panelWest = new JPanel();
		getContentPane().add(panelWest, BorderLayout.WEST);
		// enableStateKeeper.addInvariableComponent(scrollableImportTaskTable);
		tableChangeListener();
	}

	private void tableChangeListener() {
		// listener to change the file type
		TableModelListener fileTypeListener = new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				log.debug("Table changed");

			}
		};
		scrollableImportTaskTable.getTable().getModel().addTableModelListener(fileTypeListener);
	}

	private void addKeyListener() {
		scrollableImportTaskTable.getTable().addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					ImportTasksTable table = (ImportTasksTable) e.getSource();
					int[] rowIndexes = table.getSelectedRows();

					int columnIndex = table.getSelectedColumn();
					ImportTaskColumns columnType = ImportTaskColumns.values()[columnIndex];
					if (columnType == ImportTaskColumns.ASSOCIATEDMSFILE) {
						for (int rowIndex : rowIndexes) {
							MiapeExtractionTask task = table.getImportTaskTableModel().getTaskByRowIndex(rowIndex);
							((MiapeExtractionRunParametersImpl) task.getParameters()).setAssociatedMSFile(null);
							table.setRowSelectionInterval(rowIndex, rowIndex);
						}
						table.getImportTaskTableModel().fireTableDataChanged();

					} else if (columnType == ImportTaskColumns.FILE) {
						for (int rowIndex : rowIndexes) {
							MiapeExtractionTask task = table.getImportTaskTableModel().getTaskByRowIndex(rowIndex);
							((MiapeExtractionRunParametersImpl) task.getParameters()).setInputFile(null);
							table.setRowSelectionInterval(rowIndex, rowIndex);
						}
						table.getImportTaskTableModel().fireTableDataChanged();
					} else {
						if (rowIndexes.length > 0) {
							String plural = rowIndexes.length > 1 ? "s" : "";
							StringBuilder identifiers = new StringBuilder();
							for (int rowIndex : rowIndexes) {
								if (!"".equals(identifiers.toString())) {
									identifiers.append(",");
								}
								identifiers.append(
										table.getImportTaskTableModel().getTaskByRowIndex(rowIndex).getRunIdentifier());
							}
							// ask if the user wants to delete the row
							int option = JOptionPane.showConfirmDialog(MiapeExtractionFrame.this,
									"Are you sure you want to delete import task" + plural + " (ID" + plural + ":'"
											+ identifiers.toString() + "')",
									"Delete import task", JOptionPane.YES_NO_OPTION);
							if (option == JOptionPane.OK_OPTION) {
								table.getImportTaskTableModel().removeRows(rowIndexes);
							}
						}
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void addTableMouseListeners() {
		// add mouse listener to files
		MouseListener mouseListener = new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					ImportTasksTable table = (ImportTasksTable) e.getSource();
					int rowIndex = table.getSelectedRow();
					if (table.getRowSorter() != null) {
						rowIndex = table.getRowSorter().convertRowIndexToModel(rowIndex);
					}
					MiapeExtractionTask task = table.getImportTaskTableModel().getTaskByRowIndex(rowIndex);
					int columnIndex = table.getSelectedColumn();
					log.info("Row=" + rowIndex + " Column=" + columnIndex);
					if (e.getClickCount() == 2) {
						log.debug("It is a double click");
						ImportTaskColumns columnType = ImportTaskColumns.values()[columnIndex];
						if (columnType == ImportTaskColumns.FILE) {
							File[] newFiles = selectFiles(false);
							File file = newFiles[0];
							// set file to table model
							((MiapeExtractionRunParametersImpl) task.getParameters()).setInputFile(file);
							// guess the type
							guessInputDataTypes(file, task);
							// update row
							scrollableImportTaskTable.getTable().getImportTaskTableModel().fireTableRowsUpdated(task);
						} else if (columnType == ImportTaskColumns.ASSOCIATEDMSFILE) {

							List<File> newFiles = selectAttachedMSFiles(null);
							if (newFiles != null && !newFiles.isEmpty()) {
								// set value of new file
								((MiapeExtractionRunParametersImpl) task.getParameters())
										.setAssociatedMSFile(newFiles.get(0));
								// try to guess the new value of the associated
								// ms FIle
								String extension = FilenameUtils.getExtension(newFiles.get(0).getAbsolutePath());
								if (extension != null) {
									if ("mgf".equalsIgnoreCase(extension)) {
										((MiapeExtractionRunParametersImpl) task.getParameters())
												.setAssociatedMSFileType(AssociatedMSInputFileType.MGF);
										// update row
										scrollableImportTaskTable.getTable().getImportTaskTableModel()
												.fireTableRowsUpdated(task);
									} else if ("mzml".equalsIgnoreCase(extension)) {
										((MiapeExtractionRunParametersImpl) task.getParameters())
												.setAssociatedMSFileType(AssociatedMSInputFileType.MZML);
										// update row
										scrollableImportTaskTable.getTable().getImportTaskTableModel()
												.fireTableRowsUpdated(task);
									} else {
										log.info("I cannot guess what type of file is "
												+ newFiles.get(0).getAbsolutePath());
									}
								}
							}
						} else {
							log.info("Ignoring double click");
						}
					} else if (e.getClickCount() == 1) {
						log.debug("It is a single click");
						if (columnIndex >= 0) {
							ImportTaskColumns columnType = ImportTaskColumns.values()[columnIndex];
							if (columnType == ImportTaskColumns.FILE
									|| columnType == ImportTaskColumns.ASSOCIATEDMSFILE) {
								appendStatus("Double click to select a new file");
							}
						}
					}
				} catch (IllegalArgumentException ex) {
					ex.printStackTrace();
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		};
		scrollableImportTaskTable.getTable().addMouseListener(mouseListener);
	}

	protected void deleteImportTaskAction(ActionEvent e) {
		int[] selectedRowIndexes = scrollableImportTaskTable.getTable().getSelectedRows();
		if (selectedRowIndexes.length > 0) {
			int[] jobIDs = scrollableImportTaskTable.getTable().getImportTaskTableModel()
					.removeRows(selectedRowIndexes);
			for (int i = 0; i < jobIDs.length; i++) {
				appendStatus("Import task '" + jobIDs[i] + "' in row '" + (selectedRowIndexes[i] + 1) + "' deleted.");
			}
		}
	}

	private void asynchronouslyLoadMetadataTemplateNames() {
		Runnable task = () -> {

			List<String> metadataList = new ArrayList<String>();
			while (!FileManager.isMetadataTemplatesLoaded()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			log.debug("Wait for metadata templates finished");

			List<String> list = FileManager.getMetadataTemplateList(cvManager);
			metadataList.addAll(list);
			// sort by name
			if (metadataList != null) {
				Collections.sort(metadataList);
				if (metadataList.isEmpty() || !"".equals(metadataList.get(0))) {
					metadataList.add(0, "");
				}

				// load them into the cell editor of the corresponding column
				TableColumn column = scrollableImportTaskTable.getTable()
						.getColumn(ImportTaskColumns.METADATA_TEMPLATE.getName());
				if (column != null && column.getCellEditor() instanceof UpdatableComboBoxEditor) {
					UpdatableComboBoxEditor<String> cellEditor = (UpdatableComboBoxEditor<String>) column
							.getCellEditor();
					cellEditor.setComboItems(metadataList);
					scrollableImportTaskTable.getTable().getImportTaskTableModel().fireTableDataChanged();
				}
			}
			// enable manage ms metadata button
			appendStatus("MS metadata templates loaded");
			jButtonOpenMSFormDialogButton.setEnabled(true);
		};
		new Thread(task).start();

	}

	protected void jButtonInputfileAttachActionPerformed(ActionEvent evt) {
		// get input file type from the table, from the selected row
		InputFileType inputFileType = this.scrollableImportTaskTable.getSelectedInputFileTypeFromSelectedRow();
		List<File> attachedMSFiles = selectAttachedMSFiles(inputFileType);

		// if the user selected one single file, ask whether he wants to
		// assign it to all input files or not
		// if the user selected more than one file, check if they are the
		// same number of rows in the table. If so, assign one by one. If
		// not, throw error message.d
		if (attachedMSFiles != null && !attachedMSFiles.isEmpty()) {
			List<MiapeExtractionTask> importTasks = this.scrollableImportTaskTable.getTable().getImportTasks();
			List<MiapeExtractionTask> selectedImportTasks = this.scrollableImportTaskTable.getTable()
					.getSelectedImportTasks();
			int rowCount = importTasks.size();
			if (attachedMSFiles.size() == 1) {
				if (rowCount == 0) {
					// throw error
					log.error("This shoudn't happen. Button should be disable if there is not data in the table");
					appendStatus("Select an input file before selecting associated MS files");
				} else if (rowCount == 1) {
					int firstJobID = importTasks.get(0).getRunIdentifier();
					// add to the row in the table
					scrollableImportTaskTable.getTable().associateMSFileToJobID(attachedMSFiles.get(0), firstJobID);
				} else {
					// there is more than one job in the table and 1 MS File
					int firstJobID = importTasks.get(0).getRunIdentifier();
					// check if there is selected rows

					if (selectedImportTasks.isEmpty() || selectedImportTasks.size() == importTasks.size()) {
						String message = "You selected one MS file, but in the table there are " + rowCount
								+ " import tasks.\nDo you want to associate it to JUST the first one (jobID="
								+ firstJobID + ") or to ALL of them";
						String title = "How to associate MS file to import tasks";
						String[] options = { "associate to first import task", "associate to all tasks" };
						Object userSelection = JOptionPane.showInputDialog(this, message, title,
								JOptionPane.WARNING_MESSAGE, null, options, "associate to all");
						if ("associate to all tasks".equals(userSelection)) {
							scrollableImportTaskTable.getTable().associateMSFileToAll(attachedMSFiles.get(0));
						} else if ("associate to first import task".equals(userSelection)) {
							scrollableImportTaskTable.getTable().associateMSFileToJobID(attachedMSFiles.get(0),
									firstJobID);
						} else {
							appendStatus("No MS file was associated");
						}
					} else {
						if (selectedImportTasks.size() != importTasks.size()) {
							String message = "You selected one MS file, and you have " + selectedImportTasks.size()
									+ " import tasks selected in the table.\nDo you want to associate it to just JUST of the selected tasks or to ALL the jobs in the table";
							String title = "How to associate MS file to import tasks";
							String[] options = { "associate to selected tasks", "associate to all tasks" };
							Object userSelection = JOptionPane.showInputDialog(this, message, title,
									JOptionPane.WARNING_MESSAGE, null, options, "associate to selected tasks");
							if ("associate to all tasks".equals(userSelection)) {
								scrollableImportTaskTable.getTable().associateMSFileToAll(attachedMSFiles.get(0));
							} else if ("associate to selected tasks".equals(userSelection)) {
								for (MiapeExtractionTask task : selectedImportTasks) {
									scrollableImportTaskTable.getTable().associateMSFileToJobID(attachedMSFiles.get(0),
											task.getRunIdentifier());
								}

							} else {
								appendStatus("No MS file was associated");
							}
						}
					}

				}

			} else {
				// more than one file
				if (rowCount == 0) {
					// throw error
					log.error("This shoudn't happen. Button should be disable if there is not data in the table");
					appendStatus("Select an input file before selecting associated MS files");
				} else if (rowCount == 1) {
					JOptionPane.showConfirmDialog(this,
							"You selected more than one MS file but there is only one import task loaded in the table.\n"
									+ "Select either just one MS file or more load more import taks first by selecting input files.",
							"Error", JOptionPane.OK_OPTION);
				} else {
					// check if there is selected rows
					if (attachedMSFiles.size() != selectedImportTasks.size()
							&& attachedMSFiles.size() != importTasks.size()) {
						JOptionPane.showConfirmDialog(this,
								"You selected " + attachedMSFiles.size() + " MS files but there are "
										+ importTasks.size() + " import task loaded in the table (and "
										+ selectedImportTasks.size() + " of them selected)\n"
										+ "Either associated them one by one or select the same number of MS files, so that they will be associated in the same order they were selected.",
								"Error", JOptionPane.OK_OPTION);
					} else if (importTasks.size() == selectedImportTasks.size()) {
						// add one by one in order
						int i = 0;
						for (MiapeExtractionTask task : importTasks) {
							scrollableImportTaskTable.getTable().associateMSFileToJobID(attachedMSFiles.get(i++),
									task.getRunIdentifier());
						}
					} else if (attachedMSFiles.size() == selectedImportTasks.size()) {
						// warn and add one by one
						String message = "You selected " + attachedMSFiles.size() + " MS files and there are "
								+ selectedImportTasks.size() + " import task selected in the table.\n"
								+ "Do you want to associated them in order to the selected import tasks?";
						String title = "How to associate MS file to import tasks";

						int userSelection = JOptionPane.showConfirmDialog(this, message, title,
								JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
						if (userSelection == JOptionPane.YES_OPTION) {
							int i = 0;
							for (MiapeExtractionTask task : selectedImportTasks) {
								scrollableImportTaskTable.getTable().associateMSFileToJobID(attachedMSFiles.get(i++),
										task.getRunIdentifier());
							}

						} else {
							appendStatus("No MS file was associated");
						}
					} else if (attachedMSFiles.size() == importTasks.size()) {
						// warn and add one by one
						// warn and add one by one
						String message = "You selected " + attachedMSFiles.size() + " MS files and there are "
								+ importTasks.size() + " import task in the table.\n"
								+ "Do you want to associated them in order to all import tasks in the table?";
						String title = "How to associate MS file to import tasks";

						int userSelection = JOptionPane.showConfirmDialog(this, message, title,
								JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
						if (userSelection == JOptionPane.YES_OPTION) {
							int i = 0;
							for (MiapeExtractionTask task : importTasks) {
								scrollableImportTaskTable.getTable().associateMSFileToJobID(attachedMSFiles.get(i++),
										task.getRunIdentifier());
							}

						} else {
							appendStatus("No MS file was associated");
						}
					}

				}

			}

		} else {
			appendStatus("No files selected or valid");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Window#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean b) {
		// check if the mainFrame user id has change. In that case, remove the
		// loaded projects

		loadProjects(true, true);

		if (mainFrame != null) {
			mainFrame.setVisible(!b);
		}
		super.setVisible(b);
	}

	/**
	 * Loads projects from repository in background
	 * 
	 * @param b
	 */
	private void loadProjects(boolean forceChange, boolean silence) {
		if (loadedProjects == null || loadedProjects.isEmpty() || forceChange) {
			if (loadedProjects != null)
				loadedProjects.clear();
			loadProjectsFromDisk(silence);

		} else {
			showLoadedProjectTable();
		}

	}

	private void loadProjectsFromDisk(boolean silence) {
		LoadProjectsTask loadProjectsThread = new LoadProjectsTask(this);
		if (!silence) {
			loadProjectsThread.addPropertyChangeListener(this);
		}
		loadProjectsThread.execute();
	}

	boolean isGeneratingMS() {
		List<MiapeExtractionTask> tasks = scrollableImportTaskTable.getTable().getImportTasks();
		for (MiapeExtractionTask miapeExtractionTask : tasks) {
			if (miapeExtractionTask.getParameters().getAssociatedMSFileType() != null) {
				return true;
			}
		}
		return false;
	}

	private void initializeFrame() {

		jTextAreaStatus.setText("");
		// jTextFieldProjectName.setText("");
		jProgressBar.setIndeterminate(false);
		this.setCursor(null); // turn off the wait cursor
		jButtonSubmit.setEnabled(true);
		if (miapeExtractorBatchManager != null && !miapeExtractorBatchManager.getRunningJobs().isEmpty()) {
			miapeExtractorBatchManager.cancelMiapeExtractions();
			log.info("Tasks canceled ");
			miapeExtractorBatchManager = null;
		}

	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		buttonGroupInputFileFormat = new javax.swing.ButtonGroup();
		jFileChooser = new JFileChooser(MainFrame.currentFolder);

		jPanelCenter = new javax.swing.JPanel();
		jPanelCenter.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanelNorth = new javax.swing.JPanel();
		jTextFieldProjectName = new javax.swing.JTextField("my default dataset folder");
		jTextFieldProjectName.setColumns(100);
		jButtonProject = new javax.swing.JButton();
		jPanelSouth = new javax.swing.JPanel();
		jPanelSouth.setPreferredSize(new Dimension(10, 170));
		jScrollPane1 = new javax.swing.JScrollPane();
		jTextAreaStatus = new javax.swing.JTextArea();
		jProgressBar = new javax.swing.JProgressBar();
		jButtonCancel = new javax.swing.JButton();
		jButtonCancel.setEnabled(false);
		jButtonSubmit = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Import data");

		panel_2 = new JPanel();
		panel_2.setAlignmentX(Component.LEFT_ALIGNMENT);

		jPanelNorth.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
				"Type a name to create a new dataset folder, or select one from the list (click on 'Select dataset folder')",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		jPanelNorth.setToolTipText(
				"<html>Write directly a new dataset folder name to create a new<br>\n folder in which the datasets will be stored.</html>");

		jTextFieldProjectName.setToolTipText(
				"<html>Write directly a new dataset folder name to create a new<br>\n folder in which the datasets will be stored.</html>");

		jButtonProject.setText("Select dataset folder");
		jButtonProject.setToolTipText(
				"<html>Select one of the dataset folders<br>or write a new name to create a new one</html>");
		jButtonProject.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonProjectActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout gl_jPanelNorth = new javax.swing.GroupLayout(jPanelNorth);
		gl_jPanelNorth
				.setHorizontalGroup(gl_jPanelNorth.createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING,
						gl_jPanelNorth.createSequentialGroup().addContainerGap()
								.addComponent(jTextFieldProjectName, GroupLayout.PREFERRED_SIZE, 223,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(jButtonProject)
								.addContainerGap(667, Short.MAX_VALUE)));
		gl_jPanelNorth.setVerticalGroup(gl_jPanelNorth.createParallelGroup(Alignment.LEADING).addGroup(gl_jPanelNorth
				.createSequentialGroup().addGap(11)
				.addGroup(gl_jPanelNorth.createParallelGroup(Alignment.BASELINE)
						.addComponent(jTextFieldProjectName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonProject))
				.addContainerGap(26, Short.MAX_VALUE)));
		jPanelNorth.setLayout(gl_jPanelNorth);

		jPanelSouth.setBorder(javax.swing.BorderFactory.createTitledBorder("Status"));

		jScrollPane1.setAutoscrolls(true);

		jTextAreaStatus.setColumns(20);
		jTextAreaStatus.setEditable(false);
		jTextAreaStatus.setFont(new java.awt.Font("Dialog", 0, 10));
		jTextAreaStatus.setRows(5);
		jTextAreaStatus.setToolTipText("Task status");
		jScrollPane1.setViewportView(jTextAreaStatus);

		jButtonCancel.setText("Cancel");
		jButtonCancel.setToolTipText("Cancel current task");
		jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonCancelActionPerformed(evt);
			}
		});

		jButtonSubmit.setText("Import data");
		jButtonSubmit.setToolTipText("Start with the import of the dataset from the input file(s)");
		jButtonSubmit.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonSubmitActionPerformed(evt);
			}
		});

		jButtonGoToData = new JButton("Go to Comparison Project Manager");
		jButtonGoToData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				goToDataInspection();
			}
		});
		jButtonGoToData.setToolTipText("Click here to go directly to the Comparison Project Manager");

		jButtonHelp = new OpenHelpButton(this);

		javax.swing.GroupLayout gl_jPanelSouth = new javax.swing.GroupLayout(jPanelSouth);
		gl_jPanelSouth.setHorizontalGroup(gl_jPanelSouth.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_jPanelSouth.createSequentialGroup().addContainerGap()
						.addGroup(gl_jPanelSouth.createParallelGroup(Alignment.TRAILING)
								.addComponent(jProgressBar, GroupLayout.DEFAULT_SIZE, 961, Short.MAX_VALUE)
								.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 961, Short.MAX_VALUE)
								.addGroup(gl_jPanelSouth.createSequentialGroup().addComponent(jButtonGoToData)
										.addPreferredGap(ComponentPlacement.RELATED, 443, Short.MAX_VALUE)
										.addComponent(jButtonCancel).addGap(18).addComponent(jButtonSubmit).addGap(18)
										.addComponent(jButtonHelp)))
						.addContainerGap()));
		gl_jPanelSouth.setVerticalGroup(gl_jPanelSouth.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_jPanelSouth.createSequentialGroup()
						.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(jProgressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_jPanelSouth.createParallelGroup(Alignment.TRAILING)
								.addGroup(gl_jPanelSouth.createParallelGroup(Alignment.BASELINE)
										.addComponent(jButtonSubmit, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(jButtonCancel))
								.addComponent(jButtonGoToData).addComponent(jButtonHelp))));
		jPanelSouth.setLayout(gl_jPanelSouth);

		pack();
		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		java.awt.Dimension dialogSize = getSize();
		setLocation((screenSize.width - dialogSize.width) / 2, (screenSize.height - dialogSize.height) / 2);
	}// </editor-fold>

	protected void goToDataInspection() {
		this.setVisible(false);
		this.mainFrame.startProjectComparison();
	}

	public static String getTextTableFormatText1() {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<html>PACOM supports text files containing a table with different predefined columns, separated by a symbol (select one in the combo-box).<br>");
		sb.append(
				"These files <b>MUST</b> contain a header in which the following <b>header names</b> are allowed at the first row:");
		sb.append("<ul>");
		for (TableTextFileColumn columnName : TableTextFileColumn.values()) {
			sb.append(getHeaderString(columnName));
		}
		sb.append("</ul>");
		sb.append("Any other header name different than any in that list will be recognized as a <b>new score</b>,"
				+ " and values in that column should be real numbers.<br>"
				+ "Here the user could insert any value, being a score or not, that want to evaluate looking to<br>"
				+ " its distribution and looking how its value change between datasets for the same peptide.");
		sb.append("Lines starting by '<b>#</b>' will be <b>ignored</b>.");
		return sb.toString();
	}

	private static String getHeaderString(TableTextFileColumn column) {
		String mandatory = column.isMandatory() ? " (mandatory)" : " (optional)";
		return "<li><b>" + column.getHeaderName() + "</b>" + mandatory + ": " + column.getHeaderExplanation() + "</li>";
	}

	private void jButtonInputFileActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			selectFilesAndGuessTypes();
		} catch (Exception e) {
			appendStatus(e.getMessage());
		}
	}

	private void selectFilesAndGuessTypes() {

		selectedInputFiles = selectFiles(true);
		guessInputDataTypes(selectedInputFiles);
	}

	private void guessInputDataTypes(File file, MiapeExtractionTask associatedTask) {
		inputDataTypeGuesser = new InputDataTypeGuesser(file, associatedTask);
		inputDataTypeGuesser.addPropertyChangeListener(this);
		inputDataTypeGuesser.execute();
	}

	private void guessInputDataTypes(File[] files) {
		inputDataTypeGuesser = new InputDataTypeGuesser(files);
		inputDataTypeGuesser.addPropertyChangeListener(this);
		inputDataTypeGuesser.execute();
	}

	/**
	 * Creates a job list and creates the GUI controls for each
	 * 
	 * @param inputFiles
	 * @param inputDataType
	 * @param mode
	 */
	private void createAndLoadImportTasks(File[] inputFiles, Map<File, InputFileType> filesAndTypes) {
		// create manager from the table
		this.miapeExtractorBatchManager = createMiapeExtractorBatchManagerFromTable();
		if (inputFiles != null) {

			// add one task per selected file
			for (File selectedFile : inputFiles) {
				InputFileType inputFileType = null; // by default
				InputFileType guessedInputDataType = filesAndTypes.get(selectedFile);
				if (guessedInputDataType != null) {
					inputFileType = guessedInputDataType;
				} else {
					guessedInputDataType = ImportTasksUtil.getSuggestedFileTypeByFileName(selectedFile);
					if (guessedInputDataType != null && guessedInputDataType != inputFileType) {
						// create message to show the message to the user
						appendStatus("Guessing by its name, the input file '"
								+ FilenameUtils.getName(selectedFile.getAbsolutePath()) + "' seems to be a '"
								+ guessedInputDataType.getPrimaryFileDescription()
								+ "'. Check this change before proceed.");
						inputFileType = guessedInputDataType;
					}
				}
				MiapeExtractionTask task = this.miapeExtractorBatchManager.addImportTask(selectedFile, inputFileType,
						null, getProjectName(), null);
				// add that task to the table
				scrollableImportTaskTable.getTable().addRow(task);
				// enable button for selecting attached MS files
				enableSelectionOfAttachedMSInputFiles(true);
			}
		} else

		{
			appendStatus("No input file selected");
		}
	}

	private MiapeExtractionBatchManager createMiapeExtractorBatchManagerFromTable() {
		MiapeExtractionBatchManager ret = new MiapeExtractionBatchManager(this, cvManager);
		List<MiapeExtractionTask> importTasks = scrollableImportTaskTable.getTable().getImportTasks();
		for (MiapeExtractionTask task : importTasks) {
			if (task.isDone() && (task.getResult() == null || task.getResult().getErrorMessage() != null)) {
				MiapeExtractionTask newTask = new MiapeExtractionTask(task.getParameters());
				scrollableImportTaskTable.getTable().getImportTaskTableModel().replaceImportTask(task, newTask);
				ret.addTaskToQueue(newTask);
			} else if (!task.isDone()) {
				ret.addTaskToQueue(task);
			}
		}
		return ret;
	}

	private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {
		if (this.miapeExtractorBatchManager != null) {
			this.miapeExtractorBatchManager.cancelMiapeExtractions();
		}
		if (inputDataTypeGuesser != null && inputDataTypeGuesser.getState() == StateValue.STARTED) {
			inputDataTypeGuesser.cancel(true);
		}
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	private void enableSelectionOfAttachedMSInputFiles(boolean b) {

		jButtonInputFileAttach.setEnabled(b);

	}

	private File[] selectFiles(boolean multipleFiles) {
		FileNameExtensionFilter filter = null;

		jFileChooser = new JFileChooser(MainFrame.currentFolder);
		jFileChooser.setMultiSelectionEnabled(multipleFiles);

		jFileChooser.setDialogTitle("Select one or more input files or a folder");
		filter = new FileNameExtensionFilter("PRIDE XML files (*.xml)", "xml");
		jFileChooser.addChoosableFileFilter(filter);
		filter = new FileNameExtensionFilter("mzIdentML files (*.mzid, *.mzidentml, *.xml)", "mzid", "mzidentml",
				"xml");
		jFileChooser.addChoosableFileFilter(filter);
		filter = new FileNameExtensionFilter("DTASelect-filter files (*.txt)", "txt");
		jFileChooser.addChoosableFileFilter(filter);
		filter = new FileNameExtensionFilter("XTandem output files (*.xml)", "xml");
		jFileChooser.addChoosableFileFilter(filter);
		filter = new FileNameExtensionFilter("pepXML files (*.pepxml, *.xml)", "pepxml", "xml");
		jFileChooser.addChoosableFileFilter(filter);
		filter = new FileNameExtensionFilter("Table text files (*.txt, *.csv, *.tsv)", "txt", "csv", "tsv");
		jFileChooser.addChoosableFileFilter(filter);

		jFileChooser.showOpenDialog(this);
		File[] selectedFiles = null;
		if (jFileChooser.getSelectedFiles().length == 1 && jFileChooser.getSelectedFile().isDirectory()) {
			log.info("Folder selected " + jFileChooser.getSelectedFile().getAbsolutePath());
			selectedFiles = jFileChooser.getSelectedFile().listFiles();
		} else {
			if (multipleFiles) {
				selectedFiles = jFileChooser.getSelectedFiles();
			} else if (jFileChooser.getSelectedFile() != null) {
				selectedFiles = new File[1];
				selectedFiles[0] = jFileChooser.getSelectedFile();
			}
		}
		MainFrame.currentFolder = jFileChooser.getCurrentDirectory();
		if (selectedFiles != null) {
			return selectedFiles;
		}
		return null;

	}

	private List<File> selectAttachedMSFiles(InputFileType selecteInputFileType) {
		TFileExtension filter = null;

		jFileChooser = new JFileChooser(MainFrame.currentFolder);
		jFileChooser.setMultiSelectionEnabled(true);
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		// selectedInputFileType can be null
		if (selecteInputFileType == null) {
			jFileChooser.setDialogTitle("Select one or more input mgf or mzML files");
			filter = new TFileExtension("Mascot Generic files", new String[] { "MGF", "mgf", "mzML", "xml" });
			jFileChooser.setFileFilter(filter);
		} else {
			switch (selecteInputFileType) {

			case DTASELECTPLUSMGF:
			case MZIDENTMLPLUSMGF:
			case PEPXMLPLUSMGF:
			case XTANDEMPLUSMGF:
				jFileChooser.setDialogTitle("Select one or more input mgf files");
				filter = new TFileExtension("Mascot Generic files", new String[] { "MGF", "mgf" });
				jFileChooser.setFileFilter(filter);
				break;

			case MZIDENTMLPLUSMZML:
				jFileChooser.setDialogTitle("Select one or more input mzML files");
				filter = new TFileExtension("mzML files", new String[] { "mzml", "xml" });
				jFileChooser.setFileFilter(filter);
				break;
			default:
				break;

			}
		}
		jFileChooser.showOpenDialog(this);
		File[] selectedFiles = null;
		if (jFileChooser.getSelectedFiles().length == 1 && jFileChooser.getSelectedFile().isDirectory()) {
			log.info("Folder selected " + jFileChooser.getSelectedFile().getAbsolutePath());
			selectedFiles = jFileChooser.getSelectedFile().listFiles();
		} else {
			selectedFiles = jFileChooser.getSelectedFiles();
		}
		if (selectedFiles != null && selectedFiles.length > 0) {
			log.info(selectedFiles.length + " selected Files");
			List<File> validFiles = Arrays.asList(selectedFiles);

			return validFiles;
		}
		return null;

	}

	private void jButtonSubmitActionPerformed(java.awt.event.ActionEvent evt) {
		if (evt.getSource() instanceof JButton) {
			if (!((JButton) evt.getSource()).isEnabled())
				return;
		}
		// check the tasks in the table
		boolean valid = checkTaskConsistency();
		if (valid) {
			// clear status
			jTextAreaStatus.setText("");
			startExtraction();
		}
	}

	/**
	 * Checks tasks consistency just before starting import
	 * 
	 * @return true if everything is fine
	 */
	private boolean checkTaskConsistency() {
		appendStatus("Checking import tasks...");
		boolean valid = true;
		for (MiapeExtractionTask task : scrollableImportTaskTable.getTable().getImportTasks()) {
			try {
				task.checkConsistency();
			} catch (MiapeDataInconsistencyException e) {
				valid = false;
				appendStatus("Error in import task '" + task.getRunIdentifier() + "': " + e.getMessage());
			}

			log.info("No miape extraction process started");
		}
		if (valid) {
			appendStatus("Import tasks checked. Everything is ready!");
		}
		return valid;
	}

	private synchronized void startExtraction() {

		enableStateKeeper.keepEnableStates(this);
		enableStateKeeper.disable(this);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		jProgressBar.setIndeterminate(true);
		miapeExtractorBatchManager = createMiapeExtractorBatchManagerFromTable();
		miapeExtractorBatchManager.startMiapeExtractionNextInQueue();
	}

	private void jButtonProjectActionPerformed(java.awt.event.ActionEvent evt) {
		appendStatus("Opening project table");
		showProjectTable = true;
		loadProjects(true, false);

	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.ButtonGroup buttonGroupInputFileFormat;
	private javax.swing.JButton jButtonCancel;
	public javax.swing.JButton jButtonInputFileAttach;
	public javax.swing.JButton jButtonInputFile;
	public javax.swing.JButton jButtonProject;
	public javax.swing.JButton jButtonSubmit;

	private javax.swing.JFileChooser jFileChooser;

	private javax.swing.JPanel jPanelCenter;
	private javax.swing.JPanel jPanelNorth;
	private javax.swing.JPanel jPanelSouth;

	private javax.swing.JProgressBar jProgressBar;
	private javax.swing.JScrollPane jScrollPane1;

	private javax.swing.JTextArea jTextAreaStatus;
	private JTextField jTextFieldProjectName;
	// End of variables declaration//GEN-END:variables

	private TFrmInputTable additionalDataForm;

	private MainFrame mainFrame = null;

	private boolean isLoadingProjects; // indicate if the thread
										// LoadProjectsThread is already loading
										// or not
	private final TIntObjectHashMap<String> loadedProjects = new TIntObjectHashMap<String>();
	private JButton jButtonGoToData;
	private JButton jButtonHelp;
	private ControlVocabularyManager cvManager;
	private JPanel panel_2;
	private JButton btnDeleteImportTask;
	private JPanel panel;
	private JPanel panel_1;
	private JPanel panel_3;
	private JPanel panel_4;
	private JPanel panelWest;

	// public int selectedInstrumentNumber;

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress".equals(evt.getPropertyName())) {
			int progress = (Integer) evt.getNewValue();
			jProgressBar.setValue(progress);
		} else if (MiapeExtractionTask.NOTIFICATION.equals(evt.getPropertyName())) {
			String notificacion = evt.getNewValue().toString();
			appendStatus(notificacion);
		} else if (MiapeExtractionTask.MIAPE_CREATION_ERROR.equals(evt.getPropertyName())) {
			if (evt.getNewValue() != null) {
				MiapeExtractionResult extractionResult = (MiapeExtractionResult) evt.getNewValue();
				appendStatus(extractionResult.getErrorMessage());
				ImportTaskDataModel model = scrollableImportTaskTable.getTable().getImportTaskTableModel();
				int row = model.indexOf(model.getTaskByID(extractionResult.getMiapeExtractionTaskIdentifier()));
				model.fireTableRowsUpdated(row, row);
			}
		} else if (MiapeExtractionTask.MIAPE_CREATION_CANCELED.equals(evt.getPropertyName())) {
			enableStateKeeper.setToPreviousState(this);
			appendStatus("Data import cancelled");
			this.setCursor(null); // turn off the wait cursor
			jProgressBar.setIndeterminate(false);
		} else if (MiapeExtractionTask.MIAPE_MSI_CREATED_DONE.equals(evt.getPropertyName())) {
			File miapeIDString = (File) evt.getNewValue();
			log.info("Miape MSI created done finished: " + miapeIDString.getAbsolutePath());
			FileManager.deleteMetadataFile(MIAPEMSChecker.CURRENT_MZML);
			MiapeMSFormsDialog.getInstance(this, getControlVocabularyManager()).initMetadataCombo(null,
					getControlVocabularyManager());
			// load new projects
			loadProjects(false, true);
		} else if (MiapeExtractionTask.MIAPE_MS_CREATED_DONE.equals(evt.getPropertyName())) {
			File miapeIDString = (File) evt.getNewValue();
			log.info("Miape MS created done finished: " + miapeIDString.getAbsolutePath());
			// load new projects
			loadProjects(false, true);
		} else if (OntologyLoaderWaiter.ONTOLOGY_LOADED.equals(evt.getPropertyName())) {
			cvManager = (ControlVocabularyManager) evt.getNewValue();
			appendStatus("Ontologies loaded.");
			MiapeMSFormsDialog.getInstance(this, getControlVocabularyManager()).initMetadataCombo(null, cvManager);

		} else if (OntologyLoaderWaiter.ONTOLOGY_LOADING_ERROR.equals(evt.getPropertyName())) {
			appendStatus(
					"Error loading ontologies. Please note that this could lead to a non expected behaviour of the tool");

		} else if (OntologyLoaderWaiter.ONTOLOGY_LOADING_NETWORK_ERROR.equals(evt.getPropertyName())) {
			appendStatus(
					"Error loading ontologies. Please check your internet connection or institution firewall and run again the software. If the problem persist, you can contact salvador@scripps.edu for help.");

		} else if (MiapeExtractionTask.MIAPE_CREATION_TOTAL_DONE.equals(evt.getPropertyName())) {
			MiapeExtractionResult extractionResult = (MiapeExtractionResult) evt.getNewValue();
			appendStatus("Import task '" + extractionResult.getMiapeExtractionTaskIdentifier()
					+ "' finished. Dataset imported at: "
					+ extractionResult.getDirectLinkToMIAPEMSI().getAbsolutePath());
			ImportTaskDataModel model = scrollableImportTaskTable.getTable().getImportTaskTableModel();
			int row = model.indexOf(model.getTaskByID(extractionResult.getMiapeExtractionTaskIdentifier()));
			model.fireTableRowsUpdated(row, row);
		} else if (MiapeExtractionBatchManager.MIAPE_BATCH_DONE.equals(evt.getPropertyName())) {
			String statisticsString = (String) evt.getNewValue();
			enableStateKeeper.setToPreviousState(this);
			jProgressBar.setIndeterminate(false);
			this.setCursor(null); // turn off the wait cursor
			appendStatus(statisticsString);

		} else if (MiapeExtractionTask.MIAPE_CREATION_STARTS.equals(evt.getPropertyName())) {

			appendStatus("Starting process...");
			scrollableImportTaskTable.getTable().startThreadToUpdateProgressOnTask((Integer) evt.getNewValue());
		} else if (LoadProjectsTask.PROJECT_LOADED_DONE.equals(evt.getPropertyName())) {
			showLoadedProjectTable();
			// ArrayList<String> list = new ArrayList<String>();
			// list.addAll(loadedProjects.valueCollection());
			// AutoCompleteDecorator.decorate(this.jTextFieldProjectName, list,
			// false);
		} else if (MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE.equals(evt.getPropertyName())) {
			appendStatus("Copying file '" + evt.getNewValue() + "' to the local file structure");
		} else if (MiapeExtractionTask.MIAPE_CREATION_COPYING_FILE_DONE.equals(evt.getPropertyName())) {
			appendStatus(evt.getNewValue().toString());
			appendStatus("Now reading and processing it...");

		} else if (InputDataTypeGuesser.INPUT_DATA_TYPE_GUESSING_STARTED.equals(evt.getPropertyName())) {
			enableStateKeeper.keepEnableStates(this);
			enableStateKeeper.disable(this);
			InputDataTypeGuesser inputDataTypeGuesser = (InputDataTypeGuesser) evt.getNewValue();
			int totalFiles = inputDataTypeGuesser.getFiles().length;
			String plural = totalFiles > 1 ? "s" : "";
			jProgressBar.setMaximum(totalFiles);
			jProgressBar.setIndeterminate(true);
			jProgressBar.setStringPainted(true);
			String message = "Checking input file" + plural + " format...";
			jProgressBar.setString(message);
			appendStatus(message);
		} else if (InputDataTypeGuesser.INPUT_DATA_TYPE_GUESSED.equals(evt.getPropertyName())) {
			InputDataTypeGuesser guesser = (InputDataTypeGuesser) evt.getNewValue();
			File file = (File) evt.getOldValue();
			InputFileType inputFileType = guesser.getGuessedTypes().get(file);

			if (inputFileType != null) {
				appendStatus("File '" + FilenameUtils.getName(file.getAbsolutePath()) + "' has been recognized as a '"
						+ inputFileType.getPrimaryFileDescription() + "'");

				// check if this was a result of changing the file directly in
				// the table, and that happens when associated task is not null
				if (guesser.getAssociatedTask() != null) {
					// in that case, add it to the table
					((MiapeExtractionRunParametersImpl) guesser.getAssociatedTask().getParameters())
							.setInputFileType(inputFileType);
					int row = scrollableImportTaskTable.getTable().getImportTaskTableModel()
							.indexOf(guesser.getAssociatedTask());
					scrollableImportTaskTable.getTable().getImportTaskTableModel().fireTableRowsUpdated(row, row);
				}

			} else {
				InputFileType guessedInputDataType = ImportTasksUtil.getSuggestedFileTypeByFileName(file);
				if (guessedInputDataType != null) {
					appendStatus(
							"Guessing by its name, the input file '" + FilenameUtils.getName(file.getAbsolutePath())
									+ "' seems to be a '" + guessedInputDataType.getPrimaryFileDescription()
									+ "'. Check this change before proceed.");
					// check if this was a result of changing the file directly
					// in the table, and that happens when associated task is
					// not null
					if (guesser.getAssociatedTask() != null) {
						((MiapeExtractionRunParametersImpl) guesser.getAssociatedTask().getParameters())
								.setInputFileType(guessedInputDataType);
						int row = scrollableImportTaskTable.getTable().getImportTaskTableModel()
								.indexOf(guesser.getAssociatedTask());
						scrollableImportTaskTable.getTable().getImportTaskTableModel().fireTableRowsUpdated(row, row);
					}
				}
			}
		} else if (InputDataTypeGuesser.INPUT_DATA_TYPE_GUESSING_FINISHED.equals(evt.getPropertyName())) {
			InputDataTypeGuesser inputDataTypeGuesser = (InputDataTypeGuesser) evt.getNewValue();
			enableStateKeeper.setToPreviousState(this);
			jProgressBar.setStringPainted(false);
			jProgressBar.setIndeterminate(false);
			jProgressBar.setValue(0);
			HashMap<File, InputFileType> map = inputDataTypeGuesser.getGuessedTypes();
			String plural = map.size() > 1 ? "s" : "";
			appendStatus("Input file" + plural + " format checked.");
			// if it is coming from a check from a single file, dont load
			// everything
			if (inputDataTypeGuesser.getAssociatedTask() == null) {
				createAndLoadImportTasks(selectedInputFiles, map);
			}
		} else if (InputDataTypeGuesser.INPUT_DATA_TYPE_GUESSING_ERROR.equals(evt.getPropertyName())) {
			Exception error = (Exception) evt.getNewValue();
			enableStateKeeper.setToPreviousState(this);
			jProgressBar.setStringPainted(false);
			jProgressBar.setMaximum(0);
			appendStatus("Error chcking Input file format: " + error.getMessage());
			appendStatus(
					"Chack if there is something wrong with your input files and try again. If the problem persist, contact salvador at scripps.edu");
		} else if (InputDataTypeGuesser.INPUT_DATA_TYPE_GUESSING_CANCELLED.equals(evt.getPropertyName())) {
			enableStateKeeper.setToPreviousState(this);
			jProgressBar.setStringPainted(false);
			jProgressBar.setMaximum(0);
			jProgressBar.setIndeterminate(false);
			appendStatus("Checking input file format cancelled by user");
		}
	}

	private void showLoadedProjectTable() {
		if (showProjectTable) {
			TIntObjectHashMap<String> miapeProjects = getLoadedProjects();
			// this.appendStatus(miapeProjects.size() + " projects
			// retrieved\n");
			if (miapeProjects != null && !miapeProjects.isEmpty()) {
				additionalDataForm = new TFrmInputTable(this, true, miapeProjects);
				additionalDataForm.setVisible(true);
			} else {
				appendStatus("There is no projects to show");
			}
			showProjectTable = false;
		}
	}

	protected ControlVocabularyManager getControlVocabularyManager() {
		return OntologyLoaderTask.getCvManager();
	}

	// public void fillInstrumentUserSelection(String userSelection, int i) {
	// this.jLabelInstrument.setText(userSelection);
	// this.selectedInstrumentNumber = i + 1;
	// }

	public void fillProjectUserSelection(String userSelection) {
		jTextFieldProjectName.setText(userSelection);
	}

	public String getProjectName() {
		return jTextFieldProjectName.getText();
	}

	public void setProjectName(String name) {
		jTextFieldProjectName.setText(name);
	}

	public String getStatus() {
		return jTextAreaStatus.getText();
	}

	void appendStatus(String text) {
		ZonedDateTime zonedDateTime = ZonedDateTime.now();

		String dateText = zonedDateTime.format(formatter);
		jTextAreaStatus.append(dateText + ": " + text + "\n");
		jTextAreaStatus.setCaretPosition(jTextAreaStatus.getText().length() - 1);
	}

	public void setLoadedProjects(TIntObjectHashMap<String> projects) {

		loadedProjects.clear();
		loadedProjects.putAll(projects);
	}

	private TIntObjectHashMap<String> getLoadedProjects() {
		while (isLoadingProjects()) {
			try {
				Thread.currentThread();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return loadedProjects;
	}

	public synchronized boolean isLoadingProjects() {
		return isLoadingProjects;
	}

	public synchronized void setLoadingProjects(boolean b) {
		isLoadingProjects = b;
	}

	public static void main(String[] args) {
		MiapeExtractionFrame instance = new MiapeExtractionFrame(null, false);
		instance.setVisible(true);

	}

	/**
	 * Show a dialog with the option of opening a browser with the direct link
	 * to the MIAPE documents
	 *
	 * @param msURL
	 * @param msiURL
	 * @param directLinks
	 */
	private void showOpenBrowserDialog(File msURL, File msiURL, String directLinks) {
		String plural = "";
		if (msURL != null && msiURL != null)
			plural = "(s)";

		Object[] dialog_options = { "Yes, open system explorer", "No, close this dialog" };
		int selected_option = JOptionPane.showOptionDialog(this,
				directLinks + "\n"
						+ "\nClick on yes to open the system explorer to go directly to the imported dataset file"
						+ plural + "\n",
				"Dataset" + plural + " imported", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				dialog_options, dialog_options[1]);
		if (selected_option == JOptionPane.YES_OPTION) { // Yes
			Desktop desktop = Desktop.getDesktop();
			if (msURL != null) {

				try {
					desktop.open(msURL.getParentFile());
				} catch (IOException e) {
					appendStatus(e.getMessage());
				}
				// HttpUtilities.openURL(msURL.toString());
			}
			if (msiURL != null) {
				try {
					desktop.open(msiURL.getParentFile());
				} catch (IOException e) {
					appendStatus(e.getMessage());
				}
				// HttpUtilities.openURL(msiURL.toString());
			}
		}
	}

	public boolean isFastParsing() {
		return isFastParsing;
	}

	@Override
	public List<String> getHelpMessages() {
		String[] array = { "Import datasets into PACOM:", //
				"<b>How to import datasets:</b>", //

				"1. Select the <b>type of input file</b> you are going to import. "
						+ "You can either select it from the '<i>Input type</i>' panel or from the '<i>Input type + MS data file</i>' panel.", //

				"If you select one of option in <i>'Input type + MS data file'</i> panel, "
						+ "you will be able to import a spectra file (MGF or MZML file) and "
						+ "a <i>Mass Spectrometry metadata template</i>. "
						+ "This option is useful in case you want to generate a <i>PRIDE XML</i> "
						+ "file containing the mass spectra.", //

				"2. Select the <b>Input file(s)</b> from your file system using the file selectors in the top.", //

				"3. Type the <b>name of the project</b> or select one by clicking on <i>'Select dataset folder'</i> button. "
						+ "These projects in which the input datasets will be stored, "
						+ "are just a way for organizing your imported datasets.", //

				"If you type a new project name, it will appear the next time you click on <i>'Select dataset folder'</i>.", //

				"4. Click on <b>'Import data'</b> to start the import data process.", //
				"<b>Other buttons:</b>", //
				"- Click on <b>'Go to Data Inspection</b> to go directly to the <i>Comparison Project Manager</i> "
						+ "without the need of going through the main dialog.", //
				"- Click on the small <b>'?'</b> button for help about the format of the <i>table text input file</i>", //
				"Mass spectrometry metadata template <b>'Edit'</b> button: clicking here will open the <i>mass spectrometry metadata editor</i>."
						+ " After saving some metadata under a name in that editor,"
						+ " it will be available for being selected at the drop-down menu of <i>mass spectrometry metadatas</i>.", //
				"- Click on <i>'Go to Comparison Project Manager'</i> to close this window and go directly to the Comparison Project Manager." };
		return Arrays.asList(array);
	}

	public ScrollableImportTaskJPanel getScrollableImportTaskTable() {
		return scrollableImportTaskTable;
	}
}