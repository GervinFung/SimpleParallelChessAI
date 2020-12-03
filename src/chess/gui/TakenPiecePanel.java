package chess.gui;

import chess.engine.board.Move;
import chess.engine.pieces.Piece;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.border.EtchedBorder;
import javax.swing.JLabel;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;

import static chess.gui.Table.*;

public final class TakenPiecePanel extends JPanel {

    private static final EtchedBorder PANEL_BORDER = new EtchedBorder(EtchedBorder.RAISED);

    private final JPanel northPanel, southPanel;

    private static final Dimension TAKEN_PIECE_DIMENSION = new Dimension(40, 80);

    public TakenPiecePanel() {
        super(new BorderLayout());
        this.setBorder(PANEL_BORDER);
        GridLayout gridLayout = new GridLayout(8, 2);
        this.northPanel = new JPanel(gridLayout);
        this.southPanel = new JPanel(gridLayout);
        this.add(this.northPanel, BorderLayout.NORTH);
        this.add(this.southPanel, BorderLayout.SOUTH);
        this.setPreferredSize(TAKEN_PIECE_DIMENSION);
    }

    public void redo(final MoveLog moveLog) {

        this.southPanel.removeAll();
        this.northPanel.removeAll();

        final List<Piece> whiteTakenPieces = new ArrayList<>();
        final List<Piece> blackTakenPieces = new ArrayList<>();

        for (final Move move: moveLog.getMoves()) {

            if (move.isAttack()) {
                final Piece takenPiece = move.getAttackedPiece();
                if (takenPiece.getLeague().isWhite()) {
                    whiteTakenPieces.add(takenPiece);
                }else if (takenPiece.getLeague().isBlack()){
                    blackTakenPieces.add(takenPiece);
                }else {
                    throw new RuntimeException("should not instantiate me");
                }
            }
        }

        whiteTakenPieces.sort(Comparator.comparingInt(Piece::getPieceValue));

        blackTakenPieces.sort(Comparator.comparingInt(Piece::getPieceValue));


        String defaultPieceIconPath = "image/chessPieceImages/";
        for (final Piece takenPiece : whiteTakenPieces) {
            final String alliance = takenPiece.getLeague().toString().substring(0, 1);
            final String pieceName = takenPiece.toString() + ".png";
            try {
                final BufferedImage image = ImageIO.read(new File(defaultPieceIconPath + alliance + pieceName));
                final ImageIcon icon = new ImageIcon(image);
                final JLabel imageLabel = new JLabel(new ImageIcon(resizeImage(icon)));
                this.northPanel.add(imageLabel);
            } catch (final IOException ignored) { }
        }

        for (final Piece takenPiece : blackTakenPieces) {
            final String alliance = takenPiece.getLeague().toString().substring(0, 1);
            final String pieceName = takenPiece.toString() + ".png";
            try {
                final BufferedImage image = ImageIO.read(new File(defaultPieceIconPath + alliance + pieceName));
                final ImageIcon icon = new ImageIcon(image);
                final JLabel imageLabel = new JLabel(new ImageIcon(resizeImage(icon)));
                this.southPanel.add(imageLabel);
            } catch (final IOException ignored) { }
        }
        validate();
    }

    private Image resizeImage(final ImageIcon icon) {
        return icon.getImage().getScaledInstance(icon.getIconWidth() - 35, icon.getIconWidth() - 35, Image.SCALE_SMOOTH);
    }
}
