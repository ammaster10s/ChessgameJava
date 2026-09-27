import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A local two-player Swing chess game. ChessGame owns the rules and position. */
public final class ChessApp {
    private static final Color BACKGROUND = new Color(22, 32, 38);
    private static final Color PANEL = new Color(29, 43, 49);
    private static final Color PANEL_RAISED = new Color(41, 58, 64);
    private static final Color TEXT = new Color(244, 241, 231);
    private static final Color MUTED = new Color(187, 203, 201);
    private static final Color ACCENT = new Color(225, 183, 105);
    private static final Color DANGER = new Color(234, 139, 126);
    private static final Color LIGHT_SQUARE = new Color(234, 230, 214);
    private static final Color DARK_SQUARE = new Color(106, 139, 127);

    private final ChessGame game = new ChessGame();
    private final Map<ChessGame.Side, Map<ChessGame.PieceType, BufferedImage>> images =
            new EnumMap<>(ChessGame.Side.class);
    private final BoardPanel board = new BoardPanel();
    private final ClockCard blackClock = new ClockCard("BLACK");
    private final ClockCard whiteClock = new ClockCard("WHITE");
    private final JLabel status = new JLabel();
    private final JLabel hint = new JLabel();
    private final DefaultListModel<String> moveModel = new DefaultListModel<>();
    private final JList<String> moveList = new JList<>(moveModel);
    private final JPanel moveContent = new JPanel(new BorderLayout());
    private final List<TimeButton> timeButtons = new ArrayList<>();
    private final Timer clockTimer = new Timer(100, this::onClockTick);
    private PaintButton drawButton;
    private PaintButton resignButton;

    private JFrame frame;
    private TimeControl timeControl = TimeControl.FIVE_FIVE;
    private ChessGame.Result engineResult = ChessGame.Result.ONGOING;
    private String localResult;
    private List<ChessGame.Move> selectedMoves = List.of();
    private ChessGame.Move lastMove;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private int ply;
    private boolean clockStarted;
    private long lastTickNanos;
    private long whiteMillis;
    private long blackMillis;

    private enum TimeControl {
        THREE_TWO("3 + 2", 3, 2),
        FIVE_FIVE("5 + 5", 5, 5),
        TEN_ZERO("10 + 0", 10, 0);

        final String label;
        final int minutes;
        final int incrementSeconds;

        TimeControl(String label, int minutes, int incrementSeconds) {
            this.label = label;
            this.minutes = minutes;
            this.incrementSeconds = incrementSeconds;
        }
    }

