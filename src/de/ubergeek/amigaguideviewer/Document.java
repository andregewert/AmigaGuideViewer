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
 *
 * @author agewert
 */
public class Document {

    // <editor-fold desc="Properties">
    
    private Node firstNode;
    
    private final Map<String, Node> nodes;
    
    private final Map<String, String> attributes;
    
    // </editor-fold>

    
    // <editor-fold desc="Accessors">

    public Node getFirstNode() {
        return firstNode;
    }
    
    public void setFirstNode(Node node) {
        if (!nodes.containsValue(node)) return;
        firstNode = node;
    }
    
    public List<Node> getNodesList() {
        return new ArrayList<>(nodes.values());
    }
    
    // </editor-fold>
    
    
    // <editor-fold desc="Constructors">
    
    public Document() {
        nodes = new LinkedHashMap<>();
        attributes = new LinkedHashMap<>();
    }
    
    // </editor-fold>
    
    
    // <editor-fold desc="Public methods">

    public Node createAddNode(String identifier, String title) {
        var node = new Node(this, identifier, title);
        nodes.put(identifier.toLowerCase(), node);
        if (nodes.size() == 1) {
            firstNode = node;
        }
        return node;
    }
    
    public Node getNodeByIdentifier(String identifier) {
        if (nodes.containsKey(identifier.toLowerCase())) {
            return nodes.get(identifier.toLowerCase());
        }
        return null;
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
        return null;
    }
    
    public String getIndexNodeIdentifier() {
        if (isAttributeSet("index")) {
            return getAttributeValue("index");
        }
        return null;
    }
    
    // </editor-fold>
    
}
