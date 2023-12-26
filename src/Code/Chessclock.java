package Code;

import javax.swing.JPanel;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

// @SuppressWarnings("serial")
public class Chessclock extends JPanel implements ActionListener {
    static boolean Gamestart = false;

    private JButton bullet3 = new JButton("3|2");
    private JButton blitz3 = new JButton("3");
    private JButton blitz5 = new JButton("5|5");
    private JButton Rapid10 = new JButton("10");

    static int remainingSeconds1;
    static int remainingSeconds2;

    private static Timer timer1;
    private static Timer timer2;

    private JLabel player1Label;
    private JLabel player2Label;

    public int minutes1 = 0;
    public int seconds1 = 0;

    public int minutes2 = 0;
    public int seconds2 = 0;
    public static int constantsec;

    public int totalSeconds1;
    public int totalSeconds2;

    public Chessclock(JLabel player1, JLabel player2) {
        this.setLayout(new GridLayout(2, 2));
        this.add(bullet3);
        this.add(blitz3);
        this.add(blitz5);
        this.add(Rapid10);

        bullet3.addActionListener(this);
        blitz3.addActionListener(this);
        blitz5.addActionListener(this);
        Rapid10.addActionListener(this);

        // Initialize the player labels
        // player1Label = new JLabel("", SwingConstants.CENTER);
        this.player1Label = player1;
        this.player2Label = player2;
        player1Label.setFont(new Font("Arial", Font.PLAIN, 48));
        // player1Label = new JLabel();

        // player2Label = new JLabel("", SwingConstants.CENTER);
        player2Label.setFont(new Font("Arial", Font.PLAIN, 48));
        // player2Label = new JLabel();

    }

    public void actionPerformed(ActionEvent e) {
        JButton btn = (JButton) e.getSource();

        switch (btn.getText()) {
            case "3|2":
                minutes1 = 3;
                seconds1 = 2;
                minutes2 = 3;
                seconds2 = 2;
                this.constantsec = 2;
                break;
            case "3":
                minutes1 = 3;
                seconds1 = 0;
                minutes2 = 3;
                seconds2 = 0;
                this.constantsec = 0;
                break;
            case "5|5":
                minutes1 = 5;
                seconds1 = 5;
                minutes2 = 5;
                seconds2 = 5;
                this.constantsec = 5;
                break;
            case "10":
                minutes1 = 10;
                seconds1 = 0;
                minutes2 = 10;
                seconds2 = 0;
                this.constantsec = 0;
                break;

        }
        player1Label.setText(String.format("%02d:%02d", minutes1, seconds1));
        player2Label.setText(String.format("%02d:%02d", minutes2, seconds2));
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.dispose();

        Gamestart = true;
        Chessboard game = new Chessboard();
        checkclock();

    }

    public void checkclock() {

        int totalSeconds1 = minutes1 * 60 + seconds1;
        int totalSeconds2 = minutes2 * 60 + seconds2;
        timer1 = new Timer(1000, new ActionListener() {
            int remainingSeconds1 = totalSeconds1;

            public void actionPerformed(ActionEvent evt) {
                remainingSeconds1--;
                int remainingMinutes = remainingSeconds1 / 60;
                int remainingSecondsDisplay = remainingSeconds1 % 60;
                player1Label.setText(String.format("%02d:%02d", remainingMinutes, remainingSecondsDisplay));
                if (remainingSeconds1 == 0) {
                    JOptionPane.showMessageDialog(null, "Time's up!");
                    timer1.stop();
                    // player1Label.setText("");
                }
            }
        });
        timer1.stop();

        timer2 = new Timer(1000, new ActionListener() {
            int remainingSeconds2 = totalSeconds2;

            public void actionPerformed(ActionEvent evt) {
                remainingSeconds2--;
                int remainingMinutes = remainingSeconds2 / 60;
                int remainingSecondsDisplay = remainingSeconds2 % 60;
                player2Label.setText(String.format("%02d:%02d", remainingMinutes, remainingSecondsDisplay));
                if (remainingSeconds2 == 0) {
                    JOptionPane.showMessageDialog(null, "Time's up!");
                    timer2.stop();
                    // player2Label.setText("");
                }
            }
        });
        timer2.stop();
        // Set the initial turn to player 1

    }

    public static void addTimeToTimer1(Timer timer, int secondsToAdd) {
        ActionListener[] listeners = timer.getActionListeners();
        for (ActionListener listener : listeners) {
            if (listener instanceof Chessclock) {
                Chessclock timerListener = (Chessclock) listener;
                Chessclock.remainingSeconds1 += secondsToAdd;
                break;
            }
        }
    }

    public static void addTimeToTimer2(Timer timer, int secondsToAdd) {
        ActionListener[] listeners = timer.getActionListeners();
        for (ActionListener listener : listeners) {
            if (listener instanceof Chessclock) {
//                Chessclock timerListener = (Chessclock) listener;
                Chessclock.remainingSeconds2 += secondsToAdd;
                break;
            }
        }
    }

    public static void checkstatus(boolean check, boolean check2) {

        if (check == true && check2 == true) {
            timer1.stop();
            timer2.stop();
        } else if (check == false && check2 == false) {
            // currentLabel = player2Label;
            timer2.start();
            timer1.stop();
            addTimeToTimer1(timer1, constantsec);
        } else if (check == true && check2 == false) {
            // currentLabel = player1Label;
            timer1.start();
            timer2.stop();
            remainingSeconds2 += constantsec;
            addTimeToTimer2(timer2, constantsec);
        }

    }

}