    private ChessApp() {
        loadImages();
        resetGame();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // The default Swing look and feel remains usable.
            }
            new ChessApp().show();
        });
    }

    private void show() {
        frame = new JFrame("Chess — Local match");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(790, 590));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(createHeader(), BorderLayout.NORTH);

        JPanel playArea = new JPanel(new BorderLayout(14, 0));
        playArea.setBackground(BACKGROUND);
        playArea.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 14));
        playArea.add(board, BorderLayout.CENTER);
        playArea.add(createSidebar(), BorderLayout.EAST);
        root.add(playArea, BorderLayout.CENTER);

        frame.setContentPane(root);
        frame.setSize(1120, 760);
        frame.setLocationRelativeTo(null);
        refreshStatus();
        refreshClocks();
        frame.setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(15, 23, 14, 25));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Chess");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        JLabel subtitle = new JLabel("Local two-player match");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        heading.add(title);
        heading.add(subtitle);
        header.add(heading, BorderLayout.WEST);

        PaintButton newGame = new PaintButton("New game", PaintButton.Style.PRIMARY);
        newGame.addActionListener(e -> resetGame());
        header.add(newGame, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 18));
        sidebar.setPreferredSize(new Dimension(288, 640));
        sidebar.setBackground(PANEL);
        sidebar.setBorder(BorderFactory.createEmptyBorder(17, 17, 17, 17));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        blackClock.setAlignmentX(0);
        whiteClock.setAlignmentX(0);
        top.add(blackClock);
        top.add(Box.createVerticalStrut(9));
        top.add(whiteClock);
        top.add(Box.createVerticalStrut(18));

        JPanel statusPanel = new JPanel();
        statusPanel.setOpaque(false);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setAlignmentX(0);
        status.setFont(new Font("SansSerif", Font.BOLD, 17));
        status.setForeground(TEXT);
        status.setAlignmentX(0);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hint.setForeground(MUTED);
        hint.setAlignmentX(0);
        statusPanel.add(status);
        statusPanel.add(Box.createVerticalStrut(5));
        statusPanel.add(hint);
        top.add(statusPanel);
        top.add(Box.createVerticalStrut(21));

        JLabel timeLabel = sectionLabel("Time control");
        timeLabel.setAlignmentX(0);
        top.add(timeLabel);
        top.add(Box.createVerticalStrut(9));
        JPanel presets = new JPanel(new GridLayout(1, 3, 7, 0));
        presets.setOpaque(false);
        presets.setAlignmentX(0);
        presets.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        ButtonGroup group = new ButtonGroup();
        for (TimeControl control : TimeControl.values()) {
            TimeButton button = new TimeButton(control);
            button.setSelected(control == timeControl);
            button.addActionListener(e -> chooseTimeControl(control));
            group.add(button);
            timeButtons.add(button);
            presets.add(button);
        }
        top.add(presets);
        sidebar.add(top, BorderLayout.NORTH);

        JPanel history = new JPanel(new BorderLayout(0, 10));
        history.setOpaque(false);
        history.add(sectionLabel("Moves"), BorderLayout.NORTH);
        moveContent.setOpaque(false);
        moveList.setBackground(PANEL_RAISED);
        moveList.setForeground(TEXT);
        moveList.setFont(new Font("Monospaced", Font.PLAIN, 13));
        moveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        moveList.setFocusable(false);
        moveList.setCellRenderer(new MoveRenderer());
        JScrollPane scroller = new JScrollPane(moveList);
        scroller.setBorder(BorderFactory.createEmptyBorder());
        scroller.getViewport().setBackground(PANEL_RAISED);
        scroller.getVerticalScrollBar().setUnitIncrement(18);
        moveContent.add(scroller, BorderLayout.CENTER);
        history.add(moveContent, BorderLayout.CENTER);
        sidebar.add(history, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 9, 0));
        actions.setOpaque(false);
        drawButton = new PaintButton("Agree draw", PaintButton.Style.SECONDARY);
        drawButton.addActionListener(e -> agreeDraw());
        resignButton = new PaintButton("Resign", PaintButton.Style.DANGER);
        resignButton.addActionListener(e -> resign());
        actions.add(drawButton);
        actions.add(resignButton);
        sidebar.add(actions, BorderLayout.SOUTH);
        updateMoveContent();
        return sidebar;
    }

    private static JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        return label;
    }

    private void chooseTimeControl(TimeControl control) {
        if (clockStarted || isFinished()) return;
        timeControl = control;
        whiteMillis = blackMillis = control.minutes * 60_000L;
        refreshClocks();
    }

    private void resetGame() {
        clockTimer.stop();
        game.reset();
        engineResult = ChessGame.Result.ONGOING;
        localResult = null;
        selectedMoves = List.of();
        selectedRow = selectedCol = -1;
        lastMove = null;
        ply = 0;
        clockStarted = false;
        whiteMillis = blackMillis = timeControl.minutes * 60_000L;
        moveModel.clear();
        updateMoveContent();
        for (TimeButton button : timeButtons) button.setEnabled(true);
        refreshStatus();
        refreshClocks();
        board.repaint();
    }

    private void selectSquare(int row, int col) {
        if (isFinished()) return;
        settleClock();
        if (isFinished()) return;

        if (selectedRow == row && selectedCol == col) {
            clearSelection();
            return;
        }
        ChessGame.Piece piece = game.pieceAt(row, col);
        if (piece != null && piece.side == game.sideToMove()) {
            selectedRow = row;
            selectedCol = col;
            selectedMoves = game.legalMovesFrom(row, col);
            board.repaint();
            return;
        }
        List<ChessGame.Move> candidates = new ArrayList<>();
        for (ChessGame.Move move : selectedMoves) {
            if (move.toRow == row && move.toCol == col) candidates.add(move);
        }
        if (!candidates.isEmpty()) {
            ChessGame.Move chosen = chooseMove(candidates);
            if (chosen != null) playMove(chosen);
        } else {
            clearSelection();
        }
    }

    private ChessGame.Move chooseMove(List<ChessGame.Move> candidates) {
        if (candidates.size() == 1) return candidates.get(0);
        ChessGame.PieceType[] order = {
                ChessGame.PieceType.QUEEN, ChessGame.PieceType.ROOK,
                ChessGame.PieceType.BISHOP, ChessGame.PieceType.KNIGHT
        };
        String[] labels = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(frame, "Choose a piece for your pawn.",
                "Promote pawn", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, labels, labels[0]);
        if (choice < 0) return null;
        for (ChessGame.Move move : candidates) {
            if (move.promotion == order[choice]) return move;
        }
        return null;
    }

    private void playMove(ChessGame.Move move) {
        // A promotion dialog can stay open while the opponent's clock expires.
        if (isFinished()) return;
        settleClock();
        if (isFinished()) return;
        ChessGame.Side mover = game.sideToMove();
        ChessGame.Piece piece = game.pieceAt(move.fromRow, move.fromCol);
        ChessGame.Piece target = game.pieceAt(move.toRow, move.toCol);
        boolean capture = target != null || (piece.type == ChessGame.PieceType.PAWN
                && move.fromCol != move.toCol);
        String notation = moveNotation(move, piece, capture);
        if (!game.makeMove(move)) return;

        if (clockStarted) addIncrement(mover);
        else {
            clockStarted = true;
            addIncrement(mover);
            for (TimeButton button : timeButtons) button.setEnabled(false);
        }
        lastTickNanos = System.nanoTime();
        engineResult = game.result();
        if (engineResult == ChessGame.Result.CHECKMATE) notation += "#";
        else if (engineResult == ChessGame.Result.ONGOING && game.isInCheck(game.sideToMove())) {
            notation += "+";
        }
        addMoveToHistory(mover, notation);
        lastMove = move;
        selectedMoves = List.of();
        selectedRow = selectedCol = -1;
        if (engineResult == ChessGame.Result.ONGOING) clockTimer.start();
        else clockTimer.stop();
        refreshStatus();
        refreshClocks();
        board.repaint();
    }

    private void addMoveToHistory(ChessGame.Side mover, String notation) {
        if (mover == ChessGame.Side.WHITE) {
            moveModel.addElement(String.format("%2d.  %s", ply / 2 + 1, notation));
        } else {
            int last = moveModel.size() - 1;
            if (last >= 0) moveModel.set(last, moveModel.get(last) + "    " + notation);
            else moveModel.addElement(String.format("%2d... %s", ply / 2 + 1, notation));
        }
        ply++;
        updateMoveContent();
        moveList.ensureIndexIsVisible(moveModel.size() - 1);
    }

    private void updateMoveContent() {
        if (moveContent == null) return;
        if (moveModel.isEmpty()) {
            moveContent.removeAll();
            JLabel empty = new JLabel("Select a piece to begin", SwingConstants.CENTER);
            empty.setForeground(MUTED);
            empty.setFont(new Font("SansSerif", Font.PLAIN, 12));
            empty.setOpaque(true);
            empty.setBackground(PANEL_RAISED);
            moveContent.add(empty, BorderLayout.CENTER);
        } else if (!(moveContent.getComponent(0) instanceof JScrollPane)) {
            moveContent.removeAll();
            JScrollPane scroller = new JScrollPane(moveList);
            scroller.setBorder(BorderFactory.createEmptyBorder());
            scroller.getViewport().setBackground(PANEL_RAISED);
            scroller.getVerticalScrollBar().setUnitIncrement(18);
            moveContent.add(scroller, BorderLayout.CENTER);
        }
        moveContent.revalidate();
        moveContent.repaint();
    }

    private static String moveNotation(ChessGame.Move move, ChessGame.Piece piece,
                                       boolean capture) {
        if (piece.type == ChessGame.PieceType.KING
                && Math.abs(move.toCol - move.fromCol) == 2) {
            return move.toCol > move.fromCol ? "O-O" : "O-O-O";
        }
        String from = squareName(move.fromRow, move.fromCol);
        String to = squareName(move.toRow, move.toCol);
        String result = from + (capture ? "×" : "–") + to;
        if (move.promotion != null) result += "=" + move.promotion.name().charAt(0);
        return result;
    }

    private static String squareName(int row, int col) {
        return "" + (char) ('a' + col) + (8 - row);
    }

    private void clearSelection() {
        selectedRow = selectedCol = -1;
        selectedMoves = List.of();
        board.repaint();
    }

    private void addIncrement(ChessGame.Side mover) {
        long increment = timeControl.incrementSeconds * 1_000L;
        if (mover == ChessGame.Side.WHITE) whiteMillis += increment;
        else blackMillis += increment;
    }

    private void onClockTick(ActionEvent ignored) {
        settleClock();
        refreshClocks();
    }

    private void settleClock() {
        if (!clockStarted || isFinished()) return;
        long now = System.nanoTime();
        long elapsed = Math.max(0, (now - lastTickNanos) / 1_000_000L);
        lastTickNanos += elapsed * 1_000_000L;
        if (game.sideToMove() == ChessGame.Side.WHITE) {
            whiteMillis = Math.max(0, whiteMillis - elapsed);
            if (whiteMillis == 0) finishLocally("Black wins on time");
        } else {
            blackMillis = Math.max(0, blackMillis - elapsed);
            if (blackMillis == 0) finishLocally("White wins on time");
        }
    }

    private void agreeDraw() {
        if (isFinished()) return;
        int answer = JOptionPane.showConfirmDialog(frame, "End this game as a draw?",
                "Agree draw", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer == JOptionPane.YES_OPTION && !isFinished()) finishLocally("Draw agreed");
    }

    private void resign() {
        if (isFinished()) return;
        String player = sideName(game.sideToMove());
        String winner = sideName(opposite(game.sideToMove()));
        int answer = JOptionPane.showConfirmDialog(frame,
                player + " resigns. " + winner + " wins. Continue?",
                "Resign game", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION && !isFinished()) {
            finishLocally(winner + " wins by resignation");
        }
    }

    private void finishLocally(String result) {
        localResult = result;
        clockTimer.stop();
        clearSelection();
        refreshStatus();
        refreshClocks();
    }

    private boolean isFinished() {
        return localResult != null || engineResult != ChessGame.Result.ONGOING;
    }

    private void refreshStatus() {
        if (localResult != null) {
            status.setText(localResult);
            hint.setText("Start a new game to play again.");
        } else if (engineResult == ChessGame.Result.CHECKMATE) {
            status.setText(sideName(opposite(game.sideToMove())) + " wins by checkmate");
            hint.setText("Start a new game to play again.");
        } else if (engineResult == ChessGame.Result.STALEMATE) {
            status.setText("Draw by stalemate");
            hint.setText("Neither player has a legal move.");
        } else if (engineResult == ChessGame.Result.DRAW_INSUFFICIENT_MATERIAL) {
            status.setText("Draw — insufficient material");
            hint.setText("Neither side can force checkmate.");
        } else if (game.isInCheck(game.sideToMove())) {
            status.setText(sideName(game.sideToMove()) + " is in check");
            hint.setText("Protect your king with a legal move.");
        } else {
            status.setText(sideName(game.sideToMove()) + " to move");
            hint.setText(clockStarted ? "Select a piece to see legal moves."
                    : "The clock starts after the first move.");
        }
        status.setForeground(game.isInCheck(game.sideToMove()) && !isFinished() ? DANGER : TEXT);
        if (drawButton != null) drawButton.setEnabled(!isFinished());
        if (resignButton != null) resignButton.setEnabled(!isFinished());
    }

    private void refreshClocks() {
        blackClock.update(blackMillis, game.sideToMove() == ChessGame.Side.BLACK && !isFinished());
        whiteClock.update(whiteMillis, game.sideToMove() == ChessGame.Side.WHITE && !isFinished());
    }

    private static String sideName(ChessGame.Side side) {
        return side == ChessGame.Side.WHITE ? "White" : "Black";
    }

    private static ChessGame.Side opposite(ChessGame.Side side) {
        return side == ChessGame.Side.WHITE ? ChessGame.Side.BLACK : ChessGame.Side.WHITE;
    }

    private static String formatTime(long millis) {
        if (millis < 10_000L) return String.format("%d.%d", millis / 1_000L,
                (millis % 1_000L) / 100L);
        long seconds = (millis + 999L) / 1_000L;
        return String.format("%d:%02d", seconds / 60L, seconds % 60L);
    }

    private void loadImages() {
        for (ChessGame.Side side : ChessGame.Side.values()) {
            Map<ChessGame.PieceType, BufferedImage> perSide =
                    new EnumMap<>(ChessGame.PieceType.class);
            images.put(side, perSide);
            for (ChessGame.PieceType type : ChessGame.PieceType.values()) {
                String pieceName = type == ChessGame.PieceType.KNIGHT ? "night"
                        : type.name().toLowerCase();
                String fileName = (side == ChessGame.Side.WHITE ? "W" : "B")
                        + pieceName + ".png";
                try (InputStream in = imageStream(fileName)) {
                    if (in != null) perSide.put(type, cropTransparent(ImageIO.read(in)));
                } catch (IOException error) {
                    System.err.println("Could not load " + fileName + ": " + error.getMessage());
                }
            }
        }
    }

    private static InputStream imageStream(String name) throws IOException {
        InputStream resource = ChessApp.class.getResourceAsStream("/" + name);
        if (resource != null) return resource;
        Path local = Path.of(name);
        if (Files.isRegularFile(local)) return Files.newInputStream(local);
        Path assets = Path.of("assets", name);
        if (Files.isRegularFile(assets)) return Files.newInputStream(assets);
        return null;
    }

    private static BufferedImage cropTransparent(BufferedImage image) {
        if (image == null) return null;
        int minX = image.getWidth(), minY = image.getHeight(), maxX = -1, maxY = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) > 8) {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        return maxX < minX ? image : image.getSubimage(minX, minY,
                maxX - minX + 1, maxY - minY + 1);
    }

    private static final class ClockCard extends JPanel {
        private static final long serialVersionUID = 1L;
        private final JLabel name = new JLabel();
        private final JLabel time = new JLabel();
        private final String player;
        private boolean active;

        ClockCard(String player) {
            this.player = player;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(10, 13, 10, 13));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
            setPreferredSize(new Dimension(240, 74));
            name.setFont(new Font("SansSerif", Font.BOLD, 11));
            name.setForeground(MUTED);
            time.setHorizontalAlignment(SwingConstants.RIGHT);
            time.setFont(new Font("Monospaced", Font.BOLD, 29));
            time.setForeground(TEXT);
            add(name, BorderLayout.NORTH);
            add(time, BorderLayout.CENTER);
        }

        void update(long millis, boolean isActive) {
            active = isActive;
            name.setText(player + (active ? "  ·  TO MOVE" : ""));
            name.setForeground(active ? ACCENT : MUTED);
            time.setText(formatTime(millis));
            time.setForeground(millis < 20_000L ? DANGER : TEXT);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(active ? new Color(48, 69, 69) : PANEL_RAISED);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g.setColor(active ? ACCENT : new Color(74, 92, 96));
            g.setStroke(new BasicStroke(active ? 2f : 1f));
            g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class MoveRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;
        @Override
        public java.awt.Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean selected, boolean focused) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
                    index, selected, focused);
            label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 8));
            label.setBackground(selected ? new Color(64, 84, 84)
                    : index % 2 == 0 ? PANEL_RAISED : new Color(46, 64, 69));
            label.setForeground(TEXT);
            return label;
        }
    }

    private static final class PaintButton extends JButton {
        private static final long serialVersionUID = 1L;
        enum Style { PRIMARY, SECONDARY, DANGER }
        private final Style style;

        PaintButton(String label, Style style) {
            super(label);
            this.style = style;
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(style == Style.PRIMARY ? 100 : 112, 36));
            getAccessibleContext().setAccessibleName(label);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = switch (style) {
                case PRIMARY -> ACCENT;
                case SECONDARY -> PANEL_RAISED;
                case DANGER -> new Color(65, 52, 54);
            };
            if (getModel().isRollover()) fill = fill.brighter();
            if (getModel().isPressed()) fill = fill.darker();
            if (!isEnabled()) fill = PANEL_RAISED;
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g.setColor(hasFocus() ? ACCENT : style == Style.DANGER ? DANGER : fill);
            g.setStroke(new BasicStroke(hasFocus() ? 2f : 1f));
            g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
            g.setColor(style == Style.PRIMARY ? BACKGROUND : style == Style.DANGER ? DANGER : TEXT);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g.drawString(getText(), x, y);
            g.dispose();
        }
    }

    private static final class TimeButton extends JToggleButton {
        private static final long serialVersionUID = 1L;
        final TimeControl control;

        TimeButton(TimeControl control) {
            super(control.label);
            this.control = control;
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            getAccessibleContext().setAccessibleDescription(control.minutes + " minutes with "
                    + control.incrementSeconds + " seconds added after each move");
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = isSelected() ? ACCENT : PANEL_RAISED;
            if (getModel().isRollover() && isEnabled()) fill = fill.brighter();
            if (!isEnabled()) fill = isSelected() ? new Color(145, 125, 89) : PANEL_RAISED;
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
            if (hasFocus()) {
                g.setColor(TEXT);
                g.setStroke(new BasicStroke(2f));
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 9, 9);
            }
            g.setFont(getFont());
            g.setColor(isSelected() ? BACKGROUND : isEnabled() ? TEXT : MUTED);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g.dispose();
        }
    }

    private final class BoardPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Map<String, BufferedImage> scaledImages = new HashMap<>();
        private final Map<String, BufferedImage> outlines = new HashMap<>();
        private int cachedCell = -1;
        private int originX;
        private int originY;
        private int cell;
        private int focusRow = 6;
        private int focusCol = 4;

        BoardPanel() {
            setBackground(BACKGROUND);
            setPreferredSize(new Dimension(670, 670));
            setFocusable(true);
            getAccessibleContext().setAccessibleName("Chess board");
            getAccessibleContext().setAccessibleDescription(
                    "Use arrow keys to choose a square, Enter or Space to select or move, and Escape to clear selection.");
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    requestFocusInWindow();
                    int[] square = squareAt(event.getX(), event.getY());
                    if (square != null) {
                        focusRow = square[0];
                        focusCol = square[1];
                        selectSquare(square[0], square[1]);
                        repaint();
                    }
                }
            });
            bindKey("UP", -1, 0);
            bindKey("DOWN", 1, 0);
            bindKey("LEFT", 0, -1);
            bindKey("RIGHT", 0, 1);
            getInputMap(WHEN_FOCUSED).put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "choose");
            getInputMap(WHEN_FOCUSED).put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "choose");
            getActionMap().put("choose", new javax.swing.AbstractAction() {
                @Override public void actionPerformed(ActionEvent event) {
                    selectSquare(focusRow, focusCol);
                }
            });
            getInputMap(WHEN_FOCUSED).put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clear");
            getActionMap().put("clear", new javax.swing.AbstractAction() {
                @Override public void actionPerformed(ActionEvent event) { clearSelection(); }
            });
        }

        private void bindKey(String key, int rowDelta, int colDelta) {
            getInputMap(WHEN_FOCUSED).put(javax.swing.KeyStroke.getKeyStroke(key), key);
            getActionMap().put(key, new javax.swing.AbstractAction() {
                @Override public void actionPerformed(ActionEvent event) {
                    focusRow = Math.max(0, Math.min(7, focusRow + rowDelta));
                    focusCol = Math.max(0, Math.min(7, focusCol + colDelta));
                    repaint();
                }
            });
        }

        private int[] squareAt(int x, int y) {
            if (cell < 1 || x < originX || y < originY
                    || x >= originX + 8 * cell || y >= originY + 8 * cell) return null;
            return new int[]{(y - originY) / cell, (x - originX) / cell};
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            cell = Math.max(1, Math.min((getWidth() - 28) / 8, (getHeight() - 28) / 8));
            originX = (getWidth() - cell * 8) / 2;
            originY = (getHeight() - cell * 8) / 2;
            if (cachedCell != cell) {
                scaledImages.clear();
                outlines.clear();
                cachedCell = cell;
            }
            g.setColor(new Color(8, 18, 22, 100));
            g.fillRoundRect(originX - 5, originY + 6, cell * 8 + 10, cell * 8 + 8, 8, 8);

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    int x = originX + col * cell, y = originY + row * cell;
                    Color square = (row + col) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE;
                    g.setColor(square);
                    g.fillRect(x, y, cell, cell);
                    if (lastMove != null && ((lastMove.fromRow == row && lastMove.fromCol == col)
                            || (lastMove.toRow == row && lastMove.toCol == col))) {
                        g.setColor(new Color(231, 185, 87, 90));
                        g.fillRect(x, y, cell, cell);
                    }
                    if (selectedRow == row && selectedCol == col) {
                        g.setColor(new Color(244, 197, 89, 125));
                        g.fillRect(x, y, cell, cell);
                    }
                    ChessGame.Piece piece = game.pieceAt(row, col);
                    if (piece != null && piece.side == game.sideToMove()
                            && piece.type == ChessGame.PieceType.KING
                            && game.isInCheck(piece.side) && !isFinished()) {
                        g.setColor(new Color(207, 75, 69, 140));
                        g.fillRect(x, y, cell, cell);
                    }
                }
            }

            for (ChessGame.Move move : selectedMoves) {
                int x = originX + move.toCol * cell, y = originY + move.toRow * cell;
                ChessGame.Piece target = game.pieceAt(move.toRow, move.toCol);
                ChessGame.Piece selected = game.pieceAt(move.fromRow, move.fromCol);
                boolean capture = target != null || (selected != null
                        && selected.type == ChessGame.PieceType.PAWN
                        && move.fromCol != move.toCol);
                g.setColor(new Color(34, 54, 50, 130));
                if (capture) {
                    g.setStroke(new BasicStroke(Math.max(2f, cell * 0.055f)));
                    int inset = Math.max(5, cell / 12);
                    g.drawOval(x + inset, y + inset, cell - inset * 2, cell - inset * 2);
                } else {
                    int diameter = Math.max(10, cell / 4);
                    g.fillOval(x + (cell - diameter) / 2, y + (cell - diameter) / 2,
                            diameter, diameter);
                }
            }

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    ChessGame.Piece piece = game.pieceAt(row, col);
                    if (piece != null) drawPiece(g, piece, originX + col * cell, originY + row * cell);
                }
            }
            drawCoordinates(g);
            if (hasFocus()) {
                g.setColor(new Color(250, 205, 112, 220));
                g.setStroke(new BasicStroke(2f));
                g.drawRect(originX + focusCol * cell + 2, originY + focusRow * cell + 2,
                        cell - 5, cell - 5);
            }
            g.dispose();
        }

        private void drawCoordinates(Graphics2D g) {
            g.setFont(new Font("SansSerif", Font.BOLD, Math.max(10, cell / 7)));
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i < 8; i++) {
                g.setColor(i % 2 == 0 ? DARK_SQUARE.darker() : LIGHT_SQUARE);
                g.drawString(Integer.toString(8 - i), originX + 4,
                        originY + i * cell + fm.getAscent() + 2);
                g.setColor((7 + i) % 2 == 0 ? DARK_SQUARE.darker() : LIGHT_SQUARE);
                String file = Character.toString((char) ('a' + i));
                g.drawString(file, originX + i * cell + cell - fm.stringWidth(file) - 5,
                        originY + 8 * cell - 5);
            }
        }

        private void drawPiece(Graphics2D g, ChessGame.Piece piece, int x, int y) {
            BufferedImage source = images.get(piece.side).get(piece.type);
            if (source == null) {
                drawFallbackPiece(g, piece, x, y);
                return;
            }
            String key = piece.side.name() + piece.type.name();
            BufferedImage scaled = scaledImages.computeIfAbsent(key,
                    ignored -> scalePiece(source, cell));
            BufferedImage outline = outlines.computeIfAbsent(key,
                    ignored -> silhouette(scaled, piece.side == ChessGame.Side.WHITE
                            ? new Color(35, 53, 52) : new Color(242, 236, 216)));
            int px = x + (cell - scaled.getWidth()) / 2;
            int py = y + (cell - scaled.getHeight()) / 2;
            int radius = Math.max(1, cell / 48);
            for (int oy = -radius; oy <= radius; oy++) {
                for (int ox = -radius; ox <= radius; ox++) {
                    if (ox != 0 || oy != 0) g.drawImage(outline, px + ox, py + oy, null);
                }
            }
            g.drawImage(scaled, px, py, null);
        }

        private void drawFallbackPiece(Graphics2D g, ChessGame.Piece piece, int x, int y) {
            String letter = piece.type == ChessGame.PieceType.KNIGHT ? "N"
                    : piece.type.name().substring(0, 1);
            g.setFont(new Font("Serif", Font.BOLD, Math.max(19, cell * 2 / 3)));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(piece.side == ChessGame.Side.WHITE ? TEXT : BACKGROUND);
            g.drawString(letter, x + (cell - fm.stringWidth(letter)) / 2,
                    y + (cell + fm.getAscent() - fm.getDescent()) / 2);
        }

        private BufferedImage scalePiece(BufferedImage source, int square) {
            int max = Math.max(1, square * 4 / 5);
            double ratio = Math.min((double) max / source.getWidth(),
                    (double) max / source.getHeight());
            int width = Math.max(1, (int) Math.round(source.getWidth() * ratio));
            int height = Math.max(1, (int) Math.round(source.getHeight() * ratio));
            BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = scaled.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(source, 0, 0, width, height, null);
            graphics.dispose();
            return scaled;
        }

        private BufferedImage silhouette(BufferedImage source, Color color) {
            BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = result.createGraphics();
            graphics.drawImage(source, 0, 0, null);
            graphics.setComposite(AlphaComposite.SrcIn);
            graphics.setColor(color);
            graphics.fillRect(0, 0, result.getWidth(), result.getHeight());
            graphics.dispose();
            return result;
        }
    }
}
