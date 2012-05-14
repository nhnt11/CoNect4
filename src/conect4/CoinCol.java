package conect4;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class CoinCol extends JPanel {

    Coin coins[] = new Coin[6];
    int row_count = 5;
    RackView view;
    int col;

    public CoinCol(RackView view2, int col2) {
        this.col = col2;
        view = view2;
        setPreferredSize(new Dimension(80, 480));
        setLayout(new GridLayout(6, 1, 0, 0));
        setOpaque(false);
        for (int i = 0; i < coins.length; i++) {
            final int ii = i;
            add(coins[i] = new Coin(view.blank, view.blank, i, col, view));
            coins[i].addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent me) {
                    if (view.isHumanTurn()) {
                        if (view.isPlayable()) {
                            ((Connect4Human) view.getCurrentPlayer()).setCol(col);
                            view.getCurrentPlayer().play();
                        } else {
                            JOptionPane.showMessageDialog(view, "The other person isn't ready!");
                        }
                    } else {
                        JOptionPane.showMessageDialog(view, "It's not your turn!");
                    }
                    coins[ii].repaint();
                }
            });
        }
    }

    int addCoin(Color who) throws CoinException {
        if (row_count < 0) {
            throw new CoinException("That column is full!");
        }
        coins[row_count].setWho(who);
        if (who.equals(view.plyr1)) coins[row_count].setOpp(view.plyr2);
        else coins[row_count].setOpp(view.plyr1);
        if (row_count == 0) {
            view.fullPlus();
        }
        return row_count--;
    }

    boolean isFull() {
        return (row_count < 0);
    }

    int getRowCount() {
        return row_count;
    }
}