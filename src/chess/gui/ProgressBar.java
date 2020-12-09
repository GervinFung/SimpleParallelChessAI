package chess.gui;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JDialog;
import javax.swing.JPanel;

import java.awt.Dimension;

public class ProgressBar extends JDialog{

    private final JProgressBar progressBar;
    private final Dimension dimension;
    private final JFrame gameFrame;

    public ProgressBar(final JFrame gameFrame) {
        // initialize Progress Bar
        this.progressBar = new JProgressBar();
        this.dimension = new Dimension(300, 60);
        this.gameFrame = gameFrame;
        // add to JDialog
        this.add(progressBar);
    }

    public void showProgress() {
        final JPanel panel = new JPanel();
        this.setTitle("AI thinking");
        this.setSize(this.dimension);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setLocationRelativeTo(this.gameFrame);
        this.setResizable(false);
        this.setVisible(true);
        this.getContentPane().add(panel);
        panel.add(this.progressBar);
        this.progressBar.setVisible(true);
        this.progressBar.setIndeterminate(true);

        this.setVisible(true);
        this.validate();
        this.repaint();
    }

    public void disposeFrame() {
        this.dispose();
    }
}
