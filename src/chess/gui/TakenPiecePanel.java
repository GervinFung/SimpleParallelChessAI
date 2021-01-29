package chess.gui;

import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.pieces.Piece;

import javax.imageio.ImageIO;
import java.io.IOException;

import java.util.List;
import java.util.HashMap;
import java.util.Comparator;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import java.awt.Image;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Objects;
import java.util.stream.Collectors;

import static chess.gui.Table.*;

public final class TakenPiecePanel extends JPanel {

    private final JPanel northPanel, southPanel;

    public TakenPiecePanel() {
        super(new BorderLayout());
        this.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        final GridLayout gridLayout = new GridLayout(7, 2);
        this.northPanel = new JPanel(gridLayout);
        this.southPanel = new JPanel(gridLayout);
        this.add(this.northPanel, BorderLayout.NORTH);
        this.add(this.southPanel, BorderLayout.SOUTH);
        this.setPreferredSize(new Dimension(60, 80));
    }

    public boolean notContainSamePiece(final HashMap<Piece, Integer> takenPieces, final Piece takenPiece) {
        for (final Piece piece : takenPieces.keySet()) {
            if (takenPiece.toString().equals(piece.toString())) {
                final int quantity = takenPieces.get(piece) + 1;
                takenPieces.remove(piece);
                takenPieces.put(takenPiece, quantity);
                return false;
            }
        }
        return true;
    }

    public void redo(final MoveLog moveLog) {

        final HashMap<Piece, Integer> whiteTakenPieces = new HashMap<>();
        final HashMap<Piece, Integer> blackTakenPieces = new HashMap<>();

        for (final Move move: moveLog.getMoves()) {

            if (move.isAttack()) {
                final Piece takenPiece = move.getAttackedPiece();
                if (takenPiece.getLeague().isWhite()) {
                    if (this.notContainSamePiece(whiteTakenPieces, takenPiece)) {
                        whiteTakenPieces.put(takenPiece, 1);
                    }
                }else if (takenPiece.getLeague().isBlack()){
                    if (this.notContainSamePiece(blackTakenPieces, takenPiece)) {
                        blackTakenPieces.put(takenPiece, 1);
                    }
                }else {
                    throw new RuntimeException("should not instantiate me");
                }
            }
        }

        this.addTakenPiece(whiteTakenPieces, this.northPanel);
        this.addTakenPiece(blackTakenPieces, this.southPanel);

        this.validate();
    }

    private void addTakenPiece(final HashMap<Piece, Integer> takenPiecesMap, final JPanel takenPiecePanel) {
        final List<Piece> takenPieces = takenPiecesMap.keySet().stream().sorted(Comparator.comparingInt(Piece::getPieceValue)).collect(Collectors.toList());
        takenPiecePanel.removeAll();
        for (final Piece takenPiece : takenPieces) {
            try {
                final Image image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource(BoardUtils.imagePath(takenPiece))));
                final JLabel imageLabel = new JLabel(Integer.toString(takenPiecesMap.get(takenPiece)), new ImageIcon(resizeImage(image)), SwingConstants.LEADING);
                takenPiecePanel.add(imageLabel);
            } catch (final IOException | NullPointerException e) { System.err.println("Invalid Path"); }
        }
    }

    private Image resizeImage(final Image image) {
        return image.getScaledInstance(image.getWidth(null) / 2, image.getHeight(null) / 2, Image.SCALE_SMOOTH);
    }
}