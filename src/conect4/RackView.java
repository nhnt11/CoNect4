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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class RackView extends JPanel implements Runnable {

    CoinCol[] cols = new CoinCol[7];
    Coin[][] coins = new Coin[7][6];
    int count = 0;
    MainApp app;
    int full = 0;
    Color plyr2 = Color.YELLOW.brighter();
    Color plyr1 = Color.RED;
    Color orig_Who = plyr1;
    Color whoseTurn = plyr2;
    final Color blank = new Color(240, 240, 240);
    Connect4Player player1;
    Connect4Player player2;
    int turn_count = 0;
    Coin prevCoin = new Coin(blank, blank, -1, -1, this);
    String name = String.valueOf(Math.random() * Integer.MAX_VALUE);
    ServerConnection server;
    boolean LAN = false;
    JFrame jd = new JFrame();
    Vector listitems = new Vector();
    JList list = new JList(listitems);
    Hashtable names = new Hashtable();
    boolean success = false;
    JButton acceptBtn = new JButton("OK");
    Connect4Player thisPlyr;
    boolean justPlayed = false;
    boolean idtaken = false;
    boolean iStarted = false;
    boolean playable = true;
    String theirName = "";
    JLabel statusLabel = new JLabel("", JLabel.CENTER);
    JLabel scoreLabel = new JLabel("", JLabel.CENTER);
    int counter = 0;
    long init_time = 0;

    public RackView(final MainApp app, boolean LAN2) {
        init_time = System.currentTimeMillis();
        this.app = app;
        LAN = LAN2;
        app.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (LAN) {
                    app.setVisible(false);
                    reset();
                    success = false;
                    iStarted = false;
                    jd.setVisible(true);
                    server.gameEnded();
                } else {
                    System.exit(0);
                }
            }
        });
        jd.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                server.delete();
                System.exit(0);
            }
        });
        setLayout(new GridLayout(1, 6));
        setOpaque(true);
        setBackground(new Color(50, 55, 155));
        setSize(new Dimension(560, 480));
        app.add(statusLabel, BorderLayout.SOUTH);
        app.add(scoreLabel, BorderLayout.NORTH);
        scoreLabel.setOpaque(true);
        scoreLabel.setBackground(Color.BLACK);
        scoreLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(Color.BLACK);
        statusLabel.setForeground(Color.WHITE);
        while (true) {
            name = JOptionPane.showInputDialog("Enter your name: ");
            if (name == null) {
                System.exit(0);
            } else if (name.equals("")) {
                continue;
            } else {
                break;
            }
        }
        if (LAN) {
            try {
                String site;
                while (true) {
                    site = JOptionPane.showInputDialog("Enter IP: ");
                    if (site == null) {
                        System.exit(0);
                    } else if (site.equals("")) {
                        continue;
                    } else {
                        break;
                    }
                }
                server = new ServerConnection(this, site);
                server.setName(name);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            app.setVisible(true);
            player2 = new Connect4AI(plyr2, this, plyr1, "Computer");
            thisPlyr = player1 = new Connect4Human(plyr1, this, plyr2, name);
            scoreLabel.setText("<html><b>Score: </b><span style='color:red'>" + player1.getName() + "</span>: 0 <span style = 'color:yellow'>" + player2.getName() + "</span>: 0</html>");
            reset();
            changeTurn();
        }
        new Thread(this).start();
    }

    void add(String name) {
        if (name.equals(getClientName()) || ((DefaultListModel) list.getModel()).contains(name)) {
            return;
        }
        ((DefaultListModel) list.getModel()).addElement(name);
    }

    boolean startGame(String name, String to) {
        if (!to.equals(getClientName()) && !name.equals(getClientName())) {
            return false;
        }
        if (iStarted) {
            player2 = new Connect4LAN(plyr2, this, theirName = (String) list.getSelectedValue());
            thisPlyr = player1 = new Connect4Human(plyr1, this, plyr2, "You");
        } else {
            player1 = new Connect4LAN(plyr1, this, theirName = name);
            thisPlyr = player2 = new Connect4Human(plyr2, this, plyr1, "You");
        }
        whoseTurn = plyr2;
        scoreLabel.setText("<html><b>Score:</b> <span style='color:red'>" + player1.getName() + "</span>: 0 <span style = 'color:yellow'>" + player2.getName() + "</span>: 0</html>");
        jd.dispose();
        reset();
        changeTurn();
        app.setVisible(true);
        app.pack();
        return true;
    }

    void idtaken() {
        name = JOptionPane.showInputDialog("That name is already taken! Choose another!");
        server.setName(name);
    }

    void chat(String from, String what) {
        JOptionPane.showMessageDialog(this, from + " says: " + what);
    }

    void delete(String name) {
        ((DefaultListModel) list.getModel()).removeElement(name);
    }

    void otherQuit(String name) {
        if (success && (player1.getName().equals(name) || player2.getName().equals(name))) {
            JOptionPane.showMessageDialog(this, name + "  has quit.");
            app.setVisible(false);
            reset();
            success = false;
            iStarted = false;
            jd.setVisible(true);
            server.gameEnded();
        }
    }

    String getClientName() {
        return name;
    }

    synchronized void success() {
        success = true;
        acceptBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if ((String) list.getSelectedValue() == null || ((String) list.getSelectedValue()).equals("")) {
                    return;
                }
                iStarted = true;
                server.startGame((String) list.getSelectedValue());
            }
        });
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setModel(new DefaultListModel());
        jd.setLayout(new BorderLayout());
        jd.add(new JLabel("People online:", JLabel.CENTER), BorderLayout.NORTH);
        jd.add(list, BorderLayout.CENTER);
        jd.add(acceptBtn, BorderLayout.SOUTH);
        jd.setSize(240, 320);
        jd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jd.setVisible(true);
    }

    void cant(String name, String from) {
        if (!from.equals(getClientName())) {
            return;
        }
        iStarted = false;
        JOptionPane.showMessageDialog(this, name + " is already playing!");
    }

    synchronized void reset() {
        removeAll();
        if (orig_Who == plyr1) {
            whoseTurn = orig_Who = plyr2;
        } else {
            whoseTurn = orig_Who = plyr1;
        }
        full = 0;
        for (int i = 0; i < cols.length; i++) {
            add(cols[i] = new CoinCol(this, i));
            coins[i] = cols[i].coins;
        }
        System.gc();
        validate();
    }

    synchronized void fullPlus() {
        full++;
    }

    synchronized Connect4Player getCurrentPlayer() {
        if (whoseTurn == player1.getColor()) {
            return player1;
        } else {
            return player2;
        }
    }

    void chat(String s) {
        JOptionPane.showMessageDialog(this, getCurrentPlayer().getName() + ": " + s);
    }

    Connect4Player getOtherPlayer() {
        if (getCurrentPlayer() == player1) {
            return player2;
        } else {
            return player1;
        }
    }

    boolean isPlayable() {
        return playable;
    }

    synchronized Coin addCoin(int col, Color who) {
        Coin coin = null;
        repaint();
        try {
            int row = cols[col].addCoin(who);
            if (getCurrentPlayer().isHuman() && LAN) {
                server.play(col);
            }
            coin = coins[col][row] = cols[col].coins[row];
            if (prevCoin.row != -1) {
                coins[prevCoin.col][prevCoin.row].foc = false;
            }
            coins[col][row].foc = true;
            prevCoin = coin;
            if (who == plyr2 || who == plyr1) {
                if (checkForWin(coin, coins)) {
                    if (LAN) {
                        server.playable(false);
                    }
                    //repaint();
                    JOptionPane.showMessageDialog(this, getCurrentPlayer().getName() + " won!");
                    if (LAN) {
                        server.playable(true);
                    }
                    counter++;
//                    app.setTitle(String.valueOf(counter));
//                    long time = System.currentTimeMillis();
//                    if (time - init_time > 60000) {
//                        JOptionPane.showMessageDialog(this, String.valueOf(counter));
//                        System.exit(0);
//                    }
                    getCurrentPlayer().win();
                    scoreLabel.setText("<html><b>Score:</b> <span style='color:red'>" + player1.getName() + "</span>: " + player1.getWins() + " <span style = 'color:yellow'>" + player2.getName() + "</span>: " + player2.getWins() + "</html>");
                    reset();
                } else if (full == 7) {
                    counter++;
                    if (LAN) {
                        server.playable(false);
                    }
                    JOptionPane.showMessageDialog(this, "Nobody wins!");
                    if (LAN) {
                        server.playable(true);
                    }
                    reset();
                }
            }
            changeTurn();
        } catch (CoinException e) {
            Connect4Player p;
            if (whoseTurn == plyr1) {
                p = player1;
            } else {
                p = player2;
            }
            if (!p.isHuman() && !p.isLAN()) {
                p.play();
            } else {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Illegal Move!", JOptionPane.ERROR_MESSAGE);
            }
        }
        return coin;
    }

    Color getWho() {
        return whoseTurn;
    }

    boolean isHumanTurn() {
        if (getCurrentPlayer().isHuman()) {
            return true;
        } else {
            return false;
        }
    }

    boolean isLANTurn() {
        if (getCurrentPlayer().isLAN()) {
            return true;
        } else {
            return false;
        }
    }

    String theirName() {
        return theirName;
    }

    void lanPlay(int col) {
        ((Connect4LAN) getCurrentPlayer()).play(col);
    }

    synchronized void changeTurn() {
        whoseTurn = whoseTurn == plyr2 ? plyr1 : plyr2;
        turn_count++;
        String who = "";
        who = getCurrentPlayer().getName() + "'s";
        statusLabel.setText("It's " + who + " turn");
        if (isHumanTurn() || isLANTurn()) {
            return;
        }
        if (getCurrentPlayer() == player1) {
            player1Play();
        } else {
            player2Play();
        }
    }

    synchronized void player1Play() {
        new Thread(new Runnable() {

            public void run() {
                while (player2.isPlaying()) {
                    continue;
                }
                player1.play();
            }
        }).start();
    }

    synchronized void player2Play() {
        new Thread(new Runnable() {

            public void run() {
                while (player1.isPlaying()) {
                    continue;
                }
                player2.play();
            }
        }).start();
    }

    static synchronized boolean checkForWin(Coin coin, Coin[][] coins) {
        int row = coin.row;
        int col = coin.col;
        Color who = coin.who;
        int c = 0;
        //System.out.println(row + ", " + col);
        for (int i = 1; i < 5 && row + i < 6; i++) {
            if (coins[col][row + i].daWho == who) {
                c++;

            //System.out.print("+col ");
            } else {
                break;
            }
        }
        for (int i = 1; i < 5 && row - i > -1; i++) {
            if (coins[col][row - i].daWho == who) {
                c++;
            //System.out.print("-col ");
            } else {
                break;
            }
        }
        //System.out.println();
        if (c > 2) {
            return true;
        }
        c = 0;
        for (int i = 1; i < 5 && col + i < 7; i++) {
            if (coins[col + i][row].daWho == who) {
                c++;
            //System.out.print("+row ");
            } else {
                break;
            }
        }
        for (int i = 1; i < 5 && col - i > -1; i++) {
            if (coins[col - i][row].daWho == who) {
                c++;
            //System.out.print("-row ");
            } else {
                break;
            }
        }
        //System.out.println();
        if (c > 2) {
            return true;
        }
        c = 0;
        for (int i = 1; i < 5 && col + i < 7 && row + i < 6; i++) {
            if (coins[col + i][row + i].daWho == who) {
                c++;
            //System.out.print("+diag ");
            } else {
                break;
            }
        }
        for (int i = 1; i < 5 && col - i > -1 && row - i > -1; i++) {
            if (coins[col - i][row - i].daWho == who) {
                c++;
            //System.out.print("-diag ");
            } else {
                break;
            }
        }
        //System.out.println();
        if (c > 2) {
            return true;
        }
        c = 0;
        for (int i = 1; i < 5 && col + i < 7 && row - i > -1; i++) {
            if (coins[col + i][row - i].daWho == who) {
                c++;
            //System.out.print("++diag ");
            } else {
                break;
            }
        }
        for (int i = 1; i < 5 && col - i > -1 && row + i < 6; i++) {
            if (coins[col - i][row + i].daWho == who) {
                c++;
            //System.out.print("--diag ");
            } else {
                break;
            }
        }
        //System.out.println();
        if (c > 2) {
            return true;
        }
        return false;
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            if (app.isVisible() && getCurrentPlayer().isHuman()) {
                repaint();
            }
        }
    }
}
