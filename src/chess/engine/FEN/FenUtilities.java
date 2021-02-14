package chess.engine.FEN;

import chess.engine.League;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.MoveLog;
import chess.engine.pieces.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static chess.engine.board.Board.*;

public class FenUtilities {

    private FenUtilities() {
        throw new RuntimeException("Non instantiable");
    }

    public static final File file = new File(System.getProperty("user.home") + System.getProperty("file.separator") + "chess_data.txt");

    public static void writeMoveToFiles(final MoveLog moveLog) {
        try {
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
            objectOutputStream.writeObject(moveLog);
            objectOutputStream.close();
        } catch (final IOException e) { e.printStackTrace(); }
    }

    public static MoveLog readFile() {
        try {
            final ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
            final MoveLog moveLog = (MoveLog)objectInputStream.readObject();
            objectInputStream.close();
            return moveLog;
        } catch (final IOException | ClassNotFoundException e) {
            throw new RuntimeException("error");
        }
    }

    private static boolean kingSideCastle(final String fenCastleString, final boolean isWhite) { return isWhite ? fenCastleString.contains("K") : fenCastleString.contains("k"); }

    private static boolean queenSideCastle(final String fenCastleString, final boolean isWhite) { return isWhite ? fenCastleString.contains("Q") : fenCastleString.contains("q"); }

    private static Pawn getEnPassantPawn(final League league, final String fenEnPassantCoordinate) {
        if (!"-".equals(fenEnPassantCoordinate)) {
            final int enPassantPawnPosition = BoardUtils.getCoordinateAtPosition(fenEnPassantCoordinate) - (8) * league.getDirection();
            return new Pawn(league.isBlack() ? League.WHITE : League.BLACK, enPassantPawnPosition);
        }
        return null;
    }

    public static Board parseFEN(final String fenString) {
        final String[] fenPartitions = fenString.trim().split(" ");

        final League playerLeague = getLeague(fenPartitions[1]);

        final Builder builder = new Builder(Integer.parseInt(fenPartitions[fenPartitions.length - 1]), playerLeague, getEnPassantPawn(playerLeague, fenPartitions[3]));

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
                    throw new RuntimeException("Invalid FEN String " + gameConfiguration);
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
}