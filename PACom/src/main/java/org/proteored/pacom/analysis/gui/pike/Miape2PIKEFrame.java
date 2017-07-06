/*
 * Miape2PIKEFrame.java
 * Created on __DATE__, __TIME__
 */

package org.proteored.pacom.analysis.gui.pike;

import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.exceptions.IllegalMiapeArgumentException;
import org.proteored.pacom.utils.ClientHttpRequest;
import org.proteored.pacom.utils.HttpUtilities;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import gnu.trove.map.hash.THashMap;

/**
 * 
 * @author __USER__
 */
public class Miape2PIKEFrame extends javax.swing.JFrame implements PropertyChangeListener {
	private ClientHttpRequest pikeClient;
	private final List<String> accessions;
	private Map<String, String> databasesHash;
	private final Frame parentDialog;
	private static Logger log = Logger.getLogger("log4j.logger.org.proteored");
	public static final String TASK_DONE = "TASK_DONE";
	public static final String TASK_ERROR = "TASK_ERROR";

	/** Creates new form Miape2PIKEFrame */
	public Miape2PIKEFrame(Frame parent, List<String> accessions) {
		this.parentDialog = parent;
		if (this.parentDialog != null)
			this.parentDialog.setVisible(false);
		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
		}
		initComponents();
		initDatabaseComboBox();
		try {
			pikeClient = new ClientHttpRequest(new URL("http://proteo.cnb.csic.es:8080/pike/pike"));

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.accessions = accessions;
		if (accessions != null)
			this.jLabelSending.setText("Sending " + accessions.size() + " protein accessions");
	}

	@Override
	public void dispose() {
		if (this.parentDialog != null) {
			this.parentDialog.setEnabled(true);
			this.parentDialog.setVisible(true);

		}
		super.dispose();
	}

	private void initDatabaseComboBox() {
		this.databasesHash = new THashMap<String, String>();
		databasesHash.put("UniProt/SwissProt Accession (fi: P27098)", "0_SWISS-PROT-AC");
		databasesHash.put("UniProt/SwissProt ID (fi: ALBU_HUMAN)", "0_SWISS-PROT-ID");
		databasesHash.put("EBI IPI", "1_IPI");
		databasesHash.put("GI number (from NCBI nr)", "1_NCBI");
		databasesHash.put("RefSeq accession(from NCBI nr)", "1_NCBI");
		databasesHash.put("GenBank Accession", "1_GenBank");
		databasesHash.put("GenPept", "1_GenPept");
		databasesHash.put("PDB (Protein Data Bank)", "2_NCBI");
		databasesHash.put("DDBJ (DataBase from Japan)", "2_NCBI");
		databasesHash.put("Gene ID", "1_GeneName");
		this.jComboBoxDatabase.setModel(new DefaultComboBoxModel(databasesHash.keySet().toArray(new String[0])));
	}

