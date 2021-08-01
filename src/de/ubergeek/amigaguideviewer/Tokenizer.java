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

/**
 * A simple tokenizer for the contents of a single node.
 * This is a helping class to split the node content into its components.
 * Line commands should be removed before utilizing the tokenizer.
 * @author André Gewert <agewert@ubergeek.de>
 */
public class Tokenizer {
    
    // <editor-fold desc="Properties">
    
    private String content;
    
    private int cursor;
    
    private String token;
    
    // </editor-fold>
    
    
    // <editor-fold desc="Accessors">
    
    /**
     * Returns the last read token.
     * This method should be called only if the boolean return value of
     * parseNextToken() was checked.
     * @return The last read token
     */
    public String getToken() {
        return token;
    }
    
    // </editor-fold>
    

    // <editor-fold desc="Constructors">
    
    /**
     * Initializes the tokenizer without a content.
     * The content to be tokenized has to be set with setContent() later
     */
    public Tokenizer() {
        setContent("");
    }

    /**
     * Initializes the tokenizer with the given content
     * @param content Content to be tokenized
     */
    public Tokenizer(String content) {
        setContent(content);
    }
    
    // </editor-fold>
    
    
    // <editor-fold desc="Public methods">
    
    /**
     * Sets the content to be tokenized and resets the (internal) reading cursor
     * @param content The content to be tokenized
     */
    public final void setContent(String content) {
        this.content = content;
        cursor = 0;
    }
    
    /**
     * Parses the next token.
     * Return true if any token has been read successfully, otherwise false.
     * Normally this method should return false at the end of the content. It's
     * intended use is the determination of a while (reading) loop.
     * @return true if another token could be read or false if there are no more tokens
     */
    public boolean parseNextToken() {
        String tmpToken;
        var currentChar = nextChar();
        if (currentChar == Character.MIN_VALUE) return false;
        
        switch (currentChar) {
            
            // Escape character
            case '\\' -> {
                tmpToken = String.valueOf(currentChar);
                var nextChar = nextChar();
                if (nextChar == '@' || nextChar == '\\') {
                    token = tmpToken.concat(String.valueOf(nextChar));
                    return true;
                } else {
                    token = tmpToken;
                    setCursorBack();
                    return true;
                }
            }

            // Beginning of a command
            case '@' -> {
                var nextChar = nextChar();
                if (nextChar == '{') {
                    setCursorBack(2);
                    token = readUntilCharClass("\\}", true);
                    return true;
                } else {
                    token = String.valueOf(currentChar);
                    setCursorBack();
                    return true;
                }
            }

            // Normal content
            default -> {
                setCursorBack();
                token = readUntilCharClass("[\\\\@]", false);
                return true;
            }
        }
    }
    
    // </editor-fold>
    
    
    // <editor-fold desc="Internal methods">
    
    private char nextChar() {
        if (cursor < content.length() -1) {            
            var c = content.charAt(cursor);
            cursor++;
            return c;
        }
        return Character.MIN_VALUE;
    }
    
    private void setCursorBack(int distance) {
        cursor -= distance;
    }
    
    private void setCursorBack() {
        setCursorBack(1);
    }
    
    private String readUntilCharClass(String charClass, boolean includeInCurrentToken) {
        String readContent = "";
        boolean continueToRead = true;

        while (continueToRead) {
            var nextChar = nextChar();
            if (nextChar == Character.MIN_VALUE) return readContent;
            
            continueToRead = !String.valueOf(nextChar).matches(charClass);
            
            if (continueToRead || includeInCurrentToken) {
                readContent = readContent.concat(String.valueOf(nextChar));
            } else if (!includeInCurrentToken) {
                setCursorBack();
            }
        }
        return readContent;
    }
    
    // </editor-fold>
    
}
