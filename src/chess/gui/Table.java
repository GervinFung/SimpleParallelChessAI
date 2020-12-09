package chess.gui;

import chess.engine.FEN.FenUtilities;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.board.Tile;
import chess.engine.pieces.Piece;
import chess.engine.player.MoveTransition;
import chess.engine.player.ArtificialIntelligence.MiniMax;

import javax.imageio.ImageIO;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.SwingWorker;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import static javax.swing.SwingUtilities.isLeftMouseButton;
import static chess.engine.board.Move.*;

public final class Table {

    private final BoardPanel boardPanel;
    private Board chessBoard;
    private final MoveLog moveLog;
    private final GameSetup gameSetup;

    private static boolean gameEnded;

    private boolean highlightLegalMoves, showAIThinking;
    private final GameHistoryPanel gameHistoryPanel;
    private final TakenPiecePanel takenPiecePanel;

    private int mousePressedX, mousePressedY;

    private Tile sourceTile;
    private Piece humanMovePiece;

    private static final Dimension OUTER_FRAME_DIMENSION = new Dimension(700, 600);
    private static final Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 350);
    private static final Dimension TILE_PANEL_DIMENSION = new Dimension(10, 10);

    private static final Table INSTANCE = new Table();

    private final PropertyChangeSupport propertyChangeSupport;
    private final JFrame gameFrame;

    private Table() {
        this.gameFrame = new JFrame("Simple Chess");
        gameFrame.setResizable(false);
        gameFrame.setLayout(new BorderLayout());
        this.chessBoard = Board.createStandardBoard();
        this.gameHistoryPanel = new GameHistoryPanel();
        this.takenPiecePanel = new TakenPiecePanel();
        this.boardPanel = new BoardPanel();
        this.moveLog = new MoveLog();
        this.gameSetup = new GameSetup(gameFrame, true);
        this.highlightLegalMoves = true;
        gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        final JMenuBar tableMenuBar = createTableMenuBar();
        gameFrame.add(this.takenPiecePanel, BorderLayout.WEST);
        gameFrame.add(this.gameHistoryPanel, BorderLayout.EAST);
        gameFrame.setJMenuBar(tableMenuBar);
        gameFrame.setSize(OUTER_FRAME_DIMENSION);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setResizable(false);
        gameFrame.setVisible(true);
        gameEnded = false;

        //a property change listener for AI, as Observable is deprecated
        PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
            if (Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().currentPlayer()) &&
                    !Table.get().getGameBoard().currentPlayer().isInCheckmate() &&
                    !Table.get().getGameBoard().currentPlayer().isInStalemate()) {
                final AIThinkTank thinkTank = new AIThinkTank();
                thinkTank.execute();
            }
            displayEndGameMessage();
        };

        this.propertyChangeSupport = new PropertyChangeSupport(propertyChangeListener);
        this.propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
    }


    private static void displayEndGameMessage() {
        if (Table.get().getGameBoard().currentPlayer().isInCheckmate()) {
            JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                    "Game Over: Player " + Table.get().getGameBoard().currentPlayer() + " is in checkmate!", "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
            gameEnded = true;
        }

        if (Table.get().getGameBoard().currentPlayer().isInStalemate()) {
            JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                    "Game Over: Player " + Table.get().getGameBoard().currentPlayer() + " is in checkmate!", "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
            gameEnded = true;
        }

        if (gameEnded) {
            JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                    "From Game Menu\n1. New Game to start a new game\n2. Exit Game to exit this game", "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        gameEnded = false;
    }


    public void show() {
        Table.get().getMoveLog().clear();
        Table.get().getGameHistoryPanel().redo(this.chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().boardPanel.drawBoard(Table.get().getGameBoard());
    }

    public static Table get() { return INSTANCE; }

    private GameSetup getGameSetup() { return this.gameSetup; }

    private Board getGameBoard() { return this.chessBoard; }

    private JFrame getGameFrame() { return this.gameFrame; }

    private static final class AIThinkTank extends SwingWorker<Move ,String> {

        private final ProgressBar bar;

        private AIThinkTank() {
            this.bar = new ProgressBar(Table.get().getGameFrame());
        }

        @Override
        protected Move doInBackground(){
            try {
                final MiniMax miniMax = new MiniMax(Table.get().getGameSetup().getSearchDepth());
                if (Table.get().showAIThinking) {
                    bar.showProgress();
                }
                //return best move
                return miniMax.execute(Table.get().getGameBoard());

            } catch (final Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void done() {
            if (Table.get().showAIThinking) {
                this.bar.disposeFrame();
            }
            try {
                final Move bestMove = this.get();
                Table.get().updateGameBoard(Table.get().getGameBoard().currentPlayer().makeMove(bestMove).getBoard());
                Table.get().getMoveLog().addMove(bestMove);
                Table.get().getGameHistoryPanel().redo(Table.get().getGameBoard(), Table.get().getMoveLog());
                Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
                Table.get().boardPanel.drawBoard(Table.get().getGameBoard());
                Table.get().moveMadeUpdate();

            } catch (final ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public final JPanel getBoardPanel() {
        return this.boardPanel;
    }

    private void moveMadeUpdate() {
        this.propertyChangeSupport.firePropertyChange("moveMadeUpdate", PlayerType.COMPUTER, PlayerType.HUMAN);
    }

    private void setupUpdate(final GameSetup gameSetup) {
        this.propertyChangeSupport.firePropertyChange("setupUpdate", null, gameSetup);
    }

    private MoveLog getMoveLog() {
        return this.moveLog;
    }

    private GameHistoryPanel getGameHistoryPanel() {
        return this.gameHistoryPanel;
    }

    private TakenPiecePanel getTakenPiecesPanel() {
        return this.takenPiecePanel;
    }

    private void updateGameBoard(final Board board) {
        this.chessBoard = board;
    }

    private JMenu createPreferencesMenu() {
        final JMenu preferenceMenu = new JMenu("Preferences");

        preferenceMenu.addSeparator();

        final JCheckBoxMenuItem legalMoveHighlighterCheckBox = new JCheckBoxMenuItem("Highlight Legal Moves");
        final JCheckBoxMenuItem AIThinkingProgressBarCheckBox = new JCheckBoxMenuItem("Show AI thinking");

        legalMoveHighlighterCheckBox.setState(true);
        AIThinkingProgressBarCheckBox.setState(true);

        legalMoveHighlighterCheckBox.addActionListener(actionEvent -> this.highlightLegalMoves = legalMoveHighlighterCheckBox.isSelected());
        AIThinkingProgressBarCheckBox.addActionListener(actionEvent -> this.showAIThinking = AIThinkingProgressBarCheckBox.isSelected());

        preferenceMenu.add(legalMoveHighlighterCheckBox);
        preferenceMenu.add(AIThinkingProgressBarCheckBox);

        return preferenceMenu;
    }

    private JMenu createOptionMenu() {

        final JMenu optionMenu = new JMenu("Options");

        final JMenuItem setupGameMenuItem = new JMenuItem("Setup Game");
        setupGameMenuItem.addActionListener(actionEvent -> {
            Table.get().getGameSetup().promptUser();
            Table.get().setupUpdate(Table.get().getGameSetup());
        });
        optionMenu.add(setupGameMenuItem);
        return optionMenu;
    }

    private JMenuBar createTableMenuBar() {
        final JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferencesMenu());
        tableMenuBar.add(createOptionMenu());
        return tableMenuBar;
    }

    private void restartGame() {
        this.chessBoard = Board.createStandardBoard();
        gameHistoryPanel.redo(chessBoard, moveLog);
        this.show();
    }

    private JMenu createFileMenu() {
        final JMenu gameMenu = new JMenu("Game");

        final JMenuItem newGameMenuItem = new JMenuItem("New Game");
        newGameMenuItem.addActionListener(actionEvent -> {
            gameEnded = false;
            restartGame();
        });

        final JMenuItem saveGameMenuItem = new JMenuItem("Save Game");
        saveGameMenuItem.addActionListener(actionEvent -> FenUtilities.writeFENToFile(this.chessBoard));


        final JMenuItem loadGameMenuItem = new JMenuItem("Load Saved Game");
        loadGameMenuItem.addActionListener(e -> {
            chessBoard = FenUtilities.createGameFromFEN();
            Table.get().boardPanel.drawBoard(chessBoard);
            if (chessBoard.currentPlayer().getLeague().isBlack()) {
                JOptionPane.showMessageDialog(this.boardPanel, "Black to move","Welcome",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this.boardPanel, "White to move","Welcome",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });


        final JMenuItem exitMenuItem = new JMenuItem("Exit game");
        exitMenuItem.addActionListener(actionEvent -> {
            final int confirmedExit = JOptionPane.showConfirmDialog(this.boardPanel, "Are you sure you want to quit game without saving?","Exit Game",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (confirmedExit == JOptionPane.YES_OPTION) {
                System.exit(0);
            } else if (confirmedExit == JOptionPane.NO_OPTION) {
                FenUtilities.writeFENToFile(this.chessBoard);
            }
        });

        gameMenu.add(newGameMenuItem);
        gameMenu.add(saveGameMenuItem);
        gameMenu.add(loadGameMenuItem);
        gameMenu.add(exitMenuItem);

        return gameMenu;
    }

    private final class BoardPanel extends JPanel {

        final List<TilePanel> boardTiles;

        private BoardPanel() {
            super(new GridLayout(8, 8));
            this.boardTiles = new ArrayList<>();
            for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);
            }
            setPreferredSize(BOARD_PANEL_DIMENSION);
            validate();
        }

        public List<TilePanel> getBoardTiles() {
            return Collections.unmodifiableList(this.boardTiles);
        }

        public void drawBoard(final Board board) {
            this.removeAll();
            for (final TilePanel tilePanel : boardTiles) {
                tilePanel.drawTile(board);
                this.add(tilePanel);
            }
            validate();
            repaint();
        }
    }

    public static final class MoveLog {

        private final List<Move> moves;

        public MoveLog() {
            this.moves = new ArrayList<>();
        }

        public List<Move> getMoves() {
            return this.moves;
        }

        public void addMove(final Move move) {
            this.moves.add(move);
        }

        public int size() {
            return this.moves.size();
        }

        public void clear() {
            this.moves.clear();
        }
    }

    enum PlayerType {
        HUMAN, COMPUTER
    }

    private final class TilePanel extends JPanel {

        private final int tileID;

        public TilePanel(final BoardPanel boardPanel, final int tileID) {
            super(new GridBagLayout());
            this.tileID = tileID;
            this.setPreferredSize(TILE_PANEL_DIMENSION);
            this.validate();

            this.addMouseMotionListener(new MouseMotionListener() {
                @Deprecated
                public void mouseMoved(final MouseEvent mouseEvent) {}

                @Override
                public void mouseDragged(final MouseEvent mouseEvent) {}
            });

            this.addMouseListener(new MouseListener() {
                @Deprecated
                public void mouseClicked(final MouseEvent mouseEvent) {}
                @Deprecated
                public void mouseEntered(final MouseEvent mouseEvent) {}
                @Deprecated
                public void mouseExited(final MouseEvent mouseEvent) {}

                @Override
                public void mousePressed(final MouseEvent mouseEvent) {
                    if (isLeftMouseButton(mouseEvent) && !gameEnded) {
                        mousePressedY = (int) Math.ceil(mouseEvent.getY() / 64.0);
                        mousePressedX = (int) Math.ceil(mouseEvent.getX() / 64.0);
                        if (sourceTile == null) {
                            sourceTile = chessBoard.getTile(tileID);
                            humanMovePiece = sourceTile.getPiece();
                            highlightLegals(chessBoard);
                        }
                    }
                }

                @Override
                public void mouseReleased(final MouseEvent mouseEvent) {
                    if (isLeftMouseButton(mouseEvent) && !gameEnded) {
                        final int releasedX = (int) Math.ceil(mouseEvent.getX() / 64.0) - mousePressedX;
                        final int releasedY = ((int) Math.ceil(mouseEvent.getY() / 64.0) - mousePressedY) * 8;
                        if (humanMovePiece != null && humanMovePiece.getLeague() == chessBoard.currentPlayer().getLeague()) {
                            final Move move = MoveFactory.createMove(chessBoard, humanMovePiece, sourceTile.getTileCoordinate(), tileID + (releasedY + releasedX));
                            final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                            if (transition.getMoveStatus().isDone()) {
                                chessBoard = transition.getBoard();
                                if (move instanceof PawnPromotion) {
                                    //display pawn promotion interface
                                    final PawnPromotion promoteInterface = (PawnPromotion)move;
                                    chessBoard = promoteInterface.promotePawn(chessBoard);
                                }
                                moveLog.addMove(move);
                            }
                            boardPanel.drawBoard(chessBoard);
                            SwingUtilities.invokeLater(() -> {
                                gameHistoryPanel.redo(chessBoard, moveLog);
                                takenPiecePanel.redo(moveLog);
                                if (!gameSetup.isAIPlayer(chessBoard.currentPlayer())) {
                                    displayEndGameMessage();
                                }
                                else if (gameSetup.isAIPlayer(chessBoard.currentPlayer())) {
                                    Table.get().moveMadeUpdate();
                                }
                            });
                        }
                        sourceTile = null;
                        humanMovePiece = null;
                    }
                }
            });
        }

        public void drawTile(final Board board) {
            this.assignTileColor();
            if (board.currentPlayer().isInCheck()) {
                this.highlightKingCheck(board.currentPlayer().getPlayerKing().getPiecePosition());
            }
            this.assignTilePieceIcon(board);
            this.validate();
            this.repaint();
        }

        private void assignTilePieceIcon(final Board board) {
            this.removeAll();
            if (board.getTile(this.tileID).isTileOccupied()) {
                final File imageFile = new File("image/chessPieceImages/");
                final String absolutePieceIconPath = imageFile.getAbsolutePath() + "/";
                final String alliance = board.getTile(this.tileID).getPiece().getLeague().toString().substring(0, 1);
                final String pieceName = board.getTile(this.tileID).getPiece().toString() + ".png";
                try {
                    final BufferedImage image = ImageIO.read(new File(absolutePieceIconPath + alliance + pieceName));
                    this.add(new JLabel(new ImageIcon(image)));

                } catch (final IOException e) {
                    System.err.println("Image path is invalid");
                }
            }
        }

        private void highlightKingCheck(final int kingCoordinate) {
            if (kingCoordinate == this.tileID) {
                this.setBackground(Color.RED);
            }
        }

        private void highlightLegals(final Board board) {

            if (highlightLegalMoves) {

                final List<Move> legalMoves = new ArrayList<>(pieceLegalMoves(board));

                for (final Move move : legalMoves) {

                    final int coordinate = move.getDestinationCoordinate();

                    final Color lightTileColor = Color.lightGray;
                    final Color darkTileColor = new Color(100 ,100 ,93);
                    Color tileColor;
                    if (move instanceof MajorAttackMove || move instanceof PawnAttackMove) {
                        boardPanel.getBoardTiles().get(coordinate).setBackground(new Color(204, 0, 0));
                    }
                    else {
                        if (BoardUtils.FIRST_ROW[coordinate] ||
                                BoardUtils.THIRD_ROW[coordinate] ||
                                BoardUtils.FIFTH_ROW[coordinate] ||
                                BoardUtils.SEVENTH_ROW[coordinate]) {
                            tileColor = (coordinate % 2 == 0 ? lightTileColor : darkTileColor);
                        } else {
                            tileColor = (coordinate % 2 != 0 ? lightTileColor : darkTileColor);
                        }
                        boardPanel.getBoardTiles().get(coordinate).setBackground(tileColor);
                    }
                }
            }
        }

        private Collection<Move> pieceLegalMoves(final Board board) {
            if (humanMovePiece != null && humanMovePiece.getLeague() == board.currentPlayer().getLeague()){
                return humanMovePiece.calculateLegalMoves(board);
            }
            return Collections.emptyList();
        }

        private void assignTileColor() {
            final Color lightTileColor = Color.WHITE;
            final Color darkTileColor = Color.decode("#1D3D63");//Color.LIGHT_GRAY;
            if (BoardUtils.FIRST_ROW[this.tileID] ||
                    BoardUtils.THIRD_ROW[this.tileID] ||
                    BoardUtils.FIFTH_ROW[this.tileID] ||
                    BoardUtils.SEVENTH_ROW[this.tileID]) {
                this.setBackground(this.tileID % 2 == 0 ? lightTileColor : darkTileColor);
            }
            else {
                this.setBackground(this.tileID % 2 != 0 ? lightTileColor : darkTileColor);
            }
        }
    }
}