package conect4;

import java.awt.*;
import javax.swing.*;

public class Coin extends JButton implements Cloneable {

    Color who;
    int col;
    int row;
    boolean foc = false;
    Color daWho;
    Color opp;
    private RackView view;

    public Coin(Color who, Color opp, int row, int col, RackView view) {
        this.who = who;
        this.opp = opp;
        this.view = view;
        daWho = who;
        this.col = col;
        this.row = row;
        setRolloverEnabled(false);
        setDoubleBuffered(true);
        setFocusPainted(false);
        repaint();
    }

    @Override
    protected Coin clone() {
        return new Coin(daWho, opp, row, col, view);
    }

    void setOpp(Color c) {
        opp = c;
    }

    Color getWho() {
        return daWho;
    }

    void setWho(Color who) {
        daWho = who;
        this.who = who;
    }

    @Override
    public void paint(Graphics g2) {
        Graphics2D g = (Graphics2D) g2;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 6; i < 17; i++) {
            Color c = daWho;
            for (int j = (17 - i); j > 6; j--) {
                c = c.darker();
            }
            g.setColor(c);
            g.fillOval(i, i, getWidth() - 2 * i, getHeight() - 2 * i);
        }
//        double alpha = 100;
//        for (int i = 6; i > 3; i--) {
//            Color c = new Color(255, 255, 255, (int)alpha);
//            g.setColor(c);
//            g.drawOval(i, i, getWidth() - 2 * i, getHeight() - 2 * i);
//            alpha /= 1.5;
//        }
        if (foc) {
            g.setColor(Color.WHITE);
            g.drawRect(4, 4, getWidth() - 8, getHeight() - 8);
        }
//        g.setColor(Color.BLACK);
//        g.drawRect(0, 0, getWidth(), getHeight());
//        g.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
    }
}
