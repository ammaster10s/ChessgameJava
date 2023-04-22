
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseMotion implements MouseListener, MouseMotionListener ,ActionListener{
    public static int positionclickx;
    public static int positionclicky;
    JLabel labletest;
   
    int clicklocalx;
    int clicklocaly;
    int releasedlocalx;
    int releasedlocaly;
    static boolean turntomove = true;
    JPanel temp;

    JButton resign1;
    JButton resign2;
    JButton draw1;
    JButton draw2;

    boolean draw1accept;
    boolean draw2accept;

    public boolean playerwin ;

    MouseMotion(JPanel test ,JLabel labletest , JButton resign1 , JButton resign2,JButton draw1 ,JButton draw2) {
        this.labletest = labletest;
        this.temp = test;
        this.resign1= resign1;
        this.resign2= resign2;
        this.draw1= draw1;
        this.draw2= draw2;

        
        resign1.addActionListener(this);
        resign2.addActionListener(this);
        draw1.addActionListener(this);
        draw2.addActionListener(this);
    }
    // public static String[][] board = new String[8][8];
    // public static String[][] posiblemove = new String[8][8];

    

    public void repaint(JPanel temp) {
        this.temp = temp;
        temp.getParent().repaint();
        temp.revalidate();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Piece check = new Piece();

        this.clicklocalx = ((e.getX() - 9) / 100);
        this.clicklocaly = ((e.getY() - 32) / 100);
        // labletest.setText(clicklocaly+" "+clicklocalx);
        check.checklayoutboard(clicklocaly, clicklocalx);

        if (Piece.Pieces != null) {

            check.posiblemovecal(clicklocaly, clicklocalx);

            labletest.setText(Piece.Pieces);
            // labletest.setText(Chessboard.board[clicklocaly][clicklocalx]);

        } else {
            // labletest.setText(Chessboard.board[clicklocaly][clicklocalx]);
        }
        repaint(temp);

    }

    @Override
    public void mouseReleased(MouseEvent e) {

        int releasedlocalx = ((e.getX() - 9) / 100);
        int releasedlocaly = ((e.getY() - 32) / 100);
        // labletest.setText(releasedlocaly+" "+ releasedlocalx);
        if (Piece.posiblemove[releasedlocaly][releasedlocalx] == "Posible" && Piece.Pieces != null) {

            if (Piece.Pieces == "Bpawn" && releasedlocaly == 7) {
                Piece.promotion(releasedlocaly, releasedlocalx, turntomove,clicklocaly,clicklocalx);
            } else if (Piece.Pieces == "Wpawn" && releasedlocaly == 0) {
                Piece.promotion(releasedlocaly, releasedlocalx, turntomove,clicklocaly,clicklocalx);
            } else {
                Chessboard.board[releasedlocaly][releasedlocalx] = Piece.Pieces;
                
                Chessboard.board[clicklocaly][clicklocalx] = null;
            }
            Piece.posiblemove = new String[8][8];

            repaint(temp);
            if (turntomove == true) {
                turntomove = false;
                Chessclock.checkstatus(turntomove,false);
            } else {
                turntomove = true;
                Chessclock.checkstatus(turntomove,false);
            }
        }

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        ;
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        labletest.setText((e.getX() - 9) + " " + (e.getY() - 32));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        ;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        ;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        ;
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() ==resign1 ){
            playerwin = false;
            Chessboard.checkwincodition(playerwin,"resign");
        }
        else if (e.getSource() ==resign2 ){
            playerwin = true;
            Chessboard.checkwincodition(playerwin,"resign");
        }
        else if (e.getSource() == draw1){
            
            labletest.setText("Player 1 Offers a Draw");
            draw1accept=true;
            playerwin = true;
            if (draw1accept == true && draw2accept ==true){
                Chessboard.checkwincodition(playerwin, "draw");
                draw1accept=false;
                draw2accept=false;
            }
            
    }
        else if (e.getSource() == draw2){

            labletest.setText("Player 2 Offers a Draw");
            draw2accept=true;
            playerwin = true;
            if (draw1accept == true && draw2accept ==true){
                Chessboard.checkwincodition(playerwin, "draw");
                draw2accept=false;
                draw1accept=false;
            }
        }
    }

}