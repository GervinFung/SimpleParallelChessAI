package chess.engine.player;

import chess.engine.League;
import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.engine.board.Tile;
import chess.engine.pieces.Piece;
import chess.engine.pieces.Rook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static chess.engine.board.Move.*;

public final class BlackPlayer extends Player{
    public BlackPlayer(final Board board, final Collection<Move> whiteStandardLegalMoves, final Collection<Move> blackStandardLegalMoves, final int minute, final int second) { super(board, blackStandardLegalMoves, whiteStandardLegalMoves, minute, second); }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getBlackPieces();
    }

    @Override
    public League getLeague() {
        return League.BLACK;
    }

    @Override
    public Player getOpponent() {
        return this.board.whitePlayer();
    }

    @Override
    public String toString() {
        return "Black";
    }

    @Override
    protected Collection<Move> calculateKingCastles(final Collection<Move> opponentLegals) {

        if (this.isCastled()) { return Collections.emptyList(); }

        final List<Move> kingCastles = new ArrayList<>();

        if (this.playerKing.isFirstMove() && !this.isInCheck()) {
            //blacks king side castle
            if (!this.board.getTile(5).isTileOccupied() && !this.board.getTile(6).isTileOccupied()) {
                final Tile rookTile = this.board.getTile(7);
                if (rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove()) {
                    if (Player.calculateAttacksOnTile(5, opponentLegals).isEmpty() &&
                            Player.calculateAttacksOnTile(6, opponentLegals).isEmpty() &&
                            rookTile.getPiece() instanceof Rook) {
                        kingCastles.add(new KingSideCastleMove(this.board, this.playerKing, 6, (Rook) rookTile.getPiece(), rookTile.getTileCoordinate(), 5));
                    }
                }
            }
            //blacks queens side castle
            if (!this.board.getTile(1).isTileOccupied() &&
                    !this.board.getTile(2).isTileOccupied() &&
                    !this.board.getTile(3).isTileOccupied()) {
                final Tile rookTile = this.board.getTile(0);
                if (rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove() &&
                        Player.calculateAttacksOnTile(2, opponentLegals).isEmpty() &&
                        Player.calculateAttacksOnTile(3, opponentLegals).isEmpty() &&
                        rookTile.getPiece() instanceof Rook) {
                    kingCastles.add(new QueenSideCastleMove(this.board, this.playerKing, 2, (Rook) rookTile.getPiece(), rookTile.getTileCoordinate(), 3));
                }
            }
        }
        return Collections.unmodifiableList(kingCastles);
    }
}