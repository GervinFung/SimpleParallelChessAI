package chess.engine.FEN;

import chess.engine.League;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.pieces.*;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

import java.nio.file.Files;
import java.nio.file.Path;

import static chess.engine.board.Board.*;

public class FenUtilities {

    private FenUtilities() {
        throw new RuntimeException("Non instantiable");
    }

    private static String createFENFromFile() {

        try {
            return Files.readString(Path.of(new File(System.getProperty("user.dir") + File.separator + "DO_NOT_DELETE.txt").toURI()));
        } catch (final NullPointerException | IOException ignored) { }
        throw new RuntimeException("Path for FEN file is invalid");
    }

    public static void writeFENToFile(final Board board) {
        try {
            final FileWriter myWriter = new FileWriter(new File(System.getProperty("user.dir") + File.separator + "DO_NOT_DELETE.txt").getAbsolutePath());
            myWriter.write(createFENFromGame(board));
            myWriter.close();
        } catch (final NullPointerException | IOException ignored) {}
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

    private static boolean kingSideCastle(final String fenCastleString, final boolean isWhite) { return isWhite ? fenCastleString.contains("K") : fenCastleString.contains("k"); }

    private static boolean queenSideCastle(final String fenCastleString, final boolean isWhite) { return isWhite ? fenCastleString.contains("Q") : fenCastleString.contains("q"); }

    private static boolean enPassantPawnExist(final String fenEnPassantCoordinate) { return !"-".equals(fenEnPassantCoordinate); }

    private static Pawn getEnPassantPawn(final String[] fenPartitions) {
        if (enPassantPawnExist(fenPartitions[3])) {
            final int enPassantPawnPosition = Integer.parseInt(fenPartitions[3].substring(0, 2));
            final String league = Character.toString(fenPartitions[3].charAt(2));
            return new Pawn(getLeague(league), enPassantPawnPosition);
        }
        return null;
    }

    private static Board parseFEN(final String fenString) {
        final String[] fenPartitions = fenString.trim().split(" ");

        final Builder builder = new Builder(Integer.parseInt(fenPartitions[fenPartitions.length - 1]), getLeague(fenPartitions[1]), getEnPassantPawn(fenPartitions));

        final boolean whiteKingSideCastle = kingSideCastle(fenPartitions[2], true);
        final boolean whiteQueenSideCastle = queenSideCastle(fenPartitions[2], true);
        final boolean blackKingSideCastle = kingSideCastle(fenPartitions[2], false);
        final boolean blackQueenSideCastle = queenSideCastle(fenPartitions[2], false);

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