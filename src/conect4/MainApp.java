/*
 * CoNect 4  Copyright (C) 2012  Nihanth Subramanya
 * 
 * CoNect 4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * CoNect 4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with CoNect 4.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package conect4;

import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {

    RackView view;

    public MainApp(boolean LAN) {
        setLayout(new BorderLayout());
        add(view = new RackView(this, LAN), BorderLayout.CENTER);
        //setResizable(false);
        pack();
    }

    public static void main(String[] args) {
        // TODO code application logic here
        boolean LAN = false;
        boolean SERVER = false;
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("lan") && !LAN) LAN = true;
                if (args[i].equalsIgnoreCase("server") && !SERVER) {
                    SERVER = true;
                    new Thread(new Server()).start();
                }
            }
        }
        if (!SERVER) new MainApp(LAN);
    }
}