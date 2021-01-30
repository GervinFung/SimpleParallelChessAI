package chess.gui;

import chess.engine.League;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

public final class GameTimerPanel extends JPanel implements Runnable{

    private final TimerPanel eastTimer, westTimer;
    private final Thread timerThread;
    private final Table table;

    private volatile boolean includeTimer, terminate, resumeEnabled;

    public GameTimerPanel(final Table table) {
        this.timerThread = new Thread(this);
        this.setPreferredSize(new Dimension(720, 80));
        this.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        this.setLayout(new GridLayout(0, 2));

        this.eastTimer = new TimerPanel(League.BLACK, table);
        this.westTimer = new TimerPanel(League.WHITE, table);

        this.table = table;

        this.eastTimer.setBackground(Color.BLACK);
        this.westTimer.setBackground(Color.WHITE);

        this.add(this.eastTimer, BorderLayout.EAST);
        this.add(this.westTimer, BorderLayout.WEST);

        this.includeTimer = true;
        this.terminate = false;
        this.resumeEnabled = true;
    }

    protected Thread getTimer() { return this.timerThread; }

    private boolean isGameEnd() { return (this.westTimer.isGameEnd() || this.eastTimer.isGameEnd()); }

    private void update(final League league) {
        if (league.isBlack()) {
            this.eastTimer.updateTimer();
            return;
        }
        this.westTimer.updateTimer();
    }

    protected void setIncludeTimer(final boolean includeTimer) {
        synchronized(this) {
            this.includeTimer = includeTimer;
            this.notify();
        }
    }

    protected void setResumeEnabled(final boolean resumeEnabled) {
        synchronized(this) {
            this.resumeEnabled = resumeEnabled;
            this.notify();
        }
    }

    protected void setTerminateTimer(final boolean terminate) { this.terminate = terminate; }

    @Override
    public void run() {
        long start = System.nanoTime();
        while (!isGameEnd() && !this.terminate) {
            synchronized (this) {
                try {
                    if (!this.resumeEnabled) {
                        this.wait();
                    }
                    else if (!this.includeTimer) {
                        this.eastTimer.setVisible(false);
                        this.westTimer.setVisible(false);
                        this.wait();
                    } else {
                        this.eastTimer.setVisible(true);
                        this.westTimer.setVisible(true);
                        if (((System.nanoTime() - start) / 1000000000) == 1) {
                            this.update(this.table.getGameBoard().currentPlayer().getLeague());
                            start = System.nanoTime();
                        } else if (((System.nanoTime() - start) / 1000000000) > 1) {
                            start = System.nanoTime();
                        }
                    }
                } catch (final InterruptedException e) { e.printStackTrace(); }
            }
        }
        if (isGameEnd() && !this.terminate && this.table.getGameSetup().isAIPlayer(this.table.getGameBoard().currentPlayer())) { this.table.displayEndGameMessage(); }
    }

    private static final class TimerPanel extends JPanel {

        private final JLabel label;
        private final Table table;

        //pass table parameter to obtain the latest player state, as player is renewed after each move
        private TimerPanel(final League league, final Table table) {
            super(new GridLayout(2, 0));
            this.table = table;
            final Color color = league.isBlack() ? Color.WHITE : Color.BLACK;
            final JLabel title = new JLabel(league.toString());
            title.setForeground(color);
            this.add(title, BorderLayout.NORTH);

            this.label = new JLabel(this.getTimeFormat());
            this.label.setForeground(color);
            this.add(this.label, BorderLayout.SOUTH);
        }

        private boolean isGameEnd() { return table.getGameBoard().currentPlayer().isTimeOut(); }

        private void updateTimer() {
            table.getGameBoard().currentPlayer().countDown();
            this.label.setText(this.getTimeFormat());
        }

        private String getTimeFormat() {
            if (table.getGameBoard().currentPlayer().getSecond() / 10 == 0) {
                return table.getGameBoard().currentPlayer().getMinute() + " : 0" + table.getGameBoard().currentPlayer().getSecond();
            }
            return table.getGameBoard().currentPlayer().getMinute() + " : " + table.getGameBoard().currentPlayer().getSecond();
        }
    }
}
