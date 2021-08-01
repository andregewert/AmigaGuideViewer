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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a single document node
 * @author André Gewert <agewert@ubergeek.de>
 */
public final class Node {
    
    // <editor-fold desc="Properties">
    
    private final Document document;

    private String identifier;
    
    private String title;
    
    private String content = "";
    
    private final Map<String, String> attributes;

    // </editor-fold>
    
    
    // <editor-fold desc="Accessors">
    
    /**
     * Return the reference to the linked document
     * @return Reference to the document
     */
    public Document getDocument() {
        return document;
    }
    
    /**
     * Return the node's identification string.
     * This identification string is case insensitive and has to be unique
     * within the document.
     * @return Node identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifcation string
     * @param identifier New identfication string
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier.toLowerCase();
    }
    
    /**
     * Return the title string
     * @return Title of the node
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the display title for this node.
     * The title does not need to be unique.
     * @param title New title
     */
    public void setTitle(String title) {
        if (title == null) {
            this.title = "Untitled";
        } else {
            this.title = title;
        }
    }
    
    /**
     * Return the contents (source code) of this node.
     * @return Content
     */
    public String getContent() {
        return content;
    }
    
    /**
     * Replaces the node's content with the given string
     * @param content New content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Appends the given string to the node's content
     * @param content Content string to be added
     */
    public void appendContent(String content) {
        this.setContent(getContent().concat(content));
    }
    
    /**
     * Setes a node attribute.
     * @param name Attribute name
     * @param value Value
     */
    public void setAttribute(String name, String value) {
        if (name == null || name.isBlank()) return;
        attributes.put(name.toLowerCase(), value);
    }
    
    /**
     * Returns the node attribute value or null
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
     * Checks if a node attribute is defined
     * @param name Name of the attribute
     * @return true if the attribute is defined for this node
     */
    public boolean isAttributeSet(String name) {
        if (name == null || name.isBlank()) return false;
        return attributes.containsKey(name.toLowerCase());
    }
    
    /**
     * Returns the identification string for the node that includes the table
     * of contents.
     * If no table of contents is defined for this node the globally defined
     * toc (if any) will be returned.
     * @return Identifier for the toc node or null
     */
    public String getTocNodeIdentifier() {
        if (isAttributeSet("toc")) {
            return getAttributeValue("toc");
        }
        return document.getTocNodeIdentifier();
    }
    
    /**
     * Return the identification string for the node that includes the index.
     * If no index is defined for this node the globally defined index node
     * (if any) will be returned.
     * @return Identifier for the index node or null
     */
    public String getIndexNodeIdentifier() {
        if (isAttributeSet("index")) {
            return getAttributeValue("index");
        }
        return document.getIndexNodeIdentifier();
    }
    
    /**
     * Gets the identifier for the next node if one is defined
     * @return Identifier for the next node or null
     */
    public String getPreviousNodeIdentifier() {
        return getAttributeValue("prev");
    }
    
    /**
     * Gets the identifier for the previous node if one is defined
     * @return Identifier for the previous node or null
     */
    public String getNextNodeIdentifier() {
        return getAttributeValue("next");
    }
    
    // </editor-fold>
    
    
    // <editor-fold desc="Constructors">
    
    /**
     * Creates a new node which is linked to the given document
     * @param document The parent document
     * @param identifier Identification string
     * @param title Display title
     */
    public Node(Document document, String identifier, String title) {
        this.document = document;
        setIdentifier(identifier);
        setTitle(title);
        attributes = new LinkedHashMap<>();
    }

    /**
     * Creates a new node which is linked to the given document
     * @param document The parent document
     * @param identifier Identification string
     */
    public Node(Document document, String identifier) {
        this(document, identifier, "Untitled");
    }
    
    // </editor-fold>
    
    
    // <editor-fold desc="Public methods">
    
    /**
     * Returns a simple string representation derived from the display title.
     * Currently this string representation is used in the tree list that
     * displays the available contents.
     * @return String representation
     */
    @Override
    public String toString() {
        String displayTitle = getTitle();
        if (displayTitle == null) displayTitle = getIdentifier();
        if (displayTitle.matches(".*\\/.*")) {
            var parts = displayTitle.split("\\/");
            displayTitle = parts[parts.length -1];
        }
        return displayTitle;
    }
    
    /**
     * Returns an html representation of the node contents
     * @return Node contents rendered to html
     */
    public String toHtmlString() {
        var nodeHtmlConverter = new NodeHtmlConverter(this);
        return nodeHtmlConverter.toHtml();
    }
    
    // </editor-fold>
    
}
