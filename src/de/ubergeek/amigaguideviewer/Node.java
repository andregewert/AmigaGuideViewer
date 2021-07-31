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
 *
 * @author agewert
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
    
    public Document getDocument() {
        return document;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier.toLowerCase();
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        if (title == null) {
            this.title = "Untitled";
        } else {
            this.title = title;
        }
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public void appendContent(String content) {
        this.setContent(getContent().concat(content));
    }
    
    public void setAttribute(String name, String value) {
        if (name == null || name.isBlank()) return;
        attributes.put(name.toLowerCase(), value);
    }
    
    public String getAttributeValue(String name) {
        if (name == null || name.isBlank()) return null;
        if (attributes.containsKey(name.toLowerCase())) {
            return attributes.get(name);
        }
        return null;
    }
    
    public boolean isAttributeSet(String name) {
        if (name == null || name.isBlank()) return false;
        return attributes.containsKey(name.toLowerCase());
    }
    
    public String getTocNodeIdentifier() {
        if (isAttributeSet("toc")) {
            return getAttributeValue("toc");
        }
        return document.getTocNodeIdentifier();
    }
    
    public String getIndexNodeIdentifier() {
        if (isAttributeSet("index")) {
            return getAttributeValue("index");
        }
        return document.getIndexNodeIdentifier();
    }
    
    public String getPreviousNodeIdentifier() {
        return getAttributeValue("prev");
    }
    
    public String getNextNodeIdentifier() {
        return getAttributeValue("next");
    }
    
    // </editor-fold>
    
    
    // <editor-fold desc="Constructors">
    
    public Node(Document document, String identifier, String title) {
        this.document = document;
        setIdentifier(identifier);
        setTitle(title);
        attributes = new LinkedHashMap<>();
    }
    
    public Node(Document document, String identifier) {
        this(document, identifier, "Untitled");
    }
    
    // </editor-fold>
    
    
    // <editor-fold desc="Public methods">
    
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
    
    public String toHtmlString() {
        var nodeHtmlConverter = new NodeHtmlConverter(this);
        return nodeHtmlConverter.toHtml();
        //var html = nodeHtmlConverter.toHtml();
        //System.out.println(html);
        //return html;
    }
    
    // </editor-fold>
    
}