	private List<String> getFieldSelectParameters() {
		List<String> ret = new ArrayList<String>();
		if (this.jCheckBoxProteinName.isSelected())
			ret.add("0_Protein name");
		if (this.jCheckBoxGeneName.isSelected())
			ret.add("0_Gene name");
		if (this.jCheckBoxProteinTaxonomy.isSelected())
			ret.add("0_taxonomy");
		if (this.jCheckBoxProteinSequence.isSelected())
			ret.add("0_Sequence");
		if (this.jCheckBoxAlternativeIDs.isSelected())
			ret.add("0_Protein syno");
		if (this.jCheckBoxProteinFunction.isSelected())
			ret.add("1_function");
		if (this.jCheckBoxSubcellularLocation.isSelected())
			ret.add("1_subcellular location");
		if (this.jCheckBoxTissueSpecificity.isSelected())
			ret.add("1_tissue specifity");
		if (this.jCheckBoxRelatedDisseases.isSelected())
			ret.add("1_disease");
		if (this.jCheckBoxSPKeywords.isSelected())
			ret.add("3_Keywords");
		if (this.jCheckBoxGOTerms.isSelected())
			ret.add("2_dbxref?GO");
		if (this.jCheckBoxHPRDLink.isSelected())
			ret.add("2_dbxref?HPRD");
		if (this.jCheckBoxOMIMDisseases.isSelected())
			ret.add("2_dbxref?MIM");
		if (this.jCheckBoxKEGGPathway.isSelected())
			ret.add("2_dbxref?KEGG");
		if (this.jCheckBoxPRIDEEntries.isSelected())
			ret.add("2_dbxref?PRIDE");
		if (this.jCheckBoxSTRINGInteractions.isSelected())
			ret.add("2_dbxref?STRING");
		if (this.jCheckBoxIntActInteractions.isSelected())
			ret.add("2_dbxref?IntAct");
		if (this.jCheckBoxInterProDomains.isSelected())
			ret.add("2_dbxref?InterPro");
		if (this.jCheckBoxPhosphoSiteLink.isSelected())
			ret.add("2_dbxref?PhosphoSite");
		if (this.jCheckBoxPharmaGKBLink.isSelected())
			ret.add("2_dbxref?PharmaGKB");
		return ret;
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jPanel1 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jTextFieldName = new javax.swing.JTextField();
		jTextFieldEMail = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jPanel2 = new javax.swing.JPanel();
		jComboBoxDatabase = new javax.swing.JComboBox();
		jPanel3 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		jCheckBoxProteinName = new javax.swing.JCheckBox();
		jCheckBoxGeneName = new javax.swing.JCheckBox();
		jCheckBoxProteinTaxonomy = new javax.swing.JCheckBox();
		jCheckBoxProteinSequence = new javax.swing.JCheckBox();
		jCheckBoxAlternativeIDs = new javax.swing.JCheckBox();
		jCheckBoxProteinFunction = new javax.swing.JCheckBox();
		jCheckBoxSubcellularLocation = new javax.swing.JCheckBox();
		jCheckBoxTissueSpecificity = new javax.swing.JCheckBox();
		jCheckBoxRelatedDisseases = new javax.swing.JCheckBox();
		jCheckBoxSPKeywords = new javax.swing.JCheckBox();
		jCheckBoxProteinAnnotationsSelectAll = new javax.swing.JCheckBox();
		jPanel5 = new javax.swing.JPanel();
		jCheckBoxGOTerms = new javax.swing.JCheckBox();
		jCheckBoxPharmaGKBLink = new javax.swing.JCheckBox();
		jCheckBoxPhosphoSiteLink = new javax.swing.JCheckBox();
		jCheckBoxInterProDomains = new javax.swing.JCheckBox();
		jCheckBoxIntActInteractions = new javax.swing.JCheckBox();
		jCheckBoxSTRINGInteractions = new javax.swing.JCheckBox();
		jCheckBoxPRIDEEntries = new javax.swing.JCheckBox();
		jCheckBoxKEGGPathway = new javax.swing.JCheckBox();
		jCheckBoxOMIMDisseases = new javax.swing.JCheckBox();
		jCheckBoxHPRDLink = new javax.swing.JCheckBox();
		jCheckBoxCrossReferencesSelectAll = new javax.swing.JCheckBox();
		jPanel6 = new javax.swing.JPanel();
		jPanel7 = new javax.swing.JPanel();
		jCheckBoxEnableExhaustiveSearch = new javax.swing.JCheckBox();
		jComboBoxDeepLevel = new javax.swing.JComboBox();
		jLabelDeepLevel = new javax.swing.JLabel();
		jPanel8 = new javax.swing.JPanel();
		jLabelSending = new javax.swing.JLabel();
		jButton1 = new javax.swing.JButton();
		jProgressBar1 = new javax.swing.JProgressBar();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Protein Information and Knowledge Extractor (PIKE)");

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"1. Contact Information"));

		jLabel1.setText("Your name:");

		jLabel2.setText("Your e-mail:");

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jLabel2).addComponent(jLabel1))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jTextFieldName, javax.swing.GroupLayout.DEFAULT_SIZE, 266,
										Short.MAX_VALUE)
								.addComponent(jTextFieldEMail, javax.swing.GroupLayout.DEFAULT_SIZE, 266,
										Short.MAX_VALUE))
						.addContainerGap()));
		jPanel1Layout
				.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel1)
								.addComponent(jTextFieldName, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(jLabel2).addComponent(jTextFieldEMail,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"2. Select the database"));

		jComboBoxDatabase.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "dsf" }));

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup().addContainerGap()
						.addComponent(jComboBoxDatabase, 0, 336, Short.MAX_VALUE).addContainerGap()));
		jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addComponent(jComboBoxDatabase, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"3. Select the annotations"));

		jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Protein Annotations"));

		jCheckBoxProteinName.setText("Protein name");

		jCheckBoxGeneName.setText("Gene name");

		jCheckBoxProteinTaxonomy.setText("Protein Taxonomy");

		jCheckBoxProteinSequence.setText("Protein Sequence");

		jCheckBoxAlternativeIDs.setText("Alternative IDs");

		jCheckBoxProteinFunction.setText("Protein Function");

		jCheckBoxSubcellularLocation.setText("Subcellular location");

		jCheckBoxTissueSpecificity.setText("Tissue Specificity");

		jCheckBoxRelatedDisseases.setText("Related disseases");

		jCheckBoxSPKeywords.setText("SP keywords");

		jCheckBoxProteinAnnotationsSelectAll.setText("select all");
		jCheckBoxProteinAnnotationsSelectAll.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxProteinAnnotationsSelectAllActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel4Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jCheckBoxProteinName).addComponent(jCheckBoxGeneName)
								.addComponent(jCheckBoxProteinTaxonomy).addComponent(jCheckBoxProteinSequence)
								.addComponent(jCheckBoxAlternativeIDs).addComponent(jCheckBoxProteinFunction)
								.addComponent(jCheckBoxSubcellularLocation).addComponent(jCheckBoxTissueSpecificity)
								.addComponent(jCheckBoxRelatedDisseases).addComponent(jCheckBoxSPKeywords)
								.addComponent(jCheckBoxProteinAnnotationsSelectAll))
						.addContainerGap(11, Short.MAX_VALUE)));
		jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
						.addComponent(jCheckBoxProteinAnnotationsSelectAll)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
						.addComponent(jCheckBoxProteinName)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxGeneName)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxProteinTaxonomy)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxProteinSequence)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxAlternativeIDs)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxProteinFunction)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxSubcellularLocation)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxTissueSpecificity)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxRelatedDisseases)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxSPKeywords).addContainerGap()));

		jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Cross References"));

		jCheckBoxGOTerms.setText("Gene Ontology Terms");

		jCheckBoxPharmaGKBLink.setText("PharmaGKB link");

		jCheckBoxPhosphoSiteLink.setText("PhosphoSite link");

		jCheckBoxInterProDomains.setText("InterPro Domains");

		jCheckBoxIntActInteractions.setText("IntAct Interactions");

		jCheckBoxSTRINGInteractions.setText("STRING Interactions");

		jCheckBoxPRIDEEntries.setText("PRIDE entries");

		jCheckBoxKEGGPathway.setText("KEGG pathways");

		jCheckBoxOMIMDisseases.setText("OMIM Disseases");

		jCheckBoxHPRDLink.setText("HPRD link");

		jCheckBoxCrossReferencesSelectAll.setText("select all");
		jCheckBoxCrossReferencesSelectAll.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxCrossReferencesSelectAllActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
		jPanel5.setLayout(jPanel5Layout);
		jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel5Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jCheckBoxGOTerms).addComponent(jCheckBoxHPRDLink)
								.addComponent(jCheckBoxOMIMDisseases).addComponent(jCheckBoxKEGGPathway)
								.addComponent(jCheckBoxPRIDEEntries).addComponent(jCheckBoxSTRINGInteractions)
								.addComponent(jCheckBoxIntActInteractions).addComponent(jCheckBoxInterProDomains)
								.addComponent(jCheckBoxPhosphoSiteLink).addComponent(jCheckBoxPharmaGKBLink)
								.addComponent(jCheckBoxCrossReferencesSelectAll))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
						.addComponent(jCheckBoxCrossReferencesSelectAll)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
						.addComponent(jCheckBoxGOTerms)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxHPRDLink)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxOMIMDisseases)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxKEGGPathway)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxPRIDEEntries)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxSTRINGInteractions)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxIntActInteractions)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxInterProDomains)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxPhosphoSiteLink)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jCheckBoxPharmaGKBLink).addContainerGap()));

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup().addContainerGap()
						.addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap()));
		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup()
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
								.addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"4. GO Additional Information"));

		jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"Go Exhaustive search"));

		jCheckBoxEnableExhaustiveSearch.setText("enable");
		jCheckBoxEnableExhaustiveSearch.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckBoxEnableExhaustiveSearchActionPerformed(evt);
			}
		});

		jComboBoxDeepLevel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4" }));

		jLabelDeepLevel.setText("deep level:");

		javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
		jPanel7.setLayout(jPanel7Layout);
		jPanel7Layout.setHorizontalGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel7Layout.createSequentialGroup().addContainerGap()
						.addComponent(jCheckBoxEnableExhaustiveSearch).addGap(38, 38, 38).addComponent(jLabelDeepLevel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jComboBoxDeepLevel, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(123, Short.MAX_VALUE)));
		jPanel7Layout.setVerticalGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel7Layout.createSequentialGroup()
						.addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jCheckBoxEnableExhaustiveSearch).addComponent(jLabelDeepLevel)
								.addComponent(jComboBoxDeepLevel, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
		jPanel6.setLayout(jPanel6Layout);
		jPanel6Layout.setHorizontalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel6Layout.createSequentialGroup().addContainerGap().addComponent(jPanel7,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap()));
		jPanel6Layout.setVerticalGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel6Layout.createSequentialGroup()
						.addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
				"5. Submit"));

		jLabelSending.setText("Sending 0 proteins");

		jButton1.setText("Submit to PIKE");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
		jPanel8.setLayout(jPanel8Layout);
		jPanel8Layout.setHorizontalGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel8Layout.createSequentialGroup().addGroup(jPanel8Layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanel8Layout.createSequentialGroup().addContainerGap().addComponent(jLabelSending))
						.addGroup(jPanel8Layout.createSequentialGroup().addGap(122, 122, 122).addComponent(jButton1))
						.addGroup(jPanel8Layout.createSequentialGroup().addContainerGap().addComponent(jProgressBar1,
								javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)))
						.addContainerGap()));
		jPanel8Layout.setVerticalGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel8Layout.createSequentialGroup().addComponent(jLabelSending)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jButton1)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jPanel8, javax.swing.GroupLayout.Alignment.TRAILING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))
						.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 412) / 2, (screenSize.height - 836) / 2, 412, 836);
	}// </editor-fold>
		// GEN-END:initComponents

	private void jCheckBoxEnableExhaustiveSearchActionPerformed(java.awt.event.ActionEvent evt) {
		this.jLabelDeepLevel.setEnabled(this.jCheckBoxEnableExhaustiveSearch.isSelected());
		this.jComboBoxDeepLevel.setEnabled(this.jCheckBoxEnableExhaustiveSearch.isSelected());
	}

	private void jCheckBoxCrossReferencesSelectAllActionPerformed(java.awt.event.ActionEvent evt) {
		boolean b = this.jCheckBoxCrossReferencesSelectAll.isSelected();
		if (b)
			this.jCheckBoxCrossReferencesSelectAll.setText("deselect all");
		else
			this.jCheckBoxCrossReferencesSelectAll.setText("select all");
		this.jCheckBoxGOTerms.setSelected(b);
		this.jCheckBoxHPRDLink.setSelected(b);
		this.jCheckBoxOMIMDisseases.setSelected(b);
		this.jCheckBoxKEGGPathway.setSelected(b);
		this.jCheckBoxPRIDEEntries.setSelected(b);
		this.jCheckBoxSTRINGInteractions.setSelected(b);
		this.jCheckBoxIntActInteractions.setSelected(b);
		this.jCheckBoxInterProDomains.setSelected(b);
		this.jCheckBoxPhosphoSiteLink.setSelected(b);
		this.jCheckBoxPharmaGKBLink.setSelected(b);

	}

	private void jCheckBoxProteinAnnotationsSelectAllActionPerformed(java.awt.event.ActionEvent evt) {
		boolean b = this.jCheckBoxProteinAnnotationsSelectAll.isSelected();
		if (b)
			this.jCheckBoxProteinAnnotationsSelectAll.setText("deselect all");
		else
			this.jCheckBoxProteinAnnotationsSelectAll.setText("select all");

		this.jCheckBoxProteinName.setSelected(b);
		this.jCheckBoxGeneName.setSelected(b);
		this.jCheckBoxProteinTaxonomy.setSelected(b);
		this.jCheckBoxProteinSequence.setSelected(b);
		this.jCheckBoxAlternativeIDs.setSelected(b);
		this.jCheckBoxProteinFunction.setSelected(b);
		this.jCheckBoxSubcellularLocation.setSelected(b);
		this.jCheckBoxTissueSpecificity.setSelected(b);
		this.jCheckBoxRelatedDisseases.setSelected(b);
		this.jCheckBoxSPKeywords.setSelected(b);
	}

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
		sendToPIKE();
	}

	private void sendToPIKE() {

		try {
			this.jProgressBar1.setIndeterminate(true);
			checkInputs();
			pikeClient.setParameter("inputfile", "txt");
			pikeClient.setParameter("InputFile", getFileFromAccessions());
			pikeClient.setParameter("username", this.jTextFieldName.getText());
			final String text = this.jTextFieldEMail.getText();
			pikeClient.setParameter("usermail", text);
			pikeClient.setParameter("database", databasesHash.get(this.jComboBoxDatabase.getSelectedItem()));
			final List<String> fieldSelectParameters = getFieldSelectParameters();
			for (String fieldSelectParameter : fieldSelectParameters) {
				pikeClient.setParameter("fieldselect", fieldSelectParameter);
			}
			if (this.jCheckBoxEnableExhaustiveSearch.isSelected()) {
				pikeClient.setParameter("gocheck", "1");
				pikeClient.setParameter("maxdeep", (String) this.jComboBoxDeepLevel.getSelectedItem());
			}
			Miape2PIKETask pikeTask = new Miape2PIKETask(pikeClient);
			pikeTask.addPropertyChangeListener(this);
			pikeTask.execute();

			String messageEmail = "";
			if (!"".equals(text)) {
				messageEmail = "PIKE will send some emails to '" + text + "' notifying the status of the analysis.<br>";
			}
			JOptionPane.showConfirmDialog(this,
					"<html>A list of " + this.accessions.size() + " proteins has been sent to PIKE.<br>"
							+ "A notification dialog will be showed when results are generated.<br>" + messageEmail
							+ "You can continue using the tool while PIKE is running on the server.</html>",
					"Proteins sent to PIKE", JOptionPane.OK_OPTION);

		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} catch (IllegalMiapeArgumentException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

		} finally {
			this.jProgressBar1.setIndeterminate(false);
		}

	}

	private void showMessageDialog() {
		JOptionPane.showMessageDialog(this,
				"PIKE has finished the processing. Go to your email account to see the result link",
				"PIKE results received", JOptionPane.INFORMATION_MESSAGE);

	}

	private void showOpenBrowserDialog(URL url) {

		Object[] dialog_options = { "Yes, open browser", "No, close this dialog" };
		int selected_option = JOptionPane.showOptionDialog(this,
				url.toString() + "\n" + "\nClick on yes to open a browser to go directly to the PIKE results" + "\n",
				"PIKE results received", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				dialog_options, dialog_options[1]);
		if (selected_option == 0) { // Yes
			if (url != null)
				HttpUtilities.openURL(url.toString());
		}
	}

	private void checkInputs() {
		checkEmail();

	}

	private void checkEmail() {
		String email = this.jTextFieldEMail.getText();
		if (!"".equals(email)) {
			Pattern p = Pattern.compile("^\\.|^\\@");
			Matcher m = p.matcher(email);
			if (m.find())
				throw new IllegalMiapeArgumentException("Email addresses don't start" + " with dots or @ signs.");
			// Checks for email addresses that start with
			// www. and prints a message if it does.
			p = Pattern.compile("^www\\.");
			m = p.matcher(email);
			if (m.find()) {
				throw new IllegalMiapeArgumentException(
						"Email addresses don't start" + " with \"www.\", only web pages do.");
			}
			p = Pattern.compile("[^A-Za-z0-9\\.\\@_\\-~#]+");
			m = p.matcher(email);
			StringBuffer sb = new StringBuffer();
			boolean result = m.find();
			boolean deletedIllegalChars = false;

			while (result) {
				deletedIllegalChars = true;
				m.appendReplacement(sb, "");
				result = m.find();
			}

			// Add the last segment of input to the new String
			m.appendTail(sb);

			email = sb.toString();

			if (deletedIllegalChars) {
				throw new IllegalMiapeArgumentException(
						"It contained incorrect characters" + " , such as spaces or commas.");
			}
		}
	}

	private URL getResultsURLFromResults(String results) throws MalformedURLException {
		final Pattern p = Pattern.compile(".*(http://proteo.cnb.csic.es/pike/userdata/\\S+).*");
		Matcher m = p.matcher(results);
		if (m.find()) {
			final String urlString = m.group();
			log.info("Found URL: " + urlString);
			return new URL(urlString);
		}
		return null;
	}

	private File getFileFromAccessions() throws IOException {
		if (accessions == null)
			throw new IllegalMiapeArgumentException("no accessions has been captured");
		final File tempFile = File.createTempFile("accessions", ".txt");
		FileOutputStream out = new FileOutputStream(tempFile);
		for (String accession : accessions) {
			final String string = accession + "\n";
			out.write(string.getBytes());
		}
		out.close();
		return tempFile;
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Miape2PIKEFrame(null, null).setVisible(true);
			}
		});
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JButton jButton1;
	private javax.swing.JCheckBox jCheckBoxAlternativeIDs;
	private javax.swing.JCheckBox jCheckBoxCrossReferencesSelectAll;
	private javax.swing.JCheckBox jCheckBoxEnableExhaustiveSearch;
	private javax.swing.JCheckBox jCheckBoxGOTerms;
	private javax.swing.JCheckBox jCheckBoxGeneName;
	private javax.swing.JCheckBox jCheckBoxHPRDLink;
	private javax.swing.JCheckBox jCheckBoxIntActInteractions;
	private javax.swing.JCheckBox jCheckBoxInterProDomains;
	private javax.swing.JCheckBox jCheckBoxKEGGPathway;
	private javax.swing.JCheckBox jCheckBoxOMIMDisseases;
	private javax.swing.JCheckBox jCheckBoxPRIDEEntries;
	private javax.swing.JCheckBox jCheckBoxPharmaGKBLink;
	private javax.swing.JCheckBox jCheckBoxPhosphoSiteLink;
	private javax.swing.JCheckBox jCheckBoxProteinAnnotationsSelectAll;
	private javax.swing.JCheckBox jCheckBoxProteinFunction;
	private javax.swing.JCheckBox jCheckBoxProteinName;
	private javax.swing.JCheckBox jCheckBoxProteinSequence;
	private javax.swing.JCheckBox jCheckBoxProteinTaxonomy;
	private javax.swing.JCheckBox jCheckBoxRelatedDisseases;
	private javax.swing.JCheckBox jCheckBoxSPKeywords;
	private javax.swing.JCheckBox jCheckBoxSTRINGInteractions;
	private javax.swing.JCheckBox jCheckBoxSubcellularLocation;
	private javax.swing.JCheckBox jCheckBoxTissueSpecificity;
	private javax.swing.JComboBox jComboBoxDatabase;
	private javax.swing.JComboBox jComboBoxDeepLevel;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabelDeepLevel;
	private javax.swing.JLabel jLabelSending;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JProgressBar jProgressBar1;
	private javax.swing.JTextField jTextFieldEMail;
	private javax.swing.JTextField jTextFieldName;

	// End of variables declaration//GEN-END:variables

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if (evt.getPropertyName().equals(TASK_DONE)) {
			URL link = (URL) evt.getNewValue();

			if (link != null)
				showOpenBrowserDialog(link);
			else
				showMessageDialog();

		}
	}
}