package chess.engine.board;

import chess.engine.pieces.Pawn;
import chess.engine.pieces.Piece;
import chess.engine.pieces.Rook;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static chess.engine.board.Board.*;

public abstract class Move {

    protected final Board board;
    protected final Piece movePiece;
    protected final int destinationCoordinate;
    protected final boolean isFirstMove;

    private Move(final Board board, final Piece movePiece, final int destinationCoordinate) {
        this.board = board;
        this.movePiece = movePiece;
        this.destinationCoordinate = destinationCoordinate;
        this.isFirstMove = movePiece.isFirstMove();
    }

    private Move(final Board board, final int destinationCoordinate) {
        this.board = board;
        this.destinationCoordinate = destinationCoordinate;
        this.movePiece = null;
        this.isFirstMove = false;
    }
    @Override
    public int hashCode() { return Objects.hash(this.destinationCoordinate, this.movePiece.hashCode(), this.movePiece.getPiecePosition()); }

    @Override
    public boolean equals(final Object object) {

        if (this == object) { return true; }

        if (!(object instanceof Move)) { return false; }

        final Move otherMove = (Move) object;
        return getCurrentCoordinate() == otherMove.getCurrentCoordinate() &&
                getDestinationCoordinate() == otherMove.getDestinationCoordinate() &&
                getMovedPiece().equals(otherMove.getMovedPiece());
    }


    public final Board getBoard() {
        return this.board;
    }

    public int getCurrentCoordinate() {
        return this.getMovedPiece().getPiecePosition();
    }

    public final int getDestinationCoordinate() {
        return this.destinationCoordinate;
    }

    public final Piece getMovedPiece() {
        return this.movePiece;
    }

    public boolean isAttack() {
        return false;
    }

    public boolean isCastlingMove() { return false; }

    public Piece getAttackedPiece() {
        return null;
    }

    public Board execute() {

        final Builder builder = new Builder(this.board.getMoveCount() + 1, this.board.currentPlayer().getOpponent().getLeague(), null);

        this.board.currentPlayer().getActivePieces().stream().filter(piece -> !this.movePiece.equals(piece)).forEach(builder::setPiece);
        this.board.currentPlayer().getOpponent().getActivePieces().forEach(builder::setPiece);

        builder.setPiece(this.movePiece.movedPiece(this));

        return builder.build();
    }

    public Board undo() {
        final Builder builder = new Builder(this.board.getMoveCount() - 1, this.board.currentPlayer().getLeague(), null);
        this.board.getAllPieces().forEach(builder::setPiece);
        return builder.build();
    }

    public static final class MajorMove extends Move {

        public MajorMove(final Board board, final Piece movePiece, final int destinationCoordinate) {
            super(board, movePiece, destinationCoordinate);
        }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof  MajorMove && super.equals(object); }

