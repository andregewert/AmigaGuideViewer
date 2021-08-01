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
import java.util.List;

/**
 * Represents a line command
 * @author André Gewert <agewert@ubergeek.de>
 */
public final class Command {

    private String name;
    
    private final List<String> arguments;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        if (name != null) {
            this.name = name.toLowerCase();
        } else {
            this.name = null;
        }
    }
    
    public String getArgument(int index) {
        if (arguments == null || arguments.size() <= index) return null;
        return arguments.get(index);
    }

    public void addArgument(String argument) {
        arguments.add(argument);
    }
    
    public Command() {
        this(null);
    }
    
    public Command(String name) {
        setName(name);
        arguments = new ArrayList<>();
    }
    
}
