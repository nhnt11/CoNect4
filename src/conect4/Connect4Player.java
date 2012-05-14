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
import java.util.*;

public abstract class Connect4Player {

    protected boolean isPlaying = false;

    abstract int play();

    abstract Color getColor();

    abstract boolean isPlaying();

    abstract boolean isHuman();

    abstract boolean isLAN();

    abstract int getWins();

    abstract void win();

    abstract String getName();
}

class Connect4AI extends Connect4Player {

    Color color;
    RackView view;
    Coin[][] coins;
    Vector mycoins = new Vector();
    int turnCount = 0;
    Color opponent;
    Coin co;
    Coin com;
    Coin oo;
    Coin oom;
    String name;
    int wins = 0;

    public Connect4AI(Color c, RackView view, Color opponent, String name) {
        color = c;
        co = new Coin(c, opponent, 0, 0, view);
        com = new Coin(c, opponent, 0, 0, view);
        oo = new Coin(opponent, c, 0, 0, view);
        oom = new Coin(opponent, c, 0, 0, view);
        this.opponent = opponent;
        this.view = view;
        coins = null;
        coins = view.coins;
        this.name = name;
    }

    void win() {
        wins++;
    }

    int getWins() {
        return wins;
    }

    boolean isHuman() {
        return false;
    }

    String getName() {
        return name;
    }

    boolean isLAN() {
        return false;
    }

    synchronized int play() {
        long time = System.currentTimeMillis();
        isPlaying = true;
        coins = null;
        coins = view.coins.clone();
        int bestMove = getBestMove(true, coins, new Coin(color, opponent, -1, -1, view));
        view.addCoin(bestMove, color);
        isPlaying = false;
//        long time2 = System.currentTimeMillis();
//        try {
//            if (time2 - time < 100) {
//                Thread.sleep(100 - (time2 - time));
//            }
//        } catch (InterruptedException e) {
//        }
        return bestMove;
    }

    synchronized boolean isPlaying() {
        return isPlaying;
    }

