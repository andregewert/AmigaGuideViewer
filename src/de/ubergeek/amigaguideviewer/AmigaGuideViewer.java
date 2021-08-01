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

import com.formdev.flatlaf.FlatLightLaf;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Simple AmigaGuide viewer - command line wrapper.
 * First argument is interpreted as the name of a file to be opened at startup.
 * If no file name is given, the user interface will be opened withour a file;
 * files can be opened via drag and drop or with a file chooser.
 * @author André Gewert <agewert@ubergeek.de>
 */
public class AmigaGuideViewer {

    /**
     * AmigaGuideViewer - command line wrapper
     * 
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    public static void main(String[] args) throws IOException, URISyntaxException {
        
        // Try to set look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            // We can safely ignore errors while setting the look and feel
        }

        var window = new MainWindow();
        window.setVisible(true);
        
        if (args.length >= 1) {
            window.openDocumentFile(Path.of(args[0]));
        }
    }
    
}
