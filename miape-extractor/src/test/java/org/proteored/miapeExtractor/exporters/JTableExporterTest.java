package org.proteored.miapeExtractor.exporters;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.table.TableModel;

import org.junit.Test;
import org.proteored.miapeExtractor.analysis.exporters.ExporterManager;
import org.proteored.miapeExtractor.analysis.exporters.gui.ScrollableJTable;
import org.proteored.miapeExtractor.analysis.exporters.tasks.JTableLoader;
import org.proteored.miapeExtractor.analysis.exporters.util.ExportedColumns;
import org.proteored.miapeExtractor.chart.ExperimentsUtilTest;
import org.proteored.miapeapi.experiment.model.ExperimentList;

public class JTableExporterTest {

	private JTableLoader exporter;
	private ScrollableJTable scrollPanel;

	@Test
	public void jTableExporterTest() {
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		final ExperimentList createExperiments = ExperimentsUtilTest
				.createExperiments(null);
		System.out.println("Experimento creado");
		ExporterManager manager = new ExporterManager() {

			@Override
			public boolean retrieveProteinSequences() {
				return false;
			}

			@Override
			public boolean showPeptides() {
				return false;
			}

			@Override
			public boolean isReplicateAndExperimentOriginIncluded() {
				return true;
			}

			@Override
			public boolean isDecoyHitsIncluded() {
				return true;
			}

			@Override
			public boolean isGeneInfoIncluded() {
				return false;
			}

			@Override
			public boolean showBestProteins() {
				return true;
			}

			@Override
			public boolean showBestPeptides() {
				return true;
			}

			@Override
			public boolean isNonConclusiveProteinsIncluded() {
				return false;
			}

			@Override
			public boolean isFDRApplied() {
				return true;
			}
		};
		JTable table = new JTable();
		this.exporter = new JTableLoader(manager, createExperiments, table,
				true);
		table = exporter.export();

		this.scrollPanel = new ScrollableJTable();

		JPanel parentPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;

		JLabel label = new JLabel("Protein Accession filter");
		parentPanel.add(label, c);
		c.gridx++;
		JTextField textField = new JTextField(15);
		KeyListener l = getKeyListener();
		textField.addKeyListener(l);
		parentPanel.add(textField, c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		parentPanel.add(scrollPanel, c);

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Scrollable JTable");
		frame.setContentPane(parentPanel);
		frame.pack();
		frame.setVisible(true);
		while (true) {
			try {
				Thread.sleep(100000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private KeyListener getKeyListener() {
		return new java.awt.event.KeyAdapter() {
			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
				applyFilter((JTextField) evt.getSource());
			}
		};
	}

	private void applyFilter(JTextField textField) {
		RowFilter<TableModel, Object> rf = null;

		final String text = textField.getText();
		this.scrollPanel
				.setFilter(ExportedColumns.PROTEIN_ACC.toString(), text);

	}
}
