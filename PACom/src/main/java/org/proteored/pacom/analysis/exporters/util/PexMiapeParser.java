package org.proteored.pacom.analysis.exporters.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.proteored.miapeapi.cv.AdditionalInformationName;
import org.proteored.miapeapi.cv.CellTypes;
import org.proteored.miapeapi.cv.ControlVocabularyManager;
import org.proteored.miapeapi.cv.ControlVocabularyTerm;
import org.proteored.miapeapi.cv.ExperimentAdditionalParameter;
import org.proteored.miapeapi.cv.HumanDisseases;
import org.proteored.miapeapi.cv.MainTaxonomies;
import org.proteored.miapeapi.cv.SampleProcessingStep;
import org.proteored.miapeapi.cv.TissuesTypes;
import org.proteored.miapeapi.cv.ms.DataProcessingAction;
import org.proteored.miapeapi.cv.ms.DataTransformation;
import org.proteored.miapeapi.cv.ms.ExperimentType;
import org.proteored.miapeapi.cv.ms.InstrumentModel;
import org.proteored.miapeapi.cv.ms.SpectrometerName;
import org.proteored.miapeapi.interfaces.Software;
import org.proteored.miapeapi.interfaces.ms.DataAnalysis;
import org.proteored.miapeapi.interfaces.ms.MSAdditionalInformation;
import org.proteored.miapeapi.interfaces.ms.MiapeMSDocument;
import org.proteored.miapeapi.interfaces.ms.Spectrometer;
import org.proteored.miapeapi.interfaces.msi.MSIAdditionalInformation;
import org.proteored.miapeapi.interfaces.msi.MiapeMSIDocument;
import org.proteored.pacom.gui.tasks.OntologyLoaderTask;

public class PexMiapeParser {
	private static final Logger log = Logger
			.getLogger("log4j.logger.org.proteored");

	private static ControlVocabularyManager cvManager = OntologyLoaderTask
			.getCvManager();

	public static Set<String> getSpecies(MiapeMSDocument miapeMSDocument) {
		Set<String> ret = new HashSet<String>();
		if (miapeMSDocument != null) {
			List<MSAdditionalInformation> additionalInformations = miapeMSDocument
					.getAdditionalInformations();
			if (additionalInformations != null) {
				for (MSAdditionalInformation msAdditionalInformation : additionalInformations) {
					String specieName = msAdditionalInformation.getName();
					String parseSpecie = PexMiapeParser.parseSpecie(specieName,
							msAdditionalInformation.getValue());
					if (parseSpecie != null)
						ret.add(parseSpecie);
					log.info("Specie= " + parseSpecie);
				}
			}
		}
		return ret;
	}

	/**
	 * Returns the specie name from a name-value pair. If the name is not a
	 * CVTerm from {@link MainTaxonomies}, it will return null
	 * 
	 * @param specieName
	 * @param value
	 * @return
	 */
	public static String parseSpecie(String specieName, String value) {
		ControlVocabularyTerm cvTermByPreferredName = MainTaxonomies
				.getInstance(cvManager).getCVTermByPreferredName(specieName);
		if (cvTermByPreferredName != null) {
			String text = "[" + cvTermByPreferredName.getCVRef() + ","
					+ cvTermByPreferredName.getTermAccession().toString() + ","
					+ cvTermByPreferredName.getPreferredName() + ",";
			if (value != null)
				text = text + replaceNotAllowedCharacteres(value);
			text = text + "]";

			return text;
		}
		return null;
	}

