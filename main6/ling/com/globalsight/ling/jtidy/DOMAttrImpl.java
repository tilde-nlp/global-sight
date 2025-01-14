/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.ling.jtidy;

import org.w3c.dom.DOMException;
import org.w3c.dom.TypeInfo;


/**
 * Tidy implementation of org.w3c.dom.DOMAttrImpl.
 * @author Dave Raggett <a href="mailto:dsr@w3.org">dsr@w3.org </a>
 * @author Andy Quick <a href="mailto:ac.quick@sympatico.ca">ac.quick@sympatico.ca </a> (translation to Java)
 * @author Fabrizio Giustina
 * @version $Revision: 1.1 $ ($Author: yorkjin $)
 */
public class DOMAttrImpl extends DOMNodeImpl implements org.w3c.dom.Attr, Cloneable
{

    /**
     * wrapped com.globalsight.ling.jtidy.AttVal.
     */
    protected AttVal avAdaptee;

    /**
     * instantiates a new DOMAttrImpl which wraps the given AttVal.
     * @param adaptee wrapped AttVal
     */
    protected DOMAttrImpl(AttVal adaptee)
    {
        super(null); // must override all methods of DOMNodeImpl
        this.avAdaptee = adaptee;
    }

    /**
     * @see org.w3c.dom.Node#getNodeValue()
     */
    public String getNodeValue() throws DOMException
    {
        return getValue();
    }

    /**
     * @see org.w3c.dom.Node#setNodeValue(java.lang.String)
     */
    public void setNodeValue(String nodeValue) throws DOMException
    {
        setValue(nodeValue);
    }

    /**
     * @see org.w3c.dom.Node#getNodeName()
     */
    public String getNodeName()
    {
        return getName();
    }

    /**
     * @see org.w3c.dom.Node#getNodeType()
     */
    public short getNodeType()
    {
        return org.w3c.dom.Node.ATTRIBUTE_NODE;
    }

    /**
     * @see org.w3c.dom.Attr#getName
     */
    public String getName()
    {
        return avAdaptee.attribute;
    }

    /**
     * @see org.w3c.dom.Attr#getSpecified
     */
    public boolean getSpecified()
    {
        return avAdaptee.value != null;
    }

    /**
     * @see org.w3c.dom.Attr#getValue
     */
    public String getValue()
    {
        // Thanks to Brett Knights brett@knightsofthenet.com for this fix.
        return (avAdaptee.value == null) ? avAdaptee.attribute : avAdaptee.value;
    }

    /**
     * @see org.w3c.dom.Attr#setValue(java.lang.String)
     */
    public void setValue(String value)
    {
        avAdaptee.value = value;
    }

    /**
     * @see org.w3c.dom.Node#getParentNode()
     */
    public org.w3c.dom.Node getParentNode()
    {
        // Attr.getParentNode() should always return null
        // http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-637646024
        return null;
    }

    /**
     * @todo DOM level 2 getChildNodes() Not implemented. Returns an empty NodeList.
     * @see org.w3c.dom.Node#getChildNodes()
     */
    public org.w3c.dom.NodeList getChildNodes()
    {
        // Calling getChildNodes on a DOM Attr node does return the children of the Attr, which are the text and
        // EntityReference nodes that make up the Attr's content.
        return new DOMNodeListImpl(null);
    }

    /**
     * @todo DOM level 2 getFirstChild() Not implemented. Returns null.
     * @see org.w3c.dom.Node#getFirstChild()
     */
    public org.w3c.dom.Node getFirstChild()
    {
        return null;
    }

    /**
     * @todo DOM level 2 getLastChild() Not implemented. Returns null.
     * @see org.w3c.dom.Node#getLastChild()
     */
    public org.w3c.dom.Node getLastChild()
    {
        return null;
    }

    /**
     * @see org.w3c.dom.Node#getPreviousSibling()
     */
    public org.w3c.dom.Node getPreviousSibling()
    {
        // Attr.getPreviousSibling() should always return null
        return null;
    }

    /**
     * @see org.w3c.dom.Node#getNextSibling()
     */
    public org.w3c.dom.Node getNextSibling()
    {
        // Attr.getNextSibling() should always return null
        return null;
    }

    /**
     * @see org.w3c.dom.Node#getAttributes()
     */
    public org.w3c.dom.NamedNodeMap getAttributes()
    {
        return null;
    }

    /**
     * @todo DOM level 2 getOwnerDocument() Not implemented. Returns null.
     * @see org.w3c.dom.Node#getOwnerDocument()
     */
    public org.w3c.dom.Document getOwnerDocument()
    {
        return null;
    }

    /**
     * Not supported.
     * @see org.w3c.dom.Node#insertBefore(org.w3c.dom.Node, org.w3c.dom.Node)
     */
    public org.w3c.dom.Node insertBefore(org.w3c.dom.Node newChild, org.w3c.dom.Node refChild) throws DOMException
    {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "Not supported");
    }

    /**
     * Not supported.
     * @see org.w3c.dom.Node#replaceChild(org.w3c.dom.Node, org.w3c.dom.Node)
     */
    public org.w3c.dom.Node replaceChild(org.w3c.dom.Node newChild, org.w3c.dom.Node oldChild) throws DOMException
    {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "Not supported");
    }

    /**
     * Not supported.
     * @see org.w3c.dom.Node#removeChild(org.w3c.dom.Node)
     */
    public org.w3c.dom.Node removeChild(org.w3c.dom.Node oldChild) throws DOMException
    {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "Not supported");
    }

    /**
     * Not supported.
     * @see org.w3c.dom.Node#appendChild(org.w3c.dom.Node)
     */
    public org.w3c.dom.Node appendChild(org.w3c.dom.Node newChild) throws DOMException
    {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, "Not supported");
    }

    /**
     * @see org.w3c.dom.Node#hasChildNodes()
     */
    public boolean hasChildNodes()
    {
        return false;
    }

    /**
     * @see org.w3c.dom.Node#cloneNode(boolean)
     */
    public org.w3c.dom.Node cloneNode(boolean deep)
    {
        // http://java.sun.com/j2se/1.5.0/docs/api/index.html?org/w3c/dom/Attr.html
        // Cloning an Attr always clones its children, since they represent its value, no matter whether this is a deep
        // clone or not.
        return (org.w3c.dom.Node) clone();
    }

    /**
     * @todo DOM level 2 getOwnerElement() Not implemented. Returns null.
     * @see org.w3c.dom.Attr#getOwnerElement()
     */
    public org.w3c.dom.Element getOwnerElement()
    {
        return null;
    }

    /**
     * @todo DOM level 3 getSchemaTypeInfo() Not implemented. Returns null.
     * @see org.w3c.dom.Attr#getSchemaTypeInfo()
     */
    public TypeInfo getSchemaTypeInfo()
    {
        return null;
    }

    /**
     * @see org.w3c.dom.Attr#isId()
     */
    public boolean isId()
    {
        return "id".equals(this.avAdaptee.getAttribute());
    }

    /**
     * @see java.lang.Object#clone()
     */
    protected Object clone()
    {
        DOMAttrImpl clone;
        try
        {
            clone = (DOMAttrImpl) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            // should never happen
            throw new RuntimeException("Clone not supported");
        }
        clone.avAdaptee = (AttVal) this.avAdaptee.clone();
        return clone;
    }
}
