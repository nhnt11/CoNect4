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