//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.06.22 at 09:36:55 PM PDT 
//


package org.proteored.miapeExtractor.analysis.conf.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{}CP_Node">
 *       &lt;sequence>
 *         &lt;element ref="{}CP_MSI_List"/>
 *         &lt;element ref="{}CP_MS_List"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "cpmsiList",
    "cpmsList"
})
@XmlRootElement(name = "CP_Replicate")
public class CPReplicate
    extends CPNode
{

    @XmlElement(name = "CP_MSI_List", required = true)
    protected CPMSIList cpmsiList;
    @XmlElement(name = "CP_MS_List", required = true)
    protected CPMSList cpmsList;

    /**
     * Gets the value of the cpmsiList property.
     * 
     * @return
     *     possible object is
     *     {@link CPMSIList }
     *     
     */
    public CPMSIList getCPMSIList() {
        return cpmsiList;
    }

    /**
     * Sets the value of the cpmsiList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CPMSIList }
     *     
     */
    public void setCPMSIList(CPMSIList value) {
        this.cpmsiList = value;
    }

    /**
     * Gets the value of the cpmsList property.
     * 
     * @return
     *     possible object is
     *     {@link CPMSList }
     *     
     */
    public CPMSList getCPMSList() {
        return cpmsList;
    }

    /**
     * Sets the value of the cpmsList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CPMSList }
     *     
     */
    public void setCPMSList(CPMSList value) {
        this.cpmsList = value;
    }

}
