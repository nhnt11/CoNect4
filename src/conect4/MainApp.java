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