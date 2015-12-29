package org.proteored.miapeapi.webservice.clients.miapeapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for integerString complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="integerString">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="miapeID" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="miapeType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "integerString", propOrder = { "miapeID", "miapeType" })
public class IntegerString {

	protected Integer miapeID;
	protected String miapeType;

	/**
	 * Gets the value of the miapeID property.
	 * 
	 * @return
	 *         possible object is {@link Integer }
	 * 
	 */
	public Integer getMiapeID() {
		return miapeID;
	}

	/**
	 * Sets the value of the miapeID property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setMiapeID(Integer value) {
		this.miapeID = value;
	}

	/**
	 * Gets the value of the miapeType property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getMiapeType() {
		return miapeType;
	}

	/**
	 * Sets the value of the miapeType property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMiapeType(String value) {
		this.miapeType = value;
	}

}
