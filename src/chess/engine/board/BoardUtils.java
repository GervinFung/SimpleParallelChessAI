package chess.engine.board;

import chess.engine.pieces.Piece;
import chess.engine.pieces.PieceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static chess.engine.board.Move.MoveFactory;

public final class BoardUtils {

    public static final List<Boolean> FIRST_COLUMN = initColumn(0);
    public static final List<Boolean> SECOND_COLUMN = initColumn(1);
    public static final List<Boolean> SEVENTH_COLUMN = initColumn(6);
    public static final List<Boolean> EIGHTH_COLUMN = initColumn(7);

    public static final List<Boolean> FIRST_ROW = initRow(0);
    public static final List<Boolean> SECOND_ROW = initRow(8);
    public static final List<Boolean> THIRD_ROW = initRow(16);
    public static final List<Boolean> FIFTH_ROW = initRow(32);
    public static final List<Boolean> SEVENTH_ROW = initRow(48);
    public static final List<Boolean> EIGHTH_ROW = initRow(56);

    public static final List<String> ALGEBRAIC_NOTATION = initializeAlgebraicNotation();

    private static List<String> initializeAlgebraicNotation() {
        return List.of(
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
                "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
                "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
                "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
                "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
                "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1");
    }


    public static final int NUM_TILES = 64;
    public static final int NUM_TILES_PER_ROW = 8;

    private static List<Boolean> initColumn(int columnNumber) {
        final boolean[] columns = new boolean[NUM_TILES];
        do {
            columns[columnNumber] = true;
            columnNumber += NUM_TILES_PER_ROW;
        } while (columnNumber < NUM_TILES);
        final List<Boolean> columnList = new ArrayList<>(NUM_TILES);
        for (final boolean column : columns) {
            columnList.add(column);
        }
        return Collections.unmodifiableList(columnList);
    }

    private static List<Boolean> initRow(int rowNumber) {
        final boolean[] rows = new boolean[NUM_TILES];
        do {
            rows[rowNumber] = true;
            rowNumber ++;
        } while (rowNumber % NUM_TILES_PER_ROW != 0);
        final List<Boolean> rowList = new ArrayList<>(NUM_TILES);
        for (final boolean row : rows) {
            rowList.add(row);
        }
        return Collections.unmodifiableList(rowList);
    }

    private BoardUtils() {
        throw new RuntimeException("You cannot instantiate BoardUtils");
    }

    public static boolean isValidTileCoordinate(final int coordinate) { return coordinate >= 0 && coordinate < NUM_TILES; }

    public static String getPositionAtCoordinate(final int destinationCoordinate) { return ALGEBRAIC_NOTATION.get(destinationCoordinate); }

    public static int mostValuableVictimLeastValuableAggressor(final Move move) {
        final Piece movingPiece = move.getMovedPiece();
        if(move.isAttack()) {
            final Piece attackedPiece = move.getAttackedPiece();
            return (attackedPiece.getPieceValue() - movingPiece.getPieceValue() +  PieceType.KING.getPieceValue()) * 100;
        }
        return PieceType.KING.getPieceValue() - movingPiece.getPieceValue();
    }

    public static List<Move> lastNMoves(final Board board, int N) {
        final List<Move> moveHistory = new ArrayList<>();
        Move currentMove = board.getTransitionMove();
        int i = 0;
        while(currentMove != MoveFactory.getNullMove() && i < N) {
            moveHistory.add(currentMove);
            currentMove = currentMove.getBoard().getTransitionMove();
            i++;
        }
        return Collections.unmodifiableList(moveHistory);
    }

    public static boolean kingThreat(final Move move) { return move.getBoard().currentPlayer().makeMove(move).getLatestBoard().currentPlayer().isInCheck(); }

    public static boolean isEndGameScenario(final Board board) { return board.currentPlayer().isInCheckmate() || board.currentPlayer().isInStalemate(); }
}