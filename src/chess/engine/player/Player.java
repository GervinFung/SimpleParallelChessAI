package chess.engine.player;

import chess.engine.League;
import chess.engine.board.Board;
import chess.engine.board.MoveTransition;
import chess.engine.board.MoveStatus;
import chess.engine.board.Move;
import chess.engine.board.Tile;
import chess.engine.pieces.King;
import chess.engine.pieces.Piece;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.collectingAndThen;

public abstract class Player {

    protected final Board board;
    protected final King playerKing;
    protected final Collection<Move> legalMoves;
    private final boolean isInCheck;
    private int minute, second;

    public Player(final Board board, final Collection<Move> legalMoves, final Collection<Move> opponentLegalMoves, final int minute, final int second) {
        this.board = board;
        this.playerKing = this.establishKing();
        final List<Move> legal = new ArrayList<>(legalMoves);
        this.isInCheck = !Player.calculateAttacksOnTile(this.playerKing.getPiecePosition(), opponentLegalMoves).isEmpty();
        //for ai
        legal.addAll(calculateKingCastles(opponentLegalMoves));
        this.legalMoves = legal;
        this.minute = minute;
        this.second = second;
    }

    public final void countDown() {
        if (this.second == 0) {
            if (this.minute == 0) {
                return;
            }
            this.second = 60;
            this.minute -= 1;
        }
        this.second -= 1;
    }

    public final int getMinute() { return this.minute; }

    public final int getSecond() { return this.second; }

    public final King getPlayerKing() {
        return this.playerKing;
    }

    public final Collection<Move> getLegalMoves() {
        return Collections.unmodifiableCollection(this.legalMoves);
    }

    public static List<Move> calculateAttacksOnTile(final int piecePosition, final Collection<Move> moves) { return moves.stream().filter(move -> move.getDestinationCoordinate() == piecePosition).collect(collectingAndThen(Collectors.toList(), Collections::unmodifiableList)); }

    private King establishKing() { return (King) getActivePieces().stream().filter(piece -> piece.getPieceType().isKing()).findAny().orElseThrow(RuntimeException::new); }

    public abstract Collection<Piece> getActivePieces();

    public abstract League getLeague();

    public abstract Player getOpponent();

    public final boolean isInCheck() {
        return this.isInCheck;
    }

    public final boolean isInCheckmate() {
        return this.isInCheck && this.noEscapeMoves();
    }

    public final boolean isInStalemate() {
        return !this.isInCheck && this.noEscapeMoves();
    }

    public final boolean isTimeOut() { return this.minute == 0 && this.second == 0; }

    protected abstract Collection<Move> calculateKingCastles(final Collection<Move> opponentLegals);

    public final boolean isCastled() {
        return this.playerKing.isCastled();
    }

    public final boolean isKingSideCastleCapable() {
        final Tile rookTile = board.getTile(this.getLeague().isWhite() ? 63 : 7);
        if (!rookTile.isTileOccupied() || this.playerKing.isCastled()) {
            return false;
        }
        return rookTile.getPiece().isFirstMove();
    }

    public final boolean isQueenSideCastleCapable() {
        final Tile rookTile = board.getTile(this.getLeague().isWhite() ? 56 : 0);
        if (!rookTile.isTileOccupied() || this.playerKing.isCastled()) {
            return false;
        }
        return rookTile.getPiece().isFirstMove();
    }

    protected final boolean noEscapeMoves() { return this.legalMoves.stream().noneMatch(move -> makeMove(move).getMoveStatus().isDone()); }

    public final MoveTransition makeMove(final Move move) {

        final Board transitionBoard = move.execute();
        if (transitionBoard != null) {
            final Collection<Move> currentPlayerLegals = transitionBoard.currentPlayer().getLegalMoves();
            final List<Move> kingAttacks = Player.calculateAttacksOnTile(transitionBoard.currentPlayer().getOpponent().getPlayerKing().getPiecePosition(), currentPlayerLegals);

            if (!kingAttacks.isEmpty()) {
                return new MoveTransition(this.board, this.board, MoveStatus.LEAVES_PLAYER_IN_CHECK);
            }

            return new MoveTransition(transitionBoard, this.board, MoveStatus.DONE);
        }
        return new MoveTransition(null, null, MoveStatus.Illegal_Move);
    }

    public MoveTransition undoMove(final Move move) { return new MoveTransition(this.board, move.undo(), MoveStatus.DONE); }
}