	public static Set<String> getInstruments(MiapeMSDocument miapeMSDocument) {
		Set<String> ret = new HashSet<String>();
		if (miapeMSDocument != null) {
			Set<Spectrometer> spectrometers = miapeMSDocument
					.getSpectrometers();
			if (spectrometers != null) {
				for (Spectrometer spectrometer : spectrometers) {
					String text = "";
					// Name
					ControlVocabularyTerm cvTermByPreferredName = SpectrometerName
							.getInstance(cvManager).getCVTermByPreferredName(
									spectrometer.getName());
					if (cvTermByPreferredName != null) {
						text = "["
								+ cvTermByPreferredName.getCVRef()
								+ ","
								+ cvTermByPreferredName.getTermAccession()
										.toString() + ","
								+ cvTermByPreferredName.getPreferredName()
								+ ",]";
					} else {
						// [MS, MS:1000031, instrument model, custom name]
						text = "[MS, MS:1000031, instrument model,"
								+ replaceNotAllowedCharacteres(spectrometer
										.getName()) + "]";
					}

					// // Model
					// if (spectrometer.getModel() != null) {
					// cvTermByPreferredName = InstrumentModel.getInstance(
					// cvManager).getCVTermByPreferredName(
					// spectrometer.getModel());
					// if (cvTermByPreferredName != null) {
					// if (!"".equals(text))
					// text += ",";
					// text += "["
					// + cvTermByPreferredName.getCVRef()
					// + ","
					// + cvTermByPreferredName.getTermAccession()
					// .toString() + ","
					// + cvTermByPreferredName.getPreferredName()
					// + ",]";
					// } else {
					// // userPAram
					// if (!"".equals(text))
					// text += ",";
					// text += "[,,Model,"
					// + replaceNotAllowedCharacteres(spectrometer
					// .getModel()) + "]";
					// }
					// }
					ret.add(text);
					// Manufacturer
					text = "";
					if (spectrometer.getManufacturer() != null) {
						cvTermByPreferredName = InstrumentModel.getInstance(
								cvManager).getCVTermByPreferredName(
								spectrometer.getManufacturer());
						if (cvTermByPreferredName != null) {
							text += "["
									+ cvTermByPreferredName.getCVRef()
									+ ","
									+ cvTermByPreferredName.getTermAccession()
											.toString() + ","
									+ cvTermByPreferredName.getPreferredName()
									+ ",]";
						} else {
							// userPArams not allowed
							// text += "[,,Manufacturer,"
							// + replaceNotAllowedCharacteres(spectrometer
							// .getManufacturer()) + "]";
						}
					}
					// Customizations
					// if (spectrometer.getCustomizations() != null) {
					// cvTermByPreferredName = InstrumentCustomization
					// .getInstance(cvManager)
					// .getCVTermByPreferredName(
					// spectrometer.getCustomizations());
					// if (cvTermByPreferredName != null) {
					// if (!"".equals(text))
					// text += ",";
					// text += "["
					// + cvTermByPreferredName.getCVRef()
					// + ","
					// + cvTermByPreferredName.getTermAccession()
					// .toString() + ","
					// + cvTermByPreferredName.getPreferredName()
					// + ",]";
					// } else {
					// // userPAram
					// if (!"".equals(text))
					// text += ",";
					// text += "[,,Customizations,"
					// + replaceNotAllowedCharacteres(spectrometer
					// .getCustomizations()) + "]";
					// }
					// }
					if (spectrometer.getVersion() != null
							&& !"".equals(spectrometer.getVersion())) {
						// userPAram
						if (!"".equals(text))
							text += ",";
						text += "[,,Version,"
								+ replaceNotAllowedCharacteres(spectrometer
										.getVersion()) + "]";
					}
					// if (spectrometer.getDescription() != null) {
					// // userPAram
					// if (!"".equals(text))
					// text += ",";
					// text += "[,,Description,"
					// + replaceNotAllowedCharacteres(spectrometer
					// .getDescription()) + "]";
					// }
					// if (spectrometer.getComments() != null) {
					// // userPAram
					// if (!"".equals(text))
					// text += ",";
					// text += "[,,Comments,"
					// + replaceNotAllowedCharacteres(spectrometer
					// .getComments()) + "]";
					// }
					// if (spectrometer.getParameters() != null) {
					// // userPAram
					// if (!"".equals(text))
					// text += ",";
					// text += "[,,Instrument parameters,"
					// + replaceNotAllowedCharacteres(spectrometer
					// .getParameters()) + "]";
					// }
					log.info("Instrument = " + text);
					ret.add(text);
				}
			}
		}
		return ret;
	}

