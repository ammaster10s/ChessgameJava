import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** The rules and state for one chess game, independent of its user interface. */
public class ChessGame {
    public enum Side { WHITE, BLACK }
    public enum PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }
    public enum Result { ONGOING, CHECKMATE, STALEMATE, DRAW_INSUFFICIENT_MATERIAL }

    /** An immutable piece; row zero is Black's home rank. */
    public static final class Piece {
        public final Side side;
        public final PieceType type;

        private Piece(Side side, PieceType type) {
            this.side = side;
            this.type = type;
        }
    }

    /** An immutable move. Promotions must name the new piece explicitly. */
    public static final class Move {
        public final int fromRow;
        public final int fromCol;
        public final int toRow;
        public final int toCol;
        public final PieceType promotion;

        public Move(int fromRow, int fromCol, int toRow, int toCol, PieceType promotion) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.promotion = promotion;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Move move)) return false;
            return fromRow == move.fromRow && fromCol == move.fromCol
                    && toRow == move.toRow && toCol == move.toCol
                    && promotion == move.promotion;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fromRow, fromCol, toRow, toCol, promotion);
        }

        @Override
        public String toString() {
            return "(" + fromRow + "," + fromCol + ") to (" + toRow + "," + toCol + ")"
                    + (promotion == null ? "" : "=" + promotion);
        }
    }

    private static final int[][] KNIGHT_STEPS = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
    };
    private static final int[][] DIAGONALS = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    private static final int[][] STRAIGHTS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static final PieceType[] PROMOTIONS = {
            PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT
    };

    private Piece[][] board = new Piece[8][8];
    private Side turn;
    private boolean whiteKingSide;
    private boolean whiteQueenSide;
    private boolean blackKingSide;
    private boolean blackQueenSide;
    private int enPassantRow;
    private int enPassantCol;

    /** Starts with the standard chess position. */
    public ChessGame() {
        reset();
    }

    /** Restores the standard position, including all special move rights. */
    public void reset() {
        board = new Piece[8][8];
        PieceType[] home = {
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };
        for (int col = 0; col < 8; col++) {
            board[0][col] = new Piece(Side.BLACK, home[col]);
            board[1][col] = new Piece(Side.BLACK, PieceType.PAWN);
            board[6][col] = new Piece(Side.WHITE, PieceType.PAWN);
            board[7][col] = new Piece(Side.WHITE, home[col]);
        }
        turn = Side.WHITE;
        whiteKingSide = whiteQueenSide = blackKingSide = blackQueenSide = true;
        enPassantRow = enPassantCol = -1;
    }

    /** Returns the piece on a square, or null for an empty square. */
    public Piece pieceAt(int row, int col) {
        requireSquare(row, col);
        return board[row][col];
    }

    /** Returns the side whose move is next. */
    public Side sideToMove() {
        return turn;
    }

    /** Returns every legal move from the square for the side to move. */
    public List<Move> legalMovesFrom(int row, int col) {
        if (!onBoard(row, col)) return List.of();
        Piece piece = board[row][col];
        if (piece == null || piece.side != turn) return List.of();
        List<Move> legal = new ArrayList<>();
        for (Move move : pseudoMoves(row, col, piece)) {
            Piece[][] next = copyBoard();
            moveOnBoard(next, move);
            if (!isInCheck(next, turn)) legal.add(move);
        }
        return List.copyOf(legal);
    }

    /** Returns all legal moves for the side to move. */
    public List<Move> legalMoves() {
        List<Move> moves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] != null && board[row][col].side == turn) {
                    moves.addAll(legalMovesFrom(row, col));
                }
            }
        }
        return List.copyOf(moves);
    }

    /** Applies a legal move and returns true, or leaves the position unchanged and returns false. */
    public boolean makeMove(Move move) {
        if (move == null || !onBoard(move.fromRow, move.fromCol)
                || result() != Result.ONGOING
                || !legalMovesFrom(move.fromRow, move.fromCol).contains(move)) return false;

        Piece moving = board[move.fromRow][move.fromCol];
        Piece captured = board[move.toRow][move.toCol];
        updateCastlingRights(moving, move, captured);
        moveOnBoard(board, move);
        enPassantRow = enPassantCol = -1;
        if (moving.type == PieceType.PAWN && Math.abs(move.toRow - move.fromRow) == 2) {
            enPassantRow = (move.toRow + move.fromRow) / 2;
            enPassantCol = move.fromCol;
        }
        turn = opposite(turn);
        return true;
    }

    /** Returns whether the named side's king is attacked. */
    public boolean isInCheck(Side side) {
        Objects.requireNonNull(side, "side");
        return isInCheck(board, side);
    }

    /** Returns the current game result. */
    public Result result() {
        if (legalMoves().isEmpty()) return isInCheck(turn) ? Result.CHECKMATE : Result.STALEMATE;
        if (hasInsufficientMaterial()) return Result.DRAW_INSUFFICIENT_MATERIAL;
        return Result.ONGOING;
    }

    private List<Move> pseudoMoves(int row, int col, Piece piece) {
        List<Move> moves = new ArrayList<>();
        switch (piece.type) {
            case PAWN -> pawnMoves(moves, row, col, piece.side);
            case KNIGHT -> {
                for (int[] step : KNIGHT_STEPS) {
                    addIfAvailable(moves, row, col, row + step[0], col + step[1], piece.side);
                }
            }
            case BISHOP -> addSlidingMoves(moves, row, col, piece.side, DIAGONALS);
            case ROOK -> addSlidingMoves(moves, row, col, piece.side, STRAIGHTS);
            case QUEEN -> {
                addSlidingMoves(moves, row, col, piece.side, DIAGONALS);
                addSlidingMoves(moves, row, col, piece.side, STRAIGHTS);
            }
            case KING -> {
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr != 0 || dc != 0) {
                            addIfAvailable(moves, row, col, row + dr, col + dc, piece.side);
                        }
                    }
                }
                addCastlingMoves(moves, row, col, piece.side);
            }
        }
        return moves;
    }

    private void pawnMoves(List<Move> moves, int row, int col, Side side) {
        int direction = side == Side.WHITE ? -1 : 1;
        int start = side == Side.WHITE ? 6 : 1;
        int next = row + direction;
        if (onBoard(next, col) && board[next][col] == null) {
            addPawnMove(moves, row, col, next, col);
            int doubleStep = row + 2 * direction;
            if (row == start && board[doubleStep][col] == null) {
                moves.add(new Move(row, col, doubleStep, col, null));
            }
        }
        for (int delta : new int[]{-1, 1}) {
            int targetCol = col + delta;
            if (!onBoard(next, targetCol)) continue;
            Piece target = board[next][targetCol];
            if (target != null && target.side != side && target.type != PieceType.KING) {
                addPawnMove(moves, row, col, next, targetCol);
            } else if (next == enPassantRow && targetCol == enPassantCol && target == null) {
                Piece victim = board[row][targetCol];
                if (victim != null && victim.side != side && victim.type == PieceType.PAWN) {
                    moves.add(new Move(row, col, next, targetCol, null));
                }
            }
        }
    }

    private void addPawnMove(List<Move> moves, int fromRow, int fromCol, int toRow, int toCol) {
        if (toRow == 0 || toRow == 7) {
            for (PieceType promotion : PROMOTIONS) {
                moves.add(new Move(fromRow, fromCol, toRow, toCol, promotion));
            }
        } else {
            moves.add(new Move(fromRow, fromCol, toRow, toCol, null));
        }
    }

    private void addIfAvailable(List<Move> moves, int row, int col, int toRow, int toCol, Side side) {
        if (!onBoard(toRow, toCol)) return;
        Piece target = board[toRow][toCol];
        if (target == null || (target.side != side && target.type != PieceType.KING)) {
            moves.add(new Move(row, col, toRow, toCol, null));
        }
    }

    private void addSlidingMoves(List<Move> moves, int row, int col, Side side, int[][] directions) {
        for (int[] direction : directions) {
            int toRow = row + direction[0];
            int toCol = col + direction[1];
            while (onBoard(toRow, toCol)) {
                Piece target = board[toRow][toCol];
                if (target == null) {
                    moves.add(new Move(row, col, toRow, toCol, null));
                } else {
                    if (target.side != side && target.type != PieceType.KING) {
                        moves.add(new Move(row, col, toRow, toCol, null));
                    }
                    break;
                }
                toRow += direction[0];
                toCol += direction[1];
            }
        }
    }

    private void addCastlingMoves(List<Move> moves, int row, int col, Side side) {
        int home = side == Side.WHITE ? 7 : 0;
        if (row != home || col != 4 || isInCheck(board, side)) return;
        Side attacker = opposite(side);
        boolean kingSide = side == Side.WHITE ? whiteKingSide : blackKingSide;
        boolean queenSide = side == Side.WHITE ? whiteQueenSide : blackQueenSide;
        if (kingSide && isRook(home, 7, side) && board[home][5] == null && board[home][6] == null
                && !isSquareAttacked(board, home, 5, attacker)
                && !isSquareAttacked(board, home, 6, attacker)) {
            moves.add(new Move(home, 4, home, 6, null));
        }
        if (queenSide && isRook(home, 0, side) && board[home][1] == null
                && board[home][2] == null && board[home][3] == null
                && !isSquareAttacked(board, home, 3, attacker)
                && !isSquareAttacked(board, home, 2, attacker)) {
            moves.add(new Move(home, 4, home, 2, null));
        }
    }

    private boolean isRook(int row, int col, Side side) {
        Piece piece = board[row][col];
        return piece != null && piece.side == side && piece.type == PieceType.ROOK;
    }

    private void updateCastlingRights(Piece moving, Move move, Piece captured) {
        if (moving.type == PieceType.KING) {
            if (moving.side == Side.WHITE) whiteKingSide = whiteQueenSide = false;
            else blackKingSide = blackQueenSide = false;
        }
        if (moving.type == PieceType.ROOK) removeRookRight(moving.side, move.fromRow, move.fromCol);
        if (captured != null && captured.type == PieceType.ROOK) {
            removeRookRight(captured.side, move.toRow, move.toCol);
        }
    }

    private void removeRookRight(Side side, int row, int col) {
        if (side == Side.WHITE && row == 7) {
            if (col == 0) whiteQueenSide = false;
            if (col == 7) whiteKingSide = false;
        } else if (side == Side.BLACK && row == 0) {
            if (col == 0) blackQueenSide = false;
            if (col == 7) blackKingSide = false;
        }
    }

    private void moveOnBoard(Piece[][] position, Move move) {
        Piece piece = position[move.fromRow][move.fromCol];
        if (piece.type == PieceType.PAWN && move.fromCol != move.toCol
                && position[move.toRow][move.toCol] == null) {
            position[move.fromRow][move.toCol] = null; // en passant
        }
        position[move.toRow][move.toCol] = move.promotion == null
                ? piece : new Piece(piece.side, move.promotion);
        position[move.fromRow][move.fromCol] = null;
        if (piece.type == PieceType.KING && Math.abs(move.toCol - move.fromCol) == 2) {
            int rookFrom = move.toCol == 6 ? 7 : 0;
            int rookTo = move.toCol == 6 ? 5 : 3;
            position[move.fromRow][rookTo] = position[move.fromRow][rookFrom];
            position[move.fromRow][rookFrom] = null;
        }
    }

    private Piece[][] copyBoard() {
        Piece[][] copy = new Piece[8][8];
        for (int row = 0; row < 8; row++) {
            System.arraycopy(board[row], 0, copy[row], 0, 8);
        }
        return copy;
    }

    private static boolean isInCheck(Piece[][] position, Side side) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = position[row][col];
                if (piece != null && piece.side == side && piece.type == PieceType.KING) {
                    return isSquareAttacked(position, row, col, opposite(side));
                }
            }
        }
        throw new IllegalStateException("Position has no " + side + " king");
    }

    private static boolean isSquareAttacked(Piece[][] position, int row, int col, Side bySide) {
        int pawnSourceRow = row + (bySide == Side.WHITE ? 1 : -1);
        for (int dc : new int[]{-1, 1}) {
            if (isPiece(position, pawnSourceRow, col + dc, bySide, PieceType.PAWN)) return true;
        }
        for (int[] step : KNIGHT_STEPS) {
            if (isPiece(position, row + step[0], col + step[1], bySide, PieceType.KNIGHT)) return true;
        }
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if ((dr != 0 || dc != 0)
                        && isPiece(position, row + dr, col + dc, bySide, PieceType.KING)) return true;
            }
        }
        return attackedAlong(position, row, col, bySide, DIAGONALS, PieceType.BISHOP)
                || attackedAlong(position, row, col, bySide, STRAIGHTS, PieceType.ROOK);
    }

    private static boolean attackedAlong(Piece[][] position, int row, int col, Side bySide,
                                         int[][] directions, PieceType type) {
        for (int[] direction : directions) {
            int scanRow = row + direction[0];
            int scanCol = col + direction[1];
            while (onBoard(scanRow, scanCol)) {
                Piece piece = position[scanRow][scanCol];
                if (piece != null) {
                    if (piece.side == bySide && (piece.type == type || piece.type == PieceType.QUEEN)) {
                        return true;
                    }
                    break;
                }
                scanRow += direction[0];
                scanCol += direction[1];
            }
        }
        return false;
    }

    private static boolean isPiece(Piece[][] position, int row, int col, Side side, PieceType type) {
        if (!onBoard(row, col)) return false;
        Piece piece = position[row][col];
        return piece != null && piece.side == side && piece.type == type;
    }

    private boolean hasInsufficientMaterial() {
        int minorPieces = 0;
        int knights = 0;
        int bishopSquareColor = -1;
        boolean bishopsOnOneColor = true;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece == null || piece.type == PieceType.KING) continue;
                switch (piece.type) {
                    case PAWN, ROOK, QUEEN -> { return false; }
                    case BISHOP -> {
                        minorPieces++;
                        int squareColor = (row + col) & 1;
                        if (bishopSquareColor >= 0 && squareColor != bishopSquareColor) {
                            bishopsOnOneColor = false;
                        }
                        bishopSquareColor = squareColor;
                    }
                    case KNIGHT -> {
                        minorPieces++;
                        knights++;
                    }
                    default -> { }
                }
            }
        }
        if (minorPieces <= 1) return true; // Bare kings, or one minor piece.
        return knights == 0 && bishopsOnOneColor;
    }

    private static Side opposite(Side side) {
        return side == Side.WHITE ? Side.BLACK : Side.WHITE;
    }

    private static boolean onBoard(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    private static void requireSquare(int row, int col) {
        if (!onBoard(row, col)) throw new IllegalArgumentException("Square is outside the board");
    }
}
