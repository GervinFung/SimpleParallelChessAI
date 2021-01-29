package chess.gui;

import chess.engine.FEN.FenUtilities;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.pieces.Piece;
import chess.engine.board.MoveTransition;
import chess.engine.player.ArtificialIntelligence.MiniMax;

import javax.imageio.ImageIO;

import java.awt.Dimension;
import java.awt.Cursor;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JProgressBar;
import javax.swing.JDialog;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;
import static javax.swing.SwingUtilities.isLeftMouseButton;
import static chess.engine.board.Move.*;
import static javax.swing.SwingUtilities.isRightMouseButton;

public final class Table {

    private final BoardPanel boardPanel;
    private Board chessBoard;
    private Move computerMove, humanMove;
    private final MoveLog moveLog;
    private final GameSetup gameSetup;
    private final TimerSetup timerSetup;

    private boolean highlightLegalMoves, showAIThinking, AIThinking, gameEnded, includeTimer;
    private boolean mouseEnteredHighlightMoves, showHumanMove, showAIMove, mouseClicked;
    private final GameHistoryPanel gameHistoryPanel;
    private final TakenPiecePanel takenPiecePanel;

    private int mousePressedX, mousePressedY, mouseClickedID;

    private Piece humanMovePiece;

    private static final Dimension OUTER_FRAME_DIMENSION = new Dimension(720, 700);
    private static final Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 400);
    private static final Dimension TILE_PANEL_DIMENSION = new Dimension(10, 10);

    private static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
    private static final Cursor MOVE_CURSOR = new Cursor(Cursor.MOVE_CURSOR);

    private final PropertyChangeSupport gameSetupPropertyChangeSupport, timerSetupPropertyChangeSupport;
    private final JFrame gameFrame;

    private Color darkTileColor, lightTileColor;
    private Color legalMovesLightTileColor, legalMovesDarkTileColor;
    private BoardDirection boardDirection;

    private GameTimerPanel gameTimerPanel;

    private Table() {
        this.gameFrame = new JFrame("Simple Chess");
        this.gameFrame.setResizable(false);
        this.gameFrame.setLayout(new BorderLayout());
        this.chessBoard = Board.createStandardBoard(BoardUtils.DEFAULT_TIMER_MINUTE, BoardUtils.DEFAULT_TIMER_SECOND);
        this.gameHistoryPanel = new GameHistoryPanel();
        this.takenPiecePanel = new TakenPiecePanel();
        this.gameTimerPanel = new GameTimerPanel(this, this.chessBoard.whitePlayer(), this.chessBoard.blackPlayer());
        this.boardPanel = new BoardPanel();
        this.moveLog = new MoveLog();
        this.gameSetup = new GameSetup(this.gameFrame, true);
        this.timerSetup = new TimerSetup(this.gameFrame, true);
        this.highlightLegalMoves = true;
        this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        final JMenuBar tableMenuBar = createTableMenuBar();
        this.gameFrame.add(this.gameTimerPanel, BorderLayout.SOUTH);
        this.gameFrame.add(this.takenPiecePanel, BorderLayout.EAST);
        this.gameFrame.add(this.gameHistoryPanel, BorderLayout.WEST);
        this.gameFrame.setJMenuBar(tableMenuBar);
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.gameFrame.setLocationRelativeTo(null);
        this.gameFrame.setResizable(false);
        this.gameFrame.setVisible(true);
        this.AIThinking = false;
        this.showAIThinking = true;
        this.showAIMove = true;
        this.showHumanMove = true;
        this.mouseEnteredHighlightMoves = true;
        this.mouseClicked = false;
        this.gameEnded = false;
        this.includeTimer = true;
        this.gameFrame.setCursor(MOVE_CURSOR);
        this.boardDirection = BoardDirection.NORMAL;
        //a property change listener for AI, as Observable is deprecated
        final PropertyChangeListener gameSetupPropertyChangeListener = propertyChangeEvent -> {
            if (Table.this.getGameSetup().isAIPlayer(Table.this.getGameBoard().currentPlayer()) &&
                    !Table.this.getGameBoard().currentPlayer().isInCheckmate() &&
                    !Table.this.getGameBoard().currentPlayer().isInStalemate()) {
                new AIThinkTank(this).execute();
            }
            Table.this.displayEndGameMessage();
        };

        //a property change listener for Timer, as Observable is deprecated
        final PropertyChangeListener timerSetupPropertyChangeListener = propertyChangeEvent -> {
            if (Table.this.isIncludeTimer() && Table.this.getTimerSetup().changeTimer()) {
                final int minute = Table.this.getTimerSetup().getMinute();
                final int second = Table.this.getTimerSetup().getSecond();
                final Board.Builder builder = new Board.Builder(this.getGameBoard().getMoveCount(), this.getGameBoard().currentPlayer().getLeague(), this.getGameBoard().getEnPassantPawn())
                                            .updateWhiteTimer(minute, second).updateBlackTimer(minute, second);
                this.getGameBoard().getAllPieces().forEach(builder::setPiece);
                this.updateGameBoard(builder.build());
                this.reInitTimerPanel();
            }
        };

        this.gameSetupPropertyChangeSupport = new PropertyChangeSupport(gameSetupPropertyChangeListener);
        this.gameSetupPropertyChangeSupport.addPropertyChangeListener(gameSetupPropertyChangeListener);

        this.timerSetupPropertyChangeSupport = new PropertyChangeSupport(timerSetupPropertyChangeListener);
        this.timerSetupPropertyChangeSupport.addPropertyChangeListener(timerSetupPropertyChangeListener);

        this.legalMovesLightTileColor = new Color(169, 169, 169);
        this.legalMovesDarkTileColor = new Color(105, 105, 105);

        this.lightTileColor = new Color(240, 217, 181);
        this.darkTileColor = new Color(181, 136, 99);
    }

    //setter
    private void setAIThinking(final boolean AIThinking) { this.AIThinking = AIThinking; }

    private void setMouseEnteredHighlightMoves(final boolean mouseEnteredHighlightMoves) { this.mouseEnteredHighlightMoves = mouseEnteredHighlightMoves; }

    private void setShowAIThinking(final boolean showAIThinking) { this.showAIThinking = showAIThinking; }

    private void enableHighLightMoves(final boolean highlightLegalMoves) { this.highlightLegalMoves = highlightLegalMoves; }

    private void updateGameBoard(final Board board) { this.chessBoard = board; }

    private void startCountDownTimer() { this.gameTimerPanel.getTimer().start(); }

    private void setBoardDirection(final BoardDirection boardDirection) { this.boardDirection = boardDirection; }

    private void updateComputerMove(final Move move) { this.computerMove = move; }

    private void updateHumanMove(final Move move) { this.humanMove = move; }

    private void setShowHumanMove(final boolean showHumanMove) {
        this.showHumanMove = showHumanMove;
        this.getBoardPanel().drawBoard(this.chessBoard);
    }

    private void setShowAIMove(final boolean showAIMove) {
        this.showAIMove = showAIMove;
        this.getBoardPanel().drawBoard(this.chessBoard);
    }

    private void renewTimerPanel(final GameTimerPanel gameTimerPanel) { this.gameTimerPanel = gameTimerPanel; }

    private void includeTimer(final boolean renewTimer) { this.includeTimer = renewTimer; }

    //getter
    private boolean getAIThinking() { return this.AIThinking; }

    private boolean getMouseEnteredHighlightMoves() { return this.mouseEnteredHighlightMoves; }

    private boolean getShowAIThinking() { return this.showAIThinking; }

    private boolean getHighLightMovesEnabled() { return this.highlightLegalMoves; }

    private boolean isGameEnded() { return this.gameEnded; }

    private GameSetup getGameSetup() { return this.gameSetup; }

    private TimerSetup getTimerSetup() { return this.timerSetup; }

    protected Board getGameBoard() { return this.chessBoard; }

    private JFrame getGameFrame() { return this.gameFrame; }

    private BoardPanel getBoardPanel() { return this.boardPanel; }

    private MoveLog getMoveLog() { return this.moveLog; }

    private GameHistoryPanel getGameHistoryPanel() { return this.gameHistoryPanel; }

    private TakenPiecePanel getTakenPiecesPanel() { return this.takenPiecePanel; }

    private BoardDirection getBoardDirection() { return this.boardDirection; }

    private boolean getShowHumanMove() { return this.showHumanMove; }

    private boolean getShowAIMove() { return this.showAIMove; }

    private GameTimerPanel getGameTimerPanel() { return this.gameTimerPanel; }

    private boolean isIncludeTimer() { return this.includeTimer; }

    //singleton
    public static Table getSingletonInstance() { return SingleTon.INSTANCE; }

    private static final class SingleTon { private static final Table INSTANCE = new Table();}

    protected void displayEndGameMessage() {
        if (this.getGameBoard().currentPlayer().isInCheckmate()) {
            JOptionPane.showMessageDialog(this.getBoardPanel(), "Game Over: Player " + this.getGameBoard().currentPlayer() + " is in checkmate!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            this.gameEnded = true;
        }

        if (this.getGameBoard().currentPlayer().isInStalemate()) {
            JOptionPane.showMessageDialog(this.getBoardPanel(), "Game Over: Player " + this.getGameBoard().currentPlayer() + " is in stalemate!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            this.gameEnded = true;
        }

        if (this.getGameBoard().currentPlayer().isTimeOut()) {
            JOptionPane.showMessageDialog(this.getBoardPanel(), "Game Over: Player " + this.getGameBoard().currentPlayer() + " is time out!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            this.gameEnded = true;
        }

        if (this.gameEnded) {
            JOptionPane.showMessageDialog(this.getBoardPanel(), "From Preference\n 1. Set Timer in Setup Game Menu\n From Game Menu\n1. New Game to start a new game\n2. Exit Game to exit this game", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void start() {
        this.getMoveLog().clear();
        this.getGameHistoryPanel().redo(this.getGameBoard(), this.getMoveLog());
        this.getTakenPiecesPanel().redo(this.getMoveLog());
        this.getBoardPanel().drawBoard(this.getGameBoard());
        JOptionPane.showConfirmDialog(this.gameFrame, "1. Left press and release to move the piece\n2. Right click to move the piece from one tile to another");
        this.updateGameBoard(this.getGameBoard());
        this.startCountDownTimer();
    }

    private static final class AIThinkTank extends SwingWorker<Move, Integer> {

        private final JDialog dialog;
        private final JProgressBar bar;
        private final int max;
        private final Table table;
        private final boolean showProgressbar;

        private AIThinkTank(final Table table) {
            this.table = table;
            this.showProgressbar = this.table.getShowAIThinking();
            if (this.showProgressbar) {
                this.dialog = new JDialog();
                this.dialog.setTitle("AI Thinking...");
                this.bar = new JProgressBar(0, 100);
                this.bar.setStringPainted(true);
                this.bar.setForeground(new Color(50, 205, 50));
                this.dialog.add(this.bar);
                this.dialog.setSize(300, 60);
                this.dialog.setLocationRelativeTo(this.table.getGameFrame());
                this.dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                this.dialog.setVisible(true);
                this.dialog.setResizable(false);
                this.max = this.table.getGameBoard().currentPlayer().getLegalMoves().size();
            } else {
                this.dialog = null;
                this.bar = null;
                this.max = 0;
            }
            this.table.setAIThinking(true);
        }

        @Override
        protected void process(final List<Integer> chunks) { this.bar.setValue(chunks.get(chunks.size() - 1)); }

        @Override
        protected Move doInBackground(){
            try {
                final AtomicBoolean running = new AtomicBoolean(true);
                final MiniMax miniMax = new MiniMax(this.table.getGameSetup().getSearchDepth());
                if (this.showProgressbar) {

                    //progress is shown based on move count / total available moves ratio
                    new Thread(() -> {
                        AIThinkTank.this.table.getBoardPanel().updateBoardPanelCursor(Table.WAIT_CURSOR);
                        AIThinkTank.this.dialog.setCursor(Table.WAIT_CURSOR);
                        while(running.get()) {
                            final double progress = ((double)miniMax.getMoveCount() / AIThinkTank.this.max) * 100;
                            AIThinkTank.this.publish((int)progress);//publish the progress
                        }
                        //so 100% progress is shown
                        try {
                            sleep(100);
                            AIThinkTank.this.dialog.dispose();
                            AIThinkTank.this.table.getBoardPanel().updateBoardPanelCursor(Table.MOVE_CURSOR);
                        }
                        catch (final InterruptedException e) { e.printStackTrace(); }
                    }).start();
                }

                final Move bestMove = miniMax.execute(this.table.getGameBoard());
                //stop the loop in thread
                running.lazySet(false);
                return bestMove;

            } catch (final Exception e) { e.printStackTrace(); }
            return null;
        }

        @Override
        public void done() {
            try {
                final Move bestMove = this.get();
                this.table.updateComputerMove(bestMove);
                this.table.updateHumanMove(null);
                this.table.updateGameBoard(this.table.getGameBoard().currentPlayer().makeMove(bestMove).getLatestBoard());
                this.table.getMoveLog().addMove(bestMove);
                this.table.getGameHistoryPanel().redo(this.table.getGameBoard(), this.table.getMoveLog());
                this.table.getTakenPiecesPanel().redo(this.table.getMoveLog());
                this.table.getBoardPanel().drawBoard(this.table.getGameBoard());
                this.table.moveMadeUpdate();
                this.table.setAIThinking(false);
            } catch (final ExecutionException | InterruptedException e) { e.printStackTrace(); }
        }
    }

    private void moveMadeUpdate() { this.gameSetupPropertyChangeSupport.firePropertyChange("moveMadeUpdate", PlayerType.COMPUTER, PlayerType.HUMAN); }

    private void setupUpdate() { this.gameSetupPropertyChangeSupport.firePropertyChange("setupUpdate", null, null); }

    private void timerSetupUpdate() { this.timerSetupPropertyChangeSupport.firePropertyChange("timerSetup", null, null); }

    private void undoLastMove() {
        final Move lastMove = this.getMoveLog().removeMove();
        this.updateGameBoard(this.getGameBoard().currentPlayer().undoMove(lastMove).getPreviousBoard());
        this.updateComputerMove(null);
        this.updateHumanMove(null);
        this.getGameHistoryPanel().redo(this.getGameBoard(), this.getMoveLog());
        this.getTakenPiecesPanel().redo(this.getMoveLog());
        this.getBoardPanel().drawBoard(this.getGameBoard());
    }

    private void undoAllMoves() {
        for(int i = this.getMoveLog().size() - 1; i >= 0; i--) {
            this.undoLastMove();
        }
    }

    private JMenu createPreferencesMenu() {
        final JMenu preferenceMenu = new JMenu("Preferences");

        preferenceMenu.addSeparator();

        final JCheckBoxMenuItem legalMoveHighlighterCheckBox = new JCheckBoxMenuItem("Highlight Legal Moves");
        final JCheckBoxMenuItem AIThinkingProgressBarCheckBox = new JCheckBoxMenuItem("Show AI thinking");
        final JCheckBoxMenuItem showHumanMoveCheckBox = new JCheckBoxMenuItem("Show Human Move");
        final JCheckBoxMenuItem showAIMoveCheckBox = new JCheckBoxMenuItem("Show AI Move");
        final JCheckBoxMenuItem includeTimerCheckBox = new JCheckBoxMenuItem("Include Timer");

        legalMoveHighlighterCheckBox.setState(true);
        AIThinkingProgressBarCheckBox.setState(true);
        showHumanMoveCheckBox.setState(true);
        showAIMoveCheckBox.setState(true);
        includeTimerCheckBox.setState(true);

        legalMoveHighlighterCheckBox.addActionListener(e -> Table.this.enableHighLightMoves(legalMoveHighlighterCheckBox.isSelected()));
        AIThinkingProgressBarCheckBox.addActionListener(e -> Table.this.setShowAIThinking(AIThinkingProgressBarCheckBox.isSelected()));
        showAIMoveCheckBox.addActionListener(e -> Table.this.setShowAIMove(showAIMoveCheckBox.isSelected()));
        showHumanMoveCheckBox.addActionListener(e -> Table.this.setShowHumanMove(showHumanMoveCheckBox.isSelected()));
        includeTimerCheckBox.addActionListener(e -> {
            Table.this.getGameTimerPanel().setIncludeTimer(includeTimerCheckBox.isSelected());
            Table.this.includeTimer(includeTimerCheckBox.isSelected());
        });

        preferenceMenu.add(legalMoveHighlighterCheckBox);
        preferenceMenu.add(AIThinkingProgressBarCheckBox);
        preferenceMenu.add(showHumanMoveCheckBox);
        preferenceMenu.add(showAIMoveCheckBox);
        preferenceMenu.add(includeTimerCheckBox);

        return preferenceMenu;
    }

    private JMenu createOptionMenu() {

        final JMenu optionMenu = new JMenu("Options");

        final JMenuItem setupGameMenuItem = new JMenuItem("Setup Game");
        final JMenuItem setupTimerMenuItem = new JMenuItem("Setup Timer");
        final JMenuItem undoMoveMenuItem = new JMenuItem("Undo last move");
        final JMenuItem flipBoardMenuItem = new JMenuItem("Flip board");

        setupGameMenuItem.addActionListener(e -> {
            Table.this.getGameSetup().promptUser();
            Table.this.setupUpdate();
        });
        setupTimerMenuItem.addActionListener(e -> {
            Table.this.getTimerSetup().promptUser();
            Table.this.timerSetupUpdate();
        });
        undoMoveMenuItem.addActionListener(e -> {
            if(Table.this.getMoveLog().size() > 0) { Table.this.undoLastMove(); }
        });
        flipBoardMenuItem.addActionListener(e -> {
            Table.this.setBoardDirection(Table.this.getBoardDirection().opposite());
            Table.this.getBoardPanel().drawBoard(Table.this.getGameBoard());
        });

        optionMenu.add(undoMoveMenuItem);
        optionMenu.add(setupTimerMenuItem);
        optionMenu.add(setupGameMenuItem);
        optionMenu.add(flipBoardMenuItem);

        return optionMenu;
    }

    private JMenuBar createTableMenuBar() {
        final JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferencesMenu());
        tableMenuBar.add(createOptionMenu());
        tableMenuBar.add(createBoardColorMenu());
        return tableMenuBar;
    }

    private void restartGame(final Board board) {
        this.getGameTimerPanel().setTerminateTimer(true);
        this.undoAllMoves();
        this.updateGameBoard(board);
        this.getBoardPanel().drawBoard(this.getGameBoard());
    }

    private final static class BoardColor implements ActionListener {

        private final Table table;
        private final Color darkTileColor, lightTileColor, legalMovesLightTileColor, legalMovesDarkTileColor;

        private BoardColor(final Table table, final Color darkTileColor, final Color lightTileColor, final Color legalMovesLightTileColor, final Color legalMovesDarkTileColor) {
            this.table = table;
            this.darkTileColor = darkTileColor;
            this.lightTileColor = lightTileColor;
            this.legalMovesLightTileColor = legalMovesLightTileColor;
            this.legalMovesDarkTileColor = legalMovesDarkTileColor;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            this.table.darkTileColor = this.darkTileColor;
            this.table.lightTileColor = this.lightTileColor;
            this.table.legalMovesLightTileColor = this.legalMovesLightTileColor;
            this.table.legalMovesDarkTileColor = this.legalMovesDarkTileColor;
            this.table.getBoardPanel().drawBoard(this.table.getGameBoard());
        }
    }

    private JMenu createBoardColorMenu() {
        final JMenu gameMenu = new JMenu("Board Color");

        final JMenuItem blueWhiteMenuItem = new JMenuItem("Blue & White");
        final JMenuItem bumbleBeeMenuItem = new JMenuItem("Bumblebee");
        final JMenuItem greyWhiteMenuItem = new JMenuItem("Grey & White");
        final JMenuItem classicMenuItem = new JMenuItem("Classic Board");
        final JMenuItem lightGreyWhiteMenuItem = new JMenuItem("Light Grey & White");
        final JMenuItem lightBlueWhiteMenuItem = new JMenuItem("Light Blue & White");

        blueWhiteMenuItem.addActionListener(new BoardColor(this, new Color(29 ,61 ,99), Color.white, new Color(169, 169, 169), new Color(105, 105, 105)));
        greyWhiteMenuItem.addActionListener(new BoardColor(this, new Color(105, 105, 105), Color.white, new Color(255, 253, 156), new Color(255, 252, 84)));
        classicMenuItem.addActionListener(new BoardColor(this, new Color(240, 217, 181), new Color(181, 136, 99), new Color(169, 169, 169), new Color(105, 105, 105)));
        lightGreyWhiteMenuItem.addActionListener(new BoardColor(this, new Color(177, 179, 179), Color.white, new Color(255, 253, 156), new Color(255, 252, 84)));
        lightBlueWhiteMenuItem.addActionListener(new BoardColor(this, new Color(137, 171, 227), Color.white, new Color(169, 169, 169), new Color(105, 105, 105)));
        bumbleBeeMenuItem.addActionListener(new BoardColor(this, new Color(64, 64, 64), new Color(254, 231, 21), new Color(169, 169, 169), new Color(105, 105, 105)));

        gameMenu.add(bumbleBeeMenuItem);
        gameMenu.add(blueWhiteMenuItem);
        gameMenu.add(classicMenuItem);
        gameMenu.add(lightGreyWhiteMenuItem);
        gameMenu.add(lightBlueWhiteMenuItem);
        gameMenu.add(greyWhiteMenuItem);

        return gameMenu;
    }

    private void reInitTimerPanel() {
        this.getGameTimerPanel().setTerminateTimer(false);
        this.getGameFrame().remove(Table.this.getGameTimerPanel());
        this.renewTimerPanel(new GameTimerPanel(this, Table.this.getGameBoard().whitePlayer(), Table.this.getGameBoard().blackPlayer()));
        this.getGameFrame().add(Table.this.getGameTimerPanel(), BorderLayout.SOUTH);
        this.startCountDownTimer();
    }

    private Board resumeLoadedGame(final Board board) {
        final Board.Builder builder = new Board.Builder(board.getMoveCount(), board.currentPlayer().getLeague(), board.getEnPassantPawn())
                                    .updateWhiteTimer(this.getTimerSetup().getMinute(), this.getTimerSetup().getSecond())
                                    .updateBlackTimer(this.getTimerSetup().getMinute(), this.getTimerSetup().getSecond());
        board.getAllPieces().forEach(builder::setPiece);
        return builder.build();
    }

    private JMenu createFileMenu() {
        final JMenu gameMenu = new JMenu("Game");

        final JMenuItem newGameMenuItem = new JMenuItem("New Game");
        newGameMenuItem.addActionListener(e -> {
            Table.this.AIThinking = false;
            if (!Table.this.isGameEnded()) {
                final int confirmedRestart = JOptionPane.showConfirmDialog(Table.this.getBoardPanel(), "Are you sure you want to restart game without saving?", "Restart Game", JOptionPane.YES_NO_CANCEL_OPTION);
                if (confirmedRestart == JOptionPane.YES_OPTION) {
                    Table.this.getGameTimerPanel().setResumeEnabled(false);
                    Table.this.restartGame(Board.createStandardBoard(BoardUtils.DEFAULT_TIMER_MINUTE, BoardUtils.DEFAULT_TIMER_SECOND));
                    Table.this.getGameTimerPanel().setResumeEnabled(true);
                    this.reInitTimerPanel();
                } else if (confirmedRestart == JOptionPane.NO_OPTION) {
                    Table.this.getGameTimerPanel().setResumeEnabled(false);
                    FenUtilities.writeFENToFile(Table.this.getGameBoard());
                    Table.this.restartGame(Board.createStandardBoard(BoardUtils.DEFAULT_TIMER_MINUTE, BoardUtils.DEFAULT_TIMER_SECOND));
                    Table.this.getGameTimerPanel().setResumeEnabled(true);
                    this.reInitTimerPanel();
                }
            }
        });

        final JMenuItem loadGameMenuItem = new JMenuItem("Load Saved Game");
        loadGameMenuItem.addActionListener(e -> {
            final int confirmLoadGame = JOptionPane.showConfirmDialog(Table.this.getBoardPanel(), "Confirm to load previous game?", "Load Game", JOptionPane.YES_NO_CANCEL_OPTION);

            if(confirmLoadGame == JOptionPane.YES_OPTION) {
                Table.this.getGameTimerPanel().setResumeEnabled(false);
                Table.this.restartGame(this.resumeLoadedGame(FenUtilities.createGameFromFEN()));
                if (Table.this.getGameBoard().currentPlayer().getLeague().isBlack()) {
                    JOptionPane.showConfirmDialog(Table.this.getBoardPanel(), "Black to move", "Welcome", JOptionPane.YES_NO_CANCEL_OPTION);
                } else {
                    JOptionPane.showConfirmDialog(Table.this.getBoardPanel(), "White to move", "Welcome", JOptionPane.YES_NO_CANCEL_OPTION);
                }
                Table.this.getGameTimerPanel().setResumeEnabled(true);
                this.reInitTimerPanel();
            }
        });

        final JMenuItem saveGameMenuItem = new JMenuItem("Save Game");
        saveGameMenuItem.addActionListener(e -> {
            final int confirmSave = JOptionPane.showConfirmDialog(Table.this.getBoardPanel(), "Confirm to save this game?", "Save Game", JOptionPane.YES_NO_CANCEL_OPTION);
            if (confirmSave == JOptionPane.YES_OPTION) {
                FenUtilities.writeFENToFile(Table.this.getGameBoard());
                JOptionPane.showMessageDialog(Table.this.getGameFrame(), "Game Saved!");
                loadGameMenuItem.setVisible(true);
            }
        });

        final JMenuItem exitMenuItem = new JMenuItem("Exit game");
        exitMenuItem.addActionListener(e -> {
            final int confirmedExit = JOptionPane.showConfirmDialog(Table.this.getBoardPanel(), "Are you sure you want to quit game without saving?","Exit Game", JOptionPane.YES_NO_CANCEL_OPTION);
            if (confirmedExit == JOptionPane.YES_OPTION) {
                System.exit(0);
            } else if (confirmedExit == JOptionPane.NO_OPTION) {
                FenUtilities.writeFENToFile(Table.this.getGameBoard());
                System.exit(0);
            }
        });

        if (!new File(new File(System.getProperty("user.dir") + File.separator + ".DO_NOT_DELETE.txt").getAbsolutePath()).exists()) { loadGameMenuItem.setVisible(false); }

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
            this.setPreferredSize(BOARD_PANEL_DIMENSION);
            this.validate();
        }

        public List<TilePanel> getBoardTiles() { return Collections.unmodifiableList(this.boardTiles); }
        public void drawBoard(final Board board) {
            this.removeAll();
            for (final TilePanel tilePanel : boardDirection.traverse(boardTiles)) {
                tilePanel.drawTile(board);
                this.add(tilePanel);
            }
            this.validate();
            this.repaint();
        }
        public void updateBoardPanelCursor(final Cursor cursor) { this.setCursor(cursor); }
    }

    public static final class MoveLog {

        private final List<Move> moves;

        public MoveLog() { this.moves = new ArrayList<>(); }

        public List<Move> getMoves() { return this.moves; }

        public void addMove(final Move move) { this.moves.add(move); }

        public int size() { return this.moves.size(); }

        public void clear() { this.moves.clear(); }

        public Move removeMove() { return this.moves.remove(this.moves.size() - 1); }
    }

    enum PlayerType {HUMAN, COMPUTER}

    enum BoardDirection {
        NORMAL {
            @Override
            List<TilePanel> traverse(final List<TilePanel> boardTiles) { return Collections.unmodifiableList(boardTiles); }

            @Override
            BoardDirection opposite() {
                return FLIPPED;
            }
        },
        FLIPPED {
            @Override
            List<TilePanel> traverse(final List<TilePanel> boardTiles) {
                final List<TilePanel> reversePanel = new ArrayList<>(boardTiles);
                Collections.reverse(reversePanel);
                return Collections.unmodifiableList(reversePanel);
            }

            @Override
            BoardDirection opposite() {
                return NORMAL;
            }
        };

        abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);
        abstract BoardDirection opposite();

    }

    private final class TilePanel extends JPanel {

        private final int tileID;
        private final BoardPanel boardPanel;
        private TilePanel(final BoardPanel boardPanel, final int tileID) {
            super(new GridBagLayout());
            this.tileID = tileID;
            this.boardPanel = boardPanel;
            this.setPreferredSize(TILE_PANEL_DIMENSION);
            this.validate();

            this.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(final MouseEvent mouseEvent) {
                    if (Table.this.isGameEnded() || Table.this.getAIThinking()) { return; }
                    if (isRightMouseButton(mouseEvent)) {
                        if (!Table.this.mouseClicked && Table.this.humanMovePiece != null && Table.this.humanMovePiece.getLeague() == Table.this.getGameBoard().currentPlayer().getLeague()) {
                            Table.this.setMouseEnteredHighlightMoves(false);
                            Table.this.mouseClickedID = TilePanel.this.tileID;
                            Table.this.humanMovePiece = Table.this.getGameBoard().getTile(TilePanel.this.tileID).getPiece();
                            final Image image = ((ImageIcon)((JLabel) TilePanel.this.boardPanel.getBoardTiles().get(TilePanel.this.tileID).getComponent(0)).getIcon()).getImage();
                            try {
                                TilePanel.this.boardPanel.updateBoardPanelCursor(Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(mouseEvent.getX(), mouseEvent.getY()), null));
                                TilePanel.this.boardPanel.getBoardTiles().get(TilePanel.this.tileID).remove(0);
                            } catch (final IndexOutOfBoundsException ignored) {}
                            TilePanel.this.validate();
                            TilePanel.this.repaint();
                            Table.this.mouseClicked = true;
                        } else if (Table.this.mouseClicked && Table.this.humanMovePiece != null && Table.this.humanMovePiece.getLeague() == Table.this.getGameBoard().currentPlayer().getLeague()) {
                            final Move move = MoveFactory.createMove(Table.this.getGameBoard(), Table.this.humanMovePiece, Table.this.mouseClickedID, TilePanel.this.tileID);
                            final MoveTransition transition = Table.this.getGameBoard().currentPlayer().makeMove(move);
                            if (transition.getMoveStatus().isDone()) {

                                Table.this.updateGameBoard(transition.getLatestBoard());
                                if (move instanceof PawnPromotion) {
                                    TilePanel.this.boardPanel.updateBoardPanelCursor(Table.MOVE_CURSOR);
                                    //display pawn promotion interface
                                    Table.this.updateGameBoard(((PawnPromotion)move).promotePawn(Table.this.getGameBoard()));
                                }
                                Table.this.getMoveLog().addMove(move);
                                Table.this.updateHumanMove(move);
                            }
                            TilePanel.this.boardPanel.drawBoard(Table.this.getGameBoard());
                            Table.this.mouseClicked = false;
                            SwingUtilities.invokeLater(() -> {
                                Table.this.getGameHistoryPanel().redo(Table.this.getGameBoard(), Table.this.getMoveLog());
                                Table.this.getTakenPiecesPanel().redo(Table.this.getMoveLog());

                                if (!Table.this.getGameSetup().isAIPlayer(Table.this.getGameBoard().currentPlayer())) {
                                    Table.this.displayEndGameMessage();
                                }
                                else if (Table.this.getGameSetup().isAIPlayer(Table.this.getGameBoard().currentPlayer())) {
                                    Table.this.moveMadeUpdate();
                                }
                            });
                            TilePanel.this.boardPanel.updateBoardPanelCursor(Table.MOVE_CURSOR);
                            Table.this.humanMovePiece = null;
                            Table.this.setMouseEnteredHighlightMoves(true);
                        }
                    }
                }
                @Override
                public void mouseEntered(final MouseEvent mouseEvent) {
                    if (!Table.this.getAIThinking() && Table.this.getMouseEnteredHighlightMoves()) {
                        Table.this.humanMovePiece = Table.this.getGameBoard().getTile(TilePanel.this.tileID).getPiece();
                        highlightLegals(Table.this.getGameBoard());
                    }
                }
                @Override
                public void mouseExited(final MouseEvent mouseEvent) {
                    if (!Table.this.getAIThinking() && Table.this.getMouseEnteredHighlightMoves() && Table.this.humanMovePiece != null) {
                        Table.this.humanMovePiece = null;
                        Table.this.getBoardPanel().drawBoard(Table.this.getGameBoard());
                    }
                }

                @Override
                public void mousePressed(final MouseEvent mouseEvent) {
                    if (Table.this.isGameEnded() || Table.this.getAIThinking()) { return; }
                    if (isLeftMouseButton(mouseEvent)) {
                        if (Table.this.humanMovePiece != null && Table.this.humanMovePiece.getLeague() == Table.this.getGameBoard().currentPlayer().getLeague()) {
                            Table.this.mousePressedY = (int) Math.ceil(mouseEvent.getY() / TilePanel.this.getSize().getHeight());
                            Table.this.mousePressedX = (int) Math.ceil(mouseEvent.getX() / TilePanel.this.getSize().getWidth());

                            Table.this.setMouseEnteredHighlightMoves(false);
                            Table.this.humanMovePiece = Table.this.getGameBoard().getTile(TilePanel.this.tileID).getPiece();
                            final Image image = ((ImageIcon)((JLabel) TilePanel.this.boardPanel.getBoardTiles().get(TilePanel.this.tileID).getComponent(0)).getIcon()).getImage();
                            try {
                                TilePanel.this.boardPanel.updateBoardPanelCursor(Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(mouseEvent.getX(), mouseEvent.getY()), null));
                                TilePanel.this.boardPanel.getBoardTiles().get(TilePanel.this.tileID).remove(0);
                            } catch (final IndexOutOfBoundsException ignored) {}
                            TilePanel.this.validate();
                            TilePanel.this.repaint();
                        }
                    }
                }

                @Override
                public void mouseReleased(final MouseEvent mouseEvent) {
                    if (!Table.this.getAIThinking() && isLeftMouseButton(mouseEvent)) {

                        if (Table.this.humanMovePiece != null && Table.this.humanMovePiece.getLeague() == Table.this.getGameBoard().currentPlayer().getLeague()) {

                            final int releasedX = (int) Math.ceil(mouseEvent.getX() / TilePanel.this.getSize().getHeight()) - Table.this.mousePressedX;
                            final int releasedY = ((int) Math.ceil(mouseEvent.getY() / TilePanel.this.getSize().getWidth()) - Table.this.mousePressedY) * 8;

                            final int direction = Table.this.getBoardDirection() == BoardDirection.NORMAL ? 1 : -1;
                            final Move move = MoveFactory.createMove(Table.this.getGameBoard(), Table.this.humanMovePiece, TilePanel.this.tileID, TilePanel.this.tileID + (direction)*(releasedY + releasedX));
                            final MoveTransition transition = Table.this.getGameBoard().currentPlayer().makeMove(move);
                            if (transition.getMoveStatus().isDone()) {
                                Table.this.updateGameBoard(transition.getLatestBoard());
                                if (move instanceof PawnPromotion) {
                                    TilePanel.this.boardPanel.updateBoardPanelCursor(Table.MOVE_CURSOR);
                                    //display pawn promotion interface
                                    Table.this.updateGameBoard(((PawnPromotion)move).promotePawn(Table.this.getGameBoard()));
                                }
                                Table.this.getMoveLog().addMove(move);
                                Table.this.updateHumanMove(move);
                            }
                            TilePanel.this.boardPanel.drawBoard(Table.this.getGameBoard());
                            SwingUtilities.invokeLater(() -> {
                                Table.this.getGameHistoryPanel().redo(Table.this.getGameBoard(), Table.this.getMoveLog());
                                Table.this.getTakenPiecesPanel().redo(Table.this.getMoveLog());

                                if (!Table.this.getGameSetup().isAIPlayer(Table.this.getGameBoard().currentPlayer())) {
                                    Table.this.displayEndGameMessage();
                                }
                                else if (Table.this.getGameSetup().isAIPlayer(Table.this.getGameBoard().currentPlayer())) {
                                    Table.this.moveMadeUpdate();
                                }
                            });
                        }
                        TilePanel.this.boardPanel.updateBoardPanelCursor(Table.MOVE_CURSOR);
                        Table.this.humanMovePiece = null;
                    }
                    Table.this.setMouseEnteredHighlightMoves(true);
                }
            });
        }

        private void drawTile(final Board board) {
            this.assignTileColor();
            if (board.currentPlayer().isInCheck()) {
                this.highlightKingCheck(board.currentPlayer().getPlayerKing().getPiecePosition());
            }
            this.highlightAIMove();
            this.highlightHumanMove();
            this.assignTilePieceIcon(board);
            this.validate();
            this.repaint();
        }

        private void assignTilePieceIcon(final Board board) {
            this.removeAll();
            if (board.getTile(this.tileID).isTileOccupied()) {
                try {
                    this.add(new JLabel(new ImageIcon(ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader().getResource(BoardUtils.imagePath(board.getTile(this.tileID).getPiece())))))));
                } catch (final IOException | NullPointerException e) { System.err.println("Invalid Path"); }
            }
        }

        private void highlightKingCheck(final int kingCoordinate) {
            if (kingCoordinate == this.tileID) {
                this.setBackground(Color.RED);
            }
        }

        private void highlightAIMove() {
            if(Table.this.computerMove != null && Table.this.getShowAIMove()) {
                if(this.tileID == Table.this.computerMove.getCurrentCoordinate()) {
                    this.setBackground(Color.PINK);
                } else if(this.tileID == Table.this.computerMove.getDestinationCoordinate()) {
                    this.setBackground(new Color(255, 51, 51));
                }
            }
        }

        private void highlightHumanMove() {
            if (Table.this.humanMove != null && Table.this.getShowHumanMove()) {
                if(this.tileID == Table.this.humanMove.getCurrentCoordinate()) {
                    this.setBackground(new Color(102, 255, 102));
                } else if(this.tileID == Table.this.humanMove.getDestinationCoordinate()) {
                    this.setBackground(new Color(50, 205, 50));
                }
            }
        }

        private void highlightLegals(final Board board) {

            if (Table.this.getHighLightMovesEnabled()) {

                final List<Move> legalMoves = new ArrayList<>(pieceLegalMoves(board));

                for (final Move move : legalMoves) {

                    final int coordinate = move.getDestinationCoordinate();
                    Color tileColor;
                    if (move.isAttack() || move instanceof PawnPromotion && ((PawnPromotion)move).getDecoratedMove().isAttack()) {
                        //dark red
                        this.boardPanel.getBoardTiles().get(coordinate).setBackground(new Color(204, 0, 0));
                    } else {
                        if (BoardUtils.FIRST_ROW.get(coordinate) || BoardUtils.THIRD_ROW.get(coordinate) || BoardUtils.FIFTH_ROW.get(coordinate) || BoardUtils.SEVENTH_ROW.get(coordinate)) {
                            tileColor = (coordinate % 2 == 0 ? Table.this.legalMovesLightTileColor : Table.this.legalMovesDarkTileColor);
                        } else {
                            tileColor = (coordinate % 2 != 0 ? Table.this.legalMovesLightTileColor : Table.this.legalMovesDarkTileColor);
                        }
                        this.boardPanel.getBoardTiles().get(coordinate).setBackground(tileColor);
                    }
                }
            }
        }

        private Collection<Move> pieceLegalMoves(final Board board) {
            if (Table.this.humanMovePiece != null && Table.this.humanMovePiece.getLeague() == board.currentPlayer().getLeague()){
                return Collections.unmodifiableCollection(Table.this.humanMovePiece.calculateLegalMoves(board));
            }
            return Collections.emptyList();
        }

        private void assignTileColor() {
            if (BoardUtils.FIRST_ROW.get(this.tileID) || BoardUtils.THIRD_ROW.get(this.tileID) || BoardUtils.FIFTH_ROW.get(this.tileID) || BoardUtils.SEVENTH_ROW.get(this.tileID)) {
                this.setBackground(this.tileID % 2 == 0 ? Table.this.lightTileColor : Table.this.darkTileColor);
            }
            else {
                this.setBackground(this.tileID % 2 != 0 ? Table.this.lightTileColor : Table.this.darkTileColor);
            }
        }
    }
}
