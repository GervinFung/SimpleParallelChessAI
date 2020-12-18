package chess.engine.FEN;

import chess.engine.League;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.pieces.*;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;

import static chess.engine.board.Board.*;

public class FenUtilities {

    private FenUtilities() {
        throw new RuntimeException("Non instantiable");
    }

    private static String createFENFromFile() {
        final File FEN_file = new File("src/chess/engine/FEN/chessGame.fen");
        try {
            return new BufferedReader(new FileReader(FEN_file.getAbsolutePath())).readLine();
        }
        catch (final IOException ignored) {}
        throw new RuntimeException("Path for FEN file is invalid");
    }

    public static void writeFENToFile(final Board board) {
        try {
            final File FEN_file = new File("src/chess/engine/FEN/chessGame.fen");
            final FileWriter myWriter = new FileWriter(FEN_file.getAbsolutePath());
            myWriter.write(createFENFromGame(board));
            myWriter.close();
        } catch (final IOException ignored) {}
    }

    public static Board createGameFromFEN() {
        return parseFEN(createFENFromFile());
    }

    private static String createFENFromGame(final Board board) {
        return calculateBoardText(board) + " " +
                calculateCurrentPlayerText(board) + " " +
                calculateCastleText(board) + " " +
                calculateEnPassantText(board) + " " +
                "0 " + board.getMoveCount();
    }

    private static String calculateBoardText(final Board board) {
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
            final String tileText = board.getTile(i).toString();
            builder.append(tileText);
        }
        builder.insert(8, "/");
        builder.insert(17, "/");
        builder.insert(26, "/");
        builder.insert(35, "/");
        builder.insert(44, "/");
        builder.insert(53, "/");
        builder.insert(62, "/");

        return builder.toString()
                .replaceAll("--------", "8")
                .replaceAll("-------", "7")
                .replaceAll("------", "6")
                .replaceAll("-----", "5")
                .replaceAll("----", "4")
                .replaceAll("---", "3")
                .replaceAll("--", "2")
                .replaceAll("-", "1");

    }

    private static boolean whiteKingSideCastle(final String fenCastleString) {
        return fenCastleString.contains("K");
    }

    private static boolean whiteQueenSideCastle(final String fenCastleString) {
        return fenCastleString.contains("Q");
    }

    private static boolean blackKingSideCastle(final String fenCastleString) {
        return fenCastleString.contains("k");
    }

    private static boolean blackQueenSideCastle(final String fenCastleString) {
        return fenCastleString.contains("q");
    }

    private static boolean enPassantPawnExist(final String fenEnPassantCoordinate) {
        return !"-".equals(fenEnPassantCoordinate);
    }

    private static Board parseFEN(final String fenString) {
        final String[] fenPartitions = fenString.trim().split(" ");

        final Builder builder = new Builder(Integer.parseInt(fenPartitions[fenPartitions.length - 1]));

        final boolean whiteKingSideCastle = whiteKingSideCastle(fenPartitions[2]);
        final boolean whiteQueenSideCastle = whiteQueenSideCastle(fenPartitions[2]);
        final boolean blackKingSideCastle = blackKingSideCastle(fenPartitions[2]);
        final boolean blackQueenSideCastle = blackQueenSideCastle(fenPartitions[2]);

        if (enPassantPawnExist(fenPartitions[3])) {
            final int enPassantPawnPosition = Integer.parseInt(fenPartitions[3].substring(0, 2));
            final String league = Character.toString(fenPartitions[3].charAt(2));
            builder.setEnPassantPawn(new Pawn(getLeague(league), enPassantPawnPosition));
        }

        final String gameConfiguration = fenPartitions[0];
        final char[] boardTiles = gameConfiguration.replaceAll("/", "")
                .replaceAll("8", "--------")
                .replaceAll("7", "-------")
                .replaceAll("6", "------")
                .replaceAll("5", "-----")
                .replaceAll("4", "----")
                .replaceAll("3", "---")
                .replaceAll("2", "--")
                .replaceAll("1", "-")
                .toCharArray();
        int i = 0;
        while (i < boardTiles.length) {
            switch (boardTiles[i]) {
                case 'r':
                    builder.setPiece(new Rook(League.BLACK, i));
                    i++;
                    break;
                case 'n':
                    builder.setPiece(new Knight(League.BLACK, i));
                    i++;
                    break;
                case 'b':
                    builder.setPiece(new Bishop(League.BLACK, i));
                    i++;
                    break;
                case 'q':
                    builder.setPiece(new Queen(League.BLACK, i));
                    i++;
                    break;
                case 'k':
                    builder.setPiece(new King(League.BLACK, i, blackKingSideCastle, blackQueenSideCastle));
                    i++;
                    break;
                case 'p':
                    builder.setPiece(new Pawn(League.BLACK, i));
                    i++;
                    break;
                case 'R':
                    builder.setPiece(new Rook(League.WHITE, i));
                    i++;
                    break;
                case 'N':
                    builder.setPiece(new Knight(League.WHITE, i));
                    i++;
                    break;
                case 'B':
                    builder.setPiece(new Bishop(League.WHITE, i));
                    i++;
                    break;
                case 'Q':
                    builder.setPiece(new Queen(League.WHITE, i));
                    i++;
                    break;
                case 'K':
                    builder.setPiece(new King(League.WHITE, i, whiteKingSideCastle, whiteQueenSideCastle));
                    i++;
                    break;
                case 'P':
                    builder.setPiece(new Pawn(League.WHITE, i));
                    i++;
                    break;
                case '-':
                    i++;
                    break;
                default:
                    throw new RuntimeException("Invalid FEN String " +gameConfiguration);
            }
        }
        builder.setMoveMaker(getLeague(fenPartitions[1]));
        return builder.build();
    }
    private static League getLeague(final String moveMakerString) {
        if("w".equals(moveMakerString)) {
            return League.WHITE;
        } else if("b".equals(moveMakerString)) {
            return League.BLACK;
        }
        throw new RuntimeException("Invalid FEN String " + moveMakerString);
    }

    private static String calculateEnPassantText(final Board board) {

        final Pawn enPassantPawn = board.getEnPassantPawn();

        if (enPassantPawn != null) {
            final String league = enPassantPawn.getLeague().isWhite() ? "w" : "b";
            return enPassantPawn.getPiecePosition() + league;
        }
        return "-";
    }

    private static String calculateCurrentPlayerText(final Board board) {
        return board.currentPlayer().toString().substring(0, 1).toLowerCase();
    }

    private static String calculateCastleText(final Board board) {
        final StringBuilder builder = new StringBuilder();

        if (board.whitePlayer().isKingSideCastleCapable()) {
            builder.append("K");
        }
        if (board.whitePlayer().isQueenSideCastleCapable()) {
            builder.append("Q");
        }

        if (board.blackPlayer().isKingSideCastleCapable()) {
            builder.append("k");
        }
        if (board.blackPlayer().isQueenSideCastleCapable()) {
            builder.append("q");
        }

        final String result = builder.toString();

        return result.isEmpty() ? "-" : result;
    }
}