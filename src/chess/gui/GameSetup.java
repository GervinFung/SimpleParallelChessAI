package chess.gui;

import chess.engine.League;
import chess.engine.player.Player;
import chess.gui.Table.PlayerType;

import javax.swing.JDialog;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpinnerModel;

import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Container;

public final class GameSetup extends JDialog {

    private PlayerType whitePlayerType;
    private PlayerType blackPlayerType;
    private final JSpinner searchDepthSpinner;

    private static final String HUMAN_TEXT = "Human";
    private static final String COMPUTER_TEXT = "Computer";

    public GameSetup(final JFrame frame, final boolean modal) {
        super(frame, modal);
        final JPanel myPanel = new JPanel(new GridLayout(0, 1));
        final JRadioButton whiteHumanButton = new JRadioButton(HUMAN_TEXT);
        final JRadioButton whiteComputerButton = new JRadioButton(COMPUTER_TEXT);
        final JRadioButton blackHumanButton = new JRadioButton(HUMAN_TEXT);
        final JRadioButton blackComputerButton = new JRadioButton(COMPUTER_TEXT);
        whiteHumanButton.setActionCommand(HUMAN_TEXT);
        final ButtonGroup whiteGroup = new ButtonGroup();
        whiteGroup.add(whiteHumanButton);
        whiteGroup.add(whiteComputerButton);
        whiteHumanButton.setSelected(true);

        final ButtonGroup blackGroup = new ButtonGroup();
        blackGroup.add(blackHumanButton);
        blackGroup.add(blackComputerButton);
        blackHumanButton.setSelected(true);

        getContentPane().add(myPanel);
        myPanel.add(new JLabel("White"));
        myPanel.add(whiteHumanButton);
        myPanel.add(whiteComputerButton);
        myPanel.add(new JLabel("Black"));
        myPanel.add(blackHumanButton);
        myPanel.add(blackComputerButton);

        this.searchDepthSpinner = addLabeledSpinner(myPanel, new SpinnerNumberModel(1, 1, 5, 1));

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(Color.lightGray);
        final JButton okButton = new JButton("OK");
        okButton.setBackground(Color.WHITE);

        okButton.addActionListener(e -> {
            this.whitePlayerType = whiteComputerButton.isSelected() ? PlayerType.COMPUTER : PlayerType.HUMAN;
            this.blackPlayerType = blackComputerButton.isSelected() ? PlayerType.COMPUTER : PlayerType.HUMAN;
            GameSetup.this.setVisible(false);
        });

        cancelButton.addActionListener(e -> {
            System.out.println("Cancel");
            GameSetup.this.setVisible(false);
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

    boolean isAIPlayer(final Player player) {
        if(player.getLeague() == League.WHITE) {
            return getWhitePlayerType() == PlayerType.COMPUTER;
        }
        return getBlackPlayerType() == PlayerType.COMPUTER;
    }

    PlayerType getWhitePlayerType() {
        return this.whitePlayerType;
    }

    PlayerType getBlackPlayerType() {
        return this.blackPlayerType;
    }

    private static JSpinner addLabeledSpinner(final Container c,
                                              final SpinnerModel model) {
        final JLabel l = new JLabel("Select Level");
        c.add(l);
        final JSpinner spinner = new JSpinner(model);
        l.setLabelFor(spinner);
        c.add(spinner);
        return spinner;
    }

    int getSearchDepth() {
        return (Integer)this.searchDepthSpinner.getValue();
    }
}