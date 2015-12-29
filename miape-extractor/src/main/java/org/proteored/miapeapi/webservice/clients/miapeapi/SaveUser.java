package org.proteored.miapeapi.webservice.clients.miapeapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>
 * Java class for saveUser complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="saveUser">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="arg0" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg3" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="arg4" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg5" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg6" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg7" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="arg8" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg9" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg10" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg11" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg12" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg13" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg14" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg15" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg16" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg17" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg18" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg19" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg20" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="arg21" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg22" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="arg23" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "saveUser", propOrder = { "arg0", "arg1", "arg2", "arg3", "arg4", "arg5", "arg6",
		"arg7", "arg8", "arg9", "arg10", "arg11", "arg12", "arg13", "arg14", "arg15", "arg16",
		"arg17", "arg18", "arg19", "arg20", "arg21", "arg22", "arg23" })
public class SaveUser {

	protected String arg0;
	protected String arg1;
	protected String arg2;
	protected Integer arg3;
	protected String arg4;
	protected String arg5;
	protected String arg6;
	protected Integer arg7;
	protected String arg8;
	protected String arg9;
	protected String arg10;
	protected String arg11;
	protected String arg12;
	protected String arg13;
	protected String arg14;
	protected String arg15;
	protected String arg16;
	protected String arg17;
	protected String arg18;
	protected String arg19;
	@XmlSchemaType(name = "dateTime")
	protected XMLGregorianCalendar arg20;
	protected String arg21;
	protected String arg22;
	protected String arg23;

	/**
	 * Gets the value of the arg0 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg0() {
		return arg0;
	}

	/**
	 * Sets the value of the arg0 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg0(String value) {
		this.arg0 = value;
	}

	/**
	 * Gets the value of the arg1 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg1() {
		return arg1;
	}

	/**
	 * Sets the value of the arg1 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg1(String value) {
		this.arg1 = value;
	}

	/**
	 * Gets the value of the arg2 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg2() {
		return arg2;
	}

	/**
	 * Sets the value of the arg2 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg2(String value) {
		this.arg2 = value;
	}

	/**
	 * Gets the value of the arg3 property.
	 * 
	 * @return
	 *         possible object is {@link Integer }
	 * 
	 */
	public Integer getArg3() {
		return arg3;
	}

	/**
	 * Sets the value of the arg3 property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setArg3(Integer value) {
		this.arg3 = value;
	}

	/**
	 * Gets the value of the arg4 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg4() {
		return arg4;
	}

	/**
	 * Sets the value of the arg4 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg4(String value) {
		this.arg4 = value;
	}

	/**
	 * Gets the value of the arg5 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg5() {
		return arg5;
	}

	/**
	 * Sets the value of the arg5 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg5(String value) {
		this.arg5 = value;
	}

	/**
	 * Gets the value of the arg6 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg6() {
		return arg6;
	}

	/**
	 * Sets the value of the arg6 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg6(String value) {
		this.arg6 = value;
	}

	/**
	 * Gets the value of the arg7 property.
	 * 
	 * @return
	 *         possible object is {@link Integer }
	 * 
	 */
	public Integer getArg7() {
		return arg7;
	}

	/**
	 * Sets the value of the arg7 property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setArg7(Integer value) {
		this.arg7 = value;
	}

	/**
	 * Gets the value of the arg8 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg8() {
		return arg8;
	}

	/**
	 * Sets the value of the arg8 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg8(String value) {
		this.arg8 = value;
	}

	/**
	 * Gets the value of the arg9 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg9() {
		return arg9;
	}

	/**
	 * Sets the value of the arg9 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg9(String value) {
		this.arg9 = value;
	}

	/**
	 * Gets the value of the arg10 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg10() {
		return arg10;
	}

	/**
	 * Sets the value of the arg10 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg10(String value) {
		this.arg10 = value;
	}

	/**
	 * Gets the value of the arg11 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg11() {
		return arg11;
	}

	/**
	 * Sets the value of the arg11 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg11(String value) {
		this.arg11 = value;
	}

	/**
	 * Gets the value of the arg12 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg12() {
		return arg12;
	}

	/**
	 * Sets the value of the arg12 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg12(String value) {
		this.arg12 = value;
	}

	/**
	 * Gets the value of the arg13 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg13() {
		return arg13;
	}

	/**
	 * Sets the value of the arg13 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg13(String value) {
		this.arg13 = value;
	}

	/**
	 * Gets the value of the arg14 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg14() {
		return arg14;
	}

	/**
	 * Sets the value of the arg14 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg14(String value) {
		this.arg14 = value;
	}

	/**
	 * Gets the value of the arg15 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg15() {
		return arg15;
	}

	/**
	 * Sets the value of the arg15 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg15(String value) {
		this.arg15 = value;
	}

	/**
	 * Gets the value of the arg16 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg16() {
		return arg16;
	}

	/**
	 * Sets the value of the arg16 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg16(String value) {
		this.arg16 = value;
	}

	/**
	 * Gets the value of the arg17 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg17() {
		return arg17;
	}

	/**
	 * Sets the value of the arg17 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg17(String value) {
		this.arg17 = value;
	}

	/**
	 * Gets the value of the arg18 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg18() {
		return arg18;
	}

	/**
	 * Sets the value of the arg18 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg18(String value) {
		this.arg18 = value;
	}

	/**
	 * Gets the value of the arg19 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg19() {
		return arg19;
	}

	/**
	 * Sets the value of the arg19 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg19(String value) {
		this.arg19 = value;
	}

	/**
	 * Gets the value of the arg20 property.
	 * 
	 * @return
	 *         possible object is {@link XMLGregorianCalendar }
	 * 
	 */
	public XMLGregorianCalendar getArg20() {
		return arg20;
	}

	/**
	 * Sets the value of the arg20 property.
	 * 
	 * @param value
	 *            allowed object is {@link XMLGregorianCalendar }
	 * 
	 */
	public void setArg20(XMLGregorianCalendar value) {
		this.arg20 = value;
	}

	/**
	 * Gets the value of the arg21 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg21() {
		return arg21;
	}

	/**
	 * Sets the value of the arg21 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg21(String value) {
		this.arg21 = value;
	}

	/**
	 * Gets the value of the arg22 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg22() {
		return arg22;
	}

	/**
	 * Sets the value of the arg22 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg22(String value) {
		this.arg22 = value;
	}

	/**
	 * Gets the value of the arg23 property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getArg23() {
		return arg23;
	}

	/**
	 * Sets the value of the arg23 property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setArg23(String value) {
		this.arg23 = value;
	}

}
