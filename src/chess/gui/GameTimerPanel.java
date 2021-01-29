package chess.gui;

import chess.engine.League;
import chess.engine.player.Player;

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

    public GameTimerPanel(final Table table, final Player whitePlayer, final Player blackPlayer) {
        this.timerThread = new Thread(this);
        this.setPreferredSize(new Dimension(720, 80));
        this.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        this.setLayout(new GridLayout(0, 2));

        this.eastTimer = new TimerPanel(Color.WHITE, blackPlayer);
        this.westTimer = new TimerPanel(Color.BLACK, whitePlayer);

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
        if (isGameEnd() && !terminate) { this.table.displayEndGameMessage(); }
    }

    private static final class TimerPanel extends JPanel {

        private final JLabel label;
        private final Player player;

        private TimerPanel(final Color color, final Player player) {
            super(new GridLayout(2, 0));

            final JLabel title = new JLabel(player.toString());
            title.setForeground(color);
            this.add(title, BorderLayout.NORTH);

            this.player = player;
            this.label = new JLabel(this.getTimeFormat());
            this.label.setForeground(color);
            this.add(this.label, BorderLayout.SOUTH);
        }

        private boolean isGameEnd() { return this.player.isTimeOut(); }

        private void updateTimer() {
            this.player.countDown();
            this.label.setText(this.getTimeFormat());
        }

        private String getTimeFormat() {
            if (this.player.getSecond() / 10 == 0) {
                return this.player.getMinute() + " : 0" + this.player.getSecond();
            }
            return this.player.getMinute() + " : " + this.player.getSecond();
        }
    }
}
