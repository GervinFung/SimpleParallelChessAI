package chess.engine.board;

import chess.engine.League;
import chess.engine.pieces.*;
import chess.engine.player.BlackPlayer;
import chess.engine.player.Player;
import chess.engine.player.WhitePlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static chess.engine.board.Move.MoveFactory;

public final class Board{

    private final List<Tile> gameBoard;
    private final Collection<Piece> whitePieces, blackPieces;

    private final WhitePlayer whitePlayer;
    private final BlackPlayer blackPlayer;
    private final Player currentPlayer;

    private final Pawn enPassantPawn;
    private final int moveCount;

    private final Move transitionMove;

    private Board(final Builder builder) {
        this.gameBoard = createGameBoard(builder);
        this.whitePieces = calculateActivePieces(builder, League.WHITE);
        this.blackPieces = calculateActivePieces(builder, League.BLACK);

        this.enPassantPawn = builder.enPassantPawn;
        final Collection<Move> whiteStandardLegalMoves = this.calculateLegalMoves(this.whitePieces);
        final Collection<Move> blackStandardLegalMoves = this.calculateLegalMoves(this.blackPieces);

        this.whitePlayer = new WhitePlayer(this, whiteStandardLegalMoves, blackStandardLegalMoves);
        this.blackPlayer = new BlackPlayer(this, whiteStandardLegalMoves, blackStandardLegalMoves);

        this.currentPlayer = builder.nextMoveMaker.choosePlayer(this.whitePlayer, this.blackPlayer);

        this.moveCount = builder.moveCount();
        this.transitionMove = builder.transitionMove != null ? builder.transitionMove : MoveFactory.getNullMove();
    }

    public int getMoveCount() { return this.moveCount; }

    public Player currentPlayer() {
        return this.currentPlayer;
    }

    public Player whitePlayer() {
        return this.whitePlayer;
    }

    public Player blackPlayer() {
        return this.blackPlayer;
    }

    public Collection<Piece> getWhitePieces() {
        return this.whitePieces;
    }

    public Collection<Piece> getBlackPieces() { return this.blackPieces; }

    public Collection<Piece> getAllPieces() { return Stream.concat(this.whitePieces.stream(), this.blackPieces.stream()).collect(Collectors.toUnmodifiableList()); }

    public Pawn getEnPassantPawn() {
        return this.enPassantPawn;
    }

    private Collection<Move> calculateLegalMoves(final Collection<Piece> pieces) { return pieces.stream().flatMap(piece -> piece.calculateLegalMoves(this).stream()).collect(Collectors.toUnmodifiableList()); }

    private static Collection<Piece> calculateActivePieces(final Builder builder, final League league) { return builder.boardConfig.values().stream().filter(piece -> piece.getLeague() == league).collect(Collectors.toUnmodifiableList()); }


    public Tile getTile(final int tileCoordinate) {
        return gameBoard.get(tileCoordinate);
    }

    public Move getTransitionMove() { return this.transitionMove; }

    public static List<Tile> createGameBoard(final Builder builder) {
        final Tile[] tiles = new Tile[BoardUtils.NUM_TILES];

        for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
            tiles[i] = Tile.createTile(i, builder.boardConfig.get(i));
        }
        //PARSE array as list
        return List.of(tiles);
    }

    public static Board createStandardBoard() {
        //white to move
        final Builder builder = new Builder(0, League.WHITE, null);
        // Black Layout

        builder.setPiece(new Rook(League.BLACK, 0))
        .setPiece(new Knight(League.BLACK, 1))
        .setPiece(new Bishop(League.BLACK, 2))
        .setPiece(new Queen(League.BLACK, 3))
        .setPiece(new King(League.BLACK, 4, true, true))
        .setPiece(new Bishop(League.BLACK, 5))
        .setPiece(new Knight(League.BLACK, 6))
        .setPiece(new Rook(League.BLACK, 7));
        for (int i = 8; i < 16; i++) {
            builder.setPiece(new Pawn(League.BLACK, i));
        }
        // White Layout
        for (int i = 48; i < 56; i++) {
            builder.setPiece(new Pawn(League.WHITE, i));
        }
        builder.setPiece(new Rook(League.WHITE, 56))
        .setPiece(new Knight(League.WHITE, 57))
        .setPiece(new Bishop(League.WHITE, 58))
        .setPiece(new Queen(League.WHITE, 59))
        .setPiece(new King(League.WHITE, 60,true, true))
        .setPiece(new Bishop(League.WHITE, 61))
        .setPiece(new Knight(League.WHITE, 62))
        .setPiece(new Rook(League.WHITE, 63));
        //build the board
        return builder.build();
    }

    public static final class Builder {

        private final HashMap<Integer, Piece> boardConfig;
        private final League nextMoveMaker;
        private final Pawn enPassantPawn;
        private final int moveCount;
        private Move transitionMove;

        public Builder(final int moveCount, final League nextMoveMaker, final Pawn enPassantPawn) {
            //set initialCapacity to 32 and loadFactor to 1 to reduce chance of hash collision
            this.boardConfig = new HashMap<>(32, 1);
            this.nextMoveMaker = nextMoveMaker;
            this.moveCount = moveCount;
            this.enPassantPawn = enPassantPawn;
        }

        public Builder setPiece(final Piece piece) {
            this.boardConfig.put(piece.getPiecePosition(), piece);
            return this;
        }

        public Board build() { return new Board(this); }

        public void setTransitionMove(final Move transitionMove) { this.transitionMove = transitionMove; }

        public int moveCount() { return this.moveCount; }
    }
}