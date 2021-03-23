package chess.gui;

import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.pieces.Piece;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.Image;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public final class PawnPromotionPane {

    private ImageIcon[] pawnPromotionInterface(final List<Piece> getPromotionPieces) {
        final ImageIcon[] icons = new ImageIcon[4];
        for (int i = 0; i < 4; i++) {
            try {
                final Image image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource(BoardUtils.imagePath(getPromotionPieces.get(i)))));
                icons[i] = new ImageIcon(image);
            } catch (final IOException | NullPointerException e) { System.err.println("Invalid Path"); }

        }
        return icons;
    }

    private Piece startPromotion(final Move.PawnPromotion pawnPromotion) {
        final List<Piece> getPromotionPieces = pawnPromotion.getPromotedPawn().getPromotionPieces(pawnPromotion.getDestinationCoordinate());
        final ImageIcon[] icons = pawnPromotionInterface(getPromotionPieces);
        JOptionPane.showMessageDialog(null, "You only have 1 chance to promote your pawn\nChoose wisely");
        while (true) {
            final int promoteOption = JOptionPane.showOptionDialog(null, null, "Pawn Promotion", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, icons, null);
            if (promoteOption >= 0 && promoteOption <= 3) {
                return getPromotionPieces.get(promoteOption);
            }
            JOptionPane.showMessageDialog(null, "You must promote your pawn");
        }
    }

    private Board tempBoard(final Move.PawnPromotion pawnPromotion) {
        final Board.Builder builder = new Board.Builder(pawnPromotion.getBoard().getMoveCount() + 1, pawnPromotion.getBoard().currentPlayer().getLeague(), null)
                .updateWhiteTimer(pawnPromotion.getBoard().whitePlayer().getMinute(), pawnPromotion.getBoard().whitePlayer().getSecond())
                .updateBlackTimer(pawnPromotion.getBoard().blackPlayer().getMinute(), pawnPromotion.getBoard().blackPlayer().getSecond());

        pawnPromotion.getBoard().currentPlayer().getActivePieces().stream().filter(piece -> !pawnPromotion.getPromotedPawn().equals(piece)).forEach(builder::setPiece);
        pawnPromotion.getBoard().currentPlayer().getOpponent().getActivePieces().forEach(builder::setPiece);

        builder.setPiece(pawnPromotion.getPromotedPiece().movedPiece(pawnPromotion));
        return builder.build();
    }

    public void promotePawn(final Table table, final Move.PawnPromotion pawnPromotion) {
        table.updateGameBoard(this.tempBoard(pawnPromotion));
        final Board.Builder builder = new Board.Builder(pawnPromotion.getBoard().getMoveCount() + 1, pawnPromotion.getBoard().currentPlayer().getOpponent().getLeague(), null)
                .updateWhiteTimer(pawnPromotion.getBoard().whitePlayer().getMinute(), pawnPromotion.getBoard().whitePlayer().getSecond())
                .updateBlackTimer(pawnPromotion.getBoard().blackPlayer().getMinute(), pawnPromotion.getBoard().blackPlayer().getSecond());

        pawnPromotion.getBoard().currentPlayer().getActivePieces().stream().filter(piece -> !pawnPromotion.getPromotedPawn().equals(piece)).forEach(builder::setPiece);
        pawnPromotion.getBoard().currentPlayer().getOpponent().getActivePieces().forEach(builder::setPiece);

        pawnPromotion.setPromotedPiece(startPromotion(pawnPromotion));
        builder.setPiece(pawnPromotion.getPromotedPiece().movedPiece(pawnPromotion));
        builder.setTransitionMove(pawnPromotion);
        table.updateGameBoard(builder.build());
    }
}