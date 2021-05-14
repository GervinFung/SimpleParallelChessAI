package chess.gui.Multiplayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

// This class represents a waiting room
public class WaitingRoom extends JFrame {


    public WaitingRoom(MultiServerThread multiServerThread) {

        // Setting up the JFrame
        setSize(400, 200);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle("Waiting Room");
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

        // Setting up the JLabel text
        JLabel text = new JLabel("Please wait for an opponent");
        text.setFont(text.getFont().deriveFont(20.0f));
        text.setAlignmentX(CENTER_ALIGNMENT);

        // Adding elements to the frame
        add(Box.createRigidArea(new Dimension(0, 25)));
        add(text);
        add(Box.createRigidArea(new Dimension(0, 25)));

        // Listener used when the player decides to exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                // Confirmation text to exit
                final int option = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to exit multiplayer ?",
                        "Close Window?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                // If the answer is yes
                if (option == JOptionPane.YES_OPTION) {
                    try {
                        // exit player
                        multiServerThread.exit(true);
                        // close waiting frame
                        dispose();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }
        });

        setVisible(true);
    }

}