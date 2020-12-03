package chess.engine.player;

import chess.engine.League;
import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.engine.board.Tile;
import chess.engine.pieces.King;
import chess.engine.pieces.Piece;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public abstract class Player {

    protected final Board board;
    protected final King playerKing;
    protected final Collection<Move> legalMoves;
    private final boolean isInCheck;

    public Player(final Board board, final Collection<Move> legalMoves, final Collection<Move> opponentLegalMoves) {
        this.board = board;
        this.playerKing = establishKing();
        final List<Move> legal = new ArrayList<>(legalMoves);
        assert this.playerKing != null;
        this.isInCheck = !Player.calculateAttacksOnTile(this.playerKing.getPiecePosition(), opponentLegalMoves).isEmpty();
        //for ai
        legal.addAll(calculateKingCastles(opponentLegalMoves));
        this.legalMoves = legal;
    }

    public King getPlayerKing() {
        return this.playerKing;
    }

    public Collection<Move> getLegalMoves() {
        return Collections.unmodifiableCollection(this.legalMoves);
    }

    public static List<Move> calculateAttacksOnTile(final int piecePosition, final Collection<Move> moves) {
        final List<Move> attackMove = new ArrayList<>();

        for (final Move move : moves) {
            if(piecePosition == move.getDestinationCoordinate()) {
                attackMove.add(move);
            }
        }
        return Collections.unmodifiableList(attackMove);
    }

    private King establishKing() {
        for (final Piece piece : getActivePieces()) {
            if (piece.getPieceType().isKing()) {
                return (King) piece;
            }
        }
        //throw new RuntimeException("Invalid board");
        return null;
    }

    public abstract Collection<Piece> getActivePieces();

    public abstract League getLeague();

    public abstract Player getOpponent();

    public boolean isInCheck() {
        return this.isInCheck;
    }

    public boolean isInCheckmate() {
        return this.isInCheck && !hasEscapeMoves();
    }

    public boolean isInStalemate() {
        return !this.isInCheck && !hasEscapeMoves();
    }

    protected abstract Collection<Move> calculateKingCastles(final Collection<Move> opponentLegals);

    public boolean isCastled() {
        return this.playerKing.isCastled();
    }

    public boolean isKingSideCastleCapable() {
        final Tile rookTile = board.getTile(this.getLeague().isWhite() ? 63 : 7);
        if (!rookTile.isTileOccupied() || this.playerKing.isCastled()) {
            return false;
        }
        return rookTile.getPiece().isFirstMove();
    }

    public boolean isQueenSideCastleCapable() {
        final Tile rookTile = board.getTile(this.getLeague().isWhite() ? 56 : 0);
        if (!rookTile.isTileOccupied() || this.playerKing.isCastled()) {
            return false;
        }
        return rookTile.getPiece().isFirstMove();
    }

    protected boolean hasEscapeMoves() {

        for (final Move move : this.legalMoves) {
            final MoveTransition transition = makeMove(move);

            if (transition.getMoveStatus().isDone()) {
                return true;
            }
        }
        return false;
    }

    public MoveTransition makeMove(final Move move) {

        final Board transitionBoard = move.execute();
        if (transitionBoard != null) {
            final Collection<Move> currentPlayerLegals = transitionBoard.currentPlayer().getLegalMoves();
            final List<Move> kingAttacks = Player.calculateAttacksOnTile(transitionBoard.currentPlayer().getOpponent().getPlayerKing().getPiecePosition(), currentPlayerLegals);

            if (!kingAttacks.isEmpty()) {
                return new MoveTransition(this.board, MoveStatus.LEAVES_PLAYER_IN_CHECK);
            }

            return new MoveTransition(transitionBoard, MoveStatus.DONE);
        }
        return new MoveTransition(null, MoveStatus.Illegal_Move);
    }
}