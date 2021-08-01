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

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * Generates html code from document nodes
 * @author André Gewert <agewert@ubergeek.de>
 */
public class NodeHtmlConverter {

    // <editor-fold desc="Properties">
    
    private Node node;
    
    private boolean isIOpen = false;
    
    private boolean isBOpen = false;
    
    private boolean isUOpen = false;
    
    private boolean isCodeOpen = false;
    
    private int openFontTags = 0;
    
    // </editor-fold>
    
    
    // <editor-fold desc="Constructors">
    
    /**
     * Empty constructor
     */
    public NodeHtmlConverter() {
        this(null);
    }

    /**
     * This constructor takes the node that should be rendered as an argument
     * @param node The document node to be rendered / converted
     */
    public NodeHtmlConverter(Node node) {
        this.node = node;
    }
    
    // </editor-fold>
    
    
    // <editor-fold desc="Public interface">

    /**
     * Sets the node that should be rendered.
     * @param node Reference to the node that should be rendered
     */
    public void setNode(Node node) {
        this.node = node;
    }
    
    /**
     * Renders the node contents to html and returns the results as a string
     * @return HTML representation of the node contents
     */
    public String toHtml() {
        if (node == null) return "";
        
        var sb = new StringBuilder();
        var tokenizer = new Tokenizer(node.getContent());

        sb.append("<html><head><style type=\"text/css\">a { background-color: #eeeeee !important; color: #486fb5 !important; } span.code { }</style></head><body><a name=\"top\"></a><pre>");

        // Translate content to html
        while (tokenizer.parseNextToken()) {
            var token = tokenizer.getToken();
            
            // Single @ sign (neither quoted nor part of a command)
            if (token.equals("@")) {
                sb.append(token);
            }
            
            // Escaped back slash
            else if (token.equals("\\\\")) {
                sb.append("\\");
            }
            
            // Escaped @ sign
            else if (token.equals("\\@")) {
                sb.append("@");
            }
            
            // Commands
            else if (token.length() > 1 && token.substring(0, 2).equals("@{")) {
                if (replaceSimpleCommands(token, sb)) continue;
                if (replaceLinkCommand(token, sb)) continue;
                if (replaceColorCommand(token, sb)) continue;
                System.out.println("Unknown command: " + token);
            }
            
            // Normal content
            else {
                sb.append(escapeHtmlChars(token));
            }
        }
        
        closeFontTags(sb);
        sb.append("</pre></body></html>");
        return sb.toString();
    }
    
    // </editor-fold>
    
    
    // <editor-fold desc="Internal methods">

    private String escapeHtmlChars(String input) {
        input = input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return input;
    }
    
    private boolean replaceColorCommand(String token, StringBuilder currentStringBuilder) {
        var colorPattern = Pattern.compile("^@\\{(bg|fg)\\s+([^\\}]+)\\}$", Pattern.CASE_INSENSITIVE);
        var matcher = colorPattern.matcher(token);
        if (!matcher.matches()) return false;

        var htmlColor = replaceColorNameByColorCode(matcher.group(2));
        
        switch (matcher.group(1).toLowerCase()) {
            case "fg" -> {
                currentStringBuilder.append("<font color=\"")
                    .append(htmlColor)
                    .append("\">");
            }
            
            case "bg" -> {
                currentStringBuilder.append("<font bgcolor=\"")
                    .append(htmlColor)
                    .append("\">");
            }
        }
        openFontTags++;
        return true;
    }
    
    private void closeFontTags(StringBuilder currentStringBuilder) {
        while (openFontTags > 0) {
            currentStringBuilder.append("</font>");
            openFontTags--;
        }
    }
    
