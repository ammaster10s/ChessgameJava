package Code;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;

@SuppressWarnings("serial")
public class ImageCreater extends Chessboard {
    static ImageIcon imgblackpawn;
    static ImageIcon imgwhitepawn;

    static ImageIcon imgblackking;
    static ImageIcon imgwhiteking;

    static ImageIcon imgblackqueen;
    static ImageIcon imgwhitequeen;

    static ImageIcon imgblackbishop;
    static ImageIcon imgwhitebishop;

    static ImageIcon imgblackrook;
    static ImageIcon imgwhiterook;

    static ImageIcon imgblackknight;
    static ImageIcon imgwhiteknight;

    public static void resizeImagesandsetIcon(String piece) {
       
        if (piece.charAt(0) == 'k') {
            try {
                // Load original PNG images
                Image originalBlack = ImageIO.read(new File("/Users/ammaster10/Downloads/chessgame_6522780376/src/Materials/Bking.png"));
                Image originalWhite = ImageIO.read(new File("/Users/ammaster10/Downloads/chessgame_6522780376/src/Materials/Wking.png"));

                // Resize images to 100x100 pixels
                Image resizedBlack = originalBlack.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                Image resizedWhite = originalWhite.getScaledInstance(100, 100, Image.SCALE_SMOOTH);

                // Create new ImageIcon objects from resized images
                imgblackking = new ImageIcon(resizedBlack);
                imgwhiteking = new ImageIcon(resizedWhite);

                // jlPicblack.setIcon(imgblackking);
                // jlPicwhite.setIcon(imgwhiteking);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (piece.charAt(0) == 'p') {
            try {
                
                Image originalBlack = ImageIO.read(new File("/Users/ammaster10/Downloads/chessgame_6522780376/src/Materials/Bpawn.png"));
                Image originalWhite = ImageIO.read(new File("/Users/ammaster10/Downloads/chessgame_6522780376/src/Materials/Wpawn.png"));

                
                Image resizedBlack = originalBlack.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                Image resizedWhite = originalWhite.getScaledInstance(100, 100, Image.SCALE_SMOOTH);

                // Create new ImageIcon objects from resized images
                imgblackpawn = new ImageIcon(resizedBlack);
                imgwhitepawn = new ImageIcon(resizedWhite);


            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (piece.charAt(0) == 'q') {
            try {
               
                Image originalBlack = ImageIO.read(new File("/Users/ammaster10/Downloads/chessgame_6522780376/src/Materials/Bqueen.png"));
                Image originalWhite = ImageIO.read(new File("/Users/ammaster10/Downloads/chessgame_6522780376/src/Materials/Wqueen.png"));

                
                Image resizedBlack = originalBlack.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                Image resizedWhite = originalWhite.getScaledInstance(100, 100, Image.SCALE_SMOOTH);

               
                imgblackqueen = new ImageIcon(resizedBlack);
                imgwhitequeen = new ImageIcon(resizedWhite);


            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (piece.charAt(0) == 'b') {
            try {
               
                Image originalBlack = ImageIO.read(new File("/Users/ammaster10/Downloads/chessgame_6522780376/src/Materials/Bbishop.png"));
                Image originalWhite = ImageIO.read(new File("/Users/ammaster10/Downloads/chessgame_6522780376/src/Materials/Wbishop.png"));

               
                Image resizedBlack = originalBlack.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                Image resizedWhite = originalWhite.getScaledInstance(100, 100, Image.SCALE_SMOOTH);

        
                imgblackbishop = new ImageIcon(resizedBlack);
                imgwhitebishop = new ImageIcon(resizedWhite);



            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (piece.charAt(0) == 'r') {
            try {
             
                Image originalBlack = ImageIO.read(new File("/Users/ammaster10/Downloads/chessgame_6522780376/src/Materials/Bbishop.png"));
                Image originalWhite = ImageIO.read(new File("/Users/ammaster10/Downloads/chessgame_6522780376/src/Materials/Wrook.png"));

             
                Image resizedBlack = originalBlack.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                Image resizedWhite = originalWhite.getScaledInstance(100, 100, Image.SCALE_SMOOTH);

              
                imgblackrook = new ImageIcon(resizedBlack);
                imgwhiterook = new ImageIcon(resizedWhite);


            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (piece.charAt(0) == 'n') {
            try {
             
                Image originalBlack = ImageIO.read(new File("/Users/ammaster10/Downloads/chessgame_6522780376/src/Materials/Bnight.png"));
                Image originalWhite = ImageIO.read(new File("/Users/ammaster10/Downloads/chessgame_6522780376/src/Materials/Wnight.png"));

            
                Image resizedBlack = originalBlack.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                Image resizedWhite = originalWhite.getScaledInstance(100, 100, Image.SCALE_SMOOTH);

            
                imgblackknight = new ImageIcon(resizedBlack);
                imgwhiteknight = new ImageIcon(resizedWhite);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