	public static Set<String> getSampleProcessingProtocols(
			MiapeMSDocument miapeMSDocument) {
		Set<String> ret = new HashSet<String>();
		if (miapeMSDocument != null) {
			List<MSAdditionalInformation> additionalInformations = miapeMSDocument
					.getAdditionalInformations();
			if (additionalInformations != null) {
				for (MSAdditionalInformation msAdditionalInformation : additionalInformations) {
					ControlVocabularyTerm sampleProcessingTerm = SampleProcessingStep
							.getInstance(cvManager).getCVTermByPreferredName(
									msAdditionalInformation.getName());
					if (sampleProcessingTerm != null) {
						String name = replaceNotAllowedCharacteres(msAdditionalInformation
								.getName());
						String value = replaceNotAllowedCharacteres(msAdditionalInformation
								.getValue());
						String text = name;
						if (value != null && !"".equals(value))
							text += ": " + value;
						log.info("sample prep protocol= " + text);
						ret.add(text);
					}
				}
			}
		}
		return ret;

	}

	public static Set<String> getExperimentTypes(MiapeMSDocument miapeMSDocument) {
		Set<String> ret = new HashSet<String>();
		if (miapeMSDocument != null) {
			List<MSAdditionalInformation> additionalInformations = miapeMSDocument
					.getAdditionalInformations();
			if (additionalInformations != null) {
				for (MSAdditionalInformation msAdditionalInformation : additionalInformations) {
					ControlVocabularyTerm experimentType = ExperimentType
							.getInstance(cvManager).getCVTermByPreferredName(
									msAdditionalInformation.getName());
					if (experimentType != null) {
						String text = "[" + experimentType.getCVRef() + ","
								+ experimentType.getTermAccession().toString()
								+ "," + experimentType.getPreferredName() + ",";
						if (msAdditionalInformation.getValue() != null) {
							text += msAdditionalInformation.getValue();
						}
						text += "]";
						log.info("experiment type= " + text);
						ret.add(text);
					}
				}
			}
		}
		return ret;
	}

	public static Set<String> getExperimentTypes(
			MiapeMSIDocument miapeMSIDocument) {
		Set<String> ret = new HashSet<String>();
		if (miapeMSIDocument != null) {
			Set<MSIAdditionalInformation> additionalInformations = miapeMSIDocument
					.getAdditionalInformations();
			if (additionalInformations != null) {
				for (MSIAdditionalInformation msAdditionalInformation : additionalInformations) {
					ControlVocabularyTerm experimentType = ExperimentType
							.getInstance(cvManager).getCVTermByPreferredName(
									msAdditionalInformation.getName());
					if (experimentType != null) {
						String text = "[" + experimentType.getCVRef() + ","
								+ experimentType.getTermAccession().toString()
								+ "," + experimentType.getPreferredName() + ",";
						if (msAdditionalInformation.getValue() != null) {
							text += msAdditionalInformation.getValue();
						}
						text += "]";
						log.info("experiment type= " + text);
						ret.add(text);
					}
				}
			}
		}
		return ret;
	}

