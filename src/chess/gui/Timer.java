package chess.gui;
import javax.swing.*;
import java.awt.*;

/**
 * Class that represents a Timer
 */
public class Timer extends JFrame implements Runnable {

    private static Thread thread;
    private Table obj;
    private JLabel time;
    protected JFrame timeFrame;
    protected int seconds;


    /**
     * Setting up Timer
     * @param obj Need to have parent class inorder to start/close the Timer
     * @param seconds How many seconds is the Timer going to be
     * @param player For which player
     */
    public Timer(Table obj, int seconds, String player) {
        this.seconds = seconds;
        this.obj = obj;
        time = new JLabel();
        time.setText("Time");
        time.setFont(time.getFont().deriveFont(35.0f));

        timeFrame = new JFrame();
        timeFrame.setLayout(new FlowLayout());
        timeFrame.setSize(250, 150);
        timeFrame.setResizable(false);
        timeFrame.setVisible(true);
        timeFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        if (player.equals("white")) {
            timeFrame.setLocation(172,558);
            timeFrame.setTitle("White player");
        } else {
            timeFrame.setLocation(172, 160);
            timeFrame.setTitle("Black player");
        }

        timeFrame.add(time);

    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }


    @Override
    public void run() {
        /**
         * Start Timer
         */
        for (; seconds >= 0; seconds--) {
            try {
                thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Error occurred....");
                return;
            }
            if (seconds < 30) {
                time.setForeground(Color.red);
            }
            time.setText(String.valueOf(seconds));
        }
        /**
         * close timer
         */
        obj.timeOver=true;
        obj.displayEndGameMessage();
        obj.w.timeFrame.dispose();
        obj.b.timeFrame.dispose();

    }


}