    int getBestMove(boolean b, Coin[][] coins, Coin c) {
        if (view.full == 6) {
            int i;
            for (i = 0; view.cols[i].isFull(); i++) {
                continue;
            }
            return i;
        }
        int bestCol = -1;
        int rankings[] = {0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < 7; i++) {
            if (view.cols[i].isFull()) {
                rankings[i] = Integer.MIN_VALUE;
                continue;
            }
            if (b) {
                coins = view.coins;
            }
            oo.col = i;
            oom.col = i;
            oo.setWho(c.opp);
            oom.setWho(c.opp);
            oo.row = view.cols[i].getRowCount();
            if (c.col == i) {
                oo.row--;
            }
            oom.row = oo.row - 1;
            co.row = oo.row;
            co.col = oo.col;
            com.row = oom.row;
            com.col = oom.col;
            co.setWho(c.who);
            com.setWho(c.who);
            if (view.turn_count < 5) {
                b = false;
            }
            if (b && checkForWin(co, coins)) {
                return i;
            } else if (checkForWin(oo, coins)) {
                rankings[i] = Integer.MAX_VALUE;
                if (!b) {
                    return i;
                }
                if (b) {
                    //System.out.println(view.getOtherPlayer().getName() + " Win" + i);
                }
                continue;
            } else if (b && (oom.row >= 0 && (checkForWin(oom, coins)))) {
                if (b) {
                    //System.out.println(view.getOtherPlayer().getName() + " WillWin" + i);
                }
                rankings[i] = -2;
                continue;
            } else if (b && (checkForDoubleAttack(oo) || checkForDoubleAttack2(oo))) {
                if (b) {
                    //System.out.println(view.getOtherPlayer().getName() + " double attak " + i);
                }
                rankings[i] = Integer.MAX_VALUE - 1;
                continue;
            } else if (b && (checkForDoubleAttack(co) || checkForDoubleAttack2(co))) {
                if (b) {
                    //System.out.println(getName() + " double attak " + i);
                }
                rankings[i] = Integer.MAX_VALUE - 2;
                continue;
            } else if (b && (oom.row >= 0 && checkForWin(com, coins))) {
                if (b) {
                    //System.out.println(view.getOtherPlayer().getName() + " WillBlock" + i);
                }
                rankings[i] = -1;
                continue;
            }
//            int x[] = {checkForConns(co, 1, coins), checkForConns(co, 0, coins), checkForConns(co, 2, coins), checkForConns(co, 3, coins)};
//            int y[] = {checkForConns(oo, 1, coins), checkForConns(oo, 0, coins), checkForConns(oo, 2, coins), checkForConns(oo, 3, coins)};
//            Arrays.sort(x);
//            Arrays.sort(y);
//            rankings[i] = Math.max(x[3], y[3]);
//            int x[] = {checkForConns(co, 1, coins), checkForConns(co, 0, coins), checkForConns(co, 2, coins), checkForConns(co, 3, coins)};
//            int y[] = {checkForConns(oo, 1, coins), checkForConns(oo, 0, coins), checkForConns(oo, 2, coins), checkForConns(oo, 3, coins)};
//            if (y[3] == x[3]) {
//                int x_ = x[0] + x[1] + x[2] + x[3];
//                int y_ = y[0] + y[1] + y[2] + y[3];
//                rankings[i] = Math.max(x_, y_);
//            } else rankings[i] = Math.max(x[3], y[3]);
            if (b) {
                int x[] = {checkForConns(co, 1, coins), checkForConns(co, 0, coins), checkForConns(co, 2, coins), checkForConns(co, 3, coins)};
                int y[] = {checkForConns(oo, 1, coins), checkForConns(oo, 0, coins), checkForConns(oo, 2, coins), checkForConns(oo, 3, coins)};
                Arrays.sort(x);
                Arrays.sort(y);
                rankings[i] = Math.max(x[3], y[3]);
            }
        }
        int bestRank = Integer.MIN_VALUE;
        for (int i = 0; i < 7; i++) {
            if (rankings[i] > bestRank) {
                bestRank = rankings[i];
            }
        }
        Vector bestRanks = new Vector();
        for (int i = 0; i < 7; i++) {
            if (rankings[i] == bestRank) {
                bestRanks.add(new Integer(i));
            }
        }
        bestCol = ((Integer) bestRanks.get((int) (Math.random() * (bestRanks.size() - 1)))).intValue();
        return bestCol;
    }

    boolean checkForWin(Coin c, Coin coinss[][]) {
        if (c.col < 0 || c.row < 0) {
            return false;
        }
        coinss[c.col][c.row].who = c.who;
        boolean returnVal = RackView.checkForWin(c, coinss);
        coinss[c.col][c.row].who = view.blank;
        return returnVal;
    }

    boolean checkForDoubleAttack(Coin c) {
        if (c.col == -1 || c.row == -1) {
            return false;
        }
        Coin coinss[][] = view.coins;
        coinss[c.col][c.row].who = c.who;
        int count = 0;
        Coin cc = new Coin(c.who, c.opp, c.row, c.col, view);
        for (int i = 0; i < 7; i++) {
            cc.col = i;
            cc.row = view.cols[i].getRowCount();
            if (c.col == i) {
                cc.row--;
            }
            if (checkForWin(cc, coinss)) {
                count++;
            }

        }
        coinss[c.col][c.row].who = view.blank;
        if (count > 1) {
            //System.out.println("double attack1");
        }
        return count > 1;
    }

    boolean checkForDoubleAttack2(Coin c) {
        if (c.col == -1 || c.row == -1) {
            return false;
        }
        Coin coinss[][] = view.coins;
        coinss[c.col][c.row].who = c.who;
        int bestCol = 0;
        for (bestCol = 0; bestCol < 7; bestCol++) {
            int row = view.cols[bestCol].getRowCount();
            if (c.col == bestCol) {
                row--;
            }
            Coin coin = new Coin(c.who, c.opp, row, bestCol, view);
            if (checkForWin(coin, coinss)) {
                break;
            }
        }
        if (bestCol == 7) return false;
//        int bestCol = getBestMove(false, coinss, new Coin(c.opp, c.who, c.row, c.col, view));
        int bestRow = view.cols[bestCol].getRowCount();
        if (bestCol == c.col) {
            bestRow--;
        }
        if (bestRow < 0) {
            return false;
        }
        coinss[bestCol][bestRow].who = c.opp;
        int count = 0;
        Coin cc = new Coin(c.who, c.opp, c.row, c.col, view);
        for (int i = 0; i < 7; i++) {
            cc.col = i;
            cc.row = view.cols[i].getRowCount();
            if (c.col == i) {
                cc.row--;
            }
            if (bestCol == i) {
                cc.row--;
            }
            if (checkForWin(cc, coinss)) {
                count++;
            }

        }
        coinss[c.col][c.row].who = view.blank;
        coinss[bestCol][bestRow].who = view.blank;
        if (count > 0) {
            //System.out.println("double attack2");
        }
        return count > 0;
    }

