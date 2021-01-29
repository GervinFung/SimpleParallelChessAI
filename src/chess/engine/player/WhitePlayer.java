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

public final class WhitePlayer extends Player{
    public WhitePlayer(final Board board, final Collection<Move> whiteStandardLegalMoves, final Collection<Move> blackStandardLegalMoves, final int minute, final int second) { super(board, whiteStandardLegalMoves, blackStandardLegalMoves, minute, second); }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getWhitePieces();
    }

    @Override
    public League getLeague() {
        return League.WHITE;
    }

    @Override
    public Player getOpponent() {
        return this.board.blackPlayer();
    }

    @Override
    public String toString() {
        return "White";
    }

    @Override
    protected Collection<Move> calculateKingCastles(final Collection<Move> opponentLegals) {

        if (this.isCastled()) { return Collections.emptyList(); }

        final List<Move> kingCastles = new ArrayList<>();

        if (this.playerKing.isFirstMove() && !this.isInCheck()) {
            //white's king side castle
            if (!this.board.getTile(61).isTileOccupied() && !this.board.getTile(62).isTileOccupied()) {
                final Tile rookTile = this.board.getTile(63);
                if (rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove()) {
                    if (Player.calculateAttacksOnTile(61, opponentLegals).isEmpty() &&
                            Player.calculateAttacksOnTile(62, opponentLegals).isEmpty() &&
                            rookTile.getPiece() instanceof Rook) {
                        kingCastles.add(new KingSideCastleMove(this.board, this.playerKing, 62, (Rook) rookTile.getPiece(), rookTile.getTileCoordinate(), 61));
                    }

                }
            }
            if (!this.board.getTile(59).isTileOccupied() &&
                    !this.board.getTile(58).isTileOccupied() &&
                    !this.board.getTile(57).isTileOccupied()) {
                final Tile rookTile = this.board.getTile(59);
                if (rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove() &&
                        Player.calculateAttacksOnTile(58, opponentLegals).isEmpty() &&
                        Player.calculateAttacksOnTile(59, opponentLegals).isEmpty() &&
                        rookTile.getPiece() instanceof Rook) {
                    kingCastles.add(new QueenSideCastleMove(this.board, this.playerKing, 58, (Rook) rookTile.getPiece(), rookTile.getTileCoordinate(), 59));
                }
            }

        }

        return Collections.unmodifiableList(kingCastles);
    }
}