	public static Set<String> getDataProcessingProtocols(
			MiapeMSDocument miapeMSDocument) {
		Set<String> ret = new HashSet<String>();
		if (miapeMSDocument != null) {
			List<MSAdditionalInformation> additionalInformations = miapeMSDocument
					.getAdditionalInformations();
			if (additionalInformations != null) {
				for (MSAdditionalInformation msAdditionalInformation : additionalInformations) {
					ControlVocabularyTerm cvTerm = DataProcessingAction
							.getInstance(cvManager).getCVTermByPreferredName(
									msAdditionalInformation.getName());
					if (cvTerm != null) {
						String name = replaceNotAllowedCharacteres(msAdditionalInformation
								.getName());
						String value = replaceNotAllowedCharacteres(msAdditionalInformation
								.getValue());
						String text = name;
						if (value != null && !"".equals(value))
							text += ": " + value;
						log.info("data processing protocol= " + text);
						ret.add(text);
					}
					cvTerm = DataTransformation.getInstance(cvManager)
							.getCVTermByPreferredName(
									msAdditionalInformation.getName());
					if (cvTerm != null) {
						String name = replaceNotAllowedCharacteres(msAdditionalInformation
								.getName());
						String value = replaceNotAllowedCharacteres(msAdditionalInformation
								.getValue());
						String text = name;
						if (value != null && !"".equals(value))
							text += ": " + value;
						log.info("data processing protocol= " + text);
						ret.add(text);
					}
				}
			}
		}
		return ret;

	}

	public static Set<String> getDataProcessingProtocols(
			MiapeMSIDocument miapeMSIDocument) {
		Set<String> ret = new HashSet<String>();
		if (miapeMSIDocument != null) {
			// search engine
			final Set<Software> softwares = miapeMSIDocument.getSoftwares();
			if (softwares != null) {
				String plural = softwares.size() > 1 ? "s" : "";
				for (Software software : softwares) {
					StringBuilder sb = new StringBuilder();
					sb.append(software.getName());
					if (software.getVersion() != null) {
						sb.append(" version:" + software.getVersion() + " ");
					}
					ret.add("Samples were analyzed using software" + plural
							+ ": " + sb.toString().trim());
				}
			}
			// addtiional informations
			Set<MSIAdditionalInformation> additionalInformations = miapeMSIDocument
					.getAdditionalInformations();
			if (additionalInformations != null) {
				for (MSIAdditionalInformation msAdditionalInformation : additionalInformations) {
					ControlVocabularyTerm cvTerm = DataProcessingAction
							.getInstance(cvManager).getCVTermByPreferredName(
									msAdditionalInformation.getName());
					if (cvTerm != null) {
						String name = replaceNotAllowedCharacteres(msAdditionalInformation
								.getName());
						String value = replaceNotAllowedCharacteres(msAdditionalInformation
								.getValue());
						String text = name;
						if (value != null && !"".equals(value))
							text += ": " + value;
						log.info("data processing protocol= " + text);
						ret.add(text);
					}
					cvTerm = DataTransformation.getInstance(cvManager)
							.getCVTermByPreferredName(
									msAdditionalInformation.getName());
					if (cvTerm != null) {
						String name = replaceNotAllowedCharacteres(msAdditionalInformation
								.getName());
						String value = replaceNotAllowedCharacteres(msAdditionalInformation
								.getValue());
						String text = name;
						if (value != null && !"".equals(value))
							text += ": " + value;
						log.info("data processing protocol= " + text);
						ret.add(text);
					}
				}
			}
		}
		return ret;

	}

