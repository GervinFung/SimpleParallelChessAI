package chess.gui;

import chess.engine.board.BoardUtils;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpinnerModel;

import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Container;

public final class TimerSetup extends JDialog {

    private final JSpinner minuteSpinner, secondSpinner;
    private boolean changeTimer;

    public TimerSetup (final JFrame frame, final boolean modal) {
        super(frame, modal);
        final JPanel myPanel = new JPanel(new GridLayout(0, 1));

        this.minuteSpinner = addTimeSpinner(myPanel, "Select Minute(s)", new SpinnerNumberModel(BoardUtils.DEFAULT_TIMER_MINUTE, 0, 60, 1));
        this.secondSpinner = addTimeSpinner(myPanel, "Select Second(s)", new SpinnerNumberModel(BoardUtils.DEFAULT_TIMER_SECOND, 0, 60, 1));

        this.getContentPane().add(myPanel);

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(Color.lightGray);
        final JButton okButton = new JButton("OK");
        okButton.setBackground(Color.WHITE);

        okButton.addActionListener(e -> {
            this.changeTimer = true;
            this.dispose();
        });

        cancelButton.addActionListener(e -> {
            this.changeTimer = false;
            this.dispose();
        });

        myPanel.add(okButton);
        myPanel.add(cancelButton);

        this.setLocationRelativeTo(frame);
        this.pack();
        this.setVisible(false);
        this.setResizable(false);
    }

    public void promptUser() {
        this.setVisible(true);
        this.repaint();
    }

    private static JSpinner addTimeSpinner(final Container c, final String parameter, final SpinnerModel model) {
        final JLabel time_selector = new JLabel(parameter);
        c.add(time_selector);
        final JSpinner spinner = new JSpinner(model);
        time_selector.setLabelFor(spinner);
        c.add(spinner);
        return spinner;
    }

    protected boolean changeTimer() { return this.changeTimer; }

    protected int getMinute() { return (Integer)this.minuteSpinner.getValue(); }

    protected int getSecond() { return (Integer)this.secondSpinner.getValue(); }
}
