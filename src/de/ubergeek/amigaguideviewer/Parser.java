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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Simple AmigaGuide viewer - parser component
 * @author André Gewert <agewert@ubergeek.de>
 * @todo Parse should check if file starts with @database command
 */
public class Parser {

    // <editor-fold desc="Public methods">
    
    /**
     * Parses an AmigaGuide file.
     * If the file could not be read an exception will be thrown.
     * If the file could not be parsed an empty document (empty node list) will
     * be created.
     * @param file Path to the file
     * @return Parsed document
     * @throws IOException If the file could not be read
     */
    public Document parseAmigaGuideFromFile(Path file) throws IOException {
        String contents = Files.readString(file, StandardCharsets.ISO_8859_1);
        return parseAmigaGuide(contents);
    }

    /**
     * Parses an AmigaGuide file.
     * @param content The file content as a string
     * @return Parsed document
     */
    public Document parseAmigaGuide(String content) {
        var lines = new ArrayList<String>(Arrays.asList(content.split("\\n")));
        var document = new Document();
        
        Node currentNode = null;

        for (String line : lines) {
            
            // Line commands
            if (line.matches("^@(\\w+).*")) {
                var command = parseCommandLine(line);
                 
                switch (command.getName()) {
                    
                    // Start new node
                    case "node" -> {
                        currentNode = document.createAndAddNode(
                            command.getArgument(0), command.getArgument(1)
                        );
                    }

                    // No need to do anything (for now)                    
                    case "endnode", "database" -> {
                    }
                    
                    // Remarks should be ignored
                    case "rem", "remark" -> {
                    }
                    
                    // Global attributes
                    case "master", "width", "author", "(c)" -> {
                        System.out.println(command.getName());
                        document.setAttribute(command.getName(), command.getArgument(1));
                    }
                    
                    // Local OR global attributes
                    case "prev", "next", "toc", "index" -> {
                        if (currentNode != null) {
                            currentNode.setAttribute(command.getName(), command.getArgument(1));
                        } else {
                            document.setAttribute(command.getName(), command.getArgument(1));
                        }
                    }
                    
                    // Unsupported command
                    default -> {
                        System.out.println("Unsupported command: " + command.getName());
                    }
                }   
            }
            
            // Content lines
            else {
                if (currentNode != null) {
                    currentNode.appendContent(line + System.lineSeparator());
                }
            }
        }
        
        return document;
    }
    
    // </editor-fold>
    
    
    // <editor-fold desc="Internal methods">
    
    private Command parseCommandLine(String line) {
        var command = new Command();
        var lineCommandPattern = Pattern.compile("^@([\\w\\(\\)\\$]+)(.+?(\\\"([^\\\"]*)\\\"|\\S+)(.+?(\\\"([^\\\"]*)\\\"|\\w+))?)?$", Pattern.CASE_INSENSITIVE);
        var matcher = lineCommandPattern.matcher(line);
        
        if (matcher.matches()) {
            
            command.setName(matcher.group(1));
            
            if (matcher.group(4) != null) {
                command.addArgument(matcher.group(4));
            } else if (matcher.group(3) != null) {
                command.addArgument(matcher.group(3));
            }
            
            if (matcher.group(7) != null) {
                command.addArgument(matcher.group(7));
            } else if (matcher.group(6) != null) {
                command.addArgument(matcher.group(6));
            }
        }
        
        return command;
    }
    
    // </editor-fold>
    
}