	public static Set<String> getAdditionals(MiapeMSDocument miapeMSDocument) {
		Set<String> ret = new HashSet<String>();
		if (miapeMSDocument != null) {
			final Set<DataAnalysis> dataAnalysises = miapeMSDocument
					.getDataAnalysis();
			if (dataAnalysises != null) {
				for (DataAnalysis dataAnalysis : dataAnalysises) {
					StringBuilder sb = new StringBuilder();
					sb.append(dataAnalysis.getName());
					if (dataAnalysis.getDescription() != null) {
						sb.append(dataAnalysis.getDescription() + " ");
					}
					if (dataAnalysis.getComments() != null) {
						sb.append(dataAnalysis.getComments() + " ");
					}
					if (dataAnalysis.getParameters() != null) {
						sb.append(dataAnalysis.getParameters() + " ");
					}
					ret.add("[,," + sb.toString().trim() + ",]");
				}
			}
			List<MSAdditionalInformation> additionalInformations = miapeMSDocument
					.getAdditionalInformations();
			if (additionalInformations != null) {
				for (MSAdditionalInformation msAdditionalInformation : additionalInformations) {
					ControlVocabularyTerm taxonomyTerm = MainTaxonomies
							.getInstance(cvManager).getCVTermByPreferredName(
									msAdditionalInformation.getName());
					ControlVocabularyTerm sampleProcessingTerm = SampleProcessingStep
							.getInstance(cvManager).getCVTermByPreferredName(
									msAdditionalInformation.getName());
					ControlVocabularyTerm dataProcessingTerm = DataProcessingAction
							.getInstance(cvManager).getCVTermByPreferredName(
									msAdditionalInformation.getName());
					ControlVocabularyTerm dataTransformationTerm = DataProcessingAction
							.getInstance(cvManager).getCVTermByPreferredName(
									msAdditionalInformation.getName());
					ControlVocabularyTerm tissue = TissuesTypes.getInstance(
							cvManager).getCVTermByPreferredName(
							msAdditionalInformation.getName());
					ControlVocabularyTerm cellType = CellTypes.getInstance(
							cvManager).getCVTermByPreferredName(
							msAdditionalInformation.getName());
					ControlVocabularyTerm humanDissease = HumanDisseases
							.getInstance(cvManager).getCVTermByPreferredName(
									msAdditionalInformation.getName());

					if (taxonomyTerm != null || sampleProcessingTerm != null
							|| dataTransformationTerm != null
							|| dataProcessingTerm != null || tissue != null
							|| cellType != null || humanDissease != null) {
						log.info("This is a CV for taxonomy or sample processing or data transformation or data processing term or tissue or cell type ("
								+ msAdditionalInformation.getName()
								+ "), so it should be catured in species");
					} else {
						ControlVocabularyTerm cvTerm = AdditionalInformationName
								.getInstance(cvManager)
								.getCVTermByPreferredName(
										msAdditionalInformation.getName());
						if (cvTerm != null) {

							String text = "[" + cvTerm.getCVRef() + ","
									+ cvTerm.getTermAccession().toString()
									+ "," + cvTerm.getPreferredName() + ",]";
							log.info("Add Info= " + text);
							ret.add(text);
						} else {
							String name = replaceNotAllowedCharacteres(msAdditionalInformation
									.getName());
							String value = replaceNotAllowedCharacteres(msAdditionalInformation
									.getValue());
							// userPAram
							String text = "[,," + name + ",";
							if (value != null)
								text = text + value;
							text = text + "]";
							log.info("Add Info= " + text);
							ret.add(text);
						}
					}
				}
			}
		}
		return ret;
	}

	public static Set<String> getAdditionals(MiapeMSIDocument miapeMSIDocument) {
		Set<String> ret = new HashSet<String>();
		if (miapeMSIDocument != null) {
			Set<MSIAdditionalInformation> additionalInformations = miapeMSIDocument
					.getAdditionalInformations();
			if (additionalInformations != null) {
				for (MSIAdditionalInformation msiAdditionalInformation : additionalInformations) {
					ControlVocabularyTerm cvTermByPreferredName = MainTaxonomies
							.getInstance(cvManager).getCVTermByPreferredName(
									msiAdditionalInformation.getName());
					if (cvTermByPreferredName != null) {
						log.info("This is a CV for taxonomy, so it should be catured in species");
					} else {
						ControlVocabularyTerm cvTerm = AdditionalInformationName
								.getInstance(cvManager)
								.getCVTermByPreferredName(
										msiAdditionalInformation.getName());
						if (cvTerm != null) {
							String text = "[" + cvTerm.getCVRef() + ","
									+ cvTerm.getTermAccession().toString()
									+ "," + cvTerm.getPreferredName() + ",]";
							log.info("Add Info= " + text);
							ret.add(text);
						} else {
							// userPAram
							String name = replaceNotAllowedCharacteres(msiAdditionalInformation
									.getName());

							String value = replaceNotAllowedCharacteres(msiAdditionalInformation
									.getValue());

							String text = "[,," + name + ",";
							if (value != null)
								text = text + value;
							text = text + "]";
							log.info("Add Info= " + text);
							ret.add(text);
						}
					}
				}
			}
		}
		return ret;
	}