        @Override
        public String toString() { return getMovedPiece().getPieceType().toString() + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate); }
    }

    public static class AttackMove extends Move {

        private final Piece attackedPiece;

        public AttackMove(final Board board, final Piece movePiece, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movePiece, destinationCoordinate);
            this.attackedPiece = attackedPiece;
        }

        @Override
        public int hashCode() { return Objects.hash(this.attackedPiece.hashCode(), super.hashCode()); }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }

            if (!(object instanceof AttackMove)) {
                return false;
            }

            final AttackMove otherAttackMove = (AttackMove)object;
            return super.equals(otherAttackMove) && getAttackedPiece().equals(otherAttackMove.getAttackedPiece());
        }

        @Override
        public boolean isAttack() {
            return true;
        }

        @Override
        public Piece getAttackedPiece() {
            return this.attackedPiece;
        }
    }

    public static final class MajorAttackMove extends AttackMove {
        public MajorAttackMove(final Board board, final Piece piece, final int destinationCoordinate, final Piece pieceAttacked) {
            super(board, piece, destinationCoordinate, pieceAttacked);
        }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof MajorAttackMove && super.equals(object); }

        @Override
        public String toString() { return getMovedPiece().getPieceType() + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate) + "x" + this.getAttackedPiece(); }
    }

    public static final class PawnMove extends Move {

        public PawnMove(final Board board, final Piece movePiece, final int destinationCoordinate) {
            super(board, movePiece, destinationCoordinate);
        }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof  PawnMove && super.equals(object); }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }

    }

    public static class PawnAttackMove extends AttackMove {

        public PawnAttackMove(final Board board, final Piece movePiece, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movePiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof  PawnAttackMove && super.equals(object); }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(this.movePiece.getPiecePosition()).charAt(0) + "x" +
                    BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static final class PawnEnPassantAttackMove extends PawnAttackMove {

        public PawnEnPassantAttackMove(final Board board, final Piece movePiece, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movePiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof PawnEnPassantAttackMove && super.equals(object); }

        @Override
        public Board execute() {
            final Builder builder = new Builder(this.board.getMoveCount() + 1, this.board.currentPlayer().getOpponent().getLeague(), null);

            this.board.currentPlayer().getActivePieces().stream().filter(piece -> !this.movePiece.equals(piece)).forEach(builder::setPiece);
            this.board.currentPlayer().getOpponent().getActivePieces().stream().filter(piece -> !piece.equals(this.getAttackedPiece())).forEach(builder::setPiece);

            builder.setPiece(this.movePiece.movedPiece(this));

            return builder.build();
        }

        @Override
        public Board undo() {
            final Board.Builder builder = new Builder(this.board.getMoveCount() - 1, this.board.currentPlayer().getLeague(), (Pawn)this.getAttackedPiece());
            this.board.getAllPieces().forEach(builder::setPiece);
            return builder.build();
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static final class PawnPromotion extends Move {

        private final Move decoratedMove;
        private final Pawn promotedPawn;
        private Piece promotedPiece;
        private final Piece MinimaxPromotionPiece;

        public PawnPromotion(final Move decoratedMove, final Piece MinimaxPromotionPiece) {
            super(decoratedMove.getBoard(), decoratedMove.getMovedPiece(), decoratedMove.getDestinationCoordinate());
            this.decoratedMove = decoratedMove;
            this.promotedPawn = (Pawn)decoratedMove.getMovedPiece();
            this.MinimaxPromotionPiece = MinimaxPromotionPiece;
        }

        public Move getDecoratedMove() { return this.decoratedMove; }

        public Board promotePawn(final Board board) {
            //promotion take a move, which the move flips player turn after executed, so this should not flip again
            final Builder builder = new Builder(this.board.getMoveCount() + 1, board.currentPlayer().getLeague(), null);

            board.currentPlayer().getActivePieces().stream().filter(piece -> !this.promotedPawn.equals(piece)).forEach(builder::setPiece);
            board.currentPlayer().getOpponent().getActivePieces().forEach(builder::setPiece);

            this.promotedPiece = startPromotion();
            builder.setPiece(this.promotedPiece.movedPiece(this));
            return builder.build();
        }

        @Override
        public Board execute() {

            final Board pawnMoveBoard = this.decoratedMove.execute();
            final Board.Builder builder = new Builder(this.board.getMoveCount() + 1, pawnMoveBoard.currentPlayer().getLeague(), null);

            pawnMoveBoard.currentPlayer().getActivePieces().stream().filter(piece -> !this.promotedPawn.equals(piece)).forEach(builder::setPiece);
            pawnMoveBoard.currentPlayer().getOpponent().getActivePieces().forEach(builder::setPiece);

            this.promotedPiece = this.MinimaxPromotionPiece;
            builder.setPiece(this.MinimaxPromotionPiece.movedPiece(this));
            return builder.build();
        }

        private ImageIcon[] pawnPromotionInterface(final List<Piece> getPromotionPieces) {
            final ImageIcon[] icons = new ImageIcon[4];
            for (int i = 0; i < 4; i++) {
                try {
                    final BufferedImage image = ImageIO.read(new File(imageAbsolutePath(getPromotionPieces.get(i))));
                    icons[i] = new ImageIcon(image);
                } catch (final IOException e) {
                    System.err.println("Image path is invalid");
                }
            }
            return icons;
        }

        private Piece startPromotion() {
            final List<Piece> getPromotionPieces = this.promotedPawn.getPromotionPieces(this.destinationCoordinate);
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

        private String imageAbsolutePath(final Piece promotionPiece) {
            final File imageFile = new File("image/chessPieceImages/");
            final String absolutePieceIconPath = imageFile.getAbsolutePath() + "/";
            final String alliance = promotionPiece.getLeague().toString().substring(0, 1);
            final String pieceName = promotionPiece.toString() + ".png";
            return absolutePieceIconPath + alliance + pieceName;
        }

        @Override
        public boolean isAttack() {
            return this.decoratedMove.isAttack();
        }

        @Override
        public Piece getAttackedPiece() {
            return this.decoratedMove.getAttackedPiece();
        }

        @Override
        public String toString() { return BoardUtils.getPositionAtCoordinate(destinationCoordinate) + "=" +this.promotedPiece.toString().charAt(0); }

        @Override
        public int hashCode() { return Objects.hash(decoratedMove.hashCode(), 31 * promotedPawn.hashCode()); }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof PawnPromotion && (super.equals(object)); }
    }

    public static final class PawnJump extends Move {

        public PawnJump(final Board board, final Piece movePiece, final int destinationCoordinate) {
            super(board, movePiece, destinationCoordinate);
        }

        @Override
        public Board execute() {
            final Pawn movedPawn = (Pawn)this.movePiece.movedPiece(this);

            final Builder builder = new Builder(this.board.getMoveCount() + 1, this.board.currentPlayer().getOpponent().getLeague(), movedPawn);

            this.board.currentPlayer().getActivePieces().stream().filter(piece -> !this.movePiece.equals(piece)).forEach(builder::setPiece);
            this.board.currentPlayer().getOpponent().getActivePieces().forEach(builder::setPiece);

            builder.setPiece(movedPawn);
            return builder.build();
        }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(destinationCoordinate);
        }
    }

    private static abstract class CastleMove extends Move {

        protected final Rook castleRook;

        protected final int castleRookStart;

        protected final int castleRookDestination;

        public CastleMove(final Board board, final Piece movePiece, final int destinationCoordinate,
                          final Rook castleRook, final int castleRookStart, final int castleRookDestination) {
            super(board, movePiece, destinationCoordinate);
            this.castleRook = castleRook;
            this.castleRookStart = castleRookStart;
            this.castleRookDestination = castleRookDestination;
        }

        public Rook getCastleRook() {
            return this.castleRook;
        }

        @Override
        public boolean isCastlingMove() {
            return true;
        }

        @Override
        public Board execute() {

            final Builder builder = new Builder(this.board.getMoveCount() + 1, this.board.currentPlayer().getOpponent().getLeague(), null);

            for (final Piece piece : this.board.getAllPieces()) {
                if (!this.movePiece.equals(piece) && !this.castleRook.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            builder.setPiece(this.movePiece.movedPiece(this));
            builder.setPiece(new Rook(this.castleRook.getLeague(), this.castleRookDestination, false));
            return builder.build();
        }

        @Override
        public int hashCode() { return Objects.hash(super.hashCode(), this.castleRook.hashCode(), this.castleRookDestination); }

        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof CastleMove)) {
                return false;
            }
            final CastleMove castleMove = (CastleMove)object;
            return super.equals(castleMove) && this.castleRook.equals(castleMove.getCastleRook());
        }
    }

    public static final class KingSideCastleMove extends CastleMove {

        public KingSideCastleMove(final Board board, final Piece movePiece, final int destinationCoordinate,
                                  final Rook castleRook, final int castleRookStart, final int castleRookDestination) {
            super(board, movePiece, destinationCoordinate, castleRook, castleRookStart, castleRookDestination);
        }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof KingSideCastleMove && super.equals(object); }

        @Override
        public String toString() {
            return "O-O";
        }
    }

    public static final class QueenSideCastleMove extends CastleMove {

        public QueenSideCastleMove(final Board board, final Piece movePiece, final int destinationCoordinate,
                                   final Rook castleRook, final int castleRookStart, final int castleRookDestination) {
            super(board, movePiece, destinationCoordinate, castleRook, castleRookStart, castleRookDestination);
        }

        @Override
        public boolean equals(final Object object) { return this == object || object instanceof QueenSideCastleMove && super.equals(object); }

        @Override
        public String toString() {
            return "O-O-O";
        }
    }

    public static final class NullMove extends Move {
        public NullMove() {
            super(null, 65);
        }

        @Override
        public Board execute() {
            return null;
        }

        @Override
        public int getCurrentCoordinate() {
            return -1;
        }
    }

    public static final class MoveFactory {

        private MoveFactory() {
            throw new RuntimeException ("Not instantiatable");
        }

        public static Move createMove(final Board board, final Piece piece, final int currentCoordinate, final int destinationCoordinate) {
            for (final Move move : piece.calculateLegalMoves(board)) {
                if (move.getCurrentCoordinate() == currentCoordinate && move.getDestinationCoordinate() == destinationCoordinate) {
                    return move;
                }
            }
            return new NullMove();
        }
    }
}