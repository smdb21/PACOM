package org.proteored.pacom.utils;

import org.proteored.miapeapi.interfaces.Software;
import org.proteored.pacom.gui.MainFrame;

public class MiapeExtractorSoftware implements Software {

	public MiapeExtractorSoftware() {

	}

	@Override
	public int getId() {
		return -1;
	}

	@Override
	public String getVersion() {
		return MainFrame.getVersion();
	}

	@Override
	public String getName() {
		return "PACOM";
	}

	@Override
	public String getManufacturer() {
		return "ProteoRed + TSRI";
	}

	@Override
	public String getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		return "The ProteoRed MIAPE Extractor tool is a standalone tool that allows: to obtain graphical representations of qualitative information from MIAPE MSI documents; to automatically create MIAPE compliant reports from your standard data files (PRIDE XML, mzML, mzIdentML) and others (XTandem output xml), storing these reports in the ProteoRed MIAPE repository;  to export your MIAPE-compliant reports to standard data files (currently to PRIDE XML).";
	}

	@Override
	public String getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getComments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCatalogNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getURI() {
		return "http://proteo.cnb.csic.es/trac#MIAPEExtractorTool";
	}

	@Override
	public String getCustomizations() {
		// TODO Auto-generated method stub
		return null;
	}

}
