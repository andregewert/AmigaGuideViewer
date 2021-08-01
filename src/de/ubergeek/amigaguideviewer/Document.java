/*
 * Copyright (C) 2021 André Gewert <agewert@ubergeek.de>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.ubergeek.amigaguideviewer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an AmigaGuide document
 * @author André Gewert <agewert@ubergeek.de>
 */
public class Document {

    // <editor-fold desc="Properties">
    
    private Node firstNode;
    
    private final Map<String, Node> nodes;
    
    private final Map<String, String> attributes;
    
    // </editor-fold>

    
    // <editor-fold desc="Accessors">

    /**
     * Returns the first node that should be shown when the document is loaded.
     * Normally this is the first node in the document.
     * @return Title node
     */
    public Node getTitleNode() {
        return firstNode;
    }
    
    /**
     * Sets the title node for this document.
     * @param node The node that should be used as the title
     */
    public void setTitleNode(Node node) {
        if (!nodes.containsValue(node)) return;
        firstNode = node;
    }
    
    /**
     * Returns a list with all existing document nodes
     * @return A list with all existing document nodes
     */
    public List<Node> getNodesList() {
        return new ArrayList<>(nodes.values());
    }
    
    // </editor-fold>
    
    
    // <editor-fold desc="Constructors">
    
    /**
     * The constructor does not need / accepts arguments
     */
    public Document() {
        nodes = new LinkedHashMap<>();
        attributes = new LinkedHashMap<>();
    }
    
    // </editor-fold>
    
    
    // <editor-fold desc="Public methods">

    /**
     * Created a new document node and adds it's reference to the list of nodes
     * 
     * @param identifier The node's identification string
     * @param title Node title
     * @return Reference to the new document node
     */
    public Node createAndAddNode(String identifier, String title) {
        var node = new Node(this, identifier, title);
        nodes.put(identifier.toLowerCase(), node);
        if (nodes.size() == 1) {
            firstNode = node;
        }
        return node;
    }
    
    /**
     * Tries to find a document node with the given identification string.
     * If no node with the given identifier is existing the method returns null.
     * @param identifier The node's identification string
     * @return Reference to the found document node or null
     */
    public Node getNodeByIdentifier(String identifier) {
        if (nodes.containsKey(identifier.toLowerCase())) {
            return nodes.get(identifier.toLowerCase());
        }
        return null;
    }
    
    /**
     * Setes a global attribute.
     * @param name Attribute name
     * @param value Value
     */
    public void setAttribute(String name, String value) {
        if (name == null || name.isBlank()) return;
        attributes.put(name.toLowerCase(), value);
    }
    
    /**
     * Returns the global attribute value or null
     * @param name Name of the attribute
     * @return The attribute value or null
     */
    public String getAttributeValue(String name) {
        if (name == null || name.isBlank()) return null;
        if (attributes.containsKey(name.toLowerCase())) {
            return attributes.get(name);
        }
        return null;
    }
    
    /**
     * Checks if a global attribute is defined
     * @param name Name of the attribute
     * @return true if the attribute is defined for this document
     */
    public boolean isAttributeSet(String name) {
        if (name == null || name.isBlank()) return false;
        return attributes.containsKey(name.toLowerCase());
    }
    
    /**
     * Returns the identification string for the node that includes the table
     * of contents.
     * If no table of contents is defined null will be returned.
     * @return Identifier for the toc node or null
     */
    public String getTocNodeIdentifier() {
        if (isAttributeSet("toc")) {
            return getAttributeValue("toc");
        }
        return null;
    }
    
    /**
     * Return the identification string for the node that includes the index.
     * If no index is defined null will be returned.
     * @return Identifier for the index node or null
     */
    public String getIndexNodeIdentifier() {
        if (isAttributeSet("index")) {
            return getAttributeValue("index");
        }
        return null;
    }
    
    // </editor-fold>
    
}
