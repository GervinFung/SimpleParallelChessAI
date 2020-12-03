package chess.engine.pieces;

import chess.engine.League;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static chess.engine.board.Move.*;

public final class Pawn extends Piece {

    private static final int[] MOVE_VECTOR_COORDINATE = {8, 16, 7, 9};

    public Pawn(final League pieceCOLOR, final int piecePosition) {
        super(PieceType.PAWN, piecePosition, pieceCOLOR, true);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        for (final int currentCandidateOFFSET : MOVE_VECTOR_COORDINATE) {

            int candidateDestinationCoordinate = this.piecePosition + (currentCandidateOFFSET * this.getLeague().getDirection());

            if (!BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
                continue;
            }
            if (currentCandidateOFFSET == 8 && !board.getTile(candidateDestinationCoordinate).isTileOccupied()) {

                if (this.league.isPawnPromotionSquare(candidateDestinationCoordinate) && this.isLegalMove(board, candidateDestinationCoordinate)) {

                    legalMoves.add(new PawnPromotion(new PawnMove(board, this, candidateDestinationCoordinate), new Queen(this.league, candidateDestinationCoordinate, false)));
                    legalMoves.add(new PawnPromotion(new PawnMove(board, this, candidateDestinationCoordinate), new Rook(this.league, candidateDestinationCoordinate, false)));
                    legalMoves.add(new PawnPromotion(new PawnMove(board, this, candidateDestinationCoordinate), new Bishop(this.league, candidateDestinationCoordinate, false)));
                    legalMoves.add(new PawnPromotion(new PawnMove(board, this, candidateDestinationCoordinate), new Knight(this.league, candidateDestinationCoordinate, false)));

                } else if (!this.league.isPawnPromotionSquare(candidateDestinationCoordinate) && this.isLegalMove(board, candidateDestinationCoordinate)){
                    legalMoves.add(new PawnMove(board, this, candidateDestinationCoordinate));
                }


            } else if (currentCandidateOFFSET == 16 && this.isFirstMove() &&
                    ((BoardUtils.SECOND_ROW[this.piecePosition] && this.getLeague().isBlack()) ||
                            (BoardUtils.SEVENTH_ROW[this.piecePosition] && this.getLeague().isWhite()))) {

                final int behindCandidateDestinationCoordinate = this.piecePosition + (this.getLeague().getDirection() * 8);
                if (!board.getTile(behindCandidateDestinationCoordinate).isTileOccupied() &&
                        !board.getTile(candidateDestinationCoordinate).isTileOccupied() &&
                        this.isLegalMove(board, candidateDestinationCoordinate)) {
                    legalMoves.add(new PawnJump(board, this, candidateDestinationCoordinate));
                }
            } else if (currentCandidateOFFSET == 7 &&
                    !((BoardUtils.EIGHTH_COLUMN[this.piecePosition] && this.league.isWhite()) ||
                            (BoardUtils.FIRST_COLUMN[this.piecePosition] && this.league.isBlack()))) {
                if (board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                    final Piece pieceDestination = board.getTile(candidateDestinationCoordinate).getPiece();

                    if (this.league != pieceDestination.getLeague() && this.isLegalMove(board, candidateDestinationCoordinate)) {
                        if (this.league.isPawnPromotionSquare(candidateDestinationCoordinate)) {

                            legalMoves.add(new PawnPromotion(new PawnAttackMove(board, this, candidateDestinationCoordinate, pieceDestination), new Queen(this.league, candidateDestinationCoordinate, false)));
                            legalMoves.add(new PawnPromotion(new PawnAttackMove(board, this, candidateDestinationCoordinate, pieceDestination), new Rook(this.league, candidateDestinationCoordinate, false)));
                            legalMoves.add(new PawnPromotion(new PawnAttackMove(board, this, candidateDestinationCoordinate, pieceDestination), new Bishop(this.league, candidateDestinationCoordinate, false)));
                            legalMoves.add(new PawnPromotion(new PawnAttackMove(board, this, candidateDestinationCoordinate, pieceDestination), new Knight(this.league, candidateDestinationCoordinate, false)));

                        } else {
                            legalMoves.add(new PawnAttackMove(board, this, candidateDestinationCoordinate, pieceDestination));
                        }

                    }
                } else if (board.getEnPassantPawn() != null) {

                    if (board.getEnPassantPawn().getPiecePosition() == (this.piecePosition + (this.league.getOppositeDirection()))) {
                        final Piece pieceDestination = board.getEnPassantPawn();

                        if (this.league != pieceDestination.getLeague() && this.isLegalMove(board, candidateDestinationCoordinate)) {
                            legalMoves.add(new PawnEnPassantAttackMove(board, this, candidateDestinationCoordinate, pieceDestination));
                        }
                    }
                }

            } else if (currentCandidateOFFSET == 9 &&
                    !((BoardUtils.FIRST_COLUMN[this.piecePosition] && this.league.isWhite()) ||
                            (BoardUtils.EIGHTH_COLUMN[this.piecePosition] && this.league.isBlack()))) {
                if (board.getTile(candidateDestinationCoordinate).isTileOccupied()) {

                    final Piece pieceDestination = board.getTile(candidateDestinationCoordinate).getPiece();

                    if (this.league != pieceDestination.getLeague() && this.isLegalMove(board, candidateDestinationCoordinate)) {

                        if (this.league.isPawnPromotionSquare(candidateDestinationCoordinate)) {

                            legalMoves.add(new PawnPromotion(new PawnAttackMove(board, this, candidateDestinationCoordinate, pieceDestination), new Queen(this.league, candidateDestinationCoordinate, false)));
                            legalMoves.add(new PawnPromotion(new PawnAttackMove(board, this, candidateDestinationCoordinate, pieceDestination), new Rook(this.league, candidateDestinationCoordinate, false)));
                            legalMoves.add(new PawnPromotion(new PawnAttackMove(board, this, candidateDestinationCoordinate, pieceDestination), new Bishop(this.league, candidateDestinationCoordinate, false)));
                            legalMoves.add(new PawnPromotion(new PawnAttackMove(board, this, candidateDestinationCoordinate, pieceDestination), new Knight(this.league, candidateDestinationCoordinate, false)));

                        } else {
                            legalMoves.add(new PawnAttackMove(board, this, candidateDestinationCoordinate, pieceDestination));
                        }
                    }
                } else if (board.getEnPassantPawn() != null) {

                    if (board.getEnPassantPawn().getPiecePosition() == (this.piecePosition - (this.league.getOppositeDirection()))) {
                        final Piece pieceDestination = board.getEnPassantPawn();

                        if (this.league != pieceDestination.getLeague() && this.isLegalMove(board, candidateDestinationCoordinate)) {
                            legalMoves.add(new PawnEnPassantAttackMove(board, this, candidateDestinationCoordinate, pieceDestination));
                        }
                    }
                }

            }
        }
        return Collections.unmodifiableList(legalMoves);
    }

    @Override
    public Pawn movedPiece(final Move move) {
        return new Pawn(move.getMovedPiece().getLeague(), move.getDestinationCoordinate());
    }

    @Override
    public String toString() {
        return PieceType.PAWN.toString();
    }

    public List<Piece> getPromotionPieces(final int destinationCoordinate) {
        final List<Piece> promotionPieces = new ArrayList<>();
        promotionPieces.add(new Queen(this.league, destinationCoordinate, false));
        promotionPieces.add(new Rook(this.league, destinationCoordinate, false));
        promotionPieces.add(new Bishop(this.league, destinationCoordinate, false));
        promotionPieces.add(new Knight(this.league, destinationCoordinate, false));
        return Collections.unmodifiableList(promotionPieces);
    }
}