    int checkForConns(Coin coin, int direction, Coin[][] coins) {
        int row = coin.row;
        int col = coin.col;
        Color who = coin.who;
        if (row < 0 || col < 0) {
            return 0;
        }
        switch (direction) {
            case 0:
                int c = 1;
                for (int i = 1; row +
                        i < 6; i++) {
                    if (coins[col][row + i].who.equals(who)) {
                        c++;
                    } else {
                        break;
                    }

                }
                for (int i = 1; row -
                        i > -1; i++) {
                    if (coins[col][row - i].who.equals(who)) {
                        c++;
                    } else {
                        break;
                    }

                }
                return c;
            case 1:
                c = 1;
                for (int i = 1; col +
                        i < 7 && col + i >= -1; i++) {
                    if (coins[col + i][row].who.equals(who)) {
                        c++;
                    } else {
                        break;
                    }

                }
                for (int i = 1; col -
                        i > -1; i++) {
                    if (coins[col - i][row].who.equals(who)) {
                        c++;
                    } else {
                        break;
                    }

                }
                return c;
            case 2:
                c = 1;
                for (int i = 1; col +
                        i < 7 && row + i < 6; i++) {
                    if (coins[col + i][row + i].who.equals(who)) {
                        c++;
                    } else {
                        break;
                    }

                }
                for (int i = 1; col -
                        i > -1 && row - i > -1; i++) {
                    if (coins[col - i][row - i].who.equals(who)) {
                        c++;
                    } else {
                        break;
                    }

                }
                return c;
            case 3:
                c = 1;
                for (int i = 1; col +
                        i < 7 && row - i > -1; i++) {
                    if (coins[col + i][row - i].who.equals(who)) {
                        c++;
                    } else {
                        break;
                    }

                }
                for (int i = 1; col -
                        i > -1 && row + i < 6; i++) {
                    if (coins[col - i][row + i].who.equals(who)) {
                        c++;
                    } else {
                        break;
                    }

                }
                return c;
            default:
                return 1;
        }

    }

    Color getColor() {
        return color;
    }
}

class Connect4Human extends Connect4Player {

    RackView view;
    Color c;
    Color opponent;
    int col = 0;
    String name;
    int wins = 0;

    public Connect4Human(Color c, RackView view, Color opponent, String name) {
        this.c = c;
        this.view = view;
        this.opponent = opponent;
        this.name = name;
    }

    void win() {
        wins++;
    }

    int getWins() {
        return wins;
    }

    boolean isLAN() {
        return false;
    }

    String getName() {
        return name;
    }

    boolean isPlaying() {
        return isPlaying;
    }

    synchronized int play() {
        isPlaying = true;
        view.addCoin(col, c);
        isPlaying = false;
        return col;
    }

    synchronized void setCol(int col) {
        this.col = col;
    }

    Color getColor() {
        return c;
    }

    boolean isHuman() {
        return true;
    }
}

class Connect4LAN extends Connect4Player {

    Color who;
    RackView view;
    String name;
    int wins = 0;

    public Connect4LAN(Color who, RackView view, String name) {
        this.who = who;
        this.view = view;
        this.name = name;
    }

    String getName() {
        return name;
    }

    boolean isLAN() {
        return true;
    }

    boolean isHuman() {
        return false;
    }

    boolean isPlaying() {
        return isPlaying;
    }

    int play() {
        return 0;
    }

    void play(int col) {
        view.addCoin(col, who);
    }

    Color getColor() {
        return who;
    }

    void win() {
        wins++;
    }

    int getWins() {
        return wins;
    }
}