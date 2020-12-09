package chess.gui;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

import java.awt.Dimension;

public class ProgressBar extends JPanel{

    private final JProgressBar progressBar;
    private final Dimension dimension;
    private final JFrame gameFrame, progressFrame;

    public ProgressBar(final JFrame gameFrame) {
        // initialize Progress Bar
        this.progressBar = new JProgressBar();
        this.dimension = new Dimension(300, 50);
        this.gameFrame = gameFrame;
        this.progressFrame = new JFrame("AI is thinking");
        // add to JPanel
        this.add(progressBar);
    }

    public void showProgress() {
        this.progressFrame.setSize(this.dimension);
        this.progressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.progressFrame.setContentPane(this);
        this.progressFrame.setLocationRelativeTo(this.gameFrame);
        this.progressFrame.setResizable(false);
        this.progressFrame.setVisible(true);

        this.progressBar.setVisible(true);
        this.progressBar.setIndeterminate(true);
    }

    public void disposeFrame() {
        this.progressFrame.dispose();
    }
}
