import java.util.List;

/** Dependency-free regression tests for the headless chess rules. */
public final class ChessGameTest {
    private static int testsRun;

    public static void main(String[] args) {
        run("initial position and reset", ChessGameTest::initialPositionAndReset);
        run("turns and illegal moves", ChessGameTest::turnsAndIllegalMoves);
        run("blocked sliders and pawns", ChessGameTest::blockedSlidersAndPawns);
        run("pawn capture", ChessGameTest::pawnCapture);
        run("Fool's Mate", ChessGameTest::foolsMate);
        run("en passant", ChessGameTest::enPassant);
        run("castling", ChessGameTest::castling);
        run("pinned knight", ChessGameTest::pinnedKnight);
        run("castling into check", ChessGameTest::castlingIntoCheck);
        run("promotion choices", ChessGameTest::promotionChoices);
        System.out.println("All " + testsRun + " ChessGame tests passed.");
    }

    private static void initialPositionAndReset() {
        ChessGame game = new ChessGame();
        assertInitialPosition(game);

        play(game, 6, 4, 4, 4); // e2-e4
        play(game, 1, 4, 3, 4); // e7-e5
        game.reset();
        assertInitialPosition(game);
    }

    private static void assertInitialPosition(ChessGame game) {
        ChessGame.PieceType[] backRank = {
                ChessGame.PieceType.ROOK, ChessGame.PieceType.KNIGHT,
                ChessGame.PieceType.BISHOP, ChessGame.PieceType.QUEEN,
                ChessGame.PieceType.KING, ChessGame.PieceType.BISHOP,
                ChessGame.PieceType.KNIGHT, ChessGame.PieceType.ROOK
        };
        int whiteCount = 0;
        int blackCount = 0;
        for (int col = 0; col < 8; col++) {
            assertPiece(game, 7, col, ChessGame.Side.WHITE, backRank[col]);
            assertPiece(game, 6, col, ChessGame.Side.WHITE, ChessGame.PieceType.PAWN);
            assertPiece(game, 1, col, ChessGame.Side.BLACK, ChessGame.PieceType.PAWN);
            assertPiece(game, 0, col, ChessGame.Side.BLACK, backRank[col]);
        }
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessGame.Piece piece = game.pieceAt(row, col);
                if (piece != null) {
                    if (piece.side == ChessGame.Side.WHITE) whiteCount++;
                    else blackCount++;
                }
            }
        }
        check(whiteCount == 16 && blackCount == 16, "opening must have 16 pieces per side");
        check(game.sideToMove() == ChessGame.Side.WHITE, "white must move first");
        check(game.result() == ChessGame.Result.ONGOING, "opening must be ongoing");
        check(!game.isInCheck(ChessGame.Side.WHITE), "white starts out of check");
        check(!game.isInCheck(ChessGame.Side.BLACK), "black starts out of check");
        check(game.legalMoves().size() == 20, "opening must have 20 legal moves");
    }

    private static void turnsAndIllegalMoves() {
        ChessGame game = new ChessGame();
        ChessGame.Move whiteKnight = move(7, 6, 5, 5);
        check(!game.makeMove(move(1, 4, 3, 4)), "black cannot move first");
        check(!game.makeMove(move(7, 0, 5, 0)), "rook cannot jump over its pawn");
        check(game.pieceAt(1, 4).type == ChessGame.PieceType.PAWN,
                "rejected move must leave the board unchanged");
        check(game.sideToMove() == ChessGame.Side.WHITE,
                "rejected move must not change the turn");

        play(game, 6, 4, 4, 4);
        check(!game.makeMove(whiteKnight), "white cannot move twice in a row");
        check(game.pieceAt(7, 6).type == ChessGame.PieceType.KNIGHT,
                "rejected second move must leave the knight in place");
        check(game.sideToMove() == ChessGame.Side.BLACK,
                "rejected second move must preserve black's turn");
        play(game, 1, 4, 3, 4);
        check(game.sideToMove() == ChessGame.Side.WHITE, "turn must return to white");
    }

    private static void blockedSlidersAndPawns() {
        ChessGame game = new ChessGame();
        check(game.legalMovesFrom(7, 0).isEmpty(), "a1 rook starts blocked");
        check(game.legalMovesFrom(7, 2).isEmpty(), "c1 bishop starts blocked");
        check(game.legalMovesFrom(6, 4).size() == 2,
                "e2 pawn starts with one-step and two-step moves");
        check(!game.legalMovesFrom(6, 4).contains(move(6, 4, 5, 3)),
                "pawn cannot capture an empty square");
        check(!game.makeMove(move(6, 4, 3, 4)), "pawn cannot move three squares");

        play(game, 6, 4, 4, 4);
        play(game, 1, 4, 3, 4);
        check(game.legalMovesFrom(4, 4).isEmpty(),
                "e4 pawn cannot advance onto the opposing e5 pawn");
        check(!game.makeMove(move(4, 4, 3, 4)),
                "blocked pawn advance must be rejected");
    }

    private static void pawnCapture() {
        ChessGame game = new ChessGame();
        play(game, 6, 4, 4, 4); // e2-e4
        play(game, 1, 3, 3, 3); // d7-d5
        play(game, 4, 4, 3, 3); // e4xd5
        assertPiece(game, 3, 3, ChessGame.Side.WHITE, ChessGame.PieceType.PAWN);
        check(game.pieceAt(4, 4) == null, "capturing pawn must leave its origin");
        check(game.sideToMove() == ChessGame.Side.BLACK, "capture must pass the turn");
    }

    private static void foolsMate() {
        ChessGame game = new ChessGame();
        play(game, 6, 5, 5, 5); // f2-f3
        play(game, 1, 4, 3, 4); // e7-e5
        play(game, 6, 6, 4, 6); // g2-g4
        play(game, 0, 3, 4, 7); // Qd8-h4#
        check(game.isInCheck(ChessGame.Side.WHITE), "Fool's Mate checks white");
        check(game.result() == ChessGame.Result.CHECKMATE, "Fool's Mate is checkmate");
        check(game.legalMoves().isEmpty(), "checkmated side has no legal moves");
        check(!game.makeMove(move(6, 0, 5, 0)), "no move is allowed after checkmate");
    }

    private static void enPassant() {
        ChessGame game = enPassantPosition();
        check(game.legalMovesFrom(3, 4).contains(move(3, 4, 2, 3)),
                "e5xd6 en passant must be legal immediately");
        play(game, 3, 4, 2, 3);
        assertPiece(game, 2, 3, ChessGame.Side.WHITE, ChessGame.PieceType.PAWN);
        check(game.pieceAt(3, 3) == null,
                "en passant must remove the pawn from d5");

        game = enPassantPosition();
        play(game, 7, 6, 5, 5); // White declines en passant with Ng1-f3.
        play(game, 0, 1, 2, 2); // Black responds Nb8-c6.
        check(!game.legalMovesFrom(3, 4).contains(move(3, 4, 2, 3)),
                "en passant right must expire after one turn");
    }

    private static ChessGame enPassantPosition() {
        ChessGame game = new ChessGame();
        play(game, 6, 4, 4, 4); // e2-e4
        play(game, 1, 0, 2, 0); // a7-a6
        play(game, 4, 4, 3, 4); // e4-e5
        play(game, 1, 3, 3, 3); // d7-d5
        return game;
    }

    private static void castling() {
        ChessGame game = new ChessGame();
        play(game, 7, 6, 5, 5); // Ng1-f3
        play(game, 0, 6, 2, 5); // Ng8-f6
        play(game, 6, 4, 4, 4); // e2-e4
        play(game, 1, 4, 3, 4); // e7-e5
        play(game, 7, 5, 6, 4); // Bf1-e2
        play(game, 0, 5, 1, 4); // Bf8-e7

        check(game.legalMovesFrom(7, 4).contains(move(7, 4, 7, 6)),
                "white kingside castling must be legal after clearing the path");
        play(game, 7, 4, 7, 6);
        assertPiece(game, 7, 6, ChessGame.Side.WHITE, ChessGame.PieceType.KING);
        assertPiece(game, 7, 5, ChessGame.Side.WHITE, ChessGame.PieceType.ROOK);
        check(game.pieceAt(7, 4) == null && game.pieceAt(7, 7) == null,
                "white king and rook must leave their original squares");

        check(game.legalMovesFrom(0, 4).contains(move(0, 4, 0, 6)),
                "black kingside castling must also be legal");
        play(game, 0, 4, 0, 6);
        assertPiece(game, 0, 6, ChessGame.Side.BLACK, ChessGame.PieceType.KING);
        assertPiece(game, 0, 5, ChessGame.Side.BLACK, ChessGame.PieceType.ROOK);
    }

    private static void pinnedKnight() {
        ChessGame game = new ChessGame();
        play(game, 6, 3, 4, 3); // d2-d4 clears the d2 square behind the knight.
        play(game, 1, 4, 2, 4); // e7-e6 opens the black bishop's diagonal.
        play(game, 7, 1, 5, 2); // Nb1-c3
        play(game, 0, 5, 4, 1); // Bf8-b4 pins the knight to the king.
        check(!game.isInCheck(ChessGame.Side.WHITE), "the knight currently blocks check");
        check(game.legalMovesFrom(5, 2).isEmpty(), "pinned knight cannot expose its king");
        check(!game.makeMove(move(5, 2, 3, 1)), "pinned knight move must be rejected");
        assertPiece(game, 5, 2, ChessGame.Side.WHITE, ChessGame.PieceType.KNIGHT);
    }

    private static void castlingIntoCheck() {
        ChessGame game = new ChessGame();
        play(game, 6, 5, 5, 5); // f2-f3 opens the diagonal to g1.
        play(game, 1, 4, 3, 4); // e7-e5
        play(game, 7, 6, 5, 7); // Ng1-h3 clears g1.
        play(game, 0, 5, 3, 2); // Bf8-c5 attacks g1.
        play(game, 6, 4, 4, 4); // e2-e4 clears e2 for the white bishop.
        play(game, 1, 0, 2, 0); // a7-a6
        play(game, 7, 5, 6, 4); // Bf1-e2 clears f1.
        play(game, 2, 0, 3, 0); // a6-a5

        check(!game.isInCheck(ChessGame.Side.WHITE), "white king is not yet in check");
        check(!game.legalMovesFrom(7, 4).contains(move(7, 4, 7, 6)),
                "white cannot castle onto attacked g1");
        check(!game.makeMove(move(7, 4, 7, 6)), "castling into check must be rejected");
        assertPiece(game, 7, 4, ChessGame.Side.WHITE, ChessGame.PieceType.KING);
        assertPiece(game, 7, 7, ChessGame.Side.WHITE, ChessGame.PieceType.ROOK);
    }

    private static void promotionChoices() {
        ChessGame game = new ChessGame();
        play(game, 6, 1, 4, 1); // b2-b4
        play(game, 1, 0, 3, 0); // a7-a5
        play(game, 4, 1, 3, 0); // b4xa5
        play(game, 1, 1, 2, 1); // b7-b6
        play(game, 3, 0, 2, 1); // a5xb6
        play(game, 1, 6, 2, 6); // g7-g6
        play(game, 2, 1, 1, 1); // b6-b7
        play(game, 2, 6, 3, 6); // g6-g5

        ChessGame.PieceType[] choices = {
                ChessGame.PieceType.QUEEN, ChessGame.PieceType.ROOK,
                ChessGame.PieceType.BISHOP, ChessGame.PieceType.KNIGHT
        };
        for (ChessGame.PieceType choice : choices) {
            check(game.legalMovesFrom(1, 1).contains(
                    new ChessGame.Move(1, 1, 0, 0, choice)),
                    "promotion to " + choice + " must be legal");
        }
        check(!game.makeMove(move(1, 1, 0, 0)),
                "promotion without a chosen piece must be rejected");
        check(game.makeMove(new ChessGame.Move(1, 1, 0, 0, ChessGame.PieceType.QUEEN)),
                "chosen queen promotion must succeed");
        assertPiece(game, 0, 0, ChessGame.Side.WHITE, ChessGame.PieceType.QUEEN);
        check(game.pieceAt(1, 1) == null, "promoted pawn must leave b7");
    }

    private static ChessGame.Move move(int fromRow, int fromCol, int toRow, int toCol) {
        return new ChessGame.Move(fromRow, fromCol, toRow, toCol, null);
    }

    private static void play(ChessGame game, int fromRow, int fromCol, int toRow, int toCol) {
        ChessGame.Move move = move(fromRow, fromCol, toRow, toCol);
        List<ChessGame.Move> legalMoves = game.legalMovesFrom(fromRow, fromCol);
        check(legalMoves.contains(move), "expected legal move " + moveDescription(move));
        check(game.makeMove(move), "could not make legal move " + moveDescription(move));
    }

    private static void assertPiece(ChessGame game, int row, int col,
                                    ChessGame.Side side, ChessGame.PieceType type) {
        ChessGame.Piece piece = game.pieceAt(row, col);
        check(piece != null && piece.side == side && piece.type == type,
                "expected " + side + " " + type + " at (" + row + ", " + col + ")");
    }

    private static String moveDescription(ChessGame.Move move) {
        return "(" + move.fromRow + ", " + move.fromCol + ") to ("
                + move.toRow + ", " + move.toCol + ")";
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
            testsRun++;
            System.out.println("PASS " + name);
        } catch (AssertionError error) {
            throw new AssertionError("FAIL " + name + ": " + error.getMessage(), error);
        }
    }
}
