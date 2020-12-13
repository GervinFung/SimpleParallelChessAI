package chess.engine.pieces;

import chess.engine.League;
import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.engine.player.MoveTransition;

import java.util.Collection;
import static chess.engine.board.Move.*;

public abstract class Piece {

    protected final PieceType pieceType;
    protected final int piecePosition;
    protected final League league;
    private final boolean isFirstMove;
    private final int cachedHashCode;
    private int display;

    public Piece(final PieceType pieceType, final int piecePosition, final League league, final boolean isFirstMove) {
        this.pieceType = pieceType;
        this.piecePosition = piecePosition;
        this.league = league;
        this.isFirstMove = isFirstMove;
        this.cachedHashCode = computeCachedHasCode();
        this.display = piecePosition;
    }

    public int getDisplay() {
        return this.display;
    }

    public void setDisplay(final int display) {
        this.display = display;
    }

    private int computeCachedHasCode() {
        int result = pieceType.hashCode();
        result = 31 * result + piecePosition;
        result = 31 * result + league.hashCode();
        result = 31 * result + (isFirstMove ? 1 : 0);
        return result;
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }

    @Override
    public boolean equals(final Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof Piece)) {
            return false;
        }

        final Piece otherPiece = (Piece)object;
        return piecePosition == otherPiece.getPiecePosition() && pieceType == otherPiece.getPieceType() &&
                league == otherPiece.getLeague() && isFirstMove == otherPiece.isFirstMove();
    }

    public boolean isFirstMove() {
        return this.isFirstMove;
    }

    public abstract Collection<Move> calculateLegalMoves(final Board board);

    protected boolean isLegalMove(final Board board, final int candidateDestinationCoordinate) {
        try {
            //make a move, if the move is safe, return true, else false
            final MoveTransition moveTransition = board.currentPlayer().makeMove(new MajorMove(board, this, candidateDestinationCoordinate));
            return moveTransition.getMoveStatus().isDone();
        } catch (final RuntimeException e) {
            //for catching null board at the beginning of the game
            return true;
        }
    }

    public abstract Piece movedPiece(final Move move);

    public League getLeague() {
        return this.league;
    }

    public int getPiecePosition() {
        return this.piecePosition;
    }

    public PieceType getPieceType() {
        return this.pieceType;
    }

    public int getPieceValue() {
        return this.pieceType.getPieceValue();
    }
}