    private boolean replaceLinkCommand(String token, StringBuilder currentStringBuilder) {
        var linkPattern = Pattern.compile("^@\\{(\\\"([^\\\"]*)\\\"|\\S+?)\\s+(alink|link|close|rx|rxs|system|quit)(\\s+(\\\"([^\\\"]*)\\\"|\\S+?)(\\s+\\d+)?)?\\s*\\}$", Pattern.CASE_INSENSITIVE);
        var matcher = linkPattern.matcher(token);
        if (!matcher.matches()) return false;
        
        String htmlClass = "link";
        String target;
        String label;
        String protocol;
        
        switch (matcher.group(3).toLowerCase()) {
            
            // Remark: optional target line number is ignored atm
            case "link", "alink", "rx", "rxs", "system" -> {

                protocol = matcher.group(3).toLowerCase();
                if (matcher.group(2) != null) {
                    label = matcher.group(2);
                } else {
                    label = matcher.group(1);
                }
                if (matcher.group(6) != null) {
                    target = matcher.group(6);
                } else {
                    target = matcher.group(5);
                }
            }
            
            default -> {
                protocol = "quit";
                target = "";
                label = matcher.group(2);
            }
        }
        
        currentStringBuilder.append("<a href=\"")
            .append(protocol)
            .append("://")
            .append(URLEncoder.encode(target, Charset.defaultCharset()))
            .append("\" class=\"")
            .append(htmlClass)
            .append("\">")
            .append(escapeHtmlChars(label))
            .append("</a>");
        
        return true;
    }
    
    private String replaceColorNameByColorCode(String code) {
        switch (code.toLowerCase()) {
            case "text" -> {
                return "#000000";
            }
            
            case "shine" -> {
                //return "#ffffff";
                return "#aaaaaa";
            }
            
            case "shadow" -> {
                return "#7c7b7b";
            }
            
            case "fill" -> {
                return "#486fb5";
            }
            
            case "filltext" -> {
                return "#ffffff";
            }
            
            case "background", "back" -> {
                return "#ffffff";
            }
            
            case "highlight" -> {
                return "#486fb5";
            }
        }
        return "#000000";
    }
    
    private boolean replaceSimpleCommands(String token, StringBuilder currentStringBuilder) {
        var simpleCommandPattern = Pattern.compile("^@\\{(i|ui|b|ub|u|uu|plain|amigaguide|body|code)\\}$", Pattern.CASE_INSENSITIVE);
        var matcher = simpleCommandPattern.matcher(token);

        if (!matcher.matches()) return false;
        switch (matcher.group(1).toLowerCase()) {
            // Italic text
            case "i" -> {
                if (!isIOpen) {
                    currentStringBuilder.append("<i>");
                    isIOpen = true;
                }
            }
            
            // End italic text
            case "ui" -> {
                if (isIOpen) {
                    currentStringBuilder.append("</i>");
                    isIOpen = false;
                }
            }
            
            // Begin bold text
            case "b" -> {
                if (!isBOpen) {
                    currentStringBuilder.append("<b>");
                    isBOpen = true;
                }
            }
            
            // End bold text
            case "ub" -> {
                if (isBOpen) {
                    currentStringBuilder.append("</b>");
                    isBOpen = false;
                }
            }
            
            // Begin underlined text
            case "u" -> {
                if (!isUOpen) {
                    currentStringBuilder.append("<u>");
                    isUOpen = true;
                }
            }
            
            // End underlined text
            case "uu" -> {
                if (isUOpen) {
                    currentStringBuilder.append("</u>");
                    isUOpen = false;
                }
            }
            
            case "amigaguide" -> {
                currentStringBuilder.append("<font style=\"font-variant: small-caps; font-weight: bold\">AmigaGuide&reg;</font>");
            }
            
            // Close all formattings
            case "plain", "body" -> {
                // TODO we should remember the order of the opening tags!
                if (isBOpen) {
                    currentStringBuilder.append("</b>");
                    isBOpen = false;
                }
                if (isIOpen) {
                    currentStringBuilder.append("</i>");
                    isIOpen = false;
                }
                if (isUOpen) {
                    currentStringBuilder.append("</u>");
                    isUOpen = false;
                }
                if (isCodeOpen) {
                    currentStringBuilder.append("</span>");
                    isCodeOpen = false;
                }
                closeFontTags(currentStringBuilder);
            }
            
            // Code blocks
            case "code" -> {
                if (!isCodeOpen) {
                    closeFontTags(currentStringBuilder);
                    currentStringBuilder.append("<span class=\"code\">");
                    isCodeOpen = true;
                }
            }
        }
        return true;
    }
    
    // </editor-fold>
    
}
