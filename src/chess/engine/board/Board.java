package chess.engine.board;

import chess.engine.League;
import chess.engine.pieces.*;
import chess.engine.player.BlackPlayer;
import chess.engine.player.Player;
import chess.engine.player.WhitePlayer;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public final class Board{

    private final List<Tile> gameBoard;
    private final Collection<Piece> whitePieces, blackPieces;

    private final WhitePlayer whitePlayer;
    private final BlackPlayer blackPlayer;
    private final Player currentPlayer;

    private final Pawn enPassantPawn;
    private final int moveCount;

    private Board(final Builder builder) {
        this.gameBoard = createGameBoard(builder);
        this.whitePieces = calculateActivePieces(this.gameBoard, League.WHITE);
        this.blackPieces = calculateActivePieces(this.gameBoard, League.BLACK);

        this.enPassantPawn = builder.enPassantPawn;
        final Collection<Move> whiteStandardLegalMoves = this.calculateLegalMoves(this.whitePieces);
        final Collection<Move> blackStandardLegalMoves = this.calculateLegalMoves(this.blackPieces);

        this.whitePlayer = new WhitePlayer(this, whiteStandardLegalMoves, blackStandardLegalMoves);
        this.blackPlayer = new BlackPlayer(this, whiteStandardLegalMoves, blackStandardLegalMoves);

        this.currentPlayer = builder.nextMoveMaker.choosePlayer(this.whitePlayer, this.blackPlayer);

        this.moveCount = builder.moveCount();
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

    public Collection<Piece> getAllPieces() {
        final List <Piece> allPieces = new ArrayList<>(this.whitePieces);
        allPieces.addAll(this.blackPieces);
        return allPieces;
    }

    public Pawn getEnPassantPawn() {
        return this.enPassantPawn;
    }

    private Collection<Move> calculateLegalMoves(final Collection<Piece> pieces) {
        final List<Move> legalMoves = new ArrayList<>();

        for (final Piece piece: pieces) {
            legalMoves.addAll(piece.calculateLegalMoves(this));
        }
        return Collections.unmodifiableList(legalMoves);
    }

    private static Collection<Piece> calculateActivePieces(final List<Tile> gameBoard, final League COLOR) {

        final List<Piece> activePieces = new ArrayList<>();

        for (final Tile tile: gameBoard) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if (piece.getLeague() == COLOR) {
                    activePieces.add(piece);
                }
            }
        }
        return Collections.unmodifiableList(activePieces);
    }


    public Tile getTile(final int tileCoordinate) {
        return gameBoard.get(tileCoordinate);
    }

    public static List<Tile> createGameBoard(final Builder builder) {
        final Tile[] tiles = new Tile[BoardUtils.NUM_TILES];

        for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
            tiles[i] = Tile.createTile(i, builder.boardConfig.get(i));
        }
        //PARSE array as list
        return List.of(tiles);
    }

    public static Board createStandardBoard() {
        final Builder builder = new Builder(0);
        // Black Layout

        builder.setPiece(new Rook(League.BLACK, 0));
        builder.setPiece(new Knight(League.BLACK, 1));
        builder.setPiece(new Bishop(League.BLACK, 2));
        builder.setPiece(new Queen(League.BLACK, 3));
        builder.setPiece(new King(League.BLACK, 4, true, true));
        builder.setPiece(new Bishop(League.BLACK, 5));
        builder.setPiece(new Knight(League.BLACK, 6));
        builder.setPiece(new Rook(League.BLACK, 7));
        for (int i = 8; i < 16; i++) {
            builder.setPiece(new Pawn(League.BLACK, i));
        }
        // White Layout
        for (int i = 48; i < 56; i++) {
            builder.setPiece(new Pawn(League.WHITE, i));
        }
        builder.setPiece(new Rook(League.WHITE, 56));
        builder.setPiece(new Knight(League.WHITE, 57));
        builder.setPiece(new Bishop(League.WHITE, 58));
        builder.setPiece(new Queen(League.WHITE, 59));
        builder.setPiece(new King(League.WHITE, 60,true, true));
        builder.setPiece(new Bishop(League.WHITE, 61));
        builder.setPiece(new Knight(League.WHITE, 62));
        builder.setPiece(new Rook(League.WHITE, 63));
        //white to move
        builder.setMoveMaker(League.WHITE);
        //build the board
        return builder.build();
    }

    public static class Builder {

        private final HashMap<Integer, Piece> boardConfig;
        private League nextMoveMaker;
        private Pawn enPassantPawn;
        private final int moveCount;

        public Builder(final int moveCount) {
            this.boardConfig = new HashMap<>();
            this.moveCount = moveCount;
        }

        public Builder setPiece(final Piece piece) {
            this.boardConfig.put(piece.getPiecePosition(), piece);
            return this;
        }

        public void setMoveMaker(final League nextMoveMaker) {
            this.nextMoveMaker = nextMoveMaker;
        }

        public Board build() {
            return new Board(this);
        }

        public void setEnPassantPawn(final Pawn movedPawn) {
            this.enPassantPawn = movedPawn;
        }

        public int moveCount() { return this.moveCount; }
    }
}