	private static String replaceNotAllowedCharacteres(String input) {
		if (input != null) {
			String ret = input.replace("\n", "-");
			ret = ret.replace(",", "-");
			return ret;
		}
		return null;
	}

	public static Set<String> getTissues(MiapeMSDocument miapeMSDocument) {
		Set<String> ret = new HashSet<String>();
		if (miapeMSDocument != null) {
			List<MSAdditionalInformation> additionalInformations = miapeMSDocument
					.getAdditionalInformations();
			if (additionalInformations != null) {
				for (MSAdditionalInformation msAdditionalInformation : additionalInformations) {
					ControlVocabularyTerm experimentType = TissuesTypes
							.getInstance(cvManager).getCVTermByPreferredName(
									msAdditionalInformation.getName());
					if (experimentType != null) {
						String text = "[" + experimentType.getCVRef() + ","
								+ experimentType.getTermAccession().toString()
								+ "," + experimentType.getPreferredName() + ",";
						if (msAdditionalInformation.getValue() != null) {
							text += msAdditionalInformation.getValue();
						}
						text += "]";
						log.info("tissue= " + text);
						ret.add(text);
					}
				}
			}
		}
		return ret;
	}

	public static Set<String> getCellTypes(MiapeMSDocument miapeMSDocument) {
		Set<String> ret = new HashSet<String>();
		if (miapeMSDocument != null) {
			List<MSAdditionalInformation> additionalInformations = miapeMSDocument
					.getAdditionalInformations();
			if (additionalInformations != null) {
				for (MSAdditionalInformation msAdditionalInformation : additionalInformations) {
					ControlVocabularyTerm cellTypes = CellTypes.getInstance(
							cvManager).getCVTermByPreferredName(
							msAdditionalInformation.getName());
					if (cellTypes != null) {
						String text = "[" + cellTypes.getCVRef() + ","
								+ cellTypes.getTermAccession().toString() + ","
								+ cellTypes.getPreferredName() + ",";
						if (msAdditionalInformation.getValue() != null) {
							text += msAdditionalInformation.getValue();
						}
						text += "]";
						log.info("cell type= " + text);
						ret.add(text);
					}
				}
			}
		}
		return ret;
	}

	public static Set<String> getDisseases(MiapeMSDocument miapeMSDocument) {
		Set<String> ret = new HashSet<String>();
		if (miapeMSDocument != null) {
			List<MSAdditionalInformation> additionalInformations = miapeMSDocument
					.getAdditionalInformations();
			if (additionalInformations != null) {
				for (MSAdditionalInformation msAdditionalInformation : additionalInformations) {
					ControlVocabularyTerm experimentType = HumanDisseases
							.getInstance(cvManager).getCVTermByPreferredName(
									msAdditionalInformation.getName());
					if (experimentType != null) {
						String text = "[" + experimentType.getCVRef() + ","
								+ experimentType.getTermAccession().toString()
								+ "," + experimentType.getPreferredName() + ",";
						if (msAdditionalInformation.getValue() != null) {
							text += msAdditionalInformation.getValue();
						}
						text += "]";
						log.info("dissease= " + text);
						ret.add(text);
					}
				}
			}
		}
		return ret;
	}

	public static String getTissueNotApplicable() {
		ControlVocabularyTerm tissueNotApplicableTerm = ExperimentAdditionalParameter
				.getInstance(cvManager).getTissueNotApplicableTerm();
		return "[" + tissueNotApplicableTerm.getCVRef() + ","
				+ tissueNotApplicableTerm.getTermAccession().toString() + ","
				+ tissueNotApplicableTerm.getPreferredName() + ",]";
	}

	public static void main(String[] args) {
		final String tissueNotApplicable = PexMiapeParser
				.getTissueNotApplicable();
		System.out.println(tissueNotApplicable);
	}
}
